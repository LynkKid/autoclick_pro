package com.auto.click.modules.configuration.ui

import com.auto.click.modules.popup.adapter.ConfigItem
import com.auto.click.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class ConfigListAdapter :
    BaseQuickAdapter<ConfigItem, BaseViewHolder>(R.layout.item_config_layout) {

    init {
        addChildClickViewIds(R.id.ivAppIcon, R.id.ivStartConfig, R.id.ivConfigMore)
    }

    override fun convert(helper: BaseViewHolder, item: ConfigItem) {
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


