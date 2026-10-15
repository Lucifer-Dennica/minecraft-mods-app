package ru.minecraftmods.app.data

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object TranslatorManager {

    private var enRuTranslator: Translator? = null
    private var ruEnTranslator: Translator? = null
    private var modelDownloaded = false

    /**
     * Скачивает языковые модели (EN <-> RU) при первом запуске.
     * Вызывается один раз при старте приложения.
     */
    suspend fun prepareModels(): Boolean {
        return try {
            val enRuOptions = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.RUSSIAN)
                .build()
            enRuTranslator = Translation.getClient(enRuOptions)

            val ruEnOptions = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.RUSSIAN)
                .setTargetLanguage(TranslateLanguage.ENGLISH)
                .build()
            ruEnTranslator = Translation.getClient(ruEnOptions)

            suspendCancellableCoroutine<Unit> { cont ->
                enRuTranslator?.downloadModelIfNeeded()
                    ?.addOnSuccessListener {
                        ruEnTranslator?.downloadModelIfNeeded()
                            ?.addOnSuccessListener {
                                modelDownloaded = true
                                cont.resume(Unit)
                            }
                            ?.addOnFailureListener { e -> cont.resumeWithException(e) }
                    }
                    ?.addOnFailureListener { e -> cont.resumeWithException(e) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Перевод EN -> RU (для описаний модов).
     */
    suspend fun translateEnToRu(text: String): String {
        if (text.isBlank() || !modelDownloaded) return text
        return try {
            suspendCancellableCoroutine { cont ->
                enRuTranslator?.translate(text)
                    ?.addOnSuccessListener { result -> cont.resume(result) }
                    ?.addOnFailureListener { cont.resume(text) }
            }
        } catch (e: Exception) {
            text
        }
    }

    /**
     * Перевод RU -> EN (для поисковых запросов).
     */
    suspend fun translateRuToEn(text: String): String {
        if (text.isBlank() || !modelDownloaded) return text
        return try {
            suspendCancellableCoroutine { cont ->
                ruEnTranslator?.translate(text)
                    ?.addOnSuccessListener { result -> cont.resume(result) }
                    ?.addOnFailureListener { cont.resume(text) }
            }
        } catch (e: Exception) {
            text
        }
    }

    fun close() {
        enRuTranslator?.close()
        ruEnTranslator?.close()
    }
}
