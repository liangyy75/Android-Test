@file:Suppress("unused")

package com.liang.example.xml_inflater2

import android.content.res.ColorStateList
import android.graphics.BlendMode
import android.graphics.Insets
import android.graphics.PorterDuff
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.AnimatedStateListDrawable
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.NinePatchDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.TransitionDrawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.util.ArrayMap
import androidx.annotation.RequiresApi
import com.liang.example.basic_ktx.ReflectHelper
import com.liang.example.view_ktx.getIntIdByStrId
import java.nio.ByteBuffer
import java.nio.ByteOrder

@RequiresApi(Build.VERSION_CODES.O)
open class AdaptiveIconResProcessor : BaseDrawResProcessor<AdaptiveIconDrawable>(
        "adaptive-icon", true, true, ResType.DRAWABLE_ADAPTIVE_ICON) {
    override fun innerProcess2(node: Node): AdaptiveIconDrawable? {
        var background: Drawable? = null
        var foreground: Drawable? = null
        node.children.forEach {
            when (it.name) {
                "foreground" -> foreground = getDrawable(it, rpm, Attrs.AdaptiveIconDrawableLayer.drawable)
                        ?: return makeFail("<foreground> tag requires a 'drawable' attribute or child tag defining a drawable")
                "background" -> background = getDrawable(it, rpm, Attrs.AdaptiveIconDrawableLayer.drawable)
                        ?: return makeFail("<background> tag requires a 'drawable' attribute or child tag defining a drawable")
            }
        }
        return AdaptiveIconDrawable(background, foreground)
    }
}

// [Android：RippleDrawable 水波纹/涟漪效果](https://www.jianshu.com/p/64a825915da9)
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
open class RippleResProcessor : BaseDrawResProcessor<RippleDrawable>(
        "ripple", false, true, ResType.DRAWABLE_RIPPLE) {
    override fun innerProcess2(node: Node): RippleDrawable? {
        val colorAttr = node[Attrs.RippleDrawable.color]
                ?: return makeFail("<ripple> requires a valid color attribute")
        val colorStateList = color2(colorAttr)?.let { ColorStateList.valueOf(it) }
                ?: refer(colorAttr)?.let { ResStore.loadColorStateList(it, rpm.context, true) }
                ?: return makeFail("<ripple> requires a valid color attribute")
        val result = RippleDrawable(colorStateList, null, null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            result.radius = dimen2(node[Attrs.RippleDrawable.radius])?.toInt()
                    ?: RippleDrawable.RADIUS_AUTO
        }
        LayerListResProcessor.parseLayerList(node, result, this, rpm)
        return result
    }
}

open class TransitionResProcessor : BaseDrawResProcessor<TransitionDrawable>(
        "transition", false, true, ResType.DRAWABLE_TRANSITION) {
    override fun innerProcess2(node: Node): TransitionDrawable? {
        val result = TransitionDrawable(arrayOf())
        LayerListResProcessor.parseLayerList(node, result, this, rpm)
        return result
    }
}

