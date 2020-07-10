@file:Suppress("UNUSED_ANONYMOUS_PARAMETER", "unused", "MemberVisibilityCanBePrivate")

package com.liang.example.xml_inflater2

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.PorterDuff
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ExpandableListView
import android.widget.GridLayout
import android.widget.GridLayout.Alignment
import android.widget.GridView
import android.widget.ListView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.Switch
import android.widget.ToggleButton
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.Barrier
import androidx.constraintlayout.widget.ConstraintHelper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Constraints
import androidx.constraintlayout.widget.Group
import androidx.constraintlayout.widget.Guideline
import androidx.constraintlayout.widget.Placeholder
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.animation.AnimationUtils
import com.google.android.material.animation.MotionSpec
import com.google.android.material.animation.MotionTiming
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.shape.AbsoluteCornerSize
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.CornerSize
import com.google.android.material.shape.RelativeCornerSize
import com.google.android.material.shape.ShapeAppearanceModel
import com.liang.example.basic_ktx.ReflectHelper
import com.liang.example.xml_inflater2.ViewParserHelper.parseBlendMode
import com.liang.example.xml_inflater2.ViewParserHelper.parseTintMode
import kotlin.math.abs

open class ScrollViewParser(frameLayoutParser: FrameLayoutParser) : BaseViewParser<ScrollView>(
        "ScrollView", ResViewType.VIEW_SCROLL, mutableListOf(frameLayoutParser)) {
    override fun prepare() {
        registerNotNull(Attrs.ScrollView.fillViewport) { a, v1, p, v2: Boolean -> v1.isFillViewport = v2 }
    }

    override fun makeView(node: Node): ScrollView? = ScrollView(apm.context)
}

open class CompoundButtonParser(buttonParser: ButtonParser) : BaseViewParser<CompoundButton>(
        "CompoundButton", ResViewType.VIEW_COMPOUND_BUTTON, mutableListOf(buttonParser)) {
    override fun prepare() {
        registerNotNull(Attrs.CompoundButton.button) { a, v1, p, v2: String -> v1.buttonDrawable = loadDrawable(v2) ?: return@registerNotNull }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            registerNotNull(Attrs.CompoundButton.buttonTint) { a, v1, p, v2: String ->
                v1.buttonTintList = loadColorStateList(v2) ?: return@registerNotNull
            }
            registerNotNull(Attrs.CompoundButton.buttonTintMode) { a, v1, p, v2: Int ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    v1.buttonTintBlendMode = parseBlendMode(v2, null)
                } else {
                    v1.buttonTintMode = parseTintMode(v2, null)
                }
            }
        }
        registerNotNull(Attrs.CompoundButton.checked) { a, v1, p, v2: Boolean -> v1.isChecked = v2 }
    }

    override fun makeView(node: Node): CompoundButton? = throw RuntimeException("not supported: cannot create $rootName")
}

open class SwitchViewParser(compoundButtonParser: CompoundButtonParser) : BaseViewParser<Switch>(
        "ScrollView", ResViewType.VIEW_SWITCH, mutableListOf(compoundButtonParser)) {
    override fun prepare() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            registerNotNull(Attrs.Switch.thumb) { a, v1, p, v2: Int ->
                v1.thumbDrawable = ResStore.loadDrawable(v2, apm.context, true) ?: return@registerNotNull
            }
            registerNotNull(Attrs.Switch.track) { a, v1, p, v2: Int ->
                v1.trackDrawable = ResStore.loadDrawable(v2, apm.context, true) ?: return@registerNotNull
            }
            registerNotNull(Attrs.Switch.thumbTextPadding) { a, v1, p, v2: Float -> v1.thumbTextPadding = v2.toInt() }
            registerNotNull(Attrs.Switch.switchMinWidth) { a, v1, p, v2: Float -> v1.switchMinWidth = v2.toInt() }
            registerNotNull(Attrs.Switch.switchPadding) { a, v1, p, v2: Float -> v1.switchPadding = v2.toInt() }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerNotNull(Attrs.Switch.thumbTint) { a, v1, p, v2: String -> v1.thumbTintList = loadColorStateList(v2) ?: return@registerNotNull }
            registerNotNull(Attrs.Switch.trackTint) { a, v1, p, v2: String -> v1.trackTintList = loadColorStateList(v2) ?: return@registerNotNull }
            registerNotNull(Attrs.Switch.thumbTintMode) { a, v1, p, v2: Int ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    v1.thumbTintBlendMode = parseBlendMode(v2, null)
                } else {
                    v1.thumbTintMode = parseTintMode(v2, null)
                }
            }
            registerNotNull(Attrs.Switch.trackTintMode) { a, v1, p, v2: Int ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    v1.trackTintBlendMode = parseBlendMode(v2, null)
                } else {
                    v1.trackTintMode = parseTintMode(v2, null)
                }
            }
        }
        registerNotNull(Attrs.Switch.textOn) { a, v1, p, v2: String -> v1.textOn = v2 }
        registerNotNull(Attrs.Switch.textOff) { a, v1, p, v2: String -> v1.textOff = v2 }
        registerNotNull(Attrs.Switch.switchTextAppearance) { a, v1, p, v2: Int -> v1.setSwitchTextAppearance(apm.context, v2) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            registerNotNull(Attrs.Switch.splitTrack) { a, v1, p, v2: Boolean -> v1.splitTrack = v2 }
            registerNotNull(Attrs.Switch.showText) { a, v1, p, v2: Boolean -> v1.showText = v2 }
        }
    }

    override fun makeView(node: Node): Switch? = Switch(apm.context)
}

open class CheckBoxParser(compoundButtonParser: CompoundButtonParser) : BaseViewParser<CheckBox>(
        "CheckBox", ResViewType.VIEW_CHECK_BOX, mutableListOf(compoundButtonParser)) {
    override fun prepare() = Unit
    override fun makeView(node: Node): CheckBox? = CheckBox(apm.context)
}

open class RadioButtonParser(compoundButtonParser: CompoundButtonParser) : BaseViewParser<RadioButton>(
        "RadioButton", ResViewType.VIEW_RADIO_BUTTON, mutableListOf(compoundButtonParser)) {
    override fun prepare() = Unit
    override fun makeView(node: Node): RadioButton? = RadioButton(apm.context)
}

open class RadioGroupParser(linearLayoutParser: LinearLayoutParser) : BaseViewParser<RadioGroup>(
        "RadioGroup", ResViewType.CONTAINER_RADIO_GROUP, mutableListOf(linearLayoutParser)) {
    override fun prepare() {
        registerNotNull(Attrs.RadioGroup.checkedButton, later = AFTER_ADD_CHILDREN) { a, v1, p, v2: Int -> v1.check(v2) }
    }

    override fun makeView(node: Node): RadioGroup? = RadioGroup(apm.context)
}

