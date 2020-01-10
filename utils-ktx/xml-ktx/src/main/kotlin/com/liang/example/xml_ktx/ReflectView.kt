package com.liang.example.xml_ktx

import android.animation.AnimatorInflater
import android.animation.LayoutTransition
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.BlendMode
import android.graphics.Color
import android.graphics.RenderNode
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.util.Xml
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.InflateException
import android.view.LayoutInflater
import android.view.PointerIcon
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.AnyRes
import androidx.annotation.RequiresApi
import com.anggrayudi.hiddenapi.Res
import com.liang.example.basic_ktx.ReflectHelper
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

open class MyLayoutInflater : LayoutInflater {
    companion object {
        private val EMPTY_STACK_TRACE = arrayOfNulls<StackTraceElement>(0)

        val mFilterMap: HashMap<String, Boolean>? = null

        var mTempValue: TypedValue? = null

        const val TAG_MERGE = "merge"
        const val TAG_INCLUDE = "include"
        const val TAG_1995 = "blink"
        const val TAG_REQUEST_FOCUS = "requestFocus"
        const val TAG_TAG = "tag"

        const val ATTR_LAYOUT = "layout"

        val ATTRS_THEME = arrayOf(Res.attr.theme)

        @Throws(XmlPullParserException::class, IOException::class)
        fun consumeChildElements(parser: XmlPullParser) {
            var type: Int
            val currentDepth = parser.depth
            while ((parser.next().also { type = it } != XmlPullParser.END_TAG || parser.depth > currentDepth) &&
                    type != XmlPullParser.END_DOCUMENT) { // Empty
            }
        }
    }

    val mConstructorArgs = arrayOfNulls<Any>(2)

    constructor(context: Context) : super(context)
    constructor(original: LayoutInflater, context: Context) : super(original, context)

    override fun cloneInContext(newContext: Context?): LayoutInflater {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

//     override fun inflate(parser: XmlPullParser, root: ViewGroup?, attachToRoot: Boolean): View {
//         synchronized(mConstructorArgs) {
//             val inflaterContext = context
//             val attrs = Xml.asAttributeSet(parser)
//             val lastContext = mConstructorArgs[0] as Context
//             mConstructorArgs[0] = inflaterContext
//             var result: View? = root
//             try {
//                 advanceToRootNode(parser)
//                 val name = parser.name
//                 if (TAG_MERGE == name) {
//                     if (root == null || !attachToRoot) {
//                         throw InflateException("<merge /> can be used only with a valid "
//                                 + "ViewGroup root and attachToRoot=true")
//                     }
//                     rInflate(parser, root, inflaterContext, attrs, false)
//                 } else { // Temp is the root view that was found in the xml
//                     val temp: View = createViewFromTag(root, name, inflaterContext, attrs)
//                     var params: ViewGroup.LayoutParams? = null
//                     if (root != null) {
//                         // Create layout params that match root, if supplied
//                         params = root.generateLayoutParams(attrs)
//                         if (!attachToRoot) { // Set the layout params for temp if we are not attaching. (If we are, we use addView, below)
//                             temp.layoutParams = params
//                         }
//                     }
//                     // Inflate all children under temp against its context.
//                     rInflateChildren(parser, temp, attrs, true)
//                     // We are supposed to attach all the views we found (int temp) to root. Do that now.
//                     if (root != null && attachToRoot) {
//                         root.addView(temp, params)
//                     }
//                     // Decide whether to return the root that was passed in or the top view found in xml.
//                     if (root == null || !attachToRoot) {
//                         result = temp
//                     }
//                 }
//             } catch (e: XmlPullParserException) {
//                 val ie = InflateException(e.message, e)
//                 ie.stackTrace = EMPTY_STACK_TRACE
//                 throw ie
//             } catch (e: Exception) {
//                 val ie = InflateException(
//                         getParserStateDescription(inflaterContext, attrs)
//                                 + ": " + e.message, e)
//                 ie.stackTrace = EMPTY_STACK_TRACE
//                 throw ie
//             } finally {
//                 // Don't retain static reference on context.
//                 mConstructorArgs[0] = lastContext
//                 mConstructorArgs[1] = null
//             }
//             return result!!
//         }
//     }
//
//     @Throws(InflateException::class, IOException::class, XmlPullParserException::class)
//     open fun advanceToRootNode(parser: XmlPullParser) { // Look for the root node.
//         var type: Int
//         while (parser.next().also { type = it } != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) { // Empty
//         }
//         if (type != XmlPullParser.START_TAG) {
//             throw InflateException("${parser.positionDescription}: No start tag found!")
//         }
//     }
//
//     @Throws(XmlPullParserException::class, IOException::class)
//     open fun rInflate(parser: XmlPullParser, parent: View, context: Context?,
//                       attrs: AttributeSet?, finishInflate: Boolean) {
//         val depth = parser.depth
//         var type: Int
//         var pendingRequestFocus = false
//         while ((parser.next().also { type = it } != XmlPullParser.END_TAG ||
//                         parser.depth > depth) && type != XmlPullParser.END_DOCUMENT) {
//             if (type != XmlPullParser.START_TAG) {
//                 continue
//             }
//             val name = parser.name
//             when {
//                 TAG_REQUEST_FOCUS == name -> {
//                     pendingRequestFocus = true
//                     consumeChildElements(parser)
//                 }
//                 TAG_TAG == name -> parseViewTag(parser, parent, attrs)
//                 TAG_INCLUDE == name -> {
//                     if (parser.depth == 0) {
//                         throw InflateException("<include /> cannot be the root element")
//                     }
//                     parseInclude(parser, context, parent, attrs)
//                 }
//                 TAG_MERGE == name -> throw InflateException("<merge /> must be the root element")
//                 else -> {
//                     val view: View = createViewFromTag(parent, name, context, attrs)
//                     val viewGroup = parent as ViewGroup
//                     val params = viewGroup.generateLayoutParams(attrs)
//                     rInflateChildren(parser, view, attrs, true)
//                     viewGroup.addView(view, params)
//                 }
//             }
//         }
//         if (pendingRequestFocus) {
//             versionMethod(Build.VERSION_CODES.O) { parent.restoreDefaultFocus() }
//         }
//         if (finishInflate) {
//             ReflectHelper.invokeN("onFinishInflate", parent)
//         }
//     }
//
//     open fun getParserStateDescription(context: Context, attrs: AttributeSet): String? {
//         val sourceResId = (attrs as MyParser).getSourceResId()
//         return if (sourceResId == Resources.ID_NULL) {
//             attrs.positionDescription
//         } else {
//             "${attrs.positionDescription} in ${context.resources.getResourceName(sourceResId)}"
//         }
//     }
//
//     @Throws(XmlPullParserException::class, IOException::class)
//     open fun parseViewTag(parser: XmlPullParser, view: View, attrs: AttributeSet?) {
//         val ta: MyTypedArray = /*view.context.obtainStyledAttributes(attrs, Res.styleable.ViewTag)*/ TODO()
//         val key = ta.getResourceId(Res.styleable.ViewTag_id, 0)
//         val value = ta.getText(Res.styleable.ViewTag_value)
//         view.setTag(key, value)
//         consumeChildElements(parser)
//     }
//
//     @Throws(XmlPullParserException::class, IOException::class)
//     open fun parseInclude(parser: XmlPullParser?, context: Context, parent: View?,
//                           attrs: AttributeSet) {
//         var context = context
//         var type: Int
//         if (parent !is ViewGroup) {
//             throw InflateException("<include /> can only be used inside of a ViewGroup")
//         }
//         val ta = context.obtainStyledAttributes(attrs, ATTRS_THEME)
//         val themeResId = ta.getResourceId(0, 0)
//         val hasThemeOverride = themeResId != 0
//         if (hasThemeOverride) {
//             context = ContextThemeWrapper(context, themeResId)
//         }
//         ta.recycle()
//         var layout = attrs.getAttributeResourceValue(null, ATTR_LAYOUT, 0)
//         if (layout == 0) {
//             val value = attrs.getAttributeValue(null, ATTR_LAYOUT)
//             if (value == null || value.isEmpty()) {
//                 throw InflateException("You must specify a layout in the"
//                         + " include tag: <include layout=\"@layout/layoutID\" />")
//             }
//             layout = context.resources.getIdentifier(
//                     value.substring(1), "attr", context.packageName)
//         }
//         if (mTempValue == null) {
//             mTempValue = TypedValue()
//         }
//         if (layout != 0 && context.theme.resolveAttribute(layout, mTempValue, true)) {
//             layout = mTempValue!!.resourceId
//         }
//         if (layout == 0) {
//             val value = attrs.getAttributeValue(null, ATTR_LAYOUT)
//             throw InflateException("You must specify a valid layout "
//                     + "reference. The layout ID " + value + " is not valid.")
//         }
//         val precompiled: View? = tryInflatePrecompiled(layout, context.resources, parent as ViewGroup?,  /*attachToRoot=*/true)
//         if (precompiled == null) {
//             val childParser = context.resources.getLayout(layout)
//             try {
//                 val childAttrs = Xml.asAttributeSet(childParser)
//                 while (childParser.next().also { type = it } != XmlPullParser.START_TAG &&
//                         type != XmlPullParser.END_DOCUMENT) { // Empty.
//                 }
//                 if (type != XmlPullParser.START_TAG) {
//                     throw InflateException(getParserStateDescription(context, childAttrs) + ": No start tag found!")
//                 }
//                 val childName = childParser.name
//                 if (TAG_MERGE == childName) { // The <merge> tag doesn't support android:theme, so
// // nothing special to do here.
//                     rInflate(childParser, parent, context, childAttrs, false)
//                 } else {
//                     val view: View = createViewFromTag(parent, childName,
//                             context, childAttrs, hasThemeOverride)
//                     val a = context.obtainStyledAttributes(attrs, Res.styleable.Include)
//                     val id = a.getResourceId(Res.styleable.Include_id, View.NO_ID)
//                     val visibility = a.getInt(Res.styleable.Include_visibility, -1)
//                     var params: ViewGroup.LayoutParams? = null
//                     try {
//                         params = parent.generateLayoutParams(attrs)
//                     } catch (e: RuntimeException) { // Ignore, just fail over to child attrs.
//                     }
//                     if (params == null) {
//                         params = parent.generateLayoutParams(childAttrs)
//                     }
//                     view.layoutParams = params
//                     // Inflate all children.
//                     rInflateChildren(childParser, view, childAttrs, true)
//                     if (id != View.NO_ID) {
//                         view.id = id
//                     }
//                     when (visibility) {
//                         0 -> view.visibility = View.VISIBLE
//                         1 -> view.visibility = View.INVISIBLE
//                         2 -> view.visibility = View.GONE
//                     }
//                     parent.addView(view)
//                 }
//             } finally {
//                 childParser.close()
//             }
//         }
//         consumeChildElements(parser)
//     }
}

interface MyParser : XmlResourceParser {
    @AnyRes
    fun getSourceResId(): Int
}

interface MyTypedArray {
    val myParser: MyParser
    val length: Int
    val indexCount: Int

