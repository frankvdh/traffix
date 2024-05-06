/*
 * Copyright (c) 2022.  Control-J Pty Ltd
 * All rights reserved
 */

package com.controlj.traffic.activity

//import android.app.PendingIntent.FLAG_MUTABLE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.controlj.traffic.service.TraffixService

@ExperimentalUnsignedTypes
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            TraffixService.startService(context)
        }
    }
}
