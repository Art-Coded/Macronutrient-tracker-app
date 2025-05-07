package com.example.macrominder.Main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.macrominder.R
import com.example.macrominder.onboarding.signup.FourthSignup

class TOSsignup : Fragment() {

    private lateinit var backImageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.t_t_o_ssignup, container, false)

        backImageView = view.findViewById(R.id.back)

        backImageView.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FourthSignup())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}
