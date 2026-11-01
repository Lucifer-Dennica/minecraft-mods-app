package ru.minecraftmods.app.ui

import android.app.Activity
import android.content.Context
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader

// Твой реальный ID для рекламы при открытии и скачивании
private const val INTERSTITIAL_AD_UNIT_ID = "R-M-20308863-2"

class AppOpenAdManager(private val context: Context) {
    private var launchCount = 0

    // Показ при запуске приложения (каждый 2-й раз)
    fun showAdIfAvailable(activity: Activity) {
        launchCount++
        if (launchCount % 2 != 0) return
        loadAndShowAd(activity, onDismiss = {}, onError = {})
    }

    // Показ при скачивании мода (вызывается извне)
    fun showInterstitialAd(activity: Activity, onDismiss: () -> Unit, onError: () -> Unit) {
        loadAndShowAd(activity, onDismiss, onError)
    }

    private fun loadAndShowAd(activity: Activity, onDismiss: () -> Unit, onError: () -> Unit) {
        val adRequest = AdRequest.Builder(INTERSTITIAL_AD_UNIT_ID).build()
        val loader = InterstitialAdLoader(context)

        loader.loadAd(adRequest, object : InterstitialAdLoadListener {
            override fun onAdLoaded(ad: InterstitialAd) {
                ad.setAdEventListener(object : InterstitialAdEventListener {
                    override fun onAdShown() {}
                    override fun onAdFailedToShow(adError: AdError) { 
                        onError() 
                    }
                    override fun onAdDismissed() { 
                        onDismiss() 
                    }
                    override fun onAdClicked() {}
                    override fun onAdImpression(data: ImpressionData?) {}
                })
                ad.show(activity)
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                // Если реклама не загрузилась (нет интернета и т.д.), продолжаем без нее
                onError()
            }
        })
    }
}
