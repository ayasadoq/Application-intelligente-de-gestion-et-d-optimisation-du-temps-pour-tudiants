package com.etudiant.timemaster.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.etudiant.timemaster.AjoutTacheActivity
import com.etudiant.timemaster.adapter.TacheAdapter
import com.etudiant.timemaster.databinding.FragmentPlanningBinding
import com.etudiant.timemaster.ui.viewmodel.TacheViewModel
import com.etudiant.timemaster.utils.ChronometreHelper
import java.util.*

class PlanningFragment : Fragment() {

    private var _binding: FragmentPlanningBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TacheViewModel by viewModels()
    private lateinit var adapter: TacheAdapter
    private var selectedDate: Long = System.currentTimeMillis()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlanningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupCalendar()
        observeDate(selectedDate)
    }

    private fun setupRecyclerView() {
        adapter = TacheAdapter(
            onPlayPauseClick = { tache ->
                if (ChronometreHelper.isTacheRunning(tache.id)) {
                    val result = ChronometreHelper.stop()
                    viewModel.updateTempsPasse(tache.id, result.second)
                } else {
                    ChronometreHelper.start(tache.id)
                }
                adapter.notifyDataSetChanged()
            },
            onDoneClick = { tache ->
                viewModel.toggleTerminee(tache)
            },
            onItemClick = { tache ->
                val intent = Intent(requireContext(), AjoutTacheActivity::class.java)
                intent.putExtra("TACHE", tache)
                startActivity(intent)
            }
        )
        binding.rvPlanning.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPlanning.adapter = adapter
    }

    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.timeInMillis
            observeDate(selectedDate)
        }
    }

    private fun observeDate(timestamp: Long) {
        viewModel.getTachesParDate(timestamp).observe(viewLifecycleOwner) { taches ->
            // Tri par priorité pour le "Planning Optimal"
            val sortedTaches = taches.sortedByDescending { it.scorePriorite }
            adapter.submitList(sortedTaches)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
