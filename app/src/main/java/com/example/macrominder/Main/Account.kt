package com.example.macrominder.Main

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.macrominder.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.*

class Account : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var fbName: TextView
    private lateinit var fbEmail: TextView
    private lateinit var fbGender: TextView
    private lateinit var fbAge: TextView
    private lateinit var fbWeight: TextView
    private lateinit var fbHeight: TextView
    private lateinit var backImageView: ImageView
    private lateinit var ageBg: ImageView
    private lateinit var weightBg: ImageView
    private lateinit var heightBg: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.account, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        fbName = view.findViewById(R.id.fbName)
        fbEmail = view.findViewById(R.id.fbEmail)
        fbGender = view.findViewById(R.id.fbGender)
        fbAge = view.findViewById(R.id.fbAge)
        fbWeight = view.findViewById(R.id.fbWeight)
        fbHeight = view.findViewById(R.id.fbHeight)
        backImageView = view.findViewById(R.id.back)
        ageBg = view.findViewById(R.id.Agebg)
        weightBg = view.findViewById(R.id.Weightbg)
        heightBg = view.findViewById(R.id.Heightbg)

        backImageView.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, More())
                .addToBackStack(null)
                .commit()
        }

        ageBg.setOnClickListener { showBirthdayPicker() }
        weightBg.setOnClickListener { showWeightPicker() }
        heightBg.setOnClickListener { showHeightPicker() }

        loadAccountDetails()

        return view
    }

    private fun loadAccountDetails() {
        val currentUser = auth.currentUser ?: run {
            fbEmail.text = "Not logged in"
            return
        }

        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    fbName.text = document.getString("name") ?: "N/A"
                    fbEmail.text = document.getString("email") ?: "N/A"
                    fbGender.text = document.getString("gender") ?: "N/A"

                    val birthday = document.getString("birthday") ?: "N/A"
                    fbAge.text = if (birthday != "N/A") "${calculateAgeFromBirthday(birthday)} years" else "N/A"

                    fbWeight.text = document.getString("weight") ?: "N/A"
                    fbHeight.text = document.getString("height") ?: "N/A"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error loading data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showBirthdayPicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDay)
            }
            val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(selectedDate.time)
            updateBirthday(formattedDate)
        }, year, month, day).show()
    }

    private fun showWeightPicker() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_weight_picker, null)
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Edit Weight")
            .setPositiveButton("Save") { dialog, _ ->
                val weightInput = dialogView.findViewById<EditText>(R.id.weightInput)
                val weight = weightInput.text.toString()
                if (weight.isNotEmpty()) {
                    val isKg = dialogView.findViewById<Button>(R.id.btnKg).isSelected
                    val weightToSave = if (isKg) weight else (weight.toFloat() * 0.453592).toString()
                    updateWeight(weightToSave)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        val btnKg = dialogView.findViewById<Button>(R.id.btnKg)
        val btnLbs = dialogView.findViewById<Button>(R.id.btnLbs)
        val unitText = dialogView.findViewById<TextView>(R.id.unitSelectionText)

        // Set initial state
        btnKg.isSelected = true
        btnKg.setBackgroundColor(resources.getColor(R.color.white, null))
        btnLbs.setBackgroundColor(resources.getColor(R.color.white, null))

        btnKg.setOnClickListener {
            it.isSelected = true
            btnLbs.isSelected = false
            unitText.text = "Weight (kg)"
            btnKg.setBackgroundColor(resources.getColor(R.color.white, null))
            btnLbs.setBackgroundColor(resources.getColor(R.color.white, null))
        }

        btnLbs.setOnClickListener {
            it.isSelected = true
            btnKg.isSelected = false
            unitText.text = "Weight (lbs)"
            btnLbs.setBackgroundColor(resources.getColor(R.color.white, null))
            btnKg.setBackgroundColor(resources.getColor(R.color.white, null))
        }

        alertDialog.show()
    }

    private fun showHeightPicker() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_height_picker, null)
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Edit Height")
            .setPositiveButton("Save") { dialog, _ ->
                val numberPickerCm = dialogView.findViewById<NumberPicker>(R.id.numberPickerCm)
                val numberPickerFeet = dialogView.findViewById<NumberPicker>(R.id.numberPickerFeet)
                val numberPickerInches = dialogView.findViewById<NumberPicker>(R.id.numberPickerInches)

                val isCm = dialogView.findViewById<Button>(R.id.btnCm).isSelected
                val heightToSave = if (isCm) {
                    "${numberPickerCm.value} cm"
                } else {
                    val totalInches = numberPickerFeet.value * 12 + numberPickerInches.value
                    val cm = (totalInches * 2.54).toInt()
                    "$cm cm"
                }
                updateHeight(heightToSave)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        val numberPickerCm = dialogView.findViewById<NumberPicker>(R.id.numberPickerCm)
        val numberPickerFeet = dialogView.findViewById<NumberPicker>(R.id.numberPickerFeet)
        val numberPickerInches = dialogView.findViewById<NumberPicker>(R.id.numberPickerInches)
        val feetInchesLayout = dialogView.findViewById<LinearLayout>(R.id.feetInchesLayout)
        val btnCm = dialogView.findViewById<Button>(R.id.btnCm)
        val btnInches = dialogView.findViewById<Button>(R.id.btnInches)
        val unitText = dialogView.findViewById<TextView>(R.id.unitSelectionText)

        // Set up number pickers
        numberPickerCm.minValue = 100
        numberPickerCm.maxValue = 250
        numberPickerCm.value = 170

        numberPickerFeet.minValue = 3
        numberPickerFeet.maxValue = 8
        numberPickerInches.minValue = 0
        numberPickerInches.maxValue = 11

        // Set initial state
        btnCm.isSelected = true
        btnCm.setBackgroundColor(resources.getColor(R.color.white, null))
        btnInches.setBackgroundColor(resources.getColor(R.color.white, null))
        feetInchesLayout.visibility = View.GONE

        btnCm.setOnClickListener {
            it.isSelected = true
            btnInches.isSelected = false
            unitText.text = "Height (cm)"
            numberPickerCm.visibility = View.VISIBLE
            feetInchesLayout.visibility = View.GONE
            btnCm.setBackgroundColor(resources.getColor(R.color.white, null))
            btnInches.setBackgroundColor(resources.getColor(R.color.white, null))
        }

        btnInches.setOnClickListener {
            it.isSelected = true
            btnCm.isSelected = false
            unitText.text = "Height (ft/in)"
            numberPickerCm.visibility = View.GONE
            feetInchesLayout.visibility = View.VISIBLE
            btnInches.setBackgroundColor(resources.getColor(R.color.white, null))
            btnCm.setBackgroundColor(resources.getColor(R.color.white, null))
        }

        alertDialog.show()
    }

    private fun updateBirthday(newBirthday: String) {
        auth.currentUser?.uid?.let { userId ->
            firestore.collection("users")
                .document(userId)
                .update("birthday", newBirthday)
                .addOnSuccessListener {
                    fbAge.text = "${calculateAgeFromBirthday(newBirthday)} years"
                    Toast.makeText(requireContext(), "Birthday updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to update birthday", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateWeight(newWeight: String) {
        auth.currentUser?.uid?.let { userId ->
            firestore.collection("users")
                .document(userId)
                .update("weight", newWeight)
                .addOnSuccessListener {
                    fbWeight.text = newWeight
                    Toast.makeText(requireContext(), "Weight updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to update weight", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateHeight(newHeight: String) {
        auth.currentUser?.uid?.let { userId ->
            firestore.collection("users")
                .document(userId)
                .update("height", newHeight)
                .addOnSuccessListener {
                    fbHeight.text = newHeight
                    Toast.makeText(requireContext(), "Height updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to update height", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun calculateAgeFromBirthday(birthday: String): Int {
        return try {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            val birthDate = dateFormat.parse(birthday) ?: return -1

            val today = Calendar.getInstance()
            val birthCalendar = Calendar.getInstance().apply { time = birthDate }

            var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)

            if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            age
        } catch (e: Exception) {
            -1
        }
    }
}