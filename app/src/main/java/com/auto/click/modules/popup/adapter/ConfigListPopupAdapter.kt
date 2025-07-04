package com.auto.click.modules.popup.adapter

import com.auto.click.R
import com.auto.click.model.ConfigMultiAutoClickModel
import com.auto.click.model.ConfigOneAutoClickModel
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class ConfigListPopupAdapter :
    BaseQuickAdapter<ConfigItem, BaseViewHolder>(R.layout.item_config_popup_layout) {

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

data class ConfigItem(
    var configOne: ConfigOneAutoClickModel? = null,
    var configMulti: ConfigMultiAutoClickModel? = null
)


