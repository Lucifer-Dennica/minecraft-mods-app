package ru.minecraftmods.app.ui

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.*
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadResult
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Тестовый ID для межстраничной рекламы (Interstitial)
private const val TEST_INTERSTITIAL_AD_UNIT_ID = "demo-interstitial-yandex"

class AppOpenAdManager(private val context: Context) {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var launchCount = 0

    fun showAdIfAvailable(activity: Activity) {
        launchCount++
        // Показываем рекламу только на каждый второй запуск (2, 4, 6...)
        if (launchCount % 2 != 0) return 
        
        if (isLoading) return
        
        isLoading = true
        val adRequest = AdRequest.Builder(TEST_INTERSTITIAL_AD_UNIT_ID).build()
        val loader = InterstitialAdLoader(context)
        
        loader.setAdLoadListener(object : InterstitialAdLoader.AdLoadListener {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                isLoading = false
                ad.setAdEventListener(object : InterstitialAdEventListener {
                    override fun onAdShown() {}
                    override fun onAdFailedToShow(error: AdRequestError) { interstitialAd = null }
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
        loader.loadAd(adRequest)
    }
}
