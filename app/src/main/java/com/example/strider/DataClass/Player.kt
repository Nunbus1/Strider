package DataClass

import android.location.Location
import android.util.MutableFloat
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.strider.IdManager
import com.example.strider.ui.theme.Pages.FirestoreClient
//import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


data class Player(

    var iconUrl: Int,
    var pseudo: String,
    var isHost: Boolean,
    var listLocation: MutableList<Location> = mutableListOf(),
    var distance: MutableFloatState = mutableFloatStateOf(0f),
    var firestoreClient: FirestoreClient? = null

    ){

    fun updatePlayer(player: DataClass.Player) {
        this.iconUrl = player.iconUrl
        this.pseudo = player.pseudo
        this.isHost = player.isHost
        this.listLocation = player.listLocation
        this.distance = player.distance
    }

    fun addLocation(location: Location) {
        if (this.listLocation.isEmpty()) {
            this.listLocation.add(location)
            return
        }

        if (this.listLocation.last().distanceTo(location) > 1.0f) {
            this.listLocation.add(location)
            firestoreClient?.addLocationToPlayer(IdManager.currentRoomId!!,IdManager.currentPlayerId!!,location )
            this.calculateTotalDistance()
        }
    }

    fun calculateTotalDistance() {
        if (listLocation.size < 2) {
            return
        }
//code bullshit a mettre si deplacement impossible
        /*
        if(distance.value >= 10f) {
            distance.value--
            return
        }
        else{
        distance.value ++
        return}
        */

        this.distance.value += this.listLocation[this.listLocation.size-2].distanceTo(this.listLocation[this.listLocation.size-1])
        /*for (i in 0 until listLocation.size - 1) {
            totalDistance += locations[i].distanceTo(locations[i + 1])
        }
        this.distance = totalDistance
        return totalDistance*/

    }

}



