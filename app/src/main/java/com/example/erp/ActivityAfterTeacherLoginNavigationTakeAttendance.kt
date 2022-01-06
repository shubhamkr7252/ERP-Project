package com.example.erp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ActivityAfterTeacherLoginNavigationTakeAttendance : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val fragmentViewModel: ActivityAfterTeacherLoginNavigationTakeAttendanceViewModel by viewModels()
    private lateinit var title: TextView

    private lateinit var backButton: ImageView
    private lateinit var fragmentContainer: RelativeLayout
    private lateinit var detailsContainer: LinearLayout
    private lateinit var proceedBut: MaterialButton
    private lateinit var dateSelect: EditText
    private lateinit var batchSelect: AutoCompleteTextView
    private lateinit var semesterSelect: AutoCompleteTextView
    private lateinit var courseSelect: AutoCompleteTextView
    private lateinit var courseCode: EditText
    private lateinit var timeSelect: EditText
    private lateinit var teacherSelect: EditText
    private lateinit var teacherCode: EditText
    private lateinit var tempArrayListSemesterInfo: ArrayList<String>

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_after_teacher_login_navigation_take_attendance)

        proceedBut = findViewById(R.id.proceedBut)
        dateSelect = findViewById(R.id.dateSelect)
        batchSelect = findViewById(R.id.batchSelect)
        semesterSelect = findViewById(R.id.semesterSelect)
        courseSelect = findViewById(R.id.courseSelect)
        courseCode = findViewById(R.id.courseCode)
        timeSelect = findViewById(R.id.timeSelect)
        teacherSelect = findViewById(R.id.teacherSelect)
        teacherCode = findViewById(R.id.teacherCode)
        backButton = findViewById(R.id.backButton)
        fragmentContainer = findViewById(R.id.fragmentContainer)
        detailsContainer = findViewById(R.id.detailsContainer)
        proceedBut = findViewById(R.id.proceedBut)
        title = findViewById(R.id.title)

        title.text = "Attendance Details"
        window.statusBarColor = getColorFromAttr(R.attr.colorSurfaceVariant)

        proceedBut.setOnClickListener {
            checkInput()
        }

        supportFragmentManager.popBackStack()

        semesterSelect.setOnClickListener{
            if(batchSelect.text.toString().isBlank()){
                batchSelect.setError("Please select Batch first",null)
                batchSelect.requestFocus()
            }
        }

        dateSelect.setText(getCurrentDate(),null)
        timeSelect.setText(getCurrentTime(),null)
        fragmentViewModel.teacherCodeData.value = intent.getStringExtra("teacherCodeTxt").toString()
        teacherCode.setText(intent.getStringExtra("teacherCodeTxt").toString(),null)
        fragmentViewModel.teacherNameData.value = intent.getStringExtra("teacherTxt").toString()
        teacherSelect.setText(intent.getStringExtra("teacherTxt").toString(),null)
        val batchArrayList: ArrayList<String> = intent.getStringArrayListExtra("batchTxt") as ArrayList<String>
        val batchArrayAdapter = ArrayAdapter<String>(this,R.layout.exposed_dropdown_menu_item_layout,batchArrayList)
        batchSelect.setAdapter(batchArrayAdapter)
        val courseArrayList: ArrayList<String> = intent.getStringArrayListExtra("courseTxt") as ArrayList<String>
        val courseArrayAdapter = ArrayAdapter<String>(this,R.layout.exposed_dropdown_menu_item_layout,courseArrayList)
        courseSelect.setAdapter(courseArrayAdapter)

        tempArrayListSemesterInfo = arrayListOf<String>()
        batchSelect.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            if(batchSelect.text.isNotEmpty()){
                batchSelect.error = null
            }
            semesterSelect.text.clear()
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
                                val addStudentSemesterSelectAdapter = ArrayAdapter(this@ActivityAfterTeacherLoginNavigationTakeAttendance, R.layout.exposed_dropdown_menu_item_layout, tempArrayListSemesterInfo)
                                semesterSelect.setAdapter(addStudentSemesterSelectAdapter)
                            }
                            break
                        }

                    }
                })
        }

        courseSelect.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            if (courseSelect.text.isNotEmpty()) {
                courseSelect.error = null
            }
            val selectedItem = parent.getItemAtPosition(position).toString()
            db.collection("CourseInfo").whereEqualTo("CourseName",selectedItem).get()
                .addOnCompleteListener { itAlt ->
                    if(itAlt.isSuccessful){
                        for (doc in itAlt.result!!){
                            courseCode.setText(doc.data.getValue("CourseCode").toString(),null)
                        }
                    }
                }
        }

        proceedBut.setOnClickListener {
            checkInput()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBackPressed() {
        super.onBackPressed()
        title.text = "Attendance Details"
    }

    private fun checkInput(){
        if(batchSelect.text.isEmpty()) {
            batchSelect.setError("Please select Batch",null)
            batchSelect.requestFocus()
            return
        }
        if(semesterSelect.text.isEmpty()) {
            semesterSelect.setError("Please select semester",null)
            semesterSelect.requestFocus()
            semesterSelect.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                semesterSelect.error = null
            }
            return
        }
        if(courseSelect.text.isEmpty()) {
            courseSelect.setError("Please select course",null)
            courseSelect.requestFocus()
            return
        }
        if (courseCode.text.toString().isEmpty()){
            courseCode.setError("Please wait for Course Code to load",null)
            courseCode.requestFocus()
            return
        }
        if (teacherSelect.text.toString().isEmpty()){
            teacherSelect.setError("Please wait for Teacher Name to load",null)
            teacherSelect.requestFocus()
            return
        }
        if (teacherCode.text.toString().isEmpty()){
            teacherCode.setError("Please wait for Teacher Code to load",null)
            teacherCode.requestFocus()
            return
        }

        startAttendance()
    }

    @SuppressLint("SetTextI18n")
    private fun startAttendance() {
        fragmentViewModel.semesterDataString.value = semesterSelect.text.toString()
        fragmentViewModel.batchDataString.value = batchSelect.text.toString()
        fragmentViewModel.courseNameData.value = courseSelect.text.toString()
        fragmentViewModel.courseCodeData.value = courseCode.text.toString()
        fragmentViewModel.dateData.value = dateSelect.text.toString()
        fragmentViewModel.timeData.value = timeSelect.text.toString()
        title.setText("Students List",null)
        val fragmentTakeAttendance = FragmentTakeAttendance()
        val fragment : Fragment? =

            supportFragmentManager.findFragmentByTag(fragmentTakeAttendance::class.java.simpleName)

        if(fragment !is FragmentTakeAttendance){
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer,fragmentTakeAttendance,FragmentTakeAttendance::class.java.simpleName)
                .addToBackStack(null)
                .commit()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentDate(): String {
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("dd-MM-yyyy") //or use getDateInstance()
        return formatter.format(date)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentTime(): String {
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("HH:mm") //or use getDateInstance()
        return formatter.format(date)
    }

    @ColorInt
    private fun Context.getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }
}