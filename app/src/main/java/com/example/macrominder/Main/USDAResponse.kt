package com.example.macrominder.Main

data class USDAResponse(
    val foods: List<Food>
)

data class Food(
    val fdcId: Int,
    val description: String,
    val foodNutrients: List<FoodNutrient>,
    val publishedDate: String?,
    val packageWeight: String?,
    val ingredients: String?
)

data class FoodNutrient(
    val nutrientName: String,
    val value: Float,
    val unitName: String
)
