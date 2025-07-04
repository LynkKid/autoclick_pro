package com.auto.click.modules.home.ui

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.auto.click.AdMNG
import com.auto.click.AutoClickService
import com.auto.click.ChangeDataConfigReceiver
import com.auto.click.InAppMNG
import com.auto.click.OnChangeDataConfigReceiverListener
import com.auto.click.R
import com.auto.click.appcomponents.utility.Contact
import com.auto.click.appcomponents.utility.PreferenceHelper
import com.auto.click.appcomponents.utility.Utils
import com.auto.click.appcomponents.utility.Utils.dp2px
import com.auto.click.appcomponents.utility.Utils.setEnabledWithAlpha
import com.auto.click.appcomponents.utility.Utils.setProSwitchListener
import com.auto.click.appcomponents.utility.setSafeOnClickListener
import com.auto.click.autoClickService
import com.auto.click.databinding.FragmentHomeBinding
import com.auto.click.modules.auto_start.ui.AutoStartActivity
import com.auto.click.modules.faq.ui.FAQActivity
import com.auto.click.modules.instructions.ui.InstructionsActivity
import com.auto.click.modules.navigation.ui.MainActivity
import com.auto.click.modules.permissions.ui.PermissionsActivity
import com.auto.click.modules.popup.ConfigChoiceBottomPopup
import com.auto.click.modules.popup.PopupSettingsAntiDetect
import com.auto.click.modules.popup.SelectModeBottomPopup
import com.auto.click.modules.premium.ui.PremiumActivity
import com.auto.click.modules.settings.ui.SettingsActivity
import com.auto.click.modules.size_customization.ui.SizeCustomizationActivity
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.util.XPopupUtils
import java.util.Locale