    fun getIndex(at: Int): String
    fun getValue(index: String, outValue: TypedValue): Boolean
    fun peekValue(index: String): TypedValue?
    fun getType(index: String): Int
    fun hasValue(index: String): Boolean
    fun hasValueOrEmpty(index: String): Boolean
    fun getPositionDescription(index: String): String

    fun getText(index: String): CharSequence
    fun getString(index: String): String?
    fun getTextArray(index: String): Array<CharSequence>

    fun getNonResourceString(index: String): String
    fun getNonConfigurationString(index: String, allowedChangingConfigs: Int): String

    fun getBoolean(index: String, defValue: Boolean): Boolean
    fun getInt(index: String, defValue: Int): Int
    fun getFloat(index: String, defValue: Float): Float

    fun getInteger(index: String, defValue: Int): Int
    fun getColorStateList(index: String): ColorStateList
    fun getColor(index: String, defValue: Int): Int
    // fun getComplexColor(index: String): android.content.res.ComplexColor

    fun getDimension(index: String, defValue: Float): Float
    fun getDimensionPixelOffset(index: String, defValue: Int): Int
    fun getDimensionPixelSize(index: String, defValue: Int): Int
    fun getLayoutDimension(index: String, name: String): Int
    fun getLayoutDimension(index: String, defValue: Int): Int

    fun getFraction(index: String, base: Int, pbase: Int, defValue: Float): Float

    fun getResourceId(index: String, defValue: Int): Int
    fun getThemeAttributeId(index: String, defValue: Int): Int
    fun getSourceResourceId(index: String, defValue: Int): Int

    fun getFont(index: String): Typeface

    fun getDrawable(index: String): Drawable?
    fun getDrawableForDensity(index: String, density: Int): Drawable

