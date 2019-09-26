package com.liang.example.remoteutils;

public class NullableLogger implements Logger {
    private Logger logger;

    public NullableLogger() {
        this(false);
    }

    public NullableLogger(boolean useDefault) {
        if (useDefault) {
            this.logger = new AndroidLogger();
        }
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public int v(Object tag, String msg, Throwable t, Object... args) {
        return logger != null ? logger.v(tag, msg, t, args) : 0;
    }

    @Override
    public int d(Object tag, String msg, Throwable t, Object... args) {
        return logger != null ? logger.d(tag, msg, t, args) : 0;
    }

    @Override
    public int i(Object tag, String msg, Throwable t, Object... args) {
        return logger != null ? logger.i(tag, msg, t, args) : 0;
    }

    @Override
    public int w(Object tag, String msg, Throwable t, Object... args) {
        return logger != null ? logger.w(tag, msg, t, args) : 0;
    }

    @Override
    public int e(Object tag, String msg, Throwable t, Object... args) {
        return logger != null ? logger.e(tag, msg, t, args) : 0;
    }

    @Override
    public int wtf(Object tag, String msg, Throwable t, Object... args) {
        return logger != null ? logger.wtf(tag, msg, t, args) : 0;
    }
}
