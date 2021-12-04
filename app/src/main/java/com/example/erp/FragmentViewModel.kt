package com.example.erp

import android.content.ClipData
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FragmentViewModel : ViewModel() {
//    lateinit var imageDataRef : Uri
    val nameData = MutableLiveData<String>()
    val srnData = MutableLiveData<String>()
    val emailData = MutableLiveData<String>()
    val phoneData = MutableLiveData<String>()
    val semesterData = MutableLiveData<String>()
    val courseData = MutableLiveData<String>()
    val birthData = MutableLiveData<String>()
    val genderData = MutableLiveData<String>()
    val countryCodeData = MutableLiveData<String>()
//    val imageData = MutableLiveData<Uri>()

    fun setName (newData : String) {
        nameData.value = newData
    }
    fun setSRN (newData : String) {
        srnData.value = newData
    }
    fun setEmail (newData : String) {
        emailData.value = newData
    }
    fun setPhone (newData : String) {
        phoneData.value = newData
    }
    fun setSemester (newData : String) {
        semesterData.value = newData
    }
    fun setCourse (newData : String) {
        courseData.value = newData
    }
    fun setBirthday (newData: String) {
        birthData.value = newData
    }
    fun setGender (newData: String){
        genderData.value = newData
    }
    fun setCountryCode (newData: String){
        countryCodeData.value = newData
    }
//    fun setImage (newData: Uri) {
//        imageData.value = newData
//        imageDataRef = newData
//    }
}