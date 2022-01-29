package com.app.erp.gloabal_functions

import android.app.Activity
import android.content.Context
import android.net.NetworkInfo

import android.net.ConnectivityManager
import androidx.core.content.ContextCompat

import androidx.core.content.ContextCompat.getSystemService
import java.lang.Exception
import java.security.AccessController.getContext


object InternetState {
    internal fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetwork != null
    }

    internal fun internetIsConnected(): Boolean {
        return try {
            val command = "ping -c 1 google.com"
            Runtime.getRuntime().exec(command).waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }
}