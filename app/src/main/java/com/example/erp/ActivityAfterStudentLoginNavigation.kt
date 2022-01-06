package com.example.erp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import android.view.WindowManager
import android.widget.Toast


class ActivityAfterStudentLoginNavigation : AppCompatActivity() {
    private val fragmentViewModel : ActivityAfterStudentLoginNavigationViewModel by viewModels()

    private lateinit var activityAfterLoginLayout: RelativeLayout
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_after_student_login_navigation)

        val db = Firebase.firestore

        val settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        db.firestoreSettings = settings

        retrieveData()

//        if(savedInstanceState != null){
//            if(savedInstanceState.getString("progressBarShow") == "true"){
//                progressDialog.show()
//            }else {
//                progressDialog.dismiss()
//            }
//        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navController = findNavController(R.id.fragment)
        bottomNavigationView.setupWithNavController(navController)

    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        activityAfterLoginLayout = findViewById(R.id.activityAfterLoginLayout)
//        outState.putString("progressBarShow",progressDialog.isShowing.toString())
//        outState.putIntArray(
//            "ARTICLE_SCROLL_POSITION",
//            intArrayOf(activityAfterLoginLayout.scrollX, activityAfterLoginLayout.scrollY)
//        )
//    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        activityAfterLoginLayout = findViewById(R.id.activityAfterLoginLayout)
        val position = savedInstanceState.getIntArray("ARTICLE_SCROLL_POSITION")
        if (position != null) activityAfterLoginLayout.post { activityAfterLoginLayout.scrollTo(position[0], position[1]) }
    }

    internal fun retrieveData() {
        val docID = intent.getStringExtra("srnText")

        db.collection("StudentInfo").get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    for(document in it.result!!) {
                        if(document.id == docID){
                            fragmentViewModel.nameData.value = document.data.getValue("Name").toString()
                            fragmentViewModel.srnData.value = document.data.getValue("SRN").toString()
                            fragmentViewModel.srnDataAlt.value = document.data.getValue("SRN").toString()
                            db.collection("StudentInfo").document(document.data.getValue("SRN").toString()).get().addOnCompleteListener { itAlt ->
                                if(it.isSuccessful){
                                    fragmentViewModel.totalClassData.value = itAlt.result?.data?.getValue("TotalClasses") as Long?
                                    fragmentViewModel.attendedClassData.value = itAlt.result?.data?.getValue("AttendedClasses") as Long?
                                    fragmentViewModel.nonAttendedClassData.value = itAlt.result?.data?.getValue("NonAttendedClasses") as Long?
                                }
                            }
                            fragmentViewModel.emailData.value = document.data.getValue("Email").toString()
                            fragmentViewModel.phoneData.value = document.data.getValue("Phone") as Long?
                            fragmentViewModel.batchData.value = document.data.getValue("Batch").toString()
                            fragmentViewModel.semesterData.value = document.data.getValue("Semester").toString()
                            fragmentViewModel.birthData.value = document.data.getValue("Birthday").toString()
                            fragmentViewModel.genderData.value = document.data.getValue("Gender").toString()
                            fragmentViewModel.ageData.value = document.data.getValue("Age") as Long?
                            break
                        }
                    }

                }
            }
//        progressDialog.dismiss()
    }
}
