package com.liang.example.utils.json2

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.math.BigDecimal
import java.math.BigInteger

/**
 * Array<Unit> -> 0
 * BooleanArray -> 1 / Array<Boolean> -> 11
 * ByteArray -> 2 / Array<Byte> -> 12
 * ShortArray -> 3 / Array<Short> -> 13
 * IntArray -> 4 / Array<Int> -> 14
 * LongArray -> 5 / Array<Long> -> 15
 * FloatArray -> 6 / Array<Float> -> 16
 * DoubleArray -> 7 / Array<Double> -> 17
 * CharArray -> 8 / Array<Char> -> 18
 * Array<String> -> 9
 * Array<Any> -> 10
 * Array<BigInteger> -> 19
 * Array<BigDecimal> -> 20
 * @return Pair<type: Int, depth: Int>
 */
fun findItemTypeByCls(cls: Class<*>): Pair<Int, Int> {
    val clsStr = cls.toString()
    if (!clsStr.startsWith("class [")) {
        throw RuntimeException("cls should be array's cls")
    }
    var index = 7
    var depth = 0
    while (index < clsStr.length && clsStr[index] == '[') {
        index++
        depth++
    }
    val temp1 = clsStr[index]
    val temp2 = clsStr.substring(index + 1)
    return Pair(when {
        temp2.isEmpty() -> when (temp1) {
            'Z' -> 1
            'B' -> 2
            'S' -> 3
            'I' -> 4
            'L' -> 5
            'F' -> 6
            'D' -> 7
            'C' -> 8
            else -> 0
        }
        temp2.startsWith("java.lang.") -> when (temp2) {
            "java.lang.String;" -> 9
            "java.lang.Boolean;" -> 11
            "java.lang.Byte;" -> 12
            "java.lang.Short;" -> 13
            "java.lang.Integer;" -> 14
            "java.lang.Long;" -> 15
            "java.lang.Float;" -> 16
            "java.lang.Double;" -> 17
            "java.lang.Char;" -> 18
            else -> 10
        }
        temp2 == "kotlin.Unit;" -> 0
        temp2 == "java.math.BigInteger;" -> 19
        temp2 == "java.math.BigDecimal;" -> 20
        temp1 == 'L' -> 10
        else -> throw RuntimeException("incorrect array cls")
    }, depth)
}

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
    fun handleBigInteger(obj: Any?, f: Field?, v: BigInteger?): T?
    fun handleBigDecimal(obj: Any?, f: Field?, v: BigDecimal?): T?

    fun handleBooleanArray(obj: Any?, f: Field?, v: Array<Boolean?>?): T?
    fun handleByteArray(obj: Any?, f: Field?, v: Array<Byte?>?): T?
    fun handleShortArray(obj: Any?, f: Field?, v: Array<Short?>?): T?
    fun handleIntArray(obj: Any?, f: Field?, v: Array<Int?>?): T?
    fun handleLongArray(obj: Any?, f: Field?, v: Array<Long?>?): T?
    fun handleFloatArray(obj: Any?, f: Field?, v: Array<Float?>?): T?
    fun handleDoubleArray(obj: Any?, f: Field?, v: Array<Double?>?): T?
    fun handleCharArray(obj: Any?, f: Field?, v: Array<Char?>?): T?

    fun handleStringArray(obj: Any?, f: Field?, v: Array<String?>?): T?
    fun handleBigIntegerArray(obj: Any?, f: Field?, v: Array<BigInteger?>?): T?
    fun handleBigDecimalArray(obj: Any?, f: Field?, v: Array<BigDecimal?>?): T?

    fun handleArray(obj: Any?, f: Field?, v: Array<*>?): T?
    fun handleObject(obj: Any?, f: Field?, v: Any?): T?

    @Suppress("UNCHECKED_CAST")
    fun handleArrayInner(array: Array<*>?, transform2: (List<T?>) -> T?): T? {
        if (array.isNullOrEmpty()) {
            return null
        }
        val temp = findItemTypeByCls(array::class.java)
        val itemType = temp.first
        val depth = temp.second
        return when (itemType) {
            in 11..18 -> when (itemType) {
                11 -> transformArray<Boolean>(array, depth, { handleBooleanArray(null, null, it) }, transform2)
                12 -> transformArray<Byte>(array, depth, { handleByteArray(null, null, it) }, transform2)
                13 -> transformArray<Short>(array, depth, { handleShortArray(null, null, it) }, transform2)
                14 -> transformArray<Int>(array, depth, { handleIntArray(null, null, it) }, transform2)
                15 -> transformArray<Long>(array, depth, { handleLongArray(null, null, it) }, transform2)
                16 -> transformArray<Float>(array, depth, { handleFloatArray(null, null, it) }, transform2)
                17 -> transformArray<Double>(array, depth, { handleDoubleArray(null, null, it) }, transform2)
                18 -> transformArray<Char>(array, depth, { handleCharArray(null, null, it) }, transform2)
                else -> null
            }

            in 1..8 -> when (itemType) {
                1 -> transformArray2<BooleanArray>(array, depth, { handleBooleanArray(null, null, it?.toTypedArray() as? Array<Boolean?>) }, transform2)
                2 -> transformArray2<ByteArray>(array, depth, { handleByteArray(null, null, it?.toTypedArray() as? Array<Byte?>) }, transform2)
                3 -> transformArray2<ShortArray>(array, depth, { handleShortArray(null, null, it?.toTypedArray() as? Array<Short?>) }, transform2)
                4 -> transformArray2<IntArray>(array, depth, { handleIntArray(null, null, it?.toTypedArray() as? Array<Int?>) }, transform2)
                5 -> transformArray2<LongArray>(array, depth, { handleLongArray(null, null, it?.toTypedArray() as? Array<Long?>) }, transform2)
                6 -> transformArray2<FloatArray>(array, depth, { handleFloatArray(null, null, it?.toTypedArray() as? Array<Float?>) }, transform2)
                7 -> transformArray2<DoubleArray>(array, depth, { handleDoubleArray(null, null, it?.toTypedArray() as? Array<Double?>) }, transform2)
                8 -> transformArray2<CharArray>(array, depth, { handleCharArray(null, null, it?.toTypedArray() as? Array<Char?>) }, transform2)
                else -> null
            }

            9 -> transformArray<String>(array, depth, { handleStringArray(null, null, it) }, transform2)
            10 -> transformArray2<Any>(array, depth, { handleObject(null, null, it) }, transform2)
            19 -> transformArray<BigInteger>(array, depth, { handleBigIntegerArray(null, null, it) }, transform2)
            20 -> transformArray<BigDecimal>(array, depth, { handleBigDecimalArray(null, null, it) }, transform2)
            else -> null
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <I> transformArray(array: Array<*>?, depth: Int, transform: (Array<I?>) -> T?, transform2: (List<T?>) -> T?): T? = when {
        array.isNullOrEmpty() -> null
        depth == 0 -> transform(array as Array<I?>)
        else -> {
            val nextDepth = depth - 1
            transform2(array.map { transformArray(it as? Array<*>, nextDepth, transform, transform2) })
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <I> transformArray2(array: Array<*>?, depth: Int, transform: (I?) -> T?, transform2: (List<T?>) -> T?): T? = when {
        array.isNullOrEmpty() -> null
        depth == 0 -> transform2((array as Array<I?>).map(transform))
        else -> {
            val nextDepth = depth - 1
            transform2(array.map { transformArray2(it as? Array<*>, nextDepth, transform, transform2) })
        }
    }

    fun handleByCls(cls: Class<*>, v: Any): T? {
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
            cls == BigInteger::class.java -> handleBigInteger(null, null, v as? BigInteger)
            cls == BigDecimal::class.java -> handleBigDecimal(null, null, v as? BigDecimal)
            str.startsWith("class [") -> dispatchArrTask(cls, v as? Array<*>, null, null)
            else -> handleObject(null, null, v)
        }
    }

    fun handleByField(it: Field, obj: Any): T? {
        val cls = it.type
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
            cls == BigInteger::class.java -> handleBigInteger(null, null, it.get(obj) as? BigInteger)
            cls == BigDecimal::class.java -> handleBigDecimal(null, null, it.get(obj) as? BigDecimal)
            str.startsWith("class [") -> dispatchArrTask(cls, it.get(obj), obj, it)
            else -> handleObject(obj, it, it.get(obj))
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun dispatchArrTask(cls: Class<*>, v: Any?, obj: Any?, f: Field?): T? {
        if (v == null) {
            return null
        }
        return when (cls) {
            BooleanArray::class.java -> handleBooleanArray(obj, f, (v as BooleanArray).toTypedArray() as Array<Boolean?>)
            ByteArray::class.java -> handleByteArray(obj, f, (v as ByteArray).toTypedArray() as Array<Byte?>)
            ShortArray::class.java -> handleShortArray(obj, f, (v as ShortArray).toTypedArray() as Array<Short?>)
            IntArray::class.java -> handleIntArray(obj, f, (v as IntArray).toTypedArray() as Array<Int?>)
            LongArray::class.java -> handleLongArray(obj, f, (v as LongArray).toTypedArray() as Array<Long?>)
            FloatArray::class.java -> handleFloatArray(obj, f, (v as FloatArray).toTypedArray() as Array<Float?>)
            DoubleArray::class.java -> handleDoubleArray(obj, f, (v as DoubleArray).toTypedArray() as Array<Double?>)
            CharArray::class.java -> handleCharArray(obj, f, (v as CharArray).toTypedArray() as Array<Char?>)
            else -> handleArray(obj, f, v as Array<*>)
        }
    }
}

interface JsonInter {
    fun <T> fromJson(jsonStr: String, cls: Class<*>): T
    fun <T> fromJsonOrNull(jsonStr: String, cls: Class<*>): T?
    fun <T : Any> toJson(obj: T): String
    fun <T : Any> toJsonOrNull(obj: T): String?
}

// 最垃圾的缓存机制，没有定时清理，没有时间记录
fun getFieldsFromCls(clsCache: MutableMap<Class<*>, Array<Field>>, cls: Class<*>): Array<Field> {
    var result = clsCache[cls]
    if (result == null) {
        result = cls.declaredFields.filter { !Modifier.isStatic(it.modifiers) }.toTypedArray()
        result.forEach { it.isAccessible = true }
    }
    return result
}

fun getDefaultCtorFromCls(ctorCache: MutableMap<Class<*>, Constructor<*>>, cls: Class<*>): Constructor<*> {
    var result = ctorCache[cls]
    if (result == null) {
        result = cls.getConstructor()
        result.isAccessible = true
    }
    return result
}

open class ReflectJsonApi(
        var strategy: JsonStrategy = JsonStrategy.SIMPLEST,
        var style: JsonStyle = JsonStyle.STANDARD,
        val clsCache: MutableMap<Class<*>, Array<Field>> = mutableMapOf(),
        val ctorCache: MutableMap<Class<*>, Constructor<*>> = mutableMapOf()
) : JsonInter {
    @Suppress("UNCHECKED_CAST")
    override fun <T> fromJson(jsonStr: String, cls: Class<*>): T =
            ReflectFromJsonTask(clsCache, ctorCache).fromJsonValue(SimpleJsonParser.fromJson(jsonStr, style), cls) as T

    @Suppress("UNCHECKED_CAST")
    override fun <T> fromJsonOrNull(jsonStr: String, cls: Class<*>): T? =
            ReflectFromJsonTask(clsCache, ctorCache).fromJsonValue(SimpleJsonParser.fromJson(jsonStr, style), cls) as? T

    override fun <T : Any> toJson(obj: T): String {
        val result = ReflectToJsonTask(clsCache).handleByCls(obj::class.java, obj) ?: return ""
        if (result is SimpleJsonArray) {
            result.mStrategy = strategy
        }
        if (result is SimpleJsonObject) {
            result.mStrategy = strategy
        }
        return result.string()
    }

    override fun <T : Any> toJsonOrNull(obj: T): String? {
        val result = ReflectToJsonTask(clsCache).handleByCls(obj::class.java, obj) ?: return null
        if (result is SimpleJsonArray) {
            result.mStrategy = strategy
        }
        if (result is SimpleJsonObject) {
            result.mStrategy = strategy
        }
        return result.string()
    }

    open class ReflectToJsonTask(val clsCache: MutableMap<Class<*>, Array<Field>>) : ReflectHandleInter<SimpleJsonValue<*>> {
        // basic

        override fun handleBoolean(obj: Any?, f: Field?, v: Boolean?): SimpleJsonBoolean? = if (v != null) SimpleJsonBoolean(v) else null
        override fun handleByte(obj: Any?, f: Field?, v: Byte?): SimpleJsonNumber? = if (v != null) SimpleJsonNumber(v) else null
        override fun handleShort(obj: Any?, f: Field?, v: Short?): SimpleJsonNumber? = if (v != null) SimpleJsonNumber(v) else null
        override fun handleInt(obj: Any?, f: Field?, v: Int?): SimpleJsonNumber? = if (v != null) SimpleJsonNumber(v) else null
        override fun handleLong(obj: Any?, f: Field?, v: Long?): SimpleJsonNumber? = if (v != null) SimpleJsonNumber(v) else null
        override fun handleFloat(obj: Any?, f: Field?, v: Float?): SimpleJsonNumber? = if (v != null) SimpleJsonNumber(v) else null
        override fun handleDouble(obj: Any?, f: Field?, v: Double?): SimpleJsonNumber? = if (v != null) SimpleJsonNumber(v) else null
        override fun handleChar(obj: Any?, f: Field?, v: Char?): SimpleJsonNumber? = if (v != null) SimpleJsonNumber(v) else null
        override fun handleBigInteger(obj: Any?, f: Field?, v: BigInteger?): SimpleJsonValue<*>? = if (v != null) SimpleJsonNumber(v) else null
        override fun handleBigDecimal(obj: Any?, f: Field?, v: BigDecimal?): SimpleJsonValue<*>? = if (v != null) SimpleJsonNumber(v) else null
        override fun handleString(obj: Any?, f: Field?, v: String?): SimpleJsonString? = if (v != null) SimpleJsonString(v) else null

        // basicArray

        override fun handleBooleanArray(obj: Any?, f: Field?, v: Array<Boolean?>?): SimpleJsonArray? =
                if (v?.isNotEmpty() == true) SimpleJsonArray(v.map { if (it != null) SimpleJsonBoolean(it) else null }) else null

        override fun handleByteArray(obj: Any?, f: Field?, v: Array<Byte?>?): SimpleJsonArray? =
                if (v?.isNotEmpty() == true) SimpleJsonArray(v.map { if (it != null) SimpleJsonNumber(it) else null }) else null

        override fun handleShortArray(obj: Any?, f: Field?, v: Array<Short?>?): SimpleJsonArray? =
                if (v?.isNotEmpty() == true) SimpleJsonArray(v.map { if (it != null) SimpleJsonNumber(it) else null }) else null

        override fun handleIntArray(obj: Any?, f: Field?, v: Array<Int?>?): SimpleJsonArray? =
                if (v?.isNotEmpty() == true) SimpleJsonArray(v.map { if (it != null) SimpleJsonNumber(it) else null }) else null

        override fun handleLongArray(obj: Any?, f: Field?, v: Array<Long?>?): SimpleJsonArray? =
                if (v?.isNotEmpty() == true) SimpleJsonArray(v.map { if (it != null) SimpleJsonNumber(it) else null }) else null

        override fun handleFloatArray(obj: Any?, f: Field?, v: Array<Float?>?): SimpleJsonArray? =
                if (v?.isNotEmpty() == true) SimpleJsonArray(v.map { if (it != null) SimpleJsonNumber(it) else null }) else null

        override fun handleDoubleArray(obj: Any?, f: Field?, v: Array<Double?>?): SimpleJsonArray? =
                if (v?.isNotEmpty() == true) SimpleJsonArray(v.map { if (it != null) SimpleJsonNumber(it) else null }) else null

        override fun handleCharArray(obj: Any?, f: Field?, v: Array<Char?>?): SimpleJsonArray? =
                if (v?.isNotEmpty() == true) SimpleJsonArray(v.map { if (it != null) SimpleJsonNumber(it) else null }) else null


        override fun handleStringArray(obj: Any?, f: Field?, v: Array<String?>?): SimpleJsonArray? =
                if (v?.isNotEmpty() == true) SimpleJsonArray(v.map { if (it != null) SimpleJsonString(it) else null }) else null

        override fun handleBigIntegerArray(obj: Any?, f: Field?, v: Array<BigInteger?>?): SimpleJsonValue<*>? =
                if (v?.isNotEmpty() == true) SimpleJsonArray(v.map { if (it != null) SimpleJsonNumber(it) else null }) else null

        override fun handleBigDecimalArray(obj: Any?, f: Field?, v: Array<BigDecimal?>?): SimpleJsonValue<*>? =
                if (v?.isNotEmpty() == true) SimpleJsonArray(v.map { if (it != null) SimpleJsonNumber(it) else null }) else null

        // array: char[][]
        override fun handleArray(obj: Any?, f: Field?, v: Array<*>?): SimpleJsonValue<*>? = handleArrayInner(v) { SimpleJsonArray(it) }

        // object
        override fun handleObject(obj: Any?, f: Field?, v: Any?): SimpleJsonValue<*>? {
            if (v == null) {
                return null
            }
            val fields = getFieldsFromCls(clsCache, v::class.java)
            if (fields.isEmpty()) {
                return null
            }
            val itemMap = mutableMapOf<String, SimpleJsonValue<*>>()
            fields.forEach { itemMap[it.name] = handleByField(it, v) ?: JSON_NULL }
            return SimpleJsonObject(itemMap)
        }
    }

    open class ReflectFromJsonTask(val clsCache: MutableMap<Class<*>, Array<Field>>, val ctorCache: MutableMap<Class<*>, Constructor<*>>) {
        open fun fromJsonValue(jsonValue: SimpleJsonValue<*>, cls: Class<*>?): Any? {
            if (jsonValue.value() == null) {
                return null
            }
            val type = jsonValue.type()
            return when {
                type == JsonType.ARRAY -> fromJsonArray(jsonValue as SimpleJsonArray, cls)
                cls == null -> {
                    when (type) {
                        JsonType.NUL -> null
                        JsonType.NUMBER -> {
                            val n = jsonValue as SimpleJsonNumber
                            if (n.numberType() == JsonNumberType.CHAR) n.charValue() else n.value2()
                        }
                        JsonType.BOOL, JsonType.STRING -> jsonValue.value2()
                        JsonType.ARRAY -> throw RuntimeException("impossible error while parse jsonValue")
                        JsonType.OBJECT -> throw RuntimeException("jsonValue's type should not be object when cls is null")
                    }
                }
                type == JsonType.OBJECT -> fromJsonObject(jsonValue as SimpleJsonObject, cls)
                else -> throw RuntimeException("cls should be null when jsonValue's type isn't object")
            }
        }

        // Array.newInstance(itemCls, int... dimensions)
        @Suppress("UNCHECKED_CAST")
        open fun fromJsonArray(jsonArray: SimpleJsonArray, cls: Class<*>?): Any? {
            if (jsonArray.value().isNullOrEmpty() || cls == null) {
                return null
            }
            val temp = findItemTypeByCls(cls)
            val itemType = temp.first
            val depth = temp.second
            return when (itemType) {
                in 1..8 -> when (itemType) {
                    1 -> transformJsonArray(jsonArray, depth, cls, { it?.value() as Boolean }) { (it as List<Boolean>).toBooleanArray() }
                    2 -> transformJsonArray(jsonArray, depth, cls, { (it?.value() as Long).toByte() }) { (it as List<Byte>).toByteArray() }
                    3 -> transformJsonArray(jsonArray, depth, cls, { (it?.value() as Long).toShort() }) { (it as List<Short>).toShortArray() }
                    4 -> transformJsonArray(jsonArray, depth, cls, { (it?.value() as Long).toInt() }) { (it as List<Int>).toIntArray() }
                    5 -> transformJsonArray(jsonArray, depth, cls, { it?.value() as Long }) { (it as List<Long>).toLongArray() }
                    6 -> transformJsonArray(jsonArray, depth, cls, { (it?.value() as Double).toFloat() }) { (it as List<Float>).toFloatArray() }
                    7 -> transformJsonArray(jsonArray, depth, cls, { it?.value() as Double }) { (it as List<Double>).toDoubleArray() }
                    8 -> transformJsonArray(jsonArray, depth, cls, { (it?.value() as Long).toChar() }) { (it as List<Char>).toCharArray() }
                    else -> null
                }

                in 11..18 -> when (itemType) {
                    11 -> transformJsonArray(jsonArray, depth, cls, { it?.value() as? Boolean }) { it.toTypedArray() }
                    12 -> transformJsonArray(jsonArray, depth, cls, { (it?.value() as? Long)?.toByte() }) { it.toTypedArray() }
                    13 -> transformJsonArray(jsonArray, depth, cls, { (it?.value() as? Long)?.toShort() }) { it.toTypedArray() }
                    14 -> transformJsonArray(jsonArray, depth, cls, { (it?.value() as? Long)?.toInt() }) { it.toTypedArray() }
                    15 -> transformJsonArray(jsonArray, depth, cls, { it?.value() as? Long }) { it.toTypedArray() }
                    16 -> transformJsonArray(jsonArray, depth, cls, { (it?.value() as? Double)?.toFloat() }) { it.toTypedArray() }
                    17 -> transformJsonArray(jsonArray, depth, cls, { it?.value() as? Double }) { it.toTypedArray() }
                    18 -> transformJsonArray(jsonArray, depth, cls, { (it?.value() as? Long)?.toChar() }) { it.toTypedArray() }
                    else -> null
                }

                9 -> transformJsonArray(jsonArray, depth, cls, { it?.value() as? String }) { it.toTypedArray() }
                19 -> transformJsonArray(jsonArray, depth, cls, { it?.value() as? BigInteger }) { it.toTypedArray() }
                20 -> transformJsonArray(jsonArray, depth, cls, { it?.value() as? BigDecimal }) { it.toTypedArray() }

                10 -> {
                    val clsStr = cls.toString()
                    val finalCls = Class.forName("class ${clsStr.substring(clsStr.lastIndexOf('[') + 2, clsStr.length - 1)}")
                    transformJsonArray(jsonArray, depth, cls, transform@{ fromJsonObject(it as? SimpleJsonObject, cls) }) {
                        val result = java.lang.reflect.Array.newInstance(finalCls, it.size)
                        it.forEachIndexed { index, value -> java.lang.reflect.Array.set(result, index, fromJsonObject(value as? SimpleJsonObject, cls)) }
                        result
                    }
                }
                0 -> null
                else -> throw RuntimeException("itemType should not be $itemType, now the depth is $depth")
            }
        }

        protected fun <T, T2> transformJsonArray(jsonArray: SimpleJsonArray?, depth: Int, cls: Class<*>, transform: (SimpleJsonValue<*>?) -> T?,
                                                 transform2: ((List<T?>) -> T2)): Any? = when {
            jsonArray == null || jsonArray.value().isNullOrEmpty() -> null
            depth == 0 -> transform2(jsonArray.value2().map(transform))
            else -> {
                val nextDepth = depth - 1
                val array = jsonArray.value2()
                val result = java.lang.reflect.Array.newInstance(cls, array.size)
                val newCls = Class.forName("class ${cls.toString().substring(7)}")
                jsonArray.value2().forEachIndexed { index, it ->
                    java.lang.reflect.Array.set(array, index, transformJsonArray(it as SimpleJsonArray, nextDepth, newCls, transform, transform2))
                }
                result
            }
        }

        open fun fromJsonObject(jsonObject: SimpleJsonObject?, cls: Class<*>?): Any? {
            if (jsonObject == null || jsonObject.value().isNullOrEmpty() || cls == null) {
                return null
            }
            val fields = getFieldsFromCls(clsCache, cls)
            val result = getDefaultCtorFromCls(ctorCache, cls).newInstance()
            jsonObject.value2().forEach {
                val field = fields.find { f -> f.name == it.key } ?: return@forEach
                val jsonValueItem = it.value ?: return@forEach
                if (jsonValueItem.value() == null) {
                    return@forEach
                }
                val fieldCls = field.type
                val fieldClsStr = fieldCls.toString()
                val jsonValueType = jsonValueItem.type()
                when {
                    fieldCls == Unit::class.java && jsonValueType == JsonType.NUL -> Unit
                    fieldCls == Boolean::class.java && jsonValueType == JsonType.BOOL -> field.setBoolean(result, jsonValueItem.value() as Boolean)
                    jsonValueType == JsonType.NUMBER -> {
                        val n = jsonValueItem as SimpleJsonNumber
                        val numberType = n.numberType()
                        when {
                            fieldCls == Byte::class.java && numberType == JsonNumberType.BYTE -> field.setByte(result, n.value() as Byte)
                            fieldCls == Short::class.java && numberType == JsonNumberType.SHORT -> field.setShort(result, n.value() as Short)
                            fieldCls == Int::class.java && numberType == JsonNumberType.INT -> field.setInt(result, n.value() as Int)
                            fieldCls == Long::class.java && numberType == JsonNumberType.LONG -> field.setLong(result, n.value() as Long)
                            fieldCls == BigInteger::class.java && numberType == JsonNumberType.BIG_INTEGER -> field.set(result, n.value())
                            fieldCls == Float::class.java && numberType == JsonNumberType.FLOAT -> field.setFloat(result, n.value() as Float)
                            fieldCls == Double::class.java && numberType == JsonNumberType.DOUBLE -> field.setDouble(result, n.value() as Double)
                            fieldCls == BigInteger::class.java && numberType == JsonNumberType.BIG_DECIMAL -> field.set(result, n.value())
                            fieldCls == Char::class.java && numberType == JsonNumberType.CHAR -> field.setChar(result, n.charValue())
                            numberType == JsonNumberType.UNKNOWN -> throw RuntimeException("should not get unknown number type from json string")
                        }
                    }
                    fieldCls == String::class.java && jsonValueType == JsonType.STRING -> field.set(result, jsonValueItem.value())
                    fieldClsStr.startsWith("class L") && jsonValueType == JsonType.OBJECT ->
                        field.set(result, fromJsonObject(jsonValueItem as SimpleJsonObject, fieldCls))
                    fieldClsStr.startsWith("class [") && jsonValueType == JsonType.ARRAY ->
                        field.set(result, fromJsonArray(jsonValueItem as SimpleJsonArray, fieldCls))
                    else -> throw RuntimeException("non corresponding jsonValue's type and field's cls")
                }
            }
            return result
        }
    }
}
