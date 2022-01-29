package com.app.erp.admin_view.course_list_view

import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.AttrRes
import androidx.fragment.app.activityViewModels
import com.app.erp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FragmentRevokeTeacherCourse_CourseList : Fragment() {
    private val viewModel : ActivityCourseListViewModel by activityViewModels()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var backButton: MaterialButton
    private lateinit var disallowTeacherCourseSelect: AutoCompleteTextView
    private lateinit var disallowTeacherNameSelect: AutoCompleteTextView
    private lateinit var disallowTeacherCourseCode: EditText
    private lateinit var saveChangesBut: MaterialButton
    private lateinit var disallowTeacherCode: EditText
    private lateinit var parentFrameLayout: FrameLayout
    private lateinit var progressBar: RelativeLayout
    private lateinit var parentParentFrameLayout: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_revoke_course_teacher_course_list,container,false)

        backButton = view.findViewById(R.id.backButton)
        saveChangesBut = view.findViewById(R.id.saveChangesBut)
        disallowTeacherCourseSelect = view.findViewById(R.id.disallowTeacherCourseSelect)
        disallowTeacherCourseCode = view.findViewById(R.id.disallowTeacherCourseCode)
        disallowTeacherNameSelect = view.findViewById(R.id.disallowTeacherNameSelect)
        disallowTeacherCode = view.findViewById(R.id.disallowTeacherCode)
        parentFrameLayout = view.findViewById(R.id.parentFrameLayout)
        progressBar = view.findViewById(R.id.progressBar)
        parentParentFrameLayout = activity?.findViewById(R.id.parentFrameLayout)!!

        backButton.setOnClickListener {
            activity?.onBackPressed()
        }
        saveChangesBut.setOnClickListener {
            disallowTeacher()
        }

        disallowTeacherNameSelect.setOnClickListener {
            if(disallowTeacherCourseSelect.text.toString().isBlank()){
                disallowTeacherCourseSelect.setError("Please select Course first",null)
                disallowTeacherCourseSelect.requestFocus()
            }
        }

        viewModel.courseData.observe(viewLifecycleOwner,{
            val arrayAdapter = ArrayAdapter(requireContext(),
                R.layout.exposed_dropdown_menu_item_layout,it)
            disallowTeacherCourseSelect.setAdapter(arrayAdapter)
            disallowTeacherCourseSelect.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                if (disallowTeacherCourseSelect.text.isNotEmpty()) {
                    disallowTeacherCourseSelect.error = null
                }
                val selectedItem = parent.getItemAtPosition(position).toString()
                db.collection("CourseInfo").whereEqualTo("CourseName",selectedItem).get()
                    .addOnCompleteListener { querySnap ->
                        if(querySnap.isSuccessful){
                            val tempAlt = ArrayList<String>()
                            for(document in querySnap.result!!){
                                val temp = document.data.getValue("CourseCode").toString()
                                disallowTeacherCourseCode.setText(temp)
                                tempAlt.clear()
                                db.collection("TeacherInfo").whereArrayContainsAny("Course", listOf(temp)).get()
                                    .addOnCompleteListener { query ->
                                        if(query.isSuccessful){
                                            for(doc in query.result!!){
                                                tempAlt.add(doc.data.getValue("Name").toString())
                                            }
                                        }

                                    }
                                val arrayTeacherAdapter = ArrayAdapter(requireContext(),
                                    R.layout.exposed_dropdown_menu_item_layout,tempAlt)
                                disallowTeacherNameSelect.setAdapter(arrayTeacherAdapter)
                            }
                        }
                    }
                disallowTeacherNameSelect.onItemClickListener = AdapterView.OnItemClickListener { parentAlt, _, positionAlt, _ ->
                    val selectedItemAlt = parentAlt.getItemAtPosition(positionAlt).toString()
                    db.collection("TeacherInfo").whereEqualTo("Name",selectedItemAlt).get()
                        .addOnCompleteListener { que ->
                            if(que.isSuccessful){
                                for(docs in que.result!!){
                                    disallowTeacherCode.setText(docs.data.getValue("Code").toString())
                                }
                            }

                        }
                }
            }
        })
        return view
    }

    private fun disallowTeacher() {
        if(disallowTeacherCourseSelect.text.isEmpty()) {
            disallowTeacherCourseSelect.setError("Please select Course",null)
            disallowTeacherCourseSelect.requestFocus()
            return
        }
        if(disallowTeacherNameSelect.text.isEmpty()) {
            disallowTeacherNameSelect.setError("Please select Teacher",null)
            disallowTeacherNameSelect.requestFocus()
            disallowTeacherNameSelect.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                disallowTeacherNameSelect.error = null
            }
            return
        }
        progressBar.visibility = View.VISIBLE
        val teacherCodeTxt = disallowTeacherCode.text.toString().trim{ it <= ' '}
        val courseCodeTxt = disallowTeacherCourseCode.text.toString().trim{ it <= ' '}

        val updates = hashMapOf<String, Any>(
            "Teacher" to FieldValue.arrayRemove(teacherCodeTxt)
        )
        val updatesAlt = hashMapOf<String, Any>(
            "Course" to FieldValue.arrayRemove(courseCodeTxt)
        )

        db.collection("CourseInfo").document(courseCodeTxt).update(updates).addOnCompleteListener {
            db.collection("TeacherInfo").document(teacherCodeTxt).update(updatesAlt).addOnCompleteListener {
                progressBar.visibility = View.GONE
                Snackbar.make(parentParentFrameLayout,"Database Updated",Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                    .show()
                activity?.onBackPressed()
            }.addOnFailureListener {
                progressBar.visibility = View.GONE
                Snackbar.make(parentFrameLayout,"Error occurred",Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                    .show()
            }
        }.addOnFailureListener {
            progressBar.visibility = View.GONE
            Snackbar.make(parentFrameLayout,"Error occurred",Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                .show()
        }
    }

    private fun getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true
    ): Int {
        activity?.theme?.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }
}