    fun extractThemeAttrs(scrap: IntArray?): IntArray
}

fun versionMethod(requireVersion: Int, action: () -> Unit) {
    if (Build.VERSION.SDK_INT >= requireVersion) {
        action()
    }
}

open class MyView : View {
    lateinit var a: MyTypedArray

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    @Suppress("UNCHECKED_CAST")
    fun init(attrs: AttributeSet) {
        var background: Drawable? = null

        var leftPadding = -1
        var topPadding = -1
        var rightPadding = -1
        var bottomPadding = -1
        var startPadding: Int = UNDEFINED_PADDING
        var endPadding: Int = UNDEFINED_PADDING

        var padding = -1
        var paddingHorizontal = -1
        var paddingVertical = -1

        var viewFlagValues = 0
        var viewFlagMasks = 0

        var setScrollContainer = false

        var x = 0
        var y = 0

        var tx = 0f
        var ty = 0f
        var tz = 0f
        var elevation = 0f
        var rotation = 0f
        var rotationX = 0f
        var rotationY = 0f
        var sx = 1f
        var sy = 1f
        var transformSet = false

        var scrollbarStyle = SCROLLBARS_INSIDE_OVERLAY
        var overScrollMode: Int = 0
        var initializeScrollbars = false
        var initializeScrollIndicators = false

        var startPaddingDefined = false
        var endPaddingDefined = false
        var leftPaddingDefined = false
        var rightPaddingDefined = false

        val targetSdkVersion = context.applicationInfo.targetSdkVersion

        viewFlagValues = viewFlagValues or FOCUSABLE_AUTO
        viewFlagMasks = viewFlagMasks or FOCUSABLE_AUTO

        val tintInfo = "android.view.View\$TintInfo"
        val foregroundInfo = "android.view.View\$ForegroundInfo"

        setAttrs@ for (i in 0 until a.indexCount) {
            when (val attr = a.getIndex(i)) {
                Res.styleable.View_background -> background = a.getDrawable(attr)
                Res.styleable.View_padding -> {
                    padding = a.getDimensionPixelSize(attr, -1)
                    ReflectHelper.setInt("mUserPaddingLeftInitial", this, padding)
                    ReflectHelper.setInt("mUserPaddingRightInitial", this, padding)
                    leftPaddingDefined = true
                    rightPaddingDefined = true
                }
                Res.styleable.View_paddingHorizontal -> {
                    paddingHorizontal = a.getDimensionPixelSize(attr, -1)
                    ReflectHelper.setInt("mUserPaddingLeftInitial", this, paddingHorizontal)
                    ReflectHelper.setInt("mUserPaddingRightInitial", this, paddingHorizontal)
                    leftPaddingDefined = true
                    rightPaddingDefined = true
                }
                Res.styleable.View_paddingVertical -> paddingVertical = a.getDimensionPixelSize(attr, -1)
                Res.styleable.View_paddingLeft -> {
                    leftPadding = a.getDimensionPixelSize(attr, -1)
                    ReflectHelper.setInt("mUserPaddingLeftInitial", this, leftPadding)
                    leftPaddingDefined = true
                }
                Res.styleable.View_paddingTop -> topPadding = a.getDimensionPixelSize(attr, -1)
                Res.styleable.View_paddingRight -> {
                    rightPadding = a.getDimensionPixelSize(attr, -1)
                    ReflectHelper.setInt("mUserPaddingRightInitial", this, rightPadding)
                    rightPaddingDefined = true
                }
                Res.styleable.View_paddingBottom -> bottomPadding = a.getDimensionPixelSize(attr, -1)
                Res.styleable.View_paddingStart -> {
                    startPadding = a.getDimensionPixelSize(attr, UNDEFINED_PADDING)
                    startPaddingDefined = startPadding != UNDEFINED_PADDING
                }
                Res.styleable.View_paddingEnd -> {
                    endPadding = a.getDimensionPixelSize(attr, UNDEFINED_PADDING)
                    endPaddingDefined = endPadding != UNDEFINED_PADDING
                }
                Res.styleable.View_scrollX -> x = a.getDimensionPixelOffset(attr, 0)
                Res.styleable.View_scrollY -> y = a.getDimensionPixelOffset(attr, 0)
                Res.styleable.View_alpha -> alpha = a.getFloat(attr, 1f)
                Res.styleable.View_transformPivotX -> pivotX = a.getDimension(attr, 0f)
                Res.styleable.View_transformPivotY -> pivotY = a.getDimension(attr, 0f)
                Res.styleable.View_translationX -> {
                    tx = a.getDimension(attr, 0f)
                    transformSet = true
                }
                Res.styleable.View_translationY -> {
                    ty = a.getDimension(attr, 0f)
                    transformSet = true
                }
                Res.styleable.View_translationZ -> {
                    tz = a.getDimension(attr, 0f)
                    transformSet = true
                }
                Res.styleable.View_elevation -> {
                    elevation = a.getDimension(attr, 0f)
                    transformSet = true
                }
                Res.styleable.View_rotation -> {
                    rotation = a.getFloat(attr, 0f)
                    transformSet = true
                }
                Res.styleable.View_rotationX -> {
                    rotationX = a.getFloat(attr, 0f)
                    transformSet = true
                }
                Res.styleable.View_rotationY -> {
                    rotationY = a.getFloat(attr, 0f)
                    transformSet = true
                }
                Res.styleable.View_scaleX -> {
                    sx = a.getFloat(attr, 1f)
                    transformSet = true
                }
                Res.styleable.View_scaleY -> {
                    sy = a.getFloat(attr, 1f)
                    transformSet = true
                }
                Res.styleable.View_id -> id = a.getResourceId(attr, NO_ID)
                Res.styleable.View_tag -> tag = a.getText(attr)
                Res.styleable.View_fitsSystemWindows -> if (a.getBoolean(attr, false)) {
                    viewFlagValues = viewFlagValues or FITS_SYSTEM_WINDOWS
                    viewFlagMasks = viewFlagMasks or FITS_SYSTEM_WINDOWS
                }
                Res.styleable.View_focusable -> {
                    val t = TypedValue()
                    viewFlagValues = viewFlagValues and FOCUSABLE_MASK.inv() or if (a.getValue(Res.styleable.View_focusable, t)) {
                        if (t.type == TypedValue.TYPE_INT_BOOLEAN) {
                            if (t.data == 0) NOT_FOCUSABLE else FOCUSABLE
                        } else {
                            t.data
                        }
                    } else {
                        FOCUSABLE_AUTO
                    }
                    if (viewFlagValues and FOCUSABLE_AUTO == 0) {
                        viewFlagMasks = viewFlagMasks or FOCUSABLE_MASK
                    }
                }
                Res.styleable.View_focusableInTouchMode -> if (a.getBoolean(attr, false)) {
                    // unset auto focus since focusableInTouchMode implies explicit focusable
                    viewFlagValues = viewFlagValues and FOCUSABLE_AUTO.inv()
                    viewFlagValues = viewFlagValues or FOCUSABLE_IN_TOUCH_MODE or FOCUSABLE
                    viewFlagMasks = viewFlagMasks or FOCUSABLE_IN_TOUCH_MODE or FOCUSABLE_MASK
                }
                Res.styleable.View_clickable -> if (a.getBoolean(attr, false)) {
                    viewFlagValues = viewFlagValues or CLICKABLE
                    viewFlagMasks = viewFlagMasks or CLICKABLE
                }
                Res.styleable.View_longClickable -> if (a.getBoolean(attr, false)) {
                    viewFlagValues = viewFlagValues or LONG_CLICKABLE
                    viewFlagMasks = viewFlagMasks or LONG_CLICKABLE
                }
                Res.styleable.View_contextClickable -> if (a.getBoolean(attr, false)) {
                    viewFlagValues = viewFlagValues or CONTEXT_CLICKABLE
                    viewFlagMasks = viewFlagMasks or CONTEXT_CLICKABLE
                }
                Res.styleable.View_saveEnabled -> if (!a.getBoolean(attr, true)) {
                    viewFlagValues = viewFlagValues or SAVE_DISABLED
                    viewFlagMasks = viewFlagMasks or SAVE_DISABLED_MASK
                }
                Res.styleable.View_duplicateParentState -> if (a.getBoolean(attr, false)) {
                    viewFlagValues = viewFlagValues or DUPLICATE_PARENT_STATE
                    viewFlagMasks = viewFlagMasks or DUPLICATE_PARENT_STATE
                }
                Res.styleable.View_visibility -> {
                    val visibility = a.getInt(attr, 0)
                    if (visibility != 0) {
                        viewFlagValues = viewFlagValues or VISIBILITY_FLAGS[visibility]
                        viewFlagMasks = viewFlagMasks or VISIBILITY_MASK
                    }
                }
                Res.styleable.View_layoutDirection -> {
                    // Clear any layout direction flags (included resolved bits) already set
                    ReflectHelper.setInt("mPrivateFlags2", this, ReflectHelper.getInt("mPrivateFlags2", this) and
                            (PFLAG2_LAYOUT_DIRECTION_MASK or PFLAG2_LAYOUT_DIRECTION_RESOLVED_MASK).inv())
                    // Set the layout direction flags depending on the value of the attribute
                    val layoutDirection = a.getInt(attr, -1)
                    val value: Int = if (layoutDirection != -1) LAYOUT_DIRECTION_FLAGS[layoutDirection] else LAYOUT_DIRECTION_DEFAULT
                    ReflectHelper.setInt("mPrivateFlags2", this, ReflectHelper.getInt("mPrivateFlags2", this) or
                            (value shl PFLAG2_LAYOUT_DIRECTION_MASK_SHIFT))
                }
                Res.styleable.View_drawingCacheQuality -> {
                    val cacheQuality = a.getInt(attr, 0)
                    if (cacheQuality != 0) {
                        viewFlagValues = viewFlagValues or DRAWING_CACHE_QUALITY_FLAGS[cacheQuality]
                        viewFlagMasks = viewFlagMasks or DRAWING_CACHE_QUALITY_MASK
                    }
                }
                Res.styleable.View_contentDescription -> contentDescription = a.getString(attr)
                Res.styleable.View_accessibilityTraversalBefore -> accessibilityTraversalBefore = a.getResourceId(attr, NO_ID)
                Res.styleable.View_accessibilityTraversalAfter -> accessibilityTraversalAfter = a.getResourceId(attr, NO_ID)
                Res.styleable.View_labelFor -> labelFor = a.getResourceId(attr, NO_ID)
                Res.styleable.View_soundEffectsEnabled -> if (!a.getBoolean(attr, true)) {
                    viewFlagValues = viewFlagValues and SOUND_EFFECTS_ENABLED.inv()
                    viewFlagMasks = viewFlagMasks or SOUND_EFFECTS_ENABLED
                }
                Res.styleable.View_hapticFeedbackEnabled -> if (!a.getBoolean(attr, true)) {
                    viewFlagValues = viewFlagValues and HAPTIC_FEEDBACK_ENABLED.inv()
                    viewFlagMasks = viewFlagMasks or HAPTIC_FEEDBACK_ENABLED
                }
                Res.styleable.View_scrollbars -> {
                    val scrollbars = a.getInt(attr, SCROLLBARS_NONE)
                    if (scrollbars != SCROLLBARS_NONE) {
                        viewFlagValues = viewFlagValues or scrollbars
                        viewFlagMasks = viewFlagMasks or SCROLLBARS_MASK
                        initializeScrollbars = true
                    }
                }
                Res.styleable.View_fadingEdge -> {
                    if (targetSdkVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) { // Ignore the attribute starting with ICS
                        break@setAttrs
                    }
                    val fadingEdge = a.getInt(attr, FADING_EDGE_NONE)
                    if (fadingEdge != FADING_EDGE_NONE) {
                        viewFlagValues = viewFlagValues or fadingEdge
                        viewFlagMasks = viewFlagMasks or FADING_EDGE_MASK
                        initializeFadingEdgeInternal(a)
                    }
                }
                Res.styleable.View_requiresFadingEdge -> {
                    val fadingEdge = a.getInt(attr, FADING_EDGE_NONE)
                    if (fadingEdge != FADING_EDGE_NONE) {
                        viewFlagValues = viewFlagValues or fadingEdge
                        viewFlagMasks = viewFlagMasks or FADING_EDGE_MASK
                        initializeFadingEdgeInternal(a)
                    }
                }
                Res.styleable.View_scrollbarStyle -> {
                    scrollbarStyle = a.getInt(attr, SCROLLBARS_INSIDE_OVERLAY)
                    if (scrollbarStyle != SCROLLBARS_INSIDE_OVERLAY) {
                        viewFlagValues = viewFlagValues or scrollbarStyle and SCROLLBARS_STYLE_MASK
                        viewFlagMasks = viewFlagMasks or SCROLLBARS_STYLE_MASK
                    }
                }
                Res.styleable.View_isScrollContainer -> {
                    setScrollContainer = true
                    if (a.getBoolean(attr, false)) {
                        isScrollContainer = true
                    }
                }
                Res.styleable.View_keepScreenOn -> if (a.getBoolean(attr, false)) {
                    viewFlagValues = viewFlagValues or KEEP_SCREEN_ON
                    viewFlagMasks = viewFlagMasks or KEEP_SCREEN_ON
                }
                Res.styleable.View_filterTouchesWhenObscured -> if (a.getBoolean(attr, false)) {
                    viewFlagValues = viewFlagValues or FILTER_TOUCHES_WHEN_OBSCURED
                    viewFlagMasks = viewFlagMasks or FILTER_TOUCHES_WHEN_OBSCURED
                }
                Res.styleable.View_nextFocusLeft -> ReflectHelper.setInt("mNextFocusLeftId", this, a.getResourceId(attr, NO_ID))
                Res.styleable.View_nextFocusRight -> ReflectHelper.setInt("mNextFocusRightId", this, a.getResourceId(attr, NO_ID))
                Res.styleable.View_nextFocusUp -> ReflectHelper.setInt("mNextFocusUpId", this, a.getResourceId(attr, NO_ID))
                Res.styleable.View_nextFocusDown -> ReflectHelper.setInt("mNextFocusDownId", this, a.getResourceId(attr, NO_ID))
                Res.styleable.View_nextFocusForward -> ReflectHelper.setInt("mNextFocusForwardId", this, a.getResourceId(attr, NO_ID))
                Res.styleable.View_nextClusterForward -> ReflectHelper.setInt("mNextClusterForwardId", this, a.getResourceId(attr, NO_ID))
                Res.styleable.View_minWidth -> ReflectHelper.setInt("mMinWidth", this, a.getDimensionPixelSize(attr, 0))
                Res.styleable.View_minHeight -> ReflectHelper.setInt("mMinHeight", this, a.getDimensionPixelSize(attr, 0))
                Res.styleable.View_onClick -> {
                    check(!context.isRestricted) { ("The android:onClick attribute cannot be used within a restricted context") }
                    val handlerName = a.getString(attr)
                    if (handlerName != null) {
                        setOnClickListener(ReflectHelper.newInstance<OnClickListener>("DeclaredOnClickListener", this, handlerName))
                    }
                }
                Res.styleable.View_overScrollMode -> overScrollMode = a.getInt(attr, OVER_SCROLL_IF_CONTENT_SCROLLS)
                Res.styleable.View_verticalScrollbarPosition -> ReflectHelper.setInt("mVerticalScrollbarPosition", this,
                        a.getInt(attr, SCROLLBAR_POSITION_DEFAULT))
                Res.styleable.View_layerType -> setLayerType(a.getInt(attr, LAYER_TYPE_NONE), null)
                Res.styleable.View_textDirection -> {
                    // Clear any text direction flag already set
                    ReflectHelper.setInt("mPrivateFlags2", this, ReflectHelper.getInt("mPrivateFlags2", this) and PFLAG2_TEXT_DIRECTION_MASK.inv())
                    // Set the text direction flags depending on the value of the attribute
                    val textDirection = a.getInt(attr, -1)
                    if (textDirection != -1) {
                        ReflectHelper.setInt("mPrivateFlags2", this, ReflectHelper.getInt("mPrivateFlags2", this) or
                                PFLAG2_TEXT_DIRECTION_FLAGS[textDirection])
                    }
                }
                Res.styleable.View_textAlignment -> {
                    // Clear any text alignment flag already set
                    ReflectHelper.setInt("mPrivateFlags2", this, ReflectHelper.getInt("mPrivateFlags2", this) and PFLAG2_TEXT_ALIGNMENT_MASK.inv())
                    // Set the text alignment flag depending on the value of the attribute
                    val textAlignment = a.getInt(attr, TEXT_ALIGNMENT_DEFAULT)
                    ReflectHelper.setInt("mPrivateFlags2", this, ReflectHelper.getInt("mPrivateFlags2", this) or
                            PFLAG2_TEXT_ALIGNMENT_FLAGS[textAlignment])
                }
                Res.styleable.View_importantForAccessibility -> importantForAccessibility = a.getInt(attr, IMPORTANT_FOR_ACCESSIBILITY_DEFAULT)
                Res.styleable.View_accessibilityLiveRegion -> accessibilityLiveRegion = a.getInt(attr, ACCESSIBILITY_LIVE_REGION_DEFAULT)
                Res.styleable.View_transitionName -> transitionName = a.getString(attr)
                Res.styleable.View_nestedScrollingEnabled -> isNestedScrollingEnabled = a.getBoolean(attr, false)
                Res.styleable.View_stateListAnimator -> stateListAnimator = AnimatorInflater.loadStateListAnimator(context, a.getResourceId(attr, 0))
                "View_backgroundTint" -> {
                    // This will get applied later during setBackground().
                    if (ReflectHelper.get("mBackgroundTint", this) == null) {
                        ReflectHelper.set("mBackgroundTint", this, ReflectHelper.newInstance(tintInfo))
                    }
                    ReflectHelper.set("mTintList", ReflectHelper.get("mBackgroundTint", this)!!, a.getColorStateList("View_backgroundTint"))
                    ReflectHelper.setBoolean("mHasTintList", ReflectHelper.get("mBackgroundTint", this)!!, true)
                }
                Res.styleable.View_backgroundTintMode -> {
                    // This will get applied later during setBackground().
                    if (ReflectHelper.get("mBackgroundTint", this) == null) {
                        ReflectHelper.set("mBackgroundTint", this, ReflectHelper.newInstance(tintInfo))
                    }
                    ReflectHelper.set("mBlendMode", ReflectHelper.get("mBackgroundTint", this)!!,
                            ReflectHelper.invokeS<BlendMode>("parseBlendMode", Drawable::class.java,
                                    a.getInt(Res.styleable.View_backgroundTintMode, -1), null))
                    ReflectHelper.setBoolean("mHasTintMode", ReflectHelper.get("mBackgroundTint", this)!!, true)
                }
                Res.styleable.View_outlineProvider -> ReflectHelper.invokeN("setOutlineProviderFromAttribute", this,
                        a.getInt(Res.styleable.View_outlineProvider, PROVIDER_BACKGROUND))
                Res.styleable.View_foreground -> if (targetSdkVersion >= Build.VERSION_CODES.M || this is FrameLayout)
                    foreground = a.getDrawable(attr)
                Res.styleable.View_foregroundGravity -> if (targetSdkVersion >= Build.VERSION_CODES.M || this is FrameLayout)
                    foregroundGravity = a.getInt(attr, Gravity.NO_GRAVITY)
                Res.styleable.View_foregroundTintMode -> if (targetSdkVersion >= Build.VERSION_CODES.M || this is FrameLayout)
                    foregroundTintBlendMode = ReflectHelper.invokeS<BlendMode>("parseBlendMode", Drawable::class.java, a.getInt(attr, -1), null)
                "View_foregroundTint" -> if (targetSdkVersion >= Build.VERSION_CODES.M || this is FrameLayout)
                    foregroundTintList = a.getColorStateList(attr)
                Res.styleable.View_foregroundInsidePadding -> if (targetSdkVersion >= Build.VERSION_CODES.M || this is FrameLayout) {
                    if (ReflectHelper.get("mForegroundInfo", this) == null) {
                        ReflectHelper.set("mForegroundInfo", this, ReflectHelper.newInstance(foregroundInfo))
                    }
                    ReflectHelper.setBoolean("mInsidePadding", ReflectHelper.get("mForegroundInfo", this)!!, a.getBoolean(attr,
                            ReflectHelper.getBoolean("mInsidePadding", ReflectHelper.get("mForegroundInfo", this)!!)))
                }
                Res.styleable.View_scrollIndicators -> {
                    val scrollIndicators = (a.getInt(attr, 0) shl SCROLL_INDICATORS_TO_PFLAGS3_LSHIFT and SCROLL_INDICATORS_PFLAG3_MASK)
                    if (scrollIndicators != 0) {
                        ReflectHelper.setInt("mPrivateFlags3", this, ReflectHelper.getInt("mPrivateFlags3", this) or scrollIndicators)
                        initializeScrollIndicators = true
                    }
                }
                Res.styleable.View_pointerIcon -> {
                    val resourceId = a.getResourceId(attr, 0)
                    if (resourceId != 0) {
                        pointerIcon = PointerIcon.load(context.resources, resourceId)
                    } else {
                        val pointerType = a.getInt(attr, /*PointerIcon.TYPE_NOT_SPECIFIED*/ 1)
                        if (pointerType != /*PointerIcon.TYPE_NOT_SPECIFIED*/ 1) {
                            pointerIcon = PointerIcon.getSystemIcon(context, pointerType)
                        }
                    }
                }
                Res.styleable.View_forceHasOverlappingRendering -> if (a.peekValue(attr) != null)
                    forceHasOverlappingRendering(a.getBoolean(attr, true))
                Res.styleable.View_tooltipText -> tooltipText = a.getText(attr)
                Res.styleable.View_keyboardNavigationCluster -> if (a.peekValue(attr) != null)
                    isKeyboardNavigationCluster = a.getBoolean(attr, true)
                Res.styleable.View_focusedByDefault -> if (a.peekValue(attr) != null)
                    isFocusedByDefault = a.getBoolean(attr, true)
                Res.styleable.View_autofillHints -> if (a.peekValue(attr) != null) {
                    var rawHints: Array<CharSequence>? = null
                    var rawString: String? = null
                    if (a.getType(attr) == TypedValue.TYPE_REFERENCE) {
                        val resId = a.getResourceId(attr, 0)
                        try {
                            rawHints = a.getTextArray(attr)
                        } catch (e: Resources.NotFoundException) {
                            rawString = resources.getString(resId)
                        }
                    } else {
                        rawString = a.getString(attr)
                    }
                    if (rawHints == null) {
                        rawHints = rawString?.split(",")?.toTypedArray() as? Array<CharSequence>
                                ?: throw IllegalArgumentException("Could not resolve autofillHints")
                    }
                    val hints = arrayOfNulls<String>(rawHints.size)
                    val numHints = rawHints.size
                    var rawHintNum = 0
                    while (rawHintNum < numHints) {
                        hints[rawHintNum] = rawHints[rawHintNum].toString().trim { it <= ' ' }
                        rawHintNum++
                    }
                    setAutofillHints(*hints)
                }
                Res.styleable.View_importantForAutofill -> if (a.peekValue(attr) != null)
                    importantForAutofill = a.getInt(attr, IMPORTANT_FOR_AUTOFILL_AUTO)
                Res.styleable.View_defaultFocusHighlightEnabled -> if (a.peekValue(attr) != null)
                    defaultFocusHighlightEnabled = a.getBoolean(attr, true)
                Res.styleable.View_screenReaderFocusable -> if (a.peekValue(attr) != null)
                    isScreenReaderFocusable = a.getBoolean(attr, false)
                Res.styleable.View_accessibilityPaneTitle -> if (a.peekValue(attr) != null)
                    accessibilityPaneTitle = a.getString(attr)
                Res.styleable.View_outlineSpotShadowColor -> outlineSpotShadowColor = a.getColor(attr, Color.BLACK)
                Res.styleable.View_outlineAmbientShadowColor -> outlineAmbientShadowColor = a.getColor(attr, Color.BLACK)
                Res.styleable.View_accessibilityHeading -> isAccessibilityHeading = a.getBoolean(attr, false)
                "View_forceDarkAllowed" -> (ReflectHelper.get("mRenderNode", this) as RenderNode).isForceDarkAllowed = a.getBoolean(attr, true)
            }
        }

        setOverScrollMode(overScrollMode)

        ReflectHelper.setInt("mUserPaddingStart", this, startPadding)
        ReflectHelper.setInt("mUserPaddingEnd", this, endPadding)

        background?.let { setBackground(it) }

        ReflectHelper.setBoolean("mLeftPaddingDefined", this, leftPaddingDefined)
        ReflectHelper.setBoolean("mRightPaddingDefined", this, rightPaddingDefined)

        if (padding >= 0) {
            leftPadding = padding
            topPadding = padding
            rightPadding = padding
            bottomPadding = padding
            ReflectHelper.setInt("mUserPaddingLeftInitial", this, padding)
            ReflectHelper.setInt("mUserPaddingRightInitial", this, padding)
        } else {
            if (paddingHorizontal >= 0) {
                leftPadding = paddingHorizontal
                rightPadding = paddingHorizontal
                ReflectHelper.setInt("mUserPaddingLeftInitial", this, paddingHorizontal)
                ReflectHelper.setInt("mUserPaddingRightInitial", this, paddingHorizontal)
            }
            if (paddingVertical >= 0) {
                topPadding = paddingVertical
                bottomPadding = paddingVertical
            }
        }

        if (ReflectHelper.invoke("isRtlCompatibilityMode", this)!!) {
            if (!leftPaddingDefined && startPaddingDefined) {
                leftPadding = startPadding
            }
            ReflectHelper.setInt("mUserPaddingLeftInitial", this, if (leftPadding >= 0) leftPadding else
                ReflectHelper.getInt("mUserPaddingLeftInitial", this))
            if (!rightPaddingDefined && endPaddingDefined) {
                rightPadding = endPadding
            }
            ReflectHelper.setInt("mUserPaddingRightInitial", this, if (rightPadding >= 0) rightPadding else
                ReflectHelper.getInt("mUserPaddingRightInitial", this))
        } else {
            val hasRelativePadding = startPaddingDefined || endPaddingDefined
            if (leftPaddingDefined && !hasRelativePadding) {
                ReflectHelper.setInt("mUserPaddingLeftInitial", this, leftPadding)
            }
            if (rightPaddingDefined && !hasRelativePadding) {
                ReflectHelper.setInt("mUserPaddingRightInitial", this, rightPadding)
            }
        }

        ReflectHelper.invokeN("internalSetPadding", this,
                ReflectHelper.getInt("mUserPaddingLeftInitial", this),
                if (topPadding >= 0) topPadding else ReflectHelper.getInt("mPaddingTop", this),
                ReflectHelper.getInt("mUserPaddingRightInitial", this),
                if (bottomPadding >= 0) bottomPadding else ReflectHelper.get("mPaddingBottom", this))

        if (viewFlagMasks != 0) {
            ReflectHelper.invokeN("setFlags", this, viewFlagValues, viewFlagMasks)
        }

        if (initializeScrollbars) {
            initializeScrollbarsInternal(a)  // TODO: 坑
        }
        if (initializeScrollIndicators) {
            ReflectHelper.invokeN("initializeScrollIndicatorsInternal", this)
        }
        if (scrollbarStyle != SCROLLBARS_INSIDE_OVERLAY) {
            ReflectHelper.invokeN("recomputePadding", this)
        }
        if (x != 0 || y != 0) {
            scrollTo(x, y)
        }

        if (transformSet) {
            translationX = tx
            translationY = ty
            translationZ = tz
            setElevation(elevation)
            setRotation(rotation)
            setRotationX(rotationX)
            setRotationY(rotationY)
            scaleX = sx
            scaleY = sy
        }

        if (!setScrollContainer && viewFlagValues and SCROLLBARS_VERTICAL != 0) {
            isScrollContainer = true
        }

        ReflectHelper.invokeN("computeOpaqueFlags", this)
    }

