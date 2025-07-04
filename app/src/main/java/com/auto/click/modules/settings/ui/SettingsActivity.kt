package com.auto.click.modules.settings.ui

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.auto.click.R
import com.auto.click.appcomponents.utility.Contact
import com.auto.click.appcomponents.utility.OptionDelay
import com.auto.click.appcomponents.utility.PreferenceHelper
import com.auto.click.databinding.ActivitySettingsBinding
import com.auto.click.modules.popup.PopupWarningSpeed
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    private var optionDelay = OptionDelay.MILLISECONDS
    private var timeDelay = 100
    private var timeSwipe = 300

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.inToolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val title = getString(R.string.text_go_to_settings)
        val spannableTitle = SpannableString(title)
        spannableTitle.setSpan(
            StyleSpan(Typeface.BOLD),  // Kiểu chữ đậm
            0,
            title.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        supportActionBar?.title = spannableTitle
        optionDelay = OptionDelay.entries.toTypedArray()[PreferenceHelper.getInt(
            Contact.OPTION_DELAY_DEFAULT,
            0
        )]
        timeDelay = PreferenceHelper.getInt(Contact.TIME_DELAY_DEFAULT, 100)
        timeSwipe = PreferenceHelper.getInt(Contact.TIME_SWIPE_DEFAULT, 300)
        binding.spinnerIntervalUnit.setSelection(optionDelay.value)
        binding.editDelayTime.setText("$timeDelay")
        binding.editSwipeTime.setText("$timeSwipe")
        binding.spinnerIntervalUnit.onItemSelectedListener = object : OnItemSelectedListener,
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                optionDelay = OptionDelay.entries.toTypedArray()[p2]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                return false
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if ((binding.editDelayTime.text.toString()
                        .toInt() < 40) && (optionDelay == OptionDelay.MILLISECONDS)
                ) {
                    val popupWarningSpeed = PopupWarningSpeed(this@SettingsActivity)
                    popupWarningSpeed.onDoneAction = {
                        PreferenceHelper.putInt(Contact.OPTION_DELAY_DEFAULT, optionDelay.value)
                        PreferenceHelper.putInt(
                            Contact.TIME_DELAY_DEFAULT,
                            binding.editDelayTime.text.toString().toInt()
                        )
                        PreferenceHelper.putInt(
                            Contact.TIME_SWIPE_DEFAULT,
                            binding.editSwipeTime.text.toString().toInt()
                        )
                        finish()
                    }
                    popupWarningSpeed.showPopup()
                } else {
                    PreferenceHelper.putInt(Contact.OPTION_DELAY_DEFAULT, optionDelay.value)
                    PreferenceHelper.putInt(
                        Contact.TIME_DELAY_DEFAULT,
                        binding.editDelayTime.text.toString().toInt()
                    )
                    PreferenceHelper.putInt(
                        Contact.TIME_SWIPE_DEFAULT,
                        binding.editSwipeTime.text.toString().toInt()
                    )
                    finish()
                }
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        if ((binding.editDelayTime.text.toString()
                .toInt() < 40) && (optionDelay == OptionDelay.MILLISECONDS)
        ) {
            val popupWarningSpeed = PopupWarningSpeed(this@SettingsActivity)
            popupWarningSpeed.onDoneAction = {
                PreferenceHelper.putInt(Contact.OPTION_DELAY_DEFAULT, optionDelay.value)
                PreferenceHelper.putInt(
                    Contact.TIME_DELAY_DEFAULT,
                    binding.editDelayTime.text.toString().toInt()
                )
                PreferenceHelper.putInt(
                    Contact.TIME_SWIPE_DEFAULT,
                    binding.editSwipeTime.text.toString().toInt()
                )
                finish()
            }
            popupWarningSpeed.showPopup()
        } else {
            PreferenceHelper.putInt(Contact.OPTION_DELAY_DEFAULT, optionDelay.value)
            PreferenceHelper.putInt(
                Contact.TIME_DELAY_DEFAULT,
                binding.editDelayTime.text.toString().toInt()
            )
            PreferenceHelper.putInt(
                Contact.TIME_SWIPE_DEFAULT,
                binding.editSwipeTime.text.toString().toInt()
            )
            finish()
        }
        return true
    }

}