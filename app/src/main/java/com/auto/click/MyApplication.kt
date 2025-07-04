package com.auto.click

import android.app.Application
import com.auto.click.AdMNG.Companion.setDistance
import com.auto.click.appcomponents.utility.PreferenceHelper
import java.io.IOException
import java.security.GeneralSecurityException

class MyApplication : Application() {
    var isOpenApp: Boolean = false

    override fun onCreate() {
        super.onCreate()
        try {
            PreferenceHelper.init(applicationContext)
        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        InAppMNG.initWith(this)
        AdMNG.initWith(this).apply { setDistance(10) }
    }


}