@file:Suppress("MemberVisibilityCanBePrivate", "UNCHECKED_CAST", "unused")

package com.liang.example.xml_inflater2

import android.content.Context
import android.content.res.TypedArray
import android.util.TypedValue
import com.liang.example.basic_ktx.KKMap
import com.liang.example.basic_ktx.ReflectHelper
import com.liang.example.view_ktx.getIntIdByStrId
import com.liang.example.xml_inflater2.AtomicType.Companion.ATTR_TYPE
import java.util.regex.Pattern

abstract class AttrProcessor<T>(open var attrProcessorManager: IAttrProcessorManager?, _attr: Attr, _type: Int, vararg _formats: String) {
    open var formats: MutableList<String> = _formats.toMutableList()
    open val type = ATTR_TYPE.typeMaps.getVK(_type)
    open var mAttr: Attr = _attr
        set(value) {
            if (mAttr.format == null || formats.containsAll(mAttr.format!!.split('|'))) {
                field = value
            } else {
                throw RuntimeException("attr's format isn't ${formats.joinToString()}: $value")
            }
        }

    open fun attr(attr: Attr?): AttrProcessor<T> {
        this.mAttr = attr ?: Attr.ATTR_EMPTY
        return this
    }

    open fun from(s: String?): AttrValue<T>? {
        if (s.isNullOrEmpty()) {
            return null
        }
        val key = "<${type}_${s}>"  // todo: attrValue's key should be other format
        if (!cache.containsKey(key)) {
            val created = innerFrom(s)
            cache[key] = created
            return created
        }
        return cache[key] as? AttrValue<T>
    }

    protected abstract fun innerFrom(s: String): AttrValue<T>?

    abstract class AttrValue<T>(_type: Int, _value: T) : FormatValue<T>(_value) {
        open val type = _type
        override fun copy(): AttrValue<T> = super.copy() as AttrValue<T>
    }

    companion object {
        val cache = LinkedHashMap<String, AttrValue<*>?>(258)

        val REFER = ATTR_TYPE.inc("refer")
        val DIMEN = ATTR_TYPE.inc("dimen")
        val FRACTION = ATTR_TYPE.inc("fraction")
        val COLOR = ATTR_TYPE.inc("color")
        val STRING = ATTR_TYPE.inc("str")
        val BOOLEAN = ATTR_TYPE.inc("bool")
        val INTEGER = ATTR_TYPE.inc("int")
        val FLOAT = ATTR_TYPE.inc("float")
        val ENUM = ATTR_TYPE.inc("enum")
        val FLAG = ATTR_TYPE.inc("flag")
    }
}

/**
 * @(\w*?)(:?)(\w*?)/(\w*) / \?(\w*?)(:?)(\w*?)/(\w*)
 * package_name res_type res_name
 *
 * @+id/comment_input_edit
 * @android:id/text1
 * @string/app_name
 * ?android:attr/actionModeCutDrawable
 * ?android:textColorSecondary
 * ?colorAccent
 */
