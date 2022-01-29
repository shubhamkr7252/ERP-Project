package com.app.erp.admin_view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import com.app.erp.R
import com.app.erp.admin_view.admin_list_view.ActivityAdminList
import com.app.erp.admin_view.attendance_list_view.ActivityAttendanceList
import com.app.erp.admin_view.batch_list_view.ActivityBatchList
import com.app.erp.admin_view.course_list_view.ActivityCourseList
import com.app.erp.admin_view.student_list_view.ActivityStudentList
import com.app.erp.admin_view.teacher_list_view.ActivityTeacherList
import com.app.erp.databinding.FragmentAdminDashboardBinding
import com.google.android.material.card.MaterialCardView

class FragmentAdminDashboard : Fragment() {
    private lateinit var binding: FragmentAdminDashboardBinding
    private val viewModel : ActivityAfterAdminLoginNavigationViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminDashboardBinding.inflate(inflater,container,false)
        val obstructor = requireActivity().findViewById<RelativeLayout>(R.id.obstructor)
        val activityCall = activity as ActivityAfterAdminLoginNavigation

        binding.attendanceList.setOnClickListener {
            startActivity(Intent(requireContext(), ActivityAttendanceList::class.java))
        }
        binding.batchList.setOnClickListener {
            startActivity(Intent(requireContext(), ActivityBatchList::class.java))
        }
        binding.courseList.setOnClickListener {
            startActivity(Intent(requireContext(), ActivityCourseList::class.java))
        }
        viewModel.emailData.observe(viewLifecycleOwner,{ email ->
            binding.adminList.setOnClickListener {
                val intent = Intent(requireContext(), ActivityAdminList::class.java)
                intent.putExtra("mailTxt",email.toString())
                startActivity(intent)
            }
            binding.studentList.setOnClickListener {
                val intent = Intent(requireContext(), ActivityStudentList::class.java)
                intent.putExtra("mailTxt",email.toString())
                startActivity(intent)
//                activityCall.enterStudentListAnimation()
//                obstructor.setOnClickListener {
//                    activityCall.exitStudentListAnimation()
//                }
            }
            binding.teacherList.setOnClickListener {
                val intent = Intent(requireContext(), ActivityTeacherList::class.java)
                intent.putExtra("mailTxt",email.toString())
                startActivity(intent)
            }
        })

        return binding.root
    }


}