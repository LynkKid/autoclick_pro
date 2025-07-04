package com.auto.click.modules.auto_start.ui

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.auto.click.R
import com.auto.click.appcomponents.utility.Contact
import com.auto.click.appcomponents.utility.OptionConfig
import com.auto.click.appcomponents.utility.PreferenceHelper
import com.auto.click.appcomponents.utility.Utils
import com.auto.click.autoClickService
import com.auto.click.databinding.ActivityAutoStartBinding
import com.auto.click.model.AppInfo
import com.auto.click.modules.popup.PopupAutoOpen
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AutoStartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAutoStartBinding
    private lateinit var appListAdapter: AppListAdapter
    private var dataAdapter = arrayListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAutoStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.inToolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val title = getString(R.string.text_auto_start)
        val spannableTitle = SpannableString(title)
        spannableTitle.setSpan(
            StyleSpan(Typeface.BOLD),  // Kiểu chữ đậm
            0,
            title.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        supportActionBar?.title = spannableTitle
        binding.rvAutoOpen.setLayoutManager(
            LinearLayoutManager(
                this@AutoStartActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
        )
        CoroutineScope(Dispatchers.Main).launch {
            binding.pbLoading.visibility = View.VISIBLE
            dataAdapter.clear()
            var dataListApplicationWithConfig: ArrayList<AppInfo> = ArrayList()
            var listApplicationWithConfigString = ""
            withContext(Dispatchers.IO) {
                listApplicationWithConfigString =
                    PreferenceHelper.getString(Contact.LIST_APPLICATION_WITH_CONFIG, "")
                if (listApplicationWithConfigString != "") {
                    val type = object : TypeToken<ArrayList<AppInfo>>() {}.type
                    dataListApplicationWithConfig =
                        Gson().fromJson(listApplicationWithConfigString, type)
                }
            }
            if (dataListApplicationWithConfig.isEmpty()) {
                val installedApps = withContext(Dispatchers.IO) {
                    Utils.getInstalledApps(applicationContext)
                }
                dataAdapter.addAll(installedApps)
                val json = Gson().toJson(dataAdapter)
                PreferenceHelper.putString(Contact.LIST_APPLICATION_WITH_CONFIG, json)
            } else {
                dataAdapter.addAll(dataListApplicationWithConfig)
            }
            appListAdapter = AppListAdapter()
            binding.rvAutoOpen.adapter = appListAdapter
            appListAdapter.setNewInstance(dataAdapter)
            binding.pbLoading.visibility = if (dataAdapter.isEmpty()) View.VISIBLE else View.GONE
            appListAdapter.setOnItemClickListener { baseQuickAdapter, view, i ->
                itemClickSelectApplication(i)
            }
            appListAdapter.setOnItemChildClickListener { baseQuickAdapter, view, i ->
                itemClickSelectApplication(i)
            }
        }
    }

    private fun itemClickSelectApplication(i: Int) {
        val popupAutoOpen = PopupAutoOpen(this@AutoStartActivity, dataAdapter[i])
        popupAutoOpen.onDoneAction = { optionConfig, indexConfig ->
            dataAdapter[i].optionConfig = optionConfig
            dataAdapter[i].indexConfig = indexConfig
            dataAdapter[i].isChecked =
                OptionConfig.entries.toTypedArray()[optionConfig] != OptionConfig.NONE
            appListAdapter.notifyItemChanged(i)
            val json = Gson().toJson(dataAdapter)
            PreferenceHelper.putString(Contact.LIST_APPLICATION_WITH_CONFIG, json)
            autoClickService?.listApplication?.clear()
            dataAdapter.forEach { element ->
                if (element.isChecked) {
                    autoClickService?.listApplication?.add(element)
                }
            }
        }
        popupAutoOpen.showPopup()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}