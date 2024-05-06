/*
 * Copyright (c) 2022.  Control-J Pty Ltd
 * All rights reserved
 */

package com.controlj.traffic.view

import com.controlj.data.Constants
import com.controlj.data.Location
import com.controlj.graphics.CColor
import com.controlj.location.GpsLocation
import com.controlj.location.gpsFix
import com.controlj.location.satellitesInView
import com.controlj.settings.AviationUnitSettings
import com.controlj.settings.UnitSetting
import com.controlj.utility.Units
import com.controlj.widget.Hamburger
import com.controlj.widget.LinearContainer
import com.controlj.widget.SettingValueView
import com.controlj.widget.SingleUpdater
import com.controlj.widget.SingleValued
import com.controlj.widget.SplitContainer
import com.controlj.widget.TextValueCview
import com.controlj.widget.TimeValue
import com.controlj.widget.Updater
import kotlin.math.abs

/**
 * Created by clyde on 22/3/18.
 */
open class RouteGroup : SplitContainer(1.0, Direction.AUTOMATIC, true) {

    companion object {
        val BACKGROUND = CColor.WHITE
    }

    private open class RouteValueField(label: String, setting: UnitSetting, precision: Int = 0) :
        SettingValueView(label, setting, precision = precision, backgroundColor = BACKGROUND)

    private open class TextField(label: String) :
        TextValueCview(label, backgroundColor = BACKGROUND),
        SingleValued<String> {
        override var targetValue: String
            get() = valueText
            set(value) {
                valueText = value
            }
    }

    private open class LatLonField(private val cards: CharArray) :
        TextValueCview(label = "", backgroundColor = CColor.WHITE), SingleValued<Double> {
        override var targetValue: Double = Constants.INVALID_DATA
            set(value) {
                field = value
                valueText = if (value == Constants.INVALID_DATA)
                    "----"
                else {
                    (if (value < 0) cards[1] else cards[0]) +
                        AviationUnitSettings.latLonSetting.value.toString(abs(value)).trim()
                }
            }
    }

    class TextUpdater<T>(
        private val b: SingleValued<String>,
        private val getter: (data: T) -> String
    ) : Updater<T> {
        override fun invalidate() {
            b.targetValue = "---"
        }

        override fun update(data: T) {
            b.targetValue = getter(data)
        }

    }


    // containers

    private val sideContainer = LinearContainer(Direction.AUTOMATIC, true)
    private val containers: Array<LinearContainer> =
        Array(4) { LinearContainer(Direction.VERTICAL) }
    private val utc = TimeValue()
    private val groundSpeed = RouteValueField("GS", AviationUnitSettings.speedUnitSetting)
    private val altitude = RouteValueField("ALT", AviationUnitSettings.altitudeUnitSetting)

    private val latitude = LatLonField(charArrayOf('N', 'S'))
    private val longitude = LatLonField(charArrayOf('E', 'W'))
    private val degreeSetting = UnitSetting.single("", Units.Unit.DEGREE)
    private val track = RouteValueField("TRK", degreeSetting)
    private val hamburger = Hamburger().apply {
        backgroundColor = CColor.WHITE
        horizontalAlignment = Alignment.CENTER
        verticalAlignment = VAlignment.MIDDLE
    }

    private val source = TextField("SRC")
    private val fix = TextField("GPS FIX")
    private val inView = TextField("SATS")

    private val locationUpdateList = mutableListOf<Updater<Location>>()
    private val gpsUpdateList = mutableListOf<TextUpdater<GpsLocation>>()

    init {
        sideContainer.backgroundColor = BACKGROUND
        sideContainer.name = "sideContainer"
        addView(sideContainer)
        backgroundColor = CColor.WHITE
        containers[0].addViews(utc, latitude, longitude, source)
        containers[1].addViews(track, groundSpeed, altitude)
        containers[2].addViews(fix, inView)

        locationUpdateList.add(SingleUpdater(groundSpeed) { it.speed })
        locationUpdateList.add(SingleUpdater(altitude) { it.altitude })
        locationUpdateList.add(SingleUpdater(latitude) { it.latitude })
        locationUpdateList.add(SingleUpdater(longitude) { it.longitude })
        locationUpdateList.add(SingleUpdater(track) { it.magneticTrack })
        locationUpdateList.add(TextUpdater(source) { it.creator.name })

        gpsUpdateList.add(TextUpdater(fix) { it.gpsFix.string })
        gpsUpdateList.add(TextUpdater(inView) { it.satellitesInView.toString() })

        sideContainer.addView(hamburger)
        containers.forEach { sideContainer.addView(it) }
    }

    fun invalidateUtc() {
        utc.redrawForeground()
    }

    fun invalidateData() {
        locationUpdateList.forEach { it.invalidate() }
        gpsUpdateList.forEach { it.invalidate() }
        requestRedraw()
    }

    override fun doLayout() {
        super.doLayout()
        hamburger.margin = bounds.width / 20
        when (sideContainer.computedDirection) {
            Direction.HORIZONTAL -> sideContainer.edges.clear()
            else -> sideContainer.edges.add(Edge.LEFT)
        }
    }

    fun update(data: Location) {
        locationUpdateList.forEach { it.update(data) }
        if (data is GpsLocation)
            gpsUpdateList.forEach { it.update(data) }

        source.valueText = data.creator.name
        requestRedraw()     // full redraw in case units have changed
    }
}
