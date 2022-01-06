package com.example.erp

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue


class FragmentRemoveStudent : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private val viewModel : ActivityStudentListViewModel by activityViewModels()
    private lateinit var removeStudentBackButton: MaterialButton
    private lateinit var removeStudentBut: MaterialButton
    private lateinit var removeStudentSRN: AutoCompleteTextView
    private lateinit var parentFrameLayout: FrameLayout
    private lateinit var progressBar: RelativeLayout
    private lateinit var parentParentFrameLayout: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_remove_student,container,false)
        removeStudentSRN = view.findViewById(R.id.removeStudentSRN)
        removeStudentBackButton = view.findViewById(R.id.removeStudentBackButton)
        removeStudentBut = view.findViewById(R.id.removeStudentBut)
        parentFrameLayout = view.findViewById(R.id.parentFrameLayout)
        progressBar = view.findViewById(R.id.progressBar)
        parentParentFrameLayout = activity?.findViewById(R.id.parentFrameLayout)!!

        removeStudentBackButton.setOnClickListener {
            activity?.onBackPressed()
        }

        viewModel.studentSrnData.observe(viewLifecycleOwner,{
            val arrayAdapter = ArrayAdapter(requireContext(),R.layout.exposed_dropdown_menu_item_layout,it)
            removeStudentSRN.setAdapter(arrayAdapter)
            removeStudentSRN.setOnItemClickListener { _, _, _, _ ->
                if(removeStudentSRN.text.isNotEmpty()){
                    removeStudentSRN.error = null
                }
            }
            removeStudentBut.setOnClickListener { itAlt ->
                if(it.contains(removeStudentSRN.text.toString().uppercase())){
                    progressBar.visibility = View.VISIBLE
                    removeStudent()
                }else{
                    removeStudentSRN.setError("Please Enter a valid SRN",null)
                    removeStudentBut.requestFocus()
                }
            }
        })

        return view
    }

    private fun removeStudent() {
        val srnTxt: String = removeStudentSRN.text.toString().uppercase().trim{ it <= ' '}
        if(removeStudentSRN.text.toString().isEmpty()) {
            removeStudentSRN.setError("Please input SRN",null)
            removeStudentSRN.requestFocus()
            return
        }
        val activityCall = (activity as ActivityStudentList)
        progressBar.visibility = View.VISIBLE
        db.collection("StudentInfo").document(srnTxt).get()
            .addOnSuccessListener {
                if(it.exists()) {
                    db.collection("StudentInfo").document(srnTxt).delete().addOnSuccessListener {
                        db.collection("StudentLoginInfo").document(srnTxt).delete().addOnSuccessListener {
                            db.collection("Attendance").get()
                                .addOnCompleteListener { itAlt ->
                                    if (itAlt.isSuccessful){
                                        var temp1: Boolean = true
                                        for(doc in itAlt.result!!){
                                            temp1 = false
                                            val date = doc.data.getValue("Date").toString()
                                            val tempAlt = ArrayList<String>()
                                            tempAlt.addAll(doc.data.getValue("Time") as Collection<String>)
                                            val updatesAbsent = hashMapOf<String, Any>(
                                                "StudentAbsent" to FieldValue.arrayRemove(srnTxt)
                                            )
                                            val updatesPresent = hashMapOf<String, Any>(
                                                "StudentPresent" to FieldValue.arrayRemove(srnTxt)
                                            )
                                            val classUpdate = hashMapOf<String, Any>(
                                                "TotalStrength" to FieldValue.increment(-1)
                                            )
                                            val absentLongUpdate = hashMapOf<String, Any>(
                                                "TotalAbsent" to FieldValue.increment(-1)
                                            )
                                            val presentLongUpdate = hashMapOf<String, Any>(
                                                "TotalPresent" to FieldValue.increment(-1)
                                            )
                                            var tempAlt2: Boolean = true
                                            for(i in tempAlt){
                                                tempAlt2 = false
                                                db.collection("Attendance").document(date).collection(i).whereArrayContainsAny("StudentAbsent",
                                                    listOf(srnTxt)).get()
                                                    .addOnCompleteListener { itAlt2 ->
                                                        if(itAlt2.isSuccessful){
                                                            var temp2: Boolean = true
                                                            for(docu in itAlt2.result!!){
                                                                temp2 = false
                                                                val tempDoc:String = docu.data.getValue("Batch").toString().plus("_").plus(docu.data.getValue("Semester").toString())
                                                                db.collection("Attendance").document(date).collection(i).document(tempDoc).update(updatesAbsent).addOnCompleteListener {
                                                                    db.collection("Attendance").document(date).collection(i).document(tempDoc).update(classUpdate).addOnCompleteListener {
                                                                        db.collection("Attendance").document(date).collection(i).document(tempDoc).update(absentLongUpdate).addOnCompleteListener {
                                                                            progressBar.visibility = View.GONE
                                                                            Snackbar.make(parentParentFrameLayout,"Student Removed",Snackbar.LENGTH_SHORT)
                                                                                .setBackgroundTint(activityCall.getColorFromAttr(R.attr.colorPrimary))
                                                                                .show()
                                                                            activityCall.getStudentData()
                                                                            activity?.onBackPressed()
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            if(temp2)
                                                            {
                                                                progressBar.visibility = View.GONE
                                                                Snackbar.make(parentParentFrameLayout,"Student Removed",Snackbar.LENGTH_SHORT)
                                                                    .setBackgroundTint(activityCall.getColorFromAttr(R.attr.colorPrimary))
                                                                    .show()
                                                                activityCall.getStudentData()
                                                                activity?.onBackPressed()
                                                            }
                                                        }
                                                    }
                                                db.collection("Attendance").document(date).collection(i).whereArrayContainsAny("StudentPresent",
                                                    listOf(srnTxt)).get()
                                                    .addOnCompleteListener { itAlt2 ->
                                                        if(itAlt2.isSuccessful){
                                                            var temp3: Boolean = true
                                                            for(docu in itAlt2.result!!){
                                                                temp3 = false
                                                                val tempDoc:String = docu.data.getValue("Batch").toString().plus("_").plus(docu.data.getValue("Semester").toString())
                                                                db.collection("Attendance").document(date).collection(i).document(tempDoc).update(updatesPresent).addOnCompleteListener {
                                                                    db.collection("Attendance").document(date).collection(i).document(tempDoc).update(classUpdate).addOnCompleteListener {
                                                                        db.collection("Attendance").document(date).collection(i).document(tempDoc).update(presentLongUpdate).addOnCompleteListener {
                                                                            progressBar.visibility = View.GONE
                                                                            Snackbar.make(parentParentFrameLayout,"Student Removed",Snackbar.LENGTH_SHORT)
                                                                                .setBackgroundTint(activityCall.getColorFromAttr(R.attr.colorPrimary))
                                                                                .show()
                                                                            activityCall.getStudentData()
                                                                            activity?.onBackPressed()
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            if(temp3){
                                                                progressBar.visibility = View.GONE
                                                                Snackbar.make(parentParentFrameLayout,"Student Removed",Snackbar.LENGTH_SHORT)
                                                                    .setBackgroundTint(activityCall.getColorFromAttr(R.attr.colorPrimary))
                                                                    .show()
                                                                activityCall.getStudentData()
                                                                activity?.onBackPressed()
                                                            }
                                                        }
                                                    }
                                            }
                                            if(tempAlt2){
                                                progressBar.visibility = View.GONE
                                                Snackbar.make(parentParentFrameLayout,"Student Removed",Snackbar.LENGTH_SHORT)
                                                    .setBackgroundTint(activityCall.getColorFromAttr(R.attr.colorPrimary))
                                                    .show()
                                                activityCall.getStudentData()
                                                activity?.onBackPressed()
                                            }
                                        }
                                        if(temp1)
                                        {
                                            progressBar.visibility = View.GONE
                                            Snackbar.make(parentParentFrameLayout,"Student Removed",Snackbar.LENGTH_SHORT)
                                                .setBackgroundTint(activityCall.getColorFromAttr(R.attr.colorPrimary))
                                                .show()
                                            activityCall.getStudentData()
                                            activity?.onBackPressed()
                                        }
                                    }
                                }
                        }
                    }
                }else{
                    progressBar.visibility = View.GONE
                    Snackbar.make(parentFrameLayout,"SRN not found",Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(activityCall.getColorFromAttr(R.attr.colorPrimary))
                        .show()
                }
            }
    }
}