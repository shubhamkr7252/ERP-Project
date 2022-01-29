package com.app.erp.student_view

import android.content.ClipData
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

internal class ActivityAfterStudentLoginNavigationViewModel(internal val savedStateHandle: SavedStateHandle) : ViewModel() {

    val batchData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val semesterData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val nameData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val emailData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val phoneData: MutableLiveData<Long> by lazy {
        MutableLiveData<Long>()
    }
    val ageData: MutableLiveData<Long> by lazy {
        MutableLiveData<Long>()
    }
    val genderData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val birthData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val srnData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val srnDataAlt: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val totalClassData: MutableLiveData<Long> by lazy {
        MutableLiveData<Long>()
    }
    val attendedClassData: MutableLiveData<Long> by lazy {
        MutableLiveData<Long>()
    }
    val nonAttendedClassData: MutableLiveData<Long> by lazy {
        MutableLiveData<Long>()
    }
    val studentAttendanceArrayListData: MutableLiveData<ArrayList<FragmentStudentAttendanceLogRecyclerViewDataClass>> by lazy {
        MutableLiveData<ArrayList<FragmentStudentAttendanceLogRecyclerViewDataClass>>()
    }
    val recyclerViewPosition: MutableLiveData<SavedStateHandle> by lazy {
        MutableLiveData<SavedStateHandle>()
    }
}