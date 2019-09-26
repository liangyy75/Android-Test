package com.liang.example.remoteutils;

public interface Logger {
    default int v(Object tag, Throwable t) {
        return v(tag, "", t);
    }

    default int d(Object tag, Throwable t) {
        return d(tag, "", t);
    }

    default int i(Object tag, Throwable t) {
        return i(tag, "", t);
    }

    default int w(Object tag, Throwable t) {
        return w(tag, "", t);
    }

    default int e(Object tag, Throwable t) {
        return e(tag, "", t);
    }

    default int wtf(Object tag, Throwable t) {
        return wtf(tag, "", t);
    }

    default int v(Object tag, Object... args) {
        return v(tag, "", null, args);
    }

    default int d(Object tag, Object... args) {
        return d(tag, "", null, args);
    }

    default int i(Object tag, Object... args) {
        return i(tag, "", null, args);
    }

    default int w(Object tag, Object... args) {
        return w(tag, "", null, args);
    }

    default int e(Object tag, Object... args) {
        return e(tag, "", null, args);
    }

    default int wtf(Object tag, Object... args) {
        return wtf(tag, "", null, args);
    }

    default int v(Object tag, String msg, Object... args) {
        return v(tag, msg, null, args);
    }

    default int d(Object tag, String msg, Object... args) {
        return d(tag, msg, null, args);
    }

    default int i(Object tag, String msg, Object... args) {
        return i(tag, msg, null, args);
    }

    default int w(Object tag, String msg, Object... args) {
        return w(tag, msg, null, args);
    }

    default int e(Object tag, String msg, Object... args) {
        return e(tag, msg, null, args);
    }

    default int wtf(Object tag, String msg, Object... args) {
        return wtf(tag, msg, null, args);
    }

    int v(Object tag, String msg, Throwable t, Object... args);

    int d(Object tag, String msg, Throwable t, Object... args);

    int i(Object tag, String msg, Throwable t, Object... args);

    int w(Object tag, String msg, Throwable t, Object... args);

    int e(Object tag, String msg, Throwable t, Object... args);

    int wtf(Object tag, String msg, Throwable t, Object... args);
}
