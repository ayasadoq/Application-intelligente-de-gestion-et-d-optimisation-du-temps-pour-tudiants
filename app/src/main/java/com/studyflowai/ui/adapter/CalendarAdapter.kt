package com.studyflowai.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.studyflowai.R
import java.text.SimpleDateFormat
import java.util.*

data class CalendarDay(val date: Date, var isSelected: Boolean = false)

class CalendarAdapter(private val onDateSelected: (Date) -> Unit) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private val days = mutableListOf<CalendarDay>()
    private val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    private val numberFormat = SimpleDateFormat("dd", Locale.getDefault())

    init {
        val calendar = Calendar.getInstance()
        // Commencer au début de la semaine
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        for (i in 0..6) {
            days.add(CalendarDay(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val day = days[position]
        holder.tvDayName.text = dayFormat.format(day.date).uppercase()
        holder.tvDayNumber.text = numberFormat.format(day.date)

        holder.itemView.setOnClickListener {
            days.forEach { it.isSelected = false }
            day.isSelected = true
            notifyDataSetChanged()
            onDateSelected(day.date)
        }
    }

    override fun getItemCount(): Int = days.size

    class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDayName: TextView = view.findViewById(R.id.tvDayName)
        val tvDayNumber: TextView = view.findViewById(R.id.tvDayNumber)
    }
}
