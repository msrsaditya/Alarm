package com.unbreakable.alarm
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.unbreakable.alarm.data.AppDatabase
import com.unbreakable.alarm.data.AlarmRepository
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device booted, rescheduling alarms")
            val pendingResult = goAsync()
            val repository = (context.applicationContext as AlarmApplication).repository
            val scheduler = AlarmScheduler(context)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val alarms = repository.allAlarms.first()
                    alarms.forEach {
                        if (it.isActive) {
                            scheduler.schedule(it)
                        }
                    }
                    Log.d("BootReceiver", "Finished rescheduling alarms")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling alarms", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}