package DataClass

import android.location.Location
import com.google.android.gms.location.LocationResult

data class Player(
    var iconUrl: Int,
    var pseudo: String,
    var isHost: Boolean,
    var locationResult: LocationResult?,
    var distance: Float,
    var currentPosition: Location?

){

    fun updatePlayer(player: DataClass.Player) {
        this.iconUrl = player.iconUrl
        this.pseudo = player.pseudo
        this.isHost = player.isHost
        this.locationResult = player.locationResult
        this.distance = player.distance
    }

    fun calculateTotalDistance(): Float {
        var totalDistance = 0f
            val locations = this.locationResult?.locations ?: emptyList()

            for (i in 0 until locations.size - 1) {
                totalDistance += locations[i].distanceTo(locations[i + 1])
            }
            this.distance = totalDistance
            return totalDistance

    }

}



