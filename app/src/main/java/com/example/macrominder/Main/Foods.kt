package com.example.macrominder.Main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.macrominder.R
import com.example.macrominder.databinding.FragmentFoodsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Foods : Fragment() {

    private var _binding: FragmentFoodsBinding? = null
    private val binding get() = _binding!!

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private var currentLoadingButton: TextView? = null
    private var isFetchingData = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFoodsBinding.inflate(inflater, container, false)

        binding.button1.setOnClickListener {
            onButtonClicked(binding.button1, CreateMeal())
        }

        binding.button3.setOnClickListener {
            onButtonClicked(binding.button3, CreateFood())
        }

        binding.imageView21.setOnClickListener {
            navigateToFragment(CustomFoods())
        }

        binding.imageView24.setOnClickListener {
            navigateToFragment(CustomMeal())
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        
        if (!isFetchingData) {
            fetchAndDisplayFoodsCount()
            fetchAndDisplayMealsCount()
        }
    }

    private fun fetchAndDisplayFoodsCount() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            binding.foodsCount.text = "0 Foods"
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        isFetchingData = true
        val userId = currentUser.uid
        firestore.collection("users")
            .document(userId)
            .collection("foods")
            .get()
            .addOnSuccessListener { snapshot ->
                if (isAdded) {
                    val count = snapshot.size()
                    binding.foodsCount.text = "$count Foods"
                }
            }
            .addOnFailureListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Failed to fetch food count.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnCompleteListener {
                isFetchingData = false
            }
    }

    private fun fetchAndDisplayMealsCount() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            binding.mealCount.text = "0 Meals"
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        isFetchingData = true
        val userId = currentUser.uid
        firestore.collection("users")
            .document(userId)
            .collection("meals")
            .get()
            .addOnSuccessListener { snapshot ->
                if (isAdded) {
                    val count = snapshot.size()
                    binding.mealCount.text = "$count Meals"
                }
            }
            .addOnFailureListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Failed to fetch meal count.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnCompleteListener {
                isFetchingData = false
            }
    }

    private fun onButtonClicked(button: TextView, fragment: Fragment) {
        setLoadingState(button, true)

        navigateToFragment(fragment) {
            setLoadingState(button, false)
        }
    }

    private fun setLoadingState(button: TextView, isLoading: Boolean) {
        currentLoadingButton = if (isLoading) button else null

        if (isLoading) {
            button.visibility = View.INVISIBLE
            when (button.id) {
                R.id.button1 -> binding.progressBar1.visibility = View.VISIBLE
                R.id.button3 -> binding.progressBar3.visibility = View.VISIBLE
            }
        } else {
            button.visibility = View.VISIBLE
            when (button.id) {
                R.id.button1 -> binding.progressBar1.visibility = View.GONE
                R.id.button3 -> binding.progressBar3.visibility = View.GONE
            }
        }
    }

    private fun navigateToFragment(fragment: Fragment, onComplete: (() -> Unit)? = null) {
        if (!isAdded) return

        binding.root.postDelayed({
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
            onComplete?.invoke()
        }, 500)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
