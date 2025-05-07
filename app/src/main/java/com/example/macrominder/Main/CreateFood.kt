package com.example.macrominder.Main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.macrominder.R
import com.example.macrominder.databinding.FoodsCreateFoodBinding
import com.google.android.material.textfield.TextInputEditText

class CreateFood : Fragment() {

    private var _binding: FoodsCreateFoodBinding? = null
    private val binding get() = _binding!!

    private val nutritionItems = listOf(
        "Serving Sizes",
        "Nutrition Facts"
    )

    private val viewModel: NutritionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FoodsCreateFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.clearAllData()


        val adapter = NutritionAdapter(nutritionItems, viewModel, viewLifecycleOwner)
        binding.nutritionRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
            addItemDecoration(SpacingItemDecoration(50))
        }


        binding.cancel.setOnClickListener {
            navigateBackToFoods()
        }


        binding.Next.setOnClickListener {
            collectInputDataAndNavigate()
        }
    }

    private fun navigateBackToFoods() {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        val foodsFragment = Foods()
        transaction.replace(R.id.fragment_container, foodsFragment)
        transaction.commit()
    }

    private fun collectInputDataAndNavigate() {
        val foodName = binding.FoodName.text.toString().trim()


        if (foodName.isEmpty()) {
            binding.FoodNameLayout.helperText = "Food name is required"
            return
        }


        viewModel.setFoodName(foodName)


        val servingSizes = viewModel.servingSizes.value ?: emptyList()
        servingSizes.forEach { (name, weight) ->
            viewModel.addServingSize(name, weight)
        }


        val nutritionData = mutableMapOf<String, String>()
        listOf(
            R.id.Energy to "Energy",
            R.id.Protein to "Protein",
            R.id.Fat to "Fat",
            R.id.Cholesterol to "Cholesterol",
            R.id.Sodium to "Sodium",
            R.id.Carbs to "Carbs",
            R.id.Fiber to "Fiber",
            R.id.Sugars to "Sugars",
            R.id.Water to "Water",
            R.id.Alcohol to "Alcohol",
            R.id.Caffeine to "Caffeine",
            R.id.Potassium to "Potassium"
        ).forEach { (viewId, fieldName) ->
            val inputField = binding.root.findViewById<TextInputEditText>(viewId)
            nutritionData[fieldName] = inputField?.text?.toString()?.trim() ?: ""
        }

        val nutritionFacts = NutritionFacts(
            energy = nutritionData["Energy"] ?: "",
            protein = nutritionData["Protein"] ?: "",
            fat = nutritionData["Fat"] ?: "",
            cholesterol = nutritionData["Cholesterol"] ?: "",
            sodium = nutritionData["Sodium"] ?: "",
            carbs = nutritionData["Carbs"] ?: "",
            fiber = nutritionData["Fiber"] ?: "",
            sugars = nutritionData["Sugars"] ?: "",
            water = nutritionData["Water"] ?: "",
            alcohol = nutritionData["Alcohol"] ?: "",
            caffeine = nutritionData["Caffeine"] ?: "",
            potassium = nutritionData["Potassium"] ?: ""
        )

        viewModel.saveNutritionFacts(foodName, nutritionFacts)
        viewModel.saveNutritionData(nutritionData)


        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        val createFoodReviewFragment = CreateFoodReview()

        transaction.replace(R.id.fragment_container, createFoodReviewFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
