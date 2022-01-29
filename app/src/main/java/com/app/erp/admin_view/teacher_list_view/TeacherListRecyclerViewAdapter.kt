package com.app.erp.admin_view.teacher_list_view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.erp.R
import com.google.firebase.firestore.FirebaseFirestore

class TeacherListRecyclerViewAdapter(private val teacherList: ArrayList<TeacherListRecyclerViewDataClass>) : RecyclerView.Adapter<TeacherListRecyclerViewAdapter.MyViewHolder>()
{
    private val db = FirebaseFirestore.getInstance()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_teacher_list_item,parent,false)

        return  MyViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
        val currentItem = teacherList[position]
        holder.recyclerViewItemCount.text = (position+1).toString().plus(". ")
        holder.name.text = currentItem.Name
        holder.code.text = currentItem.Code
        holder.email.text = currentItem.Email
        holder.phone.text = currentItem.Phone.toString()
        db.collection("CourseInfo").whereArrayContainsAny("Teacher", listOf(currentItem.Code.toString())).get()
            .addOnCompleteListener { query ->
                if(query.isSuccessful){
                    val temp = ArrayList<String>()
                    for(doc in query.result!!){
                        temp.add(doc.data.getValue("CourseName").toString())
                        val temp2 = temp.toString()
                        holder.course.text = temp2.substring(1,temp2.length-1)
                    }
                }

            }
        holder.age.text = currentItem.Gender.toString().plus(", ").plus(currentItem.Age.toString())
        if(holder.age.text == "null, null"){
            holder.age.text = ""
        }
    }

    override fun getItemCount(): Int {
        return teacherList.size
    }

    public class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val recyclerViewItemCount : TextView = itemView.findViewById(R.id.recyclerViewItemCount)
        val name : TextView = itemView.findViewById(R.id.tv_name)
        val code : TextView = itemView.findViewById(R.id.tv_code)
        val email : TextView = itemView.findViewById(R.id.tv_email)
        val phone : TextView = itemView.findViewById(R.id.tv_phone)
        val age : TextView = itemView.findViewById(R.id.tv_gender)
        val course : TextView = itemView.findViewById(R.id.tv_course)
        val genderLayout : LinearLayout = itemView.findViewById(R.id.genderLayout)
        val emailLayout : LinearLayout = itemView.findViewById(R.id.emailLayout)
        val phoneLayout : LinearLayout = itemView.findViewById(R.id.phoneLayout)
    }
}