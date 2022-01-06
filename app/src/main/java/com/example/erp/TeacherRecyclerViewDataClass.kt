package com.example.erp

data class TeacherRecyclerViewDataClass(
    var Name: String ?= null,
    var Email: String ?= null,
    var Phone: Long ?= null,
    var Age: Long ?= null,
    var Gender: String ?= null,
    var Code: String ?= null,
    var Course: ArrayList<String> ?= null)
