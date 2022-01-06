package com.example.erp

data class FragmentTeacherAttendanceLogRecyclerViewDataClass(
    var Batch: String ?= null,
    var Semester: String ?= null,
    var Date: String ?= null,
    var Time: String ?= null,
    var Course: String ?= null,
    var CourseCode: String ?= null,
    var TotalPresent: Long ?= null,
    var TotalAbsent: Long ?= null,
    var TotalStrength: Long ?= null
)
