package com.example.ui.missions
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
@Composable
fun ShakeMission(targetCount: Int?, onComplete: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    var shakeCount by remember { mutableStateOf(0) }
    val target = targetCount ?: 30
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        var lastUpdate: Long = 0
        var last_x = 0f
        var last_y = 0f
        var last_z = 0f
        val SHAKE_THRESHOLD = 1500
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (shakeCount >= target) return
                val curTime = System.currentTimeMillis()
                if ((curTime - lastUpdate) > 100) {
                    val diffTime = (curTime - lastUpdate)
                    lastUpdate = curTime
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val speed = abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000
                    if (speed > SHAKE_THRESHOLD) {
                        shakeCount++
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (shakeCount >= target) {
                            onComplete()
                        }
                    }
                    last_x = x
                    last_y = y
                    last_z = z
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Shake your phone!", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(48.dp))
        Text("$shakeCount / $target", fontSize = 64.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(progress = { shakeCount.toFloat() / target.toFloat() }, modifier = Modifier.fillMaxWidth().height(16.dp))
    }
}