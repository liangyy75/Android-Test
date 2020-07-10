@file:Suppress("UNUSED_ANONYMOUS_PARAMETER", "UNCHECKED_CAST", "unused", "DEPRECATION", "MemberVisibilityCanBePrivate")

package com.liang.example.xml_inflater2

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.res.XmlResourceParser
import android.graphics.BlendMode
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.LocaleList
import android.text.InputFilter
import android.util.Log
import android.util.TypedValue
import android.view.PointerIcon
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.liang.example.basic_ktx.ReflectHelper
import com.liang.example.basic_ktx.toInt
import com.liang.example.xml_inflater.R
import com.liang.example.xml_inflater2.AtomicType.Companion.RES_TYPE
import com.liang.example.xml_inflater2.ViewParserHelper.parseBlendMode
import com.liang.example.xml_inflater2.ViewParserHelper.parseTintMode
import com.liang.example.xml_inflater2.ViewParserHelper.setOutlineProviderFromAttribute
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.lang.Exception

object ViewParserHelper {
    private var sParser: XmlResourceParser? = null

    fun generateDefaultLayoutParams(parent: ViewGroup): ViewGroup.LayoutParams? {
        if (null == sParser) {
            synchronized(ViewParserHelper::class.java) {
                if (null == sParser) {
                    initializeAttributeSet(parent)
                }
            }
        }
        return parent.generateLayoutParams(sParser)
    }

