@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.liang.example.json_inflater

import android.R
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.LevelListDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.text.TextUtils
import android.util.LruCache
import android.util.StateSet
import android.util.TypedValue
import android.view.ViewGroup
import androidx.annotation.StringDef
import com.liang.example.basic_ktx.KKMap
import com.liang.example.json_inflater.NullV.Companion.nullV
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set

abstract class Value {
    abstract fun copy(): Value
    abstract fun string(): String
}

open class ArrayV : Value {
    protected var values: MutableList<Value?>

    constructor() {
        values = ArrayList()
    }

    constructor(size: Int) {
        values = ArrayList(size)
    }

    constructor(values: Array<Value?>) {
        this.values = values.toMutableList()
    }

    constructor(values: List<Value?>) {
        this.values = values.toMutableList()
    }

    override fun copy(): ArrayV = ArrayV(values)
    override fun string(): String = '[' + values.joinToString { it?.string() ?: nullV.string() } + ']'

    open fun add(value: Boolean?) = values.add(if (value == null) nullV else PrimitiveV(value))
    open fun add(value: String?) = values.add(if (value == null) nullV else PrimitiveV(value))
    open fun add(value: Char?) = values.add(if (value == null) nullV else PrimitiveV(value))
    open fun add(value: Number?) = values.add(if (value == null) nullV else PrimitiveV(value))
    open fun add(value: Value?, pos: Int = -1) = values.add(if (pos == -1) values.size else pos, value ?: nullV)
    open fun addAll(value: ArrayV) = values.addAll(value.values)
    open operator fun set(pos: Int, value: Value?) = values.set(pos, value)
    open operator fun get(pos: Int) = values[pos]
    open fun remove(value: Value) = values.remove(value)
    open fun removeAll(value: ArrayV) = values.removeAll(value.values)
    open fun remove(pos: Int) = values.removeAt(pos)
    open operator fun contains(value: Value) = value in values
    open fun containsAll(value: ArrayV) = values.containsAll(value.values)
    open fun size() = values.size
    open operator fun iterator() = values.iterator()
    open fun values() = values.toMutableList()

    override fun equals(other: Any?): Boolean = other === this || other is ArrayV && other.values == values
    override fun hashCode(): Int = values.hashCode()

    open fun getBoolean(pos: Int) = if (values[pos] is PrimitiveV) (values[pos] as PrimitiveV).toBoolean() else false
    open fun getChar(pos: Int) = if (values[pos] is PrimitiveV) (values[pos] as PrimitiveV).toChar() else null
    open fun getByte(pos: Int) = if (values[pos] is PrimitiveV) (values[pos] as PrimitiveV).toByte() else null
    open fun getShort(pos: Int) = if (values[pos] is PrimitiveV) (values[pos] as PrimitiveV).toShort() else null
    open fun getInt(pos: Int) = if (values[pos] is PrimitiveV) (values[pos] as PrimitiveV).toInt() else null
    open fun getLong(pos: Int) = if (values[pos] is PrimitiveV) (values[pos] as PrimitiveV).toLong() else null
    open fun getFloat(pos: Int) = if (values[pos] is PrimitiveV) (values[pos] as PrimitiveV).toFloat() else null
    open fun getDouble(pos: Int) = if (values[pos] is PrimitiveV) (values[pos] as PrimitiveV).toDouble() else null
    open fun getBigInteger(pos: Int) = if (values[pos] is PrimitiveV) (values[pos] as PrimitiveV).toBigInteger() else null
    open fun getBigDecimal(pos: Int) = if (values[pos] is PrimitiveV) (values[pos] as PrimitiveV).toBigDecimal() else null
    open fun getString(pos: Int) = if (values[pos] is PrimitiveV) (values[pos] as PrimitiveV).string() else null
    open fun getArray(pos: Int) = if (values[pos] is ArrayV) values[pos] as ArrayV else null
    open fun getObject(pos: Int) = if (values[pos] is ObjectV) values[pos] as ObjectV else null
}

open class ObjectV : Value {
    protected val values: MutableMap<String, Value?>

    constructor() {
        values = mutableMapOf()
    }

    constructor(values: MutableMap<String, Value?>) {
        this.values = values
    }

    override fun string(): String = '{' + values.map {
        "\"${it.key}\": \"${it.value?.string() ?: nullV.string()}\""
    }.joinToString() + '}'

    override fun copy(): ObjectV {
        val result = ObjectV()
        this.values.forEach { e -> result.values[e.key] = e.value }
        return result
    }

    open fun add(property: String, value: Boolean?) = values.put(property, if (value != null) PrimitiveV(value) else nullV)
    open fun add(property: String, value: String?) = values.put(property, if (value != null) PrimitiveV(value) else nullV)
    open fun add(property: String, value: Char?) = values.put(property, if (value != null) PrimitiveV(value) else nullV)
    open fun add(property: String, value: Number?) = values.put(property, if (value != null) PrimitiveV(value) else nullV)
    open fun add(property: String, value: Value?) = values.put(property, value ?: nullV)
    open fun addAll(value: ObjectV) = values.putAll(value.values)
    open operator fun get(property: String) = values[property]
    open operator fun set(property: String, value: Value?) = add(property, value)
    open fun remove(property: String) = values.remove(property)
    open fun entries() = values.entries
    open fun size() = values.size
    open operator fun contains(property: String) = values.containsKey(property)
    open operator fun contains(value: Value) = values.containsValue(value)
    open operator fun iterator() = values.iterator()

    override fun equals(other: Any?): Boolean = this === other || other is ObjectV && other.values == values
    override fun hashCode(): Int = values.hashCode()

    open fun getBoolean(property: String) = if (values[property] is PrimitiveV) (values[property] as PrimitiveV).toBoolean() else null
    open fun getChar(property: String) = if (values[property] is PrimitiveV) (values[property] as PrimitiveV).toChar() else null
    open fun getByte(property: String) = if (values[property] is PrimitiveV) (values[property] as PrimitiveV).toByte() else null
    open fun getShort(property: String) = if (values[property] is PrimitiveV) (values[property] as PrimitiveV).toShort() else null
    open fun getInt(property: String) = if (values[property] is PrimitiveV) (values[property] as PrimitiveV).toInt() else null
    open fun getLong(property: String) = if (values[property] is PrimitiveV) (values[property] as PrimitiveV).toLong() else null
    open fun getFloat(property: String) = if (values[property] is PrimitiveV) (values[property] as PrimitiveV).toFloat() else null
    open fun getDouble(property: String) = if (values[property] is PrimitiveV) (values[property] as PrimitiveV).toDouble() else null
    open fun getBigInteger(property: String) = if (values[property] is PrimitiveV) (values[property] as PrimitiveV).toBigInteger() else null
    open fun getBigDecimal(property: String) = if (values[property] is PrimitiveV) (values[property] as PrimitiveV).toBigDecimal() else null
    open fun getString(property: String) = if (values[property] is PrimitiveV) (values[property] as PrimitiveV).string() else null
    open fun getArray(property: String) = if (values[property] is ArrayV) values[property] as ArrayV else null
    open fun getObject(property: String) = if (values[property] is ObjectV) values[property] as ObjectV else null
}

open class NullV private constructor() : Value() {
    override fun copy(): NullV = this
    override fun toString(): String = "null"
    override fun string() = "null"
    override fun hashCode(): Int = NullV::class.java.hashCode()
    override fun equals(other: Any?): Boolean = this === other || other is NullV

    companion object {
        val nullV = NullV()
    }
}

open class PrimitiveV : Value {
    companion object {
        val PRIMITIVE_TYPES = arrayOf(Byte::class.javaPrimitiveType, Short::class.javaPrimitiveType, Int::class.javaPrimitiveType, Long::class.javaPrimitiveType,
                Float::class.javaPrimitiveType, Double::class.javaPrimitiveType, Boolean::class.javaPrimitiveType, Char::class.javaPrimitiveType,
                Byte::class.java, Short::class.java, Int::class.java, Long::class.java, Float::class.java, Double::class.java, Boolean::class.java, Char::class.java,
                BigInteger::class.java, BigDecimal::class.java)

        val INTEGER_PRIMITIVE_TYPES = arrayOf(Byte::class.javaPrimitiveType, Short::class.javaPrimitiveType, Int::class.javaPrimitiveType, Long::class.javaPrimitiveType,
                Byte::class.java, Short::class.java, Int::class.java, Long::class.java, Float::class.java, BigInteger::class.java)
        val DECIMAL_PRIMITIVE_TYPES = arrayOf(Float::class.javaPrimitiveType, Double::class.javaPrimitiveType, Float::class.java, Double::class.java, BigDecimal::class.java)

        val EMPTY_STR = PrimitiveV("")
        val TRUE_V = PrimitiveV(true)
        val FALSE_V = PrimitiveV(false)
    }

