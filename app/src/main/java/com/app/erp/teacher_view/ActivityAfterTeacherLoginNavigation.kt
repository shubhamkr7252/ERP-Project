package com.app.erp.teacher_view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.plusAssign
import androidx.navigation.ui.setupWithNavController
import com.app.erp.FragmentUserPreferences
import com.app.erp.R
import com.app.erp.databinding.ActivityAfterTeacherLoginNavigationBinding
import com.app.erp.gloabal_functions.*
import com.app.erp.gloabal_functions.getColorFromAttribute
import com.app.erp.gloabal_functions.showSnackBar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_after_admin_login_navigation.*

class ActivityAfterTeacherLoginNavigation : AppCompatActivity() {
    private val fragmentViewModel : ActivityAfterTeacherLoginNavigationViewModel by viewModels()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var teacherAttendanceArrayListRecyclerView: ArrayList<FragmentTeacherAttendanceLogRecyclerViewDataClass>
    private lateinit var binding: ActivityAfterTeacherLoginNavigationBinding

    private var backPressedTime:Long = 0
    lateinit var backToast: Toast
    @SuppressLint("ShowToast")
    override fun onBackPressed() {
        val fragmentUserPreferences: Fragment? = supportFragmentManager.findFragmentByTag(FragmentUserPreferences()::class.java.simpleName)
        val fragmentTeacherAttendanceLogSearch: Fragment? = supportFragmentManager.findFragmentByTag(FragmentTeacherAttendanceLogSearch()::class.java.simpleName)

        when {
            fragmentUserPreferences is FragmentUserPreferences -> {
                super.onBackPressed()
            }
            fragmentTeacherAttendanceLogSearch is FragmentTeacherAttendanceLogSearch -> {
                super.onBackPressed()
            }
            else -> {
                backToast = Toast.makeText(this, "Press back again to close the app.", Toast.LENGTH_LONG)
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

    override fun onResume() {
        super.onResume()
        binding.exFabAddAttendance.visibility = View.VISIBLE
    }

    private var launchSomeActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            showSnackBar(this,binding.parentFrameLayout,"Attendance Added")
            fragmentViewModel.setAttendanceAdded(true)
        }
    }

    @SuppressLint("SetTextI18n","UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_after_teacher_login_navigation)
        teacherAttendanceArrayListRecyclerView = arrayListOf<FragmentTeacherAttendanceLogRecyclerViewDataClass>()

        val navController = findNavController(R.id.fragmentContainerView)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)!!
        val navigator = KeepStateNavigator(this, navHostFragment.childFragmentManager, R.id.fragmentContainerView)
        navController.navigatorProvider += navigator
        navController.setGraph(R.navigation.teacher_activity_bottom_navigation)
        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.menu.getItem(1).isEnabled = false

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.fragmentTeacherAttendanceLog -> {
                    binding.bottomNavigationView.menu.getItem(0).setIcon(R.drawable.ic_baseline_view_list_24)
                    binding.bottomNavigationView.menu.getItem(2).setIcon(R.drawable.ic_outline_account_circle_24)
                }
                R.id.fragmentTeacherProfile -> {
                    binding.bottomNavigationView.menu.getItem(0).setIcon(R.drawable.ic_outline_view_list_24)
                    binding.bottomNavigationView.menu.getItem(2).setIcon(R.drawable.ic_baseline_account_circle_24)
                }
            }
        }

        binding.exFabAddAttendance.setOnClickListener {
            binding.exFabAddAttendance.startAnimation(AnimationUtils.loadAnimation(this, R.anim.nav_default_exit_anim))
            binding.exFabAddAttendance.visibility = View.GONE
            startAttendanceTakingActivity()
        }

        loadTeacherInfo()
        loadBatchData()

    }

    private fun startAttendanceTakingActivity() {
        val intent = Intent(this, ActivityAfterTeacherLoginNavigationTakeAttendance::class.java)
        intent.putExtra("teacherTxt",fragmentViewModel.teacherNameData.value.toString())
        intent.putExtra("teacherCodeTxt",fragmentViewModel.teacherCodeData.value.toString())
        intent.putExtra("batchTxt", fragmentViewModel.batchData.value)
        intent.putExtra("courseTxt",fragmentViewModel.courseNameData.value)
        intent.putExtra("courseCodeTxt",fragmentViewModel.courseData.value)
        this.launchSomeActivity.launch(intent)
    }

    internal fun loadTeacherInfo(){
        val docID = intent.getStringExtra("codeTxt").toString()
        fragmentViewModel.teacherCodeData.value = docID

        db.collection("TeacherInfo").document(docID).get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    fragmentViewModel.teacherNameData.value = it.result.data?.getValue("Name").toString()
                    fragmentViewModel.teacherEmailData.value = it.result.data?.getValue("Email").toString()
                    fragmentViewModel.teacherPhoneData.value = it.result.data?.getValue("Phone") as Long?
                    fragmentViewModel.teacherGenderData.value = it.result.data?.getValue("Gender").toString()
                    fragmentViewModel.teacherAgeData.value = it.result.data?.getValue("Age") as Long?
                    fragmentViewModel.teacherBirthdayData.value = it.result.data?.getValue("Birthday").toString()
                    val temp = ArrayList<String>()
                    val tempSubName = ArrayList<String>()
                    temp.addAll(it.result.data?.getValue("Course") as Collection<String>)
                    fragmentViewModel.courseData.postValue(temp)
                    for(i in temp){
                        db.collection("CourseInfo").document(i).get()
                            .addOnCompleteListener { itAlt ->
                                if(itAlt.isSuccessful){
                                    tempSubName.add(itAlt.result.data?.getValue("CourseName").toString())
                                }
                                fragmentViewModel.courseNameData.postValue(tempSubName)
                            }
                    }
                }
            }
    }

    private fun loadBatchData(){
        db.collection("BatchInfo").get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    val temp = ArrayList<String>()
                    for(document in it.result!!) {
                        temp.add(document.data.getValue("Name").toString())
                        fragmentViewModel.batchData.postValue(temp)
                    }
                }
            }
    }
}