package com.auto.click.modules.navigation.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.auto.click.AdMNG
import com.auto.click.AutoClickService
import com.auto.click.MyApplication
import com.auto.click.R
import com.auto.click.appcomponents.utility.Utils
import com.auto.click.databinding.ActivityMainBinding
import com.auto.click.modules.navigation.viewmodel.MainViewModel
import com.auto.click.modules.navigation.viewmodel.MainViewModelFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(ViewModelStore(), MainViewModelFactory())[MainViewModel::class.java]
    }
    private var currentFragment: Fragment? = null
    private val handler = Handler(Looper.getMainLooper())

    private val checkAccessibilitySetting: Runnable = object : Runnable {
        override fun run() {
            if (Utils.isAccessibilityEnabled(this@MainActivity, AutoClickService::class.java)) {
                val activityIntent = Intent(this@MainActivity, MainActivity::class.java)
                activityIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(activityIntent)
                return
            }
            handler.postDelayed(this, 300)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.bottomNavigation.itemIconTintList = null
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            mainViewModel.onNavigationItemSelected(
                this@MainActivity,
                item,
                application as MyApplication
            )
            true
        }

        mainViewModel.currentFragment.observe(this) { newFragment ->
            if (newFragment != null && newFragment != currentFragment) {
                val transaction = supportFragmentManager.beginTransaction()

                supportFragmentManager.fragments.forEach { fragment ->
                    transaction.hide(fragment)
                }

                if (newFragment.isAdded) {
                    transaction.show(newFragment)
                } else {
                    transaction.add(
                        R.id.fragment_container,
                        newFragment,
                        newFragment::class.java.simpleName
                    )
                }

                transaction.commit()
                currentFragment = newFragment
            }
        }

        if (!Utils.isAccessibilityEnabled(applicationContext, AutoClickService::class.java)) {
            Utils.showDialogRequestAccessibility(this@MainActivity) {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                handler.postDelayed(checkAccessibilitySetting, 300)
            }
        }
        AdMNG.loadBanner(this, binding.adViewBanner)
    }

    override fun onResume() {
        super.onResume()
        when (mainViewModel.oldIndex) {
            0 -> binding.bottomNavigation.selectedItemId = R.id.homeFragment
            1 -> binding.bottomNavigation.selectedItemId = R.id.configManagerFragment
        }
    }

    override fun onDestroy() {
        Log.d("PHT", "onDestroy")
        super.onDestroy()
    }
}