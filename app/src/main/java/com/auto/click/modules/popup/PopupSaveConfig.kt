package com.auto.click.modules.popup

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auto.click.modules.popup.adapter.ConfigItem
import com.auto.click.modules.popup.adapter.ConfigListPopupAdapter
import com.auto.click.model.ConfigMultiAutoClickModel
import com.auto.click.model.ConfigOneAutoClickModel
import com.auto.click.R
import com.auto.click.appcomponents.utility.Contact
import com.auto.click.appcomponents.utility.PreferenceHelper
import com.auto.click.appcomponents.utility.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PopupSaveConfig(
    context: Context, configOneAutoClickModel: ConfigOneAutoClickModel? = null,
    configMultiAutoClickModel: ConfigMultiAutoClickModel? = null
) : Dialog(context) {

    private var _onDoneAction: ((name: String) -> Unit)? = null
    var onDoneAction: ((name: String) -> Unit)?
        get() = _onDoneAction
        set(value) {
            _onDoneAction = value
        }

    init {
        setContentView(R.layout.custom_config_save_load_popup)
        window?.apply {
            setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
            val params: WindowManager.LayoutParams = attributes
            params.width = (context.resources.displayMetrics.widthPixels * 0.90).toInt()
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            attributes = params
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        initializeViews(configOneAutoClickModel, configMultiAutoClickModel)
    }

    private fun initializeViews(
        configOneAutoClickModel: ConfigOneAutoClickModel? = null,
        configMultiAutoClickModel: ConfigMultiAutoClickModel? = null
    ) {
        val lineTabSave = findViewById<LinearLayout>(R.id.lineTabSave)
        val lineTabLoad = findViewById<LinearLayout>(R.id.lineTabLoad)
        val viewLineSave = findViewById<View>(R.id.viewLineSave)
        val viewLineLoad = findViewById<View>(R.id.viewLineLoad)
        val editConfigName = findViewById<EditText>(R.id.editConfigName)
        val frameNull = findViewById<FrameLayout>(R.id.frameNull)
        val rvConfig = findViewById<RecyclerView>(R.id.rvConfig)
        val btCancel = findViewById<TextView>(R.id.tvCancel)
        val btSave = findViewById<TextView>(R.id.tvSaveConfig)
        val tvSave = findViewById<TextView>(R.id.tvSave)
        val tvLoad = findViewById<TextView>(R.id.tvLoad)

        if (configOneAutoClickModel != null) {
            editConfigName.setText(configOneAutoClickModel.nameConfig)
        }

        if (configMultiAutoClickModel != null) {
            editConfigName.setText(configMultiAutoClickModel.nameConfig)
        }

        val dataListConfigSave = Utils.getItemDataList()
        val configListPopupAdapter = ConfigListPopupAdapter()
        rvConfig.setLayoutManager(LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false))
        rvConfig.adapter = configListPopupAdapter
        configListPopupAdapter.setEmptyView(LayoutInflater.from(context).inflate(R.layout.ui_load_config_empty, rvConfig, false))
        configListPopupAdapter.setNewInstance(dataListConfigSave)

        lineTabSave.setOnClickListener {
            viewLineSave.visibility = View.VISIBLE
            viewLineLoad.visibility = View.INVISIBLE
            editConfigName.visibility = View.VISIBLE
            frameNull.visibility = View.GONE
            btSave.visibility = View.VISIBLE
            tvSave.setTextColor(context.resources.getColor(R.color.font3))
            tvLoad.setTextColor(context.resources.getColor(R.color.font5))
        }

        lineTabLoad.setOnClickListener {
            viewLineSave.visibility = View.INVISIBLE
            viewLineLoad.visibility = View.VISIBLE
            editConfigName.visibility = View.GONE
            frameNull.visibility = View.VISIBLE
            btSave.visibility = View.GONE
            tvSave.setTextColor(context.resources.getColor(R.color.font5))
            tvLoad.setTextColor(context.resources.getColor(R.color.font3))
        }

        btCancel.setOnClickListener {
            dismiss()
        }

        btSave.setOnClickListener {
            _onDoneAction?.invoke(editConfigName.text.toString())
            dismiss()
        }
    }

    fun showPopup() {
        show()
    }
}
