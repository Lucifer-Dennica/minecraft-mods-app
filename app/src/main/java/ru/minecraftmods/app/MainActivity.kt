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
import androidx.compose.ui.platform.LocalContext
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
                var modelsReady by remember { mutableStateOf(TranslatorManager.isReady(context)) }
                var loading by remember { mutableStateOf(!modelsReady) }
                var error by remember { mutableStateOf<String?>(null) }
                val scope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    if (!modelsReady) {
                        scope.launch {
                            val targetLang = LanguagePreferences.getTargetLanguage(context)
                            val success = TranslatorManager.prepareModels(context, targetLang)
                            if (success) {
                                modelsReady = true
                            } else {
                                error = "Не удалось загрузить модель перевода. Проверьте интернет."
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
                                Text("Это нужно только один раз", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    error != null -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(error!!)
                        }
                    }
                    else -> SearchScreen()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        TranslatorManager.close()
    }
}
