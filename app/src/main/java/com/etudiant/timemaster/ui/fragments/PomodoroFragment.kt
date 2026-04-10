package com.etudiant.timemaster.ui.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.etudiant.timemaster.databinding.FragmentPomodoroBinding
import com.etudiant.timemaster.ui.viewmodel.TacheViewModel
import java.util.Locale

class PomodoroFragment : Fragment() {

    private var _binding: FragmentPomodoroBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TacheViewModel by viewModels()

    private var timer: CountDownTimer? = null
    private var isRunning = false
    private var timeLeftInMillis = 25 * 60 * 1000L

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
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isRunning) {
            // Penalize if they leave the app during pomodoro
            viewModel.penalizeFocus()
        }
    }

    override fun onResume() {
        super.onResume()
        // Check if we should restore some focus or just show current
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
                viewModel.completePomodoro()
                // Play sound/vibrate here
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
        timeLeftInMillis = 25 * 60 * 1000L
        updateCountDownText()
        updateProgressBar()
        binding.btnStartStop.text = "Start"
    }

    private fun updateCountDownText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        binding.tvTimer.text = timeFormatted
    }

    private fun updateProgressBar() {
        val progress = (timeLeftInMillis.toFloat() / (25 * 60 * 1000L) * 100).toInt()
        binding.pomodoroProgress.progress = progress
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _binding = null
    }
}
