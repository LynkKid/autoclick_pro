package com.auto.click

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.view.View
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.AdSize

class AdMNG {

    interface InterAdListener {
        fun onAdShowed()
        fun onAdHidden()
    }

    companion object {
        private var interstitialAd: InterstitialAd? = null
        private var listener: InterAdListener? = null
        private var handler: Handler? = null
        private var lastShowTime: Long = 0
        private var distance: Int = 0 * 1000 // in ms
        private const val AD_UNIT_ID = "ca-app-pub-4922850260251086/8978599447"

        fun initWith(context: Context) {
            MobileAds.initialize(context)

            val testDeviceIds = listOf(
                "0F022AA60D51FDA1D6BEB609D2B12DDD",
                "01EAFE70D62FE992DB7116A6B45F5069",
                "8F30B4659AC0BF6DBF85662A172CB011"
            )
            val config = RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build()
            MobileAds.setRequestConfiguration(config)

            loadAd(context)
        }

        fun setDistance(timeIntervalShowInterstitial: Long) {
            distance = (timeIntervalShowInterstitial * 1000).toInt()
        }

        fun hasAd(activity: Activity): Boolean {
            val current = System.currentTimeMillis()
            return interstitialAd != null && (current - lastShowTime > distance)
        }

        fun loadAd(context: Context) {
            if (InAppMNG.isProVersion()) return

            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                AD_UNIT_ID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                    }
                }
            )
        }

        fun showAd(activity: Activity, callback: InterAdListener?) {
            if (InAppMNG.isProVersion()) {
                callback?.onAdHidden()
                return
            }

            if (hasAd(activity)) {
                listener = callback
                interstitialAd?.apply {
                    fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdShowedFullScreenContent() {
                            listener?.onAdShowed()
                            interstitialAd = null
                        }

                        override fun onAdDismissedFullScreenContent() {
                            listener?.onAdHidden()
                            lastShowTime = System.currentTimeMillis()
                            loadAd(activity)
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            listener?.onAdHidden()
                            loadAd(activity)
                        }
                    }
                    show(activity)
                }
            } else {
                loadAd(activity)
                callback?.onAdHidden()
            }
        }

        fun loadBanner(activity: Activity, adView: AdView) {
            if (InAppMNG.isProVersion()) {
                adView.visibility = View.GONE
                return
            }

            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
            adView.visibility = View.VISIBLE
        }

    }
}

