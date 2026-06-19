package com.example.ui.missions
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Difficulty
import kotlin.random.Random
import kotlinx.coroutines.delay
@Composable
fun TypingMission(difficulty: Difficulty?, customPhrases: List<String>?, onComplete: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var phrase by remember { mutableStateOf(getTypingPhrase(difficulty ?: Difficulty.MEDIUM, customPhrases, context)) }
    var userInput by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(0) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    LaunchedEffect(status) {
        if (status == 1) {
            delay(300)
            onComplete()
        }
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).imePadding().verticalScroll(androidx.compose.foundation.rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Type exactly:", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Text(phrase, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
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
            onValueChange = { 
                userInput = it 
                if (userInput == phrase) {
                    status = 1
                } else if (!phrase.startsWith(userInput)) {
                    status = -1
                } else {
                    status = 0
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = containerColor,
                unfocusedContainerColor = containerColor,
            ),
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            textStyle = TextStyle(fontSize = 20.sp, color = textColor)
        )
    }
}
fun getTypingPhrase(difficulty: Difficulty, customPhrases: List<String>?, context: android.content.Context): String {
    val prefs = context.getSharedPreferences("TypingPhrases", android.content.Context.MODE_PRIVATE)
    val savedPhrases = prefs.getStringSet("phrases", emptySet())?.toList() ?: emptyList()
    if (savedPhrases.isNotEmpty()) {
        val index = prefs.getInt("current_index", 0)
        if (index < savedPhrases.size) {
            val phraseToUse = savedPhrases[index]
            prefs.edit().putInt("current_index", index + 1).apply()
            return phraseToUse
        }
    }
    return when (difficulty) {
        Difficulty.SUPER_EASY -> SUPER_EASY_PHRASES.random()
        Difficulty.EASY -> EASY_PHRASES.random()
        Difficulty.MEDIUM -> MEDIUM_PHRASES.random()
        Difficulty.HARD -> HARD_PHRASES.random()
        Difficulty.SUPER_HARD -> SUPER_HARD_PHRASES.random()
    }
}