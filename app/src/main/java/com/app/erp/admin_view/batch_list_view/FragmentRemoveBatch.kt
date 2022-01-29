package com.app.erp.admin_view.batch_list_view

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
import com.google.firebase.firestore.FirebaseFirestore
import java.util.HashMap

class FragmentRemoveBatch : Fragment() {
    private val viewModel : ActivityBatchListViewModel by activityViewModels()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var batch: AutoCompleteTextView
    private lateinit var saveBut: MaterialButton
    private lateinit var backButton: MaterialButton
    private lateinit var parentFrameLayout: FrameLayout
    private lateinit var parentParentFrameLayout: FrameLayout
    private lateinit var progressBar: RelativeLayout
    private lateinit var batchLayout: TextInputLayout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_remove_batch, container, false)
        batch = view.findViewById(R.id.batch)
        saveBut = view.findViewById(R.id.saveBut)
        backButton = view.findViewById(R.id.backButton)
        parentFrameLayout = view.findViewById(R.id.parentFrameLayout)
        progressBar = view.findViewById(R.id.progressBar)
        batchLayout = view.findViewById(R.id.batchLayout)
        parentParentFrameLayout = activity?.findViewById(R.id.parentFrameLayout)!!

        backButton.setOnClickListener {
            activity?.onBackPressed()
        }

        viewModel.batchArrayListData.observe(viewLifecycleOwner,{
            val arrayAdapter = ArrayAdapter(requireContext(),
                R.layout.exposed_dropdown_menu_item_layout,it)
            batch.setAdapter(arrayAdapter)
            batch.setOnItemClickListener { parent, _, position, _ ->
                if(batch.text.isNotEmpty()){
                    batch.error = null
                }
            }
            saveBut.setOnClickListener { _ ->
                if(it.contains(batch.text.toString())){
                    progressBar.visibility = View.VISIBLE
                    removeBatch()
                }else{
                    batch.setError("Please Enter a valid Batch",null)
                    batch.requestFocus()
                }
            }
        })
        return view
    }

    private fun removeBatch() {
        val activityCall = activity as ActivityBatchList
        val batchTxt = batch.text.toString().trim{ it <= ' '}
        val studentInfo: MutableMap<String, Any> = HashMap()
        studentInfo["Batch"] = ""
        studentInfo["Semester"] = ""
        studentInfo["AttendedClasses"] = 0
        studentInfo["NonAttendedClasses"] = 0
        studentInfo["TotalClasses"] = 0
        db.collection("BatchInfo").document(batchTxt).delete().addOnCompleteListener {
            db.collection("StudentInfo").whereEqualTo("Batch",batchTxt).get()
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        var tempS: Boolean = true
                        for (doc in it.result!!){
                            tempS = false
                            db.collection("StudentInfo").document(doc.data.getValue("SRN").toString()).update(studentInfo).addOnCompleteListener {
                                db.collection("Attendance").get()
                                    .addOnCompleteListener { itAlt ->
                                        if (itAlt.isSuccessful){
                                            var temp1: Boolean = true
                                            for(documentAlt in itAlt.result!!){
                                                temp1 = false
                                                val date = documentAlt.data.getValue("Date").toString()
                                                val tempAlt = ArrayList<String>()
                                                tempAlt.addAll(documentAlt.data.getValue("Time") as Collection<String>)
                                                for(i in tempAlt){
                                                    db.collection("Attendance").document(date).collection(i).whereEqualTo("Batch",
                                                        batchTxt).get()
                                                        .addOnCompleteListener { itAlt2 ->
                                                            if(itAlt2.isSuccessful){
                                                                var temp2: Boolean = true
                                                                for(docu in itAlt2.result!!){
                                                                    temp2 = false
                                                                    val tempDoc:String = docu.data.getValue("Batch").toString().plus("_").plus(docu.data.getValue("Semester").toString())
                                                                    db.collection("Attendance").document(date).collection(i).document(tempDoc).delete().addOnSuccessListener {
                                                                        progressBar.visibility = View.GONE
                                                                        activityCall.getBatchArrayListData()
                                                                        activityCall.getBatchData()
                                                                        Snackbar.make(parentParentFrameLayout, "Batch Removed",Snackbar.LENGTH_SHORT)
                                                                            .setBackgroundTint(
                                                                                getColorFromAttribute(requireActivity(),R.attr.colorPrimary))
                                                                            .show()
                                                                        activity?.onBackPressed()
                                                                    }
                                                                }
                                                                if(temp2){
                                                                    progressBar.visibility = View.GONE
                                                                    activityCall.getBatchArrayListData()
                                                                    activityCall.getBatchData()
                                                                    Snackbar.make(parentParentFrameLayout, "Batch Removed",Snackbar.LENGTH_SHORT)
                                                                        .setBackgroundTint(getColorFromAttribute(requireActivity(),R.attr.colorPrimary))
                                                                        .show()
                                                                    activity?.onBackPressed()
                                                                }
                                                            }
                                                        }
                                                }
                                            }
                                            if(temp1){
                                                progressBar.visibility = View.GONE
                                                activityCall.getBatchArrayListData()
                                                activityCall.getBatchData()
                                                Snackbar.make(parentParentFrameLayout, "Batch Removed",Snackbar.LENGTH_SHORT)
                                                    .setBackgroundTint(
                                                        getColorFromAttribute(requireActivity(), R.attr.colorPrimary))
                                                    .show()
                                                activity?.onBackPressed()
                                            }
                                        }
                                    }
                            }
                        }
                        if(tempS){
                            progressBar.visibility = View.GONE
                            activityCall.getBatchArrayListData()
                            activityCall.getBatchData()
                            Snackbar.make(parentParentFrameLayout, "Batch Removed",Snackbar.LENGTH_SHORT)
                                .setBackgroundTint(
                                    getColorFromAttribute(requireActivity(), R.attr.colorPrimary))
                                .show()
                            activity?.onBackPressed()
                        }
                    }
                }

        }.addOnFailureListener {
            progressBar.visibility = View.GONE
            Snackbar.make(parentFrameLayout, "Error Occurred",Snackbar.LENGTH_SHORT)
                .setBackgroundTint(
                    getColorFromAttribute(requireActivity(), R.attr.colorPrimary))
                .show()
        }
    }
}