package com.app.erp.admin_view.student_list_view

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.app.erp.R
import com.app.erp.teacher_view.ViewHolderAttendance
import com.google.android.material.card.MaterialCardView

class StudentListRecyclerViewAdapter(
    private var studentList : ArrayList<StudentListRecyclerViewDataClass>, val context: Context, private val listener: ViewHolderStudentListOptions.Listener) :
    RecyclerView.Adapter<ViewHolderStudentListOptions>() {

    private var selectedStudent: ArrayList<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderStudentListOptions {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_student_list_item,parent,false)
        return ViewHolderStudentListOptions(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolderStudentListOptions, position: Int) {
        val currentItem = studentList[position]
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

        holder.menuBut.setOnClickListener {
            selectedStudent.clear()
            val popup = PopupMenu(context, holder.menuBut)
            popup.inflate(R.menu.student_list_options_menu)
            popup.setForceShowIcon(true)
            popup.setOnMenuItemClickListener { menuItem ->
                selectedStudent.add(menuItem.numericShortcut.toString())
                selectedStudent.add(currentItem.SRN.toString())
                selectedStudent.add(currentItem.Name.toString())
                selectedStudent.add(currentItem.Email.toString())
                listener.getSelectedStudent(selectedStudent)
                false
            }
            popup.show()
        }
    }

    private fun getSelectedStudent(): ArrayList<String> {
        return selectedStudent
    }

    override fun getItemCount(): Int {
        return studentList.size
    }
}

class ViewHolderStudentListOptions(view: View) : RecyclerView.ViewHolder(view) {
    val name : TextView = itemView.findViewById(R.id.tv_name)
    val srn : TextView = itemView.findViewById(R.id.tv_srn)
    val phone : TextView = itemView.findViewById(R.id.tv_phone)
    val gender : TextView = itemView.findViewById(R.id.tv_gender)
    val batch : TextView = itemView.findViewById(R.id.tv_batch)
    val menuBut: ImageView = itemView.findViewById(R.id.menu)

    interface Listener {
        fun getSelectedStudent(list : ArrayList<String>)
    }
}