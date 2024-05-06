/*
 * Copyright (c) 2021.  Control-J Pty Ltd
 * All rights reserved
 */

package com.controlj.traffic.fragments

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.controlj.traffic.R
import com.controlj.traffic.presenter.TrafficPresenter
import com.controlj.view.ListFragment

class TrafficFragment : ListFragment<TrafficPresenter.Target, TrafficPresenter>() {
    override val presenter: TrafficPresenter = TrafficPresenter()

    private class TrafficView(private val view: View) : TrafficPresenter.TrafficView {

        private fun setText(id: Int, text: String) {
            view.findViewById<TextView>(id)?.text = text
        }

        override fun setCallsign(value: String) {
            setText(R.id.callsign, value)
        }

        override fun setDistance(value: String) {
            setText(R.id.distance, value)
        }

        override fun setAltitude(value: String) {
            setText(R.id.altitude, value)
        }

        override fun setType(value: String) {
            setText(R.id.type, value)
        }
    }

    override fun bindHeader(view: View, section: Int, textValues: List<String>) {
        TrafficView(view).setHeader()
    }

    override fun bindItem(view: View, item: TrafficPresenter.Target, textValues: List<String>) {
        TrafficView(view).set(item)
    }

    override fun getRowLayout(parent: ViewGroup, viewType: Int): View {
        return inflater.inflate(R.layout.traffic_entry, parent, false)
    }
}
