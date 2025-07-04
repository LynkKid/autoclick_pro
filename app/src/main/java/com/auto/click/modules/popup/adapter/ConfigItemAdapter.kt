package com.auto.click.modules.popup.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.auto.click.R

class ConfigItemAdapter(context: Context, configItems: List<ConfigItem>) :
    ArrayAdapter<ConfigItem>(context, 0, configItems) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_spinner_item, parent, false)
        val item = getItem(position)
        val textView: TextView = view.findViewById(android.R.id.text1)
        if (item?.configOne != null) {
            val data = item.configOne
            textView.text = data!!.nameConfig
        } else if (item?.configMulti != null) {
            val data = item.configMulti
            textView.text = data!!.nameConfig
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)
        val item = getItem(position)
        val textView: TextView = view.findViewById(android.R.id.text1)
        if (item?.configOne != null) {
            val data = item.configOne
            textView.text = data!!.nameConfig
        } else if (item?.configMulti != null) {
            val data = item.configMulti
            textView.text = data!!.nameConfig
        }
        return view
    }
}