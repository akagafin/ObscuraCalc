package com.obscuracalc.app.ui.theme

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
    primary = Color(0xFF319795), // Deeper Teal for light mode
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB2F5EA),
    secondary = Color(0xFF3182CE), // Function Blue (Accessible)
    tertiary = Color(0xFF2C7A7B), // Operator Teal
    background = Color(0xFFF7FAFC), // Very Light Gray
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEDF2F7), // Light Gray for number buttons
    onBackground = Color(0xFF1A202C),
    onSurface = Color(0xFF1A202C),
    onSurfaceVariant = Color(0xFF2D3748),
    outline = Color(0xFF718096), // Muted for history text
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4FD1C5), // AccentTeal
    onPrimary = Color(0xFF0A0A0A),
    primaryContainer = Color(0xFF319795),
    secondary = Color(0xFF63B3ED), // Function Blue
    tertiary = Color(0xFF81E6D9), // Operator Teal
    background = Color(0xFF0A0A0A), // BgDark
    surface = Color(0xFF1A1A20), 
    surfaceVariant = Color(0xFF262630), // SurfaceButtonNum
    onBackground = Color(0xFFF7FAFC),
    onSurface = Color(0xFFF7FAFC),
    onSurfaceVariant = Color(0xFFE2E8F0),
    outline = Color(0xFF718096),
)

@Composable
fun ObscuraCalcTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled by default to follow custom black theme
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