    private fun initializeAttributeSet(parent: ViewGroup) {
        sParser = parent.resources.getLayout(R.layout.layout_params_hack)
        try {
            while (sParser!!.nextToken() != XmlPullParser.START_TAG) {
                // Skip everything until the view tag.
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("NewApi")
    fun parseBlendMode(value: Int, defaultMode: BlendMode?): BlendMode? {
        return when (value) {
            3 -> BlendMode.SRC_OVER
            5 -> BlendMode.SRC_IN
            9 -> BlendMode.SRC_ATOP
            14 -> BlendMode.MODULATE
            15 -> BlendMode.SCREEN
            16 -> BlendMode.PLUS
            else -> defaultMode
        }
    }

    fun parseTintMode(value: Int, defaultMode: PorterDuff.Mode?): PorterDuff.Mode? {
        return when (value) {
            3 -> PorterDuff.Mode.SRC_OVER
            5 -> PorterDuff.Mode.SRC_IN
            9 -> PorterDuff.Mode.SRC_ATOP
            14 -> PorterDuff.Mode.MULTIPLY
            15 -> PorterDuff.Mode.SCREEN
            16 -> PorterDuff.Mode.ADD
            else -> defaultMode
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setOutlineProviderFromAttribute(view: View, providerInt: Int) {
        when (providerInt) {
            /*View.PROVIDER_BACKGROUND*/ 0 -> view.outlineProvider = ViewOutlineProvider.BACKGROUND
            /*View.PROVIDER_NONE*/ 1 -> view.outlineProvider = null
            /*View.PROVIDER_BOUNDS*/ 2 -> view.outlineProvider = ViewOutlineProvider.BOUNDS
            /*View.PROVIDER_PADDED_BOUNDS*/ 3 -> view.outlineProvider = ViewOutlineProvider.PADDED_BOUNDS
        }
    }
}

abstract class BaseViewParser<V : View>(
        rootName: String, resIntType: Int,
        open val depends: MutableList<BaseViewParser<*>>? = null, open val attrHelpers: MutableMap<String, AttrHelper<*, V>> = mutableMapOf(),
        open val childAttrHelpers: MutableMap<String, AttrHelper<*, View>> = mutableMapOf(),
        otherRootNames: Array<String> = arrayOf(), careChild: Boolean = false, careName: Boolean = false
) : ResProcessor<V>(rootName, careChild, careName, resIntType, *otherRootNames) {
    init {
        @Suppress("LeakingThis")
        prepare()
    }

    abstract fun prepare()
    abstract fun makeView(node: Node): V?

    // process

    override fun innerProcess(node: Node): V? {
        val result = makeView(node) ?: return makeFail("create view($rootName) failed")
        val parent = p
        p = null
        laterAttrHelpers.clear()
        multiAttrHelpers.clear()

        node.attributes.forEach {
            val attrHelper = this[it.name] ?: return@forEach
            try {
                attrHelper.process(it.name, it.value, result, parent)
            } catch (e: Exception) {
                Log.e(tag, "process error: ${it.name}, ${it.value}, $result, $parent", e)
                if (throwFlag) {
                    throw e
                }
            }
            if (attrHelper is MultiAttrHelper) {
                multiAttrHelpers.add(attrHelper)
            } else if (attrHelper.later != NOT_USE_AFTER) {
                laterAttrHelpers.add(attrHelper)
            }
        }
        multiAttrHelpers.forEach {
            if (it.later == NOT_USE_AFTER) {
                it.process2(result, parent)
            }
        }

        if (result is ViewGroup && node.children.isNotEmpty()) {
            node.children.forEach childForEach@{ childNode: Node ->
                val processor: BaseViewParser<View> = rpm[childNode.name] as? BaseViewParser<View>
                        ?: return@childForEach makeFail("get processor failed: ${childNode.name}", Unit)!!
                processor.p = result
                val child = processor.process(childNode)?.value()
                        ?: return@childForEach makeFail("create child view(${childNode.name}) failed", Unit)!!
                child.layoutParams = ViewParserHelper.generateDefaultLayoutParams(result)
                childNode.attributes.forEach {
                    getForChild(it.name)?.process(it.name, it.value, child, result)
                }
                processor.notifyWithState(child, AFTER_LAYOUT_PARAMS, result)
                result.addView(child)
                processor.notifyWithState(child, AFTER_ADDED, result)
            }
            notifyWithState(result, AFTER_ADD_CHILDREN, parent)
        }

        return result
    }

    open var p: ViewGroup? = null
    open val multiAttrHelpers = mutableSetOf<MultiAttrHelper<*, View>>()
    open val laterAttrHelpers = mutableSetOf<AttrHelper<*, View>>()
    open fun notifyWithState(v: V, state: Int, p: ViewGroup? = null) {
        val parent = p ?: v.parent as? ViewGroup
        laterAttrHelpers.forEach {
            if (it.later == state) {
                it.processLater(v, parent)
            }
        }
        multiAttrHelpers.forEach {
            if (it.later == state) {
                it.process2(v, parent)
            }
        }
    }

    // get attrHelper

    open operator fun get(attrName: String): AttrHelper<*, View>? {
        var result: AttrHelper<*, View>? = attrHelpers[attrName] as? AttrHelper<*, View>
        if (result == null && !depends.isNullOrEmpty()) {
            for (dependParser in depends!!) {
                result = dependParser[attrName]
                if (result != null) {
                    break
                }
            }
        }
        return result
    }

    open fun getForChild(attrName: String): AttrHelper<*, View>? {
        var result: AttrHelper<*, View>? = childAttrHelpers[attrName]
        if (result == null && !depends.isNullOrEmpty()) {
            for (dependParser in depends!!) {
                result = dependParser.getForChild(attrName)
                if (result != null) {
                    break
                }
            }
        }
        return result
    }

    // register attrHelper for self

    open fun <T> register(attr: Attr, later: Int = NOT_USE_AFTER, action: (a: Attr, v1: V, p: ViewGroup?, v2: T?) -> Unit): AttrHelper<T?, V> {
        val attrHelper = AttrHelper(apm, rpm, this, attr, action, later)
        attrHelpers[attr.name] = attrHelper
        return attrHelper
    }

    open fun <T> registerNotNull(attr: Attr, later: Int = NOT_USE_AFTER, action: (a: Attr, v1: V, p: ViewGroup?, v2: T) -> Unit): NotNullAttrHelper<T, V> {
        val attrHelper = NotNullAttrHelper(apm, rpm, this, attr, action, later)
        attrHelpers[attr.name] = attrHelper
        return attrHelper
    }

    open fun <T> registerMulti(
            vararg attrAndDefaults: Pair<Attr, T?>, later: Int = NOT_USE_AFTER,
            action2: (v: V, p: ViewGroup?, vs: MutableMap<Attr, T?>, helper: MultiAttrHelper<T, V>) -> Unit
    ): MultiAttrHelper<T, V> {
        val multiAttrHelper = MultiAttrHelper(apm, rpm, this, attrAndDefaults[0].first, attrAndDefaults.toMap().toMutableMap(), action2, later)
        attrAndDefaults.forEach { attrHelpers[it.first.name] = multiAttrHelper }
        return multiAttrHelper
    }

    // register attrHelper for child -- all after layout_params

    open fun <T> registerForChild(attr: Attr, action: (a: Attr, v1: View, p: ViewGroup?, v2: T?) -> Unit): AttrHelper<T?, View> {
        val attrHelper = AttrHelper(apm, rpm, this, attr, action)
        childAttrHelpers[attr.name] = attrHelper
        return attrHelper
    }

    open fun <T> registerNotNullForChild(attr: Attr, action: (a: Attr, v1: View, p: ViewGroup?, v2: T) -> Unit): NotNullAttrHelper<T, View> {
        val attrHelper = NotNullAttrHelper(apm, rpm, this, attr, action)
        childAttrHelpers[attr.name] = attrHelper
        return attrHelper
    }

    open fun <T> registerMultiForChild(
            vararg attrAndDefaults: Pair<Attr, T?>,
            action2: (v: View, p: ViewGroup?, vs: MutableMap<Attr, T?>, helper: MultiAttrHelper<T, View>) -> Unit
    ): MultiAttrHelper<T, View> {
        val multiAttrHelper = MultiAttrHelper(apm, rpm, this, attrAndDefaults[0].first, attrAndDefaults.toMap().toMutableMap(), action2)
        attrAndDefaults.forEach { childAttrHelpers[it.first.name] = multiAttrHelper }
        return multiAttrHelper
    }

    // attrHelpers class defined

    @Suppress("LeakingThis")
    open class AttrHelper<T, V : View>(
            open val apm: IAttrProcessorManager, open val rpm: IResProcessorManager, open val rp: ResProcessor<*>,
            open var attr: Attr, open val action: (a: Attr, v1: V, p: ViewGroup?, v2: T?) -> Unit, open val later: Int = NOT_USE_AFTER
    ) {
        init {
            ATTR_VALUES[attr] = mutableMapOf()
        }

        open var single = attr.format?.contains('|') != true

        open fun process(n: String, s: String?, v: V, p: ViewGroup? = null) {
            val format = attr.format ?: return makeFail("attr's format is wrong", Unit)!!
            val value = when {
                single -> {
                    when (format) {
                        "dimen", "dimension" -> rp.dimen2(s, attr)
                        "fraction" -> rp.fractionValue2(s)
                        "color" -> rp.color2(s, attr)
                        "str", "string" -> rp.str2(s, attr)
                        "bool", "boolean" -> rp.bool2(s, attr)
                        "int", "integer" -> rp.int2(s, attr)
                        "long" -> rp.long2(s, attr)
                        "float" -> rp.float2(s, attr)
                        "refer", "reference" -> rp.refer(s)
                        "enum" -> rp.enum(s, attr)
                        "flag" -> rp.flag(s, attr)
                        else -> s
                    }
                }
                else -> s
            }
            ATTR_VALUES[attr]!![v] = value
            if (later == NOT_USE_AFTER && this !is MultiAttrHelper) {
                processLater(v, p)
            }
        }

        open fun processLater(v: V, p: ViewGroup? = null) {
            action(attr, v, p, ATTR_VALUES[attr]!![v] as T?)
            ATTR_VALUES[attr]!!.remove(v)
        }
    }

    open class NotNullAttrHelper<T, V : View>(
            apm: IAttrProcessorManager, rpm: IResProcessorManager, rp: ResProcessor<*>,
            attr: Attr, action: (a: Attr, v1: V, p: ViewGroup?, v2: T) -> Unit, later: Int = NOT_USE_AFTER
    ) : AttrHelper<T, V>(apm, rpm, rp, attr, { a, v1, p, v2 ->
        if (v2 != null) {
            action(a, v1, p, v2)
        }
    }, later)

    @Suppress("LeakingThis")
    open class MultiAttrHelper<T, V : View>(
            apm: IAttrProcessorManager, rpm: IResProcessorManager, rp: ResProcessor<*>, attr: Attr, open val vs: MutableMap<Attr, T?>,
            open val action2: (v1: V, p: ViewGroup?, vs: MutableMap<Attr, T?>, helper: MultiAttrHelper<T, V>) -> Unit, later: Int = NOT_USE_AFTER
    ) : AttrHelper<T, V>(apm, rpm, rp, attr, { a, v1, p, v2 ->
        if (v2 != null) {
            vs[a] = v2
        }
    }, later) {
        open var attrs = vs.keys.map {
            if (it !in ATTR_VALUES) {
                ATTR_VALUES[it] = mutableMapOf()
            }
            Pair(it, it.format?.contains('|') != true)
        }
        open var defaults = vs.toMap()

        override fun process(n: String, s: String?, v: V, p: ViewGroup?) {
            val attrData = attrs.find { it.first.name == n } ?: return
            this.attr = attrData.first
            this.single = attrData.second
            super.process(n, s, v, p)
        }

        open fun process2(v: V, p: ViewGroup? = null) {
            attrs.forEach {
                this.attr = it.first
                processLater(v, p)
            }
            action2(v, p, vs, this)
            defaults.forEach { (_attr, _default) -> vs[_attr] = _default }
        }

        open operator fun get(attr: Attr): T = vs[attr]!!
        open fun getNullable(attr: Attr): T? = vs[attr]
    }

    companion object {
        const val NOT_USE_AFTER = 0
        const val AFTER_LAYOUT_PARAMS = 1  // parent notify child
        const val AFTER_ADDED = 2  // parent notify child
        const val AFTER_ADD_CHILDREN = 2  // parent self

        val ATTR_VALUES = mutableMapOf<Attr, MutableMap<View, Any?>>()
    }

    // helper method

    open fun loadDrawable(s: String?): Drawable? = when (s) {
        null -> null
        else -> when (val colorValue = color2(s)) {
            null -> when (val drawResId = refer(s)) {
                null, 0 -> null
                else -> ResStore.loadDrawable(drawResId, apm.context)
            }
            else -> ColorDrawable(colorValue)
        }
    }

    open fun loadColorStateList(s: String?): ColorStateList? = when (s) {
        null -> null
        else -> when (val colorValue = color2(s)) {
            null -> when (val drawResId = refer(s)) {
                null, 0 -> null
                else -> ResStore.loadColorStateList(drawResId, apm.context)
            }
            else -> ColorStateList.valueOf(colorValue)
        }
    }
}

object ResViewType {
    val VIEW = RES_TYPE.inc("View")
    val VIEW_GROUP = RES_TYPE.inc("ViewGroup")
    val VIEW_TEXT = RES_TYPE.inc("TextView")
    val VIEW_BUTTON = RES_TYPE.inc("Button")
    val VIEW_EDIT = RES_TYPE.inc("EditText")
    val VIEW_IMAGE = RES_TYPE.inc("ImageView")
    val VIEW_IMAGE_BUTTON = RES_TYPE.inc("ImageButton")

    val LAYOUT_FRAME = RES_TYPE.inc("FrameLayout")
    val LAYOUT_LINEAR = RES_TYPE.inc("LinearLayout")
    val LAYOUT_RELATIVE = RES_TYPE.inc("RelativeLayout")

    val VIEW_SCROLL = RES_TYPE.inc("ScrollView")
    val VIEW_COMPOUND_BUTTON = RES_TYPE.inc("CompoundButton")
    val VIEW_SWITCH = RES_TYPE.inc("Switch")
    val VIEW_CHECK_BOX = RES_TYPE.inc("CheckBox")
    val VIEW_RADIO_BUTTON = RES_TYPE.inc("RadioButton")
    val VIEW_TOGGLE_BUTTON = RES_TYPE.inc("ToggleButton")
    val VIEW_CLIP = RES_TYPE.inc("Clip")
    val VIEW_FLOATING_ACTION_BUTTON = RES_TYPE.inc("FloatingActionButton")

    val CONTAINER_RADIO_GROUP = RES_TYPE.inc("RadioGroup")
    val CONTAINER_CLIP_GROUP = RES_TYPE.inc("ClipGroup")
    val CONTAINER_ABS_LIST_VIEW = RES_TYPE.inc("AbsListView")
    val CONTAINER_LIST_VIEW = RES_TYPE.inc("ListView")
    val CONTAINER_EXPANDABLE_LIST_VIEW = RES_TYPE.inc("ExpandableListView")
    val CONTAINER_GRID_VIEW = RES_TYPE.inc("GridView")
    val CONTAINER_RECYCLER_VIEW = RES_TYPE.inc("RecyclerView")
    val CONTAINER_VIEW_PAGER = RES_TYPE.inc("ViewPager")
    val LAYOUT_COORDINATOR = RES_TYPE.inc("CoordinatorLayout")
    val LAYOUT_CONSTRAINT = RES_TYPE.inc("ConstraintLayout")
    val LAYOUT_GRID = RES_TYPE.inc("GridLayout")

    val VIEW_PLACE_HOLDER = RES_TYPE.inc("Placeholder")
    val VIEW_GUIDE_LINE = RES_TYPE.inc("Guideline")
    val VIEW_CONSTRAINT_HELPER = RES_TYPE.inc("ConstraintHelper")
    val VIEW_BARRIER = RES_TYPE.inc("Barrier")
    val VIEW_CONSTRAINT_GROUP = RES_TYPE.inc("Group")
    val CONTAINER_CONSTRAINTS = RES_TYPE.inc("Constraints")

    val VIEW_WEB = RES_TYPE.inc("WebView")
    val VIEW_VIDEO = RES_TYPE.inc("VidioView")
    val VIEW_CALENDAR = RES_TYPE.inc("CalendarView")
    val VIEW_DATE_PICKER = RES_TYPE.inc("DatePicker")
    val VIEW_TIME_PICKER = RES_TYPE.inc("TimePicker")
    val VIEW_NUMBER_PICKER = RES_TYPE.inc("NumberPicker")
    val VIEW_PROGRESS_BAR = RES_TYPE.inc("ProgressBar")
    val VIEW_SEEK_BAR = RES_TYPE.inc("SeekBar")
    val VIEW_ABS_SEEK_BAR = RES_TYPE.inc("AbsSeekBar")
    val VIEW_RATING_BAR = RES_TYPE.inc("RatingBar")
    val VIEW_SEARCH = RES_TYPE.inc("SearchView")
    val VIEW_SURFACE = RES_TYPE.inc("SurfaceView")
    val VIEW_TEXTURE = RES_TYPE.inc("TextureView")
    val VIEW_ANIMATOR = RES_TYPE.inc("ViewAnimator")
    val VIEW_FLIPPER = RES_TYPE.inc("ViewFlipper")
    val VIEW_SWITCHER = RES_TYPE.inc("ViewSwitcher")
    val VIEW_TEXT_SWITCHER = RES_TYPE.inc("TextSwitcher")
    val VIEW_IMAGE_SWITCHER = RES_TYPE.inc("ImageSwitcher")

    val VIEW_STUB = RES_TYPE.inc("ViewStub")
}

open class ViewParser : BaseViewParser<View>("View", ResViewType.VIEW) {
    override fun prepare() {
        registerClick()
        registerEnable()
        registerScroll()
        registerFadingEdge()
        registerTransition()
        registerOutline()
        registerText()
        registerAccessibility()
        registerGround()
        registerSizeAndPos()
        registerFocus()
        registerOther()
    }

    private fun registerFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerNotNull(Attrs.View.focusable) { a, v1, p, s: String -> v1.focusable = bool2(s)?.toInt() ?: (enum(s, a) ?: View.FOCUSABLE_AUTO) }
            registerNotNull(Attrs.View.defaultFocusHighlightEnabled) { a, v1, p, v2: Boolean -> v1.defaultFocusHighlightEnabled = v2 }
        } else {
            registerNotNull(Attrs.View.focusable) { a, v1, p, s: String -> v1.isFocusable = bool2(s) ?: true }
        }
        registerNotNull(Attrs.View.focusableInTouchMode) { a, v1, p, v2: Boolean -> v1.isFocusableInTouchMode = v2 }
        registerNotNull(Attrs.View.focusedByDefault) { a, v1, p, v2: Float -> v1.minimumWidth = v2.toInt() }
        registerNotNull(Attrs.View.nextFocusDown) { a, v1, p, v2: Int -> v1.nextFocusDownId = v2 }
        registerNotNull(Attrs.View.nextFocusUp) { a, v1, p, v2: Int -> v1.nextFocusUpId = v2 }
        registerNotNull(Attrs.View.nextFocusLeft) { a, v1, p, v2: Int -> v1.nextFocusLeftId = v2 }
        registerNotNull(Attrs.View.nextFocusRight) { a, v1, p, v2: Int -> v1.nextFocusRightId = v2 }
        registerNotNull(Attrs.View.nextFocusForward) { a, v1, p, v2: Int -> v1.nextFocusForwardId = v2 }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerNotNull(Attrs.View.nextClusterForward) { a, v1, p, v2: Int -> v1.nextClusterForwardId = v2 }
        }
    }

    private fun registerSizeAndPos() {
        registerNotNull(Attrs.View.minHeight) { a, v1, p, v2: Float -> v1.minimumHeight = v2.toInt() }
        registerNotNull(Attrs.View.minWidth) { a, v1, p, v2: Float -> v1.minimumWidth = v2.toInt() }
        registerNotNull(Attrs.Resources.layout_gravity, AFTER_LAYOUT_PARAMS) { a, v1, p, v2: Int -> ReflectHelper.setInt("gravity", v1.layoutParams, v2) }

        registerMulti(Attrs.ViewGroup_Layout.layout_height to -2f, Attrs.ViewGroup_Layout.layout_width to -2f, later = AFTER_LAYOUT_PARAMS) { v, p, vs, helper ->
            val lp = v.layoutParams ?: return@registerMulti
            lp.width = helper[Attrs.ViewGroup_Layout.layout_width].toInt()
            lp.height = helper[Attrs.ViewGroup_Layout.layout_height].toInt()
            v.layoutParams = lp
        }

        registerMulti(
                Attrs.View.padding to 0f, Attrs.View.paddingHorizontal to 0f, Attrs.View.paddingVertical to 0f,
                Attrs.View.paddingLeft to 0f, Attrs.View.paddingRight to 0f, Attrs.View.paddingStart to 0f,
                Attrs.View.paddingEnd to 0f, Attrs.View.paddingTop to 0f, Attrs.View.paddingBottom to 0f, later = AFTER_LAYOUT_PARAMS
        ) { v, p, vs, helper ->
            var left = 0
            var right = 0
            var top = 0
            var bottom = 0
            var value = helper[Attrs.View.padding].toInt()
            if (value != 0) {
                left = value
                right = value
                top = value
                bottom = value
            }
            value = helper[Attrs.View.paddingHorizontal].toInt()
            if (value != 0) {
                left = value
                right = value
            }
            value = helper[Attrs.View.paddingVertical].toInt()
            if (value != 0) {
                top = value
                bottom = value
            }
            if (vs[Attrs.View.paddingLeft] != 0f) {
                left = vs[Attrs.View.paddingLeft]?.toInt() ?: 0
            }
            if (vs[Attrs.View.paddingStart] != 0f) {
                left = vs[Attrs.View.paddingStart]?.toInt() ?: 0
            }
            if (vs[Attrs.View.paddingRight] != 0f) {
                right = vs[Attrs.View.paddingRight]?.toInt() ?: 0
            }
            if (vs[Attrs.View.paddingEnd] != 0f) {
                right = vs[Attrs.View.paddingEnd]?.toInt() ?: 0
            }
            if (vs[Attrs.View.paddingTop] != 0f) {
                top = vs[Attrs.View.paddingTop]?.toInt() ?: 0
            }
            if (vs[Attrs.View.paddingBottom] != 0f) {
                bottom = vs[Attrs.View.paddingBottom]?.toInt() ?: 0
            }
            v.setPadding(left, top, right, bottom)
        }
    }

    private fun registerGround() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            registerNotNull(Attrs.View.background) { a, v1, p, v2: String -> v1.background = loadDrawable(v2) ?: return@registerNotNull }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            registerNotNull(Attrs.View.backgroundTint) { a, v1, p, v2: String ->
                v1.backgroundTintList = loadColorStateList(v2) ?: return@registerNotNull
            }
            register(Attrs.View.backgroundTintMode) { a, v1, p, v2: Int? ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    v1.backgroundTintBlendMode = parseBlendMode(v2 ?: -1, null)
                } else {
                    v1.backgroundTintMode = parseTintMode(v2 ?: -1, null)
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerNotNull(Attrs.View.foreground) { a, v1, p, v2: String ->
                if (v1 is FrameLayout) {
                    v1.foreground = loadDrawable(v2) ?: return@registerNotNull
                }
            }
            registerNotNull(Attrs.View.foregroundGravity) { a, v1, p, v2: Int ->
                if (v1 is FrameLayout) {
                    v1.foregroundGravity = v2
                }
            }
            registerNotNull(Attrs.View.foregroundTint) { a, v1, p, v2: String ->
                if (v1 is FrameLayout) {
                    v1.setForegroundTintList(loadColorStateList(v2) ?: return@registerNotNull)
                }
            }
            register(Attrs.View.foregroundInsidePadding) { a, v1, p, v2: Boolean? ->
                if (v1 is FrameLayout) {
                    var mForegroundInfo = ReflectHelper.get("mForegroundInfo", v1)
                    if (mForegroundInfo == null) {
                        mForegroundInfo = ReflectHelper.newInstance("android.view.View\$ForegroundInfo")
                        ReflectHelper.set("mForegroundInfo", v1, mForegroundInfo)
                    }
                    if (mForegroundInfo != null) {
                        ReflectHelper.setBoolean("mInsidePadding", mForegroundInfo, v2 ?: true)
                    }
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            register(Attrs.View.foregroundTintMode) { a, v1, p, v2: Int? ->
                if (v1 is FrameLayout) {
                    v1.setForegroundTintBlendMode(parseBlendMode(v2 ?: -1, null))
                }
            }
        }
    }

    private fun registerAccessibility() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            registerNotNull(Attrs.View.accessibilityHeading) { a, v1, p, v2: Boolean -> v1.isAccessibilityHeading = v2 }
            registerNotNull(Attrs.View.accessibilityPaneTitle) { a, v1, p, v2: String -> v1.accessibilityPaneTitle = v2 }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            registerNotNull(Attrs.View.accessibilityLiveRegion) { a, v1, p, v2: Int -> v1.accessibilityLiveRegion = v2 }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            registerNotNull(Attrs.View.accessibilityTraversalBefore) { a, v1, p, v2: Int -> v1.accessibilityTraversalBefore = v2 }
            registerNotNull(Attrs.View.accessibilityTraversalAfter) { a, v1, p, v2: Int -> v1.accessibilityTraversalAfter = v2 }
        }
    }

    private fun registerText() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            registerNotNull(Attrs.View.textDirection) { a, v1, p, v2: Int -> v1.textDirection = v2 }
            registerNotNull(Attrs.View.textAlignment) { a, v1, p, v2: Int -> v1.textAlignment = v2 }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerNotNull(Attrs.View.tooltipText) { a, v1, p, v2: String -> v1.tooltipText = v2 }
        }
    }

    private fun registerOutline() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            register(Attrs.View.outlineProvider) { a, v1, p, v2: Int? -> setOutlineProviderFromAttribute(v1, v2 ?: 0) }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            registerNotNull(Attrs.View.outlineAmbientShadowColor) { a, v1, p, v2: Int -> v1.outlineAmbientShadowColor = v2 }
            registerNotNull(Attrs.View.outlineSpotShadowColor) { a, v1, p, v2: Int -> v1.outlineSpotShadowColor = v2 }
        }
    }

    private fun registerOther() {
        registerNotNull(Attrs.View.id) { a, v1, p, v2: Int -> v1.id = v2 }
        registerNotNull(Attrs.View.tag) { a, v1, p, v2: String -> v1.tag = v2 }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            registerNotNull(Attrs.View.labelFor) { a, v1, p, v2: Int -> v1.labelFor = v2 }
        }
        registerNotNull(Attrs.View.visibility) { a, v1, p, v2: Int -> v1.visibility = v2 }
        registerNotNull(Attrs.TextView.enabled) { a, v1, p, v2: Boolean -> v1.isEnabled = v2 }
        registerNotNull(Attrs.View.layerType) { a, v1, p, v2: Int -> v1.setLayerType(v2, null) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            registerNotNull(Attrs.View.layoutDirection) { a, v1, p, v2: Int -> v1.layoutDirection = v2 }
        }
        registerNotNull(Attrs.View.drawingCacheQuality) { a, v1, p, v2: Int -> v1.drawingCacheQuality = v2 }
        registerNotNull(Attrs.View.contentDescription) { a, v1, p, v2: String -> v1.contentDescription = v2 }
        register(Attrs.View.forceDarkAllowed) { a, v1, p, v2: Boolean? ->
            ReflectHelper.setBoolean("setForceDarkAllowed", ReflectHelper.get("mRenderNode", v1), v2 ?: true)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerNotNull(Attrs.View.importantForAutofill) { a, v1, p, v2: Int -> v1.importantForAutofill = v2 }
            registerNotNull(Attrs.View.autofillHints) { a, v1, p, v2: String ->
                val hints = if (v2[0] == '@' || v2[0] == '?') {
                    ResStore.loadStringArray(refer(v2) ?: return@registerNotNull, apm.context)
                } else {
                    v2.split(',').toTypedArray()
                } ?: return@registerNotNull
                v1.setAutofillHints(*hints)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerNotNull(Attrs.View.pointerIcon) { a, v1, p, v2: String ->
                val resourceId = refer(v2) ?: 0
                if (resourceId != 0) {
                    v1.pointerIcon = PointerIcon.load(apm.context.resources, resourceId)
                } else {
                    val pointerType = enum(v2, a) ?: /*PointerIcon.TYPE_NOT_SPECIFIED*/ 1
                    if (pointerType != /*PointerIcon.TYPE_NOT_SPECIFIED*/ 1) {
                        v1.pointerIcon = PointerIcon.getSystemIcon(apm.context, pointerType)
                    }
                }
            }
        }
    }

    private fun registerTransition() {
        registerNotNull(Attrs.View.transformPivotX) { a, v1, p, v2: Float -> v1.pivotX = v2 }
        registerNotNull(Attrs.View.transformPivotY) { a, v1, p, v2: Float -> v1.pivotY = v2 }
        registerNotNull(Attrs.View.scaleX) { a, v1, p, v2: Float -> v1.scaleX = v2 }
        registerNotNull(Attrs.View.scaleY) { a, v1, p, v2: Float -> v1.scaleY = v2 }
        registerNotNull(Attrs.View.translationX) { a, v1, p, v2: Float -> v1.translationX = v2 }
        registerNotNull(Attrs.View.translationY) { a, v1, p, v2: Float -> v1.translationY = v2 }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            registerNotNull(Attrs.View.translationZ) { a, v1, p, v2: Float -> v1.translationZ = v2 }
        }
        registerNotNull(Attrs.View.rotation) { a, v1, p, v2: Float -> v1.rotation = v2 }
        registerNotNull(Attrs.View.rotationX) { a, v1, p, v2: Float -> v1.rotationX = v2 }
        registerNotNull(Attrs.View.rotationY) { a, v1, p, v2: Float -> v1.rotationY = v2 }
        registerNotNull(Attrs.View.alpha) { a, v1, p, v2: Float -> v1.alpha = v2 }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            registerNotNull(Attrs.View.elevation) { a, v1, p, v2: Float -> v1.elevation = v2 }
            registerNotNull(Attrs.View.transitionName) { a, v1, p, v2: String -> v1.transitionName = v2 }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun registerFadingEdge() {
        registerNotNull(Attrs.View.fadingEdgeLength) { a, v1, p, v2: Float -> v1.setFadingEdgeLength(v2.toInt()) }
        val action = { a: Attr, v1: View, p: ViewGroup?, v2: Int ->
            // final int fadingEdge = a.getInt(attr, FADING_EDGE_NONE);
            // if (fadingEdge != FADING_EDGE_NONE) {
            //     viewFlagValues |= fadingEdge;
            //     viewFlagMasks |= FADING_EDGE_MASK;
            //     initializeFadingEdgeInternal(a);
            // }
            TODO("not implemented")
        }
        registerNotNull(Attrs.View.fadingEdge) { a, v1, p, v2: Int ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                action(a, v1, p, v2)
            }
        }
        registerNotNull(Attrs.View.requiresFadingEdge, NOT_USE_AFTER, action)
    }

    private fun registerScroll() {
        registerNotNull(Attrs.View.scrollX) { a, v1, p, v2: Float -> v1.scrollX = v2.toInt() }
        registerNotNull(Attrs.View.scrollY) { a, v1, p, v2: Float -> v1.scrollY = v2.toInt() }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerNotNull(Attrs.View.scrollIndicators) { a, v1, p, v2: Int -> v1.scrollIndicators = v2 }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            registerNotNull(Attrs.View.nestedScrollingEnabled) { a, v1, p, v2: Boolean -> v1.isNestedScrollingEnabled = v2 }
        }

        registerNotNull(Attrs.View.fadeScrollbars) { a, v1, p, v2: Boolean -> TODO("not implemented") }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            registerNotNull(Attrs.View.scrollbarFadeDuration) { a, v1, p, v2: Int -> v1.scrollBarFadeDuration = v2 }
            registerNotNull(Attrs.View.scrollbarDefaultDelayBeforeFade) { a, v1, p, v2: Int -> v1.scrollBarDefaultDelayBeforeFade = v2 }
        }

        registerNotNull(Attrs.View.scrollbarStyle) { a, v1, p, v2: Int -> v1.scrollBarStyle = v2 }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            registerNotNull(Attrs.View.scrollbarSize) { a, v1, p, v2: Float -> v1.scrollBarSize = v2.toInt() }
        }
        registerNotNull(Attrs.View.isScrollContainer) { a, v1, p, v2: Boolean -> v1.isScrollContainer = v2 }
        registerNotNull(Attrs.View.overScrollMode) { a, v1, p, v2: Int -> v1.overScrollMode = v2 }
        registerNotNull(Attrs.View.verticalScrollbarPosition) { a, v1, p, v2: Int -> v1.verticalScrollbarPosition = v2 }

        registerMulti(
                Attrs.View.scrollbars to 0,
                Attrs.View.scrollbarTrackHorizontal to null, Attrs.View.scrollbarThumbHorizontal to null,
                Attrs.View.scrollbarTrackVertical to null, Attrs.View.scrollbarThumbVertical to null,
                Attrs.View.scrollbarAlwaysDrawHorizontalTrack to false, Attrs.View.scrollbarAlwaysDrawVerticalTrack to false
        ) { v, p, vs, helper ->
            val scrollbarsValue = helper[Attrs.View.scrollbars]
            if (scrollbarsValue != 0) {
                // viewFlagValues |= scrollbars;
                // viewFlagMasks |= SCROLLBARS_MASK;
                TODO("not implemented")
            }
        }
    }

    private fun registerClick() {
        registerNotNull(Attrs.View.clickable) { a, v1, p, v2: Boolean -> v1.isClickable = v2 }
        registerNotNull(Attrs.View.longClickable) { a, v1, p, v2: Boolean -> v1.isLongClickable = v2 }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerNotNull(Attrs.View.contextClickable) { a, v1, p, v2: Boolean -> v1.isContextClickable = v2 }
        }
        registerNotNull(Attrs.View.onClick) { a, v1, p, v2: String -> TODO("not implemented") }
    }

    private fun registerEnable() {
        registerNotNull(Attrs.View.hapticFeedbackEnabled) { a, v1, p, v2: Boolean -> v1.isHapticFeedbackEnabled = v2 }
        registerNotNull(Attrs.View.soundEffectsEnabled) { a, v1, p, v2: Boolean -> v1.isSoundEffectsEnabled = v2 }
        registerNotNull(Attrs.View.saveEnabled) { a, v1, p, v2: Boolean -> v1.isSaveEnabled = v2 }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            registerNotNull(Attrs.View.screenReaderFocusable) { a, v1, p, v2: Boolean -> v1.isScreenReaderFocusable = v2 }
        }
        registerNotNull(Attrs.View.fitsSystemWindows) { a, v1, p, v2: Boolean -> v1.fitsSystemWindows = v2 }
        registerNotNull(Attrs.View.duplicateParentState) { a, v1, p, v2: Boolean -> v1.isDuplicateParentStateEnabled = v2 }
        registerNotNull(Attrs.View.keepScreenOn) { a, v1, p, v2: Boolean -> v1.keepScreenOn = v2 }
        registerNotNull(Attrs.View.filterTouchesWhenObscured) { a, v1, p, v2: Boolean -> v1.filterTouchesWhenObscured = v2 }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerNotNull(Attrs.View.keyboardNavigationCluster) { a, v1, p, v2: Boolean -> v1.isKeyboardNavigationCluster = v2 }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerNotNull(Attrs.View.forceHasOverlappingRendering) { a, v1, p, v2: Boolean -> v1.forceHasOverlappingRendering(v2) }
        }
    }

    override fun makeView(node: Node): View? = View(apm.context)
}

