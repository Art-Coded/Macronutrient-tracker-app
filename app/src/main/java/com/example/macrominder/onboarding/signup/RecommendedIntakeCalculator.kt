package com.example.macrominder.onboarding.signup

import java.text.SimpleDateFormat
import java.util.*

data class RecommendedIntake(
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val water: Int,
    val cholesterol: Int,
    val sodium: Int,
    val sugars: Int,
    val fiber: Int,
    val caffeine: Int,
    val potassium: Int
)

class RecommendedIntakeCalculator {

    companion object {
        fun calculate(viewModel: SignupViewModel): RecommendedIntake {
            val weight = viewModel.weight?.let {
                it.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 70.0
            } ?: 70.0

            val activityMultiplier = when (viewModel.activityLevel) {
                0 -> 1.2
                1 -> 1.2
                2 -> 1.375
                3 -> 1.55
                4 -> 1.725
                else -> 1.2
            }

            val goalAdjustment = when (viewModel.weightGoal) {
                0 -> -500
                1 -> -500
                2 -> -450
                3 -> -350
                4 -> -250
                5 -> -150
                6 -> -100
                7 -> 0
                8 -> 150
                9 -> 250
                10 -> 350
                11 -> 450
                12 -> 550
                13 -> 650
                14 -> 750
                15 -> 850
                else -> 0
            }

            val baseCalories = calculateCalories(viewModel.gender, weight, activityMultiplier, goalAdjustment)
            val macronutrients = calculateMacronutrients(viewModel.gender, baseCalories)

            val water = calculateWaterIntake(viewModel.gender, weight)


            val age = calculateAge(viewModel.birthday)


            val ageGenderRecommendations = getRecommendationsForAgeAndGender(age, viewModel.gender)

            return RecommendedIntake(
                calories = baseCalories.toInt(),
                protein = macronutrients["protein"] ?: 0,
                carbs = macronutrients["carbs"] ?: 0,
                fats = macronutrients["fats"] ?: 0,
                water = water,
                cholesterol = ageGenderRecommendations.cholesterol,
                sodium = ageGenderRecommendations.sodium,
                sugars = ageGenderRecommendations.sugars,
                fiber = ageGenderRecommendations.fiber,
                caffeine = ageGenderRecommendations.caffeine,
                potassium = ageGenderRecommendations.potassium
            )
        }

        private fun calculateCalories(gender: String?, weight: Double, activityMultiplier: Double, goalAdjustment: Int): Double {
            val baseBMR = when (gender) {
                "Male" -> (10 * weight) + (6.25 * 170) - (5 * 30) + 5
                "Female", "Pregnant", "Breastfeeding" -> (10 * weight) + (6.25 * 170) - (5 * 30) - 161
                else -> (10 * weight) + (6.25 * 170) - (5 * 30)
            }

            val extraCalories = when (gender) {
                "Pregnant" -> 300
                "Breastfeeding" -> 500
                else -> 0
            }

            return (baseBMR * activityMultiplier) + goalAdjustment + extraCalories
        }

        private fun calculateMacronutrients(gender: String?, calories: Double): Map<String, Int> {
            val proteinPercentage = if (gender == "Pregnant" || gender == "Breastfeeding") 0.25 else 0.20
            val fatPercentage = if (gender == "Pregnant" || gender == "Breastfeeding") 0.35 else 0.30
            val carbPercentage = 1.0 - proteinPercentage - fatPercentage

            val protein = (calories * proteinPercentage / 4).toInt()
            val fats = (calories * fatPercentage / 9).toInt()
            val carbs = (calories * carbPercentage / 4).toInt()

            return mapOf("protein" to protein, "fats" to fats, "carbs" to carbs)
        }

        private fun calculateWaterIntake(gender: String?, weight: Double): Int {
            return when (gender) {
                "Pregnant" -> (weight * 35).toInt()
                "Breastfeeding" -> (weight * 40).toInt()
                else -> (weight * 30).toInt()
            }
        }

        private fun calculateAge(birthday: String?): Int {
            if (birthday.isNullOrEmpty()) return 30

            return try {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val birthDate = dateFormat.parse(birthday) ?: return 30
                val today = Calendar.getInstance()
                val birthCalendar = Calendar.getInstance().apply { time = birthDate }

                var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
                if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                    age--
                }
                age
            } catch (e: Exception) {
                30
            }
        }

        private fun getRecommendationsForAgeAndGender(age: Int, gender: String?): RecommendedIntake {
            return when (gender) {
                "Pregnant" -> RecommendedIntake(0, 0, 0, 0, 0, 300, 2300, 37, 30, 200, 4700)
                "Breastfeeding" -> RecommendedIntake(0, 0, 0, 0, 0, 300, 2300, 37, 30, 300, 4700)
                "Male" -> RecommendedIntake(0, 0, 0, 0, 0, 300, 2300, 37, 30, 400, 4700)
                "Female" -> RecommendedIntake(0, 0, 0, 0, 0, 300, 2300, 37, 30, 200, 4700)
                else -> RecommendedIntake(0, 0, 0, 0, 0, 300, 2300, 37, 30, 200, 4700)
            }
        }
    }
}

