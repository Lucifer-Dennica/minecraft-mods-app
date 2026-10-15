package ru.minecraftmods.app.data

import android.content.Context
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object TranslatorManager {

    // Список поддерживаемых языков приложения
    data class LanguageOption(val code: String, val displayName: String, val flag: String)

    val SUPPORTED_LANGUAGES = listOf(
        LanguageOption("ru", "Русский", "🇷🇺"),
        LanguageOption("en", "English", "🇬🇧"),
        LanguageOption("es", "Español", "🇪🇸"),
        LanguageOption("de", "Deutsch", "🇩🇪"),
        LanguageOption("fr", "Français", "🇫🇷"),
        LanguageOption("it", "Italiano", "🇮🇹"),
        LanguageOption("pt", "Português", "🇵🇹"),
        LanguageOption("pl", "Polski", "🇵🇱"),
        LanguageOption("tr", "Türkçe", "🇹🇷"),
        LanguageOption("uk", "Українська", "🇺🇦"),
        LanguageOption("zh", "中文", "🇨🇳"),
        LanguageOption("ja", "日本語", "🇯🇵"),
        LanguageOption("ko", "한국어", "🇰🇷")
    )

    private var enToTargetTranslator: Translator? = null
    private var targetToEnTranslator: Translator? = null
    private var currentTargetLanguage: String = "ru"

    /**
     * Скачивает модели перевода для указанного языка.
     * EN -> TARGET и TARGET -> EN.
     */
    suspend fun prepareModels(context: Context, targetLanguageCode: String): Boolean {
        return try {
            close() // Закрываем предыдущие модели, если были

            currentTargetLanguage = targetLanguageCode

            val targetMlKitLang = mapToMlKitCode(targetLanguageCode)

            val enToTargetOptions = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(targetMlKitLang)
                .build()
            enToTargetTranslator = Translation.getClient(enToTargetOptions)

            val targetToEnOptions = TranslatorOptions.Builder()
                .setSourceLanguage(targetMlKitLang)
                .setTargetLanguage(TranslateLanguage.ENGLISH)
                .build()
            targetToEnTranslator = Translation.getClient(targetToEnOptions)

            suspendCancellableCoroutine<Unit> { cont ->
                enToTargetTranslator?.downloadModelIfNeeded()
                    ?.addOnSuccessListener {
                        targetToEnTranslator?.downloadModelIfNeeded()
                            ?.addOnSuccessListener {
                                LanguagePreferences.setModelsReady(context, true)
                                cont.resume(Unit)
                            }
                            ?.addOnFailureListener { e -> cont.resumeWithException(e) }
                    }
                    ?.addOnFailureListener { e -> cont.resumeWithException(e) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            LanguagePreferences.setModelsReady(context, false)
            false
        }
    }

    /**
     * Проверяет, готова ли модель для текущего языка.
     */
    fun isReady(context: Context): Boolean {
        return LanguagePreferences.isModelsReady(context) && enToTargetTranslator != null
    }

    /**
     * Перевод EN -> выбранный язык (для описаний модов).
     */
    suspend fun translateEnToTarget(text: String): String {
        if (text.isBlank() || enToTargetTranslator == null) return text
        // Если целевой язык английский — ничего не переводим
        if (currentTargetLanguage == "en") return text
        return try {
            suspendCancellableCoroutine { cont ->
                enToTargetTranslator?.translate(text)
                    ?.addOnSuccessListener { result -> cont.resume(result) }
                    ?.addOnFailureListener { cont.resume(text) }
            }
        } catch (e: Exception) {
            text
        }
    }

    /**
     * Перевод с выбранного языка -> EN (для поисковых запросов).
     */
    suspend fun translateTargetToEn(text: String): String {
        if (text.isBlank() || targetToEnTranslator == null) return text
        if (currentTargetLanguage == "en") return text
        return try {
            suspendCancellableCoroutine { cont ->
                targetToEnTranslator?.translate(text)
                    ?.addOnSuccessListener { result -> cont.resume(result) }
                    ?.addOnFailureListener { cont.resume(text) }
            }
        } catch (e: Exception) {
            text
        }
    }

    /**
     * Преобразует наш код языка в код ML Kit.
     */
    private fun mapToMlKitCode(code: String): String {
        return when (code) {
            "ru" -> TranslateLanguage.RUSSIAN
            "en" -> TranslateLanguage.ENGLISH
            "es" -> TranslateLanguage.SPANISH
            "de" -> TranslateLanguage.GERMAN
            "fr" -> TranslateLanguage.FRENCH
            "it" -> TranslateLanguage.ITALIAN
            "pt" -> TranslateLanguage.PORTUGUESE
            "pl" -> TranslateLanguage.POLISH
            "tr" -> TranslateLanguage.TURKISH
            "uk" -> TranslateLanguage.UKRAINIAN
            "zh" -> TranslateLanguage.CHINESE
            "ja" -> TranslateLanguage.JAPANESE
            "ko" -> TranslateLanguage.KOREAN
            else -> TranslateLanguage.RUSSIAN
        }
    }

    fun close() {
        enToTargetTranslator?.close()
        targetToEnTranslator?.close()
        enToTargetTranslator = null
        targetToEnTranslator = null
    }
}
