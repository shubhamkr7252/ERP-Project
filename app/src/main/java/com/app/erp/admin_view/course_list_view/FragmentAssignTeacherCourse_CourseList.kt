package com.app.erp.admin_view.course_list_view

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

class FragmentAssignTeacherCourse_CourseList : Fragment() {
    private val viewModel : ActivityCourseListViewModel by activityViewModels()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var saveChangesBut: MaterialButton
    private lateinit var backButton: MaterialButton
    private lateinit var assignTeacherCourseSelect: AutoCompleteTextView
    private lateinit var assignTeacherCourseCode: EditText
    private lateinit var assignTeacherSelect: AutoCompleteTextView
    private lateinit var alreadyAssignedTeacherNameList: TextView
    private lateinit var assignedTeacherLayout: MaterialCardView
    private lateinit var assignTeacherCode: EditText
    private lateinit var parentFrameLayout: FrameLayout
    private lateinit var parentParentFrameLayout: FrameLayout
    private lateinit var progressBar: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_assign_teacher_course_course_list,container,false)

        backButton = view.findViewById(R.id.backButton)
        assignTeacherCourseSelect = view.findViewById(R.id.assignTeacherCourseSelect)
        assignTeacherCourseCode = view.findViewById(R.id.assignTeacherCourseCode)
        assignTeacherSelect = view.findViewById(R.id.assignTeacherSelect)
        alreadyAssignedTeacherNameList = view.findViewById(R.id.alreadyAssignedTeacherNameList)
        assignedTeacherLayout = view.findViewById(R.id.assignedTeacherLayout)
        saveChangesBut = view.findViewById(R.id.saveChangesBut)
        assignTeacherCode = view.findViewById(R.id.assignTeacherCode)
        parentFrameLayout = view.findViewById(R.id.parentFrameLayout)
        progressBar = view.findViewById(R.id.progressBar)
        parentParentFrameLayout = activity?.findViewById(R.id.parentFrameLayout)!!

        backButton.setOnClickListener {
            activity?.onBackPressed()
        }

        assignTeacherSelect.setOnClickListener {
            if(assignTeacherCourseSelect.text.toString().isBlank()){
                assignTeacherCourseSelect.setError("Please select Course first",null)
                assignTeacherCourseSelect.requestFocus()
            }
        }

        viewModel.courseData.observe(viewLifecycleOwner,{
            val arrayAdapter = ArrayAdapter(requireContext(),
                R.layout.exposed_dropdown_menu_item_layout,it)
            assignTeacherCourseSelect.setAdapter(arrayAdapter)
            assignTeacherCourseSelect.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                if (assignTeacherCourseSelect.text.isNotEmpty()) {
                    assignTeacherCourseSelect.error = null
                }
                assignTeacherSelect.text.clear()
                assignTeacherCode.text.clear()
                alreadyAssignedTeacherNameList.text = null
                assignedTeacherLayout.visibility = View.GONE
                val selectedItem = parent.getItemAtPosition(position).toString()
                db.collection("CourseInfo").whereEqualTo("CourseName",selectedItem).get()
                    .addOnCompleteListener { querySnap ->
                        if(querySnap.isSuccessful){
                            for(document in querySnap.result!!){
                                assignTeacherCourseCode.setText(document.data.getValue("CourseCode").toString())
                                db.collection("TeacherInfo").whereArrayContainsAny("Course", listOf(document.data.getValue("CourseCode"))).get()
                                    .addOnCompleteListener { query ->
                                        if(query.isSuccessful){
                                            val temp = ArrayList<String>()
                                            for(doc in query.result!!){
                                                temp.add(doc.data.getValue("Name").toString())
                                                val temp2 = temp.toString()
                                                alreadyAssignedTeacherNameList.text = temp2.substring(1,temp2.length-1)
                                                if(alreadyAssignedTeacherNameList.text.toString().isNotBlank()){
                                                    assignedTeacherLayout.visibility = View.VISIBLE
                                                }
                                            }
                                        }

                                    }
                                db.collection("TeacherInfo").whereNotEqualTo("Course", listOf(document.data.getValue("CourseCode"))).get()
                                    .addOnCompleteListener { q ->
                                        if(q.isSuccessful){
                                            val temp = ArrayList<String>()
                                            for(d in q.result!!){
                                                temp.add(d.data.getValue("Name").toString())
                                                val arrayTeacherAdapter = ArrayAdapter(requireContext(),
                                                    R.layout.exposed_dropdown_menu_item_layout,temp)
                                                assignTeacherSelect.setAdapter(arrayTeacherAdapter)
                                            }
                                        }
                                    }
                                break
                            }
                        }
                    }
            }
            assignTeacherSelect.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                db.collection("TeacherInfo").whereEqualTo("Name",selectedItem).get()
                    .addOnCompleteListener { que ->
                        if(que.isSuccessful){
                            for(docs in que.result!!){
                                assignTeacherCode.setText(docs.data.getValue("Code").toString())
                            }
                        }

                    }
            }
        })

        saveChangesBut.setOnClickListener {
            assignCourse()
        }

        return view
    }

    private fun assignCourse() {
        if(assignTeacherCourseSelect.text.isEmpty()) {
            assignTeacherCourseSelect.setError("Please select Course",null)
            assignTeacherCourseSelect.requestFocus()
            return
        }
        if(assignTeacherSelect.text.isEmpty()) {
            assignTeacherSelect.setError("Please select Teacher",null)
            assignTeacherSelect.requestFocus()
            assignTeacherSelect.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                assignTeacherSelect.error = null
            }
            return
        }
        progressBar.visibility = View.VISIBLE
        val updates = hashMapOf<String, Any>(
            "Teacher" to FieldValue.arrayUnion(assignTeacherCode.text.toString().trim{it <= ' '})
        )
        val updatesAlt = hashMapOf<String, Any>(
            "Course" to FieldValue.arrayUnion(assignTeacherCourseCode.text.toString().trim{it <= ' '})
        )

        db.collection("CourseInfo").document(assignTeacherCourseCode.text.toString().trim{it <= ' '}).update(updates).addOnSuccessListener {
            db.collection("TeacherInfo").document(assignTeacherCode.text.toString().trim{it <= ' '}).update(updatesAlt).addOnSuccessListener {
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

    @ColorInt
    private fun getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true
    ): Int {
        activity?.theme?.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }
}