    open fun initializeFadingEdgeInternal(a: MyTypedArray) {
        ReflectHelper.invokeN("initScrollCache", this)
        val fadingEdgeLength = a.getDimensionPixelSize(Res.styleable.View_fadingEdgeLength, ViewConfiguration.get(context).scaledFadingEdgeLength)
        ReflectHelper.setInt("fadingEdgeLength", ReflectHelper.get("mScrollCache", this)!!, fadingEdgeLength)
    }

    open fun initializeScrollbarsInternal(a: MyTypedArray) {
        ReflectHelper.invokeN("initScrollCache", this)

        val scrollabilityCache = ReflectHelper.get("mScrollCache", this)!!
        if (ReflectHelper.get("scrollBar", scrollabilityCache) == null) {
            ReflectHelper.set("scrollBar", scrollabilityCache, ReflectHelper.newInstance<Drawable>("android.widget.ScrollBarDrawable"))
            ReflectHelper.invokeN("setState", ReflectHelper.get("scrollBar", scrollabilityCache)!!, drawableState)
            ReflectHelper.findMethod("setCallback", Drawable::class.java, Drawable.Callback::class.java)
                    ?.invoke(ReflectHelper.get("scrollBar", scrollabilityCache), this)
        }

        val fadeScrollbars = a.getBoolean(Res.styleable.View_fadeScrollbars, true)

        if (!fadeScrollbars) {
            ReflectHelper.setInt("state", scrollabilityCache, 1 /*View.ScrollabilityCache.ON*/)
        }
        ReflectHelper.set("fadeScrollBars", scrollabilityCache, fadeScrollbars)

        ReflectHelper.setInt("scrollBarFadeDuration", scrollabilityCache,
                a.getInt(Res.styleable.View_scrollbarFadeDuration, ViewConfiguration.getScrollBarFadeDuration()))
        ReflectHelper.setInt("scrollBarDefaultDelayBeforeFade", scrollabilityCache,
                a.getInt(Res.styleable.View_scrollbarDefaultDelayBeforeFade, ViewConfiguration.getScrollDefaultDelay()))
        ReflectHelper.setInt("scrollBarSize", scrollabilityCache,
                a.getDimensionPixelSize(Res.styleable.View_scrollbarSize, ViewConfiguration.get(context).scaledScrollBarSize))

        ReflectHelper.invokeN("setHorizontalTrackDrawable", ReflectHelper.get("scrollBar", scrollabilityCache)!!,
                a.getDrawable(Res.styleable.View_scrollbarTrackHorizontal))

        var thumb = a.getDrawable(Res.styleable.View_scrollbarThumbHorizontal)
        if (thumb != null) {
            ReflectHelper.invokeN("setHorizontalThumbDrawable", ReflectHelper.get("scrollBar", scrollabilityCache)!!, thumb)
        }

        if (a.getBoolean(Res.styleable.View_scrollbarAlwaysDrawHorizontalTrack, false)) {
            ReflectHelper.invokeN("setAlwaysDrawHorizontalTrack", ReflectHelper.get("scrollBar", scrollabilityCache)!!, true)
        }

        val track = a.getDrawable(Res.styleable.View_scrollbarTrackVertical)
        ReflectHelper.invokeN("setVerticalTrackDrawable", ReflectHelper.get("scrollBar", scrollabilityCache)!!, track)

        thumb = a.getDrawable(Res.styleable.View_scrollbarThumbVertical)
        if (thumb != null) {
            ReflectHelper.invokeN("setVerticalThumbDrawable", ReflectHelper.get("scrollBar", scrollabilityCache)!!, thumb)
        }

        if (a.getBoolean(Res.styleable.View_scrollbarAlwaysDrawVerticalTrack, false)) {
            ReflectHelper.invokeN("setAlwaysDrawVerticalTrack", ReflectHelper.get("scrollBar", scrollabilityCache)!!, true)
        }

        // Apply layout direction to the new Drawables if needed
        val layoutDirection = layoutDirection
        track?.layoutDirection = layoutDirection
        thumb?.layoutDirection = layoutDirection

        // Re-apply user/background padding so that scrollbar(s) get added
        ReflectHelper.invokeN("resolvePadding", this)
    }

