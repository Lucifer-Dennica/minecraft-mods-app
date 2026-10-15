package ru.minecraftmods.app.data

import android.content.Context
import android.content.SharedPreferences

object LanguagePreferences {

    private const val PREFS_NAME = "app_prefs"
    private const val KEY_TARGET_LANGUAGE = "target_language"
    private const val KEY_MODELS_READY = "models_ready"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getTargetLanguage(context: Context): String {
        return prefs(context).getString(KEY_TARGET_LANGUAGE, "ru") ?: "ru"
    }

    fun setTargetLanguage(context: Context, code: String) {
        prefs(context).edit().putString(KEY_TARGET_LANGUAGE, code).apply()
        setModelsReady(context, false)
    }

    fun isModelsReady(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_MODELS_READY, false)
    }

    fun setModelsReady(context: Context, ready: Boolean) {
        prefs(context).edit().putBoolean(KEY_MODELS_READY, ready).apply()
    }
}
