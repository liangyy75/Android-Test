@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.liang.example.xml_inflater

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.AnimatedStateListDrawable
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.LevelListDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.StateListDrawable
import android.graphics.drawable.TransitionDrawable
import android.graphics.drawable.VectorDrawable
import android.graphics.fonts.Font
import android.os.Build
import android.util.Log
import android.view.Menu
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import com.liang.example.basic_ktx.ReflectHelper

// TODO: auto process attr's value, but seems unnecessary
abstract class ResProcessor<T>(open val apm: IAttrProcessorManager, open val type: String) {
    abstract fun process(node: Node): NamedNodeValue<T>?

    open fun <T> makeFail(reason: String, value: T? = null): T? {
        if (throwFlag) {
            throw RuntimeException(reason)
        }
        if (debugFlag) {
            Log.w(tag, reason)
        }
        return value
    }

    open class NamedNodeValue<T>(open val node: Node, _value: T, open var type: Int, open var name: String?) : FormatValue<T>(_value) {
        override fun copy(): NamedNodeValue<T> = super.copy() as NamedNodeValue<T>
        override fun innerCopy(): NamedNodeValue<T> = NamedNodeValue(node, value, type, name)
    }

    open class ListNodeValue(_node: Node, _value: List<NamedNodeValue<*>>, _type: Int, _name: String? = null)
        : NamedNodeValue<List<NamedNodeValue<*>>>(_node, _value, _type, _name)

    open fun dimen(s: String?, context: Context, attr: Attr? = null): Float? =
            (apm["dimen"] as DimenAttrProcessor).attr(attr).from(s)?.let { DimenAttrProcessor.staticApply(context, it as DimenAttrProcessor.DimenAttrValue) }

    open fun fraction(s: String?): Float? = (apm["fraction"] as FractionAttrProcessor).from(s)?.value()
    open fun refer(s: String?): Int? = (apm["refer"] as ReferenceAttrProcessor).from(s)?.value()
    open fun color(s: String?): Long? = (apm["color"] as ColorAttrProcessor).from(s)?.value()
    open fun str(s: String?): String? = (apm["str"] as StringAttrProcessor).from(s)?.value()
    open fun bool(s: String?): Boolean? = (apm["bool"] as BooleanAttrProcessor).from(s)?.value()
    open fun int(s: String?, attr: Attr? = null): Long? = (apm["int"] as IntegerAttrProcessor).attr(attr).from(s)?.value()
    open fun float(s: String?): Float? = (apm["float"] as FloatAttrProcessor).from(s)?.value()
    open fun enum(s: String?, attr: Attr): Long? = (apm["enum"] as EnumAttrProcessor).attr(attr).from(s)?.value()
    open fun flag(s: String?, attr: Attr): Long? = (apm["flag"] as FlagAttrProcessor).attr(attr).from(s)?.value()

    companion object {
        const val NOT_CARED = -1

        const val ITEM = 1  // 注意， string / bool / dimen / ... 这种基本类型会转换为对应的 FormatValue ，其余用 ReferenceAttrValue
        const val STYLE = ITEM + 1
        const val DECLARE_STYLEABLE = STYLE + 1
        const val ATTR = DECLARE_STYLEABLE + 1

        const val DRAWABLE = ATTR + 1
        const val ARRAY = DRAWABLE + 1

        const val DIMEN = ARRAY + 1
        const val COLOR = DIMEN + 1
        const val BOOL = COLOR + 1
        const val FRACTION = BOOL + 1
        const val INTEGER = FRACTION + 1
        const val INTEGER_ARRAY = INTEGER + 1
        const val STRING = INTEGER_ARRAY + 1
        const val STRING_ARRAY = STRING + 1
        const val PLURALS = STRING_ARRAY + 1

        const val COLOR_STATE_LIST = PLURALS + 1

        const val ANIMATOR = COLOR_STATE_LIST + 1
    }
}

interface IResProcessorManager {
    var processors: MutableList<ResProcessor<*>>
    // TODO
}

/**
 * values 下面的
 * <resources>
 *     <item name="string" type=["transition" | "animator" | "anim" | "interpolator" | "style" | "string" | "array" | "attr" | "bool" | "color" | "dimen" | "drawable"
 *         | "font" | "fraction" | "id" | "integer" | "layout" | "menu" | "mipmap" | "navigation" | "plurals" | "raw" | "xml"] format=["integer" | "float" | "reference"
 *         | "fraction" | "color" | "enum" | "string" | "boolean" | "dimension" | "flags"] >text</item>
 *     <style name="string" parent="string">
 *         <item name="string">text</item>
 *     </style>
 *     <declare-styleable name="string">
 *         <attr name="string" format="...">
 *             <enum name="string" value="int" />
 *             <flag name="string" value="int" />
 *         </attr>
 *     </declare-styleable>
 *     <drawable name="string">reference</drawable>
 *     <dimen name="string">100dp</dimen>
 *     <color name="string">#rgb</color>
 *     <array name="string">
 *         <item>reference</item>
 *     </array>
 *     <attr .../>
 *     <bool name="string">[true | false]</bool>
 *     <fraction name="string">100%p</fraction>
 *     <!-- eat-comment -->
 *     <integer name="string">100</integer>
 *     <integer-array name="string">
 *         <item>100</item>
 *     </integer-array>
 *     <string name="string" formatted="bool" translatable="bool">string</string>
 *     <string-array name="string">
 *         <item>string</item>
 *     </string-array>
 *     <plurals name="string">
 *         <item quantity=["zero" | "one" | "two" | "few" | "many" | "other"]>string</item>
 *     </plurals>
 * </resources>
 */
