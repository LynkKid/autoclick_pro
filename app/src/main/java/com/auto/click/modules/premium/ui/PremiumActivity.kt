package com.auto.click.modules.premium.ui

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.ProductDetails
import com.auto.click.InAppMNG
import com.auto.click.R
import com.auto.click.appcomponents.utility.Utils.dp2px
import com.auto.click.appcomponents.utility.setSafeOnClickListener
import com.auto.click.databinding.ActivityPremiumBinding
import com.google.android.material.card.MaterialCardView

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

        binding.lineSubYear.setSafeOnClickListener {
            buyProduct(InAppMNG.SUBS_KEY_2, "SUBS")
        }
        binding.lineSubMonth.setSafeOnClickListener {
            buyProduct(InAppMNG.SUBS_KEY_1, "SUBS")
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
                    binding.tvYearPrice.text = price
                    val price1 = product.subscriptionOfferDetails?.get(0)?.pricingPhases
                        ?.pricingPhaseList
                        ?.firstOrNull { it.priceAmountMicros > 0 }?.priceAmountMicros
                    binding.tvByMonthPrice.text = "(${price1.toString().toInt() / 12}/month)"
                    if (hasFreeTrial) {
                        binding.tvYearSub.text = "FREE TRIAL"
                    } else {
                        binding.tvYearSub.text = "BUY"
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
                    binding.tvMonthPrice.text = price
                    binding.tvByYearPrice.text = "(${price1.toString().toInt() * 12}/year)"
                    if (hasFreeTrial) {
                        binding.tvYearSub.text = "FREE TRIAL"
                    } else {
                        binding.tvYearSub.text = "BUY"
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
                    toast = Toast.makeText(applicationContext, "Purchase Error! " + errorCode + ". MSG = " + error?.localizedMessage, Toast.LENGTH_SHORT)
                    toast?.show()
                }
                override fun onProductPurchased() {
                    progressDialog?.dismiss()
                    progressDialog = null
                    toast?.cancel()
                    toast = Toast.makeText(applicationContext, "Purchase Success!", Toast.LENGTH_SHORT)
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
                    toast = Toast.makeText(applicationContext, "Purchase Error! " + errorCode + ". MSG = " + error?.localizedMessage, Toast.LENGTH_SHORT)
                    toast?.show()
                }
                override fun onProductPurchased() {
                    progressDialog?.dismiss()
                    progressDialog = null
                    toast?.cancel()
                    toast = Toast.makeText(applicationContext, "Purchase Success!", Toast.LENGTH_SHORT)
                    toast?.show()
                    setResult(RESULT_OK)
                    finish()
                }
            })
        }
    }
}