@file:Suppress("MemberVisibilityCanBePrivate")

package com.liang.example.xml_ktx

import java.lang.reflect.Array as RArray
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.math.BigDecimal
import java.math.BigInteger

@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
annotation class XmlRoot(val value: String)

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
    return Pair(
            when {
                temp2.isEmpty() -> when (temp1) {
                    'Z' -> 1
                    'B' -> 2
                    'S' -> 3
                    'I' -> 4
                    'J' -> 5
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
            }, depth
    )
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
                1 -> transformArray2<BooleanArray>(
                        array,
                        depth,
                        { handleBooleanArray(null, null, it?.toTypedArray() as? Array<Boolean?>) },
                        transform2
                )
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

interface XmlInter {
    fun <T> fromXml(xmlStr: String, cls: Class<*>): T
    fun <T> fromXmlOrNull(xmlStr: String, cls: Class<*>): T?
    fun <T : Any> toXml(obj: T): String
    fun <T : Any> toXmlOrNull(obj: T): String?
}

enum class XmlArrayDimenStrategy {
    GET_FIRST,
    CALCULATE,
}

open class ReflectXmlApi(
        var strategy: XmlStrategy = XmlStrategy.SIMPLEST,
        var arrayStrategy: XmlArrayDimenStrategy = XmlArrayDimenStrategy.CALCULATE,
        val clsCache: MutableMap<Class<*>, Array<Field>> = mutableMapOf(),
        val ctorCache: MutableMap<Class<*>, Constructor<*>> = mutableMapOf()
) : XmlInter {
    companion object {
        const val XML_ARRAY_NODE = "xml.array.node"
        const val XML_ITEM_NODE = "xml.item.node"
    }

    open class ReflectToXmlTask(val clsCache: MutableMap<Class<*>, Array<Field>>) : ReflectHandleInter<EasyXmlNode> {
        // basic

        override fun handleBoolean(obj: Any?, f: Field?, v: Boolean?): EasyXmlNode? = when {
            v != null -> EasyXmlAttribute(f?.name ?: XML_ITEM_NODE, EasyXmlBool.valueOf(v))
            else -> null
        }

        protected fun processXmlNumber(f: Field?, v: Number?, nt: XmlNumberType): EasyXmlNode? = when {
            v != null -> EasyXmlAttribute(f?.name ?: XML_ITEM_NODE, EasyXmlNumber(v, nt))
            else -> null
        }

        override fun handleByte(obj: Any?, f: Field?, v: Byte?): EasyXmlNode? = processXmlNumber(f, v, XmlNumberType.BYTE)
        override fun handleShort(obj: Any?, f: Field?, v: Short?): EasyXmlNode? = processXmlNumber(f, v, XmlNumberType.SHORT)
        override fun handleInt(obj: Any?, f: Field?, v: Int?): EasyXmlNode? = processXmlNumber(f, v, XmlNumberType.INT)
        override fun handleLong(obj: Any?, f: Field?, v: Long?): EasyXmlNode? = processXmlNumber(f, v, XmlNumberType.LONG)
        override fun handleFloat(obj: Any?, f: Field?, v: Float?): EasyXmlNode? = processXmlNumber(f, v, XmlNumberType.FLOAT)
        override fun handleDouble(obj: Any?, f: Field?, v: Double?): EasyXmlNode? = processXmlNumber(f, v, XmlNumberType.DOUBLE)
        override fun handleBigInteger(obj: Any?, f: Field?, v: BigInteger?): EasyXmlNode? = processXmlNumber(f, v, XmlNumberType.BIG_INTEGER)
        override fun handleBigDecimal(obj: Any?, f: Field?, v: BigDecimal?): EasyXmlNode? = processXmlNumber(f, v, XmlNumberType.BIG_DECIMAL)

        override fun handleChar(obj: Any?, f: Field?, v: Char?): EasyXmlNode? = processXmlNumber(f, v?.toLong(), XmlNumberType.CHAR)  // 处理特殊字符

        override fun handleString(obj: Any?, f: Field?, v: String?): EasyXmlNode? = when {  // 处理特殊字符
            v != null -> EasyXmlAttribute(f?.name ?: XML_ITEM_NODE, EasyXmlString(v))
            else -> null
        }

        // basic array

        protected fun <T : Any> processArray(f: Field?, v: Array<T?>?): EasyXmlNode? = when {
            v != null -> EasyXmlAttribute(f?.name ?: XML_ITEM_NODE, EasyXmlString(v.joinToString(",") { it?.toString() ?: "null" }))
            else -> null
        }

        override fun handleBooleanArray(obj: Any?, f: Field?, v: Array<Boolean?>?): EasyXmlNode? = processArray(f, v)
        override fun handleByteArray(obj: Any?, f: Field?, v: Array<Byte?>?): EasyXmlNode? = processArray(f, v)
        override fun handleShortArray(obj: Any?, f: Field?, v: Array<Short?>?): EasyXmlNode? = processArray(f, v)

        override fun handleIntArray(obj: Any?, f: Field?, v: Array<Int?>?): EasyXmlNode? = processArray(f, v)
        override fun handleLongArray(obj: Any?, f: Field?, v: Array<Long?>?): EasyXmlNode? = processArray(f, v)
        override fun handleFloatArray(obj: Any?, f: Field?, v: Array<Float?>?): EasyXmlNode? = processArray(f, v)
        override fun handleDoubleArray(obj: Any?, f: Field?, v: Array<Double?>?): EasyXmlNode? = processArray(f, v)
        override fun handleCharArray(obj: Any?, f: Field?, v: Array<Char?>?): EasyXmlNode? = processArray(f, v)
        override fun handleStringArray(obj: Any?, f: Field?, v: Array<String?>?): EasyXmlNode? = processArray(f, v)
        override fun handleBigIntegerArray(obj: Any?, f: Field?, v: Array<BigInteger?>?): EasyXmlNode? = processArray(f, v)
        override fun handleBigDecimalArray(obj: Any?, f: Field?, v: Array<BigDecimal?>?): EasyXmlNode? = processArray(f, v)

        // object array

        override fun handleArray(obj: Any?, f: Field?, v: Array<*>?): EasyXmlNode? {
            val result = handleArrayInner(v) {
                val result = EasyXmlElement(XML_ARRAY_NODE)
                it.forEach { node ->
                    when (node) {
                        is EasyXmlAttribute -> result.add(attrToElement(node))
                        is EasyXmlElement -> result.add(node)
                        null -> Unit
                        else -> throw RuntimeException("impossible node's type: ${node.elementType()}")
                    }
                }
                result
            }
            (result as? EasyXmlElement)?.mTag = f?.name ?: XML_ARRAY_NODE
            return result
        }

        protected fun attrToElement(node: EasyXmlAttribute): EasyXmlElement {
            val element = EasyXmlElement(node.mName)
            element.add(EasyXmlText(node.mValue.string()))
            return element
        }

        // object

        override fun handleObject(obj: Any?, f: Field?, v: Any?): EasyXmlNode? {
            if (v == null) {
                return null
            }
            val fields = getFieldsFromCls(clsCache, v::class.java)
            if (fields.isEmpty()) {
                return null
            }
            val result = EasyXmlElement(f?.name ?: XML_ITEM_NODE)
            fields.forEach {
                val temp = handleByField(it, v)
                when {
                    temp is EasyXmlElement -> result.add(temp)
                    temp is EasyXmlAttribute -> result.addAttr(temp)
                    temp != null -> throw RuntimeException("impossible node's type: ${temp.elementType()}")
                }
            }
            return result
        }

        override fun handleByCls(cls: Class<*>, v: Any): EasyXmlNode? {
            var result = super.handleByCls(cls, v)
            if (result is EasyXmlAttribute) {
                result = attrToElement(result)
            }
            return result
        }
    }

    open class ReflectFromXmlTask(
            var arrayStrategy: XmlArrayDimenStrategy = XmlArrayDimenStrategy.CALCULATE,
            val clsCache: MutableMap<Class<*>, Array<Field>>, val ctorCache: MutableMap<Class<*>, Constructor<*>>
    ) {
        // use dom parser
    }

    override fun <T> fromXml(xmlStr: String, cls: Class<*>): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T> fromXmlOrNull(xmlStr: String, cls: Class<*>): T? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any> toXml(obj: T): String = toXmlInner(obj, "")!!
    override fun <T : Any> toXmlOrNull(obj: T): String? = toXmlInner(obj, null)
    protected fun <T : Any> toXmlInner(obj: T, default: String?): String? {
        val cls = obj::class.java
        val result = ReflectToXmlTask(clsCache).handleByCls(cls, obj) ?: return default
        if (result is EasyXmlNode2) {
            result.setRightStrategy(strategy)
            result.setRightDepth(0)
        }
        val rootAnnotation = cls.getAnnotation(XmlRoot::class.java)
        if (rootAnnotation != null) {
            (result as EasyXmlElement).mTag = rootAnnotation.value
        }
        return result.string()
    }
}

// TODO:
