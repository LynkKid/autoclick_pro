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
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import com.auto.click.model.ConfigMultiAutoClickModel
import com.auto.click.model.ConfigOneAutoClickModel
import com.auto.click.R
import com.auto.click.appcomponents.utility.Contact
import com.auto.click.appcomponents.utility.OptionDelay
import com.auto.click.appcomponents.utility.OptionSelected
import com.auto.click.appcomponents.utility.PreferenceHelper
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
import com.google.android.material.switchmaterial.SwitchMaterial

class PopupGameModeTargetEdit(context: Context) : Dialog(context) {

    private var _onSwitchModeAction: (() -> Unit)? = null
    var onSwitchModeAction: (() -> Unit)?
        get() = _onSwitchModeAction
        set(value) {
            _onSwitchModeAction = value
        }

    private var _onCloseAction: (() -> Unit)? = null
    var onCloseAction: (() -> Unit)?
        get() = _onCloseAction
        set(value) {
            _onCloseAction = value
        }


    init {
        setContentView(R.layout.custom_game_mode_target_edit_popup)
        window?.apply {
            setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
            val params: WindowManager.LayoutParams = attributes
            params.width = (context.resources.displayMetrics.widthPixels * 0.90).toInt()
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            attributes = params
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        initializeViews()
    }

    private fun initializeViews() {
        val tvDefaultMode = findViewById<TextView>(R.id.tvDefaultMode)
        val tvCloseFloating = findViewById<TextView>(R.id.tvCloseFloating)
        val ivClose = findViewById<ImageView>(R.id.ivClose)


        ivClose.setOnClickListener {
            dismiss()
        }

        tvDefaultMode.setOnClickListener {
            _onSwitchModeAction?.invoke()
            dismiss()
        }

        tvCloseFloating.setOnClickListener {
            _onCloseAction?.invoke()
            dismiss()
        }
    }

    fun showPopup() {
        show()
    }
}