open class ViewGroupParser(viewParser: ViewParser) : BaseViewParser<ViewGroup>("ViewGroup", ResViewType.VIEW_GROUP, mutableListOf(viewParser)) {
    override fun prepare() {
        registerNotNull(Attrs.ViewGroup.clipChildren) { a, v1, p, v2: Boolean -> v1.clipChildren = v2 }
        registerNotNull(Attrs.ViewGroup.clipToPadding) { a, v1, p, v2: Boolean -> v1.clipToPadding = v2 }
        registerNotNull(Attrs.ViewGroup.animationCache) { a, v1, p, v2: Boolean -> v1.isAnimationCacheEnabled = v2 }
        registerNotNull(Attrs.ViewGroup.persistentDrawingCache) { a, v1, p, v2: Int -> v1.persistentDrawingCache = v2 }
        registerNotNull(Attrs.ViewGroup.addStatesFromChildren) { a, v1, p, v2: Boolean -> v1.setAddStatesFromChildren(v2) }
        registerNotNull(Attrs.ViewGroup.alwaysDrawnWithCache) { a, v1, p, v2: Boolean -> v1.isAlwaysDrawnWithCacheEnabled = v2 }
        registerNotNull(Attrs.ViewGroup.layoutAnimation) { a, v1, p, v2: Int ->
            v1.layoutAnimation = ResStore.loadLayoutAnimationController(v2, apm.context, true)
        }
        registerNotNull(Attrs.ViewGroup.descendantFocusability) { a, v1, p, v2: Int -> v1.descendantFocusability = v2 }
        registerNotNull(Attrs.ViewGroup.splitMotionEvents) { a, v1, p, v2: Boolean -> v1.isMotionEventSplittingEnabled = v2 }
        registerNotNull(Attrs.ViewGroup.animateLayoutChanges) { a, v1, p, v2: Boolean ->
            if (v2) {
                v1.layoutTransition = LayoutTransition()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            registerNotNull(Attrs.ViewGroup.layoutMode) { a, v1, p, v2: Int -> v1.layoutMode = v2 }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            registerNotNull(Attrs.ViewGroup.transitionGroup) { a, v1, p, v2: Boolean -> v1.isTransitionGroup = v2 }
            registerNotNull(Attrs.ViewGroup.touchscreenBlocksFocus) { a, v1, p, v2: Boolean -> v1.touchscreenBlocksFocus = v2 }
        }
    }

    override fun makeView(node: Node): ViewGroup? = throw RuntimeException("not supported: cannot create $rootName")
}

open class TextViewParser(viewParser: ViewParser) : BaseViewParser<TextView>("TextView", ResViewType.VIEW_TEXT, mutableListOf(viewParser)) {
    override fun prepare() {
        registerNotNull(Attrs.TextView.bufferType) { a, v1, p, v2: Int -> TODO("later") }
        registerNotNull(Attrs.TextView.password) { a, v1, p, v2: Boolean -> TODO("later") }
        registerNotNull(Attrs.TextView.numeric) { a, v1, p, v2: Int -> TODO("later") }
        registerNotNull(Attrs.TextView.digits) { a, v1, p, v2: String -> TODO("later") }
        registerNotNull(Attrs.TextView.phoneNumber) { a, v1, p, v2: Boolean -> TODO("later") }
        registerNotNull(Attrs.TextView.inputMethod) { a, v1, p, v2: String -> TODO("later") }
        registerNotNull(Attrs.TextView.capitalize) { a, v1, p, v2: Int -> TODO("later") }
        registerNotNull(Attrs.TextView.autoText) { a, v1, p, v2: Boolean -> TODO("later") }
        registerNotNull(Attrs.TextView.editable) { a, v1, p, v2: Boolean -> TODO("later") }
        registerNotNull(Attrs.TextView.ellipsize) { a, v1, p, v2: Int -> /*v1.ellipsize = v2*/ TODO("later") }
        registerNotNull(Attrs.TextView.textAllCaps) { a, v1, p, v2: Boolean -> v1.isAllCaps = v2 }

        registerNotNull(Attrs.TextView.text) { a, v1, p, v2: String -> v1.text = v2 }
        registerNotNull(Attrs.TextView.hint) { a, v1, p, v2: String -> v1.hint = v2 }
        registerNotNull(Attrs.TextView.textColor) { a, v1, p, v2: String -> v1.setTextColor(loadColorStateList(v2) ?: return@registerNotNull) }
        registerNotNull(Attrs.TextView.textColorHighlight) { a, v1, p, v2: Int -> v1.highlightColor = v2 }
        registerNotNull(Attrs.TextView.textColorHint) { a, v1, p, v2: String ->
            v1.setHintTextColor(loadColorStateList(v2) ?: return@registerNotNull)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerNotNull(Attrs.TextView.textAppearance) { a, v1, p, v2: Int -> v1.setTextAppearance(v2) }
        }
        registerNotNull(Attrs.TextView.textSize) { a, v1, p, v2: String ->
            if (v2.last() !in digital || v2.startsWith("@")) {
                v1.setTextSize(TypedValue.COMPLEX_UNIT_PX, dimen2(v2) ?: return@registerNotNull)
            } else {
                v1.setTextSize(TypedValue.COMPLEX_UNIT_SP, int2(v2)?.toFloat() ?: return@registerNotNull)
            }
        }
        registerNotNull(Attrs.TextView.textScaleX) { a, v1, p, v2: Float -> v1.textScaleX = v2 }
        // registerNotNull(Attrs.TextView.typeface) { a, v, p1, v2 -> }
        // registerNotNull(Attrs.TextView.textStyle) { a, v, p1, v2 -> }
        // registerNotNull(Attrs.TextView.textFontWeight) { a, v, p1, v2: Int -> }
        // registerNotNull(Attrs.TextView.fontFamily) { a, v, p1, v2 -> }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerNotNull(Attrs.TextView.textLocale) { a, v1, p, v2: String ->
                val locales = LocaleList.forLanguageTags(v2)
                if (!locales.isEmpty) {
                    v1.textLocales = locales
                }
            }
        }
        registerNotNull(Attrs.TextView.textColorLink) { a, v1, p, v2: String ->
            v1.setLinkTextColor(loadColorStateList(v2) ?: return@registerNotNull)
        }

