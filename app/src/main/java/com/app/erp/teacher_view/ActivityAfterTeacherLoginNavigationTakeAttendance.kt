package com.app.erp.teacher_view

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.app.erp.R
import com.app.erp.databinding.ActivityAfterTeacherLoginNavigationTakeAttendanceBinding
import com.app.erp.gloabal_functions.getColorFromAttribute
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ActivityAfterTeacherLoginNavigationTakeAttendance : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val fragmentViewModel: ActivityAfterTeacherLoginNavigationTakeAttendanceViewModel by viewModels()
    private lateinit var binding: ActivityAfterTeacherLoginNavigationTakeAttendanceBinding

    private lateinit var tempArrayListSemesterInfo: ArrayList<String>
    override fun onResume() {
        super.onResume()

        val batchArrayList: ArrayList<String> = intent.getStringArrayListExtra("batchTxt") as ArrayList<String>
        val batchArrayAdapter = ArrayAdapter<String>(this, R.layout.exposed_dropdown_menu_item_layout,batchArrayList)
        binding.batchSelect.setAdapter(batchArrayAdapter)
        val courseArrayList: ArrayList<String> = intent.getStringArrayListExtra("courseTxt") as ArrayList<String>
        val courseArrayAdapter = ArrayAdapter<String>(this, R.layout.exposed_dropdown_menu_item_layout,courseArrayList)
        binding.courseSelect.setAdapter(courseArrayAdapter)

        if(binding.batchSelect.text.isNotEmpty()){
            db.collection("BatchInfo").whereEqualTo("Name",binding.batchSelect.text.toString())
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
                                val addStudentSemesterSelectAdapter = ArrayAdapter(this@ActivityAfterTeacherLoginNavigationTakeAttendance,
                                    R.layout.exposed_dropdown_menu_item_layout, tempArrayListSemesterInfo)
                                binding.semesterSelect.setAdapter(addStudentSemesterSelectAdapter)
                            }
                            break
                        }

                    }
                })
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAfterTeacherLoginNavigationTakeAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = getColorFromAttribute(this, R.attr.colorSurfaceVariant)

        binding.title.text = "Attendance Details"

        binding.proceedBut.setOnClickListener {
            checkInput()
        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.semesterSelect.setOnClickListener{
            if(binding.batchSelect.text.toString().isBlank()){
                binding.batchSelect.setError("Please select Batch first",null)
                binding.batchSelect.requestFocus()
            }
        }

        binding.dateSelect.setText(getCurrentDate())
        binding.timeSelect.setText(getCurrentTime())
        fragmentViewModel.teacherCodeData.value = intent.getStringExtra("teacherCodeTxt").toString()
        binding.teacherCode.setText(intent.getStringExtra("teacherCodeTxt").toString())
        fragmentViewModel.teacherNameData.value = intent.getStringExtra("teacherTxt").toString()
        binding.teacherSelect.setText(intent.getStringExtra("teacherTxt").toString())
        val batchArrayList: ArrayList<String> = intent.getStringArrayListExtra("batchTxt") as ArrayList<String>
        val batchArrayAdapter = ArrayAdapter<String>(this, R.layout.exposed_dropdown_menu_item_layout,batchArrayList)
        binding.batchSelect.setAdapter(batchArrayAdapter)
        val courseArrayList: ArrayList<String> = intent.getStringArrayListExtra("courseTxt") as ArrayList<String>
        val courseArrayAdapter = ArrayAdapter<String>(this, R.layout.exposed_dropdown_menu_item_layout,courseArrayList)
        binding.courseSelect.setAdapter(courseArrayAdapter)

        binding.batchSelect.setOnFocusChangeListener { _, batchFocus ->
            if(batchFocus){
                binding.batchSemesterIcon.setImageResource(R.drawable.ic_baseline_school_24)
            }
            else{
                binding.batchSemesterIcon.setImageResource(R.drawable.ic_outline_school_24)
            }
        }
        binding.semesterSelect.setOnFocusChangeListener { _, semesterFocus ->
            if(semesterFocus){
                binding.batchSemesterIcon.setImageResource(R.drawable.ic_baseline_school_24)
            }
            else{
                binding.batchSemesterIcon.setImageResource(R.drawable.ic_outline_school_24)
            }
        }
        binding.courseSelect.setOnFocusChangeListener { _, courseFocus ->
            if (courseFocus){
                binding.courseIcon.setImageResource(R.drawable.ic_baseline_book_24)
            }
            else{
                binding.courseIcon.setImageResource(R.drawable.ic_outline_book_24)
            }
        }

        tempArrayListSemesterInfo = arrayListOf<String>()
        binding.batchSelect.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            if(binding.batchSelect.text.isNotEmpty()){
                binding.batchSelect.error = null
            }
            binding.semesterSelect.text.clear()
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
                                val addStudentSemesterSelectAdapter = ArrayAdapter(this@ActivityAfterTeacherLoginNavigationTakeAttendance,
                                    R.layout.exposed_dropdown_menu_item_layout, tempArrayListSemesterInfo)
                                binding.semesterSelect.setAdapter(addStudentSemesterSelectAdapter)
                            }
                            break
                        }

                    }
                })
        }

        binding.courseSelect.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            if (binding.courseSelect.text.isNotEmpty()) {
                binding.courseSelect.error = null
            }
            val selectedItem = parent.getItemAtPosition(position).toString()
            db.collection("CourseInfo").whereEqualTo("CourseName",selectedItem).get()
                .addOnCompleteListener { itAlt ->
                    if(itAlt.isSuccessful){
                        for (doc in itAlt.result!!){
                            binding.courseCode.setText(doc.data.getValue("CourseCode").toString(),null)
                        }
                    }
                }
        }

        binding.proceedBut.setOnClickListener {
            checkInput()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBackPressed() {
        super.onBackPressed()
        binding.title.text = "Attendance Details"
//        overridePendingTransition(0,R.anim.nav_default_exit_anim)
    }

    private fun checkInput(){
        if(binding.batchSelect.text.isEmpty()) {
            binding.batchSelect.setError("Please select Batch",null)
            binding.batchSelect.requestFocus()
            return
        }
        if(binding.semesterSelect.text.isEmpty()) {
            binding.semesterSelect.setError("Please select semester",null)
            binding.semesterSelect.requestFocus()
            binding.semesterSelect.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                binding.semesterSelect.error = null
            }
            return
        }
        if(binding.courseSelect.text.isEmpty()) {
            binding.courseSelect.setError("Please select course",null)
            binding.courseSelect.requestFocus()
            return
        }
        if (binding.courseCode.text.toString().isEmpty()){
            binding.courseCode.setError("Please wait for Course Code to load",null)
            binding.courseCode.requestFocus()
            return
        }
        if (binding.teacherSelect.text.toString().isEmpty()){
            binding.teacherSelect.setError("Please wait for Teacher Name to load",null)
            binding.teacherSelect.requestFocus()
            return
        }
        if (binding.teacherCode.text.toString().isEmpty()){
            binding.teacherCode.setError("Please wait for Teacher Code to load",null)
            binding.teacherCode.requestFocus()
            return
        }

        binding.batchSelect.clearFocus()
        binding.semesterSelect.clearFocus()
        binding.courseSelect.clearFocus()
        binding.courseCode.clearFocus()

        startAttendance()
    }

    @SuppressLint("SetTextI18n")
    private fun startAttendance() {
        fragmentViewModel.semesterDataString.value = binding.semesterSelect.text.toString()
        fragmentViewModel.batchDataString.value = binding.batchSelect.text.toString()
        fragmentViewModel.courseNameData.value = binding.courseSelect.text.toString()
        fragmentViewModel.courseCodeData.value = binding.courseCode.text.toString()
        fragmentViewModel.dateData.value = binding.dateSelect.text.toString()
        fragmentViewModel.timeData.value = binding.timeSelect.text.toString()
        binding.title.setText("Students List",null)
        val fragmentTakeAttendance = FragmentTakeAttendanceStudentList()
        val fragment : Fragment? =

            supportFragmentManager.findFragmentByTag(fragmentTakeAttendance::class.java.simpleName)

        if(fragment !is FragmentTakeAttendanceStudentList){
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer,fragmentTakeAttendance, FragmentTakeAttendanceStudentList::class.java.simpleName)
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

}