    protected var value: Any

    constructor(value: String) {
        this.value = value
    }

    constructor(value: Number) {
        if (value::class.java !in PRIMITIVE_TYPES) {
            throw IllegalArgumentException()
        }
        this.value = value
    }

    constructor(value: Char) {
        this.value = java.lang.String.valueOf(value)
    }

    constructor(value: Boolean) {
        this.value = value
    }

    override fun copy(): PrimitiveV = this

    open fun isBoolean() = value is Boolean
    open fun isNumber() = value is Number
    open fun isString() = value is String

    override fun string(): String = if (value is String) value as String else value.toString()
    open fun toBoolean() = if (value is Boolean) value as Boolean else java.lang.Boolean.parseBoolean(value as String)
    open fun toNumber() = if (value is Number) value as Number else LazilyParsedNumber(value as String)

    open fun toChar() = if (value is Number) (value as Number).toChar() else (value as String).toCharArray()[0]
    open fun toByte() = if (value is Number) (value as Number).toByte() else (value as String).toByte()
    open fun toShort() = if (value is Number) (value as Number).toShort() else (value as String).toShort()
    open fun toInt() = if (value is Number) (value as Number).toInt() else (value as String).toInt()
    open fun toLong() = if (value is Number) (value as Number).toLong() else (value as String).toLong()
    open fun toFloat() = if (value is Number) (value as Number).toFloat() else (value as String).toFloat()
    open fun toDouble() = if (value is Number) (value as Number).toDouble() else (value as String).toDouble()
    open fun toBigInteger() = if (value is Number) value as BigInteger else (value as String).toBigInteger()
    open fun toBigDecimal() = if (value is Number) value as BigDecimal else (value as String).toBigDecimal()

    override fun hashCode(): Int {
        if (value is Number && value::class.java in PRIMITIVE_TYPES) {
            val temp = toLong()
            return (temp xor (temp shl 32)).toInt()
        }
        return value.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || other !is PrimitiveV) {
            return false
        }
        if (value is Number && value::class.java in INTEGER_PRIMITIVE_TYPES && other.value is Number && other::class.java in INTEGER_PRIMITIVE_TYPES) {
            return toLong() == other.toLong()
        }
        if (value is Number && value::class.java in DECIMAL_PRIMITIVE_TYPES && other.value is Number && other::class.java in DECIMAL_PRIMITIVE_TYPES) {
            val d1 = toDouble()
            val d2 = other.toDouble()
            return d1 == d2 || java.lang.Double.isNaN(d1) && java.lang.Double.isNaN(d2)
        }
        return super.equals(other)
    }

    override fun toString(): String = string()
    open fun singleQuotedString(): String? = "'${string()}'"
    open fun doubleQuotedString(): String? = "\"${string()}\""

    class LazilyParsedNumber(val value: String) : Number() {
        override fun toByte(): Byte = value.toByte()
        override fun toChar(): Char = value.toCharArray()[0]
        override fun toDouble(): Double = value.toDouble()
        override fun toFloat(): Float = value.toFloat()
        override fun toInt(): Int = value.toInt()
        override fun toLong(): Long = value.toLong()
        override fun toShort(): Short = value.toShort()
        fun toBigInteger(): BigInteger = value.toBigInteger()
        fun toBigDecimal(): BigDecimal = value.toBigDecimal()

        override fun toString(): String = value
        override fun hashCode(): Int = value.hashCode()
        override fun equals(other: Any?): Boolean = this === other || other is LazilyParsedNumber && other.value == value
    }
}

open class LayoutV(
        open var type: String,
        open var attributes: MutableList<Attribute>?,
        open var data: MutableMap<String, Value>?,
        open var extra: ObjectV?
) : Value() {

    override fun copy(): LayoutV = LayoutV(type, attributes?.map { it.copy() }?.toMutableList(), data, extra)
    override fun string(): String {
        var result = "{\"type\": \"${type}\""
        attributes?.forEach { it ->
            TODO()
        }
        data?.forEach { (name, value) ->
            TODO()
        }
        """"extra": "${extra?.string() ?: "\"null\""}""""
        return "$result}"
    }

    open fun merge(include: LayoutV): LayoutV {
        val attributes = this.attributes?.map { it.copy() }?.toMutableList()
        include.attributes?.let { attributes?.addAll(it) }
        val data = this.data?.map { it.key to it.value.copy() }?.toMap()?.toMutableMap()
        include.data?.let { data?.putAll(it) }
        val extra = this.extra?.copy()
        include.extra?.let { extra?.addAll(it) }
        return LayoutV(type, attributes, data, extra)
    }

    open class Attribute(open var id: Int, open var value: Value) {
        open fun copy(): Attribute = Attribute(id, value.copy())
    }
}

@Suppress("LeakingThis")
open class DimensionV : Value {
    companion object {
        const val DIMENSION_UNIT_ENUM = -1
        const val DIMENSION_UNIT_PX = TypedValue.COMPLEX_UNIT_PX
        const val DIMENSION_UNIT_DP = TypedValue.COMPLEX_UNIT_DIP
        const val DIMENSION_UNIT_SP = TypedValue.COMPLEX_UNIT_SP
        const val DIMENSION_UNIT_PT = TypedValue.COMPLEX_UNIT_PT
        const val DIMENSION_UNIT_IN = TypedValue.COMPLEX_UNIT_IN
        const val DIMENSION_UNIT_MM = TypedValue.COMPLEX_UNIT_MM

        const val MATCH_PARENT = "match_parent"
        const val FILL_PARENT = "fill_parent"
        const val WRAP_CONTENT = "wrap_content"

        const val SUFFIX_PX = "px"
        const val SUFFIX_DP = "dp"
        const val SUFFIX_SP = "sp"
        const val SUFFIX_PT = "pt"
        const val SUFFIX_IN = "in"
        const val SUFFIX_MM = "mm"

        val sDimensionsMap: KKMap<String, Int> = KKMap(3)
        val sDimensionsUnitsMap: KKMap<String, Int> = KKMap(6)
        val ZERO = DimensionV(0.0, DIMENSION_UNIT_PX)

        val cache = LruCache<String, DimensionV>(64)

        init {
            sDimensionsMap[FILL_PARENT] = ViewGroup.LayoutParams.MATCH_PARENT
            sDimensionsMap[MATCH_PARENT] = ViewGroup.LayoutParams.MATCH_PARENT
            sDimensionsMap[WRAP_CONTENT] = ViewGroup.LayoutParams.WRAP_CONTENT

            sDimensionsUnitsMap[SUFFIX_PX] = DIMENSION_UNIT_PX
            sDimensionsUnitsMap[SUFFIX_DP] = DIMENSION_UNIT_DP
            sDimensionsUnitsMap[SUFFIX_SP] = DIMENSION_UNIT_SP
            sDimensionsUnitsMap[SUFFIX_PT] = DIMENSION_UNIT_PT
            sDimensionsUnitsMap[SUFFIX_IN] = DIMENSION_UNIT_IN
            sDimensionsUnitsMap[SUFFIX_MM] = DIMENSION_UNIT_MM
        }

        fun valueOf(dimension: String?): DimensionV = if (null == dimension) {
            cache.put(dimension, ZERO)
            ZERO
        } else cache[dimension] ?: DimensionV(dimension).cache()

        fun apply(dimension: String?, context: Context): Float = valueOf(dimension).apply(context)
    }

    open val value: Double
    open val unit: Int
    open var dimension: String? = null

    constructor(value: Double, unit: Int) {
        this.value = value
        this.unit = unit
        cache.put(toString(), this)
    }

    constructor(dimension: String) {
        val parameter = sDimensionsMap[dimension]
        val value: Double
        val unit: Int

        if (parameter != null) {
            value = parameter.toDouble()
            unit = DIMENSION_UNIT_ENUM
        } else {
            val length: Int = dimension.length
            if (length < 2) {
                value = 0.0
                unit = DIMENSION_UNIT_PX
            } else {
                // find the units and value by splitting at the second-last character of the dimension
                val u = sDimensionsUnitsMap[dimension.substring(length - 2)]
                val stringValue: String = dimension.substring(0, length - 2)
                if (u != null) {
                    value = stringValue.toDoubleOrNull() ?: 0.0
                    unit = u
                } else {
                    value = 0.0
                    unit = DIMENSION_UNIT_PX
                }
            }
        }

        this.value = value
        this.unit = unit
        this.dimension = dimension
    }