open class ValuesResProcessor(open var context: Context, _attrProcessorManager: IAttrProcessorManager)
    : ResProcessor<List<ResProcessor.NamedNodeValue<*>>>(_attrProcessorManager, "values") {

    override fun process(node: Node): NamedNodeValue<List<NamedNodeValue<*>>>? {
        if ((node.name != "resources" && node.name != "array") || node.children.isEmpty()) {
            return makeFail("resources should have resouces' element or array's element, and children should not be empty: " +
                    "name: ${node.name}, node-children-size ${node.children.size}")
        }
        val list = mutableListOf<NamedNodeValue<*>>()
        node.children.forEach {
            val name = str(it[Attrs.Resources2.name.name]) ?: return makeFail("resources's element should has name attribute")
            val temp: NamedNodeValue<*> = when (it.name) {
                "item" -> item(it, name)
                "style" -> style(it, name)
                "attr" -> attr(it, name)
                "declare-styleable" -> declareStyleable(it, name)
                "drawable" -> drawable(it, name)
                "array" -> array(it, name)
                "dimen", "dimension" -> dimen(it, name)
                "color" -> color(it, name)
                "bool", "boolean" -> bool(it, name)
                "fraction" -> fraction(it, name)
                "integer", "int" -> integer(it, name)
                "integer-array" -> integerArray(it, name)
                "string", "str" -> string(it, name)
                "string-array" -> stringArray(it, name)
                "plurals" -> plurals(it, name)
                else -> return makeFail("unknown resources's element: ${it.name}", null)
            } ?: return null
            list.add(temp)
        }
        return ListNodeValue(node, list, NOT_CARED)
    }

    protected open fun item(it: Node, name: String): NamedNodeValue<*>? {
        return NamedNodeValue(it, TODO(), ITEM, name)
    }

    protected open fun style(it: Node, name: String): NamedNodeValue<*>? {
        return NamedNodeValue(it, TODO(), STYLE, name)
    }

    protected open fun attr(it: Node, name: String): NamedNodeValue<*>? {
        return NamedNodeValue(it, TODO(), ATTR, name)
    }

    protected open fun declareStyleable(it: Node, name: String): NamedNodeValue<*>? {
        return NamedNodeValue(it, TODO(), DECLARE_STYLEABLE, name)
    }

    protected open fun drawable(it: Node, name: String): NamedNodeValue<*>? {
        return NamedNodeValue(it, refer(it.text) ?: return makeFail("drawable's text should be reference"), DRAWABLE, name)
    }

    protected open fun array(it: Node, name: String): NamedNodeValue<*>? {
        if (it.children.isEmpty()) {
            return makeFail("array must have children")
        }
        return NamedNodeValue(it, it.children.map { subItem ->
            TODO("not implement: ${subItem.text}") ?: return makeFail("array's children is incorrect")
        }, ARRAY, name)
    }

    protected open fun dimen(it: Node, name: String): NamedNodeValue<*>? {
        return NamedNodeValue(it, dimen(it.text, context) ?: return makeFail("dimen's text is incorrect"), DIMEN, name)
    }

    protected open fun color(it: Node, name: String): NamedNodeValue<*>? {
        return NamedNodeValue(it, color(it.text) ?: return makeFail("color's text is incorrect"), COLOR, name)
    }

    protected open fun bool(it: Node, name: String): NamedNodeValue<*>? {
        return NamedNodeValue(it, bool(it.text) ?: return makeFail("bool's text is incorrect"), BOOL, name)
    }

    protected open fun fraction(it: Node, name: String): NamedNodeValue<*>? {
        return NamedNodeValue(it, fraction(it.text) ?: return makeFail("fraction's text is incorrect"), FRACTION, name)
    }

    protected open fun integer(it: Node, name: String): NamedNodeValue<*>? {
        return NamedNodeValue(it, int(it.text) ?: return makeFail("integer's text is incorrect"), INTEGER, name)
    }

    protected open fun integerArray(it: Node, name: String): NamedNodeValue<*>? {
        if (it.children.isEmpty()) {
            return makeFail("integer array must have children")
        }
        return NamedNodeValue(it, it.children.map { integer ->
            int(integer.text) ?: return makeFail("integer array's item text is incorrect")
        }.toLongArray(), INTEGER_ARRAY, name)
    }

    protected open fun string(it: Node, name: String): NamedNodeValue<*>? {
        return NamedNodeValue(it, str(it.text) ?: return makeFail("string's text is incorrect"), STRING, name)
    }

    protected open fun stringArray(it: Node, name: String): NamedNodeValue<*>? {
        if (it.children.isEmpty()) {
            return makeFail("string array must have children")
        }
        return NamedNodeValue(it, it.children.map { string ->
            str(string.text) ?: return makeFail("string array's item text is incorrect")
        }.toTypedArray(), STRING_ARRAY, name)
    }

    protected open fun plurals(it: Node, name: String): NamedNodeValue<*>? {
        if (it.children.isEmpty()) {
            return makeFail("plurals must have children")
        }
        return NamedNodeValue(it, it.children.map { item ->
            (enum(item[Attrs.Resources2.quantity.name], Attrs.Resources2.quantity) ?: return makeFail(
                    "the quantity attribute of plurals's item is incorrect: ${item[Attrs.Resources2.quantity.name]}, ${Attrs.Resources2.quantity}")) to
                    // (str(item.text) ?: return makeFail("plurals's item text is incorrect: ${item.text}"))
                    item.text  // TODO: 为什么 str(item.text) 会报错: java.lang.Long cannot be cast to java.lang.String
        }.toMap(), STRING_ARRAY, name)
    }

    companion object {
        const val PLURALS_ZERO = 0L
        const val PLURALS_ONE = 1L
        const val PLURALS_TWO = 2L
        const val PLURALS_FEW = 3L
        const val PLURALS_MANY = 4L
        const val PLURALS_OTHER = 5L

        fun NamedNodeValue<String>.str(vararg args: Any): String = String.format(value(), *args)
        fun NamedNodeValue<Map<Long, String>>.plurals(num: Long, vararg args: Any): String {
            val temp = value()
            return String.format(when {
                num == 0L -> temp[PLURALS_ZERO]
                num == 1L -> temp[PLURALS_ONE]
                num == 2L -> temp[PLURALS_TWO]
                num < 100L -> temp[PLURALS_FEW]
                num >= 100L -> temp[PLURALS_MANY]
                else -> temp[PLURALS_OTHER]
            }!!, *args)
        }
    }
}

/**
 * color @[package:]color/filename
 * <selector>
 *     <item android:color="color" .../>
 * </selector>
 * <gradient
 *     android:centerColor="color"
 *     android:centerX="integer"
 *     android:centerY="integer"
 *     android:endColor="color"
 *     android:endX="integer"
 *     android:endY="integer"
 *     android:gradientRadius="integer"
 *     android:startColor="color"
 *     android:startX="integer"
 *     android:startY="integer"
 *     android:tileMode="mirror"
 *     android:type="linear">
 *     <item android:color="color" android:offset="integer"/>
 * </gradient>
 */
