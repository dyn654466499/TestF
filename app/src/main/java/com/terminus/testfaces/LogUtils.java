package com.terminus.testfaces;

import android.util.Log;

import java.util.HashMap;
import java.util.Set;

public class LogUtils {
    private final static int LOG_LENGTH_LINE = 100;
    private final static int LOG_LENGTH = 3500;

    private final static boolean DEBUG = true;

    private final static String TAG = "x5-cm";

    private static String formatMsg(String msg) {
        int count = (msg.length() - 1) / LOG_LENGTH_LINE;
        String logMsg = "";
        for (int i = 0; i < count; i++) {
            logMsg = logMsg + msg.substring(i * LOG_LENGTH_LINE, (i + 1) * LOG_LENGTH_LINE) + "\n";
        }
        return logMsg + msg.substring(count * LOG_LENGTH_LINE, msg.length());
    }

    public static void i(String msg) {
        i(TAG, msg);
    }

    public static void i(String tag, String msg) {
        if (DEBUG && msg != null) {
            int count = (msg.length() - 1) / LOG_LENGTH;
            for (int i = 0; i < count; i++) {
                Log.i(tag, formatMsg(msg.substring(i * LOG_LENGTH, (i + 1) * LOG_LENGTH)));
            }
            Log.i(tag, formatMsg(msg.substring(count * LOG_LENGTH, msg.length())));
        }
    }

    public static void d(String msg) {
        d(TAG, msg);
    }

    public static void d(String tag, String msg) {
        if (DEBUG && msg != null) {
            int count = (msg.length() - 1) / LOG_LENGTH;
            for (int i = 0; i < count; i++) {
                Log.d(tag, formatMsg(msg.substring(i * LOG_LENGTH, (i + 1) * LOG_LENGTH)));
            }
            Log.d(tag, formatMsg(msg.substring(count * LOG_LENGTH, msg.length())));
        }
    }

    public static void e(String msg) {
        e(TAG, msg);
    }

    public static void e(String tag, String msg) {
        if (DEBUG && msg != null) {
            int count = (msg.length() - 1) / LOG_LENGTH;
            for (int i = 0; i < count; i++) {
                Log.e(tag, formatMsg(msg.substring(i * LOG_LENGTH, (i + 1) * LOG_LENGTH)));
            }
            Log.e(tag, formatMsg(msg.substring(count * LOG_LENGTH, msg.length())));
        }
    }

    public static void e(Throwable tr) {
        e(TAG, "", tr);
    }

    public static void e(String tag, Throwable tr) {
        e(tag, "", tr);
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (DEBUG && msg != null) {
            int count = (msg.length() - 1) / LOG_LENGTH;
            for (int i = 0; i < count; i++) {
                Log.e(tag, formatMsg(msg.substring(i * LOG_LENGTH, (i + 1) * LOG_LENGTH)), tr);
            }
            Log.e(tag, formatMsg(msg.substring(count * LOG_LENGTH, msg.length())), tr);
        }
    }

    public static void i(String tag, HashMap<String, String> map) {
        if (DEBUG && map != null) {
            Set<String> keys = map.keySet();
            for (String key : keys) {
                Log.i(tag, key + "--" + map.get(key));
            }
        }
    }
}
