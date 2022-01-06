package com.example.erp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Patterns
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
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FragmentAddTeacher : Fragment(), DatePickerDialog.OnDateSetListener {
    private val viewModel : ActivityTeacherListViewModel by activityViewModels()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var backButton: MaterialButton
    private lateinit var genderSelect: AutoCompleteTextView
    private lateinit var teacherBirthday: AutoCompleteTextView
    private lateinit var teacherName: EditText
    private lateinit var teacherCode: EditText
    private lateinit var teacherPhone: EditText
    private lateinit var teacherEmail: EditText
    private lateinit var courseCode: EditText
    private lateinit var password: EditText
    private lateinit var cnfPassword: EditText
    private lateinit var confirmBut: MaterialButton
    private lateinit var courseSelect: AutoCompleteTextView
    private lateinit var progressBar: RelativeLayout
    private lateinit var parentFrameLayout: FrameLayout
    private lateinit var parentParentFrameLayout: FrameLayout

    private var day = 0
    private var month = 0
    private var year = 0

    private var savedDay = 0
    private var savedMonth = 0
    private var savedYear = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_teacher, container, false)
        backButton = view.findViewById(R.id.backButton)
        confirmBut = view.findViewById(R.id.confirmBut)

        genderSelect = view.findViewById(R.id.genderSelect)
        teacherBirthday = view.findViewById(R.id.teacherBirthday)
        teacherName = view.findViewById(R.id.teacherName)
        teacherCode = view.findViewById(R.id.teacherCode)
        teacherPhone = view.findViewById(R.id.teacherPhone)
        teacherEmail = view.findViewById(R.id.teacherEmail)
        password = view.findViewById(R.id.password)
        cnfPassword = view.findViewById(R.id.cnfPassword)
        courseSelect = view.findViewById(R.id.courseSelect)
        courseCode = view.findViewById(R.id.courseCode)
        progressBar = view.findViewById(R.id.progressBar)
        parentFrameLayout = view.findViewById(R.id.parentFrameLayout)
        parentParentFrameLayout = activity?.findViewById(R.id.parentFrameLayout)!!

        backButton.setOnClickListener {
            activity?.onBackPressed()
        }

        teacherBirthday.setOnClickListener{
            pickDate()
        }

        val genderList = resources.getStringArray(R.array.genders)
        val genderArrayAdapter = ArrayAdapter(requireContext(),R.layout.exposed_dropdown_menu_item_layout,genderList)
        genderSelect.setAdapter(genderArrayAdapter)

        viewModel.courseData.observe(viewLifecycleOwner,{
            val arrayAdapter = ArrayAdapter(requireContext(),R.layout.exposed_dropdown_menu_item_layout,it)
            courseSelect.setAdapter(arrayAdapter)
            courseSelect.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                if (courseSelect.text.isNotEmpty()) {
                    courseSelect.error = null
                }
                val selectedItem = parent.getItemAtPosition(position).toString()
                db.collection("CourseInfo").whereEqualTo("CourseName",selectedItem).get()
                    .addOnCompleteListener { querySnap ->
                        if (querySnap.isSuccessful) {
                            for (document in querySnap.result!!) {
                                courseCode.setText(
                                    document.data.getValue("CourseCode").toString()
                                )
                            }
                        }
                    }
            }
        })

        confirmBut.setOnClickListener {
            checkInput()
        }

        return view
    }

    private fun checkInput(){
        val birthTxt: String = teacherBirthday.text.toString().trim{ it <= ' '}
        if(teacherName.text.toString().isEmpty()) {
            teacherName.setError("Please input name",null)
            teacherName.requestFocus()
            return
        }
        if(courseSelect.text.isEmpty()) {
            courseSelect.setError("Please select Course",null)
            courseSelect.requestFocus()
            return
        }
        if(genderSelect.text.isEmpty()) {
            genderSelect.setError("Please select Gender",null)
            genderSelect.requestFocus()
            genderSelect.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                genderSelect.error = null
            }
            return
        }
        if(teacherBirthday.text.toString().isEmpty()) {
            teacherBirthday.performClick()
            Toast.makeText(activity,"Birthday filed should not be empty",Toast.LENGTH_SHORT).show()
            return
        }
        if(calculateAgeFromDob(birthTxt,"dd MMMM yyyy") < 18) {
            teacherBirthday.performClick()
            Toast.makeText(activity,"The age of teacher should be above 18 years to register",Toast.LENGTH_SHORT).show()
            return
        }
        if(teacherCode.text.toString().isEmpty()) {
            teacherCode.setError("Please input Teacher code",null)
            teacherCode.requestFocus()
            return
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(teacherEmail.text.toString()).matches()) {
            if(teacherEmail.text.toString().isEmpty()) {
                teacherEmail.setError("Please input Email",null)
            }
            else {
                teacherEmail.setError("Please input a valid Email",null)
            }
            teacherEmail.requestFocus()
            return
        }
        if(teacherPhone.text.toString().isEmpty()) {
            teacherPhone.setError("Please input Phone Number",null)
            teacherPhone.requestFocus()
            return
        }
        if(!Patterns.PHONE.matcher(teacherPhone.text.toString()).matches()) {
            teacherPhone.setError("Please input valid Phone Number",null)
            teacherPhone.requestFocus()
            return
        }
        if(password.text.toString().isEmpty()) {
            password.setError("Please input Password",null)
            password.requestFocus()
            return
        }
        else if(password.text.toString().length < 6) {
            password.setError("The password cannot be less than six characters",null)
            password.requestFocus()
            return
        }
        if(cnfPassword.text.toString().isEmpty()) {
            cnfPassword.setError("Please input Password",null)
            cnfPassword.requestFocus()
            return
        }
        else if(cnfPassword.text.toString().length < 6) {
            cnfPassword.setError("The password cannot be less than six characters",null)
            cnfPassword.requestFocus()
            return
        }
        if(password.text.toString() != cnfPassword.text.toString()) {
            cnfPassword.setError("The Passwords doesn't match",null)
            cnfPassword.requestFocus()
            return
        }

        progressBar.visibility = View.VISIBLE
        checkTeacherExist()
    }

    private fun checkTeacherExist() {
        val codeTxt: String = teacherCode.text.toString().trim{ it <= ' '}.uppercase()
        val emailTxt: String = teacherEmail.text.toString().trim{ it <= ' '}.lowercase()
        val phoneTxt: Long = teacherPhone.text.toString().toLong()

        db.collection("TeacherLoginInfo").document(codeTxt).get()
            .addOnSuccessListener { document ->
                if (document.data?.getValue("Code").toString() == codeTxt) {
                    Snackbar.make(parentFrameLayout,"Teacher Code already exist",
                        Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                        .show()
                    progressBar.visibility = View.GONE
                }else{
                    db.collection("TeacherLoginInfo")
                        .whereEqualTo("Email", emailTxt)
                        .get()
                        .addOnSuccessListener { documents ->
                            var temp: Boolean = false
                            for (document_it in documents) {
                                Snackbar.make(parentFrameLayout,"Email already exist",
                                    Snackbar.LENGTH_SHORT)
                                    .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                                    .show()
                                temp = true
                                progressBar.visibility = View.GONE
                                break
                            }
                            if(!temp){
                                db.collection("StudentInfo")
                                    .whereEqualTo("Phone", phoneTxt)
                                    .get()
                                    .addOnSuccessListener { documentsIt ->
                                        var tempSecond: Boolean = false
                                        for (document_it_two in documentsIt) {
                                            Snackbar.make(parentFrameLayout,"Phone number already Registered",
                                                Snackbar.LENGTH_SHORT)
                                                .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                                                .show()
                                            tempSecond = true
                                            progressBar.visibility = View.GONE
                                            break
                                        }
                                        if(!tempSecond){
                                            addTeacher()
                                        }
                                    }
                            }
                        }
                }
            }
    }

    private fun addTeacher() {
        val nameTxt: String = viewModel.capitalizeWords(teacherName.text.toString().trim{ it <= ' '}).toString()
        val codeTxt: String = teacherCode.text.toString().trim{ it <= ' '}.uppercase()
        val birthTxt: String = teacherBirthday.text.toString().trim{ it <= ' '}
        val genderTxt: String = genderSelect.text.toString().trim{ it <= ' '}
        val ageTxt: Long = calculateAgeFromDob(birthTxt,"dd MMMM yyyy").toLong()
        val phoneTxt: Long = teacherPhone.text.toString().toLong()
        val emailTxt: String = teacherEmail.text.toString().trim{ it <= ' '}.lowercase()
        val courseTxt: String = courseCode.text.toString().trim { it <= ' ' }.uppercase()
        val courseTxtArray = ArrayList<String>()
        courseTxtArray.add(courseTxt)
        val passTxt: String = password.text.toString().trim{ it <= ' '}

        val teacherCredentials: MutableMap<String, Any> = HashMap()
        teacherCredentials["Code"] = codeTxt
        teacherCredentials["Email"] = emailTxt
        teacherCredentials["Password"] = passTxt

        db.collection("TeacherLoginInfo").document(codeTxt).set(teacherCredentials).addOnCompleteListener {
            val teacherInfo: MutableMap<String, Any> = HashMap()
            teacherInfo["Name"] = nameTxt
            teacherInfo["Birthday"] = birthTxt
            teacherInfo["Age"] = ageTxt
            teacherInfo["Gender"] = genderTxt
            teacherInfo["Code"] = codeTxt
            teacherInfo["Email"] = emailTxt
            teacherInfo["Phone"] = phoneTxt
            db.collection("TeacherInfo").document(codeTxt).set(teacherInfo).addOnSuccessListener {
                val teacherInfoCourse: MutableMap<String, Any> = HashMap()
                teacherInfoCourse["Course"] = courseTxtArray
                db.collection("TeacherInfo").document(codeTxt).update("Course",courseTxtArray).addOnSuccessListener {
                    val updates = hashMapOf<String, Any>(
                        "Teacher" to FieldValue.arrayUnion(codeTxt)
                    )
                    db.collection("CourseInfo").document(courseTxt).update(updates).addOnSuccessListener {
                        Snackbar.make(parentParentFrameLayout,"Teacher added",
                            Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                            .show()
                        progressBar.visibility = View.GONE
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
        }.addOnFailureListener {
            progressBar.visibility = View.GONE
            Snackbar.make(parentFrameLayout,"Error occurred",
                Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                .show()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDateCalendar() {
        val cal = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
    }

    private fun pickDate() {
        getDateCalendar()
        DatePickerDialog(requireContext(),this,year,month,day).show()
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        savedMonth = month
        savedYear = year

        getDateCalendar()
        val tempMonth :String = DateFormatSymbols().months[savedMonth].toString()
        teacherBirthday.setText("$savedDay $tempMonth $savedYear")
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

    private fun getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true
    ): Int {
        activity?.theme?.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }
}