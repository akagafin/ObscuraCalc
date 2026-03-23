package com.obscuracalc.app.ui.theme

import android.app.Activity
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

private val LightColors = lightColorScheme(
    primary = Color(0xFF425F57),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC8E7DC),
    secondary = Color(0xFF5A6259),
    tertiary = Color(0xFF6B5C4B),
    background = Color(0xFFF4F2EC),
    surface = Color(0xFFFFFBF5),
    surfaceVariant = Color(0xFFE4E1D7),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9FCDBE),
    onPrimary = Color(0xFF14332C),
    primaryContainer = Color(0xFF27443C),
    secondary = Color(0xFFC1C9BC),
    tertiary = Color(0xFFD6C1A8),
    background = Color(0xFF111412),
    surface = Color(0xFF171A18),
    surfaceVariant = Color(0xFF30352F),
)

@Composable
fun ObscuraCalcTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