        registerNotNull(Attrs.TextView.maxLines) { a, v1, p, v2: Int -> v1.maxLines = v2 }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            registerNotNull(Attrs.TextView.maxHeight) { a, v1, p, v2: Float -> v1.maxHeight = v2.toInt() }
            registerNotNull(Attrs.TextView.minWidth) { a, v1, p, v2: Float -> v1.minHeight = v2.toInt() }
            registerNotNull(Attrs.TextView.maxWidth) { a, v1, p, v2: Float -> v1.maxWidth = v2.toInt() }
            registerNotNull(Attrs.TextView.minWidth) { a, v1, p, v2: Float -> v1.minWidth = v2.toInt() }
        }
        registerNotNull(Attrs.TextView.lines) { a, v1, p, v2: Int -> v1.setLines(v2) }
        registerNotNull(Attrs.TextView.minLines) { a, v1, p, v2: Int -> v1.minLines = v2 }
        registerNotNull(Attrs.TextView.maxEms) { a, v1, p, v2: Int -> v1.maxEms = v2 }
        registerNotNull(Attrs.TextView.ems) { a, v1, p, v2: Int -> v1.setEms(v2) }
        registerNotNull(Attrs.TextView.minEms) { a, v1, p, v2: Int -> v1.minEms = v2 }

        registerNotNull(Attrs.TextView.gravity) { a, v1, p, v2: Int -> v1.gravity = v2 }

        registerNotNull(Attrs.TextView.cursorVisible) { a, v1, p, v2: Boolean -> v1.isCursorVisible = v2 }
        registerNotNull(Attrs.TextView.scrollHorizontally) { a, v1, p, v2: Boolean -> v1.setHorizontallyScrolling(v2) }
        registerNotNull(Attrs.TextView.singleLine) { a, v1, p, v2: Boolean -> if (v2) v1.setSingleLine() }
        registerNotNull(Attrs.TextView.selectAllOnFocus) { a, v1, p, v2: Boolean -> v1.setSelectAllOnFocus(v2) }
        registerNotNull(Attrs.TextView.includeFontPadding) { a, v1, p, v2: Boolean -> v1.includeFontPadding = v2 }

        registerNotNull(Attrs.TextView.maxLength) { a, v1, p, v2: Int ->
            v1.filters = when {
                v2 >= 0 -> arrayOf(InputFilter.LengthFilter(v2))
                else -> NO_FILTERS
            }
        }
        registerMulti<Any>(
                Attrs.TextView.shadowColor to null, Attrs.TextView.shadowDx to 0f, Attrs.TextView.shadowDy to 0f, Attrs.TextView.shadowRadius to 0f
        ) { v, p, vs, helper ->
            v.setShadowLayer(
                    helper[Attrs.TextView.shadowRadius] as? Float ?: 0f,
                    helper[Attrs.TextView.shadowDx] as? Float ?: 0f,
                    helper[Attrs.TextView.shadowDy] as? Float ?: 0f,
                    helper.getNullable(Attrs.TextView.shadowColor) as? Int ?: 0)
        }
        registerNotNull(Attrs.TextView.autoLink) { a, v1, p, v2: Int -> v1.autoLinkMask = v2 }
        registerNotNull(Attrs.TextView.linksClickable) { a, v1, p, v2: Boolean -> v1.linksClickable = v2 }

        registerNotNull(Attrs.TextView.freezesText) { a, v1, p, v2: Boolean -> v1.freezesText = v2 }

        registerMulti(
                Attrs.TextView.drawableTop to null, Attrs.TextView.drawableBottom to null, Attrs.TextView.drawableLeft to null,
                Attrs.TextView.drawableRight to null, Attrs.TextView.drawableStart to null, Attrs.TextView.drawableEnd to null, Attrs.TextView.drawablePadding to 0f,
                Attrs.TextView.drawableTint to 0, Attrs.TextView.drawableTintMode to 0) { v, p, vs, helper ->
            val left = loadDrawable(helper.getNullable(Attrs.TextView.drawableLeft) as? String)
            val right = loadDrawable(helper.getNullable(Attrs.TextView.drawableRight) as? String)
            val top = loadDrawable(helper.getNullable(Attrs.TextView.drawableTop) as? String)
            val bottom = loadDrawable(helper.getNullable(Attrs.TextView.drawableBottom) as? String)
            val start = loadDrawable(helper.getNullable(Attrs.TextView.drawableStart) as? String)
            val end = loadDrawable(helper.getNullable(Attrs.TextView.drawableEnd) as? String)
            v.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)
            ReflectHelper.findMethod("setRelativeDrawablesIfNeeded", TextView::class.java, Drawable::class.java, Drawable::class.java)?.invoke(v, start, end)
            v.compoundDrawablePadding = (helper[Attrs.TextView.drawablePadding] as? Float)?.toInt() ?: 0
            val drawableTint = loadColorStateList(helper.getNullable(Attrs.TextView.drawableTint) as? String)
            val drawableTintMode = parseBlendMode(helper[Attrs.TextView.drawableTintMode] as? Int ?: 0, null)
            if (drawableTint != null || drawableTintMode != null) {
                var mDrawables = ReflectHelper.get("mDrawables", v)
                if (mDrawables == null) {
                    mDrawables = ReflectHelper.newInstance("android.widget.TextView\$Drawables", apm.context)
                    ReflectHelper.set("mDrawables", v, mDrawables)
                }
                if (drawableTint != null) {
                    ReflectHelper.set("mTintList", mDrawables, drawableTint)
                    ReflectHelper.setBoolean("mHasTint", mDrawables, true)
                }
                if (drawableTintMode != null) {
                    ReflectHelper.set("mBlendMode", mDrawables, drawableTintMode)
                    ReflectHelper.setBoolean("mHasTintMode", mDrawables, true)
                }
            }
        }

        register(Attrs.TextView.lineSpacingExtra) { a, v1, p, v2: Float? -> ReflectHelper.setFloat("mSpacingAdd", v1, v2 ?: 0f) }
        register(Attrs.TextView.lineSpacingMultiplier) { a, v1, p, v2: Float? -> ReflectHelper.setFloat("mSpacingMult", v1, v2 ?: 0f) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            registerNotNull(Attrs.TextView.lineHeight) { a, v1, p, v2: Float -> v1.lineHeight = v2.toInt() }
            registerNotNull(Attrs.TextView.firstBaselineToTopHeight) { a, v1, p, v2: Float -> v1.firstBaselineToTopHeight = v2.toInt() }
            registerNotNull(Attrs.TextView.lastBaselineToBottomHeight) { a, v1, p, v2: Float -> v1.lastBaselineToBottomHeight = v2.toInt() }
        }
        registerNotNull(Attrs.TextView.marqueeRepeatLimit) { a, v1, p, v2: Int -> v1.marqueeRepeatLimit = v2 }
        registerNotNull(Attrs.TextView.inputType) { a, v1, p, v2: Int -> v1.inputType = v2 }
        registerNotNull(Attrs.TextView.allowUndo) { a, v1, p, v2: Boolean ->
            ReflectHelper.invokeN("createEditorIfNeeded", v1)
            val mEditor = ReflectHelper.get("mEditor", v1)
            ReflectHelper.setBoolean("mAllowUndo", mEditor, v2)
        }
        registerNotNull(Attrs.TextView.imeOptions) { a, v1, p, v2: Int -> v1.imeOptions = v2 }
        registerNotNull(Attrs.TextView.privateImeOptions) { a, v1, p, v2: String -> v1.privateImeOptions = v2 }
        registerNotNull(Attrs.TextView.imeActionLabel) { a, v1, p, v2: String -> }
        registerNotNull(Attrs.TextView.imeActionId) { a, v1, p, v2: Int ->
            ReflectHelper.invokeN("createEditorIfNeeded", v1)
            val mEditor = ReflectHelper.get("mEditor", v1)
            ReflectHelper.invokeN("createInputContentTypeIfNeeded", mEditor)
            ReflectHelper.setInt("imeActionId", ReflectHelper.get("mInputContentType", mEditor), v2)
        }
        registerNotNull(Attrs.TextView.editorExtras) { a, v1, p, v2: Int -> v1.setInputExtras(v2) }
        registerNotNull(Attrs.TextView.textSelectHandleLeft) { a, v1, p, v2: Int -> ReflectHelper.setInt("mTextSelectHandleLeftRes", v1, v2) }
        registerNotNull(Attrs.TextView.textSelectHandleRight) { a, v1, p, v2: Int -> ReflectHelper.setInt("mTextSelectHandleRightRes", v1, v2) }
        registerNotNull(Attrs.TextView.textSelectHandle) { a, v1, p, v2: Int -> ReflectHelper.setInt("mTextSelectHandleRes", v1, v2) }
        // registerNotNull(Attrs.TextView.textEditPasteWindowLayout) { a, v1, p, v2 -> }
        // registerNotNull(Attrs.TextView.textEditNoPasteWindowLayout) { a, v1, p, v2 -> }
        // registerNotNull(Attrs.TextView.textEditSidePasteWindowLayout) { a, v1, p, v2 -> }
        // registerNotNull(Attrs.TextView.textEditSideNoPasteWindowLayout) { a, v1, p, v2 -> }
        registerNotNull(Attrs.TextView.textEditSuggestionItemLayout) { a, v1, p, v2: Int ->
            ReflectHelper.setInt("mTextEditSuggestionItemLayout", v1, v2)
        }
        registerNotNull(Attrs.TextView.textEditSuggestionContainerLayout) { a, v1, p, v2: Int ->
            ReflectHelper.setInt("mTextEditSuggestionContainerLayout", v1, v2)
        }
        registerNotNull(Attrs.TextView.textEditSuggestionHighlightStyle) { a, v1, p, v2: Int ->
            ReflectHelper.setInt("mTextEditSuggestionHighlightStyle", v1, v2)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            registerNotNull(Attrs.TextView.textCursorDrawable) { a, v1, p, v2: String -> v1.textCursorDrawable = loadDrawable(v2) }
        }
        registerNotNull(Attrs.TextView.textIsSelectable) { a, v1, p, v2: Boolean -> v1.setTextIsSelectable(v2) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            registerNotNull(Attrs.TextView.elegantTextHeight) { a, v1, p, v2: Boolean -> v1.isElegantTextHeight = v2 }
            registerNotNull(Attrs.TextView.letterSpacing) { a, v1, p, v2: Float -> v1.letterSpacing = v2 }
            registerNotNull(Attrs.TextView.fontFeatureSettings) { a, v1, p, v2: String -> v1.fontFeatureSettings = v2 }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            registerNotNull(Attrs.TextView.fallbackLineSpacing) { a, v1, p, v2: Boolean -> v1.isFallbackLineSpacing = v2 }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerNotNull(Attrs.TextView.fontVariationSettings) { a, v1, p, v2: String -> v1.fontVariationSettings = v2 }
            registerNotNull(Attrs.TextView.autoSizeTextType) { a, v1, p, v2: Int -> v1.setAutoSizeTextTypeWithDefaults(v2) }
            registerNotNull(Attrs.TextView.justificationMode) { a, v1, p, v2: Int -> v1.justificationMode = v2 }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerNotNull(Attrs.TextView.breakStrategy) { a, v1, p, v2: Int -> v1.breakStrategy = v2 }
            registerNotNull(Attrs.TextView.hyphenationFrequency) { a, v1, p, v2: Int -> v1.hyphenationFrequency = v2 }
        }
        registerNotNull(Attrs.TextView.autoSizeStepGranularity) { a, v1, p, v2: Float ->
            ReflectHelper.setFloat("mAutoSizeStepGranularityInPx", v1, v2)
        }
        registerNotNull(Attrs.TextView.autoSizePresetSizes) { a, v1, p, v2: Int ->
            TODO("not implemented")
        }
        registerNotNull(Attrs.TextView.autoSizeMinTextSize) { a, v1, p, v2: Float ->
            ReflectHelper.setFloat("mAutoSizeMinTextSizeInPx", v1, v2)
        }
        registerNotNull(Attrs.TextView.autoSizeMaxTextSize) { a, v1, p, v2: Float ->
            ReflectHelper.setFloat("mAutoSizeMaxTextSizeInPx", v1, v2)
        }
    }

    override fun makeView(node: Node): TextView? = TextView(apm.context)

    companion object {
        private val NO_FILTERS = arrayOfNulls<InputFilter>(0)
    }
}

