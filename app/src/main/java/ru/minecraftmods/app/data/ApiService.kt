package ru.minecraftmods.app.data

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("search")
    suspend fun search(
        @Query("query") query: String,
        @Query("source") source: String = "all",
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("game_version") gameVersion: String = "",
        @Query("sort") sort: String = "downloads"
    ): SearchResponse

    @GET("files")
    suspend fun getFiles(
        @Query("id") id: String,
        @Query("source") source: String
    ): FilesResponse
}
