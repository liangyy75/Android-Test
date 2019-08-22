package liang.example.utils;

public class ApiManager {
    public static LoggerManager loggerManager = new LoggerManager(LoggerLevel.VERBOSE, true);

    public static void init(String tag) {
        LogApiKt.setDEFAULT_TAG(tag);
    }
}
