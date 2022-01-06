package com.example.erp

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
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FragmentStudentAttendanceLogRecyclerViewAdapter(private val studentAttendanceListRecyclerView: ArrayList<FragmentStudentAttendanceLogRecyclerViewDataClass>, val context: Context, private val srn: String):
    RecyclerView.Adapter<FragmentStudentAttendanceLogRecyclerViewAdapter.ViewHolderAlt>() {
    private val db = FirebaseFirestore.getInstance()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderAlt {
        return (ViewHolderAlt(
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_student_attendance_log_list_item, parent, false))
                )
    }

    override fun getItemCount(): Int {
        return studentAttendanceListRecyclerView.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolderAlt, position: Int) {
        holder.dateAlt.text = studentAttendanceListRecyclerView[position].Date
        holder.timeAlt.text = convertedTime(studentAttendanceListRecyclerView[position].Time.toString())
        holder.teacherAlt.text = studentAttendanceListRecyclerView[position].Teacher
        holder.courseAlt.text = studentAttendanceListRecyclerView[position].Course.toString().plus(" - ").plus(studentAttendanceListRecyclerView[position].CourseCode.toString())

        db.collection("Attendance")
            .document(studentAttendanceListRecyclerView[position].Date.toString())
            .collection(studentAttendanceListRecyclerView[position].Time.toString())
            .whereArrayContainsAny("StudentPresent", listOf(srn)).get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    for (doc in it.result!!){
                        holder.attendanceCheckLayout.visibility = View.VISIBLE
                        holder.attendanceCheckTV.setText("Present",null)
                        holder.attendanceCheckLayout.setCardBackgroundColor(context.getColorFromAttr(R.attr.colorPrimary))
                        holder.attendanceCheckTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24,0,0,0)
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
                        holder.attendanceCheckLayout.visibility = View.VISIBLE
                        holder.attendanceCheckTV.setText("Absent",null)
                        holder.attendanceCheckLayout.setCardBackgroundColor(context.getColorFromAttr(R.attr.colorError))
                        holder.attendanceCheckTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_outline_close_24,0,0,0)
                        break
                    }
                }
            }
    }
    @ColorInt
    fun Context.getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }

    class ViewHolderAlt(view: View) : RecyclerView.ViewHolder(view){
        val dateAlt: TextView = view.findViewById(R.id.tv_course_date)
        val timeAlt: TextView = view.findViewById(R.id.tv_time)
        val teacherAlt: TextView = view.findViewById(R.id.tv_course_teacher)
        val courseAlt: TextView = view.findViewById(R.id.tv_course)
        val attendanceCheckLayout: MaterialCardView = view.findViewById(R.id.attendanceCheckLayout)
        val attendanceCheckTV: TextView = view.findViewById(R.id.attendanceCheckTV)
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


