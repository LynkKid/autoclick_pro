package com.auto.click.modules.auto_start.ui

import android.view.MotionEvent
import android.widget.CheckBox
import android.widget.ImageView
import com.auto.click.R
import com.auto.click.appcomponents.utility.Utils
import com.auto.click.model.AppInfo
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class AppListAdapter :
    BaseQuickAdapter<AppInfo, BaseViewHolder>(R.layout.item_game_app_list) {

    init {
        addChildClickViewIds(R.id.cbChoice)
    }

    override fun convert(helper: BaseViewHolder, appInfo: AppInfo) {
        val imageView = helper.getView<ImageView>(R.id.ivIcon)
        Glide.with(context)
            .load(Utils.base64ToBitmap(appInfo.appIcon))
            .into(imageView)
        helper.setText(R.id.tvName, appInfo.appName)

        val checkBox = helper.getView<CheckBox>(R.id.cbChoice)
        checkBox.isChecked = appInfo.isChecked
        checkBox.isClickable = false
        helper.itemView.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    checkBox.isPressed = true
                    checkBox.jumpDrawablesToCurrentState()
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    true
                }

                MotionEvent.ACTION_UP -> {
                    checkBox.isPressed = false
                    checkBox.jumpDrawablesToCurrentState()
                    view.performClick()
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    checkBox.isPressed = false
                    checkBox.jumpDrawablesToCurrentState()
                    true
                }

                else -> false
            }
        }
    }
}


