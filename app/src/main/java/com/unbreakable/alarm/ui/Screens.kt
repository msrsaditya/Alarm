package com.unbreakable.alarm.ui
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import android.content.ContextWrapper
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unbreakable.alarm.data.Alarm
import com.unbreakable.alarm.ui.missions.MissionsSection
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UppercaseAmPmTimePicker(
    state: TimePickerState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customContext = remember(context) {
        object : ContextWrapper(context) {
            @Suppress("DEPRECATION")
            override fun getResources(): android.content.res.Resources {
                val baseRes = super.getResources()
                return object : android.content.res.Resources(baseRes.assets, baseRes.displayMetrics, baseRes.configuration) {
                    override fun getString(id: Int): String {
                        val str = super.getString(id)
                        if (str.equals("am", ignoreCase = true) || str.equals("a.m.", ignoreCase = true)) return "A.M."
                        if (str.equals("pm", ignoreCase = true) || str.equals("p.m.", ignoreCase = true)) return "P.M."
                        return str
                    }
                    override fun getString(id: Int, vararg formatArgs: Any?): String {
                        val str = super.getString(id, *formatArgs)
                        if (str.equals("am", ignoreCase = true) || str.equals("a.m.", ignoreCase = true)) return "A.M."
                        if (str.equals("pm", ignoreCase = true) || str.equals("p.m.", ignoreCase = true)) return "P.M."
                        return str
                    }
                    override fun getText(id: Int): CharSequence {
                        val str = super.getText(id)
                        if (str.toString().equals("am", ignoreCase = true) || str.toString().equals("a.m.", ignoreCase = true)) return "A.M."
                        if (str.toString().equals("pm", ignoreCase = true) || str.toString().equals("p.m.", ignoreCase = true)) return "P.M."
                        return str
                    }
                }
            }
        }
    }
    CompositionLocalProvider(LocalContext provides customContext) {
        TimePicker(state = state, modifier = modifier)
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(viewModel: AlarmViewModel, onNavigateToAdd: () -> Unit, onNavigateToEdit: (Int) -> Unit) {
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alarm", fontSize = 28.sp, fontWeight = FontWeight.Medium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_alarm_fab").padding(bottom = 16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Alarm")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (alarms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No alarms set.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(alarms, key = { it.id }) { alarm ->
                    Box(modifier = Modifier.animateItem(
                        placementSpec = androidx.compose.animation.core.tween(400)
                    )) {
                        AlarmItem(
                            alarm = alarm,
                            onToggle = { isActive -> viewModel.toggleAlarm(alarm) },
                            onDelete = { viewModel.deleteAlarm(alarm) },
                            onEdit = { onNavigateToEdit(alarm.id) }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}
fun calculateTimeLeft(hour: Int, minute: Int, daysOfWeek: List<Int>, isActive: Boolean = true): String {
    if (!isActive) return "Not active"
    val now = java.util.Calendar.getInstance()
    val next = java.util.Calendar.getInstance()
    next.set(java.util.Calendar.HOUR_OF_DAY, hour)
    next.set(java.util.Calendar.MINUTE, minute)
    next.set(java.util.Calendar.SECOND, 0)
    next.set(java.util.Calendar.MILLISECOND, 0)
    if (daysOfWeek.isEmpty()) {
        if (next.before(now) || next.timeInMillis == now.timeInMillis) {
            next.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
    } else {
        while (true) {
            val cDay = next.get(java.util.Calendar.DAY_OF_WEEK)
            val dbDay = if (cDay == java.util.Calendar.SUNDAY) 7 else cDay - 1
            if (daysOfWeek.contains(dbDay) && next.timeInMillis > now.timeInMillis) {
                break
            }
            next.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
    }
    var diff = next.timeInMillis - now.timeInMillis
    if (diff < 0) diff = 0
    val diffHours = diff / (1000 * 60 * 60)
    val diffMinutes = (diff / (1000 * 60)) % 60
    val dDays = diffHours / 24
    val dHours = diffHours % 24
    return if (dDays > 0) {
        "Rings in $dDays d ${dHours} hr $diffMinutes min"
    } else {
        if (diffHours == 0L && diffMinutes == 0L) {
            "Ringing now..."
        } else if (diffHours == 0L) {
            "Rings in $diffMinutes min"
        } else {
            "Rings in $diffHours hr $diffMinutes min"
        }
    }
}
@Composable
fun AlarmItem(alarm: Alarm, onToggle: (Boolean) -> Unit, onDelete: () -> Unit, onEdit: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var trigger by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while(true) {
            kotlinx.coroutines.delay(10000)
            trigger++
        }
    }
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Alarm") },
            text = { Text("Are you sure you want to delete this alarm?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false; haptic.performHapticFeedback(HapticFeedbackType.LongPress) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    val amPm = if (alarm.hour >= 12) "P.M." else "A.M."
    val displayHour = if (alarm.hour == 0) 12 else if (alarm.hour > 12) alarm.hour - 12 else alarm.hour
    val displayTimeFormatted = String.format(java.util.Locale.getDefault(), "%02d:%02d", displayHour, alarm.minute)
    val targetActiveBackgroundColor = if (alarm.id % 2 == 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    val targetContainerColor = if (alarm.isActive) targetActiveBackgroundColor else MaterialTheme.colorScheme.surface
    val targetAlpha = if (alarm.isActive) 1f else 0.5f
    val containerColor by androidx.compose.animation.animateColorAsState(targetValue = targetContainerColor, animationSpec = androidx.compose.animation.core.tween(500))
    val alpha by androidx.compose.animation.core.animateFloatAsState(targetValue = targetAlpha, animationSpec = androidx.compose.animation.core.tween(500))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .clickable { onEdit() }
            .testTag("alarm_item_${alarm.id}"),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(24.dp),
        border = if (!alarm.isActive) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = displayTimeFormatted,
                        fontSize = 48.sp,
                        letterSpacing = (-1).sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.alignByBaseline()
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = amPm,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.alignByBaseline()
                    )
                }
                Switch(
                    checked = alarm.isActive,
                    onCheckedChange = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggle(it) 
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        uncheckedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.testTag("alarm_switch_${alarm.id}")
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (alarm.label.isNotBlank()) alarm.label else "Alarm",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = trigger.let { calculateTimeLeft(alarm.hour, alarm.minute, alarm.daysOfWeek, alarm.isActive) },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    if (alarm.missions.isNotEmpty()) {
                        Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            alarm.missions.forEach { mission ->
                                val icon = when(mission.type) {
                                    com.unbreakable.alarm.data.MissionType.COLOR_TILES -> Icons.Default.GridView
                                    com.unbreakable.alarm.data.MissionType.TYPING -> Icons.Default.Keyboard
                                    com.unbreakable.alarm.data.MissionType.MATH -> Icons.Default.Calculate
                                    com.unbreakable.alarm.data.MissionType.STEP -> Icons.AutoMirrored.Filled.DirectionsWalk
                                    com.unbreakable.alarm.data.MissionType.SHAKE -> Icons.Default.Vibration
                                }
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon, 
                                        contentDescription = null, 
                                        tint = MaterialTheme.colorScheme.primary, 
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Row {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val alarmLabel = if (alarm.label.isNotBlank()) alarm.label else "Alarm"
                        val serviceIntent = android.content.Intent(context, com.unbreakable.alarm.AlarmService::class.java).apply {
                            putExtra("ALARM_ID", alarm.id)
                            putExtra("ALARM_LABEL", alarmLabel)
                        }
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            context.startForegroundService(serviceIntent)
                        } else {
                            context.startService(serviceIntent)
                        }
                        val activityIntent = android.content.Intent(context, com.unbreakable.alarm.AlarmRingingActivity::class.java).apply {
                            putExtra("ALARM_ID", alarm.id)
                            putExtra("ALARM_LABEL", alarmLabel)
                            putExtra("IS_PREVIEW", true)
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        }
                        context.startActivity(activityIntent)
                    }) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Preview Alarm",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDeleteConfirm = true 
                    }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete Alarm",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAlarmScreen(viewModel: AlarmViewModel, alarm: Alarm?, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val alarms by viewModel.alarms.collectAsState()
    var label by remember { mutableStateOf(alarm?.label ?: "") }
    var missions by remember { mutableStateOf(alarm?.missions ?: emptyList()) }
    var mutePeriodSecs by remember { mutableStateOf(alarm?.mutePeriodSecs ?: 60) }
    var daysOfWeek by remember { mutableStateOf(alarm?.daysOfWeek ?: emptyList()) }
    var snoozeDuration by remember { mutableStateOf(alarm?.snoozeDuration ?: 5) }
        val currentCalendar = remember { java.util.Calendar.getInstance() }
    val initialHour = alarm?.hour ?: currentCalendar.get(java.util.Calendar.HOUR_OF_DAY)
    val initialMinute = alarm?.minute ?: currentCalendar.get(java.util.Calendar.MINUTE)
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )
    val haptic = LocalHapticFeedback.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (alarm == null) "Add Alarm" else "Edit Alarm") },
                navigationIcon = {
                    IconButton(onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateBack() 
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = label,
                    onValueChange = { label = it },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.Center) {
                            if (label.isEmpty()) {
                                Text(
                                    text = "Alarm name...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            var trigger by remember { mutableStateOf(0) }
            LaunchedEffect(Unit) {
                while(true) {
                    kotlinx.coroutines.delay(10000)
                    trigger++
                }
            }
            Text(
                text = trigger.let { calculateTimeLeft(timePickerState.hour, timePickerState.minute, daysOfWeek, true) },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            UppercaseAmPmTimePicker(
                state = timePickerState,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
            val weekDays = listOf("M", "T", "W", "T", "F", "S", "S")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                weekDays.forEachIndexed { index, day ->
                    val isSelected = daysOfWeek.contains(index + 1)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                daysOfWeek = if (isSelected) daysOfWeek - (index + 1) else daysOfWeek + (index + 1)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(day, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                Text("Snooze limit (minutes)", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(
                        value = snoozeDuration.toFloat(),
                        onValueChange = { snoozeDuration = it.toInt() },
                        onValueChangeFinished = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                        valueRange = 1f..30f,
                        steps = 28,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${snoozeDuration}m", color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            MissionsSection(missions = missions, onMissionsChange = { missions = it })
            if (missions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    Text("Mute alarm during mission", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Slider(
                            value = mutePeriodSecs.toFloat(),
                            onValueChange = { mutePeriodSecs = it.toInt() },
                            onValueChangeFinished = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                            valueRange = 0f..120f,
                            steps = 11,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (mutePeriodSecs == 0) "Off" else "${mutePeriodSecs}s", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val timeInMins = timePickerState.hour * 60 + timePickerState.minute
                    val clashing = alarms.any {
                        if (it.id != alarm?.id) {
                            val itTimeInMins = it.hour * 60 + it.minute
                            var diff = Math.abs(itTimeInMins - timeInMins)
                            if (diff > 12 * 60) diff = 24 * 60 - diff
                            diff < 1
                        } else false
                    }
                    if (clashing) {
                        android.widget.Toast.makeText(context, "Cannot set alarms within 1 minute of each other", android.widget.Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    if (alarm == null) {
                        viewModel.addAlarm(timePickerState.hour, timePickerState.minute, label, true, daysOfWeek, "Default", true, snoozeDuration, missions, mutePeriodSecs)
                    } else {
                        viewModel.updateAlarm(alarm.copy(
                            hour = timePickerState.hour,
                            minute = timePickerState.minute,
                            label = label,
                            daysOfWeek = daysOfWeek,
                            vibrate = true,
                            snoozeDuration = snoozeDuration,
                            missions = missions,
                            mutePeriodSecs = mutePeriodSecs
                        ))
                    }
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_alarm_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}