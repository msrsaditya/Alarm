package com.unbreakable.alarm.ui.theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
private val DarkGeometricColorScheme =
  darkColorScheme(
      primary = DarkGeometricPrimary,
      onPrimary = DarkGeometricBackground,
      primaryContainer = DarkGeometricPrimaryContainer,
      onPrimaryContainer = DarkGeometricText,
      secondaryContainer = DarkGeometricSecondaryContainer,
      onSecondaryContainer = DarkGeometricText,
      background = DarkGeometricBackground,
      onBackground = DarkGeometricText,
      surface = DarkGeometricSurface,
      onSurface = DarkGeometricText,
      surfaceVariant = DarkGeometricSurfaceVariant,
      onSurfaceVariant = DarkGeometricMuted,
      outline = DarkGeometricOutline,
      error = DarkError
  )
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = DarkGeometricColorScheme, typography = Typography, content = content)
}