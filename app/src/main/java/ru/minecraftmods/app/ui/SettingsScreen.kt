package ru.minecraftmods.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ru.minecraftmods.app.data.LanguagePreferences
import ru.minecraftmods.app.data.ThemePreferences
import ru.minecraftmods.app.data.TranslatorManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLanguageChanged: (String) -> Unit,
    onThemeChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var currentLang by remember { mutableStateOf(LanguagePreferences.getTargetLanguage(context)) }
    var currentTheme by remember { mutableStateOf(ThemePreferences.getTheme(context)) }
    var downloading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- БЛОК ТЕМЫ ОФОРМЛЕНИЯ ---
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Тема оформления",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(Modifier.height(8.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = currentTheme == ThemePreferences.THEME_DARK,
                        onClick = { 
                            ThemePreferences.setTheme(context, ThemePreferences.THEME_DARK)
                            currentTheme = ThemePreferences.THEME_DARK
                            onThemeChanged(ThemePreferences.THEME_DARK) 
                        },
                        label = { Text("Темная") }
                    )
                    FilterChip(
                        selected = currentTheme == ThemePreferences.THEME_LIGHT,
                        onClick = { 
                            ThemePreferences.setTheme(context, ThemePreferences.THEME_LIGHT)
                            currentTheme = ThemePreferences.THEME_LIGHT
                            onThemeChanged(ThemePreferences.THEME_LIGHT) 
                        },
                        label = { Text("Светлая") }
                    )
                    FilterChip(
                        selected = currentTheme == ThemePreferences.THEME_AMOLED,
                        onClick = { 
                            ThemePreferences.setTheme(context, ThemePreferences.THEME_AMOLED)
                            currentTheme = ThemePreferences.THEME_AMOLED
                            onThemeChanged(ThemePreferences.THEME_AMOLED) 
                        },
                        label = { Text("AMOLED") }
                    )
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
            }

            // --- БЛОК ЯЗЫКА ---
            item {
                Text(
                    "Язык приложения",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Модель перевода скачается один раз (~30 МБ) и будет работать офлайн.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(16.dp))
            }

            items(TranslatorManager.SUPPORTED_LANGUAGES) { lang ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (lang.code == currentLang || downloading) return@Card
                        downloading = true
                        statusMessage = "Скачивание модели для ${lang.displayName}..."
                        scope.launch {
                            LanguagePreferences.setTargetLanguage(context, lang.code)
                            val success = TranslatorManager.prepareModels(context, lang.code)
                            if (success) {
                                currentLang = lang.code
                                statusMessage = "Язык изменён на ${lang.displayName}"
                                onLanguageChanged(lang.code)
                            } else {
                                statusMessage = "Не удалось скачать модель. Проверьте интернет."
                            }
                            downloading = false
                        }
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (lang.code == currentLang)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(lang.flag, fontSize = 28.sp)
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                lang.displayName,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Text(
                                lang.code.uppercase(),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        if (lang.code == currentLang) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // --- СТАТУС И ИНДИКАТОР ЗАГРУЗКИ ---
            item {
                Spacer(Modifier.height(16.dp))
                if (downloading) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                if (statusMessage.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        statusMessage,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
