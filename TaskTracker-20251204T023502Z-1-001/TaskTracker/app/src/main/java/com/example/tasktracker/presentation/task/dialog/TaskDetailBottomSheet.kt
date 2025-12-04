package com.example.tasktracker.presentation.task.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tasktracker.data.model.Task
import com.example.tasktracker.databinding.DialogTaskDetailBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Locale

class TaskDetailBottomSheet : BottomSheetDialogFragment() {

    private var _binding: DialogTaskDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var task: Task

    companion object {
        private const val ARG_TASK = "task"

        fun newInstance(task: Task): TaskDetailBottomSheet {
            return TaskDetailBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TASK, task)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set bottom sheet style
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
        _binding = DialogTaskDetailBinding.inflate(inflater, container, false)

        // Set rounded corners
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    override fun onStart() {
        super.onStart()

        // Set bottom sheet to expand fully
        val dialog = dialog as com.google.android.material.bottomsheet.BottomSheetDialog
        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
            behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }

    private fun setupUI() {
        binding.tvTitle.text = task.title
        binding.tvDescription.text = task.description
        binding.tvPriority.text = task.priority.displayName
        binding.tvCategory.text = task.category

        if (task.dueDate != null) {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvDueDate.text = dateFormat.format(task.dueDate)
        } else {
            binding.tvDueDate.text = "No due date"
        }

        binding.tvStatus.text = if (task.isCompleted) "Completed" else "In Progress"
        binding.tvStatus.setTextColor(
            if (task.isCompleted) {
                resources.getColor(android.R.color.holo_green_dark, null)
            } else {
                resources.getColor(android.R.color.holo_orange_dark, null)
            }
        )

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}