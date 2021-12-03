package com.example.erp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import android.widget.TextView

class Activity4Login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var registerText: TextView
    private lateinit var resetText: TextView
    private lateinit var loginButton: MaterialButton
    private lateinit var mail: EditText
    private lateinit var password: EditText
    private lateinit var loginLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity4_login)

        auth = Firebase.auth

        registerText = findViewById(R.id.signUpBut)
        resetText = findViewById(R.id.forgotPasswordText)
        loginButton = findViewById(R.id.loginBut)

        registerText.setOnClickListener {
            startActivity(Intent(this,Activity2Registration::class.java))
        }

        resetText.setOnClickListener {
            startActivity(Intent(this,Activity3ResetPassword::class.java))
        }

        loginButton.setOnClickListener {
            signIn()
        }

    }

    private fun signIn () {
        loginLayout = findViewById(R.id.activity4LoginLayout)
        mail = findViewById(R.id.mail)
        password = findViewById(R.id.password)

        if(!Patterns.EMAIL_ADDRESS.matcher(mail.text.toString()).matches()) {
            if(mail.text.toString().isEmpty()) {
                mail.setError("Please input email",null)
            }
            else {
                mail.setError("Please input a valid email",null)
            }
            mail.requestFocus()
            return
        }
        if(password.text.toString().isEmpty()) {
            password.setError("Please input password",null)
            password.requestFocus()
            return
        }
        else if(password.text.toString().length < 6) {
            password.setError("The password should not be less than six characters",null)
            password.requestFocus()
            return
        }

        val mailTxt: String = mail.text.toString().trim {it <= ' '}
        val passTxt: String = password.text.toString().trim {it <= ' '}

        auth.signInWithEmailAndPassword(mailTxt,passTxt)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this@Activity4Login,AfterLoginNavigation::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Snackbar.make(loginLayout,task.exception!!.message.toString(),Snackbar.LENGTH_INDEFINITE)
                        .setAction("Dismiss",View.OnClickListener{
                        })
                        .setBackgroundTint(resources.getColor(R.color.lightColorPrimary))
                        .show()
                }
            }
    }
}