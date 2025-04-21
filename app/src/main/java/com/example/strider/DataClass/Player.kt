package com.example.strider.DataClass

import android.location.Location
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import com.example.strider.IdManager
import com.example.strider.ui.theme.Pages.FirestoreClient

/**
 * Représente un joueur dans l'application Strider.
 * Gère son état (pseudo, position, distance parcourue) et permet d'enregistrer sa progression.
 *
 * @property iconUrl Ressource drawable représentant l'avatar du joueur.
 * @property pseudo Nom affiché du joueur.
 * @property isHost Indique si ce joueur est l'hôte de la partie.
 * @property listLocation Liste des positions GPS enregistrées du joueur.
 * @property distance Distance totale parcourue, stockée comme état observable.
 * @property timedDistance Liste de paires (distance, timestamp) pour tracer la progression dans le temps.
 * @property firestoreClient Référence vers le client Firestore pour enregistrer les positions.
 */
data class Player(
    var iconUrl: Int,
    var pseudo: String,
    var isHost: Boolean,
    var listLocation: MutableList<Location> = mutableListOf(),
    var distance: MutableFloatState = mutableFloatStateOf(0f),
    var timedDistance: MutableList<Pair<Float, Long>> = mutableListOf(),
    var firestoreClient: FirestoreClient? = null,
) {

    /**
     * Ajoute une nouvelle position au joueur si elle est suffisamment éloignée (> 10m),
     * met à jour la distance, et envoie les données à Firestore.
     *
     * @param location La nouvelle position GPS à enregistrer.
     */
    fun addLocation(location: Location) {
        if (listLocation.isEmpty()) {
            listLocation.add(location)
            timedDistance.add(distance.floatValue to System.currentTimeMillis())
            return
        }

        val lastLocation = listLocation.last()
        val distanceToLast = lastLocation.distanceTo(location)

        if (distanceToLast > 10.0f) {
            listLocation.add(location)

            timedDistance.add(distance.floatValue to System.currentTimeMillis())

            firestoreClient?.addLocationToPlayer(
                roomCode = IdManager.currentRoomId!!,
                playerId = IdManager.currentPlayerId!!,
                location = location
            )

            calculateTotalDistance()
        }
    }

    /**
     * Calcule et ajoute la distance entre les deux dernières positions.
     */
    private fun calculateTotalDistance() {
        if (listLocation.size < 2) return

        val last = listLocation[listLocation.size - 1]
        val beforeLast = listLocation[listLocation.size - 2]

        distance.value += beforeLast.distanceTo(last)
    }
}



