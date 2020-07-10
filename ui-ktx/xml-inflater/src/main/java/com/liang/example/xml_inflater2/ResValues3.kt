@file:Suppress("unused", "DEPRECATION", "MemberVisibilityCanBePrivate")

package com.liang.example.xml_inflater2

import android.content.res.Resources
import android.graphics.BlendMode
import android.graphics.Color
import android.graphics.Insets
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableContainer
import android.graphics.drawable.DrawableWrapper
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.LevelListDrawable
import android.graphics.drawable.RotateDrawable
import android.graphics.drawable.ScaleDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import androidx.annotation.RequiresApi
import com.liang.example.basic_ktx.ReflectHelper
import com.liang.example.view_ktx.getIntIdByStrId

// android.graphics.drawable.DrawableInflater
abstract class BaseDrawResProcessor<T : Drawable>(
        rootName: String, careChild: Boolean, careName: Boolean, resIntType: Int)
    : ResProcessor<T>(rootName, careChild, careName, resIntType) {
    abstract fun innerProcess2(node: Node): T?

    override fun innerProcess(node: Node): T? {
        val result = innerProcess2(node) ?: return null
        ReflectHelper.findField("mVisible", Drawable::class.java)?.setBoolean(result, bool2(node[Attrs.Drawable.visible]) ?: true)
        return result
    }

    open fun getDrawable(node: Node, rpm: IResProcessorManager, attr: Attr/* = Attrs.DrawableWrapper.drawable*/): Drawable? {
        val drawAttr = node[attr] ?: return null
        var drawable = color2(drawAttr)?.let { ColorDrawable(it) } ?: refer(drawAttr)?.let { ResStore.loadDrawable(it, rpm.context, true) }
        if (drawable == null && node.children.isNotEmpty()) {
            val temp = node[0]
            drawable = when (temp.name) {
                "rotate" -> rpm.get<RotateDrawResProcessor>(RotateDrawResProcessor::class.java)?.process(temp, false)?.value()
                "scale" -> rpm.get<ScaleDrawResProcessor>(ScaleDrawResProcessor::class.java)?.process(temp, false)?.value()
                "color" -> rpm.get<ColorDrawableResProcessor>(ColorDrawableResProcessor::class.java)?.process(temp, false)?.value()
                "selector" -> rpm.get<StateListDrawableResProcessor>(StateListDrawableResProcessor::class.java)?.process(temp, false)?.value()
                else -> rpm.process(temp, false)
            }
        }
        return drawable
    }

    open fun setLevel(node: Node, result: Drawable) {
        node[Attrs.ClipDrawable.level]?.let {
            result.level = when {
                it.last() !in digital -> fraction2(it)?.times(100)?.toInt()
                        ?: return makeFail("clip drawable's level should be integer / float / fraction", Unit)!!
                !it.contains('.') && !it.contains('e', ignoreCase = false) -> int2(it)
                        ?: return makeFail("clip drawable's level should be integer / float / fraction", Unit)!!
                else -> (10000 * (float2(it) ?: return makeFail("clip drawable's level should be integer / float / fraction", Unit)!!)).toInt()
            }
        }  // 剪切的level值在0-10000之间，0表示全剪切，10000表示全显示
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
open class ShapeResProcessor : BaseDrawResProcessor<GradientDrawable>("shape", true, true, ResType.DRAWABLE_SHAPE) {
    override fun innerProcess2(node: Node): GradientDrawable? {
        val context = apm.context
        val result = GradientDrawable()
        val shape = enum(node, Attrs.GradientDrawable.shape) ?: GradientDrawable.RECTANGLE
        result.shape = shape
        result.setDither(bool2(node[Attrs.GradientDrawable.dither]) ?: false)
        val mGradientState = ReflectHelper.get("mGradientState", result)
        if (shape == GradientDrawable.RING) {
            val innerRadius = dimen2(node[Attrs.GradientDrawable.innerRadius])?.toInt() ?: -1
            ReflectHelper.setInt("mInnerRadius", mGradientState, innerRadius)
            if (innerRadius == -1) {
                ReflectHelper.setFloat("mInnerRadiusRatio", mGradientState, float2(node[Attrs.GradientDrawable.innerRadiusRatio]) ?: 3.0f)
            }
            val thickness = dimen2(node[Attrs.GradientDrawable.thickness])?.toInt() ?: -1
            ReflectHelper.setInt("mThickness", mGradientState, thickness)
            if (thickness == -1) {
                ReflectHelper.setFloat("mThicknessRatio", mGradientState, float2(node[Attrs.GradientDrawable.thicknessRatio]) ?: 9.0f)
            }
            result.useLevel = bool2(node[Attrs.GradientDrawable.useLevel]) ?: true
        }
        val tintMode = int2(node[Attrs.GradientDrawable.tintMode]) ?: -1
        if (tintMode != -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ReflectHelper.set("mBlendMode", mGradientState,
                        ReflectHelper.invoke<Any>("parseBlendMode", Drawable::class.java, tintMode, BlendMode.SRC_IN))
            } else {
                ReflectHelper.set("mTintMode", mGradientState,
                        ReflectHelper.invoke<Any>("parseTintMode", Drawable::class.java, tintMode, PorterDuff.Mode.SRC_IN))
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val tintColor = color2(node[Attrs.GradientDrawable.tint])
            if (tintColor != null) {
                result.setTint(tintColor)
            } else {
                val tint = refer(node[Attrs.GradientDrawable.tint])?.let { ResStore.loadColorStateList(it, context, true) }
                if (tint != null) {
                    result.setTintList(tint)
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ReflectHelper.set("mOpticalInsets", mGradientState, Insets.of(
                    dimen2(node[Attrs.GradientDrawable.opticalInsetLeft])?.toInt() ?: 0, dimen2(node[Attrs.GradientDrawable.opticalInsetTop])?.toInt() ?: 0,
                    dimen2(node[Attrs.GradientDrawable.opticalInsetRight])?.toInt() ?: 0, dimen2(node[Attrs.GradientDrawable.opticalInsetBottom])?.toInt()
                    ?: 0))
        } else {
            ReflectHelper.set("mOpticalInsets", mGradientState, ofInsets?.invoke(null,
                    dimen2(node[Attrs.GradientDrawable.opticalInsetLeft])?.toInt() ?: 0, dimen2(node[Attrs.GradientDrawable.opticalInsetTop])?.toInt() ?: 0,
                    dimen2(node[Attrs.GradientDrawable.opticalInsetRight])?.toInt() ?: 0, dimen2(node[Attrs.GradientDrawable.opticalInsetBottom])?.toInt()
                    ?: 0))
        }
        node.children.forEach {
            when (it.name) {
                "size" -> result.setSize(dimen2(it[Attrs.GradientDrawableSize.width])?.toInt() ?: -1,
                        dimen2(it[Attrs.GradientDrawableSize.height])?.toInt() ?: -1)
                "gradient" -> {
                    val centerXAttr = it[Attrs.GradientDrawableGradient.centerX]
                    val centerYAttr = it[Attrs.GradientDrawableGradient.centerY]
                    val centerX = float2(centerXAttr) ?: fraction2(centerXAttr) ?: 0.5f
                    val centerY = float2(centerYAttr) ?: fraction2(centerYAttr) ?: 0.5f
                    result.setGradientCenter(centerX, centerY)
                    result.useLevel = bool2(it[Attrs.GradientDrawableGradient.useLevel]) ?: false
                    result.gradientType = enum(it, Attrs.GradientDrawableGradient.type) ?: GradientDrawable.LINEAR_GRADIENT
                    val hasGradientColors = ReflectHelper.getBoolean("mGradientColors", mGradientState)
                    val hasGradientCenter = ReflectHelper.invoke<Boolean>("hasCenterColor", mGradientState) ?: false
                    val mGradientColors = ReflectHelper.get("mGradientColors", mGradientState) as? IntArray
                    val prevStart = when {
                        hasGradientColors -> mGradientColors!![0]
                        else -> 0
                    }
                    val prevCenter = when {
                        hasGradientColors -> mGradientColors!![1]
                        else -> 0
                    }
                    val prevEnd = when {
                        hasGradientCenter -> mGradientColors!![2]
                        hasGradientColors -> mGradientColors!![1]
                        else -> 0
                    }
                    val startColor = color2(it[Attrs.GradientDrawableGradient.startColor]) ?: prevStart
                    val hasCenterColor = Attrs.GradientDrawableGradient.centerColor.name in it || hasGradientCenter
                    val centerColor = color2(it[Attrs.GradientDrawableGradient.centerColor]) ?: prevCenter
                    val endColor = color2(it[Attrs.GradientDrawableGradient.endColor]) ?: prevEnd
                    if (hasCenterColor) {
                        ReflectHelper.set("mGradientColors", mGradientState, intArrayOf(startColor, centerColor, endColor))
                        ReflectHelper.set("mPositions", mGradientState, floatArrayOf(0f, when (centerX) {
                            0.5f -> centerY
                            else -> centerX
                        }, 1f))
                    } else {
                        ReflectHelper.set("mGradientColors", mGradientState, intArrayOf(startColor, endColor))
                    }
                    ReflectHelper.setInt("mAngle", mGradientState, (((int2(it[Attrs.GradientDrawableGradient.angle])
                            ?: 0) % 360) + 360) % 360)
                    if (Attrs.GradientDrawableGradient.gradientRadius.name in it) {
                        val radiusType: Int
                        val gradientRadius = it[Attrs.GradientDrawableGradient.gradientRadius]
                        val fractionValue = fractionValue2(gradientRadius)
                        val radius = if (fractionValue != null) {
                            radiusType = when (fractionValue.first) {
                                FractionAttrProcessor.RELATIVE_TO_PARENT -> 2
                                else -> 1
                            }
                            fractionValue.second
                        } else {
                            radiusType = 0
                            dimen2(gradientRadius) ?: float2(gradientRadius)
                        }
                        result.gradientRadius = radius ?: 0.5f
                        ReflectHelper.setInt("mGradientRadiusType", mGradientState, radiusType)
                    }
                }
                "solid" -> {
                    val color = color2(it[Attrs.GradientDrawableSolid.color])
                    if (color != null) {
                        result.setColor(color)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val colorStateList = refer(it[Attrs.GradientDrawableSolid.color])
                                ?.let { resId -> ResStore.loadColorStateList(resId, context, true) }
                        if (colorStateList != null) {
                            result.color = colorStateList
                        }
                    }
                }
                "stroke" -> {
                    val width = dimen2(it[Attrs.GradientDrawableStroke.width])?.toInt() ?: 0
                    val colorAttr = it[Attrs.GradientDrawableStroke.color]
                    val color = color2(colorAttr)
                    val colorStateList = when {
                        color == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ->
                            refer(colorAttr)?.let { resId -> ResStore.loadColorStateList(resId, context, true) }
                        else -> null
                    }
                    val dashWidth = dimen2(it[Attrs.GradientDrawableStroke.dashWidth])
                    when {
                        dashWidth != null -> {
                            val dashGap = dimen2(it[Attrs.GradientDrawableStroke.dashGap]) ?: 0f
                            when {
                                color != null -> result.setStroke(width, color, dashWidth, dashGap)
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> result.setStroke(width, colorStateList, dashWidth, dashGap)
                            }
                        }
                        else -> when {
                            color != null -> result.setStroke(width, color)
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> result.setStroke(width, colorStateList)
                        }
                    }
                }
                "corners" -> {
                    val radius = dimen2(it[Attrs.DrawableCorners.radius]) ?: 0f
                    if (radius != 0f) {
                        result.cornerRadius = radius
                    }
                    val topLeftRadius = dimen2(it[Attrs.DrawableCorners.topLeftRadius]) ?: radius
                    val topRightRadius = dimen2(it[Attrs.DrawableCorners.topRightRadius]) ?: radius
                    val bottomLeftRadius = dimen2(it[Attrs.DrawableCorners.bottomLeftRadius]) ?: radius
                    val bottomRightRadius = dimen2(it[Attrs.DrawableCorners.bottomRightRadius]) ?: radius
                    if (topLeftRadius != radius || topRightRadius != radius || bottomLeftRadius != radius || bottomRightRadius != radius) {
                        result.cornerRadii = floatArrayOf(topLeftRadius, topLeftRadius, topRightRadius, topRightRadius, bottomRightRadius,
                                bottomRightRadius, bottomLeftRadius, bottomLeftRadius)
                    }
                }
                "padding" -> {
                    val padding = Rect(dimen2(it[Attrs.GradientDrawablePadding.left])?.toInt()
                            ?: -1, dimen2(it[Attrs.GradientDrawablePadding.top])?.toInt() ?: -1,
                            dimen2(it[Attrs.GradientDrawablePadding.right])?.toInt() ?: -1, dimen2(it[Attrs.GradientDrawablePadding.bottom])?.toInt()
                            ?: -1)
                    ReflectHelper.set("mPadding", mGradientState, padding)
                }
                else -> makeFail("uncorrect tag in $rootName: ${it.name}")
            }
        }
        ReflectHelper.findMethod("updateLocalState", GradientDrawable::class.java, Resources::class.java)?.invoke(result, apm.context.resources)
        return result
    }

    companion object {
        val ofInsets = ReflectHelper.findMethod("of", ReflectHelper.findCls("android.graphics.Insets"), Int::class.java, Int::class.java, Int::class.java, Int::class.java)
    }
}

open class ScaleDrawResProcessor : BaseDrawResProcessor<ScaleDrawable>("scale", false, true, ResType.DRAWABLE_SCALE) {
    override fun innerProcess2(node: Node): ScaleDrawable? {
        val result = ScaleDrawable(getDrawable(node, rpm, Attrs.ScaleDrawable.drawable)
                ?: return makeFail("scale drawable should have drawable attr"),
                flag(node, Attrs.ScaleDrawable.scaleGravity) ?: Gravity.START,
                fraction2(node[Attrs.ScaleDrawable.scaleWidth]) ?: 0f,
                fraction2(node[Attrs.ScaleDrawable.scaleHeight]) ?: 0f)
        val mState = ReflectHelper.get("mState", result)
        ReflectHelper.setBoolean("mUseIntrinsicSizeAsMin", mState, bool2(node[Attrs.ScaleDrawable.useIntrinsicSizeAsMinimum]) ?: false)
        setLevel(node, result)
        return result
    }
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
open class RotateDrawResProcessor : BaseDrawResProcessor<RotateDrawable>("rotate", false, true, ResType.DRAWABLE_SCALE) {
    override fun innerProcess2(node: Node): RotateDrawable? {
        val result = RotateDrawable()
        result.drawable = getDrawable(node, rpm, Attrs.RotateDrawable.drawable)
                ?: return makeFail("<rotate> tag requires a 'drawable' attribute or child tag defining a drawable")
        result.fromDegrees = float2(node[Attrs.RotateDrawable.fromDegrees]) ?: 0f
        result.toDegrees = float2(node[Attrs.RotateDrawable.toDegrees]) ?: 360f
        val pivotXAttr = node[Attrs.RotateDrawable.pivotX]
        if (pivotXAttr != null) {
            val fractionValue = fraction2(pivotXAttr)
            if (fractionValue != null) {
                result.isPivotXRelative = true
                result.pivotX = fractionValue
            } else {
                result.isPivotXRelative = false
                result.pivotX = float2(pivotXAttr) ?: 0.5f
            }
        }
        val pivotYAttr = node[Attrs.RotateDrawable.pivotY]
        if (pivotYAttr != null) {
            val fractionValue = fraction2(pivotYAttr)
            if (fractionValue != null) {
                result.isPivotYRelative = true
                result.pivotY = fractionValue
            } else {
                result.isPivotYRelative = false
                result.pivotY = float2(pivotYAttr) ?: 0.5f
            }
        }
        result.setVisible(true, true)
        setLevel(node, result)
        return result
    }
}

open class ClipResProcessor : BaseDrawResProcessor<ClipDrawable>("clip", false, true, ResType.DRAWABLE_CLIP) {
    override fun innerProcess2(node: Node): ClipDrawable? {
        val result = ClipDrawable(getDrawable(node, rpm, Attrs.ClipDrawable.drawable)
                ?: return makeFail("clip drawable should have drawable attr"),
                flag(node, Attrs.ClipDrawable.gravity) ?: Gravity.START,
                flag(node, Attrs.ClipDrawable.clipOrientation) ?: ClipDrawable.HORIZONTAL)
        setLevel(node, result)
        return result
    }
}

open class InsetResProcessor : BaseDrawResProcessor<InsetDrawable>("inset", false, true, ResType.DRAWABLE_INSET) {
    override fun innerProcess2(node: Node): InsetDrawable? {
        val drawable = getDrawable(node, rpm, Attrs.InsetDrawable.drawable)
                ?: return makeFail("inset drawable should have drawable attr")
        val insetPair = getInset(node[Attrs.InsetDrawable.inset])
        return when {
            insetPair.first != 0 -> {
                val inset = insetPair.first
                InsetDrawable(drawable, inset, inset, inset, inset)
            }
            insetPair.second != 0f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                val inset = insetPair.second
                InsetDrawable(drawable, inset, inset, inset, inset)
            }
            else -> {
                val leftPair = getInset(node[Attrs.InsetDrawable.insetLeft])
                val topPair = getInset(node[Attrs.InsetDrawable.insetTop])
                val rightPair = getInset(node[Attrs.InsetDrawable.insetRight])
                val bottomPair = getInset(node[Attrs.InsetDrawable.insetBottom])
                if (leftPair.first == 0 && topPair.first == 0 && rightPair.first == 0 && bottomPair.first == 0) {
                    InsetDrawable(drawable, leftPair.first, topPair.first, rightPair.first, bottomPair.first)
                } else if (leftPair.second == 0f && topPair.second == 0f && rightPair.second == 0f && bottomPair.second == 0f
                        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    InsetDrawable(drawable, leftPair.second, topPair.second, rightPair.second, bottomPair.second)
                } else {
                    val drawResult = ReflectHelper.newInstance<InsetDrawable>(InsetDrawable::class.java)
                    val mState = ReflectHelper.get("mState", drawResult)
                    ReflectHelper.set("mInsetLeft", mState, ReflectHelper.newInstance<Any>("android.graphics.drawable.InsetDrawable\$InsetValue",
                            leftPair.second, leftPair.first))
                    ReflectHelper.set("mInsetTop", mState, ReflectHelper.newInstance<Any>("android.graphics.drawable.InsetDrawable\$InsetValue",
                            topPair.second, topPair.first))
                    ReflectHelper.set("mInsetRight", mState, ReflectHelper.newInstance<Any>("android.graphics.drawable.InsetDrawable\$InsetValue",
                            rightPair.second, rightPair.first))
                    ReflectHelper.set("mInsetBottom", mState, ReflectHelper.newInstance<Any>("android.graphics.drawable.InsetDrawable\$InsetValue",
                            bottomPair.second, bottomPair.first))
                    drawResult
                }
            }
        }
    }

    protected fun getInset(s: String?): Pair<Int, Float> = when (val fractionValue = fraction2(s)) {
        null -> Pair(dimen2(s)?.toInt() ?: 0, 0f)
        else -> Pair(0, fractionValue)
    }
}

open class LayerListResProcessor : BaseDrawResProcessor<LayerDrawable>("layer-list", true, true, ResType.DRAWABLE_LAYER_LIST) {
    override fun innerProcess2(node: Node): LayerDrawable? {
        val result = LayerDrawable(arrayOf())
        parseLayerList(node, result, this, rpm)
        return result
    }

    companion object {
        fun parseLayerList(node: Node, result: LayerDrawable, resProcessor: BaseDrawResProcessor<*>, resProcessorManager: IResProcessorManager) {
            var index = 0
            node.children.forEach {
                if (it.name == "item") {
                    val drawable = resProcessor.getDrawable(it, resProcessorManager, Attrs.LayerDrawableItem.drawable)
                            ?: return@forEach makeFail<Unit>("<item> tag requires a 'drawable' attribute or child tag defining a drawable")!!
                    var id: Int? = null
                    var width: Int? = null
                    var height: Int? = null
                    var gravity: Int? = null
                    var left: Int? = null
                    var right: Int? = null
                    var top: Int? = null
                    var bottom: Int? = null
                    var start: Int? = null
                    var end: Int? = null
                    it.attributes.forEach { attr ->
                        when (attr.name) {
                            Attrs.LayerDrawableItem.gravity.name -> gravity = resProcessor.flag(it, Attrs.LayerDrawableItem.gravity)
                                    ?: Gravity.START
                            Attrs.LayerDrawableItem.width.name -> width = resProcessor.dimen2(it[Attrs.LayerDrawableItem.width])?.toInt()
                            Attrs.LayerDrawableItem.height.name -> height = resProcessor.dimen2(it[Attrs.LayerDrawableItem.height])?.toInt()
                            Attrs.LayerDrawableItem.left.name -> left = resProcessor.dimen2(it[Attrs.LayerDrawableItem.left])?.toInt()
                            Attrs.LayerDrawableItem.right.name -> right = resProcessor.dimen2(it[Attrs.LayerDrawableItem.right])?.toInt()
                            Attrs.LayerDrawableItem.top.name -> top = resProcessor.dimen2(it[Attrs.LayerDrawableItem.top])?.toInt()
                            Attrs.LayerDrawableItem.bottom.name -> bottom = resProcessor.dimen2(it[Attrs.LayerDrawableItem.bottom])?.toInt()
                            Attrs.LayerDrawableItem.start.name -> start = resProcessor.dimen2(it[Attrs.LayerDrawableItem.start])?.toInt()
                            Attrs.LayerDrawableItem.end.name -> end = resProcessor.dimen2(it[Attrs.LayerDrawableItem.end])?.toInt()
                            Attrs.LayerDrawableItem.id.name -> id = getIntIdByStrId(it[Attrs.LayerDrawableItem.id])
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        result.addLayer(drawable)
                        if (left != null) {
                            result.setLayerInsetLeft(index, left!!)
                        }
                        if (right != null) {
                            result.setLayerInsetRight(index, right!!)
                        }
                        if (top != null) {
                            result.setLayerInsetTop(index, top!!)
                        }
                        if (bottom != null) {
                            result.setLayerInsetBottom(index, bottom!!)
                        }
                        if (start != null) {
                            result.setLayerInsetStart(index, start!!)
                        }
                        if (end != null) {
                            result.setLayerInsetEnd(index, end!!)
                        }
                        if (id != null) {
                            result.setId(index, id!!)
                        }
                        if (width != null) {
                            result.setLayerWidth(index, width!!)
                        }
                        if (height != null) {
                            result.setLayerHeight(index, height!!)
                        }
                        if (gravity != null) {
                            result.setLayerGravity(index, gravity!!)
                        }
                    } else {
                        ReflectHelper.invokeN("addLayer", result, drawable, intArrayOf(), id ?: View.NO_ID, left ?: 0, top ?: 0, right ?: 0, bottom
                                ?: 0)
                    }
                    index++
                } else {
                    makeFail<Unit>("incorrect tag in ${resProcessor.rootName}: ${it.name}")!!
                }
            }
            result.opacity = resProcessor.enum(node, Attrs.LayerDrawable.opacity) ?: PixelFormat.UNKNOWN
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val leftPadding = resProcessor.int2(node[Attrs.LayerDrawable.paddingLeft]) ?: -1
                val rightPadding = resProcessor.int2(node[Attrs.LayerDrawable.paddingRight]) ?: -1
                val topPadding = resProcessor.int2(node[Attrs.LayerDrawable.paddingTop]) ?: -1
                val bottomPadding = resProcessor.int2(node[Attrs.LayerDrawable.paddingBottom]) ?: -1
                val startPadding = resProcessor.int2(node[Attrs.LayerDrawable.paddingStart]) ?: -1
                val endPadding = resProcessor.int2(node[Attrs.LayerDrawable.paddingEnd]) ?: -1
                if (leftPadding != -1 || rightPadding != -1 || topPadding != -1 || bottomPadding != -1) {
                    result.setPadding(leftPadding, topPadding, rightPadding, bottomPadding)
                } else if (startPadding != -1 || endPadding != -1) {
                    result.setPaddingRelative(startPadding, topPadding, endPadding, bottomPadding)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                result.isAutoMirrored = resProcessor.bool2(node[Attrs.LayerDrawable.autoMirrored]) ?: false
                result.paddingMode = resProcessor.enum(node, Attrs.LayerDrawable.paddingMode) ?: LayerDrawable.PADDING_MODE_NEST
            }
        }
    }
}

open class LevelListResProcessor : BaseDrawResProcessor<LevelListDrawable>("level-list", true, true, ResType.DRAWABLE_LEVEL_LIST) {
    override fun innerProcess2(node: Node): LevelListDrawable? {
        val result = LevelListDrawable()
        node.children.forEach {
            if (it.name == "item") {
                val drawable = getDrawable(it, rpm, Attrs.LevelListDrawableItem.drawable)
                        ?: return@forEach makeFail("<item> tag requires a 'drawable' attribute or child tag defining a drawable", Unit)!!
                val low = int2(it[Attrs.LevelListDrawableItem.minLevel]) ?: 0
                val high = int2(it[Attrs.LevelListDrawableItem.maxLevel]) ?: 0
                if (high < 0) {
                    return@forEach makeFail("<item> tag requires a 'maxLevel' attribute", Unit)!!
                }
                result.addLevel(low, high, drawable)
            }
        }
        setLevel(node, result)
        return result
    }
}

open class ColorDrawableResProcessor : BaseDrawResProcessor<ColorDrawable>("color", false, true, ResType.DRAWABLE_COLOR) {
    override fun innerProcess2(node: Node): ColorDrawable? = ColorDrawable(color2(node[Attrs.ColorDrawable.color]) ?: Color.BLACK)
}

open class StateListDrawableResProcessor : BaseDrawResProcessor<StateListDrawable>("selector", true, true, ResType.DRAWABLE_SELECTOR) {
    override fun innerProcess2(node: Node): StateListDrawable? {
        val result = StateListDrawable()
        parseDrawableContainer(result, node, this)
        node.children.forEach {
            if (it.name == "item") {
                val drawable: Drawable = getDrawable(it, rpm, Attrs.StateListDrawableItem.drawable)
                        ?: return@forEach makeFail<Unit>("<item> tag requires a 'drawable' attribute or child tag defining a drawable")!!
                result.addState((ColorSelectorResProcessor.getStates(it, this, arrayOf(Attrs.StateListDrawableItem.drawable))
                        ?: return@forEach).toIntArray(), drawable)
            }
        }
        return result
    }

    companion object {
        fun parseDrawableContainer(result: StateListDrawable, node: Node, resProcessor: ResProcessor<*>) {
            val mStateListState = (result.constantState as DrawableContainer.DrawableContainerState)
            mStateListState.setVariablePadding(resProcessor.bool2(node[Attrs.StateListDrawable.variablePadding]) ?: false)
            mStateListState.isConstantSize = resProcessor.bool2(node[Attrs.StateListDrawable.constantSize]) ?: false
            mStateListState.enterFadeDuration = resProcessor.int2(node[Attrs.StateListDrawable.enterFadeDuration]) ?: 0
            mStateListState.exitFadeDuration = resProcessor.int2(node[Attrs.StateListDrawable.exitFadeDuration]) ?: 0
            result.setDither(resProcessor.bool2(node[Attrs.StateListDrawable.dither]) ?: false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                result.isAutoMirrored = resProcessor.bool2(node[Attrs.StateListDrawable.autoMirrored]) ?: false
            }
        }
    }
}
