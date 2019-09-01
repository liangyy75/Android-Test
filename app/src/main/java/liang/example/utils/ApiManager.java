package liang.example.utils;

import android.content.Context;
import android.util.Log;

import java.util.Arrays;

import liang.example.utils.logger.LoggerKt;
import liang.example.utils.logger.LoggerLevel;
import liang.example.utils.logger.MultiLogger;
import liang.example.utils.logger.NullableLogger;

public class ApiManager {
    public static MultiLogger LOGGER;

    public static void init(Context context) {
        XmlApiKt.init(context);
        String loggerTag = XmlApiKt.getStringMetaData("logger_tag", "default_tag");
        LoggerKt.setDEFAULT_TAG(loggerTag);
        LoggerLevel defaultLevel = LoggerLevel.valueOf(XmlApiKt.getStringMetaData("logger_level", "DEBUG"));
        LoggerKt.setDEFAULT_LEVEL(defaultLevel);
        int mask = XmlApiKt.getIntMetaData("logger_mask", 1);
        String android_logger_name = XmlApiKt.getStringMetaData("android_logger_name");
        String system_logger_name = XmlApiKt.getStringMetaData("system_logger_name");
        String file_logger_name = XmlApiKt.getStringMetaData("file_logger_name");
        Log.d(loggerTag, String.format("logger_tag: %s, logger_level: %s, mask: %s, android_logger_name: %s, system_logger_name: %s, file_logger_name: %s"
                , loggerTag, defaultLevel.toString(), mask, android_logger_name, system_logger_name, file_logger_name));
        LOGGER = LoggerKt.getStandardLogger(defaultLevel, 1, Arrays.asList(android_logger_name, system_logger_name, file_logger_name));
    }
}
