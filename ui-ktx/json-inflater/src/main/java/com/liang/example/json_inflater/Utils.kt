@file:Suppress("MemberVisibilityCanBePrivate")

package com.liang.example.json_inflater

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnticipateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.CycleInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.view.animation.PathInterpolator
import android.view.animation.RotateAnimation
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.ImageView.ScaleType
import android.widget.LinearLayout
import android.widget.RelativeLayout

var debug = false
const val TAG = "NUtils"

fun parseBoolean(value: Value?): Boolean =
        value != null && value is PrimitiveV && value.toBoolean()
                || value != null && value !is NullV && value.string().toBoolean()

fun getAndroidXmlResId(fullResIdStr: String?): Int = when (fullResIdStr) {
    null -> View.NO_ID
    else -> {
        val i = fullResIdStr.indexOf("/")
        when {
            i >= 0 -> getResId(fullResIdStr.substring(i + 1), android.R.id::class.java)
            else -> View.NO_ID
        }
    }
}

fun getResId(variableName: String, clazz: Class<*>): Int = try {
    clazz.getField(variableName).getInt(null)
} catch (e: Exception) {
    e.printStackTrace()
    0
}

fun parseRelativeLayoutBoolean(value: Boolean): Int = if (value) RelativeLayout.TRUE else 0

fun addRelativeLayoutRule(view: View, verb: Int, anchor: Int) {
    val layoutParams = view.layoutParams
    if (layoutParams is RelativeLayout.LayoutParams) {
        layoutParams.addRule(verb, anchor)
        view.layoutParams = layoutParams
    } else if (debug) {
        Log.e(TAG, "cannot add relative layout rules when container is not relative")
    }
}

@SuppressLint("RtlHardcoded")
object Util {
    const val CENTER = "center"
    const val CENTER_HORIZONTAL = "center_horizontal"
    const val CENTER_VERTICAL = "center_vertical"
    const val LEFT = "left"
    const val RIGHT = "right"
    const val TOP = "top"
    const val BOTTOM = "bottom"
    const val START = "start"
    const val END = "end"
    const val MIDDLE = "middle"
    const val BEGINNING = "beginning"
    const val MARQUEE = "marquee"

    const val VISIBLE = "visible"
    const val INVISIBLE = "invisible"
    const val GONE = "gone"

    const val BOLD = "bold"
    const val ITALIC = "italic"
    const val BOLD_ITALIC = "bold|italic"

    const val TEXT_ALIGNMENT_INHERIT = "inherit"
    const val TEXT_ALIGNMENT_GRAVITY = "gravity"
    const val TEXT_ALIGNMENT_CENTER = "center"
    const val TEXT_ALIGNMENT_TEXT_START = "start"
    const val TEXT_ALIGNMENT_TEXT_END = "end"
    const val TEXT_ALIGNMENT_VIEW_START = "viewStart"
    const val TEXT_ALIGNMENT_VIEW_END = "viewEnd"

    val sGravityMap: MutableMap<String, PrimitiveV> = HashMap()
    val sVisibilityMap: MutableMap<Int, PrimitiveV> = HashMap()
    val sVisibilityMode: MutableMap<String, Int> = HashMap()
    val sDividerMode: MutableMap<String, Int> = HashMap()
    val sEllipsizeMode: MutableMap<String, Enum<*>> = HashMap()
    val sTextAlignment: MutableMap<String, Int> = HashMap()
    val sImageScaleType: MutableMap<String, ScaleType> = HashMap()

