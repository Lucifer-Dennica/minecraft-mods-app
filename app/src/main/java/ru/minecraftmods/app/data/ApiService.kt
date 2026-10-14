package ru.minecraftmods.app.data

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("search")
    suspend fun search(
        @Query("query") query: String,
        @Query("source") source: String = "all",
        @Query("limit") limit: Int = 20
    ): SearchResponse

    @GET("files")
    suspend fun getFiles(
        @Query("id") id: String,
        @Query("source") source: String
    ): FilesResponse
}
