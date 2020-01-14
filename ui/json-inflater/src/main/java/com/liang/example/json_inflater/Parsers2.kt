package com.liang.example.json_inflater

import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TabHost
import android.widget.TextClock
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.lang.RuntimeException

object Support {
    const val VIEW = "View"
    const val VIEW_GROUP = "ViewGroup"
    const val VIEW_STUB = "ViewStub"
    const val FRAGMENT = "Fragment"
    const val INCLUDE = "Include"
    const val MERGE = "Merge"

    const val TEXT_VIEW = "TextView"
    const val BUTTON = "Button"
    const val EDIT_TEXT = "EditText"
    const val CHECK_EDIT_TEXT = "CheckEditText"

    const val IMAGE_VIEW = "ImageView"
    const val IMAGE_BUTTON = "ImageButton"
    const val FLOATING_ACTION_BUTTON = "FloatingActionButton"

    const val CHECK_BOX = "CheckBox"
    const val RADIO_BUTTON = "RadioButton"
    const val SWITCH = "Switch"
    const val TOGGLE_BUTTON = "ToggleButton"
    const val RADIO_GROUP = "RadioGroup"

    const val ANALOG_CLOCK = "AnalogClock"
    const val CHRONOMETER = "Chronometer"
    const val DIGITAL_CLOCK = "DigitalClock"
    const val TEXT_CLOCK = "TextClock"

    const val PROGRESS_BAR = "ProgressBar"
    const val RATING_BAR = "RatingBar"
    const val SEEK_BAR = "SeekBar"
    const val SLIDE = "Slide"
    const val CALENDAR = "Calendar"

    const val SURFACE_VIEW = "SurfaceView"
    const val TEXTURE_VIEW = "TextureView"
    const val SEARCH_VIEW = "SearchView"

    const val TAB_ITEM = "TabItem"
    const val VIDEO_VIEW = "VideoView"
    const val VIDEO_VIEW2 = "VideoView2"
    const val WEB_VIEW2 = "WebView2"

    const val VIEW_PAGER = "ViewPager"
    const val VIEW_PAGER2 = "ViewPager2"
    const val VIEW_CARD = "ViewCard"
    const val VIEW_RECYCLER = "RecyclerView"
    const val VIEW_GRID = "GridView"
    const val VIEW_LIST = "ListView"
    const val VIEW_SCROLL = "ScrollView"
    const val VIEW_SPINNER = "Spinner"
    const val VIEW_SCROLL_HORIZONTAL = "HorizontalScrollView"
    const val VIEW_SCROLL_NESTED = "NestedScrollView"

    const val VIEW_AD = "AdView"
    const val VIEW_MAP = "MapView"

    const val LAYOUT_ABSOLUTE = "AbsoluteLayout"
    const val LAYOUT_FRAME = "FrameLayout"
    const val LAYOUT_LINEAR = "LinearLayout"
    const val LAYOUT_RELATIVE = "RelativeLayout"
    const val LAYOUT_GRID = "GridLayout"
    const val LAYOUT_TABLE = "TableLayout"
    const val LAYOUT_TABLE_ROW = "TableRow"
    const val LAYOUT_GOORDINATOR = "GoordinatorLayout"
    const val LAYOUT_GONSTRAINT = "GonstraintLayout"

    const val LAYOUT_SPACE = "Space"
    const val LAYOUT_TAB_HOST = "TabHost"
    const val LAYOUT_TAB = "TabLayout"
    const val LAYOUT_TAB_ITEM = "TabItem"
    const val LAYOUT_TOOL_BAR = "Toolbar"

    const val LAYOUT_APPBAR = "AppBarLayout"
    const val LAYOUT_BOTTOM_APPBAR = "BottomAppBar"
    const val LAYOUT_BOTTOM_NAVIGATION = "BottomNavigationView"
    const val LAYOUT_DRAWER = "DrawerLayout"
}

open class NViewHolder2<V : View>(override val view: V, override var nManager: NView.NManager? = null) : NView<V>

// TODO
open class NViewParser2<V : NView<View>> : ViewTypeParser<V>() {
    override fun getType(): String = Support.VIEW
    override fun getParentType(): String? = null

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> = NViewHolder2(View(context))

