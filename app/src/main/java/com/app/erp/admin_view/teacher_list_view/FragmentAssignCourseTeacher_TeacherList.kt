package com.app.erp.admin_view.teacher_list_view

import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.fragment.app.activityViewModels
import com.app.erp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FragmentAssignCourseTeacher_TeacherList : Fragment() {
    private val viewModel : ActivityTeacherListViewModel by activityViewModels()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var backButton: MaterialButton
    private lateinit var teacherNameSelect: AutoCompleteTextView
    private lateinit var teacherCode: EditText
    private lateinit var courseNameSelect: AutoCompleteTextView
    private lateinit var courseCode: EditText
    private lateinit var saveChangesBut: MaterialButton
    private lateinit var alreadyAssignedCourseNameList: TextView
    private lateinit var assignedCourseLayout: MaterialCardView
    private lateinit var parentFrameLayout: FrameLayout
    private lateinit var parentParentFrameLayout: FrameLayout
    private lateinit var progressBar: RelativeLayout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view : View = inflater.inflate(R.layout.fragment_assign_course_teacher_teacher_list, container, false)
        backButton = view.findViewById(R.id.backButton)
        teacherNameSelect = view.findViewById(R.id.teacherNameSelect)
        teacherCode = view.findViewById(R.id.teacherCode)
        courseNameSelect = view.findViewById(R.id.courseNameSelect)
        courseCode = view.findViewById(R.id.courseCode)
        saveChangesBut = view.findViewById(R.id.saveChangesBut)
        alreadyAssignedCourseNameList = view.findViewById(R.id.alreadyAssignedCourseNameList)
        assignedCourseLayout = view.findViewById(R.id.assignedCourseLayout)
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
            val arrayAdapter = ArrayAdapter(requireContext(),
                R.layout.exposed_dropdown_menu_item_layout,it)
            teacherNameSelect.setAdapter(arrayAdapter)
            teacherNameSelect.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                if (teacherNameSelect.text.isNotEmpty()) {
                    teacherNameSelect.error = null
                }
                courseNameSelect.text.clear()
                courseCode.text.clear()
                alreadyAssignedCourseNameList.text = null
                assignedCourseLayout.visibility = View.GONE
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
                                                val temp2 = temp.toString()
                                                alreadyAssignedCourseNameList.text = temp2.substring(1,temp2.length-1)
                                                if(alreadyAssignedCourseNameList.text.toString().isNotBlank()){
                                                    assignedCourseLayout.visibility = View.VISIBLE
                                                }
                                            }
                                        }

                                    }
                                db.collection("CourseInfo").whereNotEqualTo("Teacher", listOf(document.data.getValue("Code"))).get()
                                    .addOnCompleteListener { q ->
                                        if(q.isSuccessful){
                                            val temp = ArrayList<String>()
                                            for(d in q.result!!){
                                                temp.add(d.data.getValue("CourseName").toString())
                                                val arrayTeacherAdapter = ArrayAdapter(requireContext(),
                                                    R.layout.exposed_dropdown_menu_item_layout,temp)
                                                courseNameSelect.setAdapter(arrayTeacherAdapter)
                                            }
                                        }
                                    }
                                break
                            }
                        }
                    }
            }
            courseNameSelect.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                db.collection("CourseInfo").whereEqualTo("CourseName",selectedItem).get()
                    .addOnCompleteListener { que ->
                        if(que.isSuccessful){
                            for(docs in que.result!!){
                                courseCode.setText(docs.data.getValue("CourseCode").toString())
                            }
                        }

                    }
            }
        })

        saveChangesBut.setOnClickListener {
            assignTeacher()
        }

        return view
    }

    private fun assignTeacher() {
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
        val updates = hashMapOf<String, Any>(
            "Teacher" to FieldValue.arrayUnion(teacherCode.text.toString().trim{it <= ' '})
        )
        val updatesAlt = hashMapOf<String, Any>(
            "Course" to FieldValue.arrayUnion(courseCode.text.toString().trim{it <= ' '})
        )

        db.collection("CourseInfo").document(courseCode.text.toString().trim{it <= ' '}).update(updates).addOnSuccessListener {
            db.collection("TeacherInfo").document(teacherCode.text.toString().trim{it <= ' '}).update(updatesAlt).addOnSuccessListener {
                Snackbar.make(parentParentFrameLayout,"Database Updated",Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                    .show()
                progressBar.visibility = View.GONE
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

    @ColorInt
    private fun getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true
    ): Int {
        activity?.theme?.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }

}