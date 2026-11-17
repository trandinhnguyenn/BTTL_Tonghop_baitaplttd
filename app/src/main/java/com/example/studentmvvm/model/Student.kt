package com.example.studentmvvm.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "students_table", indices = [Index(value = ["student_code"], unique = true)])
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "full_name") val fullName: String,
    @ColumnInfo(name = "student_code") val studentCode: String,
    val email: String,
    val gpa: Double
)