open class ToggleButtonParser(compoundButtonParser: CompoundButtonParser) : BaseViewParser<ToggleButton>(
        "ToggleButton", ResViewType.VIEW_TOGGLE_BUTTON, mutableListOf(compoundButtonParser)) {
    override fun prepare() {
        registerNotNull(Attrs.ToggleButton.disabledAlpha) { a, v1, p, v2: Float -> ReflectHelper.setFloat("mDisabledAlpha", v1, v2) }
        registerNotNull(Attrs.ToggleButton.textOn) { a, v1, p, v2: String -> v1.textOn = v2 }
        registerNotNull(Attrs.ToggleButton.textOff) { a, v1, p, v2: String -> v1.textOff = v2 }
    }

    override fun makeView(node: Node): ToggleButton? = ToggleButton(apm.context)
}

open class AbsListViewParser(viewGroupParser: ViewGroupParser) : BaseViewParser<AbsListView>(
        "AbsListView", ResViewType.CONTAINER_ABS_LIST_VIEW, mutableListOf(viewGroupParser)) {
    override fun prepare() {
        registerNotNull(Attrs.AbsListView.listSelector) { a, v1, p, v2: String ->
            ReflectHelper.invokeN("setSelector", v1, loadDrawable(v2) ?: return@registerNotNull)
        }
        registerNotNull(Attrs.AbsListView.drawSelectorOnTop) { a, v1, p, v2: Boolean -> v1.isDrawSelectorOnTop = v2 }
        registerNotNull(Attrs.AbsListView.stackFromBottom) { a, v1, p, v2: Boolean -> v1.isStackFromBottom = v2 }
        registerNotNull(Attrs.AbsListView.scrollingCache) { a, v1, p, v2: Boolean -> v1.isScrollingCacheEnabled = v2 }
        registerNotNull(Attrs.AbsListView.textFilterEnabled) { a, v1, p, v2: Boolean -> v1.isTextFilterEnabled = v2 }
        registerNotNull(Attrs.AbsListView.transcriptMode) { a, v1, p, v2: Int -> v1.transcriptMode = v2 }
        registerNotNull(Attrs.AbsListView.cacheColorHint) { a, v1, p, v2: Int -> v1.cacheColorHint = v2 }
        registerNotNull(Attrs.AbsListView.fastScrollEnabled) { a, v1, p, v2: Boolean -> v1.isFastScrollEnabled = v2 }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            registerNotNull(Attrs.AbsListView.fastScrollStyle) { a, v1, p, v2: Int ->
                v1.setFastScrollStyle(v2)
            }
        }
        registerNotNull(Attrs.AbsListView.smoothScrollbar) { a, v1, p, v2: Boolean -> v1.isSmoothScrollbarEnabled = v2 }
        registerNotNull(Attrs.AbsListView.fastScrollAlwaysVisible) { a, v1, p, v2: Boolean -> v1.isFastScrollAlwaysVisible = v2 }
        registerNotNull(Attrs.AbsListView.choiceMode) { a, v1, p, v2: Int -> v1.choiceMode = v2 }
    }

    override fun makeView(node: Node): AbsListView? = throw RuntimeException("not supported: cannot create $rootName")
}

open class ListViewParser(absListViewParser: AbsListViewParser) : BaseViewParser<ListView>(
        "ListView", ResViewType.CONTAINER_LIST_VIEW, mutableListOf(absListViewParser)) {
    override fun prepare() {
        registerNotNull(Attrs.ListView.entries) { a, v1, p, v2: Int ->
            val entries = ResStore.loadStringArray(v2, apm.context, true) ?: return@registerNotNull
            v1.adapter = ArrayAdapter<CharSequence>(apm.context, android.R.layout.simple_list_item_1, entries)
        }
        registerNotNull(Attrs.ListView.divider) { a, v1, p, v2: String -> v1.divider = loadDrawable(v2) ?: return@registerNotNull }
        registerNotNull(Attrs.ListView.dividerHeight) { a, v1, p, v2: Float -> v1.dividerHeight = v2.toInt() }
        registerNotNull(Attrs.ListView.headerDividersEnabled) { a, v1, p, v2: Boolean -> v1.setHeaderDividersEnabled(v2) }
        registerNotNull(Attrs.ListView.footerDividersEnabled) { a, v1, p, v2: Boolean -> v1.setFooterDividersEnabled(v2) }
        registerNotNull(Attrs.ListView.overScrollHeader) { a, v1, p, v2: String -> v1.overscrollHeader = loadDrawable(v2) ?: return@registerNotNull }
        registerNotNull(Attrs.ListView.overScrollFooter) { a, v1, p, v2: String -> v1.overscrollFooter = loadDrawable(v2) ?: return@registerNotNull }
    }

    override fun makeView(node: Node): ListView? = ListView(apm.context)
}

open class ExpandableListViewParser(listViewParser: ListViewParser) : BaseViewParser<ExpandableListView>(
        "ExpandableListView", ResViewType.CONTAINER_EXPANDABLE_LIST_VIEW, mutableListOf(listViewParser)) {
    @SuppressLint("NewApi")
    override fun prepare() {
        registerNotNull(Attrs.ExpandableListView.groupIndicator) { a, v1, p, v2: String ->
            v1.setGroupIndicator(loadDrawable(v2) ?: return@registerNotNull)
        }
        registerNotNull(Attrs.ExpandableListView.childIndicator) { a, v1, p, v2: String ->
            v1.setChildIndicator(loadDrawable(v2) ?: return@registerNotNull)
        }
        registerMulti(Attrs.ExpandableListView.indicatorLeft to 0f, Attrs.ExpandableListView.indicatorRight to 0f) { v, p, vs, helper ->
            v.setIndicatorBounds(helper[Attrs.ExpandableListView.indicatorLeft].toInt(), helper[Attrs.ExpandableListView.indicatorRight].toInt())
        }
        registerMulti(Attrs.ExpandableListView.childIndicatorLeft to 0f, Attrs.ExpandableListView.childIndicatorRight to 0f) { v, p, vs, helper ->
            v.setChildIndicatorBounds(helper[Attrs.ExpandableListView.childIndicatorLeft].toInt(),
                    helper[Attrs.ExpandableListView.childIndicatorRight].toInt())
        }
        registerNotNull(Attrs.ExpandableListView.childDivider) { a, v1, p, v2: String ->
            v1.setChildDivider(loadDrawable(v2) ?: return@registerNotNull)
        }
        if (!isRtlCompatibilityMode()) {
            registerMulti(Attrs.ExpandableListView.indicatorStart to 0f, Attrs.ExpandableListView.indicatorEnd to 0f) { v, p, vs, helper ->
                v.setIndicatorBoundsRelative(helper[Attrs.ExpandableListView.indicatorStart].toInt(),
                        helper[Attrs.ExpandableListView.indicatorEnd].toInt())
            }
            registerMulti(Attrs.ExpandableListView.childIndicatorStart to 0f, Attrs.ExpandableListView.childIndicatorEnd to 0f) { v, p, vs, helper ->
                v.setChildIndicatorBoundsRelative(helper[Attrs.ExpandableListView.childIndicatorStart].toInt(),
                        helper[Attrs.ExpandableListView.childIndicatorEnd].toInt())
            }
        }
    }

    open fun isRtlCompatibilityMode(): Boolean = apm.context.applicationInfo.targetSdkVersion < Build.VERSION_CODES.JELLY_BEAN_MR1 ||
            ReflectHelper.invoke<Boolean>("hasRtlSupport", apm.context.applicationInfo) != true

    override fun makeView(node: Node): ExpandableListView? = ExpandableListView(apm.context)
}

