package com.app.erp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.app.erp.admin_view.ActivityAfterAdminLoginNavigation
import com.app.erp.databinding.Activity4LoginBinding
import com.app.erp.gloabal_functions.showSnackBar
import com.app.erp.student_view.ActivityAfterStudentLoginNavigation
import com.app.erp.teacher_view.ActivityAfterTeacherLoginNavigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class Activity4Login : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: Activity4LoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = Activity4LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.loginButton.setOnClickListener {
            login()
        }

        binding.forgotPasswordText.setOnClickListener {
            sendEmail()
        }
    }

    private fun login(){
        if(binding.email.text.toString().isEmpty()) {
            binding.email.setError("Please input Teacher code",null)
            binding.email.requestFocus()
            return
        }
        if(binding.password.text.toString().isEmpty()) {
            binding.password.setError("Please input Password",null)
            binding.password.requestFocus()
            return
        }

        binding.progressBarLayout.visibility = View.VISIBLE

        val mailTxt: String = binding.email.text.toString().trim {it <= ' '}.lowercase()
        val passTxt: String = binding.password.text.toString().trim {it <= ' '}

        auth.signInWithEmailAndPassword(mailTxt,passTxt).addOnSuccessListener {
            db.collection("AdminLoginInfo").document(mailTxt).get().addOnSuccessListener { taskAdmin ->
                if(taskAdmin.exists()){
                    if(taskAdmin.data?.getValue("Email").toString() == mailTxt){
                        val codeTxt: String = taskAdmin.data?.getValue("Code").toString()
                        val intent = Intent(this, ActivityAfterAdminLoginNavigation::class.java)
                        intent.putExtra("codeTxt",codeTxt)
                        startActivity(intent)
                        binding.progressBarLayout.visibility = View.GONE
                        finish()
                    }
                }
                else{
                    db.collection("StudentLoginInfo").document(mailTxt).get().addOnSuccessListener { taskStudent ->
                        if(taskStudent.exists()){
                            if(taskStudent.data?.getValue("Email").toString() == mailTxt){
                                val srnTxt: String = taskStudent.data?.getValue("SRN").toString()
                                val intent = Intent(this,
                                    ActivityAfterStudentLoginNavigation::class.java)
                                intent.putExtra("srnTxt",srnTxt)
                                startActivity(intent)
                                binding.progressBarLayout.visibility = View.GONE
                                finish()
                            }
                        }
                        else{
                            db.collection("TeacherLoginInfo").document(mailTxt).get().addOnSuccessListener { taskTeacher ->
                                if (taskTeacher.exists()){
                                    if(taskTeacher.data?.getValue("Email").toString() == mailTxt){
                                        val codeTxt: String = taskTeacher.data?.getValue("Code").toString()
                                        val intent = Intent(this,
                                            ActivityAfterTeacherLoginNavigation::class.java)
                                        intent.putExtra("codeTxt",codeTxt)
                                        startActivity(intent)
                                        binding.progressBarLayout.visibility = View.GONE
                                        finish()
                                    }
                                }
                                else{
                                    binding.progressBarLayout.visibility = View.GONE
                                    showSnackBar(this,binding.parentFrameLayout,"User Information Not Found, Please Contact Admin")
                                    auth.signOut()
                                }
                            }
                        }
                    }
                }
            }
        }.addOnFailureListener {
            binding.progressBarLayout.visibility = View.GONE
            when {
                it.toString().contains("FirebaseNetworkException") -> {
                    showSnackBar(this,binding.parentFrameLayout,"Please check your Internet Connection")
                }
                it.toString().contains("FirebaseAuthInvalidCredentialsException") -> {
                    showSnackBar(this,binding.parentFrameLayout,"Invalid Password")
                }
                it.toString().contains("FirebaseAuthInvalidUserException") -> {
                    showSnackBar(this,binding.parentFrameLayout,"Invalid User")
                }
                else -> {
                    showSnackBar(this,binding.parentFrameLayout,"$it")
                }
            }
        }
    }

    private fun sendEmail() {
        /*ACTION_SEND action to launch an email client installed on your Android device.*/
        val mIntent = Intent(Intent.ACTION_SENDTO)
        /*To send an email you need to specify mailto: as URI using setData() method
        and data type will be to text/plain using setType() method*/
        mIntent.data = Uri.parse("mailto:"+"support@reva.edu.in")
        mIntent.type = "text/plain"
        // put recipient email in intent
        /* recipient is put as array because you may wanna send email to multiple emails
           so enter comma(,) separated emails, it will be stored in array*/
        mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("support@reva.edu.in"))
        //put the Subject in the intent
        mIntent.putExtra(Intent.EXTRA_SUBJECT, "Password Reset")
        //put the message in the intent
        mIntent.putExtra(Intent.EXTRA_TEXT, "I want to reset my password\n\n<Please add additional information below this line (SRN, Name, etc.)>\n\n")


        try {
            //start email intent
            startActivity(Intent.createChooser(mIntent, "Choose Email Client..."))
        }
        catch (e: Exception){
            //if any thing goes wrong for example no email client application or any exception
            //get and show exception message
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }

    }

}