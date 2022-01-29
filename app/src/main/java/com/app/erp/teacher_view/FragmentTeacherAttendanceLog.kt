package com.app.erp.teacher_view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.adapters.ViewGroupBindingAdapter.setListener
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.erp.R
import com.app.erp.admin_view.batch_list_view.FragmentAddBatch
import com.app.erp.databinding.FragmentTeacherAttendanceLogBinding
import com.app.erp.gloabal_functions.getColorFromAttribute
import com.app.erp.gloabal_functions.showToast
import com.google.firebase.firestore.*

class FragmentTeacherAttendanceLog : Fragment() {
    private val recyclerViewAdapter by lazy { FragmentTeacherAttendanceLogRecyclerViewAdapter(teacherAttendanceArrayListRecyclerView) }
    private val viewModel : ActivityAfterTeacherLoginNavigationViewModel by activityViewModels()
    private lateinit var binding: FragmentTeacherAttendanceLogBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var teacherAttendanceArrayListRecyclerView: ArrayList<FragmentTeacherAttendanceLogRecyclerViewDataClass>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTeacherAttendanceLogBinding.inflate(inflater,container,false)

        teacherAttendanceArrayListRecyclerView = arrayListOf<FragmentTeacherAttendanceLogRecyclerViewDataClass>()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        binding.recyclerView.setHasFixedSize(true)

        binding.swipeRefreshLayout.setOnRefreshListener {
            getAttendanceData()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.recyclerView.adapter = recyclerViewAdapter
        recyclerViewAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        getAttendanceData()

        binding.search.setOnClickListener {
            val fragmentTeacherAttendanceLogSearch = FragmentTeacherAttendanceLogSearch()
            val fragment : Fragment? =

                activity?.supportFragmentManager?.findFragmentByTag(fragmentTeacherAttendanceLogSearch::class.java.simpleName)

            if(fragment !is FragmentTeacherAttendanceLogSearch){
                activity?.supportFragmentManager?.beginTransaction()
                    ?.add(
                        R.id.secondaryFragmentContainerTeacher,fragmentTeacherAttendanceLogSearch,
                        FragmentTeacherAttendanceLogSearch::class.java.simpleName)
                    ?.addToBackStack(null)
                    ?.commit()
            }
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy < 0)
                    binding.fab.extend()
                else if (dy > 0)
                    binding.fab.shrink()
            }
        })

//        binding.fab.setOnClickListener {
//            startAttendanceTakingActivity()
//        }

        return binding.root
    }

//    private fun startAttendanceTakingActivity() {
//        val intent = Intent(activity, ActivityAfterTeacherLoginNavigationTakeAttendance::class.java)
//        intent.putExtra("teacherTxt",viewModel.teacherNameData.value.toString())
//        intent.putExtra("teacherCodeTxt",viewModel.teacherCodeData.value.toString())
//        intent.putExtra("batchTxt", viewModel.batchData.value)
//        intent.putExtra("courseTxt",viewModel.courseNameData.value)
//        intent.putExtra("courseCodeTxt",viewModel.courseData.value)
//        (activity as ActivityAfterTeacherLoginNavigation).launchSomeActivity.launch(intent)
//    }

    private fun getAttendanceData() {
        val teacherData = viewModel.teacherCodeData.value.toString()

        db.collection("Attendance").get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    var dateTxt: String
                    val tempTxt = ArrayList<String>()
                    for(doc in it.result!!){
                        tempTxt.clear()
                        dateTxt = doc.data.getValue("Date").toString()
                        tempTxt.addAll(doc.data.getValue("Time") as Collection<String>)
                        for (i in tempTxt){
                            teacherAttendanceArrayListRecyclerView.clear()

                            db.collection("Attendance").document(dateTxt).collection(i).whereEqualTo("TeacherCode",
                                teacherData).addSnapshotListener(object:
                                EventListener<QuerySnapshot> {
                                @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
                                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                                    if(error != null){
                                        Toast.makeText(requireActivity(),"Firestore Error ${error.message.toString()}",
                                            Toast.LENGTH_LONG).show()
                                        return
                                    }
                                    for(dc : DocumentChange in value?.documentChanges!!){
                                        if(dc.type == DocumentChange.Type.ADDED){
                                            teacherAttendanceArrayListRecyclerView.add(dc.document.toObject(FragmentTeacherAttendanceLogRecyclerViewDataClass::class.java))
                                        }
                                    }
                                    recyclerViewAdapter.notifyDataSetChanged()
                                    viewModel.setTeacherAttendanceData(teacherAttendanceArrayListRecyclerView)
                                }
                            })
                        }

                    }
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.setFabState(binding.fab.isExtended)
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.getAttendanceAdded()){
            getAttendanceData()
        }
        if(!viewModel.getFabState()){
            binding.fab.shrink()
        }
        else{
            binding.fab.extend()
        }
    }

}