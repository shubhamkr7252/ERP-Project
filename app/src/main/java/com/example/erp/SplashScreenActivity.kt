package com.example.erp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        auth = Firebase.auth

        val revaUni = findViewById<ImageView>(R.id.revaUni)
        revaUni.alpha = 0f

        revaUni.animate().setDuration(2000).alpha(1f).withEndAction {
            val currentUser = auth.currentUser
            if(currentUser != null){
                val i = Intent(this, AfterLoginNavigation::class.java)
                startActivity(i)
            }
            else {
                val i = Intent(this, Activity4Login::class.java)
                startActivity(i)
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
}