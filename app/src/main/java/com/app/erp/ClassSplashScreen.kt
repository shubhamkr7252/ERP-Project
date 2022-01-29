package com.app.erp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

@SuppressLint("CustomSplashScreen")
class ClassSplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("appConfig",Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("ONBOARD_DONE",false)){
            val i = Intent(this,ActivityLoadingScreen::class.java)
            startActivity(i)
            finish()
        }
        else{
            val i = Intent(this,OnboardActivity::class.java)
            startActivity(i)
            finish()
        }
    }
}