open class AnimatedRotateResProcessor : BaseDrawResProcessor</*AnimatedRotateDrawable*/ Drawable>(
        "animated-rotate", false, true, ResType.DRAWABLE_ANIMATED_ROTATE) {
    override fun innerProcess2(node: Node): /*AnimatedRotateDrawable*/ Drawable? {
        val result = ReflectHelper.newInstance<Drawable>("android.graphics.drawable.AnimatedRotateDrawable")
                ?: return null
        val pivotXAttr = node[Attrs.AnimatedRotateDrawable.pivotX]
        val mState = ReflectHelper.get("mState", result) ?: return null
        if (pivotXAttr != null) {
            val fractionValue = fraction2(pivotXAttr)
            if (fractionValue != null) {
                ReflectHelper.setBoolean("mPivotXRel", mState, true)
                ReflectHelper.setFloat("mPivotX", mState, fractionValue)
            } else {
                ReflectHelper.setBoolean("mPivotXRel", mState, false)
                ReflectHelper.setFloat("mPivotX", mState, float2(pivotXAttr) ?: 0.5f)
            }
        }
        val pivotYAttr = node[Attrs.AnimatedRotateDrawable.pivotY]
        if (pivotYAttr != null) {
            val fractionValue = fraction2(pivotYAttr)
            if (fractionValue != null) {
                ReflectHelper.setBoolean("mPivotYRel", mState, true)
                ReflectHelper.setFloat("mPivotY", mState, fractionValue)
            } else {
                ReflectHelper.setBoolean("mPivotYRel", mState, false)
                ReflectHelper.setFloat("mPivotY", mState, float2(pivotYAttr) ?: 0.5f)
            }
        }
        ReflectHelper.invokeN("setFramesCount", result, int2(node[Attrs.AnimatedRotateDrawable.frameDuration])
                ?: 150)
        ReflectHelper.setInt("mFrameDuration", mState, int2(node[Attrs.AnimatedRotateDrawable.framesCount])
                ?: 12)
        return result
    }
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
open class AnimatedStateListResProcessor : BaseDrawResProcessor<AnimatedStateListDrawable>(
        "animated-selector", true, true, ResType.DRAWABLE_ANIMATED_SELECTOR) {
    override fun innerProcess2(node: Node): AnimatedStateListDrawable? {
        val result = AnimatedStateListDrawable()
        StateListDrawableResProcessor.parseDrawableContainer(result, node, this)
        val mState = ReflectHelper.get("mState", result)
        node.children.forEach {
            when (it.name) {
                "item" -> {
                    val drawable: Drawable = getDrawable(it, rpm, Attrs.StateListDrawableItem.drawable)
                            ?: return@forEach makeFail<Unit>("<item> tag requires a 'drawable' attribute or child tag defining a drawable")!!
                    result.addState((ColorSelectorResProcessor.getStates(it, this, arrayOf(Attrs.StateListDrawableItem.drawable))
                            ?: return@forEach).toIntArray(), drawable, getIntIdByStrId(it[Attrs.AnimatedStateListDrawableItem.id]))
                }
                "transition" -> {
                    val drawable: Drawable = getDrawable(it, rpm, Attrs.StateListDrawableItem.drawable)
                            ?: return@forEach makeFail<Unit>("<item> tag requires a 'drawable' attribute or child tag defining a drawable")!!
                    ReflectHelper.invokeN("addTransition", mState, getIntIdByStrId(it[Attrs.AnimatedStateListDrawableTransition.fromId]),
                            getIntIdByStrId(it[Attrs.AnimatedStateListDrawableTransition.toId]), drawable,
                            bool2(it[Attrs.AnimatedStateListDrawableTransition.reversible])
                                    ?: false)
                }
            }
        }
        return result
    }
}

// TODO: 华为手机不认同 😭
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
open class VectorResProcessor : BaseDrawResProcessor<VectorDrawable>(
        "vector", true, true, ResType.DRAWABLE_VECTOR) {
    override fun innerProcess2(node: Node): VectorDrawable? {
        val result = VectorDrawable()

        val mVectorState = ReflectHelper.get("mVectorState", result)
                ?: return makeFail("no mVectorState field in VectorDrawable, why?")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val tintColor = color2(node[Attrs.VectorDrawable.tint])
            if (tintColor != null) {
                result.setTint(tintColor)
            } else {
                val tint = refer(node[Attrs.VectorDrawable.tint])?.let { ResStore.loadColorStateList(it, apm.context, true) }
                if (tint != null) {
                    result.setTintList(tint)
                }
            }
        }
        val tintMode = int2(node[Attrs.VectorDrawable.tintMode]) ?: -1
        if (tintMode != -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ReflectHelper.set("mBlendMode", mVectorState,
                        ReflectHelper.invoke<Any>("parseBlendMode", Drawable::class.java, tintMode, BlendMode.SRC_IN))
            } else {
                ReflectHelper.set("mTintMode", mVectorState,
                        ReflectHelper.invoke<Any>("parseTintMode", Drawable::class.java, tintMode, PorterDuff.Mode.SRC_IN))
            }
        }
        result.isAutoMirrored = bool2(node[Attrs.VectorDrawable.autoMirrored]) ?: false
        val mBaseWidth = dimen2(node[Attrs.VectorDrawable.width])?.toInt() ?: 0
        if (mBaseWidth > 0) {
            ReflectHelper.setInt("mBaseWidth", mVectorState, mBaseWidth)
        } else {
            return makeFail("<vector> tag requires width > 0")
        }
        val mBaseHeight = dimen2(node[Attrs.VectorDrawable.height])?.toInt() ?: 0
        if (mBaseHeight > 0) {
            ReflectHelper.setInt("mBaseHeight", mVectorState, mBaseHeight)
        } else {
            return makeFail("<vector> tag requires height > 0")
        }
        // ReflectHelper.invokeN("setViewportSize", mVectorState, float2(node[Attrs.VectorDrawable.viewportWidth]) ?: 0f,
        //         float2(node[Attrs.VectorDrawable.viewportHeight]) ?: 0f)
        ReflectHelper.findMethod("setViewportSize", mVectorState::class.java, Float::class.java, Float::class.java)!!
                .invoke(mVectorState, float2(node[Attrs.VectorDrawable.viewportWidth])
                        ?: 0f, float2(node[Attrs.VectorDrawable.viewportHeight]) ?: 0f)
        ReflectHelper.invokeN("setAlpha", mVectorState, float2(node[Attrs.VectorDrawable.alpha])
                ?: ReflectHelper.invoke<Float>("getAlpha", mVectorState))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ReflectHelper.set("mOpticalInsets", mVectorState, Insets.of(
                    dimen2(node[Attrs.GradientDrawable.opticalInsetLeft])?.toInt()
                            ?: 0, dimen2(node[Attrs.GradientDrawable.opticalInsetTop])?.toInt()
                    ?: 0,
                    dimen2(node[Attrs.GradientDrawable.opticalInsetRight])?.toInt()
                            ?: 0, dimen2(node[Attrs.GradientDrawable.opticalInsetBottom])?.toInt()
                    ?: 0))
        } else {
            ReflectHelper.set("mOpticalInsets", mVectorState, ShapeResProcessor.ofInsets!!.invoke(null,
                    dimen2(node[Attrs.GradientDrawable.opticalInsetLeft])?.toInt()
                            ?: 0, dimen2(node[Attrs.GradientDrawable.opticalInsetTop])?.toInt()
                    ?: 0,
                    dimen2(node[Attrs.GradientDrawable.opticalInsetRight])?.toInt()
                            ?: 0, dimen2(node[Attrs.GradientDrawable.opticalInsetBottom])?.toInt()
                    ?: 0))
        }
        val name = node[Attrs.VectorDrawable.name]
        if (name != null) {
            ReflectHelper.set("mRootName", mVectorState, name)
            (ReflectHelper.get("mVGTargetsMap", mVectorState) as? ArrayMap<String, Any?>)?.put(name, mVectorState)
        }

        var noPathTag = true
        val currentGroup = ReflectHelper.get("mRootGroup", mVectorState)
                ?: return makeFail("no field mRootGroup in VectorDrawable, why?")
        val mVGTargetsMap = ReflectHelper.get("mVGTargetsMap", mVectorState) as? ArrayMap<String, Any?>
                ?: return makeFail("no field mVGTargetsMap in VectorDrawable, why?")
        noPathTag = processGroupChild(node, noPathTag, currentGroup, mVGTargetsMap)
        if (noPathTag) {
            return makeFail("no path tag defined")
        }
        return result
    }

    open fun processGroupChild(node: Node, noPathTag: Boolean, currentGroup: Any, mVGTargetsMap: ArrayMap<String, Any?>): Boolean {
        var name: String? = ""
        var noPathTag2 = noPathTag
        node.children.forEach {
            name = it[Attrs.VectorDrawable.name]
            val child = when (it.name) {
                "path" -> {
                    val temp = ReflectHelper.newInstance<Any>("android.graphics.drawable.VectorDrawable\$VFullPath")
                            ?: return@forEach makeFail("create VFullPath failed", Unit)!!
                    val mNativePtr = ReflectHelper.getLong("mNativePtr", temp)
                    processClipPath(temp, name, it, mNativePtr)
                    var mPropertyData = ReflectHelper.get("mPropertyData", temp) as? ByteArray
                    if (mPropertyData == null) {
                        mPropertyData = ByteArray(48)
                        ReflectHelper.set("mPropertyData", temp, mPropertyData)
                    }
                    if (nGetFullPathProperties!!.invoke(null, mNativePtr, mPropertyData, 48) as? Boolean != true) {
                        throw RuntimeException("Error: inconsistent property count")
                    }

                    val properties = ByteBuffer.wrap(mPropertyData)
                    properties.order(ByteOrder.nativeOrder())
                    val strokeWidth = float2(it[Attrs.VectorDrawablePath.strokeWidth])
                            ?: properties.getFloat(0 * 4)
                    val strokeColor = color2(it[Attrs.VectorDrawablePath.strokeColor])
                            ?: properties.getInt(1 * 4)
                    val strokeAlpha = float2(it[Attrs.VectorDrawablePath.strokeAlpha])
                            ?: properties.getFloat(2 * 4)

                    val fillColor = color2(it[Attrs.VectorDrawablePath.fillColor])
                            ?: properties.getInt(3 * 4)
                    val fillAlpha = float2(it[Attrs.VectorDrawablePath.fillAlpha])
                            ?: properties.getFloat(4 * 4)
                    val fillType = int2(it[Attrs.VectorDrawablePath.fillType])
                            ?: properties.getInt(11 * 4)

                    val trimPathStart = float2(it[Attrs.VectorDrawablePath.trimPathStart])
                            ?: properties.getFloat(5 * 4)
                    val trimPathEnd = float2(it[Attrs.VectorDrawablePath.trimPathEnd])
                            ?: properties.getFloat(6 * 4)
                    val trimPathOffset = float2(it[Attrs.VectorDrawablePath.trimPathOffset])
                            ?: properties.getFloat(7 * 4)

                    val strokeLineCap = int2(it[Attrs.VectorDrawablePath.strokeLineCap])
                            ?: properties.getInt(8 * 4)
                    val strokeLineJoin = int2(it[Attrs.VectorDrawablePath.strokeLineJoin])
                            ?: properties.getInt(9 * 4)
                    val strokeMiterLimit = float2(it[Attrs.VectorDrawablePath.strokeMiterLimit])
                            ?: properties.getFloat(10 * 4)

                    // Shader就不考虑了
                    nUpdateFullPathFillGradient!!.invoke(null, mNativePtr, 0L)
                    nUpdateFullPathStrokeGradient!!.invoke(null, mNativePtr, 0L)
                    nUpdateFullPathProperties!!.invoke(null, mNativePtr, strokeWidth, strokeColor, strokeAlpha,
                            fillColor, fillAlpha, trimPathStart, trimPathEnd, trimPathOffset,
                            strokeMiterLimit, strokeLineCap, strokeLineJoin, fillType)

                    noPathTag2 = false
                    temp
                }
                "clip-path" -> {
                    val temp = ReflectHelper.newInstance<Any>("android.graphics.drawable.VectorDrawable\$VClipPath")
                            ?: return@forEach makeFail("create VClipPath failed", Unit)!!
                    processClipPath(temp, name, it, ReflectHelper.getLong("mNativePtr", temp))
                    temp
                }
                "group" -> {
                    val temp = processGroup(it, noPathTag2, mVGTargetsMap)
                            ?: return@forEach makeFail("VGroup create failed", Unit)!!
                    noPathTag2 = temp.second
                    temp.first
                }
                else -> return@forEach makeFail("incorrect tag name: ${it.name}", Unit)!!
            }
            addChild!!.invoke(currentGroup, child)
            if (name != null) {
                mVGTargetsMap[name] = child;
            }
        }
        return noPathTag2
    }

    open fun processClipPath(temp: Any, name: String?, node: Node, mNativePtr: Long) {
        if (name != null) {
            mPathName!!.set(temp, name)
            nSetName!!.invoke(null, mNativePtr, name)
        }
        node[Attrs.VectorDrawablePath.pathData]?.let { pathDataString: String ->
            mPathData!!.set(temp, ReflectHelper.newInstance("android.util.PathParser\$PathData", pathDataString))
            nSetPathString!!.invoke(null, mNativePtr, pathDataString, pathDataString.length)
        }
    }

    open fun processGroup(node: Node, noPathTag: Boolean, mVGTargetsMap: ArrayMap<String, Any?>): Pair<Any, Boolean>? {
        val temp = ReflectHelper.newInstance<Any>("android.graphics.drawable.VectorDrawable\$VGroup")
                ?: return makeFail("VGroup create failed")
        var mTransform = ReflectHelper.get("mTransform", temp) as? FloatArray
        if (mTransform == null) {
            mTransform = FloatArray(7)
            ReflectHelper.set("mTransform", temp, mTransform)
        }
        val mNativePtr = ReflectHelper.getLong("mNativePtr", temp)
        if (nGetGroupProperties!!.invoke(null, mNativePtr, mTransform, 7) as? Boolean != true) {
            throw java.lang.RuntimeException("Error: inconsistent property count")
        }
        val rotate = float2(node[Attrs.VectorDrawableGroup.rotation]) ?: mTransform[0]
        val pivotX = float2(node[Attrs.VectorDrawableGroup.pivotX]) ?: mTransform[1]
        val pivotY = float2(node[Attrs.VectorDrawableGroup.pivotY]) ?: mTransform[2]
        val scaleX = float2(node[Attrs.VectorDrawableGroup.scaleX]) ?: mTransform[3]
        val scaleY = float2(node[Attrs.VectorDrawableGroup.scaleY]) ?: mTransform[4]
        val translateX = float2(node[Attrs.VectorDrawableGroup.translateX]) ?: mTransform[5]
        val translateY = float2(node[Attrs.VectorDrawableGroup.translateY]) ?: mTransform[6]
        val name = node[Attrs.VectorDrawablePath.name]
        if (name != null) {
            mGroupName!!.set(temp, name)
            nSetName!!.invoke(null, mNativePtr, name)
        }
        nUpdateGroupProperties!!.invoke(null, mNativePtr, rotate, pivotX, pivotY, scaleX, scaleY, translateX, translateY)
        return Pair(temp, processGroupChild(node, noPathTag, temp, mVGTargetsMap))
    }

    companion object {
        val mPathName = ReflectHelper.findField("mPathName", ReflectHelper.findCls("android.graphics.drawable.VectorDrawable\$VPath"))
        val mPathData = ReflectHelper.findField("mPathData", ReflectHelper.findCls("android.graphics.drawable.VectorDrawable\$VPath"))
        val mGroupName = ReflectHelper.findField("mGroupName", ReflectHelper.findCls("android.graphics.drawable.VectorDrawable\$VGroup"))

        val addChild = ReflectHelper.findMethod("addChild", ReflectHelper.findCls("android.graphics.drawable.VectorDrawable\$VGroup"),
                ReflectHelper.findCls("android.graphics.drawable.VectorDrawable\$VObject"))

        val nSetName = ReflectHelper.findMethod("nSetName", VectorDrawable::class.java, Long::class.java, String::class.java)
        val nSetPathString = ReflectHelper.findMethod("nSetPathString", VectorDrawable::class.java, Long::class.java, String::class.java,
                Int::class.java)
        val nGetFullPathProperties = ReflectHelper.findMethod("nGetFullPathProperties", VectorDrawable::class.java, Long::class.java,
                ByteArray::class.java, Int::class.java)

        val nUpdateFullPathFillGradient = ReflectHelper.findMethod("nUpdateFullPathFillGradient", VectorDrawable::class.java, Long::class.java,
                Long::class.java)
        val nUpdateFullPathStrokeGradient = ReflectHelper.findMethod("nUpdateFullPathStrokeGradient", VectorDrawable::class.java, Long::class.java,
                Long::class.java)
        val nUpdateFullPathProperties = ReflectHelper.findMethod("nUpdateFullPathProperties", VectorDrawable::class.java, Long::class.java,
                Float::class.java, Int::class.java, Float::class.java, Int::class.java, Float::class.java, Float::class.java, Float::class.java,
                Float::class.java, Float::class.java, Int::class.java, Int::class.java, Int::class.java)

        val nGetGroupProperties = ReflectHelper.findMethod("nGetGroupProperties", VectorDrawable::class.java, Long::class.java,
                FloatArray::class.java, Int::class.java)
        val nUpdateGroupProperties = ReflectHelper.findMethod("nUpdateGroupProperties", VectorDrawable::class.java, Long::class.java,
                Float::class.java, Float::class.java, Float::class.java, Float::class.java, Float::class.java, Float::class.java, Float::class.java)
    }
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
open class AnimatedVectorResProcessor : BaseDrawResProcessor<AnimatedVectorDrawable>(
        "animated-vector", true, true, ResType.DRAWABLE_ANIMATED_VECTOR) {
    override fun innerProcess2(node: Node): AnimatedVectorDrawable? {
        val result = AnimatedVectorDrawable()
        val mAnimatedVectorState = ReflectHelper.get("mAnimatedVectorState", result)
                ?: return makeFail("mAnimatedVectorState isn't a field in AnimatedVectorDrawable, why?")
        val vectorDrawable = getDrawable(node, rpm, Attrs.AnimatedVectorDrawable.drawable) as? VectorDrawable
                ?: return makeFail("AnimatedVectorDrawable need a vector drawable")
        vectorDrawable.callback = ReflectHelper.get("mCallback", result) as? Drawable.Callback
        ReflectHelper.invokeN("setAllowCaching", vectorDrawable, false)
        val pathErrorScale = ReflectHelper.invoke<Float>("getPixelSize", vectorDrawable) ?: -1
        (ReflectHelper.get("mVectorDrawable", mAnimatedVectorState) as? Drawable)?.callback = null
        ReflectHelper.set("mVectorDrawable", mAnimatedVectorState, vectorDrawable)

        node.children.forEach { target ->
            if (target.name == "target") {
                val animator = ResStore.loadAnimator(refer(node[Attrs.AnimatedVectorDrawableTarget.animation])
                        ?: return@forEach, apm.context, true)
                val name = str2(node[Attrs.AnimatedVectorDrawableTarget.name])
                ReflectHelper.invokeS<Unit>("updateAnimatorProperty", AnimatedVectorResProcessor::class.java, animator,
                        name, vectorDrawable, ReflectHelper.getBoolean("mShouldIgnoreInvalidAnim", mAnimatedVectorState))
                ReflectHelper.invokeN("addTargetAnimator", mAnimatedVectorState, name, animator)
            } else makeFail("incorrect tag name: ${target.name}", Unit)!!
        }
        return result
    }
}

