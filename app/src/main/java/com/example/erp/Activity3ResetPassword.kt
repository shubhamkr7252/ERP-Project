package com.example.erp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.core.widget.doBeforeTextChanged
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Activity3ResetPassword : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailLayout: TextInputLayout
    private lateinit var loginText: MaterialButton
    private lateinit var resetBut: MaterialButton
    private lateinit var mail: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity3_reset_password)

        loginText = findViewById(R.id.loginText)
        loginText.setOnClickListener {
            onBackPressed()
        }

        auth = Firebase.auth

        resetBut = findViewById(R.id.resetBut)
        mail = findViewById(R.id.mail)
        emailLayout = findViewById(R.id.emailTextInputLayout)

        fun forgetPass() {
            if(!Patterns.EMAIL_ADDRESS.matcher(mail.text.toString()).matches()) {
                if(mail.text.toString().isEmpty()) {
                    emailLayout.error = "Please input email"
                    emailLayout.errorIconDrawable = null
                    mail.doBeforeTextChanged { text, start, count, after ->
                        emailLayout.error = null
                        emailLayout.boxStrokeErrorColor = null
                    }
                    //mail.setError("Please input email",null)
                }
                else {
                    emailLayout.error = "Please input a valid email"
                    emailLayout.errorIconDrawable = null
                    mail.doBeforeTextChanged { text, start, count, after ->
                        emailLayout.error = null
                        emailLayout.boxStrokeErrorColor = null
                    }
                    // mail.setError("Please input a valid email",null)
                }
                mail.requestFocus()
                return
            }

            val mailTxt: String = mail.text.toString().trim {it <= ' '}

            auth.sendPasswordResetEmail(mailTxt)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        Toast.makeText(baseContext, "Password reset link successfully sent", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@Activity3ResetPassword,Activity4Login::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@Activity3ResetPassword,task.exception!!.message.toString(),
                            Toast.LENGTH_LONG).show()
                    }
                }
        }

        resetBut.setOnClickListener {
            forgetPass()
        }

    }
}