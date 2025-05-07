package com.example.macrominder.Main

import MealItemsViewModel
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.macrominder.R


class FoodListAdapter(
    private var foodList: List<FoodItem>,
    private val mealItemsViewModel: MealItemsViewModel
) : RecyclerView.Adapter<FoodListAdapter.FoodViewHolder>() {

    class FoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodId: TextView = view.findViewById(R.id.foodIdTextView)
        val foodName: TextView = view.findViewById(R.id.foodNameTextView)
        val foodNutrientTextView: TextView = view.findViewById(R.id.foodNutrientTextView)
        val foodNutrientDetailsTextView: TextView = view.findViewById(R.id.foodNutrientDetailsTextView)
        val foodPublishedDate: TextView = view.findViewById(R.id.foodPublishedDateTextView)
        val foodWeight: TextView = view.findViewById(R.id.foodWeightTextView)
        val foodIngredients: TextView = view.findViewById(R.id.foodIngredientsTextView)
        val foodIngredientsDetails: TextView = view.findViewById(R.id.foodIngredientsDetailsTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.food_item_layout, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val foodItem = foodList[position]

        holder.foodId.text = foodItem.id
        holder.foodName.text = foodItem.name
        holder.foodNutrientTextView.text = "Nutrient Data:"
        holder.foodNutrientDetailsTextView.text = foodItem.nutrientData
        holder.foodPublishedDate.text = foodItem.publishedDate
        holder.foodWeight.text = foodItem.weight
        holder.foodIngredients.text = "Ingredients:"
        holder.foodIngredientsDetails.text = foodItem.ingredients

        holder.itemView.setOnClickListener {
            showAddFoodDialog(holder.itemView.context, foodItem)
        }
    }

    override fun getItemCount(): Int = foodList.size

    fun updateData(newFoodList: List<FoodItem>) {
        foodList = newFoodList
        notifyDataSetChanged()
    }

    private fun showAddFoodDialog(context: Context, foodItem: FoodItem) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_food_item, null)

        val dialogTitle: TextView = dialogView.findViewById(R.id.dialogTitle)
        val dialogMessage1: TextView = dialogView.findViewById(R.id.dialogMessage1)
        val dialogFoodName: TextView = dialogView.findViewById(R.id.dialogFoodName)
        val dialogMessage2: TextView = dialogView.findViewById(R.id.dialogMessage2)
        val dialogCancelButton: Button = dialogView.findViewById(R.id.dialogCancelButton)
        val dialogConfirmButton: Button = dialogView.findViewById(R.id.dialogConfirmButton)

        dialogTitle.text = "Add Food Item"
        dialogMessage1.text = "Are you sure you want to add"
        dialogFoodName.text = foodItem.name
        dialogMessage2.text = "to your Meal Items?"

        val alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        dialogCancelButton.setOnClickListener {
            alertDialog.dismiss()
        }

        dialogConfirmButton.setOnClickListener {
            mealItemsViewModel.addMealItem(foodItem)
            Toast.makeText(context, "${foodItem.name} added successfully!", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
}
