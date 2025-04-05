package DataClass

import android.location.Location
import com.google.android.gms.location.LocationResult

data class Player(
    var iconUrl: Int,
    var pseudo: String,
    var isHost: Boolean,
    var listLocation: MutableList<Location> = mutableListOf(),
    var distance: Float = 0f,

){

    fun updatePlayer(player: DataClass.Player) {
        this.iconUrl = player.iconUrl
        this.pseudo = player.pseudo
        this.isHost = player.isHost
        this.listLocation = player.listLocation
        this.distance = player.distance
    }

    fun addLocation(location: Location) {
        this.listLocation.add(location)
        this.calculateTotalDistance()
    }

    fun calculateTotalDistance(): Float {
        if (listLocation.isEmpty()) {
            return 0f
        }
        var totalDistance = 0f
            val locations = this.listLocation

            for (i in 0 until listLocation.size - 1) {
                totalDistance += locations[i].distanceTo(locations[i + 1])
            }
            this.distance = totalDistance
            return totalDistance

    }

}