    open fun cache(): DimensionV = if (dimension == null) this else cache.put(dimension, this)

    override fun copy(): DimensionV = this
    override fun string(): String = dimension ?: TODO()

    open fun apply(context: Context): Float {
        var result = 0.0
        when (unit) {
            DIMENSION_UNIT_ENUM -> result = value
            DIMENSION_UNIT_PX,
            DIMENSION_UNIT_DP,
            DIMENSION_UNIT_SP,
            DIMENSION_UNIT_PT,
            DIMENSION_UNIT_MM,
            DIMENSION_UNIT_IN -> result = TypedValue.applyDimension(unit, value.toFloat(), context.resources.displayMetrics).toDouble()
        }
        return result.toFloat()
    }

    override fun toString(): String {
        return if (this.unit == DIMENSION_UNIT_ENUM) {
            sDimensionsMap.getVK(this.value.toInt())!!
        } else {
            if (this.value % 1 == 0.0) {
                this.value.toInt().toString()
            } else {
                this.value.toString()
            } + sDimensionsUnitsMap.getVK(this.unit)!!
        }
    }
}

/**
 * android:minWidth="?android:textSize" 这种
 */
@Suppress("LeakingThis")
open class StyleResourceV : Value {
    companion object {
        val NULL = StyleResourceV(-1, -1)
        val styleMap: MutableMap<String, Int> = HashMap()
        val attributeMap: MutableMap<String, Int> = HashMap()
        val sHashMap: MutableMap<String, Class<*>> = HashMap()
        const val ATTR_START_LITERAL = "?"

        fun isStyleResource(value: String): Boolean = value.startsWith(ATTR_START_LITERAL)

        val cache = LruCache<String, StyleResourceV>(64)

        fun valueOf(value: String, context: Context): StyleResourceV? = cache[value] ?: try {
            StyleResourceV(value, context).cache()
        } catch (e: Exception) {
            cache.put(value, NULL)
            null
        }
    }

    open val styleId: Int
    open val attributeId: Int
    open var strStyleAttr: String? = null

    constructor(styleId: Int, attributeId: Int) {
        this.styleId = styleId
        this.attributeId = attributeId
    }

    @Throws(java.lang.IllegalArgumentException::class, NoSuchFieldException::class, IllegalAccessException::class, ClassNotFoundException::class)
    constructor(value: String, context: Context) {
        val tokens = value.substring(1, value.length).split(":").toTypedArray()
        val style = tokens[0]
        val attr = tokens[1]
        var clazz: Class<*>?
        var styleId = styleMap[style]
        if (styleId == null) {
            val className = context.packageName + ".R\$style"
            clazz = sHashMap[className]
            if (null == clazz) {
                clazz = Class.forName(className)
                sHashMap[className] = clazz
            }
            styleId = clazz.getField(style).getInt(null)
            styleMap[style] = styleId
        }
        this.styleId = styleId
        var attrId = attributeMap[attr]
        if (attrId == null) {
            val className = context.packageName + ".R\$attr"
            clazz = sHashMap[className]
            if (null == clazz) {
                clazz = Class.forName(className)
                sHashMap[className] = clazz
            }
            attrId = clazz.getField(attr).getInt(null)
            attributeMap[attr] = attrId
        }
        this.attributeId = attrId
        this.strStyleAttr = value
    }

    open fun cache(): StyleResourceV = if (strStyleAttr == null) this else cache.put(strStyleAttr, this)
    override fun string(): String = strStyleAttr ?: TODO()

    override fun copy(): StyleResourceV = this

    fun apply(context: Context): TypedArray = context.obtainStyledAttributes(styleId, intArrayOf(attributeId))
}

abstract class ColorV : Value() {
    companion object {
        const val COLOR_PREFIX_LITERAL = "#"
        val sColorNameMap: MutableMap<String, Int> = mutableMapOf("black" to Color.BLACK, "darkgray" to Color.DKGRAY, "gray" to Color.GRAY,
                "lightgray" to Color.LTGRAY, "white" to Color.WHITE, "red" to Color.RED, "green" to Color.GREEN, "blue" to Color.BLUE,
                "yellow" to Color.YELLOW, "cyan" to Color.CYAN, "magenta" to Color.MAGENTA, "aqua" to 0xFF00FFFF.toInt(),
                "fuchsia" to 0xFFFF00FF.toInt(), "darkgrey" to Color.DKGRAY, "grey" to Color.GRAY, "lightgrey" to Color.LTGRAY,
                "lime" to 0xFF00FF00.toInt(), "maroon" to 0xFF800000.toInt(), "navy" to 0xFF000080.toInt(), "olive" to 0xFF808000.toInt(),
                "purple" to 0xFF800080.toInt(), "silver" to 0xFFC0C0C0.toInt(), "teal" to 0xFF008080.toInt())
        val sAttributesMap: HashMap<String, Int> = HashMap(15)
        val cache: LruCache<String, ColorV> = LruCache(64)

        fun apply(value: String?): Int = when (val color = valueOf(value)) {
            is IntV -> color.value
            else -> IntV.BLACK.value
        }

        fun valueOf(value: String?, defValue: ColorV = IntV.BLACK): ColorV = when {
            value.isNullOrEmpty() -> defValue
            else -> cache.get(value) ?: when {
                value in sColorNameMap -> IntV(sColorNameMap[value]!!).cache()
                value.startsWith(COLOR_PREFIX_LITERAL) -> IntV(Color.parseColor(value)).cache()
                else -> defValue
            }
        }

        fun isColor(color: String): Boolean = color.startsWith(COLOR_PREFIX_LITERAL)

        fun modulateColorAlpha(baseColor: Int, alphaMod: Float): Int = when (alphaMod) {
            1.0f -> baseColor
            else -> baseColor and 0xFFFFFF or (constrain(((baseColor shr 24) * alphaMod + 0.5f).toInt(), 0, 255) shl 24)
        }

        fun constrain(amount: Int, low: Int, high: Int): Int = when {
            amount < low -> low
            amount > high -> high
            else -> amount
        }

        fun idealByteArraySize(need: Int): Int {
            Int.MAX_VALUE
            (4..31).forEach {
                val temp = 1 shl it - 12
                if (need <= temp) {
                    return temp
                }
            }
            return need
        }

        fun idealIntArraySize(need: Int): Int = idealByteArraySize(need * 4) / 4

        /**
         * {
         *     "type": "selector",
         *     "children": [
         *         {
         *             "type": "item",
         *             "color": "#000000",
         *             "alpha": "0.50"
         *             "state_pressed": "true"
         *         },
         *         {
         *             /* ...类似 */
         *         }
         *     ]
         * }
         */
        fun valueOf(value: ObjectV): ColorV {
            if (value["type"] !is PrimitiveV || !TextUtils.equals((value["type"] as PrimitiveV).string(), "selector") || value["children"] !is ArrayV) {
                return IntV.BLACK
            }

            val iterator = (value["children"] as ArrayV).iterator()
            var child: ObjectV
            var listAllocated = 20
            var listSize = 0
            var colorList = IntArray(listAllocated)
            var stateSpecList = arrayOfNulls<IntArray>(listAllocated)

            parseColorStateList@ while (iterator.hasNext()) {
                child = iterator.next() as ObjectV
                if (child.size() == 0) {
                    continue
                }
                var j = 0
                var baseColor: Int? = null
                var alphaMod = 1.0f
                val stateSpec = IntArray(child.size() - 1)

                parseItem@ for ((key, value1) in child.entries()) {
                    if (value1 !is PrimitiveV) {
                        continue
                    }
                    val attributeId = sAttributesMap[key] ?: continue
                    when (attributeId) {
                        R.attr.type -> if (!TextUtils.equals("item", value1.string())) continue@parseColorStateList
                        R.attr.color -> {
                            val colorRes = value1.string()
                            if (!TextUtils.isEmpty(colorRes)) {
                                baseColor = apply(colorRes)
                            }
                        }
                        R.attr.alpha -> {
                            val alphaStr = value1.string()
                            if (!TextUtils.isEmpty(alphaStr)) {
                                alphaMod = alphaStr.toFloat()
                            }
                        }
                        else -> stateSpec[j++] = if (value1.toBoolean()) attributeId else -attributeId
                    }
                }

                checkNotNull(baseColor) { "No ColorValue Specified" }
                if (listSize + 1 >= listAllocated) {
                    listAllocated = idealIntArraySize(listSize + 1)
                    val colorTempArr = IntArray(listAllocated)
                    System.arraycopy(colorList, 0, colorTempArr, 0, listSize)
                    val stateTempArr = arrayOfNulls<IntArray>(listAllocated)
                    System.arraycopy(stateSpecList, 0, stateTempArr, 0, listSize)
                    colorList = colorTempArr
                    stateSpecList = stateTempArr
                }
                colorList[listSize] = modulateColorAlpha(baseColor, alphaMod)
                stateSpecList[listSize] = StateSet.trimStateSet(stateSpec, j)
                listSize++
            }

            if (listSize > 0) {
                val colors = IntArray(listSize)
                val stateSpecs = arrayOfNulls<IntArray>(listSize)
                System.arraycopy(colorList, 0, colors, 0, listSize)
                System.arraycopy(stateSpecList, 0, stateSpecs, 0, listSize)
                return StateList(colors, stateSpecs)
            }
            return IntV.BLACK
        }

        init {
            sAttributesMap["type"] = R.attr.type
            sAttributesMap["color"] = R.attr.color
            sAttributesMap["alpha"] = R.attr.alpha
            sAttributesMap["state_pressed"] = R.attr.state_pressed
            sAttributesMap["state_focused"] = R.attr.state_focused
            sAttributesMap["state_selected"] = R.attr.state_selected
            sAttributesMap["state_checkable"] = R.attr.state_checkable
            sAttributesMap["state_checked"] = R.attr.state_checked
            sAttributesMap["state_enabled"] = R.attr.state_enabled
            sAttributesMap["state_window_focused"] = R.attr.state_window_focused
        }
    }

    abstract fun apply(context: Context): Result

    @Suppress("LeakingThis")
    open class IntV(open val value: Int) : ColorV() {
        companion object {
            val BLACK = IntV(0).cache()

            fun valueOf(number: Int): IntV = when (number) {
                0 -> BLACK
                else -> cache.get(number.toString()) as? IntV ?: IntV(number).cache()
            }
        }

        open fun cache(): IntV = cache.put(value.toString(), this) as IntV

        override fun copy(): IntV = this
        override fun apply(context: Context): Result = Result(value, null)
        override fun string(): String = TODO()
    }

    open class StateList(open val colors: IntArray, open val states: Array<IntArray?>) : ColorV() {
        override fun copy(): StateList = this
        override fun apply(context: Context): Result = Result(null, ColorStateList(states, colors))
        override fun string(): String = TODO()
    }

    data class Result(val color: Int?, val colors: ColorStateList?)
}

