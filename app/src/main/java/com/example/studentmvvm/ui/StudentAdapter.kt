package com.example.studentmvvm.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmvvm.databinding.ItemStudentRoomBinding
import com.example.studentmvvm.model.Student
import java.util.Locale

class StudentAdapter(
    private val onEdit: (Student) -> Unit,
    private val onDelete: (Student) -> Unit,
    private val onUpload: (Student) -> Unit
) : ListAdapter<Student, StudentAdapter.StudentViewHolder>(Diff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = StudentViewHolder(
        ItemStudentRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) = holder.bind(getItem(position))
    inner class StudentViewHolder(private val binding: ItemStudentRoomBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(student: Student) = with(binding) {
            tvName.text = student.fullName
            tvCode.text = "MSSV: ${student.studentCode}"
            tvEmail.text = student.email
            tvGpa.text = String.format(Locale.getDefault(), "GPA %.2f / 4", student.gpa)
            tvAvatar.text = student.fullName.trim().substringAfterLast(' ').take(1).uppercase()
            btnEdit.setOnClickListener { onEdit(student) }
            btnDelete.setOnClickListener { onDelete(student) }
            btnUpload.setOnClickListener { onUpload(student) }
        }
    }
    class Diff : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(oldItem: Student, newItem: Student) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Student, newItem: Student) = oldItem == newItem
    }
}
