package com.studyflowai.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.studyflowai.R
import com.studyflowai.data.entity.Achievement
import com.studyflowai.databinding.ItemAchievementBinding

class AchievementAdapter : RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    private var achievements: List<Achievement> = emptyList()

    fun setAchievements(newAchievements: List<Achievement>) {
        this.achievements = newAchievements
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val binding = ItemAchievementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AchievementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(achievements[position])
    }

    override fun getItemCount(): Int = achievements.size

    class AchievementViewHolder(private val binding: ItemAchievementBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(achievement: Achievement) {
            binding.tvAchievementName.text = achievement.name
            binding.ivAchievementIcon.setImageResource(achievement.iconResId)
            
            if (achievement.isUnlocked) {
                binding.ivAchievementIcon.alpha = 1.0f
                binding.ivAchievementIcon.setTint(binding.root.context.getColor(R.color.quantum_accent))
                binding.tvAchievementName.setTextColor(binding.root.context.getColor(R.color.white))
                binding.ivAchievementIcon.strokeColor = android.content.res.ColorStateList.valueOf(binding.root.context.getColor(R.color.copper_gold))
            } else {
                binding.ivAchievementIcon.alpha = 0.3f
                binding.ivAchievementIcon.setTint(binding.root.context.getColor(R.color.gray_medium))
                binding.tvAchievementName.setTextColor(binding.root.context.getColor(R.color.gray_medium))
                binding.ivAchievementIcon.strokeColor = android.content.res.ColorStateList.valueOf(binding.root.context.getColor(R.color.night_blue_light))
            }
        }
        
        // Helper to set tint since ShapeableImageView might need it
        private fun com.google.android.material.imageview.ShapeableImageView.setTint(color: Int) {
            this.imageTintList = android.content.res.ColorStateList.valueOf(color)
        }
    }
}