class HomeFragment : Fragment(), OnChangeDataConfigReceiverListener {
    private lateinit var binding: FragmentHomeBinding
    private val changeDataConfigReceiver = ChangeDataConfigReceiver()
    private val handler = Handler(Looper.getMainLooper())
    private val checkAccessibilitySetting: Runnable = object : Runnable {
        override fun run() {
            if (Utils.isAccessibilityEnabled(requireActivity(), AutoClickService::class.java)) {
                val activityIntent = Intent(requireActivity(), MainActivity::class.java)
                activityIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(activityIntent)
                return
            }
            handler.postDelayed(this, 300)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeDataConfigReceiver.listener = this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHomeBinding.inflate(
            inflater, container, false
        )
        val filter = IntentFilter("AUTO_CLICK_CHANGE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireActivity().registerReceiver(
                changeDataConfigReceiver, filter, Context.RECEIVER_EXPORTED
            )
        } else {
            requireActivity().registerReceiver(changeDataConfigReceiver, filter)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.mainToolBar.toolbar)
        val title = getString(R.string.app_name)
        val spannableTitle = SpannableString(title)
        spannableTitle.setSpan(
            StyleSpan(Typeface.BOLD),  // Kiểu chữ đậm
            0, title.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowHomeEnabled(true)
            setDisplayUseLogoEnabled(true)
            this.title = spannableTitle
        }

        setupDrawableStartSizes()
        setupDrawableEndSizes()
        binding.tvVersionName.text =
            String.format("V%s", Utils.getAppVersionName(requireActivity()))
        binding.switchGameModeMenu.isChecked = PreferenceHelper.getBoolean(Contact.MODE_GAME, false)
        binding.switchFoldedMenu.isChecked =
            PreferenceHelper.getBoolean(Contact.MENU_FOLDABLE, false)
        binding.switchMenuLayoutStatus.isChecked =
            PreferenceHelper.getBoolean(Contact.DISPLAY_MENU_HORIZONTAL, false)
        binding.switchMinStatus.isChecked =
            PreferenceHelper.getBoolean(Contact.DISPLAY_MINI_MENU, false)
        binding.switchDailyFree.isChecked =
            PreferenceHelper.getBoolean(Contact.SETTINGS_ANTI_DETECT, false)
        binding.tvInterval.text = String.format(
            Locale.getDefault(),
            "%d ms",
            PreferenceHelper.getInt(Contact.SETTINGS_ANTI_DETECT_TIME, 10)
        )
        binding.tvPosition.text = String.format(
            Locale.getDefault(),
            "%d px",
            PreferenceHelper.getInt(Contact.SETTINGS_ANTI_DETECT_POSITIONS, 0)
        )
        binding.switchGameModeMenu.setProSwitchListener(
            { InAppMNG.isProVersion() },
            { b -> PreferenceHelper.putBoolean(Contact.MODE_GAME, b) },
            {
                val intent = Intent(requireActivity(), PremiumActivity::class.java)
                startActivity(intent)
            })
        binding.switchFoldedMenu.setProSwitchListener(
            { InAppMNG.isProVersion() },
            { b -> PreferenceHelper.putBoolean(Contact.MENU_FOLDABLE, b) },
            {
                val intent = Intent(requireActivity(), PremiumActivity::class.java)
                startActivity(intent)
            })
        binding.switchMenuLayoutStatus.setProSwitchListener(
            { InAppMNG.isProVersion() },
            { b -> PreferenceHelper.putBoolean(Contact.DISPLAY_MENU_HORIZONTAL, b) },
            {
                val intent = Intent(requireActivity(), PremiumActivity::class.java)
                startActivity(intent)
            })
        binding.switchMinStatus.setProSwitchListener(
            { InAppMNG.isProVersion() },
            { b -> PreferenceHelper.putBoolean(Contact.DISPLAY_MINI_MENU, b) },
            {
                val intent = Intent(requireActivity(), PremiumActivity::class.java)
                startActivity(intent)
            })
        binding.switchDailyFree.setOnCheckedChangeListener { compoundButton, b ->
            PreferenceHelper.putBoolean(Contact.SETTINGS_ANTI_DETECT, b)
        }

        binding.tvLetStart.setSafeOnClickListener {
            onRunAutoClick()
        }
        binding.lineSettings.setSafeOnClickListener {
            if (AdMNG.hasAd(requireActivity())) {
                AdMNG.showAd(requireActivity(), object : AdMNG.InterAdListener {
                    override fun onAdShowed() {
                    }

                    override fun onAdHidden() {
                        startActivity(Intent(requireActivity(), SettingsActivity::class.java))
                    }
                })
            } else {
                startActivity(Intent(requireActivity(), SettingsActivity::class.java))
            }
        }
        binding.lineInstructions.setSafeOnClickListener {
            if (AdMNG.hasAd(requireActivity())) {
                AdMNG.showAd(requireActivity(), object : AdMNG.InterAdListener {
                    override fun onAdShowed() {
                    }

                    override fun onAdHidden() {
                        startActivity(Intent(requireActivity(), InstructionsActivity::class.java))
                    }
                })
            } else {
                startActivity(Intent(requireActivity(), InstructionsActivity::class.java))
            }
        }
        binding.lineFAQ.setSafeOnClickListener {
            if (AdMNG.hasAd(requireActivity())) {
                AdMNG.showAd(requireActivity(), object : AdMNG.InterAdListener {
                    override fun onAdShowed() {
                    }

                    override fun onAdHidden() {
                        startActivity(Intent(requireActivity(), FAQActivity::class.java))
                    }
                })
            } else {
                startActivity(Intent(requireActivity(), FAQActivity::class.java))
            }
        }
        binding.linePermissions.setSafeOnClickListener {
            if (AdMNG.hasAd(requireActivity())) {
                AdMNG.showAd(requireActivity(), object : AdMNG.InterAdListener {
                    override fun onAdShowed() {
                    }

                    override fun onAdHidden() {
                        startActivity(Intent(requireActivity(), PermissionsActivity::class.java))
                    }
                })
            } else {
                startActivity(Intent(requireActivity(), PermissionsActivity::class.java))
            }
        }
        binding.lineConfig.setSafeOnClickListener {
            if (AdMNG.hasAd(requireActivity())) {
                AdMNG.showAd(requireActivity(), object : AdMNG.InterAdListener {
                    override fun onAdShowed() {
                    }

                    override fun onAdHidden() {
                        startActivity(
                            Intent(
                                requireActivity(), SizeCustomizationActivity::class.java
                            )
                        )
                    }
                })
            } else {
                startActivity(Intent(requireActivity(), SizeCustomizationActivity::class.java))
            }
        }
        binding.lineAutoStart.setSafeOnClickListener {
            if (AdMNG.hasAd(requireActivity())) {
                AdMNG.showAd(requireActivity(), object : AdMNG.InterAdListener {
                    override fun onAdShowed() {
                    }

                    override fun onAdHidden() {
                        startActivity(Intent(requireActivity(), AutoStartActivity::class.java))
                    }
                })
            } else {
                startActivity(Intent(requireActivity(), AutoStartActivity::class.java))
            }
        }
        binding.lineEditRandom.setSafeOnClickListener {
            val popupSettingsAntiDetect = PopupSettingsAntiDetect(requireActivity())
            popupSettingsAntiDetect.onDoneAction = { antiDetectTime, antiDetectPosition ->
                PreferenceHelper.putInt(Contact.SETTINGS_ANTI_DETECT_TIME, antiDetectTime)
                PreferenceHelper.putInt(Contact.SETTINGS_ANTI_DETECT_POSITIONS, antiDetectPosition)
                binding.tvInterval.text = String.format(
                    Locale.getDefault(), "%d ms", antiDetectTime
                )
                binding.tvPosition.text = String.format(
                    Locale.getDefault(), "%d px", antiDetectPosition
                )
            }
            if (InAppMNG.isProVersion()) {
                popupSettingsAntiDetect.showPopup()
            } else {
                startActivity(Intent(context, PremiumActivity::class.java))
            }
        }
        binding.lineVIP.setSafeOnClickListener {
            if (AdMNG.hasAd(requireActivity())) {
                AdMNG.showAd(requireActivity(), object : AdMNG.InterAdListener {
                    override fun onAdShowed() {
                    }

                    override fun onAdHidden() {
                        startActivity(Intent(context, PremiumActivity::class.java))
                    }
                })
            } else {
                startActivity(Intent(context, PremiumActivity::class.java))
            }
        }
        binding.lineRestore.setSafeOnClickListener {
            if (InAppMNG.isProVersion()) {
                restoreSettings()
            } else {
                startActivity(Intent(context, PremiumActivity::class.java))
            }
        }

        binding.lineVIP.visibility = if (InAppMNG.isProVersion()) View.GONE else View.VISIBLE
    }

