package com.multitv.multitvcommonsdk.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by root on 28/4/16.
 */
public class ToastMessage {

    public enum LogType {
        DEBUG, ERROR, WARNING, INFORMATION;
    }

    public static void showToastMsg(Context ctx, String msg, int duration) {
        if (Constant.getInstance().isEnableDebug()) {
            Toast.makeText(ctx, msg, duration).show();
        }
    }

    public static void showLogs(LogType type, String tag, String msg) {
        if (Constant.getInstance().isEnableDebug()) {
            if (msg != null && msg.trim().length() > 0)
                if (type == LogType.DEBUG) {
                    Log.d(tag, msg);
                } else if (type == LogType.ERROR) {
                    Log.e(tag, msg);
                } else if (type == LogType.WARNING) {
                    Log.w(tag, msg);
                } else {
                    Log.i(tag, msg);
                }
        }
    }
}