    @Suppress("unused", "MemberVisibilityCanBePrivate", "SpellCheckingInspection")
    companion object {
        const val UNDEFINED_PADDING = Int.MIN_VALUE

        const val FOCUSABLE_MASK = 0x00000011
        const val FITS_SYSTEM_WINDOWS = 0x00000002
        const val FOCUSABLE_IN_TOUCH_MODE = 0x00040000

        val VISIBILITY_FLAGS = intArrayOf(VISIBLE, INVISIBLE, GONE)
        const val VISIBILITY_MASK = 0x0000000C
        const val PUBLIC_STATUS_BAR_VISIBILITY_MASK = 0x00003FF7

        const val CLICKABLE = 0x00004000
        const val LONG_CLICKABLE = 0x00200000
        const val CONTEXT_CLICKABLE = 0x00800000

        const val DRAWING_CACHE_QUALITY_MASK = 0x00180000
        const val DRAWING_CACHE_ENABLED = 0x00008000
        const val WILL_NOT_CACHE_DRAWING = 0x000020000
        val DRAWING_CACHE_QUALITY_FLAGS = intArrayOf(DRAWING_CACHE_QUALITY_AUTO, DRAWING_CACHE_QUALITY_LOW, DRAWING_CACHE_QUALITY_HIGH)

        const val SAVE_DISABLED = 0x000010000
        const val SAVE_DISABLED_MASK = 0x000010000

        const val DUPLICATE_PARENT_STATE = 0x00400000

        /*
         * Masks for mPrivateFlags2, as generated by dumpFlags():
         *
         * |-------|-------|-------|-------|
         *                                 1 PFLAG2_DRAG_CAN_ACCEPT
         *                                1  PFLAG2_DRAG_HOVERED
         *                              11   PFLAG2_LAYOUT_DIRECTION_MASK
         *                             1     PFLAG2_LAYOUT_DIRECTION_RESOLVED_RTL
         *                            1      PFLAG2_LAYOUT_DIRECTION_RESOLVED
         *                            11     PFLAG2_LAYOUT_DIRECTION_RESOLVED_MASK
         *                           1       PFLAG2_TEXT_DIRECTION_FLAGS[1]
         *                          1        PFLAG2_TEXT_DIRECTION_FLAGS[2]
         *                          11       PFLAG2_TEXT_DIRECTION_FLAGS[3]
         *                         1         PFLAG2_TEXT_DIRECTION_FLAGS[4]
         *                         1 1       PFLAG2_TEXT_DIRECTION_FLAGS[5]
         *                         11        PFLAG2_TEXT_DIRECTION_FLAGS[6]
         *                         111       PFLAG2_TEXT_DIRECTION_FLAGS[7]
         *                         111       PFLAG2_TEXT_DIRECTION_MASK
         *                        1          PFLAG2_TEXT_DIRECTION_RESOLVED
         *                       1           PFLAG2_TEXT_DIRECTION_RESOLVED_DEFAULT
         *                     111           PFLAG2_TEXT_DIRECTION_RESOLVED_MASK
         *                    1              PFLAG2_TEXT_ALIGNMENT_FLAGS[1]
         *                   1               PFLAG2_TEXT_ALIGNMENT_FLAGS[2]
         *                   11              PFLAG2_TEXT_ALIGNMENT_FLAGS[3]
         *                  1                PFLAG2_TEXT_ALIGNMENT_FLAGS[4]
         *                  1 1              PFLAG2_TEXT_ALIGNMENT_FLAGS[5]
         *                  11               PFLAG2_TEXT_ALIGNMENT_FLAGS[6]
         *                  111              PFLAG2_TEXT_ALIGNMENT_MASK
         *                 1                 PFLAG2_TEXT_ALIGNMENT_RESOLVED
         *                1                  PFLAG2_TEXT_ALIGNMENT_RESOLVED_DEFAULT
         *              111                  PFLAG2_TEXT_ALIGNMENT_RESOLVED_MASK
         *           111                     PFLAG2_IMPORTANT_FOR_ACCESSIBILITY_MASK
         *         11                        PFLAG2_ACCESSIBILITY_LIVE_REGION_MASK
         *       1                           PFLAG2_ACCESSIBILITY_FOCUSED
         *      1                            PFLAG2_SUBTREE_ACCESSIBILITY_STATE_CHANGED
         *     1                             PFLAG2_VIEW_QUICK_REJECTED
         *    1                              PFLAG2_PADDING_RESOLVED
         *   1                               PFLAG2_DRAWABLE_RESOLVED
         *  1                                PFLAG2_HAS_TRANSIENT_STATE
         * |-------|-------|-------|-------|
         */

        const val PFLAG2_DRAG_CAN_ACCEPT = 0x00000001
        const val PFLAG2_DRAG_HOVERED = 0x00000002
        const val PFLAG_INVALIDATED = -0x80000000
        const val PFLAG_ACTIVATED = 0x40000000

        const val PFLAG2_LAYOUT_DIRECTION_MASK_SHIFT = 2
        val PFLAG2_LAYOUT_DIRECTION_MASK = 0x00000003 shl PFLAG2_LAYOUT_DIRECTION_MASK_SHIFT
        val PFLAG2_LAYOUT_DIRECTION_RESOLVED_RTL = 4 shl PFLAG2_LAYOUT_DIRECTION_MASK_SHIFT
        val PFLAG2_LAYOUT_DIRECTION_RESOLVED = 8 shl PFLAG2_LAYOUT_DIRECTION_MASK_SHIFT
        val PFLAG2_LAYOUT_DIRECTION_RESOLVED_MASK = 0x0000000C shl PFLAG2_LAYOUT_DIRECTION_MASK_SHIFT
        val LAYOUT_DIRECTION_FLAGS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            intArrayOf(LAYOUT_DIRECTION_LTR, LAYOUT_DIRECTION_RTL, LAYOUT_DIRECTION_INHERIT, LAYOUT_DIRECTION_LOCALE)
        } else {
            intArrayOf(LAYOUT_DIRECTION_RTL, LAYOUT_DIRECTION_INHERIT, LAYOUT_DIRECTION_LOCALE)
        }
        const val LAYOUT_DIRECTION_DEFAULT = LAYOUT_DIRECTION_INHERIT
        const val LAYOUT_DIRECTION_RESOLVED_DEFAULT = LAYOUT_DIRECTION_LTR

