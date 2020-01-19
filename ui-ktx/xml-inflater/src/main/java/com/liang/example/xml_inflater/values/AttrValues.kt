@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.liang.example.xml_inflater.values

import android.content.Context
import android.content.res.TypedArray
import android.util.TypedValue
import com.liang.example.basic_ktx.KKMap
import com.liang.example.basic_ktx.ReflectHelper
import com.liang.example.xml_inflater.Attr
import com.liang.example.xml_inflater.FormatValue
import com.liang.example.xml_inflater.throwFlag
import java.lang.StringBuilder
import java.util.regex.Pattern

abstract class AttrProcessor<T>(_attr: Attr, vararg _formats: String) {
    open var formats: MutableList<String> = _formats.toMutableList()
    open var attr: Attr = _attr
        set(value) {
            if (attr.format == null || formats.containsAll(attr.format!!.split('|'))) {
                field = value
            } else {
                throw RuntimeException("attr's format isn't $formats: $value")
            }
        }

    open fun from(s: String?): AttrValue<T>? {
        if (s.isNullOrEmpty()) {
            return null
        }
        if (!cache.containsKey(s)) {
            val created = innerFrom(s)
            cache[s] = created
            return created
        }
        return cache[s] as? AttrValue<T>
    }

    protected abstract fun innerFrom(s: String): AttrValue<T>?

    abstract class AttrValue<T>(open val attr: Attr, _value: T) : FormatValue<T>(_value) {
        override fun copy(): AttrValue<T> = super.copy() as AttrValue<T>
    }

    companion object {
        val cache = LinkedHashMap<String, AttrValue<*>?>(258)
    }
}

open class WrongAttrProcessor : AttrProcessor<Unit>(Attr.ATTR_EMPTY) {
    override fun innerFrom(s: String): AttrValue<Unit>? = null

    companion object {
        val WRONG_PROCESSOR = WrongAttrProcessor()
    }
}

/**
 * px / dp / sp / pt / in / mm
 */
open class DimenAttrProcessor(attr: Attr) : AttrProcessor<Float>(attr, "dimension", "dimen") {
    override fun innerFrom(s: String): AttrValue<Float>? {
        val len = s.length
        return when {
            s in attr -> DimenAttrValue(attr, attr[s]!!.toFloat(), DIMENSION_UNIT_ENUM)
            len <= 2 -> DimenAttrValue(attr, 0.0f, DIMENSION_UNIT_PX)
            else -> DimenAttrValue(attr, s.substring(0, len - 2).toFloatOrNull()
                    ?: 0.0f, dimenUnitsMap[s.substring(len - 2)]
                    ?: return DimenAttrValue(attr, 0.0f, DIMENSION_UNIT_PX))
        }
    }

    open class DimenAttrValue(attr: Attr, value: Float, _unit: Int) : AttrValue<Float>(attr, value) {
        open var unit: Int = _unit
            set(value) {
                if (value in dimenUnitsMap.values || value == DIMENSION_UNIT_ENUM) {
                    field = value
                } else if (throwFlag) {
                    throw RuntimeException("unit cannot be set to $value")
                }
            }

        override fun innerCopy(): AttrValue<Float> = DimenAttrValue(attr, value, unit)
        override fun innerString(): String = when (unit) {
            DIMENSION_UNIT_ENUM -> attr[value.toLong()]!!
            else -> "$value${dimenUnitsMap.getVK(unit)}"
        }

        open fun apply(context: Context): Float = staticApply(context, this)
    }

