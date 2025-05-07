import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.macrominder.Main.FoodItem

class MealItemsViewModel : ViewModel() {

    private val _mealItems = MutableLiveData<List<FoodItem>>(emptyList())
    val mealItems: LiveData<List<FoodItem>> get() = _mealItems

    private val _totalNutrients = MutableLiveData<Map<String, String>>(emptyMap())
    val totalNutrients: LiveData<Map<String, String>> get() = _totalNutrients

    fun addMealItem(foodItem: FoodItem) {
        val currentList = _mealItems.value.orEmpty().toMutableList()
        currentList.add(foodItem)
        _mealItems.value = currentList

        calculateTotalNutrients()
    }

    fun removeMealItem(foodItem: FoodItem) {
        val currentList = _mealItems.value.orEmpty().toMutableList()
        currentList.remove(foodItem)
        _mealItems.value = currentList

        calculateTotalNutrients()
    }

    fun clearMealItems() {
        _mealItems.value = emptyList()
        _totalNutrients.value = emptyMap()
    }

    private fun calculateTotalNutrients() {
        val nutrientTotals = mutableMapOf<String, Double>()

        _mealItems.value?.forEach { foodItem ->
            foodItem.nutrientData.split("\n").forEach { nutrient ->
                val parts = nutrient.split(" - ")
                if (parts.size == 2) {
                    val nutrientName = parts[0].trim()
                    val nutrientValueString = parts[1].trim().replace(Regex("[^0-9.]"), "")
                    val nutrientValue = nutrientValueString.toFloatOrNull()

                    if (nutrientValue != null) {
                        nutrientTotals[nutrientName] = nutrientTotals.getOrDefault(nutrientName, 0.0) + nutrientValue
                    }
                }
            }
        }

        val nutrientWithUnits = nutrientTotals.mapValues { (name, value) ->
            when (name) {
                "Energy" -> "$value kcal"
                "Protein" -> "$value g"
                "Fat" -> "$value g"
                "Cholesterol" -> "$value mg"
                "Sodium" -> "$value mg"
                "Carbs" -> "$value g"
                "Fiber" -> "$value g"
                "Sugars" -> "$value g"
                "Water" -> "$value g"
                "Alcohol" -> "$value g"
                "Caffeine" -> "$value mg"
                "Potassium" -> "$value mg"
                else -> "$value"
            }
        }

        _totalNutrients.value = nutrientWithUnits.filterValues { it.isNotBlank() }
    }
}
