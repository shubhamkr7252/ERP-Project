package com.app.erp.admin_view.admin_list_view

import android.os.Bundle
import android.util.Patterns
import android.util.TypedValue
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import com.app.erp.gloabal_functions.EncryptDecryptPassword
import com.app.erp.R
import com.app.erp.gloabal_functions.hideKeyboard
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class FragmentRemoveAdmin : Fragment() {
    private var db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private val viewModel : ActivityAdminListViewModel by activityViewModels()

    private lateinit var saveBut: MaterialButton
    private lateinit var email: AutoCompleteTextView
    private lateinit var name: EditText
    private lateinit var code: EditText
    private lateinit var title: TextView
    private lateinit var backButton: MaterialButton
    private lateinit var parentFrameLayout: FrameLayout
    private lateinit var parentParentFrameLayout: FrameLayout
    private lateinit var progressBar: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_remove_admin, container, false)
        backButton = view.findViewById(R.id.backButton)
        saveBut = view.findViewById(R.id.saveBut)
        email = view.findViewById(R.id.email)
        name = view.findViewById(R.id.name)
        code = view.findViewById(R.id.code)
        title = view.findViewById(R.id.title)
        parentFrameLayout = view.findViewById(R.id.parentFrameLayout)
        progressBar = view.findViewById(R.id.progressBar)
        parentParentFrameLayout = activity?.findViewById(R.id.parentFrameLayout)!!

        auth = Firebase.auth

        backButton.setOnClickListener {
            activity?.onBackPressed()
        }

        viewModel.adminEmailDataList.observe(viewLifecycleOwner,{
            val arrayAdapter = ArrayAdapter(requireContext(),
                R.layout.exposed_dropdown_menu_item_layout,it)
            email.setAdapter(arrayAdapter)

            saveBut.setOnClickListener { _ ->
                if(!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()) {
                    if(email.text.toString().isEmpty()) {
                        email.setError("Please input email",null)
                    }
                    else {
                        email.setError("Please input a valid email",null)
                    }
                    email.requestFocus()
                    return@setOnClickListener
                }
                else if(it.contains(email.text.toString())){
                    if(code.text.toString().isEmpty() || name.text.toString().isEmpty()){
                        Toast.makeText(activity,"Retrieving Data, Please wait",Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    else{
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Confirm Deletion")
                            .setMessage("Are you sure you want to remove ${email.text.toString()}?")
                            .setNegativeButton("Cancel") { _, _ ->
                                return@setNegativeButton
                            }
                            .setPositiveButton("Delete") { _, _ ->
                                deleteAdmin()
                            }
                            .show()
                    }
                }
                else{
                    email.setError("Please select a valid Admin",null)
                    email.requestFocus()
                    return@setOnClickListener
                }
            }

            email.doOnTextChanged { text, start, before, count ->
                val temp = text.toString().lowercase()
                if(it.contains(temp)){
                    getAndSetAdminInfo(temp)
                }
                else{
                    code.text = null
                    name.text = null
                }
            }

            email.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                    email.dismissDropDown()
                    hideKeyboard(requireActivity())
                }
                false
            })

            email.setOnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                getAndSetAdminInfo(selectedItem)
            }
        })

        return view
    }

    private fun getAndSetAdminInfo(adminData: String){
        db.collection("AdminLoginInfo").document(adminData).get()
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    code.setText(task.result.data?.getValue("Code").toString())
                    db.collection("AdminInfo").document(task.result.data?.getValue("Code").toString()).get()
                        .addOnCompleteListener { taskAlt ->
                            if(taskAlt.isSuccessful){
                                name.setText(taskAlt.result.data?.getValue("Name").toString())
                            }
                        }
                }
            }
    }

    private fun checkInput() {
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
        else if(email.text.isNotEmpty()){
            if(code.text.toString().isEmpty() || name.text.toString().isEmpty()){
                Toast.makeText(activity,"Retrieving Data, Please wait",Toast.LENGTH_SHORT).show()
                return
            }
        }
    }

    private fun deleteAdmin() {
        val activityCall = (activity as ActivityAdminList)

        progressBar.visibility = View.VISIBLE
        val emailTxtAlt: String = email.text.toString().lowercase().trim{ it <= ' '}

        viewModel.emailData.observe(viewLifecycleOwner,{ emailTxt ->
            if(emailTxtAlt == emailTxt){
                Snackbar.make(parentFrameLayout,"Logged in Admin cannot be deleted",Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                    .show()
                email.requestFocus()
                progressBar.visibility = View.GONE
                return@observe
            }
            db.collection("AdminLoginInfo").document(emailTxtAlt).get()
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        if(emailTxtAlt == it.result.data?.getValue("Email")){
                            val currentPassword: String = EncryptDecryptPassword.decrypt(
                                it.result.data?.getValue(
                                    "Password"
                                ).toString()
                            ).toString()
                            auth.signInWithEmailAndPassword(emailTxtAlt,currentPassword).addOnCompleteListener {
                                auth.currentUser?.delete()?.addOnSuccessListener {
                                    db.collection("AdminLoginInfo").document(emailTxtAlt).get()
                                        .addOnSuccessListener { taskAlt ->
                                            if(taskAlt.exists()){
                                                db.collection("AdminLoginInfo").document(emailTxtAlt).get().addOnCompleteListener { task ->
                                                    if(task.isSuccessful){
                                                        val temp = task.result.data?.getValue("Code").toString()
                                                        db.collection("AdminInfo").document(temp).delete().addOnCompleteListener {
                                                            db.collection("AdminLoginInfo").document(emailTxtAlt).delete().addOnCompleteListener {
                                                                db.collection("AdminLoginInfo").document(emailTxt).get().addOnCompleteListener { it2 ->
                                                                    if(it2.isSuccessful){
                                                                        val currentUserPassword = it2.result.data?.getValue("Password").toString()
                                                                        val currentUserEmail = it2.result.data?.getValue("Email").toString()
                                                                        auth.signOut()
                                                                        auth.signInWithEmailAndPassword(currentUserEmail,currentUserPassword).addOnCompleteListener {
                                                                            activityCall.getAdminData()
                                                                            Snackbar.make(parentParentFrameLayout,"Admin Deleted",Snackbar.LENGTH_SHORT)
                                                                                .setBackgroundTint(getColorFromAttr(
                                                                                    R.attr.colorPrimary
                                                                                ))
                                                                                .show()
                                                                            progressBar.visibility = View.GONE
                                                                            activity?.onBackPressed()
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }else{
                                                        progressBar.visibility = View.GONE
                                                        Snackbar.make(parentFrameLayout,"Admin not found",Snackbar.LENGTH_SHORT)
                                                            .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                                                            .show()
                                                    }
                                                }
                                            }else{
                                                progressBar.visibility = View.GONE
                                                Snackbar.make(parentFrameLayout,"Admin not found",Snackbar.LENGTH_SHORT)
                                                    .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                                                    .show()
                                            }
                                        }
                                }
                            }
                        }
                    }
                }
        })
    }

    @ColorInt
    private fun getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true
    ): Int {
        activity?.theme?.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }
}