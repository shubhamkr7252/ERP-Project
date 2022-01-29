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
import com.app.erp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.HashMap

class FragmentRemoveTeacher : Fragment() {
    private lateinit var backButton: MaterialButton
    private lateinit var teacherCode: EditText
    private lateinit var confirmBut: MaterialButton
    private lateinit var parentFrameLayout: FrameLayout
    private lateinit var parentParentFrameLayout: FrameLayout
    private lateinit var progressBar: RelativeLayout
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_remove_teacher, container, false)
        backButton = view.findViewById(R.id.backButton)
        teacherCode = view.findViewById(R.id.teacherCode)
        confirmBut = view.findViewById(R.id.confirmBut)
        parentFrameLayout = view.findViewById(R.id.parentFrameLayout)
        progressBar = view.findViewById(R.id.progressBar)
        parentParentFrameLayout = activity?.findViewById(R.id.parentFrameLayout)!!

        backButton.setOnClickListener {
            activity?.onBackPressed()
        }
        confirmBut.setOnClickListener {
            removeTeacher()
        }
        return view
    }

    private fun removeTeacher() {
        val codeTxt: String = teacherCode.text.toString().uppercase().trim{ it <= ' '}
        if(teacherCode.text.toString().isEmpty()) {
            teacherCode.setError("Please input code",null)
            teacherCode.requestFocus()
            return
        }
        progressBar.visibility = View.VISIBLE
        val updates = hashMapOf<String, Any>(
            "Teacher" to FieldValue.arrayRemove(codeTxt)
        )
        val teacherInfo: MutableMap<String, Any> = HashMap()
        teacherInfo["Teacher"] = ""
        teacherInfo["TeacherCode"] = ""

        db.collection("TeacherLoginInfo").document(codeTxt).get()
            .addOnSuccessListener {
                if(it.exists()) {
                    db.collection("TeacherInfo").document(codeTxt).delete().addOnSuccessListener {
                        db.collection("TeacherLoginInfo").document(codeTxt).delete().addOnSuccessListener {
                            db.collection("CourseInfo").whereArrayContainsAny("Teacher", listOf(codeTxt)).get()
                                .addOnCompleteListener { itAlt ->
                                    if(itAlt.isSuccessful){
                                        for(doc in itAlt.result!!){
                                            db.collection("CourseInfo").document(doc.data.getValue("CourseCode").toString()).update(updates).addOnCompleteListener {
                                                db.collection("Attendance").get()
                                                    .addOnCompleteListener { itAlt ->
                                                        if (itAlt.isSuccessful){
                                                            for(documentAlt in itAlt.result!!){
                                                                val date = documentAlt.data.getValue("Date").toString()
                                                                val tempAlt = ArrayList<String>()
                                                                tempAlt.addAll(documentAlt.data.getValue("Time") as Collection<String>)
                                                                for(i in tempAlt){
                                                                    db.collection("Attendance").document(date).collection(i).whereEqualTo("TeacherCode",
                                                                        codeTxt).get()
                                                                        .addOnCompleteListener { itAlt2 ->
                                                                            if(itAlt2.isSuccessful){
                                                                                for(docu in itAlt2.result!!){
                                                                                    val tempDoc:String = docu.data.getValue("Batch").toString().plus("_").plus(docu.data.getValue("Semester").toString())
                                                                                    db.collection("Attendance").document(date).collection(i).document(tempDoc).update(teacherInfo).addOnCompleteListener {
                                                                                        progressBar.visibility = View.GONE
                                                                                        Snackbar.make(parentParentFrameLayout,"Teacher Removed",
                                                                                            Snackbar.LENGTH_SHORT)
                                                                                            .setBackgroundTint(getColorFromAttr(
                                                                                                R.attr.colorPrimary
                                                                                            ))
                                                                                            .show()
                                                                                        activity?.onBackPressed()
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                }
                                                            }
                                                        }
                                                    }
                                            }
                                        }
                                    }
                                }
                        }
                    }
                }else{
                    progressBar.visibility = View.GONE
                    Snackbar.make(parentFrameLayout,"Teacher not found",Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                        .show()
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