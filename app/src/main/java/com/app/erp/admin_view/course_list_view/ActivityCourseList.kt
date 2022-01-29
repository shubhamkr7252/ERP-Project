package com.app.erp.admin_view.course_list_view

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.AttrRes
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.erp.R
import com.app.erp.databinding.ActivityCourseListBinding
import com.app.erp.gloabal_functions.getColorFromAttribute
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import kotlin.collections.ArrayList

class ActivityCourseList : AppCompatActivity() {
    private lateinit var courseListRecyclerViewAdapter: CourseListRecyclerViewAdapter
    private lateinit var courseArrayList: ArrayList<CourseRecyclerViewDataClass>

    private val fragmentViewModel : ActivityCourseListViewModel by viewModels()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var binding: ActivityCourseListBinding

    private lateinit var tempArrayListCourseInfo: ArrayList<String>
    private lateinit var tempArrayListTeacherInfo: ArrayList<String>
    private lateinit var tempArrayListCourseCodeInfo: ArrayList<String>

    private var clicked = false

    override fun onBackPressed() {
        if(binding.obstructor.isVisible){
            binding.editCourseFab.performClick()
        }
        else{
            super.onBackPressed()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        window.statusBarColor = getColorFromAttribute(this,R.attr.colorSurfaceVariant)

        binding.refreshButton.setOnClickListener {
            getCourseData()
            retrieveData()
            Snackbar.make(binding.parentFrameLayout,"Data Refreshed", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColorFromAttribute(this,R.attr.colorPrimary))
                .show()
        }

        binding.addCourseBut.setOnClickListener {
            val addCourseFragment = FragmentAddCourse()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(addCourseFragment::class.java.simpleName)

            if(fragment !is FragmentAddCourse){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer,addCourseFragment, FragmentAddCourse::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }
        binding.removeCourseBut.setOnClickListener {
            val removeCourseFragment = FragmentRemoveCourse()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(removeCourseFragment::class.java.simpleName)

            if(fragment !is FragmentRemoveCourse){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer,removeCourseFragment, FragmentRemoveCourse::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }
        binding.assignTeacherBut.setOnClickListener {
            val assignTeacherCourseFragment = FragmentAssignTeacherCourse_CourseList()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(assignTeacherCourseFragment::class.java.simpleName)

            if(fragment !is FragmentAssignTeacherCourse_CourseList){
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragmentContainer,assignTeacherCourseFragment,
                        FragmentAssignTeacherCourse_CourseList::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }
        binding.revokeTeacherBut.setOnClickListener{
            val revokeTeacherCourseFragment = FragmentRevokeTeacherCourse_CourseList()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(revokeTeacherCourseFragment::class.java.simpleName)

            if(fragment !is FragmentRevokeTeacherCourse_CourseList){
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragmentContainer,revokeTeacherCourseFragment,
                        FragmentRevokeTeacherCourse_CourseList::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }

        binding.courseRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.courseRecyclerView.setHasFixedSize(true)

        courseArrayList = arrayListOf<CourseRecyclerViewDataClass>()
        courseListRecyclerViewAdapter = CourseListRecyclerViewAdapter(courseArrayList)

        binding.courseRecyclerView.adapter = courseListRecyclerViewAdapter

        binding.courseRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(courseRecyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) binding.editCourseFab.hide() else if (dy < 0) binding.editCourseFab.show()
            }
        })

        retrieveData()
        getCourseData()

        binding.editCourseFab.setOnClickListener {
            onEditButtonClicked()
        }
        binding.obstructor.setOnClickListener {
            binding.editCourseFab.performClick()
        }
    }

    private fun retrieveData() {
        tempArrayListCourseInfo = arrayListOf<String>()
        tempArrayListTeacherInfo = arrayListOf<String>()
        tempArrayListCourseCodeInfo = arrayListOf<String>()

        db.collection("TeacherInfo").get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    val temp = ArrayList<String>()
                    for(document in it.result!!) {
                        tempArrayListTeacherInfo.add(document.data.getValue("Name").toString())
                        temp.add(document.data.getValue("Code").toString())
                        fragmentViewModel.teacherData.postValue(tempArrayListTeacherInfo)
                        fragmentViewModel.teacherCodeData.postValue(temp)
                    }
                }
            }
        db.collection("CourseInfo").get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    for (document in it.result!!){
                        tempArrayListCourseInfo.add(document.data.getValue("CourseName").toString())
                        tempArrayListCourseCodeInfo.add(document.data.getValue("CourseCode").toString())
                        fragmentViewModel.courseData.postValue(tempArrayListCourseInfo)
                        fragmentViewModel.courseCodeData.postValue(tempArrayListCourseCodeInfo)
                    }
                }
            }

    }

    private fun onEditButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        clicked = !clicked
    }

    @SuppressLint("SetTextI18n")
    private fun setAnimation(clicked: Boolean) {
        if(!clicked){
            window.statusBarColor = Color.parseColor("#cc000000")
            binding.editCourseFab.setIconResource(R.drawable.ic_outline_close_24)
            binding.obstructor.startAnimation(AnimationUtils.loadAnimation(this,
                R.anim.nav_default_pop_enter_anim
            ))
            binding.fabListMenuLayout.startAnimation(AnimationUtils.loadAnimation(this,
                R.anim.nav_default_pop_enter_anim
            ))
        }else{
            window.statusBarColor = getColorFromAttribute(this,R.attr.colorSurfaceVariant)
            binding.editCourseFab.setIconResource(R.drawable.ic_outline_edit_24)
            binding.obstructor.startAnimation(AnimationUtils.loadAnimation(this,
                R.anim.nav_default_pop_exit_anim
            ))
            binding.fabListMenuLayout.startAnimation(AnimationUtils.loadAnimation(this,
                R.anim.nav_default_pop_exit_anim
            ))
        }
    }

    private fun setVisibility(clicked: Boolean) {
        if(!clicked){
            binding.obstructor.visibility = View.VISIBLE
            binding.fabListMenuLayout.visibility = View.VISIBLE
        }else{
            binding.fabListMenuLayout.visibility = View.INVISIBLE
            binding.obstructor.visibility = View.INVISIBLE
        }
    }

    private fun getCourseData() {
        courseArrayList.clear()
        db.collection("CourseInfo").addSnapshotListener(object: EventListener<QuerySnapshot> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if(error != null){
                    Log.e("Firestore Error",error.message.toString())
                    return
                }
                for(dc : DocumentChange in value?.documentChanges!!){
                    if(dc.type == DocumentChange.Type.ADDED){
                        courseArrayList.add(dc.document.toObject(CourseRecyclerViewDataClass::class.java))
                    }
                }
                courseListRecyclerViewAdapter.notifyDataSetChanged()
            }

        })
    }
}