    companion object {
        const val DIMENSION_UNIT_ENUM = -2
        const val DIMENSION_UNIT_PX = TypedValue.COMPLEX_UNIT_PX
        const val DIMENSION_UNIT_DP = TypedValue.COMPLEX_UNIT_DIP
        const val DIMENSION_UNIT_SP = TypedValue.COMPLEX_UNIT_SP
        const val DIMENSION_UNIT_PT = TypedValue.COMPLEX_UNIT_PT
        const val DIMENSION_UNIT_IN = TypedValue.COMPLEX_UNIT_IN
        const val DIMENSION_UNIT_MM = TypedValue.COMPLEX_UNIT_MM

        const val SUFFIX_PX = "px"
        const val SUFFIX_DP = "dp"
        const val SUFFIX_SP = "sp"
        const val SUFFIX_PT = "pt"
        const val SUFFIX_IN = "in"
        const val SUFFIX_MM = "mm"

        val dimenUnitsMap: KKMap<String, Int> = KKMap(6)

        init {
            dimenUnitsMap[SUFFIX_PX] = DIMENSION_UNIT_PX
            dimenUnitsMap[SUFFIX_DP] = DIMENSION_UNIT_DP
            dimenUnitsMap[SUFFIX_SP] = DIMENSION_UNIT_SP
            dimenUnitsMap[SUFFIX_PT] = DIMENSION_UNIT_PT
            dimenUnitsMap[SUFFIX_IN] = DIMENSION_UNIT_IN
            dimenUnitsMap[SUFFIX_MM] = DIMENSION_UNIT_MM
        }

        fun staticApply(context: Context, dimen: DimenAttrValue) = when (dimen.unit) {
            DIMENSION_UNIT_ENUM -> dimen.value()
            DIMENSION_UNIT_PX,
            DIMENSION_UNIT_DP,
            DIMENSION_UNIT_SP,
            DIMENSION_UNIT_PT,
            DIMENSION_UNIT_MM,
            DIMENSION_UNIT_IN -> TypedValue.applyDimension(dimen.unit, dimen.value(), context.resources.displayMetrics)
            else -> 0.0f
        }
    }
}

/**
 * 100% / 100%p / 100%r
 */
open class FractionAttrProcessor(attr: Attr) : AttrProcessor<Float>(attr, "fraction") {
    override fun innerFrom(s: String): AttrValue<Float>? {
        val len = s.length
        if (s == "0") {
            return FractionAttrValue(attr, 0.0f, RELATIVE_TO_SELF)
        }
        if (len == 1) {
            return null
        }
        val start = if (s[0] == '-') 1 else 0
        val last = s.last()
        val end = if (last == 'r' || last == 'p') {
            if (len == 2) {
                return null
            }
            len - 2
        } else len - 1
        return when {
            s[end] != '%' -> null
            else -> {
                val value = s.substring(start, end).toFloatOrNull() ?: return null
                return FractionAttrValue(attr, value, relatives.get(last.toString())!!)
            }
        }
    }

    open class FractionAttrValue(_attr: Attr, _value: Float, _mode: Int) : AttrValue<Float>(_attr, _value) {
        override var value: Float = _value
            set(value) {
                if (value < 100 && value > -100) {
                    field = value
                } else if (throwFlag) {
                    throw RuntimeException("fraction's value is incorrect, it should be between (-100, 100), but it's $field")
                }
            }
        open var mode: Int = _mode
            set(value) {
                if (value in relatives.values) {
                    field = value
                } else if (throwFlag) {
                    throw RuntimeException("fraction mode is incorrect, should be one of [${relatives.values.joinToString()}], but it's $field")
                }
            }

        override fun innerString(): String = "${value.toInt()}%${relatives.getVK(mode)}"
        override fun innerCopy(): AttrValue<Float> = FractionAttrValue(attr, value, mode)
    }

    companion object {
        const val RELATIVE_TO_SELF = 0
        const val RELATIVE_TO_PARENT = 1
        const val RELATIVE_TO_ROOT = 2

        const val SUFFIX_SELF = ""
        const val SUFFIX_PARENT = "p"
        const val SUFFIX_ROOT = "r"

        val relatives = KKMap(
                SUFFIX_SELF to RELATIVE_TO_SELF,
                SUFFIX_PARENT to RELATIVE_TO_PARENT,
                SUFFIX_ROOT to RELATIVE_TO_ROOT
        )
    }
}

/**
 * @(\w*?)(:?)(\w*?)/(\w*) / \?(\w*?)(:?)(\w*?)/(\w*)
 * package_name res_type res_name
 *
 * @+id/comment_input_edit --> 会被特殊处理，这里不考虑
 * @android:id/text1
 * @string/app_name
 * ?android:attr/actionModeCutDrawable
 * ?android:textColorSecondary
 * ?colorAccent
 */
open class ReferenceAttrProcessor(attr: Attr, open var context: Context) : AttrProcessor<Int>(attr, "reference", "refer") {
    override fun innerFrom(s: String): AttrValue<Int>? {
        val len = s.length
        if (len < 5) {
            return null
        }
        val first = s.first()
        val matcher = when (first) {
            '?' -> pattern2.matcher(s)
            '@' -> pattern1.matcher(s)
            else -> return null
        }
        val matches = matcher.matches()
        var packageName: String = context.packageName
        var resType = DEFAULT_RES_TYPE
        val resName = if (matches) {
            packageName = matcher.group(1)!!
            if (packageName.isEmpty()) {
                packageName = context.packageName
            }
            resType = matcher.group(3)!!
            if (resType.isEmpty()) {
                resType = DEFAULT_RES_TYPE
            }
            matcher.group(4)!!
        } else {
            s.substring(1)
        }
        if (cache.containsKey("${resType}_$resName")) {
            return ReferenceAttrValue(attr, first, packageName, resType, resName, INNER_RES)
        }
        val resId = ReflectHelper.getInt("$packageName.R\$$resType", resName)
        return when {
            resId != 0 -> ReferenceAttrValue(attr, first, packageName, resType, resName, resId)
            else -> null
        }
    }

