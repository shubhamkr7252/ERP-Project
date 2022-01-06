package com.example.erp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.log

class FragmentAdminProfile : Fragment() {
    private lateinit var name: TextView
    private lateinit var code: TextView
    private lateinit var mail: TextView
    private lateinit var phone: TextView
    private lateinit var age: TextView
    private lateinit var gender: TextView
    private lateinit var birthday: TextView
    private lateinit var changePasswordBut: MaterialButton
    private lateinit var logoutBut: MaterialButton
    private lateinit var genderLayout: ImageView

    private val viewModel : ActivityAfterAdminLoginNavigationViewModel by activityViewModels()
    private val db = FirebaseFirestore.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_profile, container, false)
        name = view.findViewById(R.id.name)
        code = view.findViewById(R.id.code)
        mail = view.findViewById(R.id.mail)
        phone = view.findViewById(R.id.phone)
        age = view.findViewById(R.id.age)
        gender = view.findViewById(R.id.gender)
        birthday = view.findViewById(R.id.birthday)
        changePasswordBut = view.findViewById(R.id.changePasswordBut)
        logoutBut = view.findViewById(R.id.logoutBut)
        genderLayout = view.findViewById(R.id.genderLayout)

        logoutBut.setOnClickListener {
            startActivity(Intent(activity,Activity4Login::class.java))
            activity?.finish()
        }

        viewModel.nameData.observe(viewLifecycleOwner,{
            name.text = it
        })
        viewModel.ageData.observe(viewLifecycleOwner,{
            age.text = it.toString()
        })
        viewModel.genderData.observe(viewLifecycleOwner,{
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
        viewModel.emailData.observe(viewLifecycleOwner,{
            mail.text = it
        })
        viewModel.codeData.observe(viewLifecycleOwner,{
            code.text = it
        })
        viewModel.birthdayData.observe(viewLifecycleOwner,{
            birthday.text = it
        })
        viewModel.phoneData.observe(viewLifecycleOwner,{
            phone.text = it.toString()
        })

        changePasswordBut.setOnClickListener {
            val intent = Intent(activity,ActivityAfterAdminLoginNavigationPasswordChange::class.java)
            intent.putExtra("mailTxt",mail.text.toString())
            startActivity(intent)
        }

        return view
    }

}