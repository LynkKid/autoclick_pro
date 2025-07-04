package com.auto.click.modules.popup

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import com.auto.click.R
import com.auto.click.appcomponents.utility.Contact
import com.auto.click.appcomponents.utility.PreferenceHelper
import com.auto.click.appcomponents.utility.Utils.dp2px

class PopupSettingsAntiDetect(context: Context) : Dialog(context) {

    private var _onDoneAction: ((antiDetectTime: Int, antiDetectPosition: Int) -> Unit)? = null
    var onDoneAction: ((antiDetectTime: Int, antiDetectPosition: Int) -> Unit)?
        get() = _onDoneAction
        set(value) {
            _onDoneAction = value
        }

    init {
        setContentView(R.layout.dialog_random_range_layout)
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
        val seekbar = findViewById<SeekBar>(R.id.seekbar)
        val ivRadiusPx = findViewById<View>(R.id.ivRadiusPx)
        val editRandomInterval = findViewById<EditText>(R.id.editRandomInterval)
        val tvSeekBarNum = findViewById<TextView>(R.id.tvSeekBarNum)
        val btCancel = findViewById<TextView>(R.id.tvCancel)
        val btSave = findViewById<TextView>(R.id.tvDone)

        editRandomInterval.setText(
            "${
                PreferenceHelper.getInt(
                    Contact.SETTINGS_ANTI_DETECT_TIME,
                    10
                )
            }"
        )
        tvSeekBarNum.text =
            "${PreferenceHelper.getInt(Contact.SETTINGS_ANTI_DETECT_POSITIONS, 0)} px"
        seekbar.progress = PreferenceHelper.getInt(Contact.SETTINGS_ANTI_DETECT_POSITIONS, 0)
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val dp = (progress / 100f) * 50f
                val px = context.dp2px(dp)
                tvSeekBarNum.text = "$progress px"
                ivRadiusPx.updateLayoutParams {
                    width = px
                    height = px
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btCancel.setOnClickListener {
            dismiss()
        }

        btSave.setOnClickListener {
            _onDoneAction?.invoke(
                editRandomInterval.text.toString().toInt(), seekbar.progress
            )
            dismiss()
        }
    }

    fun showPopup() {
        show()
    }
}
