/*
 * Copyright (c) 2022.  Control-J Pty Ltd
 * All rights reserved
 */

package com.controlj.traffic.ui

import com.controlj.logging.CJLog
import com.controlj.ui.ButtonDialogItem
import com.controlj.ui.DialogData
import com.controlj.ui.DialogItem
import com.controlj.ui.UiAction
import com.controlj.widget.Hamburger

object MainMenu : DialogData(Type.MODAL, "TraffiX") {
    enum class Action(
        val title: String,
        val image: String = "",
        val enabled: () -> Boolean = { true },
        val color: () -> Int = { 0 },
        val action: (UiAction, DialogItem?) -> Unit = { actionItem, _ -> UiAction.sendAction(actionItem) },
    ) : UiAction {
        Map("Map", "menu_map"),
        Settings("Settings", "menu_settings"),
        Traffic("Traffic", "menu_traffic"),
        Help("Help", "menu_help"),
        Exit("Shutdown", "menu_exit")
    }


    /**
     * An object to hang the open menu action off.
     */

    init {

        Action.values().forEach { menuAction ->
            addItem(object : ButtonDialogItem(
                menuAction.title,
                image = menuAction.image,
                action = { CJLog.logMsg("action ${menuAction.title}"); menuAction.action(menuAction, it); true }
            ) {
                @Suppress("UNUSED_PARAMETER")
                override var isEnabled: Boolean
                    get() = menuAction.enabled.invoke()
                    set(value) {}
            })
        }
    }
}
