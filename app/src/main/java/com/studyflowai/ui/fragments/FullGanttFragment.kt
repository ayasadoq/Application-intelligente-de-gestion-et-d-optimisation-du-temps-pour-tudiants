package com.studyflowai.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.studyflowai.R
import com.studyflowai.databinding.FragmentFullGanttBinding
import com.studyflowai.viewmodel.TaskViewModel
import java.util.*
import android.util.TypedValue
import android.view.Gravity
import com.google.android.material.card.MaterialCardView
import androidx.appcompat.content.res.AppCompatResources
import android.text.TextUtils
import android.content.res.ColorStateList
import androidx.core.view.setPadding

class FullGanttFragment : Fragment() {

    private var _binding: FragmentFullGanttBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by viewModels()

    private val HOUR_WIDTH_DP = 160f
    private val ROW_HEIGHT_DP = 70f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFullGanttBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        
        viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            val activeTasks = tasks.filter { !it.isCompleted }
                .sortedBy { it.deadline - (it.estimatedTime * 60_000L) }

            if (activeTasks.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.hsvGantt.visibility = View.GONE
                binding.hsvTimeAxis.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.hsvGantt.visibility = View.VISIBLE
                binding.hsvTimeAxis.visibility = View.VISIBLE
                renderGantt(activeTasks)
            }
        }
    }

    private fun renderGantt(tasks: List<com.studyflowai.data.entity.Task>) {
        val context = requireContext()
        val hourWidth = dpToPx(HOUR_WIDTH_DP)
        val rowHeight = dpToPx(ROW_HEIGHT_DP)
        val totalWidth = (hourWidth * 24).toInt()

        binding.timeAxisContainer.removeAllViews()
        binding.ganttContainer.removeAllViews()
        
        // 1. Grid & Labels
        for (h in 0..24) {
            val label = TextView(context).apply {
                text = String.format("%02dh", h)
                textSize = 12f
                setTextColor(Color.WHITE)
                typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
                gravity = Gravity.CENTER_HORIZONTAL
                layoutParams = FrameLayout.LayoutParams(hourWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT).apply { 
                    marginStart = (h * hourWidth - hourWidth/2).toInt() 
                }
            }
            binding.timeAxisContainer.addView(label)

            val line = View(context).apply {
                setBackgroundColor(Color.parseColor("#40FFFFFF")) // Grille plus visible
                layoutParams = FrameLayout.LayoutParams(dpToPx(1f).toInt(), ViewGroup.LayoutParams.MATCH_PARENT).apply {
                    marginStart = (h * hourWidth).toInt()
                }
            }
            binding.ganttContainer.addView(line)
        }

        // 1b. Background contrast
        binding.ganttContainer.setBackgroundColor(Color.parseColor("#1A000000")) 

        // 2. Tasks
        val calendar = Calendar.getInstance().apply { 
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis

        tasks.forEachIndexed { i, task ->
            val startTime = task.deadline - (task.estimatedTime * 60_000L)
            val startH = (startTime - startOfDay).toFloat() / 3600000f
            val durH = task.estimatedTime.toFloat() / 60f

            val card = MaterialCardView(context).apply {
                radius = dpToPx(8f)
                setCardBackgroundColor(getPriorityColor(task.priority))
                strokeColor = Color.WHITE
                strokeWidth = dpToPx(1f).toInt()
                cardElevation = 6f
                
                val txt = TextView(context).apply {
                    text = task.title
                    setTextColor(Color.WHITE)
                    setPadding(dpToPx(8f).toInt(), 0, dpToPx(8f).toInt(), 0)
                    gravity = Gravity.CENTER_VERTICAL
                    textSize = 11f
                    maxLines = 2
                    ellipsize = TextUtils.TruncateAt.END
                    typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.NORMAL)
                }
                addView(txt)

                val lp = FrameLayout.LayoutParams((durH * hourWidth).toInt().coerceAtLeast(dpToPx(80f).toInt()), dpToPx(48f).toInt())
                lp.marginStart = (startH * hourWidth).toInt()
                lp.topMargin = (i * rowHeight).toInt() + dpToPx(20f).toInt()
                layoutParams = lp
            }
            binding.ganttContainer.addView(card)
        }

        // 3. Now Line
        val now = System.currentTimeMillis()
        val nowH = (now - startOfDay).toFloat() / 3600000f
        
        val nowLine = View(context).apply {
            setBackgroundColor(Color.parseColor("#6366F1"))
            layoutParams = FrameLayout.LayoutParams(dpToPx(2f).toInt(), 5000).apply { 
                marginStart = (nowH * hourWidth).toInt() 
            }
        }
        
        val nowIndicator = View(context).apply {
            background = AppCompatResources.getDrawable(context, R.drawable.ic_language)
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#6366F1"))
            layoutParams = FrameLayout.LayoutParams(dpToPx(14f).toInt(), dpToPx(14f).toInt()).apply {
                marginStart = (nowH * hourWidth - dpToPx(6f)).toInt()
                topMargin = dpToPx(4f).toInt()
            }
        }
        
        binding.ganttContainer.addView(nowLine)
        binding.ganttContainer.addView(nowIndicator)

        binding.ganttContainer.layoutParams.width = totalWidth
        binding.timeAxisContainer.layoutParams.width = totalWidth
        binding.ganttContainer.layoutParams.height = (tasks.size * rowHeight).toInt() + dpToPx(100f).toInt()

        // FOCUS ON FIRST TASK OR NOW
        binding.hsvGantt.post {
            val focusX = if (tasks.isNotEmpty()) {
                val tStart = (tasks[0].deadline - (tasks[0].estimatedTime * 60_000L) - startOfDay).toFloat() / 3600000f
                ((tStart - 0.5f) * hourWidth).toInt()
            } else ((nowH - 1) * hourWidth).toInt()
            binding.hsvGantt.scrollTo(focusX.coerceAtLeast(0), 0)
        }
        
        binding.hsvGantt.setOnScrollChangeListener { _, x, _, _, _ -> binding.hsvTimeAxis.scrollTo(x, 0) }
    }

    private fun getPriorityColor(p: Int) = when(p) {
        3 -> Color.parseColor("#90EF4444") // Red Glass
        2 -> Color.parseColor("#90F59E0B") // Amber Glass
        else -> Color.parseColor("#906366F1") // Indigo Glass
    }

    private fun dpToPx(dp: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
}