        const val TEXT_DIRECTION_DEFAULT = TEXT_DIRECTION_INHERIT
        const val TEXT_DIRECTION_RESOLVED_DEFAULT = TEXT_DIRECTION_FIRST_STRONG

        const val PFLAG2_TEXT_DIRECTION_MASK_SHIFT = 6
        val PFLAG2_TEXT_DIRECTION_MASK = 0x00000007 shl PFLAG2_TEXT_DIRECTION_MASK_SHIFT
        val PFLAG2_TEXT_DIRECTION_FLAGS = intArrayOf(
                TEXT_DIRECTION_INHERIT shl PFLAG2_TEXT_DIRECTION_MASK_SHIFT,
                TEXT_DIRECTION_FIRST_STRONG shl PFLAG2_TEXT_DIRECTION_MASK_SHIFT,
                TEXT_DIRECTION_ANY_RTL shl PFLAG2_TEXT_DIRECTION_MASK_SHIFT,
                TEXT_DIRECTION_LTR shl PFLAG2_TEXT_DIRECTION_MASK_SHIFT,
                TEXT_DIRECTION_RTL shl PFLAG2_TEXT_DIRECTION_MASK_SHIFT,
                TEXT_DIRECTION_LOCALE shl PFLAG2_TEXT_DIRECTION_MASK_SHIFT,
                TEXT_DIRECTION_FIRST_STRONG_LTR shl PFLAG2_TEXT_DIRECTION_MASK_SHIFT,
                TEXT_DIRECTION_FIRST_STRONG_RTL shl PFLAG2_TEXT_DIRECTION_MASK_SHIFT
        )

