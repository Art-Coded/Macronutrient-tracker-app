package com.example.macrominder.Main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

class DiaryViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    private val _nutritionFacts = MutableLiveData<Map<String, Float>>(emptyMap())
    val nutritionFacts: LiveData<Map<String, Float>> = _nutritionFacts

    fun loadNutritionFactsForDate(date: String) {
        userId?.let { uid ->
            firestore.collection("users").document(uid)
                .collection("dates").document(date)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nutritionData = document.get("nutrition") as? Map<String, String>
                        val facts = nutritionData?.mapValues { (_, value) ->
                            value.split(" / ")[0].toFloatOrNull() ?: 0f
                        } ?: emptyMap()
                        _nutritionFacts.value = facts
                    } else {
                        _nutritionFacts.value = emptyMap()
                    }
                }
                .addOnFailureListener {
                    _nutritionFacts.value = emptyMap()
                }
        }
    }

    fun accumulateNutritionFacts(newNutritionFacts: Map<String, Float>, date: String) {
        val currentFacts = _nutritionFacts.value ?: emptyMap()
        val updatedFacts = currentFacts.toMutableMap()

        newNutritionFacts.forEach { (key, value) ->
            updatedFacts[key] = updatedFacts.getOrDefault(key, 0f) + value
        }

        _nutritionFacts.value = updatedFacts
        saveNutritionFactsToFirestore(updatedFacts, date)
    }

    private fun saveNutritionFactsToFirestore(facts: Map<String, Float>, date: String) {
        userId?.let { uid ->
            val nutritionValues = facts.mapValues { (key, value) ->
                "$value"
            }

            val dateData = hashMapOf(
                "timestamp" to System.currentTimeMillis(),
                "nutrition" to nutritionValues
            )

            firestore.collection("users").document(uid)
                .collection("dates").document(date)
                .set(dateData, SetOptions.merge())
        }
    }

    fun resetNutritionFacts() {
        _nutritionFacts.value = emptyMap()
    }
}