package com.example.erp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.erp.R.id.profileFragmentScrollViewLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import org.w3c.dom.Text
import java.io.File


class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val viewModel : FragmentViewModel by activityViewModels()
    private lateinit var name : TextView
    private lateinit var srn : TextView
    private lateinit var email : TextView
    private lateinit var phone : TextView
    private lateinit var semester : TextView
    private lateinit var course : TextView
    private lateinit var birthdate : TextView
    private lateinit var logoutBut : MaterialButton
    private lateinit var gender : TextView
    private lateinit var editProfileBut : MaterialButton
    private lateinit var profileImg : ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view: View = inflater.inflate(R.layout.fragment_profile,container,false)
        logoutBut = view.findViewById(R.id.logoutBut)

        name = view.findViewById(R.id.tv_result_name)
        srn = view.findViewById(R.id.tv_result_srn)
        email = view.findViewById(R.id.tv_result_email)
        phone = view.findViewById(R.id.tv_result_phone)
        semester = view.findViewById(R.id.tv_result_semester)
        birthdate = view.findViewById(R.id.tv_result_birthday)
        course = view.findViewById(R.id.tv_result_course)
        gender = view.findViewById(R.id.tv_result_gender)
        editProfileBut = view.findViewById(R.id.editProfileBut)

        auth = Firebase.auth
        profileImg = view.findViewById(R.id.profileImage)

        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.profileFragmentSwipeRefresh)

        viewModel.nameData.observe(viewLifecycleOwner,{
            name.text = it
        })
        viewModel.srnData.observe(viewLifecycleOwner,{
            srn.text = it
        })
        viewModel.emailData.observe(viewLifecycleOwner,{
            email.text = it
        })
        viewModel.phoneData.observe(viewLifecycleOwner,{
            phone.text = it
        })
        viewModel.courseData.observe(viewLifecycleOwner,{
            course.text = it
        })
        viewModel.semesterData.observe(viewLifecycleOwner,{
            semester.text = it
        })
        viewModel.birthData.observe(viewLifecycleOwner,{
            birthdate.text = it
        })
        viewModel.genderData.observe(viewLifecycleOwner,{
            gender.text = it
        })
        viewModel.imageData.observe(viewLifecycleOwner, {
            profileImg.setImageBitmap(viewModel.imageData.value)
        })

        swipeRefresh.setOnRefreshListener {
            val clsActivity = activity as AfterLoginNavigation
            clsActivity.retrieveData()
            swipeRefresh.isRefreshing = false
        }

        editProfileBut.setOnClickListener {
            startActivity(Intent(activity, EditProfileActivity::class.java))
        }

        logoutBut.setOnClickListener {
            //FirebaseAuth.getInstance().signOut()
            startActivity(Intent(activity, Activity4Login::class.java))
            activity?.finish()
        }

        return view
    }

}