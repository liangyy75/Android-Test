@file:Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate", "unused", "SpellCheckingInspection")

package com.liang.example.utils.json

import java.lang.RuntimeException
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.math.BigDecimal
import java.math.BigInteger

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
            'B' -> 6
            'S' -> 7
            'I' -> 8
            'L' -> 9
            'F' -> 10
            'D' -> 11
            'C' -> 12
            else -> 0
        }
        temp2.startsWith("java.lang.") -> when (temp2) {
            "java.lang.String;" -> 3
            "java.lang.Boolean;" -> 15
            "java.lang.Byte;" -> 16
            "java.lang.Short;" -> 17
            "java.lang.Int;" -> 18
            "java.lang.Long;" -> 19
            "java.lang.Float;" -> 20
            "java.lang.Double;" -> 21
            "java.lang.Char;" -> 22
            else -> 5
        }
        temp2 == "kotlin.Unit;" -> 0
        temp2 == "java.math.BigInteger;" -> 13
        temp2 == "java.math.BigDecimal;" -> 14
        temp1 == 'L' -> 5
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
            str.startsWith("class [") -> handleArray(null, null, v as? Array<*>)
            else -> handleObject(null, null, v)
        }
    }

    fun getResultByField(it: Field, obj: Any): T? {
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
            str.startsWith("class [") -> handleArray(obj, it, it.get(obj) as? Array<*>)
            else -> handleObject(obj, it, it.get(obj))
        }
    }
}

