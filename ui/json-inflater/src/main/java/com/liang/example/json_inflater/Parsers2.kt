package com.liang.example.json_inflater

import android.view.View
import android.view.ViewGroup

object Support {
    const val VIEW = "View"
}

open class NViewHolder2<V : View>(override val view: V, override var nManager: NView.NManager? = null) : NView<V>

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
