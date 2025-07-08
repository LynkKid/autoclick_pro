package com.auto.click

import android.app.Application
import android.util.Log
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
            Log.e("MyApplication", "Security exception during PreferenceHelper init", e)
        } catch (e: IOException) {
            Log.e("MyApplication", "IO exception during PreferenceHelper init", e)
        } catch (e: Exception) {
            Log.e("MyApplication", "Unexpected error during PreferenceHelper init", e)
        }
        
        try {
            InAppMNG.initWith(this)
        } catch (e: Exception) {
            Log.e("MyApplication", "Error initializing InAppMNG", e)
        }
        
        try {
            AdMNG.initWith(this).apply { setDistance(10) }
        } catch (e: Exception) {
            Log.e("MyApplication", "Error initializing AdMNG", e)
        }
    }
}