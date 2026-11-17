package com.example.studentmvvm.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.studentmvvm.databinding.DialogAddStudentBinding
import com.example.studentmvvm.model.Student
import com.example.studentmvvm.model.StudentValidator
import com.example.studentmvvm.viewmodel.StudentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.UUID

class AddStudentDialogFragment : DialogFragment() {
    private val viewModel: StudentViewModel by activityViewModels { (requireActivity() as MainActivity).studentFactory }
    private var form: DialogAddStudentBinding? = null
    private val token get() = requireArguments().getString("token")!!
    private val studentId get() = requireArguments().getInt("id")

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogAddStudentBinding.inflate(LayoutInflater.from(requireContext()))
        form = binding
        binding.etName.setText(savedInstanceState?.getString("name") ?: arguments?.getString("name", ""))
        binding.etCode.setText(savedInstanceState?.getString("code") ?: arguments?.getString("code", ""))
        binding.etEmail.setText(savedInstanceState?.getString("email") ?: arguments?.getString("email", ""))
        binding.etGpa.setText(savedInstanceState?.getString("gpa") ?: arguments?.getString("gpa", ""))
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (studentId == 0) "Thêm sinh viên" else "Sửa sinh viên")
            .setView(binding.root).setPositiveButton("Lưu", null).setNegativeButton("Hủy", null).create()
    }

    override fun onStart() {
        super.onStart()
        val alert = requireDialog() as AlertDialog
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val binding = form ?: return@setOnClickListener
            val name = binding.etName.text.toString().trim()
            val code = binding.etCode.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val gpa = StudentValidator.parseGpa(binding.etGpa.text.toString())
            val error = StudentValidator.validate(name, code, email, gpa)
            if (error != null) { binding.tvError.text = error; return@setOnClickListener }
            viewModel.save(Student(studentId, name, code, email, gpa!!), token)
        }
        viewModel.saveState.removeObservers(this)
        // DialogFragment tạo Dialog trực tiếp: dùng lifecycle của DialogFragment, không dùng viewLifecycleOwner.
        viewModel.saveState.observe(this) { state ->
            if (state?.token == token) {
                val binding = form ?: return@observe
                alert.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !state.busy
                alert.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled = !state.busy
                isCancelable = !state.busy
                binding.tvError.text = if (state.busy) "Đang lưu…" else state.error.orEmpty()
                if (state.success) dismiss()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        form?.let {
            outState.putString("name", it.etName.text.toString())
            outState.putString("code", it.etCode.text.toString())
            outState.putString("email", it.etEmail.text.toString())
            outState.putString("gpa", it.etGpa.text.toString())
        }
        super.onSaveInstanceState(outState)
    }
    override fun onDestroyView() { form = null; super.onDestroyView() }

    companion object {
        fun newInstance(student: Student?) = AddStudentDialogFragment().apply {
            arguments = bundleOf("token" to UUID.randomUUID().toString(), "id" to (student?.id ?: 0),
                "name" to student?.fullName.orEmpty(), "code" to student?.studentCode.orEmpty(),
                "email" to student?.email.orEmpty(), "gpa" to (student?.gpa?.toString() ?: ""))
        }
    }
}
