package ru.minecraftmods.app.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.minecraftmods.app.data.DownloadHistory
import ru.minecraftmods.app.data.Mod
import ru.minecraftmods.app.data.ModRepository
import ru.minecraftmods.app.data.Screenshot
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
    currentLanguage: String,
    onLanguageChanged: (String) -> Unit,
    onThemeChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(androidx.compose.material3.DrawerValue.Closed)

    var isInitialLoading by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableStateOf(0) }

    var query by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf("downloads") }
    var gameVersion by remember { mutableStateOf("") }
    var mods by remember { mutableStateOf<List<Mod>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var loadingMore by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var selectedMod by remember { mutableStateOf<Mod?>(null) }
    var screenshots by remember { mutableStateOf<List<Screenshot>>(emptyList()) }
    var loadingScreenshots by remember { mutableStateOf(false) }
    var fullscreenIndex by remember { mutableStateOf<Int?>(null) }
    var downloadingMod by remember { mutableStateOf(false) }
    var offset by remember { mutableStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }
    var showSettings by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }

    BackHandler(enabled = showSettings || showHistory || drawerState.isOpen || selectedMod != null) {
        when {
            selectedMod != null -> selectedMod = null
            showSettings -> showSettings = false
            showHistory -> showHistory = false
            drawerState.isOpen -> scope.launch { drawerState.close() }
        }
    }

    LaunchedEffect(Unit) {
        val progressJob = launch {
            while (loadProgress < 95) { delay(30); loadProgress += 1 }
        }
        try {
            val initialMods = ModRepository.search("", "all", 20, 0, "", "downloads")
            mods = initialMods
        } catch (e: Exception) {
            statusMessage = "Ошибка загрузки: ${e.message}"
        }
        progressJob.cancel()
        loadProgress = 100
        delay(300)
        isInitialLoading = false
    }

    if (showSettings) {
        SettingsScreen(
            onBack = { showSettings = false },
            onLanguageChanged = { newLang -> onLanguageChanged(newLang) },
            onThemeChanged = { newTheme -> onThemeChanged(newTheme) }
        )
        return
    }

    if (showHistory) {
        HistoryScreen(onBack = { showHistory = false })
        return
    }

    if (isInitialLoading) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                Icon(Icons.Default.Gamepad, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(24.dp))
                Text("Загрузка модов...", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { loadProgress / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
                Spacer(Modifier.height(8.dp))
                Text("$loadProgress%", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(32.dp))
                Text("Загружаем самую свежую коллекцию модов", fontSize = 14.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
        return
    }

    val categories = listOf("Мебель", "Оружие", "Броня", "Руды", "Еда", "Машины", "Животные", "Магия", "Декор", "Инструменты", "Биомы", "Структуры")

    fun doSearch(force: Boolean = false) {
        if (!force && query.isBlank()) return
        scope.launch {
            if (offset == 0) loading = true else loadingMore = true
            statusMessage = ""
            try {
                val result = ModRepository.search(query.trim(), "all", 20, offset, gameVersion, sort)
                if (offset == 0) mods = result else mods = mods + result
                hasMore = result.size >= 20
                if (mods.isEmpty()) statusMessage = "Ничего не найдено"
            } catch (e: Exception) {
                statusMessage = "Ошибка: ${e.message}"
            } finally {
                loading = false
                loadingMore = false
            }
        }
    }

    fun resetAndSearch() {
        offset = 0; hasMore = true; mods = emptyList(); doSearch(force = true)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(300.dp), drawerContainerColor = MaterialTheme.colorScheme.surface) {
                Spacer(Modifier.height(16.dp))
                Text("Фильтры", modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(16.dp))
                Text("Сортировка", modifier = Modifier.padding(horizontal = 16.dp), fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = sort == "downloads", onClick = { sort = "downloads"; resetAndSearch() }, label = { Text("По загрузкам") })
                    FilterChip(selected = sort == "updated", onClick = { sort = "updated"; resetAndSearch() }, label = { Text("По дате") })
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
                Text("Категории", modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 16.dp)) {
                    items(categories) { cat ->
                        NavigationDrawerItem(
                            label = { Text(cat) },
                            selected = query.equals(cat, ignoreCase = true),
                            onClick = { query = cat; scope.launch { drawerState.close() }; resetAndSearch() },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                NavigationDrawerItem(label = { Text("Мои загрузки", fontWeight = FontWeight.Bold) }, icon = { Icon(Icons.Default.History, null) }, selected = false, onClick = { scope.launch { drawerState.close() }; showHistory = true }, modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp))
                NavigationDrawerItem(label = { Text("Настройки", fontWeight = FontWeight.Bold) }, icon = { Icon(Icons.Default.Settings, null) }, selected = false, onClick = { scope.launch { drawerState.close() }; showSettings = true }, modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp))
                Spacer(Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ModChest", fontWeight = FontWeight.Bold) },
                    navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, "Меню") } },
                    actions = { IconButton(onClick = { showHistory = true }) { Icon(Icons.Default.History, "История") } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            bottomBar = {
                YandexBanner(adUnitId = "demo-banner-yandex")
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 12.dp)) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = query, onValueChange = { query = it }, placeholder = { Text("Поиск модов...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = { resetAndSearch() }, modifier = Modifier.fillMaxWidth(), enabled = !loading) {
                    Text(if (loading) "Поиск..." else "Найти")
                }
                Spacer(Modifier.height(8.dp))
                if (statusMessage.isNotEmpty()) {
                    Text(statusMessage, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                }
                if (loading && offset == 0) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                        items(mods) { mod ->
                            ModCard(
                                mod = mod,
                                onClick = {
                                    selectedMod = mod; loadingScreenshots = true
                                    scope.launch {
                                        try { screenshots = ModRepository.getScreenshots(mod.id, mod.source) } catch (e: Exception) { screenshots = emptyList() } finally { loadingScreenshots = false }
                                    }
                                },
                                onInstall = {
                                    if (!downloadingMod) {
                                        downloadingMod = true
                                        downloadAndInstall(scope, context, mod, onFinish = { downloadingMod = false })
                                    }
                                }
                            )
                        }
                        if (hasMore && mods.isNotEmpty()) {
                            item {
                                if (loadingMore) {
                                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                                } else {
                                    Button(onClick = { offset += 20; doSearch(force = true) }, modifier = Modifier.fillMaxWidth()) { Text("Загрузить ещё") }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (downloadingMod) {
            androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(); Spacer(Modifier.height(16.dp)); Text("Скачивание мода...")
                    }
                }
            }
        }

        selectedMod?.let { mod ->
            ModalBottomSheet(onDismissRequest = { selectedMod = null; screenshots = emptyList() }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
                ModDetailsContent(
                    mod = mod, screenshots = screenshots, loadingScreenshots = loadingScreenshots,
                    onScreenshotClick = { index -> fullscreenIndex = index },
                    onDownloadClick = {
                        if (!downloadingMod) {
                            downloadingMod = true
                            downloadAndInstall(scope, context, mod, onFinish = { downloadingMod = false })
                        }
                    }
                )
            }
        }

        if (fullscreenIndex != null && screenshots.isNotEmpty()) {
            FullScreenImageGallery(screenshots = screenshots, startIndex = fullscreenIndex!!, onDismiss = { fullscreenIndex = null })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var history by remember { mutableStateOf(DownloadHistory.getHistory(context)) }

    var selectedMod by remember { mutableStateOf<Mod?>(null) }
    var screenshots by remember { mutableStateOf<List<Screenshot>>(emptyList()) }
    var loadingScreenshots by remember { mutableStateOf(false) }
    var downloadingMod by remember { mutableStateOf(false) }

    BackHandler(enabled = selectedMod != null) { selectedMod = null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои загрузки") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Назад") } },
                actions = { IconButton(onClick = { DownloadHistory.clearHistory(context); history = emptyList() }) { Icon(Icons.Default.Delete, "Очистить") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        if (history.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Вы ещё не скачивали моды", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(history) { mod ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            selectedMod = mod; loadingScreenshots = true
                            scope.launch {
                                try { screenshots = ModRepository.getScreenshots(mod.id, mod.source) } catch (e: Exception) { screenshots = emptyList() } finally { loadingScreenshots = false }
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (!mod.iconUrl.isNullOrBlank()) {
                                AsyncImage(model = mod.iconUrl, contentDescription = null, modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)))
                            } else {
                                Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Extension, null) }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = mod.title, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Скачано ранее", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = {
                                if (!downloadingMod) {
                                    downloadingMod = true
                                    downloadAndInstall(scope, context, mod, onFinish = { downloadingMod = false })
                                }
                            }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Скачать заново", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    selectedMod?.let { mod ->
        ModalBottomSheet(onDismissRequest = { selectedMod = null; screenshots = emptyList() }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            ModDetailsContent(
                mod = mod, screenshots = screenshots, loadingScreenshots = loadingScreenshots,
                onScreenshotClick = { },
                onDownloadClick = {
                    if (!downloadingMod) {
                        downloadingMod = true
                        downloadAndInstall(scope, context, mod, onFinish = { downloadingMod = false })
                    }
                },
                isFromHistory = true
            )
        }
    }

    if (downloadingMod) {
        androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(); Spacer(Modifier.height(16.dp)); Text("Скачивание мода...")
                }
            }
        }
    }
}