    init {
        sGravityMap[CENTER] = PrimitiveV(Gravity.CENTER)
        sGravityMap[CENTER_HORIZONTAL] = PrimitiveV(Gravity.CENTER_HORIZONTAL)
        sGravityMap[CENTER_VERTICAL] = PrimitiveV(Gravity.CENTER_VERTICAL)
        sGravityMap[LEFT] = PrimitiveV(Gravity.LEFT)
        sGravityMap[RIGHT] = PrimitiveV(Gravity.RIGHT)
        sGravityMap[TOP] = PrimitiveV(Gravity.TOP)
        sGravityMap[BOTTOM] = PrimitiveV(Gravity.BOTTOM)
        sGravityMap[START] = PrimitiveV(Gravity.START)
        sGravityMap[END] = PrimitiveV(Gravity.END)

        sVisibilityMap[View.VISIBLE] = PrimitiveV(View.VISIBLE)
        sVisibilityMap[View.INVISIBLE] = PrimitiveV(View.INVISIBLE)
        sVisibilityMap[View.GONE] = PrimitiveV(View.GONE)

        sVisibilityMode[VISIBLE] = View.VISIBLE
        sVisibilityMode[INVISIBLE] = View.INVISIBLE
        sVisibilityMode[GONE] = View.GONE

        sDividerMode[END] = LinearLayout.SHOW_DIVIDER_END
        sDividerMode[MIDDLE] = LinearLayout.SHOW_DIVIDER_MIDDLE
        sDividerMode[BEGINNING] = LinearLayout.SHOW_DIVIDER_BEGINNING

        sEllipsizeMode[END] = TextUtils.TruncateAt.END
        sEllipsizeMode[START] = TextUtils.TruncateAt.START
        sEllipsizeMode[MARQUEE] = TextUtils.TruncateAt.MARQUEE
        sEllipsizeMode[MIDDLE] = TextUtils.TruncateAt.MIDDLE

        sImageScaleType[CENTER] = ScaleType.CENTER
        sImageScaleType["center_crop"] = ScaleType.CENTER_CROP
        sImageScaleType["center_inside"] = ScaleType.CENTER_INSIDE
        sImageScaleType["fitCenter"] = ScaleType.FIT_CENTER
        sImageScaleType["fit_xy"] = ScaleType.FIT_XY
        sImageScaleType["matrix"] = ScaleType.MATRIX

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            sTextAlignment[TEXT_ALIGNMENT_INHERIT] = View.TEXT_ALIGNMENT_INHERIT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            sTextAlignment[TEXT_ALIGNMENT_GRAVITY] = View.TEXT_ALIGNMENT_GRAVITY
            sTextAlignment[TEXT_ALIGNMENT_CENTER] = View.TEXT_ALIGNMENT_CENTER
            sTextAlignment[TEXT_ALIGNMENT_TEXT_START] = View.TEXT_ALIGNMENT_TEXT_START
            sTextAlignment[TEXT_ALIGNMENT_TEXT_END] = View.TEXT_ALIGNMENT_TEXT_END
            sTextAlignment[TEXT_ALIGNMENT_VIEW_START] = View.TEXT_ALIGNMENT_VIEW_START
            sTextAlignment[TEXT_ALIGNMENT_VIEW_END] = View.TEXT_ALIGNMENT_VIEW_END
        }
    }

    fun parseGravity(value: String): Int {
        val gravities = value.split("\\|").toTypedArray()
        var returnGravity = Gravity.NO_GRAVITY
        for (gravity in gravities) {
            val gravityValue = sGravityMap[gravity]
            if (null != gravityValue) {
                returnGravity = returnGravity or gravityValue.toInt()
            }
        }
        return returnGravity
    }

    fun getGravity(value: String): PrimitiveV? {
        return PrimitiveV(parseGravity(value))
    }

    fun parseVisibility(value: Value?): Int {
        var returnValue: Int? = null
        if (null != value && value is PrimitiveV) {
            val attributeValue = value.string()
            returnValue = sVisibilityMode[attributeValue]
            if (null == returnValue && (attributeValue.isEmpty() || "false" == attributeValue || "null" == attributeValue)) {
                returnValue = View.GONE
            }
        } else if (value is NullV) {
            returnValue = View.GONE
        }
        return returnValue ?: View.VISIBLE
    }

    fun getVisibility(visibility: Int): PrimitiveV? = sVisibilityMap[visibility] ?: sVisibilityMap[View.GONE]
}

object AnimationUtils {
    const val TAG = "AnimationUtils"
    const val LINEAR_INTERPOLATOR = "linearInterpolator"
    const val ACCELERATE_INTERPOLATOR = "accelerateInterpolator"
    const val DECELERATE_INTERPOLATOR = "decelerateInterpolator"
    const val ACCELERATE_DECELERATE_INTERPOLATOR = "accelerateDecelerateInterpolator"
    const val CYCLE_INTERPOLATOR = "cycleInterpolator"
    const val ANTICIPATE_INTERPOLATOR = "anticipateInterpolator"
    const val OVERSHOOT_INTERPOLATOR = "overshootInterpolator"
    const val ANTICIPATE_OVERSHOOT_INTERPOLATOR = "anticipateOvershootInterpolator"
    const val BOUNCE_INTERPOLATOR = "bounceInterpolator"
    const val PATH_INTERPOLATOR = "pathInterpolator"
    const val TYPE = "type"
    const val SET = "set"
    const val ALPHA = "alpha"
    const val SCALE = "scale"
    const val ROTATE = "rotate"
    const val TRANSLATE = "translate"
    const val PERCENT_SELF = "%"
    const val PERCENT_RELATIVE_PARENT = "%p"

