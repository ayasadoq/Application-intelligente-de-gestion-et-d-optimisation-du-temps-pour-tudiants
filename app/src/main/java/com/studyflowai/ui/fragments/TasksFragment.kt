package com.studyflowai.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.studyflowai.ui.tasks.AddEditTaskActivity
import com.studyflowai.ui.adapter.TaskAdapter
import com.studyflowai.databinding.FragmentTaskListBinding
import com.studyflowai.viewmodel.TaskViewModel
import com.google.android.material.snackbar.Snackbar

class TasksFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by viewModels()
    private lateinit var adapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onItemClick = { task ->
                val intent = Intent(requireContext(), AddEditTaskActivity::class.java)
                intent.putExtra("TASK", task)
                startActivity(intent)
            },
            onDeleteClick = { task ->
                viewModel.delete(task)
                Snackbar.make(binding.root, "Task deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") { viewModel.insert(task) }
                    .show()
            },
            onStatusChange = { task ->
                viewModel.toggleTaskStatus(task)
            },
            onTimerUpdate = { task, newTime ->
                viewModel.updateTaskTime(task, newTime)
            }
        )
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            adapter.submitList(tasks)
        }

        viewModel.smartPlannedTasks.observe(viewLifecycleOwner) { tactical ->
            if (tactical.isNotEmpty()) {
                val topTask = tactical.first()
                binding.tvSuggestionText.text = "Next priority: ${topTask.title}"
            } else {
                binding.tvSuggestionText.text = "All tasks under control."
            }
        }
    }

    private fun setupListeners() {
        binding.fabAddTask.setOnClickListener {
            val intent = Intent(requireContext(), AddEditTaskActivity::class.java)
            startActivity(intent)
        }

        binding.tvSuggestionText.setOnClickListener {
            viewModel.seedTestTasks()
            Snackbar.make(binding.root, "Test tasks added!", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
