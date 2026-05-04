package com.studyflowai.utils

import android.content.Context
import android.view.LayoutInflater
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.studyflowai.databinding.DialogGuidedInputBinding
import java.util.Calendar

class GuidedInputHelper(private val context: Context) {

    fun showAiAssistantDialog(onConfirm: (String) -> Unit) {
        val binding = DialogGuidedInputBinding.inflate(LayoutInflater.from(context))
        
        val dialog = MaterialAlertDialogBuilder(context)
            .setView(binding.root)
            .setPositiveButton("Lancer l'IA", null)
            .setNegativeButton("Annuler", null)
            .create()

        // Humeur / Énergie
        binding.btnMoodLow.setOnClickListener {
            binding.etAiInput.setText("Je suis fatigué, aide-moi à alléger mon planning.")
            binding.tilAiInput.error = null
        }
        binding.btnMoodNormal.setOnClickListener {
            binding.etAiInput.setText("Je me sens normal, optimise ma journée.")
            binding.tilAiInput.error = null
        }
        binding.btnMoodHigh.setOnClickListener {
            binding.etAiInput.setText("Je suis boosté ! Propose-moi un sprint intensif.")
            binding.tilAiInput.error = null
        }

        // Actions rapides (SOS/Focus via IDs xml)
        binding.chipEmergency.setOnClickListener {
            binding.etAiInput.setText("SOS : Je suis en retard, sauve ma soirée !")
        }
        binding.chipFocus.setOnClickListener {
            binding.etAiInput.setText("Focus Express : session de 15min maintenant.")
        }
        binding.chipReplan.setOnClickListener {
            binding.etAiInput.setText("Replanifie mes tâches en retard.")
        }

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val input = binding.etAiInput.text.toString().trim()
                
                when {
                    input.isEmpty() -> {
                        binding.tilAiInput.error = "Choisis une option ou tape ta demande."
                    }
                    else -> {
                        onConfirm(input)
                        dialog.dismiss()
                    }
                }
            }
        }

        updateChipsByTime(binding)
        dialog.show()
    }

    private fun updateChipsByTime(binding: DialogGuidedInputBinding) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.cgQuickActions.removeAllViews()
        
        val suggestions = when {
            hour in 5..11 -> listOf("Matinée studieuse", "Planifier ma journée", "Révision flash")
            hour in 12..17 -> listOf("Après-midi focus", "Pause productive", "Terminer mes devoirs")
            hour in 18..22 -> listOf("Session nuit", "Bilan du soir", "Préparer demain")
            else -> listOf("Planifier demain", "Insomnie productive", "Réveil tôt")
        }
        
        suggestions.forEach { text ->
            val chip = Chip(context)
            chip.text = text
            chip.setOnClickListener {
                binding.etAiInput.setText(text)
                binding.tilAiInput.error = null
            }
            binding.cgQuickActions.addView(chip)
        }
    }
}
