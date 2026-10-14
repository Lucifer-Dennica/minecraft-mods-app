package ru.minecraftmods.app.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
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

    var query by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("all") }
    var mods by remember { mutableStateOf<List<Mod>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    val categories = listOf(
        "Приключение", "Оптимизация", "Технологии", "Магия",
        "Мобы", "Снаряжение", "Еда", "Декор",
        "Транспорт", "Хранение", "Сражения", "Квесты",
        "Библиотека", "Утилиты", "Реализм", "Мультиплеер"
    )

    fun doSearch() {
        if (query.isBlank()) return
        scope.launch {
            loading = true
            statusMessage = ""
            try {
                val result = ModRepository.search(query.trim(), source)
                mods = result
                if (result.isEmpty()) statusMessage = "Ничего не найдено"
            } catch (e: Exception) {
                statusMessage = "Ошибка: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Моды для Minecraft", fontWeight = FontWeight.Bold) },
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

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = source == "all",
                    onClick = { source = "all"; if (query.isNotBlank()) doSearch() },
                    label = { Text("Все") }
                )
                FilterChip(
                    selected = source == "modrinth",
                    onClick = { source = "modrinth"; if (query.isNotBlank()) doSearch() },
                    label = { Text("Modrinth") }
                )
                FilterChip(
                    selected = source == "curseforge",
                    onClick = { source = "curseforge"; if (query.isNotBlank()) doSearch() },
                    label = { Text("CurseForge") }
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { doSearch() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading && query.isNotBlank()
            ) {
                Text(if (loading) "Поиск..." else "Найти")
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Категории:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = query.equals(cat, ignoreCase = true),
                        onClick = {
                            query = cat
                            doSearch()
                        },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (statusMessage.isNotEmpty()) {
                Text(statusMessage, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
            }

            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(mods) { mod ->
                        ModCard(mod = mod, onInstall = { downloadAndInstall(context, mod) })
                    }
                }
            }
        }
    }
}

@Composable
fun ModCard(mod: Mod, onInstall: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mod.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = mod.description ?: "",
                    fontSize = 12.sp,
                    maxLines = 2,
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
