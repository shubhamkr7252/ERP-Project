package com.app.erp.teacher_view

import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

internal class ActivityAfterTeacherLoginNavigationViewModel(val savedStateHandle: SavedStateHandle) :ViewModel() {
    val batchData: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>()
    }
    val courseData: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>()
    }
    val courseNameData: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>()
    }
    val teacherNameData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val teacherCodeData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val teacherEmailData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val teacherPhoneData: MutableLiveData<Long> by lazy {
        MutableLiveData<Long>()
    }
    val teacherGenderData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val teacherAgeData: MutableLiveData<Long> by lazy {
        MutableLiveData<Long>()
    }
    val teacherBirthdayData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private var attendanceAdded: Boolean = false
    internal fun setAttendanceAdded (newState: Boolean){
        attendanceAdded = newState
    }
    internal fun getAttendanceAdded(): Boolean {
        return attendanceAdded
    }

    private lateinit var teacherAttendanceData: MutableList<FragmentTeacherAttendanceLogRecyclerViewDataClass>
    internal fun setTeacherAttendanceData (newData: MutableList<FragmentTeacherAttendanceLogRecyclerViewDataClass>){
        teacherAttendanceData = newData
    }
    internal fun getTeacherAttendanceData(): MutableList<FragmentTeacherAttendanceLogRecyclerViewDataClass> {
        return teacherAttendanceData
    }

    private var fabState: Boolean = true
    internal fun setFabState (newState: Boolean){
        fabState = newState
    }
    internal fun getFabState() : Boolean {
        return fabState
    }

    var isQueryExist: Boolean = false
    internal fun setQueryState(newData:Boolean){
        isQueryExist = newData
    }
    internal fun getQueryState(): Boolean{
        return isQueryExist
    }
}