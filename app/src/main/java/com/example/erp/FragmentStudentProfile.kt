package com.example.erp

import android.annotation.SuppressLint
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
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.roundToInt
import android.widget.RelativeLayout





class FragmentStudentProfile : Fragment() {

    private val viewModel : ActivityAfterStudentLoginNavigationViewModel by activityViewModels()
    private lateinit var name : TextView
    private lateinit var srn : TextView
    private lateinit var email : TextView
    private lateinit var phone : TextView
    private lateinit var semester : TextView
    private lateinit var batch : TextView
    private lateinit var birthdate : TextView
    private lateinit var logoutBut : MaterialButton
    private lateinit var gender : TextView
    private lateinit var genderLayout: ImageView
    private lateinit var age: TextView
    private lateinit var attendancePercentage: TextView
    private lateinit var attendedClasses: TextView
    private lateinit var nonAttendanceClasses: TextView
    private lateinit var changePasswordBut: MaterialButton
    private lateinit var secondaryFragmentContainer: RelativeLayout

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view: View = inflater.inflate(R.layout.fragment_student_profile,container,false)
        logoutBut = view.findViewById(R.id.logoutBut)

        name = view.findViewById(R.id.tv_result_name)
        srn = view.findViewById(R.id.tv_result_srn)
        email = view.findViewById(R.id.tv_result_email)
        phone = view.findViewById(R.id.tv_result_phone)
        semester = view.findViewById(R.id.tv_result_semester)
        birthdate = view.findViewById(R.id.tv_result_birthday)
        batch = view.findViewById(R.id.tv_result_batch)
        gender = view.findViewById(R.id.tv_result_gender)
        genderLayout = view.findViewById(R.id.genderLayout)
        age = view.findViewById(R.id.tv_result_age)
        attendancePercentage = view.findViewById(R.id.attendancePercentage)
        attendedClasses = view.findViewById(R.id.attendedClasses)
        nonAttendanceClasses = view.findViewById(R.id.nonAttendanceClasses)
        changePasswordBut = view.findViewById(R.id.changePasswordBut)
        secondaryFragmentContainer = view.findViewById(R.id.secondaryFragmentContainer)

        changePasswordBut.setOnClickListener {
            val intent = Intent(activity,ActivityAfterStudentLoginNavigationPasswordChange::class.java)
            intent.putExtra("srnTxt",srn.text.toString())
            startActivity(intent)
        }

        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.profileFragmentSwipeRefresh)
        age.text = viewModel.ageData.value.toString()
        viewModel.nameData.observe(viewLifecycleOwner,{
            name.text = it
        })
        viewModel.emailData.observe(viewLifecycleOwner,{
            email.text = it
        })
        viewModel.srnData.observe(viewLifecycleOwner,{
            srn.text = it
        })
        viewModel.ageData.observe(viewLifecycleOwner,{
            age.text = it.toString()
        })
        viewModel.batchData.observe(viewLifecycleOwner,{
            batch.text = it
        })
        viewModel.phoneData.observe(viewLifecycleOwner,{
            phone.text = it.toString()
        })
        viewModel.semesterData.observe(viewLifecycleOwner,{
            semester.text = it
        })
        viewModel.birthData.observe(viewLifecycleOwner,{
            birthdate.text = it
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
        viewModel.totalClassData.observe(viewLifecycleOwner,{
            viewModel.attendedClassData.observe(viewLifecycleOwner,{ itAlt ->
                val floatIt: Float = it.toFloat()
                val floatAltIt: Float = itAlt.toFloat()
                val temp: Float = floatAltIt/floatIt
                val percentage = ((temp*100).roundToInt())
                attendancePercentage.text = percentage.toString().plus("%")
                attendedClasses.text = it.toString()
            })
            viewModel.nonAttendedClassData.observe(viewLifecycleOwner,{itAltNon ->
                nonAttendanceClasses.text = itAltNon.toString()
            })
        })

        swipeRefresh.setOnRefreshListener {
            val clsActivity = activity as ActivityAfterStudentLoginNavigation

            clsActivity.retrieveData()
            swipeRefresh.isRefreshing = false
        }

        logoutBut.setOnClickListener {
            startActivity(Intent(activity, Activity4Login::class.java))
            activity?.finish()
        }

        return view
    }

}