open class GridViewParser(absListViewParser: AbsListViewParser) : BaseViewParser<GridView>(
        "GridView", ResViewType.CONTAINER_LIST_VIEW, mutableListOf(absListViewParser)) {
    override fun prepare() {
        registerNotNull(Attrs.GridView.horizontalSpacing) { a, v1, p, v2: Float -> v1.horizontalSpacing = v2.toInt() }
        registerNotNull(Attrs.GridView.verticalSpacing) { a, v1, p, v2: Float -> v1.verticalSpacing = v2.toInt() }
        registerNotNull(Attrs.GridView.stretchMode) { a, v1, p, v2: Int -> v1.stretchMode = v2 }
        registerNotNull(Attrs.GridView.columnWidth) { a, v1, p, v2: Float -> v1.columnWidth = v2.toInt() }
        registerNotNull(Attrs.GridView.numColumns) { a, v1, p, v2: Int -> v1.numColumns = v2 }
        registerNotNull(Attrs.GridView.gravity) { a, v1, p, v2: Int -> v1.gravity = v2 }
    }

    override fun makeView(node: Node): GridView? = GridView(apm.context)
}

open class RecyclerViewParser(viewGroupParser: ViewGroupParser) : BaseViewParser<RecyclerView>(
        "androidx.recyclerview.widget.RecyclerView", ResViewType.CONTAINER_RECYCLER_VIEW, mutableListOf(viewGroupParser)) {
    override fun prepare() {
        registerMulti(
                Attrs.RecyclerView.layoutManager to null,
                Attrs.RecyclerView.orientation to 1, Attrs.RecyclerView.spanCount to 1,
                Attrs.RecyclerView.reverseLayout to false, Attrs.RecyclerView.stackFromEnd to false) { v, p, vs, helper ->
            val layoutManagerName = helper.getNullable(Attrs.RecyclerView.layoutManager) ?: return@registerMulti
            val orientation = helper[Attrs.RecyclerView.orientation] as? Int ?: 1
            val spanCount = helper[Attrs.RecyclerView.spanCount] as? Int ?: 1
            val reverseLayout = helper[Attrs.RecyclerView.reverseLayout] as? Boolean ?: false
            val stackFromEnd = helper[Attrs.RecyclerView.stackFromEnd] as? Boolean ?: false
            v.layoutManager = when (layoutManagerName) {
                "StaggeredGridLayoutManager" -> {
                    val layoutManager = StaggeredGridLayoutManager(spanCount, orientation)
                    layoutManager.reverseLayout = reverseLayout
                    layoutManager
                }
                "LinearLayoutManager" -> {
                    val layoutManager = LinearLayoutManager(apm.context, orientation, reverseLayout)
                    layoutManager.stackFromEnd = stackFromEnd
                    layoutManager
                }
                "GridLayoutManager" -> GridLayoutManager(apm.context, spanCount)
                else -> createLayoutManagerTask?.invoke(orientation, spanCount, reverseLayout, stackFromEnd) ?: return@registerMulti
            }
        }
    }

    open var createLayoutManagerTask: ((orientation: Int, spanCount: Int, reverseLayout: Boolean, stackFromEnd: Boolean) ->
    RecyclerView.LayoutManager?)? = null

    override fun makeView(node: Node): RecyclerView? = RecyclerView(apm.context)
}

open class ViewPagerParser(viewGroupParser: ViewGroupParser) : BaseViewParser<ViewPager>(
        "androidx.viewpager.widget.ViewPager", ResViewType.CONTAINER_VIEW_PAGER, mutableListOf(viewGroupParser)) {
    override fun prepare() = Unit
    override fun makeView(node: Node): ViewPager? = ViewPager(apm.context)
}

open class CoordinatorLayoutParser(viewGroupParser: ViewGroupParser) : BaseViewParser<CoordinatorLayout>(
        "androidx.coordinatorlayout.widget.CoordinatorLayout", ResViewType.LAYOUT_COORDINATOR, mutableListOf(viewGroupParser)) {
    override fun prepare() {
        registerNotNull(Attrs.CoordinatorLayout.keylines) { a, v1, p, v2: Int ->
            if (v2 != 0) {
                ReflectHelper.set("mKeylines", v1, ResStore.loadIntArray(v2, apm.context, true) ?: return@registerNotNull)
            }
        }
        registerNotNull(Attrs.CoordinatorLayout.statusBarBackground) { a, v1, p, v2: String ->
            v1.statusBarBackground = loadDrawable(v2) ?: return@registerNotNull
        }

        registerMultiForChild<Any>(
                Attrs.CoordinatorLayout_Layout.layout_behavior to null, Attrs.CoordinatorLayout_Layout.layout_keyline to null,
                Attrs.CoordinatorLayout_Layout.layout_anchor to null, Attrs.CoordinatorLayout_Layout.layout_anchorGravity to null,
                Attrs.CoordinatorLayout_Layout.layout_insetEdge to null, Attrs.CoordinatorLayout_Layout.layout_dodgeInsetEdges to null
        ) { v, p, vs, helper ->
            val lp = v.layoutParams as? CoordinatorLayout.LayoutParams ?: return@registerMultiForChild
            lp.behavior  // TODO() -- 很多的 CoordinatorLayout.Behavior
            lp.anchorId = helper.getNullable(Attrs.CoordinatorLayout_Layout.layout_anchor) as? Int ?: View.NO_ID
            lp.keyline = helper.getNullable(Attrs.CoordinatorLayout_Layout.layout_keyline) as? Int ?: -1
            lp.anchorGravity = helper.getNullable(Attrs.CoordinatorLayout_Layout.layout_anchorGravity) as? Int ?: Gravity.NO_GRAVITY
            lp.insetEdge = helper.getNullable(Attrs.CoordinatorLayout_Layout.layout_insetEdge) as? Int ?: Gravity.NO_GRAVITY
            lp.dodgeInsetEdges = helper.getNullable(Attrs.CoordinatorLayout_Layout.layout_dodgeInsetEdges) as? Int ?: Gravity.NO_GRAVITY
            v.layoutParams = lp
        }
    }

    override fun makeView(node: Node): CoordinatorLayout? = CoordinatorLayout(apm.context)
}

