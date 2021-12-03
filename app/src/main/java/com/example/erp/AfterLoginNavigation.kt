package com.example.erp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.squareup.picasso.Picasso
import kotlinx.coroutines.awaitAll
import java.io.File
import com.google.firebase.storage.StorageReference as StorageRef

class AfterLoginNavigation : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val fragmentViewModel : FragmentViewModel by viewModels()

    private lateinit var activityAfterLoginLayout : RelativeLayout
    private lateinit var linearProgressIndicator : LinearProgressIndicator
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_after_login_navigation)

        linearProgressIndicator = findViewById(R.id.linearProgressIndicator)
        activityAfterLoginLayout = findViewById(R.id.activityAfterLoginLayout)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navController = findNavController(R.id.fragment)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.dashboardFragment,R.id.profileFragment))
        bottomNavigationView.setupWithNavController(navController)

        auth = Firebase.auth
        val db = Firebase.firestore

        val settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        db.firestoreSettings = settings

        linearProgressIndicator.visibility = View.VISIBLE

        fun onStart() {
            if(auth.currentUser != null) {
                fun reload() {
                    retrieveData()
                    retrieveImage()
                }
                reload()
            }
            else {
                startActivity(Intent(this, Activity4Login::class.java))
                finish()
            }
            super.onStart()
        }
        onStart()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        activityAfterLoginLayout = findViewById(R.id.activityAfterLoginLayout)
        outState.putIntArray(
            "ARTICLE_SCROLL_POSITION",
            intArrayOf(activityAfterLoginLayout.scrollX, activityAfterLoginLayout.scrollY)
        )
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        activityAfterLoginLayout = findViewById(R.id.activityAfterLoginLayout)
        val position = savedInstanceState.getIntArray("ARTICLE_SCROLL_POSITION")
        if (position != null) activityAfterLoginLayout.post { activityAfterLoginLayout.scrollTo(position[0], position[1]) }
    }

    private fun retrieveImage() {
        activityAfterLoginLayout = findViewById(R.id.activityAfterLoginLayout)
        val authMail = auth.currentUser?.email
        val imageRefUrl = FirebaseStorage.getInstance().reference.child("profileImages/$authMail.jpg").downloadUrl.addOnSuccessListener {
            val imageRef = FirebaseStorage.getInstance().reference.child("profileImages/$authMail.jpg")
            val localFile = File.createTempFile("tempImage","jpg")
            imageRef.getFile(localFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                fragmentViewModel.setImage(bitmap)
            }
        }
    }

    internal fun retrieveData() {
        val nameTxt = StringBuffer()
        val srnTxt = StringBuffer()
        val mailTxt = StringBuffer()
        val phoneTxt = StringBuffer()
        val semTxt = StringBuffer()
        val courseTxt = StringBuffer()
        val birthTxt = StringBuffer()
        val genderTxt = StringBuffer()
        val countryCodeText = StringBuffer()

        val authMail = auth.currentUser?.email

        db.collection("studentInfo").get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    for(document in it.result!!) {
                        val temp = document.id
                        if(document.id == authMail){
                            fragmentViewModel.setName(nameTxt.append(document.data.getValue("Name")).toString())
                            fragmentViewModel.setSRN(srnTxt.append(document.data.getValue("SRN")).toString())
                            fragmentViewModel.setEmail(mailTxt.append(document.data.getValue("Email")).toString())
                            countryCodeText.append(document.data.getValue("Country Code")).toString()
                            val tempCountryCode = countryCodeText.split(" ")
                            phoneTxt.append(tempCountryCode[0])
                            phoneTxt.append(" ")
                            fragmentViewModel.setPhone(phoneTxt.append(document.data.getValue("Phone")).toString())
                            fragmentViewModel.setCourse(courseTxt.append(document.data.getValue("Course")).toString())
                            fragmentViewModel.setSemester(semTxt.append(document.data.getValue("Semester")).toString())
                            fragmentViewModel.setBirthday(birthTxt.append(document.data.getValue("Birthday")).toString())
                            genderTxt.append(document.data.getValue("Gender")).toString()
                            genderTxt.append(", ")
                            fragmentViewModel.setGender(genderTxt.append(document.data.getValue("Age")).toString())
                            break
                        }
                    }

                }
            }
        linearProgressIndicator.visibility = View.GONE
    }
}
