package com.example.studentmvvm.model

/** Quy ước GPA 0..4 cho bài thực hành; chấp nhận dấu phẩy hoặc dấu chấm. */
object StudentValidator {
    fun parseGpa(value: String): Double? = value.trim().replace(',', '.').toDoubleOrNull()
    fun validate(name: String, code: String, email: String, gpa: Double?): String? = when {
        name.isBlank() -> "Vui lòng nhập họ tên."
        code.isBlank() -> "Vui lòng nhập mã số sinh viên."
        !Regex("[A-Za-z0-9_-]+").matches(code) -> "MSSV chỉ gồm chữ, số, dấu gạch ngang hoặc gạch dưới."
        !Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email) -> "Email chưa đúng định dạng."
        gpa == null || !gpa.isFinite() || gpa !in 0.0..4.0 -> "GPA phải là số từ 0 đến 4."
        else -> null
    }
}
