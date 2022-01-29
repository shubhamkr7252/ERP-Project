package com.app.erp.admin_view.student_list_view

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.activityViewModels
import com.app.erp.R
import com.app.erp.databinding.FragmentEditStudentBinding
import com.app.erp.gloabal_functions.capitalizeWords
import com.app.erp.gloabal_functions.getColorFromAttribute
import com.app.erp.gloabal_functions.showSnackBar
import com.app.erp.gloabal_functions.showToast
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FragmentEditStudent : Fragment(), DatePickerDialog.OnDateSetListener {
    private val db = FirebaseFirestore.getInstance()
    private val viewModel : ActivityStudentListViewModel by activityViewModels()
    private lateinit var binding: FragmentEditStudentBinding
    private lateinit var srnData: String

    private lateinit var parentParentFrameLayout: FrameLayout

    private var day = 0
    private var month = 0
    private var year = 0

    private var savedDay = 0
    private var savedMonth = 0
    private var savedYear = 0

    private lateinit var tempArrayListSemesterInfo: ArrayList<String>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditStudentBinding.inflate(inflater, container, false)
        parentParentFrameLayout = activity?.findViewById(R.id.parentFrameLayout)!!

        binding.backButton.setOnClickListener {
            activity?.onBackPressed()
        }

        tempArrayListSemesterInfo = arrayListOf<String>()

        val bundle = this.arguments
        srnData = bundle!!.getString("srnData")!!

        pickDate()
        getStudentInfo(srnData)

        binding.confirmBut.setOnClickListener {
            checkInput()
        }

        return binding.root
    }

    private fun checkInput(){
        val birthTxt : String = binding.birthday.text.toString().trim{ it <= ' '}
        val activityCall = (activity as ActivityStudentList)
        val ageData: Long = calculateAgeFromDob(birthTxt,"dd MMMM yyyy").toLong()

        if(binding.name.text.toString().isEmpty()) {
            binding.name.setError("Name cannot be Empty",null)
            binding.name.requestFocus()
            return
        }
        if(binding.gender.text.isEmpty()) {
            binding.gender.setError("Gender cannot be Empty",null)
            binding.gender.requestFocus()
            binding.gender.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                binding.gender.error = null
            }
            return
        }
        if(binding.birthday.text.toString().isEmpty()) {
            binding.birthday.performClick()
            showSnackBar(requireContext(), binding.parentFrameLayout,"Birthday field should not be empty")
            return
        }
        if(ageData < 17) {
            binding.birthday.performClick()
            showSnackBar(requireContext(), binding.parentFrameLayout,"The age of student should be above 17 years to register")
            return
        }
        if(binding.batch.text.isEmpty()) {
            binding.batch.setError("Please select Batch",null)
            binding.batch.requestFocus()
            return
        }
        if(binding.semester.text.isEmpty()) {
            binding.semester.setError("Please select semester",null)
            binding.semester.requestFocus()
            binding.semester.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                binding.semester.error = null
            }
            return
        }
        if(binding.phone.text.toString().isEmpty()) {
            binding.phone.setError("Please input phone number",null)
            binding.phone.requestFocus()
            return
        }
        if(!Patterns.PHONE.matcher(binding.phone.text.toString()).matches()) {
            binding.phone.setError("Please input valid phone number",null)
            binding.phone.requestFocus()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        updateStudentInfo()
    }

    private fun updateStudentInfo() {
        val activityCall = (activity as ActivityStudentList)

        val birthTxt : String = binding.birthday.text.toString().trim{ it <= ' '}
        val nameTxt: String = capitalizeWords(binding.name.text.toString().trim{ it <= ' '}).toString()
        val genderTxt: String = binding.gender.text.toString().trim{ it <= ' '}
        val batchTxt : String = binding.batch.text.toString().trim{ it <= ' '}
        val semesterTxt : String = binding.semester.text.toString().trim{ it <= ' '}
        val phoneTxt : Long = binding.phone.text.toString().toLong()
        val ageTxt: Long = activityCall.calculateAgeFromDob(birthTxt,"dd MMMM yyyy").toLong()

        val studentInfo: MutableMap<String, Any> = HashMap()
        studentInfo["Name"] = nameTxt
        studentInfo["Birthday"] = birthTxt
        studentInfo["Age"] = ageTxt
        studentInfo["Gender"] = genderTxt
        studentInfo["Batch"] = batchTxt
        studentInfo["Semester"] = semesterTxt
        studentInfo["Phone"] = phoneTxt

        db.collection("StudentInfo").document(srnData).update(studentInfo).addOnSuccessListener {
            showSnackBar(requireContext(), parentParentFrameLayout, "Data Updated")
            activityCall.getStudentData()
            activity?.onBackPressed()

        }.addOnFailureListener {
            showSnackBar(requireContext(), binding.parentFrameLayout, "Error occurred")
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun getStudentInfo(srn: String){
        db.collection("StudentInfo").document(srn).get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    binding.name.setText(it.result.data?.getValue("Name").toString(),null)
                    binding.gender.setText(it.result.data?.getValue("Gender").toString(),null)
                    binding.birthday.setText(it.result.data?.getValue("Birthday").toString(),null)
                    binding.batch.setText(it.result.data?.getValue("Batch").toString(),null)
                    viewModel.batchData.observe(viewLifecycleOwner) { array ->
                        val arrayAdapter = ArrayAdapter(
                            requireContext(),
                            R.layout.exposed_dropdown_menu_item_layout, array
                        )
                        binding.batch.setAdapter(arrayAdapter)
                        if (binding.batch.text.isNotEmpty()) {
                            binding.batch.error = null
                        }
                        binding.batch.setOnItemClickListener { parent, _, position, _ ->
                            val selectedItem = parent.getItemAtPosition(position).toString()
                            binding.semester.text = null
                            getSemesterInfo(selectedItem)
                        }
                    }
                    binding.semester.setText(it.result.data?.getValue("Semester").toString(),null)
                    db.collection("BatchInfo").whereEqualTo("Name",binding.batch.text.toString())
                        .addSnapshotListener(object: EventListener<QuerySnapshot> {
                            @SuppressLint("NotifyDataSetChanged")
                            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                                if(error != null){
                                    Log.e("Firestore Error",error.message.toString())
                                    return
                                }
                                for(dc : DocumentChange in value?.documentChanges!!){
                                    val temp = ArrayList<String>()
                                    if(dc.type == DocumentChange.Type.ADDED){
                                        temp.clear()
                                        temp.addAll(dc.document.data.getValue("Semester") as Collection<String>)
                                        val addStudentSemesterSelectAdapter = ArrayAdapter(requireContext(),
                                            R.layout.exposed_dropdown_menu_item_layout,temp)
                                        binding.semester.setAdapter(addStudentSemesterSelectAdapter)
                                    }
                                    break
                                }

                            }
                        })
                    binding.phone.setText(it.result.data?.getValue("Phone").toString(),null)
                }
            }
    }

    private fun getSemesterInfo(batchData: String){
        tempArrayListSemesterInfo.clear()
        db.collection("BatchInfo").whereEqualTo("Name",batchData)
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
                            val addStudentSemesterSelectAdapter = ArrayAdapter(requireContext(),
                                R.layout.exposed_dropdown_menu_item_layout,tempArrayListSemesterInfo)
                            binding.semester.setAdapter(addStudentSemesterSelectAdapter)
                        }
                        break
                    }

                }
            })
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDateCalendar() {
        val cal = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
    }

    private fun pickDate() {
        binding.birthday.setOnClickListener {
            getDateCalendar()
            DatePickerDialog(requireActivity(),this,year,month,day).show()
        }
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        savedMonth = month
        savedYear = year

        getDateCalendar()
        val tempMonth :String = DateFormatSymbols().months[savedMonth].toString()
        binding.birthday.setText("$savedDay $tempMonth $savedYear")
    }

    @SuppressLint("SimpleDateFormat")
    fun calculateAgeFromDob(birthDate: String, dateFormat:String): Int {

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