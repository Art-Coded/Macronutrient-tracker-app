package com.example.macrominder

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

class SplashFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val handler = Handler()
    private var isHandlerActive = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        auth = FirebaseAuth.getInstance()


        startNavigationDelay()
    }

    private fun startNavigationDelay() {

        if (!isHandlerActive) {
            handler.postDelayed({
                if (isAdded) {
                    if (auth.currentUser != null) {

                        findNavController().navigate(R.id.action_splashFragment_to_mainViewPagerFragment)
                    } else {

                        findNavController().navigate(R.id.action_splashFragment_to_viewPagerFragment)
                    }
                }
            }, 3000)
            isHandlerActive = true
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
        isHandlerActive = false
    }

    override fun onResume() {
        super.onResume()
        startNavigationDelay()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        isHandlerActive = false
    }
}
