package com.auto.click

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.BillingProcessor.IPurchasesResponseListener
import com.anjlab.android.iab.v3.PurchaseInfo
import com.google.common.collect.ImmutableList
import androidx.core.net.toUri


class InAppMNG {
    interface InAppListener {
        fun onProductPurchased()
        fun onBillingError(errorCode: Int, error: Throwable?)
    }

    companion object : BillingProcessor.IBillingHandler {
        public val INAPP_KEY = "com.hd.gba.psa.emulator.lifetime"
        public val SUBS_KEY_1 = "com.hd.gba.psa.emulator.weekly"
        public val SUBS_KEY_2 = "com.hd.gba.psa.emulator.monthly"

        private val LICENSE_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgiTdooXItyWPzPm60aFXZr43fsJx90Ye3WZs+APIc0RGlmILDvIFUxsATwPJYViHuQJcd1kL4JV1+oRGuvB1R7YpiQdjyYeEv9+TTmNgPoz8Wf8VVxp2lH9iJw9oLPxXckohjk5s4Gt1zyiXdz8NuNRU5kk5s2T7oJaYszyEJOT9/qQc+s3lYvbE/yQcY8HIFH4oMRO9QTP/glbBlzB5TOl6l3Q0wc2oNMDKQbu9wLYxtR56r7TBIaoZS+5b1sPHijxp4NAlo02KCCBd4ObvoDt8fpZGdaxIY43uJpVGMicsVRvf1yx5GtoBTwFU2knVCPPkjUUHgSsYgrvq+CS42QIDAQAB"
        private var inAppListener: InAppListener? = null

        var bp: BillingProcessor? = null
        var tempPro: Boolean = false

        var context: Context? = null

        fun initWith(context: Context) {
            this.context = context
            if (bp == null) {
                bp = BillingProcessor(context, LICENSE_KEY, this)
            }
            if (!bp!!.isInitialized) {
                bp?.initialize()
            }
        }

        fun buyProductID(activity: Activity, productId: String, listener: InAppListener) {
            inAppListener = listener
            bp?.purchase(activity, productId)
        }

        fun subsProductID(activity: Activity, productId: String, listener: InAppListener) {
            inAppListener = listener
            bp?.mySubscribe(activity, productId)
        }

        fun isProVersion(): Boolean {
            bp?.let {
                if (it.isSubscribed(SUBS_KEY_1)) {
                    return true
                }
                if (it.isSubscribed(SUBS_KEY_2)) {
                    return true
                }
                if (it.isPurchased(INAPP_KEY)) {
                    return true
                }
            }
            return tempPro
//            return true
        }

        fun isSubs(): Boolean {
            bp?.let {
                if (it.isSubscribed(SUBS_KEY_1)) {
                    return true
                }
                if (it.isSubscribed(SUBS_KEY_2)) {
                    return true
                }
            }
            return false
        }

        fun gotoCancelSubScreen(context: Context) {
            if (isProVersion()) return
            var skuID = ""
            bp?.let {
                if (it.isSubscribed(SUBS_KEY_1)) {
                    skuID = SUBS_KEY_1
                }
                if (it.isSubscribed(SUBS_KEY_2)) {
                    skuID = SUBS_KEY_2
                }
            }
            if (skuID.isEmpty()) return
            val urlStr =
                "https://play.google.com/store/account/subscriptions?sku=${skuID}&package=${context.packageName}"
            val browserIntent = Intent(Intent.ACTION_VIEW, urlStr.toUri())
            context.startActivity(browserIntent)
        }

        fun getSubsProductInfo(listener: ProductDetailsResponseListener) {
            val queryProductDetailsParams =
                QueryProductDetailsParams.newBuilder()
                    .setProductList(
                        ImmutableList.of(
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(SUBS_KEY_1)
                                .setProductType(BillingClient.ProductType.SUBS)
                                .build(),
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(SUBS_KEY_2)
                                .setProductType(BillingClient.ProductType.SUBS)
                                .build(),
                        )
                    ).build()
            bp?.billingService?.queryProductDetailsAsync(queryProductDetailsParams, listener)
        }

        fun getInAppProductInfo(listener: ProductDetailsResponseListener) {
            val queryProductDetailsParams =
                QueryProductDetailsParams.newBuilder()
                    .setProductList(
                        ImmutableList.of(
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(INAPP_KEY)
                                .setProductType(BillingClient.ProductType.INAPP)
                                .build(),
                        )
                    ).build()
            bp?.billingService?.queryProductDetailsAsync(queryProductDetailsParams, listener)
        }

        fun restorePurchases(listener: InAppListener? = null) {
            inAppListener = listener
            bp?.loadOwnedPurchasesFromGoogleAsync(object : IPurchasesResponseListener {
                override fun onPurchasesSuccess() {
                    Log.d("CUONGNN", "restorePurchases onPurchasesSuccess")
                    // Cập nhật trạng thái premium sau khi restore
                    tempPro = isProVersion()
                    // Gửi broadcast để thông báo cho các component khác
                    context?.let {
                        LocalBroadcastManager.getInstance(it).sendBroadcast(Intent("Action_InApp"))
                    }
                    // Gọi listener nếu có
                    inAppListener?.onProductPurchased()
                    inAppListener = null
                }

                override fun onPurchasesError() {
                    Log.d("CUONGNN", "restorePurchases onPurchasesError")
                    inAppListener?.onBillingError(-1, null)
                    inAppListener = null
                }
            })
        }

        override fun onProductPurchased(productId: String, details: PurchaseInfo?) {
            bp?.handleItemAlreadyOwned(productId)
            tempPro = true
            inAppListener?.onProductPurchased()
            inAppListener = null
            context?.let {
                LocalBroadcastManager.getInstance(it).sendBroadcast(Intent("Action_InApp"))
            }
        }

        override fun onPurchaseHistoryRestored() {
            Log.d("CUONGNN", "onPurchaseHistoryRestored")
            // Cập nhật trạng thái premium sau khi restore
            tempPro = isProVersion()
            // Gửi broadcast để thông báo cho các component khác
            context?.let {
                LocalBroadcastManager.getInstance(it).sendBroadcast(Intent("Action_InApp"))
            }
            // Gọi listener nếu có
            inAppListener?.onProductPurchased()
            inAppListener = null
        }

        override fun onBillingError(errorCode: Int, error: Throwable?) {
            Log.d("CUONGNN", "onBillingError${errorCode} = ${error?.localizedMessage}")
            inAppListener?.onBillingError(errorCode, error)
            inAppListener = null
        }

        override fun onBillingInitialized() {
            Log.d("CUONGNN", "onBillingInitialized")
            bp?.loadOwnedPurchasesFromGoogleAsync(object : IPurchasesResponseListener {
                override fun onPurchasesSuccess() {
                    //
                }

                override fun onPurchasesError() {
                    //
                }
            })
        }
    }
}
