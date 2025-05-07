package com.example.macrominder.Main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.macrominder.R
import com.example.macrominder.views.CircularProgressBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DiaryMacro1st : Fragment() {

    private lateinit var progressBarConsumed: CircularProgressBar
    private lateinit var progressBarRemaining: CircularProgressBar
    private lateinit var tvConsumedValue: TextView
    private lateinit var tvRemainingValue: TextView

    private var totalCalories = 0
    private var consumedCalories = 0

    private val diaryViewModel: DiaryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diary_macro1st, container, false)

        progressBarConsumed = view.findViewById(R.id.progressBarConsumed)
        progressBarRemaining = view.findViewById(R.id.progressBarRemaining)
        tvConsumedValue = view.findViewById(R.id.tvConsumedValue)
        tvRemainingValue = view.findViewById(R.id.tvRemainingValue)

        fetchRecommendedCalories()
        observeNutritionData()

        return view
    }

    private fun fetchRecommendedCalories() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    totalCalories = document.getLong("recommendedCalories")?.toInt() ?: 0
                    updateUI()
                }
            }
            .addOnFailureListener {
            }
    }

    private fun observeNutritionData() {
        diaryViewModel.nutritionFacts.observe(viewLifecycleOwner) { nutritionFacts ->
            consumedCalories = nutritionFacts["Energy"]?.toInt() ?: 0

            updateUI()
        }
    }

    private fun updateUI() {
        val remainingCalories = totalCalories - consumedCalories

        tvConsumedValue.text = consumedCalories.toString()
        tvRemainingValue.text = remainingCalories.toString()

        progressBarConsumed.post {
            progressBarConsumed.setMax(totalCalories)
            progressBarConsumed.setProgress(consumedCalories)
            progressBarConsumed.invalidate()
        }

        progressBarRemaining.post {
            progressBarRemaining.setMax(totalCalories)
            progressBarRemaining.setProgress(remainingCalories)
            progressBarRemaining.invalidate()
        }
    }
}