    private fun restoreSettings() {
        PreferenceHelper.putInt(Contact.OPTION_DELAY_DEFAULT, 0)
        PreferenceHelper.putInt(Contact.TIME_DELAY_DEFAULT, 100)
        PreferenceHelper.putInt(Contact.TIME_SWIPE_DEFAULT, 300)

        binding.switchGameModeMenu.isChecked = false
        binding.switchFoldedMenu.isChecked = false
        binding.switchMenuLayoutStatus.isChecked = false
        binding.switchMinStatus.isChecked = false
        binding.switchDailyFree.isChecked = false

        PreferenceHelper.putBoolean(Contact.MODE_GAME, false)
        PreferenceHelper.putBoolean(Contact.MENU_FOLDABLE, false)
        PreferenceHelper.putBoolean(Contact.DISPLAY_MENU_HORIZONTAL, false)
        PreferenceHelper.putBoolean(Contact.DISPLAY_MINI_MENU, false)
        PreferenceHelper.putBoolean(Contact.SETTINGS_ANTI_DETECT, false)
        PreferenceHelper.putInt(Contact.SETTINGS_ANTI_DETECT_TIME, 10)
        PreferenceHelper.putInt(Contact.SETTINGS_ANTI_DETECT_POSITIONS, 0)
        binding.tvInterval.text = String.format(
            Locale.getDefault(), "%d ms", 10
        )
        binding.tvPosition.text = String.format(
            Locale.getDefault(), "%d px", 0
        )
    }

    override fun onResume() {
        super.onResume()
        if (autoClickService?.isViewCreated == true) {
            binding.tvLetStart.setBackgroundResource(R.drawable.btn_gray_r_max_bg)
            binding.tvLetStartName.text = resources.getString(R.string.text_let_s_stop)
        } else {
            binding.tvLetStart.setBackgroundResource(R.drawable.bg_2e2ee5_radius_max)
            binding.tvLetStartName.text = resources.getString(R.string.text_let_s_start)
        }
    }

    private fun onRunAutoClick() {
        if (autoClickService?.isViewCreated == true) {
            autoClickService?.removeView()
            return
        }

        // Nếu chưa bật Accessibility Service thì yêu cầu bật
        if (!Utils.isAccessibilityEnabled(requireContext(), AutoClickService::class.java)) {
            Utils.showDialogRequestAccessibility(requireActivity()) {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                handler.postDelayed(checkAccessibilitySetting, 300)
            }
            return
        }

        // Nếu có quảng cáo => hiện quảng cáo trước rồi mới startConfig
        if (AdMNG.hasAd(requireActivity())) {
            AdMNG.showAd(requireActivity(), object : AdMNG.InterAdListener {
                override fun onAdShowed() {}

                override fun onAdHidden() {
                    startConfig()
                }
            })
        } else {
            startConfig()
        }
    }

