package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PolishPrimaryLight,
    onPrimary = PolishOnPrimaryContainer,
    primaryContainer = PolishPrimaryDark,
    onPrimaryContainer = PolishPrimaryContainer,
    secondary = PolishTealContainer,
    background = Color(0xFF19120D),
    surface = Color(0xFF241C15),
    surfaceVariant = Color(0xFF332920),
    onBackground = Color(0xFFEDE0D8),
    onSurface = Color(0xFFEDE0D8)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PolishPrimary,
    onPrimary = Color.White,
    primaryContainer = PolishPrimaryContainer,
    onPrimaryContainer = PolishOnPrimaryContainer,
    secondary = PolishTeal,
    onSecondary = Color.White,
    secondaryContainer = PolishTealBadge,
    onSecondaryContainer = PolishTeal,
    tertiary = PolishPurpleText,
    tertiaryContainer = PolishPurpleContainer,
    background = PolishBackground,
    surface = PolishSurface,
    surfaceVariant = PolishBorder,
    onBackground = PolishTextPrimary,
    onSurface = PolishTextPrimary,
    outline = PolishBorderDarker,
    outlineVariant = PolishBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Keep the distinctive custom theme consistent
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