@Suppress("LeakingThis")
open class AttributeResourceV : Value {
    companion object {
        val NULL = AttributeResourceV(-1)

        const val ATTR_START_LITERAL = "?"
        const val ATTR_LITERAL = "attr/"
        val attributePattern: Pattern = Pattern.compile("(\\?)(\\S*)(:?)(attr/?)(\\S*)", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val sHashMap: MutableMap<String, Class<*>> = HashMap()

        fun isAttributeResource(value: String): Boolean = value.startsWith(ATTR_START_LITERAL) && value.contains(ATTR_LITERAL)

        val cache = LruCache<String, AttributeResourceV>(16)

        fun valueOf(value: String, context: Context): AttributeResourceV? = cache[value] ?: try {
            AttributeResourceV(value, context).cache()
        } catch (e: java.lang.Exception) {
            cache.put(value, NULL)
            null
        }
    }

    open val attributeId: Int
    open var strAttr: String? = null

    constructor(attributeId: Int) {
        this.attributeId = attributeId
    }

    constructor(value: String, context: Context) {
        var packageName: String? = null
        val matcher = attributePattern.matcher(value)
        val attributeName: String = when {
            matcher.matches() -> {
                packageName = matcher.group(2)
                matcher.group(5)!!
            }
            else -> value.substring(1)
        }
        var clazz: Class<*>?
        packageName = when {
            packageName.isNullOrEmpty() -> context.packageName
            else -> packageName.substring(0, packageName.length - 1)
        }
        val className = "$packageName.R\$attr"
        clazz = sHashMap[className]
        if (null == clazz) {
            clazz = Class.forName(className)
            sHashMap[className] = clazz
        }
        attributeId = clazz.getField(attributeName).getInt(null)
    }

    open fun cache(): AttributeResourceV = if (strAttr == null) this else cache.put(strAttr, this)
    override fun copy(): AttributeResourceV = this
    open fun apply(context: Context): TypedArray = context.obtainStyledAttributes(intArrayOf(attributeId))
    override fun string(): String = strAttr ?: TODO()
}

open class ResourceV(open val resId: Int) : Value() {
    companion object {
        const val RESOURCE_PREFIX_ANIMATION = "@anim/"
        const val RESOURCE_PREFIX_BOOLEAN = "@bool/"
        const val RESOURCE_PREFIX_COLOR = "@color/"
        const val RESOURCE_PREFIX_DIMENSION = "@dimen/"
        const val RESOURCE_PREFIX_DRAWABLE = "@drawable/"
        const val RESOURCE_PREFIX_STRING = "@string/"

        const val ANIM = "anim"
        const val BOOLEAN = "bool"
        const val COLOR = "color"
        const val DIMEN = "dimen"
        const val DRAWABLE = "drawable"
        const val STRING = "string"

        val NOT_FOUND: ResourceV = ResourceV(0)
        val cache: LruCache<String, ResourceV> = LruCache(64)

        fun isAnimation(string: String): Boolean = string.startsWith(RESOURCE_PREFIX_ANIMATION)
        fun isBoolean(string: String): Boolean = string.startsWith(RESOURCE_PREFIX_BOOLEAN)
        fun isColor(string: String): Boolean = string.startsWith(RESOURCE_PREFIX_COLOR)
        fun isDimension(string: String): Boolean = string.startsWith(RESOURCE_PREFIX_DIMENSION)
        fun isDrawable(string: String): Boolean = string.startsWith(RESOURCE_PREFIX_DRAWABLE)
        fun isString(string: String): Boolean = string.startsWith(RESOURCE_PREFIX_STRING)
        fun isResource(string: String): Boolean =
                isAnimation(string) || isBoolean(string) || isColor(string) || isDimension(string) || isDrawable(string) || isString(string)

        fun getInteger(resId: Int, context: Context) = try {
            context.resources.getInteger(resId)
        } catch (e: Resources.NotFoundException) {
            null
        }

        fun getBoolean(resId: Int, context: Context): Boolean? = try {
            context.resources.getBoolean(resId)
        } catch (e: Resources.NotFoundException) {
            null
        }

        fun getColor(resId: Int, context: Context): Int? = try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> context.resources.getColor(resId, context.theme)
                else -> context.resources.getColor(resId)
            }
        } catch (e: Resources.NotFoundException) {
            null
        }

        fun getColorStateList(resId: Int, context: Context): ColorStateList? = try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> context.getColorStateList(resId)
                else -> context.resources.getColorStateList(resId)
            }
        } catch (nfe: Resources.NotFoundException) {
            null
        }

        fun getDrawable(resId: Int, context: Context): Drawable? = try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> context.resources.getDrawable(resId, context.theme)
                else -> context.resources.getDrawable(resId)
            }
        } catch (e: Resources.NotFoundException) {
            null
        }

        fun getDimension(resId: Int, context: Context): Float? = try {
            context.resources.getDimension(resId)
        } catch (e: Resources.NotFoundException) {
            null
        }

        fun getString(resId: Int, context: Context): String? = try {
            context.getString(resId)
        } catch (e: Resources.NotFoundException) {
            null
        }

        fun valueOf(value: String, @ResourceVType type: String?, context: Context): ResourceV {
            var result = cache[value]
            if (result == null) {
                result = when (val resId = context.resources.getIdentifier(value, type, context.packageName)) {
                    0 -> NOT_FOUND
                    else -> ResourceV(resId)
                }
                cache.put(value, result)
            }
            return result
        }
    }

    @StringDef(ANIM, BOOLEAN, COLOR, DRAWABLE, DIMEN, STRING)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class ResourceVType

    override fun copy(): ResourceV = this
    override fun string(): String = TODO()
}