    private fun startConfig() {
        val dataListConfigSave = Utils.getItemDataList()
        if (dataListConfigSave.isEmpty()) {
            if (autoClickService?.isViewCreated != true) {
                val selectModeBottomPopup =
                    XPopup.Builder(requireActivity()).hasShadowBg(true).moveUpToKeyboard(false)
                        .isViewMode(true).isDestroyOnDismiss(false).statusBarBgColor(
                            ContextCompat.getColor(
                                requireContext(), R.color.color_4D000000
                            )
                        ).asCustom(
                            SelectModeBottomPopup(
                                requireActivity(),
                                XPopupUtils.getStatusBarHeight(requireActivity().window),
                                XPopupUtils.getNavBarHeight(requireActivity().window)
                            )
                        )
                selectModeBottomPopup.show()
            } else {
                autoClickService?.removeView()
            }
        } else {
            if (autoClickService?.isViewCreated != true) {
                val selectModeBottomPopup =
                    XPopup.Builder(requireActivity()).hasShadowBg(true).moveUpToKeyboard(false)
                        .isViewMode(true).isDestroyOnDismiss(false).dismissOnTouchOutside(false)
                        .statusBarBgColor(
                            ContextCompat.getColor(
                                requireContext(), R.color.color_4D000000
                            )
                        ).asCustom(
                            ConfigChoiceBottomPopup(
                                requireActivity(),
                                XPopupUtils.getStatusBarHeight(requireActivity().window),
                                XPopupUtils.getNavBarHeight(requireActivity().window)
                            )
                        )
                selectModeBottomPopup.show()
            } else {
                autoClickService?.removeView()
            }
        }
    }

    override fun onDestroy() {
        requireActivity().unregisterReceiver(changeDataConfigReceiver)
        super.onDestroy()
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }

    override fun onChangeDataConfig() {}

    override fun onAutoClickChange() {
        if (autoClickService?.isViewCreated == true) {
            binding.tvLetStart.setBackgroundResource(R.drawable.btn_gray_r_max_bg)
            binding.tvLetStartName.text = resources.getString(R.string.text_let_s_stop)

            binding.lineSettings.setEnabledWithAlpha(false)
            binding.lineConfig.setEnabledWithAlpha(false)
            binding.lineAutoStart.setEnabledWithAlpha(false)
            binding.switchGameModeMenu.setEnabledWithAlpha(false)
            binding.switchFoldedMenu.setEnabledWithAlpha(false)
            binding.switchMenuLayoutStatus.setEnabledWithAlpha(false)
            binding.switchMinStatus.setEnabledWithAlpha(false)
            binding.switchDailyFree.setEnabledWithAlpha(false)

        } else {
            binding.tvLetStart.setBackgroundResource(R.drawable.bg_2e2ee5_radius_max)
            binding.tvLetStartName.text = resources.getString(R.string.text_let_s_start)

            binding.lineSettings.setEnabledWithAlpha(true)
            binding.lineConfig.setEnabledWithAlpha(true)
            binding.lineAutoStart.setEnabledWithAlpha(true)
            binding.switchGameModeMenu.setEnabledWithAlpha(true)
            binding.switchFoldedMenu.setEnabledWithAlpha(true)
            binding.switchMenuLayoutStatus.setEnabledWithAlpha(true)
            binding.switchMinStatus.setEnabledWithAlpha(true)
            binding.switchDailyFree.setEnabledWithAlpha(true)
        }
    }

    private fun setupDrawableStartSizes() {
        val textViewsWithDrawable = listOf(
            R.id.tv_remove_ads, R.id.tv_unlock_all_features
        )

        textViewsWithDrawable.forEach { viewId ->
            val textView = requireActivity().findViewById<TextView>(viewId)
            val drawables = textView?.compoundDrawablesRelative
            val drawableEnd = drawables?.get(0)

            drawableEnd?.let {
                val height = requireActivity().dp2px(24f)
                val with = requireActivity().dp2px(24f)
                it.setBounds(0, 0, with, height)
                textView.setCompoundDrawablesRelative(
                    it, drawables[1], drawables[2], drawables[3]
                )
            }
        }
    }

    private fun setupDrawableEndSizes() {
        val textViewsWithDrawable = listOf(
            R.id.tv_game_mode,
            R.id.tv_folded_menu,
            R.id.tv_menu_display,
            R.id.tv_minimise_menu,
            R.id.tv_edit,
            R.id.tv_restore_defaults
        )

        textViewsWithDrawable.forEach { viewId ->
            val textView = requireActivity().findViewById<TextView>(viewId)
            val drawables = textView?.compoundDrawablesRelative
            val drawableEnd = drawables?.get(2)

            drawableEnd?.let {
                val height = requireActivity().dp2px(24f)
                val with = requireActivity().dp2px(40f)
                it.setBounds(0, 0, with, height)
                textView.setCompoundDrawablesRelative(
                    drawables[0], drawables[1], it, drawables[3]
                )
            }
        }
    }
}