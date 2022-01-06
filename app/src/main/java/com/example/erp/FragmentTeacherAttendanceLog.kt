package com.example.erp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.firestore.*

class FragmentTeacherAttendanceLog : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: FragmentTeacherAttendanceLogRecyclerViewAdapter
    private lateinit var studentAttendanceArrayListRecyclerView: ArrayList<FragmentTeacherAttendanceLogRecyclerViewDataClass>
    private val viewModel : ActivityAfterTeacherLoginNavigationViewModel by activityViewModels()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var addAttendanceBut: ExtendedFloatingActionButton
    private lateinit var fragmentContainer: RelativeLayout
    private val db = FirebaseFirestore.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_teacher_attendance_log, container, false)
        addAttendanceBut = view.findViewById(R.id.addAttendanceBut)
        fragmentContainer = view.findViewById(R.id.fragmentContainer)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        recyclerView = view.findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        studentAttendanceArrayListRecyclerView = arrayListOf<FragmentTeacherAttendanceLogRecyclerViewDataClass>()

        swipeRefreshLayout.setOnRefreshListener {
            getAttendanceData()
        }

        getAttendanceData()

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(studentRecyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) addAttendanceBut.hide() else if (dy < 0) addAttendanceBut.show()
            }
        })

        addAttendanceBut.setOnClickListener {
            val intent = Intent(activity,ActivityAfterTeacherLoginNavigationTakeAttendance::class.java)
            intent.putExtra("teacherTxt",viewModel.teacherNameData.value.toString())
            intent.putExtra("teacherCodeTxt",viewModel.teacherCodeData.value.toString())
            intent.putExtra("batchTxt", viewModel.batchData.value)
            intent.putExtra("courseTxt",viewModel.courseNameData.value)
            intent.putExtra("courseCodeTxt",viewModel.courseData.value)
            startActivity(intent)
        }
        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getAttendanceData() {
        studentAttendanceArrayListRecyclerView.clear()
        viewModel.teacherCodeData.observe(viewLifecycleOwner, { teacherData ->
            studentAttendanceArrayListRecyclerView.clear()
            recyclerViewAdapter = FragmentTeacherAttendanceLogRecyclerViewAdapter(studentAttendanceArrayListRecyclerView,requireContext())
            recyclerView.adapter = recyclerViewAdapter

            db.collection("Attendance").get()
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        var dateTxt = String()
                        val tempTxt = ArrayList<String>()
                        for(doc in it.result!!){
                            tempTxt.clear()
                            dateTxt = doc.data.getValue("Date").toString()
                            tempTxt.addAll(doc.data.getValue("Time") as Collection<String>)
                            for (i in tempTxt){
                                db.collection("Attendance").document(dateTxt).collection(i).whereEqualTo("TeacherCode",
                                    teacherData).addSnapshotListener(object:
                                    EventListener<QuerySnapshot> {
                                    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
                                    override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                                        if(error != null){
                                            Toast.makeText(activity,"Firestore Error ${error.message.toString()}",
                                                Toast.LENGTH_LONG).show()
                                            return
                                        }
                                        for(dc : DocumentChange in value?.documentChanges!!){
                                            val temp = ArrayList<String>()
                                            if(dc.type == DocumentChange.Type.ADDED){
                                                studentAttendanceArrayListRecyclerView.add(dc.document.toObject(FragmentTeacherAttendanceLogRecyclerViewDataClass::class.java))
                                            }
                                        }
                                        swipeRefreshLayout.isRefreshing = false
                                        recyclerViewAdapter.notifyDataSetChanged()
                                    }

                                })
                            }
                        }
                    }
                }
            swipeRefreshLayout.isRefreshing = false
        })
    }
}