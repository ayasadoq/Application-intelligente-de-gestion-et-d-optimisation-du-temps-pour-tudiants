package com.etudiant.timemaster.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.etudiant.timemaster.databinding.FragmentSocialBinding

import com.etudiant.timemaster.data.model.Student
import com.etudiant.timemaster.ui.adapter.SocialAdapter
import java.util.Random

class SocialFragment : Fragment() {

    private var _binding: FragmentSocialBinding? = null
    private val binding get() = _binding!!
    private lateinit var socialAdapter: SocialAdapter

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
        
        binding.btnJoinRoom.setOnClickListener {
            val code = binding.etRoomCode.text.toString()
            if (code.isNotEmpty()) {
                simulateJoiningRoom(code)
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