open class ConstraintLayoutParser(viewGroupParser: ViewGroupParser) : BaseViewParser<ConstraintLayout>(
        "androidx.constraintlayout.widget.ConstraintLayout", ResViewType.LAYOUT_CONSTRAINT, mutableListOf(viewGroupParser)) {
    override fun prepare() {
        registerNotNull(Attrs.ConstraintLayout_Layout.minWidth) { a, v1, p, v2: Float -> v1.minWidth = v2.toInt() }
        registerNotNull(Attrs.ConstraintLayout_Layout.minHeight) { a, v1, p, v2: Float -> v1.minHeight = v2.toInt() }
        registerNotNull(Attrs.ConstraintLayout_Layout.maxWidth) { a, v1, p, v2: Float -> v1.maxWidth = v2.toInt() }
        registerNotNull(Attrs.ConstraintLayout_Layout.maxHeight) { a, v1, p, v2: Float -> v1.maxHeight = v2.toInt() }
        registerNotNull(Attrs.ConstraintLayout_Layout.layout_optimizationLevel) { a, v1, p, v2: Int -> }
        registerNotNull(Attrs.ConstraintLayout_Layout.constraintSet) { a, v1, p, v2: Int ->
            try {
                val constraintSet = ConstraintSet()
                constraintSet.load(apm.context, v2)
                v1.setConstraintSet(constraintSet)
            } catch (e: Resources.NotFoundException) {
                // ignore...
            }
            ReflectHelper.setInt("mConstraintSetId", v1, v2)
        }

        registerMultiForChild<Any>(
                Attrs.ConstraintLayout_Layout.orientation to null,
                // Attrs.ConstraintLayout_Layout.barrierDirection to null, Attrs.ConstraintLayout_Layout.barrierAllowsGoneWidgets to null,
                // Attrs.ConstraintLayout_Layout.constraint_referenced_ids to null, Attrs.ConstraintLayout_Layout.chainUseRtl to null,

                Attrs.ConstraintLayout_Layout.layout_constraintCircle to null, Attrs.ConstraintLayout_Layout.layout_constraintCircleRadius to null,
                Attrs.ConstraintLayout_Layout.layout_constraintCircleAngle to null,

                Attrs.ConstraintLayout_Layout.layout_constraintGuide_begin to null, Attrs.ConstraintLayout_Layout.layout_constraintGuide_end to null,
                Attrs.ConstraintLayout_Layout.layout_constraintGuide_percent to null,

                Attrs.ConstraintLayout_Layout.layout_constraintLeft_toLeftOf to null, Attrs.ConstraintLayout_Layout.layout_constraintLeft_toRightOf to null,
                Attrs.ConstraintLayout_Layout.layout_constraintRight_toLeftOf to null, Attrs.ConstraintLayout_Layout.layout_constraintRight_toRightOf to null,
                Attrs.ConstraintLayout_Layout.layout_constraintTop_toTopOf to null, Attrs.ConstraintLayout_Layout.layout_constraintTop_toBottomOf to null,
                Attrs.ConstraintLayout_Layout.layout_constraintBottom_toTopOf to null, Attrs.ConstraintLayout_Layout.layout_constraintBottom_toBottomOf to null,
                Attrs.ConstraintLayout_Layout.layout_constraintBaseline_toBaselineOf to null,
                Attrs.ConstraintLayout_Layout.layout_constraintStart_toEndOf to null, Attrs.ConstraintLayout_Layout.layout_constraintStart_toStartOf to null,
                Attrs.ConstraintLayout_Layout.layout_constraintEnd_toStartOf to null, Attrs.ConstraintLayout_Layout.layout_constraintEnd_toEndOf to null,

                Attrs.ConstraintLayout_Layout.layout_goneMarginLeft to null, Attrs.ConstraintLayout_Layout.layout_goneMarginRight to null,
                Attrs.ConstraintLayout_Layout.layout_goneMarginTop to null, Attrs.ConstraintLayout_Layout.layout_goneMarginBottom to null,
                Attrs.ConstraintLayout_Layout.layout_goneMarginStart to null, Attrs.ConstraintLayout_Layout.layout_goneMarginEnd to null,

                Attrs.ConstraintLayout_Layout.layout_constrainedWidth to null, Attrs.ConstraintLayout_Layout.layout_constrainedHeight to null,
                Attrs.ConstraintLayout_Layout.layout_constraintHorizontal_bias to null, Attrs.ConstraintLayout_Layout.layout_constraintVertical_bias to null,
                Attrs.ConstraintLayout_Layout.layout_constraintWidth_default to null, Attrs.ConstraintLayout_Layout.layout_constraintHeight_default to null,
                Attrs.ConstraintLayout_Layout.layout_constraintWidth_min to null, Attrs.ConstraintLayout_Layout.layout_constraintWidth_max to null,
                Attrs.ConstraintLayout_Layout.layout_constraintHeight_min to null, Attrs.ConstraintLayout_Layout.layout_constraintHeight_max to null,
                Attrs.ConstraintLayout_Layout.layout_constraintWidth_percent to null, Attrs.ConstraintLayout_Layout.layout_constraintHeight_percent to null,

                // Attrs.ConstraintLayout_Layout.layout_constraintLeft_creator to null, Attrs.ConstraintLayout_Layout.layout_constraintRight_creator to null,
                // Attrs.ConstraintLayout_Layout.layout_constraintTop_creator to null, Attrs.ConstraintLayout_Layout.layout_constraintBottom_creator to null,
                // Attrs.ConstraintLayout_Layout.layout_constraintBaseline_creator to null,

                Attrs.ConstraintLayout_Layout.layout_constraintDimensionRatio to null,
                Attrs.ConstraintLayout_Layout.layout_constraintHorizontal_weight to null, Attrs.ConstraintLayout_Layout.layout_constraintVertical_weight to null,
                Attrs.ConstraintLayout_Layout.layout_constraintHorizontal_chainStyle to null, Attrs.ConstraintLayout_Layout.layout_constraintVertical_chainStyle to null,
                Attrs.ConstraintLayout_Layout.layout_editor_absoluteX to null, Attrs.ConstraintLayout_Layout.layout_editor_absoluteY to null
        ) { v, p, vs, helper ->
            val lp = v.layoutParams as? ConstraintLayout.LayoutParams ?: return@registerMultiForChild
            lp.orientation = helper[Attrs.ConstraintLayout_Layout.orientation] as? Int ?: -1

            lp.circleConstraint = loadConstraintRefer(helper[Attrs.ConstraintLayout_Layout.layout_constraintCircle] as? String)
            lp.circleRadius = helper[Attrs.ConstraintLayout_Layout.layout_constraintCircleRadius] as? Int ?: -1
            var circleAngle = (helper[Attrs.ConstraintLayout_Layout.layout_constraintCircleAngle] as? Float ?: 0f) % 360f
            if (circleAngle < 0f) {
                circleAngle = (360.0f - circleAngle) % 360.0f
            }
            lp.circleAngle = circleAngle

            lp.guideBegin = (helper[Attrs.ConstraintLayout_Layout.layout_constraintGuide_begin] as? Float)?.toInt() ?: -1
            lp.guideEnd = (helper[Attrs.ConstraintLayout_Layout.layout_constraintGuide_end] as? Float)?.toInt() ?: -1
            lp.guidePercent = helper[Attrs.ConstraintLayout_Layout.layout_constraintGuide_percent] as? Float ?: -1f

            lp.leftToLeft = loadConstraintRefer(helper[Attrs.ConstraintLayout_Layout.layout_constraintLeft_toLeftOf] as? String)
            lp.leftToRight = loadConstraintRefer(helper[Attrs.ConstraintLayout_Layout.layout_constraintLeft_toRightOf] as? String)
            lp.rightToLeft = loadConstraintRefer(helper[Attrs.ConstraintLayout_Layout.layout_constraintRight_toLeftOf] as? String)
            lp.rightToRight = loadConstraintRefer(helper[Attrs.ConstraintLayout_Layout.layout_constraintRight_toRightOf] as? String)
            lp.topToTop = loadConstraintRefer(helper[Attrs.ConstraintLayout_Layout.layout_constraintTop_toTopOf] as? String)
            lp.topToBottom = loadConstraintRefer(helper[Attrs.ConstraintLayout_Layout.layout_constraintTop_toBottomOf] as? String)
            lp.bottomToTop = loadConstraintRefer(helper[Attrs.ConstraintLayout_Layout.layout_constraintBottom_toTopOf] as? String)
            lp.bottomToBottom = loadConstraintRefer(helper[Attrs.ConstraintLayout_Layout.layout_constraintBottom_toBottomOf] as? String)

            lp.baselineToBaseline = loadConstraintRefer(helper[Attrs.ConstraintLayout_Layout.layout_constraintBaseline_toBaselineOf] as? String)
            lp.startToEnd = loadConstraintRefer(helper[Attrs.ConstraintLayout_Layout.layout_constraintStart_toEndOf] as? String)
            lp.startToStart = loadConstraintRefer(helper[Attrs.ConstraintLayout_Layout.layout_constraintStart_toStartOf] as? String)
            lp.endToStart = loadConstraintRefer(helper[Attrs.ConstraintLayout_Layout.layout_constraintEnd_toStartOf] as? String)
            lp.endToEnd = loadConstraintRefer(helper[Attrs.ConstraintLayout_Layout.layout_constraintEnd_toEndOf] as? String)

            lp.goneLeftMargin = (helper[Attrs.ConstraintLayout_Layout.layout_goneMarginLeft] as? Float)?.toInt() ?: -1
            lp.goneTopMargin = (helper[Attrs.ConstraintLayout_Layout.layout_goneMarginTop] as? Float)?.toInt() ?: -1
            lp.goneRightMargin = (helper[Attrs.ConstraintLayout_Layout.layout_goneMarginRight] as? Float)?.toInt() ?: -1
            lp.goneBottomMargin = (helper[Attrs.ConstraintLayout_Layout.layout_goneMarginBottom] as? Float)?.toInt() ?: -1
            lp.goneStartMargin = (helper[Attrs.ConstraintLayout_Layout.layout_goneMarginStart] as? Float)?.toInt() ?: -1
            lp.goneEndMargin = (helper[Attrs.ConstraintLayout_Layout.layout_goneMarginEnd] as? Float)?.toInt() ?: -1

            lp.constrainedWidth = helper[Attrs.ConstraintLayout_Layout.layout_constrainedWidth] as? Boolean ?: false
            lp.constrainedHeight = helper[Attrs.ConstraintLayout_Layout.layout_constrainedHeight] as? Boolean ?: false
            lp.horizontalBias = helper[Attrs.ConstraintLayout_Layout.layout_constraintHorizontal_bias] as? Float ?: 0.5f
            lp.verticalBias = helper[Attrs.ConstraintLayout_Layout.layout_constraintVertical_bias] as? Float ?: 0.5f
            lp.matchConstraintDefaultWidth = helper[Attrs.ConstraintLayout_Layout.layout_constraintWidth_default] as? Int ?: -1
            lp.matchConstraintDefaultHeight = helper[Attrs.ConstraintLayout_Layout.layout_constraintHeight_default] as? Int ?: -1
            var intValue = loadConstraintSize(helper[Attrs.ConstraintLayout_Layout.layout_constraintWidth_min] as? String, lp)
            if (intValue != null) {
                lp.matchConstraintMinWidth = intValue
            }
            intValue = loadConstraintSize(helper[Attrs.ConstraintLayout_Layout.layout_constraintWidth_max] as? String, lp)
            if (intValue != null) {
                lp.matchConstraintMaxWidth = intValue
            }
            lp.matchConstraintPercentWidth = helper[Attrs.ConstraintLayout_Layout.layout_constraintWidth_percent] as? Float ?: 0f
            intValue = loadConstraintSize(helper[Attrs.ConstraintLayout_Layout.layout_constraintHeight_min] as? String, lp)
            if (intValue != null) {
                lp.matchConstraintMinHeight = intValue
            }
            intValue = loadConstraintSize(helper[Attrs.ConstraintLayout_Layout.layout_constraintHeight_max] as? String, lp)
            if (intValue != null) {
                lp.matchConstraintMaxHeight = intValue
            }
            lp.matchConstraintPercentHeight = helper[Attrs.ConstraintLayout_Layout.layout_constraintHeight_percent] as? Float ?: 0f

            val dimensionRatio = helper[Attrs.ConstraintLayout_Layout.layout_constraintDimensionRatio] as? String
            var dimensionRatioValue = 0f
            var dimensionRatioSide = -1
            if (dimensionRatio != null) {
                val len = dimensionRatio.length
                commaIndex = dimensionRatio.indexOf(44.toChar())
                if (commaIndex > 0 && commaIndex < len - 1) {
                    val dimension = dimensionRatio.substring(0, commaIndex)
                    if (dimension.equals("W", ignoreCase = false)) {
                        dimensionRatioSide = 0
                    } else if (dimension.equals("H", ignoreCase = false)) {
                        dimensionRatioSide = 1
                    }
                    ++commaIndex
                } else {
                    commaIndex = 0
                }
                val colonIndex = dimensionRatio.indexOf(58.toChar())
                val r: String
                if (colonIndex >= 0 && colonIndex < len - 1) {
                    r = dimensionRatio.substring(commaIndex, colonIndex)
                    val denominator = dimensionRatio.substring(colonIndex + 1)
                    if (r.isNotEmpty() && denominator.isNotEmpty()) {
                        try {
                            val nominatorValue = r.toFloat()
                            val denominatorValue = denominator.toFloat()
                            if (nominatorValue > 0.0F && denominatorValue > 0.0F) {
                                dimensionRatioValue = when (dimensionRatioSide) {
                                    1 -> abs(denominatorValue / nominatorValue)
                                    else -> abs(nominatorValue / denominatorValue)
                                }
                            }
                        } catch (e: NumberFormatException) {
                        }
                    }
                } else {
                    r = dimensionRatio.substring(commaIndex)
                    if (r.isNotEmpty()) {
                        try {
                            dimensionRatioValue = r.toFloat()
                        } catch (e: NumberFormatException) {
                        }
                    }
                }
            }
            lp.dimensionRatio = dimensionRatio
            ReflectHelper.setFloat("dimensionRatioValue", lp, dimensionRatioValue)
            ReflectHelper.setInt("dimensionRatioSide", lp, dimensionRatioSide)

            lp.horizontalWeight = helper[Attrs.ConstraintLayout_Layout.layout_constraintHorizontal_weight] as? Float ?: -1f
            lp.verticalWeight = helper[Attrs.ConstraintLayout_Layout.layout_constraintVertical_weight] as? Float ?: -1f
            lp.horizontalChainStyle = helper[Attrs.ConstraintLayout_Layout.layout_constraintHorizontal_chainStyle] as? Int ?: 0
            lp.verticalChainStyle = helper[Attrs.ConstraintLayout_Layout.layout_constraintVertical_chainStyle] as? Int ?: 0
            lp.editorAbsoluteX = (helper[Attrs.ConstraintLayout_Layout.layout_editor_absoluteX] as? Float)?.toInt() ?: -1
            lp.editorAbsoluteY = (helper[Attrs.ConstraintLayout_Layout.layout_editor_absoluteY] as? Float)?.toInt() ?: -1
            v.layoutParams = lp
        }
    }

    open fun loadConstraintRefer(s: String?, default: Int = -1): Int = when (s) {
        null -> default
        else -> refer(s) ?: int(s) ?: default
    }

    open var commaIndex = 0
    open fun loadConstraintSize(s: String?, lp: ConstraintLayout.LayoutParams): Int? = when (s) {
        null -> null
        else -> {
            var value = refer(s)
            when (value) {
                null, 0 -> {
                    value = int(s) ?: 0
                    commaIndex = value
                    when (value) {
                        -2 -> value
                        else -> null
                    }
                }
                else -> value
            }
        }
    }

    override fun makeView(node: Node): ConstraintLayout? = ConstraintLayout(apm.context)
}

