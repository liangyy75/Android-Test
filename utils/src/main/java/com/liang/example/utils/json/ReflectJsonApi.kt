package com.liang.example.utils.json

interface ReflectHandleInter {
    fun handleByte()
    fun handleShort()
    fun handleInt()
    fun handleLong()
    fun handleFloat()
    fun handleDouble()
    fun handleChar()
    fun handleString()
}

interface JsonInter {
    fun <T> fromJson(jsonStr: String): T
    fun <T> toJson(obj: T): String
}

open class ReflectJsonApi : JsonInter {
    override fun <T> fromJson(jsonStr: String): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T> toJson(obj: T): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
