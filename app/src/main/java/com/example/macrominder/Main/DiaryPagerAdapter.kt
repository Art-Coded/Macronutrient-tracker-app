package com.example.macrominder.Main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class DiaryPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DiaryMacro1st()
            1 -> DiaryMacro2nd()
            2 -> DiaryMacro3rd()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}
