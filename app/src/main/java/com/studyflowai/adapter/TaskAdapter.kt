package com.studyflowai.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.studyflowai.R
import com.studyflowai.data.entity.Task
import com.studyflowai.databinding.ItemTaskRoyalBinding
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val onDoneClick: (Task) -> Unit,
    private val onItemClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskRoyalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(private val binding: ItemTaskRoyalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.tvTaskTitle.text = task.title
            binding.tvTaskDesc.text = task.description
            
            val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
            binding.tvTaskDeadline.text = sdf.format(Date(task.deadline))
            binding.tvTaskDuration.text = task.estimatedTime.toString() + " min"

            binding.cbCompleted.isChecked = task.isCompleted
            
            // Visual feedback for priority
            val priorityColor = when (task.priority) {
                3 -> R.color.error_red
                2 -> R.color.warning
                else -> R.color.copper_gold
            }
            binding.priorityIndicator.setBackgroundColor(binding.root.context.getColor(priorityColor))

            binding.cbCompleted.setOnClickListener {
                onDoneClick(task)
            }

            binding.root.setOnClickListener {
                onItemClick(task)
            }
            
            updateChronoDisplay(task.realTime)
        }

        private fun updateChronoDisplay(totalMinutes: Int) {
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            binding.tvChronoDisplay.text = String.format(Locale.getDefault(), "%02d:%02d:00", hours, minutes)
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
    }
}