    override fun addAttributeProcessors() {
        addAttributeProcessor(Attributes2.View.id.name, TODO())
        addAttributeProcessor(Attributes2.View.tag.name, TODO())
        addAttributeProcessor(Attributes2.View.scrollX.name, TODO())
        addAttributeProcessor(Attributes2.View.scrollY.name, TODO())
        addAttributeProcessor(Attributes2.View.background.name, TODO())
        addAttributeProcessor(Attributes2.View.padding.name, TODO())
        addAttributeProcessor(Attributes2.View.paddingHorizontal.name, TODO())
        addAttributeProcessor(Attributes2.View.paddingVertical.name, TODO())
        addAttributeProcessor(Attributes2.View.paddingLeft.name, TODO())
        addAttributeProcessor(Attributes2.View.paddingTop.name, TODO())
        addAttributeProcessor(Attributes2.View.paddingRight.name, TODO())
        addAttributeProcessor(Attributes2.View.paddingBottom.name, TODO())
        addAttributeProcessor(Attributes2.View.paddingStart.name, TODO())
        addAttributeProcessor(Attributes2.View.paddingEnd.name, TODO())
        addAttributeProcessor(Attributes2.View.focusable.name, TODO())
        addAttributeProcessor(Attributes2.View.__removed3.name, TODO())
        addAttributeProcessor(Attributes2.View.__removed4.name, TODO())
        addAttributeProcessor(Attributes2.View.__removed5.name, TODO())
        addAttributeProcessor(Attributes2.View.autofillHints.name, TODO())
        addAttributeProcessor(Attributes2.View.importantForAutofill.name, TODO())
        addAttributeProcessor(Attributes2.View.__removed6.name, TODO())
        addAttributeProcessor(Attributes2.View.focusableInTouchMode.name, TODO())
        addAttributeProcessor(Attributes2.View.visibility.name, TODO())
        addAttributeProcessor(Attributes2.View.fitsSystemWindows.name, TODO())
        addAttributeProcessor(Attributes2.View.scrollbars.name, TODO())
        addAttributeProcessor(Attributes2.View.scrollbarStyle.name, TODO())
        addAttributeProcessor(Attributes2.View.isScrollContainer.name, TODO())
        addAttributeProcessor(Attributes2.View.fadeScrollbars.name, TODO())
        addAttributeProcessor(Attributes2.View.scrollbarFadeDuration.name, TODO())
        addAttributeProcessor(Attributes2.View.scrollbarDefaultDelayBeforeFade.name, TODO())
        addAttributeProcessor(Attributes2.View.scrollbarSize.name, TODO())
        addAttributeProcessor(Attributes2.View.scrollbarThumbHorizontal.name, TODO())
        addAttributeProcessor(Attributes2.View.scrollbarThumbVertical.name, TODO())
        addAttributeProcessor(Attributes2.View.scrollbarTrackHorizontal.name, TODO())
        addAttributeProcessor(Attributes2.View.scrollbarTrackVertical.name, TODO())
        addAttributeProcessor(Attributes2.View.scrollbarAlwaysDrawHorizontalTrack.name, TODO())
        addAttributeProcessor(Attributes2.View.scrollbarAlwaysDrawVerticalTrack.name, TODO())
        addAttributeProcessor(Attributes2.View.fadingEdge.name, TODO())
        addAttributeProcessor(Attributes2.View.requiresFadingEdge.name, TODO())
        addAttributeProcessor(Attributes2.View.fadingEdgeLength.name, TODO())
        addAttributeProcessor(Attributes2.View.nextFocusLeft.name, TODO())
        addAttributeProcessor(Attributes2.View.nextFocusRight.name, TODO())
        addAttributeProcessor(Attributes2.View.nextFocusUp.name, TODO())
        addAttributeProcessor(Attributes2.View.nextFocusDown.name, TODO())
        addAttributeProcessor(Attributes2.View.nextFocusForward.name, TODO())
        addAttributeProcessor(Attributes2.View.clickable.name, TODO())
        addAttributeProcessor(Attributes2.View.longClickable.name, TODO())
        addAttributeProcessor(Attributes2.View.contextClickable.name, TODO())
        addAttributeProcessor(Attributes2.View.saveEnabled.name, TODO())
        addAttributeProcessor(Attributes2.View.filterTouchesWhenObscured.name, TODO())
        addAttributeProcessor(Attributes2.View.drawingCacheQuality.name, TODO())
        addAttributeProcessor(Attributes2.View.keepScreenOn.name, TODO())
        addAttributeProcessor(Attributes2.View.duplicateParentState.name, TODO())
        addAttributeProcessor(Attributes2.View.minHeight.name, TODO())
        addAttributeProcessor(Attributes2.View.minWidth.name, TODO())
        addAttributeProcessor(Attributes2.View.soundEffectsEnabled.name, TODO())
        addAttributeProcessor(Attributes2.View.hapticFeedbackEnabled.name, TODO())
        addAttributeProcessor(Attributes2.View.contentDescription.name, TODO())
        addAttributeProcessor(Attributes2.View.accessibilityTraversalBefore.name, TODO())
        addAttributeProcessor(Attributes2.View.accessibilityTraversalAfter.name, TODO())
        addAttributeProcessor(Attributes2.View.onClick.name, TODO())
        addAttributeProcessor(Attributes2.View.overScrollMode.name, TODO())
        addAttributeProcessor(Attributes2.View.alpha.name, TODO())
        addAttributeProcessor(Attributes2.View.elevation.name, TODO())
        addAttributeProcessor(Attributes2.View.translationX.name, TODO())
        addAttributeProcessor(Attributes2.View.translationY.name, TODO())
        addAttributeProcessor(Attributes2.View.translationZ.name, TODO())
        addAttributeProcessor(Attributes2.View.transformPivotX.name, TODO())
        addAttributeProcessor(Attributes2.View.transformPivotY.name, TODO())
        addAttributeProcessor(Attributes2.View.rotation.name, TODO())
        addAttributeProcessor(Attributes2.View.rotationX.name, TODO())
        addAttributeProcessor(Attributes2.View.rotationY.name, TODO())
        addAttributeProcessor(Attributes2.View.scaleX.name, TODO())
        addAttributeProcessor(Attributes2.View.scaleY.name, TODO())
        addAttributeProcessor(Attributes2.View.verticalScrollbarPosition.name, TODO())
        addAttributeProcessor(Attributes2.View.layerType.name, TODO())
        addAttributeProcessor(Attributes2.View.layoutDirection.name, TODO())
        addAttributeProcessor(Attributes2.View.textDirection.name, TODO())
        addAttributeProcessor(Attributes2.View.textAlignment.name, TODO())
        addAttributeProcessor(Attributes2.View.importantForAccessibility.name, TODO())
        addAttributeProcessor(Attributes2.View.accessibilityLiveRegion.name, TODO())
        addAttributeProcessor(Attributes2.View.labelFor.name, TODO())
        addAttributeProcessor(Attributes2.View.theme.name, TODO())
        addAttributeProcessor(Attributes2.View.transitionName.name, TODO())
        addAttributeProcessor(Attributes2.View.nestedScrollingEnabled.name, TODO())
        addAttributeProcessor(Attributes2.View.stateListAnimator.name, TODO())
        addAttributeProcessor(Attributes2.View.backgroundTint.name, TODO())
        addAttributeProcessor(Attributes2.View.backgroundTintMode.name, TODO())
        addAttributeProcessor(Attributes2.View.outlineProvider.name, TODO())
        addAttributeProcessor(Attributes2.View.foreground.name, TODO())
        addAttributeProcessor(Attributes2.View.foregroundGravity.name, TODO())
        addAttributeProcessor(Attributes2.View.foregroundInsidePadding.name, TODO())
        addAttributeProcessor(Attributes2.View.foregroundTint.name, TODO())
        addAttributeProcessor(Attributes2.View.foregroundTintMode.name, TODO())
        addAttributeProcessor(Attributes2.View.scrollIndicators.name, TODO())
        addAttributeProcessor(Attributes2.View.pointerIcon.name, TODO())
        addAttributeProcessor(Attributes2.View.forceHasOverlappingRendering.name, TODO())
        addAttributeProcessor(Attributes2.View.tooltipText.name, TODO())
        addAttributeProcessor(Attributes2.View.keyboardNavigationCluster.name, TODO())
        addAttributeProcessor(Attributes2.View.__removed0.name, TODO())
        addAttributeProcessor(Attributes2.View.nextClusterForward.name, TODO())
        addAttributeProcessor(Attributes2.View.__removed1.name, TODO())
        addAttributeProcessor(Attributes2.View.focusedByDefault.name, TODO())
        addAttributeProcessor(Attributes2.View.defaultFocusHighlightEnabled.name, TODO())
        addAttributeProcessor(Attributes2.View.screenReaderFocusable.name, TODO())
        addAttributeProcessor(Attributes2.View.accessibilityPaneTitle.name, TODO())
        addAttributeProcessor(Attributes2.View.accessibilityHeading.name, TODO())
        addAttributeProcessor(Attributes2.View.outlineSpotShadowColor.name, TODO())
        addAttributeProcessor(Attributes2.View.outlineAmbientShadowColor.name, TODO())
        addAttributeProcessor(Attributes2.View.forceDarkAllowed.name, TODO())
    }
}