open class ReferenceAttrProcessor(manager: IAttrProcessorManager, attr: Attr = Attr.ATTR_EMPTY)
    : AttrProcessor<Int>(manager, attr, REFER, "reference", "refer") {
    override fun from(s: String?): ReferenceAttrValue? = super.from(s) as? ReferenceAttrValue

    override fun innerFrom(s: String): AttrValue<Int>? {
        val context = attrProcessorManager!!.context
        val len = s.length
        if (len < 3) {
            return null
        }
        if (s.startsWith("@+id/")) {
            return when {
                len > 5 -> ReferenceAttrValue('@', null, "id", s.substring(5))
                else -> null
            }
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
        if (resType !in validRefType) {
            return null
        }
        return ReferenceAttrValue(first, packageName, resType, resName)
    }

    open class ReferenceAttrValue(
            open var prefix: Char, open var packageName: String?, open var resType: String, open var resName: String
    ) : AttrValue<Int>(REFER, INVALID_RES_ID) {
        override fun value(): Int {
            if (value == INVALID_RES_ID) {
                value = when (resType) {
                    "id" -> getIntIdByStrId(resName)
                    else -> {
                        val resNameKey = ResStore.makeResNameKey(resType, resName)
                        when {
                            ResStore.getRes(resNameKey) != null -> getIntIdByStrId(resNameKey)
                            else -> try {
                                ReflectHelper.getIntStatic(resName, ReflectHelper.findCls("$packageName.R\$$resType"))
                            } catch (e: NoSuchFieldException) {
                                NO_SUCH_RES_ID
                            }
                        }
                    }
                }
            }
            return value
        }

        override fun innerString(): String = "$prefix$packageName:$resType/$resName"
        override fun innerCopy(): AttrValue<Int> = ReferenceAttrValue(prefix, packageName, resType, resName)
        open fun apply(context: Context): TypedArray = context.obtainStyledAttributes(intArrayOf(value()))
        open fun isInner() = ResStore.getRes(ResStore.makeResNameKey(resType, resName)) != null
    }

    companion object {
        val validRefType = mutableListOf("animator", "anim", "drawable", "color", "layout", "menu",
                "style", "string", "id", "font", "dimen", "fraction", "integer", "bool", "array", "attr")

        val pattern1: Pattern = Pattern.compile("@(\\w*?)(:?)(\\w*?)/(\\w*)", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val pattern2: Pattern = Pattern.compile("\\?(\\w*?)(:?)(\\w*?)/(\\w*)", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)

        const val INVALID_RES_ID = -1
        const val NO_SUCH_RES_ID = 0
        const val DEFAULT_RES_TYPE = "attr"

        // https://blog.csdn.net/xdy1120/article/details/98478691 -- @android, ?attr/ 和 ?android 的区别

        fun staticApply(context: Context, refer: ReferenceAttrValue): TypedArray = context.obtainStyledAttributes(intArrayOf(refer.value()))
    }
}

/**
 * px / dp / sp / pt / in / mm
 */
open class DimenAttrProcessor(manager: IAttrProcessorManager, attr: Attr = Attr.ATTR_EMPTY)
    : AttrProcessor<Float>(manager, attr, DIMEN, "dimension", "dimen") {
    override fun attr(attr: Attr?): DimenAttrProcessor = super.attr(attr) as DimenAttrProcessor

    override fun innerFrom(s: String): AttrValue<Float>? {
        val len = s.length
        return when {
            s in mAttr -> DimenAttrValue(mAttr[s]!!.toFloat(), DIMENSION_UNIT_ENUM)
            len <= 2 -> null
            else -> {
                val splitIndex = len - 2
                val unit = dimenUnitsMap[s.substring(splitIndex)] ?: return null
                val value = s.substring(0, splitIndex).toFloatOrNull() ?: return null
                DimenAttrValue(value, unit)
            }
        }
    }

    open class DimenAttrValue(value: Float, _unit: Int) : AttrValue<Float>(DIMEN, value) {
        open var unit: Int = _unit
            set(value) {
                if (value in dimenUnitsMap.values || value == DIMENSION_UNIT_ENUM) {
                    field = value
                } else if (throwFlag) {
                    throw RuntimeException("unit cannot be set to $value")
                }
            }

        override fun innerCopy(): AttrValue<Float> = DimenAttrValue(value, unit)
        override fun innerString(): String = "$value${dimenUnitsMap.getVK(unit)}"

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
open class FractionAttrProcessor(manager: IAttrProcessorManager, attr: Attr = Attr.ATTR_EMPTY)
    : AttrProcessor<Float>(manager, attr, FRACTION, "fraction") {
    override fun innerFrom(s: String): AttrValue<Float>? {
        val len = s.length
        if (s == "0") {
            return FractionAttrValue(0.0f, RELATIVE_TO_SELF)
        }
        if (len <= 1) {
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
                return FractionAttrValue(value, relatives[last.toString()]!!)
            }
        }
    }

    open class FractionAttrValue(_value: Float, _mode: Int) : AttrValue<Float>(FRACTION, _value) {
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
        override fun innerCopy(): AttrValue<Float> = FractionAttrValue(value, mode)
    }

    companion object {
        const val RELATIVE_TO_SELF = 0
        const val RELATIVE_TO_PARENT = 1
        const val RELATIVE_TO_ROOT = 2

        const val SUFFIX_SELF = "%"
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
 * #rgb / #argb / #rrggbb / #aarrggbb /red / green / blue / ...
 * TODO: 其他类型的color，而不仅仅是 argb
 */
open class ColorAttrProcessor(manager: IAttrProcessorManager, attr: Attr = Attr.ATTR_EMPTY)
    : AttrProcessor<Long>(manager, attr, COLOR, "color") {
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
                    else -> ColorAttrValue(temp)
                }
            }
        }
        colors.containsKey(s) -> ColorAttrValue(colors[s]!!)
        else -> null
    }

    open class ColorAttrValue(value: Long) : AttrValue<Long>(COLOR, value) {
        override fun innerString(): String = "#${value.toString(16)}"
        override fun innerCopy(): AttrValue<Long> = ColorAttrValue(value)
    }

    companion object {
        val colors = mutableMapOf("black" to 0xFF000000, "darkgray" to 0xFF444444, "gray" to 0xFF888888, "lightgray" to 0xFFCCCCCC,
                "white" to 0xFFFFFFFF, "red" to 0xFFFF0000, "green" to 0xFF00FF00, "blue" to 0xFF0000FF, "yellow" to 0xFFFFFF00, "cyan" to 0xFF00FFFF,
                "magenta" to 0xFFFF00FF, "aqua" to 0xFF00FFFF, "fuchsia" to 0xFFFF00FF, "lime" to 0xFF00FF00, "maroon" to 0xFF800000, "navy" to 0xFF000080,
                "olive" to 0xFF808000, "purple" to 0xFF800080, "silver" to 0xFFC0C0C0, "teal" to 0xFF008080)

        fun isColor(value: String?) = value != null && value.startsWith("#") && value in colors.keys
    }
}

/**
 * none
 */
open class StringAttrProcessor(manager: IAttrProcessorManager, attr: Attr = Attr.ATTR_EMPTY)
    : AttrProcessor<String>(manager, attr, STRING, "string", "str") {
    override fun innerFrom(s: String): AttrValue<String>? = StringAttrValue(s)
    open class StringAttrValue(value: String) : AttrValue<String>(STRING, value) {
        override fun innerCopy(): AttrValue<String> = StringAttrValue(value)
    }
}

/**
 * true / false / 0 / 1
 */
open class BooleanAttrProcessor(manager: IAttrProcessorManager, attr: Attr = Attr.ATTR_EMPTY)
    : AttrProcessor<Boolean>(manager, attr, BOOLEAN, "boolean", "bool") {
    override fun innerFrom(s: String): AttrValue<Boolean>? = when (s) {
        "1", "true" -> ATTR_TRUE
        "0", "false" -> ATTR_FALSE
        else -> ATTR_FALSE
    }

    open class BooleanAttrValue(value: Boolean) : AttrValue<Boolean>(BOOLEAN, value) {
        override fun innerCopy(): AttrValue<Boolean> = BooleanAttrValue(value)
    }

    companion object {
        val ATTR_TRUE = BooleanAttrValue(true)
        val ATTR_FALSE = BooleanAttrValue(false)
    }
}

open class IntegerAttrProcessor(manager: IAttrProcessorManager, attr: Attr = Attr.ATTR_EMPTY)
    : AttrProcessor<Long>(manager, attr, INTEGER, "integer", "int") {
    override fun attr(attr: Attr?): IntegerAttrProcessor = super.attr(attr) as IntegerAttrProcessor

    override fun innerFrom(s: String): AttrValue<Long>? = when (s) {
        in mAttr -> IntegerAttrValue(mAttr[s]!!, INTEGER_TYPE_ENUM)
        else -> when (val temp = s.toLongOrNull()) {
            null -> null
            else -> IntegerAttrValue(temp, INTEGER_TYPE_NORMAL)
        }
    }

    open class IntegerAttrValue(value: Long, open var intType: Int) : AttrValue<Long>(INTEGER, value) {
        override fun innerCopy(): AttrValue<Long> = IntegerAttrValue(value, intType)
        open fun int() = value.toInt()
    }

    companion object {
        const val INTEGER_TYPE_ENUM = -1
        const val INTEGER_TYPE_NORMAL = 0
    }
}

open class FloatAttrProcessor(manager: IAttrProcessorManager, attr: Attr = Attr.ATTR_EMPTY)
    : AttrProcessor<Float>(manager, attr, FLOAT, "float") {
    override fun innerFrom(s: String): AttrValue<Float>? {
        return FloatAttrValue(s.toFloatOrNull() ?: return null)
    }

    open class FloatAttrValue(value: Float) : AttrValue<Float>(FLOAT, value) {
        override fun innerCopy(): AttrValue<Float> = FloatAttrValue(value)
    }
}

open class EnumAttrProcessor(manager: IAttrProcessorManager, attr: Attr = Attr.ATTR_EMPTY)
    : AttrProcessor<Long>(manager, attr, ENUM, "enum") {
    override fun attr(attr: Attr?): EnumAttrProcessor = super.attr(attr) as EnumAttrProcessor

    override fun innerFrom(s: String): AttrValue<Long>? = when (s) {
        in mAttr -> EnumAttrValue(mAttr, mAttr[s]!!)
        else -> null
    }

    open class EnumAttrValue(open val attr: Attr, value: Long) : AttrValue<Long>(ENUM, value) {
        override fun innerString(): String = attr[value]!!
        override fun innerCopy(): AttrValue<Long> = EnumAttrValue(attr, value)
    }
}

open class FlagAttrProcessor(manager: IAttrProcessorManager, attr: Attr = Attr.ATTR_EMPTY)
    : AttrProcessor<Long>(manager, attr, FLAG, "flag") {
    override fun attr(attr: Attr?): FlagAttrProcessor = super.attr(attr) as FlagAttrProcessor

    override fun innerFrom(s: String): AttrValue<Long>? {
        val flags = s.split('|').filter { it in mAttr }
        if (flags.isEmpty()) {
            return null
        }
        var value: Long = 0
        flags.forEach { value = value.or(mAttr[it]!!) }
        return FlagAttrValue(mAttr, value)
    }

    open class FlagAttrValue(open val attr: Attr, value: Long) : AttrValue<Long>(FLAG, value) {
        override fun innerCopy(): AttrValue<Long> = FlagAttrValue(attr, value)
        override fun innerString(): String = attr.values!!.filter { it.second.and(value) == it.second }.joinToString("|") { it.first }
    }
}

interface IAttrProcessorManager {
    var processors: MutableList<AttrProcessor<*>>
    var context: Context

    operator fun plus(processor: AttrProcessor<*>): Boolean
    operator fun plus(processors: Collection<AttrProcessor<*>>): Boolean

    operator fun minus(processor: AttrProcessor<*>): Boolean
    operator fun minus(index: Int): AttrProcessor<*>
    operator fun minus(processors: Collection<AttrProcessor<*>>): Boolean

    operator fun get(index: Int): AttrProcessor<*>
    operator fun get(format: String): AttrProcessor<*>?
    fun <T : AttrProcessor<*>> get(clazz: Class<*>): T?

    fun getAll(format: String): List<AttrProcessor<*>>
    fun <T : AttrProcessor<*>> getAll(clazz: Class<*>): List<T>

    fun dimen(s: String?, attr: Attr? = null): Float?
    fun fraction(s: String?): Float?
    fun float(s: String?): Float?
    fun refer(s: String?): Int?
    fun str(s: String?): String?
    fun bool(s: String?): Boolean?
    fun color(s: String?): Long?
    fun int(s: String?, attr: Attr? = null): Long?
    fun enum(s: String?, attr: Attr): Long?
    fun flag(s: String?, attr: Attr): Long?
}

open class AttrProcessorManager(override var context: Context, override var processors: MutableList<AttrProcessor<*>> = mutableListOf())
    : AttrProcessor<Any>(null, Attr.ATTR_EMPTY, 0), IAttrProcessorManager {
    override fun plus(processor: AttrProcessor<*>) = processors.add(processor)
    override fun plus(processors: Collection<AttrProcessor<*>>) = this.processors.addAll(processors)

    override fun minus(processor: AttrProcessor<*>) = processors.remove(processor)
    override fun minus(index: Int) = processors.removeAt(index)
    override fun minus(processors: Collection<AttrProcessor<*>>) = this.processors.removeAll(processors)

    override fun <T : AttrProcessor<*>> get(clazz: Class<*>) = processors.filterIsInstance(clazz).getOrNull(0) as? T
    override fun get(format: String) = processors.find { format in it.formats }
    override fun get(index: Int) = processors[index]

    override fun getAll(format: String): List<AttrProcessor<*>> = processors.filter { format in it.formats }
    override fun <T : AttrProcessor<*>> getAll(clazz: Class<*>): List<T> = processors.filterIsInstance(clazz) as List<T>

    override fun dimen(s: String?, attr: Attr?): Float? =
            (get("dimen") as DimenAttrProcessor).attr(attr).from(s)?.let { DimenAttrProcessor.staticApply(context, it as DimenAttrProcessor.DimenAttrValue) }

    override fun fraction(s: String?): Float? = (get("fraction") as FractionAttrProcessor).from(s)?.value()
    override fun refer(s: String?): Int? = (get("refer") as ReferenceAttrProcessor).from(s)?.value()
    override fun color(s: String?): Long? = (get("color") as ColorAttrProcessor).from(s)?.value()
    override fun str(s: String?): String? = (get("str") as StringAttrProcessor).from(s)?.value()
    override fun bool(s: String?): Boolean? = (get("bool") as BooleanAttrProcessor).from(s)?.value()
    override fun int(s: String?, attr: Attr?): Long? = (get("int") as IntegerAttrProcessor).attr(attr).from(s)?.value()
    override fun float(s: String?): Float? = (get("float") as FloatAttrProcessor).from(s)?.value()
    override fun enum(s: String?, attr: Attr): Long? = (get("enum") as EnumAttrProcessor).attr(attr).from(s)?.value()
    override fun flag(s: String?, attr: Attr): Long? = (get("flag") as FlagAttrProcessor).attr(attr).from(s)?.value()

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
