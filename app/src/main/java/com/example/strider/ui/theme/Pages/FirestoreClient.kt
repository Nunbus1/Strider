package com.example.strider.ui.theme.Pages

import DataClass.Player
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

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
                .document(room.code) // 🔁 on utilise code comme ID
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

    fun getPlayersInRoom(roomCode: String): Flow<List<Player>> = callbackFlow {
        val playersRef = db.collection(collection).document(roomCode).collection("players")
        val subscription = playersRef.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                println("$tag Error fetching players: ${exception.message}")
                close(exception)
                return@addSnapshotListener
            }
            val players = snapshot?.documents?.mapNotNull { it.toObject(Player::class.java) } ?: emptyList()
            trySend(players).isSuccess
        }
        awaitClose { subscription.remove() }
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
}