open class SelectorColorResProcessor(manager: IAttrProcessorManager) : ResProcessor<ColorStateList>(manager, "color") {
    override fun process(node: Node): NamedNodeValue<ColorStateList>? {
        if (node.name != "selector" || node.children.isEmpty()) {
            return null
        }
        val name = str(node[Attrs.FreeRes.name.name]) ?: return makeFail("color selector's element should has name attribute")
        val stateSpecs = mutableListOf<IntArray>()
        val colors = mutableListOf<Int>()
        node.children.forEach { item ->
            val color = color(item[Attrs.Color.color.name])?.toInt()
                    ?: return makeFail("color selector's item' color attribute should has color value")
            val itemStates = mutableListOf<Int>()
            item.attributes.forEach { attr ->
                val attrName = attr.name
                if (states.containsKey(attrName)) {
                    val value = bool(attr.value) ?: return makeFail("color selector's item's attribute's value is incorrect: ${attr.value}")
                    itemStates.add(when {
                        value -> states[attrName]!!
                        else -> -states[attrName]!!
                    })
                } else if (attrName != Attrs.Color.color.name && throwFlag) {
                    throw RuntimeException("color selector's item has incorrect attribute: $attrName")
                }
            }
            stateSpecs.add(itemStates.toIntArray())
            colors.add(color)
        }
        return NamedNodeValue(node, ColorStateList(stateSpecs.toTypedArray(), colors.toIntArray()), COLOR_STATE_LIST, name)
    }

    companion object {
        val states = mutableMapOf(
                Attrs.Color.state_accelerated.name to android.R.attr.state_accelerated,
                Attrs.Color.state_activated.name to android.R.attr.state_activated,
                Attrs.Color.state_active.name to android.R.attr.state_active,
                Attrs.Color.state_checkable.name to android.R.attr.state_checkable,
                Attrs.Color.state_checked.name to android.R.attr.state_checked,
                Attrs.Color.state_drag_can_accept.name to android.R.attr.state_drag_can_accept,
                Attrs.Color.state_drag_hovered.name to android.R.attr.state_drag_hovered,
                Attrs.Color.state_enabled.name to android.R.attr.state_enabled,
                Attrs.Color.state_first.name to android.R.attr.state_first,
                Attrs.Color.state_focused.name to android.R.attr.state_focused,
                Attrs.Color.state_hovered.name to android.R.attr.state_hovered,
                Attrs.Color.state_last.name to android.R.attr.state_last,
                Attrs.Color.state_middle.name to android.R.attr.state_middle,
                Attrs.Color.state_pressed.name to android.R.attr.state_pressed,
                Attrs.Color.state_selected.name to android.R.attr.state_selected,
                Attrs.Color.state_single.name to android.R.attr.state_single,
                Attrs.Color.state_window_focused.name to android.R.attr.state_window_focused
        )
    }
}

// open class GradientColorResProcessor(_attrProcessorManager: IAttrProcessorManager) : ResProcessor<android.content.res.GradientColor>(_attrProcessorManager, "color")
// android.content.res.GradientColor is hidden

/**
 * 属性动画 animator: @[package:]animator/filename
 * <set
 *     android:ordering=["together" | "sequentially"]>
 *     <objectAnimator
 *         android:propertyName="string"
 *         android:duration="int"
 *         android:valueFrom="float | int | color"
 *         android:valueTo="float | int | color"
 *         android:startOffset="int"
 *         android:repeatCount="int"
 *         android:repeatMode=["repeat" | "reverse"]
 *         android:valueType=["intType" | "floatType" | "colorType" | "pathData"]
 *         android:interpolator="@android:anim/accelerate_decelerate_interpolator"
 *         android:pathData="???"
 *         android:propertyXName="string"
 *         android:propertyYName="string" >
 *         <propertyValuesHolder
 *             android:propertyName="string"
 *             android:valueFrom="float | int | color"
 *             android:valueTo="float | int | color"
 *             android:valueType=["intType" | "floatType" | "colorType" | "pathData"] >
 *             <keyframe
 *                 android:value="float | int | color"
 *                 android:fraction="float"
 *                 android:interpolator="@android:interpolator/accelerate_decelerate"
 *                 android:valueType=["intType" | "floatType" | "colorType" | "pathData"] />
 *         </propertyValuesHolder/>
 *     <objectAnimator/>
 *     <animator
 *         android:duration="int"
 *         android:valueFrom="float | int | color"
 *         android:valueTo="float | int | color"
 *         android:startOffset="int"
 *         android:repeatCount="int"
 *         android:repeatMode=["repeat" | "reverse"]
 *         android:valueType=["intType" | "floatType"]
 *         android:interpolator="@android:anim/accelerate_decelerate_interpolator" >
 *         <propertyValuesHolder ...>
 *             <keyframe .../>
 *         </propertyValuesHolder/>
 *     </animator>
 *     <set>
 *         ...
 *     </set>
 *     <propertyValueHolder
 *         android:propertyName="string"
 *         android:valueFrom="float | int | color"
 *         android:valueTo="float | int | color"
 *         android:valueType=["colorType" | "floatType" | "intType" | "pathType"] />
 * </set>
 *
 * <selector>
 *     <item
 *         android:animation="@anim/xxx"
 *         android:state_accelerated="bool"
 *         android:state_activated="bool"
 *         android:state_active="bool"
 *         android:state_checkable="bool"
 *         android:state_checked="false"
 *         android:state_drag_can_accept="bool"
 *         android:state_drag_hovered="bool"
 *         android:state_enabled="bool"
 *         android:state_first="bool"
 *         android:state_focused="bool"
 *         android:state_hovered="bool"
 *         android:state_last="bool"
 *         android:state_middle="bool"
 *         android:state_pressed="bool"
 *         android:state_selected="bool"
 *         android:state_single="bool"
 *         android:state_window_focused="bool" />
 * </selector>
 *
 * https://developer.android.com/guide/topics/resources/animation-resource.html?hl=zh-cn
 *
 * set
 * animator
 * objectAnimator
 * selector
 * propertyValuesHolder
 * keyframe
 * item
 */
open class AnimatorResProcessor(_context: Context, manager: IAttrProcessorManager) : ResProcessor<ValueAnimator>(manager, "animator") {
    open var context: Context = _context
        set(value) {
            field = value
            propertyValuesResProcessor.context = value
        }
    open var propertyValuesResProcessor = PropertyValueResProcessor(_context, manager)

