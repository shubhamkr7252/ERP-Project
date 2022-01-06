package com.example.erp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.activityViewModels
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.*

class FragmentAdminDashboard : Fragment() {
    private lateinit var studentList: MaterialCardView
    private lateinit var teacherList: MaterialCardView
    private lateinit var batchList: MaterialCardView
    private lateinit var courseList: MaterialCardView
    private lateinit var adminList: MaterialCardView
    private lateinit var attendanceList: MaterialCardView
    private val viewModel : ActivityAfterAdminLoginNavigationViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false)
        studentList = view.findViewById(R.id.studentList)
        teacherList = view.findViewById(R.id.teacherList)
        batchList = view.findViewById(R.id.batchList)
        courseList = view.findViewById(R.id.courseList)
        adminList = view.findViewById(R.id.adminList)
        attendanceList = view.findViewById(R.id.attendanceList)

        studentList.setOnClickListener {
            startActivity(Intent(requireContext(),ActivityStudentList::class.java))
        }
        teacherList.setOnClickListener {
            startActivity(Intent(requireContext(),ActivityTeacherList::class.java))
        }
        attendanceList.setOnClickListener {
            startActivity(Intent(requireContext(),ActivityAttendanceList::class.java))
        }
        batchList.setOnClickListener {
            startActivity(Intent(requireContext(),ActivityBatchList::class.java))
        }
        courseList.setOnClickListener {
            startActivity(Intent(requireContext(),ActivityCourseList::class.java))
        }
        viewModel.emailData.observe(viewLifecycleOwner,{
            val temp = it.toString()
            adminList.setOnClickListener {
                val intent = Intent(requireContext(),ActivityAdminList::class.java)
                intent.putExtra("mailTxt",temp)
                startActivity(intent)
            }
        })

        return view
    }
}