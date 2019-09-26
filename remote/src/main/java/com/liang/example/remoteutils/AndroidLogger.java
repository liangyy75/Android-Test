package com.liang.example.remoteutils;

import android.util.Log;

public class AndroidLogger implements Logger {
    @Override
    public int v(Object tag, String msg, Throwable t, Object... args) {
        String msg2 = args.length == 0 ? msg : String.format(msg, args);
        return t != null ? Log.v(tag.toString(), msg2, t) : Log.v(tag.toString(), msg2);
    }

    @Override
    public int d(Object tag, String msg, Throwable t, Object... args) {
        String msg2 = args.length == 0 ? msg : String.format(msg, args);
        return t != null ? Log.d(tag.toString(), msg2, t) : Log.d(tag.toString(), msg2);
    }

    @Override
    public int i(Object tag, String msg, Throwable t, Object... args) {
        String msg2 = args.length == 0 ? msg : String.format(msg, args);
        return t != null ? Log.i(tag.toString(), msg2, t) : Log.i(tag.toString(), msg2);
    }

    @Override
    public int w(Object tag, String msg, Throwable t, Object... args) {
        String msg2 = args.length == 0 ? msg : String.format(msg, args);
        return t != null ? Log.w(tag.toString(), msg2, t) : Log.w(tag.toString(), msg2);
    }

    @Override
    public int e(Object tag, String msg, Throwable t, Object... args) {
        String msg2 = args.length == 0 ? msg : String.format(msg, args);
        return t != null ? Log.e(tag.toString(), msg2, t) : Log.e(tag.toString(), msg2);
    }

    @Override
    public int wtf(Object tag, String msg, Throwable t, Object... args) {
        String msg2 = args.length == 0 ? msg : String.format(msg, args);
        return t != null ? Log.wtf(tag.toString(), msg2, t) : Log.wtf(tag.toString(), msg2);
    }
}
