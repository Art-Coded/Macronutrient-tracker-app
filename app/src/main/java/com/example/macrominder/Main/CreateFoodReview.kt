package com.example.macrominder.Main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.macrominder.R
import com.example.macrominder.databinding.FoodsCreateFoodReviewBinding
import com.example.macrominder.views.CircularProgressPieChart
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateFoodReview : Fragment() {

    private var _binding: FoodsCreateFoodReviewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NutritionViewModel by activityViewModels()
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val nutrientSuffixes = mapOf(
        "Energy" to "kcal",
        "Protein" to "g",
        "Fat" to "g",
        "Cholesterol" to "mg",
        "Sodium" to "mg",
        "Carbs" to "g",
        "Fiber" to "g",
        "Sugars" to "g",
        "Water" to "g",
        "Alcohol" to "g",
        "Caffeine" to "mg",
        "Potassium" to "mg"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FoodsCreateFoodReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.foodName.observe(viewLifecycleOwner) { name ->
            binding.FoodName.setText(name)
        }

        viewModel.servingSizes.observe(viewLifecycleOwner) { servingSizes ->
            val adapter = ServingSizesAdapter(servingSizes)
            binding.servingSizesRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                this.adapter = adapter
            }
        }

        viewModel.nutritionData.observe(viewLifecycleOwner) { nutritionData ->
            bindNutritionProgressBars(nutritionData)
            updateNutritionTexts(nutritionData)
            updatePieChart(nutritionData)
            checkAndUpdateVisibility(nutritionData)
        }

        binding.cancel.setOnClickListener {
            navigateBackToCreateFood()
        }

        binding.Save.setOnClickListener {
            toggleProgress(true)
            saveFoodToFirestore()
        }
    }

    private fun saveFoodToFirestore() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            toggleProgress(false)
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val foodName = viewModel.foodName.value
        val servingSizes = viewModel.servingSizes.value
        val nutritionData = viewModel.nutritionData.value

        if (foodName.isNullOrEmpty() || servingSizes.isNullOrEmpty() || nutritionData.isNullOrEmpty()) {
            toggleProgress(false)
            Toast.makeText(
                requireContext(),
                "You'll need at least one Serving Size and one Nutrition Facts input to continue.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Convert applicable nutrient values to Float
        val formattedNutritionData = nutritionData.mapValues { (key, value) ->
            if (key in nutrientSuffixes.keys) {
                value.toFloatOrNull()
            } else {
                value
            }
        }

        val foodData = mapOf(
            "foodName" to foodName,
            "servingSizes" to servingSizes,
            "nutritionFacts" to formattedNutritionData
        )

        firestore.collection("users")
            .document(userId)
            .collection("foods")
            .add(foodData)
            .addOnSuccessListener {
                toggleProgress(false)
                Toast.makeText(requireContext(), "Food saved successfully!", Toast.LENGTH_SHORT).show()
                navigateBackToFoods()
            }
            .addOnFailureListener { exception ->
                Log.e("CreateFoodReview", "Error saving food: $exception")
                toggleProgress(false)
                Toast.makeText(requireContext(), "Failed to save food. Try again.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toggleProgress(showProgress: Boolean) {
        if (showProgress) {
            binding.Save.visibility = View.GONE
            binding.progressBar1.visibility = View.VISIBLE
        } else {
            binding.Save.visibility = View.VISIBLE
            binding.progressBar1.visibility = View.GONE
        }
    }

    private fun bindNutritionProgressBars(nutritionData: Map<String, String>) {
        val progressBars = mapOf(
            "Energy" to binding.progressBarEnergy,
            "Protein" to binding.progressBarProtein,
            "Fat" to binding.progressBarFat,
            "Cholesterol" to binding.progressBarCholesterol,
            "Sodium" to binding.progressBarSodium,
            "Carbs" to binding.progressBarCarbs,
            "Fiber" to binding.progressBarFiber,
            "Sugars" to binding.progressBarSugars,
            "Water" to binding.progressBarWater,
            "Alcohol" to binding.progressBarAlcohol,
            "Caffeine" to binding.progressBarCaffeine,
            "Potassium" to binding.progressBarPotassium
        )

        nutritionData.forEach { (key, value) ->
            val progress = value.toIntOrNull() ?: 0
            progressBars[key]?.progress = progress
        }
    }

    private fun updateNutritionTexts(nutritionData: Map<String, String>) {
        val nutritionTextViews = mapOf(
            "Energy" to binding.Energy,
            "Protein" to binding.Protein,
            "Fat" to binding.Fat,
            "Cholesterol" to binding.Cholesterol,
            "Sodium" to binding.Sodium,
            "Carbs" to binding.Carbs,
            "Fiber" to binding.Fiber,
            "Sugars" to binding.Sugars,
            "Water" to binding.Water,
            "Alcohol" to binding.Alcohol,
            "Caffeine" to binding.Caffeine,
            "Potassium" to binding.Potassium
        )

        nutritionData.forEach { (key, value) ->
            val displayValue = "$key - $value ${nutrientSuffixes[key] ?: ""}"
            nutritionTextViews[key]?.text = displayValue
        }

        binding.colorIndicatorsContainer.removeAllViews()
        val chartColors = binding.CircularProgressPieChart.colors
        var colorIndex = 0

        nutritionData.forEach { (key, value) ->
            if (value.isNotBlank()) {
                val customTextView = TextView(requireContext()).apply {
                    text = "$key: $value ${nutrientSuffixes[key] ?: ""}"
                    textSize = 16f
                    setPadding(0, 8, 0, 8)
                    setTextColor(chartColors[colorIndex % chartColors.size])
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    typeface = resources.getFont(R.font.gurmukhibold)
                }
                binding.colorIndicatorsContainer.addView(customTextView)
                colorIndex++
            }
        }
    }

    private fun updatePieChart(nutritionData: Map<String, String>) {
        val nutrientValues = nutritionData.mapValues { it.value.toFloatOrNull() ?: 0f }
        binding.CircularProgressPieChart.setData(nutrientValues)
    }

    private fun checkAndUpdateVisibility(nutritionData: Map<String, String>) {
        val anyNutrientEntered = nutritionData.values.any { it.isNotBlank() }

        if (anyNutrientEntered) {
            binding.NutrientsLabel.visibility = View.VISIBLE
            binding.Chart.visibility = View.VISIBLE
        } else {
            binding.NutrientsLabel.visibility = View.GONE
            binding.Chart.visibility = View.GONE
        }
    }

    private fun navigateBackToCreateFood() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CreateFood())
            .addToBackStack(null)
            .commit()
    }

    private fun navigateBackToFoods() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, Foods())
            .addToBackStack(null)
            .commit()
    }
}
