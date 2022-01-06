package com.example.erp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.activityViewModels
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class FragmentTeacherProfile : Fragment() {
    private val viewModel : ActivityAfterTeacherLoginNavigationViewModel by activityViewModels()
    private lateinit var name: TextView
    private lateinit var code: TextView
    private lateinit var mail: TextView
    private lateinit var phone: TextView
    private lateinit var gender: TextView
    private lateinit var subject: TextView
    private lateinit var age: TextView
    private lateinit var birthday: TextView
    private lateinit var logoutBut: MaterialButton
    private lateinit var changePasswordBut: MaterialButton
    private lateinit var genderLayout: ImageView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var fragmentContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_teacher_profile, container, false)
        name = view.findViewById(R.id.name)
        code = view.findViewById(R.id.code)
        mail = view.findViewById(R.id.mail)
        phone = view.findViewById(R.id.phone)
        gender = view.findViewById(R.id.gender)
        subject = view.findViewById(R.id.subject)
        age = view.findViewById(R.id.age)
        birthday = view.findViewById(R.id.birthday)
        logoutBut = view.findViewById(R.id.logoutBut)
        genderLayout = view.findViewById(R.id.genderLayout)
        changePasswordBut = view.findViewById(R.id.changePasswordBut)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        fragmentContainer = activity?.findViewById(R.id.linearLayout)!!

        logoutBut.setOnClickListener {
            startActivity(Intent(activity,Activity4Login::class.java))
            activity?.finish()
        }

        changePasswordBut.setOnClickListener {
            val intent = Intent(activity,ActivityAfterTeacherLoginNavigationPasswordChange::class.java)
            intent.putExtra("teacherCodeTxt",code.text.toString())
            startActivity(intent)
        }

        swipeRefreshLayout.setOnRefreshListener {
            (activity as ActivityAfterTeacherLoginNavigation).loadTeacherInfo()
            swipeRefreshLayout.isRefreshing = false
        }

        viewModel.teacherNameData.observe(viewLifecycleOwner,{
            name.text = it
        })
        viewModel.teacherCodeData.observe(viewLifecycleOwner,{
            code.text = it
        })
        viewModel.teacherEmailData.observe(viewLifecycleOwner,{
            mail.text = it
        })
        viewModel.teacherPhoneData.observe(viewLifecycleOwner,{
            phone.text = it.toString()
        })
        viewModel.teacherAgeData.observe(viewLifecycleOwner,{
            age.text = it.toString()
        })
        viewModel.teacherBirthdayData.observe(viewLifecycleOwner,{
            birthday.text = it
        })
        viewModel.teacherGenderData.observe(viewLifecycleOwner,{
            gender.text = it
            when {
                it.toString() == "Female" -> {
                    genderLayout.visibility = View.VISIBLE
                    genderLayout.setImageResource(R.drawable.ic_baseline_female_24)
                }
                it.toString() == "Male" -> {
                    genderLayout.visibility = View.VISIBLE
                    genderLayout.setImageResource(R.drawable.ic_baseline_male_24)
                }
                else -> {
                    genderLayout.visibility = View.INVISIBLE
                }
            }
        })
        viewModel.courseNameData.observe(viewLifecycleOwner,{
            val temp = it.toString()
            subject.text = temp.substring(1,temp.length-1)
        })
        return view
    }

}