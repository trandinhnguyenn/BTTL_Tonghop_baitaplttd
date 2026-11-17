package com.example.studentmvvm.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmvvm.databinding.FragmentStudentListBinding
import com.example.studentmvvm.model.Student
import com.example.studentmvvm.viewmodel.StudentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class StudentListFragment : Fragment() {
    private var _binding: FragmentStudentListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StudentViewModel by activityViewModels { (requireActivity() as MainActivity).studentFactory }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStudentListBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = StudentAdapter(
            onEdit = { openEditor(it) },
            onDelete = { student ->
                MaterialAlertDialogBuilder(requireContext()).setTitle("Xóa sinh viên?")
                    .setMessage("Xóa ${student.fullName} khỏi thiết bị? Thao tác này không xóa bản ghi trên API.")
                    .setNegativeButton("Hủy", null)
                    .setPositiveButton("Xóa") { _, _ -> viewModel.delete(student) }.show()
            }, onUpload = { student ->
                MaterialAlertDialogBuilder(requireContext()).setTitle("Gửi sinh viên lên API?")
                    .setMessage("Gửi ${student.fullName} (${student.studentCode}) lên máy chủ mẫu. Cùng MSSV sẽ được cập nhật trên máy chủ.")
                    .setNegativeButton("Hủy", null)
                    .setPositiveButton("Gửi") { _, _ -> viewModel.upload(student) }.show()
            })
        binding.rvStudents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStudents.adapter = adapter
        viewModel.allStudents.observe(viewLifecycleOwner) { students ->
            adapter.submitList(students)
            binding.emptyLayout.isVisible = students.isEmpty()
            binding.tvCount.text = "${students.size} sinh viên hiển thị · Room Database"
        }
        binding.etSearch.doAfterTextChanged { viewModel.search(it.toString()) }
        binding.fabAdd.setOnClickListener { openEditor(null) }
        binding.btnRefresh.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext()).setTitle("Nạp dữ liệu từ API?")
                .setMessage("Sinh viên cùng MSSV sẽ được cập nhật theo dữ liệu API. Các sinh viên khác trên thiết bị được giữ lại.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Nạp") { _, _ -> viewModel.refresh() }.show()
        }
        viewModel.networkBusy.observe(viewLifecycleOwner) { busy ->
            binding.progress.isVisible = busy
            binding.btnRefresh.isEnabled = !busy
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
            }
        }
    }
    private fun openEditor(student: Student?) {
        if (childFragmentManager.findFragmentByTag("STUDENT_EDITOR") == null) {
            AddStudentDialogFragment.newInstance(student).show(childFragmentManager, "STUDENT_EDITOR")
        }
    }
    override fun onDestroyView() {
        binding.rvStudents.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