// TODO
open class NViewGroupParser2<V : NView<ViewGroup>> : ViewTypeParser<V>() {
    override fun getType(): String = VIEW_GROUP
    override fun getParentType(): String? = Support.VIEW
    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> =
            throw RuntimeException("not support create viewGroup directly")

    override fun addAttributeProcessors() {
        addAttributeProcessor(Attributes2.ViewGroup.animateLayoutChanges.name, TODO())
        addAttributeProcessor(Attributes2.ViewGroup.clipChildren.name, TODO())
        addAttributeProcessor(Attributes2.ViewGroup.clipToPadding.name, TODO())
        addAttributeProcessor(Attributes2.ViewGroup.layoutAnimation.name, TODO())
        addAttributeProcessor(Attributes2.ViewGroup.animationCache.name, TODO())
        addAttributeProcessor(Attributes2.ViewGroup.persistentDrawingCache.name, TODO())
        addAttributeProcessor(Attributes2.ViewGroup.alwaysDrawnWithCache.name, TODO())
        addAttributeProcessor(Attributes2.ViewGroup.addStatesFromChildren.name, TODO())
        addAttributeProcessor(Attributes2.ViewGroup.descendantFocusability.name, TODO())
        addAttributeProcessor(Attributes2.ViewGroup.touchscreenBlocksFocus.name, TODO())
        addAttributeProcessor(Attributes2.ViewGroup.splitMotionEvents.name, TODO())
        addAttributeProcessor(Attributes2.ViewGroup.layoutMode.name, TODO())
        addAttributeProcessor(Attributes2.ViewGroup.transitionGroup.name, TODO())
    }
}