open class ButtonParser(textViewParser: TextViewParser) : BaseViewParser<Button>("Button", ResViewType.VIEW_BUTTON, mutableListOf(textViewParser)) {
    override fun prepare() = Unit
    override fun makeView(node: Node): Button? = Button(apm.context)
}

open class EditTextParser(textViewParser: TextViewParser) : BaseViewParser<EditText>(
        "EditText", ResViewType.VIEW_EDIT, mutableListOf(textViewParser)) {
    override fun prepare() = Unit
    override fun makeView(node: Node): EditText? = EditText(apm.context)
}

open class ImageViewParser(viewParser: ViewParser) : BaseViewParser<ImageView>("ImageView", ResViewType.VIEW_IMAGE, mutableListOf(viewParser)) {
    override fun prepare() {
        registerNotNull(Attrs.ImageView.src) { a, v1, p, v2: String -> v1.setImageDrawable(loadDrawable(v2) ?: return@registerNotNull) }
        registerNotNull(Attrs.ImageView.scaleType) { a, v1, p, v2: Int -> v1.scaleType = sScaleTypeArray[v2] }
        registerNotNull(Attrs.ImageView.adjustViewBounds) { a, v1, p, v2: Boolean -> v1.adjustViewBounds = v2 }
        registerNotNull(Attrs.ImageView.maxHeight) { a, v1, p, v2: Float -> v1.maxHeight = v2.toInt() }
        registerNotNull(Attrs.ImageView.maxWidth) { a, v1, p, v2: Float -> v1.maxWidth = v2.toInt() }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            registerNotNull(Attrs.ImageView.tint) { a, v1, p, v2: String -> v1.imageTintList = loadColorStateList(v2) ?: return@registerNotNull }
            registerNotNull(Attrs.ImageView.tintMode) { a, v1, p, v2: Int ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    v1.imageTintBlendMode = parseBlendMode(v2, null)
                } else {
                    v1.imageTintMode = parseTintMode(v2, null)
                }
            }
        }
        registerNotNull(Attrs.ImageView.baselineAlignBottom) { a, v1, p, v2: Boolean -> v1.baselineAlignBottom = v2 }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            registerNotNull(Attrs.ImageView.cropToPadding) { a, v1, p, v2: Boolean -> v1.cropToPadding = v2 }
            registerNotNull(Attrs.ImageView.drawableAlpha) { a, v1, p, v2: Int -> v1.imageAlpha = v2 }
        }
        registerNotNull(Attrs.ImageView.baseline) { a, v1, p, v2: Float -> v1.baseline = v2.toInt() }
    }

    override fun makeView(node: Node): ImageView? {
        val result = ImageView(apm.context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && result.importantForAutofill == View.IMPORTANT_FOR_AUTOFILL_AUTO) {
            result.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        }
        return result
    }

    companion object {
        val sScaleTypeArray = arrayOf(
                ScaleType.MATRIX, ScaleType.FIT_XY, ScaleType.FIT_START, ScaleType.FIT_CENTER,
                ScaleType.FIT_END, ScaleType.CENTER, ScaleType.CENTER_CROP, ScaleType.CENTER_INSIDE
        )
    }
}

