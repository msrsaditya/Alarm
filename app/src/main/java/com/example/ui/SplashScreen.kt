package com.example.ui
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.draw.rotate
@Composable
fun AppSplashScreen(onAnimationFinished: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }
    var isPulsing by remember { mutableStateOf(false) }
    val scale = remember { Animatable(0.5f) }
    val rotation = remember { Animatable(-15f) }
    LaunchedEffect(Unit) {
        isVisible = true
        launch {
            rotation.animateTo(
                targetValue = 0f,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                )
            )
        }
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = androidx.compose.animation.core.spring(
                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                stiffness = androidx.compose.animation.core.Spring.StiffnessLow
            )
        )
        delay(600)
        isVisible = false
        delay(300)
        onAnimationFinished()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(600)) + scaleIn(initialScale = 0.5f, animationSpec = tween(600)),
            exit = fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 1.2f, animationSpec = tween(300))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_alarm_perfect),
                contentDescription = "App Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(108.dp)
                    .scale(scale.value)
                    .rotate(rotation.value)
            )
        }
    }
}