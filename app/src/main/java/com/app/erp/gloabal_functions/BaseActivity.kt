package com.app.erp.gloabal_functions

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.app.erp.R
import com.google.android.material.snackbar.Snackbar
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

internal fun showToast(context: Context,message: String){
    Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
}

internal fun showSnackBar(context: Context, view: View,message: String){
    Snackbar.make(view,message,Snackbar.LENGTH_SHORT)
        .setBackgroundTint(getColorFromAttribute(context, R.attr.colorPrimary))
        .show()
}

internal fun showKeyboard(activity: Activity) {
    val view = activity.currentFocus
    val methodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    assert(view != null)
    methodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

internal fun capitalizeWords(capString: String): String? {
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

internal fun getColorFromAttribute(context: Context, @AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true
): Int {
    context.theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}

internal fun hideKeyboard(activity: Activity) {
    val view: View? = activity.currentFocus
    val inputManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(
        view?.windowToken,
        InputMethodManager.HIDE_NOT_ALWAYS
    )
}

internal fun saveDarkThemeData(activity: Activity,state: Boolean){
    val sharedPreferences: SharedPreferences = activity.getSharedPreferences("appConfig",
        Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = sharedPreferences.edit()
    editor.apply {
        putBoolean("DARK_MODE",state)
    }.apply()
}

internal fun loadDarkThemeData(activity: Activity): Boolean {
    val sharedPreferences =
        activity.getSharedPreferences("appConfig", Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean("DARK_MODE", false)
}

internal fun saveLoginSecurityData(activity: Activity, state: Boolean){
    val sharedPreferences: SharedPreferences = activity.getSharedPreferences("appConfig",
        Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = sharedPreferences.edit()
    editor.apply {
        putBoolean("FINGERPRINT_AUTHENTICATION",state)
    }.apply()
}

internal fun loadLoginSecurityData(activity: Activity): Boolean {
    val sharedPreferences =
        activity.getSharedPreferences("appConfig", Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean("FINGERPRINT_AUTHENTICATION", false)
}

internal fun hideSystemUI(activity: Activity, parentLayout: View) {
    WindowCompat.setDecorFitsSystemWindows(activity.window, false)
    WindowInsetsControllerCompat(activity.window, parentLayout).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.isAppearanceLightNavigationBars = true
    }
}