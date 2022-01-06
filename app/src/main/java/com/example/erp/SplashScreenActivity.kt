package com.example.erp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var revaUni: ConstraintLayout
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        revaUni = findViewById(R.id.revaUni)
        window.navigationBarColor = getColorFromAttr(R.attr.colorSurface)

        hideSystemUI()
        getCollectionExistInfo()

        revaUni.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_enter_anim))
        Handler(Looper.getMainLooper()).postDelayed({
            val i = Intent(this, Activity4Login::class.java)
            startActivity(i)
            finish()
            overridePendingTransition(0, R.anim.nav_default_pop_exit_anim)
        },2000)
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

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, revaUni).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.isAppearanceLightNavigationBars = true
        }
    }

    @ColorInt
    fun Context.getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }

}