open class ImageButtonParser(imageViewParser: ImageViewParser) : BaseViewParser<ImageButton>(
        "ImageButton", ResViewType.VIEW_IMAGE_BUTTON, mutableListOf(imageViewParser)) {
    override fun prepare() = Unit
    override fun makeView(node: Node): ImageButton? {
        val result = ImageButton(apm.context)
        result.isFocusable = true
        return result
    }
}

open class FrameLayoutParser(viewGroupParser: ViewGroupParser) : BaseViewParser<FrameLayout>(
        "FrameLayout", ResViewType.LAYOUT_FRAME, mutableListOf(viewGroupParser)) {
    override fun prepare() {
        registerNotNull(Attrs.FrameLayout.measureAllChildren) { a, v1, p, v2: Boolean -> v1.measureAllChildren = v2 }
    }

    override fun makeView(node: Node): FrameLayout? = FrameLayout(apm.context)
}

open class LinearLayoutParser(viewGroupParser: ViewGroupParser) : BaseViewParser<LinearLayout>(
        "LinearLayout", ResViewType.LAYOUT_LINEAR, mutableListOf(viewGroupParser)) {
    override fun prepare() {
        registerNotNull(Attrs.LinearLayout.baselineAligned) { a, v1, p, v2: Boolean -> v1.isBaselineAligned = v2 }
        registerNotNull(Attrs.LinearLayout.baselineAlignedChildIndex) { a, v1, p, v2: Int -> v1.baselineAlignedChildIndex = v2 }
        registerNotNull(Attrs.LinearLayout.measureWithLargestChild) { a, v1, p, v2: Boolean -> v1.isMeasureWithLargestChildEnabled = v2 }
        registerNotNull(Attrs.LinearLayout.divider) { a, v1, p, v2: String -> v1.dividerDrawable = loadDrawable(v2) }
        registerNotNull(Attrs.LinearLayout.dividerPadding) { a, v1, p, v2: Float -> v1.dividerPadding = v2.toInt() }
        registerNotNull(Attrs.LinearLayout.showDividers) { a, v1, p, v2: Int -> v1.showDividers = v2 }
        registerNotNull(Attrs.LinearLayout.orientation) { a, v1, p, v2: Int -> v1.orientation = v2 }
        registerNotNull(Attrs.LinearLayout.weightSum) { a, v1, p, v2: Float -> v1.weightSum = v2 }
        registerNotNull(Attrs.LinearLayout.gravity) { a, v1, p, v2: Int -> v1.gravity = v2 }

        registerMultiForChild(
                Attrs.ViewGroup_MarginLayout.layout_margin to 0f, Attrs.ViewGroup_MarginLayout.layout_marginHorizontal to 0f,
                Attrs.ViewGroup_MarginLayout.layout_marginVertical to 0f, Attrs.ViewGroup_MarginLayout.layout_marginLeft to 0f,
                Attrs.ViewGroup_MarginLayout.layout_marginStart to 0f, Attrs.ViewGroup_MarginLayout.layout_marginRight to 0f,
                Attrs.ViewGroup_MarginLayout.layout_marginEnd to 0f, Attrs.ViewGroup_MarginLayout.layout_marginTop to 0f,
                Attrs.ViewGroup_MarginLayout.layout_marginBottom to 0f
        ) { v, p, vs, helper ->
            val lp = v.layoutParams as? ViewGroup.MarginLayoutParams ?: return@registerMultiForChild
            var left = 0
            var right = 0
            var top = 0
            var bottom = 0
            var value = helper[Attrs.ViewGroup_MarginLayout.layout_margin].toInt()
            if (value != 0) {
                left = value
                right = value
                top = value
                bottom = value
            }
            value = helper[Attrs.ViewGroup_MarginLayout.layout_marginHorizontal].toInt()
            if (value != 0) {
                left = value
                right = value
            }
            value = helper[Attrs.ViewGroup_MarginLayout.layout_marginVertical].toInt()
            if (value != 0) {
                top = value
                bottom = value
            }
            if (vs[Attrs.ViewGroup_MarginLayout.layout_marginLeft] != 0f) {
                left = vs[Attrs.ViewGroup_MarginLayout.layout_marginLeft]?.toInt() ?: 0
            }
            if (vs[Attrs.ViewGroup_MarginLayout.layout_marginStart] != 0f) {
                left = vs[Attrs.ViewGroup_MarginLayout.layout_marginStart]?.toInt() ?: 0
            }
            if (vs[Attrs.ViewGroup_MarginLayout.layout_marginRight] != 0f) {
                right = vs[Attrs.ViewGroup_MarginLayout.layout_marginRight]?.toInt() ?: 0
            }
            if (vs[Attrs.ViewGroup_MarginLayout.layout_marginEnd] != 0f) {
                right = vs[Attrs.ViewGroup_MarginLayout.layout_marginEnd]?.toInt() ?: 0
            }
            if (vs[Attrs.ViewGroup_MarginLayout.layout_marginTop] != 0f) {
                top = vs[Attrs.ViewGroup_MarginLayout.layout_marginTop]?.toInt() ?: 0
            }
            if (vs[Attrs.ViewGroup_MarginLayout.layout_marginBottom] != 0f) {
                bottom = vs[Attrs.ViewGroup_MarginLayout.layout_marginBottom]?.toInt() ?: 0
            }
            lp.setMargins(left, top, right, bottom)
        }

        registerForChild(Attrs.LinearLayout_Layout.layout_weight) { a, v1, p, v2: Float? ->
            val lp = v1.layoutParams as? LinearLayout.LayoutParams ?: return@registerForChild
            lp.weight = v2 ?: return@registerForChild
            v1.layoutParams = lp
        }
    }

    override fun makeView(node: Node): LinearLayout? = LinearLayout(apm.context)
}

