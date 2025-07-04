package com.auto.click.modules.splash.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.auto.click.AdMNG
import com.auto.click.MyApplication
import com.auto.click.R
import com.auto.click.appcomponents.utility.Contact.FIRST_OPEN_APP
import com.auto.click.appcomponents.utility.PreferenceHelper
import com.auto.click.appcomponents.utility.Utils
import com.auto.click.modules.navigation.ui.MainActivity

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val application = application as MyApplication
        setContentView(R.layout.activity_splash_screen)

        Utils.runDelayed(3000) {
            goToHomeScreen()
            /*if (!PreferenceHelper.getBoolean(FIRST_OPEN_APP, false)) {
                goToTutorialScreen()
            } else {
                goToHomeScreen()
            }*/
            application.isOpenApp = true
        }
        AdMNG.loadAd(this)
    }

    private fun goToHomeScreen() {
        val startIntent = Intent(this, MainActivity::class.java)
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(startIntent)
        PreferenceHelper.putBoolean(FIRST_OPEN_APP, true)
        finish()
    }
}