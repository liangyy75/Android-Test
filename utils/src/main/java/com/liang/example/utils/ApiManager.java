package com.liang.example.utils;

import android.content.Context;
import android.util.Log;

import java.util.Arrays;

import com.liang.example.utils.logger.LoggerKt;
import com.liang.example.utils.logger.LoggerLevel;
import com.liang.example.utils.logger.MultiLogger;

public class ApiManager {
    public static String DEFAULT_TAG;
    public static MultiLogger LOGGER;

    public static void init(Context context) {
        XmlApiKt.init(context);
        JsonApiKt.init();

        DEFAULT_TAG = XmlApiKt.getStringMetaData("logger_tag", "default_tag");
        LoggerKt.setDEFAULT_TAG(DEFAULT_TAG);
        LoggerLevel defaultLevel = LoggerLevel.valueOf(XmlApiKt.getStringMetaData("logger_level", "DEBUG"));
        LoggerKt.setDEFAULT_LEVEL(defaultLevel);
        int mask = XmlApiKt.getIntMetaData("logger_mask", 1);
        String android_logger_name = XmlApiKt.getStringMetaData("android_logger_name");
        String system_logger_name = XmlApiKt.getStringMetaData("system_logger_name");
        String file_logger_name = XmlApiKt.getStringMetaData("file_logger_name");
        Log.d(DEFAULT_TAG, String.format("logger_tag: %s, logger_level: %s, mask: %s, android_logger_name: %s, system_logger_name: %s, file_logger_name: %s"
                , DEFAULT_TAG, defaultLevel.toString(), mask, android_logger_name, system_logger_name, file_logger_name));
        LOGGER = LoggerKt.getStandardLogger(defaultLevel, 1, Arrays.asList(android_logger_name, system_logger_name, file_logger_name));
    }
}
