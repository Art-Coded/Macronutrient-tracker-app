package com.example.macrominder.Main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.macrominder.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

class MainViewPagerFragment : Fragment() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var fab: FloatingActionButton
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_bottomnav, container, false)

        auth = FirebaseAuth.getInstance()

        bottomNavigationView = view.findViewById(R.id.bottomNavigationView)
        fab = view.findViewById(R.id.fab)
        bottomNavigationView.background = null
        bottomNavigationView.menu.getItem(2).isEnabled = false

        if (savedInstanceState == null) {
            replaceFragment(Home())
        }

        fab.setOnClickListener {
            val bottomSheetFragment = BottomSheetFragment()
            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home2 -> {
                    replaceFragment(Home())
                    true
                }
                R.id.diary -> {
                    replaceFragment(Diary())
                    true
                }
                R.id.foods -> {
                    replaceFragment(Foods())
                    true
                }
                R.id.more -> {
                    replaceFragment(More())
                    true
                }
                else -> false
            }
        }


        return view
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            navigateToLogin()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().moveTaskToBack(true)
                }
            }
        )
    }

    private fun navigateToLogin() {
        parentFragmentManager.popBackStack()
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
