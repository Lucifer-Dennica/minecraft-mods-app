package ru.minecraftmods.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFC107),
    onPrimary = Color.Black,
    background = Color(0xFF1A1A1A),
    surface = Color(0xFF2A2A2A),
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun MinecraftModsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else DarkColors,
        content = content
    )
}
