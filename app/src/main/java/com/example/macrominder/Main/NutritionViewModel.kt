package com.example.macrominder.Main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NutritionViewModel : ViewModel() {

    private val _foodName = MutableLiveData<String>()
    val foodName: LiveData<String> get() = _foodName

    private val _servingSizes = MutableLiveData<List<Pair<String, String>>>(emptyList())
    val servingSizes: LiveData<List<Pair<String, String>>> get() = _servingSizes

    private val _nutritionFacts = MutableLiveData<Map<String, NutritionFacts>>(emptyMap())
    val nutritionFacts: LiveData<Map<String, NutritionFacts>> get() = _nutritionFacts

    private val _nutritionData = MutableLiveData<Map<String, String>>(emptyMap())
    val nutritionData: LiveData<Map<String, String>> get() = _nutritionData

    fun setFoodName(name: String) {
        _foodName.value = name
    }

    fun addServingSize(name: String, weight: String) {
        if (name.isBlank() || weight.isBlank()) return

        val updatedServingSizes = _servingSizes.value?.toMutableList() ?: mutableListOf()

        if (updatedServingSizes.any { it.first == name && it.second == weight }) return

        updatedServingSizes.add(Pair(name, weight))
        _servingSizes.value = updatedServingSizes
    }

    fun removeServingSize(name: String, weight: String) {
        val updatedServingSizes = _servingSizes.value?.toMutableList() ?: mutableListOf()
        updatedServingSizes.remove(Pair(name, weight))
        _servingSizes.value = updatedServingSizes
    }

    fun saveNutritionFacts(servingName: String, nutritionFacts: NutritionFacts) {
        if (servingName.isBlank()) return
        val updatedFacts = _nutritionFacts.value?.toMutableMap() ?: mutableMapOf()
        updatedFacts[servingName] = nutritionFacts
        _nutritionFacts.value = updatedFacts
    }

    fun saveNutritionField(fieldName: String, value: String?) {
        val updatedData = _nutritionData.value?.toMutableMap() ?: mutableMapOf()

        if (value.isNullOrBlank()) {
            updatedData.remove(fieldName)
        } else {
            updatedData[fieldName] = value
        }

        _nutritionData.value = updatedData
    }

    fun saveNutritionData(data: Map<String, String>) {
        _nutritionData.value = data.filterValues { it.isNotBlank() }
    }

    fun clearAllData() {
        _foodName.value = ""
        _servingSizes.value = emptyList()
        _nutritionFacts.value = emptyMap()
        _nutritionData.value = emptyMap()
    }
}