open class RelativeLayoutParser(viewGroupParser: ViewGroupParser) : BaseViewParser<RelativeLayout>(
        "RelativeLayout", ResViewType.LAYOUT_RELATIVE, mutableListOf(viewGroupParser)) {
    override fun prepare() {
        registerNotNull(Attrs.RelativeLayout.gravity) { a, v1, p, v2: Int -> v1.gravity = v2 }
        registerNotNull(Attrs.RelativeLayout.ignoreGravity) { a, v1, p, v2: Int -> v1.ignoreGravity = v2 }

        // registerMultiForChild(
        //         Attrs.RelativeLayout_Layout.layout_above to 0, Attrs.RelativeLayout_Layout.layout_below to 0,
        //         Attrs.RelativeLayout_Layout.layout_toLeftOf to 0, Attrs.RelativeLayout_Layout.layout_toRightOf to 0,
        //         Attrs.RelativeLayout_Layout.layout_toStartOf to 0f, Attrs.RelativeLayout_Layout.layout_toEndOf to 0f,
        //         Attrs.RelativeLayout_Layout.layout_alignLeft to 0, Attrs.RelativeLayout_Layout.layout_alignRight to 0,
        //         Attrs.RelativeLayout_Layout.layout_alignStart to 0, Attrs.RelativeLayout_Layout.layout_alignEnd to 0,
        //         Attrs.RelativeLayout_Layout.layout_alignTop to 0, Attrs.RelativeLayout_Layout.layout_alignBottom to 0,
        //         Attrs.RelativeLayout_Layout.layout_alignParentLeft to false, Attrs.RelativeLayout_Layout.layout_alignParentRight to false,
        //         Attrs.RelativeLayout_Layout.layout_alignParentStart to false, Attrs.RelativeLayout_Layout.layout_alignParentEnd to false,
        //         Attrs.RelativeLayout_Layout.layout_alignParentTop to false, Attrs.RelativeLayout_Layout.layout_alignParentBottom to false,
        //         Attrs.RelativeLayout_Layout.layout_centerHorizontal to false, Attrs.RelativeLayout_Layout.layout_centerInParent to false,
        //         Attrs.RelativeLayout_Layout.layout_centerVertical to false, Attrs.RelativeLayout_Layout.layout_alignWithParentIfMissing to false,
        //         Attrs.RelativeLayout_Layout.layout_alignBaseline to 0,
        //         later = AFTER_LAYOUT_PARAMS
        // ) { v, p, vs, helper ->
        //     hahaha
        // }
        registerRelative(Attrs.RelativeLayout_Layout.layout_above, RelativeLayout.ABOVE)
        registerRelative(Attrs.RelativeLayout_Layout.layout_below, RelativeLayout.BELOW)
        registerRelative(Attrs.RelativeLayout_Layout.layout_toLeftOf, RelativeLayout.LEFT_OF)
        registerRelative(Attrs.RelativeLayout_Layout.layout_toRightOf, RelativeLayout.RIGHT_OF)

        registerRelative(Attrs.RelativeLayout_Layout.layout_alignLeft, RelativeLayout.ALIGN_LEFT)
        registerRelative(Attrs.RelativeLayout_Layout.layout_alignRight, RelativeLayout.ALIGN_RIGHT)
        registerRelative(Attrs.RelativeLayout_Layout.layout_alignTop, RelativeLayout.ALIGN_TOP)
        registerRelative(Attrs.RelativeLayout_Layout.layout_alignBottom, RelativeLayout.ALIGN_BOTTOM)
        registerRelative(Attrs.RelativeLayout_Layout.layout_alignParentLeft, RelativeLayout.ALIGN_PARENT_LEFT)
        registerRelative(Attrs.RelativeLayout_Layout.layout_alignParentRight, RelativeLayout.ALIGN_PARENT_RIGHT)
        registerRelative(Attrs.RelativeLayout_Layout.layout_alignParentTop, RelativeLayout.ALIGN_PARENT_TOP)
        registerRelative(Attrs.RelativeLayout_Layout.layout_alignParentBottom, RelativeLayout.ALIGN_PARENT_BOTTOM)

        registerRelative(Attrs.RelativeLayout_Layout.layout_alignBaseline, RelativeLayout.ALIGN_BASELINE)
        registerNotNullForChild(Attrs.RelativeLayout_Layout.layout_alignWithParentIfMissing) { a, v1, p, v2: Boolean ->
            val lp = v1.layoutParams as? RelativeLayout.LayoutParams ?: return@registerNotNullForChild
            ReflectHelper.setBoolean("alignWithParent", lp, v2)
            v1.layoutParams = lp
        }
        registerRelative(Attrs.RelativeLayout_Layout.layout_centerInParent, RelativeLayout.CENTER_IN_PARENT)
        registerRelative(Attrs.RelativeLayout_Layout.layout_centerHorizontal, RelativeLayout.CENTER_HORIZONTAL)
        registerRelative(Attrs.RelativeLayout_Layout.layout_centerVertical, RelativeLayout.CENTER_VERTICAL)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            registerRelative(Attrs.RelativeLayout_Layout.layout_toStartOf, RelativeLayout.START_OF)
            registerRelative(Attrs.RelativeLayout_Layout.layout_toEndOf, RelativeLayout.END_OF)
            registerRelative(Attrs.RelativeLayout_Layout.layout_alignStart, RelativeLayout.ALIGN_START)
            registerRelative(Attrs.RelativeLayout_Layout.layout_alignEnd, RelativeLayout.ALIGN_END)
            registerRelative(Attrs.RelativeLayout_Layout.layout_alignParentStart, RelativeLayout.ALIGN_PARENT_START)
            registerRelative(Attrs.RelativeLayout_Layout.layout_alignParentEnd, RelativeLayout.ALIGN_PARENT_END)
        }
    }

    open fun registerRelative(attr: Attr, verb: Int) {
        registerNotNullForChild(attr) { a, v1, p, v2: Int -> addRelativeLayoutRule(v1, verb, v2) }
    }

    open fun registerRelative2(attr: Attr, verb: Int) {
        registerNotNullForChild(attr) { a, v1, p, v2: Boolean -> addRelativeLayoutRule(v1, verb, v2) }
    }

    open fun addRelativeLayoutRule(view: View, verb: Int, anchor: Boolean) {
        val lp = view.layoutParams as? RelativeLayout.LayoutParams ?: return
        lp.addRule(verb, if (anchor) RelativeLayout.TRUE else 0)
        view.layoutParams = lp
    }

    open fun addRelativeLayoutRule(view: View, verb: Int, anchor: Int) {
        val lp = view.layoutParams as? RelativeLayout.LayoutParams ?: return
        lp.addRule(verb, anchor)
        view.layoutParams = lp
    }

    override fun makeView(node: Node): RelativeLayout? = RelativeLayout(apm.context)
}