abstract class BindingV : Value() {
    companion object {
        const val BINDING_PREFIX_0 = '@'
        const val BINDING_PREFIX_1 = '{'
        const val BINDING_SUFFIX = '}'

        const val INDEX = "\$index"

        const val ARRAY_DATA_LENGTH_REFERENCE = "\$length"
        const val ARRAY_DATA_LAST_INDEX_REFERENCE = "\$last"

        val BINDING_PATTERN: Pattern = Pattern.compile("@\\{fn:(\\S+?)\\(((?:(?<!\\\\)'.*?(?<!\\\\)'|.?)+)\\)}|@\\{(.+)}")
        val FUNCTION_ARGS_DELIMITER: Pattern = Pattern.compile(",(?=(?:[^']*'[^']*')*[^']*$)")

        const val DATA_PATH_DELIMITERS = ".]"

        const val DELIMITER_OBJECT = '.'
        const val DELIMITER_ARRAY_OPENING = '['
        const val DELIMITER_ARRAY_CLOSING = ']'

        fun isBindingValue(value: String): Boolean = value.length > 3 && value[0] == BINDING_PREFIX_0 && value[1] == BINDING_PREFIX_1 && value[value.length - 1] == BINDING_SUFFIX

        fun valueOf(value: String, context: Context, funcManager: FuncManager?): BindingV {
            val matcher = BINDING_PATTERN.matcher(value)
            return when {
                matcher.find() -> when {
                    matcher.group(3) != null -> DataBinding.valueOf(matcher.group(3)!!)
                    else -> FuncBinding.valueOf(matcher.group(1)!!, matcher.group(2)!!, context, funcManager!!)
                }
                else -> throw IllegalArgumentException("$value is not a binding")
            }
        }
    }

    override fun toString(): String = string()
    override fun copy(): Value = this
    abstract fun evaluate(context: Context, data: Value?, index: Int): Value?

    open class Token(open var value: String, open var isArray: Boolean, open var isArrayIndex: Boolean) {
        open var isBinding: Boolean = false
    }

    open class DataBinding(open val tokens: Array<Token>) : BindingV() {
        companion object {
            val cache: LruCache<String, DataBinding> = LruCache(64)

            fun valueOf(path: String): DataBinding {
                var result = cache[path]
                if (result == null) {
                    val tokenizer = StringTokenizer(path, DATA_PATH_DELIMITERS, true)
                    val tokens = mutableListOf<Token>()
                    var token: String
                    var length: Int
                    var first: Char
                    while (tokenizer.hasMoreTokens()) {
                        token = tokenizer.nextToken()
                        length = token.length
                        first = token[0]
                        if (length == 1 || first == DELIMITER_OBJECT) {
                            continue
                        }
                        if (length == 1 || first == DELIMITER_ARRAY_CLOSING) {
                            val last = tokens.last().value
                            val index = last.indexOf(DELIMITER_ARRAY_OPENING)
                            val prefix = last.substring(0, index)
                            tokens.last().isArray = true
                            if (prefix.isNotEmpty()) {
                                tokens.last().value = prefix
                            }
                            tokens.add(Token(last.substring(index + 1), false, isArrayIndex = true))
                            continue
                        }
                        tokens.add(Token(token, false, isArrayIndex = false))
                    }
                    result = DataBinding(tokens.toTypedArray())
                    cache.put(path, result)
                }
                return result
            }

            fun getArrayItem(array: ArrayV, index: Int, isArray: Boolean): Value {
                if (index > array.size()) {
                    while (array.size() < index) {
                        array.add(nullV)
                    }
                    array.add(if (isArray) ArrayV() else ObjectV())
                }
                return array[index]!!
            }

            fun getArrayIndex(token: String, dataIndex: Int) = when (token) {
                INDEX -> dataIndex
                else -> token.toInt()
            }

            fun getSubArray(parent: Value, token: String, index: Int): ArrayV = when (parent) {
                is ArrayV -> {
                    if (parent[index] == null || parent[index] !is ArrayV) {
                        parent[index] = ArrayV()
                    }
                    parent[index] as ArrayV
                }
                is ObjectV -> {
                    if (parent[token] == null || parent[token] !is ArrayV) {
                        parent[token] = ArrayV()
                    }
                    parent[token] as ArrayV
                }
                else -> throw RuntimeException("parent should be ArrayV or ObjectV")
            }

            fun getSubObject(parent: Value, token: String, index: Int): ObjectV = when (parent) {
                is ArrayV -> {
                    if (parent[index] == null || parent[index] !is ObjectV) {
                        parent[index] = ObjectV()
                    }
                    parent[index] as ObjectV
                }
                is ObjectV -> {
                    if (parent[token] == null || parent[token] !is ObjectV) {
                        parent[token] = ObjectV()
                    }
                    parent[token] as ObjectV
                }
                else -> throw RuntimeException("parent should be ArrayV or ObjectV")
            }

            // 使用 tokens 往 value 里面的某个 array / object 里面塞入 data ， key 则是 index / tokens[...].value
            fun assign(tokens: Array<Token>, value: Value?, data: Value, index: Int) {
                var nowData = data
                var nowIndex = index
                (0 until (tokens.size - 1)).forEach { i ->
                    val token = tokens[i]
                    nowData = when {
                        token.isArrayIndex -> {
                            try {
                                nowIndex = getArrayIndex(token.value, index)
                            } catch (e: NumberFormatException) {
                                return
                            }
                            getArrayItem(nowData as ArrayV, nowIndex, token.isArray)
                        }
                        token.isArray -> getSubArray(nowData, token.value, nowIndex)
                        else -> getSubObject(nowData, token.value, nowIndex)
                    }
                }
                val token = tokens.last()
                if (token.isArrayIndex) {
                    try {
                        nowIndex = getArrayIndex(token.value, index)
                    } catch (e: NumberFormatException) {
                        return
                    }
                    getArrayItem(nowData as ArrayV, nowIndex, false)
                    (nowData as ArrayV)[index] = value
                } else {
                    (nowData as ObjectV)[token.value] = value
                }
            }

            fun resolve(tokens: Array<Token>, data: Value?, index: Int): Pair<Value, Int> {
                if (tokens.size == 1 && tokens[0].value == INDEX) {
                    return Pair(PrimitiveV(index.toString()), RESULT_SUCCESS)
                }
                var result: Value? = data
                var nowIndex = index
                for (it in tokens) {
                    if (result == null) {
                        return Pair(nullV, RESULT_NO_SUCH_DATA_PATH_EXCEPTION)
                    }
                    if (result is NullV) {
                        return Pair(nullV, RESULT_NULL_EXCEPTION)
                    }
                    val segment = it.value
                    if (segment.isEmpty()) {
                        continue
                    }
                    when (result) {
                        is ArrayV -> {
                            val arrSize = result.size()
                            when (segment) {
                                INDEX -> when {
                                    nowIndex < arrSize -> result = result[nowIndex]
                                    else -> return Pair(nullV, RESULT_NO_SUCH_DATA_PATH_EXCEPTION)
                                }
                                ARRAY_DATA_LENGTH_REFERENCE -> result = PrimitiveV(arrSize)
                                ARRAY_DATA_LAST_INDEX_REFERENCE -> when (arrSize) {
                                    0 -> return Pair(nullV, RESULT_NO_SUCH_DATA_PATH_EXCEPTION)
                                    else -> result = result[arrSize - 1]
                                }
                                else -> {
                                    try {
                                        nowIndex = segment.toInt()
                                    } catch (e: NumberFormatException) {
                                        return Pair(nullV, RESULT_INVALID_DATA_PATH_EXCEPTION)
                                    }
                                    when {
                                        nowIndex < arrSize -> result = result[nowIndex]
                                        else -> return Pair(nullV, RESULT_NO_SUCH_DATA_PATH_EXCEPTION)
                                    }
                                }
                            }
                        }
                        is ObjectV -> result = result[segment] ?: return Pair(nullV, RESULT_NO_SUCH_DATA_PATH_EXCEPTION)
                        is PrimitiveV -> return Pair(nullV, RESULT_INVALID_DATA_PATH_EXCEPTION)
                        else -> return Pair(nullV, RESULT_NO_SUCH_DATA_PATH_EXCEPTION)
                    }
                }
                return when (result) {
                    null, is NullV -> Pair(nullV, RESULT_NULL_EXCEPTION)
                    else -> Pair(result, RESULT_SUCCESS)
                }
            }

            const val RESULT_SUCCESS = 0
            const val RESULT_NO_SUCH_DATA_PATH_EXCEPTION = -1
            const val RESULT_INVALID_DATA_PATH_EXCEPTION = -2
            const val RESULT_NULL_EXCEPTION = -3
        }

