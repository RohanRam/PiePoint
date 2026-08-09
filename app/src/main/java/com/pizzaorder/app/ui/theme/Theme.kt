package com.pizzaorder.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = OrangeAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDDD0),
    onPrimaryContainer = Color(0xFF3B0A00),
    secondary = YellowAccent,
    onSecondary = Color(0xFF1A1A00),
    secondaryContainer = Color(0xFFFFF3CC),
    onSecondaryContainer = Color(0xFF261A00),
    tertiary = Color(0xFF006C51),
    onTertiary = Color.White,
    background = BackgroundWhite,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFF0F0F0),
)

@Composable
fun PizzaOrderTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