open class PlaceholderParser(viewParser: ViewParser) : BaseViewParser<Placeholder>(
        "androidx.constraintlayout.widget.PlaceHolder", ResViewType.VIEW_PLACE_HOLDER, mutableListOf(viewParser)) {
    override fun prepare() {
        registerNotNull(Attrs.ConstraintLayout_placeholder.content) { a, v1, p, v2: Int -> v1.setContentId(v2) }
        registerNotNull(Attrs.ConstraintLayout_placeholder.emptyVisibility) { a, v1, p, v2: Int -> v1.emptyVisibility = v2 }
    }

    override fun makeView(node: Node): Placeholder? = Placeholder(apm.context)
}

open class GuidelineParser(viewParser: ViewParser) : BaseViewParser<Guideline>(
        "androidx.constraintlayout.widget.Guideline", ResViewType.VIEW_GUIDE_LINE, mutableListOf(viewParser)) {
    override fun prepare() = Unit
    override fun makeView(node: Node): Guideline? = Guideline(apm.context)
}

open class ConstraintHelperParser(viewParser: ViewParser) : BaseViewParser<ConstraintHelper>(
        "androidx.constraintlayout.widget.ConstraintHelper", ResViewType.VIEW_CONSTRAINT_HELPER, mutableListOf(viewParser)) {
    override fun prepare() {
        registerNotNull(Attrs.ConstraintLayout_Layout.constraint_referenced_ids) { a, v1, p, v2: String ->
            v1.referencedIds = ResStore.loadIntArray(refer(v2) ?: return@registerNotNull, apm.context) ?: return@registerNotNull
        }
    }

    override fun makeView(node: Node): ConstraintHelper? = throw RuntimeException("not supported: cannot create $rootName")
}

