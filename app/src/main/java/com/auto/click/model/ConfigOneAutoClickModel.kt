package com.auto.click.model

import android.view.View
import android.view.WindowManager
import com.auto.click.appcomponents.utility.Contact
import com.auto.click.appcomponents.utility.PreferenceHelper
import java.util.Timer

open class ConfigOneAutoClickModel(
    val id: Long = System.currentTimeMillis(),
    var nameConfig: String,
    var optionDelay: Int = PreferenceHelper.getInt(Contact.OPTION_DELAY_DEFAULT, 0),
    var timerDelay: Int = PreferenceHelper.getInt(Contact.TIME_DELAY_DEFAULT, 100),
    var optionSelected: Int = 0,
    var hours: Int = 0,
    var minutes: Int = 5,
    var seconds: Int = 0,
    var numberOfCycles: Int = 10,
    var antiDetection: Boolean = PreferenceHelper.getBoolean(Contact.SETTINGS_ANTI_DETECT, false),
    var modelAddView: ModelAddView,
)

open class ModelAddView(
    var view: View? = null,
    var paramLayout: WindowManager.LayoutParams? = null,
    var timer: Timer? = null,
    var isAdded: Boolean? = false,
    var locationPoints: LocationPointsModel? = null,
)

fun ConfigOneAutoClickModel.clone(): ConfigOneAutoClickModel {
    return ConfigOneAutoClickModel(
        id = System.currentTimeMillis(),
        nameConfig = nameConfig,
        optionDelay = optionDelay,
        timerDelay = timerDelay,
        optionSelected = optionSelected,
        hours = hours,
        minutes = minutes,
        seconds = seconds,
        numberOfCycles = numberOfCycles,
        antiDetection = antiDetection,
        modelAddView = modelAddView.clone()
    )
}

fun ModelAddView.clone(): ModelAddView {
    return ModelAddView(
        view, paramLayout, timer, isAdded, locationPoints?.clone()
    )
}