package com.unbreakable.alarm
import android.app.Application
import androidx.room.Room
import com.unbreakable.alarm.data.AppDatabase
import com.unbreakable.alarm.data.AlarmRepository
class AlarmApplication : Application() {
    val database by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java, "alarm-database"
        ).fallbackToDestructiveMigration(false)
        .build()
    }
    val repository by lazy {
        AlarmRepository(database.alarmDao())
    }
}
