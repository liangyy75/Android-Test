@file:Suppress("UNCHECKED_CAST")

package com.liang.example.utils.json

import java.lang.RuntimeException
import java.lang.reflect.Field

interface ReflectHandleInter<T> {
    fun handleBoolean(obj: Any?, f: Field?, v: Boolean?): T?
    fun handleByte(obj: Any?, f: Field?, v: Byte?): T?
    fun handleShort(obj: Any?, f: Field?, v: Short?): T?
    fun handleInt(obj: Any?, f: Field?, v: Int?): T?
    fun handleLong(obj: Any?, f: Field?, v: Long?): T?
    fun handleFloat(obj: Any?, f: Field?, v: Float?): T?
    fun handleDouble(obj: Any?, f: Field?, v: Double?): T?
    fun handleChar(obj: Any?, f: Field?, v: Char?): T?
    fun handleString(obj: Any?, f: Field?, v: String?): T?

    fun handleBooleanArray(obj: Any?, f: Field?, v: BooleanArray?): T?
    fun handleByteArray(obj: Any?, f: Field?, v: ByteArray?): T?
    fun handleShortArray(obj: Any?, f: Field?, v: ShortArray?): T?
    fun handleIntArray(obj: Any?, f: Field?, v: IntArray?): T?
    fun handleLongArray(obj: Any?, f: Field?, v: LongArray?): T?
    fun handleFloatArray(obj: Any?, f: Field?, v: FloatArray?): T?
    fun handleDoubleArray(obj: Any?, f: Field?, v: DoubleArray?): T?
    fun handleCharArray(obj: Any?, f: Field?, v: CharArray?): T?
    fun handleStringArray(obj: Any?, f: Field?, v: Array<String>?): T?

    fun handleArray(obj: Any?, f: Field?, v: Array<*>?): T?
    fun handleObject(obj: Any?, f: Field?, v: Any?): T?

    fun getResultByCls(cls: Class<*>, v: Any): T? {
        val str = cls.toString()
        return when {
            !str.startsWith("class ") -> when (cls) {
                Boolean::class.java -> handleBoolean(null, null, v as? Boolean)
                Byte::class.java -> handleByte(null, null, v as? Byte)
                Short::class.java -> handleShort(null, null, v as? Short)
                Int::class.java -> handleInt(null, null, v as? Int)
                Long::class.java -> handleLong(null, null, v as? Long)
                Float::class.java -> handleFloat(null, null, v as? Float)
                Double::class.java -> handleDouble(null, null, v as? Double)
                Char::class.java -> handleChar(null, null, v as? Char)  // 9次判断
                else -> throw RuntimeException("unknown class type")
            }
            cls == String::class.java -> handleString(null, null, v as? String)

            str.startsWith("class [") -> if (str.startsWith("class [L")) {
                when (cls) {
                    Array<Boolean>::class.java -> handleBooleanArray(null, null, (v as? Array<Boolean>)?.toBooleanArray())
                    Array<Byte>::class.java -> handleByteArray(null, null, (v as? Array<Byte>)?.toByteArray())
                    Array<Short>::class.java -> handleShortArray(null, null, (v as? Array<Short>)?.toShortArray())
                    Array<Int>::class.java -> handleIntArray(null, null, (v as? Array<Int>)?.toIntArray())
                    Array<Long>::class.java -> handleLongArray(null, null, (v as? Array<Long>)?.toLongArray())
                    Array<Float>::class.java -> handleFloatArray(null, null, (v as? Array<Float>)?.toFloatArray())
                    Array<Double>::class.java -> handleDoubleArray(null, null, (v as? Array<Double>)?.toDoubleArray())
                    Array<Char>::class.java -> handleCharArray(null, null, (v as? Array<Char>)?.toCharArray())
                    Array<String>::class.java -> handleStringArray(null, null, v as? Array<String>)
                    else -> handleArray(null, null, v as? Array<*>)  // 14次
                }
            } else {
                when (cls) {
                    BooleanArray::class.java -> handleBooleanArray(null, null, v as? BooleanArray)
                    ByteArray::class.java -> handleByteArray(null, null, v as? ByteArray)
                    ShortArray::class.java -> handleShortArray(null, null, v as? ShortArray)
                    IntArray::class.java -> handleIntArray(null, null, v as? IntArray)
                    LongArray::class.java -> handleLongArray(null, null, v as? LongArray)
                    FloatArray::class.java -> handleFloatArray(null, null, v as? FloatArray)
                    DoubleArray::class.java -> handleDoubleArray(null, null, v as? DoubleArray)
                    CharArray::class.java -> handleCharArray(null, null, v as? CharArray)  // 12次
                    else -> throw RuntimeException("unknown class type")
                }
            }

            else -> handleObject(null, null, v)
        }
    }

