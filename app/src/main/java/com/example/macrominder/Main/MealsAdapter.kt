package com.example.macrominder.Main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog
import com.example.macrominder.R
import com.example.macrominder.Main.MealData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MealsAdapter(
    private val viewModel: DiaryViewModel,
    private val onDeleteClick: (MealData, Int) -> Unit,
    private val onItemClick: (MealData) -> Unit,
    private val currentDate: Calendar
) : RecyclerView.Adapter<MealsAdapter.MealViewHolder>() {

    private val meals = mutableListOf<MealData>()
    private val expandedStates = mutableMapOf<Int, Boolean>()

    fun updateMeals(newMeals: List<MealData>) {
        meals.clear()
        meals.addAll(newMeals)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meal, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]
        holder.bind(meal, position)
    }

    override fun getItemCount(): Int = meals.size

    inner class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mealNameTextView: TextView = itemView.findViewById(R.id.mealNameTextView)
        private val deleteButton: ImageView = itemView.findViewById(R.id.delete)
        private val expandedLayout: LinearLayout = itemView.findViewById(R.id.expandedLayout)
        private val hintTextView: TextView = itemView.findViewById(R.id.hint)
        private val totalNutrientsTextView: TextView = itemView.findViewById(R.id.totalNutrientsTextView)

        fun bind(meal: MealData, position: Int) {
            mealNameTextView.text = meal.mealName

            deleteButton.visibility = if (meals.size > 1) View.VISIBLE else View.GONE

            deleteButton.setOnClickListener {
                startDeleteAnimation(itemView, position, meal)
            }

            val isExpanded = expandedStates[position] ?: false
            expandedLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
            hintTextView.text = if (isExpanded) "Click to collapse" else "Click to view full details"

            itemView.setOnClickListener {
                expandedStates[position] = !isExpanded
                notifyItemChanged(position)
                onItemClick(meal)
            }

            itemView.findViewById<ImageView>(R.id.addIcon).setOnClickListener {
                showAddMealDialog(itemView.context, meal)
            }

            if (isExpanded) {
                displayExpandedMealDetails(meal)
            }

            displayTotalNutrients(meal.totalNutrientData)
        }

        private fun displayTotalNutrients(totalNutrients: Map<String, Float>) {
            if (totalNutrients.isNotEmpty()) {
                totalNutrientsTextView.text = totalNutrients.entries.joinToString("\n") {
                    "${it.key}: ${it.value}g"
                }
            } else {
                totalNutrientsTextView.text = "No nutrient data available"
            }
        }

        private fun displayExpandedMealDetails(meal: MealData) {
            val foodItemsContainer = itemView.findViewById<LinearLayout>(R.id.foodItemsContainer)
            foodItemsContainer.removeAllViews()

            for (item in meal.mealItems) {
                val itemView = LayoutInflater.from(itemView.context).inflate(R.layout.item_food_details, null)

                val foodNameTextView: TextView = itemView.findViewById(R.id.foodNameTextView)
                val foodWeightTextView: TextView = itemView.findViewById(R.id.foodWeightTextView)
                val foodIngredientsTextView: TextView = itemView.findViewById(R.id.foodIngredientsTextView)
                val foodNutrientTextView: TextView = itemView.findViewById(R.id.foodNutrientTextView)

                foodNameTextView.text = item.name
                foodWeightTextView.text = item.weight
                foodIngredientsTextView.text = item.ingredients
                foodNutrientTextView.text = item.nutrientData

                foodItemsContainer.addView(itemView)
            }
        }

        private fun startDeleteAnimation(itemView: View, position: Int, meal: MealData) {
            itemView.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    deleteMealFromFirestore(meal, position)
                    itemView.alpha = 1f
                }
                .start()
        }

        private fun showAddMealDialog(context: Context, meal: MealData) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_food_item, null)

            val mealNameTextView: TextView = dialogView.findViewById(R.id.dialogFoodName)
            mealNameTextView.text = meal.mealName

            val confirmButton: Button = dialogView.findViewById(R.id.dialogConfirmButton)
            val cancelButton: Button = dialogView.findViewById(R.id.dialogCancelButton)

            val dialog = AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

            confirmButton.setOnClickListener {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dateString = dateFormat.format(currentDate.time)

                viewModel.accumulateNutritionFacts(meal.totalNutrientData, dateString)

                Toast.makeText(context, "${meal.mealName}'s nutrients are now added to your daily nutrient intake", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            cancelButton.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun deleteMealFromFirestore(meal: MealData, position: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()

        val mealName = meal.mealName

        firestore.collection("users")
            .document(userId)
            .collection("meals")
            .whereEqualTo("mealName", mealName)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                }

                meals.removeAt(position)
                notifyItemRemoved(position)

                if (meals.size == 1) {
                    notifyItemChanged(0)
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    fun removeMealWithAnimation(position: Int) {
        meals.removeAt(position)
        notifyItemRemoved(position)
    }
}