    open class ReferenceAttrValue(
            _attr: Attr, open var prefix: Char, open var packageName: String?, open var resType: String?, open var resName: String, value: Int
    ) : AttrValue<Int>(_attr, value) {
        override fun innerString(): String = "$prefix$packageName:$resType/$resName"
        override fun innerCopy(): AttrValue<Int> = ReferenceAttrValue(attr, prefix, packageName, resType, resName, value)
        open fun apply(context: Context): TypedArray = context.obtainStyledAttributes(intArrayOf(value))
        open fun isInnerRes() = value == INNER_RES
    }

    companion object {
        val validRefType = mutableListOf("animator", "anim", "drawable", "color", "layout", "menu",
                "style", "string", "id", "font", "dimen", "fraction", "integer", "bool", "array", "attr")

        val pattern1: Pattern = Pattern.compile("@(\\w*?)(:?)(\\w*?)/(\\w*)", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val pattern2: Pattern = Pattern.compile("\\?(\\w*?)(:?)(\\w*?)/(\\w*)", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)

        const val INNER_RES = -1
        const val DEFAULT_RES_TYPE = "attr"

        const val PREFIX_ATTR = '?'
        const val PREFIX_OTHER = '@'
        // https://blog.csdn.net/xdy1120/article/details/98478691 -- @android, ?attr/ 和 ?android 的区别

        fun staticApply(context: Context, refer: ReferenceAttrValue): TypedArray = context.obtainStyledAttributes(intArrayOf(refer.value()))
    }
}

/**
 * #rgb / #argb / #rrggbb / #aarrggbb /red / green / blue / ...
 * TODO: 其他类型的color，而不仅仅是 argb
 */
open class ColorAttrProcessor(attr: Attr) : AttrProcessor<Long>(attr, "color") {
    override fun innerFrom(s: String): AttrValue<Long>? = when {
        s.startsWith('#') -> {
            val len = s.length
            var s2 = when {
                len < 7 -> {
                    val sb = StringBuilder("#")
                    (1..len).forEach { sb.append(s[it]).append(s[it]) }
                    sb.toString()
                }
                else -> s
            }
            if (s2.length == 7) {
                s2 = "#ff${s2.substring(1)}"
            }
            when {
                s2.length != 9 -> null
                else -> when (val temp = s2.substring(1).toLongOrNull(16)) {
                    null -> null
                    else -> ColorAttrValue(attr, temp)
                }
            }
        }
        colors.containsKey(s) -> ColorAttrValue(attr, colors[s]!!)
        else -> null
    }

    open class ColorAttrValue(_attr: Attr, value: Long) : AttrValue<Long>(_attr, value) {
        override fun innerString(): String = "#${value.toString(16)}"
        override fun innerCopy(): AttrValue<Long> = ColorAttrValue(attr, value)
    }

    companion object {
        val colors = mutableMapOf("black" to 0xFF000000, "darkgray" to 0xFF444444, "gray" to 0xFF888888, "lightgray" to 0xFFCCCCCC,
                "white" to 0xFFFFFFFF, "red" to 0xFFFF0000, "green" to 0xFF00FF00, "blue" to 0xFF0000FF, "yellow" to 0xFFFFFF00, "cyan" to 0xFF00FFFF,
                "magenta" to 0xFFFF00FF, "aqua" to 0xFF00FFFF, "fuchsia" to 0xFFFF00FF, "lime" to 0xFF00FF00, "maroon" to 0xFF800000, "navy" to 0xFF000080,
                "olive" to 0xFF808000, "purple" to 0xFF800080, "silver" to 0xFFC0C0C0, "teal" to 0xFF008080)

        val BLACK = ColorAttrValue(Attr.ATTR_EMPTY, 0xFF000000)
    }
}

/**
 * none
 */
open class StringAttrProcessor(attr: Attr) : AttrProcessor<String>(attr, "string", "str") {
    override fun innerFrom(s: String): AttrValue<String>? = StringAttrValue(attr, s)
    open class StringAttrValue(_attr: Attr, value: String) : AttrValue<String>(_attr, value) {
        override fun innerCopy(): AttrValue<String> = StringAttrValue(attr, value)
    }
}

/**
 * true / false / 0 / 1
 */
open class BooleanAttrProcessor(attr: Attr) : AttrProcessor<Boolean>(attr, "boolean") {
    override fun innerFrom(s: String): AttrValue<Boolean>? = when (s) {
        "1", "true" -> ATTR_TRUE
        "0", "false" -> ATTR_FALSE
        else -> ATTR_FALSE
    }

