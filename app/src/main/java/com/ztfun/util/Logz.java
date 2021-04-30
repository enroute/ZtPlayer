package com.ztfun.util;

import android.util.Log;

import com.ztfun.ztplayer.BuildConfig;

import java.util.Locale;

public class Logz {
    // if is debug, then only warning and error messages are shown
    public static boolean isDebug = BuildConfig.DEBUG;

    // logs with level higher than this one would be printed
    public static int logLevel = Log.VERBOSE;

    // log TAG (or prefix) can be customized by caller, for search convenience
    public static String TAG = "ZT";

    public static void v(String msg) {
        log(Log.VERBOSE, msg, null);
    }

    public static void v(String msg, Throwable t) {
        log(Log.VERBOSE, msg, t);
    }

    public static void d(String msg) {
        log(Log.DEBUG, msg, null);
    }

    public static void d(String msg, Throwable t) {
        log(Log.DEBUG, msg, t);
    }

    public static void i(String msg) {
        log(Log.INFO, msg, null);
    }

    public static void i(String msg, Throwable t) {
        log(Log.INFO, msg, t);
    }

    public static void w(String msg) {
        log(Log.WARN, msg, null);
    }

    public static void w(String msg, Throwable t) {
        log(Log.WARN, msg, t);
    }

    public static void e(String msg) {
        log(Log.ERROR, msg, null);
    }

    public static void e(String msg, Throwable t) {
        log(Log.ERROR, msg, t);
    }

    // don't make it public, otherwise the generateTag would return an unexpected caller
    private static void log(int level, String msg, Throwable t) {
        if (level < logLevel ||                     // don't show lower levels in both debug and release mode
                ((!isDebug) && level < Log.WARN)) { // only levels higher than Log.WARN in release mode
            return;
        }

        if (msg == null) {
            msg = "(null)";
        }

        if (t != null) {
            msg += '\n' + Log.getStackTraceString(t);
        }

        Log.println(level, generateTag(), msg);
    }

    private static String generateTag() {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[5];
        return TAG + ":(" + caller.getFileName() + ":" + caller.getLineNumber() + ")." + caller.getMethodName();
    }
}
