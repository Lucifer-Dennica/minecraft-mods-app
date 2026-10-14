package ru.minecraftmods.app.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.minecraftmods.app.data.Mod
import ru.minecraftmods.app.data.ModRepository
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    var query by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("all") }
    var sort by remember { mutableStateOf("downloads") }
    var gameVersion by remember { mutableStateOf("") }
    var mods by remember { mutableStateOf<List<Mod>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var loadingMore by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var selectedMod by remember { mutableStateOf<Mod?>(null) }
    var offset by remember { mutableStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }

    val categories = listOf(
        "Приключение", "Оптимизация", "Технологии", "Магия", "Мобы",
        "Снаряжение", "Еда", "Декор", "Транспорт", "Хранение",
        "Сражения", "Квесты", "Библиотека", "Утилиты", "Реализм", "Мультиплеер"
    )

    val gameVersions = listOf(
        "1.21", "1.20", "1.19", "1.18", "1.17", "1.16"
    )

    fun resetAndSearch() {
        offset = 0
        hasMore = true
        mods = emptyList()
        doSearch()
    }

    fun doSearch() {
        if (query.isBlank() && gameVersion.isBlank()) return
        scope.launch {
            if (offset == 0) loading = true else loadingMore = true
            statusMessage = ""
            try {
                val result = ModRepository.search(
                    query = query.trim(),
                    source = source,
                    limit = 20,
                    offset = offset,
                    gameVersion = gameVersion,
                    sort = sort
                )
                if (offset == 0) {
                    mods = result
                } else {
                    mods = mods + result
                }
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Фильтры",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(Modifier.height(16.dp))

                Text("Источник", modifier = Modifier.padding(horizontal = 16.dp), fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = source == "all",
                        onClick = { source = "all"; resetAndSearch() },
                        label = { Text("Все") }
                    )
                    FilterChip(
                        selected = source == "modrinth",
                        onClick = { source = "modrinth"; resetAndSearch() },
                        label = { Text("Modrinth") }
                    )
                    FilterChip(
                        selected = source == "curseforge",
                        onClick = { source = "curseforge"; resetAndSearch() },
                        label = { Text("CurseForge") }
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text("Сортировка", modifier = Modifier.padding(horizontal = 16.dp), fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = sort == "downloads",
                        onClick = { sort = "downloads"; resetAndSearch() },
                        label = { Text("По загрузкам") }
                    )
                    FilterChip(
                        selected = sort == "updated",
                        onClick = { sort = "updated"; resetAndSearch() },
                        label = { Text("По дате") }
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text("Версия игры", modifier = Modifier.padding(horizontal = 16.dp), fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = gameVersion == "",
                            onClick = { gameVersion = ""; resetAndSearch() },
                            label = { Text("Любая") }
                        )
                    }
                    items(gameVersions) { version ->
                        FilterChip(
                            selected = gameVersion == version,
                            onClick = { gameVersion = version; resetAndSearch() },
                            label = { Text(version) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                Text(
                    "Категории",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(categories) { cat ->
                        NavigationDrawerItem(
                            label = { Text(cat) },
                            selected = query.equals(cat, ignoreCase = true),
                            onClick = {
                                query = cat
                                scope.launch { drawerState.close() }
                                resetAndSearch()
                            },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Моды для Minecraft", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Меню")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Поиск модов на русском...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { resetAndSearch() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading && (query.isNotBlank() || gameVersion.isNotBlank())
                ) {
                    Text(if (loading) "Поиск..." else "Найти")
                }

                Spacer(Modifier.height(8.dp))

                if (statusMessage.isNotEmpty()) {
                    Text(statusMessage, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                }

                if (loading && offset == 0) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(mods) { mod ->
                            ModCard(
                                mod = mod,
                                onClick = { selectedMod = mod },
                                onInstall = { downloadAndInstall(context, mod) }
                            )
                        }

                        if (hasMore && mods.isNotEmpty()) {
                            item {
                                if (loadingMore) {
                                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            offset += 20
                                            doSearch()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Загрузить ещё")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Модальное окно с деталями мода
        selectedMod?.let { mod ->
            ModalBottomSheet(
                onDismissRequest = { selectedMod = null },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                ModDetailsContent(mod = mod, context = context)
            }
        }
    }
}

@Composable
fun ModCard(mod: Mod, onClick: () -> Unit, onInstall: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!mod.iconUrl.isNullOrBlank()) {
                AsyncImage(
                    model = mod.iconUrl,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Extension, contentDescription = null)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mod.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = mod.description ?: "",
                    fontSize = 12.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${mod.source} • ${mod.downloads} загрузок",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            IconButton(onClick = onInstall) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "Установить",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ModDetailsContent(mod: Mod, context: Context) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!mod.iconUrl.isNullOrBlank()) {
                AsyncImage(
                    model = mod.iconUrl,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp))
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(mod.title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Автор: ${mod.author ?: "Неизвестно"}", fontSize = 14.sp)
                Text("Источник: ${mod.source}", fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Описание", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Text(mod.description ?: "Нет описания", fontSize = 14.sp)

        if (!mod.gameVersions.isNullOrEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("Поддерживаемые версии", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Text(mod.gameVersions.joinToString(", "), fontSize = 14.sp)
        }

        Spacer(Modifier.height(16.dp))
        Text("Загрузок: ${mod.downloads}", fontSize = 14.sp)

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { downloadAndInstall(context, mod) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Download, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Установить мод")
        }
    }
}

fun downloadAndInstall(context: Context, mod: Mod) {
    Thread {
        try {
            val files = kotlinx.coroutines.runBlocking {
                ModRepository.getFiles(mod.id, mod.source)
            }
            if (files.isEmpty()) return@Thread

            val file = files.first()
            val targetFile = File(context.cacheDir, file.filename)

            val client = OkHttpClient()
            val request = Request.Builder().url(file.url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@Thread
                response.body?.byteStream()?.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                targetFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/octet-stream")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }.start()
}
