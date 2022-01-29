package com.app.erp.teacher_view

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
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
import com.google.firebase.firestore.*
import java.util.HashMap
import android.widget.Toast
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.ColorUtils
import com.app.erp.R
import com.app.erp.databinding.FragmentTakeAttendanceStudentListBinding
import com.app.erp.gloabal_functions.getColorFromAttribute

class FragmentTakeAttendanceStudentList : Fragment(R.layout.fragment_take_attendance_student_list) {
    private lateinit var binding: FragmentTakeAttendanceStudentListBinding

    private val db = FirebaseFirestore.getInstance()
    private val viewModel : ActivityAfterTeacherLoginNavigationTakeAttendanceViewModel by activityViewModels()
    private lateinit var recyclerViewAdapter: TakeAttendanceListRecyclerViewAdapter
    private lateinit var arrayListRecyclerViewTake: ArrayList<TakeAttendanceListRecyclerViewDataClass>

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        val view = inflater.inflate(R.layout.fragment_take_attendance_student_list, container, false)
        binding = FragmentTakeAttendanceStudentListBinding.inflate(inflater,container,false)

        binding.obstructorInfoLayout.setBackgroundColor(ColorUtils.setAlphaComponent(getColorFromAttribute(requireActivity(),R.attr.colorOnPrimary),240))

        teacherName = String()
        teacherCode = String()
        batchName = String()
        semesterName = String()
        courseName = String()
        courseCode = String()

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner,object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if(binding.obstructorInfoLayout.isVisible){
                        binding.obstructorInfoLayout.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.nav_default_pop_exit_anim))
                        binding.obstructorInfoLayout.visibility = View.GONE
                        binding.saveBut.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fab_scale_up))
                        binding.saveBut.visibility = View.VISIBLE
                        binding.infoBut.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fab_scale_up))
                        binding.infoBut.visibility = View.VISIBLE
                    }
                    else{
                        if (isEnabled) {
                            isEnabled = false
                            requireActivity().onBackPressed()
                        }
                    }
                }
            })

        viewModel.dateData.observe(viewLifecycleOwner,{
            date = it
            viewModel.timeData.observe(viewLifecycleOwner,{ itAlt ->
                time = itAlt
                binding.timeDate.setText(it.plus(", ").plus(itAlt),null)
            })
        })
        viewModel.teacherNameData.observe(viewLifecycleOwner,{
            teacherName = it
            viewModel.teacherCodeData.observe(viewLifecycleOwner,{ itAlt ->
                teacherCode = itAlt
                binding.teacherNameCode.setText(it.plus(" - ").plus(itAlt),null)
            })
        })
        viewModel.batchDataString.observe(viewLifecycleOwner,{
            batchName = it
            viewModel.semesterDataString.observe(viewLifecycleOwner,{ itAlt ->
                semesterName = itAlt
                getStudentData(it, itAlt)
                binding.batchSemester.setText(it.plus(" - ").plus(itAlt),null)
            })
        })
        viewModel.courseNameData.observe(viewLifecycleOwner,{
            courseName = it
            viewModel.courseCodeData.observe(viewLifecycleOwner,{ itAlt ->
                courseCode = itAlt
                binding.courseNameCode.setText(it.plus(" - ").plus(itAlt),null)
            })
        })

        binding.obstructorInfoLayout.setOnClickListener {
            binding.obstructorInfoLayout.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.nav_default_pop_exit_anim))
            binding.obstructorInfoLayout.visibility = View.GONE
            binding.saveBut.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fab_scale_up))
            binding.saveBut.visibility = View.VISIBLE
            binding.infoBut.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fab_scale_up))
            binding.infoBut.visibility = View.VISIBLE
        }
        binding.obstructorInfoLayoutCloseBut.setOnClickListener {
            binding.obstructorInfoLayout.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.nav_default_pop_exit_anim))
            binding.obstructorInfoLayout.visibility = View.GONE
            binding.saveBut.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fab_scale_up))
            binding.saveBut.visibility = View.VISIBLE
            binding.infoBut.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fab_scale_up))
            binding.infoBut.visibility = View.VISIBLE
        }
        binding.infoBut.setOnClickListener {
            binding.obstructorInfoLayout.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.nav_default_pop_enter_anim))
            binding.obstructorInfoLayout.visibility = View.VISIBLE
            binding.saveBut.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fab_scale_down))
            binding.saveBut.visibility = View.GONE
            binding.infoBut.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fab_scale_down))
            binding.infoBut.visibility = View.GONE
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.setHasFixedSize(true)

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(studentRecyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) binding.saveBut.shrink() else if (dy < 0) binding.saveBut.extend()
                if (dy > 0) binding.infoBut.hide() else if (dy < 0) binding.infoBut.show()
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

        binding.recyclerView.adapter = recyclerViewAdapter
        if(savedInstanceState != null){
            val savedPos: Parcelable? = savedInstanceState.getParcelable("RECYCLER_VIEW_SAVED_POS")
            (binding.recyclerView.layoutManager as LinearLayoutManager).onRestoreInstanceState(savedPos)
        }

        binding.saveBut.setOnClickListener {
            binding.obstructor.visibility = View.VISIBLE
            addAttendance()
            addAttendanceDetails()
        }

        return binding.root
    }

    private fun finishActivity() {
        val intent = Intent(requireActivity(),ActivityAfterTeacherLoginNavigation::class.java)
        intent.putExtra("ATTENDANCE_ADDED",true)
        activity?.setResult(RESULT_OK,intent)
        activity?.finish()
        activity?.overridePendingTransition(0, R.anim.nav_default_pop_exit_anim)
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
        binding.obstructor.visibility = View.GONE
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
                                    finishActivity()
                                }
                            }else{
                                val returnIntent = Intent()
                                returnIntent.putExtra("result", "your message")
                                db.collection("Attendance").document(dateTxt).set(attendanceDocDetails).addOnSuccessListener {
                                    finishActivity()
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
                        arrayListRecyclerViewTake.add(dc.document.toObject(
                            TakeAttendanceListRecyclerViewDataClass::class.java))
                    }
                }
                recyclerViewAdapter.notifyDataSetChanged()
            }

        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(
            "RECYCLER_VIEW_SAVED_POS",
            (binding.recyclerView.layoutManager as LinearLayoutManager).onSaveInstanceState()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.setObstructorState(binding.obstructorInfoLayout.isVisible)
        viewModel.setFabState(binding.saveBut.isExtended)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(viewModel.getFabState()){
            binding.saveBut.extend()
        }
        else if(!viewModel.getFabState()){
            binding.saveBut.shrink()
        }
        if(viewModel.getObstructorState()){
            binding.obstructorInfoLayout.visibility = View.VISIBLE
            binding.saveBut.visibility = View.GONE
            binding.infoBut.visibility = View.GONE
        }
        else if(!viewModel.getObstructorState()){
            binding.obstructorInfoLayout.visibility = View.GONE
            binding.saveBut.visibility = View.VISIBLE
            binding.infoBut.visibility = View.VISIBLE
        }
    }
}