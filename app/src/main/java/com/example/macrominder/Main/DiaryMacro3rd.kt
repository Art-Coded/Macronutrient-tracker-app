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

class DiaryMacro3rd : Fragment() {

    private lateinit var tvWater: TextView
    private lateinit var progressBarWater: ProgressBar
    private lateinit var tvCholesterol: TextView
    private lateinit var progressBarCholesterol: ProgressBar
    private lateinit var tvSodium: TextView
    private lateinit var progressBarSodium: ProgressBar
    private lateinit var tvSugars: TextView
    private lateinit var progressBarSugars: ProgressBar
    private lateinit var tvFiber: TextView
    private lateinit var progressBarFiber: ProgressBar
    private lateinit var tvCaffeine: TextView
    private lateinit var progressBarCaffeine: ProgressBar
    private lateinit var tvPotassium: TextView
    private lateinit var progressBarPotassium: ProgressBar

    private val diaryViewModel: DiaryViewModel by activityViewModels()

    private var recommendedWater = 0
    private var recommendedCholesterol = 0
    private var recommendedSodium = 0
    private var recommendedSugars = 0
    private var recommendedFiber = 0
    private var recommendedCaffeine = 0
    private var recommendedPotassium = 0

    private var consumedWater = 0
    private var consumedCholesterol = 0
    private var consumedSodium = 0
    private var consumedSugars = 0
    private var consumedFiber = 0
    private var consumedCaffeine = 0
    private var consumedPotassium = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diary_macro3rd, container, false)

        tvWater = view.findViewById(R.id.tvWater)
        progressBarWater = view.findViewById(R.id.progressBarWater)
        tvCholesterol = view.findViewById(R.id.tvCholesterol)
        progressBarCholesterol = view.findViewById(R.id.progressBarCholesterol)
        tvSodium = view.findViewById(R.id.tvSodium)
        progressBarSodium = view.findViewById(R.id.progressBarSodium)
        tvSugars = view.findViewById(R.id.tvSugars)
        progressBarSugars = view.findViewById(R.id.progressBarSugars)
        tvFiber = view.findViewById(R.id.tvFiber)
        progressBarFiber = view.findViewById(R.id.progressBarFiber)
        tvCaffeine = view.findViewById(R.id.tvCaffeine)
        progressBarCaffeine = view.findViewById(R.id.progressBarCaffeine)
        tvPotassium = view.findViewById(R.id.tvPotassium)
        progressBarPotassium = view.findViewById(R.id.progressBarPotassium)

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
                    recommendedWater = document.getLong("recommendedWater")?.toInt() ?: 0
                    recommendedCholesterol = document.getLong("recommendedCholesterol")?.toInt() ?: 0
                    recommendedSodium = document.getLong("recommendedSodium")?.toInt() ?: 0
                    recommendedSugars = document.getLong("recommendedSugars")?.toInt() ?: 0
                    recommendedFiber = document.getLong("recommendedFiber")?.toInt() ?: 0
                    recommendedCaffeine = document.getLong("recommendedCaffeine")?.toInt() ?: 0
                    recommendedPotassium = document.getLong("recommendedPotassium")?.toInt() ?: 0

                    updateUI()
                }
            }
            .addOnFailureListener { e ->
                Log.e("DiaryMacro3rd", "Error fetching recommended intake: ${e.message}")
            }
    }

    private fun observeNutritionData() {
        diaryViewModel.nutritionFacts.observe(viewLifecycleOwner) { nutritionFacts ->
            consumedWater = nutritionFacts["Water"]?.toInt() ?: 0
            consumedCholesterol = nutritionFacts["Cholesterol"]?.toInt() ?: 0
            consumedSodium = nutritionFacts["Sodium"]?.toInt() ?: 0
            consumedSugars = nutritionFacts["Sugars"]?.toInt() ?: 0
            consumedFiber = nutritionFacts["Fiber"]?.toInt() ?: 0
            consumedCaffeine = nutritionFacts["Caffeine"]?.toInt() ?: 0
            consumedPotassium = nutritionFacts["Potassium"]?.toInt() ?: 0

            updateUI()
        }
    }

    private fun updateUI() {
        tvWater.text = "Water - $consumedWater / $recommendedWater ml"
        progressBarWater.max = recommendedWater
        progressBarWater.progress = consumedWater

        tvCholesterol.text = "Cholesterol - $consumedCholesterol / $recommendedCholesterol mg"
        progressBarCholesterol.max = recommendedCholesterol
        progressBarCholesterol.progress = consumedCholesterol

        tvSodium.text = "Sodium - $consumedSodium / $recommendedSodium mg"
        progressBarSodium.max = recommendedSodium
        progressBarSodium.progress = consumedSodium

        tvSugars.text = "Sugars - $consumedSugars / $recommendedSugars g"
        progressBarSugars.max = recommendedSugars
        progressBarSugars.progress = consumedSugars

        tvFiber.text = "Fiber - $consumedFiber / $recommendedFiber g"
        progressBarFiber.max = recommendedFiber
        progressBarFiber.progress = consumedFiber

        tvCaffeine.text = "Caffeine - $consumedCaffeine / $recommendedCaffeine mg"
        progressBarCaffeine.max = recommendedCaffeine
        progressBarCaffeine.progress = consumedCaffeine

        tvPotassium.text = "Potassium - $consumedPotassium / $recommendedPotassium mg"
        progressBarPotassium.max = recommendedPotassium
        progressBarPotassium.progress = consumedPotassium
    }
}