        const val PFLAG2_TEXT_ALIGNMENT_MASK_SHIFT = 13
        val PFLAG2_TEXT_ALIGNMENT_MASK = 0x00000007 shl PFLAG2_TEXT_ALIGNMENT_MASK_SHIFT
        val PFLAG2_TEXT_ALIGNMENT_FLAGS = intArrayOf(
                TEXT_ALIGNMENT_INHERIT shl PFLAG2_TEXT_ALIGNMENT_MASK_SHIFT,
                TEXT_ALIGNMENT_GRAVITY shl PFLAG2_TEXT_ALIGNMENT_MASK_SHIFT,
                TEXT_ALIGNMENT_TEXT_START shl PFLAG2_TEXT_ALIGNMENT_MASK_SHIFT,
                TEXT_ALIGNMENT_TEXT_END shl PFLAG2_TEXT_ALIGNMENT_MASK_SHIFT,
                TEXT_ALIGNMENT_CENTER shl PFLAG2_TEXT_ALIGNMENT_MASK_SHIFT,
                TEXT_ALIGNMENT_VIEW_START shl PFLAG2_TEXT_ALIGNMENT_MASK_SHIFT,
                TEXT_ALIGNMENT_VIEW_END shl PFLAG2_TEXT_ALIGNMENT_MASK_SHIFT
        )
        val PFLAG2_TEXT_ALIGNMENT_RESOLVED = 0x00000008 shl PFLAG2_TEXT_ALIGNMENT_MASK_SHIFT

        const val TEXT_ALIGNMENT_RESOLVED_DEFAULT = TEXT_ALIGNMENT_GRAVITY
        const val TEXT_ALIGNMENT_DEFAULT = TEXT_ALIGNMENT_GRAVITY

        const val PFLAG2_IMPORTANT_FOR_ACCESSIBILITY_SHIFT = 20
        const val IMPORTANT_FOR_ACCESSIBILITY_DEFAULT = IMPORTANT_FOR_ACCESSIBILITY_AUTO
        val PFLAG2_IMPORTANT_FOR_ACCESSIBILITY_MASK = ((IMPORTANT_FOR_ACCESSIBILITY_AUTO
                or IMPORTANT_FOR_ACCESSIBILITY_YES or IMPORTANT_FOR_ACCESSIBILITY_NO
                or IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS)
                shl PFLAG2_IMPORTANT_FOR_ACCESSIBILITY_SHIFT)
        const val PFLAG2_ACCESSIBILITY_LIVE_REGION_SHIFT = 23

        const val ACCESSIBILITY_LIVE_REGION_DEFAULT = ACCESSIBILITY_LIVE_REGION_NONE
        val PFLAG2_ACCESSIBILITY_LIVE_REGION_MASK = ((ACCESSIBILITY_LIVE_REGION_NONE
                or ACCESSIBILITY_LIVE_REGION_POLITE or ACCESSIBILITY_LIVE_REGION_ASSERTIVE)
                shl PFLAG2_ACCESSIBILITY_LIVE_REGION_SHIFT)
        const val PFLAG2_ACCESSIBILITY_FOCUSED = 0x04000000
        const val PFLAG2_SUBTREE_ACCESSIBILITY_STATE_CHANGED = 0x08000000
        const val PFLAG2_VIEW_QUICK_REJECTED = 0x10000000
        const val PFLAG2_PADDING_RESOLVED = 0x20000000
        const val PFLAG2_DRAWABLE_RESOLVED = 0x40000000
        const val PFLAG2_HAS_TRANSIENT_STATE = 0x80000000

        /*
     * Masks for mPrivateFlags3, as generated by dumpFlags():
     *
     * |-------|-------|-------|-------|
     *                                 1 PFLAG3_VIEW_IS_ANIMATING_TRANSFORM
     *                                1  PFLAG3_VIEW_IS_ANIMATING_ALPHA
     *                               1   PFLAG3_IS_LAID_OUT
     *                              1    PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT
     *                             1     PFLAG3_CALLED_SUPER
     *                            1      PFLAG3_APPLYING_INSETS
     *                           1       PFLAG3_FITTING_SYSTEM_WINDOWS
     *                          1        PFLAG3_NESTED_SCROLLING_ENABLED
     *                         1         PFLAG3_SCROLL_INDICATOR_TOP
     *                        1          PFLAG3_SCROLL_INDICATOR_BOTTOM
     *                       1           PFLAG3_SCROLL_INDICATOR_LEFT
     *                      1            PFLAG3_SCROLL_INDICATOR_RIGHT
     *                     1             PFLAG3_SCROLL_INDICATOR_START
     *                    1              PFLAG3_SCROLL_INDICATOR_END
     *                   1               PFLAG3_ASSIST_BLOCKED
     *                  1                PFLAG3_CLUSTER
     *                 1                 PFLAG3_IS_AUTOFILLED
     *                1                  PFLAG3_FINGER_DOWN
     *               1                   PFLAG3_FOCUSED_BY_DEFAULT
     *           1111                    PFLAG3_IMPORTANT_FOR_AUTOFILL
     *          1                        PFLAG3_OVERLAPPING_RENDERING_FORCED_VALUE
     *         1                         PFLAG3_HAS_OVERLAPPING_RENDERING_FORCED
     *        1                          PFLAG3_TEMPORARY_DETACH
     *       1                           PFLAG3_NO_REVEAL_ON_FOCUS
     *      1                            PFLAG3_NOTIFY_AUTOFILL_ENTER_ON_LAYOUT
     *     1                             PFLAG3_SCREEN_READER_FOCUSABLE
     *    1                              PFLAG3_AGGREGATED_VISIBLE
     *   1                               PFLAG3_AUTOFILLID_EXPLICITLY_SET
     *  1                                PFLAG3_ACCESSIBILITY_HEADING
     * |-------|-------|-------|-------|
     */