interface JsonInter {
    fun <T> fromJson(jsonStr: String, cls: Class<*>): T
    fun <T> fromJsonOrNull(jsonStr: String, cls: Class<*>): T?
    fun <T : Any> toJson(obj: T): String
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

// 不对基本类型和数组类型进行处理，只对包含它们的复杂类型进行处理
open class ReflectJsonApi(
        var strategy: JsonStrategy = JsonStrategy.SIMPLEST,
        var style: JsonStyle = JsonStyle.STANDARD,
        val clsCache: MutableMap<Class<*>, Array<Field>> = mutableMapOf(),
        val ctorCache: MutableMap<Class<*>, Constructor<*>> = mutableMapOf()
) : JsonInter {
    override fun <T> fromJson(jsonStr: String, cls: Class<*>): T = ReflectFromJsonTask(style, clsCache, ctorCache).run(jsonStr, cls)
    override fun <T> fromJsonOrNull(jsonStr: String, cls: Class<*>): T? = ReflectFromJsonTask(style, clsCache).runOrNull(jsonStr, cls)

    override fun <T : Any> toJson(obj: T): String = ReflectToJsonTask(strategy, clsCache).run(obj).string()

    open class ReflectToJsonTask(var strategy: JsonStrategy, val clsCache: MutableMap<Class<*>, Array<Field>> = mutableMapOf()) :
            ReflectHandleInter<SimpleJsonValue<*>> {
        // basic

        override fun handleBoolean(obj: Any?, f: Field?, v: Boolean?): SimpleJsonBoolean? =
                if (v != null || strategy.useNull()) SimpleJsonBoolean(v!!) else null

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

        // basicArray

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

        // array<*>

        // TODO: handle char[][]
        override fun handleArray(obj: Any?, f: Field?, v: Array<*>?): SimpleJsonArray? = if (v?.isNullOrEmpty() == false || strategy.useNull()) {
            if (v?.isNullOrEmpty() == true) {
                JSON_EMPTY_ARRAY
            }
            val itemCls = v[0]!!::class.java
            SimpleJsonArray(v.mapNotNull { if (it != null || strategy.useNull()) handleObjectInner(it, itemCls) else null })
        } else null

        fun handleArrayInner(v: Array<*>): Any? {
            val temp = findItemTypeByCls(v::class.java)
            val itemType = temp.first
            val depth = temp.second
            return when (itemType) {
                0 -> null
                1 -> dispatchTransformToArr<Boolean, SimpleJsonValue<*>>(v, depth, { handleBooleanArray(null, null, it.toBooleanArray()) }) { SimpleJsonArray(it) }
                3 -> dispatchTransformToArr<String, SimpleJsonValue<*>>(v, depth, { handleStringArray(null, null, it) }) { SimpleJsonArray(it) }
                6 -> dispatchTransformToArr<Byte, SimpleJsonValue<*>>(v, depth, { handleByteArray(null, null, it.toByteArray()) }) { SimpleJsonArray(it) }
                7 -> dispatchTransformToArr<Short, SimpleJsonValue<*>>(v, depth, { handleShortArray(null, null, it.toShortArray()) }) { SimpleJsonArray(it) }
                8 -> dispatchTransformToArr<Int, SimpleJsonValue<*>>(v, depth, { handleIntArray(null, null, it.toIntArray()) }) { SimpleJsonArray(it) }
                5 -> {
                    TODO()
                }
                else -> throw RuntimeException("itemType should not be $itemType, now the depth is $depth")
            }
        }

        fun <T, T2 : Any> dispatchTransformToArr(array: Array<*>?, depth: Int, t1: ((Array<T>) -> T2?), t2: ((List<T2>) -> T2)): T2? =
                when {
                    array.isNullOrEmpty() -> null
                    depth == 0 -> t1(array as Array<T>)
                    else -> {
                        val nextDepth = depth - 1
                        t2(array.mapNotNull { dispatchTransformToArr(it as? Array<*>, nextDepth, t1, t2) })
                    }
                }

        // object

        override fun handleObject(obj: Any?, f: Field?, v: Any?): SimpleJsonObject? =
                if (v != null || strategy.useNull()) handleObjectInner(v!!, v::class.java) else null

        open fun handleObjectInner(v: Any?, cls: Class<*>): SimpleJsonObject? {
            if (v == null) {
                return JSON_EMPTY_OBJECT
            }
            val fields = getFieldsFromCls(clsCache, cls)
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

    open class ReflectFromJsonTask(var style: JsonStyle, val clsCache: MutableMap<Class<*>, Array<Field>> = mutableMapOf(),
                                   val ctorCache: MutableMap<Class<*>, Constructor<*>> = mutableMapOf()) {
        open fun <T> run(jsonStr: String, cls: Class<*>): T {
            val jsonValue = SimpleJsonParser.fromJsonOrNull(jsonStr, style)
            if (jsonValue !is SimpleJsonObject) {
                throw RuntimeException("this string cannot be transferred to SimpleJsonObject")
            }
            if (jsonValue.isValueNull()) {
                throw RuntimeException("this SimpleJsonObject should not be empty")
            }
            return fromJsonObject(jsonValue, cls) as T
        }

        open fun <T> runOrNull(jsonStr: String, cls: Class<*>): T? {
            val jsonValue = SimpleJsonParser.fromJsonOrNull(jsonStr, style)
            return if (jsonValue !is SimpleJsonObject || jsonValue.isValueNull()) null else {
                fromJsonObject(jsonValue, cls) as? T
            }
        }

        open fun fromJson(jsonValue: SimpleJsonValue<*>, cls: Class<*>?): Any? {
            if (jsonValue.isValueNull()) {
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
                else -> throw RuntimeException("cls should be null when jsonValue's type isn't array or object")
            }
        }

        // 可以处理 char[][] crr; 等等复杂类型，只要 cls 是正确的 -- 返回结果可能是 Array<Array<BooleanArray>> ，也可能是 CharArray
        // TODO: object[] cannot be cast to char[]
        protected fun fromJsonArray(jsonArray: SimpleJsonArray, cls: Class<*>?): Any? {
            if (jsonArray.value() == null) {
                return null
            }
            val temp = findItemType(jsonArray, cls)
            val itemType = temp.first
            val depth = temp.second
            return when (itemType) {
                0 -> null
                1 -> dispatchTransformToArr(jsonArray, depth, { it.value() as Boolean }) { it.toBooleanArray() }
                3 -> dispatchTransformToArr(jsonArray, depth, { it.value() as String }) { it.toTypedArray() }

                6 -> dispatchTransformToArr(jsonArray, depth, { (it.value() as Long).toByte() }) { it.toByteArray() }
                7 -> dispatchTransformToArr(jsonArray, depth, { (it.value() as Long).toShort() }) { it.toShortArray() }
                8 -> dispatchTransformToArr(jsonArray, depth, { (it.value() as Long).toInt() }) { it.toIntArray() }
                9 -> dispatchTransformToArr(jsonArray, depth, { it.value() as Long }) { it.toLongArray() }
                10 -> dispatchTransformToArr(jsonArray, depth, { (it.value() as Double).toFloat() }) { it.toFloatArray() }
                11 -> dispatchTransformToArr(jsonArray, depth, { it.value() as Double }) { it.toDoubleArray() }
                12 -> dispatchTransformToArr(jsonArray, depth, { (it.value() as Long).toChar() }) { it.toCharArray() }

                13 -> dispatchTransformToArr(jsonArray, depth, { it.value() as BigInteger }) { it.toTypedArray() }
                14 -> dispatchTransformToArr(jsonArray, depth, { it.value() as BigDecimal }) { it.toTypedArray() }

                15 -> dispatchTransformToArr(jsonArray, depth, { it.value() as Boolean }) { it.toTypedArray() }
                16 -> dispatchTransformToArr(jsonArray, depth, { (it.value() as Long).toByte() }) { it.toTypedArray() }
                17 -> dispatchTransformToArr(jsonArray, depth, { (it.value() as Long).toShort() }) { it.toTypedArray() }
                18 -> dispatchTransformToArr(jsonArray, depth, { (it.value() as Long).toInt() }) { it.toTypedArray() }
                19 -> dispatchTransformToArr(jsonArray, depth, { it.value() as Long }) { it.toTypedArray() }
                20 -> dispatchTransformToArr(jsonArray, depth, { (it.value() as Double).toFloat() }) { it.toTypedArray() }
                21 -> dispatchTransformToArr(jsonArray, depth, { it.value() as Double }) { it.toTypedArray() }
                22 -> dispatchTransformToArr(jsonArray, depth, { (it.value() as Long).toChar() }) { it.toTypedArray() }

                5 -> {
                    if (cls == null) {
                        throw RuntimeException("cls should not be null when itemType is JsonType.OBJECT")
                    }
                    val clsStr = cls.toString()
                    val itemCls = Class.forName(clsStr.substring(depth + 7, clsStr.length - 1))
//                    dispatchTransformToArr(jsonArray, depth, { fromJsonObject(it as SimpleJsonObject, itemCls) })
                    // 深度优先搜索试试吧
                }
                else -> throw RuntimeException("itemType should not be $itemType, now the depth is $depth")
            }
        }

        protected fun <T, T2> dispatchTransformToArr(jsonArray: SimpleJsonArray, depth: Int, transform: (SimpleJsonValue<*>) -> T,
                                                     transform2: ((List<T>) -> T2)? = null): Any? =
                when {
                    jsonArray.value().isNullOrEmpty() -> null
                    depth == 0 -> {
                        val result = jsonArray.value2().map(transform)
                        if (transform2 == null) result else transform2(result)
                    }
                    else -> {
                        val nextDepth = depth - 1
                        jsonArray.value2().map { dispatchTransformToArr<T, T2>(it as SimpleJsonArray, nextDepth, transform, transform2) }.toTypedArray()
                    }
                }

        /**
         * JsonType.NUL --> 0
         * JsonType.BOOL --> 1(BooleanArray) / 15(Array<Boolean>)
         * JsonType.NUMBER --> 2
         *     JsonNumberType.BYTE -> 6 / 16
         *     JsonNumberType.SHORT -> 7 / 17
         *     JsonNumberType.INT -> 8 / 18
         *     JsonNumberType.LONG -> 9 / 19
         *     JsonNumberType.FLOAT -> 10 / 20
         *     JsonNumberType.DOUBLE -> 11 / 21
         *     JsonNumberType.CHAR -> 12 / 22
         *     JsonNumberType.BIG_INTEGER -> 13
         *     JsonNumberType.BIG_DECIMAL -> 14
         * JsonType.STRING --> 3
         * JsonType.ARRAY --> 4
         * JsonType.OBJECT --> 5
         * @return itemType, depth
         */
        protected fun findItemType(jsonArray: SimpleJsonArray, cls: Class<*>?): Pair<Int, Int> {
            if (cls != null) {
                return findItemTypeByCls(cls)
            }
            if (jsonArray.value().isNullOrEmpty()) {
                return Pair(0, 0)
            }
            val arr = jsonArray.value2()
            val firstItem = arr[0]
            return Pair(when (firstItem.type()) {
                JsonType.NUL -> 0
                JsonType.BOOL -> 1
                JsonType.NUMBER -> {
                    val jsonNumber = firstItem as SimpleJsonNumber
                    when (jsonNumber.numberType()) {
                        JsonNumberType.BYTE -> 6
                        JsonNumberType.SHORT -> 7
                        JsonNumberType.INT -> 8
                        JsonNumberType.LONG -> 9
                        JsonNumberType.FLOAT -> 10
                        JsonNumberType.DOUBLE -> 11
                        JsonNumberType.CHAR -> 12
                        JsonNumberType.BIG_INTEGER -> 13
                        JsonNumberType.BIG_DECIMAL -> 14
                        JsonNumberType.UNKNOWN -> 2
                    }
                }
                JsonType.STRING -> 3
                JsonType.ARRAY -> {
                    var maxDepth = 0
                    arr.forEach {
                        val result = findItemType(it as SimpleJsonArray, cls)
                        val trueDepth = result.second + 1
                        if (result.first != 0) {
                            return Pair(result.first, trueDepth)
                        }
                        if (trueDepth > maxDepth) {
                            maxDepth = trueDepth
                        }
                    }
                    return Pair(0, maxDepth)
                }
                JsonType.OBJECT -> 5
            }, 0)
        }

        protected fun fromJsonObject(jsonObject: SimpleJsonObject, cls: Class<*>): Any? {
            if (jsonObject.isValueNull()) {
                return null
            }
            val fields = getFieldsFromCls(clsCache, cls)
            val result = getDefaultCtorFromCls(ctorCache, cls).newInstance()
            jsonObject.value()!!.forEach {
                val field = fields.find { f -> f.name == it.key } ?: return@forEach
                val jsonValueItem = it.value ?: return@forEach
                if (jsonValueItem.isValueNull()) {
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

    companion object {
        fun <T : Any> toJson(obj: T): String = DEFAULT_REFLECT_JSON_API.toJson(obj)
        fun <T> fromJson(jsonStr: String, cls: Class<*>): T = DEFAULT_REFLECT_JSON_API.fromJson(jsonStr, cls)
        fun <T> fromJsonOrNull(jsonStr: String, cls: Class<*>): T? = DEFAULT_REFLECT_JSON_API.fromJsonOrNull(jsonStr, cls)
    }
}

val DEFAULT_REFLECT_JSON_API = ReflectJsonApi()