    override fun process(node: Node): NamedNodeValue<ValueAnimator>? {
        if (node.name != "animator") {
            return null
        }
        val name = str(node[Attrs.FreeRes.name.name]) ?: return makeFail("animator's element should has name attribute")
        val result = ValueAnimator()
        if (node.children.isNotEmpty()) {
            val pVHs = node.children.mapNotNull { propertyValuesResProcessor.process(it) }
            if (pVHs.isNotEmpty()) {
                result.setValues(*pVHs.map { it.value() }.toTypedArray())
            }
        }
        Attrs.Animator.duration.name
        return NamedNodeValue(node, result, ANIMATOR, name)
    }

    // TODO: support pathData
    open class PropertyValueResProcessor(open var context: Context, manager: IAttrProcessorManager) : ResProcessor<PropertyValuesHolder>(manager, "animator") {
        override fun process(node: Node): NamedNodeValue<PropertyValuesHolder>? {
            if (node.name != "propertyValuesHolder") {
                return null
            }
            val propertyName = str(node[Attrs.PropertyValuesHolder.propertyName.name])
                    ?: return makeFail("animator's element should has propertyName attribute")
            var valueType: Int = VALUE_TYPE_UNDEFINED
            val propertyValuesHolder: PropertyValuesHolder? = if (node.children.isNotEmpty()) {
                val pair = keyFramesParse(node, valueType, propertyName) ?: return null
                valueType = pair.second
                pair.first
            } else {
                val valueFrom = node[Attrs.PropertyValuesHolder.valueFrom.name]
                val valueTo = node[Attrs.PropertyValuesHolder.valueTo.name]
                val hasFrom = valueFrom != null
                val hasTo = valueTo != null
                valueType = when {
                    hasFrom && ColorAttrProcessor.isColor(valueFrom) || hasTo && ColorAttrProcessor.isColor(valueTo) -> VALUE_TYPE_COLOR
                    else -> VALUE_TYPE_FLOAT
                }
                when (valueType) {
                    VALUE_TYPE_PATH -> pathTypeParse(hasFrom, valueFrom, hasTo, valueTo, propertyName)
                    VALUE_TYPE_FLOAT -> when {
                        hasFrom && hasTo -> PropertyValuesHolder.ofFloat(propertyName,
                                dimen(valueFrom, context) ?: float(valueFrom)
                                ?: return makeFail("animator element should has right valueFrom attribute"),
                                dimen(valueTo, context) ?: float(valueTo) ?: return makeFail("animator element should has right valueTo attribute"))
                        hasFrom -> PropertyValuesHolder.ofFloat(propertyName,
                                dimen(valueFrom, context) ?: float(valueFrom)
                                ?: return makeFail("animator element should has right valueFrom attribute"))
                        hasTo -> PropertyValuesHolder.ofFloat(propertyName,
                                dimen(valueTo, context) ?: float(valueTo) ?: return makeFail("animator element should has right valueTo attribute"))
                        else -> null
                    }
                    else -> when {
                        hasFrom && hasTo -> PropertyValuesHolder.ofInt(propertyName,
                                dimen(valueFrom, context)?.toInt() ?: color(valueFrom)?.toInt() ?: int(valueFrom)?.toInt()
                                ?: return makeFail("animator element should has right valueFrom attribute"),
                                dimen(valueTo, context)?.toInt() ?: color(valueTo)?.toInt() ?: int(valueTo)?.toInt()
                                ?: return makeFail("animator element should has right valueTo attribute"))
                        hasFrom -> PropertyValuesHolder.ofInt(propertyName,
                                dimen(valueFrom, context)?.toInt() ?: color(valueFrom)?.toInt() ?: int(valueFrom)?.toInt()
                                ?: return makeFail("animator element should has right valueFrom attribute"))
                        hasTo -> PropertyValuesHolder.ofInt(propertyName,
                                dimen(valueTo, context)?.toInt() ?: color(valueTo)?.toInt() ?: int(valueTo)?.toInt()
                                ?: return makeFail("animator element should has right valueTo attribute"))
                        else -> null
                    }
                }
            }
            return when {
                propertyValuesHolder != null -> {
                    if (valueType == VALUE_TYPE_COLOR) {
                        propertyValuesHolder.setEvaluator(argbEvaluator)
                    }
                    NamedNodeValue(node, propertyValuesHolder, NOT_CARED, "")
                }
                else -> null
            }
        }

        private fun keyFramesParse(node: Node, valueType: Int, propertyName: String): Pair<PropertyValuesHolder, Int>? {
            val keyframes = node.children.mapNotNull { keyFrameParse(valueType, it) }.toMutableList()
            val size = keyframes.size
            if (size != 0) {
                distributeKeyFrames(keyframes, size)
            }
            return Pair(PropertyValuesHolder.ofKeyframe(propertyName, *keyframes.toTypedArray()), valueType)
        }

        private fun keyFrameParse(valueType: Int, keyFrameNode: Node): Keyframe? {
            var valueType2 = valueType
            return if (keyFrameNode.name == "keyframe") {
                val keyFrameValue = keyFrameNode[Attrs.Keyframe.value.name]
                val keyFrameFraction = keyFrameNode[Attrs.Keyframe.fraction.name]
                if (valueType2 == VALUE_TYPE_UNDEFINED) {
                    valueType2 = keyFrameTypes[keyFrameNode[Attrs.Keyframe.valueType.name]] ?: when {
                        ColorAttrProcessor.isColor(keyFrameValue) -> VALUE_TYPE_COLOR
                        else -> VALUE_TYPE_FLOAT
                    }
                }
                if (valueType2 == VALUE_TYPE_PATH) {
                    return makeFail("keyframe node can't have pathType")
                }
                val keyframe = when {
                    keyFrameValue == null && valueType2 == VALUE_TYPE_FLOAT -> Keyframe.ofFloat(float(keyFrameFraction)
                            ?: return makeFail("keyframe node should have right fraction attribute or value attribute"))
                    keyFrameValue == null -> Keyframe.ofFloat(float(keyFrameFraction)
                            ?: return makeFail("keyframe node should have right fraction attribute or value attribute"))
                    valueType2 == VALUE_TYPE_FLOAT -> Keyframe.ofFloat(
                            float(keyFrameFraction) ?: return makeFail("keyframe node should have right fraction attribute"),
                            float(keyFrameValue) ?: return makeFail("keyframe node should have right value attribute"))
                    else -> Keyframe.ofInt(
                            float(keyFrameFraction) ?: return makeFail("keyframe node should have right fraction attribute"),
                            int(keyFrameValue)?.toInt() ?: return makeFail("keyframe node should have right value attribute"))
                }
                val interpolatorId = interpolators[keyFrameNode[Attrs.Keyframe.interpolator.name]] // TODO: custom interpolator
                if (interpolatorId != null) {
                    keyframe.interpolator = AnimationUtils.loadInterpolator(context, interpolatorId)
                }
                keyframe
            } else null
        }