        override fun string(): String = "@{${tokens.joinToString(".") { it.value }}}"
        open fun iterator() = tokens.iterator()
        open fun assign(value: Value?, data: Value, index: Int) = assign(this.tokens, value, data, index)
        override fun evaluate(context: Context, data: Value?, index: Int): Value = resolve(tokens, data, index).first  // 好像不用管失败原因。。。
    }

    open class FuncBinding(val func: FuncManager.Func, val args: Array<Value?>?) : BindingV() {
        companion object {
            fun valueOf(name: String, args: String, context: Context, funcManager: FuncManager): FuncBinding = FuncBinding(
                    funcManager[name],
                    FUNCTION_ARGS_DELIMITER.split(args).map {
                        var temp = it.trim()
                        if (temp.isNotEmpty() && temp[0] == '\'') {
                            temp = temp.substring(1, temp.length - 1)
                            PrimitiveV(temp)
                        } else {
                            NAttributeProcessor.staticPreCompile(PrimitiveV(temp), context, funcManager) ?: PrimitiveV(temp)
                        }
                    }.toTypedArray()
            )

            fun resolve(context: Context, inArr: Array<Value?>?, data: Value?, index: Int) =
                    inArr?.map { NAttributeProcessor.evaluate(context, it, data, index) }?.toTypedArray() ?: arrayOf()
        }

        override fun string(): String = "@{fn:${func.getName()}(${args?.joinToString { it?.string() ?: nullV.string() }
                ?: nullV.string()})}"

        override fun evaluate(context: Context, data: Value?, index: Int): Value? {
            return try {
                func.call(context, data, index, *resolve(context, this.args, data, index))
            } catch (e: Exception) {
                nullV
            }
        }

        open fun iterator() = args?.iterator()
    }

    open class NestedBinding(open val value: Value) : BindingV() {
        override fun string(): String = "${javaClass.name}@${Integer.toHexString(hashCode())}"

        override fun evaluate(context: Context, data: Value?, index: Int): Value? = evaluate(context, value, data, index)
        open fun evaluate(context: Context, entry: Value?, data: Value?, index: Int): Value? = when (data) {
            is BindingV -> data.evaluate(context, data, index)
            is ObjectV -> ObjectV(data.entries().map { it.key to evaluate(context, it.value, data, index) }.toMap().toMutableMap())
            is ArrayV -> ArrayV(data.values().map { evaluate(context, it, data, index) })
            else -> data
        }
    }
}

abstract class DrawableV : Value() {
    companion object {
        const val TYPE = "type"
        const val CHILDREN = "children"

        const val DRAWABLE_COLOR = "color"
        const val DRAWABLE_SHAPE = "shape"
        const val DRAWABLE_SELECTOR = "selector"
        const val DRAWABLE_LAYER_LIST = "layer-list"
        const val DRAWABLE_LEVEL_LIST = "level-list"
        const val DRAWABLE_RIPPLE = "ripple"

        const val TYPE_GRADIENT = "gradient"
        const val TYPE_CORNERS = "corners"
        const val TYPE_SOLID = "solid"
        const val TYPE_SIZE = "size"
        const val TYPE_STROKE = "stroke"
        const val TYPE_PADDING = "padding"

        fun valueOf(string: String): DrawableV = if (ColorV.isColor(string)) ColorDrawV.valueOf(string) else UrlDrawV(string)
        fun valueOf(objectV: ObjectV, context: Context): DrawableV? = when (objectV.getString(TYPE)) {
            DRAWABLE_SELECTOR -> StateListDrawV(objectV.getArray(CHILDREN)!!, context)
            DRAWABLE_SHAPE -> ShapeDrawV(objectV, context)
            DRAWABLE_LAYER_LIST -> LayerListDrawV(objectV.getArray(CHILDREN)!!, context)
            DRAWABLE_LEVEL_LIST -> LevelListDrawV(objectV.getArray(CHILDREN)!!, context)
            DRAWABLE_RIPPLE -> RippleDrawV(objectV, context)
            else -> null
        }
    }

    abstract fun apply(view: NView<*>, context: Context, loader: NInflater.ImageLoader? /*这里只有urlDrawV需要用到*/, callback: Callback)
    override fun copy(): DrawableV = this

    // callback

    interface Callback {
        fun apply(drawable: Drawable?)
    }

    abstract class AsyncCallback : Callback {
        private var recycled = false

        open fun setBitmap(bitmap: Bitmap) {
            if (recycled) {
                throw RuntimeException("Cannot make calls to a recycled instance!")
            }
            apply(bitmap)
            recycled = true
        }

        open fun setDrawable(drawable: Drawable) {
            if (recycled) {
                throw RuntimeException("Cannot make calls to a recycled instance!")
            }
            apply(drawable)
            recycled = true
        }

        abstract fun apply(bitmap: Bitmap)
    }

    // element

    abstract class DrawElementV : Value() {
        override fun copy(): Value = this
        abstract fun apply(view: NView<*>, drawable: GradientDrawable)
    }

    open class Gradient(gradient: ObjectV, context: Context) : DrawElementV() {
        val gradientType: Int = when (gradient.getString(GRADIENT_TYPE)) {
            LINEAR_GRADIENT -> GradientDrawable.LINEAR_GRADIENT
            RADIAL_GRADIENT -> GradientDrawable.RADIAL_GRADIENT
            SWEEP_GRADIENT -> GradientDrawable.SWEEP_GRADIENT
            else -> GRADIENT_TYPE_NONE
        }
        val angle: Int? = gradient.getInt(ANGLE)
        val centerX: Float? = gradient.getFloat(CENTER_X)
        val centerY: Float? = gradient.getFloat(CENTER_Y)
        val startColor: Value = gradient[START_COLOR]!!.let { NColorResourceProcessor.staticCompile(it, context) }
        val centerColor: Value? = gradient[CENTER_COLOR]?.let { NColorResourceProcessor.staticCompile(it, context) }
        val endColor: Value = gradient[END_COLOR]!!.let { NColorResourceProcessor.staticCompile(it, context) }
        val gradientRadius: Value? = gradient[GRADIENT_RADIUS]?.let { NColorResourceProcessor.staticCompile(it, context) }
        val useLevel: Boolean? = gradient.getBoolean(USE_LEVEL)

        override fun string(): String = TODO()
        override fun apply(view: NView<*>, drawable: GradientDrawable) {
            if (centerX != null && centerY != null) {
                drawable.setGradientCenter(centerX, centerY)
            }
            if (gradientRadius != null) {
                drawable.gradientRadius = NDimensionAttributeProcessor.evaluate(gradientRadius, view)
            }
            if (gradientType != GRADIENT_TYPE_NONE) {
                drawable.gradientType = gradientType
            }
        }

        open fun init(view: NView<*>): GradientDrawable = init(when (centerColor) {
            null -> intArrayOf(NColorResourceProcessor.evaluate(startColor, view).color!!, NColorResourceProcessor.evaluate(centerColor, view).color!!,
                    NColorResourceProcessor.evaluate(endColor, view).color!!)
            else -> intArrayOf(NColorResourceProcessor.evaluate(startColor, view).color!!, NColorResourceProcessor.evaluate(endColor, view).color!!)
        }, angle)

        companion object {
            const val ANGLE = "angle"
            const val CENTER_X = "centerX"
            const val CENTER_Y = "centerY"
            const val CENTER_COLOR = "centerColor"
            const val START_COLOR = "startColor"
            const val END_COLOR = "endColor"
            const val GRADIENT_RADIUS = "gradientRadius"
            const val GRADIENT_TYPE = "gradientType"
            const val USE_LEVEL = "useLevel"

            private const val GRADIENT_TYPE_NONE = -1

            private const val LINEAR_GRADIENT = "linear"
            private const val RADIAL_GRADIENT = "radial"
            private const val SWEEP_GRADIENT = "sweep"

            fun getOrientation(angle: Int?): GradientDrawable.Orientation {
                var orientation = GradientDrawable.Orientation.LEFT_RIGHT
                if (angle != null) {
                    val angle1 = angle % 360
                    if (angle1 % 45 == 0) {
                        orientation = when (angle1) {
                            0 -> GradientDrawable.Orientation.LEFT_RIGHT
                            45 -> GradientDrawable.Orientation.BL_TR
                            90 -> GradientDrawable.Orientation.BOTTOM_TOP
                            135 -> GradientDrawable.Orientation.BR_TL
                            180 -> GradientDrawable.Orientation.RIGHT_LEFT
                            225 -> GradientDrawable.Orientation.TR_BL
                            270 -> GradientDrawable.Orientation.TOP_BOTTOM
                            315 -> GradientDrawable.Orientation.TL_BR
                            else -> GradientDrawable.Orientation.LEFT_RIGHT
                        }
                    }
                }
                return orientation
            }

            fun init(colors: IntArray?, angle: Int?): GradientDrawable = when (angle) {
                null -> GradientDrawable()
                else -> GradientDrawable(getOrientation(angle), colors)
            }
        }
    }