    const val TWEEN_LOCAL_RESOURCE_STR = "@anim/"

    fun isTweenAnimationResource(attributeValue: String): Boolean = attributeValue.startsWith(TWEEN_LOCAL_RESOURCE_STR)

    /**
     * Loads an [Animation] object from a resource
     *
     * @param context Application context used to access resources
     * @param value   JSON representation of the Animation
     * @return The animation object reference by the specified id
     * @throws android.content.res.Resources.NotFoundException when the animation cannot be loaded
     */
    @Throws(Resources.NotFoundException::class)
    fun loadAnimation(context: Context, value: Value?): Animation? = when (value) {
        is PrimitiveV -> handleString(context, value.string())
        is ObjectV -> handleElement(context, value)
        else -> {
            if (debug) {
                Log.e(TAG, "Could not load animation for : $value")
            }
            null
        }
    }

    fun handleString(context: Context, value: String): Animation? {
        var anim: Animation? = null
        if (isTweenAnimationResource(value)) {
            try {
                anim = android.view.animation.AnimationUtils.loadAnimation(context, context.resources.getIdentifier(value, "anim", context.packageName))
            } catch (ex: java.lang.Exception) {
                if (debug) {
                    Log.d(TAG, "Could not load local resource $value")
                }
            }
        }
        return anim
    }

    fun handleElement(context: Context, value: ObjectV): Animation? {
        val type = value.getString(TYPE)
        return when {
            SET.equals(type, ignoreCase = true) -> AnimationSetProperties(value)
            ALPHA.equals(type, ignoreCase = true) -> AlphaAnimProperties(value)
            SCALE.equals(type, ignoreCase = true) -> ScaleAnimProperties(value)
            ROTATE.equals(type, ignoreCase = true) -> RotateAnimProperties(value)
            TRANSLATE.equals(type, ignoreCase = true) -> TranslateAnimProperties(value)
            else -> null
        }?.instantiate(context)
    }

    /**
     * Loads an [Interpolator] object from a resource
     *
     * @param context Application context used to access resources
     * @param value   Json representation of the Interpolator
     * @return The animation object reference by the specified id
     * @throws android.content.res.Resources.NotFoundException
     */
    @Throws(Resources.NotFoundException::class)
    fun loadInterpolator(context: Context, value: Value): Interpolator? = when (value) {
        is PrimitiveV -> handleStringInterpolator(context, value.string())
        is ObjectV -> handleElementInterpolator(context, value)
        else -> {
            if (debug) {
                Log.e(TAG, "Could not load interpolator for : $value")
            }
            null
        }
    }

    fun handleStringInterpolator(context: Context, value: String): Interpolator? {
        var interpolator: Interpolator? = null
        if (isTweenAnimationResource(value)) {
            try {
                interpolator = android.view.animation.AnimationUtils.loadInterpolator(context, context.resources.getIdentifier(value, "anim", context.packageName))
            } catch (ex: java.lang.Exception) {
                if (debug) {
                    Log.d(TAG, "Could not load local resource $value")
                }
            }
        }
        return interpolator
    }

