package com.example.macrominder.Main

import MealItemsViewModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.macrominder.R

class MealItemsAdapter(
    mealItems: List<FoodItem> = listOf(),
    private val viewModel: MealItemsViewModel
) : RecyclerView.Adapter<MealItemsAdapter.MealItemViewHolder>() {

    private var mutableMealItems = mealItems.toMutableList()

    inner class MealItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val foodPublishedDateTextView: TextView = itemView.findViewById(R.id.foodPublishedDateTextView)
        private val foodNameTextView: TextView = itemView.findViewById(R.id.foodNameTextView)
        private val foodNutrientTextView: TextView = itemView.findViewById(R.id.foodNutrientTextView)
        private val foodNutrientDetailsTextView: TextView = itemView.findViewById(R.id.foodNutrientDetailsTextView)
        private val foodWeightTextView: TextView = itemView.findViewById(R.id.foodWeightTextView)
        private val foodIngredientsTextView: TextView = itemView.findViewById(R.id.foodIngredientsTextView)
        private val foodIngredientsDetailsTextView: TextView = itemView.findViewById(R.id.foodIngredientsDetailsTextView)
        private val deleteButton: ImageView = itemView.findViewById(R.id.delete)

        fun bind(foodItem: FoodItem) {
            foodPublishedDateTextView.text = foodItem.publishedDate ?: "N/A"
            foodNameTextView.text = foodItem.name
            foodWeightTextView.text = foodItem.weight
            foodIngredientsTextView.text = "Ingredients:"
            foodIngredientsDetailsTextView.text = foodItem.ingredients

            val filteredNutrientData = foodItem.nutrientData
                .split("\n")
                .filterNot { it.contains("No Data", ignoreCase = true) }
                .joinToString("\n")

            if (filteredNutrientData.isNotBlank()) {
                foodNutrientTextView.text = "Nutrient Data:"
                foodNutrientDetailsTextView.text = filteredNutrientData
                foodNutrientTextView.visibility = View.VISIBLE
                foodNutrientDetailsTextView.visibility = View.VISIBLE
            } else {
                foodNutrientTextView.visibility = View.GONE
                foodNutrientDetailsTextView.visibility = View.GONE
            }

            deleteButton.setOnClickListener {
                startDeleteAnimation(itemView, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.meal_item, parent, false)
        return MealItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealItemViewHolder, position: Int) {
        holder.bind(mutableMealItems[position])
    }

    override fun getItemCount(): Int = mutableMealItems.size

    fun updateData(newMealItems: List<FoodItem>) {
        mutableMealItems.clear()
        mutableMealItems.addAll(newMealItems)
        notifyDataSetChanged()
    }

    private fun startDeleteAnimation(itemView: View, position: Int) {
        itemView.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                removeItem(position)
                itemView.alpha = 1f
            }
            .start()
    }

    private fun removeItem(position: Int) {
        val itemToRemove = mutableMealItems[position]
        viewModel.removeMealItem(itemToRemove)
        mutableMealItems.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, mutableMealItems.size)
    }
}

