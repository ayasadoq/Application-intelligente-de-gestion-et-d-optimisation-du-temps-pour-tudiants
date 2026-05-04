package com.studyflowai.ui.tasks

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.studyflowai.data.entity.Task
import com.studyflowai.databinding.ActivityAddEditTaskBinding
import com.studyflowai.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

import com.studyflowai.notification.NotificationScheduler

import com.studyflowai.ui.base.BaseActivity

class AddEditTaskActivity : BaseActivity() {

    private lateinit var binding: ActivityAddEditTaskBinding
    private val viewModel: TaskViewModel by viewModels()
    private var deadline: Long = System.currentTimeMillis()
    private var existingTask: Task? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        existingTask = intent.getSerializableExtra("TASK") as? Task
        setupUI()
        setupListeners()
        observeViewModel()
    }

    private fun setupUI() {
        existingTask?.let {
            binding.toolbar.title = "Edit Task"
            binding.etTitle.setText(it.title)
            binding.etDescription.setText(it.description)
            binding.sliderImportance.value = it.priority.toFloat()
            binding.etDuration.setText(it.estimatedTime.toString())
            deadline = it.deadline
            updateDeadlineText()
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnDatePicker.setOnClickListener { showDatePicker() }
        binding.btnTimePicker.setOnClickListener { showTimePicker() }

        binding.btnSave.setOnClickListener {
            saveTask()
        }
    }

    private fun observeViewModel() {
        // Observers for standard task operations
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(deadline)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selection
            val oldCalendar = Calendar.getInstance()
            oldCalendar.timeInMillis = deadline
            calendar.set(Calendar.HOUR_OF_DAY, oldCalendar.get(Calendar.HOUR_OF_DAY))
            calendar.set(Calendar.MINUTE, oldCalendar.get(Calendar.MINUTE))
            deadline = calendar.timeInMillis
            updateDeadlineText()
        }
        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = deadline
        
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE))
            .setTitleText("Select Time")
            .build()

        timePicker.addOnPositiveButtonClickListener {
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            calendar.set(Calendar.MINUTE, timePicker.minute)
            deadline = calendar.timeInMillis
            updateDeadlineText()
        }
        timePicker.show(supportFragmentManager, "TIME_PICKER")
    }

    private fun updateDeadlineText() {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        binding.tvDeadlineDisplay.text = sdf.format(Date(deadline))
    }

    private fun saveTask() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val durationStr = binding.etDuration.text.toString().trim()
        val priority = binding.sliderImportance.value.toInt()

        if (title.isEmpty()) {
            binding.etTitle.error = "Title required"
            return
        }

        val duration = durationStr.toIntOrNull() ?: 0
        val userId = "default_user"

        val task = Task(
            id = existingTask?.id ?: 0,
            userId = userId,
            title = title,
            description = description,
            deadline = deadline,
            priority = priority,
            estimatedTime = duration,
            realTime = existingTask?.realTime ?: 0,
            isCompleted = existingTask?.isCompleted ?: false
        )

        if (existingTask == null) {
            viewModel.insert(task)
            NotificationScheduler.schedule(this, task)
            Toast.makeText(this, "Task created", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.update(task)
            NotificationScheduler.schedule(this, task)
            Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}
