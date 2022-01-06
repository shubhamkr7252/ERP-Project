package com.example.erp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ActivityBatchListViewModel :ViewModel(){
    val batchArrayListData: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>()
    }
}