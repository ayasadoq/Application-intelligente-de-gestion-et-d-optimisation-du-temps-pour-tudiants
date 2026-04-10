package com.etudiant.timemaster.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.etudiant.timemaster.AjoutTacheActivity
import com.etudiant.timemaster.adapter.TacheAdapter
import com.etudiant.timemaster.databinding.FragmentTachesBinding
import com.etudiant.timemaster.ui.viewmodel.TacheViewModel
import com.etudiant.timemaster.utils.ChronometreHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TachesFragment : Fragment() {

    private var _binding: FragmentTachesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TacheViewModel by viewModels()
    private lateinit var adapter: TacheAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTachesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeToDelete()
        observeViewModel()
        setupListeners()
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
                viewModel.completeTask(tache)
                showConfetti()
                Snackbar.make(binding.root, "Tâche terminée ! +XP", Snackbar.LENGTH_SHORT).show()
            },
            onItemClick = { tache ->
                val intent = Intent(requireContext(), AjoutTacheActivity::class.java)
                intent.putExtra("TACHE", tache)
                startActivity(intent)
            }
        )
        binding.rvTaches.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTaches.adapter = adapter
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val tache = adapter.currentList[position]
                
                viewModel.delete(tache)
                
                Snackbar.make(binding.root, "Tâche supprimée", Snackbar.LENGTH_LONG)
                    .setAction("ANNULER") {
                        viewModel.insert(tache)
                    }.show()
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvTaches)
    }

    private fun observeViewModel() {
        viewModel.allTaches.observe(viewLifecycleOwner) { taches ->
            adapter.submitList(taches)
        }

        viewModel.userStats.observe(viewLifecycleOwner) { stats ->
            stats?.let {
                binding.tvUserLevel.text = "Lvl ${it.level}"
                binding.tvUserXp.text = "${it.xp} XP"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            ChronometreHelper.tempsEcoule.collectLatest { seconds ->
                val runningId = ChronometreHelper.getCurrentTacheId()
                if (runningId != null) {
                    // Update the specific item in the list if visible
                    val position = adapter.currentList.indexOfFirst { it.id == runningId }
                    if (position != -1) {
                        val viewHolder = binding.rvTaches.findViewHolderForAdapterPosition(position) as? TacheAdapter.TacheViewHolder
                        val currentTache = adapter.currentList[position]
                        viewHolder?.updateChronoDisplay(currentTache.dureeReelle + seconds)
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.fabAjouter.setOnClickListener {
            startActivity(Intent(requireContext(), AjoutTacheActivity::class.java))
        }
    }

    private fun showConfetti() {
        binding.animationConfetti.visibility = View.VISIBLE
        binding.animationConfetti.playAnimation()
        // Hide after animation ends or after a delay
        binding.animationConfetti.postDelayed({
            binding.animationConfetti.visibility = View.GONE
        }, 3000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
