package ru.minecraftmods.app.ui

import android.app.Activity
import android.content.Context
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader

private const val TEST_INTERSTITIAL_AD_UNIT_ID = "demo-interstitial-yandex"

class AppOpenAdManager(private val context: Context) {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var launchCount = 0

    fun showAdIfAvailable(activity: Activity) {
        launchCount++
        if (launchCount % 2 != 0) return

        if (isLoading) return

        isLoading = true
        val adRequest = AdRequest.Builder(TEST_INTERSTITIAL_AD_UNIT_ID).build()
        val loader = InterstitialAdLoader(context)

        loader.setAdLoadListener(object : InterstitialAdLoadListener {
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
