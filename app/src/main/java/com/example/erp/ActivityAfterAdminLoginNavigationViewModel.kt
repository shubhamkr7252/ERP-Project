package com.example.erp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

internal class ActivityAfterAdminLoginNavigationViewModel:ViewModel() {
    val nameData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val ageData: MutableLiveData<Long> by lazy {
        MutableLiveData<Long>()
    }
    val phoneData: MutableLiveData<Long> by lazy {
        MutableLiveData<Long>()
    }
    val codeData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val birthdayData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val genderData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val emailData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
}