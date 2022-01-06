package com.example.erp

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
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import java.util.*
import kotlin.collections.ArrayList

class ActivityStudentList : AppCompatActivity() {
    private lateinit var studentRecyclerView: RecyclerView
    private lateinit var studentListRecyclerViewAdapter: StudentListRecyclerViewAdapter
    private val db = FirebaseFirestore.getInstance()
    private lateinit var studentArrayList: ArrayList<StudentListRecyclerViewDataClass>
    private val fragmentViewModel : ActivityStudentListViewModel by viewModels()
    private lateinit var relativeLayoutFragmentLayoutContainer: RelativeLayout
    private lateinit var parentFrameLayout: FrameLayout

    private lateinit var filterStudentFab: ExtendedFloatingActionButton
    private lateinit var editStudentFab: ExtendedFloatingActionButton
    private lateinit var backButton: ImageView
    private lateinit var addStudentFab: MaterialCardView
    private lateinit var removeStudentFab: MaterialCardView
    private lateinit var filterStudentList: MaterialButton
    private lateinit var linearLayoutTopToolbar: LinearLayout
    private lateinit var linearLayoutTopToolbarDivider: MaterialDivider
    private lateinit var obstructor: RelativeLayout
    private lateinit var fabListMenuLayout: MaterialCardView
    private lateinit var refreshButton: ImageView

    private lateinit var studentListBatchSelect: AutoCompleteTextView
    private lateinit var studentListSemesterSelect: AutoCompleteTextView

    private lateinit var tempArrayListBatchInfo: ArrayList<String>
    private lateinit var tempArrayListSemesterInfo: ArrayList<String>

