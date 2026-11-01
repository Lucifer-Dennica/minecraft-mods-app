package ru.minecraftmods.app.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import kotlinx.coroutines.launch
import ru.minecraftmods.app.data.LanguagePreferences
import ru.minecraftmods.app.data.SettingsPreferences
import ru.minecraftmods.app.data.ThemePreferences
import ru.minecraftmods.app.data.TranslatorManager

// Твой реальный ID для баннера
private const val BANNER_AD_UNIT_ID = "R-M-20308863-1"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLanguageChanged: (String) -> Unit,
    onThemeChanged: (String) -> Unit
) {
    // Состояния для навигации внутри настроек
    var showAboutScreen by remember { mutableStateOf(false) }
    var showLanguageScreen by remember { mutableStateOf(false) }

    // Обработка системной кнопки "Назад"
    BackHandler(enabled = showAboutScreen || showLanguageScreen) {
        if (showAboutScreen) showAboutScreen = false
        else if (showLanguageScreen) showLanguageScreen = false
    }

    if (showAboutScreen) {
        AboutScreen(onBack = { showAboutScreen = false })
        return
    }

    if (showLanguageScreen) {
        LanguageScreen(
            onBack = { showLanguageScreen = false },
            onLanguageChanged = onLanguageChanged
        )
        return
    }

    // --- ГЛАВНЫЙ ЭКРАН НАСТРОЕК ---
    val context = LocalContext.current
    var currentTheme by remember { mutableStateOf(ThemePreferences.getTheme(context)) }
    var autoDelete by remember { mutableStateOf(SettingsPreferences.isAutoDeleteEnabled(context)) }
    var gameVersion by remember { mutableStateOf(SettingsPreferences.getGameVersion(context)) }
    
    val availableVersions = listOf("1.21", "1.20", "1.19", "1.18", "1.17", "1.16")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Назад") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            // Баннер рекламы внизу настроек
            AndroidView(
                factory = { ctx ->
                    BannerAdView(ctx).apply {
                        setAdSize(BannerAdSize.fixed(ctx, 320, 50))
                        loadAd(AdRequest.Builder(BANNER_AD_UNIT_ID).build())
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp).background(MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ТЕМА
            item {
                Spacer(Modifier.height(8.dp))
                Text("Тема оформления", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = currentTheme == ThemePreferences.THEME_DARK, onClick = { ThemePreferences.setTheme(context, ThemePreferences.THEME_DARK); currentTheme = ThemePreferences.THEME_DARK; onThemeChanged(ThemePreferences.THEME_DARK) }, label = { Text("Темная") })
                    FilterChip(selected = currentTheme == ThemePreferences.THEME_LIGHT, onClick = { ThemePreferences.setTheme(context, ThemePreferences.THEME_LIGHT); currentTheme = ThemePreferences.THEME_LIGHT; onThemeChanged(ThemePreferences.THEME_LIGHT) }, label = { Text("Светлая") })
                    FilterChip(selected = currentTheme == ThemePreferences.THEME_AMOLED, onClick = { ThemePreferences.setTheme(context, ThemePreferences.THEME_AMOLED); currentTheme = ThemePreferences.THEME_AMOLED; onThemeChanged(ThemePreferences.THEME_AMOLED) }, label = { Text("AMOLED") })
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
            }

            // ВЕРСИЯ ИГРЫ
            item {
                Text("Версия Minecraft", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Влияет на совместимость модов", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableVersions.forEach { version ->
                        FilterChip(
                            selected = gameVersion == version,
                            onClick = { SettingsPreferences.setGameVersion(context, version); gameVersion = version },
                            label = { Text(version) }
                        )
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
            }

            // АВТОУДАЛЕНИЕ
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Автоудаление модов", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Удалять файл после установки в игру", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Switch(
                        checked = autoDelete,
                        onCheckedChange = { 
                            SettingsPreferences.setAutoDeleteEnabled(context, it)
                            autoDelete = it 
                        }
                    )
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
            }

            // ПЕРЕХОД К ЯЗЫКАМ
            item {
                SettingItem(
                    icon = Icons.Default.Language,
                    title = "Язык приложения",
                    subtitle = "Текущий: ${TranslatorManager.SUPPORTED_LANGUAGES.find { it.code == LanguagePreferences.getTargetLanguage(context) }?.displayName ?: "Русский"}",
                    onClick = { showLanguageScreen = true }
                )
            }

            // ПЕРЕХОД К "О ПРИЛОЖЕНИИ"
            item {
                SettingItem(
                    icon = Icons.Default.Info,
                    title = "О приложении",
                    subtitle = "Поддержка, донаты, отзывы",
                    onClick = { showAboutScreen = true }
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// --- ВСПОМОГАТЕЛЬНЫЙ КОМПОНЕНТ ДЛЯ ПУНКТА МЕНЮ ---
@Composable
fun SettingItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

// --- ЭКРАН ЯЗЫКОВ ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(onBack: () -> Unit, onLanguageChanged: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentLang by remember { mutableStateOf(LanguagePreferences.getTargetLanguage(context)) }
    var downloading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Язык приложения") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Назад") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Text(
                    "Модель перевода скачается один раз и будет работать офлайн.",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
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
                                statusMessage = "Ошибка загрузки. Проверьте интернет."
                            }
                            downloading = false
                        }
                    },
                    colors = CardDefaults.cardColors(containerColor = if (lang.code == currentLang) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(lang.flag, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(lang.displayName, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                            Text(lang.code.uppercase(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        if (lang.code == currentLang) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            item {
                if (downloading) Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                if (statusMessage.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(statusMessage, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// --- ЭКРАН "О ПРИЛОЖЕНИИ" ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    // Функция для безопасного открытия ссылок
    fun openLink(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            // Обработка, если нет браузера
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("О приложении") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Назад") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Шапка
            item {
                Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Text("ModChest", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                Text("Версия 1.0.0", fontSize = 14.sp, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Удобный менеджер модов для Minecraft Bedrock. Мы делаем установку модов максимально простой и безопасной.",
                    textAlign = TextAlign.Center, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                HorizontalDivider(Modifier.padding(vertical = 16.dp))
            }

            // Разработчик
            item {
                Text("Разработчик", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                SettingItem(icon = Icons.Default.Email, title = "Почта", subtitle = "denis22142qwe@gmail.com") {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:denis22142qwe@gmail.com"))
                    context.startActivity(intent)
                }
                Spacer(Modifier.height(8.dp))
                SettingItem(icon = Icons.Default.Send, title = "Telegram", subtitle = "@Lucifer_Denicca_22142") {
                    openLink("https://t.me/Lucifer_Denicca_22142")
                }
                Spacer(Modifier.height(8.dp))
                SettingItem(icon = Icons.Default.Language, title = "Мой сайт", subtitle = "Другие приложения и проекты") {
                    openLink("https://lucifer-dennica.github.io/apps.html")
                }
                HorizontalDivider(Modifier.padding(vertical = 16.dp))
            }

            // Донаты
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Поддержите разработчика", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Если вам нравится приложение и вы хотите помочь ему развиваться, вы можете поддержать меня донатом. Это помогает оплачивать серверы и добавлять новые функции!",
                            textAlign = TextAlign.Center, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { openLink("https://www.donationalerts.com/r/lucifer_dennica_1999") }) {
                            Icon(Icons.Default.Favorite, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Поддержать донатом")
                        }
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 16.dp))
            }

            // Отзыв
            item {
                Text("Оставьте отзыв", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(4.dp))
                Text(
                    "Ваш отзыв в магазине приложений (RuStore) — это лучшая мотивация развивать проект. Напишите, что вам нравится, или что мы можем улучшить. Это займет всего минуту!",
                    fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(12.dp))
                // Здесь можно добавить кнопку перехода в RuStore (если знаешь package name)
                // Button(onClick = { openLink("https://www.rustore.ru/catalog/app/ru.minecraftmods.app") }) {
                //     Text("Оставить отзыв в RuStore")
                // }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
