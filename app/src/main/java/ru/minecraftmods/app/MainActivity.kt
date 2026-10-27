package ru.minecraftmods.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
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
            
            var themeKey by remember { mutableStateOf(ThemePreferences.getTheme(context)) }
            var languageKey by remember { mutableStateOf(LanguagePreferences.getTargetLanguage(context)) }
            
            var modelsReady by remember { mutableStateOf(TranslatorManager.isReady(context)) }
            var loading by remember { mutableStateOf(!modelsReady) }
            var error by remember { mutableStateOf<String?>(null) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                ModRepository.init(context)
                if (!modelsReady) {
                    scope.launch {
                        val success = TranslatorManager.prepareModels(context, languageKey)
                        if (success) {
                            modelsReady = true
                        } else {
                            error = "Не удалось загрузить модель перевода.\nПроверьте интернет и перезапустите приложение."
                        }
                        loading = false
                    }
                }
            }

            MinecraftModsTheme(themeMode = themeKey) {
                when {
                    loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(16.dp))
                                Text("Загрузка модели перевода...", color = MaterialTheme.colorScheme.onBackground)
                                Spacer(Modifier.height(8.dp))
                                Text("Это займет всего минуту и нужно только один раз", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                            }
                        }
                    }
                    error != null -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(error!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp))
                        }
                    }
                    else -> {
                        key(themeKey, languageKey) {
                            SearchScreen(
                                currentLanguage = languageKey,
                                onLanguageChanged = { newLang -> languageKey = newLang },
                                onThemeChanged = { newTheme -> themeKey = newTheme }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        TranslatorManager.close()
    }
}
