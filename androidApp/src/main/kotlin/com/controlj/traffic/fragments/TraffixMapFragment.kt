/*
 * Copyright (c) 2022.  Control-J Pty Ltd
 * All rights reserved
 */

package com.controlj.traffic.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.os.postDelayed
import com.controlj.view.RouteMapFragment

class TraffixMapFragment : RouteMapFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Handler(Looper.getMainLooper()).postDelayed(4000L) {
            val attView = view.findViewWithTag<View>("attrView")
            if (attView?.contentDescription?.contains("attribution") == true)
                attView.isEnabled = false
        }
    }
}