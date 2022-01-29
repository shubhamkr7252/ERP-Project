package com.app.erp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.*
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.app.erp.admin_view.ActivityAfterAdminLoginNavigation
import com.app.erp.databinding.ActivityLoadingScreenBinding
import com.app.erp.gloabal_functions.getColorFromAttribute
import com.app.erp.gloabal_functions.hideSystemUI
import com.app.erp.gloabal_functions.loadLoginSecurityData
import com.app.erp.gloabal_functions.showToast
import com.app.erp.student_view.ActivityAfterStudentLoginNavigation
import com.app.erp.teacher_view.ActivityAfterTeacherLoginNavigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.*
import java.util.concurrent.Executor
import kotlin.collections.ArrayList

@SuppressLint("CustomSplashScreen")
class ActivityLoadingScreen : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoadingScreenBinding

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: androidx.biometric.BiometricPrompt
    private lateinit var promptInfo: androidx.biometric.BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
        }

        getCollectionExistInfo()

        val currentUserEmail: String = auth.currentUser?.email.toString()
        val fingerprintErrorCodes: ArrayList<Int> = ArrayList(listOf(11,12,1,14))
        val fingerprintAvailability: Int = BiometricManager.from(this).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)

        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = androidx.biometric.BiometricPrompt(this@ActivityLoadingScreen, executor, object: androidx.biometric.BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == 13 || errorCode == 10 || errorCode == 7){
                    finish()
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                showToast(this@ActivityLoadingScreen, "Authentication Failed")
            }

            override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                showToast(this@ActivityLoadingScreen, "Authentication Successful")
                reload(currentUserEmail)
            }
        })

        promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock ERP")
            .setSubtitle("Login using fingerprint")
            .setNegativeButtonText("Cancel")
            .build()

        if (auth.currentUser != null){
            if(loadLoginSecurityData(this)){
                biometricPrompt.authenticate(promptInfo)
            }else{
                reload(currentUserEmail)
            }

        }
        else{
            val i = Intent(this, com.app.erp.Activity4Login::class.java)
            startActivity(i)
            finish()
            overridePendingTransition(0, R.anim.nav_default_pop_exit_anim)
        }
    }

    private fun reload(mailTxt: String){
        db.collection("AdminLoginInfo").document(mailTxt).get().addOnCompleteListener { taskAdmin ->
            if(taskAdmin.isSuccessful){
                if(taskAdmin.result.data?.getValue("Email").toString() == mailTxt){
                    val codeTxt: String = taskAdmin.result.data?.getValue("Code").toString()
                    val intent = Intent(this, ActivityAfterAdminLoginNavigation::class.java)
                    intent.putExtra("codeTxt",codeTxt)
                    startActivity(intent)
                    finish()
                    overridePendingTransition(0, R.anim.nav_default_pop_exit_anim)
                }
                else{
                    db.collection("StudentLoginInfo").document(mailTxt).get().addOnCompleteListener { taskStudent ->
                        if(taskStudent.isSuccessful){
                            if(taskStudent.result.data?.getValue("Email").toString() == mailTxt){
                                val srnTxt: String = taskStudent.result.data?.getValue("SRN").toString()
                                val intent = Intent(this,
                                    ActivityAfterStudentLoginNavigation::class.java)
                                intent.putExtra("srnTxt",srnTxt)
                                startActivity(intent)
                                finish()
                                overridePendingTransition(0, R.anim.nav_default_pop_exit_anim)
                            }
                            else{
                                db.collection("TeacherLoginInfo").document(mailTxt).get().addOnCompleteListener { taskTeacher ->
                                    if (taskTeacher.isSuccessful){
                                        if(taskTeacher.result.data?.getValue("Email").toString() == mailTxt){
                                            val codeTxt: String = taskTeacher.result.data?.getValue("Code").toString()
                                            val intent = Intent(this,
                                                ActivityAfterTeacherLoginNavigation::class.java)
                                            intent.putExtra("codeTxt",codeTxt)
                                            startActivity(intent)
                                            finish()
                                            overridePendingTransition(0, R.anim.nav_default_pop_exit_anim)
                                        }
                                        else{
                                            auth.signOut()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getCollectionExistInfo() {
        db.collection("Attendance").get()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    for(doc in it.result!!){
                        val date = doc.data.getValue("Date").toString()
                        val tempAlt = ArrayList<String>()
                        tempAlt.addAll(doc.data.getValue("Time") as Collection<String>)
                        if(tempAlt.size == 0){
                            db.collection("Attendance").document(date).delete()
                        }
                        else{
                            for (i in tempAlt){
                                db.collection("Attendance").document(date).collection(i).get()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            if (task.result.size() > 0) {
                                                //TO CHECK FOR ATTENDANCE COLLECTION EXISTENCE
                                            } else {
                                                val updates = hashMapOf<String, Any>(
                                                    "Time" to FieldValue.arrayRemove(i)
                                                )
                                                db.collection("Attendance").document(date).update(updates).addOnSuccessListener {
                                                    db.collection("Attendance").document(date).get()
                                                        .addOnCompleteListener { itAlt ->
                                                            if(itAlt.isSuccessful){
                                                                val temp = ArrayList<String>()
                                                                temp.addAll(itAlt.result.data?.getValue("Time") as Collection<String>)
                                                                if(temp.size == 0){
                                                                    db.collection("Attendance").document(date).delete()
                                                                }
                                                            }
                                                        }
                                                }
                                            }
                                        } else {
                                            Toast.makeText(this,"Error ${task.exception}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        }
                    }
                }
            }
    }
}