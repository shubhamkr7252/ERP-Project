package com.app.erp.student_view

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.plusAssign
import androidx.navigation.ui.setupWithNavController
import com.app.erp.FragmentUserPreferences
import com.app.erp.R
import com.app.erp.databinding.ActivityAfterStudentLoginNavigationBinding
import com.app.erp.gloabal_functions.KeepStateNavigator
import com.google.firebase.firestore.*

class ActivityAfterStudentLoginNavigation : AppCompatActivity() {
    private val fragmentViewModel : ActivityAfterStudentLoginNavigationViewModel by viewModels()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var binding: ActivityAfterStudentLoginNavigationBinding
    private lateinit var studentAttendanceArrayListRecyclerView: ArrayList<FragmentStudentAttendanceLogRecyclerViewDataClass>

    private var backPressedTime:Long = 0
    lateinit var backToast:Toast
    @SuppressLint("ShowToast")
    override fun onBackPressed() {
        val fragment: Fragment? = supportFragmentManager.findFragmentByTag(FragmentUserPreferences()::class.java.simpleName)
        if(fragment is FragmentUserPreferences){
            super.onBackPressed()
        }
        else{
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_after_student_login_navigation)

        studentAttendanceArrayListRecyclerView = arrayListOf<FragmentStudentAttendanceLogRecyclerViewDataClass>()

        val navController = findNavController(R.id.fragmentContainerView)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)!!
        val navigator = KeepStateNavigator(this, navHostFragment.childFragmentManager, R.id.fragmentContainerView)
        navController.navigatorProvider += navigator
        navController.setGraph(R.navigation.student_activity_bottom_navigation)
        binding.bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.fragmentStudentAttendanceLog -> {
                    binding.bottomNavigationView.menu.getItem(0).setIcon(R.drawable.ic_baseline_view_list_24)
                    binding.bottomNavigationView.menu.getItem(1).setIcon(R.drawable.ic_outline_account_circle_24)
                }
                R.id.fragmentStudentProfile -> {
                    binding.bottomNavigationView.menu.getItem(0).setIcon(R.drawable.ic_outline_view_list_24)
                    binding.bottomNavigationView.menu.getItem(1).setIcon(R.drawable.ic_baseline_account_circle_24)
                }
            }
        }

        retrieveData()
        getAttendanceData()

    }

    internal fun retrieveData() {
        val docID = intent.getStringExtra("srnTxt").toString()

        db.collection("StudentInfo").document(docID).get()
            .addOnSuccessListener {
                if(it.exists()) {
                    fragmentViewModel.nameData.value = it.data?.getValue("Name").toString()
                    fragmentViewModel.srnData.value = it.data?.getValue("SRN").toString()
                    fragmentViewModel.srnDataAlt.value = it.data?.getValue("SRN").toString()
                    fragmentViewModel.totalClassData.value = it.data?.getValue("TotalClasses") as Long?
                    fragmentViewModel.attendedClassData.value = it.data?.getValue("AttendedClasses") as Long?
                    fragmentViewModel.nonAttendedClassData.value = it.data?.getValue("NonAttendedClasses") as Long?
                    fragmentViewModel.emailData.value = it.data?.getValue("Email").toString()
                    fragmentViewModel.phoneData.value = it.data?.getValue("Phone") as Long?
                    fragmentViewModel.batchData.value = it.data?.getValue("Batch").toString()
                    fragmentViewModel.semesterData.value = it.data?.getValue("Semester").toString()
                    fragmentViewModel.birthData.value = it.data?.getValue("Birthday").toString()
                    fragmentViewModel.genderData.value = it.data?.getValue("Gender").toString()
                    fragmentViewModel.ageData.value = it.data?.getValue("Age") as Long?

                }
            }
    }

    internal fun getAttendanceData() {
        var temp = true
        val srnData = intent.getStringExtra("srnTxt").toString()
        db.collection("Attendance").get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    var dateTxt: String
                    val tempTxt = ArrayList<String>()
                    for(doc in it.result!!){
                        temp = false
                        tempTxt.clear()
                        dateTxt = doc.data.getValue("Date").toString()
                        tempTxt.addAll(doc.data.getValue("Time") as Collection<String>)
                        for (i in tempTxt){

                            studentAttendanceArrayListRecyclerView.clear()
                            db.collection("Attendance").document(dateTxt).collection(i).whereArrayContainsAny("StudentPresent",
                                listOf(srnData)).addSnapshotListener(object:
                                EventListener<QuerySnapshot> {
                                @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
                                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                                    if(error != null){
                                        Toast.makeText(this@ActivityAfterStudentLoginNavigation,"Firestore Error ${error.message.toString()}",Toast.LENGTH_SHORT).show()
                                        return
                                    }
                                    for(dc : DocumentChange in value?.documentChanges!!){
                                        if(dc.type == DocumentChange.Type.ADDED){
                                            studentAttendanceArrayListRecyclerView.add(dc.document.toObject(
                                                FragmentStudentAttendanceLogRecyclerViewDataClass::class.java))
                                        }
                                    }
                                }

                            })
                            db.collection("Attendance").document(dateTxt).collection(i).whereArrayContainsAny("StudentAbsent",
                                listOf(srnData)).addSnapshotListener(object:
                                EventListener<QuerySnapshot> {
                                @SuppressLint("NotifyDataSetChanged")
                                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                                    if(error != null){
                                        Toast.makeText(this@ActivityAfterStudentLoginNavigation,"Firestore Error ${error.message.toString()}",Toast.LENGTH_LONG).show()
                                        return
                                    }
                                    for(dc : DocumentChange in value?.documentChanges!!){
                                        if(dc.type == DocumentChange.Type.ADDED){
                                            studentAttendanceArrayListRecyclerView.add(dc.document.toObject(
                                                FragmentStudentAttendanceLogRecyclerViewDataClass::class.java))
                                        }
                                    }
                                    fragmentViewModel.studentAttendanceArrayListData.postValue(studentAttendanceArrayListRecyclerView)
                                }
                            })
                        }
                    }
                }
            }
    }
}
