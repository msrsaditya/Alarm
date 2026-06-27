package com.unbreakable.alarm
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.unbreakable.alarm.data.Alarm
import java.util.Calendar
class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    fun schedule(alarm: Alarm) {
        if (!alarm.isActive) return
        val triggerTime = calculateNextTime(alarm)
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
            putExtra("ALARM_LABEL", alarm.label)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val showIntent = Intent(context, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            0,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent),
                pendingIntent
            )
            Log.d("AlarmScheduler", "Alarm ${alarm.id} scheduled for $triggerTime")
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "Missing EXACT_ALARM permission", e)
        }
    }
    fun cancel(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
    private fun calculateNextTime(alarm: Alarm): Long {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance()
        next.set(Calendar.HOUR_OF_DAY, alarm.hour)
        next.set(Calendar.MINUTE, alarm.minute)
        next.set(Calendar.SECOND, 0)
        next.set(Calendar.MILLISECOND, 0)
        if (alarm.daysOfWeek.isEmpty()) {
            if (next.timeInMillis <= now.timeInMillis) {
                next.add(Calendar.DAY_OF_YEAR, 1)
            }
            return next.timeInMillis
        }
        while (true) {
            val cDay = next.get(Calendar.DAY_OF_WEEK)
            val dbDay = if (cDay == Calendar.SUNDAY) 7 else cDay - 1
            if (alarm.daysOfWeek.contains(dbDay) && next.after(now)) {
                break
            }
            next.add(Calendar.DAY_OF_YEAR, 1)
        }
        return next.timeInMillis
    }
}