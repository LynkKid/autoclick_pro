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
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import com.auto.click.AutoClickService
import com.auto.click.model.ModelAddViewForMultiClick
import com.auto.click.R
import com.auto.click.appcomponents.utility.OptionDelay
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener

class PopupWarningSpeed(context: Context, isServices: Boolean = false) : Dialog(context) {

    private var _onDoneAction: (() -> Unit)? = null
    var onDoneAction: (() -> Unit)?
        get() = _onDoneAction
        set(value) {
            _onDoneAction = value
        }

    init {
        setContentView(R.layout.custom_warning_speed_center_popup)
        window?.apply {
            if (isServices) {
                setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
            }
            val params: WindowManager.LayoutParams = attributes
            params.width = (context.resources.displayMetrics.widthPixels * 0.90).toInt()
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            attributes = params
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        initializeViews()
    }

    private fun initializeViews() {
        // Khởi tạo các view trong Dialog
        val btCancel = findViewById<TextView>(R.id.tvCancel)
        val btDone = findViewById<TextView>(R.id.tvConfirm)

        btCancel.setOnClickListener {
            dismiss()
        }

        btDone.setOnClickListener {
            _onDoneAction?.invoke()
            dismiss()
        }
    }

    fun showPopup() {
        show()
    }
}