        protected fun distributeKeyFrames(keyframes: MutableList<Keyframe>, size: Int) {
            var size1 = size
            val first = keyframes[0]
            val last = keyframes[size1 - 1]
            var fraction = last.fraction
            if (fraction < 1f) {
                if (fraction < 0f) {
                    last.fraction = 1f
                } else {
                    keyframes.add(createNewKeyframe(last, 1f))
                    ++size1
                }
            }
            fraction = first.fraction
            if (fraction != 0f) {
                if (fraction < 0f) {
                    first.fraction = 0f
                } else {
                    keyframes.add(createNewKeyframe(first, 0f))
                    ++size1
                }
            }
            keyframes.forEachIndexed { index, keyframe ->
                if (keyframe.fraction < 0f) {
                    when (index) {
                        0 -> keyframe.fraction = 0f
                        size1 - 1 -> keyframe.fraction = 1f
                        else -> {
                            var endIndex = index
                            for (index2 in (index + 1) until (size1 - 1)) {
                                if (keyframes[index2].fraction >= 0f) {
                                    break
                                }
                                endIndex = index2
                            }
                            val increment = (keyframes[endIndex + 1].fraction - keyframes[index - 1].fraction) / (endIndex - index + 2)
                            (index..endIndex).forEach { index2 -> keyframes[index2].fraction = keyframes[index2 + 1].fraction + increment }
                        }
                    }
                }
            }
        }

        protected fun pathTypeParse(hasFrom: Boolean, valueFrom: String?, hasTo: Boolean, valueTo: String?, propertyName: String): PropertyValuesHolder? {
            val nodeFrom = when {
                hasFrom -> ReflectHelper.newInstance<Any>("android.util.PathParser.PathData", valueFrom)
                else -> null
            }
            val nodeTo = when {
                hasTo -> ReflectHelper.newInstance<Any>("android.util.PathParser.PathData", valueTo)
                else -> null
            }
            return when {
                hasFrom && hasTo -> {
                    if (ReflectHelper.findMethod("canMorph", ReflectHelper.findCls("android.util.PathParser")!!,
                                    nodeFrom!!::class.java, nodeTo!!::class.java)?.invoke(nodeFrom, nodeTo) as? Boolean == false) {
                        return makeFail("Can't morph from $valueFrom to $valueTo")
                    }
                    PropertyValuesHolder.ofObject(propertyName, ReflectHelper.newInstance<TypeEvaluator<*>>(
                            "android.animation.AnimationInflater.TypeEvaluator"), nodeFrom, nodeTo)
                }
                hasFrom -> PropertyValuesHolder.ofObject(propertyName, ReflectHelper.newInstance<TypeEvaluator<*>>(
                        "android.animation.AnimationInflater.TypeEvaluator"), nodeFrom)
                hasTo -> PropertyValuesHolder.ofObject(propertyName, ReflectHelper.newInstance<TypeEvaluator<*>>(
                        "android.animation.AnimationInflater.TypeEvaluator"), nodeTo)
                else -> null
            }
        }

        open fun createNewKeyframe(sampleKeyframe: Keyframe, fraction: Float): Keyframe = when (sampleKeyframe.type) {
            Float::class.javaPrimitiveType -> Keyframe.ofFloat(fraction)
            Int::class.javaPrimitiveType -> Keyframe.ofInt(fraction)
            else -> Keyframe.ofObject(fraction)
        }
    }

    companion object {
        const val VALUE_TYPE_FLOAT = 0
        const val VALUE_TYPE_INT = 1
        const val VALUE_TYPE_PATH = 2
        const val VALUE_TYPE_COLOR = 3
        const val VALUE_TYPE_UNDEFINED = 4

        val argbEvaluator = ArgbEvaluator()

        val keyFrameTypes = mutableMapOf(
                "intType" to VALUE_TYPE_INT,
                "floatType" to VALUE_TYPE_FLOAT,
                "pathData" to VALUE_TYPE_PATH,
                "colorType" to VALUE_TYPE_COLOR
        )

        val interpolators = mutableMapOf(
                "@android:interpolator/accelerate_cubic" to android.R.interpolator.accelerate_cubic,
                "@android:interpolator/accelerate_decelerate" to android.R.interpolator.accelerate_decelerate,
                "@android:interpolator/accelerate_quad" to android.R.interpolator.accelerate_quad,
                "@android:interpolator/accelerate_quint" to android.R.interpolator.accelerate_quint,
                "@android:interpolator/anticipate" to android.R.interpolator.anticipate,
                "@android:interpolator/anticipate_overshoot" to android.R.interpolator.anticipate_overshoot,
                "@android:interpolator/bounce" to android.R.interpolator.bounce,
                "@android:interpolator/cycle" to android.R.interpolator.cycle,
                "@android:interpolator/decelerate_cubic" to android.R.interpolator.decelerate_cubic,
                "@android:interpolator/decelerate_quad" to android.R.interpolator.decelerate_quad,
                "@android:interpolator/decelerate_quint" to android.R.interpolator.decelerate_quint,
                "@android:interpolator/linear" to android.R.interpolator.linear,
                "@android:interpolator/overshoot" to android.R.interpolator.overshoot,
                "@android:anim/accelerate_decelerate_interpolator" to android.R.anim.accelerate_decelerate_interpolator,
                "@android:anim/accelerate_interpolator" to android.R.anim.accelerate_interpolator,
                "@android:anim/anticipate_interpolator" to android.R.anim.anticipate_interpolator,
                "@android:anim/anticipate_overshoot_interpolator" to android.R.anim.anticipate_overshoot_interpolator,
                "@android:anim/bounce_interpolator" to android.R.anim.bounce_interpolator,
                "@android:anim/cycle_interpolator" to android.R.anim.cycle_interpolator,
                "@android:anim/decelerate_interpolator" to android.R.anim.decelerate_interpolator,
                "@android:anim/fade_in" to android.R.anim.fade_in,
                "@android:anim/fade_out" to android.R.anim.fade_out,
                "@android:anim/linear_interpolator" to android.R.anim.linear_interpolator,
                "@android:anim/overshoot_interpolator" to android.R.anim.overshoot_interpolator,
                "@android:anim/slide_in_left" to android.R.anim.slide_in_left,
                "@android:anim/slide_out_right" to android.R.anim.slide_out_right
        )

        init {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                interpolators["@android:interpolator/fast_out_extra_slow_in"] = android.R.interpolator.fast_out_extra_slow_in
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                interpolators["@android:interpolator/fast_out_linear_in"] = android.R.interpolator.fast_out_linear_in
                interpolators["@android:interpolator/fast_out_slow_in"] = android.R.interpolator.fast_out_slow_in
                interpolators["@android:interpolator/linear_out_slow_in"] = android.R.interpolator.linear_out_slow_in
            }
        }
    }
}

