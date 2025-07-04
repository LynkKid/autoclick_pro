package com.auto.click.modules.popup.adapter

import android.view.View
import android.widget.ImageView
import com.auto.click.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class ConfigChoiceListPopupAdapter :
    BaseQuickAdapter<ConfigItem, BaseViewHolder>(R.layout.item_config_popup_layout) {

    override fun convert(helper: BaseViewHolder, item: ConfigItem) {
        val imageView = helper.getView<ImageView>(R.id.ivAppIcon)
        imageView.visibility = View.VISIBLE
        if (item.configOne != null) {
            val data = item.configOne
            helper.setText(
                R.id.tvConfigMode,
                context.getString(R.string.text_clicker_single_mode)
            )
            helper.setText(R.id.tvConfigName, data!!.nameConfig)
            return
        }
        if (item.configMulti != null) {
            val data = item.configMulti
            helper.setText(
                R.id.tvConfigMode,
                context.getString(R.string.text_clicker_multi_mode)
            )
            helper.setText(R.id.tvConfigName, data!!.nameConfig)
            return
        }
    }
}


