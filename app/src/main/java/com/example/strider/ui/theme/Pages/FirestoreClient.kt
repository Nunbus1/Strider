package com.example.strider.ui.theme.Pages

import DataClass.Player
import android.location.Location
import androidx.compose.runtime.mutableFloatStateOf
import com.example.strider.PlayerManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

/**
 * Classe client pour interagir avec Firestore.
 * Gère les opérations de lecture, d'écriture et de mise à jour des rooms et des joueurs.
 */
@Suppress("UNCHECKED_CAST")
class FirestoreClient {

    // -------------------------
    // Firestore config
    // -------------------------

    private val tag = "FirestoreClient: "
    private val db = FirebaseFirestore.getInstance()
    private val collection = "rooms"

    /**
     * Récupère en temps réel l'état de lancement du jeu depuis Firestore.
     *
     * @param roomCode Le code de la room concernée.
     * @return Un Flow émettant true/false ou null si erreur.
     */
    fun getHostLaunchGame(roomCode: String): Flow<Boolean?> = callbackFlow {
        val roomRef = db.collection(collection).document(roomCode)
        val listener = roomRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                error.printStackTrace()
                trySend(null)
                return@addSnapshotListener
            }
            val hostLaunchGame = snapshot?.getBoolean("hostLaunchGame")
            trySend(hostLaunchGame)
        }
        awaitClose { listener.remove() }
    }.distinctUntilChanged()

    /**
     * Met à jour dans Firestore l'état `hostLaunchGame` à true pour lancer la partie.
     *
     * @param roomCode Le code de la room à mettre à jour.
     */
    fun setHostLaunchGame(roomCode: String) {
        db.collection(collection).document(roomCode).update("hostLaunchGame", true)
    }

    /**
     * Récupère les données d'une room depuis Firestore.
     *
     * @param code Le code unique de la room.
     * @return Un Flow contenant la room ou null si elle n'existe pas.
     */
    fun getRoom(code: String): Flow<Room?> = callbackFlow {
        db.collection(collection)
            .document(code)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val room = document.data?.toRoom()
                    println("$tag room found: ${room?.code}")
                    trySend(room)
                } else {
                    println("$tag room not found: $code")
                    trySend(null)
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
                println("$tag error getting room: ${it.message}")
                trySend(null)
            }
        awaitClose {}
    }

    /**
     * Vérifie si une room avec le code donné existe déjà.
     *
     * @param code Le code de la room.
     * @return true si la room existe, false sinon.
     */
    suspend fun checkIfRoomExists(code: String): Boolean {
        val doc = db.collection(collection).document(code).get().await()
        return doc.exists()
    }

    /**
     * Insère une nouvelle room dans Firestore avec un joueur hôte (id = 0).
     *
     * @param roomCode Le code de la room à créer.
     * @param player Le joueur qui sera l'hôte de la room.
     * @return Un Flow contenant le code de la room si réussite, sinon null.
     */
    fun insertRoomWithHost(roomCode: String, player: Player): Flow<String?> = callbackFlow {
        val room = Room(code = roomCode, hostId = "0", hostLaunchGame = false, lastPlayerIndex = 0)
        val roomRef = db.collection(collection).document(roomCode)

        val playerMap = mapOf(
            "id" to 0,
            "pseudo" to player.pseudo,
            "iconUrl" to player.iconUrl,
            "isHost" to player.isHost,
            "distance" to player.distance.floatValue,
            "timedDistances" to emptyList<Map<String, Any>>()
        )

        roomRef.set(room.toHashMap())
            .addOnSuccessListener {
                roomRef.collection("players").document("0").set(playerMap)
                    .addOnSuccessListener {
                        println("$tag Room and host created")
                        trySend(room.code)
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                        println("$tag Error adding host: ${it.message}")
                        trySend(null)
                    }
            }
            .addOnFailureListener {
                it.printStackTrace()
                println("$tag Error creating room: ${it.message}")
                trySend(null)
            }

        awaitClose {}
    }

    /**
     * Rejoint une room existante et génère automatiquement un nouvel ID joueur.
     *
     * @param roomCode Le code de la room à rejoindre.
     * @param player Le joueur qui rejoint la room.
     * @return Un Flow contenant l'identifiant (index) du joueur dans la room, ou null si échec.
     */
    fun joinRoomWithAutoId(roomCode: String, player: Player): Flow<Int?> = callbackFlow {
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
                "distance" to player.distance.floatValue,
                "timedDistances" to emptyList<Map<String, Any>>()
            )

            transaction.set(
                roomRef.collection("players").document(newIndex.toString()),
                playerMap
            )
            transaction.update(roomRef, "lastPlayerIndex", newIndex)

            newIndex
        }.addOnSuccessListener {
            trySend(it)
        }.addOnFailureListener {
            it.printStackTrace()
            println("$tag Error joining room: ${it.message}")
            trySend(null)
        }

        awaitClose {}
    }

    /**
     * Récupère en temps réel la liste des joueurs présents dans une room.
     *
     * @param roomCode Le code de la room.
     * @return Un Flow contenant une liste de paires (id, Player).
     */
    fun getPlayersInRoom(roomCode: String): Flow<List<Pair<Int, Player>>> = callbackFlow {
        val playersRef = db.collection(collection)
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

    /**
     * Met à jour la position actuelle du joueur dans Firestore et enregistre une entrée `timedDistance`.
     *
     * @param roomCode Le code de la room.
     * @param playerId L'identifiant du joueur dans Firestore.
     * @param location La position GPS à enregistrer.
     */
    fun addLocationToPlayer(roomCode: String, playerId: Int, location: Location) {
        val playerRef = db.collection(collection)
            .document(roomCode)
            .collection("players")
            .document(playerId.toString())

        val locationData = mapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "distance" to PlayerManager.currentPlayer?.distance?.floatValue
        )

        playerRef.update(locationData)
            .addOnSuccessListener {
                println("$tag Updated location for player $playerId in room $roomCode")
            }
            .addOnFailureListener {
                it.printStackTrace()
                println("$tag Error updating location: ${it.message}")
            }

        val timedDistanceData = mapOf(
            "distance" to PlayerManager.currentPlayer?.distance?.floatValue,
            "timestamp" to System.currentTimeMillis()
        )

        playerRef.update("timedDistance", FieldValue.arrayUnion(timedDistanceData))
            .addOnSuccessListener {
                println("$tag Added timedDistance entry")
            }
            .addOnFailureListener {
                println("$tag Error adding timedDistance: ${it.message}")
            }
    }

    // -------------------------
    // UTILS
    // -------------------------

    /**
     * Convertit une instance de [Room] en [HashMap] pour insertion dans Firestore.
     *
     * @receiver La room à convertir.
     * @return Un HashMap contenant les champs de la room.
     */
    private fun Room.toHashMap(): HashMap<String, Any> = hashMapOf(
        "code" to code,
        "hostId" to hostId,
        "hostLaunchGame" to hostLaunchGame,
        "lastPlayerIndex" to lastPlayerIndex
    )

    /**
     * Convertit une [Map] représentant un document Firestore en instance de [Room].
     *
     * @receiver Une map contenant les champs d'une room.
     * @return Une instance de [Room] avec les données extraites.
     */
    private fun Map<String, Any>.toRoom(): Room {
        return Room(
            code = this["code"] as String,
            hostId = this["hostId"] as String,
            lastPlayerIndex = (this["lastPlayerIndex"] as Long).toInt()
        )
    }

    /**
     * Analyse une liste de maps représentant des distances/temps bruts Firestore en paires typées.
     *
     * @param data Liste de maps contenant des champs "distance" (Float) et "timestamp" (Long).
     * @return Une liste de paires (distance, timestamp), ou une liste vide si invalide.
     */
    private fun parseTimedDistances(data: List<Map<String, Any>>): List<Pair<Float, Long>> {
        return data.mapNotNull {
            val distance = (it["distance"] as? Number)?.toFloat()
            val timestamp = (it["timestamp"] as? Number)?.toLong()
            if (distance != null && timestamp != null) distance to timestamp else null
        }
    }

}
