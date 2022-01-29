package com.app.erp.admin_view.student_list_view

import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

internal class ActivityStudentListViewModel : ViewModel() {
    val batchData: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>()
    }
    val studentSrnData: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>()
    }
    val studentName: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val studentGender: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val studentBatch: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val studentSemester: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val studentBirthday: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val studentEmail: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val studentPhone: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val studentPassword: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val adminEmailData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
}