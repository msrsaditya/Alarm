@file:Suppress("DEPRECATION")
package com.example.ui.missions
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Difficulty
import com.example.data.MissionConfig
import com.example.data.MissionType
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionSetupFlow(
    initialMission: MissionConfig?,
    onSave: (MissionConfig) -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    var step by remember { mutableStateOf(if (initialMission == null) 0 else 1) }
    var selectedType by remember { mutableStateOf(initialMission?.type ?: MissionType.MATH) }
    if (step == 0) {
        Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
            Text("Select Mission", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            val types = listOf(
                MissionType.COLOR_TILES to "Find Color Tiles",
                MissionType.TYPING to "Typing",
                MissionType.MATH to "Math",
                MissionType.STEP to "Step",
                MissionType.SHAKE to "Shake"
            )
            LazyColumn {
                items(types) { (type, label) ->
                    val icon = when(type) {
                        MissionType.COLOR_TILES -> Icons.Default.GridView
                        MissionType.TYPING -> Icons.Default.Keyboard
                        MissionType.MATH -> Icons.Default.Calculate
                        MissionType.STEP -> Icons.Default.DirectionsWalk
                        MissionType.SHAKE -> Icons.Default.Vibration
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedType = type
                                step = 1
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(label, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    } else {
        MissionConfigEditor(
            initialConfig = initialMission ?: MissionConfig(type = selectedType, difficulty = if (selectedType in listOf(MissionType.STEP, MissionType.SHAKE)) null else Difficulty.MEDIUM, targetCount = if (selectedType == MissionType.STEP) 50 else if (selectedType == MissionType.SHAKE) 30 else null, repetition = if (selectedType in listOf(MissionType.STEP, MissionType.SHAKE)) 1 else 3),
            onSave = onSave,
            onBack = { if (initialMission == null) step = 0 else onCancel() },
            isEditing = initialMission != null,
            onDelete = onDelete
        )
    }
}
@Composable
fun MissionConfigEditor(
    initialConfig: MissionConfig,
    onSave: (MissionConfig) -> Unit,
    onBack: () -> Unit,
    isEditing: Boolean,
    onDelete: () -> Unit
) {
    var difficulty by remember { mutableStateOf(initialConfig.difficulty) }
    var repetitions by remember { mutableStateOf(initialConfig.repetition) }
    var targetCount by remember { mutableStateOf(initialConfig.targetCount ?: 30) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("TypingPhrases", android.content.Context.MODE_PRIVATE) }
    var customPhrases by remember { mutableStateOf(prefs.getStringSet("phrases", emptySet())?.toList() ?: emptyList()) }
    var phraseInput by remember { mutableStateOf("") }
    val title = when(initialConfig.type) {
        MissionType.COLOR_TILES -> "Find Color Tiles"
        MissionType.TYPING -> "Typing"
        MissionType.MATH -> "Math"
        MissionType.STEP -> "Step"
        MissionType.SHAKE -> "Shake"
    }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 24.dp).imePadding().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (isEditing) {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            when (initialConfig.type) {
                MissionType.MATH -> {
                    var mathProb by remember(difficulty) { mutableStateOf(generateMathProblem(difficulty ?: Difficulty.MEDIUM)) }
                    LaunchedEffect(difficulty) {
                        while(true) {
                            kotlinx.coroutines.delay(2000)
                            mathProb = generateMathProblem(difficulty ?: Difficulty.MEDIUM)
                        }
                    }
                    Text("${mathProb.first} ${mathProb.second}", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
                MissionType.TYPING -> {
                     val context = androidx.compose.ui.platform.LocalContext.current
                     var typingProb by remember(difficulty) { mutableStateOf(getTypingPhrase(difficulty ?: Difficulty.MEDIUM, null, context)) }
                     LaunchedEffect(difficulty) {
                         while(true) {
                             kotlinx.coroutines.delay(3500)
                             typingProb = getTypingPhrase(difficulty ?: Difficulty.MEDIUM, null, context)
                         }
                     }
                     Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                         SuggestionChip(onClick = {}, label = { Text("Example Phrase") }, colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer))
                         Spacer(modifier = Modifier.height(8.dp))
                         Text(typingProb, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                     }
                }
                MissionType.STEP -> {
                    val offsetY = remember { androidx.compose.animation.core.Animatable(0f) }
                    LaunchedEffect(Unit) {
                        while(true) {
                            offsetY.animateTo(-20f, animationSpec = androidx.compose.animation.core.tween(300))
                            offsetY.animateTo(0f, animationSpec = androidx.compose.animation.core.tween(300))
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.DirectionsWalk, contentDescription = null, modifier = Modifier.size(64.dp).offset(y = offsetY.value.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                MissionType.SHAKE -> {
                    val rotation = remember { androidx.compose.animation.core.Animatable(0f) }
                    LaunchedEffect(Unit) {
                        while(true) {
                            rotation.animateTo(-15f, animationSpec = androidx.compose.animation.core.tween(50))
                            rotation.animateTo(15f, animationSpec = androidx.compose.animation.core.tween(100))
                            rotation.animateTo(-15f, animationSpec = androidx.compose.animation.core.tween(100))
                            rotation.animateTo(0f, animationSpec = androidx.compose.animation.core.tween(50))
                            kotlinx.coroutines.delay(500)
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(64.dp).rotate(rotation.value), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                MissionType.COLOR_TILES -> {
                    val gridSize = when (difficulty) {
                        Difficulty.SUPER_EASY -> 2; Difficulty.EASY -> 3; Difficulty.MEDIUM -> 4; Difficulty.HARD -> 5; Difficulty.SUPER_HARD -> 6; null -> 4
                    }
                    var activeTiles by remember(gridSize) { mutableStateOf(setOf(0, 1)) }
                    LaunchedEffect(gridSize) {
                        while(true) {
                            val newActive = mutableSetOf<Int>()
                            while(newActive.size < gridSize) { newActive.add(kotlin.random.Random.nextInt(gridSize*gridSize)) }
                            activeTiles = newActive
                            kotlinx.coroutines.delay(1000)
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp), contentAlignment = Alignment.Center) {
                        Column(modifier = Modifier.aspectRatio(1f).fillMaxHeight()) {
                            for (i in 0 until gridSize) {
                                Row(modifier = Modifier.weight(1f)) {
                                    for (j in 0 until gridSize) {
                                        val idx = i * gridSize + j
                                        Box(modifier = Modifier.weight(1f).padding(2.dp).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(if (activeTiles.contains(idx)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (initialConfig.type in listOf(MissionType.MATH, MissionType.COLOR_TILES, MissionType.TYPING)) {
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    val diffString = difficulty?.name?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Medium"
                    Text(diffString, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    val diffs = Difficulty.values()
                    val selectedIndex = diffs.indexOf(difficulty ?: Difficulty.MEDIUM).toFloat()
                    Slider(
                        value = selectedIndex,
                        onValueChange = { difficulty = diffs[it.toInt()] },
                        valueRange = 0f..4f,
                        steps = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Easy", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Hard", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = { if (repetitions > 1) repetitions-- }) { Text("-", fontSize = 24.sp) }
                    Text("$repetitions", fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp))
                    IconButton(onClick = { repetitions++ }) { Text("+", fontSize = 24.sp) }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("times", fontSize = 18.sp)
                }
            }
            if (initialConfig.type == MissionType.TYPING) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Custom phrases (optional)", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = phraseInput,
                            onValueChange = { phraseInput = it },
                            placeholder = { Text("Add custom phrase...") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            if (phraseInput.isNotBlank()) {
                                customPhrases = customPhrases + phraseInput
                                prefs.edit().putStringSet("phrases", customPhrases.toSet()).apply()
                                phraseInput = ""
                            }
                        }) {
                            Text("Add")
                        }
                    }
                    if (customPhrases.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            customPhrases.forEach { phrase ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(phrase, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { 
                                        customPhrases = customPhrases.filter { it != phrase } 
                                        prefs.edit().putStringSet("phrases", customPhrases.toSet()).apply()
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = { if (targetCount > 5) targetCount -= 5 }) { Text("-", fontSize = 24.sp) }
                    Text("$targetCount", fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp))
                    IconButton(onClick = { targetCount += 5 }) { Text("+", fontSize = 24.sp) }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(if (initialConfig.type == MissionType.STEP) "Steps" else "times", fontSize = 18.sp)
                }
            }
        }
        Button(
            onClick = {
                onSave(initialConfig.copy(difficulty = difficulty, repetition = if (initialConfig.type in listOf(MissionType.STEP, MissionType.SHAKE)) 1 else repetitions, targetCount = targetCount, customPhrases = if (customPhrases.isEmpty()) null else customPhrases))
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Complete", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}