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

private val DarkColorScheme = darkColorScheme(
    primary = TelemetryCyan,
    onPrimary = Color.Black,
    primaryContainer = TelemetryCyanDim,
    onPrimaryContainer = TextPrimary,
    secondary = TelemetryGreen,
    onSecondary = Color.Black,
    tertiary = TelemetryPurple,
    background = TelemetryObsidian,
    onBackground = TextPrimary,
    surface = TelemetrySurface,
    onSurface = TextPrimary,
    surfaceVariant = TelemetrySurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = TelemetryCardBorder,
    error = TelemetryRed,
    onError = Color.White
)

private val LightColorScheme = DarkColorScheme // Always use dark telemetry theme for quantitative clarity

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
