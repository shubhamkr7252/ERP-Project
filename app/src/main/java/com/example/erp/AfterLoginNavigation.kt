package com.example.erp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
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
import android.app.ProgressDialog
import android.view.WindowManager
import androidx.core.view.isVisible


class AfterLoginNavigation : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val fragmentViewModel : FragmentViewModel by viewModels()

    private lateinit var activityAfterLoginLayout: RelativeLayout
    private lateinit var loadingDataLayout: ConstraintLayout
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_after_login_navigation)

        loadingDataLayout = findViewById(R.id.loadingDataLayout)

        auth = Firebase.auth
        val db = Firebase.firestore

        val settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        db.firestoreSettings = settings

        if(savedInstanceState == null){
            blockInput()
            loadingDataLayout.visibility = View.VISIBLE
            retrieveData()
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navController = findNavController(R.id.fragment)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.dashboardFragment,R.id.profileFragment))
        bottomNavigationView.setupWithNavController(navController)
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

    fun AppCompatActivity.blockInput() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    fun AppCompatActivity.unblockInput() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

//    internal fun retrieveImage() {
//
//        val authMail = auth.currentUser?.email.toString()
//        val imageRefUrl = FirebaseStorage.getInstance().reference.child("profileImages/$authMail.jpg").downloadUrl.addOnSuccessListener {
//            val imageRef = FirebaseStorage.getInstance().reference.child("profileImages/$authMail.jpg")
//            val localFile = File.createTempFile("tempImage","jpg")
//            imageRef.getFile(localFile).addOnSuccessListener {
//                val uri = Uri.fromFile(localFile)
//                fragmentViewModel.setImage(uri)
//
//            }
//        }
//    }

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

        loadingDataLayout = findViewById(R.id.loadingDataLayout)
        val authMail = auth.currentUser?.email.toString()

        db.collection("studentInfo").get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    for(document in it.result!!) {

                        if(document.id == authMail){
                            fragmentViewModel.setName(nameTxt.append(document.data.getValue("Name")).toString())
                            fragmentViewModel.setSRN(srnTxt.append(document.data.getValue("SRN")).toString().uppercase())
                            fragmentViewModel.setEmail(mailTxt.append(document.data.getValue("Email")).toString())
                            countryCodeText.append(document.data.getValue("Country Code")).toString()
                            val tempCountryCode = countryCodeText.split(" ")
                            phoneTxt.append(tempCountryCode[0])
                            phoneTxt.append(" ")
                            fragmentViewModel.setCountryCode(document.data.getValue("Country Code").toString())
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
        unblockInput()
//                Toast.makeText(this,"Imaged loaded",Toast.LENGTH_SHORT).show()
        loadingDataLayout.visibility = View.GONE
    }
}
