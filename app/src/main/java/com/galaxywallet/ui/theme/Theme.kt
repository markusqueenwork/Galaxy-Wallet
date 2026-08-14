package com.galaxywallet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Accent,
    background = Background,
    surface = Surface,
    onPrimary = Background,
    onBackground = TextMain,
    onSurface = TextMain
)

@Composable
fun GalaxyWalletTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
