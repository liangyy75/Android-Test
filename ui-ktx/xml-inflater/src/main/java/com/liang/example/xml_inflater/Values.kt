@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.liang.example.xml_inflater

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.TypedValue
import com.liang.example.basic_ktx.KKMap
import com.liang.example.basic_ktx.ReflectHelper
import java.util.regex.Pattern

abstract class AttrProcessor<T>(open val attr: Attr) {
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

    protected abstract fun innerFrom(s: String): AttrValue<T>?  // 这里本来应该写成static的，但是每个子类又必须继承来方便使用，所以放在这里，然后每个子类添加staticFrom

    abstract class AttrValue<T>(open val attr: Attr) {
        open var str: String? = null  // 修改值之后记得还原 str 为 null

        open fun string(): String {
            if (str == null) {
                str = innerString()
            }
            return str!!
        }

        open fun copy(): AttrValue<T> {
            val result = innerCopy()
            result.str = str
            return result
        }

        protected abstract fun innerString(): String
        protected abstract fun innerCopy(): AttrValue<T>
        abstract fun value(): T
    }

    companion object {
        val cache = LinkedHashMap<String, AttrValue<*>?>(258)
    }
}

abstract class SingleAttrProcessor<T>(_attr: Attr, targetFormat: String) : AttrProcessor<T>(_attr) {
    init {
        if (_attr.format?.split("|")?.contains(targetFormat) == false) {
            throw RuntimeException("attr's format isn't dimension: $_attr")
        }
    }
}

/**
 * px / dp / sp / pt / in / mm
 */
open class DimenAttrProcessor(attr: Attr) : SingleAttrProcessor<Float>(attr, "dimension") {
    override fun innerFrom(s: String): AttrValue<Float>? {
        val len = s.length
        return when {
            attr.values?.containsKey(s) == true -> DimenAttrValue(attr, attr.values!![s]!!.toFloat(), DIMENSION_UNIT_ENUM)
            len <= 2 -> DimenAttrValue(attr, 0.0f, DIMENSION_UNIT_PX)
            else -> DimenAttrValue(attr, s.substring(0, len - 2).toFloatOrNull() ?: 0.0f, dimenUnitsMap[s.substring(len - 2)]
                    ?: return DimenAttrValue(attr, 0.0f, DIMENSION_UNIT_PX))
        }
    }

    open class DimenAttrValue(attr: Attr, open var value: Float, _unit: Int) : AttrValue<Float>(attr) {
        open var unit: Int = _unit
            set(value) {
                if (value in dimenUnitsMap.values || value == DIMENSION_UNIT_ENUM) {
                    field = value
                } else if (throwFlag) {
                    throw RuntimeException("unit cannot be set to $value")
                }
            }

        override fun innerString(): String = "$value${dimenUnitsMap.getVK(unit)}"
        override fun value(): Float = value
        override fun innerCopy(): AttrValue<Float> = DimenAttrValue(attr, value, unit)

        open fun apply(context: Context): Float = when (unit) {
            DIMENSION_UNIT_ENUM -> value.toFloat()
            DIMENSION_UNIT_PX,
            DIMENSION_UNIT_DP,
            DIMENSION_UNIT_SP,
            DIMENSION_UNIT_PT,
            DIMENSION_UNIT_MM,
            DIMENSION_UNIT_IN -> TypedValue.applyDimension(unit, value.toFloat(), context.resources.displayMetrics)
            else -> 0.0f
        }
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
    }
}

/**
 * 100% / 100%p / 100%r
 */
open class FractionAttrProcessor(attr: Attr) : SingleAttrProcessor<Float>(attr, "fraction") {
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

