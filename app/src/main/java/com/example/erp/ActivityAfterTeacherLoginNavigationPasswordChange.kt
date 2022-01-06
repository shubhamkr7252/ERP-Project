package com.example.erp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class ActivityAfterTeacherLoginNavigationPasswordChange : AppCompatActivity() {
    private lateinit var backButton: MaterialButton
    private lateinit var oldPassword: EditText
    private lateinit var newPassword: EditText
    private lateinit var newPasswordCnf: EditText
    private lateinit var saveBut: MaterialButton
    private lateinit var parentFrameLayout: FrameLayout

    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_after_teacher_login_navigation_password_change)

        backButton = findViewById(R.id.backButton)
        oldPassword = findViewById(R.id.oldPassword)
        newPassword = findViewById(R.id.newPassword)
        newPasswordCnf = findViewById(R.id.newPasswordCnf)
        saveBut = findViewById(R.id.saveBut)
        parentFrameLayout = findViewById(R.id.parentFrameLayout)

        backButton.setOnClickListener {
            onBackPressed()
        }

        saveBut.setOnClickListener {
            checkInput()
        }
    }

    private fun checkInput() {
        if(oldPassword.text.toString().isEmpty()) {
            oldPassword.setError("Please input old password",null)
            oldPassword.requestFocus()
            return
        }
        if(newPassword.text.toString().isEmpty()) {
            newPassword.setError("Please input new password",null)
            newPassword.requestFocus()
            return
        }
        if(newPassword.text.toString() == oldPassword.text.toString()){
            newPassword.setError("Old and New passwords cannot be same",null)
            newPassword.requestFocus()
            return
        }
        if(newPasswordCnf.text.toString() == oldPassword.text.toString()){
            newPasswordCnf.setError("Old and New passwords cannot be same",null)
            newPasswordCnf.requestFocus()
            return
        }
        if(newPasswordCnf.text.toString().isEmpty()) {
            newPasswordCnf.setError("Please confirm new password",null)
            newPasswordCnf.requestFocus()
            return
        }
        if(newPassword.text.toString() != newPasswordCnf.text.toString()) {
            newPasswordCnf.setError("The passwords doesn't match",null)
            newPasswordCnf.requestFocus()
            return
        }

        changePassword()
    }

    private fun changePassword() {
        db.collection("TeacherLoginInfo").document(intent.getStringExtra("teacherCodeTxt").toString()).get()
            .addOnCompleteListener { itAlt ->
                if(itAlt.isSuccessful){
                    val temp = itAlt.result.data?.getValue("Password").toString()
                    if(temp == oldPassword.text.toString()){
                        val updates = hashMapOf<String, Any>(
                            "Password" to newPassword.text.toString().trim{it <= ' '}
                        )
                        db.collection("StudentLoginInfo").document(intent.getStringExtra("srnTxt").toString()).update(updates)
                            .addOnSuccessListener {
                                Toast.makeText(this,"Password Changed",Toast.LENGTH_SHORT).show()
                                onBackPressed()
                                finish()
                            }
                    }else{
                        Snackbar.make(parentFrameLayout,"Current Password is incorrect",Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                            .show()
                        oldPassword.requestFocus()
                    }
                }
            }
    }

    @ColorInt
    private fun getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }
}