    open class BooleanAttrValue(_attr: Attr, value: Boolean) : AttrValue<Boolean>(_attr, value) {
        override fun innerCopy(): AttrValue<Boolean> = BooleanAttrValue(attr, value)
    }

    companion object {
        val ATTR_TRUE = BooleanAttrValue(Attr.ATTR_EMPTY, true)
        val ATTR_FALSE = BooleanAttrValue(Attr.ATTR_EMPTY, false)
    }
}

open class IntegerAttrProcessor(attr: Attr) : AttrProcessor<Long>(attr, "integer", "int") {
    override fun innerFrom(s: String): AttrValue<Long>? = when (s) {
        in attr -> IntegerAttrValue(attr, attr[s]!!, INTEGER_TYPE_ENUM)
        else -> when (val temp = s.toLongOrNull()) {
            null -> null
            else -> IntegerAttrValue(attr, temp, INTEGER_TYPE_NORMAL)
        }
    }

    open class IntegerAttrValue(_attr: Attr, value: Long, open var type: Int) : AttrValue<Long>(_attr, value) {
        override fun innerCopy(): AttrValue<Long> = IntegerAttrValue(attr, value, type)
        override fun string(): String = when (type) {
            INTEGER_TYPE_ENUM -> attr[value]!!
            else -> value.toString()
        }

        open fun int() = value.toInt()
    }

    companion object {
        const val INTEGER_TYPE_ENUM = -1
        const val INTEGER_TYPE_NORMAL = 0
    }
}

open class FloatAttrProcessor(attr: Attr) : AttrProcessor<Float>(attr, "float") {
    override fun innerFrom(s: String): AttrValue<Float>? {
        return FloatAttrValue(attr, s.toFloatOrNull() ?: return null)
    }

    open class FloatAttrValue(_attr: Attr, value: Float) : AttrValue<Float>(_attr, value) {
        override fun innerCopy(): AttrValue<Float> = FloatAttrValue(attr, value)
    }
}

open class EnumAttrProcessor(attr: Attr) : AttrProcessor<Long>(attr, "enum") {
    override fun innerFrom(s: String): AttrValue<Long>? = when (s) {
        in attr -> EnumAttrValue(attr, attr[s]!!)
        else -> null
    }

    open class EnumAttrValue(_attr: Attr, value: Long) : AttrValue<Long>(_attr, value) {
        override fun innerString(): String = attr[value]!!
        override fun innerCopy(): AttrValue<Long> = EnumAttrValue(attr, value)
    }
}

open class FlagAttrProcessor(attr: Attr) : AttrProcessor<Long>(attr, "flag") {
    override fun innerFrom(s: String): AttrValue<Long>? {
        val flags = s.split('|').filter { it in attr }
        if (flags.isEmpty()) {
            return null
        }
        var value: Long = 0
        flags.forEach { value = value.or(attr[it]!!) }
        return FlagsAttrValue(attr, value)
    }

    open class FlagsAttrValue(_attr: Attr, value: Long) : AttrValue<Long>(_attr, value) {
        override fun innerCopy(): AttrValue<Long> = FlagsAttrValue(attr, value)
        override fun innerString(): String = attr.values!!.filter { it.second.and(value) == it.second }.joinToString("|") { it.first }
    }
}

open class ComplexAttrProcessor(attr: Attr, _processors: MutableList<AttrProcessor<*>>) : AttrProcessor<Any>(attr) {
    protected var processors: MutableList<AttrProcessor<*>> = _processors
    override var formats: MutableList<String> = mutableListOf()

    init {
        _processors.forEach { formats.addAll(it.formats) }
    }

    operator fun plus(processor: AttrProcessor<*>): Boolean {
        if (processor.formats.isEmpty() || processor.formats.any { it in formats }) {
            return false
        }
        formats.addAll(processor.formats)
        return processors.add(processor)
    }

