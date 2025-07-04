package com.auto.click.modules.size_customization.ui

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.auto.click.R
import com.auto.click.appcomponents.utility.Contact
import com.auto.click.appcomponents.utility.PreferenceHelper
import com.auto.click.appcomponents.utility.Utils.dp2px
import com.auto.click.databinding.ActivitySizeCustomizationBinding

class SizeCustomizationActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySizeCustomizationBinding
    private val sizeSteps = listOf(40f, 45f, 50f)
    private val textSizeSp = listOf(22f, 24f, 26f)
    private val menuLayouts = listOf(
        R.layout.menu_in_settings_small_layout,
        R.layout.menu_in_settings_medium_layout,
        R.layout.menu_in_settings_large_layout
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySizeCustomizationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.inToolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val title = getString(R.string.text_size_customization)
        val spannableTitle = SpannableString(title)
        spannableTitle.setSpan(
            StyleSpan(Typeface.BOLD),  // Kiểu chữ đậm
            0,
            title.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        supportActionBar?.title = spannableTitle
        applyInitialState()
        setupSeekBars()
    }

    private fun applyInitialState() {
        applyTargetSize(PreferenceHelper.getInt(Contact.TARGET_SIZE, 1))
        binding.tvClickImage2.alpha = PreferenceHelper.getFloat(Contact.TARGET_TRANSPARENCY, 1f)
        applyMenuLayout(PreferenceHelper.getInt(Contact.MENU_SIZE, 1))
    }

    private fun setupSeekBars() {
        binding.seekbar.max = 100
        binding.seekbar.progress =
            PreferenceHelper.getInt(Contact.TARGET_TRANSPARENCY_PROGRESS, 100)
        binding.seekbar.setOnSeekBarChangeListener(SimpleSeekBarListener { progress ->
            val alpha = (progress / 100f).coerceIn(0.1f, 1.0f)
            binding.tvClickImage2.alpha = alpha
            PreferenceHelper.putFloat(Contact.TARGET_TRANSPARENCY, alpha)
            PreferenceHelper.putInt(Contact.TARGET_TRANSPARENCY_PROGRESS, progress)
        })

        binding.seekBarTargetSize.max = 2
        binding.seekBarTargetSize.progress = PreferenceHelper.getInt(Contact.TARGET_SIZE, 1)
        binding.seekBarTargetSize.setOnSeekBarChangeListener(SimpleSeekBarListener { index ->
            applyTargetSize(index)
        })

        binding.seekBarMenuSize.max = 2
        binding.seekBarMenuSize.progress = PreferenceHelper.getInt(Contact.MENU_SIZE, 1)
        binding.seekBarMenuSize.setOnSeekBarChangeListener(SimpleSeekBarListener { index ->
            applyMenuLayout(index)
        })
    }

    private fun applyTargetSize(index: Int) {
        val dp = sizeSteps.getOrElse(index) { 45f }
        val px = dp2px(dp)

        with(binding.tvClickImage) {
            textSize = textSizeSp.getOrElse(index) { 24f }
            layoutParams.width = px
            layoutParams.height = px
            requestLayout()
        }
        PreferenceHelper.putInt(Contact.TARGET_SIZE, index)
    }

    private fun applyMenuLayout(index: Int) {
        val layoutId = menuLayouts.getOrElse(index) { menuLayouts[1] }

        val menuView = LayoutInflater.from(this).inflate(layoutId, binding.lineMenu, false)

        binding.lineMenu.removeAllViews()
        binding.lineMenu.addView(menuView)
        PreferenceHelper.putInt(Contact.MENU_SIZE, index)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // Utility: SeekBar change listener shortcut
    private class SimpleSeekBarListener(val onChanged: (Int) -> Unit) :
        android.widget.SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(
            seekBar: android.widget.SeekBar?,
            progress: Int,
            fromUser: Boolean
        ) {
            onChanged(progress)
        }

        override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
    }
}