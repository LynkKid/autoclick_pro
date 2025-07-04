package com.auto.click.model

import android.view.View
import android.view.WindowManager
import com.auto.click.appcomponents.utility.Contact
import com.auto.click.appcomponents.utility.OrderView
import com.auto.click.appcomponents.utility.PreferenceHelper
import java.util.Timer

open class ConfigMultiAutoClickModel(
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
    var listClick: ArrayList<ModelAddViewForMultiClick>,
    var isMultiClickSimultaneous: Boolean = false,
)

open class ModelAddViewForMultiClick(
    var id: Int,
    var viewStart: View? = null,
    var viewEnd: View? = null,
    var viewOrder: OrderView? = null,
    var paramLayoutStart: WindowManager.LayoutParams? = null,
    var paramLayoutEnd: WindowManager.LayoutParams? = null,
    var paramLayoutOrder: WindowManager.LayoutParams? = null,
    var locationPointsModelStart: LocationPointsModel? = null,
    var locationPointsModelEnd: LocationPointsModel? = null,
    var timer: Timer? = null,
    var isAdded: Boolean = false,
    var optionDelay: Int = 0,
    var timerDelay: Int = 0,
    var optionDelayClickOfNext: Int = PreferenceHelper.getInt(Contact.OPTION_DELAY_DEFAULT, 0),
    var timerDelayClickOfNext: Int = PreferenceHelper.getInt(Contact.TIME_DELAY_DEFAULT, 100),
    var numberOfCycles: Int = 1,
    var timeOfSwipe: Int = PreferenceHelper.getInt(Contact.TIME_SWIPE_DEFAULT, 300),
    var timeOfPressAndHold: Int = PreferenceHelper.getInt(Contact.TIME_PRESS_AND_HOLD_DEFAULT, 300),
    var isClick: Boolean = true,
    var isPressAndHold: Boolean = false,
)

fun ConfigMultiAutoClickModel.clone(): ConfigMultiAutoClickModel {
    val listData = arrayListOf<ModelAddViewForMultiClick>()
    listClick.forEach { element ->
        listData.add(element.clone())
    }
    return ConfigMultiAutoClickModel(
        System.currentTimeMillis(),
        nameConfig,
        optionDelay,
        timerDelay,
        optionSelected,
        hours,
        minutes,
        seconds,
        numberOfCycles,
        antiDetection,
        listData,
        isMultiClickSimultaneous
    )
}

fun ModelAddViewForMultiClick.clone(): ModelAddViewForMultiClick {
    return ModelAddViewForMultiClick(
        id,
        viewStart,
        viewEnd,
        viewOrder,
        paramLayoutStart,
        paramLayoutEnd,
        paramLayoutOrder,
        locationPointsModelStart?.clone(),
        locationPointsModelEnd?.clone(),
        timer,
        isAdded,
        optionDelay,
        timerDelay,
        optionDelayClickOfNext,
        timerDelayClickOfNext,
        numberOfCycles,
        timeOfSwipe,
        timeOfPressAndHold,
        isClick,
        isPressAndHold
    )
}