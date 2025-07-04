package com.auto.click.modules.popup

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.TextView
import com.auto.click.modules.popup.adapter.ConfigItem
import com.auto.click.R
import com.lxj.xpopup.core.AttachPopupView

class ConfigEditAttachPopup(context: Context, private var configItem: ConfigItem) :
    AttachPopupView(context) {

    var listener: ((View, ConfigItem) -> Unit)? = null

    override fun getImplLayoutId(): Int {
        return R.layout.custom_config_menu_attach_popup
    }

    override fun onCreate() {
        super.onCreate()
        setupListeners()
        setupDrawableSizes()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupListeners() {
        val actions = listOf(
            R.id.tvEditRename to "Rename",
            R.id.tvBackup to "Backup",
            R.id.tvDelete to "Delete",
            R.id.tvExport to "Export"
        )

        actions.forEach { (viewId, actionName) ->
            findViewById<View>(viewId)?.setOnClickListener { view ->
                listener?.invoke(view, configItem)?.let {
                    dismiss()
                }
            }
        }
    }

    private fun setupDrawableSizes() {
        // Các TextView cần thay đổi kích thước drawableStart
        val textViewsWithDrawable = listOf(
            R.id.tvEditRename,
            R.id.tvBackup,
            R.id.tvDelete,
            R.id.tvExport
        )

        textViewsWithDrawable.forEach { viewId ->
            val textView = findViewById<TextView>(viewId)
            val drawables = textView?.compoundDrawablesRelative
            val drawableStart = drawables?.get(0) // Lấy drawableStart

            drawableStart?.let {
                val size =
                    (24 * resources.displayMetrics.density + 0.5f).toInt() // Kích thước 24dp chuyển sang px
                it.setBounds(0, 0, size, size) // Thiết lập kích thước mới cho drawable
                textView.setCompoundDrawablesRelative(
                    it,
                    drawables[1],
                    drawables[2],
                    drawables[3]
                ) // Thiết lập lại drawable
            }
        }
    }

}