package com.app.erp.student_view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.activityViewModels
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import android.widget.RelativeLayout
import com.app.erp.Activity4Login
import com.app.erp.FragmentUserPreferences
import com.app.erp.R
import com.app.erp.databinding.FragmentStudentProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jakewharton.processphoenix.ProcessPhoenix

class FragmentStudentProfile : Fragment() {
    private val viewModel : ActivityAfterStudentLoginNavigationViewModel by activityViewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentStudentProfileBinding

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStudentProfileBinding.inflate(inflater,container,false)
        auth = Firebase.auth

        binding.menuBut.setOnClickListener {
            val fragmentUserPreferences = FragmentUserPreferences()
            val fragment : Fragment? =

                activity?.supportFragmentManager?.findFragmentByTag(fragmentUserPreferences::class.java.simpleName)

            if(fragment !is FragmentUserPreferences){

                activity?.supportFragmentManager?.beginTransaction()
                    ?.add(
                        R.id.secondaryFragmentContainerStudent,fragmentUserPreferences,
                        FragmentUserPreferences::class.java.simpleName)
                    ?.addToBackStack(null)
                    ?.commit()
            }
        }

        binding.changePasswordBut.setOnClickListener {
            val intent = Intent(activity, ActivityAfterStudentLoginNavigationPasswordChange::class.java)
            intent.putExtra("srnTxt",binding.tvResultSrn.text.toString())
            startActivity(intent)
        }

        viewModel.nameData.observe(viewLifecycleOwner,{
            binding.tvResultName.text = it
        })
        viewModel.emailData.observe(viewLifecycleOwner,{
            binding.tvResultEmail.text = it
        })
        viewModel.srnData.observe(viewLifecycleOwner,{
            binding.tvResultSrn.text = it
        })
        viewModel.ageData.observe(viewLifecycleOwner,{
            binding.tvResultAge.text = it.toString()
        })
        viewModel.batchData.observe(viewLifecycleOwner,{
            binding.tvResultBatch.text = it
        })
        viewModel.phoneData.observe(viewLifecycleOwner,{
            binding.tvResultPhone.text = it.toString()
        })
        viewModel.semesterData.observe(viewLifecycleOwner,{
            binding.tvResultSemester.text = it
        })
        viewModel.birthData.observe(viewLifecycleOwner,{
            binding.tvResultBirthday.text = it
        })
        viewModel.genderData.observe(viewLifecycleOwner,{
            binding.tvResultGender.text = it
            when {
                it.toString() == "Female" -> {
                    binding.genderLayout.visibility = View.VISIBLE
                    binding.genderLayout.setImageResource(R.drawable.ic_baseline_female_24)
                }
                it.toString() == "Male" -> {
                    binding.genderLayout.visibility = View.VISIBLE
                    binding.genderLayout.setImageResource(R.drawable.ic_baseline_male_24)
                }
                else -> {
                    binding.genderLayout.visibility = View.INVISIBLE
                }
            }
        })
        viewModel.totalClassData.observe(viewLifecycleOwner,{
            viewModel.attendedClassData.observe(viewLifecycleOwner,{ itAlt ->
                val floatIt: Float = it.toFloat()
                val floatAltIt: Float = itAlt.toFloat()
                val percentage: Float = (floatAltIt/floatIt)*100
                when {
                    percentage.toString() == "NaN" -> {
                        binding.attendancePercentage.text = "0%"
                    }
                    percentage.toString().contains(".0") -> {
                        binding.attendancePercentage.text = percentage.toInt().toString().plus("%")
                    }
                    else -> {
                        binding.attendancePercentage.text = String.format("%.1f",percentage) + "%"
                    }
                }
                binding.attendedClasses.text = it.toString()
            })
            viewModel.nonAttendedClassData.observe(viewLifecycleOwner,{itAltNon ->
                binding.nonAttendanceClasses.text = itAltNon.toString()
            })
        })

        binding.swipeRefreshLayout.setOnRefreshListener {
            val clsActivity = activity as ActivityAfterStudentLoginNavigation

            clsActivity.retrieveData()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        return binding.root
    }
}