package com.example.macrominder

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class login : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var loginButton: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var signUpButton: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        loginButton = view.findViewById(R.id.button)
        progressBar = view.findViewById(R.id.progressBar)
        emailInputLayout = view.findViewById(R.id.textInputEmail)
        passwordInputLayout = view.findViewById(R.id.textInputPassword)
        emailEditText = view.findViewById(R.id.EditTextEmail)
        passwordEditText = view.findViewById(R.id.EditTextPassword)
        signUpButton = view.findViewById(R.id.button3)

        val backImageView: ImageView = view.findViewById(R.id.back)

        setLoginButtonEnabled(false)

        emailEditText.addTextChangedListener(loginTextWatcher)
        passwordEditText.addTextChangedListener(loginTextWatcher)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (isNetworkAvailable()) {
                    showLoading(true)
                    performLogin(email, password)
                } else {
                    emailInputLayout.helperText = "No network connection."
                }
            }
        }

        backImageView.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_viewPagerFragment)
        }

        signUpButton.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_signupViewPagerFragment)
        }
    }

    private val loginTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            setLoginButtonEnabled(email.isNotEmpty() && password.isNotEmpty())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun setLoginButtonEnabled(isEnabled: Boolean) {
        loginButton.isEnabled = isEnabled
        val background = if (isEnabled) {
            R.drawable.button_color_green
        } else {
            R.drawable.button_color_inactive_green
        }
        loginButton.background = ContextCompat.getDrawable(requireContext(), background)
    }

    private fun performLogin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                showLoading(false)
                emailInputLayout.error = null
                passwordInputLayout.error = null

                if (task.isSuccessful) {
                    findNavController().navigate(R.id.action_login_to_mainViewPagerFragment)
                } else {
                    val errorMessage = task.exception?.message.orEmpty()

                    when {
                        errorMessage.contains("email", ignoreCase = true) -> {
                            emailInputLayout.helperText = "Invalid email address."
                            emailInputLayout.boxStrokeColor =
                                ContextCompat.getColor(requireContext(), R.color.red)
                            passwordInputLayout.helperText = null
                        }
                        errorMessage.contains("password", ignoreCase = true) -> {
                            passwordInputLayout.helperText = "Invalid password."
                            passwordInputLayout.boxStrokeColor =
                                ContextCompat.getColor(requireContext(), R.color.red)
                            emailInputLayout.helperText = null
                        }
                        else -> {
                            emailInputLayout.helperText = null
                            passwordInputLayout.helperText = "Login failed. Please double check any details and try again."
                        }
                    }
                }
            }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            loginButton.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            loginButton.visibility = View.VISIBLE
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return networkInfo?.isConnected == true
    }
}
