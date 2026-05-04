package com.studyflowai.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.studyflowai.data.model.Student
import com.studyflowai.databinding.ItemStudentActiveBinding

class SocialAdapter : RecyclerView.Adapter<SocialAdapter.StudentViewHolder>() {

    private var students = listOf<Student>()

    fun setStudents(newStudents: List<Student>) {
        students = newStudents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = ItemStudentActiveBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(students[position])
    }

    override fun getItemCount() = students.size

    class StudentViewHolder(private val binding: ItemStudentActiveBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(student: Student) {
            binding.tvStudentName.text = student.name
            binding.tvStudentStatus.text = "Focus: ${student.status} (${student.focusScore}%)"
            // If offline, we could show different colors for status
        }
    }
}

