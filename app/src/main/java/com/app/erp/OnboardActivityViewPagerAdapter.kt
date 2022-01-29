package com.app.erp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OnboardActivityViewPagerAdapter(private var image: List<Int>, private var title: List<String>, private var description: List<String>): RecyclerView.Adapter<OnboardActivityViewPagerAdapter.Pager2ViewHolder>(){

    inner class Pager2ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val itemTitle: TextView = itemView.findViewById(R.id.tv_title)
        val itemDescription: TextView = itemView.findViewById(R.id.tv_description)
        val itemImage: ImageView = itemView.findViewById(R.id.iv_title)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OnboardActivityViewPagerAdapter.Pager2ViewHolder {
        return Pager2ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.viewpager_onboarding_list_item,parent,false))
    }

    override fun onBindViewHolder(
        holder: OnboardActivityViewPagerAdapter.Pager2ViewHolder,
        position: Int
    ) {
        holder.itemTitle.text = title[position]
        holder.itemDescription.text = description[position]
        holder.itemImage.setImageResource(image[position])
    }

    override fun getItemCount(): Int {
        return title.size
    }
}