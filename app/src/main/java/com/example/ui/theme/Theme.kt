package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TerminalColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = TerminalBlack,
    primaryContainer = VeryDimGreen,
    onPrimaryContainer = NeonGreen,
    secondary = DimGreen,
    onSecondary = TerminalBlack,
    secondaryContainer = VeryDimGreen,
    onSecondaryContainer = DimGreen,
    tertiary = CyanAccent,
    onTertiary = TerminalBlack,
    background = TerminalBlack,
    onBackground = NeonGreen,
    surface = TerminalDark,
    onSurface = NeonGreen,
    surfaceVariant = TerminalPanelBg,
    onSurfaceVariant = DimGreen,
    outline = TerminalBorder,
    outlineVariant = TerminalBorderDim,
    error = NeonRed,
    onError = TerminalBlack
)

@Composable
fun CipherLockTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TerminalColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    CipherLockTheme(content = content)
}
