package com.example.othersensors.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    private lateinit var sensorManager: SensorManager
    private var isPermissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            isPermissionGranted = isGranted
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            isPermissionGranted = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.BODY_SENSORS)
        }

        setContent {
            MultiSensorApp(isPermissionGranted)
        }
    }
}

@Composable
fun MultiSensorApp(isPermissionGranted: Boolean) {
    var heartRateValue by remember { mutableStateOf("Heart Rate: Loading...") }
    var selectedRoom by remember { mutableStateOf("None") }

    if (isPermissionGranted) {
        val context = LocalContext.current
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        LaunchedEffect(true) {
            setupHeartRateSensor(sensorManager) { newHeartRate ->
                heartRateValue = newHeartRate
            }
        }
    }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = { selectedRoom = "Living Room" }) {
                Text(text = "Living Room",fontSize = 10.sp,)
            }
            Text(text = heartRateValue, fontSize = 10.sp, color = Color.Black)
            Button(onClick = { selectedRoom = "Study Room" }) {
                Text(text = "Study Room",fontSize = 10.sp,)
            }
            Text(text = "You are now in: $selectedRoom", fontSize = 10.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Where are you now?",
                fontSize = 10.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

fun setupHeartRateSensor(sensorManager: SensorManager, updateHeartRate: (String) -> Unit) {
    val heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
    val heartRateListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val newHeartRate = "Heart Rate: ${event.values[0].toInt()} BPM"
            updateHeartRate(newHeartRate)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    heartRateSensor?.let {
        sensorManager.registerListener(
            heartRateListener, it, SensorManager.SENSOR_DELAY_NORMAL
        )
    }
}
