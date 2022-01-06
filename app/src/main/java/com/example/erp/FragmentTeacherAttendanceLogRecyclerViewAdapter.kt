package com.example.erp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class FragmentTeacherAttendanceLogRecyclerViewAdapter(
    private val teacherAttendanceListRecyclerView: ArrayList<FragmentTeacherAttendanceLogRecyclerViewDataClass>,
    val context: Context):

    RecyclerView.Adapter<FragmentTeacherAttendanceLogRecyclerViewAdapter.ViewHolderAltTeacher>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderAltTeacher {
        return (ViewHolderAltTeacher(
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_teacher_attendance_log_list_item, parent, false))
                )
    }

    override fun getItemCount(): Int {
        return teacherAttendanceListRecyclerView.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolderAltTeacher, position: Int) {
        holder.timeAlt.text = convertedTime(teacherAttendanceListRecyclerView[position].Time.toString())
        holder.dateAlt.text = teacherAttendanceListRecyclerView[position].Date
        holder.courseAlt.text = teacherAttendanceListRecyclerView[position].Course.toString().plus(" - ").plus(teacherAttendanceListRecyclerView[position].CourseCode.toString())
        holder.tv_batch_semester.text = teacherAttendanceListRecyclerView[position].Batch.toString().plus(", ").plus(teacherAttendanceListRecyclerView[position].Semester.toString())
        holder.tv_student_strength_details.text = "Students Present: "
            .plus(teacherAttendanceListRecyclerView[position].TotalPresent.toString())
            .plus("\nStudents Absent: ")
            .plus(teacherAttendanceListRecyclerView[position].TotalAbsent.toString())
            .plus("\nTotal Strength: ")
            .plus(teacherAttendanceListRecyclerView[position].TotalStrength.toString())
    }

    public class ViewHolderAltTeacher(view: View) : RecyclerView.ViewHolder(view){
        val dateAlt: TextView = view.findViewById(R.id.tv_course_date)
        val timeAlt: TextView = view.findViewById(R.id.tv_time)
        val courseAlt: TextView = view.findViewById(R.id.tv_course)
        val tv_student_strength_details: TextView = view.findViewById(R.id.tv_student_strength_details)
        val tv_batch_semester: TextView = view.findViewById(R.id.tv_batch_semester)
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