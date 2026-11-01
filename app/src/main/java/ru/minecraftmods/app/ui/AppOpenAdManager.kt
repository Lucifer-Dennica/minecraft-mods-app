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

// Твой реальный ID для рекламы при открытии
private const val INTERSTITIAL_AD_UNIT_ID = "R-M-20308863-2"

class AppOpenAdManager(private val context: Context) {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var launchCount = 0

    fun showAdIfAvailable(activity: Activity) {
        launchCount++
        // Показ на каждый 2-й запуск
        if (launchCount % 2 != 0) return

        if (isLoading) return

        isLoading = true
        val adRequest = AdRequest.Builder(INTERSTITIAL_AD_UNIT_ID).build()
        val loader = InterstitialAdLoader(context)

        loader.loadAd(adRequest, object : InterstitialAdLoadListener {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                isLoading = false
                ad.setAdEventListener(object : InterstitialAdEventListener {
                    override fun onAdShown() {}
                    override fun onAdFailedToShow(adError: AdError) { interstitialAd = null }
                    override fun onAdDismissed() { interstitialAd = null }
                    override fun onAdClicked() {}
                    override fun onAdImpression(data: ImpressionData?) {}
                })
                ad.show(activity)
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                isLoading = false
                interstitialAd = null
            }
        })
    }
}