    open class Corners(corner: ObjectV, context: Context) : DrawElementV() {
        val radius: Value? = NDimensionAttributeProcessor.staticCompile(corner[RADIUS], context)
        val topLeftRadius: Value? = NDimensionAttributeProcessor.staticCompile(corner[TOP_LEFT_RADIUS], context)
        val topRightRadius: Value? = NDimensionAttributeProcessor.staticCompile(corner[TOP_RIGHT_RADIUS], context)
        val bottomLeftRadius: Value? = NDimensionAttributeProcessor.staticCompile(corner[BOTTOM_LEFT_RADIUS], context)
        val bottomRightRadius: Value? = NDimensionAttributeProcessor.staticCompile(corner[BOTTOM_RIGHT_RADIUS], context)

        override fun string(): String = TODO()
        override fun apply(view: NView<*>, drawable: GradientDrawable) {
            if (radius != null) {
                drawable.gradientRadius = NDimensionAttributeProcessor.evaluate(radius, view)
            }
            val fTopLeftRadius = NDimensionAttributeProcessor.evaluate(topLeftRadius, view)
            val fTopRightRadius = NDimensionAttributeProcessor.evaluate(topRightRadius, view)
            val fBottomRightRadius = NDimensionAttributeProcessor.evaluate(bottomRightRadius, view)
            val fBottomLeftRadius = NDimensionAttributeProcessor.evaluate(bottomLeftRadius, view)
            if (fTopLeftRadius != 0f || fTopRightRadius != 0f || fBottomLeftRadius != 0f || fBottomRightRadius != 0f) {
                drawable.cornerRadii = floatArrayOf(
                        fTopLeftRadius, fTopLeftRadius,
                        fTopRightRadius, fTopRightRadius,
                        fBottomRightRadius, fBottomRightRadius,
                        fBottomLeftRadius, fBottomLeftRadius
                )
            }
        }

        companion object {
            const val RADIUS = "radius"
            const val TOP_LEFT_RADIUS = "topLeftRadius"
            const val TOP_RIGHT_RADIUS = "topRightRadius"
            const val BOTTOM_LEFT_RADIUS = "bottomLeftRadius"
            const val BOTTOM_RIGHT_RADIUS = "bottomRightRadius"
        }
    }

    open class Solid(value: ObjectV, context: Context) : DrawElementV() {
        val color = NColorResourceProcessor.staticCompile(value[COLOR], context)

        override fun string(): String = TODO()
        override fun apply(view: NView<*>, drawable: GradientDrawable) {
            val result = NColorResourceProcessor.evaluate(color, view)
            if (result.colors != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawable.color = result.colors
            } else if (result.color != null) {
                drawable.setColor(result.color)
            }
        }

        companion object {
            const val COLOR = "color"
        }
    }

    open class Size(size: ObjectV, context: Context) : DrawElementV() {
        val width = NDimensionAttributeProcessor.staticCompile(size[WIDTH], context)
        val height = NDimensionAttributeProcessor.staticCompile(size[HEIGHT], context)

        override fun string(): String = TODO()
        override fun apply(view: NView<*>, drawable: GradientDrawable) =
                drawable.setSize(NDimensionAttributeProcessor.evaluate(width, view).toInt(), NDimensionAttributeProcessor.evaluate(height, view).toInt())

        companion object {
            const val WIDTH = "width"
            const val HEIGHT = "height"
        }
    }

    open class Stroke(stroke: ObjectV, context: Context) : DrawElementV() {
        val width = NDimensionAttributeProcessor.staticCompile(stroke[WIDTH], context)
        val color = NColorResourceProcessor.staticCompile(stroke[COLOR], context)
        val dashWidth = NDimensionAttributeProcessor.staticCompile(stroke[DASH_WIDTH], context)
        val dashGap = NDimensionAttributeProcessor.staticCompile(stroke[DASH_GAP], context)

        override fun string(): String = TODO()
        override fun apply(view: NView<*>, drawable: GradientDrawable) = when {
            null == dashWidth -> drawable.setStroke(NDimensionAttributeProcessor.evaluate(width, view).toInt(), NColorResourceProcessor.evaluate(color, view).color!!)
            null != dashGap -> drawable.setStroke(NDimensionAttributeProcessor.evaluate(width, view).toInt(), NColorResourceProcessor.evaluate(color, view).color!!,
                    NDimensionAttributeProcessor.evaluate(dashWidth, view), NDimensionAttributeProcessor.evaluate(dashGap, view))
            else -> Unit
        }

        companion object {
            const val WIDTH = "width"
            const val COLOR = "color"
            const val DASH_WIDTH = "dashWidth"
            const val DASH_GAP = "dashGap"
        }
    }

    // real type

    open class ColorDrawV(open val color: Value) : DrawableV() {
        override fun string(): String = TODO()
        override fun apply(view: NView<*>, context: Context, loader: NInflater.ImageLoader?, callback: Callback) =
                callback.apply(ColorDrawable(NColorResourceProcessor.evaluate(color, view).color!!))

        companion object {
            val BLACK = ColorDrawV(ColorV.IntV.BLACK)

            fun valueOf(value: String) = ColorDrawV(ColorV.valueOf(value))
            fun valueOf(value: Value, context: Context) = ColorDrawV(NColorResourceProcessor.staticCompile(value, context))
        }
    }

    open class ShapeDrawV : DrawableV {
        override fun string(): String = TODO()
        override fun apply(view: NView<*>, context: Context, loader: NInflater.ImageLoader?, callback: Callback) {
            val d = this.gradient?.init(view) ?: GradientDrawable()
            if (shape != SHAPE_NONE) {
                d.shape = shape
            }
            elements?.forEach { it.apply(view, d) }
            callback.apply(d)
        }

        val shape: Int
        var gradient: Gradient?
        var elements: Array<DrawElementV>?

        constructor(shape: Int, gradient: Gradient?, elements: Array<DrawElementV>?) {
            this.shape = shape
            this.gradient = gradient
            this.elements = elements
        }

        constructor(value: ObjectV, context: Context) {
            if (value[SHAPE] !is PrimitiveV) {
                this.shape = SHAPE_NONE
            } else {
                this.shape = when ((value[SHAPE] as PrimitiveV).string()) {
                    SHAPE_RECTANGLE -> GradientDrawable.RECTANGLE
                    SHAPE_OVAL -> GradientDrawable.OVAL
                    SHAPE_LINE -> GradientDrawable.LINE
                    SHAPE_RING -> GradientDrawable.RING
                    else -> SHAPE_NONE
                }
            }
            val children = value[CHILDREN]
            if (children !is ArrayV || children.size() == 0) {
                this.gradient = null
                this.elements = null
            } else {
                val elementList = mutableListOf<DrawElementV>()
                this.gradient = null
                for (element in children) {
                    if (element is ObjectV) {
                        val typeKey = element[TYPE]
                        if (typeKey !is PrimitiveV) {
                            continue
                        }
                        when (typeKey.string()) {
                            TYPE_CORNERS -> elementList.add(Corners(element, context))
                            TYPE_PADDING -> Unit
                            TYPE_SIZE -> elementList.add(Size(element, context))
                            TYPE_SOLID -> elementList.add(Solid(element, context))
                            TYPE_STROKE -> elementList.add(Stroke(element, context))
                            TYPE_GRADIENT -> {
                                this.gradient = Gradient(element, context)
                                elementList.add(this.gradient!!)
                            }
                        }
                    }
                }
                this.elements = elementList.toTypedArray()
            }
        }

        companion object {
            const val SHAPE_NONE = -1
            const val SHAPE = "shape"

            const val SHAPE_RECTANGLE = "rectangle"
            const val SHAPE_OVAL = "oval"
            const val SHAPE_LINE = "line"
            const val SHAPE_RING = "ring"
        }
    }

