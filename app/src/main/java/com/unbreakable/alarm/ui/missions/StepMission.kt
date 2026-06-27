package com.unbreakable.alarm.ui.missions
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
@Composable
fun StepMission(targetCount: Int?, onComplete: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    var initialSteps by remember { mutableStateOf(-1f) }
    var stepCount by remember { mutableStateOf(0) }
    val target = targetCount ?: 50
    var permissionGranted by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        permissionGranted = isGranted
    }
    LaunchedEffect(Unit) {
        if (!permissionGranted && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }
    DisposableEffect(permissionGranted) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        var lastMag = 0f
        var peakTimer = 0L
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (stepCount >= target) return
                if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                    if (event.values[0] == 1.0f) {
                        stepCount++
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (stepCount >= target) {
                            onComplete()
                        }
                    }
                } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val magnitude = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                    val currentTime = System.currentTimeMillis()
                    if (magnitude > 13.5f && lastMag <= 13.5f && (currentTime - peakTimer) > 300) {
                        stepCount++
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        peakTimer = currentTime
                        if (stepCount >= target) onComplete()
                    }
                    lastMag = magnitude
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        if (stepDetector != null) {
            sensorManager.registerListener(sensorEventListener, stepDetector, SensorManager.SENSOR_DELAY_UI)
        } else if (accelerometer != null) {
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!permissionGranted) {
            Text("Physical activity permission required for step tracking", textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                }
            }) {
                Text("Grant Permission")
            }
        } else {
            Text("Walk around!", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(48.dp))
            Text("$stepCount / $target", fontSize = 64.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(progress = { stepCount.toFloat() / target.toFloat() }, modifier = Modifier.fillMaxWidth().height(16.dp))
        }
    }
}