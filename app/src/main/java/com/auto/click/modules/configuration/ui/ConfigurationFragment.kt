package com.auto.click.modules.configuration.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.auto.click.AdMNG
import com.auto.click.AutoClickService
import com.auto.click.ChangeDataConfigReceiver
import com.auto.click.modules.popup.adapter.ConfigItem
import com.auto.click.OnChangeDataConfigReceiverListener
import com.auto.click.R
import com.auto.click.appcomponents.utility.Contact
import com.auto.click.appcomponents.utility.PreferenceHelper
import com.auto.click.appcomponents.utility.Utils
import com.auto.click.autoClickService
import com.auto.click.model.clone
import com.auto.click.databinding.FragmentConfigurationBinding
import com.auto.click.modules.navigation.ui.MainActivity
import com.auto.click.modules.popup.ConfigEditAttachPopup
import com.auto.click.modules.popup.PopupConfirm
import com.auto.click.modules.popup.PopupRename
import com.auto.click.modules.popup.SelectModeBottomPopup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.gson.Gson
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.util.XPopupUtils

class ConfigurationFragment : Fragment(), OnChangeDataConfigReceiverListener {
    private lateinit var binding: FragmentConfigurationBinding
    private var currentActivity: Activity? = null
    private lateinit var configListAdapter: ConfigListAdapter
    private val changeDataConfigReceiver = ChangeDataConfigReceiver()
    private val handler = Handler(Looper.getMainLooper())
    private val checkAccessibilitySetting: Runnable = object : Runnable {
        override fun run() {
            if (Utils.isAccessibilityEnabled(currentActivity!!, AutoClickService::class.java)) {
                val activityIntent = Intent(currentActivity!!, MainActivity::class.java)
                activityIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(activityIntent)
                return
            }
            handler.postDelayed(this, 300)
        }
    }
    private var dataAdapter = arrayListOf<ConfigItem>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeDataConfigReceiver.listener = this
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentConfigurationBinding.inflate(inflater, container, false)
        currentActivity = activity
        val filter = IntentFilter("CHANGE_DATA_CONFIG")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            currentActivity!!.registerReceiver(
                changeDataConfigReceiver,
                filter,
                Context.RECEIVER_EXPORTED
            )
        } else {
            currentActivity!!.registerReceiver(changeDataConfigReceiver, filter)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvManageConfig.setLayoutManager(
            LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
            )
        )
        dataAdapter = Utils.getItemDataList()
        configListAdapter = ConfigListAdapter()
        binding.rvManageConfig.adapter = configListAdapter
        configListAdapter.setEmptyView(
            LayoutInflater.from(context).inflate(R.layout.ui_empty, binding.rvManageConfig, false)
                .apply {
                    findViewById<TextView>(R.id.tvCreate).setOnClickListener {
                        if (!Utils.isAccessibilityEnabled(
                                requireContext(),
                                AutoClickService::class.java
                            )
                        ) {
                            Utils.showDialogRequestAccessibility(currentActivity!!) {
                                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                handler.postDelayed(checkAccessibilitySetting, 300)
                            }
                        } else {
                            if (autoClickService?.isViewCreated != true) {
                                val selectModeBottomPopup =
                                    XPopup.Builder(currentActivity!!).hasShadowBg(true)
                                        .moveUpToKeyboard(false)
                                        .isViewMode(true)
                                        .isDestroyOnDismiss(false)
                                        .statusBarBgColor(resources.getColor(R.color.color_4D000000))
                                        .asCustom(
                                            SelectModeBottomPopup(
                                                currentActivity!!,
                                                XPopupUtils.getStatusBarHeight(currentActivity!!.window),
                                                XPopupUtils.getNavBarHeight(currentActivity!!.window)
                                            )
                                        )
                                selectModeBottomPopup.show()
                            } else {
                                autoClickService?.removeView()
                            }
                        }
                    }
                })
        configListAdapter.setNewInstance(dataAdapter)
        configListAdapter.setOnItemChildClickListener() { baseQuickAdapter, view, i ->
            when (view.id) {
                R.id.ivStartConfig -> {
                    if (!Utils.isAccessibilityEnabled(
                            currentActivity!!,
                            AutoClickService::class.java
                        )
                    ) {
                        Utils.showDialogSettingAccessibility(currentActivity!!, {
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                            handler.postDelayed(checkAccessibilitySetting, 300)
                        }, {})
                    } else {
                        if (AdMNG.hasAd(currentActivity!!)) {
                            AdMNG.showAd(currentActivity!!, object : AdMNG.InterAdListener {
                                override fun onAdShowed() {
                                }

                                override fun onAdHidden() {
                                    startConfig(baseQuickAdapter, i)
                                }
                            })
                        } else {
                            startConfig(baseQuickAdapter, i)
                        }
                    }
                }

                R.id.ivConfigMore -> {
                    val configEditAttachPopup = ConfigEditAttachPopup(
                        currentActivity!!,
                        (baseQuickAdapter.data[i] as ConfigItem)
                    )
                    configEditAttachPopup.listener = { view, item ->
                        when (view.id) {
                            R.id.tvEditRename -> {
                                var name: String = ""
                                if (item.configOne != null) {
                                    name = item.configOne!!.nameConfig
                                } else if (item.configMulti != null) {
                                    name = item.configMulti!!.nameConfig
                                }
                                val popupRename = PopupRename(autoClickService!!, name)
                                popupRename.onDoneAction = { name ->
                                    if (item.configOne != null) {
                                        item.configOne!!.nameConfig = name
                                    } else if (item.configMulti != null) {
                                        item.configMulti!!.nameConfig = name
                                    }
                                    configListAdapter.notifyItemChanged(i)
                                    val json = Gson().toJson(dataAdapter)
                                    PreferenceHelper.putString(Contact.LIST_CONFIG_SAVE, json)
                                }
                                popupRename.showPopup()
                            }

                            R.id.tvBackup -> {
                                val configItem = ConfigItem()
                                if (item.configOne != null) {
                                    configItem.configOne = item.configOne!!.clone()
                                } else if (item.configMulti != null) {
                                    configItem.configMulti = item.configMulti!!.clone()
                                }
                                dataAdapter.add(configItem)
                                configListAdapter.notifyDataSetChanged()
                                val json = Gson().toJson(dataAdapter)
                                PreferenceHelper.putString(Contact.LIST_CONFIG_SAVE, json)
                            }

                            R.id.tvDelete -> {
                                val popupConfirm = PopupConfirm(currentActivity!!)
                                popupConfirm.onDeleteAction =
                                    {
                                        dataAdapter.removeAt(i)
                                        configListAdapter.notifyDataSetChanged()
                                        val json = Gson().toJson(dataAdapter)
                                        PreferenceHelper.putString(Contact.LIST_CONFIG_SAVE, json)
                                    }
                                popupConfirm.showPopup()
                            }

                            R.id.tvExport -> {
                                println("Export clicked")
                            }

                            else -> {}
                        }
                    }
                    val configMenuPopup = XPopup.Builder(currentActivity!!)
                        .isDestroyOnDismiss(false)
                        .atView(view)
                        .asCustom(
                            configEditAttachPopup
                        )
                    configMenuPopup.show()
                }
            }
        }
    }

    private fun startConfig(
        baseQuickAdapter: BaseQuickAdapter<*, *>,
        i: Int
    ) {
        if (baseQuickAdapter.data[i] is ConfigItem) {
            if (autoClickService?.isViewCreated == true) return
            if ((baseQuickAdapter.data[i] as ConfigItem).configOne != null) {
                autoClickService?.createViewOnePointMode(
                    (baseQuickAdapter.data[i] as ConfigItem).configOne!!.id
                )
                val intent = Intent()
                intent.setAction("AUTO_CLICK_CHANGE")
                currentActivity!!.sendBroadcast(intent)
                return
            }
            if ((baseQuickAdapter.data[i] as ConfigItem).configMulti != null) {
                autoClickService?.createViewMultiPointMode(
                    (baseQuickAdapter.data[i] as ConfigItem).configMulti!!.id
                )
                val intent = Intent()
                intent.setAction("AUTO_CLICK_CHANGE")
                currentActivity!!.sendBroadcast(intent)
                return
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ConfigurationFragment()
    }

    override fun onChangeDataConfig() {
        Utils.runOnUiThread {
            dataAdapter = Utils.getItemDataList()
            binding.rvManageConfig.adapter = configListAdapter
            configListAdapter.setNewInstance(dataAdapter)
            configListAdapter.notifyDataSetChanged()
        }
    }

    override fun onAutoClickChange() {}

    override fun onDestroy() {
        currentActivity!!.unregisterReceiver(changeDataConfigReceiver)
        currentActivity = null
        super.onDestroy()
    }
}