    private var clicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_list)

        addStudentFab = findViewById(R.id.addStudentFab)
        removeStudentFab = findViewById(R.id.removeStudentFab)
        relativeLayoutFragmentLayoutContainer = findViewById(R.id.relativeLayoutFragmentLayoutContainer)
        backButton = findViewById(R.id.backButton)
        editStudentFab = findViewById(R.id.editStudentFab)
        filterStudentFab = findViewById(R.id.filterStudentFab)
        studentListBatchSelect = findViewById(R.id.studentListBatchSelect)
        studentListSemesterSelect = findViewById(R.id.studentListSemesterSelect)
        filterStudentList = findViewById(R.id.filterStudentList)
        linearLayoutTopToolbar = findViewById(R.id.linearLayoutTopToolbar)
        linearLayoutTopToolbarDivider = findViewById(R.id.linearLayoutTopToolbarDivider)
        obstructor = findViewById(R.id.obstructor)
        fabListMenuLayout = findViewById(R.id.fabListMenuLayout)
        refreshButton = findViewById(R.id.refreshButton)
        parentFrameLayout = findViewById(R.id.parentFrameLayout)

        window.statusBarColor = getColorFromAttr(R.attr.colorSurfaceVariant)

        retrieveBatchSemesterDataList()

        tempArrayListBatchInfo = arrayListOf<String>()
        tempArrayListSemesterInfo = arrayListOf<String>()

        studentListSemesterSelect.setOnClickListener {
            if(studentListBatchSelect.text.isEmpty()){
                studentListBatchSelect.setError("Please select batch first",null)
                studentListBatchSelect.requestFocus()
            }
        }

        studentRecyclerView = findViewById(R.id.studentRecyclerView)
        studentRecyclerView.layoutManager = LinearLayoutManager(this)
        studentRecyclerView.setHasFixedSize(true)

        studentArrayList = arrayListOf<StudentListRecyclerViewDataClass>()
        studentListRecyclerViewAdapter = StudentListRecyclerViewAdapter(studentArrayList)

        studentRecyclerView.adapter = studentListRecyclerViewAdapter

        studentRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(studentRecyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) editStudentFab.hide() else if (dy < 0) editStudentFab.show()
                if (dy > 0) filterStudentFab.hide() else if (dy < 0) filterStudentFab.show()
            }
        })

        getStudentData()

        addStudentFab.setOnClickListener {
            val addStudentFragment = FragmentAddStudent()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(addStudentFragment::class.java.simpleName)

            if(fragment !is FragmentAddStudent){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.relativeLayoutFragmentLayoutContainer,addStudentFragment,FragmentAddStudent::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }
        removeStudentFab.setOnClickListener {
            val removeStudentFragment = FragmentRemoveStudent()
            val fragment : Fragment? =

            supportFragmentManager.findFragmentByTag(removeStudentFragment::class.java.simpleName)

            if(fragment !is FragmentRemoveStudent){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.relativeLayoutFragmentLayoutContainer,removeStudentFragment,FragmentRemoveStudent::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }

        filterStudentFab.setOnClickListener {
            if(linearLayoutTopToolbar.isVisible){
                linearLayoutTopToolbar.visibility = View.GONE
                linearLayoutTopToolbarDivider.visibility = View.GONE
                filterStudentFab.setIconResource(R.drawable.ic_outline_filter_alt_24)
                if(studentListBatchSelect.text.isNotBlank() && studentListSemesterSelect.text.isNotBlank()){
                    studentListBatchSelect.text = null
                    studentListSemesterSelect.text = null
                    studentListBatchSelect.clearFocus()
                    studentListSemesterSelect.clearFocus()
                    getStudentData()
                    Snackbar.make(parentFrameLayout,"Filter Cleared",Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                        .show()
                }
            }else{
                linearLayoutTopToolbar.visibility = View.VISIBLE
                linearLayoutTopToolbarDivider.visibility = View.VISIBLE
                filterStudentFab.setIconResource(R.drawable.ic_outline_filter_alt_off_24)
            }
        }

        obstructor.setOnClickListener {
            editStudentFab.performClick()
        }

        refreshButton.setOnClickListener {
            if(linearLayoutTopToolbar.isVisible){
                if(studentListBatchSelect.text.isNotBlank() && studentListSemesterSelect.text.isNotBlank()){
                    getFilteredStudentData()
                }else{
                    getStudentData()
                }
            }else{
                getStudentData()
            }
            Snackbar.make(parentFrameLayout,"Data Refreshed",Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                .show()
        }

        editStudentFab.setOnClickListener {
            onEditButtonClicked()
        }

        filterStudentList.setOnClickListener {
            Snackbar.make(parentFrameLayout,"Filter Applied",Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                .show()
            getFilteredStudentData()
        }

        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    internal fun getStudentData() {
        studentArrayList.clear()
        val tempArray = ArrayList<String>()

        db.collection("StudentInfo").addSnapshotListener(object: EventListener<QuerySnapshot> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if(error != null){
                    Log.e("Firestore Error",error.message.toString())
                    return
                }

                for(dc : DocumentChange in value?.documentChanges!!){
                    if(dc.type == DocumentChange.Type.ADDED){
                        studentArrayList.add(dc.document.toObject(StudentListRecyclerViewDataClass::class.java))
                        tempArray.add(dc.document.data.getValue("SRN").toString())
                    }
                }
                fragmentViewModel.studentSrnData.postValue(tempArray)
                studentListRecyclerViewAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun getFilteredStudentData() {
        studentArrayList.clear()

        if(studentListBatchSelect.text.isEmpty()){
            studentListBatchSelect.setError("Please Choose a Batch",null)
            studentListBatchSelect.requestFocus()
            studentListBatchSelect.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, _, _ ->
                    studentListBatchSelect.error = null
                }
            return
        }
        if(studentListSemesterSelect.text.isEmpty()){
            studentListSemesterSelect.setError("Please Choose a Semester",null)
            studentListSemesterSelect.requestFocus()
            studentListSemesterSelect.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, _, _ ->
                    studentListSemesterSelect.error = null
                }
            return
        }

        db.collection("StudentInfo").whereEqualTo("Batch",studentListBatchSelect.text.toString()).whereEqualTo("Semester",studentListSemesterSelect.text.toString()).addSnapshotListener(object:
            EventListener<QuerySnapshot> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if(error != null){
                    Log.e("Firestore Error",error.message.toString())
                    return
                }

                for(dc : DocumentChange in value?.documentChanges!!){
                    if(dc.type == DocumentChange.Type.ADDED){
                        studentArrayList.add(dc.document.toObject(StudentListRecyclerViewDataClass::class.java))
                    }
                }
                studentListRecyclerViewAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun retrieveBatchSemesterDataList() {
        db.collection("BatchInfo").get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    for(document in it.result!!) {
                        tempArrayListBatchInfo.add(document.data.getValue("Name").toString())
                    }
                    fragmentViewModel.batchData.value = tempArrayListBatchInfo
                    val arrayAdapter = ArrayAdapter(this,R.layout.exposed_dropdown_menu_item_layout,tempArrayListBatchInfo)
                    studentListBatchSelect.setAdapter(arrayAdapter)
                    studentListBatchSelect.setOnItemClickListener { parent, view, position, id ->
                        if(studentListBatchSelect.text.isNotEmpty()){
                            studentListBatchSelect.error = null
                        }
                        studentListSemesterSelect.text.clear()
                        tempArrayListSemesterInfo.clear()
                        val selectedItem = parent.getItemAtPosition(position).toString()

                        db.collection("BatchInfo").whereEqualTo("Name",selectedItem)
                            .addSnapshotListener(object: EventListener<QuerySnapshot> {
                                @SuppressLint("NotifyDataSetChanged")
                                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                                    if(error != null){
                                        Log.e("Firestore Error",error.message.toString())
                                        return
                                    }
                                    for(dc : DocumentChange in value?.documentChanges!!){
                                        if(dc.type == DocumentChange.Type.ADDED){
                                            tempArrayListSemesterInfo.addAll(dc.document.data.getValue("Semester") as Collection<String>)
                                            val addStudentSemesterSelectAdapter = ArrayAdapter(this@ActivityStudentList,R.layout.exposed_dropdown_menu_item_layout,tempArrayListSemesterInfo)
                                            studentListSemesterSelect.setAdapter(addStudentSemesterSelectAdapter)
                                        }
                                        break
                                    }

                                }
                            })
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
            filterStudentFab.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_pop_exit_anim))
            editStudentFab.setIconResource(R.drawable.ic_outline_close_24)
            fabListMenuLayout.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_pop_enter_anim))
        }else{
            window.statusBarColor = getColorFromAttr(R.attr.colorSurfaceVariant)
            obstructor.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_pop_exit_anim))
            filterStudentFab.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_pop_enter_anim))
            editStudentFab.setIconResource(R.drawable.ic_outline_edit_24)
            fabListMenuLayout.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_pop_exit_anim))
        }
    }

    private fun setVisibility(clicked: Boolean) {
        if(!clicked){
            filterStudentFab.visibility = View.GONE
            obstructor.visibility = View.VISIBLE
            fabListMenuLayout.visibility = View.VISIBLE
        }else{
            filterStudentFab.visibility = View.VISIBLE
            fabListMenuLayout.visibility = View.INVISIBLE
            obstructor.visibility = View.INVISIBLE
        }
    }

    @ColorInt
    internal fun getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }

}