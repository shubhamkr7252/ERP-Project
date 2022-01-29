package com.app.erp.admin_view.course_list_view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.activityViewModels
import com.app.erp.R
import com.app.erp.gloabal_functions.getColorFromAttribute
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FragmentRemoveCourse : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var removeCourseBackButton: MaterialButton
    private lateinit var removeCourseCode: AutoCompleteTextView
    private lateinit var confirmRemoveCourseBut: MaterialButton
    private lateinit var parentFrameLayout: FrameLayout
    private lateinit var parentParentFrameLayout: FrameLayout
    private lateinit var progressBar: RelativeLayout
    private lateinit var courseName: EditText
    private lateinit var codeLayout: TextInputLayout
    private lateinit var nameLayout: TextInputLayout
    private val viewModel : ActivityCourseListViewModel by activityViewModels()

override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
    val view: View = inflater.inflate(R.layout.fragment_remove_course,container,false)
    removeCourseCode = view.findViewById(R.id.removeCourseCode)
    removeCourseBackButton = view.findViewById(R.id.removeCourseBackButton)
    confirmRemoveCourseBut = view.findViewById(R.id.confirmRemoveCourseBut)
    parentFrameLayout = view.findViewById(R.id.parentFrameLayout)
    progressBar = view.findViewById(R.id.progressBar)
    courseName = view.findViewById(R.id.courseName)
    codeLayout = view.findViewById(R.id.codeLayout)
    nameLayout = view.findViewById(R.id.nameLayout)
    parentParentFrameLayout = activity?.findViewById(R.id.parentFrameLayout)!!

    removeCourseBackButton.setOnClickListener {
        activity?.onBackPressed()
    }

    viewModel.courseCodeData.observe(viewLifecycleOwner,{
        val arrayAdapter = ArrayAdapter(requireContext(),
            R.layout.exposed_dropdown_menu_item_layout,it)
        removeCourseCode.setAdapter(arrayAdapter)
        removeCourseCode.setOnItemClickListener { parent, _, position, _ ->
            if(removeCourseCode.text.isNotEmpty()){
                removeCourseCode.error = null
//                codeLayout.error = null
            }
            val selectedItem = parent.getItemAtPosition(position).toString()
            db.collection("CourseInfo").document(selectedItem).get()
                .addOnCompleteListener { itAlt ->
                    if(itAlt.isSuccessful){
                        courseName.setText(itAlt.result.data?.getValue("CourseName").toString(),null)
                    }
                }
        }
//        removeCourseCode.doOnTextChanged { _, _, _, _ ->
//            codeLayout.error = null
//        }
        confirmRemoveCourseBut.setOnClickListener { _ ->
            if(it.contains(removeCourseCode.text.toString().uppercase())){
                progressBar.visibility = View.VISIBLE
                removeCourse()
            }else{
                removeCourseCode.setError("Please select a valid Course",null)
//                codeLayout.error = "Please select a valid Course"
                removeCourseCode.requestFocus()
            }
        }
    })

    return view
    }

    private fun removeCourse() {
        val codeTxt: String = removeCourseCode.text.toString().uppercase().trim{ it <= ' '}
        if(removeCourseCode.text.toString().isEmpty()) {
            removeCourseCode.setError("Please input Course code",null)
            removeCourseCode.requestFocus()
            return
        }
        val activityCall = (activity as ActivityCourseList)
        val updates = hashMapOf<String, Any>(
            "Course" to FieldValue.arrayRemove(codeTxt)
        )

        db.collection("CourseInfo").document(codeTxt).get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    db.collection("CourseInfo").document(codeTxt).delete().addOnSuccessListener {
                        db.collection("TeacherInfo").whereArrayContainsAny("Course", listOf(codeTxt)).get()
                            .addOnCompleteListener { itAlt ->
                                if(itAlt.isSuccessful){
                                    var temp: Boolean = true
                                    for(doc in itAlt.result!!){
                                        temp = false
                                        progressBar.visibility = View.GONE
                                        db.collection("TeacherInfo").document(doc.data.getValue("Code").toString()).update(updates).addOnCompleteListener {
                                            progressBar.visibility = View.GONE
                                            Snackbar.make(parentParentFrameLayout,"Course Removed",Snackbar.LENGTH_SHORT)
                                                .setBackgroundTint(
                                                    getColorFromAttribute(requireActivity(), R.attr.colorPrimary)
                                                )
                                                .show()
                                            activity?.onBackPressed()
                                        }
                                    }
                                    if(temp){
                                        progressBar.visibility = View.GONE
                                        Snackbar.make(parentFrameLayout,"Course not found",Snackbar.LENGTH_SHORT)
                                            .setBackgroundTint(
                                                getColorFromAttribute(requireActivity(), R.attr.colorPrimary))
                                            .show()
                                    }
                                }
                            }
                    }
                }
            }
    }
}