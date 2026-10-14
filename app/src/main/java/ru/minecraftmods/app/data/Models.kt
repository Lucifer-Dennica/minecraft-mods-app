package ru.minecraftmods.app.data

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    @SerializedName("results") val results: List<Mod> = emptyList()
)

data class Mod(
    @SerializedName("id") val id: String,
    @SerializedName("source") val source: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("icon_url") val iconUrl: String?,
    @SerializedName("downloads") val downloads: Long = 0,
    @SerializedName("author") val author: String? = null,
    @SerializedName("page_url") val pageUrl: String? = null
)

data class FilesResponse(
    @SerializedName("files") val files: List<ModFile> = emptyList()
)

data class ModFile(
    @SerializedName("url") val url: String,
    @SerializedName("filename") val filename: String,
    @SerializedName("size") val size: Long = 0
)