// TODO
open class NTextViewParser2<V : NView<TextView>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// TODO
open class NEditTextParser2<V : NView<EditText>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// TODO
open class NButtonParser2<V : NView<Button>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// TODO
open class NImageViewParser2<V : NView<ImageView>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// TODO
open class NCheckBoxParser2<V : NView<CheckBox>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// TODO
open class NSwitchParser2<V : NView<Switch>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// TODO
open class NRadioButtonParser2<V : NView<RadioButton>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// TODO
open class NRadioGroupParser2<V : NView<RadioGroup>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// TODO
open class NTextureViewParser2<V : NView<TextureView>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// TODO
open class NTextClockParser2<V : NView<TextClock>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// TODO
open class NViewPagerParser2<V : NView<ViewPager>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// TODO
open class NScrollViewParser2<V : NView<ScrollView>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// TODO
open class NRecyclerViewParser2<V : NView<RecyclerView>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// TODO
open class NListViewParser2<V : NView<ListView>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// TODO
open class NGridViewParser2<V : NView<GridView>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// TODO
open class NDrawerLayoutParser2<V : NView<DrawerLayout>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}



// TODO
open class NFrameLayoutParser2<V : NView<FrameLayout>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// TODO
open class NLinearLayoutParser2<V : NView<LinearLayout>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// TODO
open class NRelativeLayoutParser2<V : NView<RelativeLayout>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// TODO
open class NConstraintLayoutParser2<V : NView<ConstraintLayout>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// TODO
open class NCoordinatorLayoutParser2<V : NView<CoordinatorLayout>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}



open class NTabHostParser2<V : NView<TabHost>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class NAppBarLayoutParser2<V : NView<AppBarLayout>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class NBottomAppBarParser2<V : NView<BottomAppBar>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class NBottomNavigationViewParser2<V : NView<BottomNavigationView>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
