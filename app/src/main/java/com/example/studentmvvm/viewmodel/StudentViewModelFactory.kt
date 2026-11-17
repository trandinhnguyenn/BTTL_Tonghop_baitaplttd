package com.example.studentmvvm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.studentmvvm.repository.StudentRepository

class StudentViewModelFactory(private val repository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(StudentViewModel::class.java)) { "ViewModel không được hỗ trợ" }
        @Suppress("UNCHECKED_CAST")
        return StudentViewModel(repository) as T
    }
}
