package com.example.studentmvvm.viewmodel

import androidx.lifecycle.*
import com.example.studentmvvm.model.Student
import com.example.studentmvvm.repository.StudentRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// token giúp dialog sau khi xoay màn hình nhận đúng kết quả lưu của chính nó.
data class SaveState(val token: String, val busy: Boolean, val success: Boolean = false, val error: String? = null)

class StudentViewModel(private val repository: StudentRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    val allStudents = combine(repository.allStudents, query) { students, text ->
        students.filter { it.fullName.contains(text, true) || it.studentCode.contains(text, true) }
    }.asLiveData(Dispatchers.IO)
    private val _saveState = MutableLiveData<SaveState?>()
    val saveState: LiveData<SaveState?> = _saveState
    private val _networkBusy = MutableLiveData(false)
    val networkBusy: LiveData<Boolean> = _networkBusy
    private val messages = Channel<String>(Channel.BUFFERED)
    val events = messages.receiveAsFlow()

    fun search(text: String) { query.value = text.trim() }
    fun save(student: Student, token: String) {
        if (_saveState.value?.busy == true) return
        _saveState.value = SaveState(token, busy = true)
        viewModelScope.launch {
            try {
                repository.save(student)
                _saveState.value = SaveState(token, busy = false, success = true)
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) {
                _saveState.value = SaveState(token, busy = false, error = errorText(error))
            }
        }
    }

    fun delete(student: Student) = viewModelScope.launch {
        try { repository.delete(student); messages.send("Đã xóa ${student.fullName} khỏi thiết bị.") }
        catch (error: CancellationException) { throw error }
        catch (error: Exception) { messages.send(errorText(error)) }
    }

    fun refresh() = networkAction {
        val count = repository.refresh()
        "Đã nạp $count sinh viên từ API vào Room."
    }

    fun upload(student: Student) = networkAction {
        repository.upload(student)
        "Đã gửi ${student.studentCode} lên API."
    }

    private fun networkAction(action: suspend () -> String) {
        if (_networkBusy.value == true) return
        _networkBusy.value = true
        viewModelScope.launch {
            try { messages.send(action()) }
            catch (error: CancellationException) { throw error }
            catch (error: Exception) { messages.send(errorText(error)) }
            finally { _networkBusy.value = false }
        }
    }

    private fun errorText(error: Exception): String = when (error) {
        is IOException -> "Không kết nối được API. Kiểm tra server và địa chỉ mạng. Dữ liệu Room vẫn được giữ."
        is HttpException -> "API trả lỗi HTTP ${error.code()}. Dữ liệu Room vẫn được giữ."
        is IllegalArgumentException, is IllegalStateException -> error.message ?: "Dữ liệu không hợp lệ."
        else -> "Thao tác chưa thành công. Vui lòng kiểm tra dữ liệu hoặc phản hồi API."
    }
}
