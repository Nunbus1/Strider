package com.example.strider

import com.example.strider.DataClass.Player
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.LifecycleService
import com.example.strider.ui.theme.Pages.FirestoreClient
import com.google.firebase.FirebaseApp

/**
 * Objet singleton responsable de stocker le joueur actuellement actif dans l'application.
 * Utilisé pour accéder globalement aux informations du joueur (pseudo, distance, etc.).
 */
object PlayerManager {
    var currentPlayer: Player? = Player(
        1,
        "",
        false
    )
}

/**
 * Objet singleton qui gère les identifiants de session :
 * - `currentPlayerId` : identifiant du joueur local
 * - `currentRoomId` : code de la room en cours
 */
object IdManager {
    var currentPlayerId: Int? = 0
    var currentRoomId: String? = ""
}

/**
 * Activité principale de l'application Strider.
 * Initialise Firebase, le capteur de pas, et l'image de profil du joueur.
 * Lance le thème et l'écran principal de l'application.
 */
class MainActivity : ComponentActivity(), SensorEventListener {

    // -------------------------
    // Gestion des capteurs
    // -------------------------

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private val stepCount = mutableIntStateOf(0)

    // -------------------------
    // ViewModel & Joueur
    // -------------------------

    private lateinit var imageView: ImageViewModel
    private lateinit var player: Player

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialisation Firebase
        FirebaseApp.initializeApp(this)

        // Configuration du capteur de pas
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        // Initialisation du ViewModel d'image et du joueur
        imageView = ViewModelProvider(this)[ImageViewModel::class.java]
        player = Player(2, "", false)

        // Assignation globale
        PlayerManager.currentPlayer = player

        // Fullscreen & comportement des barres système
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        enableEdgeToEdge()

        // Lancement de l'UI
        setContent {
            StriderTheme {
                StriderApp(imageViewModel = imageView)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
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

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}

/**
 * Service Android en tâche de fond chargé de suivre la position du joueur.
 * Envoie les coordonnées GPS au modèle de joueur courant à intervalle régulier,
 * et utilise une notification persistante pour rester actif même en arrière-plan.
 */
@Suppress("DEPRECATION")
class LocationService : LifecycleService() {

    // -------------------------
    // Gestion de la localisation
    // -------------------------

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            FirestoreClient()
            locationResult.lastLocation?.let { location ->
                PlayerManager.currentPlayer?.addLocation(location)

            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Initialisation du client de localisation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configuration de la requête
        locationRequest = LocationRequest.Builder(500)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        // Lancement en mode foreground avec notification
        startForeground(1, createNotification())
        requestLocationUpdates()
    }

    /**
     * Demande les mises à jour de localisation si la permission est accordée.
     */
    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )
        }
    }

    /**
     * Crée une notification système pour garder le service actif en foreground.
     */
    private fun createNotification(): Notification {
        val channelId = "location_channel"
        val notificationManager = getSystemService(NotificationManager::class.java)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Location Service",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)

            Notification.Builder(this, channelId)
                .setContentTitle("Tracking Location")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("Tracking Location")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .build()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}

/**
 * Composable qui affiche une interface simple de suivi des pas.
 * Si le capteur de pas est disponible, affiche le nombre de pas détectés.
 * Sinon, affiche un message d’erreur ainsi que la dernière position connue si disponible.
 *
 * @param stepCount Nombre de pas détectés.
 * @param isSensorAvailable Indique si le capteur de pas est disponible.
 * @param currentPosition Dernière position GPS connue (optionnelle).
 */
@Composable
fun StepTrackerApp(
    stepCount: Int,
    isSensorAvailable: Boolean,
    currentPosition: Location? = null
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (isSensorAvailable) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Step Tracker",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Steps: $stepCount",
                        fontSize = 48.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "current position: $currentPosition",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Step detector sensor not available on this device.",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}


/**
 * Composable responsable de démarrer le service de localisation (LocationService).
 * Demande la permission d'accès à la localisation si nécessaire, et démarre le service en tâche de fond.
 *
 * @param context Le contexte Android requis pour lancer le service.
 */
@Composable
fun LocationScreen(context: Context) {

    // -------------------------
    // Intent du service de localisation
    // -------------------------

    val serviceIntent = remember { Intent(context, LocationService::class.java) }
    var isServiceRunning by remember { mutableStateOf(false) }

    // -------------------------
    // Permission d’accès à la localisation
    // -------------------------

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted && !isServiceRunning) {
                context.startService(serviceIntent)
                isServiceRunning = true
            }
        }
    )

    // -------------------------
    // Lancement automatique si déjà autorisé
    // -------------------------

    LaunchedEffect(Unit) {
        val permissionGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            if (!isServiceRunning) {
                context.startService(serviceIntent)
                isServiceRunning = true
            }
        } else {
            permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StepTrackerPreview() {
    StriderTheme {
        StepTrackerApp(
            stepCount = 1234,
            isSensorAvailable = true,
            currentPosition = null
        )
    }
}