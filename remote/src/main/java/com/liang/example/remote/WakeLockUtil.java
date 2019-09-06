package com.liang.example.remote;

import android.content.Context;
import android.os.PowerManager;

public class WakeLockUtil {
    private static PowerManager.WakeLock wakeLock;

    public static void lock(Context context, String tag, long timeout) {
        if (wakeLock != null) {
            return;
        }
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag);
        }
        wakeLock.acquire(timeout);
    }

    public static void release() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
    }
}
