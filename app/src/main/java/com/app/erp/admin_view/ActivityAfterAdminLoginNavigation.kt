package com.app.erp.admin_view

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.app.erp.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import androidx.navigation.plusAssign
import com.app.erp.FragmentUserPreferences
import com.app.erp.databinding.ActivityAfterAdminLoginNavigationBinding
import com.app.erp.gloabal_functions.KeepStateNavigator


class ActivityAfterAdminLoginNavigation : AppCompatActivity() {
    private val fragmentViewModel : ActivityAfterAdminLoginNavigationViewModel by viewModels()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var binding: ActivityAfterAdminLoginNavigationBinding

    private var backPressedTime:Long = 0
    lateinit var backToast:Toast
    @SuppressLint("ShowToast")
    override fun onBackPressed() {
        val fragment: Fragment? = supportFragmentManager.findFragmentByTag(FragmentUserPreferences()::class.java.simpleName)
        when {
            fragment is FragmentUserPreferences -> {
                super.onBackPressed()
            }
            binding.studentListOptionsLayout.isVisible -> {
                binding.studentListOptionsLayout.visibility = View.GONE
                binding.studentListOptionsLayout.startAnimation(AnimationUtils.loadAnimation(this,R.anim.fab_scale_down))
                binding.obstructor.visibility = View.GONE
                binding.obstructor.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_exit_anim))
            }
            else -> {
                backToast = Toast.makeText(this, "Press back again to leave the app.", Toast.LENGTH_LONG)
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    backToast.cancel()
                    super.onBackPressed()
                    return
                } else {
                    backToast.show()
                }
                backPressedTime = System.currentTimeMillis()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAfterAdminLoginNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.fragmentContainerView)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)!!
        val navigator = KeepStateNavigator(this, navHostFragment.childFragmentManager, R.id.fragmentContainerView)
        navController.navigatorProvider += navigator
        navController.setGraph(R.navigation.admin_activity_bottom_navigation)
        binding.bottomNavigationView.setupWithNavController(navController)

        retrieveData()
    }

    internal fun retrieveData() {
        val docId: String = intent.getStringExtra("codeTxt").toString()

        db.collection("AdminInfo").document(docId).get()
            .addOnSuccessListener {
                if(it.exists()){
                    fragmentViewModel.nameData.value = it.data?.getValue("Name").toString()
                    fragmentViewModel.codeData.value = it.data?.getValue("Code").toString()
                    fragmentViewModel.emailData.value = it.data?.getValue("Email").toString()
                    fragmentViewModel.birthdayData.value = it.data?.getValue("Birthday").toString()
                    fragmentViewModel.genderData.value = it.data?.getValue("Gender").toString()
                    fragmentViewModel.phoneData.value = it.data?.getValue("Phone") as Long?
                    fragmentViewModel.ageData.value = it.data?.getValue("Age") as Long?
                }
            }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("STUDENT_OPTION_LIST_STATE",binding.obstructor.isVisible)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if(savedInstanceState.getBoolean("STUDENT_OPTION_LIST_STATE")){
            enterStudentListAnimation()
        }
        else{
            exitStudentListAnimation()
        }
    }

    internal fun enterStudentListAnimation(){
        binding.obstructor.visibility = View.VISIBLE
        binding.obstructor.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_enter_anim))
        binding.studentListOptionsLayout.visibility = View.VISIBLE
        binding.studentListOptionsLayout.startAnimation(AnimationUtils.loadAnimation(this,R.anim.fab_scale_up))
    }

    internal fun exitStudentListAnimation() {
        binding.studentListOptionsLayout.visibility = View.GONE
        binding.studentListOptionsLayout.startAnimation(AnimationUtils.loadAnimation(this,R.anim.fab_scale_down))
        binding.obstructor.visibility = View.GONE
        binding.obstructor.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_exit_anim))
    }
}