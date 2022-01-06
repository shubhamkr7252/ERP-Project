package com.example.erp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore
import android.content.IntentFilter

import android.content.BroadcastReceiver
import android.content.Context
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView


class ActivityAfterTeacherLoginNavigation : AppCompatActivity() {
    private val fragmentViewModel : ActivityAfterTeacherLoginNavigationViewModel by viewModels()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_after_teacher_login_navigation)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navController = findNavController(R.id.fragmentContainerView)
        bottomNavigation.setupWithNavController(navController)

        loadTeacherInfo()
        loadBatchData()

    }

    internal fun loadTeacherInfo(){
        val docID = intent.getStringExtra("codeTxt").toString()
        fragmentViewModel.teacherCodeData.value = docID

        db.collection("TeacherInfo").document(docID).get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    fragmentViewModel.teacherNameData.value = it.result.data?.getValue("Name").toString()
                    fragmentViewModel.teacherEmailData.value = it.result.data?.getValue("Email").toString()
                    fragmentViewModel.teacherPhoneData.value = it.result.data?.getValue("Phone") as Long?
                    fragmentViewModel.teacherGenderData.value = it.result.data?.getValue("Gender").toString()
                    fragmentViewModel.teacherAgeData.value = it.result.data?.getValue("Age") as Long?
                    fragmentViewModel.teacherBirthdayData.value = it.result.data?.getValue("Birthday").toString()
                    val temp = ArrayList<String>()
                    val tempSubName = ArrayList<String>()
                    temp.addAll(it.result.data?.getValue("Course") as Collection<String>)
                    fragmentViewModel.courseData.postValue(temp)
                    for(i in temp){
                        db.collection("CourseInfo").document(i.toString()).get()
                            .addOnCompleteListener { itAlt ->
                                if(itAlt.isSuccessful){
                                    tempSubName.add(itAlt.result.data?.getValue("CourseName").toString())
                                }
                                fragmentViewModel.courseNameData.postValue(tempSubName)
                            }
                    }
                }
            }
    }

    private fun loadBatchData(){
        db.collection("BatchInfo").get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    val temp = ArrayList<String>()
                    for(document in it.result!!) {
                        temp.add(document.data.getValue("Name").toString())
                        fragmentViewModel.batchData.postValue(temp)
                    }
                }
            }
    }
}