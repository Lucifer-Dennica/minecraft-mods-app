package ru.minecraftmods.app.data

import android.content.Context

object SettingsPreferences {
    private const val PREFS_NAME = "settings_prefs"
    private const val KEY_AUTO_DELETE = "auto_delete_mods"
    private const val KEY_GAME_VERSION = "game_version"
    private const val KEY_DOWNLOAD_COUNT = "download_count"

    private val SUPPORTED_VERSIONS = listOf("1.21", "1.20", "1.19", "1.18", "1.17", "1.16")

    fun isAutoDeleteEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_AUTO_DELETE, true)
    }

    fun setAutoDeleteEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_AUTO_DELETE, enabled).apply()
    }

    fun incrementDownloadCount(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getInt(KEY_DOWNLOAD_COUNT, 0) + 1
        prefs.edit().putInt(KEY_DOWNLOAD_COUNT, current).apply()
        return current
    }

    fun getGameVersion(context: Context): String {
        val saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_GAME_VERSION, null)
        
        if (saved != null && SUPPORTED_VERSIONS.contains(saved)) return saved
        
        return try {
            val pInfo = context.packageManager.getPackageInfo("com.mojang.minecraftpe", 0)
            val fullVersion = pInfo.versionName ?: "1.21" 
            val shortVersion = fullVersion.split(".").take(2).joinToString(".")
            
            if (SUPPORTED_VERSIONS.contains(shortVersion)) shortVersion else "1.21"
        } catch (e: Exception) {
            "1.21"
        }
    }

    fun setGameVersion(context: Context, version: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_GAME_VERSION, version).apply()
    }
}
