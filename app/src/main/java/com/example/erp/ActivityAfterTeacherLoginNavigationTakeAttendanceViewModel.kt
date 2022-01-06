package com.example.erp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

internal class ActivityAfterTeacherLoginNavigationTakeAttendanceViewModel : ViewModel() {
    val dateData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val timeData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val teacherNameData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val teacherCodeData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val batchDataString: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val semesterDataString: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val courseNameData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val courseCodeData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
}