package com.app.erp.teacher_view

import androidx.recyclerview.widget.DiffUtil

class FragmentTeacherAttendanceLogDiffUtilAdapter(
    private val oldTeacherAttendanceList: ArrayList<FragmentTeacherAttendanceLogRecyclerViewDataClass>,
    private val newTeacherAttendanceList: ArrayList<FragmentTeacherAttendanceLogRecyclerViewDataClass>
): DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldTeacherAttendanceList.count()
    }

    override fun getNewListSize(): Int {
        return newTeacherAttendanceList.count()
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldTeacherAttendanceList[oldItemPosition].Course == newTeacherAttendanceList[newItemPosition].Course
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when{
            oldTeacherAttendanceList[oldItemPosition].Date != newTeacherAttendanceList[newItemPosition].Date -> {
                false
            }
            oldTeacherAttendanceList[oldItemPosition].Time != newTeacherAttendanceList[newItemPosition].Time -> {
                false
            }
            oldTeacherAttendanceList[oldItemPosition].Course != newTeacherAttendanceList[newItemPosition].Course -> {
                false
            }
            oldTeacherAttendanceList[oldItemPosition].CourseCode != newTeacherAttendanceList[newItemPosition].CourseCode -> {
                false
            }
            oldTeacherAttendanceList[oldItemPosition].Batch != newTeacherAttendanceList[newItemPosition].Batch -> {
                false
            }
            oldTeacherAttendanceList[oldItemPosition].Semester != newTeacherAttendanceList[newItemPosition].Semester -> {
                false
            }
            oldTeacherAttendanceList[oldItemPosition].TotalStrength != newTeacherAttendanceList[newItemPosition].TotalStrength -> {
                false
            }
            oldTeacherAttendanceList[oldItemPosition].TotalAbsent != newTeacherAttendanceList[newItemPosition].TotalAbsent -> {
                false
            }
            oldTeacherAttendanceList[oldItemPosition].TotalPresent != newTeacherAttendanceList[newItemPosition].TotalPresent -> {
                false
            }
            else -> true
        }
    }
}