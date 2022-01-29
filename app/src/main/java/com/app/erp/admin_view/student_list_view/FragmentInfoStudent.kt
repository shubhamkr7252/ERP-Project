package com.app.erp.admin_view.student_list_view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.erp.R
import com.app.erp.databinding.FragmentInfoStudentBinding

class FragmentInfoStudent : Fragment() {
    private lateinit var binding: FragmentInfoStudentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInfoStudentBinding.inflate(inflater, container, false)

        return binding.root
    }

}