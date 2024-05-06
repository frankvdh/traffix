/*
 * Copyright (c) 2022.  Control-J Pty Ltd
 * All rights reserved
 */

package com.controlj.uplift.activity

import android.os.Build
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.controlj.framework.ApplicationBusy
import com.controlj.traffic.activity.TraffixActivity
import com.controlj.framework.ApplicationState
import com.controlj.rx.DisposedEmitter
import com.controlj.uplift.R
import com.controlj.view.RouteMapFragment
import com.mapbox.android.telemetry.TelemetryEnabler
import com.mapbox.mapboxsdk.Mapbox
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class WelcomeActivityTest {
    private lateinit var activityController: ActivityController<TraffixActivity>

    private val telemetrySlot = CapturingSlot<TelemetryEnabler.State>()
    private lateinit var mapbox: Mapbox

    @Before
    fun setUp() {
        mapbox = mockk()
        mockkStatic(Mapbox::class)
        every { Mapbox.getInstance(any(), any()) } returns mapbox
        every { Mapbox.hasInstance() } returns true
        mockkStatic(TelemetryEnabler::class)
        every { TelemetryEnabler.updateTelemetryState(capture(telemetrySlot)) } answers {
            telemetrySlot.captured
        }
        mockkObject(RouteMapFragment)
        every { RouteMapFragment.createInstance()} answers {
            Fragment()
        }

        activityController = Robolectric.buildActivity(TraffixActivity::class.java)
    }


    @Test
    fun shouldHaveWelcomeFragment() {
        var emitter : ObservableEmitter<Boolean> = DisposedEmitter()
        mockkObject(ApplicationState)
        val observer = Observable.create<Boolean>() {
            emitter = it
        }
        every { ApplicationBusy.listener } returns observer
        // check lifecycle events update application state

        activityController.create()
        val activity = activityController.get()
        assertFalse(ApplicationState.current.data.visible)
        activityController.start()
        assertNotNull(activity.supportFragmentManager.findFragmentByTag("routeMap"))
        assertTrue(ApplicationState.current.data.visible)
        assertFalse(ApplicationState.current.data.active)
        activityController.resume()
        assertTrue(ApplicationState.current.data.active)

        assertFalse(emitter.isDisposed)
        assertFalse(activity.findViewById<ProgressBar>(R.id.busyIndicator).visibility == View.VISIBLE)
        emitter.onNext(true)
        assertTrue(activity.findViewById<ProgressBar>(R.id.busyIndicator).visibility == View.VISIBLE)
        emitter.onNext(false)
        assertFalse(activity.findViewById<ProgressBar>(R.id.busyIndicator).visibility == View.VISIBLE)
        activityController.pause()
        assertFalse(ApplicationState.current.data.active)
        activityController.stop()
        assertFalse(ApplicationState.current.data.visible)

        // check that the subscription has been disposed
        assertTrue(emitter.isDisposed)
        unmockkObject(ApplicationState)
    }
}


