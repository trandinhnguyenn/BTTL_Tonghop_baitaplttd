package com.example.studentmvvm.network

import com.google.gson.annotations.SerializedName
import com.example.studentmvvm.model.Student

data class StudentDto(
    @SerializedName("id") val remoteId: String? = null,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("student_code") val studentCode: String,
    @SerializedName("email") val email: String,
    @SerializedName("gpa") val gpa: Double
) {
    fun toEntity() = Student(fullName = fullName, studentCode = studentCode, email = email, gpa = gpa)
    companion object {
        fun from(student: Student) = StudentDto(fullName = student.fullName,
            studentCode = student.studentCode, email = student.email, gpa = student.gpa)
    }
}
