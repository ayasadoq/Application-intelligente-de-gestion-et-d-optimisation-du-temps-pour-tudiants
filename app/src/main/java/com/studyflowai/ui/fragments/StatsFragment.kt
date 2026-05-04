package com.studyflowai.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.studyflowai.databinding.FragmentStatsBinding
import com.studyflowai.ui.adapter.AchievementAdapter
import com.studyflowai.viewmodel.TaskViewModel
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.studyflowai.data.entity.Task

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by viewModels()

    @ColorInt
    private fun getColorFromAttr(@AttrRes attrColor: Int): Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(attrColor, typedValue, true)
        return typedValue.data
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAchievementList()
        observeStats()
    }

    private fun setupAchievementList() {
        val adapter = AchievementAdapter()
        binding.rvAchievements.apply {
            this.adapter = adapter
            layoutManager = GridLayoutManager(requireContext(), 3)
        }
        
        viewModel.allAchievements.observe(viewLifecycleOwner) { achievements ->
            adapter.setAchievements(achievements)
        }
    }

    private fun observeStats() {
        viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            if (tasks.isEmpty()) return@observe

            setupPieChart(tasks)
            setupBarChart(tasks)
        }
    }

    private fun setupPieChart(tasks: List<Task>) {
        val prioCounts = tasks.groupBy { it.priority }
        val entries = mutableListOf<PieEntry>()
        
        entries.add(PieEntry(prioCounts[3]?.size?.toFloat() ?: 0f, "Haute"))
        entries.add(PieEntry(prioCounts[2]?.size?.toFloat() ?: 0f, "Moyenne"))
        entries.add(PieEntry(prioCounts[1]?.size?.toFloat() ?: 0f, "Basse"))

        val colorError = getColorFromAttr(com.google.android.material.R.attr.colorError)
        val colorWarning = getColorFromAttr(com.studyflowai.R.attr.colorWarning)
        val colorSuccess = getColorFromAttr(com.studyflowai.R.attr.colorSuccess)
        val textColor = getColorFromAttr(com.google.android.material.R.attr.colorOnSurface)

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(colorError, colorWarning, colorSuccess)
        dataSet.valueTextColor = textColor
        dataSet.valueTextSize = 14f
        dataSet.sliceSpace = 3f

        binding.pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            centerText = "Priorités"
            setCenterTextColor(textColor)
            setHoleColor(Color.TRANSPARENT)
            legend.textColor = textColor
            animateY(1000)
            invalidate()
        }
    }

    private fun setupBarChart(tasks: List<Task>) {
        // Prendre les 5 dernières tâches complétées pour un graphique propre
        val relevantTasks = tasks.filter { it.isCompleted || it.realTime > 0 }.takeLast(5)
        
        val entriesEstime = mutableListOf<BarEntry>()
        val entriesReel = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        relevantTasks.forEachIndexed { index, task ->
            entriesEstime.add(BarEntry(index.toFloat(), task.estimatedTime.toFloat()))
            entriesReel.add(BarEntry(index.toFloat(), task.realTime.toFloat()))
            labels.add(if (task.title.length > 8) task.title.substring(0, 8) + ".." else task.title)
        }

        val textColor = getColorFromAttr(com.google.android.material.R.attr.colorOnSurface)
        val colorPrimary = getColorFromAttr(com.google.android.material.R.attr.colorPrimary)
        val colorWarning = getColorFromAttr(com.studyflowai.R.attr.colorWarning)

        val dataSetEstime = BarDataSet(entriesEstime, "Temps Estimé (min)")
        dataSetEstime.color = colorPrimary
        dataSetEstime.valueTextColor = textColor

        val dataSetReel = BarDataSet(entriesReel, "Temps Réel (min)")
        dataSetReel.color = colorWarning
        dataSetReel.valueTextColor = textColor

        val barData = BarData(dataSetEstime, dataSetReel)
        barData.barWidth = 0.35f 
        barData.setValueTextSize(10f)

        binding.barChart.apply {
            data = barData
            
            // Configuration de l'axe X pour le groupement
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.textColor = textColor
            xAxis.granularity = 1f
            xAxis.setCenterAxisLabels(true) 

            // Groupement des barres : (barWidth + barSpace) * 2 + groupSpace = 1.0f
            // (0.35 + 0.05) * 2 + 0.2 = 1.0f
            val groupSpace = 0.2f
            val barSpace = 0.05f
            
            axisLeft.textColor = textColor
            axisLeft.axisMinimum = 0f
            axisLeft.setDrawGridLines(true)
            axisRight.isEnabled = false
            
            xAxis.axisMinimum = 0f
            xAxis.axisMaximum = 0f + barData.getGroupWidth(groupSpace, barSpace) * relevantTasks.size
            
            groupBars(0f, groupSpace, barSpace)
            
            legend.textColor = textColor
            legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
            legend.setDrawInside(false)
            
            description.isEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
            setDoubleTapToZoomEnabled(false)

            animateY(1000)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
