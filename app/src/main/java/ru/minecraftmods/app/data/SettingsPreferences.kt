package ru.minecraftmods.app.data

import android.content.Context

object SettingsPreferences {
    private const val PREFS_NAME = "settings_prefs"
    private const val KEY_AUTO_DELETE = "auto_delete_mods"
    private const val KEY_GAME_VERSION = "game_version"

    fun isAutoDeleteEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_AUTO_DELETE, true)
    }

    fun setAutoDeleteEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_AUTO_DELETE, enabled).apply()
    }

    fun getGameVersion(context: Context): String {
        val saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_GAME_VERSION, null)
        if (saved != null) return saved
        
        // Если не сохранено, пытаемся определить версию Minecraft
        return try {
            val pInfo = context.packageManager.getPackageInfo("com.mojang.minecraftpe", 0)
            // Безопасно обрабатываем возможный null от versionName
            val fullVersion = pInfo.versionName ?: "1.21" 
            // Берем только первые две цифры (1.21)
            fullVersion.split(".").take(2).joinToString(".")
        } catch (e: Exception) {
            "1.21" // По умолчанию, если игра не найдена
        }
    }

    fun setGameVersion(context: Context, version: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_GAME_VERSION, version).apply()
    }
}
