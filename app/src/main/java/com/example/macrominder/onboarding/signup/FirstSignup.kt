package com.example.macrominder.onboarding.signup

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.macrominder.R
import java.text.SimpleDateFormat
import java.util.*

class FirstSignup : Fragment(R.layout.fragment_first_signup) {

    private val signupViewModel: SignupViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val genderTextView: TextView = view.findViewById(R.id.Gender)
        val birthdayTextView: TextView = view.findViewById(R.id.Birthday)
        val heightTextView: TextView = view.findViewById(R.id.Height)
        val weightTextView: TextView = view.findViewById(R.id.Weight)
        val nextButton: TextView = view.findViewById(R.id.button)

        val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)

        genderTextView.text = signupViewModel.gender
        birthdayTextView.text = signupViewModel.birthday
        heightTextView.text = signupViewModel.height
        weightTextView.text = signupViewModel.weight

        validateInputs(genderTextView, birthdayTextView, heightTextView, weightTextView, nextButton)

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateInputs(genderTextView, birthdayTextView, heightTextView, weightTextView, nextButton)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        genderTextView.addTextChangedListener(textWatcher)
        birthdayTextView.addTextChangedListener(textWatcher)
        heightTextView.addTextChangedListener(textWatcher)
        weightTextView.addTextChangedListener(textWatcher)

        nextButton.setOnClickListener {
            signupViewModel.gender = genderTextView.text.toString()
            signupViewModel.birthday = birthdayTextView.text.toString()
            signupViewModel.height = heightTextView.text.toString()
            signupViewModel.weight = weightTextView.text.toString()

            viewPager?.currentItem = 1
        }

        genderTextView.setOnClickListener { showGenderSelectionDialog(genderTextView) }
        birthdayTextView.setOnClickListener { showDatePickerDialog(birthdayTextView) }
        heightTextView.setOnClickListener { showHeightInputDialog(heightTextView) }
        weightTextView.setOnClickListener { showWeightInputDialog(weightTextView) }
    }

    private fun validateInputs(
        genderTextView: TextView,
        birthdayTextView: TextView,
        heightTextView: TextView,
        weightTextView: TextView,
        nextButton: TextView
    ) {
        val gender = genderTextView.text.toString().isNotEmpty()
        val birthday = birthdayTextView.text.toString().isNotEmpty()
        val height = heightTextView.text.toString().isNotEmpty()
        val weight = weightTextView.text.toString().isNotEmpty()

        if (gender && birthday && height && weight) {
            nextButton.isEnabled = true
            nextButton.setBackgroundResource(R.drawable.button_color_green)
        } else {
            nextButton.isEnabled = false
            nextButton.setBackgroundResource(R.drawable.button_color_inactive_green)
        }
    }

    private fun showGenderSelectionDialog(genderTextView: TextView) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_gender_selection, null)
        val recyclerView: RecyclerView = dialogView.findViewById(R.id.genderRecyclerView)
        val genderOptions = listOf("Male", "Female", "Pregnant", "Breastfeeding")

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(R.color.grey)


        dialog.window?.setDimAmount(0.7f)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = GenderAdapter(genderOptions) { selectedGender ->
            genderTextView.text = selectedGender
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDatePickerDialog(birthdayTextView: TextView) {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time
                birthdayTextView.text = dateFormat.format(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.window?.setBackgroundDrawableResource(R.color.grey)
        datePickerDialog.window?.setDimAmount(0.7f)

        datePickerDialog.show()

    }

    private fun showHeightInputDialog(heightTextView: TextView) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_height_picker, null)

        val unitSelectionText: TextView = dialogView.findViewById(R.id.unitSelectionText)
        val numberPickerCm: NumberPicker = dialogView.findViewById(R.id.numberPickerCm)
        val feetInchesLayout: LinearLayout = dialogView.findViewById(R.id.feetInchesLayout)
        val numberPickerFeet: NumberPicker = dialogView.findViewById(R.id.numberPickerFeet)
        val numberPickerInches: NumberPicker = dialogView.findViewById(R.id.numberPickerInches)

        val btnCm: Button = dialogView.findViewById(R.id.btnCm)
        val btnInches: Button = dialogView.findViewById(R.id.btnInches)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(R.color.grey)
        dialog.window?.setDimAmount(0.7f)

        setupNumberPicker(numberPickerCm, 50, 300)
        setupNumberPicker(numberPickerFeet, 3, 10)
        setupNumberPicker(numberPickerInches, 0, 11)

        btnCm.setOnClickListener {
            unitSelectionText.text = "Height (cm)"
            numberPickerCm.visibility = View.VISIBLE
            feetInchesLayout.visibility = View.GONE
        }

        btnInches.setOnClickListener {
            unitSelectionText.text = "Height (ft/inches)"
            numberPickerCm.visibility = View.GONE
            feetInchesLayout.visibility = View.VISIBLE
        }

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Save") { _, _ ->
            val height = if (numberPickerCm.visibility == View.VISIBLE) {
                numberPickerCm.value.toDouble()
            } else {
                val feet = numberPickerFeet.value
                val inches = numberPickerInches.value
                convertFeetInchesToCm(feet + inches / 12.0)
            }
            heightTextView.text = String.format("%.2f cm", height)
        }

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { _, _ -> dialog.dismiss() }
        dialog.show()
    }

    private fun showWeightInputDialog(weightTextView: TextView) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_weight_picker, null)
        val unitSelectionText: TextView = dialogView.findViewById(R.id.unitSelectionText)
        val weightInput: EditText = dialogView.findViewById(R.id.weightInput)
        val btnKg: Button = dialogView.findViewById(R.id.btnKg)
        val btnLbs: Button = dialogView.findViewById(R.id.btnLbs)

        var selectedUnit = "kg"
        btnKg.setOnClickListener { selectedUnit = "kg"; unitSelectionText.text = "Weight (kg)" }
        btnLbs.setOnClickListener { selectedUnit = "lbs"; unitSelectionText.text = "Weight (lbs)" }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(R.color.grey)
        dialog.window?.setDimAmount(0.7f)

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Save") { _, _ ->
            val weight = weightInput.text.toString().toDoubleOrNull()
            if (weight != null) {
                weightTextView.text = String.format("%.2f %s", weight, selectedUnit)
            }
        }

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { _, _ -> dialog.dismiss() }
        dialog.show()
    }

    private fun setupNumberPicker(numberPicker: NumberPicker, min: Int, max: Int) {
        numberPicker.minValue = min
        numberPicker.maxValue = max
        numberPicker.value = min
    }

    private fun convertFeetInchesToCm(feetInches: Double): Double = feetInches * 30.48
}
