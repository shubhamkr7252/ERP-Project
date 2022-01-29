package com.app.erp.teacher_view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.erp.R
import com.app.erp.databinding.RecyclerViewTeacherAttendanceLogListItemBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FragmentTeacherAttendanceLogRecyclerViewAdapter (private var oldTeacherAttendanceList: ArrayList<FragmentTeacherAttendanceLogRecyclerViewDataClass>) :

    RecyclerView.Adapter<FragmentTeacherAttendanceLogRecyclerViewAdapter.ViewHolderAltTeacher>() {

    inner class ViewHolderAltTeacher(val binding: RecyclerViewTeacherAttendanceLogListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderAltTeacher {
        return (ViewHolderAltTeacher(RecyclerViewTeacherAttendanceLogListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)))
    }

    override fun getItemCount(): Int {
        return oldTeacherAttendanceList.count()
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolderAltTeacher, position: Int) {
        holder.binding.tvTime.text = convertedTime(oldTeacherAttendanceList[position].Time.toString())
        holder.binding.tvDate.text = oldTeacherAttendanceList[position].Date
        holder.binding.tvCourse.text = oldTeacherAttendanceList[position].Course.toString().plus(" - ").plus(oldTeacherAttendanceList[position].CourseCode.toString())
        holder.binding.tvBatchSemester.text = oldTeacherAttendanceList[position].Batch.toString().plus(", ").plus(oldTeacherAttendanceList[position].Semester.toString())
        holder.binding.tvStudentStrengthDetails.text = "Students Present: "
            .plus(oldTeacherAttendanceList[position].TotalPresent.toString())
            .plus("\nStudents Absent: ")
            .plus(oldTeacherAttendanceList[position].TotalAbsent.toString())
            .plus("\nTotal Strength: ")
            .plus(oldTeacherAttendanceList[position].TotalStrength.toString())
    }

    @SuppressLint("SimpleDateFormat")
    private fun convertedTime(args: String): String {
        val _24HourTime = args
        val _24HourSDF = SimpleDateFormat("HH:mm")
        val _12HourSDF = SimpleDateFormat("hh:mm a")
        val _24HourDt: Date = _24HourSDF.parse(_24HourTime)!!
        System.out.println(_24HourDt)
        val result = _12HourSDF.format(_24HourDt)

        return result.uppercase()
    }
}