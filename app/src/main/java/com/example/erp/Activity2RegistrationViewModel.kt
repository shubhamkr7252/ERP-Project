package com.example.erp

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class Activity2RegistrationViewModel : ViewModel(){
    val imageRefData: MutableLiveData<Uri> by lazy {
        MutableLiveData<Uri>()
    }
}