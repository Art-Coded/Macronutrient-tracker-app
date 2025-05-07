package com.example.macrominder.Main


data class MealData(
    val mealName: String = "",
    val mealItems: List<MealItem> = emptyList(),
    val totalNutrientData: Map<String, Float> = emptyMap()
)


data class MealItem(
    val name: String = "",
    val nutrientData: String = "",
    val publishedDate: String? = null,
    val weight: String = "",
    val ingredients: String = ""
)
