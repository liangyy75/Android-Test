@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.liang.example.xml_inflater.values

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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
import android.util.Log
import android.view.Menu
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.RotateAnimation
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import androidx.annotation.CallSuper
import com.liang.example.xml_inflater.Attr
import com.liang.example.xml_inflater.Attrs
import com.liang.example.xml_inflater.FormatValue
import com.liang.example.xml_inflater.debugFlag
import com.liang.example.xml_inflater.tag
import com.liang.example.xml_inflater.throwFlag

open class Node(open var name: String) {
    open var parent: Node? = null
    open var text = EMPTY_TEXT
    open var children: MutableList<Node> = EMPTY_CHILDREN
    open var attributes: MutableList<NodeAttr> = EMPTY_ATTRIBUTES

    operator fun get(attrName: String) = attributes.find { it.name == attrName }?.value

    open class NodeAttr(open var name: String, open var value: String)

    companion object {
        val EMPTY_CHILDREN = mutableListOf<Node>()
        val EMPTY_ATTRIBUTES = mutableListOf<NodeAttr>()
        const val EMPTY_TEXT = ""
    }
}

abstract class ResProcessor<T>(open val apm: IAttrProcessorManager, open val type: String) {
    open val aps = mutableMapOf<Attr, AttrProcessor<*>>()

    open fun register(attr: Attr): Boolean {
        aps[attr] = when {
            attr.format == null || attr.format == "flag" -> apm["flag"] ?: return false
            attr.format.contains('|') -> apm.getComplex(*attr.format.split('|').toTypedArray()) ?: return false
            else -> apm[attr.format] ?: return false
        }
        return true
    }

    open fun registerGlobal(attr: Attr): Boolean {
        gaps[attr] = when {
            attr.format == null || attr.format == "flag" -> apm["flag"] ?: return false
            attr.format.contains('|') -> apm.getComplex(*attr.format.split('|').toTypedArray()) ?: return false
            else -> apm[attr.format] ?: return false
        }
        return true
    }

    abstract fun prepare()
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

    open fun fraction(s: String?): Float? = (apm["fraction"] as FractionAttrProcessor).from(s)?.value()
    open fun refer(s: String?): Int? = (apm["refer"] as ReferenceAttrProcessor).from(s)?.value()
    open fun color(s: String?): Long? = (apm["color"] as ColorAttrProcessor).from(s)?.value()
    open fun str(s: String?): String? = (apm["str"] as StringAttrProcessor).from(s)?.value()
    open fun bool(s: String?): Boolean? = (apm["bool"] as BooleanAttrProcessor).from(s)?.value()
    open fun int(s: String?): Long? = (apm["int"] as IntegerAttrProcessor).from(s)?.value()
    open fun float(s: String?): Float? = (apm["float"] as FloatAttrProcessor).from(s)?.value()
    open fun enum(s: String?): Long? = (apm["enum"] as EnumAttrProcessor).from(s)?.value()
    open fun flag(s: String?): Long? = (apm["flag"] as FlagAttrProcessor).from(s)?.value()

