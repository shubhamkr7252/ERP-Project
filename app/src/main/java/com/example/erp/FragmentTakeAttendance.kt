package com.example.erp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.firestore.*
import java.util.HashMap
import android.widget.Toast
import android.view.animation.AnimationUtils
import android.widget.TextView


class FragmentTakeAttendance : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: TakeAttendanceListRecyclerViewAdapter
    private lateinit var arrayListRecyclerViewTake: ArrayList<TakeAttendanceListRecyclerViewDataClass>
    private lateinit var obstructor: RelativeLayout

    private val viewModel : ActivityAfterTeacherLoginNavigationTakeAttendanceViewModel by activityViewModels()

    private lateinit var timeDate: TextView
    private lateinit var teacherNameCode: TextView
    private lateinit var batchSemester: TextView
    private lateinit var courseNameCode: TextView
    private lateinit var infoLinearLayout: LinearLayout
    private lateinit var infoBut: ExtendedFloatingActionButton
    private lateinit var saveBut: ExtendedFloatingActionButton
    private lateinit var parentFrameLayout: FrameLayout
    private lateinit var obstructorInfoLayout: RelativeLayout

    private lateinit var tempArrayPresentStudentList: ArrayList<String>
    private lateinit var tempArrayAbsentStudentList: ArrayList<String>

    private lateinit var time: String
    private lateinit var date: String
    private lateinit var teacherName: String
    private lateinit var teacherCode: String
    private lateinit var batchName: String
    private lateinit var semesterName: String
    private lateinit var courseName: String
    private lateinit var courseCode: String
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_take_attendance, container, false)

        timeDate = view.findViewById(R.id.timeDate)
        teacherNameCode = view.findViewById(R.id.teacherNameCode)
        batchSemester = view.findViewById(R.id.batchSemester)
        courseNameCode = view.findViewById(R.id.courseNameCode)
        obstructor = view.findViewById(R.id.obstructor)

        infoLinearLayout = view.findViewById(R.id.infoLinearLayout)
        obstructorInfoLayout = view.findViewById(R.id.obstructorInfoLayout)
        infoBut = view.findViewById(R.id.infoBut)
        saveBut = view.findViewById(R.id.saveBut)
        parentFrameLayout = view.findViewById(R.id.parentFrameLayout)

        teacherName = String()
        teacherCode = String()
        batchName = String()
        semesterName = String()
        courseName = String()
        courseCode = String()


        viewModel.dateData.observe(viewLifecycleOwner,{
            date = it
            viewModel.timeData.observe(viewLifecycleOwner,{ itAlt ->
                time = itAlt
                timeDate.setText(it.plus(", ").plus(itAlt),null)
            })
        })
        viewModel.teacherNameData.observe(viewLifecycleOwner,{
            teacherName = it
            viewModel.teacherCodeData.observe(viewLifecycleOwner,{ itAlt ->
                teacherCode = itAlt
                teacherNameCode.setText(it.plus(" - ").plus(itAlt),null)
            })
        })
        viewModel.batchDataString.observe(viewLifecycleOwner,{
            batchName = it
            viewModel.semesterDataString.observe(viewLifecycleOwner,{ itAlt ->
                semesterName = itAlt
                getStudentData(it, itAlt)
                batchSemester.setText(it.plus(" - ").plus(itAlt),null)
            })
        })
        viewModel.courseNameData.observe(viewLifecycleOwner,{
            courseName = it
            viewModel.courseCodeData.observe(viewLifecycleOwner,{ itAlt ->
                courseCode = itAlt
                courseNameCode.setText(it.plus(" - ").plus(itAlt),null)
            })
        })

        infoBut.setOnClickListener {
            if(obstructorInfoLayout.isVisible){
                obstructorInfoLayout.startAnimation(AnimationUtils.loadAnimation(activity,R.anim.nav_default_pop_exit_anim))
                saveBut.startAnimation(AnimationUtils.loadAnimation(activity,R.anim.nav_default_pop_enter_anim))
                obstructorInfoLayout.visibility = View.GONE
                saveBut.visibility = View.VISIBLE
            }else{
                obstructorInfoLayout.startAnimation(AnimationUtils.loadAnimation(activity,R.anim.nav_default_pop_enter_anim))
                saveBut.startAnimation(AnimationUtils.loadAnimation(activity,R.anim.nav_default_pop_exit_anim))
                obstructorInfoLayout.visibility = View.VISIBLE
                saveBut.visibility = View.GONE
            }
        }

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(studentRecyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) saveBut.hide() else if (dy < 0) saveBut.show()
                if (dy > 0) infoBut.hide() else if (dy < 0) infoBut.show()
            }
        })
        arrayListRecyclerViewTake = arrayListOf<TakeAttendanceListRecyclerViewDataClass>()
        tempArrayPresentStudentList = arrayListOf<String>()
        tempArrayAbsentStudentList = arrayListOf<String>()

        recyclerViewAdapter = TakeAttendanceListRecyclerViewAdapter(arrayListRecyclerViewTake, requireActivity(), object : ViewHolderAttendance.Listener {
            override fun getSelectedStudentList(list: ArrayList<String>) {
                tempArrayPresentStudentList.clear()
                tempArrayPresentStudentList.addAll(list)
            }
            override fun getUnselectedStudentList(list: ArrayList<String>) {
                tempArrayAbsentStudentList.clear()
                tempArrayAbsentStudentList.addAll(list)
            }
        })

        recyclerView.adapter = recyclerViewAdapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(courseRecyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) saveBut.hide() else if (dy < 0) saveBut.show()
            }
        })

        saveBut.setOnClickListener {
            obstructor.visibility = View.VISIBLE
            addAttendance()
            addAttendanceDetails()
        }
        return view
    }

    private fun addAttendanceDetails() {
        for (i in tempArrayPresentStudentList){
            val attendedUpdate = hashMapOf<String, Any>(
                "AttendedClasses" to FieldValue.increment(1)
            )

            db.collection("StudentInfo").document(i).update(attendedUpdate).addOnSuccessListener {
                val classUpdate = hashMapOf<String, Any>(
                    "TotalClasses" to FieldValue.increment(1)
                )
                db.collection("StudentInfo").document(i).update(classUpdate)
            }
        }
        for (i in tempArrayAbsentStudentList){
            val notAttendedUpdate = hashMapOf<String, Any>(
                "NonAttendedClasses" to FieldValue.increment(1)
            )

            db.collection("StudentInfo").document(i).update(notAttendedUpdate).addOnSuccessListener {
                val classUpdate = hashMapOf<String, Any>(
                    "TotalClasses" to FieldValue.increment(1)
                )
                db.collection("StudentInfo").document(i).update(classUpdate)
            }
        }
        obstructor.visibility = View.GONE
    }

    private fun addAttendance(){
        val dateTxt = date
        val timeTxt = time
        val batchTxt = batchName
        val semesterTxt = semesterName
        val teacherCodeTxt = teacherCode
        val tempTime = ArrayList<String>()
        val totalStrength : Long = (tempArrayAbsentStudentList.size+tempArrayPresentStudentList.size).toLong()
        tempTime.add(timeTxt)

        val attendanceDocDetails: MutableMap<String, Any> = HashMap()
        attendanceDocDetails["Time"] = tempTime
        attendanceDocDetails["Date"] = dateTxt

        val attendanceDetails: MutableMap<String, Any> = HashMap()
        attendanceDetails["Teacher"] = teacherName
        attendanceDetails["TeacherCode"] = teacherCodeTxt
        attendanceDetails["Course"] = courseName
        attendanceDetails["CourseCode"] = courseCode
        attendanceDetails["Time"] = timeTxt
        attendanceDetails["Date"] = dateTxt
        attendanceDetails["Batch"] = batchTxt
        attendanceDetails["Semester"] = semesterTxt
        attendanceDetails["TotalPresent"] = tempArrayPresentStudentList.size
        attendanceDetails["TotalAbsent"] = tempArrayAbsentStudentList.size
        attendanceDetails["TotalStrength"] = totalStrength

        val updates = hashMapOf<String, Any>(
            "Time" to FieldValue.arrayUnion(timeTxt)
        )

        val studentAttendance: MutableMap<String, Any> = HashMap()
        studentAttendance["StudentPresent"] = tempArrayPresentStudentList
        studentAttendance["StudentAbsent"] = tempArrayAbsentStudentList
        db.collection("Attendance").document(dateTxt).collection(timeTxt).document(batchTxt.plus("_").plus(semesterTxt)).set(studentAttendance)
            .addOnCompleteListener {
                db.collection("Attendance").document(dateTxt).collection(timeTxt).document(batchTxt.plus("_").plus(semesterTxt)).update(attendanceDetails).addOnSuccessListener {
                    db.collection("Attendance").document(dateTxt).get()
                        .addOnSuccessListener {
                            if (it.exists()){
                                db.collection("Attendance").document(dateTxt).update(updates).addOnSuccessListener {
                                    Toast.makeText(activity,"Attendance Added",Toast.LENGTH_SHORT).show()
                                    activity?.finish()
                                    activity?.overridePendingTransition(0,R.anim.nav_default_pop_exit_anim)
                                }
                            }else{
                                db.collection("Attendance").document(dateTxt).set(attendanceDocDetails).addOnSuccessListener {
                                    Toast.makeText(activity,"Attendance Added",Toast.LENGTH_SHORT).show()
                                    activity?.finish()
                                    activity?.overridePendingTransition(0,R.anim.nav_default_pop_exit_anim)
                                }
                            }
                        }
                }
        }
    }

    private fun getStudentData(batchTxt: String,semesterTxt: String) {
        arrayListRecyclerViewTake.clear()
        db.collection("StudentInfo").whereEqualTo("Batch",batchTxt).whereEqualTo("Semester",semesterTxt).addSnapshotListener(object:
            EventListener<QuerySnapshot> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if(error != null){
                    Log.e("Firestore Error",error.message.toString())
                    return
                }
                for(dc : DocumentChange in value?.documentChanges!!){
                    if(dc.type == DocumentChange.Type.ADDED){
                        arrayListRecyclerViewTake.add(dc.document.toObject(TakeAttendanceListRecyclerViewDataClass::class.java))
                    }
                }
                recyclerViewAdapter.notifyDataSetChanged()
            }

        })
    }
}