        const val PFLAG3_VIEW_IS_ANIMATING_TRANSFORM = 0x1
        const val PFLAG3_VIEW_IS_ANIMATING_ALPHA = 0x2
        const val PFLAG3_IS_LAID_OUT = 0x4
        const val PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT = 0x8
        const val PFLAG3_CALLED_SUPER = 0x10
        const val PFLAG3_APPLYING_INSETS = 0x20
        const val PFLAG3_FITTING_SYSTEM_WINDOWS = 0x40
        const val PFLAG3_NESTED_SCROLLING_ENABLED = 0x80
        const val PFLAG3_SCROLL_INDICATOR_TOP = 0x0100
        const val PFLAG3_SCROLL_INDICATOR_BOTTOM = 0x0200
        const val PFLAG3_SCROLL_INDICATOR_LEFT = 0x0400
        const val PFLAG3_SCROLL_INDICATOR_RIGHT = 0x0800
        const val PFLAG3_SCROLL_INDICATOR_START = 0x1000
        const val PFLAG3_SCROLL_INDICATOR_END = 0x2000
        val DRAG_MASK: Int = PFLAG2_DRAG_CAN_ACCEPT or PFLAG2_DRAG_HOVERED
        const val SCROLL_INDICATORS_NONE = 0x0000
        val SCROLL_INDICATORS_PFLAG3_MASK: Int = (PFLAG3_SCROLL_INDICATOR_TOP
                or PFLAG3_SCROLL_INDICATOR_BOTTOM or PFLAG3_SCROLL_INDICATOR_LEFT
                or PFLAG3_SCROLL_INDICATOR_RIGHT or PFLAG3_SCROLL_INDICATOR_START
                or PFLAG3_SCROLL_INDICATOR_END)
        const val SCROLL_INDICATORS_TO_PFLAGS3_LSHIFT = 8
        const val PFLAG3_ASSIST_BLOCKED = 0x4000
        const val PFLAG3_CLUSTER = 0x8000
        const val PFLAG3_IS_AUTOFILLED = 0x10000
        const val PFLAG3_FINGER_DOWN = 0x20000
        const val PFLAG3_FOCUSED_BY_DEFAULT = 0x40000
        const val PFLAG3_IMPORTANT_FOR_AUTOFILL_SHIFT = 19
        val PFLAG3_IMPORTANT_FOR_AUTOFILL_MASK = ((IMPORTANT_FOR_AUTOFILL_AUTO
                or IMPORTANT_FOR_AUTOFILL_YES or IMPORTANT_FOR_AUTOFILL_NO
                or IMPORTANT_FOR_AUTOFILL_YES_EXCLUDE_DESCENDANTS
                or IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS)
                shl PFLAG3_IMPORTANT_FOR_AUTOFILL_SHIFT)
        const val PFLAG3_OVERLAPPING_RENDERING_FORCED_VALUE = 0x800000
        const val PFLAG3_HAS_OVERLAPPING_RENDERING_FORCED = 0x1000000
        const val PFLAG3_TEMPORARY_DETACH = 0x2000000
        const val PFLAG3_NO_REVEAL_ON_FOCUS = 0x4000000
        const val PFLAG3_NOTIFY_AUTOFILL_ENTER_ON_LAYOUT = 0x8000000
        const val PFLAG3_SCREEN_READER_FOCUSABLE = 0x10000000
        const val PFLAG3_AGGREGATED_VISIBLE = 0x20000000
        const val PFLAG3_AUTOFILLID_EXPLICITLY_SET = 0x40000000
        const val PFLAG3_ACCESSIBILITY_HEADING = 0x80000000

        const val ENABLED = 0x00000000
        const val DISABLED = 0x00000020
        const val ENABLED_MASK = 0x00000020

        const val WILL_NOT_DRAW = 0x00000080
        const val DRAW_MASK = 0x00000080

        const val SCROLLBARS_NONE = 0x00000000
        const val SCROLLBARS_HORIZONTAL = 0x00000100
        const val SCROLLBARS_VERTICAL = 0x00000200
        const val SCROLLBARS_MASK = 0x00000300

        const val FILTER_TOUCHES_WHEN_OBSCURED = 0x00000400

        const val OPTIONAL_FITS_SYSTEM_WINDOWS = 0x00000800

        const val FADING_EDGE_NONE = 0x00000000
        const val FADING_EDGE_HORIZONTAL = 0x00001000
        const val FADING_EDGE_VERTICAL = 0x00002000
        const val FADING_EDGE_MASK = 0x00003000

        const val SCROLLBARS_INSET_MASK = 0x01000000
        const val SCROLLBARS_OUTSIDE_MASK = 0x02000000
        const val SCROLLBARS_STYLE_MASK = 0x03000000

        const val PARENT_SAVE_DISABLED = 0x20000000
        const val PARENT_SAVE_DISABLED_MASK = 0x20000000
        const val TOOLTIP = 0x40000000

        const val PROVIDER_BACKGROUND = 0
        const val PROVIDER_NONE = 1
        const val PROVIDER_BOUNDS = 2
        const val PROVIDER_PADDED_BOUNDS = 3
    }
}

abstract class MyViewGroup : ViewGroup {
    lateinit var a: MyTypedArray

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    fun init(attrs: AttributeSet) {
        for (i in 0 until a.indexCount) {
            when (val attr = a.getIndex(i)) {
                Res.styleable.ViewGroup_clipChildren -> clipChildren = a.getBoolean(attr, true)
                Res.styleable.ViewGroup_clipToPadding -> clipToPadding = a.getBoolean(attr, true)
                Res.styleable.ViewGroup_animationCache -> isAnimationCacheEnabled = a.getBoolean(attr, true)
                Res.styleable.ViewGroup_persistentDrawingCache -> persistentDrawingCache = a.getInt(attr, PERSISTENT_SCROLLING_CACHE)
                Res.styleable.ViewGroup_addStatesFromChildren -> setAddStatesFromChildren(a.getBoolean(attr, false))
                Res.styleable.ViewGroup_alwaysDrawnWithCache -> isAlwaysDrawnWithCacheEnabled = a.getBoolean(attr, true)
                Res.styleable.ViewGroup_layoutAnimation -> {
                    val id = a.getResourceId(attr, -1)
                    if (id > 0) layoutAnimation = AnimationUtils.loadLayoutAnimation(context, id)
                }
                Res.styleable.ViewGroup_descendantFocusability -> descendantFocusability = DESCENDANT_FOCUSABILITY_FLAGS[a.getInt(attr, 0)]
                Res.styleable.ViewGroup_splitMotionEvents -> isMotionEventSplittingEnabled = a.getBoolean(attr, false)
                Res.styleable.ViewGroup_animateLayoutChanges -> if (a.getBoolean(attr, false)) layoutTransition = LayoutTransition()
                Res.styleable.ViewGroup_layoutMode -> versionMethod(Build.VERSION_CODES.LOLLIPOP) { layoutMode = a.getInt(attr, LAYOUT_MODE_UNDEFINED) }
                Res.styleable.ViewGroup_transitionGroup -> versionMethod(Build.VERSION_CODES.LOLLIPOP) { isTransitionGroup = a.getBoolean(attr, false) }
                Res.styleable.ViewGroup_touchscreenBlocksFocus -> versionMethod(Build.VERSION_CODES.LOLLIPOP) { touchscreenBlocksFocus = a.getBoolean(attr, false) }
            }
        }
    }

    companion object {
        val DESCENDANT_FOCUSABILITY_FLAGS = intArrayOf(FOCUS_BEFORE_DESCENDANTS, FOCUS_AFTER_DESCENDANTS, FOCUS_BLOCK_DESCENDANTS)
        const val LAYOUT_MODE_UNDEFINED = -1
    }
}

open class MyTextView(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : TextView(context) {
    init {
    }
}
