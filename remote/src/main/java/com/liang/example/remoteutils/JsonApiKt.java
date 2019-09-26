package com.liang.example.remoteutils;

import com.google.gson.Gson;
import com.liang.example.remote.RemoteMsgManager;

import java.lang.reflect.Type;

public class JsonApiKt {
    private static Gson gson = null;

    public static void init() {
        gson = new Gson();
    }

    public static <T> T parseJson(String jsonStr, Type typeOfT) {
        try {
            return gson != null ? gson.fromJson(jsonStr, typeOfT) : null;
        } catch (Exception e) {
            RemoteMsgManager.logger.e("JsonApiKt", e);
        }
        return null;
    }

    public static String toJson(Object obj) {
        return gson != null ? gson.toJson(obj) : "";
    }

    public static String toJson(Object obj, String defaultStr) {
        return gson != null ? gson.toJson(obj) : defaultStr;
    }
}
