package com.studyflowai.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.studyflowai.R
import com.studyflowai.databinding.FragmentDashboardBinding
import com.studyflowai.utils.SecurityManager
import com.studyflowai.utils.GuidedInputHelper
import com.studyflowai.viewmodel.TaskViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.work.*
import com.studyflowai.worker.RescheduleWorker
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import android.graphics.Color
import java.util.concurrent.TimeUnit
import com.studyflowai.ui.adapter.CalendarAdapter
import android.widget.EditText
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.startDataInitialization()
        setupCalendar()
        setupUserHeader()
        observeViewModel()
        setupListeners()
        setupAutoRescheduling()
    }

    private fun setupCalendar() {
        val calendarAdapter = CalendarAdapter { date ->
            // Optionnel : filtrer les tâches par date dans le ViewModel
            viewModel.loadTasksForDate(date.time)
        }
        binding.rvCalendar.adapter = calendarAdapter
        binding.rvCalendar.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupAutoRescheduling() {
        val request = PeriodicWorkRequestBuilder<RescheduleWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            "RescheduleWork",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun setupUserHeader() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val name = user.displayName?.split(" ")?.get(0) ?: "Student"
            binding.tvGreeting.text = "Hello, $name"
        }
    }

    private fun observeViewModel() {
        viewModel.dashboardData.observe(viewLifecycleOwner) { data ->
            binding.tvStatCompleted.text = data.completedCount.toString()
            binding.tvStatOverdue.text = data.overdueCount.toString()

            binding.pbDailyProgress.progress = data.progressPercent
            binding.tvProgressPercent.text = "${data.progressPercent}%"

            binding.tvProgressMsg.text = "You are ${data.progressPercent}% on track today"
            binding.tvProgressSub.text = "${data.pendingTasksCount} tasks left to reach your goal."

            // Local insight based on progress (fallback)
            val insight = when {
                data.progressPercent >= 80 -> "Excellent work! Keep it up."
                data.progressPercent >= 50 -> "Good progress. Stay focused!"
                else -> "A fresh start is waiting. You can do this!"
            }
            binding.tvAiInsight.text = insight

            // Fetch AI insight ONLY if it hasn't been fetched for this progress yet
            if (binding.tvAiInsight.tag != data.progressPercent) {
                binding.tvAiInsight.tag = data.progressPercent
                viewModel.fetchAiInsight(data.progressPercent)
            }
        }

        // Observer AI response from Groq
        viewModel.aiResponse.observe(viewLifecycleOwner) { advice ->
            binding.tvAiInsight.text = advice
        }

        viewModel.aiError.observe(viewLifecycleOwner) { error ->
            if (error.contains("404") || error.contains("NOT_FOUND")) {
                binding.tvAiInsight.text = "Modèle non trouvé. Vérifiez votre clé API ou si le modèle est disponible dans votre région."
            } else if (error.contains("403")) {
                binding.tvAiInsight.text = "Clé API invalide ou accès refusé."
            } else {
                binding.tvAiInsight.text = "Note: $error"
            }
        }

        viewModel.isAiLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.pbDailyProgress.isIndeterminate = isLoading
        }

        viewModel.userStats.observe(viewLifecycleOwner) { stats ->
            stats?.let {
                binding.tvLevelInfo.text = "Niveau ${it.level} (${it.xp}/${it.xpToNextLevel} XP)"
                binding.pbLevelProgress.max = it.xpToNextLevel
                binding.pbLevelProgress.progress = it.xp
            }
        }

        viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            val activeTasks = tasks.filter { !it.isCompleted }.take(5)
            if (activeTasks.isNotEmpty()) {
                updateGanttChart(activeTasks)
            }
        }
    }

    private fun updateGanttChart(tasks: List<com.studyflowai.data.entity.Task>) {
        if (tasks.isEmpty()) return

        // On prend le début de la première tâche comme référence (T0)
        val now = System.currentTimeMillis()
        val minStart = tasks.minOf { it.deadline - (it.estimatedTime * 60000) }.coerceAtMost(now)

        val entries = tasks.mapIndexed { index, task ->
            val startOffset = ((task.deadline - (task.estimatedTime * 60000)) - minStart).coerceAtLeast(0).toFloat() / 60000f // en minutes
            val duration = task.estimatedTime.toFloat()
            // Stacked bar: [offset (transparent), duration (visible)]
            BarEntry(index.toFloat(), floatArrayOf(startOffset, duration))
        }

        val dataSet = BarDataSet(entries, "Planning (minutes)").apply {
            setDrawValues(false)
            // La première partie (offset) est transparente, la deuxième a la couleur de la tâche
            colors = listOf(Color.TRANSPARENT, Color.parseColor("#6200EE"))
        }

        binding.ganttChart.apply {
            data = BarData(dataSet)
            description.isEnabled = false

            // Configuration des axes pour la lisibilité
            xAxis.isEnabled = true
            xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.textColor = Color.WHITE

            axisLeft.textColor = Color.WHITE
            axisLeft.setDrawGridLines(true)
            axisLeft.gridColor = Color.DKGRAY
            axisRight.isEnabled = false

            legend.isEnabled = false
            setTouchEnabled(false)
            animateY(1000)
            invalidate()
        }
    }

    private fun setupListeners() {
        binding.btnHelpMain.setOnClickListener {
            HelpBottomSheet().show(parentFragmentManager, HelpBottomSheet.TAG)
        }
        binding.btnGoPlanner.setOnClickListener {
            findNavController().navigate(R.id.navigation_planner)
        }
        binding.btnGoFocus.setOnClickListener {
            findNavController().navigate(R.id.navigation_focus)
        }
        binding.btnConfigAi.setOnClickListener {
            showAiConfigDialog()
        }
        binding.cvAiCoach.setOnClickListener {
            GuidedInputHelper(requireContext()).showAiAssistantDialog { userInput ->
                viewModel.generateSmartSchedule(userInput)
            }
        }
        binding.fabChatbot.setOnClickListener {
            findNavController().navigate(R.id.navigation_chat)
        }
    }

    private fun showAiConfigDialog() {
        val securityManager = SecurityManager(requireContext())
        val currentKey = securityManager.getGroqKey() ?: ""

        val input = EditText(requireContext()).apply {
            hint = "Clé API Groq (gsk_...)"
            setText(currentKey)
            setPadding(64, 32, 64, 32)
        }

        val container = FrameLayout(requireContext())
        container.addView(input)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Configuration IA Gratuite (Groq)")
            .setMessage("Entrez votre clé API Groq pour activer le coach. C'est gratuit et ultra-rapide !")
            .setView(container)
            .setNeutralButton("Obtenir une clé") { _, _ ->
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://console.groq.com/keys"))
                startActivity(intent)
            }
            .setPositiveButton("Enregistrer") { _, _ ->
                val newKey = input.text.toString().trim()
                if (newKey.isNotEmpty()) {
                    securityManager.saveGroqKey(newKey)
                    // Déclenche une mise à jour immédiate
                    viewModel.dashboardData.value?.let {
                        viewModel.fetchAiInsight(it.progressPercent)
                    }
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
