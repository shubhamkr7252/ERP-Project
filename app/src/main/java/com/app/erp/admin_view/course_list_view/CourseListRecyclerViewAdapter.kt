package com.app.erp.admin_view.course_list_view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.erp.R
import com.google.firebase.firestore.FirebaseFirestore

internal class CourseListRecyclerViewAdapter(private val courseList: ArrayList<CourseRecyclerViewDataClass>): RecyclerView.Adapter<CourseListRecyclerViewAdapter.MyViewHolder>()
{
    private val db = FirebaseFirestore.getInstance()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_course_list_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
        val currentItem = courseList[position]
        holder.recyclerViewItemCount.text = (position+1).toString().plus(". ")
        holder.courseCode.text = currentItem.CourseCode
        holder.courseName.text = currentItem.CourseName
        db.collection("TeacherInfo").whereArrayContainsAny("Course", listOf(currentItem.CourseCode.toString())).get()
            .addOnCompleteListener { query ->
                if(query.isSuccessful){
                    val temp = ArrayList<String>()
                    for(doc in query.result!!){
                        temp.add(doc.data.getValue("Name").toString())
                        val temp2 = temp.toString()
                        holder.courseTeacher.text = temp2.substring(1,temp2.length-1)
                    }
                }

            }
    }

    override fun getItemCount(): Int {
        return courseList.size
    }

    public class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val recyclerViewItemCount : TextView = itemView.findViewById(R.id.recyclerViewItemCount)
        val courseCode : TextView = itemView.findViewById(R.id.tv_course_code)
        val courseName : TextView = itemView.findViewById(R.id.tv_course_name)
        val courseTeacher : TextView = itemView.findViewById(R.id.tv_course_teacher)
    }
}