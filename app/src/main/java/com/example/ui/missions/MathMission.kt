package com.example.ui.missions
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Difficulty
import kotlin.random.Random
@Composable
fun MathMission(difficulty: Difficulty?, onComplete: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var problem by remember { mutableStateOf(generateMathProblem(difficulty ?: Difficulty.MEDIUM)) }
    var userInput by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(0) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    LaunchedEffect(status) {
        if (status == 1) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            kotlinx.coroutines.delay(300)
            onComplete()
        } else if (status == -1) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(problem.first, fontSize = 48.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        val containerColor = when (status) {
            1 -> MaterialTheme.colorScheme.primaryContainer
            -1 -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.surface
        }
        val textColor = when (status) {
            1 -> MaterialTheme.colorScheme.onPrimaryContainer
            -1 -> MaterialTheme.colorScheme.onErrorContainer
            else -> MaterialTheme.colorScheme.onSurface
        }
        OutlinedTextField(
            value = userInput,
            onValueChange = { input -> 
                val cleanInput = input.trim()
                if (cleanInput.length <= problem.second.toString().length + 1) {
                    userInput = cleanInput
                }
                val targetStr = problem.second.toString()
                if (userInput == targetStr) {
                    status = 1
                } else if (userInput.length >= targetStr.length) {
                    status = -1
                } else {
                    status = 0
                }
            },
            textStyle = TextStyle(fontSize = 32.sp, textAlign = TextAlign.Center, color = textColor),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = containerColor,
                unfocusedContainerColor = containerColor,
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
        )
    }
}
fun generateMathProblem(difficulty: Difficulty): Pair<String, Int> {
    return when(difficulty) {
        Difficulty.SUPER_EASY -> {
            val a = Random.nextInt(1, 10)
            val b = Random.nextInt(1, 10)
            Pair("$a + $b =", a + b)
        }
        Difficulty.EASY -> {
            val a = Random.nextInt(10, 100)
            val b = Random.nextInt(1, 100)
            Pair("$a + $b =", a + b)
        }
        Difficulty.MEDIUM -> {
            val a = Random.nextInt(100, 1000)
            val b = Random.nextInt(10, 1000)
            Pair("$a + $b =", a + b)
        }
        Difficulty.HARD -> {
            val op = Random.nextInt(3)
            if (op == 0) {
                val a = Random.nextInt(10, 500)
                val b = Random.nextInt(10, 500)
                Pair("$a + $b =", a + b)
            } else if (op == 1) {
                val a = Random.nextInt(50, 500)
                val b = Random.nextInt(10, a)
                Pair("$a - $b =", a - b)
            } else {
                val a = Random.nextInt(5, 20)
                val b = Random.nextInt(5, 20)
                Pair("$a x $b =", a * b)
            }
        }
        Difficulty.SUPER_HARD -> {
             val op = Random.nextInt(4)
            if (op == 0) {
                val a = Random.nextInt(100, 1000)
                val b = Random.nextInt(100, 1000)
                Pair("$a + $b =", a + b)
            } else if (op == 1) {
                val a = Random.nextInt(100, 1000)
                val b = Random.nextInt(10, a)
                Pair("$a - $b =", a - b)
            } else if (op == 2) {
                val a = Random.nextInt(10, 50)
                val b = Random.nextInt(10, 50)
                Pair("$a x $b =", a * b)
            } else {
                val b = Random.nextInt(5, 30)
                val ret = Random.nextInt(5, 30)
                val a = b * ret
                Pair("$a ÷ $b =", ret)
            }
        }
    }
}