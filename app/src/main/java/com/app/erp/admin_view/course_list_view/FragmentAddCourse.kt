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
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FragmentAddCourse : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private val viewModel : ActivityCourseListViewModel by activityViewModels()
    private lateinit var backButton: MaterialButton
    private lateinit var confirmBut: MaterialButton
    private lateinit var addCourseTeacherSelect: AutoCompleteTextView
    private lateinit var addCourseCode: EditText
    private lateinit var addCourseName: EditText
    private lateinit var addCourseTeacherCode: EditText
    private lateinit var parentFrameLayout: FrameLayout
    private lateinit var parentParentFrameLayout: FrameLayout
    private lateinit var progressBar: RelativeLayout

    private lateinit var tempArrayListSemesterInfo: ArrayList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_add_course, container, false)
        backButton = view.findViewById(R.id.backButton)
        addCourseTeacherSelect = view.findViewById(R.id.addCourseTeacherSelect)
        addCourseCode = view.findViewById(R.id.addCourseCode)
        addCourseName = view.findViewById(R.id.addCourseName)
        confirmBut = view.findViewById(R.id.confirmBut)
        addCourseTeacherCode = view.findViewById(R.id.addCourseTeacherCode)
        parentFrameLayout = view.findViewById(R.id.parentFrameLayout)
        progressBar = view.findViewById(R.id.progressBar)
        parentParentFrameLayout = activity?.findViewById(R.id.parentFrameLayout)!!

        tempArrayListSemesterInfo = arrayListOf<String>()

        backButton.setOnClickListener {
            activity?.onBackPressed()
        }

        addCourseTeacherSelect.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            db.collection("TeacherInfo").whereEqualTo("Name",selectedItem).get()
                .addOnCompleteListener { que ->
                    if(que.isSuccessful){
                        for(docs in que.result!!){
                            addCourseTeacherCode.setText(docs.data.getValue("Code").toString())
                        }
                    }

                }
        }

        viewModel.teacherData.observe(viewLifecycleOwner,{
            val arrayAdapter = ArrayAdapter(requireContext(),
                R.layout.exposed_dropdown_menu_item_layout,it)
            addCourseTeacherSelect.setAdapter(arrayAdapter)
        })

        confirmBut.setOnClickListener {
            checkInput()
        }

        return view
    }

    private fun checkInput(){
        if(addCourseCode.text.toString().isEmpty()) {
            addCourseCode.setError("Please input Course code",null)
            addCourseCode.requestFocus()
            return
        }
        if(addCourseName.text.toString().isEmpty()) {
            addCourseName.setError("Please input Course Name",null)
            addCourseName.requestFocus()
            return
        }
        if(addCourseTeacherSelect.text.isEmpty()) {
            addCourseTeacherSelect.setError("Please select teacher",null)
            addCourseTeacherSelect.requestFocus()
            return
        }
        progressBar.visibility = View.VISIBLE
        val nameTxt: String = viewModel.capitalizeWords(addCourseName.text.toString().trim{ it <= ' '}).toString()
        val codeTxt: String = addCourseCode.text.toString().uppercase().trim{ it <= ' '}
        val teacherTxt = ArrayList<String>()
        teacherTxt.add(addCourseTeacherCode.text.toString().uppercase().trim{ it <= ' '})

        checkCourse(codeTxt,nameTxt,teacherTxt)
    }

    private fun addCourse(codeTxt: String, nameTxt: String, teacherTxt: ArrayList<String>){
        val teacherCodeTxt: String = addCourseTeacherCode.text.toString().uppercase().trim{ it <= ' '}

        val courseInfo: MutableMap<String, Any> = HashMap()
        courseInfo["CourseCode"] = codeTxt
        courseInfo["CourseName"] = nameTxt

        db.collection("CourseInfo").document(codeTxt).set(courseInfo).addOnCompleteListener {
            db.collection("CourseInfo").document(codeTxt).update("Teacher",teacherTxt).addOnSuccessListener {
                val updates = hashMapOf<String, Any>(
                    "Course" to FieldValue.arrayUnion(codeTxt)
                )
                db.collection("TeacherInfo").document(teacherCodeTxt).update(updates).addOnSuccessListener {
                    progressBar.visibility = View.GONE
                    Snackbar.make(parentParentFrameLayout,"Course Added",
                        Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                        .show()
                    activity?.onBackPressed()
                }
            }.addOnFailureListener {
                progressBar.visibility = View.GONE
                Snackbar.make(parentFrameLayout,"Error occurred",
                    Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                    .show()
            }
        }.addOnFailureListener {
            progressBar.visibility = View.GONE
            Snackbar.make(parentFrameLayout,"Error occurred",
                Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                .show()
        }
    }

    private fun checkCourse(nameTxt: String, codeTxt: String, teacherTxt: ArrayList<String>) {
        val docRef = db.collection("CourseInfo").document(codeTxt)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.data?.getValue("CourseCode").toString() == codeTxt) {
                    progressBar.visibility = View.GONE
                    Snackbar.make(parentFrameLayout,"Course Code already Registered",
                        Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                        .show()
                }else {
                    db.collection("CourseInfo")
                        .whereEqualTo("CourseName", nameTxt)
                        .get()
                        .addOnSuccessListener { documents ->
                            var temp: Boolean = false
                            for (document_it in documents) {
                                progressBar.visibility = View.GONE
                                Snackbar.make(parentFrameLayout,"Course Name already Registered",
                                    Snackbar.LENGTH_SHORT)
                                    .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                                    .show()
                                temp = true
                                break
                            }
                            if(!temp){
                                addCourse(nameTxt,codeTxt,teacherTxt)
                            }
                        }
                }
            }
    }

    @ColorInt
    private fun getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true
    ): Int {
        activity?.theme?.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }
}