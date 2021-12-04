package com.example.erp

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditProfileActivityViewModel : ViewModel() {
    val imageRefData: MutableLiveData<Uri> by lazy {
        MutableLiveData<Uri>()
    }
    val permanentImageRefData: MutableLiveData<Uri> by lazy {
        MutableLiveData<Uri>()
    }
    val nameRefData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val srnRefData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val phoneRefData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val birthRefData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val semesterRefData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val genderRefData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val courseRefData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val countryCodeRefData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
}