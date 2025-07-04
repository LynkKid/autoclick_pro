package com.auto.click.modules.navigation.viewmodel

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.auto.click.AdMNG
import com.auto.click.MyApplication
import com.auto.click.R
import com.auto.click.modules.configuration.ui.ConfigurationFragment
import com.auto.click.modules.home.ui.HomeFragment
import com.auto.click.modules.navigation.ui.MainActivity
import com.auto.click.modules.premium.ui.PremiumActivity

class MainViewModel : ViewModel() {
    private val homeFragment = HomeFragment.newInstance()
    private val configManagerFragment = ConfigurationFragment.newInstance()
    private val _currentFragment = MutableLiveData<Fragment>()
    val currentFragment: LiveData<Fragment> = _currentFragment
    var oldIndex = 0

    init {
        loadFragment(homeFragment)
    }

    fun onNavigationItemSelected(
        context: Context,
        item: MenuItem,
        application: MyApplication
    ): Boolean {
        when (item.itemId) {
            R.id.homeFragment -> {
                oldIndex = 0
                loadFragment(homeFragment)
                return true
            }

            R.id.configManagerFragment -> {
                oldIndex = 1
                loadFragment(configManagerFragment)
                return true
            }

            R.id.vipFragment -> {
                context.startActivity(Intent(context, PremiumActivity::class.java))
                return true
            }
        }
        return false
    }

    private fun loadFragment(fragment: Fragment) {
        _currentFragment.value = fragment
    }
}

class MainViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return MainViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}