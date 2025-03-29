package com.example.strider

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.strider.ui.theme.StriderTheme
import android.Manifest
import ViewModels.ImageViewModel
import androidx.lifecycle.ViewModelProvider


class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private val stepCount = mutableIntStateOf(0)
    lateinit var imageView: ImageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        val imageViewModel = ViewModelProvider(this).get(ImageViewModel::class.java)

        enableEdgeToEdge()
        setContent {


            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val permissionStatus = ContextCompat.checkSelfPermission(
                        this@MainActivity, Manifest.permission.ACTIVITY_RECOGNITION)
                    if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                        permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                    }
                }

            }
            StepTrackerApp(
                stepCount = stepCount.intValue,
                isSensorAvailable = stepSensor != null
            )
            //stepCounterViewModel = ViewModelProvider(this).get(StepCounterViewModel::class.java)

            StriderTheme {
                StriderApp(imageViewModel)
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



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StriderTheme {
    }
}

@Composable
fun StepTrackerApp(stepCount: Int, isSensorAvailable: Boolean) {
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
                    Text(text = "Step detector sensor not available on this device.", fontSize = 18.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