open class AnimatedImageResProcessor : BaseDrawResProcessor<AnimatedImageDrawable>(
        "animated-image", true, true, ResType.DRAWABLE_ANIMATED_IMAGE) {
    override fun innerProcess2(node: Node): AnimatedImageDrawable? {
        Attrs.AnimatedImageDrawable.autoMirrored
        Attrs.AnimatedImageDrawable.autoStart
        Attrs.AnimatedImageDrawable.repeatCount
        Attrs.AnimatedImageDrawable.src
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
} // TODO

open class BitmapResProcessor : BaseDrawResProcessor<BitmapDrawable>(
        "bitmap", false, true, ResType.DRAWABLE_BITMAP) {
    override fun innerProcess2(node: Node): BitmapDrawable? {
        Attrs.BitmapDrawable.alpha
        Attrs.BitmapDrawable.antialias
        Attrs.BitmapDrawable.filter
        Attrs.BitmapDrawable.dither
        Attrs.BitmapDrawable.gravity
        Attrs.BitmapDrawable.tileMode
        Attrs.BitmapDrawable.tileModeX
        Attrs.BitmapDrawable.tileModeY
        Attrs.BitmapDrawable.mipMap
        Attrs.BitmapDrawable.autoMirrored
        Attrs.BitmapDrawable.tint
        Attrs.BitmapDrawable.tintMode
        Attrs.BitmapDrawable.alpha
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
} // TODO

open class DrawableResProcessor : BaseDrawResProcessor<Drawable>(
        "drawable", false, true, ResType.DRAWABLE_DRAWABLE) {
    override fun innerProcess2(node: Node): Drawable? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
} // TODO: 这个tag意义在哪？

// -- 隔离 -- ，下面的东西太奇怪了😭

// Android虽然可以使用Java代码创建NinePatchDrawable，但是极少情况才这样做，因为Android SDK在编译工程时会对点九图片进行编译，形成特殊格式的图片。
// 使用代码创建时只能针对编译过的点九图片，而没有编译过的都被当作BitmapDrawable对待。
open class NinePatchResProcessor : BaseDrawResProcessor<NinePatchDrawable>(
        "nine-patch", false, true, ResType.DRAWABLE_NINE_PATCH) {
    override fun innerProcess2(node: Node): NinePatchDrawable? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
} // TODO: ?

// shape 呢?
open class GradientDrawableResProcessor : BaseDrawResProcessor<GradientDrawable>(
        "gradient", false, true, ResType.DRAWABLE_GRADIENT) {
    override fun innerProcess2(node: Node): GradientDrawable? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
} // TODO: ?

// 根本没有这种drawable
open class MaskableIconResProcessor : BaseDrawResProcessor<AdaptiveIconDrawable>(
        "maskable-icon", true, true, ResType.DRAWABLE_MASKABLE_ICON) {
    override fun innerProcess2(node: Node): AdaptiveIconDrawable? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
} // TODO: ?
