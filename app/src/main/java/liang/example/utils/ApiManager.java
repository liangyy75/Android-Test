package liang.example.utils;

import liang.example.utils.logger.LoggerKt;
import liang.example.utils.logger.LoggerLevel;
import liang.example.utils.logger.NullableLogger;

public class ApiManager {
    private static NullableLogger nullableLogger;

    public static void init(String tag) {
        LoggerKt.setDEFAULT_TAG(tag);
        LoggerKt.setDEFAULT_LEVEL(LoggerLevel.DEBUG);
        nullableLogger = new NullableLogger(LoggerKt.getDEFAULT_LEVEL(), true);
    }

    public static NullableLogger getNullableLogger() {
        return nullableLogger;
    }
}
