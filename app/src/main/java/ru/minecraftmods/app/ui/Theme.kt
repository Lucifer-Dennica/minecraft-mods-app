package ru.minecraftmods.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ru.minecraftmods.app.data.ThemePreferences

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFC107), onPrimary = Color.Black,
    background = Color(0xFF1A1A1A), surface = Color(0xFF2A2A2A),
    onBackground = Color.White, onSurface = Color.White
)

private val LightColors = lightColorScheme(
    primary = Color(0xFFFFC107), onPrimary = Color.Black,
    background = Color(0xFFF5F5F5), surface = Color.White,
    onBackground = Color.Black, onSurface = Color.Black
)

private val AmoledColors = darkColorScheme(
    primary = Color(0xFFFFC107), onPrimary = Color.Black,
    background = Color.Black, surface = Color(0xFF121212),
    onBackground = Color.White, onSurface = Color.White
)

@Composable
fun MinecraftModsTheme(themeMode: String, content: @Composable () -> Unit) {
    val colors = when (themeMode) {
        ThemePreferences.THEME_LIGHT -> LightColors
        ThemePreferences.THEME_AMOLED -> AmoledColors
        ThemePreferences.THEME_SYSTEM -> if (isSystemInDarkTheme()) DarkColors else LightColors
        else -> DarkColors
    }

    MaterialTheme(colorScheme = colors, content = content)
}
