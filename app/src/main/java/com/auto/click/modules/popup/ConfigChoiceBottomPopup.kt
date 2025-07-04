package com.auto.click.modules.popup

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auto.click.R
import com.auto.click.appcomponents.utility.Utils
import com.auto.click.appcomponents.utility.setSafeOnClickListener
import com.auto.click.autoClickService
import com.auto.click.modules.popup.adapter.ConfigChoiceListPopupAdapter
import com.auto.click.modules.popup.adapter.ConfigItem
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.util.XPopupUtils

class ConfigChoiceBottomPopup(
    context: Context,
    private var sttBarHeight: Int = 0,
    private var sttNavHeight: Int = 0,
) : BottomPopupView(context) {
    private lateinit var configChoiceListPopupAdapter: ConfigChoiceListPopupAdapter
    private var lastClickTime: Long = 0
    private var isClick = false

    override fun getImplLayoutId(): Int {
        return R.layout.custom_config_list_center_popup
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("PHT", "onCreate")
        val recyclerView = findViewById<RecyclerView>(R.id.rvConfigList)
        recyclerView.setLayoutManager(
            LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
            )
        )
        val dataAdapter = Utils.getItemDataList()
        configChoiceListPopupAdapter = ConfigChoiceListPopupAdapter()
        recyclerView.adapter = configChoiceListPopupAdapter
        configChoiceListPopupAdapter.setNewInstance(dataAdapter)
        (findViewById<ImageView>(R.id.ivClose)).setSafeOnClickListener { dismiss() }
        (findViewById<TextView>(R.id.tvCreateNew)).setSafeOnClickListener {
            if(isClick) return@setSafeOnClickListener
            isClick = true
            if (autoClickService?.isViewCreated == true) {
                Utils.runDelayed(100) { dismiss() }
                return@setSafeOnClickListener
            }
            val selectModeBottomPopup = XPopup.Builder(context).hasShadowBg(true)
                .moveUpToKeyboard(false)
                .isViewMode(true)
                .isDestroyOnDismiss(false)
                .dismissOnTouchOutside(false)
                .statusBarBgColor(resources.getColor(R.color.color_4D000000))
                .asCustom(
                    SelectModeBottomPopup(
                        context,
                        sttBarHeight,
                        sttNavHeight
                    )
                )
            selectModeBottomPopup.show()
            Utils.runDelayed(100) { dismiss() }
        }
        configChoiceListPopupAdapter.setOnItemClickListener { baseQuickAdapter, view, i ->
            if(isClick) return@setOnItemClickListener
            isClick = true
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < 400) {
                Log.d("PHT", "Bỏ qua -- ${currentTime - lastClickTime}")
                lastClickTime = currentTime
            } else {
                Log.d("PHT", "Thực thi -- ${currentTime - lastClickTime}")
                lastClickTime = currentTime
                if (baseQuickAdapter.data[i] is ConfigItem) {
                    if (autoClickService?.isViewCreated == true) {

                    }
                    if ((baseQuickAdapter.data[i] as ConfigItem).configOne != null) {
                        autoClickService?.createViewOnePointMode(
                            (baseQuickAdapter.data[i] as ConfigItem).configOne!!.id
                        )
                        val intent = Intent()
                        intent.setAction("AUTO_CLICK_CHANGE")
                        context.sendBroadcast(intent)
                        Utils.runDelayed(100) { dismiss() }
                        return@setOnItemClickListener
                    }
                    if ((baseQuickAdapter.data[i] as ConfigItem).configMulti != null) {
                        autoClickService?.createViewMultiPointMode(
                            (baseQuickAdapter.data[i] as ConfigItem).configMulti!!.id
                        )
                        val intent = Intent()
                        intent.setAction("AUTO_CLICK_CHANGE")
                        context.sendBroadcast(intent)
                        Utils.runDelayed(100) { dismiss() }
                        return@setOnItemClickListener
                    }
                }
            }
        }
    }

    override fun onDismiss() {
        Log.d("PHT", "onDismiss")
        super.onDismiss()
    }

    override fun getMaxHeight(): Int {
        return (XPopupUtils.getScreenHeight(context) - sttBarHeight - sttNavHeight)
    }
}