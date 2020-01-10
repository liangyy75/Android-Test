@file:Suppress("MemberVisibilityCanBePrivate")

package com.liang.example.json_inflater

import android.R
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Color
import android.text.TextUtils
import android.util.LruCache
import android.util.StateSet
import android.util.TypedValue
import android.view.ViewGroup
import com.liang.example.basic_ktx.KKMap
import com.liang.example.json_inflater.NullV.Companion.nullV
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.collections.set

abstract class Value {
    abstract fun copy(): Value
    open fun getAsString(): String = throw UnsupportedOperationException(javaClass.simpleName)
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

    open fun add(value: Boolean?) = values.add(if (value == null) nullV else PrimitiveV(value))
    open fun add(value: String?) = values.add(if (value == null) nullV else PrimitiveV(value))
    open fun add(value: Char?) = values.add(if (value == null) nullV else PrimitiveV(value))
    open fun add(value: Number?) = values.add(if (value == null) nullV else PrimitiveV(value))
    open fun add(value: Value?, pos: Int = -1) = values.add(if (pos == -1) values.size else pos, value ?: nullV)
    open fun addAll(value: ArrayV) = values.addAll(value.values)
    open fun set(value: Value?, pos: Int) = values.set(pos, value)
    open fun get(pos: Int) = values.get(pos)
    open fun remove(value: Value) = values.remove(value)
    open fun removeAll(value: ArrayV) = values.removeAll(value.values)
    open fun remove(pos: Int) = values.removeAt(pos)
    open fun contains(value: Value) = value in values
    open fun containsAll(value: ArrayV) = values.containsAll(value.values)
    open fun size() = values.size
    open fun iterator() = values.iterator()

    override fun equals(other: Any?): Boolean = other === this || other is ArrayV && other.values == values
    override fun hashCode(): Int = values.hashCode()
}

open class ObjectV : Value() {
    protected val values: MutableMap<String, Value?> = mutableMapOf()

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
    open fun get(property: String) = values[property]
    open fun remove(property: String) = values.remove(property)
    open fun entries() = values.entries
    open fun size() = values.size
    open fun contains(property: String) = values.containsKey(property)
    open fun contains(value: Value) = values.containsValue(value)

    override fun equals(other: Any?): Boolean = this === other || other is ObjectV && other.values == values
    override fun hashCode(): Int = values.hashCode()
}

open class NullV private constructor() : Value() {
    override fun copy(): Value = this
    override fun toString(): String = "NULL"
    override fun getAsString() = ""
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

    override fun getAsString(): String = if (value is String) value as String else value.toString()
    open fun toBoolean() = if (value is Boolean) value as Boolean else java.lang.Boolean.parseBoolean(value as String)
    open fun toNumber() = if (value is Number) value as Number else LazilyParsedNumber(value as String)

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

    override fun toString(): String = getAsString()
    open fun getAsSingleQuotedString(): String? = "'${getAsString()}'"
    open fun getAsDoubleQuotedString(): String? = "\"${getAsString()}\""
}

open class LayoutV(
        open var type: String?,
        open var attributes: MutableList<Attribute>?,
        open var data: MutableMap<String, Value>?,
        open var extra: ObjectV?
) : Value() {

    override fun copy(): Value = LayoutV(type, attributes?.map { it.copy() }?.toMutableList(), data, extra)

    open fun merge(include: LayoutV): LayoutV {
        val attributes = this.attributes?.map { it.copy() }?.toMutableList()
        include.attributes?.let { attributes?.addAll(it) }
        val data = this.data?.map { it.key to it.value.copy() }?.toMap()?.toMutableMap()
        include.data?.let { data?.putAll(it) }
        val extra = this.extra?.copy()
        include.extra?.let { extra?.addAll(it) }
        return LayoutV(type, attributes, data, extra)
    }

    open class Attribute(open var id: Int, open var value: Value?) {
        open fun copy(): Attribute = Attribute(id, value?.copy() ?: nullV)
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

        fun valueOf(dimension: String?): DimensionV = if (null == dimension) ZERO else cache[dimension] ?: DimensionV(dimension)
        fun apply(dimension: String?, context: Context): Float = valueOf(dimension).apply(context)
    }

    open val value: Double
    open val unit: Int

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
        cache.put(dimension, this)
    }

    override fun copy(): Value = this

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

        fun valueOf(value: String, context: Context): StyleResourceV? {
            var result = cache[value]
            if (result == null) {
                result = try {
                    StyleResourceV(value, context)
                } catch (e: Exception) {
                    NULL
                    cache.put(value, result)
                }
            }
            return if (result == NULL) null else result
        }
    }

    open val styleId: Int
    open val attributeId: Int

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
        cache.put(value, this)
    }

    override fun copy(): Value = this

    fun apply(context: Context): TypedArray = context.obtainStyledAttributes(styleId, intArrayOf(attributeId))
}

