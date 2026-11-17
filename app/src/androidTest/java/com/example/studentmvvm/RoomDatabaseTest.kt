package com.example.studentmvvm

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.studentmvvm.data.AppDatabase
import com.example.studentmvvm.model.Student
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomDatabaseTest {
    @Test fun insertUpdateDeleteAndDuplicateCode() = runTest {
        val db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).build()
        try {
            val dao = db.studentDao()
            val student = Student(fullName = "Trần Đình Nguyên", studentCode = "2415053122225", email = "nguyen.demo@example.com", gpa = 3.2)
            val id = dao.insertStudent(student).toInt()
            assertEquals(1, dao.getAllStudents().first().size)
            assertEquals(student.fullName, dao.getStudentByCode(student.studentCode)?.fullName)
            dao.updateStudent(student.copy(id = id, gpa = 3.8))
            assertEquals(3.8, dao.getStudentByCode(student.studentCode)!!.gpa, 0.001)
            var rejected = false
            try { dao.insertStudent(student) } catch (error: SQLiteConstraintException) { rejected = true }
            assertTrue(rejected)
            dao.deleteStudent(student.copy(id = id))
            assertTrue(dao.getAllStudents().first().isEmpty())
        } finally { db.close() }
    }
}
