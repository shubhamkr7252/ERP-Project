package com.example.erp

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

class FragmentRemoveAdmin : Fragment() {
    private var db = FirebaseFirestore.getInstance()
    private lateinit var saveBut: MaterialButton
    private lateinit var email: EditText
    private lateinit var backButton: MaterialButton
    private lateinit var parentFrameLayout: FrameLayout
    private lateinit var parentParentFrameLayout: FrameLayout
    private lateinit var progressBar: RelativeLayout

    private lateinit var mailTxt: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_remove_admin, container, false)
        backButton = view.findViewById(R.id.backButton)
        saveBut = view.findViewById(R.id.saveBut)
        email = view.findViewById(R.id.email)
        parentFrameLayout = view.findViewById(R.id.parentFrameLayout)
        progressBar = view.findViewById(R.id.progressBar)
        parentParentFrameLayout = activity?.findViewById(R.id.parentFrameLayout)!!
        mailTxt = String()

        backButton.setOnClickListener {
            activity?.onBackPressed()
        }

        val args = this.arguments
        mailTxt = args?.get("mail") as String

        saveBut.setOnClickListener {
            checkAdminExist()
        }
        return view
    }

    private fun checkAdminExist() {
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
        if(email.text.toString() == mailTxt){
            Snackbar.make(parentFrameLayout,"Logged in Admin cannot be deleted",Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                .show()
            email.requestFocus()
            return
        }
        progressBar.visibility = View.VISIBLE
        val mailTxtAlt: String = email.text.toString().lowercase().trim{ it <= ' '}
        db.collection("AdminLoginInfo").document(mailTxtAlt).get()
            .addOnSuccessListener {
                if(it.exists()){
                    db.collection("AdminLoginInfo").document(mailTxtAlt).delete().addOnCompleteListener {
                        db.collection("AdminInfo").document(mailTxtAlt).delete().addOnCompleteListener {
                            progressBar.visibility = View.GONE
                            Snackbar.make(parentParentFrameLayout,"Admin Deleted",Snackbar.LENGTH_SHORT)
                                .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                                .show()
                            activity?.onBackPressed()
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

    @ColorInt
    private fun getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true
    ): Int {
        activity?.theme?.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }
}