/*
 * Copyright (c) 2021.  Control-J Pty Ltd
 * All rights reserved
 */

package com.controlj.traffic.data

import com.controlj.logging.CJLog
import com.controlj.settings.AviationUnitSettings
import com.controlj.settings.IntSetting
import com.controlj.settings.Setting
import com.controlj.settings.StringSetting
import com.controlj.stratux.Stratux
import com.controlj.traffic.TrafficSettings
import com.controlj.ui.SettingsDialogData

object AppSettings {

    private val avGroup = Setting.Group("Aviation Units").apply {
        add(with(AviationUnitSettings) {
            listOf(
                distanceUnitSetting,
                speedUnitSetting,
                altitudeUnitSetting,
                vertSpeedUnitSetting,
                latLonSetting,
                timeUnitSetting,
                oatUnitSetting,
            )
        })
    }
    val flarmPortSetting = IntSetting(
        "flarmPort", "Flarm UDP port",
        "UDP port on which to broadcast Flarm data",
        4353
    )
    val logHostSetting = StringSetting(
        "logHost", "Debug log hostname",
        "Name of host to receive debug logs - applied after restart"
    )
    private val generalGroup = Setting.Group(
        "General",
        Stratux.portSetting,
        flarmPortSetting,
    )

    private val debugGroup = Setting.Group(
        "Debug",
        logHostSetting
    )
    private val groups = mutableListOf(generalGroup, TrafficSettings.group, avGroup).apply {
        if (CJLog.isDebug)
            this.add(debugGroup)
    }

    val dialogData: SettingsDialogData by lazy { SettingsDialogData("Settings", groups) }
}
