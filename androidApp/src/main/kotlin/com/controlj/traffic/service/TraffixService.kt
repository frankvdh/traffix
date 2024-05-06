/*
 * Copyright (c) 2022.  Control-J Pty Ltd
 * All rights reserved
 */

package com.controlj.traffic.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.controlj.location.LocationSource
import com.controlj.logging.CJLog.debug
import com.controlj.logging.CJLog.logException
import com.controlj.logging.CJLog.logMsg
import com.controlj.nmea.FlarmGenerator
import com.controlj.nmea.UdpBroadcaster
import com.controlj.rx.observeBy
import com.controlj.stratux.Stratux
import com.controlj.stratux.StratuxTraffic
import com.controlj.traffic.R
import com.controlj.traffic.TrafficSource
import com.controlj.traffic.activity.TraffixActivity
import com.controlj.traffic.data.AppSettings
import com.google.gson.Gson
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.IOException
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit

@ExperimentalUnsignedTypes
class TraffixService : Service() {

    companion object {
        const val CHANNEL_ID = "TraffiX.notification"
        const val NOTIFICATION_ID = 4370
        var instance: TraffixService? = null
            private set
        private val pingPorts = listOf(63093, 47578)

        /**
         * Start the service.
         */
        fun startService(context: Context) {
            val serviceIntent = Intent(context, TraffixService::class.java)
            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(serviceIntent)
            } else
                context.startService(serviceIntent)
        }

        fun stopService(context: Context) {
            context.stopService(Intent(context, TraffixService::class.java))
        }
    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private val connMgr: ConnectivityManager by lazy {
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private val wifiMgr: WifiManager by lazy {
        getSystemService(Context.WIFI_SERVICE) as WifiManager
    }
    private val broadcaster: UdpBroadcaster by lazy {
        UdpBroadcaster(address = "127.0.0.1")
    }
    private val channelId: String by lazy {
        if (Build.VERSION.SDK_INT >= 26) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = descriptionText
            notificationManager.createNotificationChannel(mChannel)
        }
        CHANNEL_ID
    }

    inner class LocalBinder : Binder() {
        val service: TraffixService = this@TraffixService
    }

    private val binder: IBinder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder {
        logMsg("Service bound")
        return binder
    }

    private fun getNotification(message: String = "running"): Notification {
        val flag = if(Build.VERSION.SDK_INT >= 23) FLAG_IMMUTABLE else 0
        val title = resources.getString(R.string.app_name)
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle("$title is $message")
            .setOnlyAlertOnce(true)
            .setContentIntent(
                Intent(this, TraffixActivity::class.java).let { notificationIntent ->
                    PendingIntent.getActivity(this, 0, notificationIntent, flag)
                })
        //if (withSound)
        //builder.setSound(Uri.parse("android.resource://$packageName/raw/${Priority.ACTION.sound}"))
        return builder.build()
    }

    private val stickyNotification by lazy { getNotification() }


    private val networkListener29 = object : NetworkCallback() {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun onAvailable(network: Network) {
            debug {
                val ssid =
                    (connMgr.getNetworkCapabilities(network)?.transportInfo as WifiInfo?)?.ssid
                "Connected to SSID $ssid"
            }
            startup()
        }
    }

    private val networkBroadcastReceiver = object : BroadcastReceiver() {
        @Suppress("DEPRECATION")
        override fun onReceive(context: Context?, intent: Intent?) {
            debug {
                "Connected to SSID ${wifiMgr.connectionInfo.ssid}"
            }
            startup()
        }
    }


    override fun onCreate() {
        instance = this
        startForeground(NOTIFICATION_ID, stickyNotification)
        startup()

        // ensure we get started whenever the network changes

        if (Build.VERSION.SDK_INT >= 29) {
            val builder = NetworkRequest.Builder()
            builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            connMgr.registerNetworkCallback(builder.build(), networkListener29)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(
                networkBroadcastReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
        logMsg("TraffiX Service created")

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logMsg("TraffiX Service started")
        return START_STICKY
    }

    private var disposables = CompositeDisposable()

    /**
     * Periodically send out pings to notify GDL90 sources that we want their data
     */

    private var lastPinged = ""

    private fun pingSources() {
        val json = Gson()
        val broadcaster = UdpBroadcaster(address = "255.255.255.255")
        disposables.add(
            Observable.interval(0, 10, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .observeBy(
                    {   // error
                        broadcaster.close()
                    },
                    {   // termination
                        broadcaster.close()
                    },
                ) {
                    val data = json.toJson(
                        mapOf(
                            "App" to "TraffiX",
                            "GDL90" to mapOf("port" to Stratux.portSetting.value)
                        )
                    ).toByteArray()
                    try {
                        val works = NetworkInterface.getNetworkInterfaces().toList()
                            .mapNotNull { it?.interfaceAddresses }
                            .flatten()
                            .mapNotNull { it.broadcast }
                            .toList()
                        if (works.toString() != lastPinged) {
                            logMsg("Pinging broadcast addresses $works")
                            lastPinged = works.toString()
                            works.forEach { address ->
                                pingPorts.forEach { port ->
                                    broadcaster.broadcast(data, address, port)
                                }
                            }
                        }
                    } catch (ex: IOException) {
                        logException(ex)
                    }
                }
        )
    }

    private fun startup() {
        disposables.clear()
        LocationSource.add(Stratux)
        TrafficSource.add(StratuxTraffic)
        pingSources()
        disposables.add(FlarmGenerator.observer
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .observeBy(
                {
                    broadcaster.close()
                    logException(it)
                },
                {
                    broadcaster.close()
                }
            ) {
                if (it.isNotBlank()) {
                    //logMsg(it)
                    broadcaster.broadcast(
                        it.toByteArray(),
                        p = AppSettings.flarmPortSetting.value
                    )
                }
            }
        )
    }

    private fun shutdown() {
        disposables.clear()
        LocationSource.remove(Stratux)
        TrafficSource.remove(StratuxTraffic)
    }

    override fun onDestroy() {
        logMsg("TraffiX service destroyed")
        shutdown()
        instance = null
    }
}
