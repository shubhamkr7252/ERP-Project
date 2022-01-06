package com.example.erp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import com.google.android.material.snackbar.Snackbar


class Activity4Login : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    private lateinit var loginType: AutoCompleteTextView
    private lateinit var firstBox: EditText
    private lateinit var passwordBox: EditText
    private lateinit var firstBoxLayout: TextInputLayout
    private lateinit var loginButton: MaterialButton
    private lateinit var progressBarLayout: RelativeLayout
    private lateinit var titleImg: ImageView
    private lateinit var forgotPasswordText: TextView
    private lateinit var activity4LoginLayout: FrameLayout
    private lateinit var buttonsRelativeLayout: RelativeLayout
    private lateinit var forgotPasswordLayout: LinearLayout
    private lateinit var passwordBoxLayout: TextInputLayout

    override fun onResume() {
        super.onResume()
        val loginTypeList = resources.getStringArray(R.array.loginTypes)
        val loginTypeAdapter = ArrayAdapter(this,R.layout.exposed_dropdown_menu_item_layout,loginTypeList)
        loginType.setAdapter(loginTypeAdapter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_4_login)

        loginType = findViewById(R.id.loginType)
        firstBox = findViewById(R.id.firstBox)
        passwordBox = findViewById(R.id.passwordBox)
        loginButton = findViewById(R.id.loginButton)
        progressBarLayout = findViewById(R.id.progressBarLayout)
        titleImg = findViewById(R.id.titleImg)
        forgotPasswordText = findViewById(R.id.forgotPasswordText)
        activity4LoginLayout = findViewById(R.id.activity4LoginLayout)
        buttonsRelativeLayout = findViewById(R.id.buttonsRelativeLayout)
        forgotPasswordLayout = findViewById(R.id.forgotPasswordLayout)
        passwordBoxLayout = findViewById(R.id.passwordBoxLayout)
        firstBoxLayout = findViewById(R.id.firstBoxLayout)

        val loginTypeList = resources.getStringArray(R.array.loginTypes)
        val loginTypeAdapter = ArrayAdapter(this,R.layout.exposed_dropdown_menu_item_layout,loginTypeList)
        loginType.setAdapter(loginTypeAdapter)

        loginType.setOnItemClickListener { parent, view, position, id ->
            firstBox.requestFocus()
            if(firstBox.isFocused){
                showKeyboard(this)
            }
            firstBoxLayout.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_pop_enter_anim))
            forgotPasswordLayout.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_pop_enter_anim))
            buttonsRelativeLayout.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_pop_enter_anim))
            passwordBoxLayout.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_pop_enter_anim))

            buttonsRelativeLayout.visibility = View.VISIBLE
            forgotPasswordLayout.visibility = View.VISIBLE
            passwordBoxLayout.visibility = View.VISIBLE
            firstBoxLayout.visibility = View.VISIBLE
            when {
                loginType.text.toString() == "Admin" -> {
                    firstBox.text = null
                    passwordBox.text = null
                    firstBoxLayout.hint = "Mail"
                    firstBoxLayout.setStartIconDrawable(R.drawable.ic_twotone_email_24)
                    loginButton.setOnClickListener {
                        progressBarLayout.visibility = View.VISIBLE
                        adminLogin()
                    }
                    titleImg.setImageResource(R.drawable.admin3)
                }
                loginType.text.toString() == "Faculty" -> {
                    firstBox.text = null
                    passwordBox.text = null
                    firstBoxLayout.hint = "Teacher Code"
                    firstBoxLayout.setStartIconDrawable(R.drawable.ic_twotone_person_24)
                    loginButton.setOnClickListener {
                        progressBarLayout.visibility = View.VISIBLE
                        teacherLogin()
                    }
                    titleImg.setImageResource(R.drawable.teacher_2)
                }
                loginType.text.toString() == "Student" -> {
                    firstBox.text = null
                    passwordBox.text = null
                    firstBoxLayout.hint = "SRN"
                    firstBoxLayout.setStartIconDrawable(R.drawable.ic_twotone_supervised_user_circle_24)
                    loginButton.setOnClickListener {
                        progressBarLayout.visibility = View.VISIBLE
                        studentLogin()
                    }
                    titleImg.setImageResource(R.drawable.student2)
                }
            }
        }

        loginButton.setOnClickListener {
            if(loginType.text.toString().isBlank()){
                Snackbar.make(activity4LoginLayout,"Please Choose a Login Type",Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                    .show()
            }
        }

        forgotPasswordText.setOnClickListener {
            sendEmail()
        }
    }

    private fun showKeyboard(activity: Activity) {
        val view = activity.currentFocus
        val methodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        assert(view != null)
        methodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun teacherLogin() {
        if(firstBox.text.toString().isEmpty()) {
            firstBox.setError("Please input Teacher code",null)
            firstBox.requestFocus()
            progressBarLayout.visibility = View.GONE
            return
        }
        if(passwordBox.text.toString().isEmpty()) {
            passwordBox.setError("Please input Password",null)
            passwordBox.requestFocus()
            progressBarLayout.visibility = View.GONE
            return
        }
        val codeTxt: String = firstBox.text.toString().trim {it <= ' '}.uppercase()
        val passTxt: String = passwordBox.text.toString().trim {it <= ' '}

        db.collection("TeacherLoginInfo").document(codeTxt).get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    if(it.result.data?.getValue("Password") == passTxt){
                        val intent = Intent(this,ActivityAfterTeacherLoginNavigation::class.java)
                        intent.putExtra("codeTxt",codeTxt)
                        startActivity(intent)
                        finish()
                        progressBarLayout.visibility = View.GONE
                    }else{
                        progressBarLayout.visibility = View.GONE
                        Snackbar.make(activity4LoginLayout,"Incorrect Credentials",Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                            .show()
                    }
                }
            }.addOnFailureListener {
                progressBarLayout.visibility = View.GONE
                Snackbar.make(activity4LoginLayout,"Incorrect Credentials",Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                    .show()
            }
    }

    private fun studentLogin() {
        if(firstBox.text.toString().isEmpty()) {
            firstBox.setError("Please input Teacher code",null)
            firstBox.requestFocus()
            progressBarLayout.visibility = View.GONE
            return
        }
        if(passwordBox.text.toString().isEmpty()) {
            passwordBox.setError("Please input Password",null)
            passwordBox.requestFocus()
            progressBarLayout.visibility = View.GONE
            return
        }

        val srnTxt: String = firstBox.text.toString().trim {it <= ' '}.uppercase()
        val passTxt: String = passwordBox.text.toString().trim {it <= ' '}

        db.collection("StudentLoginInfo").document(srnTxt).get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    if(it.result.data?.getValue("Password") == passTxt){
                        val intent = Intent(this,ActivityAfterStudentLoginNavigation::class.java)
                        intent.putExtra("srnText",srnTxt)
                        startActivity(intent)
                        finish()
                        progressBarLayout.visibility = View.GONE
                    }else{
                        progressBarLayout.visibility = View.GONE
                        Snackbar.make(activity4LoginLayout,"Incorrect Credentials",Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                            .show()
                    }
                }
            }.addOnFailureListener {
                progressBarLayout.visibility = View.GONE
                Snackbar.make(activity4LoginLayout,"Incorrect Credentials",Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                    .show()
            }
    }

    private fun adminLogin() {
        if(firstBox.text.toString().isEmpty()) {
            firstBox.setError("Please input Teacher code",null)
            firstBox.requestFocus()
            progressBarLayout.visibility = View.GONE
            return
        }
        if(passwordBox.text.toString().isEmpty()) {
            passwordBox.setError("Please input Password",null)
            passwordBox.requestFocus()
            progressBarLayout.visibility = View.GONE
            return
        }

        val mailTxt: String = firstBox.text.toString().trim {it <= ' '}.lowercase()
        val passTxt: String = passwordBox.text.toString().trim {it <= ' '}

        db.collection("AdminLoginInfo").document(mailTxt).get()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    if(passTxt == it.result.data?.getValue("Password")){
                        val intent = Intent(this,ActivityAfterAdminLoginNavigation::class.java)
                        intent.putExtra("mailTxt",mailTxt)
                        startActivity(intent)
                        finish()
                        progressBarLayout.visibility = View.GONE
                    }
                    else{
                        progressBarLayout.visibility = View.GONE
                        Snackbar.make(activity4LoginLayout,"Incorrect Credentials",Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                            .show()
                    }
                }
            }.addOnFailureListener {
                progressBarLayout.visibility = View.GONE
                Snackbar.make(activity4LoginLayout,"Incorrect Credentials",Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                    .show()
            }
    }

    @ColorInt
    fun Context.getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }

    private fun sendEmail() {
        /*ACTION_SEND action to launch an email client installed on your Android device.*/
        val mIntent = Intent(Intent.ACTION_SENDTO)
        /*To send an email you need to specify mailto: as URI using setData() method
        and data type will be to text/plain using setType() method*/
        mIntent.data = Uri.parse("mailto:"+"support@reva.edu.in")
        mIntent.type = "text/plain"
        // put recipient email in intent
        /* recipient is put as array because you may wanna send email to multiple emails
           so enter comma(,) separated emails, it will be stored in array*/
        mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("support@reva.edu.in"))
        //put the Subject in the intent
        mIntent.putExtra(Intent.EXTRA_SUBJECT, "Password Reset")
        //put the message in the intent
        mIntent.putExtra(Intent.EXTRA_TEXT, "I want to reset my password\n\n<Please add additional information below this line (SRN, Name, etc.)>\n\n")


        try {
            //start email intent
            startActivity(Intent.createChooser(mIntent, "Choose Email Client..."))
        }
        catch (e: Exception){
            //if any thing goes wrong for example no email client application or any exception
            //get and show exception message
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }

    }

}