    open class FractionAttrValue(_attr: Attr, _value: Float, _mode: Int) : AttrValue<Float>(_attr) {
        open var value: Float = _value
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
        override fun value(): Float = value
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
open class ReferenceAttrProcessor(attr: Attr, open var context: Context) : SingleAttrProcessor<Int>(attr, "reference") {
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
            _attr: Attr, open var prefix: Char, open var packageName: String?, open var resType: String?, open var resName: String, open var resId: Int
    ) : AttrValue<Int>(_attr) {
        override fun innerString(): String = ""
        override fun innerCopy(): AttrValue<Int> = ReferenceAttrValue(attr, prefix, packageName, resType, resName, resId)
        override fun value(): Int = resId
        open fun apply(context: Context): TypedArray = context.obtainStyledAttributes(intArrayOf(resId))
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
    }
}

/**
 * #rgb / #argb / #rrggbb / #aarrggbb /red / green / blue / ...
 */
open class ColorAttrProcessor(attr: Attr) : SingleAttrProcessor<Int>(attr, "color") {
    override fun innerFrom(s: String): AttrValue<Int>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    open class ColorAttrValue(_attr: Attr) : AttrValue<Int>(_attr) {
        override fun innerString(): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun innerCopy(): AttrValue<Int> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun value(): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    companion object {
        val colors = mutableMapOf("red" to Color.RED, TODO())
    }
}

/**
 * none
 */
open class StringAttrProcessor(attr: Attr) : SingleAttrProcessor<String>(attr, "string") {
    override fun innerFrom(s: String): AttrValue<String>? = StringAttrValue(attr, s)
    open class StringAttrValue(_attr: Attr, open var value: String) : AttrValue<String>(_attr) {
        override fun innerString(): String = value
        override fun innerCopy(): AttrValue<String> = StringAttrValue(attr, value)
        override fun value(): String = value
    }
}

/**
 * true / false / 0 / 1
 */
open class BooleanAttrProcessor(attr: Attr) : SingleAttrProcessor<Boolean>(attr, "boolean") {
    override fun innerFrom(s: String): AttrValue<Boolean>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    open class BooleanAttrValue(_attr: Attr) : AttrValue<Boolean>(_attr) {
        override fun innerString(): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun innerCopy(): AttrValue<Boolean> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun value(): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}

open class IntegerAttrProcessor(attr: Attr) : SingleAttrProcessor<Int>(attr, "integer") {
    override fun innerFrom(s: String): AttrValue<Int>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    open class IntegerAttrValue(_attr: Attr) : AttrValue<Int>(_attr) {
        override fun innerString(): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun innerCopy(): AttrValue<Int> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun value(): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}

open class FloatAttrProcessor(attr: Attr) : SingleAttrProcessor<Float>(attr, "float") {
    override fun innerFrom(s: String): AttrValue<Float>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    open class FloatAttrValue(_attr: Attr) : AttrValue<Float>(_attr) {
        override fun innerString(): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun innerCopy(): AttrValue<Float> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun value(): Float {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}

open class EnumAttrProcessor(attr: Attr) : SingleAttrProcessor<Int>(attr, "enum") {
    override fun innerFrom(s: String): AttrValue<Int>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    open class EnumAttrValue(_attr: Attr) : AttrValue<Int>(_attr) {
        override fun innerString(): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun innerCopy(): AttrValue<Int> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun value(): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}

open class FlagsAttrProcessor(attr: Attr) : SingleAttrProcessor<Int>(attr, "flags") {
    override fun innerFrom(s: String): AttrValue<Int>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    open class FlagsAttrValue(_attr: Attr) : AttrValue<Int>(_attr) {
        override fun innerString(): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun innerCopy(): AttrValue<Int> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun value(): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}

open class ComplexAttrProcessor

// 上面是 attributes + 部分 resources (id / fraction / bool / integer) ，下面是 res -- drawable / anim / animator / color / menu / layout / style

abstract class ResProcessor<T : ResProcessor.ResValue> {
    open fun process(s: String?): T? {
        if (s.isNullOrEmpty()) {
            return null
        }
        if (!cache.containsKey(s)) {
            val created = innerProcess(s)
            cache[s] = created
            return created
        }
        return cache[s] as? T
    }

    protected abstract fun innerProcess(s: String): T?

    abstract class ResValue {
        // TODO: 方便扩展
    }

    companion object {
        val cache = LinkedHashMap<String, ResValue?>(258)
    }
}
