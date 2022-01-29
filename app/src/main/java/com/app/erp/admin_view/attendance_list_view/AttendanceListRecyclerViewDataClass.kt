package com.app.erp.admin_view.attendance_list_view

data class AttendanceListRecyclerViewDataClass(
    var Batch: String ?= null,
    var Semester: String ?= null,
    var Course: String ?= null,
    var CourseCode: String ?= null,
    var Date: String ?= null,
    var StudentAbsent: ArrayList<String> ?= null,
    var StudentPresent: ArrayList<String> ?= null,
    var Teacher: String ?= null,
    var TeacherCode: String ?= null,
    var Time: String ?= null,
    var TotalAbsent: Long ?= null,
    var TotalPresent: Long ?= null,
    var TotalStrength: Long ?= null
)
