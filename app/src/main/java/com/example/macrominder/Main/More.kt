package com.example.macrominder.Main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.macrominder.R
import com.google.firebase.auth.FirebaseAuth

class More : Fragment() {

    private lateinit var logoutTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var accountImageView: ImageView
    private lateinit var tosImageView: ImageView
    private lateinit var aboutImageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_more, container, false)

        // Initialize views
        logoutTextView = view.findViewById(R.id.logout)
        emailTextView = view.findViewById(R.id.Email)
        accountImageView = view.findViewById(R.id.Account)
        tosImageView = view.findViewById(R.id.TOS)
        aboutImageView = view.findViewById(R.id.About)

        setEmail()

        logoutTextView.setOnClickListener {
            logout()
        }

        accountImageView.setOnClickListener {
            onButtonClicked(accountImageView, Account())
        }

        tosImageView.setOnClickListener {
            onButtonClicked(tosImageView, TOS())
        }

        aboutImageView.setOnClickListener {
            onButtonClicked(aboutImageView, About())
        }

        return view
    }

    private fun setEmail() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            emailTextView.text = user.email
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()

        clearUserData()

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

        val navController = findNavController()

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.mainViewPagerFragment, true)
            .build()

        navController.navigate(R.id.action_mainViewPagerFragment_to_login, null, navOptions)
    }


    private fun clearUserData() {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }


    private fun onButtonClicked(button: View, fragment: Fragment) {
        setLoadingState(button, true)

        navigateToFragment(fragment) {
            setLoadingState(button, false)
        }
    }

    private fun setLoadingState(button: View, isLoading: Boolean) {
        button.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
    }

    private fun navigateToFragment(fragment: Fragment, onComplete: (() -> Unit)? = null) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()

        onComplete?.invoke()
    }
}
