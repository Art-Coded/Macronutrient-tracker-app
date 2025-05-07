package com.example.macrominder.onboarding.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.macrominder.R

class ThirdSignup : Fragment() {

    private lateinit var seekBar: SeekBar
    private lateinit var descriptionTextView: TextView
    private lateinit var nextButton: TextView
    private var viewPager: ViewPager2? = null
    private val viewModel: SignupViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_third_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        seekBar = view.findViewById(R.id.seekBar)
        descriptionTextView = view.findViewById(R.id.description)
        nextButton = view.findViewById(R.id.button)

        viewPager = activity?.findViewById(R.id.viewPager)

        val weightGoals = arrayOf(
            "Lose (1.75 lbs/week)",
            "Lose (1.50 lbs/week)",
            "Lose (1.25 lbs/week)",
            "Lose (1.00 lbs/week)",
            "Lose (0.75 lbs/week)",
            "Lose (0.50 lbs/week)",
            "Lose (0.25 lbs/week)",
            "Maintain (0.00 lbs/week)",
            "Gain (0.25 lbs/week)",
            "Gain (0.50 lbs/week)",
            "Gain (0.75 lbs/week)",
            "Gain (1.00 lbs/week)",
            "Gain (1.25 lbs/week)",
            "Gain (1.50 lbs/week)",
            "Gain (1.75 lbs/week)",
            "Gain (2.00 lbs/week)"
        )

        val savedProgress = viewModel.weightGoal ?: loadFromSharedPreferences("weight_goal")
        seekBar.progress = savedProgress
        descriptionTextView.text = weightGoals[savedProgress]

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                descriptionTextView.text = weightGoals[progress]

                viewModel.weightGoal = progress
                saveToSharedPreferences("weight_goal", progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        nextButton.setOnClickListener {
            viewPager?.currentItem = 3
        }
    }

    private fun saveToSharedPreferences(key: String, value: Int) {
        val sharedPreferences = requireContext().getSharedPreferences("signup_preferences", 0)
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    private fun loadFromSharedPreferences(key: String): Int {
        val sharedPreferences = requireContext().getSharedPreferences("signup_preferences", 0)
        return sharedPreferences.getInt(key, 7)
    }
}
