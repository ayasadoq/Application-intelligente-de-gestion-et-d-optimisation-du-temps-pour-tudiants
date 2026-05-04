package com.studyflowai.ui.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.studyflowai.R
import com.studyflowai.databinding.FragmentPomodoroBinding
import com.studyflowai.viewmodel.TaskViewModel
import java.util.Locale

class PomodoroFragment : Fragment() {

    private var _binding: FragmentPomodoroBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by viewModels()

    private var timer: CountDownTimer? = null
    private var isRunning = false
    private var timeLeftInMillis = 25 * 60 * 1000L // 25 minutes standard

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPomodoroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        updateCountDownText()
        observeStats()

        binding.btnStartStop.setOnClickListener {
            if (isRunning) pauseTimer() else startTimer()
        }

        binding.btnReset.setOnClickListener {
            resetTimer()
        }
    }

    private fun observeStats() {
        viewModel.userStats.observe(viewLifecycleOwner) { stats ->
            stats?.let {
                binding.tvFocusScore.text = "Focus Score: ${it.focusScore}%"
                updateStatusColor(it.focusScore)
            }
        }
    }

    private fun updateStatusColor(focusScore: Int) {
        val color = when {
            focusScore >= 80 -> requireContext().getColor(R.color.success_green)
            focusScore >= 50 -> requireContext().getColor(R.color.copper_gold)
            else -> requireContext().getColor(R.color.error_red)
        }
        binding.pomodoroProgress.setIndicatorColor(color)
        binding.tvFocusScore.setTextColor(color)
    }

    private fun startTimer() {
        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
                updateProgressBar()
            }

            override fun onFinish() {
                isRunning = false
                binding.btnStartStop.text = "Start"
                // Reward XP for completing a Pomodoro session
                viewModel.addXp(50, isTaskCompletion = false, pomodoroCompleted = true)
            }
        }.start()

        isRunning = true
        binding.btnStartStop.text = "Pause"
    }

    private fun pauseTimer() {
        timer?.cancel()
        isRunning = false
        binding.btnStartStop.text = "Resume"
    }

    private fun resetTimer() {
        timer?.cancel()
        isRunning = false
        timeLeftInMillis = 25 * 60 * 1000L // Reset à 25 minutes
        updateCountDownText()
        updateProgressBar()
        binding.btnStartStop.text = "Start"
    }

    private fun updateCountDownText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        binding.tvTimer.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private fun updateProgressBar() {
        val totalTime = 25 * 60 * 1000L
        val progress = (timeLeftInMillis.toFloat() / totalTime * 100).toInt()
        binding.pomodoroProgress.progress = progress
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _binding = null
    }
}
