package com.example.macrominder.Main

import MealItemsViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.macrominder.R
import com.example.macrominder.databinding.FoodsCreateMealBinding
import com.example.macrominder.databinding.DialogSaveChangesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateMeal : Fragment() {

    private var _binding: FoodsCreateMealBinding? = null
    private val binding get() = _binding!!

    private val mealItemsViewModel: MealItemsViewModel by activityViewModels()
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FoodsCreateMealBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mealItemsAdapter = MealItemsAdapter(viewModel = mealItemsViewModel)
        binding.mealRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.mealRecyclerView.adapter = mealItemsAdapter

        mealItemsViewModel.mealItems.observe(viewLifecycleOwner) { mealItems ->
            mealItemsAdapter.updateData(mealItems)
        }

        mealItemsViewModel.totalNutrients.observe(viewLifecycleOwner) { totalNutrients ->
            if (totalNutrients.isNotEmpty()) {
                binding.totalNutrientsTitle.visibility = View.VISIBLE
                binding.totalNutrientsTextView.text = totalNutrients.entries.joinToString("\n") {
                    "${it.key}: ${it.value}"
                }
            } else {
                binding.totalNutrientsTitle.visibility = View.GONE
                binding.totalNutrientsTextView.text = ""
            }
        }

        binding.AddMealItem.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddMeal())
                .addToBackStack(null)
                .commit()
        }

        binding.cancel.setOnClickListener {
            if (!mealItemsViewModel.mealItems.value.isNullOrEmpty()) {
                showSaveChangesDialog()
            } else {
                resetMealData()
                navigateToFoods()
            }
        }

        binding.Save.setOnClickListener {
            val mealName = binding.textInputMealName.editText?.text?.toString()?.trim().orEmpty()
            val mealItems = mealItemsViewModel.mealItems.value.orEmpty()
            val totalNutrients = mealItemsViewModel.totalNutrients.value.orEmpty()

            when {
                mealName.isEmpty() && mealItems.isEmpty() -> {
                    binding.textInputMealName.error = "Please enter a meal name."
                    Toast.makeText(
                        requireContext(),
                        "Please enter your meal name and add a meal item.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                mealName.isEmpty() -> {
                    binding.textInputMealName.error = "Please enter a meal name."
                    Toast.makeText(
                        requireContext(),
                        "Please enter your meal name before continuing.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                mealItems.isEmpty() -> {
                    Toast.makeText(
                        requireContext(),
                        "Please add a meal item first.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    // Convert totalNutrients map from String to Float (remove units)
                    val totalNutrientsNumeric = totalNutrients.mapValues { entry ->
                        entry.value.split(" ")[0].toFloatOrNull() ?: 0f
                    }
                    saveMealDataToFirestore(mealName, mealItems, totalNutrientsNumeric)
                }
            }
        }
    }

    private fun saveMealDataToFirestore(
        mealName: String,
        mealItems: List<FoodItem>,
        totalNutrients: Map<String, Float>
    ) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show()
            return
        }

        val sanitizedMealItems = mealItems.map { foodItem ->
            val sanitizedNutrients = foodItem.nutrientData.split("\n").filter { nutrient ->
                val parts = nutrient.split(" - ")
                parts.size == 2 && parts[1].trim() != "No Data"
            }.joinToString("\n")

            foodItem.copy(nutrientData = sanitizedNutrients)
        }

        val mealData = mapOf(
            "mealName" to mealName,
            "mealItems" to sanitizedMealItems.map { foodItem ->
                mapOf(
                    "id" to foodItem.id,
                    "name" to foodItem.name,
                    "nutrientData" to foodItem.nutrientData,
                    "publishedDate" to foodItem.publishedDate,
                    "weight" to foodItem.weight,
                    "ingredients" to foodItem.ingredients
                )
            },
            "totalNutrients" to totalNutrients
        )

        firestore.collection("users")
            .document(userId)
            .collection("meals")
            .add(mealData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Meal saved successfully!", Toast.LENGTH_SHORT).show()
                resetMealData()
                navigateToFoods()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error saving meal: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showSaveChangesDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_save_changes, null)
        val dialogBinding = DialogSaveChangesBinding.bind(dialogView)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogBinding.discardButton.setOnClickListener {
            resetMealData()
            navigateToFoods()
            dialog.dismiss()
        }

        dialogBinding.saveButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun resetMealData() {
        mealItemsViewModel.clearMealItems()
        binding.textInputMealName.editText?.text?.clear()
        binding.textInputMealName.error = null
    }

    private fun navigateToFoods() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, Foods())
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
