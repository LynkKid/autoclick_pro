package com.auto.click.modules.popup

import android.content.Context
import android.content.Intent
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auto.click.R
import com.auto.click.model.SelectModeModel
import com.auto.click.modules.popup.adapter.SelectModelAdapter
import com.auto.click.autoClickService
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.util.XPopupUtils

class SelectModeBottomPopup(
    context: Context,
    private var sttBarHeight: Int = 0,
    private var sttNavHeight: Int = 0
) : BottomPopupView(context) {
    private lateinit var selectModelAdapter: SelectModelAdapter

    override fun getImplLayoutId(): Int {
        return R.layout.custom_select_clicker_mode_layout
    }

    override fun onCreate() {
        super.onCreate()
        val recyclerView = findViewById<RecyclerView>(R.id.rvSelectModel)
        recyclerView.setLayoutManager(
            LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
            )
        )
        val dataAdapter = getItemDataList()
        selectModelAdapter = SelectModelAdapter(context, dataAdapter)
        recyclerView.adapter = selectModelAdapter
        (findViewById<ImageView>(R.id.ivClose)).setOnClickListener { dismiss() }
        (findViewById<TextView>(R.id.tvDirectStart)).setOnClickListener {
            val element = dataAdapter.find { it.isSelect }
            when (element?.id) {
                1 -> {
                    autoClickService?.createViewOnePointMode(0)
                    val intent = Intent()
                    intent.setAction("AUTO_CLICK_CHANGE")
                    context.sendBroadcast(intent)
                }

                2 -> {
                    autoClickService?.createViewMultiPointMode(0)
                    val intent = Intent()
                    intent.setAction("AUTO_CLICK_CHANGE")
                    context.sendBroadcast(intent)
                }

                3 -> {
                    autoClickService?.createViewMultiClickSimultaneousMode(0)
                    val intent = Intent()
                    intent.setAction("AUTO_CLICK_CHANGE")
                    context.sendBroadcast(intent)
                }

                4 -> {
                    autoClickService?.createViewPressAndHoldMode(0)
                    val intent = Intent()
                    intent.setAction("AUTO_CLICK_CHANGE")
                    context.sendBroadcast(intent)
                }
            }
            dismiss()
        }

    }

    private fun getItemDataList(): List<SelectModeModel> {
        val arrayList: ArrayList<SelectModeModel> = ArrayList()
        val selectModeModel = SelectModeModel()
        selectModeModel.id = 1
        selectModeModel.name = context.getString(R.string.text_clicker_single_mode)
        selectModeModel.desc = context.getString(R.string.text_clicker_single_desc)
        selectModeModel.isSelect = true
        val selectModeModel2 = SelectModeModel()
        selectModeModel2.id = 2
        selectModeModel2.name = context.getString(R.string.text_clicker_multi_mode)
        selectModeModel2.desc = context.getString(R.string.text_clicker_multi_desc)
        selectModeModel2.isSelect = false
        val selectModeModel3 = SelectModeModel()
        selectModeModel3.id = 3
        selectModeModel3.name = context.getString(R.string.text_clicker_synchronization_mode)
        selectModeModel3.desc = context.getString(R.string.text_clicker_synchronization_desc)
        selectModeModel3.isSelect = false
        val selectModeModel4 = SelectModeModel()
        selectModeModel4.id = 4
        selectModeModel4.name = context.getString(R.string.text_clicker_long_mode)
        selectModeModel4.desc = context.getString(R.string.text_clicker_long_desc)
        selectModeModel4.isSelect = false
        val selectModeModel5 = SelectModeModel()
        selectModeModel5.id = 5
        selectModeModel5.name = context.getString(R.string.text_clicker_edge_mode)
        selectModeModel5.desc = context.getString(R.string.text_clicker_edge_mode_desc)
        selectModeModel5.isSelect = false
        val selectModeModel6 = SelectModeModel()
        selectModeModel6.id = 6
        selectModeModel6.name = context.getString(R.string.text_clicker_record_mode)
        selectModeModel6.desc = context.getString(R.string.text_clicker_record_mode_desc)
        selectModeModel6.isSelect = false
        arrayList.add(selectModeModel)
        arrayList.add(selectModeModel2)
        arrayList.add(selectModeModel3)
//        arrayList.add(selectModeModel6)
        arrayList.add(selectModeModel4)
//        arrayList.add(selectModeModel5)
        return arrayList
    }

    override fun getMaxHeight(): Int {
        return (XPopupUtils.getScreenHeight(context) - sttBarHeight - sttNavHeight)
    }
}