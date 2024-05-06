/*
 * Copyright (c) 2022.  Control-J Pty Ltd
 * All rights reserved
 */

package com.controlj.traffic.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.controlj.traffic.service.TraffixService


@ExperimentalUnsignedTypes
class NetworkReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val connMgr: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // is there a wifi network connected?
        val hasWiFi: Boolean =
            connMgr.allNetworks.any {
                connMgr.getNetworkCapabilities(it)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
            }

        val serviceIntent = Intent(context, TraffixService::class.java)
        if (hasWiFi) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else
                context.startService(serviceIntent)
        } else
            context.stopService(serviceIntent)
    }
}
