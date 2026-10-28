package ru.minecraftmods.app.data

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object ModRepository {
    private const val BASE_URL = "https://mods-proxy.denis22142qwe.workers.dev/"
    private var api: ApiService? = null
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        val cacheSize = 10L * 1024 * 1024
        val cache = Cache(File(context.cacheDir, "http_cache"), cacheSize)

        val client = OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .build()

        api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
        isInitialized = true
    }

    private fun getApi(): ApiService = api ?: throw IllegalStateException("ModRepository не инициализирован.")

    suspend fun search(
        query: String,
        source: String = "all",
        limit: Int = 20,
        offset: Int = 0,
        gameVersion: String = "",
        sort: String = "downloads"
    ): List<Mod> {
        val englishQuery = TranslatorManager.translateTargetToEn(query)
        val rawMods = getApi().search(englishQuery, source, limit, offset, gameVersion, sort).results

        return rawMods.map { mod ->
            mod.copy(
                title = TranslatorManager.translateEnToTarget(mod.title),
                description = mod.description?.let { TranslatorManager.translateEnToTarget(it) }
            )
        }
    }

    suspend fun getFiles(id: String, source: String): List<ModFile> {
        return getApi().getFiles(id, source).files
    }

    suspend fun getScreenshots(id: String, source: String): List<Screenshot> {
        return getApi().getScreenshots(id, source).screenshots
    }
}
