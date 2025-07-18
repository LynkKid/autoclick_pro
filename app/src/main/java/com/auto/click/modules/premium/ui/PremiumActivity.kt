package com.auto.click.modules.premium.ui

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.android.billingclient.api.ProductDetails
import com.auto.click.InAppMNG
import com.auto.click.R
import com.auto.click.WebActivity
import com.auto.click.appcomponents.utility.Utils.dp2px
import com.auto.click.appcomponents.utility.Utils.formatCurrency
import com.auto.click.appcomponents.utility.setSafeOnClickListener
import com.auto.click.databinding.ActivityPremiumBinding

class PremiumActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPremiumBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPremiumBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupDrawableSizes()
        binding.ivClose.setSafeOnClickListener {
            finish()
        }
        binding.tvSubNotice.setSafeOnClickListener {
            DialogSubscribeNoticeFragment.show(this)
        }

        binding.tvGooglePlay.setSafeOnClickListener {
            try {
                // Tạo Intent để mở Google Play Store subscription page
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "https://play.google.com/store/account/subscriptions".toUri()
                )
                startActivity(intent)

            } catch (e: Exception) {
                // Nếu không mở được Google Play Store, thử mở bằng market://
                try {
                    val marketIntent = Intent(Intent.ACTION_VIEW, "market://subscriptions".toUri())
                    marketIntent.setPackage("com.sieuthi.click.sound.prank")
                    startActivity(marketIntent)
                } catch (_: Exception) {
                }
            }
        }

        binding.tvTerms.setSafeOnClickListener {
            val intent = Intent(this, WebActivity::class.java).apply {
                putExtra(
                    "url",
                    "https://sieuthichauauprivacypolicy.blogspot.com/2025/07/terms-of-use.html"
                )
                putExtra("title", "End User License Agreement")
            }
            startActivity(intent)
        }

        binding.lineSubYear.setSafeOnClickListener {
            buyProduct(InAppMNG.SUBS_KEY_1, "SUBS")
        }
        binding.lineSubMonth.setSafeOnClickListener {
            buyProduct(InAppMNG.SUBS_KEY_2, "SUBS")
        }

        binding.tvRestore.setSafeOnClickListener {
            restorePurchases()
        }

        InAppMNG.getSubsProductInfo { _, p1 -> updatePrice(p1) }
        InAppMNG.getInAppProductInfo { _, p1 -> updatePrice(p1) }
    }

    private fun setupDrawableSizes() {
        val textViewsWithDrawable = listOf(
            R.id.tv_quick_launch_app,
            R.id.tv_unlock_all_features,
            R.id.tv_remove_ads
        )

        textViewsWithDrawable.forEach { viewId ->
            val textView = findViewById<TextView>(viewId)
            val drawables = textView?.compoundDrawablesRelative
            val drawableStart = drawables?.get(0)

            drawableStart?.let {
                val height = dp2px(15f)
                val with = dp2px(17f)
                it.setBounds(0, 0, with, height)
                textView.setCompoundDrawablesRelative(
                    it,
                    drawables[1],
                    drawables[2],
                    drawables[3]
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun updatePrice(products: MutableList<ProductDetails>?) {
        products?.let {
            for (product in products) {
                if (product.productId == InAppMNG.SUBS_KEY_1) {
                    var hasFreeTrial = false
                    product.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.let {
                        for (pricePhase in it) {
                            if (pricePhase.priceAmountMicros == 0L) {
                                hasFreeTrial = true
                            }
                        }
                    }
                    val price = product.subscriptionOfferDetails?.get(0)?.pricingPhases
                        ?.pricingPhaseList
                        ?.firstOrNull { it.priceAmountMicros > 0 }
                        ?.formattedPrice
                    val price1 = product.subscriptionOfferDetails?.get(0)?.pricingPhases
                        ?.pricingPhaseList
                        ?.firstOrNull { it.priceAmountMicros > 0 }?.priceAmountMicros
                    val currencyCode = product.subscriptionOfferDetails?.get(0)?.pricingPhases
                        ?.pricingPhaseList
                        ?.firstOrNull { it.priceAmountMicros > 0 }?.priceCurrencyCode
                    binding.tvYearPrice.text = price
                    binding.tvByMonthPrice.text = getString(
                        R.string.price_of_month, formatCurrency(
                            (price1 ?: 0) / 1_000_000.0 / 12,
                            currencyCode ?: "USD"
                        )
                    )
                    binding.tvYearSubPrice.text =
                        getString(R.string.subscription_after_trial_of_year, price)
                    if (hasFreeTrial) {
                        binding.tvYearSub.text = getString(R.string.free_trial)
                    } else {
                        binding.tvYearSub.text = getString(R.string.buy)
                    }
                }
                if (product.productId == InAppMNG.SUBS_KEY_2) {
                    var hasFreeTrial = false
                    product.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.let {
                        for (pricePhase in it) {
                            if (pricePhase.priceAmountMicros == 0L) {
                                hasFreeTrial = true
                            }
                        }
                    }
                    val price = product.subscriptionOfferDetails?.get(0)?.pricingPhases
                        ?.pricingPhaseList
                        ?.firstOrNull { it.priceAmountMicros > 0 }
                        ?.formattedPrice
                    val price1 = product.subscriptionOfferDetails?.get(0)?.pricingPhases
                        ?.pricingPhaseList
                        ?.firstOrNull { it.priceAmountMicros > 0 }?.priceAmountMicros
                    val currencyCode = product.subscriptionOfferDetails?.get(0)?.pricingPhases
                        ?.pricingPhaseList
                        ?.firstOrNull { it.priceAmountMicros > 0 }?.priceCurrencyCode
                    binding.tvMonthPrice.text = price
                    binding.tvByYearPrice.text = getString(
                        R.string.price_of_year, formatCurrency(
                            (price1 ?: 0) / 1_000_000.0 * 12,
                            currencyCode ?: "USD"
                        )
                    )
                    binding.tvMonthSubPrice.text =
                        getString(R.string.subscription_after_trial_of_month, price)
                    if (hasFreeTrial) {
                        binding.tvMonthSub.text = getString(R.string.free_trial)
                    } else {
                        binding.tvMonthSub.text = getString(R.string.buy)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        progressDialog = null
    }


    private var progressDialog: ProgressDialog? = null
    private var toast: Toast? = null
    private fun buyProduct(productID: String, type: String) {
        progressDialog?.dismiss()
        progressDialog = ProgressDialog(this)
        progressDialog?.setCancelable(true)
        progressDialog?.show()
        if (type == "SUBS") {
            InAppMNG.subsProductID(this, productID, object : InAppMNG.InAppListener {
                override fun onBillingError(errorCode: Int, error: Throwable?) {
                    progressDialog?.dismiss()
                    progressDialog = null
                    toast?.cancel()
                    toast = Toast.makeText(
                        applicationContext,
                        "Purchase Error! " + errorCode + ". MSG = " + error?.localizedMessage,
                        Toast.LENGTH_SHORT
                    )
                    toast?.show()
                }

                override fun onProductPurchased() {
                    progressDialog?.dismiss()
                    progressDialog = null
                    toast?.cancel()
                    toast =
                        Toast.makeText(applicationContext, "Purchase Success!", Toast.LENGTH_SHORT)
                    toast?.show()
                    setResult(RESULT_OK)
                    finish()
                }
            })
        }
        if (type == "INAPP") {
            InAppMNG.buyProductID(this, productID, object : InAppMNG.InAppListener {
                override fun onBillingError(errorCode: Int, error: Throwable?) {
                    progressDialog?.dismiss()
                    progressDialog = null
                    toast?.cancel()
                    toast = Toast.makeText(
                        applicationContext,
                        "Purchase Error! " + errorCode + ". MSG = " + error?.localizedMessage,
                        Toast.LENGTH_SHORT
                    )
                    toast?.show()
                }

                override fun onProductPurchased() {
                    progressDialog?.dismiss()
                    progressDialog = null
                    toast?.cancel()
                    toast =
                        Toast.makeText(applicationContext, "Purchase Success!", Toast.LENGTH_SHORT)
                    toast?.show()
                    setResult(RESULT_OK)
                    finish()
                }
            })
        }
    }

    private fun restorePurchases() {
        progressDialog?.dismiss()
        progressDialog = ProgressDialog(this)
        progressDialog?.setCancelable(true)
        progressDialog?.setMessage("Restoring purchases...")
        progressDialog?.show()

        InAppMNG.restorePurchases(object : InAppMNG.InAppListener {
            override fun onBillingError(errorCode: Int, error: Throwable?) {
                progressDialog?.dismiss()
                progressDialog = null
                toast?.cancel()
                toast = Toast.makeText(
                    applicationContext,
                    "Restore Error! " + errorCode + ". MSG = " + error?.localizedMessage,
                    Toast.LENGTH_SHORT
                )
                toast?.show()
            }

            override fun onProductPurchased() {
                progressDialog?.dismiss()
                progressDialog = null
                toast?.cancel()
                toast = Toast.makeText(
                    applicationContext,
                    "Restore Success! Premium features restored.",
                    Toast.LENGTH_SHORT
                )
                toast?.show()
                setResult(RESULT_OK)
                finish()
            }
        })
    }
}