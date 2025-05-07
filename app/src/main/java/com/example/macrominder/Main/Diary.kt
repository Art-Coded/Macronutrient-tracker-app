package com.example.macrominder.Main

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.macrominder.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

class Diary : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var dot1: View
    private lateinit var dot2: View
    private lateinit var dot3: View
    private lateinit var dateTextView: TextView
    private lateinit var leftArrow: ImageButton
    private lateinit var rightArrow: ImageButton
    private lateinit var dayTextView: TextView
    private lateinit var waterInput: EditText
    private lateinit var saveWaterButton: Button
    private lateinit var noteEditText: EditText
    private lateinit var secondSaveButton: Button
    private lateinit var refreshButton: ImageView
    private lateinit var tvWater: TextView
    private lateinit var progressBarWater: ProgressBar

    private var currentCalendar: Calendar = Calendar.getInstance()

    private var recommendedWater: Int = 0
    private var recommendedCalories: Int = 0
    private var recommendedAlcohol: Int = 0
    private var recommendedCaffeine: Int = 0
    private var recommendedCarbs: Int = 0
    private var recommendedCholesterol: Int = 0
    private var recommendedFats: Int = 0
    private var recommendedFiber: Int = 0
    private var recommendedPotassium: Int = 0
    private var recommendedProtein: Int = 0
    private var recommendedSugars: Int = 0
    private var recommendedSodium: Int = 0

    private var consumedWater = 0

    private val diaryViewModel: DiaryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diary, container, false)

        tvWater = view.findViewById(R.id.tvWater)
        progressBarWater = view.findViewById(R.id.progressBarWater)

        fetchRecommendedIntake()

        viewPager = view.findViewById(R.id.viewPager)
        dot1 = view.findViewById(R.id.dot1)
        dot2 = view.findViewById(R.id.dot2)
        dot3 = view.findViewById(R.id.dot3)
        dateTextView = view.findViewById(R.id.dateTextView)
        dayTextView = view.findViewById(R.id.dayTextView)
        leftArrow = view.findViewById(R.id.LeftArrow)
        rightArrow = view.findViewById(R.id.RightArrow)
        waterInput = view.findViewById(R.id.WaterIntake)
        saveWaterButton = view.findViewById(R.id.FirstSave)
        noteEditText = view.findViewById(R.id.NoteEditText)
        secondSaveButton = view.findViewById(R.id.SecondSave)

        viewPager.adapter = DiaryPagerAdapter(this)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
            }
        })
        updateDots(0)

        setCurrentDate()

        refreshButton = view.findViewById(R.id.refreshButton)
        refreshButton.setOnClickListener {
            resetNutritionData()
        }

        dateTextView.setOnClickListener {
            showDatePickerDialog()
        }

        leftArrow.setOnClickListener {
            currentCalendar.add(Calendar.DAY_OF_YEAR, -1)
            setCurrentDate()
            loadDataForCurrentDate()
        }

        rightArrow.setOnClickListener {
            currentCalendar.add(Calendar.DAY_OF_YEAR, 1)
            setCurrentDate()
            loadDataForCurrentDate()
        }

        saveWaterButton.setOnClickListener {
            saveWaterIntake()
        }

        secondSaveButton.setOnClickListener {
            saveUserNote()
        }

        disableRightArrowIfNeeded()

        return view
    }

    private fun loadDataForCurrentDate() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(currentCalendar.time)

        diaryViewModel.loadNutritionFactsForDate(currentDate)
        fetchNote()
    }

    private fun fetchRecommendedIntake() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    recommendedWater = document.getLong("recommendedWater")?.toInt() ?: 0
                    recommendedCalories = document.getLong("recommendedCalories")?.toInt() ?: 0
                    recommendedProtein = document.getLong("recommendedProtein")?.toInt() ?: 0
                    recommendedCarbs = document.getLong("recommendedCarbs")?.toInt() ?: 0
                    recommendedFats = document.getLong("recommendedFats")?.toInt() ?: 0
                    recommendedCholesterol = document.getLong("recommendedCholesterol")?.toInt() ?: 0
                    recommendedSodium = document.getLong("recommendedSodium")?.toInt() ?: 0
                    recommendedSugars = document.getLong("recommendedSugars")?.toInt() ?: 0
                    recommendedFiber = document.getLong("recommendedFiber")?.toInt() ?: 0
                    recommendedAlcohol = document.getLong("recommendedAlcohol")?.toInt() ?: 0
                    recommendedCaffeine = document.getLong("recommendedCaffeine")?.toInt() ?: 0
                    recommendedPotassium = document.getLong("recommendedPotassium")?.toInt() ?: 0

                    updateUI()
                    loadDataForCurrentDate()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Diary", "Error fetching recommended intake: ${e.message}")
            }
    }

    private fun updateUI() {
        diaryViewModel.nutritionFacts.observe(viewLifecycleOwner) { nutritionFacts ->
            consumedWater = nutritionFacts["Water"]?.toInt() ?: 0

            tvWater.text = "Water - $consumedWater / $recommendedWater ml"
            progressBarWater.max = recommendedWater
            progressBarWater.progress = consumedWater
        }
    }

    private fun resetNutritionData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentCalendar.time)

        val resetData = hashMapOf(
            "timestamp" to System.currentTimeMillis(),
            "nutrition" to emptyMap<String, Float>()
        )

        firestore.collection("users").document(userId)
            .collection("dates").document(currentDate)
            .set(resetData, SetOptions.merge())
            .addOnSuccessListener {
                diaryViewModel.resetNutritionFacts()
                Toast.makeText(requireContext(), "Nutrition data has been reset.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to reset nutrition data.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserNote() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(currentCalendar.time)
        val firestore = FirebaseFirestore.getInstance()

        val noteText = noteEditText.text.toString()

        val noteData = hashMapOf(
            "note" to noteText,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("users").document(userId)
            .collection("dates").document(currentDate)
            .set(noteData, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Note saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to save note.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchNote() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(currentCalendar.time)
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("users").document(userId)
            .collection("dates").document(currentDate)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val noteText = document.getString("note") ?: ""
                    noteEditText.setText(noteText)
                } else {
                    noteEditText.setText("")
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch note.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveWaterIntake() {
        val waterAmount = waterInput.text.toString().toFloatOrNull()
        if (waterAmount == null || waterAmount < 0) {
            Toast.makeText(requireContext(), "Please enter a valid water intake amount.", Toast.LENGTH_SHORT).show()
            return
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(currentCalendar.time)

        diaryViewModel.accumulateNutritionFacts(mapOf("Water" to waterAmount), currentDate)
        Toast.makeText(requireContext(), "Water intake saved successfully.", Toast.LENGTH_SHORT).show()
    }

    private fun setCurrentDate() {
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        dateTextView.text = dateFormat.format(currentCalendar.time)

        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        dayTextView.text = dayFormat.format(currentCalendar.time)

        disableRightArrowIfNeeded()
    }

    private fun updateDots(position: Int) {
        animateDot(dot1, position == 0)
        animateDot(dot2, position == 1)
        animateDot(dot3, position == 2)
    }

    private fun animateDot(dot: View, isActive: Boolean) {
        val scale = if (isActive) 1.5f else 1f
        val alpha = if (isActive) 1f else 0.5f
        val color = if (isActive) Color.parseColor("#72bd39") else Color.parseColor("#ffffff")

        val scaleXAnimator = ObjectAnimator.ofFloat(dot, "scaleX", scale)
        val scaleYAnimator = ObjectAnimator.ofFloat(dot, "scaleY", scale)
        val alphaAnimator = ObjectAnimator.ofFloat(dot, "alpha", alpha)

        val animatorSet = AnimatorSet()
        animatorSet.play(scaleXAnimator).with(scaleYAnimator).with(alphaAnimator)
        animatorSet.duration = 300
        animatorSet.start()
    }

    private fun disableRightArrowIfNeeded() {
        val currentDate = Calendar.getInstance()
        rightArrow.visibility = if (
            currentCalendar.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) &&
            currentCalendar.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH) &&
            currentCalendar.get(Calendar.DAY_OF_MONTH) == currentDate.get(Calendar.DAY_OF_MONTH)
        ) View.GONE else View.VISIBLE
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                currentCalendar = selectedDate
                setCurrentDate()
                loadDataForCurrentDate()
            },
            year, month, day
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
}