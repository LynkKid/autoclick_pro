package com.auto.click.modules.popup

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import com.auto.click.R
import com.auto.click.appcomponents.utility.Contact
import com.auto.click.appcomponents.utility.OptionConfig
import com.auto.click.appcomponents.utility.OptionDelay
import com.auto.click.appcomponents.utility.PreferenceHelper
import com.auto.click.appcomponents.utility.Utils
import com.auto.click.model.AppInfo
import com.auto.click.modules.popup.adapter.ConfigItem
import com.auto.click.modules.popup.adapter.ConfigItemAdapter
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PopupAutoOpen(context: Context, appInfo: AppInfo) : Dialog(context) {

    private var _onDoneAction: ((optionConfig:Int, indexConfig:Int) -> Unit)? = null
    var onDoneAction: ((optionConfig:Int, indexConfig:Int) -> Unit)?
        get() = _onDoneAction
        set(value) {
            _onDoneAction = value
        }

    private var optionConfig = OptionConfig.NONE
    private var indexConfig = 0

    init {
        setContentView(R.layout.dialog_auto_open_layout)
        window?.apply {
            val params: WindowManager.LayoutParams = attributes
            params.width = (context.resources.displayMetrics.widthPixels * 0.90).toInt()
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            attributes = params
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        initializeViews(appInfo)
    }

    private fun initializeViews(appInfo: AppInfo) {
        val rGroup = findViewById<RadioGroup>(R.id.rGroup)
        val rbLoadConfig = findViewById<TextView>(R.id.rbLoadConfig)
        val tvNoConfig = findViewById<TextView>(R.id.tvNoConfig)
        val spinnerConfigSelect = findViewById<Spinner>(R.id.spinnerConfigSelect)
        val btCancel = findViewById<TextView>(R.id.tvCancel)
        val btSave = findViewById<TextView>(R.id.tvSave)

        val dataConfig = Utils.getItemDataList()
        rbLoadConfig.isEnabled = dataConfig.isNotEmpty()
        tvNoConfig.visibility = if(dataConfig.isEmpty()) View.VISIBLE else View.GONE
        spinnerConfigSelect.visibility = if(dataConfig.isNotEmpty()) View.VISIBLE else View.GONE
        val adapter = ConfigItemAdapter(context, dataConfig)
        spinnerConfigSelect.adapter = adapter
        indexConfig = if(appInfo.indexConfig == -1) 0 else appInfo.indexConfig
        spinnerConfigSelect.setSelection(indexConfig)
        optionConfig = OptionConfig.entries.toTypedArray()[appInfo.optionConfig]
        when (optionConfig) {
            OptionConfig.NONE -> {
                rGroup.check(R.id.rbDoNothing)
                spinnerConfigSelect.isEnabled = false
            }
            OptionConfig.SINGLE_TARGET_MODE -> {
                rGroup.check(R.id.rbSingle)
                spinnerConfigSelect.isEnabled = false
            }
            OptionConfig.MULTI_TARGET_MODE -> {
                rGroup.check(R.id.rbMulti)
                spinnerConfigSelect.isEnabled = false
            }
            OptionConfig.LOAD_CONFIG -> {
                rGroup.check(R.id.rbLoadConfig)
                spinnerConfigSelect.isEnabled = true
            }
        }
        rGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbDoNothing -> {
                    optionConfig = OptionConfig.NONE
                    spinnerConfigSelect.isEnabled = false
                }
                R.id.rbSingle -> {
                    optionConfig = OptionConfig.SINGLE_TARGET_MODE
                    spinnerConfigSelect.isEnabled = false
                }
                R.id.rbMulti -> {
                    optionConfig = OptionConfig.MULTI_TARGET_MODE
                    spinnerConfigSelect.isEnabled = false
                }
                R.id.rbLoadConfig -> {
                    optionConfig = OptionConfig.LOAD_CONFIG
                    spinnerConfigSelect.isEnabled = true
                }
            }
        }
        spinnerConfigSelect.onItemSelectedListener = object : OnItemSelectedListener,
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                indexConfig = p2
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                return false
            }
        }
        btCancel.setOnClickListener {
            dismiss()
        }

        btSave.setOnClickListener {
            _onDoneAction?.invoke(
                optionConfig.value,
                indexConfig
            )
            dismiss()
        }
    }

    fun showPopup() {
        show()
    }
}
