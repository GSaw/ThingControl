package org.moontools.thingcontrol;

import android.util.Log;

import org.moontools.thingcontrol.controls.TCDisplay;

/**
 * Created by georg on 14.11.16.
 */

public class LogMessages {
    private static TCDisplay DISPLAY = null;
    private static boolean errorLogged = false;

    public static void registerDisplay(TCDisplay display) {
        DISPLAY = display;
    }
    public static void log(String message) {
        DISPLAY.appendText(message);
        Log.d(LogMessages.class.getName(), message);
    }

    public static void err(String message) {
        errorLogged = true;
        Log.e(LogMessages.class.getName(), "Error!: " + message);
        DISPLAY.appendText(message);
    }

    public static boolean isErrorLogged() {
        Log.d(LogMessages.class.getName(), "errorLogged = " + errorLogged);
        return errorLogged;
    }
}
