package com.app.erp.student_view

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.erp.databinding.FragmentStudentAttendanceLogBinding
import kotlinx.android.synthetic.main.fragment_admin_profile.*
import android.os.Parcelable


class FragmentStudentAttendanceLog : Fragment() {
    private val viewModel : ActivityAfterStudentLoginNavigationViewModel by activityViewModels()
    private lateinit var binding: FragmentStudentAttendanceLogBinding

    private lateinit var recyclerViewAdapter: FragmentStudentAttendanceLogRecyclerViewAdapter

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentStudentAttendanceLogBinding.inflate(inflater,container,false)

        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = linearLayoutManager
        binding.recyclerView.setHasFixedSize(true)

        binding.swipeRefreshLayout.setOnRefreshListener {
            (activity as ActivityAfterStudentLoginNavigation).getAttendanceData()
            swipeRefreshLayout.isRefreshing = false
        }

        viewModel.studentAttendanceArrayListData.observe(viewLifecycleOwner,{ attendanceList ->
            viewModel.srnData.observe(viewLifecycleOwner,{ srnData ->
                recyclerViewAdapter = FragmentStudentAttendanceLogRecyclerViewAdapter(attendanceList, requireContext(), srn = srnData)
                binding.recyclerView.adapter = recyclerViewAdapter
                if (savedInstanceState != null){
                    val savedPos: Parcelable? = savedInstanceState.getParcelable("RECYCLER_VIEW_SAVED_POS")
                    (binding.recyclerView.layoutManager as LinearLayoutManager).onRestoreInstanceState(savedPos)
                }
            })
        })

        viewModel.totalClassData.observe(viewLifecycleOwner,{
        viewModel.attendedClassData.observe(viewLifecycleOwner,{ itAlt ->
            val floatIt: Float = it.toFloat()
            val floatAltIt: Float = itAlt.toFloat()
            val percentage: Float = (floatAltIt/floatIt)*100
            binding.progressBar.progress = percentage.toInt()
            when {
                percentage.toString() == "NaN" -> {
                    binding.percentageText.text = "Attendance Percentage: 0%"
                }
                percentage.toString().contains(".0") -> {
                    binding.percentageText.text = String.format("Attendance Percentage: %d",percentage.toInt()) + "%"
                }
                else -> {
                    binding.percentageText.text = String.format("Attendance Percentage: %.1f",percentage) + "%"
                }
            }
        })
    })

        return binding.root
    }

}