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
import com.studyflowai.databinding.FragmentPlanningBinding
import com.studyflowai.viewmodel.TaskViewModel
import java.util.*

class PlanningFragment : Fragment() {

    private var _binding: FragmentPlanningBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by viewModels()
    private lateinit var adapter: TaskAdapter
    private var selectedDate: Long = System.currentTimeMillis()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlanningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupCalendar()
        observeDate(selectedDate)
        setupAiButton()
        observeHealthScore()
    }

    private fun observeHealthScore() {
        viewModel.healthScore.observe(viewLifecycleOwner) { score ->
            binding.cvDayHealth.visibility = View.VISIBLE
            binding.tvHealthScore.text = "Score de santé : $score%"
            binding.pbHealth.progress = score
            
            // Couleur dynamique
            val color = when {
                score > 70 -> android.graphics.Color.GREEN
                score > 40 -> android.graphics.Color.YELLOW
                else -> android.graphics.Color.RED
            }
            binding.pbHealth.setIndicatorColor(color)
        }
    }

    private fun setupAiButton() {
        binding.btnAiSuggest.setOnClickListener {
            triggerAiAnalysis()
        }

        binding.chipLowEnergy.setOnClickListener { triggerAiAnalysis() }
        binding.chipMidEnergy.setOnClickListener { triggerAiAnalysis() }
        binding.chipHighEnergy.setOnClickListener { triggerAiAnalysis() }

        viewModel.planningAiSuggestion.observe(viewLifecycleOwner) { suggestion: String ->
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Bilan de ta journée 📊")
                .setMessage(suggestion)
                .setPositiveButton("C'est noté !") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun triggerAiAnalysis() {
        val currentTasks = adapter.currentList
        if (currentTasks.isEmpty()) {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Planning vide")
                .setMessage("Ajoute quelques tâches avant de demander une analyse ! ✍️")
                .setPositiveButton("OK", null)
                .show()
        } else {
            val energy = when {
                binding.chipLowEnergy.isChecked -> "Fatigué"
                binding.chipHighEnergy.isChecked -> "Boosté"
                else -> "Normal"
            }
            viewModel.getPlanningSuggestion(currentTasks, energy)
        }
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
            },
            onStatusChange = { task ->
                viewModel.toggleTaskStatus(task)
            },
            onTimerUpdate = { task, newTime ->
                viewModel.updateTaskTime(task, newTime)
            }
        )
        binding.rvPlanning.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPlanning.adapter = adapter
    }

    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.timeInMillis
            observeDate(selectedDate)
        }
    }

    private fun observeDate(timestamp: Long) {
        viewModel.getTasksByDate(timestamp).observe(viewLifecycleOwner) { sortedTasks ->
            adapter.submitList(sortedTasks)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
