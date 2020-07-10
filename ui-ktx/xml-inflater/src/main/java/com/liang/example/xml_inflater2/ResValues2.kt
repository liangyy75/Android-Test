@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.liang.example.xml_inflater2

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.StateListAnimator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Path
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.DrawableContainer
import android.os.Build
import android.util.TypedValue
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.view.animation.GridLayoutAnimationController
import android.view.animation.LayoutAnimationController
import android.view.animation.RotateAnimation
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import androidx.annotation.RequiresApi
import com.liang.example.basic_ktx.ReflectHelper

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
open class AnimatorResProcessor : ResProcessor<ValueAnimator>("animator", false, true, ResType.ANIMATOR) {
    open var propertyValueResProcessor = PropertyValueResProcessor()

    override fun innerProcess(node: Node): ValueAnimator? = parseValueAnimator(node, null)?.second

    open fun parseValueAnimator(node: Node, origin: ValueAnimator?): Pair<Int, ValueAnimator>? {
        val context = apm.context
        val result = origin ?: ValueAnimator()
        if (node.children.isNotEmpty()) {
            val pVHs = node.children.mapNotNull { propertyValueResProcessor.process(it) }
            if (pVHs.isNotEmpty()) {
                result.setValues(*pVHs.map { it.value() }.toTypedArray())
            }
        }
        result.duration = long2(node[Attrs.Animator.duration]) ?: return makeFail("duration attribute should be long format")
        result.startDelay = long2(node[Attrs.Animator.startOffset]) ?: 0L
        var valueType = types[str2(node[Attrs.Animator.valueType])] ?: VALUE_TYPE_UNDEFINED
        val pair = propertyValueResProcessor.getPVH(node, valueType, "", context, this)
        if (pair != null) {
            valueType = pair.first
            result.setValues(pair.second)
        }
        result.repeatCount = int2(node[Attrs.Animator.repeatCount]) ?: 0
        result.repeatMode = enum(node, Attrs.Animator.repeatMode) ?: ValueAnimator.RESTART
        val interpolatorId = interpolators[node[Attrs.Animator.interpolator]] // TODO: custom interpolator
        if (interpolatorId != null) {
            result.interpolator = AnimationUtils.loadInterpolator(context, interpolatorId)
        }
        return Pair(valueType, result)
    }