    open class LayerListDrawV : DrawableV {
        val ids: IntArray
        val layers: Array<Value>

        constructor(ids: IntArray, layers: Array<Value>) {
            this.ids = ids
            this.layers = layers
        }

        constructor(layers: ArrayV, context: Context) {
            val ids = mutableListOf<Int>()
            val layers2 = mutableListOf<Value>()
            for (layer in layers) {
                if (layer is ObjectV) {
                    ids.add(getAndroidXmlResId(layer.getString(ID_STR)))
                    layers.add(NDrawableResourceProcessor.staticCompile(layer[DRAWABLE_STR], context))
                }
            }
            this.ids = ids.toIntArray()
            this.layers = layers2.toTypedArray()
        }

        override fun string(): String = TODO()
        override fun apply(view: NView<*>, context: Context, loader: NInflater.ImageLoader?, callback: Callback) {
            val drawable = LayerDrawable(layers.map { NDrawableResourceProcessor.evaluate(it, view) }.toTypedArray())
            ids.forEachIndexed { i, id -> drawable.setId(i, id) }
            callback.apply(drawable)
        }

        open operator fun iterator() = layers.iterator()
        open fun idIterator() = ids.iterator()

        companion object {
            const val ID_STR = "id"
            const val DRAWABLE_STR = "drawable"
        }
    }

    open class StateListDrawV : DrawableV {
        val states: Array<IntArray>
        val values: Array<Value>

        constructor(states: Array<IntArray>, values: Array<Value>) {
            this.states = states
            this.values = values
        }

        constructor(states: ArrayV, context: Context) {
            val states2 = mutableListOf<IntArray>()
            val values = mutableListOf<Value>()
            for (state in states) {
                if (state is ObjectV) {
                    val statesEntry = mutableListOf<Int>()
                    values.add(NDrawableResourceProcessor.staticCompile(state[DRAWABLE_STR], context))
                    for ((key, entry) in state) {
                        val stateEntry = sStateMap[key] ?: throw RuntimeException("$key is not a valid state")
                        statesEntry.add(if (parseBoolean(entry)) stateEntry else -stateEntry)
                    }
                    states2.add(statesEntry.toIntArray())
                }
            }
            this.states = states2.toTypedArray()
            this.values = values.toTypedArray()
        }

        open operator fun iterator() = values.iterator()
        override fun string(): String = TODO()
        override fun apply(view: NView<*>, context: Context, loader: NInflater.ImageLoader?, callback: Callback) {
            val d = StateListDrawable()
            states.forEachIndexed { index, ints -> d.addState(ints, NDrawableResourceProcessor.evaluate(values[index], view)) }
            callback.apply(d)
        }

        companion object {
            const val DRAWABLE_STR = "drawable"
            val sStateMap: MutableMap<String, Int> = java.util.HashMap()

            init {
                sStateMap["state_pressed"] = R.attr.state_pressed
                sStateMap["state_enabled"] = R.attr.state_enabled
                sStateMap["state_focused"] = R.attr.state_focused
                sStateMap["state_hovered"] = R.attr.state_hovered
                sStateMap["state_selected"] = R.attr.state_selected
                sStateMap["state_checkable"] = R.attr.state_checkable
                sStateMap["state_checked"] = R.attr.state_checked
                sStateMap["state_activated"] = R.attr.state_activated
                sStateMap["state_window_focused"] = R.attr.state_window_focused
            }
        }
    }

    @Suppress("LeakingThis")
    open class LevelListDrawV : DrawableV {
        open val levels: Array<Level>

        constructor(levelArr: ArrayV, context: Context) {
            val levels = mutableListOf<Level>()
            for (level in levelArr) {
                if (level is ObjectV) {
                    levels.add(Level(level, context))
                }
            }
            this.levels = levels.toTypedArray()
        }

        constructor(levels: Array<Level>) {
            this.levels = levels
        }

        open operator fun iterator() = levels.iterator()
        override fun string(): String = TODO()
        override fun apply(view: NView<*>, context: Context, loader: NInflater.ImageLoader?, callback: Callback) {
            val d = LevelListDrawable()
            levels.forEach { it.apply(view, d) }
            callback.apply(d)
        }

        @Suppress("LeakingThis")
        open class Level {
            open val minLevel: Int
            open val maxLevel: Int
            open val drawable: Value

            constructor(minLevel: Int, maxLevel: Int, drawable: Value, context: Context) {
                this.minLevel = minLevel
                this.maxLevel = maxLevel
                this.drawable = NDrawableResourceProcessor.staticCompile(drawable, context)
            }

            constructor(value: ObjectV, context: Context) {
                this.minLevel = value.getInt(MIN_LEVEL) ?: -1
                this.maxLevel = value.getInt(MAX_LEVEL) ?: -1
                this.drawable = NDrawableResourceProcessor.staticCompile(value[DRAWABLE], context)
            }

            open fun apply(view: NView<*>, levelListDrawable: LevelListDrawable) = levelListDrawable.addLevel(minLevel, maxLevel, NDrawableResourceProcessor.evaluate(drawable, view))

            companion object {
                const val MIN_LEVEL = "minLevel"
                const val MAX_LEVEL = "maxLevel"
                const val DRAWABLE = "drawable"
            }
        }
    }

    @Suppress("LeakingThis")
    open class RippleDrawV : DrawableV {
        open val color: Value
        open val mask: Value?
        open val content: Value?
        open val defaultBackground: Value?

        constructor(color: Value, mask: Value?, content: Value?, defaultBackground: Value?) {
            this.color = color
            this.mask = mask
            this.content = content
            this.defaultBackground = defaultBackground
        }

        constructor(objectV: ObjectV, context: Context) {
            this.color = objectV[COLOR]!!
            this.mask = NDrawableResourceProcessor.staticCompile(objectV[MASK], context)
            this.content = NDrawableResourceProcessor.staticCompile(objectV[CONTENT], context)
            this.defaultBackground = NDrawableResourceProcessor.staticCompile(objectV[DEFAULT_BACKGROUND], context)
        }

        override fun string(): String = TODO()
        override fun apply(view: NView<*>, context: Context, loader: NInflater.ImageLoader?, callback: Callback) {
            val result = NColorResourceProcessor.evaluate(color, view)
            val colorStateList = result.colors ?: ColorStateList(arrayOf(intArrayOf()), intArrayOf(result.color!!))
            val contentDrawable = NDrawableResourceProcessor.evaluate(content, view)
            val resultDrawable = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> RippleDrawable(colorStateList, contentDrawable, NDrawableResourceProcessor.evaluate(mask, view))
                defaultBackground != null -> NDrawableResourceProcessor.evaluate(defaultBackground, view)
                contentDrawable != null -> {
                    val pressedColor = colorStateList.getColorForState(intArrayOf(R.attr.state_pressed), colorStateList.defaultColor)
                    val focusedColor = colorStateList.getColorForState(intArrayOf(R.attr.state_focused), pressedColor)
                    val stateListDrawable = StateListDrawable()
                    stateListDrawable.addState(intArrayOf(R.attr.state_enabled, R.attr.state_pressed), ColorDrawable(pressedColor))
                    stateListDrawable.addState(intArrayOf(R.attr.state_enabled, R.attr.state_focused), ColorDrawable(focusedColor))
                    stateListDrawable.addState(intArrayOf(R.attr.state_enabled), contentDrawable)
                    stateListDrawable
                }
                else -> null
            }
            callback.apply(resultDrawable)
        }

        companion object {
            const val COLOR = "color"
            const val MASK = "mask"
            const val CONTENT = "content"
            const val DEFAULT_BACKGROUND = "defaultBackground"
        }
    }

    open class UrlDrawV(open val url: String) : DrawableV() {
        override fun string(): String = TODO()
        override fun apply(view: NView<*>, context: Context, loader: NInflater.ImageLoader?, callback: Callback) = loader!!.getBitmap(view, url, object : AsyncCallback() {
            override fun apply(drawable: Drawable?) = callback.apply(drawable)
            override fun apply(bitmap: Bitmap) = callback.apply(convertBitmapToDrawable(bitmap, view.viewContext))
        })

        companion object {
            fun convertBitmapToDrawable(original: Bitmap, context: Context): Drawable {
                val displayMetrics = context.resources.displayMetrics
                val matrix = Matrix()
                matrix.postScale(displayMetrics.scaledDensity, displayMetrics.scaledDensity)
                return BitmapDrawable(context.resources, Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true) /*resizedBitmap*/)
            }
        }
    }
}
