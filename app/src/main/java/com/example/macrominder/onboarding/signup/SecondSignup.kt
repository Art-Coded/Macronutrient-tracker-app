package com.example.macrominder.onboarding.signup

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.macrominder.R

class SecondSignup : Fragment() {

    private lateinit var activityLevelSeekBar: SeekBar
    private lateinit var activityLevelTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var customEditText: EditText
    private lateinit var nextButton: TextView
    private var viewPager: ViewPager2? = null

    private val signupViewModel: SignupViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_second_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityLevelSeekBar = view.findViewById(R.id.activityLevelSeekBar)
        activityLevelTextView = view.findViewById(R.id.activityLevelTextView)
        descriptionTextView = view.findViewById(R.id.textView8)
        customEditText = view.findViewById(R.id.customEditText)
        nextButton = view.findViewById(R.id.button)

        viewPager = activity?.findViewById(R.id.viewPager)

        restoreSelections()

        activityLevelSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val activityLevel = getActivityLevelText(progress)
                activityLevelTextView.text = activityLevel

                if (progress == 5) {
                    customEditText.visibility = View.VISIBLE
                    updateButtonState(isEnabled = !customEditText.text.isNullOrBlank())
                } else {
                    customEditText.visibility = View.GONE
                    updateButtonState(isEnabled = true)
                }

                // Update ViewModel
                signupViewModel.activityLevel = progress
                signupViewModel.activityLevelDescription = activityLevel
                updateDescription(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        customEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (activityLevelSeekBar.progress == 5) {
                    val isEditTextFilled = !s.isNullOrBlank()
                    updateButtonState(isEnabled = isEditTextFilled)

                    signupViewModel.customActivityValue = s.toString()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        nextButton.setOnClickListener {
            viewPager?.currentItem = 2
        }
    }

    private fun restoreSelections() {
        val activityLevel = signupViewModel.activityLevel ?: 0
        activityLevelSeekBar.progress = activityLevel

        val activityLevelText = getActivityLevelText(activityLevel)
        activityLevelTextView.text = activityLevelText

        if (activityLevel == 5) {
            customEditText.visibility = View.VISIBLE
            customEditText.setText(signupViewModel.customActivityValue)
            updateButtonState(isEnabled = !customEditText.text.isNullOrBlank())
        } else {
            customEditText.visibility = View.GONE
            updateButtonState(isEnabled = true)
        }

        updateDescription(activityLevel)
    }

    private fun updateDescription(progress: Int) {
        val description = when (progress) {
            0 -> "No activity. Indicating minimal to no physical movement throughout the day."
            1 -> "Sedentary. Little or no exercise. Just daily activities."
            2 -> "Lightly Active. Light exercise (walking) or sports 1-3 days a week."
            3 -> "Moderately Active. Moderate exercise or sports 3-5 days a week."
            4 -> "Very Active. Hard exercise such as manual labor or competitive training."
            5 -> "Custom. Set your own fixed calories burned daily."
            else -> ""
        }
        descriptionTextView.text = description
    }

    private fun updateButtonState(isEnabled: Boolean) {
        nextButton.isEnabled = isEnabled
        nextButton.setBackgroundResource(
            if (isEnabled) R.drawable.button_color_green
            else R.drawable.button_color_inactive_green
        )
    }

    private fun getActivityLevelText(progress: Int): String {
        return when (progress) {
            0 -> "None (BMR x 0.2)"
            1 -> "Sedentary (BMR x 0.2)"
            2 -> "Lightly Active (BMR x 0.375)"
            3 -> "Moderately Active (BMR x 0.5)"
            4 -> "Very Active (BMR x 0.9)"
            5 -> "Custom"
            else -> "Unknown"
        }
    }
}