    // TODO: support pathData
    open class PropertyValueResProcessor : ResProcessor<PropertyValuesHolder>(
            "propertyValuesHolder", false, false, ResType.PROPERTY_VALUES_HOLDER) {
        override fun innerProcess(node: Node): PropertyValuesHolder? {
            val context = apm.context
            val propertyName = str2(node[Attrs.PropertyValuesHolder.propertyName])
                    ?: return makeFail("animator's element should has propertyName attribute")
            var valueType: Int = types[str2(node[Attrs.PropertyValuesHolder.valueType])] ?: VALUE_TYPE_UNDEFINED
            val propertyValuesHolder: PropertyValuesHolder? = if (node.children.isNotEmpty()) {
                val pair = keyFramesParse(node, valueType, propertyName, context, this) ?: return null
                valueType = pair.first
                pair.second
            } else {
                val pair = getPVH(node, valueType, propertyName, context, this) ?: return null
                valueType = pair.first
                pair.second
            }
            return when {
                propertyValuesHolder != null -> {
                    if (valueType == VALUE_TYPE_COLOR) {
                        propertyValuesHolder.setEvaluator(argbEvaluator)
                    }
                    propertyValuesHolder
                }
                else -> null
            }
        }

        open fun getPVH(node: Node, valueType: Int, propertyName: String, context: Context, resProcessor: ResProcessor<*>)
                : Pair<Int, PropertyValuesHolder>? {
            var valueType2 = valueType
            val valueFrom = node[Attrs.PropertyValuesHolder.valueFrom]
            val valueTo = node[Attrs.PropertyValuesHolder.valueTo]
            val hasFrom = valueFrom != null
            val hasTo = valueTo != null
            if (valueType2 == VALUE_TYPE_UNDEFINED) {
                valueType2 = when {
                    hasFrom && ColorAttrProcessor.isColor(valueFrom) || hasTo && ColorAttrProcessor.isColor(valueTo) -> VALUE_TYPE_COLOR
                    else -> VALUE_TYPE_FLOAT
                }
            }
            return Pair(valueType2, when (valueType2) {
                VALUE_TYPE_PATH -> pathTypeParse(hasFrom, valueFrom, hasTo, valueTo, propertyName) ?: return null
                VALUE_TYPE_FLOAT -> when {
                    hasFrom && hasTo -> PropertyValuesHolder.ofFloat(propertyName,
                            resProcessor.dimen2(valueFrom) ?: resProcessor.float2(valueFrom)
                            ?: return makeFail("valueFrom attribute is wrong"),
                            resProcessor.dimen2(valueTo) ?: resProcessor.float2(valueTo)
                            ?: return makeFail("valueTo attribute is wrong"))
                    hasFrom -> PropertyValuesHolder.ofFloat(propertyName,
                            resProcessor.dimen2(valueFrom) ?: resProcessor.float2(valueFrom)
                            ?: return makeFail("valueFrom attribute is wrong"))
                    hasTo -> PropertyValuesHolder.ofFloat(propertyName,
                            resProcessor.dimen2(valueTo) ?: resProcessor.float2(valueTo)
                            ?: return makeFail("valueTo attribute is wrong"))
                    else -> return null
                }
                else -> when {
                    hasFrom && hasTo -> PropertyValuesHolder.ofInt(propertyName,
                            resProcessor.dimen2(valueFrom)?.toInt() ?: resProcessor.color2(valueFrom)
                            ?: resProcessor.int2(valueFrom) ?: return makeFail("valueFrom attribute is wrong"),
                            resProcessor.dimen2(valueTo)?.toInt() ?: resProcessor.color2(valueTo)
                            ?: resProcessor.int2(valueTo) ?: return makeFail("valueTo attribute is wrong"))
                    hasFrom -> PropertyValuesHolder.ofInt(propertyName,
                            resProcessor.dimen2(valueFrom)?.toInt() ?: resProcessor.color2(valueFrom)
                            ?: resProcessor.int2(valueFrom) ?: return makeFail("valueFrom attribute is wrong"))
                    hasTo -> PropertyValuesHolder.ofInt(propertyName,
                            resProcessor.dimen2(valueTo)?.toInt() ?: resProcessor.color2(valueTo)
                            ?: resProcessor.int2(valueTo) ?: return makeFail("valueTo attribute is wrong"))
                    else -> return null
                }
            })
        }

        open fun keyFramesParse(node: Node, valueType: Int, propertyName: String, context: Context, resProcessor: ResProcessor<*>)
                : Pair<Int, PropertyValuesHolder>? {
            var valueType2 = valueType
            val keyframes = node.children.mapNotNull {
                val pair = keyFrameParse(valueType2, it, context, resProcessor)
                if (pair != null) {
                    valueType2 = pair.first
                }
                pair?.second
            }.toMutableList()
            val size = keyframes.size
            if (size != 0) {
                distributeKeyFrames(keyframes, size)
            }
            return Pair(valueType2, PropertyValuesHolder.ofKeyframe(propertyName, *keyframes.toTypedArray()))
        }

        open fun keyFrameParse(valueType: Int, keyFrameNode: Node, context: Context, resProcessor: ResProcessor<*>): Pair<Int, Keyframe>? {
            var valueType2 = valueType
            return if (keyFrameNode.name == "keyframe") {
                val keyFrameValue = keyFrameNode[Attrs.Keyframe.value]
                val keyFrameFraction = keyFrameNode[Attrs.Keyframe.fraction]
                if (valueType2 == VALUE_TYPE_UNDEFINED) {
                    valueType2 = types[keyFrameNode[Attrs.Keyframe.valueType]] ?: when {
                        ColorAttrProcessor.isColor(keyFrameValue) -> VALUE_TYPE_COLOR
                        else -> VALUE_TYPE_FLOAT
                    }
                }
                if (valueType2 == VALUE_TYPE_PATH) {
                    return makeFail("keyframe node can't have pathType")
                }
                val keyframe = when {
                    keyFrameValue == null && valueType2 == VALUE_TYPE_FLOAT -> Keyframe.ofFloat(resProcessor.float2(keyFrameFraction)
                            ?: return makeFail("keyframe node should have right fraction attribute or value attribute"))
                    keyFrameValue == null -> Keyframe.ofFloat(resProcessor.float2(keyFrameFraction)
                            ?: return makeFail("keyframe node should have right fraction attribute or value attribute"))
                    valueType2 == VALUE_TYPE_FLOAT -> Keyframe.ofFloat(
                            resProcessor.float2(keyFrameFraction) ?: return makeFail("keyframe node should have right fraction attribute"),
                            resProcessor.float2(keyFrameValue) ?: return makeFail("keyframe node should have right value attribute"))
                    else -> Keyframe.ofInt(
                            resProcessor.float2(keyFrameFraction) ?: return makeFail("keyframe node should have right fraction attribute"),
                            resProcessor.int2(keyFrameValue) ?: return makeFail("keyframe node should have right value attribute"))
                }
                val interpolatorId = interpolators[keyFrameNode[Attrs.Keyframe.interpolator]] // TODO: custom interpolator
                if (interpolatorId != null) {
                    keyframe.interpolator = AnimationUtils.loadInterpolator(context, interpolatorId)
                }
                Pair(valueType2, keyframe)
            } else null
        }

        open fun distributeKeyFrames(keyframes: MutableList<Keyframe>, size: Int) {
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

        open fun pathTypeParse(hasFrom: Boolean, valueFrom: String?, hasTo: Boolean, valueTo: String?, propertyName: String): PropertyValuesHolder? {
            val nodeFrom = when {
                hasFrom -> ReflectHelper.newInstance<Any>("android.util.PathParser\$PathData", valueFrom)
                else -> null
            }
            val nodeTo = when {
                hasTo -> ReflectHelper.newInstance<Any>("android.util.PathParser\$PathData", valueTo)
                else -> null
            }
            return when {
                hasFrom && hasTo -> {
                    if (ReflectHelper.findMethod("canMorph", ReflectHelper.findCls("android.util.PathParser")!!,
                                    nodeFrom!!::class.java, nodeTo!!::class.java)?.invoke(nodeFrom, nodeTo) as? Boolean == false) {
                        return makeFail("Can't morph from $valueFrom to $valueTo")
                    }
                    PropertyValuesHolder.ofObject(propertyName, ReflectHelper.newInstance<TypeEvaluator<*>>(
                            "android.animation.AnimationInflater\$TypeEvaluator"), nodeFrom, nodeTo)
                }
                hasFrom -> PropertyValuesHolder.ofObject(propertyName, ReflectHelper.newInstance<TypeEvaluator<*>>(
                        "android.animation.AnimationInflater\$TypeEvaluator"), nodeFrom)
                hasTo -> PropertyValuesHolder.ofObject(propertyName, ReflectHelper.newInstance<TypeEvaluator<*>>(
                        "android.animation.AnimationInflater\$TypeEvaluator"), nodeTo)
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

        val types = mutableMapOf(
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

open class ObjAnimatorResProcessor(open val animatorResProcessor: AnimatorResProcessor)
    : ResProcessor<ObjectAnimator>("objectAnimator", false, true, ResType.OBJECT_ANIMATOR) {
    override fun innerProcess(node: Node): ObjectAnimator? {
        val pair = animatorResProcessor.parseValueAnimator(node, ObjectAnimator()) ?: return null
        val objectAnimator = pair.second as ObjectAnimator
        var valueType = pair.first
        val pathData = node[Attrs.ObjectAnimator.pathData]
        if (pathData != null) {
            if (valueType == AnimatorResProcessor.VALUE_TYPE_PATH || valueType == AnimatorResProcessor.VALUE_TYPE_UNDEFINED) {
                valueType = AnimatorResProcessor.VALUE_TYPE_FLOAT
            }
            val propertyXName = str2(node[Attrs.ObjectAnimator.propertyXName])
            val propertyYName = str2(node[Attrs.ObjectAnimator.propertyYName])
            if (propertyXName == null && propertyYName == null) {
                return makeFail("propertyXName or propertyYName is needed for PathData")
            }
            val path: Path = ReflectHelper.invoke("", ReflectHelper.findCls("android.util.PathParser")!!, pathData)!!
            val keyframeSet = ReflectHelper.invokeS<Any>("ofPath", ReflectHelper.findCls("android.animation.KeyframeSet")!!, path, 0.5f)!!
            val xKeyframes: Any
            val yKeyframes: Any = if (valueType == AnimatorResProcessor.VALUE_TYPE_FLOAT) {
                xKeyframes = ReflectHelper.invoke<Any>("createXFloatKeyframes", keyframeSet)!!
                ReflectHelper.invoke<Any>("createYFloatKeyframes", keyframeSet)!!
            } else {
                xKeyframes = ReflectHelper.invoke<Any>("createXIntKeyframes", keyframeSet)!!
                ReflectHelper.invoke<Any>("createYIntKeyframes", keyframeSet)!!
            }
            when {
                propertyXName == null && propertyYName == null -> Unit
                propertyXName != null -> objectAnimator.setValues(
                        ReflectHelper.invokeS("ofKeyframes", PropertyValuesHolder::class.java, propertyXName, xKeyframes))
                propertyYName != null -> objectAnimator.setValues(
                        ReflectHelper.invokeS("ofKeyframes", PropertyValuesHolder::class.java, propertyYName, yKeyframes))
                else -> objectAnimator.setValues(ReflectHelper.invokeS("ofKeyframes", PropertyValuesHolder::class.java, propertyXName, xKeyframes),
                        ReflectHelper.invokeS("ofKeyframes", PropertyValuesHolder::class.java, propertyYName, yKeyframes))
            }
        } else {
            val propertyName = str2(node[Attrs.ObjectAnimator.propertyName])
            if (propertyName != null) {
                objectAnimator.setPropertyName(propertyName)
            }
        }
        return objectAnimator
    }
}

open class AnimatorSetResProcessor(open val animatorResProcessor: AnimatorResProcessor, open val objAnimatorResProcessor: ObjAnimatorResProcessor)
    : ResProcessor<AnimatorSet>("set", true, true, ResType.ANIMATOR_SET) {
    override fun innerProcess(node: Node): AnimatorSet? {
        val set = AnimatorSet()
        val valueAnimators = node.children.mapNotNull {
            when (it.name) {
                "set" -> process(it, false)
                "animator" -> animatorResProcessor.process(it, false)
                "objectAnimator" -> objAnimatorResProcessor.process(it, false)
                else -> makeFail("child element of set should be animator or objectAnimator")
            }?.value()
        }
        if (valueAnimators.isNotEmpty()) {
            if (enum(node, Attrs.AnimatorSet.ordering) == 0) {
                set.playTogether(valueAnimators)
            } else {
                set.playSequentially(valueAnimators)
            }
        }
        return set
    }
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
open class StateListAnimatorResProcessor : ResProcessor<StateListAnimator>(
        "selector", true, true, ResType.ANIMATOR_STATE_LIST) {
    override fun innerProcess(node: Node): StateListAnimator? {
        val stateListAnimator = StateListAnimator()
        node.children.forEach { item ->
            if (item.name == "item") {
                val referId = refer(item[Attrs.StateListAnimatorItem.animation])
                var animator = when {
                    referId != null -> ResStore.loadAnimator(referId, apm.context, true)
                    else -> null
                }
                if (animator == null && item.children.isNotEmpty()) {
                    val temp = item.children[0]
                    animator = when (temp.name) {
                        "selector" -> return makeFail("<item> can't possess selector as it's child")
                        "set" -> rpm.get<AnimatorSetResProcessor>(AnimatorSetResProcessor::class.java)?.process(temp, false)?.value()
                        else -> rpm.process<Animator>(temp, false)
                    }
                }
                if (animator != null) {
                    stateListAnimator.addState((ColorSelectorResProcessor.getStates(item, this, arrayOf(Attrs.StateListAnimatorItem.animation))
                            ?: return@forEach).toIntArray(), animator)
                } else {
                    makeFail("<item> should have animation attribute or animator sub element")!!
                }
            }
        }
        return stateListAnimator
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
abstract class AnimResProcessor<T : Animation>(rootName: String, careChild: Boolean, careName: Boolean, resIntType: Int)
    : ResProcessor<T>(rootName, careChild, careName, resIntType) {
    abstract fun innerProcess2(node: Node): T?

    override fun innerProcess(node: Node): T? {
        val result = innerProcess2(node) ?: return null
        val interpolatorId = AnimatorResProcessor.interpolators[node[Attrs.Animation.interpolator]] // TODO: custom interpolator
        if (interpolatorId != null) {
            result.interpolator = AnimationUtils.loadInterpolator(apm.context, interpolatorId)
        }
        long2(node[Attrs.Animation.duration])?.let { result.duration = it }
        bool2(node[Attrs.Animation.fillAfter])?.let { result.fillAfter = it }
        bool2(node[Attrs.Animation.fillEnabled])?.let { result.isFillEnabled = it }
        bool2(node[Attrs.Animation.fillBefore])?.let { result.fillBefore = it }
        long2(node[Attrs.Animation.startOffset])?.let { result.startOffset = it }
        int2(node[Attrs.Animation.repeatCount])?.let { result.repeatCount = it }
        enum(node, Attrs.Animation.repeatMode)?.let { result.repeatMode = it }
        enum(node, Attrs.Animation.zAdjustment)?.let { result.zAdjustment = it }
        color2(node[Attrs.Animation.background])?.let { result.backgroundColor = it }
        bool2(node[Attrs.Animation.detachWallpaper])?.let { result.detachWallpaper = it }
        bool2(node[Attrs.Animation.showWallpaper])?.let { ReflectHelper.setBoolean("mShowWallpaper", result, it) }
        bool2(node[Attrs.Animation.hasRoundedCorners])?.let { ReflectHelper.setBoolean("mHasRoundedCorners", result, it) }
        return result
    }

    protected fun handlePivot(pivot: String?): Pair<Int, Float> = staticHandlePivot(pivot, this, 0f)

    companion object {
        fun staticHandlePivot(pivot: String?, resProcessor: ResProcessor<*>, default: Float) =
                staticHandlePivot(pivot, resProcessor) ?: Pair(Animation.ABSOLUTE, default)

        fun staticHandlePivot(pivot: String?, resProcessor: ResProcessor<*>) = when (pivot) {
            null -> null
            else -> {
                val pivotXFraction = resProcessor.fractionValue2(pivot)
                when {
                    pivotXFraction != null -> {
                        Pair(when (pivotXFraction.first) {
                            FractionAttrProcessor.RELATIVE_TO_PARENT -> Animation.RELATIVE_TO_PARENT
                            FractionAttrProcessor.RELATIVE_TO_SELF -> Animation.RELATIVE_TO_SELF
                            else -> Animation.RELATIVE_TO_SELF  // TODO: relative_to_root
                        }, pivotXFraction.second / 100f)
                    }
                    pivot.last() !in digital -> when (val value = resProcessor.dimen2(pivot)) {
                        null -> null
                        else -> Pair(Animation.ABSOLUTE, value)
                    }
                    else -> when (val value = resProcessor.float2(pivot) ?: resProcessor.int2(pivot)?.toFloat()) {
                        null -> null
                        else -> Pair(Animation.ABSOLUTE, value)
                    }
                }
            }
        }
    }
}

open class AnimationSetResProcessor(
        open val rotateResProcessor: RotateResProcessor, open val scaleResProcessor: ScaleResProcessor,
        open val alphaResProcessor: AlphaResProcessor, open val translateResProcessor: TranslateResProcessor
) : AnimResProcessor<AnimationSet>("set", true, true, ResType.ANIMATION_SET) {
    override fun innerProcess2(node: Node): AnimationSet? {
        val result = AnimationSet(bool2(node[Attrs.AnimationSet.shareInterpolator]) ?: false)
        node.children.forEach {
            val animation = when (it.name) {
                "set" -> process(it, false)
                "rotate" -> rotateResProcessor.process(it, false)
                "scale" -> scaleResProcessor.process(it, false)
                "alpha" -> alphaResProcessor.process(it, false)
                "translate" -> translateResProcessor.process(it, false)
                else -> makeFail("element of set's child is incorrect: ${it.name}")
            }?.value()
            if (animation != null) {
                result.addAnimation(animation)
            }
        }
        return result
    }
}

open class RotateResProcessor : AnimResProcessor<RotateAnimation>("rotate", false, true, ResType.ANIMATION_ROTATE) {
    override fun innerProcess2(node: Node): RotateAnimation? {
        val pivotXPair = handlePivot(node[Attrs.RotateAnimation.pivotX])
        val pivotYPair = handlePivot(node[Attrs.RotateAnimation.pivotY])
        return RotateAnimation(float2(node[Attrs.RotateAnimation.fromDegrees]) ?: 0f, float2(node[Attrs.RotateAnimation.toDegrees]) ?: 0f,
                pivotXPair.first, pivotXPair.second, pivotYPair.first, pivotYPair.second)
    }
}

open class ScaleResProcessor : AnimResProcessor<ScaleAnimation>("scale", false, true, ResType.ANIMATION_SCALE) {
    override fun innerProcess2(node: Node): ScaleAnimation? {
        val pivotXPair = handlePivot(node[Attrs.ScaleAnimation.pivotX])
        val pivotYPair = handlePivot(node[Attrs.ScaleAnimation.pivotY])
        val fromXTriple = handleScale(node[Attrs.ScaleAnimation.fromXScale])
        val fromYTriple = handleScale(node[Attrs.ScaleAnimation.fromYScale])
        val toXTriple = handleScale(node[Attrs.ScaleAnimation.toXScale])
        val toYTriple = handleScale(node[Attrs.ScaleAnimation.toYScale])
        val result = ScaleAnimation(fromXTriple.first, toXTriple.first, fromYTriple.first, toYTriple.first,
                pivotXPair.first, pivotXPair.second, pivotYPair.first, pivotYPair.second)
        handleInnerFields(result, fromXTriple, "mFromXData", "mFromXType")
        handleInnerFields(result, fromYTriple, "mFromYData", "mFromYType")
        handleInnerFields(result, toXTriple, "mToXData", "mToXType")
        handleInnerFields(result, toYTriple, "mToYData", "mToYType")
        return result
    }

    protected fun handleScale(scale: String?): Triple<Float, Int?, Int> = when (scale) {
        null -> Triple(0f, null, TypedValue.TYPE_FLOAT)
        else -> {
            when (val floatValue = float2(scale)) {
                null -> when (val fractionValue = fraction2(scale)) {
                    null -> when (val dimenValue = dimen2(scale)) {
                        null -> Triple(0f, null, TypedValue.TYPE_FLOAT)
                        else -> Triple(0f, dimenValue.toInt(), TypedValue.TYPE_DIMENSION)
                    }
                    else -> Triple(0f, fractionValue.times(100).toInt(), TypedValue.TYPE_FRACTION)
                }
                else -> Triple(floatValue, null, TypedValue.TYPE_FLOAT)
            }
        }
    }

    protected fun handleInnerFields(scale: ScaleAnimation, dataAndTypeTriple: Triple<Float, Int?, Int>, dataField: String, typeField: String) {
        ReflectHelper.setInt(dataField, scale, dataAndTypeTriple.second ?: return)
        ReflectHelper.setInt(typeField, scale, dataAndTypeTriple.third)
    }
}

open class AlphaResProcessor : AnimResProcessor<AlphaAnimation>("alpha", false, true, ResType.ANIMATION_ALPHA) {
    override fun innerProcess2(node: Node): AlphaAnimation? =
            AlphaAnimation(float2(node[Attrs.AlphaAnimation.fromAlpha]) ?: 1f, float2(node[Attrs.AlphaAnimation.toAlpha]) ?: 1f)
}

open class TranslateResProcessor : AnimResProcessor<TranslateAnimation>("translate", false, true, ResType.ANIMATION_TRANSLATE) {
    override fun innerProcess2(node: Node): TranslateAnimation? {
        val fromXPair = handlePivot(node[Attrs.TranslateAnimation.fromXDelta])
        val fromYPair = handlePivot(node[Attrs.TranslateAnimation.fromYDelta])
        val toXPair = handlePivot(node[Attrs.TranslateAnimation.toXDelta])
        val toYPair = handlePivot(node[Attrs.TranslateAnimation.toYDelta])
        return TranslateAnimation(fromXPair.first, fromXPair.second, toXPair.first, toXPair.second, fromYPair.first, fromYPair.second,
                toYPair.first, toYPair.second)
    }
}

open class LayoutAnimationResProcessor : ResProcessor<LayoutAnimationController>(
        "layoutAnimation", false, true, ResType.LAYOUT_ANIMATION) {
    override fun innerProcess(node: Node): LayoutAnimationController? {
        val animation = refer(node[Attrs.LayoutAnimation.animation])?.let { ResStore.loadAnimation(it, apm.context, true) }
                ?: return makeFail("layoutAnimation should have animation attribute")
        val delay = AnimResProcessor.staticHandlePivot(node[Attrs.LayoutAnimation.delay], this, 0.5f).second
        val result = LayoutAnimationController(animation, delay)
        result.order = enum(node, Attrs.LayoutAnimation.animationOrder) ?: 0
        val interpolatorId = AnimatorResProcessor.interpolators[node[Attrs.LayoutAnimation.interpolator]] // TODO: custom interpolator
        if (interpolatorId != null) {
            result.interpolator = AnimationUtils.loadInterpolator(apm.context, interpolatorId)
        }
        return result
    }
}

open class GridLayoutAnimationResProcessor : ResProcessor<GridLayoutAnimationController>(
        "gridLayoutAnimation", false, true, ResType.GRID_LAYOUT_ANIMATION) {
    override fun innerProcess(node: Node): GridLayoutAnimationController? {
        val animation = refer(node[Attrs.LayoutAnimation.animation])?.let { ResStore.loadAnimation(it, apm.context, true) }
                ?: return makeFail("layoutAnimation should have animation attribute")
        val columnDelay = AnimResProcessor.staticHandlePivot(node[Attrs.GridLayoutAnimation.columnDelay], this, 0.5f).second
        val rowDelay = AnimResProcessor.staticHandlePivot(node[Attrs.GridLayoutAnimation.rowDelay], this, 0.5f).second
        val result = GridLayoutAnimationController(animation, columnDelay, rowDelay)
        result.order = enum(node, Attrs.LayoutAnimation.animationOrder) ?: 0
        result.direction = flag(node, Attrs.GridLayoutAnimation.direction)
                ?: GridLayoutAnimationController.DIRECTION_LEFT_TO_RIGHT or GridLayoutAnimationController.DIRECTION_TOP_TO_BOTTOM
        result.directionPriority = enum(node, Attrs.GridLayoutAnimation.directionPriority) ?: GridLayoutAnimationController.PRIORITY_NONE
        val interpolatorId = AnimatorResProcessor.interpolators[node[Attrs.LayoutAnimation.interpolator]] // TODO: custom interpolator
        if (interpolatorId != null) {
            result.interpolator = AnimationUtils.loadInterpolator(apm.context, interpolatorId)
        }
        return result
    }
}

// TODO: android.view.animation.ClipRectAnimation -- @hide

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
open class AnimationListResProcessor : BaseDrawResProcessor<AnimationDrawable>(
        "animation-list", true, true, ResType.ANIMATION_DRAWABLE) {
    override fun innerProcess2(node: Node): AnimationDrawable? {
        val result = AnimationDrawable()
        result.isOneShot = bool2(node[Attrs.AnimationDrawable.oneshot]) ?: false
        (result.constantState as DrawableContainer.DrawableContainerState).setVariablePadding(
                bool2(node[Attrs.AnimationDrawable.variablePadding]) ?: false)
        node.children.forEach {
            if (it.name == "item") {
                val duration = int2(it[Attrs.AnimationDrawableItem.duration]) ?: return makeFail("<item> tag requires a 'duration' attribute")
                val drawable = getDrawable(it, rpm, Attrs.AnimationDrawableItem.drawable)
                        ?: return makeFail("<item> tag requires a 'drawable' attribute or child tag defining a drawable")
                result.addFrame(drawable, duration)
                drawable.callback = result
            }
        }
        ReflectHelper.invokeN("setFrame", result, 0, true, false)
        return result
    }
}