    operator fun get(format: String): AttrProcessor<*>? = processors.find { format in it.formats }
    operator fun minus(format: String): Boolean = this.formats.remove(format) && processors.removeAll { format in it.formats }
    open fun size() = processors.size

    open fun goodFormats(vararg formats: String): Boolean = formats.all { it in this.formats }

    override fun innerFrom(s: String): AttrValue<Any>? {
        processors.forEach {
            val temp = it.from(s)
            if (temp != null) {
                return temp as AttrValue<Any>
            }
        }
        return null
    }
}

interface IAttrProcessorManager {
    var processors: MutableList<AttrProcessor<*>>

    operator fun plus(processor: AttrProcessor<*>): Boolean
    operator fun plus(processors: Collection<AttrProcessor<*>>): Boolean

    operator fun minus(processor: AttrProcessor<*>): Boolean
    operator fun minus(index: Int): AttrProcessor<*>
    operator fun minus(processors: Collection<AttrProcessor<*>>): Boolean

    operator fun get(index: Int): AttrProcessor<*>
    operator fun get(format: String): AttrProcessor<*>?
    operator fun <T : AttrProcessor<*>> get(clazz: Class<*>): T?
    fun getComplex(vararg formats: String): AttrProcessor<*>?

    fun getAll(format: String): List<AttrProcessor<*>>
    fun <T : AttrProcessor<*>> getAll(clazz: Class<*>): List<T>

    fun dimen(): DimenAttrProcessor
    fun fraction(): FractionAttrProcessor
    fun refer(): ReferenceAttrProcessor
    fun color(): ColorAttrProcessor
    fun str(): StringAttrProcessor
    fun bool(): BooleanAttrProcessor
    fun int(): IntegerAttrProcessor
    fun float(): FloatAttrProcessor
    fun enum(): EnumAttrProcessor
    fun flag(): FlagAttrProcessor
}

open class AttrProcessorManager(override var processors: MutableList<AttrProcessor<*>>) : AttrProcessor<Any>(Attr.ATTR_EMPTY), IAttrProcessorManager {
    override fun plus(processor: AttrProcessor<*>) = processors.add(processor)
    override fun plus(processors: Collection<AttrProcessor<*>>) = this.processors.addAll(processors)

    override fun minus(processor: AttrProcessor<*>) = processors.remove(processor)
    override fun minus(index: Int) = processors.removeAt(index)
    override fun minus(processors: Collection<AttrProcessor<*>>) = this.processors.removeAll(processors)

    override fun <T : AttrProcessor<*>> get(clazz: Class<*>) = processors.filterIsInstance(clazz).getOrNull(0) as? T
    override fun get(format: String) = processors.find { format in it.formats }
    override fun get(index: Int) = processors[index]
    override fun getComplex(vararg formats: String): AttrProcessor<*>? {
        processors.forEach {
            if (it is ComplexAttrProcessor && it.goodFormats(*formats)) {
                return it
            }
        }
        val processors2 = mutableListOf<AttrProcessor<*>>()
        formats.forEach { processors2.add(get(it) ?: return null) }
        val result = ComplexAttrProcessor(Attr.ATTR_EMPTY, processors2)
        plus(result)
        return result
    }

    override fun getAll(format: String): List<AttrProcessor<*>> = processors.filter { format in it.formats }
    override fun <T : AttrProcessor<*>> getAll(clazz: Class<*>): List<T> = processors.filterIsInstance(clazz) as List<T>

    override fun dimen(): DimenAttrProcessor = get("dimen") as DimenAttrProcessor
    override fun fraction(): FractionAttrProcessor = get("fraction") as FractionAttrProcessor
    override fun refer(): ReferenceAttrProcessor = get("refer") as ReferenceAttrProcessor
    override fun color(): ColorAttrProcessor = get("color") as ColorAttrProcessor
    override fun str(): StringAttrProcessor = get("str") as StringAttrProcessor
    override fun bool(): BooleanAttrProcessor = get("bool") as BooleanAttrProcessor
    override fun int(): IntegerAttrProcessor = get("int") as IntegerAttrProcessor
    override fun float(): FloatAttrProcessor = get("float") as FloatAttrProcessor
    override fun enum(): EnumAttrProcessor = get("enum") as EnumAttrProcessor
    override fun flag(): FlagAttrProcessor = get("flag") as FlagAttrProcessor

    override fun innerFrom(s: String): AttrValue<Any>? {
        for (processor in processors) {
            val temp = processor.from(s)
            if (temp != null) {
                return temp as AttrValue<Any>
            }
        }
        return null
    }
}
