package com.unbreakable.alarm.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unbreakable.alarm.AlarmScheduler
import com.unbreakable.alarm.data.Alarm
import com.unbreakable.alarm.data.AlarmRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
class AlarmViewModel(private val repository: AlarmRepository, private val scheduler: AlarmScheduler) : ViewModel() {
    val alarms: StateFlow<List<Alarm>> = repository.allAlarms
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    fun addAlarm(hour: Int, minute: Int, label: String, isActive: Boolean, daysOfWeek: List<Int>, ringtone: String = "Default", vibrate: Boolean = true, snoozeDuration: Int = 5, missions: List<com.unbreakable.alarm.data.MissionConfig> = emptyList(), mutePeriodSecs: Int = 60) {
        viewModelScope.launch {
            val alarm = repository.insert(Alarm(hour = hour, minute = minute, label = label, isActive = isActive, daysOfWeek = daysOfWeek, ringtone = ringtone, vibrate = vibrate, snoozeDuration = snoozeDuration, missions = missions, mutePeriodSecs = mutePeriodSecs))
            if (isActive) {
                scheduler.schedule(alarm)
            }
        }
    }
    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.deleteById(alarm.id)
            scheduler.cancel(alarm)
        }
    }
    fun toggleAlarm(alarm: Alarm) {
        viewModelScope.launch {
            val newStatus = !alarm.isActive
            repository.setStatus(alarm.id, newStatus)
            if (newStatus) {
                scheduler.schedule(alarm.copy(isActive = true))
            } else {
                scheduler.cancel(alarm)
            }
        }
    }
    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch {
            val updated = repository.insert(alarm)
            if (updated.isActive) {
                scheduler.schedule(updated)
            } else {
                scheduler.cancel(updated)
            }
        }
    }
}
class AlarmViewModelFactory(private val repository: AlarmRepository, private val scheduler: AlarmScheduler) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlarmViewModel(repository, scheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}