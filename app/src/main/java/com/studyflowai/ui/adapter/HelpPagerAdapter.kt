package com.studyflowai.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.studyflowai.databinding.ItemHelpCardBinding

data class HelpPage(
    val title: String,
    val description: String,
    val iconRes: Int? = null
)

class HelpPagerAdapter(private val pages: List<HelpPage>) : RecyclerView.Adapter<HelpPagerAdapter.HelpViewHolder>() {

    class HelpViewHolder(val binding: ItemHelpCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelpViewHolder {
        val binding = ItemHelpCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HelpViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HelpViewHolder, position: Int) {
        val page = pages[position]
        holder.binding.tvHelpTitle.text = page.title
        holder.binding.tvHelpDescription.text = page.description
        page.iconRes?.let {
            holder.binding.ivHelpIllustration.setImageResource(it)
        }
    }

    override fun getItemCount() = pages.size
}
