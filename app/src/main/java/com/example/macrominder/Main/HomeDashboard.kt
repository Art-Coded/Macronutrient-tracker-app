package com.example.macrominder.Main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.macrominder.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.viewpager2.adapter.FragmentStateAdapter

class HomeDashboard : Fragment() {

    private lateinit var birdDialogTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var mainContent: View
    private lateinit var viewPager: ViewPager2
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home_dashboard, container, false)


        birdDialogTextView = view.findViewById(R.id.BirdDialog)
        progressBar = view.findViewById(R.id.progressBar)
        mainContent = view.findViewById(R.id.mainContent)
        viewPager = view.findViewById(R.id.viewPager)


        viewPager.adapter = YouTubePagerAdapter(this)


        fetchAndSetUserName()

        return view
    }

    private fun fetchAndSetUserName() {

        progressBar.visibility = View.VISIBLE
        mainContent.visibility = View.GONE

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val userId = user.uid

            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.contains("name")) {
                        val name = document.getString("name")

                        birdDialogTextView.text =
                            "Hey there, $name! Make sure to check out the latest featured posts!"
                    } else {

                        birdDialogTextView.text =
                            "Hey there, User! Make sure to check out the latest featured posts!"
                    }

                    progressBar.visibility = View.GONE
                    mainContent.visibility = View.VISIBLE
                }
                .addOnFailureListener {

                    birdDialogTextView.text =
                        "Hey there, User! Make sure to check out the latest featured posts!"

                    progressBar.visibility = View.GONE
                    mainContent.visibility = View.VISIBLE
                }
        } else {

            birdDialogTextView.text =
                "Hey there, User! Make sure to check out the latest featured posts!"

            progressBar.visibility = View.GONE
            mainContent.visibility = View.VISIBLE
        }
    }

    private inner class YouTubePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int {
            return 4
        }

        override fun createFragment(position: Int): Fragment {
            return YouTubeVideoFragment.newInstance(getVideoId(position))
        }

        private fun getVideoId(position: Int): String {

            return when (position) {
                0 -> "aADukThvjXQ"
                else -> "aADukThvjXQ"
            }
        }
    }
}
