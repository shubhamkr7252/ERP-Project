package com.app.erp.admin_view.batch_list_view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.erp.R

internal class BatchListRecyclerViewAdapter(private val batchListRecyclerView: ArrayList<BatchListRecyclerViewDataClass>): RecyclerView.Adapter<BatchListRecyclerViewAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_batch_list_item,parent,false)
        return MyViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
        holder.batch.text = (position+1).toString().plus(". ").plus(batchListRecyclerView[position].Name)
        holder.semester.text = "Semesters: ".plus(batchListRecyclerView[position].Semester?.size.toString())
    }

    override fun getItemCount(): Int {
        return batchListRecyclerView.size
    }

    public class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val batch : TextView = itemView.findViewById(R.id.tv_batch)
        val semester : TextView = itemView.findViewById(R.id.tv_semester)
    }

}