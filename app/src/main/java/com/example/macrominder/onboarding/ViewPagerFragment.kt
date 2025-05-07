package com.example.macrominder.onboarding

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color // Add this import statement
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.macrominder.R
import com.example.macrominder.onboarding.screens.FirstScreen
import com.example.macrominder.onboarding.screens.SecondScreen
import com.example.macrominder.onboarding.screens.ThirdScreen

class ViewPagerFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var dot1: View
    private lateinit var dot2: View
    private lateinit var dot3: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_pager, container, false)

        viewPager = view.findViewById(R.id.viewPager)
        dot1 = view.findViewById(R.id.dot1)
        dot2 = view.findViewById(R.id.dot2)
        dot3 = view.findViewById(R.id.dot3)

        val fragmentList = arrayListOf<Fragment>(
            FirstScreen(),
            SecondScreen(),
            ThirdScreen()
        )

        val adapter = ViewPagerAdapter(
            fragmentList,
            requireActivity().supportFragmentManager,
            lifecycle
        )

        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
            }
        })

        updateDots(0)

        return view
    }

    private fun animateDot(dot: View, isActive: Boolean) {
        val scale = if (isActive) 1.5f else 1f
        val alpha = if (isActive) 1f else 0.5f
        val color = if (isActive) Color.parseColor("#72bd39") else Color.parseColor("#ffffff")

        val scaleXAnimator = ObjectAnimator.ofFloat(dot, "scaleX", scale)
        val scaleYAnimator = ObjectAnimator.ofFloat(dot, "scaleY", scale)
        val alphaAnimator = ObjectAnimator.ofFloat(dot, "alpha", alpha)

        val colorAnimator = ObjectAnimator.ofArgb(dot.background, "#72bd39", Color.parseColor("#ffffff"), color)

        val animatorSet = AnimatorSet()
        animatorSet.play(scaleXAnimator).with(scaleYAnimator).with(alphaAnimator)
        animatorSet.duration = 300
        animatorSet.start()
    }

    private fun updateDots(position: Int) {
        animateDot(dot1, position == 0)
        animateDot(dot2, position == 1)
        animateDot(dot3, position == 2)
    }
}
