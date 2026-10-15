package ru.minecraftmods.app.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ModRepository {

    private const val BASE_URL = "https://mods-proxy.denis22142qwe.workers.dev/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    suspend fun search(
        query: String,
        source: String = "all",
        limit: Int = 20,
        offset: Int = 0,
        gameVersion: String = "",
        sort: String = "downloads"
    ): List<Mod> {
        // Переводим запрос на английский перед отправкой
        val englishQuery = TranslatorManager.translateRuToEn(query)

        val rawMods = api.search(englishQuery, source, limit, offset, gameVersion, sort).results

        // Переводим результаты обратно на русский
        return rawMods.map { mod ->
            mod.copy(
                title = TranslatorManager.translateEnToRu(mod.title),
                description = mod.description?.let { TranslatorManager.translateEnToRu(it) }
            )
        }
    }

    suspend fun getFiles(id: String, source: String): List<ModFile> {
        return api.getFiles(id, source).files
    }

    suspend fun getScreenshots(id: String, source: String): List<Screenshot> {
        return api.getScreenshots(id, source).screenshots
    }
}
