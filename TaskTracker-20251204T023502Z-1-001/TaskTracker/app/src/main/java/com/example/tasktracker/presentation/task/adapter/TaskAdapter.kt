package com.example.tasktracker.presentation.task.adapter

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tasktracker.R
import com.example.tasktracker.data.model.Task
import com.example.tasktracker.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val onToggleComplete: (Task, Boolean) -> Unit,
    private val onEdit: (Task) -> Unit,
    private val onLongClick: ((Task) -> Boolean)? = null
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task)
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            // Title with strikethrough if completed
            binding.tvTitle.text = task.title
            if (task.isCompleted) {
                binding.tvTitle.paintFlags = android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvTitle.alpha = 0.6f
            } else {
                binding.tvTitle.paintFlags = 0
                binding.tvTitle.alpha = 1.0f
            }

            // Description
            if (task.description.isNotEmpty()) {
                binding.tvDescription.text = task.description
                binding.tvDescription.visibility = android.view.View.VISIBLE
            } else {
                binding.tvDescription.visibility = android.view.View.GONE
            }

            // Due date
            if (task.dueDate != null) {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                binding.tvDueDate.text = "Due: ${dateFormat.format(task.dueDate)}"
                binding.tvDueDate.visibility = android.view.View.VISIBLE
            } else {
                binding.tvDueDate.visibility = android.view.View.GONE
            }

            // Category
            binding.chipCategory.text = task.category

            // Priority indicator color
            val priorityColor = when (task.priority) {
                Task.Priority.HIGH -> R.color.priority_high
                Task.Priority.MEDIUM -> R.color.priority_medium
                Task.Priority.LOW -> R.color.priority_low
            }
            binding.priorityIndicator.background.setColorFilter(
                ContextCompat.getColor(binding.root.context, priorityColor),
                PorterDuff.Mode.SRC_IN
            )

            // Checkbox state
            binding.cbCompleted.setOnCheckedChangeListener(null) // Clear previous listener
            binding.cbCompleted.isChecked = task.isCompleted
            binding.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                onToggleComplete(task, isChecked)
            }

            // Click listener for edit (short click)
            binding.root.setOnClickListener {
                onEdit(task)
            }

            binding.root.setOnLongClickListener {
                onLongClick?.invoke(task) ?: false
            }
        }
    }

    companion object {
        private val TaskDiffCallback = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem == newItem
            }
        }
    }
}