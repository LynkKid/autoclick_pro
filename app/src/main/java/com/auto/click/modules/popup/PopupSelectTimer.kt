package com.auto.click.modules.popup

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.NumberPicker
import android.widget.TextView
import com.auto.click.R


class PopupSelectTimer(context: Context) : Dialog(context) {

    private var _onDoneAction: ((mHours: Int, mMinutes: Int, mSeconds: Int) -> Unit)? = null
    var onDoneAction: ((mHours: Int, mMinutes: Int, mSeconds: Int) -> Unit)?
        get() = _onDoneAction
        set(value) {
            _onDoneAction = value
        }

    private lateinit var hoursPicker: NumberPicker
    private lateinit var minutesPicker: NumberPicker
    private lateinit var secondsPicker: NumberPicker

    private var hours: Int = 0
    private var minutes: Int = 0
    private var seconds: Int = 0

    init {
        setContentView(R.layout.floating_select_time_layout)
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
        hoursPicker = findViewById(R.id.hoursPicker)
        minutesPicker = findViewById(R.id.minutesPicker)
        secondsPicker = findViewById(R.id.secondsPicker)
        val btCancel = findViewById<TextView>(R.id.tvCancel)
        val btConfirm = findViewById<TextView>(R.id.tvConfirm)

        hoursPicker.minValue = 0
        hoursPicker.maxValue = 23
        val mDisplayedValuesHr = ArrayList<String>()
        for (i in 0..23) {
            mDisplayedValuesHr.add(String.format("%02d", i))
        }
        hoursPicker.displayedValues = mDisplayedValuesHr.toArray(arrayOfNulls<String>(0))

        minutesPicker.minValue = 0
        minutesPicker.maxValue = 59
        val mDisplayedValuesMin = ArrayList<String>()
        for (i in 0..59) {
            mDisplayedValuesMin.add(String.format("%02d", i))
        }
        minutesPicker.displayedValues = mDisplayedValuesMin.toArray(arrayOfNulls<String>(0))

        secondsPicker.minValue = 0
        secondsPicker.maxValue = 59
        val mDisplayedValuesSc = ArrayList<String>()
        for (i in 0..59) {
            mDisplayedValuesSc.add(String.format("%02d", i))
        }
        secondsPicker.displayedValues = mDisplayedValuesSc.toArray(arrayOfNulls<String>(0))

        btCancel.setOnClickListener {
            dismiss()
        }

        btConfirm.setOnClickListener {
            _onDoneAction?.invoke(hoursPicker.value, minutesPicker.value, secondsPicker.value)
            dismiss()
        }
    }

    private fun setTimeForNumberPicker() {
        hoursPicker.value = hours
        minutesPicker.value = minutes
        secondsPicker.value = seconds
    }

    fun setTimer(mHours: Int, mMinutes: Int, mSeconds: Int) {
        hours = mHours
        minutes = mMinutes
        seconds = mSeconds
        setTimeForNumberPicker()
    }

    fun showPopup() {
        show()
    }
}


