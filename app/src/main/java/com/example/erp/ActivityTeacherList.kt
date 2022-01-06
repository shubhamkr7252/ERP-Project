package com.example.erp

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.firestore.*
import android.view.WindowManager
import androidx.core.graphics.alpha
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.Gravity

import android.graphics.PixelFormat
import android.os.Build
import android.util.TypedValue

import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar


class ActivityTeacherList : AppCompatActivity() {
    private lateinit var teacherRecyclerView: RecyclerView
    private lateinit var teacherListRecyclerViewAdapter: TeacherListRecyclerViewAdapter
    private val db = FirebaseFirestore.getInstance()
    private lateinit var teacherArrayList: ArrayList<TeacherListRecyclerViewDataClass>

    private val fragmentViewModel : ActivityTeacherListViewModel by viewModels()
    private lateinit var backButton: ImageView

    private lateinit var editTeacherFab: ExtendedFloatingActionButton
    private lateinit var fabListMenuLayout: MaterialCardView
    private lateinit var addTeacherFab: MaterialCardView
    private lateinit var removeTeacherFab: MaterialCardView
    private lateinit var fragmentContainer: RelativeLayout
    private lateinit var revokeCourseFab: MaterialCardView
    private lateinit var assignCourseFab: MaterialCardView
    private lateinit var refreshButton: ImageView
    private lateinit var parentFrameLayout: FrameLayout

    private var clicked = false
    private lateinit var obstructor: RelativeLayout
    private lateinit var tempArrayListCourseInfo: ArrayList<String>
    private lateinit var tempArrayListTeacherInfo: ArrayList<String>

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_list)

        editTeacherFab = findViewById(R.id.editTeacherFab)
        addTeacherFab = findViewById(R.id.addTeacherFab)
        removeTeacherFab = findViewById(R.id.removeTeacherFab)
        fragmentContainer = findViewById(R.id.fragmentContainer)
        fabListMenuLayout = findViewById(R.id.fabListMenuLayout)
        revokeCourseFab = findViewById(R.id.revokeCourseFab)
        assignCourseFab = findViewById(R.id.assignCourseFab)
        refreshButton = findViewById(R.id.refreshButton)
        parentFrameLayout = findViewById(R.id.parentFrameLayout)

        window.statusBarColor = getColorFromAttr(R.attr.colorSurfaceVariant)

        obstructor = findViewById(R.id.obstructor)
        backButton = findViewById(R.id.backButton)

        teacherRecyclerView = findViewById(R.id.teacherRecyclerView)
        teacherRecyclerView.layoutManager = LinearLayoutManager(this)
        teacherRecyclerView.setHasFixedSize(true)

        teacherArrayList = arrayListOf<TeacherListRecyclerViewDataClass>()
        teacherListRecyclerViewAdapter = TeacherListRecyclerViewAdapter(teacherArrayList)

        teacherRecyclerView.adapter = teacherListRecyclerViewAdapter

        teacherRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(studentRecyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) editTeacherFab.hide() else if (dy < 0) editTeacherFab.show()
            }
        })

        refreshButton.setOnClickListener {
            getTeacherData()
            Snackbar.make(parentFrameLayout,"Data Refreshed", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                .show()
        }

        getTeacherData()

        editTeacherFab.setOnClickListener {
            onEditButtonClicked()
        }
        obstructor.setOnClickListener {
            editTeacherFab.performClick()
        }

        retrieveData()

        addTeacherFab.setOnClickListener {
            val addTeacherFragment = FragmentAddTeacher()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(addTeacherFragment::class.java.simpleName)

            if(fragment !is FragmentAddTeacher){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer,addTeacherFragment,FragmentAddTeacher::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }
        removeTeacherFab.setOnClickListener {
            val removeTeacherFragment = FragmentRemoveTeacher()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(removeTeacherFragment::class.java.simpleName)

            if(fragment !is FragmentRemoveTeacher){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer,removeTeacherFragment,FragmentRemoveTeacher::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }
        assignCourseFab.setOnClickListener {
            val assignTeacherFragment = FragmentAssignCourseTeacher_TeacherList()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(assignTeacherFragment::class.java.simpleName)

            if(fragment !is FragmentAssignCourseTeacher_TeacherList){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer,assignTeacherFragment,FragmentAssignCourseTeacher_TeacherList::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }
        revokeCourseFab.setOnClickListener {
            val revokeTeacherFragment = FragmentRevokeCourseTeacher_TeacherList()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(revokeTeacherFragment::class.java.simpleName)

            if(fragment !is FragmentRevokeCourseTeacher_TeacherList){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer,revokeTeacherFragment,FragmentRevokeCourseTeacher_TeacherList::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }

        backButton.setOnClickListener {
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
            obstructor.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_pop_enter_anim))
            editTeacherFab.setIconResource(R.drawable.ic_outline_close_24)
            fabListMenuLayout.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_pop_enter_anim))
        }else{
            window.statusBarColor = getColorFromAttr(R.attr.colorSurfaceVariant)
            obstructor.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_pop_exit_anim))
            editTeacherFab.setIconResource(R.drawable.ic_outline_edit_24)
            fabListMenuLayout.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_pop_exit_anim))
        }
    }

    private fun setVisibility(clicked: Boolean) {
        if(!clicked){
            obstructor.visibility = View.VISIBLE
            fabListMenuLayout.visibility = View.VISIBLE
        }else{
            fabListMenuLayout.visibility = View.INVISIBLE
            obstructor.visibility = View.INVISIBLE
        }
    }

    @ColorInt
    fun Context.getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }

    private fun getTeacherData() {
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