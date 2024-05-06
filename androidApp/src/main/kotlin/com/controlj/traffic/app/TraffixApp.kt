/*
 * Copyright (c) 2022.  Control-J Pty Ltd
 * All rights reserved
 */

package com.controlj.traffic.app

import com.controlj.appframework.BaseApplication
import com.controlj.framework.AndroidDeviceLocationProvider
import com.controlj.framework.AndroidSoundPlayer
import com.controlj.graphics.AndroidGraphicsFactory
import com.controlj.logging.CJLog
import com.controlj.logging.HttpLogger
import com.controlj.rx.MainScheduler
import com.controlj.settings.Properties
import com.controlj.traffic.BuildConfig
import com.controlj.traffic.R
import com.controlj.traffic.activity.TraffixActivity
import com.controlj.traffic.data.AppSettings
import com.controlj.ui.AndroidNotificationPresenter
import com.jakewharton.threetenabp.AndroidThreeTen
import com.mapbox.android.telemetry.TelemetryEnabler
import com.mapbox.mapboxsdk.Mapbox
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

open class TraffixApp : BaseApplication() {
    override val identifier: String = "Traffic"
    override val isDebug: Boolean = BuildConfig.DEBUG
    override val loggerHostName: String
        get() {
            val host = AppSettings.logHostSetting.value.trim()
            if(host.isBlank())
                return HttpLogger.SYSLOGGER
            return "http://$host/syslog.php"
        }

    override fun onCreate() {
        MainScheduler.instance = AndroidSchedulers.mainThread()
        super.onCreate()
        AndroidThreeTen.init(this)
        AndroidGraphicsFactory(this)
        //AndroidBleScanner(this)
        AndroidSoundPlayer
        CJLog.isDebug = BuildConfig.DEBUG
        // create a notficationpresenter
        AndroidNotificationPresenter(
            this,
            R.drawable.notification,
            TraffixActivity::class.java
        )
        AndroidDeviceLocationProvider(this)
        TelemetryEnabler.updateTelemetryState(TelemetryEnabler.State.DISABLED)
        Mapbox.getInstance(this, Properties.getProperty("mapboxToken"))
    }
}
