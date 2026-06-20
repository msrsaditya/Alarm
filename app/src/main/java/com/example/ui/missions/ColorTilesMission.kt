package com.example.ui.missions
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Difficulty
import kotlinx.coroutines.delay
import kotlin.random.Random
@Composable
fun ColorTilesMission(difficulty: Difficulty?, onComplete: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val gridSize = when (difficulty) {
        Difficulty.SUPER_EASY -> 2
        Difficulty.EASY -> 3
        Difficulty.MEDIUM -> 4
        Difficulty.HARD -> 5
        Difficulty.SUPER_HARD -> 6
        null -> 4
    }
    val targetActiveCount = when (difficulty) {
        Difficulty.SUPER_EASY -> 2
        Difficulty.EASY -> 4
        Difficulty.MEDIUM -> 6
        Difficulty.HARD -> 9
        Difficulty.SUPER_HARD -> 12
        null -> 6
    }
    var activeTiles by remember { mutableStateOf(emptySet<Int>()) }
    var selectedTiles by remember { mutableStateOf(emptySet<Int>()) }
    var isMemorizePhase by remember { mutableStateOf(true) }
    var countdown by remember { mutableStateOf(3) }
    var resetTrigger by remember { mutableStateOf(0) }
    var failed by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    LaunchedEffect(resetTrigger, gridSize) {
        if (success) {
            delay(500)
            onComplete()
            return@LaunchedEffect
        }
        if (failed) {
            delay(1000)
            failed = false
        }
        isMemorizePhase = true
        selectedTiles = emptySet()
        val newActive = mutableSetOf<Int>()
        while (newActive.size < targetActiveCount) {
            newActive.add(Random.nextInt(gridSize * gridSize))
        }
        activeTiles = newActive
        countdown = 3
        while (countdown > 0) {
            delay(1000L)
            countdown--
        }
        isMemorizePhase = false
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isMemorizePhase && !failed && !success) {
            Text("Memorize: ${countdown}s", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
        } else {
            if (success) {
                Text("Great Job!", fontSize = 24.sp, color = androidx.compose.ui.graphics.Color(0xFF4CAF50))
            } else if (failed) {
                Text("Wrong! Try again.", fontSize = 24.sp, color = MaterialTheme.colorScheme.error)
            } else {
                Text("Tap the highlighted tiles", fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Column(modifier = Modifier.fillMaxWidth().aspectRatio(1f), verticalArrangement = Arrangement.SpaceEvenly) {
            for (i in 0 until gridSize) {
                Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (j in 0 until gridSize) {
                        val index = i * gridSize + j
                        val isTarget = activeTiles.contains(index)
                        val isSelected = selectedTiles.contains(index)
                        val bgColor = if (success) {
                            androidx.compose.ui.graphics.Color(0xFF4CAF50)
                        } else if (failed) {
                            if (isTarget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.errorContainer
                        } else if (isMemorizePhase) {
                            if (isTarget) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor)
                                .clickable(enabled = !isMemorizePhase && !failed && !success) {
                                    if (!isTarget) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        failed = true
                                        isMemorizePhase = true
                                        resetTrigger++
                                    } else {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedTiles = selectedTiles + index
                                        if (selectedTiles.size == activeTiles.size) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            success = true
                                            resetTrigger++
                                        }
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}