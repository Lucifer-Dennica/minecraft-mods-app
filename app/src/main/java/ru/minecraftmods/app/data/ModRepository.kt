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
        return api.search(query, source, limit, offset, gameVersion, sort).results
    }

    suspend fun getFiles(id: String, source: String): List<ModFile> {
        return api.getFiles(id, source).files
    }
}