abstract class ColorV : Value() {
    companion object {
        const val COLOR_PREFIX_LITERAL = "#"
        val sAttributesMap: HashMap<String, Int> = HashMap(15)
        val cache: LruCache<String, ColorV> = LruCache(64)

        fun apply(value: String?): Int {
            val color = valueOf(value)
            return if (color is IntV) {
                color.value
            } else {
                IntV.BLACK.value
            }
        }

        fun valueOf(value: String?, defValue: ColorV = IntV.BLACK): ColorV {
            if (value.isNullOrEmpty()) {
                return defValue
            }
            var result = cache.get(value)
            if (result == null) {
                result = if (value.startsWith(COLOR_PREFIX_LITERAL)) {
                    IntV(Color.parseColor(value))
                } else {
                    defValue
                }
                cache.put(value, result)
            }
            return result
        }

        fun isColor(color: String): Boolean = color.startsWith(COLOR_PREFIX_LITERAL)

        fun modulateColorAlpha(baseColor: Int, alphaMod: Float): Int {
            if (alphaMod == 1.0f) {
                return baseColor
            }
            val baseAlpha = Color.alpha(baseColor)
            val alpha = constrain((baseAlpha * alphaMod + 0.5f).toInt(), 0, 255)
            return baseColor and 0xFFFFFF or (alpha shl 24)
        }

        fun constrain(amount: Int, low: Int, high: Int): Int = if (amount < low) low else if (amount > high) high else amount

        fun idealByteArraySize(need: Int): Int {
            (4..31).forEach {
                if (need <= (1 shl it) - 12) {
                    return (1 shl it) - 12
                }
            }
            return need
        }

        fun idealIntArraySize(need: Int): Int = idealByteArraySize(need * 4) / 4

        fun getAttribute(attribute: String?): Int? = sAttributesMap[attribute]

        fun valueOf(value: ObjectV, context: Context?): ColorV {
            if (value.get("type") !is PrimitiveV) {
                return IntV.BLACK
            }
            if (!TextUtils.equals((value.get("type") as PrimitiveV).getAsString(), "selector") || value.get("children") !is ArrayV) {
                return IntV.BLACK
            }
            val iterator = (value.get("children") as ArrayV).iterator()
            var child: ObjectV
            var listAllocated = 20
            var listSize = 0
            var colorList = IntArray(listAllocated)
            var stateSpecList = arrayOfNulls<IntArray>(listAllocated)
            while (iterator.hasNext()) {
                child = iterator.next() as ObjectV
                if (child.size() == 0) continue
                var j = 0
                var baseColor: Int? = null
                var alphaMod = 1.0f
                var stateSpec = IntArray(child.size() - 1)
                var ignoreItem = false
                for ((key, value1) in child.entries()) {
                    if (ignoreItem) break
                    if (value1 !is PrimitiveV) continue
                    val attributeId = sAttributesMap[key]
                    if (null != attributeId) {
                        when (attributeId) {
                            R.attr.type -> if (!TextUtils.equals("item", value1.getAsString())) ignoreItem = true
                            R.attr.color -> {
                                val colorRes = value1.getAsString()
                                if (!TextUtils.isEmpty(colorRes)) {
                                    baseColor = apply(colorRes)
                                }
                            }
                            R.attr.alpha -> {
                                val alphaStr = value1.getAsString()
                                if (!TextUtils.isEmpty(alphaStr)) {
                                    alphaMod = alphaStr.toFloat()
                                }
                            }
                            else -> stateSpec[j++] = if (value1.toBoolean()) attributeId else -attributeId
                        }
                    }
                }
                if (!ignoreItem) {
                    stateSpec = StateSet.trimStateSet(stateSpec, j)
                    checkNotNull(baseColor) { "No ColorValue Specified" }
                    if (listSize + 1 >= listAllocated) {
                        listAllocated = idealIntArraySize(listSize + 1)
                        val ncolor = IntArray(listAllocated)
                        System.arraycopy(colorList, 0, ncolor, 0, listSize)
                        val nstate = arrayOfNulls<IntArray>(listAllocated)
                        System.arraycopy(stateSpecList, 0, nstate, 0, listSize)
                        colorList = ncolor
                        stateSpecList = nstate
                    }
                    val color = modulateColorAlpha(baseColor, alphaMod)
                    colorList[listSize] = color
                    stateSpecList[listSize] = stateSpec
                    listSize++
                }
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

    class IntV(val value: Int) : ColorV() {
        companion object {
            val BLACK = IntV(0)

            fun valueOf(number: Int): IntV {
                if (number == 0) {
                    return BLACK
                }
                return cache.get(number.toString()) as? IntV ?: IntV(number)
            }
        }

        init {
            cache.put(value.toString(), this)
        }

        override fun copy(): IntV = this
        override fun apply(context: Context): Result = Result(value, null)
    }

    class StateList(val colors: IntArray, val states: Array<IntArray?>) : ColorV() {
        override fun copy(): Value = this
        override fun apply(context: Context): Result = Result(null, ColorStateList(states, colors))
    }

    data class Result(val color: Int?, val colors: ColorStateList?)
}

open class DrawableV : Value {}
