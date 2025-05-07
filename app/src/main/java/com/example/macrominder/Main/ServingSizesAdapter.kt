package com.example.macrominder.Main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.macrominder.R

class ServingSizesAdapter(
    private var servingSizes: List<Pair<String, String>>
) : RecyclerView.Adapter<ServingSizesAdapter.ServingSizeViewHolder>() {

    class ServingSizeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val servingName: TextView = itemView.findViewById(R.id.servingName)
        val servingWeight: TextView = itemView.findViewById(R.id.servingWeight)

        fun bind(servingSize: Pair<String, String>) {
            servingName.text = servingSize.first
            servingWeight.text = "${servingSize.second} g"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServingSizeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.serving_size_item, parent, false)
        return ServingSizeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServingSizeViewHolder, position: Int) {
        holder.bind(servingSizes[position])
    }

    override fun getItemCount(): Int = servingSizes.size

}
