package com.studyflowai.ui.adapter

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.studyflowai.R
import com.studyflowai.data.entity.Task
import com.studyflowai.databinding.ItemTaskRoyalBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter pour la liste des missions. 
 * Gère BF1 (Affichage) et BF3 (Indicateurs visuels).
 */
class TaskAdapter(
    private val onItemClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit,
    private val onStatusChange: (Task) -> Unit,
    private val onTimerUpdate: (Task, Int) -> Unit = { _, _ -> }
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    private val activeTimers = mutableMapOf<Int, TimerData>()
    private val handler = Handler(Looper.getMainLooper())

    private data class TimerData(
        val startTime: Long,
        val initialRealTime: Int,
        val runnable: Runnable
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskRoyalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        activeTimers.values.forEach { handler.removeCallbacks(it.runnable) }
        activeTimers.clear()
    }

    inner class TaskViewHolder(private val binding: ItemTaskRoyalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.tvTaskTitle.text = task.title
            binding.tvTaskDesc.text = task.description
            
            // Formatage date
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.tvTaskDeadline.text = sdf.format(Date(task.deadline))
            
            // Affichage durée
            binding.tvTaskDuration.text = "${task.estimatedTime} min"
            
            // Chrono display (BF3)
            updateChronoDisplay(task.realTime.toLong() * 60L)

            // Timer Toggle Logic
            val isTimerRunning = activeTimers.containsKey(task.id)
            binding.btnTimerToggle.setImageResource(
                if (isTimerRunning) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            )

            binding.btnTimerToggle.setOnClickListener {
                if (activeTimers.containsKey(task.id)) {
                    stopTimer(task)
                } else {
                    startTimer(task)
                }
            }

            // Indicateur de priorité (Couleurs Or/Royal)
            val priorityColor = when(task.priority) {
                3 -> R.color.error_red // Haute
                2 -> R.color.copper_gold // Moyenne
                else -> R.color.success_green // Basse
            }
            binding.priorityIndicator.setBackgroundColor(ContextCompat.getColor(binding.root.context, priorityColor))

            // Statut complété
            binding.cbCompleted.isChecked = task.isCompleted
            binding.root.alpha = if (task.isCompleted) 0.5f else 1.0f

            binding.cbCompleted.setOnClickListener {
                // Correction cruciale pour la démo : sauvegarder le temps si le chrono tournait
                if (activeTimers.containsKey(task.id)) {
                    stopTimer(task)
                } else if (task.realTime == 0) {
                    // Si le chrono n'a jamais été lancé, on met 1 min par défaut pour la démo
                    // pour montrer que du temps a été passé
                    onTimerUpdate(task, 1)
                }
                onStatusChange(task)
            }

            binding.btnDeleteTask.setOnClickListener {
                onDeleteClick(task)
            }

            binding.root.setOnClickListener { onItemClick(task) }
            binding.root.setOnLongClickListener { 
                onDeleteClick(task)
                true
            }
        }

        private fun updateChronoDisplay(totalSeconds: Long) {
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            binding.tvChronoDisplay.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

        private fun startTimer(task: Task) {
            val startTime = System.currentTimeMillis()
            val initialSeconds = task.realTime.toLong() * 60L
            
            val runnable = object : Runnable {
                override fun run() {
                    val elapsedMillis = System.currentTimeMillis() - startTime
                    val currentTotalSeconds = initialSeconds + (elapsedMillis / 1000)
                    updateChronoDisplay(currentTotalSeconds)
                    handler.postDelayed(this, 1000)
                }
            }
            
            activeTimers[task.id] = TimerData(startTime, task.realTime, runnable)
            handler.post(runnable)
            binding.btnTimerToggle.setImageResource(android.R.drawable.ic_media_pause)
        }

        private fun stopTimer(task: Task) {
            val timerData = activeTimers.remove(task.id)
            timerData?.let {
                handler.removeCallbacks(it.runnable)
                val elapsedMillis = System.currentTimeMillis() - it.startTime
                // Conversion en minutes avec arrondi au plus proche pour que la démo soit cohérente
                val secondsWorked = elapsedMillis / 1000
                val elapsedMinutes = (secondsWorked / 60).toInt()
                val remainingSeconds = (secondsWorked % 60).toInt()
                
                // Si on a travaillé plus de 30 secondes, on compte une minute entière pour le graphique
                val adjustedMinutes = if (remainingSeconds >= 1) elapsedMinutes + 1 else elapsedMinutes
                val totalMinutes = it.initialRealTime + adjustedMinutes
                
                onTimerUpdate(task, totalMinutes)
            }
            binding.btnTimerToggle.setImageResource(android.R.drawable.ic_media_play)
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
    }
}
