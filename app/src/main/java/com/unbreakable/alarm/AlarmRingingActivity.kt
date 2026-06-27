package com.unbreakable.alarm
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.unbreakable.alarm.ui.theme.MyApplicationTheme
class AlarmRingingActivity : ComponentActivity() {
    @Volatile private var isRinging = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })
        turnScreenOnAndKeyguardOff()
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Wake Up!"
        val isPreview = intent.getBooleanExtra("IS_PREVIEW", false)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                var alarm by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.unbreakable.alarm.data.Alarm?>(null) }
                var showMissions by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                androidx.compose.runtime.LaunchedEffect(alarmId) {
                    if (alarmId != -1) {
                        val repo = (applicationContext as com.unbreakable.alarm.AlarmApplication).repository
                        alarm = repo.getById(alarmId)
                    }
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        val currentAlarm = alarm
                        if (showMissions && currentAlarm?.missions?.isNotEmpty() == true) {
                            com.unbreakable.alarm.ui.missions.MissionExecutionFlow(
                                missions = currentAlarm.missions,
                                mutePeriodSecs = currentAlarm.mutePeriodSecs,
                                onCompleteAll = {
                                    isRinging = false
                                    stopServiceAndFinish(alarmId)
                                },
                                onMuteRequest = {
                                    startService(Intent(this@AlarmRingingActivity, AlarmService::class.java).apply { action = AlarmService.ACTION_MUTE_ALARM })
                                },
                                onUnmuteRequest = {
                                    startService(Intent(this@AlarmRingingActivity, AlarmService::class.java).apply { action = AlarmService.ACTION_UNMUTE_ALARM })
                                }
                            )
                        } else {
                            RingingScreen(label = alarmLabel) {
                                if (currentAlarm?.missions?.isNotEmpty() == true) {
                                    showMissions = true
                                } else {
                                    isRinging = false
                                    stopServiceAndFinish(alarmId)
                                }
                            }
                        }
                    }
                    if (isPreview) {
                        Button(
                            onClick = {
                                isRinging = false
                                stopServiceAndFinish(alarmId)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Red),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .systemBarsPadding()
                                .padding(16.dp)
                                .padding(bottom = 32.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Abort Preview", color = androidx.compose.ui.graphics.Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            }
        }
    }
    override fun onStop() {
        super.onStop()
        val isPreview = intent.getBooleanExtra("IS_PREVIEW", false)
        if (isRinging && !isPreview) {
            val alarmId = intent.getIntExtra("ALARM_ID", -1)
            val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Wake Up!"
            val launchIntent = Intent(this, AlarmRingingActivity::class.java).apply {
                putExtra("ALARM_ID", alarmId)
                putExtra("ALARM_LABEL", alarmLabel)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(launchIntent)
        }
    }
    private fun turnScreenOnAndKeyguardOff() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        }
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
    }
    private fun stopServiceAndFinish(alarmId: Int) {
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_STOP_ALARM
            putExtra("ALARM_ID", alarmId)
        }
        startService(stopIntent)
        finishAndRemoveTask()
    }
    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        if (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN ||
            keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP ||
            keyCode == android.view.KeyEvent.KEYCODE_VOLUME_MUTE) {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            audioManager.setStreamVolume(
                android.media.AudioManager.STREAM_ALARM,
                audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_ALARM),
                0
            )
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
    override fun onKeyUp(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        if (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN ||
            keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP ||
            keyCode == android.view.KeyEvent.KEYCODE_VOLUME_MUTE) {
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val isPreview = intent.getBooleanExtra("IS_PREVIEW", false)
        if (!isPreview) {
            val launchIntent = Intent(this, AlarmRingingActivity::class.java).apply {
                putExtra("ALARM_ID", intent.getIntExtra("ALARM_ID", -1))
                putExtra("ALARM_LABEL", intent.getStringExtra("ALARM_LABEL"))
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(launchIntent)
        }
    }
}
@Composable
fun RingingScreen(label: String, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Alarm Ringing",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = label,
            fontSize = 48.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(64.dp))
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("STOP", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onError)
        }
    }
}