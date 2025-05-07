package com.example.macrominder.Main

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.macrominder.R
import com.example.macrominder.databinding.ItemCustomFoodBinding
import java.text.SimpleDateFormat
import java.util.*

class FoodAdapter(
    private val context: Context,
    private val onItemClick: (Map<String, Any>) -> Unit,
    private val diaryViewModel: DiaryViewModel,
    private val currentDate: Calendar
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    private val foodList = mutableListOf<Map<String, Any>>()

    inner class FoodViewHolder(private val binding: ItemCustomFoodBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(food: Map<String, Any>, position: Int) {
            binding.foodName.text = food["foodName"] as? String ?: "Unnamed Food"
            val servingSizes = food["formattedServingSizes"] as? String ?: "No serving sizes available"
            val nutritionFacts = food["formattedNutritionFacts"] as? String ?: "No nutrition facts available"

            binding.servingSizes.text = "Serving Sizes:\n$servingSizes"
            binding.nutritionFacts.text = "Total Nutrition Facts:\n$nutritionFacts"

            binding.root.setOnClickListener { onItemClick(food) }

            binding.addIcon.setOnClickListener { showAddFoodDialog(food) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemCustomFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(foodList[position], position)
    }

    override fun getItemCount(): Int = foodList.size

    fun submitList(foods: List<Map<String, Any>>) {
        foodList.clear()
        foodList.addAll(foods)
        notifyDataSetChanged()
    }

    private fun showAddFoodDialog(food: Map<String, Any>) {
        val foodName = food["foodName"] as? String ?: "Unknown Food"
        val rawNutritionFacts = food["rawNutritionFacts"] as? Map<String, Float> ?: emptyMap()

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_food_item, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        val dialogFoodName: TextView = dialogView.findViewById(R.id.dialogFoodName)
        dialogFoodName.text = foodName

        val confirmButton: TextView = dialogView.findViewById(R.id.dialogConfirmButton)
        val cancelButton: TextView = dialogView.findViewById(R.id.dialogCancelButton)

        confirmButton.setOnClickListener {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateString = dateFormat.format(currentDate.time)

            diaryViewModel.accumulateNutritionFacts(rawNutritionFacts, dateString)

            Toast.makeText(context, "$foodName's nutrients are now added to your daily nutrient intake", Toast.LENGTH_SHORT).show()

            dialog.dismiss()
        }

        cancelButton.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}