package com.example.tasktracker.presentation.task

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tasktracker.R
import com.example.tasktracker.data.model.Task
import com.example.tasktracker.data.repository.AuthRepository
import com.example.tasktracker.data.repository.TaskRepository
import com.example.tasktracker.databinding.ActivityMainBinding
import com.example.tasktracker.presentation.auth.LoginActivity
import com.example.tasktracker.presentation.profile.ProfileActivity
import com.example.tasktracker.presentation.task.adapter.TaskAdapter
import com.example.tasktracker.presentation.task.dialog.AddTaskBottomSheet
import com.example.tasktracker.presentation.task.dialog.EditTaskBottomSheet
import com.example.tasktracker.presentation.task.dialog.TaskDetailBottomSheet
import com.example.tasktracker.presentation.task.state.TaskState
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val taskRepository = TaskRepository()
    private val authRepository = AuthRepository()

    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModel.Factory(taskRepository, authRepository)
    }

    private lateinit var taskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar with just title, no menu icons
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "My Tasks"
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        binding.toolbar.navigationIcon = null

        setupRecyclerView()
        setupFab()
        setupSwipeRefresh()
        observeTasks()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onToggleComplete = { task, isChecked ->
                taskViewModel.toggleTaskCompletion(task.id, isChecked)
            },
            onEdit = { task ->
                val detailDialog = TaskDetailBottomSheet.newInstance(task)
                detailDialog.show(supportFragmentManager, "TaskDetail")
            },
            onLongClick = { task ->
                val editDialog = EditTaskBottomSheet.newInstance(task)
                editDialog.show(supportFragmentManager, "EditTask")
                true
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = taskAdapter
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val task = taskAdapter.currentList[position]
                taskViewModel.deleteTask(task.id)

                Snackbar.make(binding.root, "Task deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        taskViewModel.createTask(task)
                    }
                    .show()
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun setupFab() {
        binding.fabAddTask.setOnClickListener {
            hideKeyboard()
            val addDialog = AddTaskBottomSheet()
            addDialog.show(supportFragmentManager, "AddTask")
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            taskViewModel.refreshTasks()
        }

        binding.swipeRefresh.setColorSchemeResources(
            R.color.primary,
            R.color.secondary,
            R.color.tertiary
        )
    }

    private fun observeTasks() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                taskViewModel.uiState.collect { state ->
                    when (state) {
                        is TaskState.Loading -> {
                            if (!binding.swipeRefresh.isRefreshing) {
                                binding.progressBar.visibility = View.VISIBLE
                            }
                            binding.emptyState.root.visibility = View.GONE
                        }
                        is TaskState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.swipeRefresh.isRefreshing = false
                            taskAdapter.submitList(state.tasks)

                            if (state.tasks.isEmpty()) {
                                binding.emptyState.root.visibility = View.VISIBLE
                                binding.recyclerView.visibility = View.GONE
                            } else {
                                binding.emptyState.root.visibility = View.GONE
                                binding.recyclerView.visibility = View.VISIBLE
                            }

                            val totalTasks = state.tasks.size
                            val completedTasks = state.tasks.count { it.isCompleted }

                            binding.tvTotalTasks.text = totalTasks.toString()
                            binding.tvCompletedTasks.text = completedTasks.toString()
                        }
                        is TaskState.Empty -> {
                            binding.progressBar.visibility = View.GONE
                            binding.swipeRefresh.isRefreshing = false
                            taskAdapter.submitList(emptyList())
                            binding.tvTotalTasks.text = "0"
                            binding.tvCompletedTasks.text = "0"
                            binding.emptyState.root.visibility = View.VISIBLE
                            binding.recyclerView.visibility = View.GONE
                        }
                        is TaskState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.swipeRefresh.isRefreshing = false
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                showProfile()
                true
            }
            R.id.filter_all -> {
                Snackbar.make(binding.root, "Showing All Tasks", Snackbar.LENGTH_SHORT).show()
                true
            }
            R.id.filter_completed -> {
                Snackbar.make(binding.root, "Showing Completed Tasks", Snackbar.LENGTH_SHORT).show()
                true
            }
            R.id.filter_pending -> {
                Snackbar.make(binding.root, "Showing Pending Tasks", Snackbar.LENGTH_SHORT).show()
                true
            }
            R.id.filter_high_priority -> {
                Snackbar.make(binding.root, "Showing High Priority Tasks", Snackbar.LENGTH_SHORT).show()
                true
            }
            R.id.filter_today -> {
                Snackbar.make(binding.root, "Showing Today's Tasks", Snackbar.LENGTH_SHORT).show()
                true
            }
            R.id.sort_priority -> {
                Snackbar.make(binding.root, "Sorted by Priority", Snackbar.LENGTH_SHORT).show()
                true
            }
            R.id.sort_due_date -> {
                Snackbar.make(binding.root, "Sorted by Due Date", Snackbar.LENGTH_SHORT).show()
                true
            }
            R.id.sort_title -> {
                Snackbar.make(binding.root, "Sorted by Title", Snackbar.LENGTH_SHORT).show()
                true
            }
            R.id.sort_created -> {
                Snackbar.make(binding.root, "Sorted by Created Date", Snackbar.LENGTH_SHORT).show()
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        authRepository.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }
}