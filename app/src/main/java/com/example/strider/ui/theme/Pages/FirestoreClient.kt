package com.example.strider.ui.theme.Pages

import DataClass.Player
import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import com.example.strider.PlayerManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import com.example.strider.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration

class FirestoreClient {
    private val tag = "FirestoreClient: "
    private val db = FirebaseFirestore.getInstance()
    private val collection = "rooms"

    fun insertRoom(room: Room): Flow<String?> {
        return callbackFlow {
            db.collection(collection)
                .document(room.code)
                .set(room.toHashMap())
                .addOnSuccessListener {
                    println(tag + "insert room with custom id (code): ${room.code}")
                    trySend(room.code)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    println(tag + "error inserting room: ${e.message}")
                    trySend(null)
                }

            awaitClose {}
        }
    }

    fun updateRoom(room: Room): Flow<Boolean> {
        return callbackFlow {
            db.collection(collection)
                .document(room.code)
                .set(room.toHashMap())
                .addOnSuccessListener {
                    println(tag + "update room with code: ${room.code}")
                    trySend(true)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    println(tag + "error updating room: ${e.message}")
                    trySend(false)
                }

            awaitClose {}
        }
    }

    fun getRoom(code: String): Flow<Room?> {
        return callbackFlow {
            db.collection(collection)
                .document(code)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val data = document.data
                        if (data != null) {
                            val room = data.toRoom()
                            println(tag + "room found: ${room.code}")
                            trySend(room)
                        } else {
                            println(tag + "empty room data for code: $code")
                            trySend(null)
                        }
                    } else {
                        println(tag + "room not found: $code")
                        trySend(null)
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    println(tag + "error getting room: ${e.message}")
                    trySend(null)
                }

            awaitClose {}
        }
    }


    fun insertRoomWithHost(roomCode: String, player: Player): Flow<String?> {
        return callbackFlow {
            val room = Room(code = roomCode, hostId = "0", lastPlayerIndex = 0)
            val roomRef = db.collection(collection).document(roomCode)

            val playerMap = mapOf(
                "id" to 0,
                "pseudo" to player.pseudo,
                "iconUrl" to player.iconUrl,
                "isHost" to true,
                "distance" to player.distance.value,
                "timedDistances" to emptyList<Map<String, Any>>()
                //"latitude" to player.listLocation.lastOrNull()?.latitude ?: 0.0,
                //"longitude" to player.listLocation.lastOrNull()?.longitude ?: 0.0
            )

            roomRef.set(room.toHashMap())
                .addOnSuccessListener {
                    roomRef.collection("players").document("0").set(playerMap)
                        .addOnSuccessListener {
                            println("$tag Room and host created")
                            trySend(room.code)
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                            println("$tag Error adding host: ${e.message}")
                            trySend(null)
                        }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    println("$tag Error creating room: ${e.message}")
                    trySend(null)
                }

            awaitClose {}
        }
    }


    fun joinRoomWithAutoId(roomCode: String, player: Player): Flow<Int?> {
        return callbackFlow {
            val roomRef = db.collection(collection).document(roomCode)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(roomRef)
                val lastIndex = (snapshot.getLong("lastPlayerIndex") ?: -1).toInt()
                val newIndex = lastIndex + 1

                val playerMap = mapOf(
                    "id" to newIndex,
                    "pseudo" to player.pseudo,
                    "iconUrl" to player.iconUrl,
                    "isHost" to false,
                    "distance" to player.distance.value,
                    "timedDistances" to emptyList<Map<String, Any>>()
                    //"latitude" to player.listLocation.lastOrNull()?.latitude ?: 0.0,
                    //"longitude" to player.listLocation.lastOrNull()?.longitude ?: 0.0
                )

                transaction.set(
                    roomRef.collection("players").document(newIndex.toString()),
                    playerMap
                )
                transaction.update(roomRef, "lastPlayerIndex", newIndex)

                newIndex
            }.addOnSuccessListener { newId ->
                trySend(newId)
            }.addOnFailureListener { e ->
                e.printStackTrace()
                println(tag + "Error joining room: ${e.message}")
                trySend(null)
            }

            awaitClose {}
        }
    }


    fun getPlayersInRoom(roomCode: String): Flow<List<Pair<Int, Player>>> = callbackFlow {
        val playersRef = db.collection("rooms")
            .document(roomCode)
            .collection("players")

        val listener = playersRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                error.printStackTrace()
                trySend(emptyList())
                return@addSnapshotListener
            }

            val players = snapshot?.documents?.mapNotNull { doc ->
                val id = doc.getLong("id")?.toInt() ?: return@mapNotNull null
                val pseudo = doc.getString("pseudo") ?: return@mapNotNull null
                val iconUrl = (doc.getLong("iconUrl") ?: 0).toInt()
                val isHost = doc.getBoolean("isHost") ?: false
                val distance = (doc.getDouble("distance") ?: 0.0).toFloat()

                val timedList = doc["timedDistance"] as? List<Map<String, Any>> ?: emptyList()
                val parsedTimedDistances = parseTimedDistances(timedList)

                val player = Player(
                    iconUrl = iconUrl,
                    pseudo = pseudo,
                    isHost = isHost,
                    listLocation = mutableListOf(),
                    distance = mutableFloatStateOf(distance)
                ).apply {
                    this.timedDistance.addAll(parsedTimedDistances)
                }

                id to player
            } ?: emptyList()

            trySend(players)
        }

        awaitClose { listener.remove() }
    }.distinctUntilChanged()


    fun getPlayerById(roomCode: String, playerId: Int): Flow<Player?> = callbackFlow {
        val playerRef = db.collection("rooms")
            .document(roomCode)
            .collection("players")
            .document(playerId.toString())

        val listener = playerRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                error.printStackTrace()
                trySend(null)
                return@addSnapshotListener
            }

            val doc = snapshot
            if (doc != null && doc.exists()) {
                val pseudo = doc.getString("pseudo")
                val iconUrl = (doc.getLong("iconUrl") ?: 0).toInt()
                val isHost = doc.getBoolean("isHost") ?: false
                val distance = (doc.getDouble("distance") ?: 0.0).toFloat()

                val timedList = doc["timedDistance"] as? List<Map<String, Any>> ?: emptyList()
                val parsedTimedDistances = parseTimedDistances(timedList)

                if (pseudo != null) {
                    val player = Player(
                        iconUrl = iconUrl,
                        pseudo = pseudo,
                        isHost = isHost,
                        listLocation = mutableListOf(),
                        distance = mutableFloatStateOf(distance)
                    ).apply {
                        this.timedDistance.addAll(parsedTimedDistances)
                    }

                    trySend(player)
                } else {
                    trySend(null)
                }
            } else {
                trySend(null)
            }
        }

        awaitClose { listener.remove() }
    }


    private fun Room.toHashMap(): HashMap<String, Any> {
        return hashMapOf(
            "code" to code,
            "hostId" to hostId,
            "lastPlayerIndex" to lastPlayerIndex
        )
    }

    private fun Map<String, Any>.toRoom(): Room {
        return Room(
            code = this["code"] as String,
            hostId = this["hostId"] as String,
            lastPlayerIndex = (this["lastPlayerIndex"] as Long).toInt()
        )
    }
    suspend fun getPlayer(flow: Flow<Player?>): Player? {
        var player: Player? = null
        flow.collect { result ->
            player = result
        }
        return player
    }

    //add location to player in firestore
    @SuppressLint("SuspiciousIndentation")
    fun addLocationToPlayer(roomCode: String, playerId: Int, location: Location) {
        val playerRef = db.collection("rooms")
            .document(roomCode)
            .collection("players")
            .document(playerId.toString())

        val locationData = hashMapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "distance" to PlayerManager.currentPlayer?.distance?.value
        )

        playerRef.update(locationData as Map<String, Any>)
            .addOnSuccessListener {
                println("$tag Updated location for player $playerId in room $roomCode")
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                println("$tag Error updating location for player $playerId: ${e.message}")
            }

        //add distance and timestamp into timedDistances to player in firebase
        val timedDistanceData = hashMapOf(
            "distance" to PlayerManager.currentPlayer?.distance?.value,
            "timestamp" to System.currentTimeMillis()
        )
            playerRef.update("timedDistance", FieldValue.arrayUnion(timedDistanceData))
                .addOnSuccessListener {
                    println("Item added successfully to $timedDistanceData!")
                }
                .addOnFailureListener { e ->
                    println("Error adding item: ${e.message}")
                }

    }


    fun parseTimedDistances(data: List<Map<String, Any>>): List<Pair<Float, Long>> {
        return data.mapNotNull {
            val distance = (it["distance"] as? Number)?.toFloat()
            val timestamp = (it["timestamp"] as? Number)?.toLong()
            if (distance != null && timestamp != null) {
                distance to timestamp
            } else null
        }
    }

    fun setPlayerStarting(roomCode: String, playerId: Int) {
        val playerRef = db.collection("rooms").document(roomCode)
            .collection("players").document(playerId.toString())

        playerRef.update("isStarting", true)
            .addOnSuccessListener {
                println("Item added successfully to $playerId!")
            }
            .addOnFailureListener { e ->
                println("Error adding item: ${e.message}")
            }
    }
}
