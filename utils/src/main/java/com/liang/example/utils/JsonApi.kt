package com.liang.example.utils

import com.google.gson.Gson
import java.lang.Exception
import java.lang.reflect.Type

object JsonApi {
    val gson: Gson = Gson()

    fun <T> parseJson(jsonStr: String, typeOfT: Type): T? {
        try {
            return gson.fromJson(jsonStr, typeOfT)
        } catch (e: Exception) {
            ApiManager.LOGGER.e(ApiManager.DEFAULT_TAG, e)
        }
        return null
    }

    fun <T> parseJsonNonNull(jsonStr: String, typeOfT: Type): T = gson.fromJson(jsonStr, typeOfT)!!

    @JvmOverloads
    fun toJson(obj: Any?, default: String = ""): String = gson.toJson(obj) ?: default
}
