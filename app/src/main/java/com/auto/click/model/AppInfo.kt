package com.auto.click.model

import com.auto.click.appcomponents.utility.OptionConfig

data class AppInfo(
    val appName: String,
    var appIcon: String,
    val applicationId: String,
    var optionConfig: Int = OptionConfig.NONE.value,
    var indexConfig: Int = -1,
    var isChecked:Boolean = false
)