    fun handleElementInterpolator(context: Context, value: ObjectV): Interpolator? {
        var interpolator: Interpolator? = null
        val type = value.getString("type")
        var interpolatorProperties: InterpolatorProperties? = null
        when {
            LINEAR_INTERPOLATOR.equals(type, ignoreCase = true) -> interpolator = LinearInterpolator()
            ACCELERATE_INTERPOLATOR.equals(type, ignoreCase = true) -> interpolator = AccelerateInterpolator()
            DECELERATE_INTERPOLATOR.equals(type, ignoreCase = true) -> interpolator = DecelerateInterpolator()
            ACCELERATE_DECELERATE_INTERPOLATOR.equals(type, ignoreCase = true) -> interpolator = AccelerateDecelerateInterpolator()
            CYCLE_INTERPOLATOR.equals(type, ignoreCase = true) -> interpolatorProperties = CycleInterpolatorProperties(value)
            ANTICIPATE_INTERPOLATOR.equals(type, ignoreCase = true) -> interpolatorProperties = AnticipateInterpolatorProperties(value)
            OVERSHOOT_INTERPOLATOR.equals(type, ignoreCase = true) -> interpolatorProperties = OvershootInterpolatorProperties(value)
            ANTICIPATE_OVERSHOOT_INTERPOLATOR.equals(type, ignoreCase = true) -> interpolatorProperties = AnticipateOvershootInterpolatorProperties(value)
            BOUNCE_INTERPOLATOR.equals(type, ignoreCase = true) -> interpolator = BounceInterpolator()
            PATH_INTERPOLATOR.equals(type, ignoreCase = true) -> interpolatorProperties = PathInterpolatorProperties(value)
            else -> {
                if (debug) {
                    Log.e(TAG, "Unknown interpolator name: $type")
                }
                throw RuntimeException("Unknown interpolator name: $type")
            }
        }
        if (null != interpolatorProperties) {
            interpolator = interpolatorProperties.createInterpolator(context)
        }
        return interpolator
    }

    /**
     * Utility class to parse a string description of a size.
     */
    open class Description {
        /**
         * One of Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, or
         * Animation.RELATIVE_TO_PARENT.
         */
        var type = 0
        /**
         * The absolute or relative dimension for this Description.
         */
        var value = 0f

        companion object {
            /**
             * Size descriptions can appear in three forms:
             *
             *  1. An absolute size. This is represented by a number.
             *  1. A size relative to the size of the object being animated. This
             * is represented by a number followed by "%". *
             *  1. A size relative to the size of the parent of object being
             * animated. This is represented by a number followed by "%p".
             *
             *
             * @param value The Json value to parse
             * @return The parsed version of the description
             */
            fun parseValue(value: Value?): Description {
                val d = Description()
                d.type = Animation.ABSOLUTE
                d.value = 0f
                if (value != null && value is PrimitiveV) {
                    if (value.isNumber()) {
                        d.type = Animation.ABSOLUTE
                        d.value = value.toFloat()
                    } else {
                        var stringValue = value.string()
                        when {
                            stringValue.endsWith(PERCENT_SELF) -> {
                                stringValue = stringValue.substring(0, stringValue.length - PERCENT_SELF.length)
                                d.value = stringValue.toFloat() / 100
                                d.type = Animation.RELATIVE_TO_SELF
                            }
                            stringValue.endsWith(PERCENT_RELATIVE_PARENT) -> {
                                stringValue = stringValue.substring(0, stringValue.length - PERCENT_RELATIVE_PARENT.length)
                                d.value = stringValue.toFloat() / 100
                                d.type = Animation.RELATIVE_TO_PARENT
                            }
                            else -> {
                                d.type = Animation.ABSOLUTE
                                d.value = value.toFloat()
                            }
                        }
                    }
                }
                return d
            }
        }
    }

    abstract class AnimationProperties(value: ObjectV) {
        open val detachWallpaper = value.getBoolean(DETACH_WALLPAPER)
        open val duration = value.getLong(DURATION)
        open val fillAfter = value.getBoolean(FILL_AFTER)
        open val fillBefore = value.getBoolean(FILL_BEFORE)
        open val fillEnabled = value.getBoolean(FILL_ENABLED)
        open val interpolator = value[INTERPOLATOR]
        open val repeatCount = value.getInt(REPEAT_COUNT)
        open val repeatMode = value.getInt(REPEAT_MODE)
        open val startOffset = value.getLong(START_OFFSET)
        open val zAdjustment = value.getInt(Z_ADJUSTMENT)

        fun instantiate(c: Context): Animation? {
            val anim = createAnimation(c)
            if (null != anim) {
                if (null != detachWallpaper) {
                    anim.detachWallpaper = detachWallpaper!!
                }
                if (null != duration) {
                    anim.duration = duration!!
                }
                if (null != fillAfter) {
                    anim.fillAfter = fillAfter!!
                }
                if (null != fillBefore) {
                    anim.fillBefore = fillBefore!!
                }
                if (null != fillEnabled) {
                    anim.isFillEnabled = fillEnabled!!
                }
                if (null != interpolator) {
                    val i = loadInterpolator(c, interpolator!!)
                    if (null != i) {
                        anim.interpolator = i
                    }
                }
                if (null != repeatCount) {
                    anim.repeatCount = repeatCount!!
                }
                if (null != repeatMode) {
                    anim.repeatMode = repeatMode!!
                }
                if (null != startOffset) {
                    anim.startOffset = startOffset!!
                }
                if (null != zAdjustment) {
                    anim.zAdjustment = zAdjustment!!
                }
            }
            return anim
        }

