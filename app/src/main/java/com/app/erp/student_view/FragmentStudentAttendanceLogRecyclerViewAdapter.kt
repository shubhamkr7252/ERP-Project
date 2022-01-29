package com.app.erp.student_view

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.app.erp.R
import com.app.erp.databinding.RecyclerViewStudentAttendanceLogListItemBinding
import com.app.erp.gloabal_functions.getColorFromAttribute
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FragmentStudentAttendanceLogRecyclerViewAdapter(private val studentAttendanceListRecyclerView: ArrayList<FragmentStudentAttendanceLogRecyclerViewDataClass>, val context: Context, private val srn: String):
    RecyclerView.Adapter<FragmentStudentAttendanceLogRecyclerViewAdapter.ViewHolderAlt>() {
    private val db = FirebaseFirestore.getInstance()

    inner class ViewHolderAlt(val binding: RecyclerViewStudentAttendanceLogListItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderAlt {
        return (ViewHolderAlt(RecyclerViewStudentAttendanceLogListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)))
    }

    override fun getItemCount(): Int {
        return studentAttendanceListRecyclerView.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolderAlt, position: Int) {
        holder.binding.tvDate.text = studentAttendanceListRecyclerView[position].Date
        holder.binding.tvTime.text = convertedTime(studentAttendanceListRecyclerView[position].Time.toString())
        holder.binding.tvTeacher.text = studentAttendanceListRecyclerView[position].Teacher
        holder.binding.tvCourse.text = studentAttendanceListRecyclerView[position].Course.toString().plus(" - ").plus(studentAttendanceListRecyclerView[position].CourseCode.toString())

        db.collection("Attendance")
            .document(studentAttendanceListRecyclerView[position].Date.toString())
            .collection(studentAttendanceListRecyclerView[position].Time.toString())
            .whereArrayContainsAny("StudentPresent", listOf(srn)).get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    for (doc in it.result!!){
                        holder.binding.attendanceCheckLayout.visibility = View.VISIBLE
                        holder.binding.attendanceCheckTV.setText("Present",null)
                        holder.binding.attendanceCheckLayout.setCardBackgroundColor(
                            getColorFromAttribute(context, R.attr.colorPrimary))
                        holder.binding.attendanceCheckTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24,0,0,0)
                        break
                    }
                }
            }
        db.collection("Attendance")
            .document(studentAttendanceListRecyclerView[position].Date.toString())
            .collection(studentAttendanceListRecyclerView[position].Time.toString())
            .whereArrayContainsAny("StudentAbsent", listOf(srn)).get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    for (doc in it.result!!){
                        holder.binding.attendanceCheckLayout.visibility = View.VISIBLE
                        holder.binding.attendanceCheckTV.setText("Absent",null)
                        holder.binding.attendanceCheckLayout.setCardBackgroundColor(
                            getColorFromAttribute(context, R.attr.colorError))
                        holder.binding.attendanceCheckTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_outline_close_24,0,0,0)
                        break
                    }
                }
            }
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


