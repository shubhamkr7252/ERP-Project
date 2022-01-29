package com.app.erp.admin_view.attendance_list_view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ActivityAttendanceListViewModel: ViewModel() {
//    val data: MutableLiveData<String> by lazy {
//        MutableLiveData<String>()
//    }
    private lateinit var attendanceResumeData: ArrayList<AttendanceListRecyclerViewDataClass>
    internal fun setResumeData(newData: ArrayList<AttendanceListRecyclerViewDataClass>){
        attendanceResumeData = newData
    }
    internal fun getResumeData(): ArrayList<AttendanceListRecyclerViewDataClass>{
        return attendanceResumeData
    }

    var isQueryExist: Boolean = false
    internal fun setQueryState(newData:Boolean){
        isQueryExist = newData
    }
    internal fun getQueryState(): Boolean{
        return isQueryExist
    }
}