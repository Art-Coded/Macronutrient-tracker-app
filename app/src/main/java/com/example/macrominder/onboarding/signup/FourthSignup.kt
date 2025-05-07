package com.example.macrominder.onboarding.signup

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.macrominder.Main.TOS
import com.example.macrominder.Main.TOSsignup
import com.example.macrominder.R
import com.example.macrominder.databinding.FragmentFourthSignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern

class FourthSignup : Fragment() {

    private var _binding: FragmentFourthSignupBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth


    private val signupViewModel: SignupViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFourthSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        firebaseAuth = FirebaseAuth.getInstance()


        binding.button.isEnabled = false
        binding.button.setBackgroundResource(R.drawable.button_color_inactive_green)


        binding.EditTextName.addTextChangedListener(nameWatcher)
        binding.EditTextEmail.addTextChangedListener(validationWatcher)
        binding.EditTextPassword.addTextChangedListener(passwordWatcher)
        binding.EditTextConfirmPassword.addTextChangedListener(validationWatcher)


        binding.button.setOnClickListener {
            val name = binding.EditTextName.text.toString().trim()
            val email = binding.EditTextEmail.text.toString().trim()
            val password = binding.EditTextPassword.text.toString().trim()
            val confirmPassword = binding.EditTextConfirmPassword.text.toString().trim()


            binding.button.isEnabled = false
            binding.button.setBackgroundResource(R.drawable.button_color_inactive_green)


            binding.progressBar.visibility = View.VISIBLE
            binding.button.visibility = View.GONE


            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Name is required", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.button.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            binding.progressBar.visibility = View.GONE
                            binding.button.visibility = View.VISIBLE

                            if (task.isSuccessful) {
                                val user = firebaseAuth.currentUser
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build()


                                user?.updateProfile(profileUpdates)?.addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {

                                        saveSignupDataToFirebase(email)

                                        val sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
                                        val editor = sharedPreferences.edit()
                                        editor.putBoolean("accountCreated", true)
                                        editor.apply()

                                        Toast.makeText(requireContext(), "Account created successfully", Toast.LENGTH_SHORT).show()

                                        findNavController().navigate(R.id.login)
                                    } else {
                                        Toast.makeText(
                                            requireContext(),
                                            "Failed to save name: ${updateTask.exception?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Error: ${task.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    binding.button.visibility = View.VISIBLE
                }
            } else {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.button.visibility = View.VISIBLE
            }

        }

    }


    private fun saveSignupDataToFirebase(email: String) {
        val recommendedIntake = RecommendedIntakeCalculator.calculate(signupViewModel)

        val userData = hashMapOf(
            "email" to email,
            "name" to binding.EditTextName.text.toString().trim(),
            "gender" to signupViewModel.gender,
            "birthday" to signupViewModel.birthday,
            "height" to signupViewModel.height,
            "weight" to signupViewModel.weight,
            "activityLevel" to signupViewModel.activityLevel,
            "weightGoal" to signupViewModel.weightGoal,
            "recommendedCalories" to recommendedIntake.calories,
            "recommendedProtein" to recommendedIntake.protein,
            "recommendedCarbs" to recommendedIntake.carbs,
            "recommendedFats" to recommendedIntake.fats,
            "recommendedWater" to recommendedIntake.water,
            "recommendedCholesterol" to recommendedIntake.cholesterol,
            "recommendedSodium" to recommendedIntake.sodium,
            "recommendedSugars" to recommendedIntake.sugars,
            "recommendedFiber" to recommendedIntake.fiber,
            "recommendedCaffeine" to recommendedIntake.caffeine,
            "recommendedPotassium" to recommendedIntake.potassium
        )

        FirebaseFirestore.getInstance().collection("users")
            .document(firebaseAuth.currentUser?.uid ?: "")
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "User data saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private val validationWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            validateForm()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private val nameWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            val name = binding.EditTextName.text.toString().trim()
            if (name.isEmpty()) {
                binding.textInputName.helperText = "Required*"
            } else {
                binding.textInputName.helperText = null
            }
            validateForm()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private val passwordWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            val password = binding.EditTextPassword.text.toString().trim()
            if (password.isEmpty()) {
                binding.textInputPassword.helperText = "Required*"
            } else {
                binding.textInputPassword.helperText = null
            }
            validateForm()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun validateForm() {
        val email = binding.EditTextEmail.text.toString().trim()
        val password = binding.EditTextPassword.text.toString().trim()
        val confirmPassword = binding.EditTextConfirmPassword.text.toString().trim()

        val isEmailValid = Pattern.compile("[a-zA-Z0-9._-]+@gmail\\.com").matcher(email).matches()
        val isPasswordValid = password == confirmPassword && password.isNotEmpty()
        val isFormValid = isEmailValid && isPasswordValid

        binding.button.isEnabled = isFormValid
        binding.button.setBackgroundResource(
            if (isFormValid) R.drawable.button_color_green else R.drawable.button_color_inactive_green
        )

        if (!isEmailValid) {
            binding.textInputEmail.helperText = "Invalid email format"
        } else {
            binding.textInputEmail.helperText = null
        }

        if (!isPasswordValid) {
            binding.textInputConfirmPassword.helperText = "Passwords do not match"
        } else {
            binding.textInputConfirmPassword.helperText = null
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
