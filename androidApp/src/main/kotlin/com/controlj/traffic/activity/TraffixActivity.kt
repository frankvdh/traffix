/*
 * Copyright (c) 2022.  Control-J Pty Ltd
 * All rights reserved
 */

package com.controlj.traffic.activity

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.controlj.framework.ApplicationBusy
import com.controlj.framework.ApplicationState
import com.controlj.graphics.CRect
import com.controlj.logging.CJLog
import com.controlj.settings.Properties
import com.controlj.traffic.R
import com.controlj.traffic.control.DisplayController
import com.controlj.traffic.fragments.HelpFragment
import com.controlj.traffic.fragments.SettingsFragment
import com.controlj.traffic.fragments.TrafficFragment
import com.controlj.traffic.fragments.TraffixMapFragment
import com.controlj.traffic.service.TraffixService
import com.controlj.traffic.ui.MainMenu
import com.controlj.ui.DialogData
import com.controlj.ui.DialogItem
import com.controlj.ui.UiAction
import com.controlj.view.AndroidMenu
import com.controlj.view.CJNavView
import com.controlj.view.CViewWrapper
import com.controlj.view.ForegroundActivity
import com.controlj.view.MainActivity
import com.controlj.view.MenuEntry
import com.controlj.view.RouteMapFragment
import com.controlj.view.SlideViewLayout
import com.controlj.view.SlideViewPresenter
import com.controlj.widget.Hamburger.Companion.CloseMenu
import com.controlj.widget.Hamburger.Companion.OpenMenu
import com.controlj.widget.Hamburger.Companion.ToggleMenu
import com.mapbox.android.telemetry.TelemetryEnabler
import com.mapbox.mapboxsdk.Mapbox
import io.reactivex.rxjava3.core.Single


class TraffixActivity : ForegroundActivity(), MainActivity {

    companion object {
        val actionMap = mapOf(
            MainMenu.Action.Traffic to TrafficFragment::class.java,
            MainMenu.Action.Help to HelpFragment::class.java,
            MainMenu.Action.Settings to SettingsFragment::class.java
        )
    }

    private val enables = ArrayList<() -> Unit>()
    private lateinit var statusView: CViewWrapper
    private lateinit var trafficView: CViewWrapper
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var slideView: SlideViewLayout
    private lateinit var menuDrawer: CJNavView
    private lateinit var leftAnchor: View
    private lateinit var busyIndicator: View

    override val useAnimation =
        false      // subclasses should override this if animation is desired

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.traffic_activity)
        busyIndicator = findViewById(R.id.busyIndicator)
        leftAnchor = findViewById(R.id.left_anchor)
        slideView = findViewById(R.id.slideview)
        menuDrawer = findViewById(R.id.menuDrawer)
        statusView = findViewById(R.id.navview)
        trafficView = findViewById(R.id.soarview)
        drawerLayout = findViewById(R.id.drawerLayout)
        statusView.cView = DisplayController.routeGroup
        supportFragmentManager.beginTransaction().add(
            R.id.fragmentContainer,
            TraffixMapFragment(),
            "routeMap"
        ).commit()
        MainMenu.items.forEach { item ->
            val menuEntry = menuDrawer.add(item.text, getImageId(item.image)) {
                drawerLayout.close()
                item.action?.invoke(item)
            }
            menuEntry.isEnabled = item.isEnabled
            enables += {
                menuEntry.isEnabled = item.isEnabled
                menuEntry.title = item.text
            }
        }

        MainMenu.shower = object : DialogData.DialogShower {
            override fun showMenu(title: String, items: List<MenuEntry>, source: DialogItem?) {
                AndroidMenu.showMenu(
                    this@TraffixActivity,
                    title,
                    items,
                    leftAnchor,
                    Gravity.END or Gravity.CENTER_VERTICAL
                )
            }

            override fun showTextBox(
                title: String,
                value: String,
                style: DialogItem.InputStyle,
                source: CRect,
                timeout: Long
            ): Single<String> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun refresh() {
                enables.forEach { it() }
            }

            override fun dismiss() {
                drawerLayout.close()
            }
        }
        TraffixService.startService(this)
    }

    override fun onDestroy() {
        // Note: the service is not stopped here.
        super.onDestroy()
        DisplayController.routeGroup.parent = null
    }

    private val mapShowing: Boolean
        get() = supportFragmentManager.backStackEntryCount == 0

    private fun showFragment(clazz: Class<out Fragment>) {
        CJLog.debug("Showing fragment ${clazz.simpleName}")
        if (!mapShowing && supportFragmentManager.getBackStackEntryAt(0).javaClass == clazz) {
            slideView.invalidate()
            slideView.presenter.state = SlideViewPresenter.State.Normal
            return
        }
        if (mapShowing)
            slideView.presenter.pushState()
        else
            supportFragmentManager.popBackStackImmediate()
        val fragment = clazz.newInstance()
        slideView.presenter.state = SlideViewPresenter.State.Normal
        val transaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.animator.slide_in_left,
                R.animator.slide_out_left,
                R.animator.slide_in_left,
                R.animator.slide_out_left
            )
            .add(R.id.fragmentContainer, fragment, fragment.javaClass.name)
        if (mapShowing)
            transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun processAction(action: UiAction) {
        when (action) {
            OpenMenu -> drawerLayout.open()
            CloseMenu -> drawerLayout.close()
            ToggleMenu -> if (drawerLayout.isOpen())
                drawerLayout.close()
            else
                drawerLayout.open()

            MainMenu.Action.Map -> {
                if (!mapShowing)
                    supportFragmentManager.popBackStackImmediate()
                slideView.presenter.state = SlideViewPresenter.State.Normal
            }
            MainMenu.Action.Exit -> {
                TraffixService.stopService(this)
                finish()
                ApplicationState.current.data = ApplicationState.Stopped
            }
            is MainMenu.Action -> actionMap[action]?.let { showFragment(it) }

            else -> super.processAction(action)
        }
    }

    private fun setBusy(state: Boolean) {
        if (state) {
            busyIndicator.visibility = View.VISIBLE
            busyIndicator.bringToFront()
        } else
            busyIndicator.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()
        disposables.add(ApplicationBusy.listener.subscribe { setBusy(it) })
        DisplayController.onStart()
    }

    override fun onStop() {
        DisplayController.onStop()
        super.onStop()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(InputMethodManager::class.java)
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    // getIdentifier() is discouraged, but is used here to enable images to be found by name, rather
    // than compiled ids. This allows portability of code between Android and iOS.
    @SuppressLint("DiscouragedApi")
    private fun getImageId(name: String): Int {
        val result = resources.getIdentifier(name.lowercase(), "drawable", packageName)
        if (result == 0) error("Did not find resource drawable \"$name\"")
        return result
    }
}
