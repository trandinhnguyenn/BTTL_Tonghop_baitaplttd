package com.example.studentmvvm.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.studentmvvm.R
import com.example.studentmvvm.StudentApplication
import com.example.studentmvvm.viewmodel.StudentViewModelFactory

class MainActivity : AppCompatActivity() {
    val studentFactory by lazy { StudentViewModelFactory((application as StudentApplication).repository) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, StudentListFragment()).commit()
        }
    }
}
