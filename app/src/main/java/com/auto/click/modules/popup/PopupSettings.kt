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

class PopupSettings(
    context: Context,
    configOneAutoClickModel: ConfigOneAutoClickModel? = null,
    configMultiAutoClickModel: ConfigMultiAutoClickModel? = null,
) : Dialog(context) {

    private var optionSelected = OptionSelected.INFINITE
    private var optionDelay = OptionDelay.MILLISECONDS

    private var _onDoneAction: ((nameConfig: String, timeDelay: Int, optionDelay: Int, optionSelected: Int, hours: Int, minutes: Int, seconds: Int, numberOfCycler: Int, antiDetect: Boolean) -> Unit)? =
        null
    var onDoneAction: ((nameConfig: String, timeDelay: Int, optionDelay: Int, optionSelected: Int, hours: Int, minutes: Int, seconds: Int, numberOfCycler: Int, antiDetect: Boolean) -> Unit)?
        get() = _onDoneAction
        set(value) {
            _onDoneAction = value
        }

    private var hours: Int = 0
    private var minutes: Int = 0
    private var seconds: Int = 0

    private lateinit var tvHour: TextView
    private lateinit var tvMinute: TextView
    private lateinit var tvSecond: TextView

    init {
        setContentView(R.layout.custom_config_set_center_popup)
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
        configOneAutoClickModel: ConfigOneAutoClickModel?,
        configMultiAutoClickModel: ConfigMultiAutoClickModel?,
    ) {
        // Khởi tạo các view trong Dialog
        val tvConfigName = findViewById<TextView>(R.id.tvConfigName)
        val ivEditName = findViewById<ImageView>(R.id.ivEditName)
        val lineDelay = findViewById<LinearLayout>(R.id.lineDelay)
        val editDelayTime = findViewById<EditText>(R.id.editDelayTime)
        val spinnerIntervalUnit = findViewById<Spinner>(R.id.spinnerIntervalUnit)
        val radioGroup = findViewById<RadioGroup>(R.id.radio_group)
        val lineTime = findViewById<LinearLayout>(R.id.lineTime)
        val edtNumberOfTimes = findViewById<EditText>(R.id.edtNumberOfTimes)
        val switchDailyFree = findViewById<SwitchMaterial>(R.id.switchDailyFree)
        tvHour = findViewById(R.id.tvHour)
        tvMinute = findViewById(R.id.tvMinute)
        tvSecond = findViewById(R.id.tvSecond)
        val btCancel = findViewById<TextView>(R.id.tvCancel)
        val btDone = findViewById<TextView>(R.id.tvDone)

        if (configOneAutoClickModel != null) {
            tvConfigName.text = configOneAutoClickModel.nameConfig
            lineDelay.visibility = View.VISIBLE
            editDelayTime.setText(configOneAutoClickModel.timerDelay.toString())
            spinnerIntervalUnit.setSelection(configOneAutoClickModel.optionDelay)
            when (configOneAutoClickModel.optionSelected) {
                0 -> radioGroup.check(R.id.rb_option1)
                1 -> radioGroup.check(R.id.rb_option2)
                2 -> radioGroup.check(R.id.rb_option3)
            }
            optionSelected =
                OptionSelected.entries.toTypedArray()[configOneAutoClickModel.optionSelected]
            hours = configOneAutoClickModel.hours
            minutes = configOneAutoClickModel.minutes
            seconds = configOneAutoClickModel.seconds
            edtNumberOfTimes.setText(configOneAutoClickModel.numberOfCycles.toString())
            setTimer()
            switchDailyFree.isChecked = configOneAutoClickModel.antiDetection
        }
        if (configMultiAutoClickModel != null) {
            tvConfigName.text = configMultiAutoClickModel.nameConfig
            if (!configMultiAutoClickModel.isMultiClickSimultaneous) {
                lineDelay.visibility = View.GONE
            }else{
                lineDelay.visibility = View.VISIBLE
                editDelayTime.setText(configMultiAutoClickModel.timerDelay.toString())
            }
            when (configMultiAutoClickModel.optionSelected) {
                0 -> radioGroup.check(R.id.rb_option1)
                1 -> radioGroup.check(R.id.rb_option2)
                2 -> radioGroup.check(R.id.rb_option3)
            }
            optionSelected =
                OptionSelected.entries.toTypedArray()[configMultiAutoClickModel.optionSelected]
            hours = configMultiAutoClickModel.hours
            minutes = configMultiAutoClickModel.minutes
            seconds = configMultiAutoClickModel.seconds
            edtNumberOfTimes.setText(configMultiAutoClickModel.numberOfCycles.toString())
            setTimer()
            switchDailyFree.isChecked = configMultiAutoClickModel.antiDetection
        }

        ivEditName.setOnClickListener {
            val popupRename = PopupRename(context, tvConfigName.text.toString())
            popupRename.onDoneAction = { name ->
                tvConfigName.text = name
            }
            popupRename.showPopup()
        }
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
        lineTime.setOnClickListener {
            val popupSelectTimer = PopupSelectTimer(context)
            popupSelectTimer.onDoneAction = { mHours, mMinutes, mSeconds ->
                hours = mHours
                minutes = mMinutes
                seconds = mSeconds
                setTimer()
            }
            popupSelectTimer.setTimer(hours, minutes, seconds)
            popupSelectTimer.showPopup()
        }
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_option1 -> optionSelected = OptionSelected.INFINITE
                R.id.rb_option2 -> optionSelected = OptionSelected.BY_TIME
                R.id.rb_option3 -> optionSelected = OptionSelected.BY_COUNT
            }
        }

        btCancel.setOnClickListener {
            dismiss()
        }

        btDone.setOnClickListener {
            if (configOneAutoClickModel != null && ((editDelayTime.text.toString().toInt() < 40) && (optionDelay == OptionDelay.MILLISECONDS))) {
                val popupWarningSpeed = PopupWarningSpeed(context, true)
                popupWarningSpeed.onDoneAction = {
                    _onDoneAction?.invoke(
                        tvConfigName.text.toString(),
                        editDelayTime.text.toString().toInt(),
                        optionDelay.value,
                        optionSelected.value,
                        hours,
                        minutes,
                        seconds,
                        edtNumberOfTimes.text.toString().toInt(),
                        switchDailyFree.isChecked
                    )
                    dismiss()
                }
                popupWarningSpeed.showPopup()
            } else {
                _onDoneAction?.invoke(
                    tvConfigName.text.toString(),
                    editDelayTime.text.toString().toInt(),
                    optionDelay.value,
                    optionSelected.value,
                    hours,
                    minutes,
                    seconds,
                    edtNumberOfTimes.text.toString().toInt(),
                    switchDailyFree.isChecked
                )
                dismiss()
            }
        }
    }

    private fun setTimer() {
        tvHour.text = String.format("%02d", hours)
        tvMinute.text = String.format("%02d", minutes)
        tvSecond.text = String.format("%02d", seconds)
    }

    fun showPopup() {
        show()
    }
}