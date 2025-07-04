package com.auto.click.modules.permissions.ui

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.auto.click.AutoClickService
import com.auto.click.R
import com.auto.click.appcomponents.utility.Utils
import com.auto.click.databinding.ActivityPermissionsBinding

class PermissionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPermissionsBinding
    private var isGotoSettings: Boolean = false

    private val handler = Handler(Looper.getMainLooper())
    private val checkAccessibilitySetting: Runnable = object : Runnable {
        override fun run() {
            if (Utils.isAccessibilityEnabled(
                    this@PermissionsActivity,
                    AutoClickService::class.java
                )
            ) {
                isGotoSettings = false
                val activityIntent =
                    Intent(this@PermissionsActivity, PermissionsActivity::class.java)
                activityIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(activityIntent)
                return
            }
            handler.postDelayed(this, 300)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.inToolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val title = getString(R.string.text_permissions)
        val spannableTitle = SpannableString(title)
        spannableTitle.setSpan(
            StyleSpan(Typeface.BOLD),  // Kiểu chữ đậm
            0,
            title.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        supportActionBar?.title = spannableTitle
        binding.tvActSet.setOnClickListener {
            Utils.showDialogSettingAccessibility(this@PermissionsActivity, {
                isGotoSettings = true
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                handler.postDelayed(checkAccessibilitySetting, 300)
            }, {

            })
        }
        binding.tvReactivation.setOnClickListener {
            isGotoSettings = true
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        binding.tvSetBattery.setOnClickListener {
            val intentSettings = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intentSettings.setData(Uri.fromParts("package", applicationContext.packageName, null))
            intentSettings.addCategory(Intent.CATEGORY_DEFAULT)
            intentSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intentSettings.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intentSettings.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            startActivity(intentSettings)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!Utils.isAccessibilityEnabled(
                this@PermissionsActivity,
                AutoClickService::class.java
            ) && isGotoSettings
        ) {
            Utils.showDialogTryRequestAccessibility(this@PermissionsActivity) {
                isGotoSettings = true
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                handler.postDelayed(checkAccessibilitySetting, 300)
            }
            isGotoSettings = false
        }
        if (!Utils.isAccessibilityEnabled(this@PermissionsActivity, AutoClickService::class.java)) {
            binding.lineTip.visibility = View.GONE
            binding.ivWarningDots.visibility = View.VISIBLE
            binding.lineCard1.setBackgroundResource(R.drawable.bg_tip_remind)
            binding.tvActSet.visibility = View.VISIBLE
            binding.ivActOpenOK.visibility = View.GONE
            binding.tvReactivation.visibility = View.GONE
        } else {
            binding.lineTip.visibility = View.VISIBLE
            binding.ivWarningDots.visibility = View.GONE
            binding.lineCard1.setBackgroundResource(R.drawable.bg_tip_green_remind)
            binding.tvActSet.visibility = View.GONE
            binding.ivActOpenOK.visibility = View.VISIBLE
            binding.tvReactivation.visibility = View.VISIBLE
        }

        if (!Utils.isIgnoringBatteryOptimizations(this@PermissionsActivity)) {
            binding.ivBatWarningDots.visibility = View.VISIBLE
            binding.lineCard2.setBackgroundResource(R.drawable.bg_tip_remind)
            binding.tvSetBattery.visibility = View.VISIBLE
            binding.ivOpenOK.visibility = View.GONE
        } else {
            binding.ivBatWarningDots.visibility = View.GONE
            binding.lineCard2.setBackgroundResource(R.drawable.bg_tip_green_remind)
            binding.tvSetBattery.visibility = View.GONE
            binding.ivOpenOK.visibility = View.VISIBLE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}