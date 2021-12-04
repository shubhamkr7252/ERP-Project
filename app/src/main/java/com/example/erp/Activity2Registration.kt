package com.example.erp

import android.R.attr
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.HashMap
import android.widget.AdapterView.OnItemClickListener
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.widget.doBeforeTextChanged
import androidx.core.widget.doOnTextChanged
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.storage.FirebaseStorage
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import android.graphics.Bitmap
import android.os.PersistableBundle
import android.util.Log
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import android.R.attr.previewImage
import android.provider.MediaStore

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.lifecycle.ViewModelProvider
import java.io.File

class Activity2Registration : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var registrationLayout: RelativeLayout

    lateinit var registrationViewModel: Activity2RegistrationViewModel

    private lateinit var name: EditText
    private lateinit var srn: EditText
    private lateinit var mail: EditText
    private lateinit var phone: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText

    private lateinit var semesterSelect: AutoCompleteTextView
    private lateinit var courseSelect: AutoCompleteTextView
    private lateinit var genderSelect: AutoCompleteTextView
    private lateinit var birthdayEditText: EditText
    private lateinit var countryCode: AutoCompleteTextView

    private lateinit var backButton: MaterialButton
//    private lateinit var selectBtn: MaterialButton
//    private lateinit var deleteBtn: MaterialButton
//    private lateinit var profileImage: ImageView
    private lateinit var linearProgressIndicator: LinearProgressIndicator
    private lateinit var mScrollView: ScrollView

//    private var profileImageUri: Uri? = null

    private var day = 0
    private var month = 0
    private var year = 0

    private var savedDay = 0
    private var savedMonth = 0
    private var savedYear = 0

    override fun onResume() {
        super.onResume()

        birthdayEditText = findViewById(R.id.birthdayEditText)
        semesterSelect = findViewById(R.id.semesterSelect)
        courseSelect = findViewById(R.id.courseSelect)
        countryCode = findViewById(R.id.countryCode)
        genderSelect = findViewById(R.id.genderSelect)

        val semesterList = resources.getStringArray(R.array.semesters)
        val courseList = resources.getStringArray(R.array.courses)
        val countryCodeList = resources.getStringArray(R.array.countryCodes)
        val genderList = resources.getStringArray(R.array.genders)

        val semesterArrayAdapter = ArrayAdapter(this,R.layout.dropdown_item,semesterList)
        val courseArrayAdapter = ArrayAdapter(this,R.layout.dropdown_item,courseList)
        val countryCodeArrayAdapter = ArrayAdapter(this,R.layout.dropdown_item,countryCodeList)
        val genderArrayAdapter = ArrayAdapter(this,R.layout.dropdown_item,genderList)

        semesterSelect.setAdapter(semesterArrayAdapter)
        courseSelect.setAdapter(courseArrayAdapter)
        countryCode.setAdapter(countryCodeArrayAdapter)
        genderSelect.setAdapter(genderArrayAdapter)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity2_registration)

        auth = Firebase.auth

        val signUp = findViewById<Button>(R.id.registerBut)
        backButton = findViewById(R.id.backButton)

        linearProgressIndicator = findViewById(R.id.linearProgressIndicator)
        linearProgressIndicator.visibility = View.GONE

//        profileImage = findViewById(R.id.profileImage)

        registrationViewModel = ViewModelProvider(this)[Activity2RegistrationViewModel::class.java]

//        profileImageUri = savedInstanceState?.getParcelable("profileImageUri")
//        profileImageUri?.let {
//            profileImage.setImageURI(it)
//        }

        backButton.setOnClickListener {
            onBackPressed()
        }
//        selectBtn = findViewById(R.id.selectBtn)
//        deleteBtn = findViewById(R.id.deleteBtn)

        initializeAutoCompleteTextViewLayoutAndData()

//        val selectImageFromGalleryResult = registerForActivityResult(
//            ActivityResultContracts.GetContent()) { uri: Uri? ->
//            uri?.let {
//                //val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,uri)
//                registrationViewModel.imageRefData.value = uri
//            }
//        }
//
//        registrationViewModel.imageRefData.observe(this, androidx.lifecycle.Observer {
//            profileImage.setImageURI(registrationViewModel.imageRefData.value)
//        })

