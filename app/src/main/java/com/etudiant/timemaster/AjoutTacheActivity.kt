package com.etudiant.timemaster

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.etudiant.timemaster.data.entity.Tache
import com.etudiant.timemaster.databinding.ActivityAjoutTacheBinding
import com.etudiant.timemaster.ui.viewmodel.TacheViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.*

class AjoutTacheActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAjoutTacheBinding
    private val viewModel: TacheViewModel by viewModels()
    private var dateEcheance: Long = System.currentTimeMillis()
    private var tacheExistante: Tache? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAjoutTacheBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tacheExistante = intent.getSerializableExtra("TACHE") as? Tache
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        tacheExistante?.let {
            binding.toolbar.title = "Modifier la tâche"
            binding.etTitre.setText(it.titre)
            binding.etDesc.setText(it.description)
            binding.sliderDuree.value = it.dureeEstimee.toFloat()
            dateEcheance = it.dateLimite
            updateDateText()
            
            when(it.priorite) {
                1 -> binding.togglePriority.check(R.id.btn_prio_low)
                2 -> binding.togglePriority.check(R.id.btn_prio_medium)
                3 -> binding.togglePriority.check(R.id.btn_prio_high)
            }
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnDate.setOnClickListener { showDateTimePicker() }

        binding.btnSave.setOnClickListener {
            saveTache()
        }
    }

    private fun showDateTimePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Sélectionner la date")
            .setSelection(dateEcheance)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selection
            
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Sélectionner l'heure")
                .build()

            timePicker.addOnPositiveButtonClickListener {
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                calendar.set(Calendar.MINUTE, timePicker.minute)
                dateEcheance = calendar.timeInMillis
                updateDateText()
            }
            timePicker.show(supportFragmentManager, "TIME_PICKER")
        }
        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun updateDateText() {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        binding.btnDate.text = "📅 Échéance : ${sdf.format(Date(dateEcheance))}"
    }

    private fun saveTache() {
        val titre = binding.etTitre.text.toString().trim()
        val desc = binding.etDesc.text.toString().trim()
        val duree = binding.sliderDuree.value.toInt()
        
        val priorite = when(binding.togglePriority.checkedButtonId) {
            R.id.btn_prio_low -> 1
            R.id.btn_prio_high -> 3
            else -> 2
        }

        if (titre.isEmpty()) {
            binding.etTitre.error = "Le titre est obligatoire"
            return
        }

        val nouvelleTache = Tache(
            id = tacheExistante?.id ?: 0,
            titre = titre,
            description = desc,
            dateLimite = dateEcheance,
            priorite = priorite,
            dureeEstimee = duree,
            dureeReelle = tacheExistante?.dureeReelle ?: 0,
            estTerminee = tacheExistante?.estTerminee ?: false
        )

        if (tacheExistante == null) {
            viewModel.insert(nouvelleTache)
            Toast.makeText(this, "Tâche ajoutée", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.update(nouvelleTache)
            Toast.makeText(this, "Tâche mise à jour", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}
