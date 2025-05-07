package com.example.macrominder.Main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.macrominder.R

class Calculator : Fragment() {

    private lateinit var inputFieldFirst: EditText
    private lateinit var fromSpinner: Spinner
    private lateinit var toSpinner: Spinner
    private lateinit var tvResult: TextView


    private val weightConversionRates = mapOf(
        "Gram (g)" to 1.0,
        "Microgram (µg)" to 1e6,
        "Quintal (q)" to 0.01,
        "Carat (ct)" to 5.0,
        "Ton (t)" to 0.000001,
        "Milligram (mg)" to 1000.0,
        "Kilogram (kg)" to 0.001,
        "Pounds (lbs)" to 0.00220462
    )


    private val lengthConversionFactors = mapOf(
        Pair("Inches (in)", "Centimeters (cm)") to 2.54,
        Pair("Centimeters (cm)", "Inches (in)") to 0.3937
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.calculator, container, false)


        inputFieldFirst = view.findViewById(R.id.inputFieldFirst)
        fromSpinner = view.findViewById(R.id.fromSpinner)
        toSpinner = view.findViewById(R.id.toSpinner)
        tvResult = view.findViewById(R.id.tvResult)


        val units = weightConversionRates.keys + lengthConversionFactors.keys.flatMap { listOf(it.first, it.second) }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units.toSet().toList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        fromSpinner.adapter = adapter
        toSpinner.adapter = adapter


        inputFieldFirst.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                calculateResult()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        fromSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                calculateResult()
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {}
        }

        toSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                calculateResult()
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {}
        }

        return view
    }

    private fun calculateResult() {
        val inputText = inputFieldFirst.text.toString()
        val fromUnit = fromSpinner.selectedItem?.toString() ?: return
        val toUnit = toSpinner.selectedItem?.toString() ?: return

        if (inputText.isNotEmpty()) {
            try {
                val inputValue = inputText.toDouble()


                if (lengthConversionFactors.containsKey(Pair(fromUnit, toUnit))) {
                    val conversionFactor = lengthConversionFactors[Pair(fromUnit, toUnit)] ?: 1.0
                    val result = inputValue * conversionFactor
                    tvResult.text = "%.4f $toUnit".format(result)
                    return
                }


                val fromRate = weightConversionRates[fromUnit] ?: 1.0
                val toRate = weightConversionRates[toUnit] ?: 1.0
                val result = (inputValue / fromRate) * toRate

                tvResult.text = "%.4f $toUnit".format(result)
            } catch (e: NumberFormatException) {
                tvResult.text = "Invalid input"
            }
        } else {
            tvResult.text = ""
        }
    }
}
