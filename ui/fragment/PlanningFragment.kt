package com.etudiant.timemaster.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.etudiant.timemaster.adapter.TacheAdapter
import com.etudiant.timemaster.databinding.FragmentPlanningBinding
import com.etudiant.timemaster.viewmodel.TacheViewModel
import java.util.*

class PlanningFragment : Fragment() {

    private var _binding: FragmentPlanningBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TacheViewModel
    private lateinit var adapter: TacheAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlanningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[TacheViewModel::class.java]
        setupRecyclerView()

        val calendar = Calendar.getInstance()
        updateList(calendar.timeInMillis)

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth, 0, 0, 0)
            updateList(selectedDate.timeInMillis)
        }
    }

    private fun setupRecyclerView() {
        adapter = TacheAdapter(
            onStatusChanged = { tache -> viewModel.update(tache) },
            onTimerAction = { /* handle timer */ },
            onDelete = { tache -> viewModel.delete(tache) }
        )
        binding.rvPlanning.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PlanningFragment.adapter
        }
    }

    private fun updateList(date: Long) {
        viewModel.getTachesByDate(date).observe(viewLifecycleOwner) { taches ->
            adapter.submitList(taches)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
