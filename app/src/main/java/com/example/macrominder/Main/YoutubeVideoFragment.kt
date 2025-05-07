package com.example.macrominder.Main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.webkit.WebSettings
import android.webkit.WebView
import com.example.macrominder.R

class YouTubeVideoFragment : Fragment() {

    private lateinit var webView: WebView

    companion object {
        private const val VIDEO_ID_KEY = "video_id"

        fun newInstance(videoId: String): YouTubeVideoFragment {
            val fragment = YouTubeVideoFragment()
            val bundle = Bundle()
            bundle.putString(VIDEO_ID_KEY, videoId)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_youtube_video, container, false)

        webView = view.findViewById(R.id.webView)

        val videoId = arguments?.getString(VIDEO_ID_KEY) ?: ""
        loadYouTubeVideo(videoId)

        return view
    }

    private fun loadYouTubeVideo(videoId: String) {
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true

        val videoUrl = "https://www.youtube.com/embed/$videoId"
        webView.loadUrl(videoUrl)
    }
}
