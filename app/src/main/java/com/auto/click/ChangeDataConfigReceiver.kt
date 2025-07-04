package com.auto.click

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ChangeDataConfigReceiver : BroadcastReceiver() {
    var listener: OnChangeDataConfigReceiverListener? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "CHANGE_DATA_CONFIG"){
            listener?.onChangeDataConfig()
        }
        if (intent?.action == "AUTO_CLICK_CHANGE"){
            listener?.onAutoClickChange()
        }
    }
}

interface OnChangeDataConfigReceiverListener {
    fun onChangeDataConfig()
    fun onAutoClickChange()
}