@Composable
fun ModCard(mod: Mod, onClick: () -> Unit, onInstall: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (!mod.iconUrl.isNullOrBlank()) {
                AsyncImage(model = mod.iconUrl, contentDescription = null, modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)))
            } else {
                Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Extension, null) }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = mod.title, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = mod.description ?: "", fontSize = 12.sp, maxLines = 3, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
            IconButton(onClick = onInstall) { Icon(Icons.Default.Download, contentDescription = "Установить", tint = MaterialTheme.colorScheme.primary) }
        }
    }
}

@Composable
fun ModDetailsContent(
    mod: Mod,
    screenshots: List<Screenshot>,
    loadingScreenshots: Boolean,
    onScreenshotClick: (Int) -> Unit,
    onDownloadClick: () -> Unit,
    isFromHistory: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!mod.iconUrl.isNullOrBlank()) {
                AsyncImage(model = mod.iconUrl, contentDescription = null, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(mod.title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Автор: ${mod.author ?: "Неизвестно"}", fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (loadingScreenshots) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (screenshots.isNotEmpty()) {
            Text("Скриншоты", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(screenshots.size) { index ->
                    AsyncImage(
                        model = screenshots[index].url, contentDescription = null,
                        modifier = Modifier.height(180.dp).width(280.dp).clip(RoundedCornerShape(8.dp)).clickable { onScreenshotClick(index) },
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        Text("Описание", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(mod.description ?: "Нет описания", fontSize = 14.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onDownloadClick, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Download, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isFromHistory) "Скачать заново" else "Установить мод")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenImageGallery(screenshots: List<Screenshot>, startIndex: Int, onDismiss: () -> Unit) {
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { screenshots.size })
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                AsyncImage(model = screenshots[page].url, contentDescription = null, modifier = Modifier.fillMaxSize().padding(8.dp), contentScale = ContentScale.Fit)
            }
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun YandexBanner(adUnitId: String) {
    AndroidView(
        factory = { context ->
            BannerAdView(context).apply {
                // Исправлено: используем stickySize вместо inlineSize для SDK 8.x
                setAdSize(BannerAdSize.stickySize(context, 320))
                // adUnitId передается в AdRequest
                loadAd(AdRequest.Builder(adUnitId).build())
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(MaterialTheme.colorScheme.surface)
    )
}

fun downloadAndInstall(scope: CoroutineScope, context: Context, mod: Mod, onFinish: () -> Unit) {
    scope.launch {
        try {
            val files = withContext(Dispatchers.IO) { ModRepository.getFiles(mod.id, mod.source) }
            if (files.isEmpty()) {
                withContext(Dispatchers.Main) { Toast.makeText(context, "Файлы не найдены", Toast.LENGTH_SHORT).show(); onFinish() }
                return@launch
            }
            val file = files.first()
            val extension = file.filename.substringAfterLast('.', "").lowercase()
            
            if (extension !in listOf("mcaddon", "mcpack", "mcworld", "mctemplate")) {
                withContext(Dispatchers.Main) { Toast.makeText(context, "Этот файл не для телефона.", Toast.LENGTH_LONG).show(); onFinish() }
                return@launch
            }

            val targetFile = File(context.cacheDir, file.filename)
            withContext(Dispatchers.IO) {
                val client = OkHttpClient()
                val request = Request.Builder().url(file.url).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw Exception("Ошибка сервера: ${response.code}")
                    response.body?.byteStream()?.use { input ->
                        targetFile.outputStream().use { output -> input.copyTo(output) }
                    }
                }
            }

            val mimeType = when (extension) {
                "mcaddon" -> "application/x-minecraft-addon"
                "mcpack" -> "application/x-minecraft-pack"
                "mcworld" -> "application/x-minecraft-world"
                "mctemplate" -> "application/x-minecraft-template"
                else -> "application/octet-stream"
            }

            val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", targetFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            withContext(Dispatchers.Main) {
                try {
                    DownloadHistory.addMod(context, mod)
                    context.startActivity(Intent.createChooser(intent, "Открыть в Minecraft"))
                    
                    scope.launch(Dispatchers.IO) {
                        kotlinx.coroutines.delay(5000)
                        if (targetFile.exists()) targetFile.delete()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Minecraft не установлен.", Toast.LENGTH_LONG).show()
                }
                onFinish()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show(); onFinish() }
        }
    }
}
