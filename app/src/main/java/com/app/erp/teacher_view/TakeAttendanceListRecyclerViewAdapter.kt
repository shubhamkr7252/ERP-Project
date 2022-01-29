package com.app.erp.teacher_view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.app.erp.R
import com.app.erp.databinding.RecyclerViewTakeAttendanceListItemBinding
import com.app.erp.gloabal_functions.getColorFromAttribute
import com.google.android.material.card.MaterialCardView

class TakeAttendanceListRecyclerViewAdapter(private val studentListRecyclerViewTake: ArrayList<TakeAttendanceListRecyclerViewDataClass>, val context: Context, private val listener: ViewHolderAttendance.Listener) :

    RecyclerView.Adapter<ViewHolderAttendance>() {
    private var selectedList: ArrayList<String> = ArrayList()
    private var unselectedList: ArrayList<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderAttendance {
        return (ViewHolderAttendance(RecyclerViewTakeAttendanceListItemBinding.inflate(
            LayoutInflater.from(parent.context),parent,false)))
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolderAttendance, position: Int) {
        holder.binding.tvName.text = studentListRecyclerViewTake[position].Name
        holder.binding.tvSrn.text = "SRN: ${studentListRecyclerViewTake[position].SRN}"
        if(!holder.binding.checkbox.isChecked){
            unselectedList.add(studentListRecyclerViewTake[position].SRN.toString())
        }
        else{
            unselectedList.remove(studentListRecyclerViewTake[position].SRN.toString())
        }
        listener.getUnselectedStudentList(unselectedList)
        holder.binding.primaryLayout.setOnClickListener {
            holder.binding.checkbox.performClick()
        }

        holder.binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                holder.binding.checkboxCardLayout.setCardBackgroundColor(getColorFromAttribute(context,R.attr.colorPrimary))
                holder.binding.checkbox.buttonTintList = ColorStateList.valueOf(getColorFromAttribute(context,R.attr.colorOnPrimary))
                holder.binding.checkbox.setTextColor(getColorFromAttribute(context,R.attr.colorOnPrimary))
                selectedList.add(studentListRecyclerViewTake[position].SRN.toString())
                unselectedList.remove(studentListRecyclerViewTake[position].SRN.toString())
                notifyDataSetChanged()
            } else if (!isChecked) {
                holder.binding.checkboxCardLayout.setCardBackgroundColor(getColorFromAttribute(context,R.attr.textFillColor))
                holder.binding.checkbox.buttonTintList = ColorStateList.valueOf(getColorFromAttribute(context,R.attr.colorOutline))
                holder.binding.checkbox.setTextColor(getColorFromAttribute(context,R.attr.colorOutline))
                selectedList.remove(studentListRecyclerViewTake[position].SRN.toString())
                unselectedList.add(studentListRecyclerViewTake[position].SRN.toString())
                notifyDataSetChanged()
            }
            listener.getSelectedStudentList(selectedList)
            listener.getUnselectedStudentList(unselectedList)
            notifyDataSetChanged()
            unselectedList.clear()
        }
    }

    private fun getSelected(): ArrayList<String> {
        return selectedList
    }
    private fun getUnselected(): ArrayList<String> {
        return unselectedList
    }

    override fun getItemCount(): Int {
        return studentListRecyclerViewTake.size
    }
}

class ViewHolderAttendance(val binding: RecyclerViewTakeAttendanceListItemBinding) : RecyclerView.ViewHolder(binding.root) {
    interface Listener {
        fun getSelectedStudentList(list : ArrayList<String>)
        fun getUnselectedStudentList(list : ArrayList<String>)
    }
}