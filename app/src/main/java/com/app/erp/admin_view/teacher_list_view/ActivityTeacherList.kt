package com.app.erp.admin_view.teacher_list_view

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.firestore.*

import android.util.TypedValue

import androidx.activity.viewModels
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import com.app.erp.R
import com.app.erp.databinding.ActivityTeacherListBinding
import com.app.erp.gloabal_functions.getColorFromAttribute
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar


class ActivityTeacherList : AppCompatActivity() {
    private lateinit var teacherListRecyclerViewAdapter: TeacherListRecyclerViewAdapter
    private lateinit var teacherArrayList: ArrayList<TeacherListRecyclerViewDataClass>

    private val db = FirebaseFirestore.getInstance()
    private val fragmentViewModel : ActivityTeacherListViewModel by viewModels()
    private lateinit var binding: ActivityTeacherListBinding

    private var clicked = false

    private lateinit var tempArrayListCourseInfo: ArrayList<String>
    private lateinit var tempArrayListTeacherInfo: ArrayList<String>

    override fun onBackPressed() {
        if(binding.obstructor.isVisible){
            binding.editTeacherFab.performClick()
        }
        else{
            super.onBackPressed()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = getColorFromAttribute(this,R.attr.colorSurfaceVariant)
        fragmentViewModel.adminEmailData.postValue(intent.getStringExtra("mailTxt"))

        binding.teacherRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.teacherRecyclerView.setHasFixedSize(true)

        teacherArrayList = arrayListOf<TeacherListRecyclerViewDataClass>()
        teacherListRecyclerViewAdapter = TeacherListRecyclerViewAdapter(teacherArrayList)

        binding.teacherRecyclerView.adapter = teacherListRecyclerViewAdapter

        binding.teacherRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(studentRecyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) binding.editTeacherFab.hide() else if (dy < 0) binding.editTeacherFab.show()
            }
        })

        binding.refreshButton.setOnClickListener {
            getTeacherData()
            Snackbar.make(binding.parentFrameLayout,"Data Refreshed", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColorFromAttribute(this,R.attr.colorPrimary))
                .show()
        }

        getTeacherData()

        binding.editTeacherFab.setOnClickListener {
            onEditButtonClicked()
        }
        binding.obstructor.setOnClickListener {
            binding.editTeacherFab.performClick()
        }

        retrieveData()

        binding.addTeacherFab.setOnClickListener {
            val addTeacherFragment = FragmentAddTeacher()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(addTeacherFragment::class.java.simpleName)

            if(fragment !is FragmentAddTeacher){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer,addTeacherFragment, FragmentAddTeacher::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }
        binding.removeTeacherFab.setOnClickListener {
            val removeTeacherFragment = FragmentRemoveTeacher()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(removeTeacherFragment::class.java.simpleName)

            if(fragment !is FragmentRemoveTeacher){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer,removeTeacherFragment, FragmentRemoveTeacher::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }
        binding.assignCourseFab.setOnClickListener {
            val assignTeacherFragment = FragmentAssignCourseTeacher_TeacherList()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(assignTeacherFragment::class.java.simpleName)

            if(fragment !is FragmentAssignCourseTeacher_TeacherList){
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragmentContainer,assignTeacherFragment,
                        FragmentAssignCourseTeacher_TeacherList::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }
        binding.revokeCourseFab.setOnClickListener {
            val revokeTeacherFragment = FragmentRevokeCourseTeacher_TeacherList()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(revokeTeacherFragment::class.java.simpleName)

            if(fragment !is FragmentRevokeCourseTeacher_TeacherList){
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragmentContainer,revokeTeacherFragment,
                        FragmentRevokeCourseTeacher_TeacherList::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

    }

    private fun retrieveData() {
        tempArrayListCourseInfo = arrayListOf<String>()
        tempArrayListTeacherInfo = arrayListOf<String>()

        db.collection("CourseInfo").get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    for (document in it.result!!){
                        tempArrayListCourseInfo.add(document.data.getValue("CourseName").toString())
                        fragmentViewModel.courseData.postValue(tempArrayListCourseInfo)
                    }
                }
            }
        db.collection("TeacherInfo").get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    for(document in it.result!!) {
                        tempArrayListTeacherInfo.add(document.data.getValue("Name").toString())
                        fragmentViewModel.teacherData.postValue(tempArrayListTeacherInfo)
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
            binding.obstructor.startAnimation(AnimationUtils.loadAnimation(this,
                R.anim.nav_default_pop_enter_anim
            ))
            binding.editTeacherFab.setIconResource(R.drawable.ic_outline_close_24)
            binding.fabListMenuLayout.startAnimation(AnimationUtils.loadAnimation(this,
                R.anim.nav_default_pop_enter_anim
            ))
        }else{
            window.statusBarColor = getColorFromAttribute(this,R.attr.colorSurfaceVariant)
            binding.obstructor.startAnimation(AnimationUtils.loadAnimation(this,
                R.anim.nav_default_pop_exit_anim
            ))
            binding.editTeacherFab.setIconResource(R.drawable.ic_outline_edit_24)
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

    internal fun getTeacherData() {
        teacherArrayList.clear()
        db.collection("TeacherInfo").addSnapshotListener(object: EventListener<QuerySnapshot> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if(error != null){
                    Log.e("Firestore Error",error.message.toString())
                    return
                }
                for(dc : DocumentChange in value?.documentChanges!!){
                    if(dc.type == DocumentChange.Type.ADDED){
                        teacherArrayList.add(dc.document.toObject(TeacherListRecyclerViewDataClass::class.java))
                    }
                }
                teacherListRecyclerViewAdapter.notifyDataSetChanged()
            }
        })
    }
}