package com.example.erp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

internal class ActivityAfterTeacherLoginNavigationViewModel :ViewModel() {
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
}