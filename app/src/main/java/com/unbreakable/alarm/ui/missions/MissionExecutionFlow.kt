package com.unbreakable.alarm.ui.missions
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context
import com.unbreakable.alarm.data.MissionConfig
import com.unbreakable.alarm.data.MissionType
@Composable
fun MissionExecutionFlow(
    missions: List<MissionConfig>,
    mutePeriodSecs: Int,
    onCompleteAll: () -> Unit,
    onMuteRequest: () -> Unit,
    onUnmuteRequest: () -> Unit
) {
    if (missions.isEmpty()) {
        LaunchedEffect(Unit) { onCompleteAll() }
        return
    }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    var currentMissionIndex by remember { mutableStateOf(0) }
    var repetitionsCompleted by remember { mutableStateOf(0) }
    val currentMission = missions[currentMissionIndex]
    LaunchedEffect(currentMissionIndex) {
        onMuteRequest()
        if (mutePeriodSecs > 0) {
             kotlinx.coroutines.delay(mutePeriodSecs * 1000L)
             onUnmuteRequest()
        }
    }
    val onCompleteTask: () -> Unit = {
        val effectiveRepetitions = if (currentMission.type in listOf(MissionType.STEP, MissionType.SHAKE)) 1 else currentMission.repetition
        if (repetitionsCompleted + 1 < effectiveRepetitions) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            repetitionsCompleted++
        } else {
            if (currentMissionIndex + 1 < missions.size) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator.vibrate(300)
                }
                currentMissionIndex++
                repetitionsCompleted = 0
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator.vibrate(1000)
                }
                onCompleteAll()
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().systemBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "MISSION ${currentMissionIndex + 1} OF ${missions.size}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 32.dp),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = currentMission.type.name.replace("_", " "),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold
            )
            if (currentMission.type !in listOf(MissionType.STEP, MissionType.SHAKE) && currentMission.repetition > 1) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("ITERATION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${repetitionsCompleted + 1} / ${currentMission.repetition}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            key(currentMissionIndex, repetitionsCompleted) {
                when (currentMission.type) {
                    MissionType.COLOR_TILES -> ColorTilesMission(currentMission.difficulty, onCompleteTask)
                    MissionType.TYPING -> TypingMission(currentMission.difficulty, currentMission.customPhrases, onCompleteTask)
                    MissionType.MATH -> MathMission(currentMission.difficulty, onCompleteTask)
                    MissionType.STEP -> StepMission(currentMission.targetCount, onCompleteTask)
                    MissionType.SHAKE -> ShakeMission(currentMission.targetCount, onCompleteTask)
                }
            }
        }
    }
}