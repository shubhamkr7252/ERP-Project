package com.example.erp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AttendanceListRecyclerViewAdapter(private val attendanceList: ArrayList<AttendanceListRecyclerViewDataClass>): RecyclerView.Adapter<AttendanceListRecyclerViewAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_attendance_list_item,parent,false)
        return MyViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tv_date.text = attendanceList[position].Date
        holder.tv_time.text = convertedTime(attendanceList[position].Time.toString())
        holder.tv_student_strength_details.text = "Students Present: ".plus(attendanceList[position].StudentPresent?.size)
            .plus("\nStudent Absent: ").plus(attendanceList[position].StudentAbsent?.size)
            .plus("\nTotal Strength: ").plus(attendanceList[position].TotalStrength)
        holder.tv_teacher.text = attendanceList[position].Teacher.plus(" - ").plus(attendanceList[position].TeacherCode)
        holder.tv_batch_semester.text = attendanceList[position].Batch.plus(" - ").plus(attendanceList[position].Semester)
        holder.tv_course.text = attendanceList[position].Course.plus(" - ").plus(attendanceList[position].CourseCode)
    }

    override fun getItemCount(): Int {
        return attendanceList.size
    }

    public class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val tv_date: TextView = itemView.findViewById(R.id.tv_date)
        val tv_time: TextView = itemView.findViewById(R.id.tv_time)
        val tv_student_strength_details: TextView = itemView.findViewById(R.id.tv_student_strength_details)
        val tv_teacher: TextView = itemView.findViewById(R.id.tv_teacher)
        val tv_batch_semester: TextView = itemView.findViewById(R.id.tv_batch_semester)
        val tv_course: TextView = itemView.findViewById(R.id.tv_course)
    }

    @SuppressLint("SimpleDateFormat")
    internal fun convertedTime(args: String): String {
        val _24HourTime = args
        val _24HourSDF = SimpleDateFormat("HH:mm")
        val _12HourSDF = SimpleDateFormat("hh:mm a")
        val _24HourDt: Date = _24HourSDF.parse(_24HourTime)!!
        System.out.println(_24HourDt)
        val result = _12HourSDF.format(_24HourDt)

        return result.uppercase()
    }
}