    companion object {
        val gaps = mutableMapOf<Attr, AttrProcessor<*>>()
        const val NOT_CARED = -1
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
    override fun prepare() {
        register(Attrs.Resources2.name)
        register(Attrs.Resources2.type)  // TODO
        register(Attrs.Resources2.format)  // TODO
        register(Attrs.Resources2.parent)  // TODO
        register(Attrs.Resources2.value)  // TODO
        register(Attrs.Resources2.formatted)  // TODO
        register(Attrs.Resources2.translatable)  // TODO
        register(Attrs.Resources2.quantity)
    }

    override fun process(node: Node): NamedNodeValue<List<NamedNodeValue<*>>>? {
        if (node.name != "resources" || node.name != "array" || node.children.isEmpty()) {
            return null
        }
        val list = mutableListOf<NamedNodeValue<*>>()
        node.children.forEach {
            val name = aps[Attrs.Resources2.name]!!.from(it["name"])?.string()
                    ?: return makeFail("resources's element should has name attribute")
            val temp: NamedNodeValue<*> = when (it.name) {
                "item" -> item(it, name)
                "style" -> style(it, name)
                "attr" -> attr(it, name)
                "declare-styleable" -> declareStyleable(it, name)
                "drawable" -> drawable(it, name)
                "array" -> array(it, name)
                "dimen" -> dimen(it, name)
                "color" -> color(it, name)
                "bool" -> bool(it, name)
                "fraction" -> fraction(it, name)
                "integer" -> integer(it, name)
                "integer-array" -> integerArray(it, name)
                "string" -> string(it, name)
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
        return NamedNodeValue(it, apm.refer(it.text) ?: return makeFail("drawable's text should be reference"), DRAWABLE, name)
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
        return NamedNodeValue(it, apm.dimen(it.text, context) ?: return makeFail("dimen's text is incorrect"), DIMEN, name)
    }

    protected open fun color(it: Node, name: String): NamedNodeValue<*>? {
        return NamedNodeValue(it, apm.color(it.text) ?: return makeFail("color's text is incorrect"), COLOR, name)
    }

    protected open fun bool(it: Node, name: String): NamedNodeValue<*>? {
        return NamedNodeValue(it, apm.bool(it.text) ?: return makeFail("bool's text is incorrect"), BOOL, name)
    }

    protected open fun fraction(it: Node, name: String): NamedNodeValue<*>? {
        return NamedNodeValue(it, apm.fraction(it.text) ?: return makeFail("fraction's text is incorrect"), FRACTION, name)
    }

    protected open fun integer(it: Node, name: String): NamedNodeValue<*>? {
        return NamedNodeValue(it, apm.int(it.text) ?: return makeFail("integer's text is incorrect"), INTEGER, name)
    }

    protected open fun integerArray(it: Node, name: String): NamedNodeValue<*>? {
        if (it.children.isEmpty()) {
            return makeFail("integer array must have children")
        }
        return NamedNodeValue(it, it.children.map { integer ->
            apm.int(integer.text) ?: return makeFail("integer array's item text is incorrect")
        }.toLongArray(), INTEGER_ARRAY, name)
    }

    protected open fun string(it: Node, name: String): NamedNodeValue<*>? {
        return NamedNodeValue(it, apm.str(it.text) ?: return makeFail("string's text is incorrect"), STRING, name)
    }

    protected open fun stringArray(it: Node, name: String): NamedNodeValue<*>? {
        if (it.children.isEmpty()) {
            return makeFail("string array must have children")
        }
        return NamedNodeValue(it, it.children.map { string ->
            apm.str(string.text) ?: return makeFail("string array's item text is incorrect")
        }.toTypedArray(), STRING_ARRAY, name)
    }

    protected open fun plurals(it: Node, name: String): NamedNodeValue<*>? {
        if (it.children.isEmpty()) {
            return makeFail("plurals must have children")
        }
        return NamedNodeValue(it, it.children.map { item ->
            (apm.enum(item["quantity"]) ?: return makeFail("the quantity attribute of plurals's item is incorrect")) to
                    (apm.str(item.text) ?: return makeFail("plurals's item text is incorrect"))
        }.toMap(), STRING_ARRAY, name)
    }

    companion object {
        const val ITEM = 1  // 注意， string / bool / dimen / ... 这种基本类型会转换为对应的 FormatValue ，其余用 ReferenceAttrValue
        const val STYLE = 2
        const val DECLARE_STYLEABLE = 3
        const val ATTR = 8

        const val DRAWABLE = 4
        const val ARRAY = 7

        const val DIMEN = 5
        const val COLOR = 6
        const val BOOL = 9
        const val FRACTION = 10
        const val INTEGER = 11
        const val INTEGER_ARRAY = 12
        const val STRING = 13
        const val STRING_ARRAY = 14
        const val PLURALS = 15

        const val PLURALS_ZERO = 0L
        const val PLURALS_ONE = 1L
        const val PLURALS_TWO = 2L
        const val PLURALS_FEW = 3L
        const val PLURALS_MANY = 4L
        const val PLURALS_OTHER = 5L

        fun NamedNodeValue<String>.str(vararg args: Any): String = String.format(value(), *args)
        fun NamedNodeValue<Map<Long, String>>.plurals(num: Long, vararg args: Any): String {
            val temp = value()
            return when {
                num == 0L -> temp[PLURALS_ZERO]
                num == 1L -> temp[PLURALS_ONE]
                num == 2L -> temp[PLURALS_TWO]
                num < 100L -> temp[PLURALS_FEW]
                num >= 100L -> temp[PLURALS_MANY]
                else -> temp[PLURALS_OTHER]
            }!!
        }
    }
}

abstract class FreeResProcessor<T>(manager: IAttrProcessorManager, _type: String) : ResProcessor<T>(manager, _type) {
    @CallSuper
    override fun prepare() {
        register(Attrs.FreeRes.name)
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
open class SelectorColorResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<ColorStateList>(manager, "color") {
    override fun prepare() {
        super.prepare()
        register(Attrs.Color.color)
        register(Attrs.Color.state_accelerated)
        register(Attrs.Color.state_activated)
        register(Attrs.Color.state_active)
        register(Attrs.Color.state_checkable)
        register(Attrs.Color.state_checked)
        register(Attrs.Color.state_drag_can_accept)
        register(Attrs.Color.state_drag_hovered)
        register(Attrs.Color.state_enabled)
        register(Attrs.Color.state_first)
        register(Attrs.Color.state_focused)
        register(Attrs.Color.state_hovered)
        register(Attrs.Color.state_last)
        register(Attrs.Color.state_middle)
        register(Attrs.Color.state_pressed)
        register(Attrs.Color.state_selected)
        register(Attrs.Color.state_single)
        register(Attrs.Color.state_window_focused)
    }

    override fun process(node: Node): NamedNodeValue<ColorStateList>? {
        if (node.name != "selector" || node.children.isEmpty()) {
            return null
        }
        val name = aps[Attrs.Resources2.name]!!.from(node["name"])?.string()
                ?: return makeFail("resources's element should has name attribute")
        val stateSpecs = mutableListOf<IntArray>()
        val colors = mutableListOf<Int>()
        node.children.forEach { item ->
            item.attributes.forEach { attrs ->
            }
        }
        return NamedNodeValue(node, ColorStateList(stateSpecs.toTypedArray(), colors.toIntArray()), COLOR_STATE_LIST, name)
    }

    companion object {
        const val COLOR_STATE_LIST = 16

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

// open class GradientColorResProcessor(_attrProcessorManager: IAttrProcessorManager) : FreeResProcessor<android.content.res.GradientColor>(_attrProcessorManager, "color")
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
open class AnimatorResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<Animator>(manager, "animator") {
    override fun process(node: Node): NamedNodeValue<Animator>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class ObjAnimatorResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<ObjectAnimator>(manager, "animator") {
    override fun process(node: Node): NamedNodeValue<ObjectAnimator>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class AnimatorSetResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<AnimatorSet>(manager, "animator") {
    override fun process(node: Node): NamedNodeValue<AnimatorSet>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class AnimatorSelectorResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<Map<Int, Animator>>(manager, "animator") {
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
open class AnimationSetResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<AnimationSet>(manager, "anim") {
    override fun process(node: Node): NamedNodeValue<AnimationSet>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class RotateResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<RotateAnimation>(manager, "anim") {
    override fun process(node: Node): NamedNodeValue<RotateAnimation>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class ScaleResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<ScaleAnimation>(manager, "anim") {
    override fun process(node: Node): NamedNodeValue<ScaleAnimation>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class AlphaResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<AlphaAnimation>(manager, "anim") {
    override fun process(node: Node): NamedNodeValue<AlphaAnimation>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class TranslateResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<TranslateAnimation>(manager, "anim") {
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
open class AnimationListResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<AnimationDrawable>(manager, "drawable") {
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
open class ShapeResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<ShapeDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<ShapeDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class ClipResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<ClipDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<ClipDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class InsetResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<InsetDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<InsetDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class LayerListResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<LayerDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<LayerDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class LevelListResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<LevelListDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<LevelListDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class VectorResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<VectorDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<VectorDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class ColorDrawableResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<ColorDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<ColorDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class SelectorDrawableResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<StateListDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<StateListDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class GradientDrawableResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<GradientDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<GradientDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class AdaptiveIconResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<AdaptiveIconDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<AdaptiveIconDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class AnimatedRotateResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<Attrs.AnimatedRotateDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<Attrs.AnimatedRotateDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class AnimatedSelectorResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<AnimatedStateListDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<AnimatedStateListDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class AnimatedVectorResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<AnimatedVectorDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<AnimatedVectorDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class BitmapResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<BitmapDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<BitmapDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class DrawableResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<Drawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<Drawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class RippleResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<RippleDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<RippleDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class MaskableIconResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<AnimatedStateListDrawable>(manager, "drawable") {
    override fun process(node: Node): NamedNodeValue<AnimatedStateListDrawable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class TransitionResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<TransitionDrawable>(manager, "drawable") {
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
open class MenuResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<Menu>(manager, "menu") {
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
open class FontResProcessor(manager: IAttrProcessorManager) : FreeResProcessor<Font>(manager, "font") {
    override fun process(node: Node): NamedNodeValue<Font>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

/**
 * custom
 */