//        selectBtn.setOnClickListener {
//            profileImage.visibility = View.VISIBLE
//            selectImageFromGalleryResult.launch("image/*")
//        }
//
//        deleteBtn.setOnClickListener {
//            registrationViewModel.imageRefData.value = null
//            profileImage.visibility = View.GONE
//        }

        pickDate()

        signUp.setOnClickListener {
            signUpUser()
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        //outState.putParcelable("profileImageUri", profileImageUri)
        super.onSaveInstanceState(outState)
        mScrollView = findViewById(R.id.mScrollView)
        outState.putIntArray(
            "ARTICLE_SCROLL_POSITION",
            intArrayOf(mScrollView.scrollX, mScrollView.scrollY)
        )
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mScrollView = findViewById(R.id.mScrollView)
        val position = savedInstanceState.getIntArray("ARTICLE_SCROLL_POSITION")
        if (position != null) mScrollView.post { mScrollView.scrollTo(position[0], position[1]) }
    }

    private fun initializeAutoCompleteTextViewLayoutAndData(){

        birthdayEditText = findViewById(R.id.birthdayEditText)
        semesterSelect = findViewById(R.id.semesterSelect)
        courseSelect = findViewById(R.id.courseSelect)
        countryCode = findViewById(R.id.countryCode)
        genderSelect = findViewById(R.id.genderSelect)

        val semesterList = resources.getStringArray(R.array.semesters)
        val courseList = resources.getStringArray(R.array.courses)
        val countryCodeList = resources.getStringArray(R.array.countryCodes)
        val genderList = resources.getStringArray(R.array.genders)

        val semesterArrayAdapter = ArrayAdapter(this,R.layout.dropdown_item,semesterList)
        val courseArrayAdapter = ArrayAdapter(this,R.layout.dropdown_item,courseList)
        val countryCodeArrayAdapter = ArrayAdapter(this,R.layout.dropdown_item,countryCodeList)
        val genderArrayAdapter = ArrayAdapter(this,R.layout.dropdown_item,genderList)

        semesterSelect.setAdapter(semesterArrayAdapter)
        courseSelect.setAdapter(courseArrayAdapter)
        countryCode.setAdapter(countryCodeArrayAdapter)
        genderSelect.setAdapter(genderArrayAdapter)
    }

    private fun signUpUser() {

        name = findViewById(R.id.name)
        srn = findViewById(R.id.SRN)
        mail = findViewById(R.id.mail)
        phone = findViewById(R.id.phone)
        password = findViewById(R.id.password)
        confirmPassword = findViewById(R.id.cnfPassword)

        registrationLayout = findViewById(R.id.registrationLayout)

        val birthTxt : String = birthdayEditText.text.toString().trim{ it <= ' '}

        if(name.text.toString().isEmpty()) {
            name.setError("Please input your name",null)
            name.requestFocus()
            return
        }
        if(genderSelect.text.isEmpty()) {
            genderSelect.setError("Please select your course",null)
            genderSelect.requestFocus()
            genderSelect.onItemClickListener = OnItemClickListener { _, _, _, _ ->
                courseSelect.error = null
            }
            return
        }
        if(birthdayEditText.text.toString().isEmpty()) {
            birthdayEditText.performClick()
            Toast.makeText(this,"Birthday filed should not be empty",Toast.LENGTH_SHORT).show()
            return
        }
        if(calculateAgeFromDob(birthTxt,"dd MMMM yyyy") < 17) {
            birthdayEditText.performClick()
            Toast.makeText(this,"You should be above 17 years to register",Toast.LENGTH_SHORT).show()
            return
        }
        if(srn.text.toString().isEmpty()) {
            srn.setError("Please input your SRN",null)
            srn.requestFocus()
            return
        }
        if(courseSelect.text.isEmpty()) {
            courseSelect.setError("Please select your course",null)
            courseSelect.requestFocus()
            courseSelect.onItemClickListener = OnItemClickListener { _, _, _, _ ->
                courseSelect.error = null
            }
            return
        }
        if(semesterSelect.text.isEmpty()) {
            semesterSelect.setError("Please select your semester",null)
            semesterSelect.requestFocus()
            semesterSelect.onItemClickListener = OnItemClickListener { _, _, _, _ ->
                semesterSelect.error = null
            }
            return
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(mail.text.toString()).matches()) {
            if(mail.text.toString().isEmpty()) {
                mail.setError("Please input your email",null)
            }
            else {
                mail.setError("Please input a valid email",null)
            }
            mail.requestFocus()
            return
        }
        if(countryCode.text.toString().isEmpty()){
            countryCode.setError("Please select your semester",null)
            countryCode.requestFocus()
            countryCode.onItemClickListener = OnItemClickListener { _, _, _, _ ->
                countryCode.error = null
            }
            return
        }
        if(!Patterns.PHONE.matcher(phone.text.toString()).matches()) {
            phone.setError("Please input valid phone number",null)
            phone.requestFocus()
            return
        }
        if(password.text.toString().isEmpty()) {
            password.setError("Please input password",null)
            password.requestFocus()
            return
        }
        else if(password.text.toString().length < 6) {
            password.setError("The password cannot be less than six characters",null)
            password.requestFocus()
            return
        }
        if(confirmPassword.text.toString().isEmpty()) {
            confirmPassword.setError("Please input password",null)
            confirmPassword.requestFocus()
            return
        }
        else if(confirmPassword.text.toString().length < 6) {
            confirmPassword.setError("The password cannot be less than six characters",null)
            confirmPassword.requestFocus()
            return
        }
        if(password.text.toString() != confirmPassword.text.toString()) {
            confirmPassword.setError("The passwords doesn't match",null)
            confirmPassword.requestFocus()
            return
        }

        val nameTxt: String = name.text.toString().trim{ it <= ' '}
        val genderTxt: String = genderSelect.text.toString().trim{ it <= ' '}
        val srnTxt: String = srn.text.toString().lowercase().trim{ it <= ' '}
        val courseTxt : String = courseSelect.text.toString().trim{ it <= ' '}
        val semesterTxt : String = semesterSelect.text.toString().trim{ it <= ' '}
        val mailTxt: String = mail.text.toString().lowercase().trim{ it <= ' '}
        val countryCode : String = countryCode.text.toString().trim{ it <= ' '}
        val phoneTxt : String = phone.text.toString().trim{ it <= ' '}
        val passTxt: String = password.text.toString().trim{ it <= ' '}

        linearProgressIndicator.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(mailTxt,passTxt)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val uid = auth.currentUser?.uid
                    val db = FirebaseFirestore.getInstance()

                    val studentInfo: MutableMap<String, Any> = HashMap()
                    studentInfo["Name"] = nameTxt
                    studentInfo["Birthday"] = birthTxt
                    studentInfo["Age"] = calculateAgeFromDob(birthTxt,"dd MMMM yyyy")
                    studentInfo["Gender"] = genderTxt
                    studentInfo["SRN"] = srnTxt
                    studentInfo["Course"] = courseTxt
                    studentInfo["Semester"] = semesterTxt
                    studentInfo["Email"] = mailTxt
                    studentInfo["Country Code"] = countryCode
                    studentInfo["Phone"] = phoneTxt
                    studentInfo["UID"] = auth.currentUser?.uid.toString()

                    if (uid != null) {
                        db.collection("studentInfo").document(auth.currentUser?.email.toString()).set(studentInfo)
                    }
//                    if(registrationViewModel.imageRefData.value != null){
//                        uploadImage()
//                    }
                    val intent = Intent(this@Activity2Registration,AfterLoginNavigation::class.java)
                    startActivity(intent)
                    Toast.makeText(this,"Registered successfully",Toast.LENGTH_SHORT).show()
                    linearProgressIndicator.visibility = View.GONE
                } else {
                    Toast.makeText(this@Activity2Registration,task.exception!!.message.toString(),Toast.LENGTH_LONG).show()
                }
            }
    }
//    private fun uploadImage() {
//        val imageFileName = auth.currentUser?.email
//        val storageReference = FirebaseStorage.getInstance().getReference("profileImages/$imageFileName.jpg")
//        storageReference.putFile(registrationViewModel.imageRefData.value!!)
//            .addOnSuccessListener {
//                Toast.makeText(this,"Uploaded",Toast.LENGTH_SHORT).show()
//            }.addOnFailureListener{
//                Toast.makeText(this,"Failed to upload image",Toast.LENGTH_SHORT).show()
//            }
//    }

    @SuppressLint("SimpleDateFormat")
    private fun getDateCalendar() {
        val cal = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
    }

    private fun pickDate() {
        birthdayEditText.setOnClickListener {
            getDateCalendar()
            DatePickerDialog(this,this,year,month,day).show()
        }
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        savedMonth = month
        savedYear = year

        getDateCalendar()
        val tempMonth :String = DateFormatSymbols().months[savedMonth].toString()
        birthdayEditText.setText("$savedDay $tempMonth $savedYear")
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