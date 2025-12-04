package com.example.tasktracker.presentation.task.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import com.example.tasktracker.data.model.Task
import com.example.tasktracker.data.repository.AuthRepository
import com.example.tasktracker.data.repository.TaskRepository
import com.example.tasktracker.databinding.BottomSheetEditTaskBinding
import com.example.tasktracker.presentation.task.TaskViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*

class EditTaskBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetEditTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var task: Task
    private val taskRepository = TaskRepository()
    private val authRepository = AuthRepository()

    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModel.Factory(taskRepository, authRepository)
    }

    companion object {
        private const val ARG_TASK = "task"

        fun newInstance(task: Task): EditTaskBottomSheet {
            return EditTaskBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TASK, task)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, com.example.tasktracker.R.style.Theme_TaskTracker_BottomSheet)

        arguments?.let {
            task = it.getParcelable(ARG_TASK) ?: throw IllegalStateException("Task is required")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetEditTaskBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = "Edit Task"

        setupPriorityDropdown()
        setupCategoryDropdown()
        populateTaskData()
        setupDatePicker()
        setupButtons()
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog as BottomSheetDialog
        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }

    private fun setupPriorityDropdown() {
        val priorityItems = listOf("Low", "Medium", "High")
        val priorityAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            priorityItems
        )

        binding.spinnerPriority.setAdapter(priorityAdapter)

        // Make it non-editable (dropdown only)
        binding.spinnerPriority.setOnClickListener {
            binding.spinnerPriority.showDropDown()
        }
    }

    private fun setupCategoryDropdown() {
        val categoryItems = listOf("Personal", "Work", "Shopping", "Health", "Education", "Finance", "Travel", "Fitness")
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categoryItems
        )

        binding.spinnerCategory.setAdapter(categoryAdapter)

        // Make it non-editable (dropdown only)
        binding.spinnerCategory.setOnClickListener {
            binding.spinnerCategory.showDropDown()
        }
    }

    private fun populateTaskData() {
        binding.etTitle.setText(task.title)
        binding.etDescription.setText(task.description)
        binding.spinnerPriority.setText(task.priority.displayName, false)
        binding.spinnerCategory.setText(task.category, false)
        binding.cbCompleted.isChecked = task.isCompleted

        task.dueDate?.let {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.etDueDate.setText(dateFormat.format(it))
        }
    }

    private fun setupDatePicker() {
        binding.etDueDate.setOnClickListener {
            val currentSelection = task.dueDate?.time ?: MaterialDatePicker.todayInUtcMilliseconds()

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Due Date")
                .setSelection(currentSelection)
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val date = Date(selection)
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                binding.etDueDate.setText(dateFormat.format(date))
            }

            datePicker.show(childFragmentManager, "DATE_PICKER")
        }
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            if (title.isEmpty()) {
                binding.etTitle.error = "Title is required"
                return@setOnClickListener
            }

            val priorityText = binding.spinnerPriority.text.toString()
            val priority = when(priorityText) {
                "High" -> Task.Priority.HIGH
                "Medium" -> Task.Priority.MEDIUM
                "Low" -> Task.Priority.LOW
                else -> Task.Priority.MEDIUM // Default
            }

            val category = binding.spinnerCategory.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val dueDateText = binding.etDueDate.text.toString().trim()
            val isCompleted = binding.cbCompleted.isChecked

            val dueDate = if (dueDateText.isNotEmpty()) {
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).parse(dueDateText)
            } else {
                null
            }

            val updatedTask = task.copy(
                title = title,
                description = description,
                priority = priority,
                dueDate = dueDate,
                isCompleted = isCompleted,
                category = category
            )

            taskViewModel.updateTask(task.id, updatedTask)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}