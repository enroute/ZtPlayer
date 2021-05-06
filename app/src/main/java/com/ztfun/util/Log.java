package com.ztfun.util;

/**
 * Log utility compatible with android.util.Log, adds extra info on the <code>tag</code> field.
 */
public class Log {
    /**
     *  If is debug, then only warning and error messages are shown
     *  E.g. Set value in <code>
     *      Application.onCreate() {
     *          Log.isDebug = BuildConfig.DEBUG;
     *      }
     *  </code>
     */
    public static boolean isDebug = true;

    // logs with level higher than this one would be printed
    public static int logLevel = android.util.Log.VERBOSE;

    /**
     * Customizable log TAG (or prefix) by default, which will be replaced if called with methods
     * containing <code>tag</code>, e.g. <code>public static void v(String tag, String msg)</code>.
     */
    public static String TAG = "ZT";

    public static void v(String msg) {
        log(android.util.Log.VERBOSE, null, msg, null);
    }

    public static void v(String tag, String msg) {
        log(android.util.Log.VERBOSE, tag, msg, null);
    }

    public static void v(String msg, Throwable t) {
        log(android.util.Log.VERBOSE, null, msg, t);
    }

    public static void v(String tag, String msg, Throwable t) {
        log(android.util.Log.VERBOSE, tag, msg, t);
    }

    public static void d(String msg) {
        log(android.util.Log.DEBUG, null, msg, null);
    }

    public static void d(String tag, String msg) {
        log(android.util.Log.DEBUG, tag, msg, null);
    }

    public static void d(String msg, Throwable t) {
        log(android.util.Log.DEBUG, null, msg, t);
    }

    public static void d(String tag, String msg, Throwable t) {
        log(android.util.Log.DEBUG, tag, msg, t);
    }

    public static void i(String msg) {
        log(android.util.Log.INFO, null, msg, null);
    }

    public static void i(String tag, String msg) {
        log(android.util.Log.INFO, tag, msg, null);
    }

    public static void i(String msg, Throwable t) {
        log(android.util.Log.INFO, null, msg, t);
    }

    public static void i(String tag, String msg, Throwable t) {
        log(android.util.Log.INFO, tag, msg, t);
    }

    public static void w(String msg) {
        log(android.util.Log.WARN, null, msg, null);
    }

    public static void w(String tag, String msg) {
        log(android.util.Log.WARN, tag, msg, null);
    }

    public static void w(String msg, Throwable t) {
        log(android.util.Log.WARN, null, msg, t);
    }

    public static void w(String tag, String msg, Throwable t) {
        log(android.util.Log.WARN, tag, msg, t);
    }

    public static void e(String msg) {
        log(android.util.Log.ERROR, null, msg, null);
    }

    public static void e(String tag, String msg) {
        log(android.util.Log.ERROR, tag, msg, null);
    }

    public static void e(String msg, Throwable t) {
        log(android.util.Log.ERROR, null, msg, t);
    }

    public static void e(String tag, String msg, Throwable t) {
        log(android.util.Log.ERROR, tag, msg, t);
    }

    // don't make it public, otherwise the generateTag would return an unexpected caller
    private static void log(int level, String tag, String msg, Throwable t) {
        if (level < logLevel ||                     // don't show lower levels in both debug and release mode
                ((!isDebug) && level < android.util.Log.WARN)) { // only levels higher than Log.WARN in release mode
            return;
        }

        if (msg == null) {
            msg = "(null)";
        }

        if (t != null) {
            msg += '\n' + android.util.Log.getStackTraceString(t);
        }

        android.util.Log.println(level, generateTag(tag), msg);
    }

    private static String generateTag(String tag) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[5];
        return (tag == null ? TAG : tag) + ":(" + caller.getFileName() + ":" + caller.getLineNumber() + ")." + caller.getMethodName();
    }
}
