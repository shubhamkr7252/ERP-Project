package com.example.erp

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditProfileActivityViewModel : ViewModel() {
    val imageRefData: MutableLiveData<Bitmap> by lazy {
        MutableLiveData<Bitmap>()
    }
}