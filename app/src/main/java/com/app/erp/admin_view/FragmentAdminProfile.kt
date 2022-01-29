package com.app.erp.admin_view

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.app.erp.gloabal_functions.InternetState
import com.app.erp.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.app.erp.FragmentUserPreferences
import com.app.erp.gloabal_functions.showToast

class FragmentAdminProfile : Fragment() {
    private lateinit var name: TextView
    private lateinit var code: TextView
    private lateinit var mail: TextView
    private lateinit var phone: TextView
    private lateinit var age: TextView
    private lateinit var gender: TextView
    private lateinit var birthday: TextView
    private lateinit var changePasswordBut: MaterialButton
    private lateinit var genderLayout: ImageView
    private lateinit var menuBut: ImageView
    private lateinit var scrollView: ScrollView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var parentFrameLayout: FrameLayout

    private val viewModel : ActivityAfterAdminLoginNavigationViewModel by activityViewModels()
    private lateinit var auth: FirebaseAuth

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
        genderLayout = view.findViewById(R.id.genderLayout)
        scrollView = view.findViewById(R.id.scrollView)
        menuBut = view.findViewById(R.id.menuBut)
        parentFrameLayout = view.findViewById(R.id.parentFrameLayout)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        menuBut.setOnClickListener {
            val fragmentUserPreferences = FragmentUserPreferences()
            val fragment : Fragment? =

                activity?.supportFragmentManager?.findFragmentByTag(fragmentUserPreferences::class.java.simpleName)

            if(fragment !is FragmentUserPreferences){

                activity?.supportFragmentManager?.beginTransaction()
                    ?.add(
                        R.id.secondaryFragmentContainer,fragmentUserPreferences,
                        FragmentUserPreferences::class.java.simpleName)
                    ?.addToBackStack(null)
                    ?.commit()
            }
        }

        auth = Firebase.auth

        swipeRefreshLayout.setOnRefreshListener {
            val clsActivity = activity as ActivityAfterAdminLoginNavigation
            Toast.makeText(activity,"${InternetState.internetIsConnected()}",Toast.LENGTH_SHORT).show()
            clsActivity.retrieveData()
            swipeRefreshLayout.isRefreshing = false
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
            val intent = Intent(activity, ActivityAfterAdminLoginNavigationPasswordChange::class.java)
            intent.putExtra("mailTxt",mail.text.toString())
            startActivity(intent)
        }

        return view
    }
}