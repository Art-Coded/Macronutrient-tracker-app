package com.example.macrominder.onboarding.signup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GenderAdapter(
    private val genders: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<GenderAdapter.GenderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return GenderViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenderViewHolder, position: Int) {
        val gender = genders[position]
        holder.textView.text = gender
        holder.itemView.setOnClickListener { onItemClick(gender) }
    }

    override fun getItemCount(): Int = genders.size

    class GenderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }
}
