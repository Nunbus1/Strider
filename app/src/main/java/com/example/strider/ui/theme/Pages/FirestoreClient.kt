package com.example.strider.ui.theme.Pages

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
    private val collection = "users"

    fun insertRoom(
        room: Room
    ): Flow<String?> {
        return callbackFlow {
            db.collection(collection)
                .document(room.code) // <- Utilise le code comme ID du document
                .set(room.toHashMap())
                .addOnSuccessListener {
                    println(tag + "insert room with custom id (code): ${room.code}")

                    CoroutineScope(Dispatchers.IO).launch {
                        updateRoom(room.copy(id = room.code)).collect {}
                    }

                    trySend(room.code) // <- On retourne le code comme ID
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    println(tag + "error inserting room: ${e.message}")
                    trySend(null)
                }

            awaitClose {}
        }
    }

    fun updateRoom(
        room: Room
    ): Flow<Boolean> {
        return callbackFlow {
            db.collection(collection)
                .document(room.id)
                .set(room.toHashMap())
                .addOnSuccessListener {
                    println(tag + "update user with id: ${room.id}")
                    trySend(true)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    println(tag + "error updating user: ${e.message}")
                    trySend(false)
                }

            awaitClose {}
        }
    }

    fun getRoom(
        code: String
    ): Flow<Room?> {
        return callbackFlow {
            db.collection(collection)
                .get()
                .addOnSuccessListener { result ->
                    var room: Room? = null

                    for (document in result) {
                        if (document.data["code"] == code) {
                            room = document.data.toUser()
                            println(tag + "user found: ${room.code}")
                            trySend(room)
                        }
                    }

                    if (room == null) {
                        println(tag + "user not found: $code")
                        trySend(null)
                    }

                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    println(tag + "error getting user: ${e.message}")
                    trySend(null)
                }

            awaitClose {}
        }
    }

    private fun Room.toHashMap(): HashMap<String, Any> {
        return hashMapOf(
            "id" to id,
            "name" to name,
            "code" to code
        )
    }

    private fun Map<String, Any>.toUser(): Room {
        return Room(
            id = this["id"] as String,
            name = this["name"] as String,
            code = this["code"] as String
        )
    }
}