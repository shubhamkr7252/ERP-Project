package com.app.erp.admin_view.student_list_view

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.erp.*
import com.app.erp.R
import com.app.erp.admin_view.admin_list_view.FragmentRemoveAdmin
import com.app.erp.databinding.ActivityStudentListBinding
import com.app.erp.gloabal_functions.EncryptDecryptPassword
import com.app.erp.gloabal_functions.getColorFromAttribute
import com.app.erp.gloabal_functions.showSnackBar
import com.app.erp.gloabal_functions.showToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ActivityStudentList : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val fragmentViewModel : ActivityStudentListViewModel by viewModels()
    private lateinit var binding: ActivityStudentListBinding
    private lateinit var auth: FirebaseAuth

    private lateinit var studentArrayList: ArrayList<StudentListRecyclerViewDataClass>
    private lateinit var studentListRecyclerViewAdapter: StudentListRecyclerViewAdapter

    private lateinit var tempArrayListBatchInfo: ArrayList<String>
    private lateinit var tempArrayListSemesterInfo: ArrayList<String>

    private var adminInfo = String()

    override fun onBackPressed() {
        if(binding.obstructor.isVisible){
            binding.editStudentFab.performClick()
        }
        else{
            super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = getColorFromAttribute(this,R.attr.colorSurfaceVariant)
        auth = Firebase.auth

        adminInfo = intent.getStringExtra("mailTxt").toString()

        retrieveBatchSemesterDataList()
        fragmentViewModel.adminEmailData.postValue(intent.getStringExtra("mailTxt"))

        tempArrayListBatchInfo = arrayListOf<String>()
        tempArrayListSemesterInfo = arrayListOf<String>()

        binding.studentListSemesterSelect.setOnClickListener {
            if(binding.studentListBatchSelect.text.isEmpty()){
                binding.studentListBatchSelect.setError("Please select batch first",null)
                binding.studentListBatchSelect.requestFocus()
            }
        }

        binding.studentRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.studentRecyclerView.setHasFixedSize(true)
        studentArrayList = arrayListOf<StudentListRecyclerViewDataClass>()
        studentListRecyclerViewAdapter = StudentListRecyclerViewAdapter(studentArrayList,this, object : ViewHolderStudentListOptions.Listener{
            override fun getSelectedStudent(list: ArrayList<String>) {
                if(list[0] == "3"){
                    MaterialAlertDialogBuilder(this@ActivityStudentList)
                        .setTitle("Remove ${list[1]}")
                        .setCancelable(false)
                        .setMessage("Are you sure you want to remove ${list[2]}? Changes cannot be reverted once done.")
                        .setNegativeButton("Cancel") { _, _ -> }
                        .setPositiveButton("Delete") { _, _ ->
                            removeStudentInfo(list[1],list[3])
                        }.show()
                }
                if(list[0] == "2"){
                    val editStudent = FragmentEditStudent()
                    val fragment : Fragment? = supportFragmentManager.findFragmentByTag(editStudent::class.java.simpleName)

                    val bundle = Bundle()
                    bundle.putString("srnData", list[1])

                    if(fragment !is FragmentEditStudent){
                        editStudent.arguments = bundle
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.relativeLayoutFragmentLayoutContainer,editStudent, FragmentEditStudent::class.java.simpleName)
                            .addToBackStack(null)
                            .commit()
                    }
                }
            }
        })
        binding.studentRecyclerView.adapter = studentListRecyclerViewAdapter
        studentListRecyclerViewAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        binding.studentRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(studentRecyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) binding.editStudentFab.hide() else if (dy < 0) binding.editStudentFab.show()
                if (dy > 0) binding.filterStudentFab.hide() else if (dy < 0) binding.filterStudentFab.show()
            }
        })

        getStudentData()

        binding.obstructor.setOnClickListener {
            binding.editStudentFab.performClick()
        }

        binding.refreshButton.setOnClickListener {
            if(binding.linearLayoutTopToolbar.isVisible){
                if(binding.studentListBatchSelect.text.isNotBlank() && binding.studentListSemesterSelect.text.isNotBlank()){
                    getFilteredStudentData()
                }else{
                    getStudentData()
                }
            }else{
                getStudentData()
            }
            Snackbar.make(binding.parentFrameLayout,"Data Refreshed",Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColorFromAttribute(this,R.attr.colorPrimary))
                .show()
        }

        binding.editStudentFab.setOnClickListener {
            val addStudentFragment = FragmentAddStudent()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(addStudentFragment::class.java.simpleName)

            if(fragment !is FragmentAddStudent){
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.relativeLayoutFragmentLayoutContainer,addStudentFragment,
                        FragmentAddStudent::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }

        binding.filterStudentList.setOnClickListener {
            Snackbar.make(binding.parentFrameLayout,"Filter Applied",Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColorFromAttribute(this,R.attr.colorPrimary))
                .show()
            getFilteredStudentData()
        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    internal fun getStudentInfo(srn: String){
        db.collection("StudentInfo").document(srn).get()
            .addOnCompleteListener { itAlt ->
                if(itAlt.isSuccessful){
                    fragmentViewModel.studentName.postValue(itAlt.result.data?.getValue("Name").toString())
                    fragmentViewModel.studentGender.postValue(itAlt.result.data?.getValue("Gender").toString())
                    fragmentViewModel.studentBirthday.postValue(itAlt.result.data?.getValue("Birthday").toString())
                    fragmentViewModel.studentBatch.postValue(itAlt.result.data?.getValue("Batch").toString())
                    fragmentViewModel.studentSemester.postValue(itAlt.result.data?.getValue("Semester").toString())
                    fragmentViewModel.studentEmail.postValue(itAlt.result.data?.getValue("Email").toString())
                    fragmentViewModel.studentPhone.postValue(itAlt.result.data?.getValue("Phone").toString())
                    db.collection("StudentLoginInfo").document(srn).get()
                        .addOnCompleteListener {
                            if(it.isSuccessful){
                                fragmentViewModel.studentPassword.postValue(it.result.data?.getValue("Password").toString())
                            }
                        }
                }
            }
    }

    private fun removeStudentAttendance(srnTxt: String) {
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

        db.collection("Attendance").get()
            .addOnSuccessListener {
                if(it.isEmpty){
                    adminLogin()
                }
                else{
                    for (doc in it.documents){
                        val date = doc.data?.getValue("Date").toString()
                        val tempAlt = ArrayList<String>()
                        tempAlt.addAll(doc.data?.getValue("Time") as Collection<String>)

                        for(i in tempAlt){
                            db.collection("Attendance").document(date).collection(i).whereArrayContainsAny("StudentAbsent",
                                listOf(srnTxt)).get()
                                .addOnSuccessListener { it2 ->
                                    for(docu in it2){
                                        val tempDoc: String = docu.data.getValue("Batch")
                                            .toString().plus("_").plus(docu.data.getValue("Semester").toString())

                                        db.collection("Attendance").document(date).collection(i).document(tempDoc).update(updatesAbsent).addOnCompleteListener {
                                            db.collection("Attendance").document(date).collection(i).document(tempDoc).update(classUpdate).addOnCompleteListener {
                                                db.collection("Attendance").document(date).collection(i).document(tempDoc).update(absentLongUpdate)
                                                    .addOnFailureListener { absentLongUpdateError ->
                                                        showToast(this,"Failed with error: $absentLongUpdateError")
                                                        adminLogin()
                                                    }
                                            }.addOnFailureListener { classUpdateError ->
                                                showToast(this,"Failed with error: $classUpdateError")
                                                adminLogin()
                                            }
                                        }.addOnFailureListener { updatesAbsentError ->
                                            showToast(this,"Failed with error: $updatesAbsentError")
                                            adminLogin()
                                        }
                                    }
                                }.addOnFailureListener { collectionError ->
                                    showToast(this,"Failed with error: $collectionError")
                                    adminLogin()
                                }
                            db.collection("Attendance").document(date).collection(i).whereArrayContainsAny("StudentPresent",
                                listOf(srnTxt)).get()
                                .addOnSuccessListener { it2 ->
                                    for(docu in it2){
                                        val tempDoc: String = docu.data.getValue("Batch").toString().plus("_").plus(docu.data.getValue("Semester").toString())

                                        db.collection("Attendance").document(date).collection(i).document(tempDoc).update(updatesPresent).addOnCompleteListener {
                                            db.collection("Attendance").document(date).collection(i).document(tempDoc).update(classUpdate).addOnCompleteListener {
                                                db.collection("Attendance").document(date).collection(i).document(tempDoc).update(presentLongUpdate)
                                                    .addOnFailureListener { updatesPresentError ->
                                                        showToast(this,"Failed with error: $updatesPresentError")
                                                        adminLogin()
                                                    }
                                            }.addOnFailureListener { classUpdateError ->
                                                showToast(this,"Failed with error: $classUpdateError")
                                                adminLogin()
                                            }
                                        }.addOnFailureListener { presentLongUpdateError ->
                                            showToast(this,"Failed with error: $presentLongUpdateError")
                                            adminLogin()
                                        }
                                    }
                                }.addOnFailureListener { collectionError ->
                                    showToast(this,"Failed with error: $collectionError")
                                    adminLogin()
                                }
                        }
                    }
                    adminLogin()
                }
            }.addOnFailureListener {
                showToast(this,"Failed with error: $it")
                adminLogin()
            }
    }

    private fun adminLogin(){
        db.collection("AdminLoginInfo").document(adminInfo).get()
            .addOnSuccessListener { adminIt ->
                if(adminIt.exists()){
                    val passwordTxt: String = EncryptDecryptPassword.decrypt(adminIt.data?.getValue("Password").toString()).toString()
                    auth.signInWithEmailAndPassword(adminInfo,passwordTxt).addOnSuccessListener {
                        showSnackBar(this,binding.parentFrameLayout,"Student Removed")
                        getStudentData()
                        binding.progressBar.visibility = View.GONE
                    }.addOnFailureListener { adminLoginError ->
                        showToast(this,"Failed with error: $adminLoginError")
                    }
                }
            }.addOnFailureListener { adminLoginInfoError ->
                showToast(this,"Failed with error: $adminLoginInfoError")
            }
    }

    private fun removeStudentInfo(srnTxt: String, mailTxt: String) {
        binding.progressBar.visibility = View.VISIBLE

        db.collection("StudentLoginInfo").document(mailTxt).get()
            .addOnSuccessListener {
                val email: String = it.data?.getValue("Email").toString()
                val password: String = EncryptDecryptPassword.decrypt(
                    it.data?.getValue("Password").toString()).toString()
                auth.signInWithEmailAndPassword(email,password).addOnSuccessListener {
                    auth.currentUser?.delete()?.addOnSuccessListener {
                        db.collection("StudentInfo").document(srnTxt).delete().addOnSuccessListener {
                            db.collection("StudentLoginInfo").document(mailTxt).delete().addOnSuccessListener {
                                removeStudentAttendance(srnTxt)
                            }.addOnFailureListener { studentLoginInfoDeleteError ->
                                showToast(this,"Failed with error: $studentLoginInfoDeleteError")
                            }
                        }.addOnFailureListener { studentInfoDeleteError ->
                            showToast(this,"Failed with error: $studentInfoDeleteError")
                        }
                    }?.addOnFailureListener { studentDeleteError ->
                        showToast(this,"Failed with error: $studentDeleteError")
                    }
                }.addOnFailureListener { studentSignInError ->
                    showToast(this,"Failed with error: $studentSignInError")
                }
            }.addOnFailureListener { studentLoginInfoError ->
                showToast(this,"Failed with error: $studentLoginInfoError")
            }
    }

    internal fun getStudentData() {
        studentArrayList.clear()
        val tempArray = ArrayList<String>()

        db.collection("StudentInfo").addSnapshotListener(object: EventListener<QuerySnapshot> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if(error != null){
                    Log.e("Firestore Error",error.message.toString())
                    return
                }

                for(dc : DocumentChange in value?.documentChanges!!){
                    if(dc.type == DocumentChange.Type.ADDED){
                        studentArrayList.add(dc.document.toObject(StudentListRecyclerViewDataClass::class.java))
                        tempArray.add(dc.document.data.getValue("SRN").toString())
                    }
                }
                fragmentViewModel.studentSrnData.postValue(tempArray)
                studentListRecyclerViewAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun getFilteredStudentData() {
        studentArrayList.clear()

        if(binding.studentListBatchSelect.text.isEmpty()){
            binding.studentListBatchSelect.setError("Please Choose a Batch",null)
            binding.studentListBatchSelect.requestFocus()
            binding.studentListBatchSelect.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, _, _ ->
                    binding.studentListBatchSelect.error = null
                }
            return
        }
        if(binding.studentListSemesterSelect.text.isEmpty()){
            binding.studentListSemesterSelect.setError("Please Choose a Semester",null)
            binding.studentListSemesterSelect.requestFocus()
            binding.studentListSemesterSelect.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, _, _ ->
                    binding.studentListSemesterSelect.error = null
                }
            return
        }

        db.collection("StudentInfo").whereEqualTo("Batch",binding.studentListBatchSelect.text.toString()).whereEqualTo("Semester",binding.studentListSemesterSelect.text.toString()).addSnapshotListener(object:
            EventListener<QuerySnapshot> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if(error != null){
                    Log.e("Firestore Error",error.message.toString())
                    return
                }

                for(dc : DocumentChange in value?.documentChanges!!){
                    if(dc.type == DocumentChange.Type.ADDED){
                        studentArrayList.add(dc.document.toObject(StudentListRecyclerViewDataClass::class.java))
                    }
                }
                studentListRecyclerViewAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun retrieveBatchSemesterDataList() {
        db.collection("BatchInfo").get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    for(document in it.result!!) {
                        tempArrayListBatchInfo.add(document.data.getValue("Name").toString())
                    }
                    fragmentViewModel.batchData.value = tempArrayListBatchInfo
                    val arrayAdapter = ArrayAdapter(this,
                        R.layout.exposed_dropdown_menu_item_layout,tempArrayListBatchInfo)
                    binding.studentListBatchSelect.setAdapter(arrayAdapter)
                    binding.studentListBatchSelect.setOnItemClickListener { parent, view, position, id ->
                        if(binding.studentListBatchSelect.text.isNotEmpty()){
                            binding.studentListBatchSelect.error = null
                        }
                        binding.studentListSemesterSelect.text.clear()
                        tempArrayListSemesterInfo.clear()
                        val selectedItem = parent.getItemAtPosition(position).toString()

                        db.collection("BatchInfo").whereEqualTo("Name",selectedItem)
                            .addSnapshotListener(object: EventListener<QuerySnapshot> {
                                @SuppressLint("NotifyDataSetChanged")
                                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                                    if(error != null){
                                        Log.e("Firestore Error",error.message.toString())
                                        return
                                    }
                                    for(dc : DocumentChange in value?.documentChanges!!){
                                        if(dc.type == DocumentChange.Type.ADDED){
                                            tempArrayListSemesterInfo.addAll(dc.document.data.getValue("Semester") as Collection<String>)
                                            val addStudentSemesterSelectAdapter = ArrayAdapter(this@ActivityStudentList,
                                                R.layout.exposed_dropdown_menu_item_layout,tempArrayListSemesterInfo)
                                            binding.studentListSemesterSelect.setAdapter(addStudentSemesterSelectAdapter)
                                        }
                                        break
                                    }

                                }
                            })
                    }
                }
            }
    }

    @SuppressLint("SimpleDateFormat")
    internal fun calculateAgeFromDob(birthDate: String, dateFormat:String): Int {

        val sdf = SimpleDateFormat(dateFormat)
        val dob = Calendar.getInstance()
        dob.time = sdf.parse(birthDate)

        val today = Calendar.getInstance()

        val curYear = today.get(Calendar.YEAR)
        val dobYear = dob.get(Calendar.YEAR)

        var age = curYear - dobYear

        try {
            // if dob is month or day is behind today's month or day
            // reduce age by 1
            val curMonth = today.get(Calendar.MONTH+1)
            val dobMonth = dob.get(Calendar.MONTH+1)
            if (dobMonth >curMonth) { // this year can't be counted!
                age--
            } else if (dobMonth == curMonth) { // same month? check for day
                val curDay = today.get(Calendar.DAY_OF_MONTH)
                val dobDay = dob.get(Calendar.DAY_OF_MONTH)
                if (dobDay > curDay) { // this year can't be counted!
                    age--
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return age
    }

}