        abstract fun createAnimation(context: Context): Animation?

        companion object {
            const val DETACH_WALLPAPER = "detachWallpaper"
            const val DURATION = "duration"
            const val FILL_AFTER = "fillAfter"
            const val FILL_BEFORE = "fillBefore"
            const val FILL_ENABLED = "fillEnabled"
            const val INTERPOLATOR = "interpolator"
            const val REPEAT_COUNT = "repeatCount"
            const val REPEAT_MODE = "repeatMode"
            const val START_OFFSET = "startOffset"
            const val Z_ADJUSTMENT = "zAdjustment"
        }
    }

    open class AnimationSetProperties(value: ObjectV) : AnimationProperties(value) {
        open var shareInterpolator: Boolean? = value.getBoolean(SHARE_INTERPOLATOR)
        open var children: Value? = value[CHILDREN]

        override fun createAnimation(context: Context): Animation {
            val animationSet = AnimationSet((if (shareInterpolator == null) true else shareInterpolator!!))
            if (null != children) {
                if (children is ArrayV) {
                    val iterator = (children!! as ArrayV).iterator()
                    while (iterator.hasNext()) {
                        val animation = loadAnimation(context, iterator.next()!!)
                        if (null != animation) {
                            animationSet.addAnimation(animation)
                        }
                    }
                } else if (children is ObjectV || children is PrimitiveV) {
                    val animation = loadAnimation(context, children!!)
                    if (null != animation) {
                        animationSet.addAnimation(animation)
                    }
                }
            }
            return animationSet
        }

        companion object {
            const val SHARE_INTERPOLATOR = "shareInterpolator"
            const val CHILDREN = "children"
        }
    }

    open class AlphaAnimProperties(value: ObjectV) : AnimationProperties(value) {
        open var fromAlpha: Float? = value.getFloat(FROM_ALPHA)
        open var toAlpha: Float? = value.getFloat(TO_ALPHA)

        override fun createAnimation(context: Context): Animation? = if (null == fromAlpha || null == toAlpha) null else AlphaAnimation(fromAlpha!!, toAlpha!!)

        companion object {
            const val FROM_ALPHA = "fromAlpha"
            const val TO_ALPHA = "toAlpha"
        }
    }

    open class ScaleAnimProperties(value: ObjectV) : AnimationProperties(value) {
        open var fromXScale = value.getFloat(FROM_X_SCALE)
        open var toXScale = value.getFloat(TO_X_SCALE)
        open var fromYScale = value.getFloat(FROM_Y_SCALE)
        open var toYScale = value.getFloat(TO_Y_SCALE)
        open var pivotX = value[PIVOT_X]
        open var pivotY = value[PIVOT_Y]

        override fun createAnimation(context: Context): Animation = if (pivotX != null && pivotY != null) {
            val pivotXDesc = Description.parseValue(pivotX)
            val pivotYDesc = Description.parseValue(pivotY)
            ScaleAnimation(fromXScale!!, toXScale!!, fromYScale!!, toYScale!!, pivotXDesc.type, pivotXDesc.value, pivotYDesc.type, pivotYDesc.value)
        } else {
            ScaleAnimation(fromXScale!!, toXScale!!, fromYScale!!, toYScale!!)
        }

        companion object {
            const val FROM_X_SCALE = "fromXScale"
            const val TO_X_SCALE = "toXScale"
            const val FROM_Y_SCALE = "fromYScale"
            const val TO_Y_SCALE = "toYScale"
            const val PIVOT_X = "pivotX"
            const val PIVOT_Y = "pivotY"
        }
    }

    open class TranslateAnimProperties(value: ObjectV) : AnimationProperties(value) {
        open var fromXDelta = value[FROM_X_DELTA]!!
        open var toXDelta = value[TO_X_DELTA]
        open var fromYDelta = value[FROM_Y_DELTA]!!
        open var toYDelta = value[TO_Y_DELTA]

