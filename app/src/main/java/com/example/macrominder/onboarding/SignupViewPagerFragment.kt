package com.example.macrominder.signup


import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2
import com.example.macrominder.R
import com.example.macrominder.onboarding.ViewPagerAdapter
import com.example.macrominder.onboarding.signup.FirstSignup
import com.example.macrominder.onboarding.signup.FourthSignup
import com.example.macrominder.onboarding.signup.SecondSignup
import com.example.macrominder.onboarding.signup.ThirdSignup

class SignupViewPagerFragment : Fragment(R.layout.fragment_signup_view_pager) {

    lateinit var viewPager: ViewPager2
    private lateinit var dot1: View
    private lateinit var dot2: View
    private lateinit var dot3: View
    private lateinit var dot4: View
    private lateinit var backButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signup_view_pager, container, false)

        viewPager = view.findViewById(R.id.viewPager)
        dot1 = view.findViewById(R.id.dot1)
        dot2 = view.findViewById(R.id.dot2)
        dot3 = view.findViewById(R.id.dot3)
        dot4 = view.findViewById(R.id.dot4)
        backButton = view.findViewById(R.id.back)

        viewPager.isUserInputEnabled = false

        val fragmentList = arrayListOf<Fragment>(
            FirstSignup(),
            SecondSignup(),
            ThirdSignup(),
            FourthSignup()
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

        backButton.setOnClickListener {
            val currentPosition = viewPager.currentItem
            if (currentPosition > 0) {
                viewPager.setCurrentItem(currentPosition - 1, true)
            } else {
                activity?.onBackPressed()
            }
        }

        return view
    }

    private fun animateDot(dot: View, isActive: Boolean) {
        val scale = if (isActive) 1.5f else 1f
        val alpha = if (isActive) 1f else 0.5f
        val color = if (isActive) Color.parseColor("#72bd39") else Color.parseColor("#ffffff")

        val scaleXAnimator = ObjectAnimator.ofFloat(dot, "scaleX", scale)
        val scaleYAnimator = ObjectAnimator.ofFloat(dot, "scaleY", scale)
        val alphaAnimator = ObjectAnimator.ofFloat(dot, "alpha", alpha)

        val animatorSet = AnimatorSet()
        animatorSet.play(scaleXAnimator).with(scaleYAnimator).with(alphaAnimator)
        animatorSet.duration = 300
        animatorSet.start()
    }

    private fun updateDots(position: Int) {
        animateDot(dot1, position == 0)
        animateDot(dot2, position == 1)
        animateDot(dot3, position == 2)
        animateDot(dot4, position == 3)
    }
}
