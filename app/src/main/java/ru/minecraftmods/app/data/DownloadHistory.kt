package ru.minecraftmods.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object DownloadHistory {
    private const val PREFS_NAME = "download_history_prefs"
    private const val KEY_HISTORY = "downloaded_mods"

    fun addMod(context: Context, mod: Mod) {
        val current = getHistory(context).toMutableList()
        current.removeAll { it.id == mod.id && it.source == mod.source }
        current.add(0, mod)
        saveHistoryManually(context, current)
    }

    fun getHistory(context: Context): List<Mod> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val type = object : TypeToken<List<Mod>>() {}.type
        return try { Gson().fromJson(json, type) } catch (e: Exception) { emptyList() }
    }

    fun saveHistoryManually(context: Context, mods: List<Mod>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(mods)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }

    fun clearHistory(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}
