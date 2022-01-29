package com.app.erp.teacher_view

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.icu.lang.UProperty
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.erp.R
import com.app.erp.databinding.FragmentTeacherAttendanceLogSearchBinding
import com.app.erp.gloabal_functions.capitalizeWords
import com.app.erp.gloabal_functions.getColorFromAttribute
import android.text.style.ForegroundColorSpan

import android.text.SpannableString
import android.widget.TextView.BufferType

import android.text.SpannableStringBuilder
import android.widget.TextView

import android.text.Spannable

import android.icu.lang.UProperty.INT_START
import android.text.style.StyleSpan
import android.text.Spanned

import android.text.style.StrikethroughSpan

import android.R.string

class FragmentTeacherAttendanceLogSearch : Fragment() {
    private lateinit var recyclerViewAdapter: FragmentTeacherAttendanceLogSearchRecyclerViewAdapter
    private lateinit var arrayList: ArrayList<FragmentTeacherAttendanceLogRecyclerViewDataClass>
    private lateinit var binding: FragmentTeacherAttendanceLogSearchBinding
    private val viewModel : ActivityAfterTeacherLoginNavigationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTeacherAttendanceLogSearchBinding.inflate(inflater,container,false)

        arrayList = arrayListOf<FragmentTeacherAttendanceLogRecyclerViewDataClass>()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        binding.recyclerView.setHasFixedSize(true)

        recyclerViewAdapter = FragmentTeacherAttendanceLogSearchRecyclerViewAdapter(arrayList, requireContext())
        binding.recyclerView.adapter = recyclerViewAdapter
        recyclerViewAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        binding.tvSearchResult.setText(returnSearchText(), BufferType.SPANNABLE)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                TODO()
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextChange(text: String?): Boolean {
                arrayList.clear()
                if(text!!.isBlank()){
                    viewModel.setQueryState(false)
                    arrayList.clear()
                    binding.tvSearchResult.setText(returnSearchText(), BufferType.SPANNABLE)
                    recyclerViewAdapter.notifyDataSetChanged()
                }
                else if(text.isNotBlank()){
                    viewModel.setQueryState(true)
                    viewModel.getTeacherAttendanceData().forEach {
                        if(it.Date!!.contains(text.toString())){
                            arrayList.add(it)
                        }
                        if(it.Course!!.contains(capitalizeWords(text.toString()).toString())){
                            arrayList.add(it)
                        }
                        if(it.Batch!!.contains(capitalizeWords(text.toString()).toString())){
                            arrayList.add(it)
                        }
                    }
                    if (text.isNotBlank() && arrayList.isEmpty()){
                        binding.tvSearchResult.setText(returnSearchNotFoundText(),BufferType.SPANNABLE)
                    }
                    recyclerViewAdapter.notifyDataSetChanged()
                }
                return false
            }

        })

        return binding.root
    }

    private fun returnSearchText(): SpannableStringBuilder {
        val builder = SpannableStringBuilder()

        val str1 = "Search using "
        val str1Spannable = SpannableString(str1)
        builder.append(str1Spannable)

        val str2 = "Date"
        val str2Spannable = SpannableString(str2)
        str2Spannable.setSpan(StyleSpan(Typeface.BOLD),0,str2.length,0)
        str2Spannable.setSpan(ForegroundColorSpan(getColorFromAttribute(requireContext(),R.attr.colorPrimary)), 0, str2.length, 0)
        builder.append(str2Spannable)

        val str3 = "\nor\n"
        val str3Spannable = SpannableString(str3)
        builder.append(str3Spannable)

        val str4 = "Course/Batch Name"
        val str4Spannable = SpannableString(str4)
        str4Spannable.setSpan(StyleSpan(Typeface.BOLD),0,str4.length,0)
        str4Spannable.setSpan(ForegroundColorSpan(getColorFromAttribute(requireContext(),R.attr.colorPrimary)), 0, str4.length, 0)
        builder.append(str4Spannable)

        return builder
    }

    private fun returnSearchNotFoundText(): SpannableStringBuilder {
        val builder = SpannableStringBuilder()

        val str1 = "No"
        val str1Spannable = SpannableString(str1)
        builder.append(str1Spannable)

        val str2 = " result "
        val str2Spannable = SpannableString(str2)
        str2Spannable.setSpan(StyleSpan(Typeface.BOLD),0,str2.length,0)
        str2Spannable.setSpan(StrikethroughSpan(), 1, str2.length-1, 0)
        str2Spannable.setSpan(ForegroundColorSpan(getColorFromAttribute(requireContext(),R.attr.colorError)), 0, str2.length, 0)
        builder.append(str2Spannable)

        val str3 = "found"
        val str3Spannable = SpannableString(str3)
        builder.append(str3Spannable)

        return builder
    }

}