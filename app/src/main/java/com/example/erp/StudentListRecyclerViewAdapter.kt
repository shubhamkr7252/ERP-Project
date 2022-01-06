package com.example.erp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentListRecyclerViewAdapter(
    private var studentList : ArrayList<StudentListRecyclerViewDataClass>) : RecyclerView.Adapter<StudentListRecyclerViewAdapter.RecyclerViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_student_list_item,parent,false)
        return RecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val currentItem = studentList[position]
        holder.recyclerViewItemCount.text = (position+1).toString().plus(". ")
        holder.name.text = currentItem.Name
        holder.srn.text = currentItem.SRN
        holder.phone.text = currentItem.Phone.toString()
        holder.gender.text = currentItem.Gender.toString().plus(", ").plus(currentItem.Age)
        if(holder.gender.text.toString() == "null, null"){
            holder.gender.text = ""
        }
        if(holder.gender.text.toString() == ", "){
            holder.gender.text = ""
        }
        holder.batch.text = currentItem.Batch.toString().plus(", ").plus(currentItem.Semester)
        if(holder.batch.text.toString() == "null, null"){
            holder.batch.text = ""
        }
        if(holder.batch.text.toString() == ", "){
            holder.batch.text = ""
        }
    }

    override fun getItemCount(): Int {
        return studentList.size
    }

    class RecyclerViewHolder(itemView : View ) : RecyclerView.ViewHolder(itemView){
        val recyclerViewItemCount : TextView = itemView.findViewById(R.id.recyclerViewItemCount)
        val name : TextView = itemView.findViewById(R.id.tv_name)
        val srn : TextView = itemView.findViewById(R.id.tv_srn)
        val phone : TextView = itemView.findViewById(R.id.tv_phone)
        val gender : TextView = itemView.findViewById(R.id.tv_gender)
        val batch : TextView = itemView.findViewById(R.id.tv_batch)
        val genderLayout : LinearLayout = itemView.findViewById(R.id.genderLayout)
        val phoneLayout : LinearLayout = itemView.findViewById(R.id.phoneLayout)
        val batchSemesterLayout : LinearLayout = itemView.findViewById(R.id.batchSemesterLayout)
    }
}