open class ObjAnimatorResProcessor(manager: IAttrProcessorManager) : ResProcessor<ObjectAnimator>(manager, "animator") {
    override fun process(node: Node): NamedNodeValue<ObjectAnimator>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class AnimatorSetResProcessor(manager: IAttrProcessorManager) : ResProcessor<AnimatorSet>(manager, "animator") {
    override fun process(node: Node): NamedNodeValue<AnimatorSet>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class AnimatorSelectorResProcessor(manager: IAttrProcessorManager) : ResProcessor<Map<Int, Animator>>(manager, "animator") {
    override fun process(node: Node): NamedNodeValue<Map<Int, Animator>>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

/**
 * 视图动画 @[package:]anim/filename
 * <set xmlns:android="http://schemas.android.com/apk/res/android"
 *     android:interpolator="@[package:]anim/interpolator_resource"
 *     android:duration="int"
 *     android:fillAfter="bool"
 *     android:fillBefore="bool"
 *     android:startOffset="int"
 *     android:repeatMode=["repeat" | "reverse"]
 *     android:shareInterpolator=["true" | "false"] >
 *     <alpha
 *         android:duration="int"
 *         android:fromAlpha="float"
 *         android:toAlpha="float" />
 *     <scale
 *         android:duration="int"
 *         android:fromXScale="float"
 *         android:toXScale="float"
 *         android:fromYScale="float"
 *         android:toYScale="float"
 *         android:pivotX="float"
 *         android:pivotY="float" />
 *     <translate
 *         android:duration="int"
 *         android:fromXDelta="float"
 *         android:toXDelta="float"
 *         android:fromYDelta="float"
 *         android:toYDelta="float" />
 *     <rotate
 *         android:duration="int"
 *         android:fromDegrees="float"
 *         android:toDegrees="float"
 *         android:pivotX="float"
 *         android:pivotY="float" />
 *     <set>
 *         ...
 *     </set>
 * </set>
 *
 * <layoutAnimation
 *     android:animation="@android:anim/fade_in"
 *     android:animationOrder=["normal" | "random" | "reverse"]
 *     android:delay="int"
 *     android:interpolator="@android:anim/accelerate_decelerate_interpolator"/>
 *
 * <gridLayoutAnimation
 *     android:columnDelay="int"
 *     android:rowDelay="int"
 *     android:direction=["bottom_to_top" | "top_to_bottom" | "right_to_left" | "left_to_right"]
 *     android:directionPriority=["column" | "none" | "row"] />
 *
 * https://developer.android.com/guide/topics/resources/animation-resource.html?hl=zh-cn
 *
 * system 专有 -- accelerateDecelerateInterpolator
 * system 专有 -- accelerateInterpolator
 * system 专有 -- anticipateInterpolator
 * system 专有 -- anticipateOvershootInterpolator
 * system 专有 -- bounceInterpolator
 * system 专有 -- cycleInterpolator
 * system 专有 -- decelerateInterpolator
 * system 专有 -- linearInterpolator
 * system 专有 -- overshootInterpolator
 * system 专有 -- pathInterpolator
 *
 * gridLayoutAnimation
 * layoutAnimation
 *
 * set
 * rotate
 * scale
 * alpha
 * translate
 */
open class AnimationSetResProcessor(manager: IAttrProcessorManager) : ResProcessor<AnimationSet>(manager, "anim") {
    override fun process(node: Node): NamedNodeValue<AnimationSet>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class RotateResProcessor(manager: IAttrProcessorManager) : ResProcessor<RotateAnimation>(manager, "anim") {
    override fun process(node: Node): NamedNodeValue<RotateAnimation>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class ScaleResProcessor(manager: IAttrProcessorManager) : ResProcessor<ScaleAnimation>(manager, "anim") {
    override fun process(node: Node): NamedNodeValue<ScaleAnimation>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class AlphaResProcessor(manager: IAttrProcessorManager) : ResProcessor<AlphaAnimation>(manager, "anim") {
    override fun process(node: Node): NamedNodeValue<AlphaAnimation>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class TranslateResProcessor(manager: IAttrProcessorManager) : ResProcessor<TranslateAnimation>(manager, "anim") {
    override fun process(node: Node): NamedNodeValue<TranslateAnimation>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

/**
 * 帧动画 @[package:]drawable.filename
 * <animation-list xmlns:android="http://schemas.android.com/apk/res/android"
 *     android:oneshot=["true" | "false"]
 *     android:variablePadding=["true" | "false"]
 *     android:visible=["true" | "false"] >
 *     <item
 *         android:drawable="@[package:]drawable/drawable_resource_name"
 *         android:duration="integer" />
 * </animation-list>
 * https://developer.android.com/guide/topics/resources/animation-resource.html?hl=zh-cn
 */
open class AnimationListResProcessor(manager: IAttrProcessorManager) : ResProcessor<AnimationDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<AnimationDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

/**
 * drawable @[package:]drawable/filename
 *
 * <selector
 *     android:autoMirrored="bool"
 *     android:constantSize="bool"
 *     android:dither="bool"
 *     android:enterFadeDuration="integer"
 *     android:exitFadeDuration="integer"
 *     android:variablePadding="bool"
 *     android:visible="bool">
 *     <item
 *         android:animation="@anim/xxx"
 *         android:state_accelerated="bool"
 *         android:state_activated="bool"
 *         android:state_active="bool"
 *         android:state_checkable="bool"
 *         android:state_checked="false"
 *         android:state_drag_can_accept="bool"
 *         android:state_drag_hovered="bool"
 *         android:state_enabled="bool"
 *         android:state_first="bool"
 *         android:state_focused="bool"
 *         android:state_hovered="bool"
 *         android:state_last="bool"
 *         android:state_middle="bool"
 *         android:state_pressed="bool"
 *         android:state_selected="bool"
 *         android:state_single="bool"
 *         android:state_window_focused="bool" >
 *         <any top drawable element>
 *     </item>
 * </selector>
 *
 * <vector
 *     android:name="string"
 *     android:width="dimen"
 *     android:height="dimen"
 *     android:alpha="integer"
 *     android:autoMirrored="bool"
 *     android:opticalInsetLeft="dimen"
 *     android:opticalInsetTop="dimen"
 *     android:opticalInsetRight="dimen"
 *     android:opticalInsetBottom="dimen"
 *     android:tint="color"
 *     android:tintMode=["add" | "multiply" | "screen" | "src_atop" | "src_in" | "src_over"]
 *     android:viewportWidth="integer"
 *     android:viewportHeight="integer">
 *     <clip-path
 *         android:name="string"
 *         android:pathData="string" />
 *     <group
 *         android:name="string"
 *         android:pivotX="integer"
 *         android:pivotY="integer"
 *         android:rotation="integer"
 *         android:scaleX="integer"
 *         android:scaleY="integer"
 *         android:translateX="integer"
 *         android:translateY="integer">
 *         <path
 *             android:name="string"
 *             android:fillAlpha="integer"
 *             android:fillColor="color"
 *             android:fillType=["evenOdd" | "nonZero"]
 *             android:pathData="string"
 *             android:strokeWidth="integer"
 *             android:strokeAlpha="integer"
 *             android:strokeColor="color"
 *             android:strokeLineCap=["butt" | "round" | "square"]
 *             android:strokeLineJoin=["bevel" | "round" | "miter"]
 *             android:strokeMiterLimit="integer"
 *             android:trimPathStart="integer"
 *             android:trimPathEnd="integer"
 *             android:trimPathOffset="integer" />
 *     </group>
 * </vector>
 *
 * <adaptive-icon
 *     android:drawable="xxx">
 *     <background
 *         android:id="reference"
 *         android:width="dimen"
 *         android:height="dimen"
 *         android:bottom="dimen"
 *         android:drawable="color"
 *         android:end="dimen"
 *         android:gravity="center_vertical"
 *         android:left="dimen"
 *         android:right="dimen"
 *         android:start="dimen"
 *         android:top="dimen">
 *         <any top drawable element>
 *     </background>
 *     <foreground
 *         android:id="reference"
 *         android:width="dimen"
 *         android:height="dimen"
 *         android:bottom="dimen"
 *         android:drawable="color"
 *         android:end="dimen"
 *         android:gravity="center_vertical"
 *         android:left="dimen"
 *         android:right="dimen"
 *         android:start="dimen"
 *         android:top="dimen">
 *         <any top drawable element>
 *     </foreground>
 * </adaptive-icon>
 *
 * <animated-rotate
 *     android:drawable="color"
 *     android:pivotX="integer"
 *     android:pivotY="integer"
 *     android:visible="bool"
 *     <any top drawable element>
 * </animated-rotate>
 *
 * <animated-selector
 *     android:autoMirrored="bool"
 *     android:constantSize="bool"
 *     android:dither="bool"
 *     android:enterFadeDuration="integer"
 *     android:exitFadeDuration="integer"
 *     android:variablePadding="bool"
 *     android:visible="bool">
 *     <item .../>
 *     <transition
 *         android:drawable="color"
 *         android:fromId="id"
 *         android:reversible="bool"
 *         android:toId="id">
 *         <animation-list .../>
 *     </transition>
 * </animated-selector>
 *
 * <animated-vector
 *     android:drawable="reference">
 *     <target
 *         android:name="string"
 *         android:animation="reference"/>
 * </animated-vector>
 *
 * <bitmap
 *     android:alpha="integer"
 *     android:antialias="bool"
 *     android:autoMirrored="bool"
 *     android:dither="bool"
 *     android:filter="bool"
 *     android:gravity="start"
 *     android:mipMap="bool"
 *     android:src="color"
 *     android:tileMode=["clamp" | "disable" | "mirror" | "repeat"]
 *     android:tileModeX=["clamp" | "disable" | "mirror" | "repeat"]
 *     android:tileModeY=["clamp" | "disable" | "mirror" | "repeat"]
 *     android:tint="color"
 *     android:tintMode=["add" | "multiply" | "screen" | "src_atop" | "src_in" | "src_over"] />
 *
 * <clip
 *     android:clipOrientation="vertical"
 *     android:drawable="color"
 *     android:gravity="start" >
 *     <any top drawable element>
 * </clip>
 *
 * <color android:color="color"/>
 *
 * <drawable class="your.drawable.full.class.name" ...(其余属性取决于自定义drawable) />
 *
 * <insert
 *     android:drawable="color"
 *     android:inset="dimen"
 *     android:insetLeft="dimen"
 *     android:insetTop="dimen"
 *     android:insetRight="dimen"
 *     android:insetBottom="dimen"
 *     android:visible="bool">
 *     <any top drawable element>
 * </insert>
 *
 * <layer-list
 *     android:autoMirrored="bool"
 *     android:opacity=["opaque" | "translucent" | "transparent"]
 *     android:paddingStart="dimen"
 *     android:paddingLeft="dimen"
 *     android:paddingTop="dimen"
 *     android:paddingEnd="dimen"
 *     android:paddingRight="dimen"
 *     android:paddingBottom="dimen"
 *     android:paddingMode=["nest" | "stack"] >
 *     <item .../>
 * </layer-list>
 *
 * <level-list>
 *     <item .../>
 * </level-list>
 *
 * <maskable-icon
 *     android:drawable="reference">
 *     <background .../>
 *     <foreground .../>
 * </maskable-icon>
 *
 * <nine-patch
 *     android:alpha="integer"
 *     android:antialias="bool"
 *     android:autoMirrored="bool"
 *     android:dither="bool"
 *     android:filter="bool"
 *     android:gravity="start"
 *     android:mipMap="bool"
 *     android:src="color"
 *     android:tileMode="mirror"
 *     android:tileModeX="mirror"
 *     android:tileModeY="mirror"
 *     android:tint="color"
 *     android:tintMode="src_over"/>
 *
 * <ripple
 *     android:color="color"
 *     android:radius="dimen">
 *     <item ... />
 * </ripple>
 *
 * <shape
 *     android:dither="bool"
 *     android:innerRadius="dimen"
 *     android:innerRadiusRatio="integer"
 *     android:opticalInsetLeft="dimen"
 *     android:opticalInsetTop="dimen"
 *     android:opticalInsetRight="dimen"
 *     android:opticalInsetBottom="dimen"
 *     android:shape=["rectangle" | "line" | "oval" | "ring"]
 *     android:thickness="dimen"
 *     android:thicknessRatio="integer"
 *     android:tint="color"
 *     android:tintMode="src_over"
 *     android:useLevel="bool"
 *     android:visible="bool">
 *     <size
 *         android:width="dimen"
 *         android:height="dimen"/>
 *     <stroke
 *         android:width="dimen"
 *         android:color="color"
 *         android:dashWidth="dimen"
 *         android:dashGap="dimen"/>
 *     <solid
 *         android:color="color"/>
 *     <corners
 *         android:bottomLeftRadius="dimen"
 *         android:topLeftRadius="dimen"
 *         android:bottomRightRadius="dimen"
 *         android:topRightRadius="dimen"
 *         android:radius="dimen"/>
 *     <gradient
 *         android:angle="integer"
 *         android:centerColor="color"
 *         android:centerX="integer"
 *         android:centerY="integer"
 *         android:endColor="color"
 *         android:gradientRadius="integer"
 *         android:startColor="color"
 *         android:type=["linear" | "radial" | "sweep"]
 *         android:useLevel="true" />
 *     <padding
 *         android:bottom="dimen"
 *         android:left="dimen"
 *         android:right="dimen"
 *         android:top="dimen"/>
 * </shape>
 *
 * adaptive-icon
 * foreground
 * background
 *
 * animated-rotate
 *
 * animated-selector
 * transition
 *
 * animated-vector
 * target
 *
 * bitmap
 *
 * clip
 *
 * color
 *
 * drawable
 *
 * inset
 *
 * layer-list
 *
 * level-list
 *
 * maskable-icon
 *
 * nine-patch
 *
 * ripple
 *
 * shape
 * size
 * stroke
 * solid
 * corners
 * gradient
 * padding
 *
 * selector
 * item
 *
 * vector
 * clip-path
 * group
 * path
 */
open class ShapeResProcessor(manager: IAttrProcessorManager) : ResProcessor<ShapeDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<ShapeDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class ClipResProcessor(manager: IAttrProcessorManager) : ResProcessor<ClipDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<ClipDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class InsetResProcessor(manager: IAttrProcessorManager) : ResProcessor<InsetDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<InsetDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class LayerListResProcessor(manager: IAttrProcessorManager) : ResProcessor<LayerDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<LayerDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class LevelListResProcessor(manager: IAttrProcessorManager) : ResProcessor<LevelListDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<LevelListDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class VectorResProcessor(manager: IAttrProcessorManager) : ResProcessor<VectorDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<VectorDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class ColorDrawableResProcessor(manager: IAttrProcessorManager) : ResProcessor<ColorDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<ColorDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class SelectorDrawableResProcessor(manager: IAttrProcessorManager) : ResProcessor<StateListDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<StateListDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class GradientDrawableResProcessor(manager: IAttrProcessorManager) : ResProcessor<GradientDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<GradientDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class AdaptiveIconResProcessor(manager: IAttrProcessorManager) : ResProcessor<AdaptiveIconDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<AdaptiveIconDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class AnimatedRotateResProcessor(manager: IAttrProcessorManager) : ResProcessor<Attrs.AnimatedRotateDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<Attrs.AnimatedRotateDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class AnimatedSelectorResProcessor(manager: IAttrProcessorManager) : ResProcessor<AnimatedStateListDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<AnimatedStateListDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class AnimatedVectorResProcessor(manager: IAttrProcessorManager) : ResProcessor<AnimatedVectorDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<AnimatedVectorDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class BitmapResProcessor(manager: IAttrProcessorManager) : ResProcessor<BitmapDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<BitmapDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class DrawableResProcessor(manager: IAttrProcessorManager) : ResProcessor<Drawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<Drawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class RippleResProcessor(manager: IAttrProcessorManager) : ResProcessor<RippleDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<RippleDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class MaskableIconResProcessor(manager: IAttrProcessorManager) : ResProcessor<AnimatedStateListDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<AnimatedStateListDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class TransitionResProcessor(manager: IAttrProcessorManager) : ResProcessor<TransitionDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<TransitionDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// 等等

/**
 * menu @[package:]menu.filename
 * <?xml version="1.0" encoding="utf-8"?>
 * <menu xmlns:android="http://schemas.android.com/apk/res/android">
 *     <item android:id="@[+][package:]id/resource_name"
 *           android:title="string"
 *           android:titleCondensed="string"
 *           android:icon="@[package:]drawable/drawable_resource_name"
 *           android:onClick="method name"
 *           android:showAsAction=["ifRoom" | "never" | "withText" | "always" | "collapseActionView"]
 *           android:actionLayout="@[package:]layout/layout_resource_name"
 *           android:actionViewClass="class name"
 *           android:actionProviderClass="class name"
 *           android:alphabeticShortcut="string"
 *           android:alphabeticModifiers=["META" | "CTRL" | "ALT" | "SHIFT" | "SYM" | "FUNCTION"]
 *           android:numericShortcut="string"
 *           android:numericModifiers=["META" | "CTRL" | "ALT" | "SHIFT" | "SYM" | "FUNCTION"]
 *           android:checkable=["true" | "false"]
 *           android:visible=["true" | "false"]
 *           android:enabled=["true" | "false"]
 *           android:menuCategory=["container" | "system" | "secondary" | "alternative"]
 *           android:orderInCategory="integer" />
 *     <group android:id="@[+][package:]id/resource name"
 *            android:checkableBehavior=["none" | "all" | "single"]
 *            android:visible=["true" | "false"]
 *            android:enabled=["true" | "false"]
 *            android:menuCategory=["container" | "system" | "secondary" | "alternative"]
 *            android:orderInCategory="integer" >
 *         <item />
 *     </group>
 *     <item >
 *         <menu>
 *           <item />
 *         </menu>
 *     </item>
 * </menu>
 */
open class MenuResProcessor(manager: IAttrProcessorManager) : ResProcessor<Menu>(manager, "menu") {
    override fun process(node: Node): NamedNodeValue<Menu>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

/**
 * font @[package:]font/font_name
 * <font-family>
 *     <font
 *         android:font="@[package:]font/font_to_include"
 *         android:fontStyle=["normal" | "italic"]
 *         android:fontWeight="weight_value" />
 * </font-family>
 */
open class FontResProcessor(manager: IAttrProcessorManager) : ResProcessor<Font>(manager, "font") {
    override fun process(node: Node): NamedNodeValue<Font>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

/**
 * custom
 */