    fun getResultByField(it: Field, obj: Any): T? {
        val cls = it.javaClass
        val str = cls.toString()
        return when {
            !str.startsWith("class ") -> when (cls) {
                Boolean::class.java -> handleBoolean(obj, it, it.getBoolean(obj))
                Byte::class.java -> handleByte(obj, it, it.getByte(obj))
                Short::class.java -> handleShort(obj, it, it.getShort(obj))
                Int::class.java -> handleInt(obj, it, it.getInt(obj))
                Long::class.java -> handleLong(obj, it, it.getLong(obj))
                Float::class.java -> handleFloat(obj, it, it.getFloat(obj))
                Double::class.java -> handleDouble(obj, it, it.getDouble(obj))
                Char::class.java -> handleChar(obj, it, it.getChar(obj))  // 9次判断
                else -> throw RuntimeException("unknown class type")
            }
            cls == String::class.java -> handleString(obj, it, it.get(obj) as? String)

            str.startsWith("class [") -> if (str.startsWith("class [L")) {
                when (cls) {
                    Array<Boolean>::class.java -> handleBooleanArray(obj, it, (it.get(obj) as? Array<Boolean>)?.toBooleanArray())
                    Array<Byte>::class.java -> handleByteArray(obj, it, (it.get(obj) as? Array<Byte>)?.toByteArray())
                    Array<Short>::class.java -> handleShortArray(obj, it, (it.get(obj) as? Array<Short>)?.toShortArray())
                    Array<Int>::class.java -> handleIntArray(obj, it, (it.get(obj) as? Array<Int>)?.toIntArray())
                    Array<Long>::class.java -> handleLongArray(obj, it, (it.get(obj) as? Array<Long>)?.toLongArray())
                    Array<Float>::class.java -> handleFloatArray(obj, it, (it.get(obj) as? Array<Float>)?.toFloatArray())
                    Array<Double>::class.java -> handleDoubleArray(obj, it, (it.get(obj) as? Array<Double>)?.toDoubleArray())
                    Array<Char>::class.java -> handleCharArray(obj, it, (it.get(obj) as? Array<Char>)?.toCharArray())
                    Array<String>::class.java -> handleStringArray(obj, it, it.get(obj) as? Array<String>)
                    else -> handleArray(obj, it, it.get(obj) as? Array<*>)  // 14次
                }
            } else {
                when (cls) {
                    BooleanArray::class.java -> handleBooleanArray(obj, it, it.get(obj) as? BooleanArray)
                    ByteArray::class.java -> handleByteArray(obj, it, it.get(obj) as? ByteArray)
                    ShortArray::class.java -> handleShortArray(obj, it, it.get(obj) as? ShortArray)
                    IntArray::class.java -> handleIntArray(obj, it, it.get(obj) as? IntArray)
                    LongArray::class.java -> handleLongArray(obj, it, it.get(obj) as? LongArray)
                    FloatArray::class.java -> handleFloatArray(obj, it, it.get(obj) as? FloatArray)
                    DoubleArray::class.java -> handleDoubleArray(obj, it, it.get(obj) as? DoubleArray)
                    CharArray::class.java -> handleCharArray(obj, it, it.get(obj) as? CharArray)  // 12次
                    else -> throw RuntimeException("unknown class type")
                }
            }

            else -> handleObject(obj, it, it.get(obj))
        }
    }
}

interface JsonInter {
    fun <T> fromJson(jsonStr: String): T
    fun <T : Any> toJson(obj: T): String
}

fun getFieldFromCls(clsCache: MutableMap<Class<*>, List<Field>>, cls: Class<*>): List<Field> {
    var result = clsCache[cls]
    if (result == null) {
        result = cls.declaredFields.toList()
        result.forEach { it.isAccessible = true }
    }
    return result
}

