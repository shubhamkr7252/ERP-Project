package com.app.erp.teacher_view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.erp.R
import com.app.erp.databinding.RecyclerViewTeacherAttendanceLogListItemBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FragmentTeacherAttendanceLogSearchRecyclerViewAdapter ( 
    private val teacherAttendanceListRecyclerView: ArrayList<FragmentTeacherAttendanceLogRecyclerViewDataClass>,
    val context: Context):

    RecyclerView.Adapter<FragmentTeacherAttendanceLogSearchRecyclerViewAdapter.ViewHolderTeacherAttendanceLogSearch>()
{
    inner class ViewHolderTeacherAttendanceLogSearch(val binding: RecyclerViewTeacherAttendanceLogListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FragmentTeacherAttendanceLogSearchRecyclerViewAdapter.ViewHolderTeacherAttendanceLogSearch {
        return ViewHolderTeacherAttendanceLogSearch(RecyclerViewTeacherAttendanceLogListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: FragmentTeacherAttendanceLogSearchRecyclerViewAdapter.ViewHolderTeacherAttendanceLogSearch,
        position: Int
    ) {
        holder.binding.tvTime.text = convertedTime(teacherAttendanceListRecyclerView[position].Time.toString())
        holder.binding.tvDate.text = teacherAttendanceListRecyclerView[position].Date
        holder.binding.tvCourse.text = teacherAttendanceListRecyclerView[position].Course.toString().plus(" - ").plus(teacherAttendanceListRecyclerView[position].CourseCode.toString())
        holder.binding.tvBatchSemester.text = teacherAttendanceListRecyclerView[position].Batch.toString().plus(", ").plus(teacherAttendanceListRecyclerView[position].Semester.toString())
        holder.binding.tvStudentStrengthDetails.text = "Students Present: "
            .plus(teacherAttendanceListRecyclerView[position].TotalPresent.toString())
            .plus("\nStudents Absent: ")
            .plus(teacherAttendanceListRecyclerView[position].TotalAbsent.toString())
            .plus("\nTotal Strength: ")
            .plus(teacherAttendanceListRecyclerView[position].TotalStrength.toString())
    }

    override fun getItemCount(): Int {
        return teacherAttendanceListRecyclerView.count()
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