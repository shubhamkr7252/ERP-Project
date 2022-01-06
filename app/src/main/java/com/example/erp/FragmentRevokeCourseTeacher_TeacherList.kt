package com.example.erp

import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.AttrRes
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FragmentRevokeCourseTeacher_TeacherList : Fragment() {
    private val viewModel : ActivityTeacherListViewModel by activityViewModels()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var backButton: MaterialButton
    private lateinit var teacherNameSelect: AutoCompleteTextView
    private lateinit var teacherCode: EditText
    private lateinit var courseNameSelect: AutoCompleteTextView
    private lateinit var courseCode: EditText
    private lateinit var saveChangesBut: MaterialButton
    private lateinit var parentFrameLayout: FrameLayout
    private lateinit var parentParentFrameLayout: FrameLayout
    private lateinit var progressBar: RelativeLayout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view : View = inflater.inflate(R.layout.fragment_revoke_teacher_course_teacher_list, container, false)
        backButton = view.findViewById(R.id.backButton)
        saveChangesBut = view.findViewById(R.id.saveChangesBut)
        teacherNameSelect = view.findViewById(R.id.teacherNameSelect)
        teacherCode = view.findViewById(R.id.teacherCode)
        courseNameSelect = view.findViewById(R.id.courseNameSelect)
        courseCode = view.findViewById(R.id.courseCode)
        parentFrameLayout = view.findViewById(R.id.parentFrameLayout)
        progressBar = view.findViewById(R.id.progressBar)
        parentParentFrameLayout = activity?.findViewById(R.id.parentFrameLayout)!!

        backButton.setOnClickListener {
            activity?.onBackPressed()
        }

        courseNameSelect.setOnClickListener {
            if(teacherNameSelect.text.toString().isBlank()){
                teacherNameSelect.setError("Please select Teacher first",null)
                teacherNameSelect.requestFocus()
            }
        }

        viewModel.teacherData.observe(viewLifecycleOwner,{
            val arrayAdapter = ArrayAdapter(requireContext(),R.layout.exposed_dropdown_menu_item_layout,it)
            teacherNameSelect.setAdapter(arrayAdapter)
            teacherNameSelect.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                if (teacherNameSelect.text.isNotEmpty()) {
                    teacherNameSelect.error = null
                }
                val selectedItem = parent.getItemAtPosition(position).toString()
                db.collection("TeacherInfo").whereEqualTo("Name",selectedItem).get()
                    .addOnCompleteListener { querySnap ->
                        if(querySnap.isSuccessful){
                            for(document in querySnap.result!!){
                                teacherCode.setText(document.data.getValue("Code").toString())
                                db.collection("CourseInfo").whereArrayContainsAny("Teacher", listOf(document.data.getValue("Code"))).get()
                                    .addOnCompleteListener { query ->
                                        if(query.isSuccessful){
                                            val temp = ArrayList<String>()
                                            for(doc in query.result!!){
                                                temp.add(doc.data.getValue("CourseName").toString())
                                                val arrayTeacherAdapter = ArrayAdapter(requireContext(),R.layout.exposed_dropdown_menu_item_layout,temp)
                                                courseNameSelect.setAdapter(arrayTeacherAdapter)
                                            }
                                        }

                                    }
                                break
                            }
                        }
                    }
                courseNameSelect.onItemClickListener = AdapterView.OnItemClickListener { parentAlt, _, positionAlt, _ ->
                    val selectedItemAlt = parentAlt.getItemAtPosition(positionAlt).toString()
                    db.collection("CourseInfo").whereEqualTo("CourseName",selectedItemAlt).get()
                        .addOnCompleteListener { que ->
                            if(que.isSuccessful){
                                for(docs in que.result!!){
                                    courseCode.setText(docs.data.getValue("CourseCode").toString())
                                }
                            }

                        }
                }
            }
        })

        saveChangesBut.setOnClickListener {
            disallowCourse()
        }

        return view
    }

    private fun disallowCourse() {
        if(teacherNameSelect.text.isEmpty()) {
            teacherNameSelect.setError("Please select Teacher",null)
            teacherNameSelect.requestFocus()
            return
        }
        if(courseNameSelect.text.isEmpty()) {
            courseNameSelect.setError("Please select Course",null)
            courseNameSelect.requestFocus()
            courseNameSelect.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                courseNameSelect.error = null
            }
            return
        }
        progressBar.visibility = View.VISIBLE
        val teacherCodeTxt = teacherCode.text.toString().trim{ it <= ' '}
        val courseCodeTxt = courseCode.text.toString().trim{ it <= ' '}

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