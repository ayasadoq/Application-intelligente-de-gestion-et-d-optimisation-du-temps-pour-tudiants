package com.etudiant.timemaster.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.etudiant.timemaster.databinding.FragmentStatsBinding
import com.etudiant.timemaster.ui.viewmodel.TacheViewModel
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TacheViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeStats()
    }

    private fun observeStats() {
        viewModel.allTaches.observe(viewLifecycleOwner) { taches ->
            if (taches.isEmpty()) return@observe

            setupPieChart(taches)
            setupBarChart(taches)
        }
    }

    private fun setupPieChart(taches: List<com.etudiant.timemaster.data.entity.Tache>) {
        val prioCounts = taches.groupBy { it.priorite }
        val entries = mutableListOf<PieEntry>()
        
        entries.add(PieEntry(prioCounts[3]?.size?.toFloat() ?: 0f, "Haute"))
        entries.add(PieEntry(prioCounts[2]?.size?.toFloat() ?: 0f, "Moyenne"))
        entries.add(PieEntry(prioCounts[1]?.size?.toFloat() ?: 0f, "Basse"))

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            Color.parseColor("#FF5252"),
            Color.parseColor("#FFAB40"),
            Color.parseColor("#4CAF50")
        )
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 14f

        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.description.isEnabled = false
        binding.pieChart.centerText = "Priorités"
        binding.pieChart.animateY(1000)
        binding.pieChart.invalidate()
    }

    private fun setupBarChart(taches: List<com.etudiant.timemaster.data.entity.Tache>) {
        val last5Taches = taches.takeLast(5)
        val entriesEstime = mutableListOf<BarEntry>()
        val entriesReel = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        last5Taches.forEachIndexed { index, tache ->
            entriesEstime.add(BarEntry(index.toFloat(), tache.dureeEstimee.toFloat()))
            entriesReel.add(BarEntry(index.toFloat(), tache.dureeReelle.toFloat() / 60f))
            labels.add(if (tache.titre.length > 8) tache.titre.substring(0, 8) + ".." else tache.titre)
        }

        val dataSetEstime = BarDataSet(entriesEstime, "Estimé")
        dataSetEstime.color = Color.parseColor("#1E3A5F")

        val dataSetReel = BarDataSet(entriesReel, "Réel")
        dataSetReel.color = Color.parseColor("#FFAB40")

        val barData = BarData(dataSetEstime, dataSetReel)
        barData.barWidth = 0.35f

        binding.barChart.data = barData
        binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.barChart.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        binding.barChart.xAxis.setDrawGridLines(false)
        binding.barChart.xAxis.granularity = 1f
        binding.barChart.groupBars(0f, 0.06f, 0.02f)
        binding.barChart.description.isEnabled = false
        binding.barChart.animateY(1000)
        binding.barChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
