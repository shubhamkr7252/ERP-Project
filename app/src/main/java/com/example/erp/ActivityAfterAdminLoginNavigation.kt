package com.example.erp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.erp.databinding.ActivityAfterAdminLoginNavigationBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.firebase.firestore.FirebaseFirestore

class ActivityAfterAdminLoginNavigation : AppCompatActivity() {
    private val fragmentViewModel : ActivityAfterAdminLoginNavigationViewModel by viewModels()
    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_after_admin_login_navigation)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navController = findNavController(R.id.fragmentContainerView2)
        bottomNavigationView.setupWithNavController(navController)

        retrieveData()
    }

    private fun retrieveData() {
        val docId: String = intent.getStringExtra("mailTxt").toString()
        db.collection("AdminInfo").document(docId).get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    fragmentViewModel.nameData.value = it.result.data?.getValue("Name").toString()
                    fragmentViewModel.codeData.value = it.result.data?.getValue("Code").toString()
                    fragmentViewModel.emailData.value = it.result.data?.getValue("Email").toString()
                    fragmentViewModel.birthdayData.value = it.result.data?.getValue("Birthday").toString()
                    fragmentViewModel.genderData.value = it.result.data?.getValue("Gender").toString()
                    fragmentViewModel.phoneData.value = it.result.data?.getValue("Phone") as Long?
                    fragmentViewModel.ageData.value = it.result.data?.getValue("Age") as Long?
                }
            }
    }

}