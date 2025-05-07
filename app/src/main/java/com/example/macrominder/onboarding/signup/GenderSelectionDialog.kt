package com.example.macrominder.onboarding.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.macrominder.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GenderSelectionDialog(
    private val listener: OnGenderSelectedListener
) : BottomSheetDialogFragment() {

    private val genderList = listOf("Male", "Female", "Other")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_gender_selection, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.genderRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = GenderAdapter(genderList) { selectedGender ->
            listener.onGenderSelected(selectedGender)
            dismiss()
        }

        return view
    }

    interface OnGenderSelectedListener {
        fun onGenderSelected(gender: String)
    }
}