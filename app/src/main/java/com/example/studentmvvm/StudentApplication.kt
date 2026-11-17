package com.example.studentmvvm

import android.app.Application
import com.example.studentmvvm.data.AppDatabase
import com.example.studentmvvm.network.RetrofitClient
import com.example.studentmvvm.repository.StudentRepository

class StudentApplication : Application() {
    val repository by lazy {
        StudentRepository(AppDatabase.getDatabase(this), RetrofitClient.apiService)
    }
}
