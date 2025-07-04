package com.auto.click.modules.popup.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auto.click.R
import com.auto.click.model.SelectModeModel
import com.bumptech.glide.Glide

class SelectModelAdapter(
    private val context: Context,
    private val modeModels: List<SelectModeModel>
) : RecyclerView.Adapter<SelectModelAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_select_clicker_mode_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = modeModels[position]
        holder.tvModeName.text = item.name
        holder.tvModeDesc.text = item.desc
        if (item.isSelect) {
            holder.lineSelectBg.setBackgroundResource(R.drawable.bg_2e2ee5_radius12)
            holder.lineSelectBg.alpha = 1.0f
            holder.ivSelectImage.setImageResource(R.drawable.icon_bs_xz)
            when (item.id) {
                1 -> {
                    Glide.with(context).load(Integer.valueOf(R.drawable.icon_gif_mode1))
                        .into(holder.ivModeImage)
                }

                2 -> {
                    Glide.with(context).load(Integer.valueOf(R.drawable.icon_gif_mode2))
                        .into(holder.ivModeImage)
                }

                3 -> {
                    Glide.with(context).load(Integer.valueOf(R.drawable.icon_gif_mode3))
                        .into(holder.ivModeImage)
                }

                4 -> {
                    Glide.with(context).load(Integer.valueOf(R.drawable.icon_gif_mode4))
                        .into(holder.ivModeImage)
                }

                5 -> {
                    holder.ivModeImage.setImageResource(R.drawable.icon_edge_mode)
                }

                6 -> {
                    holder.ivModeImage.setImageResource(R.drawable.icon_record_mode)
                }
            }
        } else {
            holder.lineSelectBg.setBackgroundResource(R.drawable.bg_2e2ee2_radius12)
            holder.lineSelectBg.alpha = 0.8f
            holder.ivSelectImage.setImageResource(R.drawable.icon_hs_wxz)
            when (item.id) {
                1 -> {
                    holder.ivModeImage.setImageResource(R.drawable.icon_mode1)
                }

                2 -> {
                    holder.ivModeImage.setImageResource(R.drawable.icon_mode2)
                }

                3 -> {
                    holder.ivModeImage.setImageResource(R.drawable.icon_mode3)
                }

                4 -> {
                    holder.ivModeImage.setImageResource(R.drawable.icon_mode4)
                }

                5 -> {
                    holder.ivModeImage.setImageResource(R.drawable.icon_edge_mode)
                }

                6 -> {
                    holder.ivModeImage.setImageResource(R.drawable.icon_record_mode)
                }
            }
        }

        holder.view.setOnClickListener {
            modeModels.forEach {
                it.isSelect = false
            }
            item.isSelect = !item.isSelect
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return modeModels.size
    }

    override fun getItemId(position: Int): Long = position.toLong()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val view: View = itemView
        val lineSelectBg: LinearLayout = itemView.findViewById(R.id.lineSelectBg)
        val ivSelectImage: ImageView = itemView.findViewById(R.id.ivSelectImage)
        val tvModeName: TextView = itemView.findViewById(R.id.tvModeName)
        val tvModeDesc: TextView = itemView.findViewById(R.id.tvModeDesc)
        val ivModeImage: ImageView = itemView.findViewById(R.id.ivModeImage)
    }
}