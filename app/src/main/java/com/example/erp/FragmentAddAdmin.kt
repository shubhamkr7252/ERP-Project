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
import androidx.annotation.ColorInt
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class FragmentAddAdmin : Fragment(), DatePickerDialog.OnDateSetListener {
    private lateinit var backButton: MaterialButton
    private lateinit var code: EditText
    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var phone: EditText
    private lateinit var password: EditText
    private lateinit var cnfPassword: EditText
    private lateinit var gender: AutoCompleteTextView
    private lateinit var birthday: AutoCompleteTextView
    private lateinit var parentFrameLayout: FrameLayout
    private lateinit var progressBar: RelativeLayout
    private lateinit var confirmBut: MaterialButton
    private lateinit var parentParentFrameLayout: FrameLayout

    private val db = FirebaseFirestore.getInstance()

    private var day = 0
    private var month = 0
    private var year = 0

    private var savedDay = 0
    private var savedMonth = 0
    private var savedYear = 0

    override fun onResume() {
        super.onResume()

        gender = requireView().findViewById(R.id.gender)
        val genderList = resources.getStringArray(R.array.genders)
        val genderArrayAdapter = ArrayAdapter(requireActivity(),R.layout.exposed_dropdown_menu_item_layout,genderList)
        gender.setAdapter(genderArrayAdapter)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_admin, container, false)
        backButton = view.findViewById(R.id.backButton)
        code = view.findViewById(R.id.code)
        name = view.findViewById(R.id.name)
        email = view.findViewById(R.id.email)
        phone = view.findViewById(R.id.phone)
        password = view.findViewById(R.id.password)
        cnfPassword = view.findViewById(R.id.cnfPassword)
        gender = view.findViewById(R.id.gender)
        birthday = view.findViewById(R.id.birthday)
        parentFrameLayout = view.findViewById(R.id.parentFrameLayout)
        progressBar = view.findViewById(R.id.progressBar)
        confirmBut = view.findViewById(R.id.confirmBut)
        parentParentFrameLayout = activity?.findViewById(R.id.parentFrameLayout)!!

        backButton.setOnClickListener {
            activity?.onBackPressed()
        }
        pickDate()

        confirmBut.setOnClickListener {
            checkInput()
        }

        return view
    }

    private fun checkInput(){
        val birthTxt : String = birthday.text.toString().trim{ it <= ' '}

        if(name.text.toString().isEmpty()) {
            name.setError("Please input name",null)
            name.requestFocus()
            return
        }
        if(gender.text.isEmpty()) {
            gender.setError("Please select gender",null)
            gender.requestFocus()
            gender.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                gender.error = null
            }
            return
        }
        if(birthday.text.toString().isEmpty()) {
            birthday.performClick()
            Snackbar.make(parentFrameLayout,"Birthday field should not be empty",Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary)).show()
            return
        }
        if(calculateAgeFromDob(birthTxt,"dd MMMM yyyy") < 17) {
            birthday.performClick()
            Snackbar.make(parentFrameLayout,"The age of student should be above 17 years to register",Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary)).show()
            return
        }
        if(code.text.toString().isEmpty()) {
            code.setError("Please input code",null)
            code.requestFocus()
            return
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()) {
            if(email.text.toString().isEmpty()) {
                email.setError("Please input email",null)
            }
            else {
                email.setError("Please input a valid email",null)
            }
            email.requestFocus()
            return
        }
        if(phone.text.toString().isEmpty()) {
            phone.setError("Please input phone number",null)
            phone.requestFocus()
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
        if(cnfPassword.text.toString().isEmpty()) {
            cnfPassword.setError("Please input password",null)
            cnfPassword.requestFocus()
            return
        }
        else if(cnfPassword.text.toString().length < 6) {
            cnfPassword.setError("The password cannot be less than six characters",null)
            cnfPassword.requestFocus()
            return
        }
        if(cnfPassword.text.toString() != cnfPassword.text.toString()) {
            cnfPassword.setError("The passwords doesn't match",null)
            cnfPassword.requestFocus()
            return
        }

        progressBar.visibility = View.VISIBLE
        checkAdminExist()
    }

    private fun checkAdminExist() {
        val mailTxt: String = email.text.toString().lowercase().trim{ it <= ' '}
        val codeTxt: String = code.text.toString().uppercase().trim{ it <= ' '}
        val phoneTxt : Long = phone.text.toString().toLong()

        val docRef = db.collection("AdminLoginInfo").document(mailTxt)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.data?.getValue("Email").toString() == mailTxt) {
                    Snackbar.make(parentFrameLayout,"Email already Registered",Snackbar.LENGTH_SHORT).setBackgroundTint(getColorFromAttr(R.attr.colorPrimary)).show()
                    progressBar.visibility = View.GONE
                }else {
                    db.collection("AdminLoginInfo")
                        .whereEqualTo("Code", codeTxt)
                        .get()
                        .addOnSuccessListener { documents ->
                            var temp: Boolean = false
                            for (document_it in documents) {
                                Snackbar.make(parentFrameLayout,"Code already exist",Snackbar.LENGTH_SHORT).setBackgroundTint(getColorFromAttr(R.attr.colorPrimary)).show()
                                progressBar.visibility = View.GONE
                                temp = true
                                break
                            }
                            if(!temp){
                                db.collection("AdminInfo")
                                    .whereEqualTo("Phone", phoneTxt)
                                    .get()
                                    .addOnSuccessListener { documentsIt ->
                                        var tempSecond: Boolean = false
                                        for (document_it_two in documentsIt) {
                                            Snackbar.make(parentFrameLayout,"Phone number already Registered",Snackbar.LENGTH_SHORT)
                                                .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                                                .show()
                                            tempSecond = true
                                            progressBar.visibility = View.GONE
                                            break
                                        }
                                        if(!tempSecond){
                                            addAdmin()
                                        }
                                    }
                            }
                        }
                }
            }.addOnFailureListener {
                progressBar.visibility = View.GONE
                Snackbar.make(parentFrameLayout,"Error occurred",Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                    .show()
            }
    }

    private fun addAdmin() {
        val birthTxt : String = birthday.text.toString().trim{ it <= ' '}
        val nameTxt: String = capitalizeWords(name.text.toString().trim{ it <= ' '}).toString()
        val genderTxt: String = gender.text.toString().trim{ it <= ' '}
        val codeTxt: String = code.text.toString().uppercase().trim{ it <= ' '}
        val emailTxt: String = email.text.toString().lowercase().trim{ it <= ' '}
        val phoneTxt : Long = phone.text.toString().toLong()
        val passTxt: String = password.text.toString().trim{ it <= ' '}
        val ageTxt: Long = calculateAgeFromDob(birthTxt,"dd MMMM yyyy").toLong()

        val adminCredentials: MutableMap<String, Any> = HashMap()
        adminCredentials["Code"] = codeTxt
        adminCredentials["Email"] = emailTxt
        adminCredentials["Password"] = passTxt
        db.collection("AdminLoginInfo").document(emailTxt).set(adminCredentials).addOnCompleteListener {
            val adminInfo: MutableMap<String, Any> = HashMap()
            adminInfo["Name"] = nameTxt
            adminInfo["Birthday"] = birthTxt
            adminInfo["Age"] = ageTxt
            adminInfo["Gender"] = genderTxt
            adminInfo["Code"] = codeTxt
            adminInfo["Email"] = emailTxt
            adminInfo["Phone"] = phoneTxt
            db.collection("AdminInfo").document(emailTxt).set(adminInfo).addOnSuccessListener {
                progressBar.visibility = View.GONE
                Snackbar.make(parentParentFrameLayout,"Admin Added",Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                    .show()
                activity?.onBackPressed()

            }.addOnFailureListener {
                Snackbar.make(parentFrameLayout,"Error occurred",
                    Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                    .show()
                progressBar.visibility = View.GONE
            }
        }.addOnFailureListener {
            Snackbar.make(parentFrameLayout,"Error occurred",
                Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                .show()
            progressBar.visibility = View.GONE
        }
        progressBar.visibility = View.GONE
    }

    private fun capitalizeWords(capString: String): String? {
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

    @SuppressLint("SimpleDateFormat")
    private fun getDateCalendar() {
        val cal = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
    }

    private fun pickDate() {
        birthday.setOnClickListener {
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
        birthday.setText("$savedDay $tempMonth $savedYear")
    }

    @ColorInt
    private fun getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true
    ): Int {
        activity?.theme?.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }
}