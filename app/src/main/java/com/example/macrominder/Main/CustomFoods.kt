package com.example.macrominder.Main

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.macrominder.R
import com.example.macrominder.databinding.CustomFoodsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CustomFoods : Fragment() {

    private var _binding: CustomFoodsBinding? = null
    private val binding get() = _binding!!
    private val diaryViewModel: DiaryViewModel by activityViewModels()

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private lateinit var foodAdapter: FoodAdapter
    private val foodList = mutableListOf<Map<String, Any>>()
    private var sortMode = SortMode.A_TO_Z
    private val currentDate = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CustomFoodsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        foodAdapter = FoodAdapter(
            context = requireContext(),
            onItemClick = { selectedFood -> navigateToFoodDetail(selectedFood) },
            diaryViewModel = diaryViewModel,
            currentDate = currentDate
        )

        binding.FoodlList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = foodAdapter
            setPadding(0, 16, 0, 400)
            clipToPadding = false
            addItemDecoration(SpacingItemDecoration(50))
        }

        binding.FoodlList.visibility = View.GONE
        binding.emptyStateMessage.visibility = View.GONE
        binding.birdIcon.visibility = View.GONE
        binding.clickMe.visibility = View.GONE

        if (isNetworkAvailable(requireContext())) {
            loadFoodData()
        } else {
            showOfflineMessage()
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterFoods(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.SortText.setOnClickListener {
            toggleSortMode()
        }

        binding.back.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Foods())
                .addToBackStack(null)
                .commit()
        }

        binding.add.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateFood())
                .addToBackStack(null)
                .commit()
        }

        binding.clickMe.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateFood())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadFoodData() {
        binding.loadingProgressBar.visibility = View.VISIBLE
        binding.FoodlList.visibility = View.GONE
        binding.emptyStateMessage.visibility = View.GONE
        binding.birdIcon.visibility = View.GONE
        binding.clickMe.visibility = View.GONE

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("CustomFoods", "No authenticated user found.")
            return
        }

        val userId = currentUser.uid
        firestore.collection("users")
            .document(userId)
            .collection("foods")
            .get()
            .addOnSuccessListener { result ->
                foodList.clear()
                val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                foodList.addAll(result.documents.map { document ->
                    val foodName = document.getString("foodName") ?: "Unnamed Food"
                    val timestamp = document.getTimestamp("timestamp")?.toDate()
                        ?.let { dateFormat.format(it) } ?: "No timestamp"
                    val servingSizes = document.get("servingSizes") as? List<Map<String, Any>> ?: emptyList()
                    val formattedServingSizes = servingSizes.joinToString("\n") { "${it["first"]} - ${it["second"]}g" }


                    val rawNutritionFacts = document["nutritionFacts"] as? Map<String, Any> ?: emptyMap()
                    val formattedNutritionFacts = rawNutritionFacts.map { (key, value) ->
                        formatNutritionFact(key, value.toString())
                    }.joinToString("\n")

                    val nutritionFactsFloat = rawNutritionFacts.mapValues { entry ->
                        (entry.value as? String)?.toFloatOrNull() ?: 0f
                    }

                    mapOf(
                        "foodName" to foodName,
                        "timestamp" to timestamp,
                        "formattedServingSizes" to formattedServingSizes,
                        "formattedNutritionFacts" to formattedNutritionFacts,
                        "rawNutritionFacts" to rawNutritionFacts,
                        "nutritionFactsFloat" to nutritionFactsFloat
                    )
                })

                sortAndDisplayFoods()
                binding.loadingProgressBar.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Log.e("CustomFoods", "Error loading food data", exception)
                showOfflineMessage()
            }
    }


    private fun toggleSortMode() {
        sortMode = when (sortMode) {
            SortMode.A_TO_Z -> SortMode.Z_TO_A
            SortMode.Z_TO_A -> SortMode.RECENTLY_ADDED
            SortMode.RECENTLY_ADDED -> SortMode.A_TO_Z
        }

        binding.SortText.text = when (sortMode) {
            SortMode.A_TO_Z -> "Sort: A to Z"
            SortMode.Z_TO_A -> "Sort: Z to A"
            SortMode.RECENTLY_ADDED -> "Sort: Recently Added"
        }

        sortAndDisplayFoods()
    }

    private fun sortAndDisplayFoods() {
        val sortedList = when (sortMode) {
            SortMode.A_TO_Z -> foodList.sortedBy { it["foodName"] as? String }
            SortMode.Z_TO_A -> foodList.sortedByDescending { it["foodName"] as? String }
            SortMode.RECENTLY_ADDED -> foodList.sortedByDescending { it["timestamp"] as? String }
        }
        foodAdapter.submitList(sortedList)
        binding.FoodlList.visibility = if (sortedList.isEmpty()) View.GONE else View.VISIBLE
        binding.emptyStateMessage.visibility = if (sortedList.isEmpty()) View.VISIBLE else View.GONE
        binding.clickMe.visibility = if (sortedList.isEmpty()) View.VISIBLE else View.GONE
        binding.birdIcon.visibility = if (sortedList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun formatNutritionFact(key: String, value: String): String {
        val suffixMap = mapOf(
            "Energy" to "kcal",
            "Protein" to "g",
            "Fat" to "g",
            "Cholesterol" to "mg",
            "Sodium" to "mg",
            "Carbs" to "g",
            "Fiber" to "g",
            "Sugars" to "g",
            "Water" to "g",
            "Alcohol" to "g",
            "Caffeine" to "mg",
            "Potassium" to "mg"
        )
        val suffix = suffixMap[key] ?: ""
        return "$key = $value $suffix".trim()
    }

    private fun filterFoods(query: String) {
        val filteredList = if (query.isBlank()) {
            foodList
        } else {
            foodList.filter {
                (it["foodName"] as? String)?.contains(query, ignoreCase = true) == true
            }
        }
        foodAdapter.submitList(filteredList)
    }

    private fun navigateToFoodDetail(food: Map<String, Any>) {
        val foodName = food["foodName"] as? String ?: return
        Log.d("CustomFoods", "Navigating to details of $foodName")
    }

    private fun showOfflineMessage() {
        binding.loadingProgressBar.visibility = View.GONE
        binding.FoodlList.visibility = View.GONE
        binding.emptyStateMessage.text = "No internet connection. Please try again later."
        binding.emptyStateMessage.visibility = View.VISIBLE
        binding.birdIcon.visibility = View.VISIBLE
        binding.clickMe.visibility = View.GONE
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class SortMode {
        A_TO_Z, Z_TO_A, RECENTLY_ADDED
    }
}
