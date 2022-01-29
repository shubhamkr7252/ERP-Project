package com.app.erp.teacher_view

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
import com.app.erp.Activity4Login
import com.app.erp.FragmentUserPreferences
import com.app.erp.R
import com.app.erp.databinding.FragmentTeacherAttendanceLogBinding
import com.app.erp.databinding.FragmentTeacherProfileBinding
import com.app.erp.gloabal_functions.getColorFromAttribute
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jakewharton.processphoenix.ProcessPhoenix

class FragmentTeacherProfile : Fragment() {
    private val viewModel : ActivityAfterTeacherLoginNavigationViewModel by activityViewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentTeacherProfileBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTeacherProfileBinding.inflate(inflater,container,false)

        auth = Firebase.auth

        binding.menuBut.setOnClickListener {
            val fragmentUserPreferences = FragmentUserPreferences()
            val fragment : Fragment? =

                activity?.supportFragmentManager?.findFragmentByTag(fragmentUserPreferences::class.java.simpleName)

            if(fragment !is FragmentUserPreferences){

                activity?.supportFragmentManager?.beginTransaction()
                    ?.add(
                        R.id.secondaryFragmentContainerTeacher,fragmentUserPreferences,
                        FragmentUserPreferences::class.java.simpleName)
                    ?.addToBackStack(null)
                    ?.commit()
            }
        }

        binding.changePasswordBut.setOnClickListener {
            val intent = Intent(activity, ActivityAfterTeacherLoginNavigationPasswordChange::class.java)
            intent.putExtra("teacherCodeTxt",binding.code.text.toString())
            startActivity(intent)
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            (activity as ActivityAfterTeacherLoginNavigation).loadTeacherInfo()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.teacherNameData.observe(viewLifecycleOwner,{
            binding.name.text = it
        })
        viewModel.teacherCodeData.observe(viewLifecycleOwner,{
            binding.code.text = it
        })
        viewModel.teacherEmailData.observe(viewLifecycleOwner,{
            binding.mail.text = it
        })
        viewModel.teacherPhoneData.observe(viewLifecycleOwner,{
            binding.phone.text = it.toString()
        })
        viewModel.teacherAgeData.observe(viewLifecycleOwner,{
            binding.age.text = it.toString()
        })
        viewModel.teacherBirthdayData.observe(viewLifecycleOwner,{
            binding.birthday.text = it
        })
        viewModel.teacherGenderData.observe(viewLifecycleOwner,{
            binding.gender.text = it
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
        viewModel.courseNameData.observe(viewLifecycleOwner,{
            val temp = it.toString()
            binding.subject.text = temp.substring(1,temp.length-1)
        })

        return binding.root
    }

}