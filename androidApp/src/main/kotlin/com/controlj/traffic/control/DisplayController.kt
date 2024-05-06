/*
 * Copyright (c) 2022.  Control-J Pty Ltd
 * All rights reserved
 */

package com.controlj.traffic.control

import com.controlj.location.LocationProvider
import com.controlj.location.LocationSource
import com.controlj.logging.CJLog.logException
import com.controlj.logging.CJLog.logMsg
import com.controlj.rx.finaliseUI
import com.controlj.rx.observeOnMainBy
import com.controlj.stratux.Stratux
import com.controlj.stratux.gdl90.StratuxStatus
import com.controlj.traffic.view.RouteGroup
import com.controlj.widget.MessageBank
import com.controlj.widget.MessageView
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * Drives the visible display
 *
 */
object DisplayController {
    val messageBank = MessageBank()
    val messageView = MessageView(messageBank)
    val routeGroup = RouteGroup()

    private val disposables = CompositeDisposable()

    /**
     * Invalidate displayed data
     */

    fun onStop() {
        disposables.clear()
        LocationSource.remove(LocationProvider.deviceProvider)
    }

    fun onStart() {
        disposables.clear()
        logMsg("onStart")
        disposables.add(messageBank.observable.observeOnMainBy {
            messageView.requestRedraw()
        })
        messageView.requestRedraw()
        disposables.add(Observable.interval(0L, 1L, TimeUnit.SECONDS)
            .observeOnMainBy { routeGroup.invalidateUtc() }
        )
        LocationSource.add(LocationProvider.deviceProvider)
        disposables.add(LocationSource.observer
            .finaliseUI(routeGroup::invalidateData)
            .observeOnMainBy { data ->
                try {
                    routeGroup.update(data)
                } catch (ex: Exception) {
                    logException(ex)
                }
            }
        )
    }
}
