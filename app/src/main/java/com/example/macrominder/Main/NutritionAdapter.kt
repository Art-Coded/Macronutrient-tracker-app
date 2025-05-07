package com.example.macrominder.Main

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.macrominder.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class NutritionAdapter(
    private val items: List<String>,
    private val viewModel: NutritionViewModel,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<NutritionAdapter.NutritionViewHolder>() {

    private val expandedStates =
        MutableList(items.size) { index -> items[index] == "Serving Sizes" }
    private val observers = mutableMapOf<Int, Observer<List<Pair<String, String>>>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NutritionViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_nutrition, parent, false)
        return NutritionViewHolder(view)
    }

    override fun onBindViewHolder(holder: NutritionViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item

        holder.resetViews()

        if (expandedStates[position]) {
            when (item) {
                "Serving Sizes" -> {
                    holder.showServingSizes()
                    setupServingSizesObserver(holder, position)
                }
                "Nutrition Facts" -> holder.showNutritionFacts()
            }
        }

        if (item == "Nutrition Facts") {
            holder.arrowIcon.visibility = View.VISIBLE
            holder.arrowIcon.setImageResource(
                if (expandedStates[position]) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down
            )
            holder.itemView.setOnClickListener {
                expandedStates[position] = !expandedStates[position]
                notifyItemChanged(position)
            }
        } else {
            holder.itemView.setOnClickListener(null)
        }

        holder.addButton.setOnClickListener {
            val servingName = holder.servingNameInput.text.toString().trim()
            val servingWeight = holder.servingWeightInput.text.toString().trim()

            holder.servingNameLayout.helperText =
                if (servingName.isEmpty()) "Please enter a serving name." else null
            holder.servingWeightLayout.helperText =
                if (servingWeight.isEmpty()) "Please enter a serving weight." else null

            if (servingName.isNotEmpty() && servingWeight.isNotEmpty()) {
                viewModel.addServingSize(servingName, servingWeight)
                holder.servingNameInput.text?.clear()
                holder.servingWeightInput.text?.clear()
            }
        }
    }


    private fun setupServingSizesObserver(holder: NutritionViewHolder, position: Int) {
        val observer = Observer<List<Pair<String, String>>> { currentServingSizes ->
            if (expandedStates[position] || items[position] == "Serving Sizes") {
                holder.updateServingSizes(currentServingSizes)
            }
        }
        viewModel.servingSizes.observe(lifecycleOwner, observer)
        observers[position] = observer
    }

    override fun getItemCount() = items.size

    inner class NutritionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.titleText)
        val servingNameLayout: TextInputLayout = itemView.findViewById(R.id.servingNameInputLayout)
        val servingWeightLayout: TextInputLayout = itemView.findViewById(R.id.servingWeightInputLayout)
        val servingNameInput: TextInputEditText = itemView.findViewById(R.id.servingNameInput)
        val servingWeightInput: TextInputEditText = itemView.findViewById(R.id.servingWeightInput)
        val addedItemsContainer: LinearLayout = itemView.findViewById(R.id.addedItemsContainer)
        val addButton: TextView = itemView.findViewById(R.id.addServingSizeButton)
        val nutritionInput: TextView = itemView.findViewById(R.id.nutritionInput)
        val selectedServingText: TextView = itemView.findViewById(R.id.selectedServingText)
        val nutritionFactsContainer: LinearLayout = itemView.findViewById(R.id.nutritionFactsContainer)
        val arrowIcon: ImageView = itemView.findViewById(R.id.arrowIcon)

            fun resetViews() {
                servingNameLayout.visibility = View.GONE
                servingWeightLayout.visibility = View.GONE
                addButton.visibility = View.GONE
                addedItemsContainer.visibility = View.GONE
                nutritionInput.visibility = View.GONE
                selectedServingText.visibility = View.GONE
                nutritionFactsContainer.visibility = View.GONE
                arrowIcon.visibility = View.GONE
            }
            fun showServingSizes() {
                servingNameLayout.visibility = View.VISIBLE
                servingWeightLayout.visibility = View.VISIBLE
                addButton.visibility = View.VISIBLE
                addedItemsContainer.visibility = View.VISIBLE
            }

        fun showNutritionFacts() {
            nutritionFactsContainer.visibility = View.VISIBLE

            val nutritionFields = listOf(
                R.id.Energy to "Energy",
                R.id.Protein to "Protein",
                R.id.Fat to "Fat",
                R.id.Cholesterol to "Cholesterol",
                R.id.Sodium to "Sodium",
                R.id.Carbs to "Carbs",
                R.id.Fiber to "Fiber",
                R.id.Sugars to "Sugars",
                R.id.Water to "Water",
                R.id.Alcohol to "Alcohol",
                R.id.Caffeine to "Caffeine",
                R.id.Potassium to "Potassium",
            )

            nutritionFields.forEach { (viewId, fieldName) ->
                val inputField = itemView.findViewById<TextInputEditText>(viewId)
                inputField?.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        val input = inputField.text?.toString()?.trim()
                        viewModel.saveNutritionField(fieldName, input)
                    }
                }
            }
        }

            fun updateServingSizes(currentServingSizes: List<Pair<String, String>>) {
                if (currentServingSizes.isEmpty()) {
                    addedItemsContainer.visibility = View.GONE
                } else {
                    addedItemsContainer.visibility = View.VISIBLE
                    addedItemsContainer.removeAllViews()

                    currentServingSizes.forEach { (name, weight) ->
                        val linearLayout = LinearLayout(itemView.context).apply {
                            orientation = LinearLayout.HORIZONTAL
                            setPadding(16, 8, 16, 8)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        }
                        val textView = TextView(itemView.context).apply {
                            text = "$name - $weight g"
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                        }
                        val deleteButton = ImageView(itemView.context).apply {
                            setImageResource(R.drawable.ic_cancel)
                            setOnClickListener {
                                viewModel.removeServingSize(name, weight)
                            }
                        }
                        linearLayout.addView(textView)
                        linearLayout.addView(deleteButton)
                        addedItemsContainer.addView(linearLayout)
                    }
                }
            }
        }
    }



