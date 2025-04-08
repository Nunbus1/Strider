package com.example.strider
//package com.example.strider.BuildConfig

import DataClass.Player
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.strider.ui.theme.StriderTheme
import ViewModels.ImageViewModel
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import androidx.compose.material3.Button
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

val Context.dataStore by preferencesDataStore(name = "location_prefs")
object PlayerManager {
    var currentPlayer: Player? = null
}
class MainActivity :  ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private val stepCount = mutableIntStateOf(0)
    lateinit var imageView: ImageViewModel
    lateinit var player: Player


    private val context: Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        imageView = ViewModelProvider(this).get(ImageViewModel::class.java)
        player = DataClass.Player(2, "", false)
        PlayerManager.currentPlayer = player
        enableEdgeToEdge()
        setContent {


            StepTrackerApp(
                stepCount = stepCount.intValue,
                isSensorAvailable = stepSensor != null,
                //currentPosition = player.listLocation.last()
            )

            StriderTheme {
                StriderApp(imageViewModel = imageView)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }


    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
            stepCount.value += 1
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

}

class LocationService : LifecycleService() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                PlayerManager.currentPlayer?.addLocation(location)
                //Log.d("LocationService", "Lat: ${location.latitude} }, Lng: ${location.longitude}")
                //saveLocation(location.latitude, location.longitude)
            }
        }
    }


    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.Builder(500).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
        startForeground(1, createNotification())
        requestLocationUpdates()
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        }
    }

    private fun createNotification(): Notification {
        val channelId = "location_channel"
        val notificationManager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,"Location Service", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)

            return Notification.Builder(this, channelId)
                .setContentTitle("Tracking Location")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .build()
        } else {
            return Notification.Builder(this)
                .setContentTitle("Tracking Location")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .build()
        }
    }

    private fun saveLocation(lat: Double, lng: Double) {
        runBlocking {
            applicationContext.dataStore.edit { prefs ->
                prefs[doublePreferencesKey("latitude")] = lat
                prefs[doublePreferencesKey("longitude")] = lng
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}

fun getPlayerUpdates(): Flow<Player> = flow {
    emit(PlayerManager.currentPlayer!!)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StriderTheme {
    }
}

@Composable
fun StepTrackerApp(stepCount: Int, isSensorAvailable: Boolean, currentPosition: Location? = null) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (isSensorAvailable) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Step Tracker", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Steps: $stepCount", fontSize = 48.sp, color = MaterialTheme.colorScheme.onBackground)

                    }
                } else {
                    Text("current position: $currentPosition", fontSize = 48.sp, color = MaterialTheme.colorScheme.onBackground)
                    Text(text = "Step detector sensor not available on this device.", fontSize = 18.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

//exemple pour utiliser la localisation
@Composable
fun LocationScreen(context: Context, player: Player) {
    //val locationService = LocationService(player)
    val serviceIntent = remember { Intent(context, LocationService::class.java) }
    var isServiceRunning by remember { mutableStateOf(false) }
    /* val latitudeFlow = context.dataStore.data.map { prefs ->
         prefs[doublePreferencesKey("latitude")] ?: 0.0
     }
     val longitudeFlow = context.dataStore.data.map { prefs ->
         prefs[doublePreferencesKey("longitude")] ?: 0.0
     }

     val latitude by latitudeFlow.collectAsState(initial = 0.0)
     val longitude by longitudeFlow.collectAsState(initial = 0.0)
 */
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                context.startService(serviceIntent)
                //locationService.startForeground(1,  locationService.createNotification())
                isServiceRunning = true
            }
        }
    )
    if (ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        //locationService.startForeground(1,  locationService.createNotification())
        context.startService(serviceIntent)
        isServiceRunning = true
    } else {
        permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }
}
/*
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Latitude: $latitude", style = MaterialTheme.typography.bodyLarge)
        Text("Longitude: $longitude", style = MaterialTheme.typography.bodyLarge)
*/
/*Button(onClick = {
    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        //locationService.startForeground(1,  locationService.createNotification())
        context.startService(serviceIntent)
        isServiceRunning = true
    } else {
        permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }
}) {
    Text("Start Location Service")
}

Button(onClick = {
    //locationService.stopForeground(true)
    context.stopService(serviceIntent)
    isServiceRunning = false
}, enabled = isServiceRunning) {
    Text("Stop Location Service")
}

 */
//}