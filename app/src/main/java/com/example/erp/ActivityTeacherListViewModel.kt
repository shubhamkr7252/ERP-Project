package com.example.erp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

internal class ActivityTeacherListViewModel: ViewModel() {
    val courseData: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>()
    }
    val teacherData: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>()
    }

    private val db = FirebaseFirestore.getInstance()

    fun capitalizeWords(capString: String): String? {
        val capBuffer = StringBuffer()
        val capMatcher: Matcher =
            Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString)
        while (capMatcher.find()) {
            capMatcher.appendReplacement(
                capBuffer,
                capMatcher.group(1).uppercase(Locale.getDefault()) + capMatcher.group(2).lowercase(
                    Locale.getDefault())
            )
        }
        return capMatcher.appendTail(capBuffer).toString()
    }

}