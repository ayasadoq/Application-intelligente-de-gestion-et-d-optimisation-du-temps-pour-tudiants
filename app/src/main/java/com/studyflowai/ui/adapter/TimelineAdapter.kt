package com.studyflowai.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.studyflowai.data.entity.Task
import com.studyflowai.databinding.ItemTimelineSimplifiedBinding
import java.text.SimpleDateFormat
import java.util.*

class TimelineAdapter : ListAdapter<Task, TimelineAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTimelineSimplifiedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemTimelineSimplifiedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.tvTaskTitle.text = task.title
            
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            // Utilise deadline comme heure de fin et calcule le début
            val startTime = task.deadline - (task.estimatedTime * 60_000L)
            binding.tvTaskTime.text = sdf.format(Date(startTime))
            
            binding.tvTaskDuration.text = "${task.estimatedTime} min"

            val colorRes = when (task.priority) {
                3 -> android.R.color.holo_red_light
                2 -> android.R.color.holo_orange_light
                else -> android.R.color.holo_blue_light
            }
            
            try {
                binding.viewPriorityIndicator.setBackgroundColor(ContextCompat.getColor(binding.root.context, colorRes))
            } catch (e: Exception) {
                binding.viewPriorityIndicator.setBackgroundColor(
                    if (task.priority == 3) 0xFFFF5252.toInt() else 0xFF2196F3.toInt()
                )
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
    }
}
