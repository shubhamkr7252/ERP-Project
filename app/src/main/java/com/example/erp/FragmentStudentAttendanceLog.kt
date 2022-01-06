package com.example.erp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.firestore.*
import kotlin.math.roundToInt

class FragmentStudentAttendanceLog : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private val viewModel : ActivityAfterStudentLoginNavigationViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: FragmentStudentAttendanceLogRecyclerViewAdapter
    private lateinit var studentAttendanceArrayListRecyclerView: ArrayList<FragmentStudentAttendanceLogRecyclerViewDataClass>
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var percentageText: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_student_attendance_log,container,false)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        percentageText = view.findViewById(R.id.percentageText)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        studentAttendanceArrayListRecyclerView = arrayListOf<FragmentStudentAttendanceLogRecyclerViewDataClass>()

        swipeRefreshLayout.setOnRefreshListener {
            getAttendanceData()
        }

        getAttendanceData()

        viewModel.totalClassData.observe(viewLifecycleOwner,{
            viewModel.attendedClassData.observe(viewLifecycleOwner,{ itAlt ->
                val floatIt: Float = it.toFloat()
                val floatAltIt: Float = itAlt.toFloat()
                val temp: Float = floatAltIt/floatIt
                val percentage = ((temp*100).roundToInt())
                progressBar.progress = percentage
                percentageText.text = "Attendance Percentage: $percentage%"
            })
        })

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getAttendanceData() {
        studentAttendanceArrayListRecyclerView.clear()
        viewModel.srnData.observe(viewLifecycleOwner,{ srnData ->
            studentAttendanceArrayListRecyclerView.clear()
            recyclerViewAdapter = FragmentStudentAttendanceLogRecyclerViewAdapter(studentAttendanceArrayListRecyclerView,requireContext(), srn = srnData)
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

                                db.collection("Attendance").document(dateTxt).collection(i).whereArrayContainsAny("StudentPresent",
                                    listOf(srnData)).addSnapshotListener(object:
                                    EventListener<QuerySnapshot> {
                                    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
                                    override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                                        if(error != null){
                                            Toast.makeText(activity,"Firestore Error ${error.message.toString()}",Toast.LENGTH_LONG).show()
                                            return
                                        }
                                        for(dc : DocumentChange in value?.documentChanges!!){
                                            if(dc.type == DocumentChange.Type.ADDED){
                                                studentAttendanceArrayListRecyclerView.add(dc.document.toObject(FragmentStudentAttendanceLogRecyclerViewDataClass::class.java))
                                            }
                                        }
                                        swipeRefreshLayout.isRefreshing = false
                                        recyclerViewAdapter.notifyDataSetChanged()
                                    }

                                })
                                db.collection("Attendance").document(dateTxt).collection(i).whereArrayContainsAny("StudentAbsent",
                                    listOf(srnData)).addSnapshotListener(object:
                                    EventListener<QuerySnapshot> {
                                    @SuppressLint("NotifyDataSetChanged")
                                    override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                                        if(error != null){
                                            Toast.makeText(activity,"Firestore Error ${error.message.toString()}",Toast.LENGTH_LONG).show()
                                            return
                                        }
                                        for(dc : DocumentChange in value?.documentChanges!!){
                                            if(dc.type == DocumentChange.Type.ADDED){
                                                studentAttendanceArrayListRecyclerView.add(dc.document.toObject(FragmentStudentAttendanceLogRecyclerViewDataClass::class.java))
                                            }
                                        }
                                        swipeRefreshLayout.isRefreshing = false
                                        recyclerViewAdapter.notifyDataSetChanged()

                                    }

                                })
                            }
                            recyclerViewAdapter.notifyDataSetChanged()
                        }
                    }
                }
            swipeRefreshLayout.isRefreshing = false
        })
    }
}