package com.auto.click.modules.popup

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import com.auto.click.R

class PopupConfirm(context: Context) : Dialog(context) {

    private var _onDeleteAction: (() -> Unit)? =
        null
    var onDeleteAction: (() -> Unit)?
        get() = _onDeleteAction
        set(value) {
            _onDeleteAction = value
        }

    init {
        setContentView(R.layout.custom_confirm_center_popup)
        window?.apply {
            val params: WindowManager.LayoutParams = attributes
            params.width = (context.resources.displayMetrics.widthPixels * 0.90).toInt()
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            attributes = params
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        initializeViews()
    }

    private fun initializeViews() {
        val btCancel = findViewById<TextView>(R.id.tvCancel)
        val btDelete = findViewById<TextView>(R.id.tvDelete)

        btCancel.setOnClickListener {
            dismiss()
        }

        btDelete.setOnClickListener {
            _onDeleteAction?.invoke()
            dismiss()
        }
    }

    fun showPopup() {
        show()
    }
}