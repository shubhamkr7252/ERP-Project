package com.app.erp.admin_view.teacher_list_view

data class TeacherListRecyclerViewDataClass(
    var Name: String ?= null,
    var Email: String ?= null,
    var Phone: Long ?= null,
    var Age: Long?= null,
    var Gender: String ?= null,
    var Code: String ?= null,
    var Course: ArrayList<String> ?= null
)
