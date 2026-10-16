package ru.minecraftmods.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.minecraftmods.app.data.LanguagePreferences
import ru.minecraftmods.app.data.TranslatorManager
import ru.minecraftmods.app.ui.MinecraftModsTheme
import ru.minecraftmods.app.ui.SearchScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MinecraftModsTheme {
                val context = LocalContext.current
                var languageKey by remember { mutableStateOf(LanguagePreferences.getTargetLanguage(context)) }
                var modelsReady by remember { mutableStateOf(TranslatorManager.isReady(context)) }
                var loading by remember { mutableStateOf(!modelsReady) }
                var error by remember { mutableStateOf<String?>(null) }
                val scope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    if (!modelsReady) {
                        scope.launch {
                            val currentLang = LanguagePreferences.getTargetLanguage(context)
                            val success = TranslatorManager.prepareModels(context, currentLang)
                            if (success) {
                                modelsReady = true
                            } else {
                                error = "Не удалось загрузить модель перевода.\nПроверьте интернет и перезапустите приложение."
                            }
                            loading = false
                        }
                    }
                }

                when {
                    loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(16.dp))
                                Text("Загрузка модели перевода...")
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Это нужно только один раз",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    error != null -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                error!!,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    }
                    else -> {
                        // key() пересоздаст SearchScreen при смене языка
                        key(languageKey) {
                            SearchScreen(
                                currentLanguage = languageKey,
                                onLanguageChanged = { newLang ->
                                    languageKey = newLang
                                }
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
