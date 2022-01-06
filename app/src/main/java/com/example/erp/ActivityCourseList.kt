package com.example.erp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import kotlin.collections.ArrayList

class ActivityCourseList : AppCompatActivity() {
    private lateinit var courseRecyclerView: RecyclerView
    private lateinit var courseListRecyclerViewAdapter: CourseListRecyclerViewAdapter
    private lateinit var courseArrayList: ArrayList<CourseRecyclerViewDataClass>

    private val fragmentViewModel : ActivityCourseListViewModel by viewModels()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var editCourseFab: ExtendedFloatingActionButton
    private lateinit var addCourseBut: MaterialCardView
    private lateinit var removeCourseBut: MaterialCardView
    private lateinit var assignTeacherBut: MaterialCardView
    private lateinit var revokeTeacherBut: MaterialCardView
    private lateinit var fabListMenuLayout: CardView
    private lateinit var obstructor: RelativeLayout
    private lateinit var fragmentContainer: RelativeLayout
    private lateinit var backButton: ImageView
    private lateinit var refreshButton: ImageView
    private lateinit var parentFrameLayout: FrameLayout

    private lateinit var tempArrayListCourseInfo: ArrayList<String>
    private lateinit var tempArrayListTeacherInfo: ArrayList<String>
    private lateinit var tempArrayListCourseCodeInfo: ArrayList<String>

    private var clicked = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_list)

        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()
        }

        window.statusBarColor = getColorFromAttr(R.attr.colorSurfaceVariant)

        editCourseFab = findViewById(R.id.editCourseFab)
        refreshButton = findViewById(R.id.refreshButton)
        obstructor = findViewById(R.id.obstructor)
        addCourseBut = findViewById(R.id.addCourseBut)
        removeCourseBut = findViewById(R.id.removeCourseBut)
        assignTeacherBut = findViewById(R.id.assignTeacherBut)
        revokeTeacherBut = findViewById(R.id.revokeTeacherBut)
        fragmentContainer = findViewById(R.id.fragmentContainer)
        fabListMenuLayout = findViewById(R.id.fabListMenuLayout)
        parentFrameLayout = findViewById(R.id.parentFrameLayout)

        refreshButton.setOnClickListener {
            getCourseData()
            retrieveData()
            Snackbar.make(parentFrameLayout,"Data Refreshed", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                .show()
        }

        addCourseBut.setOnClickListener {
            val addCourseFragment = FragmentAddCourse()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(addCourseFragment::class.java.simpleName)

            if(fragment !is FragmentAddCourse){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer,addCourseFragment,FragmentAddCourse::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }
        removeCourseBut.setOnClickListener {
            val removeCourseFragment = FragmentRemoveCourse()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(removeCourseFragment::class.java.simpleName)

            if(fragment !is FragmentRemoveCourse){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer,removeCourseFragment,FragmentRemoveCourse::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }
        assignTeacherBut.setOnClickListener {
            val assignTeacherCourseFragment = FragmentAssignTeacherCourse_CourseList()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(assignTeacherCourseFragment::class.java.simpleName)

            if(fragment !is FragmentAssignTeacherCourse_CourseList){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer,assignTeacherCourseFragment,FragmentAssignTeacherCourse_CourseList::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }
        revokeTeacherBut.setOnClickListener{
            val revokeTeacherCourseFragment = FragmentRevokeTeacherCourse_CourseList()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(revokeTeacherCourseFragment::class.java.simpleName)

            if(fragment !is FragmentRevokeTeacherCourse_CourseList){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer,revokeTeacherCourseFragment,FragmentRevokeTeacherCourse_CourseList::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }

        courseRecyclerView = findViewById(R.id.courseRecyclerView)
        courseRecyclerView.layoutManager = LinearLayoutManager(this)
        courseRecyclerView.setHasFixedSize(true)

        courseArrayList = arrayListOf<CourseRecyclerViewDataClass>()
        courseListRecyclerViewAdapter = CourseListRecyclerViewAdapter(courseArrayList)

        courseRecyclerView.adapter = courseListRecyclerViewAdapter

        courseRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(courseRecyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) editCourseFab.hide() else if (dy < 0) editCourseFab.show()
            }
        })

        retrieveData()
        getCourseData()

        editCourseFab.setOnClickListener {
            onEditButtonClicked()
        }
        obstructor.setOnClickListener {
            editCourseFab.performClick()
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
            editCourseFab.setIconResource(R.drawable.ic_outline_close_24)
            obstructor.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_pop_enter_anim))
            fabListMenuLayout.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_pop_enter_anim))
        }else{
            window.statusBarColor = getColorFromAttr(R.attr.colorSurfaceVariant)
            editCourseFab.setIconResource(R.drawable.ic_outline_edit_24)
            obstructor.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_pop_exit_anim))
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

    internal fun getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
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