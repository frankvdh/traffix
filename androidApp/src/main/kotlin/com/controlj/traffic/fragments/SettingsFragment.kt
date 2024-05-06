/*
 * Copyright (c) 2021.  Control-J Pty Ltd
 * All rights reserved
 */

package com.controlj.traffic.fragments

import android.os.Bundle
import com.controlj.settings.SettingsModel
import com.controlj.traffic.data.AppSettings
import com.controlj.view.DialogDataFragment

class SettingsFragment : DialogDataFragment<SettingsModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = SettingsModel(AppSettings.dialogData)
    }
}