open class ReflectJsonApi(
        var strategy: JsonStrategy = JsonStrategy.SIMPLEST,
        var style: JsonStyle = JsonStyle.STANDARD,
        val clsCache: MutableMap<Class<*>, List<Field>> = mutableMapOf()
) : JsonInter {

    override fun <T> fromJson(jsonStr: String): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any> toJson(obj: T): String = ReflectToJsonTask(strategy).run(obj).toString()

    open class ReflectToJsonTask(var strategy: JsonStrategy, val clsCache: MutableMap<Class<*>, List<Field>> = mutableMapOf()) :
            ReflectHandleInter<SimpleJsonValue<*>> {
        // basic

        override fun handleBoolean(obj: Any?, f: Field?, v: Boolean?): SimpleJsonBoolean? =
                if (v != null || strategy.useNull()) SimpleJsonBoolean(v) else null

        protected fun checkNumberAndStrategy(v: Number?, nt: JsonNumberType, nv: String = "0"): SimpleJsonNumber? {
            return if (v != null || strategy.useNull()) SimpleJsonNumber(v!!, nt, nv) else null
        }

        override fun handleByte(obj: Any?, f: Field?, v: Byte?): SimpleJsonNumber? = checkNumberAndStrategy(v, JsonNumberType.BYTE)
        override fun handleShort(obj: Any?, f: Field?, v: Short?): SimpleJsonNumber? = checkNumberAndStrategy(v, JsonNumberType.SHORT)
        override fun handleInt(obj: Any?, f: Field?, v: Int?): SimpleJsonNumber? = checkNumberAndStrategy(v, JsonNumberType.INT)
        override fun handleLong(obj: Any?, f: Field?, v: Long?): SimpleJsonNumber? = checkNumberAndStrategy(v, JsonNumberType.LONG)
        override fun handleFloat(obj: Any?, f: Field?, v: Float?): SimpleJsonNumber? = checkNumberAndStrategy(v, JsonNumberType.FLOAT)
        override fun handleDouble(obj: Any?, f: Field?, v: Double?): SimpleJsonNumber? = checkNumberAndStrategy(v, JsonNumberType.DOUBLE)
        override fun handleChar(obj: Any?, f: Field?, v: Char?): SimpleJsonNumber? =
                if (v != null || strategy.useNull()) SimpleJsonNumber(v!!) else null

        override fun handleString(obj: Any?, f: Field?, v: String?): SimpleJsonString? =
                if (v?.isEmpty() == false || strategy.useNull()) SimpleJsonString(v) else null

        // array

        override fun handleBooleanArray(obj: Any?, f: Field?, v: BooleanArray?): SimpleJsonArray? =
                if (v?.isNotEmpty() == true || strategy.useNull()) SimpleJsonArray(v!!.map { SimpleJsonBoolean(it) }) else null

        override fun handleByteArray(obj: Any?, f: Field?, v: ByteArray?): SimpleJsonArray? =
                if (v?.isNotEmpty() == true || strategy.useNull()) SimpleJsonArray(v!!.map { SimpleJsonNumber(it) }) else null

        override fun handleShortArray(obj: Any?, f: Field?, v: ShortArray?): SimpleJsonArray? =
                if (v?.isNotEmpty() == true || strategy.useNull()) SimpleJsonArray(v!!.map { SimpleJsonNumber(it) }) else null

        override fun handleIntArray(obj: Any?, f: Field?, v: IntArray?): SimpleJsonArray? =
                if (v?.isNotEmpty() == true || strategy.useNull()) SimpleJsonArray(v!!.map { SimpleJsonNumber(it) }) else null

        override fun handleLongArray(obj: Any?, f: Field?, v: LongArray?): SimpleJsonArray? =
                if (v?.isNotEmpty() == true || strategy.useNull()) SimpleJsonArray(v!!.map { SimpleJsonNumber(it) }) else null

        override fun handleFloatArray(obj: Any?, f: Field?, v: FloatArray?): SimpleJsonArray? =
                if (v?.isNotEmpty() == true || strategy.useNull()) SimpleJsonArray(v!!.map { SimpleJsonNumber(it) }) else null

        override fun handleDoubleArray(obj: Any?, f: Field?, v: DoubleArray?): SimpleJsonArray? =
                if (v?.isNotEmpty() == true || strategy.useNull()) SimpleJsonArray(v!!.map { SimpleJsonNumber(it) }) else null

        override fun handleCharArray(obj: Any?, f: Field?, v: CharArray?): SimpleJsonArray? =
                if (v?.isNotEmpty() == true || strategy.useNull()) SimpleJsonArray(v!!.map { SimpleJsonNumber(it) }) else null

        override fun handleStringArray(obj: Any?, f: Field?, v: Array<String>?): SimpleJsonArray? =
                if (v?.isNotEmpty() == true || strategy.useNull()) SimpleJsonArray(v!!.map { SimpleJsonString(it) }) else null

        override fun handleArray(obj: Any?, f: Field?, v: Array<*>?): SimpleJsonArray? = if (v?.isNullOrEmpty() == false || strategy.useNull()) {
            if (v?.isNullOrEmpty() == true) {
                JSON_EMPTY_ARRAY
            }
            val itemCls = v[0]!!::class.java
            SimpleJsonArray(v.mapNotNull { if (it != null || strategy.useNull()) handleObjectInner(it, itemCls) else null })
        } else null

        // object

        override fun handleObject(obj: Any?, f: Field?, v: Any?): SimpleJsonObject? =
                if (v != null || strategy.useNull()) handleObjectInner(v!!, v::class.java) else null

        open fun handleObjectInner(v: Any?, cls: Class<*>): SimpleJsonObject? {
            if (v == null) {
                return JSON_EMPTY_OBJECT
            }
            val fields = getFieldFromCls(clsCache, cls)
            if (fields.isEmpty()) {
                return null
            }
            val itemMap = mutableMapOf<String, SimpleJsonValue<*>>()
            fields.forEach {
                val jsonValue = getResultByField(it, v)
                if (jsonValue != null) {
                    itemMap[it.name] = jsonValue
                }
            }
            return SimpleJsonObject(itemMap)
        }

        // handle

        open fun run(obj: Any): SimpleJsonValue<*> = getResultByCls(obj::class.java, obj) ?: SimpleJsonString("null object")
        open fun runOrNull(obj: Any): SimpleJsonValue<*>? = getResultByCls(obj::class.java, obj)
    }
}