open class GroupParser(viewParser: ViewParser) : BaseViewParser<Group>(
        "androidx.constraintlayout.widget.Group", ResViewType.VIEW_CONSTRAINT_GROUP, mutableListOf(viewParser)) {
    override fun prepare() = Unit
    override fun makeView(node: Node): Group? = Group(apm.context)
}

open class BarrierParser(viewParser: ViewParser) : BaseViewParser<Barrier>(
        "androidx.constraintlayout.widget.Barrier", ResViewType.VIEW_BARRIER, mutableListOf(viewParser)) {
    override fun prepare() = Unit
    override fun makeView(node: Node): Barrier? = Barrier(apm.context)
}

open class ConstraintsParser(viewParser: ViewParser) : BaseViewParser<Constraints>(
        "androidx.constraintlayout.widget.Constraints", ResViewType.CONTAINER_CONSTRAINTS, mutableListOf(viewParser)) {
    override fun prepare() {
        registerMultiForChild(
                Attrs.ConstraintSet.alpha to 1f, Attrs.ConstraintSet.elevation to 0f,
                Attrs.ConstraintSet.rotationX to 0f, Attrs.ConstraintSet.rotationY to 0f, Attrs.ConstraintSet.rotation to 0f,
                Attrs.ConstraintSet.scaleX to 1f, Attrs.ConstraintSet.scaleY to 1f,
                Attrs.ConstraintSet.transformPivotX to 0f, Attrs.ConstraintSet.transformPivotY to 0f,
                Attrs.ConstraintSet.translationX to 0f, Attrs.ConstraintSet.translationY to 0f, Attrs.ConstraintSet.translationZ to 0f
        ) { v, p, vs, helper ->
            val lp = v.layoutParams as? Constraints.LayoutParams ?: return@registerMultiForChild
            lp.alpha = helper[Attrs.ConstraintSet.alpha]
            lp.elevation = helper[Attrs.ConstraintSet.elevation]
            lp.rotation = helper[Attrs.ConstraintSet.rotation]
            lp.rotationX = helper[Attrs.ConstraintSet.rotationX]
            lp.rotationY = helper[Attrs.ConstraintSet.rotationY]
            lp.scaleX = helper[Attrs.ConstraintSet.scaleX]
            lp.scaleY = helper[Attrs.ConstraintSet.scaleY]
            lp.transformPivotX = helper[Attrs.ConstraintSet.transformPivotX]
            lp.transformPivotY = helper[Attrs.ConstraintSet.transformPivotY]
            lp.translationX = helper[Attrs.ConstraintSet.translationX]
            lp.translationY = helper[Attrs.ConstraintSet.translationY]
            lp.translationZ = helper[Attrs.ConstraintSet.translationZ]
            v.layoutParams = lp
        }
    }

    override fun makeView(node: Node): Constraints? = Constraints(apm.context)
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
open class GridLayoutParser(viewGroupParser: ViewGroupParser) : BaseViewParser<GridLayout>(
        "GridLayout", ResViewType.LAYOUT_GRID, mutableListOf(viewGroupParser)) {
    override fun prepare() {
        val undefined = GridLayout.UNDEFINED

        registerNotNull(Attrs.GridLayout.orientation) { a, v1, p, v2: Int -> v1.orientation = v2 }
        registerNotNull(Attrs.GridLayout.alignmentMode) { a, v1, p, v2: Int -> v1.alignmentMode = v2 }
        registerNotNull(Attrs.GridLayout.useDefaultMargins) { a, v1, p, v2: Boolean -> v1.useDefaultMargins = v2 }
        registerNotNull(Attrs.GridLayout.columnCount) { a, v1, p, v2: Int -> v1.columnCount = v2 }
        registerNotNull(Attrs.GridLayout.rowCount) { a, v1, p, v2: Int -> v1.rowCount = v2 }
        registerNotNull(Attrs.GridLayout.columnOrderPreserved) { a, v1, p, v2: Boolean -> v1.isColumnOrderPreserved = v2 }
        registerNotNull(Attrs.GridLayout.rowOrderPreserved) { a, v1, p, v2: Boolean -> v1.isRowOrderPreserved = v2 }

        registerMultiForChild<Any>(
                Attrs.GridLayout_Layout.layout_gravity to Gravity.NO_GRAVITY,
                Attrs.GridLayout_Layout.layout_column to undefined, Attrs.GridLayout_Layout.layout_columnSpan to undefined,
                Attrs.GridLayout_Layout.layout_columnWeight to undefined,
                Attrs.GridLayout_Layout.layout_row to undefined, Attrs.GridLayout_Layout.layout_rowSpan to undefined,
                Attrs.GridLayout_Layout.layout_rowWeight to undefined
        ) { v, p, vs, helper ->
            val lp = v.layoutParams as? GridLayout.LayoutParams ?: return@registerMultiForChild
            val gravity = helper[Attrs.GridLayout_Layout.layout_gravity] as Int
            lp.columnSpec = GridLayout.spec(helper[Attrs.GridLayout_Layout.layout_column] as Int, helper[Attrs.GridLayout_Layout.layout_columnSpan] as Int,
                    getAlignment(gravity, true), helper[Attrs.GridLayout_Layout.layout_columnWeight] as Float)
            lp.rowSpec = GridLayout.spec(helper[Attrs.GridLayout_Layout.layout_row] as Int, helper[Attrs.GridLayout_Layout.layout_rowSpan] as Int,
                    getAlignment(gravity, false), helper[Attrs.GridLayout_Layout.layout_rowWeight] as Float)
            v.layoutParams = lp
        }
    }

    override fun makeView(node: Node): GridLayout? = GridLayout(apm.context)

    companion object {
        fun getAlignment(gravity: Int, horizontal: Boolean): Alignment? {
            val mask = if (horizontal) Gravity.HORIZONTAL_GRAVITY_MASK else Gravity.VERTICAL_GRAVITY_MASK
            val shift = if (horizontal) Gravity.AXIS_X_SHIFT else Gravity.AXIS_Y_SHIFT
            val flags = gravity and mask shr shift
            return when {
                flags == Gravity.AXIS_SPECIFIED or Gravity.AXIS_PULL_BEFORE -> if (horizontal) GridLayout.LEFT else GridLayout.TOP
                flags == Gravity.AXIS_SPECIFIED or Gravity.AXIS_PULL_AFTER -> if (horizontal) GridLayout.RIGHT else GridLayout.BOTTOM
                flags == Gravity.AXIS_SPECIFIED or Gravity.AXIS_PULL_BEFORE or Gravity.AXIS_PULL_AFTER -> GridLayout.FILL
                flags == Gravity.AXIS_SPECIFIED -> GridLayout.CENTER
                flags == Gravity.AXIS_SPECIFIED or Gravity.AXIS_PULL_BEFORE or Gravity.RELATIVE_LAYOUT_DIRECTION
                        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN -> GridLayout.START
                flags == Gravity.AXIS_SPECIFIED or Gravity.AXIS_PULL_AFTER or Gravity.RELATIVE_LAYOUT_DIRECTION
                        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN -> GridLayout.END
                else -> ReflectHelper.getStatic("UNDEFINED_ALIGNMENT", GridLayout::class.java) as? Alignment
            }
        }
    }
}

open class FloatingActionButtonParser(imageButtonParser: ImageButtonParser) : BaseViewParser<FloatingActionButton>(
        "FloatingActionButton", ResViewType.VIEW_FLOATING_ACTION_BUTTON, mutableListOf(imageButtonParser)) {
    override fun prepare() {
        registerNotNull(Attrs.FloatingActionButton.rippleColor) { a, v1, p, v2: String ->
            v1.setRippleColor(loadColorStateList(v2) ?: return@registerNotNull)
        }
        registerNotNull(Attrs.FloatingActionButton.fabSize) { a, v1, p, v2: Int ->
            v1.size = v2
        }
        registerNotNull(Attrs.FloatingActionButton.fabCustomSize) { a, v1, p, v2: Float ->
            v1.customSize = v2.toInt()
        }
        registerNotNull(Attrs.FloatingActionButton.borderWidth) { a, v1, p, v2: Float ->
            ReflectHelper.setInt("borderWidth", v1, v2.toInt())
        }
        registerNotNull(Attrs.FloatingActionButton.useCompatPadding) { a, v1, p, v2: Boolean ->
            v1.useCompatPadding = v2
        }
        registerNotNull(Attrs.FloatingActionButton.maxImageSize) { a, v1, p, v2: Float ->
            ReflectHelper.setInt("maxImageSize", v1, v2.toInt())
        }
        // registerNotNull(Attrs.FloatingActionButton.shapeAppearanceOverlay) { a, v1, p, v2 -> }

        registerMulti(
                Attrs.FloatingActionButton.elevation to 0f, Attrs.FloatingActionButton.ensureMinTouchTargetSize to false,
                Attrs.FloatingActionButton.hoveredFocusedTranslationZ to 0f, Attrs.FloatingActionButton.pressedTranslationZ to 0f,
                Attrs.FloatingActionButton.showMotionSpec to 0, Attrs.FloatingActionButton.hideMotionSpec to 0,
                // Attrs.FloatingActionButton.shapeAppearance to 0, Attrs.FloatingActionButton.shapeAppearanceOverlay to 0,  // TODO
                Attrs.ShapeAppearance.cornerFamily to CornerFamily.ROUNDED,
                Attrs.ShapeAppearance.cornerFamilyTopLeft to null, Attrs.ShapeAppearance.cornerFamilyTopRight to null,
                Attrs.ShapeAppearance.cornerFamilyBottomLeft to null, Attrs.ShapeAppearance.cornerFamilyBottomRight to null,
                Attrs.ShapeAppearance.cornerSize to null,
                Attrs.ShapeAppearance.cornerSizeTopLeft to null, Attrs.ShapeAppearance.cornerSizeTopRight to null,
                Attrs.ShapeAppearance.cornerSizeBottomLeft to null, Attrs.ShapeAppearance.cornerSizeBottomRight to null
        ) { v, p, vs, helper ->
            val impl = ReflectHelper.invoke<Any>("getImpl", v) ?: return@registerMulti makeFail("getImpl invoke error!!!", Unit)!!
            val cornerFamily = helper[Attrs.ShapeAppearance.cornerFamily] as Int
            setShapeAppearance?.invoke(impl, makeShapeAppearanceModel(this, helper, cornerFamily))
            initializeBackgroundDrawable?.invoke(impl, v.backgroundTintList, v.backgroundTintMode, v.rippleColorStateList,
                    ReflectHelper.getInt("borderWidth", v))
            setElevation?.invoke(impl, helper[Attrs.FloatingActionButton.elevation] as Float)
            setHoveredFocusedTranslationZ?.invoke(impl, helper[Attrs.FloatingActionButton.hoveredFocusedTranslationZ] as Float)
            setPressedTranslationZ?.invoke(impl, helper[Attrs.FloatingActionButton.pressedTranslationZ] as Float)
            setMaxImageSize?.invoke(impl, ReflectHelper.getInt("maxImageSize", v))
            v.showMotionSpec = createFromResource(apm.context, helper[Attrs.FloatingActionButton.showMotionSpec] as Int)
            v.hideMotionSpec = createFromResource(apm.context, helper[Attrs.FloatingActionButton.hideMotionSpec] as Int)
            v.setEnsureMinTouchTargetSize(helper[Attrs.FloatingActionButton.ensureMinTouchTargetSize] as Boolean)
        }

        registerNotNull(Attrs.FloatingActionButton_Behavior_Layout.behavior_autoHide) { a, v1, p, v2: Boolean ->
            TODO("not implemented")
        }
    }

    override fun makeView(node: Node): FloatingActionButton? = FloatingActionButton(apm.context)

    companion object {
        fun createFromResource(c: Context, id: Int): MotionSpec? = when (val animator = ResStore.loadAnimator(id, c)) {
            null -> null
            is AnimatorSet -> createSpecFromAnimators(animator.childAnimations)
            else -> createSpecFromAnimators(listOf(animator))
        }

        fun createSpecFromAnimators(animators: List<Animator>): MotionSpec {
            val spec = MotionSpec()
            animators.forEach { animator ->
                if (animator is ObjectAnimator) {
                    spec.setPropertyValues(animator.propertyName, animator.values)
                    spec.setTiming(animator.propertyName, createFromAnimator(animator))
                } else {
                    throw IllegalArgumentException("Animator must be an ObjectAnimator: $animator")
                }
            }
            return spec
        }

        fun createFromAnimator(animator: ValueAnimator): MotionTiming {
            val timing = MotionTiming(animator.startDelay, animator.duration, getInterpolatorCompat(animator))
            ReflectHelper.setInt("repeatCount", timing, animator.repeatCount)
            ReflectHelper.setInt("repeatMode", timing, animator.repeatMode)
            return timing
        }

        fun getInterpolatorCompat(animator: ValueAnimator): TimeInterpolator = when (val interpolator = animator.interpolator) {
            is AccelerateDecelerateInterpolator, null -> AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR
            is AccelerateInterpolator -> AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR
            is DecelerateInterpolator -> AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR
            else -> interpolator
        }

        fun getCornerSize(rp: ResProcessor<*>, s: String?, default: CornerSize): CornerSize = when (s) {
            null -> default
            else -> when (val dimenValue = rp.dimen2(s)) {
                null -> when (val fractionValue = rp.fraction2(s)) {
                    null -> default
                    else -> RelativeCornerSize(fractionValue)
                }
                else -> AbsoluteCornerSize(dimenValue)
            }
        }

        fun makeShapeAppearanceModel(
                rp: ResProcessor<*>, helper: MultiAttrHelper<Any, FloatingActionButton>, cornerFamily: Int,
                cornerSize: CornerSize = getCornerSize(rp, helper[Attrs.ShapeAppearance.cornerSize] as String?, ShapeAppearanceModel.PILL)
        ): ShapeAppearanceModel = ShapeAppearanceModel.builder()
                .setTopLeftCorner(helper[Attrs.ShapeAppearance.cornerFamilyTopLeft] as Int? ?: cornerFamily,
                        getCornerSize(rp, helper[Attrs.ShapeAppearance.cornerSizeTopLeft] as String?, cornerSize))
                .setTopRightCorner(helper[Attrs.ShapeAppearance.cornerFamilyTopRight] as Int? ?: cornerFamily,
                        getCornerSize(rp, helper[Attrs.ShapeAppearance.cornerSizeTopRight] as String?, cornerSize))
                .setBottomRightCorner(helper[Attrs.ShapeAppearance.cornerFamilyBottomLeft] as Int? ?: cornerFamily,
                        getCornerSize(rp, helper[Attrs.ShapeAppearance.cornerSizeBottomLeft] as String?, cornerSize))
                .setBottomLeftCorner(helper[Attrs.ShapeAppearance.cornerFamilyBottomLeft] as Int? ?: cornerFamily,
                        getCornerSize(rp, helper[Attrs.ShapeAppearance.cornerSizeBottomLeft] as String?, cornerSize))
                .build()

        val FloatingActionButtonImpl = ReflectHelper.findCls("com.google.android.material.floatingactionbutton.FloatingActionButtonImpl")
        val setShapeAppearance = ReflectHelper.findMethod("setShapeAppearance", FloatingActionButtonImpl, ShapeAppearanceModel::class.java)
        val initializeBackgroundDrawable = ReflectHelper.findMethod("initializeBackgroundDrawable", FloatingActionButtonImpl,
                ColorStateList::class.java, PorterDuff.Mode::class.java, ColorStateList::class.java, Int::class.java)
        val setElevation = ReflectHelper.findMethod("setElevation", FloatingActionButtonImpl, Float::class.java)
        val setHoveredFocusedTranslationZ = ReflectHelper.findMethod("setHoveredFocusedTranslationZ", FloatingActionButtonImpl, Float::class.java)
        val setPressedTranslationZ = ReflectHelper.findMethod("setPressedTranslationZ", FloatingActionButtonImpl, Float::class.java)
        val setMaxImageSize = ReflectHelper.findMethod("setMaxImageSize", FloatingActionButtonImpl, Int::class.java)
    }
}
