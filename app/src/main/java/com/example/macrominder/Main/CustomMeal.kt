package com.example.macrominder.Main

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.macrominder.R
import com.example.macrominder.databinding.CustomMealBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CustomMeal : Fragment() {

    private var _binding: CustomMealBinding? = null
    private val binding get() = _binding!!

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    private lateinit var mealsAdapter: MealsAdapter
    private val mealList = mutableListOf<MealData>()
    private var sortMode = SortMode.A_TO_Z
    private val currentDate = Calendar.getInstance()

    private lateinit var diaryViewModel: DiaryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CustomMealBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        diaryViewModel = ViewModelProvider(requireActivity()).get(DiaryViewModel::class.java)

        if (!isNetworkAvailable(requireContext())) {
            showOfflineMessage("No internet connection. Please try again later.")
            return
        }

        setupRecyclerView()
        loadMeals()
        setupListeners()

        binding.SortText.setOnClickListener { toggleSortMode() }

        binding.back.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Foods())
                .addToBackStack(null)
                .commit()
        }

        binding.add.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateMeal())
                .addToBackStack(null)
                .commit()
        }

        binding.clickMe.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateMeal())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupRecyclerView() {
        mealsAdapter = MealsAdapter(
            viewModel = diaryViewModel,
            onDeleteClick = { _, position -> mealsAdapter.removeMealWithAnimation(position) },
            onItemClick = { meal -> navigateToMealDetails(meal) },
            currentDate = currentDate
        )
        binding.MealList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mealsAdapter
            setPadding(0, 16, 0, 400)
            clipToPadding = false
            addItemDecoration(SpacingItemDecoration(50))
        }
    }

    private fun loadMeals() {
        binding.loadingProgressBar.visibility = View.VISIBLE
        val userId = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(userId)
            .collection("meals")
            .get()
            .addOnSuccessListener { querySnapshot ->
                mealList.clear()
                mealList.addAll(querySnapshot.documents.mapNotNull { document ->
                    val mealName = document.getString("mealName") ?: return@mapNotNull null
                    val mealItems = document.get("mealItems") as? List<Map<String, Any>> ?: emptyList()
                    val totalNutrients = document.get("totalNutrients") as? Map<String, Float> ?: emptyMap()

                    MealData(
                        mealName = mealName,
                        mealItems = mealItems.map {
                            MealItem(
                                name = it["name"] as? String ?: "",
                                weight = it["weight"] as? String ?: "",
                                ingredients = it["ingredients"] as? String ?: "",
                                nutrientData = it["nutrientData"] as? String ?: ""
                            )
                        },
                        totalNutrientData = totalNutrients,
                    )
                })
                handleMealsDisplay()
            }
            .addOnFailureListener {
                showOfflineMessage("Failed to load meals. Please try again.")
            }
    }

    private fun handleMealsDisplay() {
        if (mealList.isEmpty()) {
            showOfflineMessage("You don't have any meals yet. Let's try creating your first meal!")
        } else {
            mealsAdapter.updateMeals(mealList)
            binding.MealList.visibility = View.VISIBLE
            binding.emptyStateMessage.visibility = View.GONE
        }
        binding.loadingProgressBar.visibility = View.GONE
    }

    private fun setupListeners() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterMeals(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterMeals(query: String) {
        val filteredList = if (query.isBlank()) mealList else mealList.filter {
            it.mealName.contains(query, ignoreCase = true)
        }
        mealsAdapter.updateMeals(filteredList)
    }

    private fun navigateToMealDetails(meal: MealData) {
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

        val sortedList = when (sortMode) {
            SortMode.A_TO_Z -> mealList.sortedBy { it.mealName }
            SortMode.Z_TO_A -> mealList.sortedByDescending { it.mealName }
            SortMode.RECENTLY_ADDED -> mealList
        }
        mealsAdapter.updateMeals(sortedList)
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showOfflineMessage(message: String) {
        binding.loadingProgressBar.visibility = View.GONE
        binding.MealList.visibility = View.GONE
        binding.emptyStateMessage.text = message
        binding.emptyStateMessage.visibility = View.VISIBLE
        binding.birdIcon.visibility = View.VISIBLE
        binding.clickMe.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class SortMode {
        A_TO_Z, Z_TO_A, RECENTLY_ADDED
    }
}