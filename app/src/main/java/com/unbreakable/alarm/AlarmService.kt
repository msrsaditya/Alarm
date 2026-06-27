package com.unbreakable.alarm
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import com.unbreakable.alarm.data.AppDatabase
import androidx.room.Room
import com.unbreakable.alarm.data.AlarmRepository
class AlarmService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    companion object {
        const val ACTION_STOP_ALARM = "ACTION_STOP_ALARM"
        const val ACTION_MUTE_ALARM = "ACTION_MUTE_ALARM"
        const val ACTION_UNMUTE_ALARM = "ACTION_UNMUTE_ALARM"
        const val CHANNEL_ID = "alarm_channel"
        const val NOTIFICATION_ID = 101
        
        var wakeLock: android.os.PowerManager.WakeLock? = null
    }
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_ALARM) {
            val alarmId = intent.getIntExtra("ALARM_ID", -1)
            stopAlarm(alarmId)
            return START_NOT_STICKY
        } else if (intent?.action == ACTION_MUTE_ALARM) {
            mediaPlayer?.let {
                if (it.isPlaying) it.pause()
            }
            vibrator?.cancel()
            return START_STICKY
        } else if (intent?.action == ACTION_UNMUTE_ALARM) {
            mediaPlayer?.let {
                if (!it.isPlaying) it.start()
            }
            playVibration()
            return START_STICKY
        }
        val alarmId = intent?.getIntExtra("ALARM_ID", -1) ?: -1
        val alarmLabel = intent?.getStringExtra("ALARM_LABEL") ?: "Alarm"
        Log.d("AlarmService", "Starting alarm for ID $alarmId")
        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", alarmLabel)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Alarm Ringing")
            .setContentText(alarmLabel)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .build()
        startForeground(NOTIFICATION_ID, notification)
        playMedia()
        playVibration()
        return START_STICKY
    }
    private var maxVolumeJob: kotlinx.coroutines.Job? = null
    private val serviceScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main + kotlinx.coroutines.Job())
    private fun playMedia() {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            maxVolumeJob = serviceScope.launch {
                while (true) {
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_ALARM,
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),
                        0
                    )
                    kotlinx.coroutines.delay(1000)
                }
            }
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmService, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to play media", e)
        }
    }
    private fun playVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        val pattern = longArrayOf(0, 1000, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }
    private fun stopAlarm(alarmId: Int) {
        maxVolumeJob?.cancel()
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        vibrator?.cancel()
        if (alarmId != -1) {
            serviceScope.launch(Dispatchers.IO) {
                val repo = (applicationContext as AlarmApplication).repository
                val alarm = repo.getById(alarmId)
                if (alarm != null) {
                    val scheduler = AlarmScheduler(applicationContext)
                    scheduler.schedule(alarm)
                }
                stopSelf()
            }
        } else {
            stopSelf()
        }
    }
    override fun onDestroy() {
        mediaPlayer?.release()
        vibrator?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Ringing",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setBypassDnd(true)
                description = "Shows ringing alarms"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}