        override fun createAnimation(context: Context): Animation {
            val fromXDeltaDescription = Description.parseValue(fromXDelta)
            val toXDeltaDescription = Description.parseValue(toXDelta)
            val fromYDeltaDescription = Description.parseValue(fromYDelta)
            val toYDeltaDescription = Description.parseValue(toYDelta)
            return TranslateAnimation(fromXDeltaDescription.type, fromXDeltaDescription.value, toXDeltaDescription.type, toXDeltaDescription.value,
                    fromYDeltaDescription.type, fromYDeltaDescription.value, toYDeltaDescription.type, toYDeltaDescription.value)
        }

        companion object {
            const val FROM_X_DELTA = "fromXDelta"
            const val TO_X_DELTA = "toXDelta"
            const val FROM_Y_DELTA = "fromYDelta"
            const val TO_Y_DELTA = "toYDelta"
        }
    }

    open class RotateAnimProperties(value: ObjectV) : AnimationProperties(value) {
        open var fromDegrees = value.getFloat(FROM_DEGREES)
        open var toDegrees = value.getFloat(TO_DEGREES)
        open var pivotX = value[PIVOT_X]
        open var pivotY = value[PIVOT_Y]

        override fun createAnimation(context: Context): Animation = if (null != pivotX && null != pivotY) {
            val pivotXDesc = Description.parseValue(pivotX)
            val pivotYDesc = Description.parseValue(pivotY)
            RotateAnimation(fromDegrees!!, toDegrees!!, pivotXDesc.type, pivotXDesc.value, pivotYDesc.type, pivotYDesc.value)
        } else {
            RotateAnimation(fromDegrees!!, toDegrees!!)
        }

        companion object {
            const val FROM_DEGREES = "fromDegrees"
            const val TO_DEGREES = "toDegrees"
            const val PIVOT_X = "pivotX"
            const val PIVOT_Y = "pivotY"
        }
    }

    abstract class InterpolatorProperties {
        abstract fun createInterpolator(context: Context?): Interpolator?
    }

    open class PathInterpolatorProperties(parser: ObjectV) : InterpolatorProperties() {
        open var controlX1 = parser.getFloat(CONTROL_X1)
        open var controlY1 = parser.getFloat(CONTROL_Y1)
        open var controlX2 = parser.getFloat(CONTROL_X2)
        open var controlY2 = parser.getFloat(CONTROL_Y2)

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun createInterpolator(context: Context?): Interpolator = when {
            null != controlX2 && null != controlY2 -> PathInterpolator(controlX1!!, controlY1!!, controlX2!!, controlY2!!)
            else -> PathInterpolator(controlX1!!, controlY1!!)
        }

        companion object {
            const val CONTROL_X1 = "controlX1"
            const val CONTROL_Y1 = "controlY1"
            const val CONTROL_X2 = "controlX2"
            const val CONTROL_Y2 = "controlY2"
        }
    }

    open class AnticipateInterpolatorProperties(parser: ObjectV) : InterpolatorProperties() {
        open var tension: Float = parser.getFloat(TENSION)!!

        override fun createInterpolator(context: Context?): Interpolator = AnticipateInterpolator(tension)

        companion object {
            const val TENSION = "tension"
        }
    }

    open class OvershootInterpolatorProperties(parser: ObjectV) : InterpolatorProperties() {
        open var tension: Float? = parser.getFloat(TENSION)

        override fun createInterpolator(context: Context?): Interpolator = if (tension == null) OvershootInterpolator() else OvershootInterpolator(tension!!)

        companion object {
            const val TENSION = "tension"
        }
    }

    open class AnticipateOvershootInterpolatorProperties(parser: ObjectV) : InterpolatorProperties() {
        open var tension: Float? = parser.getFloat(TENSION)
        open var extraTension: Float? = parser.getFloat(EXTRA_TENSION)

        override fun createInterpolator(context: Context?): Interpolator = when {
            null == tension -> AnticipateOvershootInterpolator()
            null == extraTension -> AnticipateOvershootInterpolator(tension!!)
            else -> AnticipateOvershootInterpolator(tension!!, extraTension!!)
        }

        companion object {
            const val TENSION = "tension"
            const val EXTRA_TENSION = "extraTension"
        }
    }

    open class CycleInterpolatorProperties(parser: ObjectV) : InterpolatorProperties() {
        open var cycles: Float = parser.getFloat(CYCLES)!!

        override fun createInterpolator(context: Context?): Interpolator = CycleInterpolator(cycles)

        companion object {
            const val CYCLES = "cycles"
        }
    }
}
