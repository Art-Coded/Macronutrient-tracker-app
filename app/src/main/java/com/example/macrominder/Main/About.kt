package com.example.macrominder.Main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.macrominder.R

class About : Fragment() {

    private lateinit var backImageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.about, container, false)


        backImageView = view.findViewById(R.id.back)


        backImageView.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, More())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}
