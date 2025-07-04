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
import com.auto.click.R
import com.auto.click.appcomponents.utility.OptionDelay
import com.auto.click.model.ModelAddViewForMultiClick
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener

class PopupSettingClick(context: Context, element: ModelAddViewForMultiClick) :
    Dialog(context) {
    private var optionDelay = OptionDelay.MILLISECONDS
    private var optionDelayClickOfNext = OptionDelay.MILLISECONDS

    private var _onDoneAction: ((optionDelay: Int, delayTime: String, optionDelayOfNext: Int, delayTimeClickOfNext: String, repeatNum: String, swipeTime: String, pressAndHoldTime: String?) -> Unit)? =
        null
    var onDoneAction: ((optionDelay: Int, delayTime: String, optionDelayOfNext: Int, delayTimeClickOfNext: String, repeatNum: String, swipeTime: String, pressAndHoldTime: String?) -> Unit)?
        get() = _onDoneAction
        set(value) {
            _onDoneAction = value
        }

    init {
        setContentView(R.layout.custom_view_edit_popup)
        window?.apply {
            setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
            val params: WindowManager.LayoutParams = attributes
            params.width = (context.resources.displayMetrics.widthPixels * 0.90).toInt()
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            attributes = params
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        initializeViews(element)
    }

    private fun initializeViews(element: ModelAddViewForMultiClick) {
        // Khởi tạo các view trong Dialog
        val tvViewNumber = findViewById<TextView>(R.id.tvViewNumber)
        val lineTimeDelay = findViewById<LinearLayout>(R.id.lineTimeDelay)
        val editDelayTime = findViewById<EditText>(R.id.editDelayTime)
        val spinnerIntervalUnit = findViewById<Spinner>(R.id.spinnerIntervalUnit)
        val lineTimeDelayClickOfNext = findViewById<LinearLayout>(R.id.lineTimeDelayClickOfNext)
        val editDelayTimeClickOfNext = findViewById<EditText>(R.id.editDelayTimeClickOfNext)
        val spinnerIntervalUnitClickOfNext = findViewById<Spinner>(R.id.spinnerIntervalUnitClickOfNext)
        val lineRepeatLayout = findViewById<LinearLayout>(R.id.lineRepeatLayout)
        val editRepeatNum = findViewById<EditText>(R.id.editRepeatNum)
        val rlSetTips = findViewById<RelativeLayout>(R.id.rlSetTips)
        val ivClose = findViewById<ImageView>(R.id.ivClose)
        val lineSwipe = findViewById<LinearLayout>(R.id.lineSwipe)
        val editSwipeTime = findViewById<EditText>(R.id.editSwipeTime)
        val linePressAndHold = findViewById<LinearLayout>(R.id.linePressAndHold)
        val editPressAndHoldTime = findViewById<EditText>(R.id.editPressAndHoldTime)
        val btCancel = findViewById<TextView>(R.id.tvCancel)
        val btDone = findViewById<TextView>(R.id.tvDone)

        tvViewNumber.text =
            "${context.resources.getString(R.string.text_edit_target)} ${element.id}"
        editDelayTime.setText(element.timerDelay.toString())
        editDelayTimeClickOfNext.setText(element.timerDelayClickOfNext.toString())
        spinnerIntervalUnit.setSelection(element.optionDelay)
        spinnerIntervalUnitClickOfNext.setSelection(element.optionDelayClickOfNext)
        editRepeatNum.setText(element.numberOfCycles.toString())
        if (element.isClick && !element.isPressAndHold) {
            lineTimeDelay.visibility = View.VISIBLE
            lineTimeDelayClickOfNext.visibility = View.VISIBLE
            lineRepeatLayout.visibility = View.VISIBLE
            lineSwipe.visibility = View.GONE
            linePressAndHold.visibility = View.GONE
        } else if (element.isClick && element.isPressAndHold) {
            lineTimeDelay.visibility = View.GONE
            lineTimeDelayClickOfNext.visibility = View.VISIBLE
            lineRepeatLayout.visibility = View.GONE
            lineSwipe.visibility = View.GONE
            linePressAndHold.visibility = View.VISIBLE
        } else {
            lineTimeDelay.visibility = View.VISIBLE
            lineTimeDelayClickOfNext.visibility = View.VISIBLE
            lineRepeatLayout.visibility = View.VISIBLE
            lineSwipe.visibility = View.VISIBLE
            linePressAndHold.visibility = View.GONE
        }
        editSwipeTime.setText(element.timeOfSwipe.toString())
        editPressAndHoldTime.setText(element.timeOfPressAndHold.toString())

        spinnerIntervalUnit.onItemSelectedListener = object : OnItemSelectedListener,
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                optionDelay = OptionDelay.entries.toTypedArray()[p2]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                return false
            }
        }

        spinnerIntervalUnitClickOfNext.onItemSelectedListener = object : OnItemSelectedListener,
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                optionDelayClickOfNext = OptionDelay.entries.toTypedArray()[p2]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                return false
            }
        }

        ivClose.setOnClickListener {
            rlSetTips.visibility = View.GONE
        }

        btCancel.setOnClickListener {
            dismiss()
        }

        btDone.setOnClickListener {
            val delayTime = editDelayTime.text.toString()
            val delayTimeClickOfNext = editDelayTimeClickOfNext.text.toString()
            val repeatNum = editRepeatNum.text.toString()
            val swipeTime = editSwipeTime.text.toString()
            val pressAndHoldTime = editPressAndHoldTime.text.toString()
            if ((delayTimeClickOfNext.toInt() < 40) && (optionDelayClickOfNext == OptionDelay.MILLISECONDS)) {
                val popupWarningSpeed = PopupWarningSpeed(context, true)
                popupWarningSpeed.onDoneAction = {
                    _onDoneAction?.invoke(
                        optionDelay.value,
                        delayTime,
                        optionDelayClickOfNext.value,
                        delayTimeClickOfNext,
                        repeatNum,
                        swipeTime,
                        pressAndHoldTime
                    )
                    dismiss()
                }
                popupWarningSpeed.showPopup()
            } else {
                _onDoneAction?.invoke(
                    optionDelay.value,
                    delayTime,
                    optionDelayClickOfNext.value,
                    delayTimeClickOfNext,
                    repeatNum,
                    swipeTime,
                    pressAndHoldTime
                )
                dismiss()
            }
        }
    }

    fun showPopup() {
        show()
    }
}