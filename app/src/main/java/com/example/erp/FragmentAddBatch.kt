package com.example.erp

import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FragmentAddBatch : Fragment() {
    private lateinit var backButton: MaterialButton
    private lateinit var batchName: EditText
    private lateinit var semesters: EditText
    private lateinit var confirmBut: MaterialButton
    private lateinit var parentFrameLayout: FrameLayout
    private lateinit var parentParentFrameLayout: FrameLayout
    private lateinit var progressBar: RelativeLayout

    private val db = FirebaseFirestore.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_batch, container, false)
        backButton = view.findViewById(R.id.backButton)
        batchName = view.findViewById(R.id.batchName)
        semesters = view.findViewById(R.id.semesters)
        confirmBut = view.findViewById(R.id.confirmBut)
        parentFrameLayout = view.findViewById(R.id.parentFrameLayout)
        progressBar = view.findViewById(R.id.progressBar)
        parentParentFrameLayout = activity?.findViewById(R.id.parentFrameLayout)!!

        backButton.setOnClickListener {
            activity?.onBackPressed()
        }

        confirmBut.setOnClickListener {
            checkInput()
        }
        return view
    }

    private fun checkInput() {
        if(batchName.text.toString().isEmpty()){
            batchName.setError("Please enter Batch Name",null)
            batchName.requestFocus()
            return
        }
        if(semesters.text.toString().isEmpty()){
            semesters.setError("Please enter Number of Semesters",null)
            semesters.requestFocus()
            return
        }

        addBatch()
        progressBar.visibility = View.VISIBLE
    }

    private fun addBatch() {
        val batchTxt : String = capitalizeWords(batchName.text.toString().trim{ it <= ' '})
        val batchDetails: MutableMap<String,Any> = HashMap()
        batchDetails["Name"] = batchTxt

        val semesterArrayList = ArrayList<String>()
        val temp: Long = semesters.text.toString().toLong()
        for(i in 1..temp){
            semesterArrayList.add("Semester $i")
        }
        val semesterDetails: MutableMap<String,Any> = HashMap()
        batchDetails["Semester"] = semesterArrayList

        db.collection("BatchInfo").document(batchTxt).set(batchDetails).addOnSuccessListener {
            db.collection("BatchInfo").document(batchTxt).update(semesterDetails).addOnSuccessListener {
                progressBar.visibility = View.GONE
                Snackbar.make(parentParentFrameLayout,"Batch Added",Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                    .show()
                val activityCall = activity as ActivityBatchList
                activityCall.getBatchArrayListData()
                activityCall.getBatchData()
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

    private fun capitalizeWords(capString: String): String {
        val capBuffer = StringBuffer()
        val capMatcher: Matcher =
            Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString)
        while (capMatcher.find()) {
            capMatcher.appendReplacement(
                capBuffer,
                capMatcher.group(1).uppercase(Locale.getDefault()) + capMatcher.group(2).lowercase(
                    Locale.getDefault())
            )
        }
        return capMatcher.appendTail(capBuffer).toString()
    }

    @ColorInt
    private fun getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true
    ): Int {
        activity?.theme?.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }
}