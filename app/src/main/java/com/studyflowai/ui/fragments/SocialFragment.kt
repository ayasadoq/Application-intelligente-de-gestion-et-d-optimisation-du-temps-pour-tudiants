package com.studyflowai.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.studyflowai.databinding.FragmentSocialBinding

import com.studyflowai.data.model.Student
import com.studyflowai.ui.adapter.SocialAdapter
import com.studyflowai.ui.viewmodel.SocialViewModel
import java.util.Random

class SocialFragment : Fragment() {

    private var _binding: FragmentSocialBinding? = null
    private val binding get() = _binding!!
    private lateinit var socialAdapter: SocialAdapter
    private val viewModel: SocialViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSocialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        
        viewModel.activeStudents.observe(viewLifecycleOwner) { students ->
            socialAdapter.setStudents(students)
            if (students.size > 1) {
                // Optionnel: petit toast quand quelqu'un rejoint
            }
        }
        
        binding.btnJoinRoom.setOnClickListener {
            val code = binding.etRoomCode.text.toString()
            if (code.isNotEmpty()) {
                // On utilise un nom par défaut ou on pourrait demander à l'utilisateur
                viewModel.joinRoom(code, "Étudiant " + (1..100).random())
                Toast.makeText(requireContext(), "Connexion à la salle $code...", Toast.LENGTH_SHORT).show()
                binding.etRoomCode.isEnabled = false
                binding.btnJoinRoom.isEnabled = false
            }
        }
    }

    private fun setupRecyclerView() {
        socialAdapter = SocialAdapter()
        binding.rvActiveStudents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = socialAdapter
        }
    }

    private fun simulateJoiningRoom(code: String) {
        Toast.makeText(requireContext(), "Salle $code rejointe !", Toast.LENGTH_SHORT).show()
        
        // Simuler des données dynamiques venant d'un "serveur"
        val mockStudents = listOf(
            Student("1", "Moi (Toi)", "Maths", isOnline = true, focusScore = 100),
            Student("2", "Alice", "Physique", isOnline = true, focusScore = 95),
            Student("3", "Bob", "Java Coding", isOnline = true, focusScore = 88),
            Student("4", "Charlie", "Design UI", isOnline = true, focusScore = 92)
        )
        socialAdapter.setStudents(mockStudents)
        
        // Simuler un nouvel arrivant après 3 secondes
        binding.root.postDelayed({
            val updatedList = mockStudents.toMutableList()
            updatedList.add(Student("5", "Nouvel Étudiant", "Pause", isOnline = true, focusScore = 10))
            socialAdapter.setStudents(updatedList)
            Toast.makeText(requireContext(), "Un nouvel étudiant a rejoint la salle !", Toast.LENGTH_SHORT).show()
        }, 5000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

