package com.example.macrominder.Main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.macrominder.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DiaryMacro2nd : Fragment() {

    private lateinit var tvEnergy: TextView
    private lateinit var progressBarEnergy: ProgressBar
    private lateinit var tvProtein: TextView
    private lateinit var progressBarProtein: ProgressBar
    private lateinit var tvCarbs: TextView
    private lateinit var progressBarCarbs: ProgressBar
    private lateinit var tvFat: TextView
    private lateinit var progressBarFat: ProgressBar

    private val diaryViewModel: DiaryViewModel by activityViewModels()

    private var recommendedCalories = 0
    private var recommendedProtein = 0
    private var recommendedCarbs = 0
    private var recommendedFats = 0

    private var consumedEnergy = 0
    private var consumedProtein = 0
    private var consumedCarbs = 0
    private var consumedFat = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diary_macro2nd, container, false)

        tvEnergy = view.findViewById(R.id.tvEnergy)
        progressBarEnergy = view.findViewById(R.id.progressBarEnergy)
        tvProtein = view.findViewById(R.id.tvProtein)
        progressBarProtein = view.findViewById(R.id.progressBarProtein)
        tvCarbs = view.findViewById(R.id.tvCarbs)
        progressBarCarbs = view.findViewById(R.id.progressBarCarbs)
        tvFat = view.findViewById(R.id.tvFat)
        progressBarFat = view.findViewById(R.id.progressBarFat)

        fetchRecommendedIntake()

        observeNutritionData()

        return view
    }

    private fun fetchRecommendedIntake() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    recommendedCalories = document.getLong("recommendedCalories")?.toInt() ?: 0
                    recommendedProtein = document.getLong("recommendedProtein")?.toInt() ?: 0
                    recommendedCarbs = document.getLong("recommendedCarbs")?.toInt() ?: 0
                    recommendedFats = document.getLong("recommendedFats")?.toInt() ?: 0

                    updateUI()
                }
            }
            .addOnFailureListener { e ->
                Log.e("DiaryMacro2nd", "Error fetching recommended intake: ${e.message}")
            }
    }

    private fun observeNutritionData() {
        diaryViewModel.nutritionFacts.observe(viewLifecycleOwner) { nutritionFacts ->
            consumedEnergy = nutritionFacts["Energy"]?.toInt() ?: 0
            consumedProtein = nutritionFacts["Protein"]?.toInt() ?: 0
            consumedCarbs = nutritionFacts["Carbohydrates"]?.toInt() ?: 0
            consumedFat = nutritionFacts["Fat"]?.toInt() ?: 0

            updateUI()
        }
    }

    private fun updateUI() {
        tvEnergy.text = "Energy - $consumedEnergy / $recommendedCalories kcal"
        progressBarEnergy.max = recommendedCalories
        progressBarEnergy.progress = consumedEnergy

        tvProtein.text = "Protein - $consumedProtein / $recommendedProtein g"
        progressBarProtein.max = recommendedProtein
        progressBarProtein.progress = consumedProtein

        tvCarbs.text = "Net Carbs - $consumedCarbs / $recommendedCarbs g"
        progressBarCarbs.max = recommendedCarbs
        progressBarCarbs.progress = consumedCarbs

        tvFat.text = "Fat - $consumedFat / $recommendedFats g"
        progressBarFat.max = recommendedFats
        progressBarFat.progress = consumedFat
    }
}
