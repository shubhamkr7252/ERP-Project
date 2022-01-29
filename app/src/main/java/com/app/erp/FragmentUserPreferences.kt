package com.app.erp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.app.erp.gloabal_functions.loadLoginSecurityData
import com.app.erp.gloabal_functions.saveLoginSecurityData
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.Executor

class FragmentUserPreferences : Fragment() {
    private lateinit var backButton: MaterialButton
    private lateinit var logoutBut: MaterialButton
//    private lateinit var darkThemeLayout: MaterialCardView
    private lateinit var appLockLayout: MaterialCardView
//    private lateinit var darkThemeSwitch: androidx.appcompat.widget.SwitchCompat
    private lateinit var appLockSwitch: androidx.appcompat.widget.SwitchCompat

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: androidx.biometric.BiometricPrompt
    private lateinit var promptInfo: androidx.biometric.BiometricPrompt.PromptInfo

    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_preferences, container, false)
        backButton = view.findViewById(R.id.backButton)
        logoutBut = view.findViewById(R.id.logoutBut)
//        darkThemeLayout = view.findViewById(R.id.darkThemeLayout)
        appLockLayout = view.findViewById(R.id.appLockLayout)
//        darkThemeSwitch = view.findViewById(R.id.darkThemeSwitch)
        appLockSwitch = view.findViewById(R.id.appLockSwitch)

        auth = Firebase.auth

//        val darkThemePreferences: Boolean = DarkThemePreference.loadData(requireActivity())
//        if(darkThemePreferences){
//            darkThemeSwitch.isChecked = true
//        }
//        else if (!darkThemePreferences){
//            darkThemeSwitch.isChecked = false
//        }
        if(loadLoginSecurityData(requireActivity())){
            appLockSwitch.isChecked = true
        }
        else if (!loadLoginSecurityData(requireActivity())){
            appLockSwitch.isChecked = false
        }

//        darkThemeLayout.setOnClickListener { darkThemeSwitch.performClick() }
//        darkThemeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
//            if(isChecked){
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                DarkThemePreference.saveData(requireActivity(),true)
//            }
//            else if(!isChecked){
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                DarkThemePreference.saveData(requireActivity(),false)
//            }
//            if (!darkThemePreferences){
//                MaterialAlertDialogBuilder(requireContext())
//                    .setTitle("Switch to Dark theme?")
//                    .setMessage("Switching theme will restart the application")
//                    .setPositiveButton("Restart") { _, _ ->
//                        DarkThemePreference.saveData(requireActivity(),true)
//                        ProcessPhoenix.triggerRebirth(requireContext())
//                    }
//                    .setCancelable(false)
//                    .setNegativeButton("Cancel") { _, _ ->
//                        darkThemeSwitch.isChecked = false
//                    }
//                    .show()
//            }
//            else if(darkThemePreferences){
//                MaterialAlertDialogBuilder(requireContext())
//                    .setTitle("Switch to Light theme?")
//                    .setMessage("Switching theme will restart the application")
//                    .setPositiveButton("Restart") { _, _ ->
//                        DarkThemePreference.saveData(requireActivity(),false)
//                        ProcessPhoenix.triggerRebirth(requireContext())
//                    }
//                    .setCancelable(false)
//                    .setNegativeButton("Cancel") { _, _ ->
//                        darkThemeSwitch.isChecked = true
//                    }
//                    .show()
//            }
//        }

        appLockLayout.setOnClickListener { appLockSwitch.performClick() }
        appLockSwitch.setOnClickListener {
            val appLockPreferences: Boolean = loadLoginSecurityData(requireActivity())

            if (!appLockPreferences){
                fingerprintAuth("Authenticate to Activate","Login Security will be enabled",loginData = true)
            }
            else if(appLockPreferences){
                fingerprintAuth("Authenticate to Deactivate","Login Security will be disabled",loginData = false)
            }
        }

        logoutBut.setOnClickListener {
            MaterialAlertDialogBuilder(requireActivity())
                .setTitle("Logout")
                .setMessage("Logging out will erase your data from this device and will go back to Login screen.")
                .setPositiveButton("Logout") { _, _ ->
                    auth.signOut()
                    saveLoginSecurityData(requireActivity(),false)
                    startActivity(Intent(activity, Activity4Login::class.java))
                    activity?.finish()
                }
                .setCancelable(false)
                .setNegativeButton("Cancel") { _, _ -> }
                .show()
        }

        backButton.setOnClickListener {
            activity?.onBackPressed()
        }
        return view
    }

    private fun fingerprintAuth(promptTitle: String, promptMessage: String, loginData: Boolean){
        executor = ContextCompat.getMainExecutor(requireActivity())
        biometricPrompt = androidx.biometric.BiometricPrompt(requireActivity(), executor, object: androidx.biometric.BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(activity,"$errString $errorCode",Toast.LENGTH_SHORT).show()
                if(errorCode == 10 || errorCode == 7 || errorCode == 13){
                    if(errorCode == 7){
                        Toast.makeText(activity,"Too many attempts, Try again later",Toast.LENGTH_SHORT).show()
                    }
                    if(appLockSwitch.isChecked){
                        appLockSwitch.isChecked = false
                    }
                    else if (!appLockSwitch.isChecked){
                        appLockSwitch.isChecked = true
                    }
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(activity,"Authentication Failed",Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                saveLoginSecurityData(requireActivity(),loginData)
                if(loadLoginSecurityData(requireActivity())){
                    Toast.makeText(activity,"Login Security Enabled",Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(activity,"Login Security Disabled",Toast.LENGTH_SHORT).show()
                }
            }
        })

        promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle(promptTitle)
            .setSubtitle(promptMessage)
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}