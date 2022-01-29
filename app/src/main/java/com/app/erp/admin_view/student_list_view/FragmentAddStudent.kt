package com.app.erp.admin_view.student_list_view

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import com.app.erp.gloabal_functions.EncryptDecryptPassword
import com.app.erp.R
import com.app.erp.gloabal_functions.getColorFromAttribute
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.ktx.Firebase
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class FragmentAddStudent : Fragment(), DatePickerDialog.OnDateSetListener {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private val viewModel : ActivityStudentListViewModel by activityViewModels()

    private lateinit var backButton: MaterialButton
    private lateinit var addStudentSRN: EditText
    private lateinit var addStudentName: EditText
    private lateinit var addStudentGenderSelect: AutoCompleteTextView
    private lateinit var addStudentBirthdayEditText: AutoCompleteTextView
    private lateinit var addStudentBatchSelect: AutoCompleteTextView
    private lateinit var addStudentSemesterSelect: AutoCompleteTextView
    private lateinit var addStudentMail: AutoCompleteTextView
    private lateinit var addStudentPhone: EditText
    private lateinit var addStudentPassword: EditText
    private lateinit var addStudentCnfPassword: EditText
    private lateinit var confirmBut: MaterialButton
    private lateinit var addStudentProgressBar: RelativeLayout
    private lateinit var tempArrayListSemesterInfo: ArrayList<String>
    private lateinit var parentFrameLayout: FrameLayout
    private lateinit var parentParentFrameLayout: FrameLayout
    private lateinit var selectedEmail: String

    private var day = 0
    private var month = 0
    private var year = 0

    private var savedDay = 0
    private var savedMonth = 0
    private var savedYear = 0

    override fun onResume() {
        super.onResume()

        addStudentGenderSelect = requireView().findViewById(R.id.addStudentGenderSelect)
        val genderList = resources.getStringArray(R.array.genders)
        val genderArrayAdapter = ArrayAdapter(requireActivity(),
            R.layout.exposed_dropdown_menu_item_layout,genderList)
        addStudentGenderSelect.setAdapter(genderArrayAdapter)

        addStudentMail = requireView().findViewById(R.id.addStudentMail)
        val emailList = resources.getStringArray(R.array.emailSelect)
        val arrayAdapter = ArrayAdapter(requireContext(),
            R.layout.exposed_dropdown_menu_item_layout,emailList)
        addStudentMail.setAdapter(arrayAdapter)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_student, container, false)
        backButton = view.findViewById(R.id.backButton)
        addStudentName = view.findViewById(R.id.addStudentName)
        addStudentSRN = view.findViewById(R.id.addStudentSRN)
        addStudentMail = view.findViewById(R.id.addStudentMail)
        addStudentPhone = view.findViewById(R.id.addStudentPhone)
        addStudentPassword = view.findViewById(R.id.addStudentPassword)
        addStudentCnfPassword = view.findViewById(R.id.addStudentCnfPassword)
        addStudentGenderSelect = view.findViewById(R.id.addStudentGenderSelect)
        addStudentBatchSelect = view.findViewById(R.id.addStudentBatchSelect)
        addStudentSemesterSelect = view.findViewById(R.id.addStudentSemesterSelect)
        addStudentBirthdayEditText = view.findViewById(R.id.addStudentBirthdayEditText)
        confirmBut = view.findViewById(R.id.confirmBut)
        addStudentProgressBar = view.findViewById(R.id.addStudentProgressBar)
        parentFrameLayout = view.findViewById(R.id.parentFrameLayout)
        parentParentFrameLayout = activity?.findViewById(R.id.parentFrameLayout)!!

        backButton.setOnClickListener {
            activity?.onBackPressed()
        }

        auth = Firebase.auth

        viewModel.adminEmailData.observe(viewLifecycleOwner,{
            Toast.makeText(activity, it,Toast.LENGTH_SHORT).show()
        })

        pickDate()

        val genderList = resources.getStringArray(R.array.genders)
        val genderArrayAdapter = ArrayAdapter(requireActivity(),
            R.layout.exposed_dropdown_menu_item_layout,genderList)
        addStudentGenderSelect.setAdapter(genderArrayAdapter)

        val emailList = resources.getStringArray(R.array.emailSelect)
        val arrayAdapterEmail = ArrayAdapter(requireContext(),
            R.layout.exposed_dropdown_menu_item_layout,emailList)
        addStudentMail.setAdapter(arrayAdapterEmail)

        selectedEmail = emailList[0]

        addStudentSRN.doOnTextChanged { text, _, _, _ ->
            if(addStudentSRN.text.toString().isNotEmpty()) {
                addStudentMail.setText(text.toString().plus(selectedEmail), null)
                val arrayAdapterSecond = ArrayAdapter(requireContext(),
                    R.layout.exposed_dropdown_menu_item_layout,emailList)
                addStudentMail.setAdapter(arrayAdapterSecond)
            }
            else{
                addStudentMail.text = null
            }
        }

        addStudentMail.setOnClickListener {
            if(addStudentSRN.text.toString().isEmpty()){
                addStudentMail.dismissDropDown()
                addStudentSRN.setError("Please enter SRN first",null)
                addStudentSRN.requestFocus()
            }
        }

        addStudentMail.setOnItemClickListener { parent, _, position, _ ->
            if(addStudentSRN.text.toString().isNotEmpty()){
                val selectedItem = parent.getItemAtPosition(position)
                selectedEmail = selectedItem.toString()
                addStudentMail.setText(addStudentSRN.text.toString().plus(selectedItem),null)
                val arrayAdapterThird = ArrayAdapter(requireContext(),
                    R.layout.exposed_dropdown_menu_item_layout,emailList)
                addStudentMail.setAdapter(arrayAdapterThird)
            }
        }

        addStudentSemesterSelect.setOnClickListener{
            if(addStudentBatchSelect.text.toString().isBlank()){
                addStudentBatchSelect.setError("Please select Batch first",null)
                addStudentBatchSelect.requestFocus()
            }
        }

        tempArrayListSemesterInfo = arrayListOf<String>()

        viewModel.batchData.observe(viewLifecycleOwner,{
            val arrayAdapter = ArrayAdapter(requireContext(),
                R.layout.exposed_dropdown_menu_item_layout,it)
            addStudentBatchSelect.setAdapter(arrayAdapter)
            addStudentBatchSelect.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                if(addStudentBatchSelect.text.isNotEmpty()){
                    addStudentBatchSelect.error = null
                }
                addStudentSemesterSelect.text.clear()
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
                                    val addStudentSemesterSelectAdapter = ArrayAdapter(requireActivity(),
                                        R.layout.exposed_dropdown_menu_item_layout, tempArrayListSemesterInfo)
                                    addStudentSemesterSelect.setAdapter(addStudentSemesterSelectAdapter)
                                }
                                break
                            }

                        }
                    })
            }
        })

        confirmBut.setOnClickListener {
            checkInput()
        }

        return view
    }

    private fun checkInput(){
        val birthTxt : String = addStudentBirthdayEditText.text.toString().trim{ it <= ' '}
        val activityCall = (activity as ActivityStudentList)

        if(addStudentName.text.toString().isEmpty()) {
            addStudentName.setError("Please input name",null)
            addStudentName.requestFocus()
            return
        }
        if(addStudentGenderSelect.text.isEmpty()) {
            addStudentGenderSelect.setError("Please select gender",null)
            addStudentGenderSelect.requestFocus()
            addStudentGenderSelect.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                addStudentGenderSelect.error = null
            }
            return
        }
        if(addStudentBirthdayEditText.text.toString().isEmpty()) {
            addStudentBirthdayEditText.performClick()
            Snackbar.make(parentFrameLayout,"Birthday field should not be empty",Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColorFromAttribute(requireActivity(),R.attr.colorPrimary))
                .show()
            return
        }
        if(calculateAgeFromDob(birthTxt,"dd MMMM yyyy") < 17) {
            addStudentBirthdayEditText.performClick()
            Snackbar.make(parentFrameLayout,"The age of student should be above 17 years to register",Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColorFromAttribute(requireActivity(),R.attr.colorPrimary))
                .show()
            return
        }
        if(addStudentSRN.text.toString().isEmpty()) {
            addStudentSRN.setError("Please input SRN",null)
            addStudentSRN.requestFocus()
            return
        }
        if(addStudentBatchSelect.text.isEmpty()) {
            addStudentBatchSelect.setError("Please select Batch",null)
            addStudentBatchSelect.requestFocus()
            addStudentBatchSelect.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                addStudentBatchSelect.error = null
            }
            return
        }
        if(addStudentSemesterSelect.text.isEmpty()) {
            addStudentSemesterSelect.setError("Please select semester",null)
            addStudentSemesterSelect.requestFocus()
            addStudentSemesterSelect.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                addStudentSemesterSelect.error = null
            }
            return
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(addStudentMail.text.toString()).matches()) {
            if(addStudentMail.text.toString().isEmpty()) {
                addStudentMail.setError("Please input email",null)
            }
            else {
                addStudentMail.setError("Please input a valid email",null)
            }
            addStudentMail.requestFocus()
            return
        }
        if(addStudentPhone.text.toString().isEmpty()) {
            addStudentPhone.setError("Please input phone number",null)
            addStudentPhone.requestFocus()
            return
        }
        if(!Patterns.PHONE.matcher(addStudentPhone.text.toString()).matches()) {
            addStudentPhone.setError("Please input valid phone number",null)
            addStudentPhone.requestFocus()
            return
        }
        if(addStudentPassword.text.toString().isEmpty()) {
            addStudentPassword.setError("Please input password",null)
            addStudentPassword.requestFocus()
            return
        }
        else if(addStudentPassword.text.toString().length < 6) {
            addStudentPassword.setError("The password cannot be less than six characters",null)
            addStudentPassword.requestFocus()
            return
        }
        if(addStudentCnfPassword.text.toString().isEmpty()) {
            addStudentCnfPassword.setError("Please input password",null)
            addStudentCnfPassword.requestFocus()
            return
        }
        else if(addStudentCnfPassword.text.toString().length < 6) {
            addStudentCnfPassword.setError("The password cannot be less than six characters",null)
            addStudentCnfPassword.requestFocus()
            return
        }
        if(addStudentPassword.text.toString() != addStudentCnfPassword.text.toString()) {
            addStudentCnfPassword.setError("The passwords doesn't match",null)
            addStudentCnfPassword.requestFocus()
            return
        }
        addStudentProgressBar.visibility = View.VISIBLE
        checkStudentExist()
    }

    private fun checkStudentExist(){
        val activityCall = (activity as ActivityStudentList)

        val srnTxt: String = addStudentSRN.text.toString().uppercase().trim{ it <= ' '}
        val mailTxt: String = addStudentMail.text.toString().lowercase().trim{ it <= ' '}
        val phoneTxt : String = addStudentPhone.text.toString().trim{ it <= ' '}

        val docRef = db.collection("StudentLoginInfo").document(mailTxt)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.data?.getValue("SRN").toString() == srnTxt) {
                    Snackbar.make(parentFrameLayout,"SRN already Registered",Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(getColorFromAttribute(requireActivity(),R.attr.colorPrimary))
                        .show()
                    addStudentProgressBar.visibility = View.GONE
                }
                else {
                    db.collection("StudentLoginInfo")
                        .whereEqualTo("Email", mailTxt)
                        .get()
                        .addOnSuccessListener { documents ->
                            var temp: Boolean = false
                            for (document_it in documents) {
                                Snackbar.make(parentFrameLayout,"Email already Registered",Snackbar.LENGTH_SHORT)
                                    .setBackgroundTint(getColorFromAttribute(requireActivity(),R.attr.colorPrimary))
                                    .show()
                                temp = true
                                addStudentProgressBar.visibility = View.GONE
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
                                                .setBackgroundTint(getColorFromAttribute(requireActivity(),R.attr.colorPrimary))
                                                .show()
                                            tempSecond = true
                                            addStudentProgressBar.visibility = View.GONE
                                            break
                                        }
                                        if(!tempSecond){
                                            registerStudent()
                                        }
                                    }
                            }
                        }

                }
            }
            .addOnFailureListener { exception ->
                addStudentProgressBar.visibility = View.GONE
                Toast.makeText(activity, "get failed with $exception", Toast.LENGTH_SHORT).show()
            }
    }

    private fun registerStudent(){
        val activityCall = (activity as ActivityStudentList)

        val birthTxt : String = addStudentBirthdayEditText.text.toString().trim{ it <= ' '}
        val nameTxt: String = capitalizeWords(addStudentName.text.toString().trim{ it <= ' '}).toString()
        val genderTxt: String = addStudentGenderSelect.text.toString().trim{ it <= ' '}
        val srnTxt: String = addStudentSRN.text.toString().uppercase().trim{ it <= ' '}
        val batchTxt : String = addStudentBatchSelect.text.toString().trim{ it <= ' '}
        val semesterTxt : String = addStudentSemesterSelect.text.toString().trim{ it <= ' '}
        val emailTxt: String = addStudentMail.text.toString().lowercase().trim{ it <= ' '}
        val phoneTxt : Long = addStudentPhone.text.toString().toLong()
        val passTxt: String = addStudentPassword.text.toString().trim{ it <= ' '}
        val encryptedPasswordTxt: String = EncryptDecryptPassword.encrypt(passTxt).toString()
        val ageTxt: Long = calculateAgeFromDob(birthTxt,"dd MMMM yyyy").toLong()

        auth.createUserWithEmailAndPassword(emailTxt,passTxt).addOnCompleteListener (requireActivity()){ task ->
            if(task.isSuccessful){
                val studentCredentials: MutableMap<String, Any> = HashMap()
                studentCredentials["SRN"] = srnTxt
                studentCredentials["Email"] = emailTxt
                studentCredentials["Password"] = encryptedPasswordTxt
                studentCredentials["UID"] = auth.currentUser?.uid.toString()

                db.collection("StudentLoginInfo").document(emailTxt).set(studentCredentials).addOnCompleteListener { _ ->
                    val studentInfo: MutableMap<String, Any> = HashMap()
                    studentInfo["Name"] = nameTxt
                    studentInfo["Birthday"] = birthTxt
                    studentInfo["Age"] = ageTxt
                    studentInfo["Gender"] = genderTxt
                    studentInfo["SRN"] = srnTxt
                    studentInfo["Batch"] = batchTxt
                    studentInfo["Semester"] = semesterTxt
                    studentInfo["Email"] = emailTxt
                    studentInfo["Phone"] = phoneTxt
                    studentInfo["AttendedClasses"] = 0
                    studentInfo["NonAttendedClasses"] = 0
                    studentInfo["TotalClasses"] = 0

                    db.collection("StudentInfo").document(srnTxt).set(studentInfo).addOnSuccessListener { _ ->
                        viewModel.adminEmailData.observe(viewLifecycleOwner,{ emailData ->
                            db.collection("AdminLoginInfo").document(emailData).get()
                                .addOnCompleteListener { task ->
                                    if(task.isSuccessful){
                                        val email: String = task.result.data?.getValue("Email").toString()
                                        val password: String = task.result.data?.getValue("Password").toString()
                                        val decryptedPassword: String = EncryptDecryptPassword.decrypt(
                                            password
                                        ).toString()
                                        auth.signOut()
                                        auth.signInWithEmailAndPassword(email,decryptedPassword).addOnCompleteListener { _ ->
                                            Snackbar.make(parentParentFrameLayout,"Student Added",Snackbar.LENGTH_SHORT)
                                                .setBackgroundTint(getColorFromAttribute(requireActivity(),R.attr.colorPrimary))
                                                .show()
                                            activityCall.getStudentData()
                                            activity?.onBackPressed()
                                        }
                                    }
                                }
                        })

                    }.addOnFailureListener {
                        Snackbar.make(parentFrameLayout,"Error occurred",Snackbar.LENGTH_SHORT).setBackgroundTint(getColorFromAttribute(requireActivity(),R.attr.colorPrimary)).show()
                        addStudentProgressBar.visibility = View.GONE
                    }
                }.addOnFailureListener {
                    Snackbar.make(parentFrameLayout,"Error occurred",Snackbar.LENGTH_SHORT).setBackgroundTint(getColorFromAttribute(requireActivity(),R.attr.colorPrimary)).show()
                    addStudentProgressBar.visibility = View.GONE
                }
                addStudentProgressBar.visibility = View.GONE
            }
        }

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
    private fun calculateAgeFromDob(birthDate: String, dateFormat:String): Int {

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
        addStudentBirthdayEditText.setOnClickListener {
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
        addStudentBirthdayEditText.setText("$savedDay $tempMonth $savedYear")
    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        addStudentScrollViewLayout = findViewById(R.id.addStudentScrollViewLayout)
//        outState.putIntArray(
//            "ARTICLE_SCROLL_POSITION",
//            intArrayOf(addStudentScrollViewLayout.scrollX, addStudentScrollViewLayout.scrollY)
//        )
//    }
//
//    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
//        super.onRestoreInstanceState(savedInstanceState)
//        addStudentScrollViewLayout = findViewById(R.id.addStudentScrollViewLayout)
//        val position = savedInstanceState.getIntArray("ARTICLE_SCROLL_POSITION")
//        if (position != null) addStudentScrollViewLayout.post { addStudentScrollViewLayout.scrollTo(position[0], position[1]) }
//    }
}