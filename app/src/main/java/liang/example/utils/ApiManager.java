package liang.example.utils;

public class ApiManager {
    public static LoggerManager loggerManager;

    public static void init(String tag) {
        LogApiKt.setDEFAULT_TAG(tag);
        LogApiKt.setDEFAULT_LOGGER_LEVEL(LoggerLevel.VERBOSE);
        loggerManager = new LoggerManager(LogApiKt.getDEFAULT_LOGGER_LEVEL(), true);
    }
}
