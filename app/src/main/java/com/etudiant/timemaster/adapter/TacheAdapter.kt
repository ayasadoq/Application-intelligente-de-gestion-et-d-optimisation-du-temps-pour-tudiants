package com.etudiant.timemaster.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.etudiant.timemaster.data.entity.Tache
import com.etudiant.timemaster.databinding.ItemTacheBinding
import com.etudiant.timemaster.utils.ChronometreHelper
import java.text.SimpleDateFormat
import java.util.*

class TacheAdapter(
    private val onPlayPauseClick: (Tache) -> Unit,
    private val onDoneClick: (Tache) -> Unit,
    private val onItemClick: (Tache) -> Unit
) : ListAdapter<Tache, TacheAdapter.TacheViewHolder>(TacheDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TacheViewHolder {
        val binding = ItemTacheBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TacheViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TacheViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TacheViewHolder(private val binding: ItemTacheBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tache: Tache) {
            binding.tvTitre.text = tache.titre
            binding.tvDesc.text = tache.description
            
            val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
            binding.tvEcheance.text = "📅 ${sdf.format(Date(tache.dateLimite))}"

            // Affichage du score de priorité (BF2)
            binding.tvScore.text = String.format("Score: %.0f", tache.scorePriorite)
            binding.tvScore.setTextColor(when {
                tache.scorePriorite > 80 -> Color.RED
                tache.scorePriorite > 50 -> Color.parseColor("#FFAB40")
                else -> Color.parseColor("#4CAF50")
            })

            // Priorité couleur
            val color = when (tache.priorite) {
                3 -> "#FF5252" // Haute
                2 -> "#FFAB40" // Moyenne
                else -> "#4CAF50" // Basse
            }
            binding.viewPriority.setBackgroundColor(Color.parseColor(color))

            // Chronomètre
            updateChronoDisplay(tache.dureeReelle)
            
            val isRunning = ChronometreHelper.isTacheRunning(tache.id)
            binding.btnPlayPause.setImageResource(
                if (isRunning) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            )

            binding.cbDone.isChecked = tache.estTerminee
            
            binding.btnPlayPause.setOnClickListener { onPlayPauseClick(tache) }
            binding.cbDone.setOnClickListener { onDoneClick(tache) }
            binding.root.setOnClickListener { onItemClick(tache) }
        }

        fun updateChronoDisplay(seconds: Int) {
            val h = seconds / 3600
            val m = (seconds % 3600) / 60
            val s = seconds % 60
            binding.tvChrono.text = String.format("⏱️ %02d:%02d:%02d", h, m, s)
        }
    }

    class TacheDiffCallback : DiffUtil.ItemCallback<Tache>() {
        override fun areItemsTheSame(oldItem: Tache, newItem: Tache) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Tache, newItem: Tache) = oldItem == newItem
    }
}
