package com.app.erp.admin_view.admin_list_view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

internal class ActivityAdminListViewModel:ViewModel() {
    val emailData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val adminEmailDataList: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>()
    }
}