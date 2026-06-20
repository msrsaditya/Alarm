package com.example.data
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow
@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val label: String,
    val isActive: Boolean,
    val daysOfWeek: List<Int>,
    val ringtone: String = "Default",
    val vibrate: Boolean = true,
    val snoozeDuration: Int = 5,
    val missions: List<MissionConfig> = emptyList(),
    val mutePeriodSecs: Int = 60
)
class Converters {
    private val moshi = com.squareup.moshi.Moshi.Builder().build()
    private val type = com.squareup.moshi.Types.newParameterizedType(List::class.java, MissionConfig::class.java)
    private val adapter = moshi.adapter<List<MissionConfig>>(type)
    @TypeConverter
    fun fromDaysOfWeek(days: List<Int>): String {
        return days.joinToString(",")
    }
    @TypeConverter
    fun toDaysOfWeek(data: String): List<Int> {
        if (data.isEmpty()) return emptyList()
        return data.split(",").map { it.toInt() }
    }
    @TypeConverter
    fun fromMissions(missions: List<MissionConfig>): String {
        return adapter.toJson(missions)
    }
    @TypeConverter
    fun toMissions(data: String): List<MissionConfig> {
        if (data.isEmpty()) return emptyList()
        return adapter.fromJson(data) ?: emptyList()
    }
}
@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarms(): Flow<List<Alarm>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm): Long
    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
    suspend fun getAlarmById(id: Int): Alarm?
    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteAlarmById(id: Int)
    @Query("UPDATE alarms SET isActive = :isActive WHERE id = :id")
    suspend fun updateAlarmStatus(id: Int, isActive: Boolean)
}
@Database(entities = [Alarm::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
}
class AlarmRepository(private val alarmDao: AlarmDao) {
    val allAlarms: Flow<List<Alarm>> = alarmDao.getAllAlarms()
    suspend fun insert(alarm: Alarm): Alarm {
        val id = alarmDao.insertAlarm(alarm)
        return alarm.copy(id = id.toInt())
    }
    suspend fun getById(id: Int): Alarm? = alarmDao.getAlarmById(id)
    suspend fun deleteById(id: Int) = alarmDao.deleteAlarmById(id)
    suspend fun setStatus(id: Int, isActive: Boolean) = alarmDao.updateAlarmStatus(id, isActive)
}