package com.example.studentmvvm.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import com.example.studentmvvm.data.AppDatabase
import com.example.studentmvvm.model.Student
import com.example.studentmvvm.model.StudentValidator
import com.example.studentmvvm.network.StudentApiService
import com.example.studentmvvm.network.StudentDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StudentRepository(private val database: AppDatabase, private val api: StudentApiService) {
    private val dao = database.studentDao()
    // UI chỉ quan sát Room; không nhận danh sách trực tiếp từ Retrofit.
    val allStudents = dao.getAllStudents()

    suspend fun save(student: Student) = withContext(Dispatchers.IO) {
        StudentValidator.validate(student.fullName, student.studentCode, student.email, student.gpa)
            ?.let { throw IllegalArgumentException(it) }
        try {
            database.withTransaction {
                val existing = dao.getStudentByCode(student.studentCode)
                require(existing == null || existing.id == student.id) { "MSSV này đã tồn tại." }
                if (student.id == 0) dao.insertStudent(student)
                else check(dao.updateStudent(student) == 1) { "Sinh viên đã bị xóa. Vui lòng mở lại danh sách." }
            }
        } catch (error: SQLiteConstraintException) {
            throw IllegalArgumentException("MSSV này đã tồn tại.", error)
        }
        Unit
    }

    suspend fun delete(student: Student) = withContext(Dispatchers.IO) { dao.deleteStudent(student); Unit }

    suspend fun refresh(): Int = withContext(Dispatchers.IO) {
        val remote = api.getStudents()
        // Kiểm tra trọn gói trước khi ghi; nếu có lỗi, không thay đổi dữ liệu cũ.
        val students = remote.map { dto ->
            val entity = dto.toEntity()
            require(StudentValidator.validate(entity.fullName, entity.studentCode, entity.email, entity.gpa) == null) {
                "API trả về sinh viên không hợp lệ. Dữ liệu cũ được giữ nguyên."
            }
            entity
        }
        require(students.map { it.studentCode }.distinct().size == students.size) { "API có MSSV trùng lặp." }
        database.withTransaction {
            students.forEach { incoming ->
                val old = dao.getStudentByCode(incoming.studentCode)
                if (old == null) dao.insertStudent(incoming)
                else dao.updateStudent(incoming.copy(id = old.id))
            }
        }
        students.size
    }

    // POST tường minh từng sinh viên. Thêm/sửa/xóa cục bộ vẫn hoạt động khi mất mạng.
    suspend fun upload(student: Student) = withContext(Dispatchers.IO) {
        api.createStudent(StudentDto.from(student))
        Unit
    }
}
