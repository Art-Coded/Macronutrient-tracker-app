package com.example.macrominder.onboarding.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.example.macrominder.R

class FirstScreen : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_first_screen, container, false)


        val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)


        view.findViewById<TextView>(R.id.button2).setOnClickListener {
            viewPager?.currentItem = 1
        }

        return view
    }
}
