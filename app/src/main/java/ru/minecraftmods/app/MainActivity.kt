package ru.minecraftmods.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.minecraftmods.app.data.LanguagePreferences
import ru.minecraftmods.app.data.ModRepository
import ru.minecraftmods.app.data.ThemePreferences
import ru.minecraftmods.app.data.TranslatorManager
import ru.minecraftmods.app.ui.MinecraftModsTheme
import ru.minecraftmods.app.ui.SearchScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            
            // Состояния для темы и языка
            var themeKey by remember { mutableStateOf(ThemePreferences.getTheme(context)) }
            var languageKey by remember { mutableStateOf(LanguagePreferences.getTargetLanguage(context)) }
            
            var modelsReady by remember { mutableStateOf(TranslatorManager.isReady(context)) }
            var loading by remember { mutableStateOf(!modelsReady) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                ModRepository.init(context)
                if (!modelsReady) {
                    scope.launch {
                        val success = TranslatorManager.prepareModels(context, languageKey)
                        modelsReady = success
                        loading = false
                    }
                }
            }

            MinecraftModsTheme(themeMode = themeKey) {
                if (loading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFFFC107))
                    }
                } else {
                    // key() заставит приложение перерисоваться при смене темы
                    key(themeKey, languageKey) {
                        SearchScreen(
                            currentLanguage = languageKey,
                            onLanguageChanged = { languageKey = it },
                            onThemeChanged = { themeKey = it } // Передаем коллбэк вниз
                        )
                    }
                }
            }
        }
    }
}
