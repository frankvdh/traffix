/*
 * Copyright (c) 2022.  Control-J Pty Ltd
 * All rights reserved
 */

package com.controlj.traffic.fragments


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.controlj.framework.ApplicationState
import com.controlj.logging.CJLog
import com.controlj.traffic.R

/**
 */
class HelpFragment : Fragment() {

    private lateinit var webView: WebView
    private lateinit var toolBar: Toolbar

    private var isBusy: Boolean = false
        set(value) {
            field = value
            ApplicationState.setBusy(this::class.java.simpleName, value)
        }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_about, container, false)
        webView = view.findViewById(R.id.webview)
        toolBar = view.findViewById(R.id.toolbar)
        view.findViewById<TextView>(R.id.version).text =
            "Version ${CJLog.versionString} build ${CJLog.buildNumber}"
        toolBar.setNavigationOnClickListener { activity?.onBackPressedDispatcher?.onBackPressed() }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                isBusy = false
            }
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        webView.settings.javaScriptEnabled = false
        isBusy = true
        webView.loadUrl("file:///android_asset/html/about.html")
    }
}
