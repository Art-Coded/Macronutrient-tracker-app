package com.example.macrominder.Main

import MealItemsViewModel
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.macrominder.R
import com.example.macrominder.databinding.FragmentAddMealBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddMeal : Fragment() {

    private lateinit var binding: FragmentAddMealBinding
    private lateinit var foodListAdapter: FoodListAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var foodRecyclerView: RecyclerView
    private lateinit var drawerMenu: LinearLayout

    private val apiKey = "xSFN6Lwq7BbwVsbOISn29HY3LpOwusGkkNCcZoN9"
    private var allFoodItems: List<FoodItem> = emptyList()
    private var currentPage = 1
    private var currentPageItems: List<FoodItem> = emptyList()

    private val mealItemsViewModel: MealItemsViewModel by activityViewModels()

    private val desiredNutrients = listOf(
        "Energy", "Protein", "Fat", "Cholesterol", "Sodium", "Carbs", "Fiber", "Sugars",
        "Water", "Alcohol", "Caffeine", "Potassium"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddMealBinding.inflate(inflater, container, false)


        progressBar = binding.progressBar
        foodRecyclerView = binding.foodRecyclerView
        drawerMenu = binding.drawerMenu

        foodRecyclerView.layoutManager = LinearLayoutManager(context)
        foodListAdapter = FoodListAdapter(emptyList(), mealItemsViewModel)
        foodRecyclerView.adapter = foodListAdapter


        foodRecyclerView.setPadding(0, 16, 0, 300)
        foodRecyclerView.clipToPadding = false
        foodRecyclerView.addItemDecoration(SpacingItemDecoration(50))


        binding.cancel.setOnClickListener {
            navigateBackToCreateMeal()
        }


        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) searchFoods(s.toString()) else fetchFoodData(currentPage)
            }
            override fun afterTextChanged(s: Editable?) {}
        })


        setupSortingOptions()


        fetchFoodData(currentPage)

        return binding.root
    }

    private fun setupSortingOptions() {
        val sortCheckBoxes = mapOf(
            R.id.checkbox_sort_a_to_z to "A_TO_Z",
            R.id.checkbox_sort_z_to_a to "Z_TO_A",
            R.id.checkbox_sort_most_recent to "MOST_RECENT",
            R.id.checkbox_sort_best_match to "BEST_MATCH"
        )

        sortCheckBoxes.forEach { (checkBoxId, sortType) ->
            view?.findViewById<CheckBox>(checkBoxId)?.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    sortCheckBoxes.keys
                        .filter { it != checkBoxId }
                        .forEach { view?.findViewById<CheckBox>(it)?.isChecked = false }
                    sortData(sortType)
                }
            }
        }
    }

    private fun fetchFoodData(page: Int) {
        progressBar.visibility = View.VISIBLE
        RetrofitInstance.api.searchFoods(apiKey, "all", page, null).enqueue(object : Callback<USDAResponse> {
            override fun onResponse(call: Call<USDAResponse>, response: Response<USDAResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val foodItems = response.body()?.foods?.map { food ->
                        FoodItem(
                            id = food.fdcId.toString(),
                            name = food.description,
                            nutrientData = formatNutrientData(food.foodNutrients),
                            publishedDate = food.publishedDate ?: "Unknown Date",
                            weight = food.packageWeight ?: "Unknown Weight",
                            ingredients = food.ingredients ?: "Unknown Ingredients"
                        )
                    } ?: emptyList()

                    allFoodItems = foodItems
                    currentPageItems = getPageItems(currentPage)
                    foodListAdapter.updateData(currentPageItems)
                } else {
                    showError(response.code().toString())
                }
            }

            override fun onFailure(call: Call<USDAResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                showError(t.message)
            }
        })
    }

    private fun formatNutrientData(nutrients: List<FoodNutrient>): String {
        val nutrientMap = desiredNutrients.associateWith { "No Data" }.toMutableMap()
        nutrients.filter { desiredNutrients.contains(it.nutrientName) }
            .forEach { nutrient ->
                nutrientMap[nutrient.nutrientName] = "${nutrient.value} ${nutrient.unitName}"
            }
        return nutrientMap.entries.joinToString("\n") { "${it.key} - ${it.value}" }
    }

    private fun searchFoods(query: String) {
        progressBar.visibility = View.VISIBLE
        RetrofitInstance.api.searchFoods(apiKey, query, 1, null).enqueue(object : Callback<USDAResponse> {
            override fun onResponse(call: Call<USDAResponse>, response: Response<USDAResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val foodItems = response.body()?.foods?.map { food ->
                        FoodItem(
                            id = food.fdcId.toString(),
                            name = food.description,
                            nutrientData = formatNutrientData(food.foodNutrients),
                            publishedDate = food.publishedDate ?: "Unknown Date",
                            weight = food.packageWeight ?: "Unknown Weight",
                            ingredients = food.ingredients ?: "Unknown Ingredients"
                        )
                    } ?: emptyList()

                    allFoodItems = foodItems
                    currentPageItems = getPageItems(currentPage)
                    foodListAdapter.updateData(currentPageItems)
                } else {
                    showError(response.code().toString())
                }
            }

            override fun onFailure(call: Call<USDAResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                showError(t.message)
            }
        })
    }

    private fun getPageItems(page: Int): List<FoodItem> {
        val startIndex = (page - 1) * 25
        val endIndex = minOf(startIndex + 25, allFoodItems.size)
        return allFoodItems.subList(startIndex, endIndex)
    }

    private fun sortData(sortType: String) {
        val sortedList = when (sortType) {
            "A_TO_Z" -> allFoodItems.sortedBy { it.name }
            "Z_TO_A" -> allFoodItems.sortedByDescending { it.name }
            "MOST_RECENT" -> allFoodItems.sortedByDescending { it.publishedDate }
            else -> allFoodItems
        }
        allFoodItems = sortedList
        currentPageItems = getPageItems(currentPage)
        foodListAdapter.updateData(currentPageItems)
    }

    private fun showError(message: String?) {
        Toast.makeText(context, "Error: $message", Toast.LENGTH_SHORT).show()
    }

    private fun navigateBackToCreateMeal() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CreateMeal())
            .addToBackStack(null)
            .commit()
    }
}
