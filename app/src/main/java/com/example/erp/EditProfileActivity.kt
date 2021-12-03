package com.example.erp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    lateinit var viewModel: EditProfileActivityViewModel

    private lateinit var editProfileName: EditText
    private lateinit var editProfileSRN: EditText
    private lateinit var editProfileMail: EditText
    private lateinit var editProfilePhone: EditText
    private lateinit var editProfileBirthdayEditText: EditText
    private lateinit var editProfileGenderSelect: AutoCompleteTextView
    private lateinit var editProfileCourseSelect: AutoCompleteTextView
    private lateinit var editProfileSemesterSelect: AutoCompleteTextView
    private lateinit var editProfileCountryCode: AutoCompleteTextView
    private lateinit var editProfileBackButton: MaterialButton
    private lateinit var editProfileSelectBtn: MaterialButton
    private lateinit var editProfileDeleteBtn: MaterialButton
    private lateinit var editProfileScrollViewLayout: ScrollView
    private lateinit var editProfileProfileImage: ImageView

    private var day = 0
    private var month = 0
    private var year = 0

    private var savedDay = 0
    private var savedMonth = 0
    private var savedYear = 0

    override fun onResume() {
        super.onResume()
        editProfileGenderSelect = findViewById(R.id.editProfileGenderSelect)
        val editProfileGenderList = resources.getStringArray(R.array.genders)
        val editProfileGenderArrayAdapter = ArrayAdapter(this,R.layout.dropdown_item,editProfileGenderList)
        editProfileGenderSelect.setAdapter(editProfileGenderArrayAdapter)

        editProfileCourseSelect = findViewById(R.id.editProfileCourseSelect)
        val editProfileCourseList = resources.getStringArray(R.array.courses)
        val editProfileCourseArrayAdapter = ArrayAdapter(this,R.layout.dropdown_item,editProfileCourseList)
        editProfileCourseSelect.setAdapter(editProfileCourseArrayAdapter)

        editProfileSemesterSelect = findViewById(R.id.editProfileSemesterSelect)
        val editProfileSemesterList = resources.getStringArray(R.array.semesters)
        val editProfileSemesterArrayAdapter = ArrayAdapter(this,R.layout.dropdown_item,editProfileSemesterList)
        editProfileSemesterSelect.setAdapter(editProfileSemesterArrayAdapter)

        editProfileCountryCode = findViewById(R.id.editProfileCountryCode)
        val editProfileCountryCodeList = resources.getStringArray(R.array.countryCodes)
        val editProfileCountryCodeArrayAdapter = ArrayAdapter(this,R.layout.dropdown_item,editProfileCountryCodeList)
        editProfileCountryCode.setAdapter(editProfileCountryCodeArrayAdapter)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        viewModel = ViewModelProvider(this)[EditProfileActivityViewModel::class.java]

        editProfileBackButton = findViewById(R.id.editProfileBackButton)
        editProfileProfileImage = findViewById(R.id.editProfileProfileImage)
        editProfileSelectBtn = findViewById(R.id.editProfileSelectBtn)
        editProfileDeleteBtn = findViewById(R.id.editProfileDeleteBtn)

        auth = Firebase.auth
        val db = Firebase.firestore
        val settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        db.firestoreSettings = settings

        editProfileBackButton.setOnClickListener {
            onBackPressed()
        }

        initializeAutoCompleteTextViewLayoutAndData()
        if(savedInstanceState == null) {
            retrieveData()
            retrieveImage()
        }

        editProfileBirthdayEditText = findViewById(R.id.editProfileBirthdayEditText)
        pickDate()

        val selectImageFromGalleryResult = registerForActivityResult(
            ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,uri)
                viewModel.imageRefData.value = bitmap
            }
        }

        viewModel.imageRefData.observe(this, androidx.lifecycle.Observer {
            editProfileProfileImage.setImageBitmap(viewModel.imageRefData.value)
        })

        editProfileSelectBtn.setOnClickListener {
            selectImageFromGalleryResult.launch("image/*")
        }

        editProfileDeleteBtn.setOnClickListener {
            viewModel.imageRefData.value = null
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        editProfileScrollViewLayout = findViewById(R.id.editProfileScrollViewLayout)
        outState.putIntArray(
            "ARTICLE_SCROLL_POSITION",
            intArrayOf(editProfileScrollViewLayout.scrollX, editProfileScrollViewLayout.scrollY)
        )
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        editProfileScrollViewLayout = findViewById(R.id.editProfileScrollViewLayout)
        val position = savedInstanceState.getIntArray("ARTICLE_SCROLL_POSITION")
        if (position != null) editProfileScrollViewLayout.post { editProfileScrollViewLayout.scrollTo(position[0], position[1]) }
    }

    private fun retrieveImage() {
        val authMail = auth.currentUser?.email
        val imageRefUrl = FirebaseStorage.getInstance().reference.child("profileImages/$authMail.jpg").downloadUrl.addOnSuccessListener {
            val imageRef = FirebaseStorage.getInstance().reference.child("profileImages/$authMail.jpg")
            retrieveImageCore(imageRef)
        }
    }
    private fun retrieveImageCore(imageRef : com.google.firebase.storage.StorageReference) {
        val localFile = File.createTempFile("tempImage","jpg")
        imageRef.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            viewModel.imageRefData.value = bitmap
        }
    }

    private fun initializeAutoCompleteTextViewLayoutAndData() {

        editProfileGenderSelect = findViewById(R.id.editProfileGenderSelect)
        val editProfileGenderList = resources.getStringArray(R.array.genders)
        val editProfileGenderArrayAdapter = ArrayAdapter(this,R.layout.dropdown_item,editProfileGenderList)
        editProfileGenderSelect.setAdapter(editProfileGenderArrayAdapter)

        editProfileCourseSelect = findViewById(R.id.editProfileCourseSelect)
        val editProfileCourseList = resources.getStringArray(R.array.courses)
        val editProfileCourseArrayAdapter = ArrayAdapter(this,R.layout.dropdown_item,editProfileCourseList)
        editProfileCourseSelect.setAdapter(editProfileCourseArrayAdapter)

        editProfileSemesterSelect = findViewById(R.id.editProfileSemesterSelect)
        val editProfileSemesterList = resources.getStringArray(R.array.semesters)
        val editProfileSemesterArrayAdapter = ArrayAdapter(this,R.layout.dropdown_item,editProfileSemesterList)
        editProfileSemesterSelect.setAdapter(editProfileSemesterArrayAdapter)

        editProfileCountryCode = findViewById(R.id.editProfileCountryCode)
        val editProfileCountryCodeList = resources.getStringArray(R.array.countryCodes)
        val editProfileCountryCodeArrayAdapter = ArrayAdapter(this,R.layout.dropdown_item,editProfileCountryCodeList)
        editProfileCountryCode.setAdapter(editProfileCountryCodeArrayAdapter)
    }

    private fun uploadImage() {
        val imageFileNameRef = findViewById<EditText>(R.id.SRN)
        val imageFileName: String = imageFileNameRef.text.toString().uppercase().trim{ it <= ' '}
        val storageReference = FirebaseStorage.getInstance().getReference("profileImages/$imageFileName.jpg")
        storageReference.putFile(viewModel.imageRefData.value.toString().toUri())
            .addOnSuccessListener {
            }.addOnFailureListener{
                Toast.makeText(this,"Failed to upload image",Toast.LENGTH_SHORT).show()
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
        editProfileBirthdayEditText.setOnClickListener {
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
        editProfileBirthdayEditText.setText("$savedDay $tempMonth $savedYear")
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

    private fun retrieveData() {
        val nameTxt = StringBuffer()
        val srnTxt = StringBuffer()
        val mailTxt = StringBuffer()
        val phoneTxt = StringBuffer()
        val semTxt = StringBuffer()
        val courseTxt = StringBuffer()
        val birthTxt = StringBuffer()
        val genderTxt = StringBuffer()
        val countryCodeText = StringBuffer()

        editProfileName = findViewById(R.id.editProfileName)
        editProfileSRN = findViewById(R.id.editProfileSRN)
        editProfileMail = findViewById(R.id.editProfileMail)
        editProfilePhone = findViewById(R.id.editProfilePhone)
        editProfileBirthdayEditText = findViewById(R.id.editProfileBirthdayEditText)
        editProfileProfileImage = findViewById(R.id.editProfileProfileImage)
        editProfileGenderSelect = findViewById(R.id.editProfileGenderSelect)
        editProfileCourseSelect = findViewById(R.id.editProfileCourseSelect)
        editProfileSemesterSelect = findViewById(R.id.editProfileSemesterSelect)
        editProfileCountryCode = findViewById(R.id.editProfileCountryCode)

        val authMail = auth.currentUser?.email

        var tempTxt = StringBuffer()
        db.collection("studentInfo").get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    for(document in it.result!!) {
                        val temp = document.id
                        if(document.id == authMail){
                            editProfileName.setText(nameTxt.append(document.data.getValue("Name")).toString())
                            editProfileSRN.setText(srnTxt.append(document.data.getValue("SRN")).toString())
                            editProfileMail.setText(mailTxt.append(document.data.getValue("Email")).toString())
                            tempTxt = countryCodeText.append(document.data.getValue("Country Code"))
                            editProfileCountryCode.setText(tempTxt.toString(),false)
                            editProfilePhone.setText(phoneTxt.append(document.data.getValue("Phone")).toString())
                            tempTxt = courseTxt.append(document.data.getValue("Course"))
                            editProfileCourseSelect.setText(tempTxt.toString(),false)
                            tempTxt = semTxt.append(document.data.getValue("Semester"))
                            editProfileSemesterSelect.setText(tempTxt.toString(),false)
                            editProfileBirthdayEditText.setText(birthTxt.append(document.data.getValue("Birthday")).toString())
                            tempTxt = genderTxt.append(document.data.getValue("Gender"))
                            editProfileGenderSelect.setText(tempTxt.toString(),false)
                        }
                    }

                }
            }
    }
}