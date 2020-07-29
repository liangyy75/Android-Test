@file:Suppress("unused", "ObjectPropertyName", "ObjectPropertyName", "SpellCheckingInspection", "ClassName")

package com.liang.example.xml_inflater2

// format的可能的值：'dimension', 'fraction', 'reference', 'color', 'string', 'boolean', 'integer', 'float', 'enum', 'flag'，
// [flag, enum, dimension, integer]是重点，都带有values，即可枚举
/**
 * dimension: dp / in / pt / px / mm / sp
 * fraction: 100%(percent of myself) / 100%p(percent of parent)
 * reference: @[package_name:](animator/anim/drawable/color/layout/menu/  style/string/id/font/dimen/fraction/integer/bool/array)/(filename/name)
 * color: #aarrggbb / #(ff)rrggbb / #argb / #rgb
 * string
 * boolean(/bool): true / false
 * integer
 * float
 * enum: integer的变种
 * flags: integer的变种
 */
open class Attr(val name: String, val format: String? = null, val values: MutableList<Pair<String, Long>>? = null) {
    constructor(name: String, format: String?, values: Array<Pair<String, Int>>) : this(name, format, values.map { it.first to it.second.toLong() }.toMutableList())

    override fun hashCode(): Int = name.hashCode() * 31 + (format?.hashCode() ?: 0)
    override fun equals(other: Any?): Boolean = other === this || other is Attr && other.name == this.name && other.format == this.format

    override fun toString(): String = "attr(name: $name, format: $format, values: [${values?.toList()?.joinToString { "(${it.first}: ${it.second})" }
            ?: ""}])"

    open operator fun contains(key: String): Boolean = values?.find { it.first == key } != null
    open operator fun contains(value: Long): Boolean = values?.find { it.second == value } != null

    open operator fun get(key: String): Long? = values?.find { it.first == key }?.second
    open operator fun get(value: Long): String? = values?.find { it.second == value }?.first

    companion object {
        val ATTR_EMPTY = Attr("")
    }
}

class Attrs {
    object Theme {
        val isLightTheme = Attr("isLightTheme", "boolean")
        val colorForeground = Attr("colorForeground", "color")
        val colorForegroundInverse = Attr("colorForegroundInverse", "color")
        val colorBackground = Attr("colorBackground", "color")
        val colorBackgroundFloating = Attr("colorBackgroundFloating", "color")
        val colorBackgroundCacheHint = Attr("colorBackgroundCacheHint", "color")
        val colorPressedHighlight = Attr("colorPressedHighlight", "color")
        val colorLongPressedHighlight = Attr("colorLongPressedHighlight", "color")
        val colorFocusedHighlight = Attr("colorFocusedHighlight", "color")
        val colorActivatedHighlight = Attr("colorActivatedHighlight", "color")
        val colorMultiSelectHighlight = Attr("colorMultiSelectHighlight", "color")
        val autofilledHighlight = Attr("autofilledHighlight", "reference")
        val autofillDatasetPickerMaxWidth = Attr("autofillDatasetPickerMaxWidth", "reference")
        val autofillDatasetPickerMaxHeight = Attr("autofillDatasetPickerMaxHeight", "reference")
        val autofillSaveCustomSubtitleMaxHeight = Attr("autofillSaveCustomSubtitleMaxHeight", "reference")
        val disabledAlpha = Attr("disabledAlpha", "float")
        val primaryContentAlpha = Attr("primaryContentAlpha", "float")
        val secondaryContentAlpha = Attr("secondaryContentAlpha", "float")
        val colorError = Attr("colorError", "reference|color")
        val backgroundDimAmount = Attr("backgroundDimAmount", "float")
        val backgroundDimEnabled = Attr("backgroundDimEnabled", "boolean")
        val colorPopupBackground = Attr("colorPopupBackground", "color")
        val colorListDivider = Attr("colorListDivider", "color")
        val opacityListDivider = Attr("opacityListDivider", "color")
        val textAppearance = Attr("textAppearance", "reference")
        val textAppearanceInverse = Attr("textAppearanceInverse", "reference")
        val textColorPrimary = Attr("textColorPrimary", "reference|color")
        val textColorSecondary = Attr("textColorSecondary", "reference|color")
        val textColorTertiary = Attr("textColorTertiary", "reference|color")
        val textColorPrimaryInverse = Attr("textColorPrimaryInverse", "reference|color")
        val textColorSecondaryInverse = Attr("textColorSecondaryInverse", "reference|color")
        val textColorTertiaryInverse = Attr("textColorTertiaryInverse", "reference|color")
        val textColorHintInverse = Attr("textColorHintInverse", "reference|color")
        val textColorPrimaryDisableOnly = Attr("textColorPrimaryDisableOnly", "reference|color")
        val textColorPrimaryInverseDisableOnly = Attr("textColorPrimaryInverseDisableOnly", "reference|color")
        val textColorPrimaryNoDisable = Attr("textColorPrimaryNoDisable", "reference|color")
        val textColorSecondaryNoDisable = Attr("textColorSecondaryNoDisable", "reference|color")
        val textColorPrimaryInverseNoDisable = Attr("textColorPrimaryInverseNoDisable", "reference|color")
        val textColorSecondaryInverseNoDisable = Attr("textColorSecondaryInverseNoDisable", "reference|color")
        val textColorPrimaryActivated = Attr("textColorPrimaryActivated", "reference|color")
        val textColorSecondaryActivated = Attr("textColorSecondaryActivated", "reference|color")
        val textColorSearchUrl = Attr("textColorSearchUrl", "reference|color")
        val textColorHighlightInverse = Attr("textColorHighlightInverse", "reference|color")
        val textColorLinkInverse = Attr("textColorLinkInverse", "reference|color")
        val textColorAlertDialogListItem = Attr("textColorAlertDialogListItem", "reference|color")
        val searchWidgetCorpusItemBackground = Attr("searchWidgetCorpusItemBackground", "reference|color")
        val textAppearanceLarge = Attr("textAppearanceLarge", "reference")
        val textAppearanceMedium = Attr("textAppearanceMedium", "reference")
        val textAppearanceSmall = Attr("textAppearanceSmall", "reference")
        val textAppearanceLargeInverse = Attr("textAppearanceLargeInverse", "reference")
        val textAppearanceMediumInverse = Attr("textAppearanceMediumInverse", "reference")
        val textAppearanceSmallInverse = Attr("textAppearanceSmallInverse", "reference")
        val textAppearanceSearchResultTitle = Attr("textAppearanceSearchResultTitle", "reference")
        val textAppearanceSearchResultSubtitle = Attr("textAppearanceSearchResultSubtitle", "reference")
        val textAppearanceButton = Attr("textAppearanceButton", "reference")
        val textAppearanceLargePopupMenu = Attr("textAppearanceLargePopupMenu", "reference")
        val textAppearanceSmallPopupMenu = Attr("textAppearanceSmallPopupMenu", "reference")
        val textAppearancePopupMenuHeader = Attr("textAppearancePopupMenuHeader", "reference")
        val textAppearanceEasyCorrectSuggestion = Attr("textAppearanceEasyCorrectSuggestion", "reference")
        val textAppearanceMisspelledSuggestion = Attr("textAppearanceMisspelledSuggestion", "reference")
        val textAppearanceAutoCorrectionSuggestion = Attr("textAppearanceAutoCorrectionSuggestion", "reference")
        val textUnderlineColor = Attr("textUnderlineColor", "reference|color")
        val textUnderlineThickness = Attr("textUnderlineThickness", "reference|dimension")
        val editTextColor = Attr("editTextColor", "reference|color")
        val editTextBackground = Attr("editTextBackground", "reference")
        val errorMessageBackground = Attr("errorMessageBackground", "reference")
        val errorMessageAboveBackground = Attr("errorMessageAboveBackground", "reference")
        val candidatesTextStyleSpans = Attr("candidatesTextStyleSpans", "reference|string")
        val textCheckMark = Attr("textCheckMark", "reference")
        val textCheckMarkInverse = Attr("textCheckMarkInverse", "reference")
        val listChoiceIndicatorMultiple = Attr("listChoiceIndicatorMultiple", "reference")
        val listChoiceIndicatorSingle = Attr("listChoiceIndicatorSingle", "reference")
        val listChoiceBackgroundIndicator = Attr("listChoiceBackgroundIndicator", "reference")
        val activatedBackgroundIndicator = Attr("activatedBackgroundIndicator", "reference")
        val buttonStyle = Attr("buttonStyle", "reference")
        val buttonStyleSmall = Attr("buttonStyleSmall", "reference")
        val buttonStyleInset = Attr("buttonStyleInset", "reference")
        val buttonStyleToggle = Attr("buttonStyleToggle", "reference")
        val galleryItemBackground = Attr("galleryItemBackground", "reference")
        val listPreferredItemHeight = Attr("listPreferredItemHeight", "dimension")
        val listPreferredItemHeightSmall = Attr("listPreferredItemHeightSmall", "dimension")
        val listPreferredItemHeightLarge = Attr("listPreferredItemHeightLarge", "dimension")
        val searchResultListItemHeight = Attr("searchResultListItemHeight", "dimension")
        val listPreferredItemPaddingLeft = Attr("listPreferredItemPaddingLeft", "dimension")
        val listPreferredItemPaddingRight = Attr("listPreferredItemPaddingRight", "dimension")
        val textAppearanceListItem = Attr("textAppearanceListItem", "reference")
        val textAppearanceListItemSecondary = Attr("textAppearanceListItemSecondary", "reference")
        val textAppearanceListItemSmall = Attr("textAppearanceListItemSmall", "reference")
        val listDivider = Attr("listDivider", "reference")
        val listDividerAlertDialog = Attr("listDividerAlertDialog", "reference")
        val listSeparatorTextViewStyle = Attr("listSeparatorTextViewStyle", "reference")
        val expandableListPreferredItemPaddingLeft = Attr("expandableListPreferredItemPaddingLeft", "dimension")
        val expandableListPreferredChildPaddingLeft = Attr("expandableListPreferredChildPaddingLeft", "dimension")
        val expandableListPreferredItemIndicatorLeft = Attr("expandableListPreferredItemIndicatorLeft", "dimension")
        val expandableListPreferredItemIndicatorRight = Attr("expandableListPreferredItemIndicatorRight", "dimension")
        val expandableListPreferredChildIndicatorLeft = Attr("expandableListPreferredChildIndicatorLeft", "dimension")
        val expandableListPreferredChildIndicatorRight = Attr("expandableListPreferredChildIndicatorRight", "dimension")
        val dropdownListPreferredItemHeight = Attr("dropdownListPreferredItemHeight", "dimension")
        val listPreferredItemPaddingStart = Attr("listPreferredItemPaddingStart", "dimension")
        val listPreferredItemPaddingEnd = Attr("listPreferredItemPaddingEnd", "dimension")
        val windowBackground = Attr("windowBackground", "reference|color")
        val windowBackgroundFallback = Attr("windowBackgroundFallback", "reference|color")
        val windowFrame = Attr("windowFrame", "reference")
        val windowNoTitle = Attr("windowNoTitle", "boolean")
        val windowFullscreen = Attr("windowFullscreen", "boolean")
        val windowOverscan = Attr("windowOverscan", "boolean")
        val windowIsFloating = Attr("windowIsFloating", "boolean")
        val windowIsTranslucent = Attr("windowIsTranslucent", "boolean")
        val windowShowWallpaper = Attr("windowShowWallpaper", "boolean")
        val windowContentOverlay = Attr("windowContentOverlay", "reference")
        val windowTitleSize = Attr("windowTitleSize", "dimension")
        val windowTitleStyle = Attr("windowTitleStyle", "reference")
        val windowTitleBackgroundStyle = Attr("windowTitleBackgroundStyle", "reference")
        val windowAnimationStyle = Attr("windowAnimationStyle", "reference")
        val windowActionBar = Attr("windowActionBar", "boolean")
        val windowActionBarOverlay = Attr("windowActionBarOverlay", "boolean")
        val windowActionModeOverlay = Attr("windowActionModeOverlay", "boolean")
        val windowSoftInputMode = Attr("windowSoftInputMode", "flag", mutableListOf("stateUnspecified" to 0L, "stateUnchanged" to 1L, "stateHidden" to 2L, "stateAlwaysHidden" to 3L, "stateVisible" to 4L, "stateAlwaysVisible" to 5L, "adjustUnspecified" to 0x00L, "adjustResize" to 0x10L, "adjustPan" to 0x20L, "adjustNothing" to 0x30L))
        val windowDisablePreview = Attr("windowDisablePreview", "boolean")
        val windowNoDisplay = Attr("windowNoDisplay", "boolean")
        val windowEnableSplitTouch = Attr("windowEnableSplitTouch", "boolean")
        val windowCloseOnTouchOutside = Attr("windowCloseOnTouchOutside", "boolean")
        val windowTranslucentStatus = Attr("windowTranslucentStatus", "boolean")
        val windowTranslucentNavigation = Attr("windowTranslucentNavigation", "boolean")
        val windowSwipeToDismiss = Attr("windowSwipeToDismiss", "boolean")
        val windowContentTransitions = Attr("windowContentTransitions", "boolean")
        val windowContentTransitionManager = Attr("windowContentTransitionManager", "reference")
        val windowActivityTransitions = Attr("windowActivityTransitions", "boolean")
        val windowEnterTransition = Attr("windowEnterTransition", "reference")
        val windowReturnTransition = Attr("windowReturnTransition", "reference")
        val windowExitTransition = Attr("windowExitTransition", "reference")
        val windowReenterTransition = Attr("windowReenterTransition", "reference")
        val windowSharedElementEnterTransition = Attr("windowSharedElementEnterTransition", "reference")
        val windowSharedElementReturnTransition = Attr("windowSharedElementReturnTransition", "reference")
        val windowSharedElementExitTransition = Attr("windowSharedElementExitTransition", "reference")
        val windowSharedElementReenterTransition = Attr("windowSharedElementReenterTransition", "reference")
        val windowAllowEnterTransitionOverlap = Attr("windowAllowEnterTransitionOverlap", "boolean")
        val windowAllowReturnTransitionOverlap = Attr("windowAllowReturnTransitionOverlap", "boolean")
        val windowSharedElementsUseOverlay = Attr("windowSharedElementsUseOverlay", "boolean")
        val windowActionBarFullscreenDecorLayout = Attr("windowActionBarFullscreenDecorLayout", "reference")
        val windowTransitionBackgroundFadeDuration = Attr("windowTransitionBackgroundFadeDuration", "integer")
        val floatingToolbarCloseDrawable = Attr("floatingToolbarCloseDrawable", "reference")
        val floatingToolbarForegroundColor = Attr("floatingToolbarForegroundColor", "reference|color")
        val floatingToolbarItemBackgroundBorderlessDrawable = Attr("floatingToolbarItemBackgroundBorderlessDrawable", "reference")
        val floatingToolbarItemBackgroundDrawable = Attr("floatingToolbarItemBackgroundDrawable", "reference")
        val floatingToolbarOpenDrawable = Attr("floatingToolbarOpenDrawable", "reference")
        val floatingToolbarPopupBackgroundDrawable = Attr("floatingToolbarPopupBackgroundDrawable", "reference")
        val floatingToolbarDividerColor = Attr("floatingToolbarDividerColor", "reference")
        val alertDialogStyle = Attr("alertDialogStyle", "reference")
        val alertDialogButtonGroupStyle = Attr("alertDialogButtonGroupStyle", "reference")
        val alertDialogCenterButtons = Attr("alertDialogCenterButtons", "boolean")
        val detailsElementBackground = Attr("detailsElementBackground", "reference")
        val fingerprintAuthDrawable = Attr("fingerprintAuthDrawable", "reference")
        val panelBackground = Attr("panelBackground", "reference|color")
        val panelFullBackground = Attr("panelFullBackground", "reference|color")
        val panelColorForeground = Attr("panelColorForeground", "reference|color")
        val panelColorBackground = Attr("panelColorBackground", "reference|color")
        val panelTextAppearance = Attr("panelTextAppearance", "reference")
        val panelMenuIsCompact = Attr("panelMenuIsCompact", "boolean")
        val panelMenuListWidth = Attr("panelMenuListWidth", "dimension")
        val panelMenuListTheme = Attr("panelMenuListTheme", "reference")
        val absListViewStyle = Attr("absListViewStyle", "reference")
        val autoCompleteTextViewStyle = Attr("autoCompleteTextViewStyle", "reference")
        val checkboxStyle = Attr("checkboxStyle", "reference")
        val checkedTextViewStyle = Attr("checkedTextViewStyle", "reference")
        val dropDownListViewStyle = Attr("dropDownListViewStyle", "reference")
        val editTextStyle = Attr("editTextStyle", "reference")
        val expandableListViewStyle = Attr("expandableListViewStyle", "reference")
        val expandableListViewWhiteStyle = Attr("expandableListViewWhiteStyle", "reference")
        val galleryStyle = Attr("galleryStyle", "reference")
        val gestureOverlayViewStyle = Attr("gestureOverlayViewStyle", "reference")
        val gridViewStyle = Attr("gridViewStyle", "reference")
        val imageButtonStyle = Attr("imageButtonStyle", "reference")
        val imageWellStyle = Attr("imageWellStyle", "reference")
        val listMenuViewStyle = Attr("listMenuViewStyle", "reference")
        val listViewStyle = Attr("listViewStyle", "reference")
        val listViewWhiteStyle = Attr("listViewWhiteStyle", "reference")
        val popupWindowStyle = Attr("popupWindowStyle", "reference")
        val progressBarStyle = Attr("progressBarStyle", "reference")
        val progressBarStyleHorizontal = Attr("progressBarStyleHorizontal", "reference")
        val progressBarStyleSmall = Attr("progressBarStyleSmall", "reference")
        val progressBarStyleSmallTitle = Attr("progressBarStyleSmallTitle", "reference")
        val progressBarStyleLarge = Attr("progressBarStyleLarge", "reference")
        val progressBarStyleInverse = Attr("progressBarStyleInverse", "reference")
        val progressBarStyleSmallInverse = Attr("progressBarStyleSmallInverse", "reference")
        val progressBarStyleLargeInverse = Attr("progressBarStyleLargeInverse", "reference")
        val seekBarStyle = Attr("seekBarStyle", "reference")
        val ratingBarStyle = Attr("ratingBarStyle", "reference")
        val ratingBarStyleIndicator = Attr("ratingBarStyleIndicator", "reference")
        val ratingBarStyleSmall = Attr("ratingBarStyleSmall", "reference")
        val radioButtonStyle = Attr("radioButtonStyle", "reference")
        val scrollViewStyle = Attr("scrollViewStyle", "reference")
        val horizontalScrollViewStyle = Attr("horizontalScrollViewStyle", "reference")
        val spinnerStyle = Attr("spinnerStyle", "reference")
        val dropDownSpinnerStyle = Attr("dropDownSpinnerStyle", "reference")
        val actionDropDownStyle = Attr("actionDropDownStyle", "reference")
        val actionButtonStyle = Attr("actionButtonStyle", "reference")
        val starStyle = Attr("starStyle", "reference")
        val tabWidgetStyle = Attr("tabWidgetStyle", "reference")
        val textViewStyle = Attr("textViewStyle", "reference")
        val webTextViewStyle = Attr("webTextViewStyle", "reference")
        val webViewStyle = Attr("webViewStyle", "reference")
        val dropDownItemStyle = Attr("dropDownItemStyle", "reference")
        val spinnerDropDownItemStyle = Attr("spinnerDropDownItemStyle", "reference")
        val dropDownHintAppearance = Attr("dropDownHintAppearance", "reference")
        val spinnerItemStyle = Attr("spinnerItemStyle", "reference")
        val mapViewStyle = Attr("mapViewStyle", "reference")
        val quickContactBadgeOverlay = Attr("quickContactBadgeOverlay", "reference")
        val quickContactBadgeStyleWindowSmall = Attr("quickContactBadgeStyleWindowSmall", "reference")
        val quickContactBadgeStyleWindowMedium = Attr("quickContactBadgeStyleWindowMedium", "reference")
        val quickContactBadgeStyleWindowLarge = Attr("quickContactBadgeStyleWindowLarge", "reference")
        val quickContactBadgeStyleSmallWindowSmall = Attr("quickContactBadgeStyleSmallWindowSmall", "reference")
        val quickContactBadgeStyleSmallWindowMedium = Attr("quickContactBadgeStyleSmallWindowMedium", "reference")
        val quickContactBadgeStyleSmallWindowLarge = Attr("quickContactBadgeStyleSmallWindowLarge", "reference")
        val textSelectHandleWindowStyle = Attr("textSelectHandleWindowStyle", "reference")
        val textSuggestionsWindowStyle = Attr("textSuggestionsWindowStyle", "reference")
        val listPopupWindowStyle = Attr("listPopupWindowStyle", "reference")
        val popupMenuStyle = Attr("popupMenuStyle", "reference")
        val contextPopupMenuStyle = Attr("contextPopupMenuStyle", "reference")
        val stackViewStyle = Attr("stackViewStyle", "reference")
        val magnifierStyle = Attr("magnifierStyle", "reference")
        val fragmentBreadCrumbsStyle = Attr("fragmentBreadCrumbsStyle", "reference")
        val numberPickerStyle = Attr("numberPickerStyle", "reference")
        val calendarViewStyle = Attr("calendarViewStyle", "reference")
        val timePickerStyle = Attr("timePickerStyle", "reference")
        val timePickerDialogTheme = Attr("timePickerDialogTheme", "reference")
        val datePickerStyle = Attr("datePickerStyle", "reference")
        val datePickerDialogTheme = Attr("datePickerDialogTheme", "reference")
        val activityChooserViewStyle = Attr("activityChooserViewStyle", "reference")
        val toolbarStyle = Attr("toolbarStyle", "reference")
        val fastScrollThumbDrawable = Attr("fastScrollThumbDrawable", "reference")
        val fastScrollPreviewBackgroundRight = Attr("fastScrollPreviewBackgroundRight", "reference")
        val fastScrollPreviewBackgroundLeft = Attr("fastScrollPreviewBackgroundLeft", "reference")
        val fastScrollTrackDrawable = Attr("fastScrollTrackDrawable", "reference")
        val fastScrollOverlayPosition = Attr("fastScrollOverlayPosition", "enum", mutableListOf("floating" to 0L, "atThumb" to 1L, "aboveThumb" to 2L))
        val fastScrollTextColor = Attr("fastScrollTextColor", "color")
        val actionBarTabStyle = Attr("actionBarTabStyle", "reference")
        val actionBarTabBarStyle = Attr("actionBarTabBarStyle", "reference")
        val actionBarTabTextStyle = Attr("actionBarTabTextStyle", "reference")
        val actionOverflowButtonStyle = Attr("actionOverflowButtonStyle", "reference")
        val actionOverflowMenuStyle = Attr("actionOverflowMenuStyle", "reference")
        val actionBarPopupTheme = Attr("actionBarPopupTheme", "reference")
        val actionBarStyle = Attr("actionBarStyle", "reference")
        val actionBarSplitStyle = Attr("actionBarSplitStyle", "reference")
        val actionBarTheme = Attr("actionBarTheme", "reference")
        val actionBarWidgetTheme = Attr("actionBarWidgetTheme", "reference")
        val actionBarSize = Attr("actionBarSize", "dimension", mutableListOf("wrap_content" to 0L))
        val actionBarDivider = Attr("actionBarDivider", "reference")
        val actionBarItemBackground = Attr("actionBarItemBackground", "reference")
        val actionMenuTextAppearance = Attr("actionMenuTextAppearance", "reference")
        val actionMenuTextColor = Attr("actionMenuTextColor", "color|reference")
        val actionModeStyle = Attr("actionModeStyle", "reference")
        val actionModeCloseButtonStyle = Attr("actionModeCloseButtonStyle", "reference")
        val actionModeBackground = Attr("actionModeBackground", "reference")
        val actionModeSplitBackground = Attr("actionModeSplitBackground", "reference")
        val actionModeCloseDrawable = Attr("actionModeCloseDrawable", "reference")
        val actionModeCutDrawable = Attr("actionModeCutDrawable", "reference")
        val actionModeCopyDrawable = Attr("actionModeCopyDrawable", "reference")
        val actionModePasteDrawable = Attr("actionModePasteDrawable", "reference")
        val actionModeSelectAllDrawable = Attr("actionModeSelectAllDrawable", "reference")
        val actionModeShareDrawable = Attr("actionModeShareDrawable", "reference")
        val actionModeFindDrawable = Attr("actionModeFindDrawable", "reference")
        val actionModeWebSearchDrawable = Attr("actionModeWebSearchDrawable", "reference")
        val actionModePopupWindowStyle = Attr("actionModePopupWindowStyle", "reference")
        val preferenceScreenStyle = Attr("preferenceScreenStyle", "reference")
        val preferenceActivityStyle = Attr("preferenceActivityStyle", "reference")
        val preferenceFragmentStyle = Attr("preferenceFragmentStyle", "reference")
        val preferenceCategoryStyle = Attr("preferenceCategoryStyle", "reference")
        val preferenceStyle = Attr("preferenceStyle", "reference")
        val preferenceInformationStyle = Attr("preferenceInformationStyle", "reference")
        val checkBoxPreferenceStyle = Attr("checkBoxPreferenceStyle", "reference")
        val yesNoPreferenceStyle = Attr("yesNoPreferenceStyle", "reference")
        val dialogPreferenceStyle = Attr("dialogPreferenceStyle", "reference")
        val editTextPreferenceStyle = Attr("editTextPreferenceStyle", "reference")
        val seekBarDialogPreferenceStyle = Attr("seekBarDialogPreferenceStyle", "reference")
        val ringtonePreferenceStyle = Attr("ringtonePreferenceStyle", "reference")
        val preferenceLayoutChild = Attr("preferenceLayoutChild", "reference")
        val preferencePanelStyle = Attr("preferencePanelStyle", "reference")
        val preferenceHeaderPanelStyle = Attr("preferenceHeaderPanelStyle", "reference")
        val preferenceListStyle = Attr("preferenceListStyle", "reference")
        val preferenceFragmentListStyle = Attr("preferenceFragmentListStyle", "reference")
        val preferenceFragmentPaddingSide = Attr("preferenceFragmentPaddingSide", "dimension")
        val switchPreferenceStyle = Attr("switchPreferenceStyle", "reference")
        val seekBarPreferenceStyle = Attr("seekBarPreferenceStyle", "reference")
        val textSelectHandleLeft = Attr("textSelectHandleLeft", "reference")
        val textSelectHandleRight = Attr("textSelectHandleRight", "reference")
        val textSelectHandle = Attr("textSelectHandle", "reference")
        val textEditPasteWindowLayout = Attr("textEditPasteWindowLayout", "reference")
        val textEditNoPasteWindowLayout = Attr("textEditNoPasteWindowLayout", "reference")
        val textEditSidePasteWindowLayout = Attr("textEditSidePasteWindowLayout", "reference")
        val textEditSideNoPasteWindowLayout = Attr("textEditSideNoPasteWindowLayout", "reference")
        val textEditSuggestionItemLayout = Attr("textEditSuggestionItemLayout", "reference")
        val textEditSuggestionContainerLayout = Attr("textEditSuggestionContainerLayout", "reference")
        val textEditSuggestionHighlightStyle = Attr("textEditSuggestionHighlightStyle", "reference")
        val dialogTheme = Attr("dialogTheme", "reference")
        val dialogTitleIconsDecorLayout = Attr("dialogTitleIconsDecorLayout", "reference")
        val dialogCustomTitleDecorLayout = Attr("dialogCustomTitleDecorLayout", "reference")
        val dialogTitleDecorLayout = Attr("dialogTitleDecorLayout", "reference")
        val dialogPreferredPadding = Attr("dialogPreferredPadding", "dimension")
        val dialogCornerRadius = Attr("dialogCornerRadius", "dimension")
        val alertDialogTheme = Attr("alertDialogTheme", "reference")
        val alertDialogIcon = Attr("alertDialogIcon", "reference")
        val presentationTheme = Attr("presentationTheme", "reference")
        val dividerVertical = Attr("dividerVertical", "reference")
        val dividerHorizontal = Attr("dividerHorizontal", "reference")
        val buttonBarStyle = Attr("buttonBarStyle", "reference")
        val buttonBarButtonStyle = Attr("buttonBarButtonStyle", "reference")
        val buttonBarPositiveButtonStyle = Attr("buttonBarPositiveButtonStyle", "reference")
        val buttonBarNegativeButtonStyle = Attr("buttonBarNegativeButtonStyle", "reference")
        val buttonBarNeutralButtonStyle = Attr("buttonBarNeutralButtonStyle", "reference")
        val buttonCornerRadius = Attr("buttonCornerRadius", "dimension")
        val progressBarCornerRadius = Attr("progressBarCornerRadius", "dimension")
        val searchViewStyle = Attr("searchViewStyle", "reference")
        val segmentedButtonStyle = Attr("segmentedButtonStyle", "reference")
        val selectableItemBackground = Attr("selectableItemBackground", "reference")
        val selectableItemBackgroundBorderless = Attr("selectableItemBackgroundBorderless", "reference")
        val borderlessButtonStyle = Attr("borderlessButtonStyle", "reference")
        val toastFrameBackground = Attr("toastFrameBackground", "reference")
        val tooltipFrameBackground = Attr("tooltipFrameBackground", "reference")
        val tooltipForegroundColor = Attr("tooltipForegroundColor", "reference|color")
        val tooltipBackgroundColor = Attr("tooltipBackgroundColor", "reference|color")
        val searchDialogTheme = Attr("searchDialogTheme", "reference")
        val homeAsUpIndicator = Attr("homeAsUpIndicator", "reference")
        val preferenceFrameLayoutStyle = Attr("preferenceFrameLayoutStyle", "reference")
        val switchStyle = Attr("switchStyle", "reference")
        val mediaRouteButtonStyle = Attr("mediaRouteButtonStyle", "reference")
        val accessibilityFocusedDrawable = Attr("accessibilityFocusedDrawable", "reference")
        val findOnPageNextDrawable = Attr("findOnPageNextDrawable", "reference")
        val findOnPagePreviousDrawable = Attr("findOnPagePreviousDrawable", "reference")
        val colorPrimary = Attr("colorPrimary", "color")
        val colorPrimaryDark = Attr("colorPrimaryDark", "color")
        val colorSecondary = Attr("colorSecondary", "color")
        val colorAccent = Attr("colorAccent", "color")
        val colorControlNormal = Attr("colorControlNormal", "color")
        val colorControlActivated = Attr("colorControlActivated", "color")
        val colorControlHighlight = Attr("colorControlHighlight", "color")
        val colorButtonNormal = Attr("colorButtonNormal", "color")
        val colorSwitchThumbNormal = Attr("colorSwitchThumbNormal", "color")
        val colorProgressBackgroundNormal = Attr("colorProgressBackgroundNormal", "color")
        val colorEdgeEffect = Attr("colorEdgeEffect", "color")
        val lightY = Attr("lightY", "dimension")
        val lightZ = Attr("lightZ", "dimension")
        val lightRadius = Attr("lightRadius", "dimension")
        val ambientShadowAlpha = Attr("ambientShadowAlpha", "float")
        val spotShadowAlpha = Attr("spotShadowAlpha", "float")
        val forceDarkAllowed = Attr("forceDarkAllowed", "boolean")
    }

    object Window {
        val windowBackground = Attr("windowBackground")
        val windowBackgroundFallback = Attr("windowBackgroundFallback")
        val windowContentOverlay = Attr("windowContentOverlay")
        val windowFrame = Attr("windowFrame")
        val windowNoTitle = Attr("windowNoTitle")
        val windowFullscreen = Attr("windowFullscreen")
        val windowOverscan = Attr("windowOverscan")
        val windowIsFloating = Attr("windowIsFloating")
        val windowIsTranslucent = Attr("windowIsTranslucent")
        val windowShowWallpaper = Attr("windowShowWallpaper")
        val windowAnimationStyle = Attr("windowAnimationStyle")
        val windowSoftInputMode = Attr("windowSoftInputMode")
        val windowDisablePreview = Attr("windowDisablePreview")
        val windowNoDisplay = Attr("windowNoDisplay")
        val textColor = Attr("textColor")
        val backgroundDimEnabled = Attr("backgroundDimEnabled")
        val backgroundDimAmount = Attr("backgroundDimAmount")
        val windowActionBar = Attr("windowActionBar")
        val windowActionModeOverlay = Attr("windowActionModeOverlay")
        val windowActionBarOverlay = Attr("windowActionBarOverlay")
        val windowEnableSplitTouch = Attr("windowEnableSplitTouch")
        val windowCloseOnTouchOutside = Attr("windowCloseOnTouchOutside")
        val windowTranslucentStatus = Attr("windowTranslucentStatus")
        val windowTranslucentNavigation = Attr("windowTranslucentNavigation")
        val windowSwipeToDismiss = Attr("windowSwipeToDismiss")
        val windowContentTransitions = Attr("windowContentTransitions")
        val windowActivityTransitions = Attr("windowActivityTransitions")
        val windowContentTransitionManager = Attr("windowContentTransitionManager")
        val windowActionBarFullscreenDecorLayout = Attr("windowActionBarFullscreenDecorLayout")
        val windowMinWidthMajor = Attr("windowMinWidthMajor", "dimension|fraction")
        val windowMinWidthMinor = Attr("windowMinWidthMinor", "dimension|fraction")
        val windowFixedWidthMajor = Attr("windowFixedWidthMajor", "dimension|fraction")
        val windowFixedHeightMinor = Attr("windowFixedHeightMinor", "dimension|fraction")
        val windowFixedWidthMinor = Attr("windowFixedWidthMinor", "dimension|fraction")
        val windowFixedHeightMajor = Attr("windowFixedHeightMajor", "dimension|fraction")
        val windowOutsetBottom = Attr("windowOutsetBottom", "dimension")
        val windowEnterTransition = Attr("windowEnterTransition")
        val windowReturnTransition = Attr("windowReturnTransition")
        val windowExitTransition = Attr("windowExitTransition")
        val windowReenterTransition = Attr("windowReenterTransition")
        val windowSharedElementEnterTransition = Attr("windowSharedElementEnterTransition")
        val windowSharedElementReturnTransition = Attr("windowSharedElementReturnTransition")
        val windowSharedElementExitTransition = Attr("windowSharedElementExitTransition")
        val windowSharedElementReenterTransition = Attr("windowSharedElementReenterTransition")
        val windowAllowEnterTransitionOverlap = Attr("windowAllowEnterTransitionOverlap")
        val windowAllowReturnTransitionOverlap = Attr("windowAllowReturnTransitionOverlap")
        val windowSharedElementsUseOverlay = Attr("windowSharedElementsUseOverlay")
        val windowDrawsSystemBarBackgrounds = Attr("windowDrawsSystemBarBackgrounds", "boolean")
        val statusBarColor = Attr("statusBarColor", "color")
        val navigationBarColor = Attr("navigationBarColor", "color")
        val navigationBarDividerColor = Attr("navigationBarDividerColor", "color")
        val enforceStatusBarContrast = Attr("enforceStatusBarContrast", "boolean")
        val enforceNavigationBarContrast = Attr("enforceNavigationBarContrast", "boolean")
        val windowTransitionBackgroundFadeDuration = Attr("windowTransitionBackgroundFadeDuration")
        val windowElevation = Attr("windowElevation", "dimension")
        val windowClipToOutline = Attr("windowClipToOutline", "boolean")
        val windowLightStatusBar = Attr("windowLightStatusBar", "boolean")
        val windowSplashscreenContent = Attr("windowSplashscreenContent", "reference")
        val windowLightNavigationBar = Attr("windowLightNavigationBar", "boolean")
        val windowLayoutInDisplayCutoutMode = Attr("windowLayoutInDisplayCutoutMode", "enum", mutableListOf("default" to 0L, "shortEdges" to 1L, "never" to 2L))
    }

    object AlertDialog {
        val fullDark = Attr("fullDark", "reference|color")
        val topDark = Attr("topDark", "reference|color")
        val centerDark = Attr("centerDark", "reference|color")
        val bottomDark = Attr("bottomDark", "reference|color")
        val fullBright = Attr("fullBright", "reference|color")
        val topBright = Attr("topBright", "reference|color")
        val centerBright = Attr("centerBright", "reference|color")
        val bottomBright = Attr("bottomBright", "reference|color")
        val bottomMedium = Attr("bottomMedium", "reference|color")
        val centerMedium = Attr("centerMedium", "reference|color")
        val layout = Attr("layout")
        val buttonPanelSideLayout = Attr("buttonPanelSideLayout", "reference")
        val listLayout = Attr("listLayout", "reference")
        val multiChoiceItemLayout = Attr("multiChoiceItemLayout", "reference")
        val singleChoiceItemLayout = Attr("singleChoiceItemLayout", "reference")
        val listItemLayout = Attr("listItemLayout", "reference")
        val progressLayout = Attr("progressLayout", "reference")
        val horizontalProgressLayout = Attr("horizontalProgressLayout", "reference")
        val showTitle = Attr("showTitle", "boolean")
        val needsDefaultBackgrounds = Attr("needsDefaultBackgrounds", "boolean")
        val controllerType = Attr("controllerType", "enum", mutableListOf("normal" to 0L, "micro" to 1L))
        val selectionScrollOffset = Attr("selectionScrollOffset", "dimension")
    }

    object ButtonBarLayout {
        val allowStacking = Attr("allowStacking", "boolean")
    }

    object FragmentAnimation {
        val fragmentOpenEnterAnimation = Attr("fragmentOpenEnterAnimation", "reference")
        val fragmentOpenExitAnimation = Attr("fragmentOpenExitAnimation", "reference")
        val fragmentCloseEnterAnimation = Attr("fragmentCloseEnterAnimation", "reference")
        val fragmentCloseExitAnimation = Attr("fragmentCloseExitAnimation", "reference")
        val fragmentFadeEnterAnimation = Attr("fragmentFadeEnterAnimation", "reference")
        val fragmentFadeExitAnimation = Attr("fragmentFadeExitAnimation", "reference")
    }

    object WindowAnimation {
        val windowEnterAnimation = Attr("windowEnterAnimation", "reference")
        val windowExitAnimation = Attr("windowExitAnimation", "reference")
        val windowShowAnimation = Attr("windowShowAnimation", "reference")
        val windowHideAnimation = Attr("windowHideAnimation", "reference")
        val activityOpenEnterAnimation = Attr("activityOpenEnterAnimation", "reference")
        val activityOpenExitAnimation = Attr("activityOpenExitAnimation", "reference")
        val activityCloseEnterAnimation = Attr("activityCloseEnterAnimation", "reference")
        val activityCloseExitAnimation = Attr("activityCloseExitAnimation", "reference")
        val taskOpenEnterAnimation = Attr("taskOpenEnterAnimation", "reference")
        val taskOpenExitAnimation = Attr("taskOpenExitAnimation", "reference")
        val launchTaskBehindTargetAnimation = Attr("launchTaskBehindTargetAnimation", "reference")
        val launchTaskBehindSourceAnimation = Attr("launchTaskBehindSourceAnimation", "reference")
        val taskCloseEnterAnimation = Attr("taskCloseEnterAnimation", "reference")
        val taskCloseExitAnimation = Attr("taskCloseExitAnimation", "reference")
        val taskToFrontEnterAnimation = Attr("taskToFrontEnterAnimation", "reference")
        val taskToFrontExitAnimation = Attr("taskToFrontExitAnimation", "reference")
        val taskToBackEnterAnimation = Attr("taskToBackEnterAnimation", "reference")
        val taskToBackExitAnimation = Attr("taskToBackExitAnimation", "reference")
        val wallpaperOpenEnterAnimation = Attr("wallpaperOpenEnterAnimation", "reference")
        val wallpaperOpenExitAnimation = Attr("wallpaperOpenExitAnimation", "reference")
        val wallpaperCloseEnterAnimation = Attr("wallpaperCloseEnterAnimation", "reference")
        val wallpaperCloseExitAnimation = Attr("wallpaperCloseExitAnimation", "reference")
        val wallpaperIntraOpenEnterAnimation = Attr("wallpaperIntraOpenEnterAnimation", "reference")
        val wallpaperIntraOpenExitAnimation = Attr("wallpaperIntraOpenExitAnimation", "reference")
        val wallpaperIntraCloseEnterAnimation = Attr("wallpaperIntraCloseEnterAnimation", "reference")
        val wallpaperIntraCloseExitAnimation = Attr("wallpaperIntraCloseExitAnimation", "reference")
        val activityOpenRemoteViewsEnterAnimation = Attr("activityOpenRemoteViewsEnterAnimation", "reference")
    }

    object View {
        val id = Attr("id", "reference")
        val tag = Attr("tag", "string")
        val scrollX = Attr("scrollX", "dimension")
        val scrollY = Attr("scrollY", "dimension")
        val background = Attr("background", "reference|color")
        val padding = Attr("padding", "dimension")
        val paddingHorizontal = Attr("paddingHorizontal", "dimension")
        val paddingVertical = Attr("paddingVertical", "dimension")
        val paddingLeft = Attr("paddingLeft", "dimension")
        val paddingTop = Attr("paddingTop", "dimension")
        val paddingRight = Attr("paddingRight", "dimension")
        val paddingBottom = Attr("paddingBottom", "dimension")
        val paddingStart = Attr("paddingStart", "dimension")
        val paddingEnd = Attr("paddingEnd", "dimension")
        val focusable = Attr("focusable", "boolean|enum", mutableListOf("auto" to 0x00000010L))
        val __removed3 = Resources.__removed3
        val __removed4 = Resources.__removed4
        val __removed5 = Resources.__removed5
        val autofillHints = Attr("autofillHints", "string|reference")
        val importantForAutofill = Attr("importantForAutofill", "flag", mutableListOf("auto" to 0L, "yes" to 0x1L, "no" to 0x2L, "yesExcludeDescendants" to 0x4L, "noExcludeDescendants" to 0x8L))
        val __removed6 = Resources.__removed6
        val focusableInTouchMode = Attr("focusableInTouchMode", "boolean")
        val visibility = Attr("visibility", "enum", mutableListOf("visible" to 0L, "invisible" to 1L, "gone" to 2L))
        val fitsSystemWindows = Attr("fitsSystemWindows", "boolean")
        val scrollbars = Attr("scrollbars", "flag", mutableListOf("none" to 0x00000000L, "horizontal" to 0x00000100L, "vertical" to 0x00000200L))
        val scrollbarStyle = Attr("scrollbarStyle", "enum", mutableListOf("insideOverlay" to 0x0L, "insideInset" to 0x01000000L, "outsideOverlay" to 0x02000000L, "outsideInset" to 0x03000000L))
        val isScrollContainer = Attr("isScrollContainer", "boolean")
        val fadeScrollbars = Attr("fadeScrollbars", "boolean")
        val scrollbarFadeDuration = Attr("scrollbarFadeDuration", "integer")
        val scrollbarDefaultDelayBeforeFade = Attr("scrollbarDefaultDelayBeforeFade", "integer")
        val scrollbarSize = Attr("scrollbarSize", "dimension")
        val scrollbarThumbHorizontal = Attr("scrollbarThumbHorizontal", "reference")
        val scrollbarThumbVertical = Attr("scrollbarThumbVertical", "reference")
        val scrollbarTrackHorizontal = Attr("scrollbarTrackHorizontal", "reference")
        val scrollbarTrackVertical = Attr("scrollbarTrackVertical", "reference")
        val scrollbarAlwaysDrawHorizontalTrack = Attr("scrollbarAlwaysDrawHorizontalTrack", "boolean")
        val scrollbarAlwaysDrawVerticalTrack = Attr("scrollbarAlwaysDrawVerticalTrack", "boolean")
        val fadingEdge = Attr("fadingEdge", "flag", mutableListOf("none" to 0x00000000L, "horizontal" to 0x00001000L, "vertical" to 0x00002000L))
        val requiresFadingEdge = Attr("requiresFadingEdge", "flag", mutableListOf("none" to 0x00000000L, "horizontal" to 0x00001000L, "vertical" to 0x00002000L))
        val fadingEdgeLength = Attr("fadingEdgeLength", "dimension")
        val nextFocusLeft = Attr("nextFocusLeft", "reference")
        val nextFocusRight = Attr("nextFocusRight", "reference")
        val nextFocusUp = Attr("nextFocusUp", "reference")
        val nextFocusDown = Attr("nextFocusDown", "reference")
        val nextFocusForward = Attr("nextFocusForward", "reference")
        val clickable = Attr("clickable", "boolean")
        val longClickable = Attr("longClickable", "boolean")
        val contextClickable = Attr("contextClickable", "boolean")
        val saveEnabled = Attr("saveEnabled", "boolean")
        val filterTouchesWhenObscured = Attr("filterTouchesWhenObscured", "boolean")
        val drawingCacheQuality = Attr("drawingCacheQuality", "enum", mutableListOf("auto" to 0L, "low" to 1L, "high" to 2L))
        val keepScreenOn = Attr("keepScreenOn", "boolean")
        val duplicateParentState = Attr("duplicateParentState", "boolean")
        val minHeight = Attr("minHeight", "dimen")
        val minWidth = Attr("minWidth", "dimen")
        val soundEffectsEnabled = Attr("soundEffectsEnabled", "boolean")
        val hapticFeedbackEnabled = Attr("hapticFeedbackEnabled", "boolean")
        val contentDescription = Attr("contentDescription", "string")
        val accessibilityTraversalBefore = Attr("accessibilityTraversalBefore", "integer")
        val accessibilityTraversalAfter = Attr("accessibilityTraversalAfter", "integer")
        val onClick = Attr("onClick", "string")
        val overScrollMode = Attr("overScrollMode", "enum", mutableListOf("always" to 0L, "ifContentScrolls" to 1L, "never" to 2L))
        val alpha = Attr("alpha", "float")
        val elevation = Attr("elevation", "dimension")
        val translationX = Attr("translationX", "dimension")
        val translationY = Attr("translationY", "dimension")
        val translationZ = Attr("translationZ", "dimension")
        val transformPivotX = Attr("transformPivotX", "dimension")
        val transformPivotY = Attr("transformPivotY", "dimension")
        val rotation = Attr("rotation", "float")
        val rotationX = Attr("rotationX", "float")
        val rotationY = Attr("rotationY", "float")
        val scaleX = Attr("scaleX", "float")
        val scaleY = Attr("scaleY", "float")
        val verticalScrollbarPosition = Attr("verticalScrollbarPosition", "enum", mutableListOf("defaultPosition" to 0L, "left" to 1L, "right" to 2L))
        val layerType = Attr("layerType", "enum", mutableListOf("none" to 0L, "software" to 1L, "hardware" to 2L))
        val layoutDirection = Attr("layoutDirection", "enum", mutableListOf("ltr" to 0L, "rtl" to 1L, "inherit" to 2L, "locale" to 3L))
        val textDirection = Attr("textDirection", "integer", mutableListOf("inherit" to 0L, "firstStrong" to 1L, "anyRtl" to 2L, "ltr" to 3L, "rtl" to 4L, "locale" to 5L, "firstStrongLtr" to 6L, "firstStrongRtl" to 7L))
        val textAlignment = Attr("textAlignment", "integer", mutableListOf("inherit" to 0L, "gravity" to 1L, "textStart" to 2L, "textEnd" to 3L, "center" to 4L, "viewStart" to 5L, "viewEnd" to 6L))
        val importantForAccessibility = Attr("importantForAccessibility", "integer", mutableListOf("auto" to 0L, "yes" to 1L, "no" to 2L, "noHideDescendants" to 4L))
        val accessibilityLiveRegion = Attr("accessibilityLiveRegion", "integer", mutableListOf("none" to 0L, "polite" to 1L, "assertive" to 2L))
        val labelFor = Attr("labelFor", "reference")
        val theme = Attr("theme", "reference")
        val transitionName = Attr("transitionName", "string")
        val nestedScrollingEnabled = Attr("nestedScrollingEnabled", "boolean")
        val stateListAnimator = Attr("stateListAnimator", "reference")
        val backgroundTint = Attr("backgroundTint", "color|drawable")
        val backgroundTintMode = Attr("backgroundTintMode", "enum", mutableListOf("src_over" to 3L, "src_in" to 5L, "src_atop" to 9L, "multiply" to 14L, "screen" to 15L, "add" to 16L))
        val outlineProvider = Attr("outlineProvider", "enum", mutableListOf("background" to 0L, "none" to 1L, "bounds" to 2L, "paddedBounds" to 3L))
        val foreground = Attr("foreground", "reference|color")
        val foregroundGravity = Attr("foregroundGravity", "flag", mutableListOf("top" to 0x30L, "bottom" to 0x50L, "left" to 0x03L, "right" to 0x05L, "center_vertical" to 0x10L, "fill_vertical" to 0x70L, "center_horizontal" to 0x01L, "fill_horizontal" to 0x07L, "center" to 0x11L, "fill" to 0x77L, "clip_vertical" to 0x80L, "clip_horizontal" to 0x08L))
        val foregroundInsidePadding = Attr("foregroundInsidePadding", "boolean")
        val foregroundTint = Attr("foregroundTint", "color|drawable")
        val foregroundTintMode = Attr("foregroundTintMode", "enum", mutableListOf("src_over" to 3L, "src_in" to 5L, "src_atop" to 9L, "multiply" to 14L, "screen" to 15L, "add" to 16L))
        val scrollIndicators = Attr("scrollIndicators", "flag", mutableListOf("none" to 0x00L, "top" to 0x01L, "bottom" to 0x02L, "left" to 0x04L, "right" to 0x08L, "start" to 0x10L, "end" to 0x20L))
        val pointerIcon = Attr("pointerIcon", "reference|enum", mutableListOf("none" to 0L, "arrow" to 1000L, "context_menu" to 1001L, "hand" to 1002L, "help" to 1003L, "wait" to 1004L, "cell" to 1006L, "crosshair" to 1007L, "text" to 1008L, "vertical_text" to 1009L, "alias" to 1010L, "copy" to 1011L, "no_drop" to 1012L, "all_scroll" to 1013L, "horizontal_double_arrow" to 1014L, "vertical_double_arrow" to 1015L, "top_right_diagonal_double_arrow" to 1016L, "top_left_diagonal_double_arrow" to 1017L, "zoom_in" to 1018L, "zoom_out" to 1019L, "grab" to 1020L, "grabbing" to 1021L))
        val forceHasOverlappingRendering = Attr("forceHasOverlappingRendering", "boolean")
        val tooltipText = Attr("tooltipText", "string")
        val keyboardNavigationCluster = Attr("keyboardNavigationCluster", "boolean")
        val __removed0 = Attr("__removed0", "boolean")
        val nextClusterForward = Attr("nextClusterForward", "reference")
        val __removed1 = Attr("__removed1", "reference")
        val focusedByDefault = Attr("focusedByDefault", "boolean")
        val defaultFocusHighlightEnabled = Attr("defaultFocusHighlightEnabled", "boolean")
        val screenReaderFocusable = Attr("screenReaderFocusable", "boolean")
        val accessibilityPaneTitle = Attr("accessibilityPaneTitle", "string")
        val accessibilityHeading = Attr("accessibilityHeading", "boolean")
        val outlineSpotShadowColor = Attr("outlineSpotShadowColor", "color")
        val outlineAmbientShadowColor = Attr("outlineAmbientShadowColor", "color")
        val forceDarkAllowed = Attr("forceDarkAllowed", "boolean")
    }

    object ViewTag {
        val id = View.id
        val value = Attr("value")
    }

    object Include {
        val id = View.id
        val visibility = View.visibility
    }

    object ViewGroup {
        val animateLayoutChanges = Attr("animateLayoutChanges", "boolean")
        val clipChildren = Attr("clipChildren", "boolean")
        val clipToPadding = Attr("clipToPadding", "boolean")
        val layoutAnimation = Attr("layoutAnimation", "reference")
        val animationCache = Attr("animationCache", "boolean")
        val persistentDrawingCache = Attr("persistentDrawingCache", "flag", mutableListOf("none" to 0x0L, "" to 0x1L, "scrolling" to 0x2L, "all" to 0x3L))
        val alwaysDrawnWithCache = Attr("alwaysDrawnWithCache", "boolean")
        val addStatesFromChildren = Attr("addStatesFromChildren", "boolean")
        val descendantFocusability = Attr("descendantFocusability", "enum", mutableListOf("beforeDescendants" to 0L, "afterDescendants" to 1L, "blocksDescendants" to 2L))
        val touchscreenBlocksFocus = Attr("touchscreenBlocksFocus", "boolean")
        val splitMotionEvents = Attr("splitMotionEvents", "boolean")
        val layoutMode = Attr("layoutMode", "enum", mutableListOf("clipBounds" to 0L, "opticalBounds" to 1L))
        val transitionGroup = Attr("transitionGroup", "boolean")
    }

    object ViewStub {
        val layout = Attr("layout", "reference")
        val inflatedId = Attr("inflatedId", "reference")
    }

    object ViewGroup_Layout {
        val layout_width = Attr("layout_width", "dimension", mutableListOf("fill_parent" to -1L, "match_parent" to -1L, "wrap_content" to -2L))
        val layout_height = Attr("layout_height", "dimension", mutableListOf("fill_parent" to -1L, "match_parent" to -1L, "wrap_content" to -2L))
    }

    object ViewGroup_MarginLayout {
        val layout_width = ViewGroup_Layout.layout_width
        val layout_height = ViewGroup_Layout.layout_height
        val layout_margin = Attr("layout_margin", "dimension")
        val layout_marginLeft = Attr("layout_marginLeft", "dimension")
        val layout_marginTop = Attr("layout_marginTop", "dimension")
        val layout_marginRight = Attr("layout_marginRight", "dimension")
        val layout_marginBottom = Attr("layout_marginBottom", "dimension")
        val layout_marginStart = Attr("layout_marginStart", "dimension")
        val layout_marginEnd = Attr("layout_marginEnd", "dimension")
        val layout_marginHorizontal = Attr("layout_marginHorizontal", "dimension")
        val layout_marginVertical = Attr("layout_marginVertical", "dimension")
    }

    object InputMethod {
        val settingsActivity = Attr("settingsActivity", "string")
        val isDefault = Attr("isDefault", "boolean")
        val supportsSwitchingToNextInputMethod = Attr("supportsSwitchingToNextInputMethod", "boolean")
        val isVrOnly = Attr("isVrOnly", "boolean")
        val __removed2 = Attr("__removed2", "boolean")
    }

    object InputMethod_Subtype {
        val label = Attr("label")
        val icon = Attr("icon")
        val imeSubtypeLocale = Attr("imeSubtypeLocale", "string")
        val imeSubtypeMode = Attr("imeSubtypeMode", "string")
        val isAuxiliary = Attr("isAuxiliary", "boolean")
        val overridesImplicitlyEnabledSubtype = Attr("overridesImplicitlyEnabledSubtype", "boolean")
        val imeSubtypeExtraValue = Attr("imeSubtypeExtraValue", "string")
        val subtypeId = Attr("subtypeId", "integer")
        val isAsciiCapable = Attr("isAsciiCapable", "boolean")
        val languageTag = Attr("languageTag", "string")
    }

    object SpellChecker {
        val label = Attr("label")
        val settingsActivity = Attr("settingsActivity")
    }

    object SpellChecker_Subtype {
        val label = Attr("label")
        val subtypeLocale = Attr("subtypeLocale", "string")
        val subtypeExtraValue = Attr("subtypeExtraValue", "string")
        val subtypeId = Attr("subtypeId")
        val languageTag = Attr("languageTag")
    }

    object AccessibilityService {
        val accessibilityEventTypes = Attr("accessibilityEventTypes", "flag", mutableListOf("typeViewClicked" to 0x00000001L, "typeViewLongClicked" to 0x00000002L, "typeViewSelected" to 0x00000004L, "typeViewFocused" to 0x00000008L, "typeViewTextChanged" to 0x00000010L, "typeWindowStateChanged" to 0x00000020L, "typeNotificationStateChanged" to 0x00000040L, "typeViewHoverEnter" to 0x00000080L, "typeViewHoverExit" to 0x00000100L, "typeTouchExplorationGestureStart" to 0x00000200L, "typeTouchExplorationGestureEnd" to 0x00000400L, "typeWindowContentChanged" to 0x00000800L, "typeViewScrolled" to 0x000001000L, "typeViewTextSelectionChanged" to 0x000002000L, "typeAnnouncement" to 0x00004000L, "typeViewAccessibilityFocused" to 0x00008000L, "typeViewAccessibilityFocusCleared" to 0x00010000L, "typeViewTextTraversedAtMovementGranularity" to 0x00020000L, "typeGestureDetectionStart" to 0x00040000L, "typeGestureDetectionEnd" to 0x00080000L, "typeTouchInteractionStart" to 0x00100000L, "typeTouchInteractionEnd" to 0x00200000L, "typeWindowsChanged" to 0x00400000L, "typeContextClicked" to 0x00800000L, "typeAssistReadingContext" to 0x01000000L, "typeAllMask" to 0xffffffff))
        val packageNames = Attr("packageNames", "string")
        val accessibilityFeedbackType = Attr("accessibilityFeedbackType", "flag", mutableListOf("feedbackSpoken" to 0x00000001L, "feedbackHaptic" to 0x00000002L, "feedbackAudible" to 0x00000004L, "feedbackVisual" to 0x00000008L, "feedbackGeneric" to 0x00000010L, "feedbackAllMask" to 0xffffffff))
        val notificationTimeout = Attr("notificationTimeout", "integer")
        val nonInteractiveUiTimeout = Attr("nonInteractiveUiTimeout", "integer")
        val interactiveUiTimeout = Attr("interactiveUiTimeout", "integer")
        val accessibilityFlags = Attr("accessibilityFlags", "flag", mutableListOf("flagDefault" to 0x00000001L, "flagIncludeNotImportantViews" to 0x00000002L, "flagRequestTouchExplorationMode" to 0x00000004L, "flagRequestEnhancedWebAccessibility" to 0x00000008L, "flagReportViewIds" to 0x00000010L, "flagRequestFilterKeyEvents" to 0x00000020L, "flagRetrieveInteractiveWindows" to 0x00000040L, "flagEnableAccessibilityVolume" to 0x00000080L, "flagRequestAccessibilityButton" to 0x00000100L, "flagRequestFingerprintGestures" to 0x00000200L, "flagRequestShortcutWarningDialogSpokenFeedback" to 0x00000400L))
        val settingsActivity = Attr("settingsActivity")
        val canRetrieveWindowContent = Attr("canRetrieveWindowContent", "boolean")
        val canRequestTouchExplorationMode = Attr("canRequestTouchExplorationMode", "boolean")
        val canRequestEnhancedWebAccessibility = Attr("canRequestEnhancedWebAccessibility", "boolean")
        val canRequestFilterKeyEvents = Attr("canRequestFilterKeyEvents", "boolean")
        val canControlMagnification = Attr("canControlMagnification", "boolean")
        val canPerformGestures = Attr("canPerformGestures", "boolean")
        val canRequestFingerprintGestures = Attr("canRequestFingerprintGestures", "boolean")
        val description = Attr("description")
        val summary = Attr("summary")
    }

    object PrintService {
        val settingsActivity = Attr("settingsActivity")
        val addPrintersActivity = Attr("addPrintersActivity", "string")
        val advancedPrintOptionsActivity = Attr("advancedPrintOptionsActivity", "string")
        val vendor = Attr("vendor", "string")
    }

    object HostApduService {
        val description = Attr("description")
        val requireDeviceUnlock = Attr("requireDeviceUnlock", "boolean")
        val apduServiceBanner = Attr("apduServiceBanner", "reference")
        val settingsActivity = Attr("settingsActivity")
    }

    object OffHostApduService {
        val description = Attr("description")
        val apduServiceBanner = Attr("apduServiceBanner")
        val settingsActivity = Attr("settingsActivity")
        val secureElementName = Attr("secureElementName", "string")
    }

    object AidGroup {
        val description = Attr("description")
        val category = Attr("category", "string")
    }

    object AidFilter {
        val name = Attr("name")
    }

    object AidPrefixFilter {
        val name = Attr("name")
    }

    object HostNfcFService {
        val description = Attr("description")
    }

    object SystemCodeFilter {
        val name = Attr("name")
    }

    object Nfcid2Filter {
        val name = Attr("name")
    }

    object T3tPmmFilter {
        val name = Attr("name")
    }

    object ActionMenuItemView {
        val minWidth = TextView.minWidth
    }

    object AbsListView {
        val listSelector = Attr("listSelector", "color|reference")
        val drawSelectorOnTop = Attr("drawSelectorOnTop", "boolean")
        val stackFromBottom = Attr("stackFromBottom", "boolean")
        val scrollingCache = Attr("scrollingCache", "boolean")
        val textFilterEnabled = Attr("textFilterEnabled", "boolean")
        val transcriptMode = Attr("transcriptMode", "enum", mutableListOf("disabled" to 0L, "normal" to 1L, "alwaysScroll" to 2L))
        val cacheColorHint = Attr("cacheColorHint", "color")
        val fastScrollEnabled = Attr("fastScrollEnabled", "boolean")
        val fastScrollStyle = Attr("fastScrollStyle", "reference")
        val smoothScrollbar = Attr("smoothScrollbar", "boolean")
        val choiceMode = Attr("choiceMode", "enum", mutableListOf("none" to 0L, "singleChoice" to 1L, "multipleChoice" to 2L, "multipleChoiceModal" to 3L))
        val fastScrollAlwaysVisible = Attr("fastScrollAlwaysVisible", "boolean")
    }

    object RecycleListView {
        val paddingBottomNoButtons = Attr("paddingBottomNoButtons", "dimension")
        val paddingTopNoTitle = Attr("paddingTopNoTitle", "dimension")
    }

    object AbsSpinner {
        val entries = Resources.entries
    }

    object AnalogClock {
        val dial = Attr("dial", "reference")
        val hand_hour = Attr("hand_hour", "reference")
        val hand_minute = Attr("hand_minute", "reference")
    }

    object Chronometer {
        val format = Attr("format", "string")
        val countDown = Attr("countDown", "boolean")
    }

    object CompoundButton {
        val checked = Attr("checked", "boolean")
        val button = Attr("button", "reference|color")
        val buttonTint = Attr("buttonTint", "color|reference")
        val buttonTintMode = Attr("buttonTintMode", "enum", mutableListOf("src_over" to 3L, "src_in" to 5L, "src_atop" to 9L, "multiply" to 14L, "screen" to 15L, "add" to 16L))
    }

    object CheckedTextView {
        val checkMark = Attr("checkMark", "reference")
        val checkMarkTint = Attr("checkMarkTint", "color")
        val checkMarkTintMode = Attr("checkMarkTintMode", "enum", mutableListOf("src_over" to 3L, "src_in" to 5L, "src_atop" to 9L, "multiply" to 14L, "screen" to 15L, "add" to 16L))
        val checkMarkGravity = Attr("checkMarkGravity", "flag", mutableListOf("left" to 0x03L, "right" to 0x05L, "start" to 0x00800003L, "end" to 0x00800005L))
    }

    object FastScroll {
        val thumbDrawable = Attr("thumbDrawable", "reference")
        val thumbMinWidth = Attr("thumbMinWidth", "dimension")
        val thumbMinHeight = Attr("thumbMinHeight", "dimension")
        val trackDrawable = Attr("trackDrawable", "reference")
        val backgroundRight = Attr("backgroundRight", "reference")
        val backgroundLeft = Attr("backgroundLeft", "reference")
        val position = Attr("position", "enum", mutableListOf("floating" to 0L, "atThumb" to 1L, "aboveThumb" to 2L))
        val textAppearance = TextView.textAppearance
        val textColor = TextView.textColor
        val textSize = TextView.textSize
        val minWidth = TextView.minWidth
        val minHeight = TextView.minHeight
        val padding = View.padding
        val thumbPosition = Attr("thumbPosition", "enum", mutableListOf("midpoint" to 0L, "inside" to 1L))
    }

    object FrameLayout {
        val measureAllChildren = Attr("measureAllChildren", "boolean")
    }

    object ExpandableListView {
        val groupIndicator = Attr("groupIndicator", "color|drawable")
        val childIndicator = Attr("childIndicator", "color|drawable")
        val indicatorLeft = Attr("indicatorLeft", "dimension")
        val indicatorRight = Attr("indicatorRight", "dimension")
        val childIndicatorLeft = Attr("childIndicatorLeft", "dimension")
        val childIndicatorRight = Attr("childIndicatorRight", "dimension")
        val childDivider = Attr("childDivider", "reference|color")
        val indicatorStart = Attr("indicatorStart", "dimension")
        val indicatorEnd = Attr("indicatorEnd", "dimension")
        val childIndicatorStart = Attr("childIndicatorStart", "dimension")
        val childIndicatorEnd = Attr("childIndicatorEnd", "dimension")
    }

    object Gallery {
        val gravity = Resources.gravity
        val animationDuration = Attr("animationDuration", "integer")
        val spacing = Attr("spacing", "dimension")
        val unselectedAlpha = Attr("unselectedAlpha", "float")
    }

    object GridView {
        val horizontalSpacing = Attr("horizontalSpacing", "dimension")
        val verticalSpacing = Attr("verticalSpacing", "dimension")
        val stretchMode = Attr("stretchMode", "enum", mutableListOf("none" to 0L, "spacingWidth" to 1L, "columnWidth" to 2L, "spacingWidthUniform" to 3L))
        val columnWidth = Attr("columnWidth", "dimension")
        val numColumns = Attr("numColumns", "integer", mutableListOf("auto_fit" to -1L))
        val gravity = Resources.gravity
    }

    object ImageView {
        val src = Attr("src", "reference|color")
        val scaleType = Attr("scaleType", "enum", mutableListOf("matrix" to 0L, "fitXY" to 1L, "fitStart" to 2L, "fitCenter" to 3L, "fitEnd" to 4L, "center" to 5L, "centerCrop" to 6L, "centerInside" to 7L))
        val adjustViewBounds = Attr("adjustViewBounds", "boolean")
        val maxWidth = Attr("maxWidth", "dimension")
        val maxHeight = Attr("maxHeight", "dimension")
        val tint = Attr("tint", "color|reference")
        val baselineAlignBottom = Attr("baselineAlignBottom", "boolean")
        val cropToPadding = Attr("cropToPadding", "boolean")
        val baseline = Attr("baseline", "dimension")
        val drawableAlpha = Attr("drawableAlpha", "integer")
        val tintMode = BitmapDrawable.tintMode
    }

    object ToggleButton {
        val textOn = Attr("textOn", "string")
        val textOff = Attr("textOff", "string")
        val disabledAlpha = Theme.disabledAlpha
    }

    object RelativeLayout {
        val gravity = Resources.gravity
        val ignoreGravity = Attr("ignoreGravity", "reference")
    }

    object LinearLayout {
        val orientation = Resources.orientation
        val gravity = Resources.gravity
        val baselineAligned = Attr("baselineAligned", "boolean")
        val baselineAlignedChildIndex = Attr("baselineAlignedChildIndex", "integer")
        val weightSum = Attr("weightSum", "float")
        val measureWithLargestChild = Attr("measureWithLargestChild", "boolean")
        val divider = Attr("divider", "color|drawable")
        val showDividers = Attr("showDividers", "flag", mutableListOf("none" to 0L, "beginning" to 1L, "middle" to 2L, "end" to 4L))
        val dividerPadding = Attr("dividerPadding", "dimension")
    }

    object GridLayout {
        val orientation = Resources.orientation
        val rowCount = Attr("rowCount", "integer")
        val columnCount = Attr("columnCount", "integer")
        val useDefaultMargins = Attr("useDefaultMargins", "boolean")
        val alignmentMode = Resources.alignmentMode
        val rowOrderPreserved = Attr("rowOrderPreserved", "boolean")
        val columnOrderPreserved = Attr("columnOrderPreserved", "boolean")
    }

    object ListView {
        val entries = Resources.entries
        val divider = Attr("divider", "reference|color")
        val dividerHeight = Attr("dividerHeight", "dimension")
        val headerDividersEnabled = Attr("headerDividersEnabled", "boolean")
        val footerDividersEnabled = Attr("footerDividersEnabled", "boolean")
        val overScrollHeader = Attr("overScrollHeader", "reference|color")
        val overScrollFooter = Attr("overScrollFooter", "reference|color")
    }

    object PreferenceFrameLayout {
        val borderTop = Attr("borderTop", "dimension")
        val borderBottom = Attr("borderBottom", "dimension")
        val borderLeft = Attr("borderLeft", "dimension")
        val borderRight = Attr("borderRight", "dimension")
    }

    object PreferenceFrameLayout_Layout {
        val layout_removeBorders = Attr("layout_removeBorders", "boolean")
    }

    object MenuView {
        val itemTextAppearance = Attr("itemTextAppearance", "reference")
        val horizontalDivider = Attr("horizontalDivider", "reference")
        val verticalDivider = Attr("verticalDivider", "reference")
        val headerBackground = Attr("headerBackground", "color|reference")
        val itemBackground = Attr("itemBackground", "color|reference")
        val windowAnimationStyle = Window.windowAnimationStyle
        val itemIconDisabledAlpha = Attr("itemIconDisabledAlpha", "float")
        val preserveIconSpacing = Attr("preserveIconSpacing", "boolean")
        val subMenuArrow = Attr("subMenuArrow", "reference")
    }

    object IconMenuView {
        val rowHeight = Attr("rowHeight", "dimension")
        val maxRows = Attr("maxRows", "integer")
        val maxItemsPerRow = Attr("maxItemsPerRow", "integer")
        val maxItems = Attr("maxItems", "integer")
        val moreIcon = Attr("moreIcon", "reference")
    }

    object ProgressBar {
        val min = Attr("min", "integer")
        val max = Attr("max", "integer")
        val progress = Attr("progress", "integer")
        val secondaryProgress = Attr("secondaryProgress", "integer")
        val indeterminate = Attr("indeterminate", "boolean")
        val indeterminateOnly = Attr("indeterminateOnly", "boolean")
        val indeterminateDrawable = Attr("indeterminateDrawable", "reference")
        val progressDrawable = Attr("progressDrawable", "reference")
        val indeterminateDuration = Attr("indeterminateDuration", "integer")
        val indeterminateBehavior = Attr("indeterminateBehavior", "enum", mutableListOf("repeat" to 1L, "cycle" to 2L))
        val minWidth = Attr("minWidth", "dimension")
        val maxWidth = Attr("maxWidth", "dimension")
        val minHeight = Attr("minHeight", "dimension")
        val maxHeight = Attr("maxHeight", "dimension")
        val interpolator = Attr("interpolator", "reference")
        val animationResolution = Attr("animationResolution", "integer")
        val mirrorForRtl = Attr("mirrorForRtl", "boolean")
        val progressTint = Attr("progressTint", "color|reference")
        val progressTintMode = Attr("progressTintMode", "enum", mutableListOf("src_over" to 3L, "src_in" to 5L, "src_atop" to 9L, "multiply" to 14L, "screen" to 15L, "add" to 16L))
        val progressBackgroundTint = Attr("progressBackgroundTint", "color|reference")
        val progressBackgroundTintMode = Attr("progressBackgroundTintMode", "enum", mutableListOf("src_over" to 3L, "src_in" to 5L, "src_atop" to 9L, "multiply" to 14L, "screen" to 15L, "add" to 16L))
        val secondaryProgressTint = Attr("secondaryProgressTint", "color|reference")
        val secondaryProgressTintMode = Attr("secondaryProgressTintMode", "enum", mutableListOf("src_over" to 3L, "src_in" to 5L, "src_atop" to 9L, "multiply" to 14L, "screen" to 15L, "add" to 16L))
        val indeterminateTint = Attr("indeterminateTint", "color|reference")
        val indeterminateTintMode = Attr("indeterminateTintMode", "enum", mutableListOf("src_over" to 3L, "src_in" to 5L, "src_atop" to 9L, "multiply" to 14L, "screen" to 15L, "add" to 16L))
    }

    object SeekBar {
        val thumb = Attr("thumb", "reference")
        val thumbOffset = Attr("thumbOffset", "dimension")
        val splitTrack = Attr("splitTrack", "boolean")
        val useDisabledAlpha = Attr("useDisabledAlpha", "boolean")
        val thumbTint = Attr("thumbTint", "color|reference")
        val thumbTintMode = Attr("thumbTintMode", "enum", mutableListOf("src_over" to 3L, "src_in" to 5L, "src_atop" to 9L, "multiply" to 14L, "screen" to 15L, "add" to 16L))
        val tickMark = Attr("tickMark", "reference")
        val tickMarkTint = Attr("tickMarkTint", "color|reference")
        val tickMarkTintMode = Attr("tickMarkTintMode", "enum", mutableListOf("src_over" to 3L, "src_in" to 5L, "src_atop" to 9L, "multiply" to 14L, "screen" to 15L, "add" to 16L))
    }

    object StackView {
        val resOutColor = Attr("resOutColor", "color")
        val clickColor = Attr("clickColor", "color")
    }

    object RatingBar {
        val numStars = Attr("numStars", "integer")
        val rating = Attr("rating", "float")
        val stepSize = Attr("stepSize", "float")
        val isIndicator = Attr("isIndicator", "boolean")
    }

    object RadioGroup {
        val checkedButton = Attr("checkedButton", "reference")
        val orientation = Resources.orientation
    }

    object TableLayout {
        val stretchColumns = Attr("stretchColumns", "string")
        val shrinkColumns = Attr("shrinkColumns", "string")
        val collapseColumns = Attr("collapseColumns", "string")
    }

    object TableRow

    object TableRow_Cell {
        val layout_column = Attr("layout_column", "integer")
        val layout_span = Attr("layout_span", "integer")
    }

    object TabWidget {
        val divider = Attr("divider")
        val tabStripEnabled = Attr("tabStripEnabled", "boolean")
        val tabStripLeft = Attr("tabStripLeft", "reference")
        val tabStripRight = Attr("tabStripRight", "reference")
        val tabLayout = Attr("tabLayout", "reference")
    }

    object TextAppearance {
        val textColor = Attr("textColor")
        val textSize = Attr("textSize")
        val textStyle = Attr("textStyle")
        val textFontWeight = Attr("textFontWeight")
        val typeface = Attr("typeface")
        val fontFamily = Attr("fontFamily")
        val textLocale = Attr("textLocale", "string")
        val textColorHighlight = Attr("textColorHighlight")
        val textColorHint = Attr("textColorHint")
        val textColorLink = Attr("textColorLink")
        val textAllCaps = Attr("textAllCaps", "boolean")
        val shadowColor = Attr("shadowColor", "color")
        val shadowDx = Attr("shadowDx", "float")
        val shadowDy = Attr("shadowDy", "float")
        val shadowRadius = Attr("shadowRadius", "float")
        val elegantTextHeight = Attr("elegantTextHeight", "boolean")
        val fallbackLineSpacing = Attr("fallbackLineSpacing", "boolean")
        val letterSpacing = Attr("letterSpacing", "float")
        val fontFeatureSettings = Attr("fontFeatureSettings", "string")
        val fontVariationSettings = Attr("fontVariationSettings", "string")
    }

    object TextClock {
        val format12Hour = Attr("format12Hour", "string")
        val format24Hour = Attr("format24Hour", "string")
        val timeZone = Attr("timeZone", "string")
    }

    object TextView {
        val bufferType = Attr("bufferType", "enum", mutableListOf("normal" to 0L, "spannable" to 1L, "editable" to 2L))
        val text = Attr("text", "string")
        val hint = Attr("hint", "string")
        val textColor = Attr("textColor", "color|reference")
        val textColorHighlight = Attr("textColorHighlight", "color")
        val textColorHint = Attr("textColorHint", "color|reference")
        val textAppearance = Attr("textAppearance", "reference")
        val textSize = Attr("textSize", "dimension|integer")  // int就默认是 sp
        val textScaleX = Attr("textScaleX", "float")
        val typeface = Attr("typeface")
        val textStyle = Attr("textStyle")
        val textFontWeight = Attr("textFontWeight", "integer")
        val fontFamily = Attr("fontFamily")
        val textLocale = Attr("textLocale", "string")
        val textColorLink = Attr("textColorLink", "color|reference")
        val cursorVisible = Attr("cursorVisible", "boolean")
        val maxLines = Attr("maxLines", "integer")
        val maxHeight = Attr("maxHeight", "dimen")
        val lines = Attr("lines", "integer")
        val height = Attr("height", "dimension")
        val minLines = Attr("minLines", "integer")
        val minHeight = Attr("minHeight", "dimen")
        val maxEms = Attr("maxEms", "integer")
        val maxWidth = Attr("maxWidth", "dimen")
        val ems = Attr("ems", "integer")
        val width = Attr("width", "dimension")
        val minEms = Attr("minEms", "integer")
        val minWidth = Attr("minWidth", "dimension")
        val gravity = Resources.gravity
        val scrollHorizontally = Attr("scrollHorizontally", "boolean")
        val password = Attr("password", "boolean")
        val singleLine = Attr("singleLine", "boolean")
        val enabled = Attr("enabled", "boolean")
        val selectAllOnFocus = Attr("selectAllOnFocus", "boolean")
        val includeFontPadding = Attr("includeFontPadding", "boolean")
        val maxLength = Attr("maxLength", "integer")
        val shadowColor = TextAppearance.shadowColor
        val shadowDx = TextAppearance.shadowDx
        val shadowDy = TextAppearance.shadowDy
        val shadowRadius = TextAppearance.shadowRadius
        val autoLink = Resources.autoLink
        val linksClickable = Attr("linksClickable", "boolean")
        val numeric = Attr("numeric", "flag", mutableListOf("integer" to 0x01L, "signed" to 0x03L, "decimal" to 0x05L))
        val digits = Attr("digits", "string")
        val phoneNumber = Attr("phoneNumber", "boolean")
        val inputMethod = Attr("inputMethod", "string")
        val capitalize = Attr("capitalize", "enum", mutableListOf("none" to 0L, "sentences" to 1L, "words" to 2L, "characters" to 3L))
        val autoText = Attr("autoText", "boolean")
        val editable = Attr("editable", "boolean")
        val freezesText = Attr("freezesText", "boolean")
        val ellipsize = Resources.ellipsize
        val drawableTop = Attr("drawableTop", "reference|color")
        val drawableBottom = Attr("drawableBottom", "reference|color")
        val drawableLeft = Attr("drawableLeft", "reference|color")
        val drawableRight = Attr("drawableRight", "reference|color")
        val drawableStart = Attr("drawableStart", "reference|color")
        val drawableEnd = Attr("drawableEnd", "reference|color")
        val drawablePadding = Attr("drawablePadding", "dimension")
        val drawableTint = Attr("drawableTint", "color|reference")
        val drawableTintMode = Attr("drawableTintMode", "enum", mutableListOf("src_over" to 3L, "src_in" to 5L, "src_atop" to 9L, "multiply" to 14L, "screen" to 15L, "add" to 16L))
        val lineSpacingExtra = Attr("lineSpacingExtra", "dimension")
        val lineSpacingMultiplier = Attr("lineSpacingMultiplier", "float")
        val lineHeight = Attr("lineHeight", "dimension")
        val firstBaselineToTopHeight = Attr("firstBaselineToTopHeight", "dimension")
        val lastBaselineToBottomHeight = Attr("lastBaselineToBottomHeight", "dimension")
        val marqueeRepeatLimit = Attr("marqueeRepeatLimit", "integer", mutableListOf("marquee_forever" to -1L))
        val inputType = Resources.inputType
        val allowUndo = Attr("allowUndo", "boolean")
        val imeOptions = Resources.imeOptions
        val privateImeOptions = Attr("privateImeOptions", "string")
        val imeActionLabel = Attr("imeActionLabel", "string")
        val imeActionId = Attr("imeActionId", "integer")
        val editorExtras = Attr("editorExtras", "reference")
        val textSelectHandleLeft = Attr("textSelectHandleLeft", "reference")
        val textSelectHandleRight = Attr("textSelectHandleRight", "reference")
        val textSelectHandle = Attr("textSelectHandle", "reference")
        val textEditPasteWindowLayout = Attr("textEditPasteWindowLayout")
        val textEditNoPasteWindowLayout = Attr("textEditNoPasteWindowLayout")
        val textEditSidePasteWindowLayout = Attr("textEditSidePasteWindowLayout")
        val textEditSideNoPasteWindowLayout = Attr("textEditSideNoPasteWindowLayout")
        val textEditSuggestionItemLayout = Attr("textEditSuggestionItemLayout", "reference")
        val textEditSuggestionContainerLayout = Attr("textEditSuggestionContainerLayout", "reference")
        val textEditSuggestionHighlightStyle = Attr("textEditSuggestionHighlightStyle", "reference")
        val textCursorDrawable = Attr("textCursorDrawable", "color|drawable")
        val textIsSelectable = Attr("textIsSelectable", "boolean")
        val textAllCaps = Attr("textAllCaps", "boolean")
        val elegantTextHeight = Attr("elegantTextHeight", "boolean")
        val fallbackLineSpacing = Attr("fallbackLineSpacing", "boolean")
        val letterSpacing = Attr("letterSpacing", "dimension")
        val fontFeatureSettings = Attr("fontFeatureSettings", "string")
        val fontVariationSettings = Attr("fontVariationSettings", "string")
        val breakStrategy = Attr("breakStrategy", "enum", mutableListOf("simple" to 0L, "high_quality" to 1L, "balanced" to 2L))
        val hyphenationFrequency = Attr("hyphenationFrequency", "enum", mutableListOf("none" to 0L, "normal" to 1L, "full" to 2L))
        val autoSizeTextType = Attr("autoSizeTextType", "enum", mutableListOf("none" to 0L, "uniform" to 1L))
        val autoSizeStepGranularity = Attr("autoSizeStepGranularity", "dimension")
        val autoSizePresetSizes = Attr("autoSizePresetSizes", "reference")
        val autoSizeMinTextSize = Attr("autoSizeMinTextSize", "dimension")
        val autoSizeMaxTextSize = Attr("autoSizeMaxTextSize", "dimension")
        val justificationMode = Attr("justificationMode", "enum", mutableListOf("none" to 0L, "inter_word" to 1L))
    }

    object TextViewAppearance {
        val textAppearance = TextView.textAppearance
    }

    object SelectionModeDrawables {
        val actionModeSelectAllDrawable = Attr("actionModeSelectAllDrawable")
        val actionModeCutDrawable = Attr("actionModeCutDrawable")
        val actionModeCopyDrawable = Attr("actionModeCopyDrawable")
        val actionModePasteDrawable = Attr("actionModePasteDrawable")
    }

    object SuggestionSpan {
        val textUnderlineColor = Attr("textUnderlineColor")
        val textUnderlineThickness = Attr("textUnderlineThickness")
    }

    object InputExtras

    object AutoCompleteTextView {
        val completionHint = Attr("completionHint", "string")
        val completionHintView = Attr("completionHintView", "reference")
        val completionThreshold = Attr("completionThreshold", "integer")
        val dropDownSelector = Attr("dropDownSelector", "reference|color")
        val dropDownAnchor = Attr("dropDownAnchor", "reference")
        val dropDownWidth = Attr("dropDownWidth", "dimension", mutableListOf("fill_parent" to -1L, "match_parent" to -1L, "wrap_content" to -2L))
        val dropDownHeight = Attr("dropDownHeight", "dimension", mutableListOf("fill_parent" to -1L, "match_parent" to -1L, "wrap_content" to -2L))
        val inputType = Resources.inputType
        val popupTheme = Attr("popupTheme")
    }

    object PopupWindow {
        val popupBackground = Attr("popupBackground", "reference|color")
        val popupElevation = Attr("popupElevation", "dimension")
        val popupAnimationStyle = Attr("popupAnimationStyle", "reference")
        val overlapAnchor = Attr("overlapAnchor", "boolean")
        val popupEnterTransition = Attr("popupEnterTransition", "reference")
        val popupExitTransition = Attr("popupExitTransition", "reference")
    }

    object ListPopupWindow {
        val dropDownVerticalOffset = Attr("dropDownVerticalOffset", "dimension")
        val dropDownHorizontalOffset = Attr("dropDownHorizontalOffset", "dimension")
    }

    object ViewAnimator {
        val inAnimation = Attr("inAnimation", "reference")
        val outAnimation = Attr("outAnimation", "reference")
        val animateFirstView = Attr("animateFirstView", "boolean")
    }

    object ViewFlipper {
        val flipInterval = Attr("flipInterval", "integer")
        val autoStart = Attr("autoStart", "boolean")
    }

    object AdapterViewAnimator {
        val inAnimation = Attr("inAnimation")
        val outAnimation = Attr("outAnimation")
        val loopViews = Attr("loopViews", "boolean")
        val animateFirstView = Attr("animateFirstView")
    }

    object AdapterViewFlipper {
        val flipInterval = Attr("flipInterval")
        val autoStart = Attr("autoStart")
    }

    object ScrollView {
        val fillViewport = Attr("fillViewport", "boolean")
    }

    object HorizontalScrollView {
        val fillViewport = Attr("fillViewport")
    }

    object Spinner {
        val prompt = Attr("prompt", "reference")
        val spinnerMode = Attr("spinnerMode", "enum", mutableListOf("dialog" to 0L, "dropdown" to 1L))
        val dropDownSelector = Attr("dropDownSelector")
        val popupTheme = Attr("popupTheme")
        val popupBackground = Attr("popupBackground")
        val popupElevation = Attr("popupElevation")
        val dropDownWidth = Attr("dropDownWidth")
        val popupPromptView = Attr("popupPromptView", "reference")
        val gravity = Resources.gravity
        val disableChildrenWhenDisabled = Attr("disableChildrenWhenDisabled", "boolean")
    }

    object DatePicker {
        val firstDayOfWeek = Attr("firstDayOfWeek", "integer")
        val minDate = Attr("minDate", "string")
        val maxDate = Attr("maxDate", "string")
        val spinnersShown = Attr("spinnersShown", "boolean")
        val calendarViewShown = Attr("calendarViewShown", "boolean")
        val internalLayout = Attr("internalLayout", "reference")
        val legacyLayout = Attr("legacyLayout", "reference")
        val headerTextColor = Attr("headerTextColor", "color|reference")
        val headerBackground = Attr("headerBackground", "color|drawable")
        val yearListItemTextAppearance = Attr("yearListItemTextAppearance", "reference")
        val yearListItemActivatedTextAppearance = Attr("yearListItemActivatedTextAppearance", "reference")
        val calendarTextColor = Attr("calendarTextColor", "color")
        val datePickerMode = Attr("datePickerMode", "enum", mutableListOf("spinner" to 1L, "calendar" to 2L))
        val startYear = Attr("startYear", "integer")
        val endYear = Attr("endYear", "integer")
        val headerMonthTextAppearance = Attr("headerMonthTextAppearance", "reference")
        val headerDayOfMonthTextAppearance = Attr("headerDayOfMonthTextAppearance", "reference")
        val headerYearTextAppearance = Attr("headerYearTextAppearance", "reference")
        val dayOfWeekBackground = Attr("dayOfWeekBackground", "color")
        val dayOfWeekTextAppearance = Attr("dayOfWeekTextAppearance", "reference")
        val yearListSelectorColor = Attr("yearListSelectorColor", "color")
        val dialogMode = Attr("dialogMode", "boolean")
    }

    object TwoLineListItem {
        val mode = Attr("mode", "enum", mutableListOf("oneLine" to 1L, "collapsing" to 2L, "twoLine" to 3L))
    }

    object SlidingDrawer {
        val handle = Attr("handle", "reference")
        val content = Attr("content", "reference")
        val orientation = Resources.orientation
        val bottomOffset = Attr("bottomOffset", "dimension")
        val topOffset = Attr("topOffset", "dimension")
        val allowSingleTap = Attr("allowSingleTap", "boolean")
        val animateOnClick = Attr("animateOnClick", "boolean")
    }

    object GestureOverlayView {
        val gestureStrokeWidth = Attr("gestureStrokeWidth", "float")
        val gestureColor = Attr("gestureColor", "color")
        val uncertainGestureColor = Attr("uncertainGestureColor", "color")
        val fadeOffset = Attr("fadeOffset", "integer")
        val fadeDuration = Attr("fadeDuration", "integer")
        val gestureStrokeType = Attr("gestureStrokeType", "enum", mutableListOf("single" to 0L, "multiple" to 1L))
        val gestureStrokeLengthThreshold = Attr("gestureStrokeLengthThreshold", "float")
        val gestureStrokeSquarenessThreshold = Attr("gestureStrokeSquarenessThreshold", "float")
        val gestureStrokeAngleThreshold = Attr("gestureStrokeAngleThreshold", "float")
        val eventsInterceptionEnabled = Attr("eventsInterceptionEnabled", "boolean")
        val fadeEnabled = Attr("fadeEnabled", "boolean")
        val orientation = Resources.orientation
    }

    object QuickContactBadge {
        val quickContactWindowSize = Attr("quickContactWindowSize", "enum", mutableListOf("modeSmall" to 1L, "modeMedium" to 2L, "modeLarge" to 3L))
    }

    object AbsoluteLayout_Layout {
        val layout_x = Attr("layout_x", "dimension")
        val layout_y = Attr("layout_y", "dimension")
    }

    object LinearLayout_Layout {
        val layout_width = Attr("layout_width")
        val layout_height = Attr("layout_height")
        val layout_weight = Attr("layout_weight", "float")
        val layout_gravity = Attr("layout_gravity")
    }

    object GridLayout_Layout {
        val layout_row = Attr("layout_row", "integer")
        val layout_rowSpan = Attr("layout_rowSpan", "integer")
        val layout_rowWeight = Attr("layout_rowWeight", "float")
        val layout_column = TableRow_Cell.layout_column
        val layout_columnSpan = Attr("layout_columnSpan", "integer")
        val layout_columnWeight = Attr("layout_columnWeight", "float")
        val layout_gravity = Attr("layout_gravity")
    }

    object FrameLayout_Layout {
        val layout_gravity = Attr("layout_gravity")
    }

    object RelativeLayout_Layout {
        val layout_toLeftOf = Attr("layout_toLeftOf", "reference")
        val layout_toRightOf = Attr("layout_toRightOf", "reference")
        val layout_above = Attr("layout_above", "reference")
        val layout_below = Attr("layout_below", "reference")
        val layout_alignBaseline = Attr("layout_alignBaseline", "reference")
        val layout_alignLeft = Attr("layout_alignLeft", "reference")
        val layout_alignTop = Attr("layout_alignTop", "reference")
        val layout_alignRight = Attr("layout_alignRight", "reference")
        val layout_alignBottom = Attr("layout_alignBottom", "reference")
        val layout_alignParentLeft = Attr("layout_alignParentLeft", "boolean")
        val layout_alignParentTop = Attr("layout_alignParentTop", "boolean")
        val layout_alignParentRight = Attr("layout_alignParentRight", "boolean")
        val layout_alignParentBottom = Attr("layout_alignParentBottom", "boolean")
        val layout_centerInParent = Attr("layout_centerInParent", "boolean")
        val layout_centerHorizontal = Attr("layout_centerHorizontal", "boolean")
        val layout_centerVertical = Attr("layout_centerVertical", "boolean")
        val layout_alignWithParentIfMissing = Attr("layout_alignWithParentIfMissing", "boolean")
        val layout_toStartOf = Attr("layout_toStartOf", "reference")
        val layout_toEndOf = Attr("layout_toEndOf", "reference")
        val layout_alignStart = Attr("layout_alignStart", "reference")
        val layout_alignEnd = Attr("layout_alignEnd", "reference")
        val layout_alignParentStart = Attr("layout_alignParentStart", "boolean")
        val layout_alignParentEnd = Attr("layout_alignParentEnd", "boolean")
    }

    object VerticalSlider_Layout {
        val layout_scale = Attr("layout_scale", "float")
    }

    object WeightedLinearLayout {
        val majorWeightMin = Attr("majorWeightMin", "float")
        val minorWeightMin = Attr("minorWeightMin", "float")
        val majorWeightMax = Attr("majorWeightMax", "float")
        val minorWeightMax = Attr("minorWeightMax", "float")
    }

    object CalendarView {
        val firstDayOfWeek = Attr("firstDayOfWeek", "integer")
        val minDate = DatePicker.minDate
        val maxDate = DatePicker.maxDate
        val monthTextAppearance = Attr("monthTextAppearance", "reference")
        val weekDayTextAppearance = Attr("weekDayTextAppearance", "reference")
        val dateTextAppearance = Attr("dateTextAppearance", "reference")
        val daySelectorColor = Attr("daySelectorColor", "color|reference")
        val dayHighlightColor = Attr("dayHighlightColor", "color")
        val calendarViewMode = Attr("calendarViewMode", "enum", mutableListOf("holo" to 0L, "material" to 1L))
        val showWeekNumber = Attr("showWeekNumber", "boolean")
        val shownWeekCount = Attr("shownWeekCount", "integer")
        val selectedWeekBackgroundColor = Attr("selectedWeekBackgroundColor", "color")
        val focusedMonthDateColor = Attr("focusedMonthDateColor", "color")
        val unfocusedMonthDateColor = Attr("unfocusedMonthDateColor", "color")
        val weekNumberColor = Attr("weekNumberColor", "color")
        val weekSeparatorLineColor = Attr("weekSeparatorLineColor", "color")
        val selectedDateVerticalBar = Attr("selectedDateVerticalBar", "drawable")
    }

    object NumberPicker {
        val solidColor = Attr("solidColor", "color")
        val selectionDivider = Attr("selectionDivider", "reference")
        val selectionDividerHeight = Attr("selectionDividerHeight", "dimension")
        val selectionDividersDistance = Attr("selectionDividersDistance", "dimension")
        val internalMinHeight = Attr("internalMinHeight", "dimension")
        val internalMaxHeight = Attr("internalMaxHeight", "dimension")
        val internalMinWidth = Attr("internalMinWidth", "dimension")
        val internalMaxWidth = Attr("internalMaxWidth", "dimension")
        val internalLayout = Attr("internalLayout", "reference")
        val virtualButtonPressedDrawable = Attr("virtualButtonPressedDrawable", "reference")
        val hideWheelUntilFocused = Attr("hideWheelUntilFocused", "boolean")
    }

    object TimePicker {
        val legacyLayout = DatePicker.legacyLayout
        val internalLayout = DatePicker.internalLayout
        val headerTextColor = DatePicker.headerTextColor
        val headerBackground = DatePicker.headerBackground
        val numbersTextColor = Attr("numbersTextColor", "color|reference")
        val numbersInnerTextColor = Attr("numbersInnerTextColor", "color|reference")
        val numbersBackgroundColor = Attr("numbersBackgroundColor", "color")
        val numbersSelectorColor = Attr("numbersSelectorColor", "color|reference")
        val timePickerMode = Attr("timePickerMode", "enum", mutableListOf("spinner" to 1L, "clock" to 2L))
        val headerAmPmTextAppearance = Attr("headerAmPmTextAppearance", "reference")
        val headerTimeTextAppearance = Attr("headerTimeTextAppearance", "reference")
        val amPmTextColor = Attr("amPmTextColor", "color|reference")
        val amPmBackgroundColor = Attr("amPmBackgroundColor", "color|reference")
        val dialogMode = DatePicker.dialogMode
    }

    object Drawable {
        val visible = Attr("visible", "boolean")
        val autoMirrored = Attr("autoMirrored", "boolean")
    }

    object DrawableWrapper {
        val drawable = Attr("drawable", "reference")
    }

    object StateListDrawable {
        val visible = Attr("visible")
        val variablePadding = Attr("variablePadding", "boolean")
        val constantSize = Attr("constantSize", "boolean")
        val dither = Attr("dither", "boolean")
        val enterFadeDuration = Attr("enterFadeDuration", "integer")
        val exitFadeDuration = Attr("exitFadeDuration", "integer")
        val autoMirrored = Attr("autoMirrored")
    }

    object AnimatedStateListDrawable {
        val visible = Attr("visible")
        val variablePadding = Attr("variablePadding")
        val constantSize = Attr("constantSize")
        val dither = Attr("dither")
        val enterFadeDuration = Attr("enterFadeDuration")
        val exitFadeDuration = Attr("exitFadeDuration")
        val autoMirrored = Attr("autoMirrored")
    }

    object StateListDrawableItem {
        val drawable = DrawableWrapper.drawable
    }

    object AnimatedStateListDrawableItem {
        val drawable = DrawableWrapper.drawable
        val id = View.id
    }

    object AnimatedStateListDrawableTransition {
        val fromId = Attr("fromId", "reference")
        val toId = Attr("toId", "reference")
        val drawable = DrawableWrapper.drawable
        val reversible = Attr("reversible", "boolean")
    }

    object AnimationDrawable {
        val visible = Attr("visible")
        val variablePadding = Attr("variablePadding")
        val oneshot = Attr("oneshot", "boolean")
    }

    object AnimationDrawableItem {
        val duration = Attr("duration", "integer")
        val drawable = Attr("drawable", "reference")
    }

    object StateListAnimatorItem {
        val animation = Attr("animation")
    }

    object ColorStateListItem {
        val color = Attr("color")
        val alpha = Attr("alpha")
    }

    object AnimationScaleListDrawable

    object AnimationScaleListDrawableItem {
        val drawable = DrawableWrapper.drawable
    }

    object GradientDrawable {
        val visible = Attr("visible")
        val dither = Attr("dither")
        val shape = Attr("shape", "enum", mutableListOf("rectangle" to 0L, "oval" to 1L, "line" to 2L, "ring" to 3L))
        val innerRadiusRatio = Attr("innerRadiusRatio", "float")
        val thicknessRatio = Attr("thicknessRatio", "float")
        val innerRadius = Attr("innerRadius", "dimension")
        val thickness = Attr("thickness", "dimension")
        val useLevel = Attr("useLevel")
        val tint = Attr("tint")
        val tintMode = BitmapDrawable.tintMode
        val opticalInsetLeft = Attr("opticalInsetLeft")
        val opticalInsetTop = Attr("opticalInsetTop")
        val opticalInsetRight = Attr("opticalInsetRight")
        val opticalInsetBottom = Attr("opticalInsetBottom")
    }

    object GradientDrawableSize {
        val width = Attr("width")
        val height = Attr("height")
    }

    object GradientDrawableGradient {
        val startColor = Attr("startColor", "color")
        val centerColor = Attr("centerColor", "color")
        val endColor = Attr("endColor", "color")
        val useLevel = Attr("useLevel", "boolean")
        val angle = Attr("angle", "float")
        val type = Attr("type", "enum", mutableListOf("linear" to 0L, "radial" to 1L, "sweep" to 2L))
        val centerX = Attr("centerX", "float|fraction")
        val centerY = Attr("centerY", "float|fraction")
        val gradientRadius = Attr("gradientRadius", "float|fraction|dimension")
    }

    object GradientDrawableSolid {
        val color = Attr("color", "color")
    }

    object GradientDrawableStroke {
        val width = Attr("width")
        val color = Attr("color")
        val dashWidth = Attr("dashWidth", "dimension")
        val dashGap = Attr("dashGap", "dimension")
    }

    object DrawableCorners {
        val radius = Attr("radius", "dimension")
        val topLeftRadius = Attr("topLeftRadius", "dimension")
        val topRightRadius = Attr("topRightRadius", "dimension")
        val bottomLeftRadius = Attr("bottomLeftRadius", "dimension")
        val bottomRightRadius = Attr("bottomRightRadius", "dimension")
    }

    object GradientDrawablePadding {
        val left = Attr("left", "dimension")
        val top = Attr("top", "dimension")
        val right = Attr("right", "dimension")
        val bottom = Attr("bottom", "dimension")
    }

    object LayerDrawable {
        val opacity = Attr("opacity", "enum", mutableListOf("opaque" to -1L, "transparent" to -2L, "translucent" to -3L))
        val autoMirrored = Attr("autoMirrored")
        val paddingMode = Attr("paddingMode", "enum", mutableListOf("nest" to 0L, "stack" to 1L))
        val paddingTop = Attr("paddingTop")
        val paddingBottom = Attr("paddingBottom")
        val paddingLeft = Attr("paddingLeft")
        val paddingRight = Attr("paddingRight")
        val paddingStart = Attr("paddingStart")
        val paddingEnd = Attr("paddingEnd")
    }

    object LayerDrawableItem {
        val left = Attr("left")
        val top = Attr("top")
        val right = Attr("right")
        val bottom = Attr("bottom")
        val start = Attr("start", "dimension")
        val end = Attr("end", "dimension")
        val width = Attr("width")
        val height = Attr("height")
        val gravity = Resources.gravity
        val drawable = DrawableWrapper.drawable
        val id = View.id
    }

    object LevelListDrawableItem {
        val minLevel = Attr("minLevel", "integer")
        val maxLevel = Attr("maxLevel", "integer")
        val drawable = DrawableWrapper.drawable
    }

    object RotateDrawable {
        val visible = Attr("visible")
        val fromDegrees = Attr("fromDegrees", "float")
        val toDegrees = Attr("toDegrees", "float")
        val pivotX = RotateAnimation.pivotX
        val pivotY = RotateAnimation.pivotY
        val drawable = DrawableWrapper.drawable
        val level = ClipDrawable.level
    }

    object AnimatedRotateDrawable {
        val visible = Attr("visible")
        val frameDuration = Attr("frameDuration", "integer")
        val framesCount = Attr("framesCount", "integer")
        val pivotX = RotateAnimation.pivotX
        val pivotY = RotateAnimation.pivotY
        val drawable = DrawableWrapper.drawable
    }

    object MaterialProgressDrawable {
        val visible = Attr("visible")
        val thickness = Attr("thickness")
        val innerRadius = Attr("innerRadius")
        val width = Attr("width")
        val height = Attr("height")
        val color = Attr("color")
    }

    object InsetDrawable {
        val drawable = Attr("drawable", "reference")
        val inset = Attr("inset", "fraction|dimension")
        val insetLeft = Attr("insetLeft", "fraction|dimension")
        val insetRight = Attr("insetRight", "fraction|dimension")
        val insetTop = Attr("insetTop", "fraction|dimension")
        val insetBottom = Attr("insetBottom", "fraction|dimension")
    }

    object AnimatedImageDrawable {
        val src = Attr("src")
        val autoMirrored = Attr("autoMirrored")
        val repeatCount = Attr("repeatCount")
        val autoStart = Attr("autoStart")
    }

    object BitmapDrawable {
        val src = Attr("src")
        val antialias = Attr("antialias", "boolean")
        val filter = Attr("filter", "boolean")
        val dither = Attr("dither")
        val gravity = Resources.gravity
        val tileMode = Attr("tileMode", "enum", mutableListOf("disabled" to -1L, "clamp" to 0L, "repeat" to 1L, "mirror" to 2L))
        val tileModeX = Attr("tileModeX", "enum", mutableListOf("disabled" to -1L, "clamp" to 0L, "repeat" to 1L, "mirror" to 2L))
        val tileModeY = Attr("tileModeY", "enum", mutableListOf("disabled" to -1L, "clamp" to 0L, "repeat" to 1L, "mirror" to 2L))
        val mipMap = Attr("mipMap", "boolean")
        val autoMirrored = Attr("autoMirrored")
        val tint = ImageView.tint
        val tintMode = Attr("tintMode", "enum", mutableListOf("src_over" to 3L, "src_in" to 5L, "src_atop" to 9L, "multiply" to 14L, "screen" to 15L, "add" to 16L))
        val alpha = Attr("alpha")
    }

    object NinePatchDrawable {
        val src = Attr("src")
        val dither = Attr("dither")
        val autoMirrored = Attr("autoMirrored")
        val tint = Attr("tint")
        val tintMode = BitmapDrawable.tintMode
        val alpha = Attr("alpha")
    }

    object ColorDrawable {
        val color = Attr("color")
    }

    object AdaptiveIconDrawableLayer {
        val drawable = DrawableWrapper.drawable
    }

    object RippleDrawable {
        val color = Attr("color")
        val radius = Attr("radius")
    }

    object ScaleDrawable {
        val scaleWidth = Attr("scaleWidth", "string")
        val scaleHeight = Attr("scaleHeight", "string")
        val scaleGravity = Attr("scaleGravity", "flag", mutableListOf("top" to 0x30L, "bottom" to 0x50L, "left" to 0x03L, "right" to 0x05L, "center_vertical" to 0x10L, "fill_vertical" to 0x70L, "center_horizontal" to 0x01L, "fill_horizontal" to 0x07L, "center" to 0x11L, "fill" to 0x77L, "clip_vertical" to 0x80L, "clip_horizontal" to 0x08L, "start" to 0x00800003L, "end" to 0x00800005L))
        val level = Attr("level", "integer")
        val drawable = DrawableWrapper.drawable
        val useIntrinsicSizeAsMinimum = Attr("useIntrinsicSizeAsMinimum", "boolean")
    }

    object ClipDrawable {
        val clipOrientation = Attr("clipOrientation", "flag", mutableListOf("horizontal" to 1L, "vertical" to 2L))
        val gravity = Resources.gravity
        val drawable = Attr("drawable", "reference")

        val level = Attr("level", "float|integer|fraction")
    }

    object ShapeDrawablePadding {
        val left = Attr("left")
        val top = Attr("top")
        val right = Attr("right")
        val bottom = Attr("bottom")
    }

    object ShapeDrawable {
        val color = Attr("color")
        val width = Attr("width")
        val height = Attr("height")
        val dither = Attr("dither")
        val tint = BitmapDrawable.tint
        val tintMode = BitmapDrawable.tintMode
    }

    object VectorDrawable {
        val tint = BitmapDrawable.tint
        val tintMode = BitmapDrawable.tintMode
        val autoMirrored = Attr("autoMirrored")
        val width = Attr("width", "dimension")
        val height = Attr("height", "dimension")
        val viewportWidth = Attr("viewportWidth", "float")
        val viewportHeight = Attr("viewportHeight", "float")
        val name = Attr("name", "string")
        val alpha = Attr("alpha", "float")
        val opticalInsetLeft = Attr("opticalInsetLeft", "dimension")
        val opticalInsetTop = Attr("opticalInsetTop", "dimension")
        val opticalInsetRight = Attr("opticalInsetRight", "dimension")
        val opticalInsetBottom = Attr("opticalInsetBottom", "dimension")
    }

    object VectorDrawableGroup {
        val name = VectorDrawable.name
        val rotation = View.rotation
        val pivotX = RotateAnimation.pivotX
        val pivotY = RotateAnimation.pivotY
        val translateX = Attr("translateX", "float")
        val translateY = Attr("translateY", "float")
        val scaleX = View.scaleX
        val scaleY = View.scaleY
    }

    object VectorDrawablePath {
        val name = VectorDrawable.name
        val pathData = Attr("pathData", "string")
        val fillColor = Attr("fillColor", "color")
        val fillAlpha = Attr("fillAlpha", "float")
        val fillType = Attr("fillType", "enum", mutableListOf("nonZero" to 0L, "evenOdd" to 1L))
        val trimPathOffset = Attr("trimPathOffset", "float")
        val trimPathStart = Attr("trimPathStart", "float")
        val trimPathEnd = Attr("trimPathEnd", "float")
        val strokeWidth = Attr("strokeWidth", "float")
        val strokeColor = Attr("strokeColor", "color")
        val strokeAlpha = Attr("strokeAlpha", "float")
        val strokeLineCap = Attr("strokeLineCap", "enum", mutableListOf("butt" to 0L, "round" to 1L, "square" to 2L))
        val strokeLineJoin = Attr("strokeLineJoin", "enum", mutableListOf("miter" to 0L, "round" to 1L, "bevel" to 2L))
        val strokeMiterLimit = Attr("strokeMiterLimit", "float")
    }

    object VectorDrawableClipPath {
        val name = VectorDrawable.name
        val pathData = VectorDrawablePath.pathData
    }

    object AnimatedVectorDrawable {
        val drawable = DrawableWrapper.drawable
    }

    object AnimatedVectorDrawableTarget {
        val name = Attr("name")
        val animation = Attr("animation")
    }

    object Animation {
        val interpolator = Attr("interpolator")
        val fillEnabled = Attr("fillEnabled", "boolean")
        val fillBefore = Attr("fillBefore", "boolean")
        val fillAfter = Attr("fillAfter", "boolean")
        val duration = Attr("duration")
        val startOffset = Attr("startOffset", "integer")
        val repeatCount = Attr("repeatCount", "integer", mutableListOf("infinite" to -1L))
        val repeatMode = Attr("repeatMode", "enum", mutableListOf("restart" to 1L, "reverse" to 2L))
        val zAdjustment = Attr("zAdjustment", "enum", mutableListOf("normal" to 0L, "top" to 1L, "bottom" to -1L))
        val background = Attr("background", "color")
        val detachWallpaper = Attr("detachWallpaper", "boolean")
        val showWallpaper = Attr("showWallpaper", "boolean")
        val hasRoundedCorners = Attr("hasRoundedCorners", "boolean")
    }

    object AnimationSet {
        val shareInterpolator = Attr("shareInterpolator", "boolean")
    }

    object RotateAnimation {
        val fromDegrees = Attr("fromDegrees", "float")
        val toDegrees = Attr("toDegrees", "float")
        val pivotX = Attr("pivotX", "fraction|float|int")
        val pivotY = Attr("pivotY", "fraction|float|int")
    }

    object ScaleAnimation {
        val fromXScale = Attr("fromXScale", "float|fraction|dimension")
        val toXScale = Attr("toXScale", "float|fraction|dimension")
        val fromYScale = Attr("fromYScale", "float|fraction|dimension")
        val toYScale = Attr("toYScale", "float|fraction|dimension")
        val pivotX = RotateAnimation.pivotX
        val pivotY = RotateAnimation.pivotY
    }

    object TranslateAnimation {
        val fromXDelta = Attr("fromXDelta", "float|fraction|dimension")
        val toXDelta = Attr("toXDelta", "float|fraction|dimension")
        val fromYDelta = Attr("fromYDelta", "float|fraction|dimension")
        val toYDelta = Attr("toYDelta", "float|fraction|dimension")
    }

    object AlphaAnimation {
        val fromAlpha = Attr("fromAlpha", "float")
        val toAlpha = Attr("toAlpha", "float")
    }

    object ClipRectAnimation {
        val fromLeft = Attr("fromLeft", "fraction")
        val fromTop = Attr("fromTop", "fraction")
        val fromRight = Attr("fromRight", "fraction")
        val fromBottom = Attr("fromBottom", "fraction")
        val toLeft = Attr("toLeft", "fraction")
        val toTop = Attr("toTop", "fraction")
        val toRight = Attr("toRight", "fraction")
        val toBottom = Attr("toBottom", "fraction")
    }

    object LayoutAnimation {
        val delay = Attr("delay", "float|fraction")
        val animation = Attr("", "reference")
        val animationOrder = Attr("animationOrder", "enum", mutableListOf("normal" to 0L, "reverse" to 1L, "random" to 2L))
        val interpolator = Attr("interpolator")
    }

    object GridLayoutAnimation {
        val columnDelay = Attr("columnDelay", "float|fraction")
        val rowDelay = Attr("rowDelay", "float|fraction")
        val direction = Attr("direction", "flag", mutableListOf("left_to_right" to 0x0L, "right_to_left" to 0x1L, "top_to_bottom" to 0x0L, "bottom_to_top" to 0x2L))
        val directionPriority = Attr("directionPriority", "enum", mutableListOf("none" to 0L, "column" to 1L, "row" to 2L))
    }

    object AccelerateInterpolator {
        val factor = Attr("factor", "float")
    }

    object DecelerateInterpolator {
        val factor = Attr("factor")
    }

    object CycleInterpolator {
        val cycles = Attr("cycles", "float")
    }

    object AnticipateInterpolator {
        val tension = Attr("tension", "float")
    }

    object OvershootInterpolator {
        val tension = Attr("tension")
    }

    object AnticipateOvershootInterpolator {
        val tension = Attr("tension")
        val extraTension = Attr("extraTension", "float")
    }

    object PathInterpolator {
        val controlX1 = Attr("controlX1", "float")
        val controlY1 = Attr("controlY1", "float")
        val controlX2 = Attr("controlX2", "float")
        val controlY2 = Attr("controlY2", "float")
        val pathData = VectorDrawablePath.pathData
    }

    object Transition {
        val duration = Attr("duration")
        val startDelay = Attr("startDelay", "integer")
        val interpolator = Attr("interpolator")
        val matchOrder = Attr("matchOrder", "string")
    }

    object EpicenterTranslateClipReveal {
        val interpolatorX = Attr("interpolatorX", "reference")
        val interpolatorY = Attr("interpolatorY", "reference")
        val interpolatorZ = Attr("interpolatorZ", "reference")
    }

    object Fade {
        val fadingMode = Attr("fadingMode", "enum", mutableListOf("fade_in" to 1L, "fade_out" to 2L, "fade_in_out" to 3L))
    }

    object Slide {
        val slideEdge = Attr("slideEdge", "enum", mutableListOf("left" to 0x03L, "top" to 0x30L, "right" to 0x05L, "bottom" to 0x50L, "start" to 0x00800003L, "end" to 0x00800005L))
    }

    object VisibilityTransition {
        val transitionVisibilityMode = Attr("transitionVisibilityMode", "flag", mutableListOf("mode_in" to 1L, "mode_out" to 2L))
    }

    object TransitionTarget {
        val targetId = Attr("targetId", "reference")
        val excludeId = Attr("excludeId", "reference")
        val targetClass = Attr("targetClass")
        val excludeClass = Attr("excludeClass", "string")
        val targetName = Attr("targetName", "string")
        val excludeName = Attr("excludeName", "string")
    }

    object TransitionSet {
        val transitionOrdering = Attr("transitionOrdering", "enum", mutableListOf("together" to 0L, "sequential" to 1L))
    }

    object ChangeTransform {
        val reparentWithOverlay = Attr("reparentWithOverlay", "boolean")
        val reparent = Attr("reparent", "boolean")
    }

    object ChangeBounds {
        val resizeClip = Attr("resizeClip", "boolean")
    }

    object TransitionManager {
        val transition = Attr("transition", "reference")
        val fromScene = Attr("fromScene", "reference")
        val toScene = Attr("toScene", "reference")
    }

    object ArcMotion {
        val minimumHorizontalAngle = Attr("minimumHorizontalAngle", "float")
        val minimumVerticalAngle = Attr("minimumVerticalAngle", "float")
        val maximumAngle = Attr("maximumAngle", "float")
    }

    object PatternPathMotion {
        val patternPathData = Attr("patternPathData", "string")
    }

    object Animator {
        val interpolator = Attr("interpolator")
        val duration = Attr("duration")
        val startOffset = Attr("startOffset")
        val repeatCount = Attr("repeatCount")
        val repeatMode = Attr("repeatMode", "enum", mutableListOf("restart" to 2L, "reverse" to 1L))
        val valueFrom = Attr("valueFrom", "float|integer|color|dimension|string")
        val valueTo = Attr("valueTo", "float|integer|color|dimension|string")
        val valueType = Attr("valueType", "enum", mutableListOf("floatType" to 0L, "intType" to 1L, "pathType" to 2L, "colorType" to 3L))
        val removeBeforeMRelease = Attr("removeBeforeMRelease", "integer")
    }

    object PropertyValuesHolder {
        val valueType = Attr("valueType")
        val propertyName = Attr("propertyName")
        val valueFrom = Attr("valueFrom")
        val valueTo = Attr("valueTo")
    }

    object Keyframe {
        val valueType = Attr("valueType")
        val value = Attr("value")
        val fraction = Attr("fraction", "float")
        val interpolator = Attr("interpolator")
    }

    object PropertyAnimator {
        val propertyName = Attr("propertyName", "string")
        val propertyXName = Attr("propertyXName", "string")
        val propertyYName = Attr("propertyYName", "string")
        val pathData = VectorDrawablePath.pathData
    }

    object AnimatorSet {
        val ordering = Attr("ordering", "enum", mutableListOf("together" to 0L, "sequentially" to 1L))
    }

    object DrawableStates {
        val state_focused = Attr("state_focused", "boolean")
        val state_window_focused = Attr("state_window_focused", "boolean")
        val state_enabled = Attr("state_enabled", "boolean")
        val state_checkable = Attr("state_checkable", "boolean")
        val state_checked = Attr("state_checked", "boolean")
        val state_selected = Attr("state_selected", "boolean")
        val state_pressed = Attr("state_pressed", "boolean")
        val state_activated = Attr("state_activated", "boolean")
        val state_active = Attr("state_active", "boolean")
        val state_single = Attr("state_single", "boolean")
        val state_first = Attr("state_first", "boolean")
        val state_middle = Attr("state_middle", "boolean")
        val state_last = Attr("state_last", "boolean")
        val state_accelerated = Attr("state_accelerated", "boolean")
        val state_hovered = Attr("state_hovered", "boolean")
        val state_drag_can_accept = Attr("state_drag_can_accept", "boolean")
        val state_drag_hovered = Attr("state_drag_hovered", "boolean")
        val state_accessibility_focused = Attr("state_accessibility_focused", "boolean")
    }

    object ViewDrawableStates {
        val state_pressed = Attr("state_pressed")
        val state_focused = Attr("state_focused")
        val state_selected = Attr("state_selected")
        val state_window_focused = Attr("state_window_focused")
        val state_enabled = Attr("state_enabled")
        val state_activated = Attr("state_activated")
        val state_accelerated = Attr("state_accelerated")
        val state_hovered = Attr("state_hovered")
        val state_drag_can_accept = Attr("state_drag_can_accept")
        val state_drag_hovered = Attr("state_drag_hovered")
    }

    object MenuItemCheckedState {
        val state_checkable = Attr("state_checkable")
        val state_checked = Attr("state_checked")
    }

    object MenuItemUncheckedState {
        val state_checkable = Attr("state_checkable")
    }

    object MenuItemCheckedFocusedState {
        val state_checkable = Attr("state_checkable")
        val state_checked = Attr("state_checked")
        val state_focused = Attr("state_focused")
    }

    object MenuItemUncheckedFocusedState {
        val state_checkable = Attr("state_checkable")
        val state_focused = Attr("state_focused")
    }

    object ExpandableListChildIndicatorState {
        val state_last = Attr("state_last")
    }

    object ExpandableListGroupIndicatorState {
        val state_expanded = Attr("state_expanded", "boolean")
        val state_empty = Attr("state_empty", "boolean")
    }

    object PopupWindowBackgroundState {
        val state_above_anchor = Attr("state_above_anchor", "boolean")
    }

    object TextViewMultiLineBackgroundState {
        val state_multiline = Attr("state_multiline", "boolean")
    }

    object Searchable {
        val icon = Attr("icon")
        val label = Attr("label")
        val hint = Attr("hint")
        val searchButtonText = Attr("searchButtonText", "string")
        val inputType = Resources.inputType
        val imeOptions = Resources.imeOptions
        val searchMode = Attr("searchMode", "flag", mutableListOf("showSearchLabelAsBadge" to 0x04L, "showSearchIconAsBadge" to 0x08L, "queryRewriteFromData" to 0x10L, "queryRewriteFromText" to 0x20L))
        val voiceSearchMode = Attr("voiceSearchMode", "flag", mutableListOf("showVoiceSearchButton" to 0x01L, "launchWebSearch" to 0x02L, "launchRecognizer" to 0x04L))
        val voiceLanguageModel = Attr("voiceLanguageModel", "string")
        val voicePromptText = Attr("voicePromptText", "string")
        val voiceLanguage = Attr("voiceLanguage", "string")
        val voiceMaxResults = Attr("voiceMaxResults", "integer")
        val searchSuggestAuthority = Attr("searchSuggestAuthority", "string")
        val searchSuggestPath = Attr("searchSuggestPath", "string")
        val searchSuggestSelection = Attr("searchSuggestSelection", "string")
        val searchSuggestIntentAction = Attr("searchSuggestIntentAction", "string")
        val searchSuggestIntentData = Attr("searchSuggestIntentData", "string")
        val searchSuggestThreshold = Attr("searchSuggestThreshold", "integer")
        val includeInGlobalSearch = Attr("includeInGlobalSearch", "boolean")
        val queryAfterZeroResults = Attr("queryAfterZeroResults", "boolean")
        val searchSettingsDescription = Attr("searchSettingsDescription", "string")
        val autoUrlDetect = Attr("autoUrlDetect", "boolean")
    }

    object SearchableActionKey {
        val keycode = Attr("keycode")
        val queryActionMsg = Attr("queryActionMsg", "string")
        val suggestActionMsg = Attr("suggestActionMsg", "string")
        val suggestActionMsgColumn = Attr("suggestActionMsgColumn", "string")
    }

    object MapView {
        val apiKey = Attr("apiKey", "string")
    }

    object Menu

    object MenuGroup {
        val id = View.id
        val menuCategory = Attr("menuCategory", "enum", mutableListOf("container" to 0x00010000L, "system" to 0x00020000L, "secondary" to 0x00030000L, "alternative" to 0x00040000L))
        val orderInCategory = Attr("orderInCategory", "integer")
        val checkableBehavior = Attr("checkableBehavior", "enum", mutableListOf("none" to 0L, "all" to 1L, "single" to 2L))
        val visible = Attr("visible")
        val enabled = TextView.enabled
    }

    object MenuItem {
        val id = View.id
        val menuCategory = Attr("menuCategory")
        val orderInCategory = Attr("orderInCategory")
        val title = Attr("title", "string")
        val titleCondensed = Attr("titleCondensed", "string")
        val icon = Attr("icon")
        val iconTint = Attr("iconTint", "color")
        val iconTintMode = Attr("iconTintMode", "enum", mutableListOf("src_over" to 3L, "src_in" to 5L, "src_atop" to 9L, "multiply" to 14L, "screen" to 15L, "add" to 16L))
        val alphabeticShortcut = Attr("alphabeticShortcut", "string")
        val alphabeticModifiers = Attr("alphabeticModifiers", "flag", mutableListOf("META" to 0x10000L, "CTRL" to 0x1000L, "ALT" to 0x02L, "SHIFT" to 0x1L, "SYM" to 0x4L, "FUNCTION" to 0x8L))
        val numericShortcut = Attr("numericShortcut", "string")
        val numericModifiers = Attr("numericModifiers", "flag", mutableListOf("META" to 0x10000L, "CTRL" to 0x1000L, "ALT" to 0x02L, "SHIFT" to 0x1L, "SYM" to 0x4L, "FUNCTION" to 0x8L))
        val checkable = Attr("checkable", "boolean")
        val checked = CompoundButton.checked
        val visible = Attr("visible")
        val enabled = TextView.enabled
        val onClick = View.onClick
        val showAsAction = Attr("showAsAction", "flag", mutableListOf("never" to 0L, "ifRoom" to 1L, "always" to 2L, "withText" to 4L, "collapseActionView" to 8L))
        val actionLayout = Attr("actionLayout", "reference")
        val actionViewClass = Attr("actionViewClass", "string")
        val actionProviderClass = Attr("actionProviderClass", "string")
        val contentDescription = Attr("contentDescription", "string")
        val tooltipText = Attr("tooltipText", "string")
    }

    object ActivityChooserView {
        val initialActivityCount = Attr("initialActivityCount", "string")
        val expandActivityOverflowButtonDrawable = Attr("expandActivityOverflowButtonDrawable", "reference")
    }

    object PreferenceGroup {
        val orderingFromXml = Attr("orderingFromXml", "boolean")
    }

    object PreferenceHeader {
        val id = View.id
        val title = Attr("title")
        val summary = Attr("summary", "string")
        val breadCrumbTitle = Attr("breadCrumbTitle", "string")
        val breadCrumbShortTitle = Attr("breadCrumbShortTitle", "string")
        val icon = Attr("icon")
        val fragment = Attr("fragment", "string")
    }

    object Preference {
        val icon = Attr("icon")
        val key = Attr("key", "string")
        val title = Attr("title")
        val summary = Attr("summary")
        val order = Attr("order", "integer")
        val fragment = Attr("fragment")
        val layout = Attr("layout")
        val widgetLayout = Attr("widgetLayout", "reference")
        val enabled = TextView.enabled
        val selectable = Attr("selectable", "boolean")
        val dependency = Attr("dependency", "string")
        val persistent = Attr("persistent")
        val defaultValue = Attr("defaultValue", "string|boolean|integer|reference|float")
        val shouldDisableView = Attr("shouldDisableView", "boolean")
        val recycleEnabled = Attr("recycleEnabled", "boolean")
        val singleLineTitle = Attr("singleLineTitle", "boolean")
        val iconSpaceReserved = Attr("iconSpaceReserved", "boolean")
    }

    object CheckBoxPreference {
        val summaryOn = Attr("summaryOn", "string")
        val summaryOff = Attr("summaryOff", "string")
        val disableDependentsState = Attr("disableDependentsState", "boolean")
    }

    object DialogPreference {
        val dialogTitle = Attr("dialogTitle", "string")
        val dialogMessage = Attr("dialogMessage", "string")
        val dialogIcon = Attr("dialogIcon", "reference")
        val positiveButtonText = Attr("positiveButtonText", "string")
        val negativeButtonText = Attr("negativeButtonText", "string")
        val dialogLayout = Attr("dialogLayout", "reference")
    }

    object ListPreference {
        val entries = Resources.entries
        val entryValues = Attr("entryValues", "reference")
    }

    object MultiSelectListPreference {
        val entries = Resources.entries
        val entryValues = Attr("entryValues")
    }

    object RingtonePreference {
        val ringtoneType = Attr("ringtoneType", "flag", mutableListOf("ringtone" to 1L, "notification" to 2L, "alarm" to 4L, "all" to 7L))
        val showDefault = Attr("showDefault", "boolean")
        val showSilent = Attr("showSilent", "boolean")
    }

    object VolumePreference {
        val streamType = Attr("streamType", "enum", mutableListOf("voice" to 0L, "system" to 1L, "ring" to 2L, "music" to 3L, "alarm" to 4L))
    }

    object InputMethodService {
        val imeFullscreenBackground = Attr("imeFullscreenBackground", "reference|color")
        val imeExtractEnterAnimation = Attr("imeExtractEnterAnimation", "reference")
        val imeExtractExitAnimation = Attr("imeExtractExitAnimation", "reference")
    }

    object VoiceInteractionSession

    object KeyboardView {
        val keyboardViewStyle = Attr("keyboardViewStyle", "reference")
        val keyBackground = Attr("keyBackground", "reference")
        val keyTextSize = Attr("keyTextSize", "dimension")
        val labelTextSize = Attr("labelTextSize", "dimension")
        val keyTextColor = Attr("keyTextColor", "color")
        val keyPreviewLayout = Attr("keyPreviewLayout", "reference")
        val keyPreviewOffset = Attr("keyPreviewOffset", "dimension")
        val keyPreviewHeight = Attr("keyPreviewHeight", "dimension")
        val verticalCorrection = Attr("verticalCorrection", "dimension")
        val popupLayout = Attr("popupLayout", "reference")
        val shadowColor = Attr("shadowColor")
        val shadowRadius = Attr("shadowRadius")
    }

    object KeyboardViewPreviewState {
        val state_long_pressable = Attr("state_long_pressable", "boolean")
    }

    object Keyboard {
        val keyWidth = Attr("keyWidth", "dimension|fraction")
        val keyHeight = Attr("keyHeight", "dimension|fraction")
        val horizontalGap = Attr("horizontalGap", "dimension|fraction")
        val verticalGap = Attr("verticalGap", "dimension|fraction")
    }

    object Keyboard_Row {
        val rowEdgeFlags = Attr("rowEdgeFlags", "flag", mutableListOf("top" to 4L, "bottom" to 8L))
        val keyboardMode = Attr("keyboardMode", "reference")
    }

    object Keyboard_Key {
        val codes = Attr("codes", "integer|string")
        val popupKeyboard = Attr("popupKeyboard", "reference")
        val popupCharacters = Attr("popupCharacters", "string")
        val keyEdgeFlags = Attr("keyEdgeFlags", "flag", mutableListOf("left" to 1L, "right" to 2L))
        val isModifier = Attr("isModifier", "boolean")
        val isSticky = Attr("isSticky", "boolean")
        val isRepeatable = Attr("isRepeatable", "boolean")
        val iconPreview = Attr("iconPreview", "reference")
        val keyOutputText = Attr("keyOutputText", "string")
        val keyLabel = Attr("keyLabel", "string")
        val keyIcon = Attr("keyIcon", "reference")
        val keyboardMode = Attr("keyboardMode")
    }

    object AppWidgetProviderInfo {
        val minWidth = Attr("minWidth")
        val minHeight = Attr("minHeight")
        val minResizeWidth = Attr("minResizeWidth", "dimension")
        val minResizeHeight = Attr("minResizeHeight", "dimension")
        val updatePeriodMillis = Attr("updatePeriodMillis", "integer")
        val initialLayout = Attr("initialLayout", "reference")
        val initialKeyguardLayout = Attr("initialKeyguardLayout", "reference")
        val configure = Attr("configure", "string")
        val previewImage = Attr("previewImage", "reference")
        val autoAdvanceViewId = Attr("autoAdvanceViewId", "reference")
        val resizeMode = Attr("resizeMode", "integer", mutableListOf("none" to 0x0L, "horizontal" to 0x1L, "vertical" to 0x2L))
        val widgetCategory = Attr("widgetCategory", "integer", mutableListOf("home_screen" to 0x1L, "keyguard" to 0x2L, "searchbox" to 0x4L))
        val widgetFeatures = Attr("widgetFeatures", "integer", mutableListOf("reconfigurable" to 0x1L, "hide_from_picker" to 0x2L))
    }

    object WallpaperPreviewInfo {
        val staticWallpaperPreview = Attr("staticWallpaperPreview", "reference")
    }

    object Fragment {
        val name = Attr("name")
        val id = View.id
        val tag = Attr("tag")
        val fragmentExitTransition = Attr("fragmentExitTransition", "reference")
        val fragmentEnterTransition = Attr("fragmentEnterTransition", "reference")
        val fragmentSharedElementEnterTransition = Attr("fragmentSharedElementEnterTransition", "reference")
        val fragmentReturnTransition = Attr("fragmentReturnTransition", "reference")
        val fragmentSharedElementReturnTransition = Attr("fragmentSharedElementReturnTransition", "reference")
        val fragmentReenterTransition = Attr("fragmentReenterTransition", "reference")
        val fragmentAllowEnterTransitionOverlap = Attr("fragmentAllowEnterTransitionOverlap", "reference")
        val fragmentAllowReturnTransitionOverlap = Attr("fragmentAllowReturnTransitionOverlap", "reference")
    }

    object DeviceAdmin {
        val visible = Attr("visible")
    }

    object Wallpaper {
        val settingsActivity = Attr("settingsActivity")
        val thumbnail = Attr("thumbnail", "reference")
        val author = Attr("author", "reference")
        val description = Attr("description")
        val contextUri = Attr("contextUri", "reference")
        val contextDescription = Attr("contextDescription", "reference")
        val showMetadataInPreview = Attr("showMetadataInPreview", "boolean")
        val supportsAmbientMode = Attr("supportsAmbientMode", "boolean")
        val settingsSliceUri = Attr("settingsSliceUri", "string")
        val supportsMultipleDisplays = Attr("supportsMultipleDisplays", "boolean")
    }

    object Dream {
        val settingsActivity = Attr("settingsActivity")
    }

    object TrustAgent {
        val settingsActivity = Attr("settingsActivity")
        val title = Attr("title")
        val summary = Attr("summary")
        val unlockProfile = Attr("unlockProfile", "boolean")
    }

    object AccountAuthenticator {
        val accountType = Attr("accountType", "string")
        val label = Attr("label")
        val icon = Attr("icon")
        val smallIcon = Attr("smallIcon", "reference")
        val accountPreferences = Attr("accountPreferences", "reference")
        val customTokens = Attr("customTokens", "boolean")
    }

    object SyncAdapter {
        val contentAuthority = Attr("contentAuthority", "string")
        val accountType = Attr("accountType")
        val userVisible = Attr("userVisible", "boolean")
        val supportsUploading = Attr("supportsUploading", "boolean")
        val allowParallelSyncs = Attr("allowParallelSyncs", "boolean")
        val isAlwaysSyncable = Attr("isAlwaysSyncable", "boolean")
        val settingsActivity = Attr("settingsActivity")
    }

    object AutofillService {
        val settingsActivity = Attr("settingsActivity")
    }

    object AutofillService_CompatibilityPackage {
        val name = Attr("name")
        val maxLongVersionCode = Attr("maxLongVersionCode", "string")
    }

    object ContentCaptureService {
        val settingsActivity = Attr("settingsActivity")
    }

    object Icon {
        val icon = Attr("icon")
        val mimeType = Attr("mimeType")
    }

    object IconDefault {
        val icon = Attr("icon")
    }

    object ContactsDataKind {
        val mimeType = Attr("mimeType")
        val icon = Attr("icon")
        val summaryColumn = Attr("summaryColumn", "string")
        val detailColumn = Attr("detailColumn", "string")
        val detailSocialSummary = Attr("detailSocialSummary", "boolean")
        val allContactsName = Attr("allContactsName", "string")
    }

    object SlidingTab {
        val orientation = Resources.orientation
    }

    object GlowPadView {
        val targetDescriptions = Attr("targetDescriptions", "reference")
        val directionDescriptions = Attr("directionDescriptions", "reference")
    }

    object SettingInjectorService {
        val title = Attr("title")
        val icon = Attr("icon")
        val settingsActivity = Attr("settingsActivity")
        val userRestriction = Attr("userRestriction", "string")
    }

    object LockPatternView {
        val aspect = Attr("aspect", "string")
        val pathColor = Attr("pathColor", "color|reference")
        val regularColor = Attr("regularColor", "color|reference")
        val errorColor = Attr("errorColor", "color|reference")
        val successColor = Attr("successColor", "color|reference")
    }

    object RecognitionService {
        val settingsActivity = Attr("settingsActivity")
    }

    object VoiceInteractionService {
        val sessionService = Attr("sessionService", "string")
        val recognitionService = Attr("recognitionService", "string")
        val settingsActivity = Attr("settingsActivity")
        val supportsAssist = Attr("supportsAssist", "boolean")
        val supportsLaunchVoiceAssistFromKeyguard = Attr("supportsLaunchVoiceAssistFromKeyguard", "boolean")
        val supportsLocalInteraction = Attr("supportsLocalInteraction", "boolean")
    }

    object VoiceEnrollmentApplication {
        val searchKeyphraseId = Attr("searchKeyphraseId", "integer")
        val searchKeyphrase = Attr("searchKeyphrase", "string")
        val searchKeyphraseSupportedLocales = Attr("searchKeyphraseSupportedLocales", "string")
        val searchKeyphraseRecognitionFlags = Attr("searchKeyphraseRecognitionFlags", "flag", mutableListOf("none" to 0L, "voiceTrigger" to 0x1L, "userIdentification" to 0x2L))
    }

    object ActionBar {
        val navigationMode = Attr("navigationMode", "enum", mutableListOf("normal" to 0L, "listMode" to 1L, "tabMode" to 2L))
        val displayOptions = Attr("displayOptions", "flag", mutableListOf("none" to 0L, "useLogo" to 0x1L, "showHome" to 0x2L, "homeAsUp" to 0x4L, "showTitle" to 0x8L, "showCustom" to 0x10L, "disableHome" to 0x20L))
        val title = Attr("title")
        val subtitle = Attr("subtitle", "string")
        val titleTextStyle = Attr("titleTextStyle", "reference")
        val subtitleTextStyle = Attr("subtitleTextStyle", "reference")
        val icon = Attr("icon")
        val logo = Attr("logo")
        val divider = Attr("divider")
        val background = Attr("background")
        val backgroundStacked = Attr("backgroundStacked", "reference|color")
        val backgroundSplit = Attr("backgroundSplit", "reference|color")
        val customNavigationLayout = Attr("customNavigationLayout", "reference")
        val height = Attr("height")
        val homeLayout = Attr("homeLayout", "reference")
        val progressBarStyle = Attr("progressBarStyle")
        val indeterminateProgressStyle = Attr("indeterminateProgressStyle", "reference")
        val progressBarPadding = Attr("progressBarPadding", "dimension")
        val homeAsUpIndicator = Attr("homeAsUpIndicator")
        val itemPadding = Attr("itemPadding", "dimension")
        val hideOnContentScroll = Attr("hideOnContentScroll", "boolean")
        val contentInsetStart = Attr("contentInsetStart", "dimension")
        val contentInsetEnd = Attr("contentInsetEnd", "dimension")
        val contentInsetLeft = Attr("contentInsetLeft", "dimension")
        val contentInsetRight = Attr("contentInsetRight", "dimension")
        val contentInsetStartWithNavigation = Attr("contentInsetStartWithNavigation", "dimension")
        val contentInsetEndWithActions = Attr("contentInsetEndWithActions", "dimension")
        val elevation = View.elevation
        val popupTheme = Attr("popupTheme")
    }

    object ActionMode {
        val titleTextStyle = Attr("titleTextStyle")
        val subtitleTextStyle = Attr("subtitleTextStyle")
        val background = Attr("background")
        val backgroundSplit = Attr("backgroundSplit")
        val height = Attr("height")
        val closeItemLayout = Attr("closeItemLayout", "reference")
    }

    object SearchView {
        val layout = Attr("layout")
        val iconifiedByDefault = Attr("iconifiedByDefault", "boolean")
        val maxWidth = Attr("maxWidth", "dimension")
        val queryHint = Attr("queryHint", "string")
        val defaultQueryHint = Attr("defaultQueryHint", "string")
        val imeOptions = Resources.imeOptions
        val inputType = Resources.inputType
        val closeIcon = Attr("closeIcon", "reference")
        val goIcon = Attr("goIcon", "reference")
        val searchIcon = Attr("searchIcon", "reference")
        val searchHintIcon = Attr("searchHintIcon", "reference")
        val voiceIcon = Attr("voiceIcon", "reference")
        val commitIcon = Attr("commitIcon", "reference")
        val suggestionRowLayout = Attr("suggestionRowLayout", "reference")
        val queryBackground = Attr("queryBackground", "reference")
        val submitBackground = Attr("submitBackground", "reference")
    }

    object Switch {
        val thumb = SeekBar.thumb
        val thumbTint = SeekBar.thumbTint
        val thumbTintMode = SeekBar.thumbTintMode
        val track = Attr("track", "reference")
        val trackTint = Attr("trackTint", "color")
        val trackTintMode = Attr("trackTintMode", "enum", mutableListOf("src_over" to 3L, "src_in" to 5L, "src_atop" to 9L, "multiply" to 14L, "screen" to 15L, "add" to 16L))
        val textOn = ToggleButton.textOn
        val textOff = ToggleButton.textOff
        val thumbTextPadding = Attr("thumbTextPadding", "dimension")
        val switchTextAppearance = Attr("switchTextAppearance", "reference")
        val switchMinWidth = Attr("switchMinWidth", "dimension")
        val switchPadding = Attr("switchPadding", "dimension")
        val splitTrack = SeekBar.splitTrack
        val showText = Attr("showText", "boolean")
    }

    object Pointer {
        val pointerIconArrow = Attr("pointerIconArrow", "reference")
        val pointerIconSpotHover = Attr("pointerIconSpotHover", "reference")
        val pointerIconSpotTouch = Attr("pointerIconSpotTouch", "reference")
        val pointerIconSpotAnchor = Attr("pointerIconSpotAnchor", "reference")
        val pointerIconContextMenu = Attr("pointerIconContextMenu", "reference")
        val pointerIconHand = Attr("pointerIconHand", "reference")
        val pointerIconHelp = Attr("pointerIconHelp", "reference")
        val pointerIconWait = Attr("pointerIconWait", "reference")
        val pointerIconCell = Attr("pointerIconCell", "reference")
        val pointerIconCrosshair = Attr("pointerIconCrosshair", "reference")
        val pointerIconText = Attr("pointerIconText", "reference")
        val pointerIconVerticalText = Attr("pointerIconVerticalText", "reference")
        val pointerIconAlias = Attr("pointerIconAlias", "reference")
        val pointerIconCopy = Attr("pointerIconCopy", "reference")
        val pointerIconNodrop = Attr("pointerIconNodrop", "reference")
        val pointerIconAllScroll = Attr("pointerIconAllScroll", "reference")
        val pointerIconHorizontalDoubleArrow = Attr("pointerIconHorizontalDoubleArrow", "reference")
        val pointerIconVerticalDoubleArrow = Attr("pointerIconVerticalDoubleArrow", "reference")
        val pointerIconTopRightDiagonalDoubleArrow = Attr("pointerIconTopRightDiagonalDoubleArrow", "reference")
        val pointerIconTopLeftDiagonalDoubleArrow = Attr("pointerIconTopLeftDiagonalDoubleArrow", "reference")
        val pointerIconZoomIn = Attr("pointerIconZoomIn", "reference")
        val pointerIconZoomOut = Attr("pointerIconZoomOut", "reference")
        val pointerIconGrab = Attr("pointerIconGrab", "reference")
        val pointerIconGrabbing = Attr("pointerIconGrabbing", "reference")
    }

    object PointerIcon {
        val bitmap = Attr("bitmap", "reference")
        val hotSpotX = Attr("hotSpotX", "dimension")
        val hotSpotY = Attr("hotSpotY", "dimension")
    }

    object Storage {
        val mountPoint = Attr("mountPoint", "string")
        val storageDescription = Attr("storageDescription", "string")
        val primary = Attr("primary", "boolean")
        val removable = Attr("removable", "boolean")
        val emulated = Attr("emulated", "boolean")
        val mtpReserve = Attr("mtpReserve", "integer")
        val allowMassStorage = Attr("allowMassStorage", "boolean")
        val maxFileSize = Attr("maxFileSize", "integer")
    }

    object SwitchPreference {
        val summaryOn = Attr("summaryOn")
        val summaryOff = Attr("summaryOff")
        val switchTextOn = Attr("switchTextOn", "string")
        val switchTextOff = Attr("switchTextOff", "string")
        val disableDependentsState = Attr("disableDependentsState")
    }

    object SeekBarPreference {
        val layout = Attr("layout")
        val adjustable = Attr("adjustable", "boolean")
        val showSeekBarValue = Attr("showSeekBarValue", "boolean")
    }

    object PreferenceFragment {
        val layout = Attr("layout")
        val divider = Attr("divider")
    }

    object PreferenceScreen {
        val screenLayout = Attr("screenLayout", "reference")
        val divider = Attr("divider")
    }

    object PreferenceActivity {
        val layout = Attr("layout")
        val headerLayout = Attr("headerLayout", "reference")
        val headerRemoveIconIfEmpty = Attr("headerRemoveIconIfEmpty", "boolean")
    }

    object TextToSpeechEngine {
        val settingsActivity = Attr("settingsActivity")
    }

    object KeyboardLayout {
        val name = Attr("name")
        val label = Attr("label")
        val keyboardLayout = Attr("keyboardLayout", "reference")
        val locale = Attr("locale", "string")
        val vendorId = Attr("vendorId", "integer")
        val productId = Attr("productId", "integer")
    }

    object MediaRouteButton {
        val externalRouteEnabledDrawable = Attr("externalRouteEnabledDrawable", "reference")
        val mediaRouteTypes = Attr("mediaRouteTypes", "integer", mutableListOf("liveAudio" to 0x1L, "user" to 0x800000L))
        val minWidth = Attr("minWidth")
        val minHeight = Attr("minHeight")
    }

    object PagedView {
        val pageSpacing = Attr("pageSpacing", "dimension")
        val scrollIndicatorPaddingLeft = Attr("scrollIndicatorPaddingLeft", "dimension")
        val scrollIndicatorPaddingRight = Attr("scrollIndicatorPaddingRight", "dimension")
    }

    object KeyguardGlowStripView {
        val dotSize = Attr("dotSize", "dimension")
        val numDots = Attr("numDots", "integer")
        val glowDot = Attr("glowDot", "reference")
        val leftToRight = Attr("leftToRight", "boolean")
    }

    object FragmentBreadCrumbs {
        val gravity = Resources.gravity
        val itemLayout = Attr("itemLayout", "reference")
        val itemColor = Attr("itemColor", "color|reference")
    }

    object Toolbar {
        val titleTextAppearance = Attr("titleTextAppearance", "reference")
        val subtitleTextAppearance = Attr("subtitleTextAppearance", "reference")
        val title = Attr("title")
        val subtitle = Attr("subtitle")
        val gravity = Resources.gravity
        val titleMargin = Attr("titleMargin", "dimension")
        val titleMarginStart = Attr("titleMarginStart", "dimension")
        val titleMarginEnd = Attr("titleMarginEnd", "dimension")
        val titleMarginTop = Attr("titleMarginTop", "dimension")
        val titleMarginBottom = Attr("titleMarginBottom", "dimension")
        val contentInsetStart = Attr("contentInsetStart")
        val contentInsetEnd = Attr("contentInsetEnd")
        val contentInsetLeft = Attr("contentInsetLeft")
        val contentInsetRight = Attr("contentInsetRight")
        val contentInsetStartWithNavigation = Attr("contentInsetStartWithNavigation")
        val contentInsetEndWithActions = Attr("contentInsetEndWithActions")
        val maxButtonHeight = Attr("maxButtonHeight", "dimension")
        val navigationButtonStyle = Attr("navigationButtonStyle", "reference")
        val buttonGravity = Attr("buttonGravity", "flag", mutableListOf("top" to 0x30L, "bottom" to 0x50L))
        val collapseIcon = Attr("collapseIcon", "reference")
        val collapseContentDescription = Attr("collapseContentDescription", "string")
        val popupTheme = Attr("popupTheme", "reference")
        val navigationIcon = Attr("navigationIcon", "reference")
        val navigationContentDescription = Attr("navigationContentDescription", "string")
        val logo = Attr("logo")
        val logoDescription = Attr("logoDescription", "string")
        val titleTextColor = Attr("titleTextColor", "color")
        val subtitleTextColor = Attr("subtitleTextColor", "color")
    }

    object Toolbar_LayoutParams {
        val layout_gravity = Attr("layout_gravity")
    }

    object ActionBar_LayoutParams {
        val layout_gravity = Attr("layout_gravity")
    }

    object EdgeEffect {
        val colorEdgeEffect = Attr("colorEdgeEffect")
    }

    object TvInputService {
        val setupActivity = Attr("setupActivity", "string")
        val settingsActivity = Attr("settingsActivity")
        val canRecord = Attr("canRecord", "boolean")
        val tunerCount = Attr("tunerCount", "integer")
    }

    object RatingSystemDefinition {
        val name = Attr("name")
        val title = Attr("title")
        val description = Attr("description")
        val country = Attr("country", "string")
    }

    object RatingDefinition {
        val name = Attr("name")
        val title = Attr("title")
        val description = Attr("description")
        val contentAgeHint = Attr("contentAgeHint", "integer")
    }

    object ResolverDrawerLayout {
        val maxWidth = Attr("maxWidth")
        val maxCollapsedHeight = Attr("maxCollapsedHeight", "dimension")
        val maxCollapsedHeightSmall = Attr("maxCollapsedHeightSmall", "dimension")
        val showAtTop = Attr("showAtTop", "boolean")
    }

    object MessagingLinearLayout {
        val spacing = Attr("spacing")
    }

    object DateTimeView {
        val showRelative = Attr("showRelative", "boolean")
    }

    object ResolverDrawerLayout_LayoutParams {
        val layout_alwaysShow = Attr("layout_alwaysShow", "boolean")
        val layout_ignoreOffset = Attr("layout_ignoreOffset", "boolean")
        val layout_gravity = Attr("layout_gravity")
        val layout_hasNestedScrollIndicator = Attr("layout_hasNestedScrollIndicator", "boolean")
        val layout_maxHeight = Attr("layout_maxHeight", "dimension")
    }

    object Lighting {
        val lightY = Attr("lightY")
        val lightZ = Attr("lightZ")
        val lightRadius = Attr("lightRadius")
        val ambientShadowAlpha = Attr("ambientShadowAlpha")
        val spotShadowAlpha = Attr("spotShadowAlpha")
    }

    object RestrictionEntry {
        val key = Attr("key")
        val restrictionType = Attr("restrictionType", "enum", mutableListOf("hidden" to 0L, "bool" to 1L, "choice" to 2L, "multi-select" to 4L, "integer" to 5L, "string" to 6L, "bundle" to 7L, "bundle_array" to 8L))
        val title = Attr("title")
        val description = Attr("description")
        val defaultValue = Attr("defaultValue")
        val entries = Resources.entries
        val entryValues = Attr("entryValues")
    }

    object GradientColor {
        val startColor = Attr("startColor")
        val centerColor = Attr("centerColor")
        val endColor = Attr("endColor")
        val type = Attr("type")
        val gradientRadius = Attr("gradientRadius")
        val centerX = Attr("centerX")
        val centerY = Attr("centerY")
        val startX = Attr("startX", "float")
        val startY = Attr("startY", "float")
        val endX = Attr("endX", "float")
        val endY = Attr("endY", "float")
        val tileMode = Attr("tileMode")
    }

    object GradientColorItem {
        val offset = Attr("offset", "float")
        val color = Attr("color")
    }

    object ActivityTaskDescription {
        val colorPrimary = Attr("colorPrimary")
        val colorBackground = Attr("colorBackground")
        val statusBarColor = Attr("statusBarColor")
        val navigationBarColor = Attr("navigationBarColor")
        val enforceStatusBarContrast = Attr("enforceStatusBarContrast")
        val enforceNavigationBarContrast = Attr("enforceNavigationBarContrast")
    }

    object Shortcut {
        val shortcutId = Attr("shortcutId", "string")
        val enabled = TextView.enabled
        val icon = Attr("icon")
        val shortcutShortLabel = Attr("shortcutShortLabel", "reference")
        val shortcutLongLabel = Attr("shortcutLongLabel", "reference")
        val shortcutDisabledMessage = Attr("shortcutDisabledMessage", "reference")
    }

    object ShortcutCategories {
        val name = Attr("name")
    }

    object FontFamilyFont {
        val fontStyle = Attr("fontStyle", "enum", mutableListOf("normal" to 0L, "italic" to 1L))
        val font = Attr("font", "reference")
        val fontWeight = Attr("fontWeight", "integer")
        val ttcIndex = Attr("ttcIndex", "integer")
        val fontVariationSettings = Attr("fontVariationSettings", "string")
    }

    object FontFamily {
        val fontProviderAuthority = Attr("fontProviderAuthority", "string")
        val fontProviderPackage = Attr("fontProviderPackage", "string")
        val fontProviderQuery = Attr("fontProviderQuery", "string")
        val fontProviderCerts = Attr("fontProviderCerts", "reference")
    }

    object VideoView2 {
        val enableControlView = Attr("enableControlView", "boolean")
        val enableSubtitle = Attr("enableSubtitle", "boolean")
        val viewType = Attr("viewType", "enum", mutableListOf("surfaceView" to 0L, "textureView" to 1L))
    }

    object RecyclerView {
        val layoutManager = Attr("layoutManager", "string")
        val orientation = Resources.orientation
        val spanCount = Attr("spanCount", "integer")
        val reverseLayout = Attr("reverseLayout", "boolean")
        val stackFromEnd = Attr("stackFromEnd", "boolean")
    }

    object NotificationTheme {
        val notificationHeaderStyle = Attr("notificationHeaderStyle", "reference")
        val notificationHeaderTextAppearance = Attr("notificationHeaderTextAppearance", "reference")
        val notificationHeaderIconSize = Attr("notificationHeaderIconSize", "dimension")
        val notificationHeaderAppNameVisibility = Attr("notificationHeaderAppNameVisibility", "enum", mutableListOf("visible" to 0L, "invisible" to 1L, "gone" to 2L))
    }

    object Magnifier {
        val magnifierWidth = Attr("magnifierWidth", "dimension")
        val magnifierHeight = Attr("magnifierHeight", "dimension")
        val magnifierZoom = Attr("magnifierZoom", "float")
        val magnifierElevation = Attr("magnifierElevation", "dimension")
        val magnifierVerticalOffset = Attr("magnifierVerticalOffset", "dimension")
        val magnifierHorizontalOffset = Attr("magnifierHorizontalOffset", "dimension")
        val magnifierColorOverlay = Attr("magnifierColorOverlay", "color")
    }

    object Resources {
        val textSize = Attr("textSize", "dimension")
        val fontFamily = Attr("fontFamily", "string")
        val typeface = Attr("typeface", "enum", mutableListOf("normal" to 0L, "sans" to 1L, "serif" to 2L, "monospace" to 3L))
        val textStyle = Attr("textStyle", "flag", mutableListOf("normal" to 0L, "bold" to 1L, "italic" to 2L))
        val textColor = Attr("textColor", "reference|color")
        val textColorHighlight = Attr("textColorHighlight", "reference|color")
        val textColorHint = Attr("textColorHint", "reference|color")
        val textColorLink = Attr("textColorLink", "reference|color")
        val textCursorDrawable = Attr("textCursorDrawable", "reference")
        val textIsSelectable = Attr("textIsSelectable", "boolean")
        val ellipsize = Attr("ellipsize", "enum", mutableListOf("none" to 0L, "start" to 1L, "middle" to 2L, "end" to 3L, "marquee" to 4L))
        val inputType = Attr("inputType", "flag", mutableListOf("none" to 0x00000000L, "text" to 0x00000001L, "textCapCharacters" to 0x00001001L, "textCapWords" to 0x00002001L, "textCapSentences" to 0x00004001L, "textAutoCorrect" to 0x00008001L, "textAutoComplete" to 0x00010001L, "textMultiLine" to 0x00020001L, "textImeMultiLine" to 0x00040001L, "textNoSuggestions" to 0x00080001L, "textUri" to 0x00000011L, "textEmailAddress" to 0x00000021L, "textEmailSubject" to 0x00000031L, "textShortMessage" to 0x00000041L, "textLongMessage" to 0x00000051L, "textPersonName" to 0x00000061L, "textPostalAddress" to 0x00000071L, "textPassword" to 0x00000081L, "textVisiblePassword" to 0x00000091L, "textWebEditText" to 0x000000a1L, "textFilter" to 0x000000b1L, "textPhonetic" to 0x000000c1L, "textWebEmailAddress" to 0x000000d1L, "textWebPassword" to 0x000000e1L, "number" to 0x00000002L, "numberSigned" to 0x00001002L, "numberDecimal" to 0x00002002L, "numberPassword" to 0x00000012L, "phone" to 0x00000003L, "datetime" to 0x00000004L, "date" to 0x00000014L, "time" to 0x00000024L))
        val imeOptions = Attr("imeOptions", "flag", mutableListOf("normal" to 0x00000000L, "actionUnspecified" to 0x00000000L, "actionNone" to 0x00000001L, "actionGo" to 0x00000002L, "actionSearch" to 0x00000003L, "actionSend" to 0x00000004L, "actionNext" to 0x00000005L, "actionDone" to 0x00000006L, "actionPrevious" to 0x00000007L, "flagNoPersonalizedLearning" to 0x1000000L, "flagNoFullscreen" to 0x2000000L, "flagNavigatePrevious" to 0x4000000L, "flagNavigateNext" to 0x8000000L, "flagNoExtractUi" to 0x10000000L, "flagNoAccessoryAction" to 0x20000000L, "flagNoEnterAction" to 0x40000000L, "flagForceAscii" to 0x80000000))
        val x = Attr("x", "dimension")
        val y = Attr("y", "dimension")
        val gravity = Attr("gravity", "flag", mutableListOf("top" to 0x30L, "bottom" to 0x50L, "left" to 0x03L, "right" to 0x05L, "center_vertical" to 0x10L, "fill_vertical" to 0x70L, "center_horizontal" to 0x01L, "fill_horizontal" to 0x07L, "center" to 0x11L, "fill" to 0x77L, "clip_vertical" to 0x80L, "clip_horizontal" to 0x08L, "start" to 0x00800003L, "end" to 0x00800005L))
        val autoLink = Attr("autoLink", "flag", mutableListOf("none" to 0x00L, "web" to 0x01L, "email" to 0x02L, "phone" to 0x04L, "map" to 0x08L, "all" to 0x0fL))
        val entries = Attr("entries", "reference")
        val layout_gravity = Attr("layout_gravity", "flag", mutableListOf("top" to 0x30L, "bottom" to 0x50L, "left" to 0x03L, "right" to 0x05L, "center_vertical" to 0x10L, "fill_vertical" to 0x70L, "center_horizontal" to 0x01L, "fill_horizontal" to 0x07L, "center" to 0x11L, "fill" to 0x77L, "clip_vertical" to 0x80L, "clip_horizontal" to 0x08L, "start" to 0x00800003L, "end" to 0x00800005L))
        val orientation = Attr("orientation", "enum", mutableListOf("horizontal" to 0L, "vertical" to 1L))
        val alignmentMode = Attr("alignmentMode", "enum", mutableListOf("alignBounds" to 0L, "alignMargins" to 1L))
        val keycode = Attr("keycode", "enum", mutableListOf("KEYCODE_UNKNOWN" to 0L, "KEYCODE_SOFT_LEFT" to 1L, "KEYCODE_SOFT_RIGHT" to 2L, "KEYCODE_HOME" to 3L, "KEYCODE_BACK" to 4L, "KEYCODE_CALL" to 5L, "KEYCODE_ENDCALL" to 6L, "KEYCODE_0" to 7L, "KEYCODE_1" to 8L, "KEYCODE_2" to 9L, "KEYCODE_3" to 10L, "KEYCODE_4" to 11L, "KEYCODE_5" to 12L, "KEYCODE_6" to 13L, "KEYCODE_7" to 14L, "KEYCODE_8" to 15L, "KEYCODE_9" to 16L, "KEYCODE_STAR" to 17L, "KEYCODE_POUND" to 18L, "KEYCODE_DPAD_UP" to 19L, "KEYCODE_DPAD_DOWN" to 20L, "KEYCODE_DPAD_LEFT" to 21L, "KEYCODE_DPAD_RIGHT" to 22L, "KEYCODE_DPAD_CENTER" to 23L, "KEYCODE_VOLUME_UP" to 24L, "KEYCODE_VOLUME_DOWN" to 25L, "KEYCODE_POWER" to 26L, "KEYCODE_CAMERA" to 27L, "KEYCODE_CLEAR" to 28L, "KEYCODE_A" to 29L, "KEYCODE_B" to 30L, "KEYCODE_C" to 31L, "KEYCODE_D" to 32L, "KEYCODE_E" to 33L, "KEYCODE_F" to 34L, "KEYCODE_G" to 35L, "KEYCODE_H" to 36L, "KEYCODE_I" to 37L, "KEYCODE_J" to 38L, "KEYCODE_K" to 39L, "KEYCODE_L" to 40L, "KEYCODE_M" to 41L, "KEYCODE_N" to 42L, "KEYCODE_O" to 43L, "KEYCODE_P" to 44L, "KEYCODE_Q" to 45L, "KEYCODE_R" to 46L, "KEYCODE_S" to 47L, "KEYCODE_T" to 48L, "KEYCODE_U" to 49L, "KEYCODE_V" to 50L, "KEYCODE_W" to 51L, "KEYCODE_X" to 52L, "KEYCODE_Y" to 53L, "KEYCODE_Z" to 54L, "KEYCODE_COMMA" to 55L, "KEYCODE_PERIOD" to 56L, "KEYCODE_ALT_LEFT" to 57L, "KEYCODE_ALT_RIGHT" to 58L, "KEYCODE_SHIFT_LEFT" to 59L, "KEYCODE_SHIFT_RIGHT" to 60L, "KEYCODE_TAB" to 61L, "KEYCODE_SPACE" to 62L, "KEYCODE_SYM" to 63L, "KEYCODE_EXPLORER" to 64L, "KEYCODE_ENVELOPE" to 65L, "KEYCODE_ENTER" to 66L, "KEYCODE_DEL" to 67L, "KEYCODE_GRAVE" to 68L, "KEYCODE_MINUS" to 69L, "KEYCODE_EQUALS" to 70L, "KEYCODE_LEFT_BRACKET" to 71L, "KEYCODE_RIGHT_BRACKET" to 72L, "KEYCODE_BACKSLASH" to 73L, "KEYCODE_SEMICOLON" to 74L, "KEYCODE_APOSTROPHE" to 75L, "KEYCODE_SLASH" to 76L, "KEYCODE_AT" to 77L, "KEYCODE_NUM" to 78L, "KEYCODE_HEADSETHOOK" to 79L, "KEYCODE_FOCUS" to 80L, "KEYCODE_PLUS" to 81L, "KEYCODE_MENU" to 82L, "KEYCODE_NOTIFICATION" to 83L, "KEYCODE_SEARCH" to 84L, "KEYCODE_MEDIA_PLAY_PAUSE" to 85L, "KEYCODE_MEDIA_STOP" to 86L, "KEYCODE_MEDIA_NEXT" to 87L, "KEYCODE_MEDIA_PREVIOUS" to 88L, "KEYCODE_MEDIA_REWIND" to 89L, "KEYCODE_MEDIA_FAST_FORWARD" to 90L, "KEYCODE_MUTE" to 91L, "KEYCODE_PAGE_UP" to 92L, "KEYCODE_PAGE_DOWN" to 93L, "KEYCODE_PICTSYMBOLS" to 94L, "KEYCODE_SWITCH_CHARSET" to 95L, "KEYCODE_BUTTON_A" to 96L, "KEYCODE_BUTTON_B" to 97L, "KEYCODE_BUTTON_C" to 98L, "KEYCODE_BUTTON_X" to 99L, "KEYCODE_BUTTON_Y" to 100L, "KEYCODE_BUTTON_Z" to 101L, "KEYCODE_BUTTON_L1" to 102L, "KEYCODE_BUTTON_R1" to 103L, "KEYCODE_BUTTON_L2" to 104L, "KEYCODE_BUTTON_R2" to 105L, "KEYCODE_BUTTON_THUMBL" to 106L, "KEYCODE_BUTTON_THUMBR" to 107L, "KEYCODE_BUTTON_START" to 108L, "KEYCODE_BUTTON_SELECT" to 109L, "KEYCODE_BUTTON_MODE" to 110L, "KEYCODE_ESCAPE" to 111L, "KEYCODE_FORWARD_DEL" to 112L, "KEYCODE_CTRL_LEFT" to 113L, "KEYCODE_CTRL_RIGHT" to 114L, "KEYCODE_CAPS_LOCK" to 115L, "KEYCODE_SCROLL_LOCK" to 116L, "KEYCODE_META_LEFT" to 117L, "KEYCODE_META_RIGHT" to 118L, "KEYCODE_FUNCTION" to 119L, "KEYCODE_SYSRQ" to 120L, "KEYCODE_BREAK" to 121L, "KEYCODE_MOVE_HOME" to 122L, "KEYCODE_MOVE_END" to 123L, "KEYCODE_INSERT" to 124L, "KEYCODE_FORWARD" to 125L, "KEYCODE_MEDIA_PLAY" to 126L, "KEYCODE_MEDIA_PAUSE" to 127L, "KEYCODE_MEDIA_CLOSE" to 128L, "KEYCODE_MEDIA_EJECT" to 129L, "KEYCODE_MEDIA_RECORD" to 130L, "KEYCODE_F1" to 131L, "KEYCODE_F2" to 132L, "KEYCODE_F3" to 133L, "KEYCODE_F4" to 134L, "KEYCODE_F5" to 135L, "KEYCODE_F6" to 136L, "KEYCODE_F7" to 137L, "KEYCODE_F8" to 138L, "KEYCODE_F9" to 139L, "KEYCODE_F10" to 140L, "KEYCODE_F11" to 141L, "KEYCODE_F12" to 142L, "KEYCODE_NUM_LOCK" to 143L, "KEYCODE_NUMPAD_0" to 144L, "KEYCODE_NUMPAD_1" to 145L, "KEYCODE_NUMPAD_2" to 146L, "KEYCODE_NUMPAD_3" to 147L, "KEYCODE_NUMPAD_4" to 148L, "KEYCODE_NUMPAD_5" to 149L, "KEYCODE_NUMPAD_6" to 150L, "KEYCODE_NUMPAD_7" to 151L, "KEYCODE_NUMPAD_8" to 152L, "KEYCODE_NUMPAD_9" to 153L, "KEYCODE_NUMPAD_DIVIDE" to 154L, "KEYCODE_NUMPAD_MULTIPLY" to 155L, "KEYCODE_NUMPAD_SUBTRACT" to 156L, "KEYCODE_NUMPAD_ADD" to 157L, "KEYCODE_NUMPAD_DOT" to 158L, "KEYCODE_NUMPAD_COMMA" to 159L, "KEYCODE_NUMPAD_ENTER" to 160L, "KEYCODE_NUMPAD_EQUALS" to 161L, "KEYCODE_NUMPAD_LEFT_PAREN" to 162L, "KEYCODE_NUMPAD_RIGHT_PAREN" to 163L, "KEYCODE_VOLUME_MUTE" to 164L, "KEYCODE_INFO" to 165L, "KEYCODE_CHANNEL_UP" to 166L, "KEYCODE_CHANNEL_DOWN" to 167L, "KEYCODE_ZOOM_IN" to 168L, "KEYCODE_ZOOM_OUT" to 169L, "KEYCODE_TV" to 170L, "KEYCODE_WINDOW" to 171L, "KEYCODE_GUIDE" to 172L, "KEYCODE_DVR" to 173L, "KEYCODE_BOOKMARK" to 174L, "KEYCODE_CAPTIONS" to 175L, "KEYCODE_SETTINGS" to 176L, "KEYCODE_TV_POWER" to 177L, "KEYCODE_TV_INPUT" to 178L, "KEYCODE_STB_POWER" to 179L, "KEYCODE_STB_INPUT" to 180L, "KEYCODE_AVR_POWER" to 181L, "KEYCODE_AVR_INPUT" to 182L, "KEYCODE_PROG_GRED" to 183L, "KEYCODE_PROG_GREEN" to 184L, "KEYCODE_PROG_YELLOW" to 185L, "KEYCODE_PROG_BLUE" to 186L, "KEYCODE_APP_SWITCH" to 187L, "KEYCODE_BUTTON_1" to 188L, "KEYCODE_BUTTON_2" to 189L, "KEYCODE_BUTTON_3" to 190L, "KEYCODE_BUTTON_4" to 191L, "KEYCODE_BUTTON_5" to 192L, "KEYCODE_BUTTON_6" to 193L, "KEYCODE_BUTTON_7" to 194L, "KEYCODE_BUTTON_8" to 195L, "KEYCODE_BUTTON_9" to 196L, "KEYCODE_BUTTON_10" to 197L, "KEYCODE_BUTTON_11" to 198L, "KEYCODE_BUTTON_12" to 199L, "KEYCODE_BUTTON_13" to 200L, "KEYCODE_BUTTON_14" to 201L, "KEYCODE_BUTTON_15" to 202L, "KEYCODE_BUTTON_16" to 203L, "KEYCODE_LANGUAGE_SWITCH" to 204L, "KEYCODE_MANNER_MODE" to 205L, "KEYCODE_3D_MODE" to 206L, "KEYCODE_CONTACTS" to 207L, "KEYCODE_CALENDAR" to 208L, "KEYCODE_MUSIC" to 209L, "KEYCODE_CALCULATOR" to 210L, "KEYCODE_ZENKAKU_HANKAKU" to 211L, "KEYCODE_EISU" to 212L, "KEYCODE_MUHENKAN" to 213L, "KEYCODE_HENKAN" to 214L, "KEYCODE_KATAKANA_HIRAGANA" to 215L, "KEYCODE_YEN" to 216L, "KEYCODE_RO" to 217L, "KEYCODE_KANA" to 218L, "KEYCODE_ASSIST" to 219L, "KEYCODE_BRIGHTNESS_DOWN" to 220L, "KEYCODE_BRIGHTNESS_UP" to 221L, "KEYCODE_MEDIA_AUDIO_TRACK" to 222L, "KEYCODE_MEDIA_SLEEP" to 223L, "KEYCODE_MEDIA_WAKEUP" to 224L, "KEYCODE_PAIRING" to 225L, "KEYCODE_MEDIA_TOP_MENU" to 226L, "KEYCODE_11" to 227L, "KEYCODE_12" to 228L, "KEYCODE_LAST_CHANNEL" to 229L, "KEYCODE_TV_DATA_SERVICE" to 230L, "KEYCODE_VOICE_ASSIST" to 231L, "KEYCODE_TV_RADIO_SERVICE" to 232L, "KEYCODE_TV_TELETEXT" to 233L, "KEYCODE_TV_NUMBER_ENTRY" to 234L, "KEYCODE_TV_TERRESTRIAL_ANALOG" to 235L, "KEYCODE_TV_TERRESTRIAL_DIGITAL" to 236L, "KEYCODE_TV_SATELLITE" to 237L, "KEYCODE_TV_SATELLITE_BS" to 238L, "KEYCODE_TV_SATELLITE_CS" to 239L, "KEYCODE_TV_SATELLITE_SERVICE" to 240L, "KEYCODE_TV_NETWORK" to 241L, "KEYCODE_TV_ANTENNA_CABLE" to 242L, "KEYCODE_TV_INPUT_HDMI_1" to 243L, "KEYCODE_TV_INPUT_HDMI_2" to 244L, "KEYCODE_TV_INPUT_HDMI_3" to 245L, "KEYCODE_TV_INPUT_HDMI_4" to 246L, "KEYCODE_TV_INPUT_COMPOSITE_1" to 247L, "KEYCODE_TV_INPUT_COMPOSITE_2" to 248L, "KEYCODE_TV_INPUT_COMPONENT_1" to 249L, "KEYCODE_TV_INPUT_COMPONENT_2" to 250L, "KEYCODE_TV_INPUT_VGA_1" to 251L, "KEYCODE_TV_AUDIO_DESCRIPTION" to 252L, "KEYCODE_TV_AUDIO_DESCRIPTION_MIX_UP" to 253L, "KEYCODE_TV_AUDIO_DESCRIPTION_MIX_DOWN" to 254L, "KEYCODE_TV_ZOOM_MODE" to 255L, "KEYCODE_TV_CONTENTS_MENU" to 256L, "KEYCODE_TV_MEDIA_CONTEXT_MENU" to 257L, "KEYCODE_TV_TIMER_PROGRAMMING" to 258L, "KEYCODE_HELP" to 259L, "KEYCODE_NAVIGATE_PREVIOUS" to 260L, "KEYCODE_NAVIGATE_NEXT" to 261L, "KEYCODE_NAVIGATE_IN" to 262L, "KEYCODE_NAVIGATE_OUT" to 263L, "KEYCODE_STEM_PRIMARY" to 264L, "KEYCODE_STEM_1" to 265L, "KEYCODE_STEM_2" to 266L, "KEYCODE_STEM_3" to 267L, "KEYCODE_DPAD_UP_LEFT" to 268L, "KEYCODE_DPAD_DOWN_LEFT" to 269L, "KEYCODE_DPAD_UP_RIGHT" to 270L, "KEYCODE_DPAD_DOWN_RIGHT" to 271L, "KEYCODE_MEDIA_SKIP_FORWARD" to 272L, "KEYCODE_MEDIA_SKIP_BACKWARD" to 273L, "KEYCODE_MEDIA_STEP_FORWARD" to 274L, "KEYCODE_MEDIA_STEP_BACKWARD" to 275L, "KEYCODE_SOFT_SLEEP" to 276L, "KEYCODE_CUT" to 277L, "KEYCODE_COPY" to 278L, "KEYCODE_PASTE" to 279L, "KEYCODE_SYSTEM_NAVIGATION_UP" to 280L, "KEYCODE_SYSTEM_NAVIGATION_DOWN" to 281L, "KEYCODE_SYSTEM_NAVIGATION_LEFT" to 282L, "KEYCODE_SYSTEM_NAVIGATION_RIGHT" to 283L, "KEYCODE_ALL_APPS" to 284L, "KEYCODE_REFRESH" to 285L, "KEYCODE_THUMBS_UP" to 286L, "KEYCODE_THUMBS_DOWN" to 287L, "KEYCODE_PROFILE_SWITCH" to 288L))
        val __removed3 = Attr("__removed3")
        val __removed4 = Attr("__removed4")
        val __removed5 = Attr("__removed5")
        val __removed6 = Attr("__removed6")
        val layout_childType = Attr("layout_childType", "enum", mutableListOf("none" to 0L, "widget" to 1L, "challenge" to 2L, "userSwitcher" to 3L, "scrim" to 4L, "widgets" to 5L, "expandChallengeHandle" to 6L, "pageDeleteDropTarget" to 7L))
        val lockPatternStyle = Attr("lockPatternStyle", "reference")
        val autoSizePresetSizes = Attr("autoSizePresetSizes")
    }

    /** below are added manually **/

    object Resources2 {
        val name = Attr("name", "string")
        val type = Attr("type", "enum", arrayOf("transition" to 1, "animator" to 2, "anim" to 3, "interpolator" to 4, "style" to 5, "string" to 6, "array" to 7, "attr" to 8, "bool" to 9, "color" to 10, "dimen" to 11, "drawable" to 12, "font" to 13, "fraction" to 14, "id" to 15, "integer" to 16, "layout" to 17, "menu" to 18, "mipmap" to 19, "navigation" to 20, "plurals" to 21, "raw" to 22, "xml" to 23))
        val format = Attr("format", "enum", arrayOf("integer" to 1, "float" to 2, "reference" to 3, "fraction" to 4, "color" to 5, "enum" to 6, "string" to 7, "boolean" to 8, "dimension" to 9, "flags" to 10))
        val parent = Attr("parent", "string")
        val value = Attr("value", "int")
        val formatted = Attr("formatted", "bool")
        val translatable = Attr("translatable", "bool")
        val quantity = Attr("quantity", "enum", arrayOf("zero" to 1, "one" to 2, "two" to 3, "few" to 4, "many" to 5, "other" to 6))
    }

    object FreeRes {
        val name = Attr("name", "string")
    }

    object Color {
        val color = ColorStateListItem.color
        val alpha = ColorStateListItem.alpha
        val state_accelerated = Attr("state_accelerated", "bool")
        val state_activated = Attr("state_activated", "bool")
        val state_active = Attr("state_active", "bool")
        val state_checkable = Attr("state_checkable", "bool")
        val state_checked = Attr("state_checked", "bool")
        val state_drag_can_accept = Attr("state_drag_can_accept", "bool")
        val state_drag_hovered = Attr("state_drag_hovered", "bool")
        val state_enabled = Attr("state_enabled", "bool")
        val state_first = Attr("state_first", "bool")
        val state_focused = Attr("state_focused", "bool")
        val state_hovered = Attr("state_hovered", "bool")
        val state_last = Attr("state_last", "bool")
        val state_middle = Attr("state_middle", "bool")
        val state_pressed = Attr("state_pressed", "bool")
        val state_selected = Attr("state_selected", "bool")
        val state_single = Attr("state_single", "bool")
        val state_window_focused = Attr("state_window_focused", "bool")
    }

    object ObjectAnimator {
        val pathData = Attr("pathData", "string")
        val propertyName = Attr("propertyName", "string")
        val propertyXName = Attr("propertyXName", "string")
        val propertyYName = Attr("propertyYName", "string")
    }

    /** above are added manually **/

    /** below are androidx or android-compat **/

    object CoordinatorLayout {
        val keylines = Attr("keylines", "reference")
        val statusBarBackground = Attr("statusBarBackground", "color|reference")
    }

    object CoordinatorLayout_Layout {
        val layout_gravity = Resources.layout_gravity
        val layout_behavior = Attr("layout_behavior", "string")
        val layout_anchor = Attr("layout_anchor", "reference")
        val layout_keyline = Attr("layout_keyline", "integer")
        val layout_anchorGravity = Attr("layout_anchorGravity", "flag", mutableListOf("top" to 0x30L, "bottom" to 0x50L, "left" to 0x03L, "right" to 0x05L, "center_vertical" to 0x10L, "fill_vertical" to 0x70L, "center_horizontal" to 0x01L, "fill_horizontal" to 0x07L, "center" to 0x11L, "fill" to 0x77L, "clip_vertical" to 0x80L, "clip_horizontal" to 0x08L, "start" to 0x00800003L, "end" to 0x00800005L))
        val layout_insetEdge = Attr("layout_insetEdge", "enum", mutableListOf("none" to 0x0L, "top" to 0x30L, "bottom" to 0x50L, "left" to 0x03L, "right" to 0x05L, "start" to 0x00800003L, "end" to 0x00800005L))
        val layout_dodgeInsetEdges = Attr("layout_dodgeInsetEdges", "flag", mutableListOf("none" to 0x0L, "top" to 0x30L, "bottom" to 0x50L, "left" to 0x03L, "right" to 0x05L, "start" to 0x00800003L, "end" to 0x00800005L, "all" to 0x77L))
    }

    object ConstraintLayout_Layout {
        val minWidth = Attr("minWidth", "dimension")
        val minHeight = Attr("minHeight", "dimension")
        val maxWidth = Attr("maxWidth", "dimension")
        val maxHeight = Attr("maxHeight", "dimension")
        val layout_optimizationLevel = Attr("layout_optimizationLevel", "integer")
        val constraintSet = Attr("constraintSet", "reference")

        val orientation = Resources.orientation
        val barrierDirection = Attr("barrierDirection", "")
        val barrierAllowsGoneWidgets = Attr("barrierAllowsGoneWidgets", "")
        val constraint_referenced_ids = Attr("constraint_referenced_ids", "string")  // ConstraintHelper
        val chainUseRtl = Attr("chainUseRtl", "")

        val layout_constraintCircle = Attr("layout_constraintCircle", "reference|integer")
        val layout_constraintCircleRadius = Attr("layout_constraintCircleRadius", "dimension")
        val layout_constraintCircleAngle = Attr("layout_constraintCircleAngle", "float")

        val layout_constraintGuide_begin = Attr("layout_constraintGuide_begin", "dimension")
        val layout_constraintGuide_end = Attr("layout_constraintGuide_end", "dimension")
        val layout_constraintGuide_percent = Attr("layout_constraintGuide_percent", "float")

        val layout_constraintLeft_toLeftOf = Attr("layout_constraintLeft_toLeftOf", "reference|integer")
        val layout_constraintLeft_toRightOf = Attr("layout_constraintLeft_toRightOf", "reference|integer")
        val layout_constraintRight_toLeftOf = Attr("layout_constraintRight_toLeftOf", "reference|integer")
        val layout_constraintRight_toRightOf = Attr("layout_constraintRight_toRightOf", "reference|integer")
        val layout_constraintTop_toTopOf = Attr("layout_constraintTop_toTopOf", "reference|integer")
        val layout_constraintTop_toBottomOf = Attr("layout_constraintTop_toBottomOf", "reference|integer")
        val layout_constraintBottom_toTopOf = Attr("layout_constraintBottom_toTopOf", "reference|integer")
        val layout_constraintBottom_toBottomOf = Attr("layout_constraintBottom_toBottomOf", "reference|integer")
        val layout_constraintBaseline_toBaselineOf = Attr("layout_constraintBaseline_toBaselineOf", "reference|integer")
        val layout_constraintStart_toEndOf = Attr("layout_constraintStart_toEndOf", "reference|integer")
        val layout_constraintStart_toStartOf = Attr("layout_constraintStart_toStartOf", "reference|integer")
        val layout_constraintEnd_toStartOf = Attr("layout_constraintEnd_toStartOf", "reference|integer")
        val layout_constraintEnd_toEndOf = Attr("layout_constraintEnd_toEndOf", "reference|integer")

        val layout_goneMarginLeft = Attr("layout_goneMarginLeft", "dimension")
        val layout_goneMarginTop = Attr("layout_goneMarginTop", "dimension")
        val layout_goneMarginRight = Attr("layout_goneMarginRight", "dimension")
        val layout_goneMarginBottom = Attr("layout_goneMarginBottom", "dimension")
        val layout_goneMarginStart = Attr("layout_goneMarginStart", "dimension")
        val layout_goneMarginEnd = Attr("layout_goneMarginEnd", "dimension")

        val layout_constrainedWidth = Attr("layout_constrainedWidth", "boolean")
        val layout_constrainedHeight = Attr("layout_constrainedHeight", "boolean")
        val layout_constraintHorizontal_bias = Attr("layout_constraintHorizontal_bias", "float")
        val layout_constraintVertical_bias = Attr("layout_constraintVertical_bias", "float")
        val layout_constraintWidth_default = Attr("layout_constraintWidth_default", "int", mutableListOf("wrap" to 1L))
        val layout_constraintHeight_default = Attr("layout_constraintHeight_default", "int", mutableListOf("wrap" to 1L))
        val layout_constraintWidth_min = Attr("layout_constraintWidth_min", "dimension|integer")
        val layout_constraintWidth_max = Attr("layout_constraintWidth_max", "dimension|integer")
        val layout_constraintWidth_percent = Attr("layout_constraintWidth_percent", "float")
        val layout_constraintHeight_min = Attr("layout_constraintHeight_min", "dimension|integer")
        val layout_constraintHeight_max = Attr("layout_constraintHeight_max", "dimension|integer")
        val layout_constraintHeight_percent = Attr("layout_constraintHeight_percent", "float")

        val layout_constraintLeft_creator = Attr("layout_constraintLeft_creator", "")
        val layout_constraintTop_creator = Attr("layout_constraintTop_creator", "")
        val layout_constraintRight_creator = Attr("layout_constraintRight_creator", "")
        val layout_constraintBottom_creator = Attr("layout_constraintBottom_creator", "")
        val layout_constraintBaseline_creator = Attr("layout_constraintBaseline_creator", "")

        val layout_constraintDimensionRatio = Attr("layout_constraintDimensionRatio", "string")
        val layout_constraintHorizontal_weight = Attr("layout_constraintHorizontal_weight", "float")
        val layout_constraintVertical_weight = Attr("layout_constraintVertical_weight", "float")
        val layout_constraintHorizontal_chainStyle = Attr("layout_constraintHorizontal_chainStyle", "integer")
        val layout_constraintVertical_chainStyle = Attr("layout_constraintVertical_chainStyle", "integer")
        val layout_editor_absoluteX = Attr("layout_editor_absoluteX", "dimension")
        val layout_editor_absoluteY = Attr("layout_editor_absoluteY", "dimension")
    }

    object ConstraintLayout_placeholder {
        val emptyVisibility = Attr("emptyVisibility", "int")
        val content = Attr("content", "reference")
    }

    object ConstraintSet {
        val orientation = Attr("orientation", "")
        val id = Attr("id", "")
        val visibility = Attr("visibility", "")
        val alpha = Attr("alpha", "")
        val elevation = View.elevation
        val rotation = Attr("rotation", "")
        val rotationX = Attr("rotationX", "")
        val rotationY = Attr("rotationY", "")
        val scaleX = Attr("scaleX", "")
        val scaleY = Attr("scaleY", "")
        val transformPivotX = Attr("transformPivotX", "")
        val transformPivotY = Attr("transformPivotY", "")
        val translationX = Attr("translationX", "")
        val translationY = Attr("translationY", "")
        val translationZ = Attr("translationZ", "")
        val layout_width = Attr("layout_width", "")
        val layout_height = Attr("layout_height", "")
        val layout_marginStart = Attr("layout_marginStart", "")
        val layout_marginBottom = Attr("layout_marginBottom", "")
        val layout_marginTop = Attr("layout_marginTop", "")
        val layout_marginEnd = Attr("layout_marginEnd", "")
        val layout_marginLeft = Attr("layout_marginLeft", "")
        val layout_marginRight = Attr("layout_marginRight", "")
        val layout_constraintCircle = Attr("layout_constraintCircle", "")
        val layout_constraintCircleRadius = Attr("layout_constraintCircleRadius", "")
        val layout_constraintCircleAngle = Attr("layout_constraintCircleAngle", "")
        val layout_constraintGuide_begin = Attr("layout_constraintGuide_begin", "")
        val layout_constraintGuide_end = Attr("layout_constraintGuide_end", "")
        val layout_constraintGuide_percent = Attr("layout_constraintGuide_percent", "")
        val layout_constraintLeft_toLeftOf = Attr("layout_constraintLeft_toLeftOf", "")
        val layout_constraintLeft_toRightOf = Attr("layout_constraintLeft_toRightOf", "")
        val layout_constraintRight_toLeftOf = Attr("layout_constraintRight_toLeftOf", "")
        val layout_constraintRight_toRightOf = Attr("layout_constraintRight_toRightOf", "")
        val layout_constraintTop_toTopOf = Attr("layout_constraintTop_toTopOf", "")
        val layout_constraintTop_toBottomOf = Attr("layout_constraintTop_toBottomOf", "")
        val layout_constraintBottom_toTopOf = Attr("layout_constraintBottom_toTopOf", "")
        val layout_constraintBottom_toBottomOf = Attr("layout_constraintBottom_toBottomOf", "")
        val layout_constraintBaseline_toBaselineOf = Attr("layout_constraintBaseline_toBaselineOf", "")
        val layout_constraintStart_toEndOf = Attr("layout_constraintStart_toEndOf", "")
        val layout_constraintStart_toStartOf = Attr("layout_constraintStart_toStartOf", "")
        val layout_constraintEnd_toStartOf = Attr("layout_constraintEnd_toStartOf", "")
        val layout_constraintEnd_toEndOf = Attr("layout_constraintEnd_toEndOf", "")
        val layout_goneMarginLeft = Attr("layout_goneMarginLeft", "")
        val layout_goneMarginTop = Attr("layout_goneMarginTop", "")
        val layout_goneMarginRight = Attr("layout_goneMarginRight", "")
        val layout_goneMarginBottom = Attr("layout_goneMarginBottom", "")
        val layout_goneMarginStart = Attr("layout_goneMarginStart", "")
        val layout_goneMarginEnd = Attr("layout_goneMarginEnd", "")
        val layout_constrainedWidth = Attr("layout_constrainedWidth", "")
        val layout_constrainedHeight = Attr("layout_constrainedHeight", "")
        val layout_constraintHorizontal_bias = Attr("layout_constraintHorizontal_bias", "")
        val layout_constraintVertical_bias = Attr("layout_constraintVertical_bias", "")
        val layout_constraintWidth_default = Attr("layout_constraintWidth_default", "")
        val layout_constraintHeight_default = Attr("layout_constraintHeight_default", "")
        val layout_constraintWidth_min = Attr("layout_constraintWidth_min", "")
        val layout_constraintWidth_max = Attr("layout_constraintWidth_max", "")
        val layout_constraintWidth_percent = Attr("layout_constraintWidth_percent", "")
        val layout_constraintHeight_min = Attr("layout_constraintHeight_min", "")
        val layout_constraintHeight_max = Attr("layout_constraintHeight_max", "")
        val layout_constraintHeight_percent = Attr("layout_constraintHeight_percent", "")
        val layout_constraintLeft_creator = Attr("layout_constraintLeft_creator", "")
        val layout_constraintTop_creator = Attr("layout_constraintTop_creator", "")
        val layout_constraintRight_creator = Attr("layout_constraintRight_creator", "")
        val layout_constraintBottom_creator = Attr("layout_constraintBottom_creator", "")
        val layout_constraintBaseline_creator = Attr("layout_constraintBaseline_creator", "")
        val layout_constraintDimensionRatio = Attr("layout_constraintDimensionRatio", "")
        val layout_constraintHorizontal_weight = Attr("layout_constraintHorizontal_weight", "")
        val layout_constraintVertical_weight = Attr("layout_constraintVertical_weight", "")
        val layout_constraintHorizontal_chainStyle = Attr("layout_constraintHorizontal_chainStyle", "")
        val layout_constraintVertical_chainStyle = Attr("layout_constraintVertical_chainStyle", "")
        val layout_editor_absoluteX = Attr("layout_editor_absoluteX", "")
        val layout_editor_absoluteY = Attr("layout_editor_absoluteY", "")
        val barrierDirection = Attr("barrierDirection", "")
        val constraint_referenced_ids = Attr("constraint_referenced_ids", "")
        val maxHeight = Attr("maxHeight", "")
        val maxWidth = Attr("maxWidth", "")
        val minHeight = Attr("minHeight", "")
        val minWidth = Attr("minWidth", "")
        val barrierAllowsGoneWidgets = Attr("barrierAllowsGoneWidgets", "")
        val chainUseRtl = Attr("chainUseRtl", "")
    }

    object LinearConstraintLayout {
        val orientation = Attr("orientation", "")
    }

    object AppBarLayout {
        val elevation = View.elevation
        val background = Attr("background", "")
        val expanded = Attr("expanded", "boolean")
        val keyboardNavigationCluster = Attr("keyboardNavigationCluster", "")
        val touchscreenBlocksFocus = Attr("touchscreenBlocksFocus", "")
        val liftOnScroll = Attr("liftOnScroll", "boolean")
        val liftOnScrollTargetViewId = Attr("liftOnScrollTargetViewId", "reference")
        val statusBarForeground = Attr("statusBarForeground", "color")
    }

    object AppBarLayoutStates {
        val state_collapsed = Attr("state_collapsed", "boolean")
        val state_collapsible = Attr("state_collapsible", "boolean")
        val state_lifted = Attr("state_lifted", "boolean")
        val state_liftable = Attr("state_liftable", "boolean")
    }

    object AppBarLayout_Layout {
        val layout_scrollFlags = Attr("layout_scrollFlags", "flag", mutableListOf("noScroll" to 0x0L, "scroll" to 0x1L, "exitUntilCollapsed" to 0x2L, "enterAlways" to 0x4L, "enterAlwaysCollapsed" to 0x8L, "snap" to 0x10L, "snapMargins" to 0x20L))
        val layout_scrollInterpolator = Attr("layout_scrollInterpolator", "reference")
    }

    object Badge {
        val backgroundColor = Attr("backgroundColor", "color")
        val badgeTextColor = Attr("badgeTextColor", "color")
        val maxCharacterCount = Attr("maxCharacterCount", "integer")
        val number = Attr("number", "integer")
        val badgeGravity = Attr("badgeGravity", "enum", mutableListOf("TOP_END" to 8388661L, "TOP_START" to 8388659L, "BOTTOM_END" to 8388693L, "BOTTOM_START" to 8388691L))
        val horizontalOffset = Attr("horizontalOffset", "dimension")
        val verticalOffset = Attr("verticalOffset", "dimension")
    }

    object BottomAppBar {
        val backgroundTint = Attr("backgroundTint", "")
        val elevation = View.elevation
        val fabAlignmentMode = Attr("fabAlignmentMode", "enum", mutableListOf("center" to 0L, "end" to 1L))
        val fabAnimationMode = Attr("fabAnimationMode", "enum", mutableListOf("scale" to 0L, "slide" to 1L))
        val fabCradleMargin = Attr("fabCradleMargin", "dimension")
        val fabCradleRoundedCornerRadius = Attr("fabCradleRoundedCornerRadius", "dimension")
        val fabCradleVerticalOffset = Attr("fabCradleVerticalOffset", "dimension")
        val hideOnScroll = Attr("hideOnScroll", "boolean")
        val paddingBottomSystemWindowInsets = Attr("paddingBottomSystemWindowInsets", "")
        val paddingLeftSystemWindowInsets = Attr("paddingLeftSystemWindowInsets", "")
        val paddingRightSystemWindowInsets = Attr("paddingRightSystemWindowInsets", "")
    }

    object BottomNavigationView {
        val backgroundTint = Attr("backgroundTint", "")
        val menu = Attr("menu", "")
        val labelVisibilityMode = Attr("labelVisibilityMode", "enum", mutableListOf("auto" to -1L, "selected" to 0L, "labeled" to 1L, "unlabeled" to 2L))
        val itemBackground = Attr("itemBackground", "")
        val itemRippleColor = Attr("itemRippleColor", "color")
        val itemIconSize = Attr("itemIconSize", "")
        val itemIconTint = Attr("itemIconTint", "")
        val itemTextAppearanceInactive = Attr("itemTextAppearanceInactive", "reference")
        val itemTextAppearanceActive = Attr("itemTextAppearanceActive", "reference")
        val itemTextColor = Attr("itemTextColor", "")
        val itemHorizontalTranslationEnabled = Attr("itemHorizontalTranslationEnabled", "boolean")
        val elevation = View.elevation
    }

    object BottomSheetBehavior_Layout {
        val behavior_peekHeight = Attr("behavior_peekHeight", "dimension", mutableListOf("auto" to -1L))
        val behavior_hideable = Attr("behavior_hideable", "boolean")
        val behavior_skipCollapsed = Attr("behavior_skipCollapsed", "boolean")
        val behavior_fitToContents = Attr("behavior_fitToContents", "boolean")
        val behavior_draggable = Attr("behavior_draggable", "boolean")
        val behavior_halfExpandedRatio = Attr("behavior_halfExpandedRatio", "reference|float")
        val behavior_expandedOffset = Attr("behavior_expandedOffset", "reference|integer")
        val shapeAppearance = Attr("shapeAppearance", "")
        val shapeAppearanceOverlay = Attr("shapeAppearanceOverlay", "")
        val backgroundTint = Attr("backgroundTint", "")
        val behavior_saveFlags = Attr("behavior_saveFlags", "flag", mutableListOf("peekHeight" to 0x1L, "fitToContents" to 0x2L, "hideable" to 0x4L, "skipCollapsed" to 0x8L, "all" to -1L, "none" to 0L))
        val elevation = View.elevation
    }

    object Chip {
        val chipSurfaceColor = Attr("chipSurfaceColor", "color")
        val chipBackgroundColor = Attr("chipBackgroundColor", "color")
        val chipMinHeight = Attr("chipMinHeight", "dimension")
        val chipCornerRadius = Attr("chipCornerRadius", "dimension")
        val chipStrokeColor = Attr("chipStrokeColor", "color")
        val chipStrokeWidth = Attr("chipStrokeWidth", "dimension")
        val rippleColor = Attr("rippleColor", "")
        val chipMinTouchTargetSize = Attr("chipMinTouchTargetSize", "dimension")
        val ensureMinTouchTargetSize = Attr("ensureMinTouchTargetSize", "")
        val text = Attr("text", "")
        val textColor = Attr("textColor", "")
        val textAppearance = Attr("textAppearance", "")
        val ellipsize = Attr("ellipsize", "")
        val maxWidth = Attr("maxWidth", "")
        val chipIconVisible = Attr("chipIconVisible", "boolean")
        val chipIconEnabled = Attr("chipIconEnabled", "boolean")
        val chipIcon = Attr("chipIcon", "reference")
        val chipIconTint = Attr("chipIconTint", "color")
        val chipIconSize = Attr("chipIconSize", "dimension")
        val closeIconVisible = Attr("closeIconVisible", "boolean")
        val closeIconEnabled = Attr("closeIconEnabled", "boolean")
        val closeIcon = Attr("closeIcon", "reference")
        val closeIconTint = Attr("closeIconTint", "color")
        val closeIconSize = Attr("closeIconSize", "dimension")
        val checkable = Attr("checkable", "")
        val checkedIconVisible = Attr("checkedIconVisible", "boolean")
        val checkedIconEnabled = Attr("checkedIconEnabled", "boolean")
        val checkedIcon = Attr("checkedIcon", "")
        val showMotionSpec = Attr("showMotionSpec", "")
        val hideMotionSpec = Attr("hideMotionSpec", "")
        val shapeAppearance = Attr("shapeAppearance", "")
        val shapeAppearanceOverlay = Attr("shapeAppearanceOverlay", "")
        val chipStartPadding = Attr("chipStartPadding", "dimension")
        val iconStartPadding = Attr("iconStartPadding", "dimension")
        val iconEndPadding = Attr("iconEndPadding", "dimension")
        val textStartPadding = Attr("textStartPadding", "dimension")
        val textEndPadding = Attr("textEndPadding", "dimension")
        val closeIconStartPadding = Attr("closeIconStartPadding", "dimension")
        val closeIconEndPadding = Attr("closeIconEndPadding", "dimension")
        val chipEndPadding = Attr("chipEndPadding", "dimension")
    }

    object ChipGroup {
        val chipSpacing = Attr("chipSpacing", "dimension")
        val chipSpacingHorizontal = Attr("chipSpacingHorizontal", "dimension")
        val chipSpacingVertical = Attr("chipSpacingVertical", "dimension")
        val singleLine = Attr("singleLine", "boolean")
        val singleSelection = Attr("singleSelection", "")
        val selectionRequired = Attr("selectionRequired", "")
        val checkedChip = Attr("checkedChip", "reference")
    }

    object CollapsingToolbarLayout {
        val expandedTitleMargin = Attr("expandedTitleMargin", "dimension")
        val expandedTitleMarginStart = Attr("expandedTitleMarginStart", "dimension")
        val expandedTitleMarginTop = Attr("expandedTitleMarginTop", "dimension")
        val expandedTitleMarginEnd = Attr("expandedTitleMarginEnd", "dimension")
        val expandedTitleMarginBottom = Attr("expandedTitleMarginBottom", "dimension")
        val expandedTitleTextAppearance = Attr("expandedTitleTextAppearance", "reference")
        val collapsedTitleTextAppearance = Attr("collapsedTitleTextAppearance", "reference")
        val contentScrim = Attr("contentScrim", "color")
        val statusBarScrim = Attr("statusBarScrim", "color")
        val toolbarId = Attr("toolbarId", "reference")
        val scrimVisibleHeightTrigger = Attr("scrimVisibleHeightTrigger", "dimension")
        val scrimAnimationDuration = Attr("scrimAnimationDuration", "integer")
        val collapsedTitleGravity = Attr("collapsedTitleGravity", "flag", mutableListOf("top" to 0x30L, "bottom" to 0x50L, "left" to 0x03L, "right" to 0x05L, "center_vertical" to 0x10L, "fill_vertical" to 0x70L, "center_horizontal" to 0x01L, "center" to 0x11L, "start" to 0x00800003L, "end" to 0x00800005L))
        val expandedTitleGravity = Attr("expandedTitleGravity", "flag", mutableListOf("top" to 0x30L, "bottom" to 0x50L, "left" to 0x03L, "right" to 0x05L, "center_vertical" to 0x10L, "fill_vertical" to 0x70L, "center_horizontal" to 0x01L, "center" to 0x11L, "start" to 0x00800003L, "end" to 0x00800005L))
        val titleEnabled = Attr("titleEnabled", "boolean")
        val title = Attr("title", "")
    }

    object CollapsingToolbarLayout_Layout {
        val layout_collapseMode = Attr("layout_collapseMode", "enum", mutableListOf("none" to 0L, "pin" to 1L, "parallax" to 2L))
        val layout_collapseParallaxMultiplier = Attr("layout_collapseParallaxMultiplier", "float")
    }

    object ExtendedFloatingActionButton {
        val elevation = View.elevation
        val showMotionSpec = Attr("showMotionSpec", "")
        val hideMotionSpec = Attr("hideMotionSpec", "")
        val extendMotionSpec = Attr("extendMotionSpec", "reference")
        val shrinkMotionSpec = Attr("shrinkMotionSpec", "reference")
    }

    object ExtendedFloatingActionButton_Behavior_Layout {
        val behavior_autoHide = Attr("behavior_autoHide", "")
        val behavior_autoShrink = Attr("behavior_autoShrink", "boolean")
    }

    object FloatingActionButton {
        val rippleColor = Attr("rippleColor", "color|reference")
        val fabSize = Attr("fabSize", "enum", mutableListOf("auto" to -1L, "normal" to 0L, "mini" to 1L))
        val fabCustomSize = Attr("fabCustomSize", "dimension")
        val elevation = View.elevation
        val ensureMinTouchTargetSize = Attr("ensureMinTouchTargetSize", "boolean")
        val hoveredFocusedTranslationZ = Attr("hoveredFocusedTranslationZ", "dimension")
        val pressedTranslationZ = Attr("pressedTranslationZ", "dimension")
        val borderWidth = Attr("borderWidth", "dimension")
        val useCompatPadding = Attr("useCompatPadding", "boolean")
        val maxImageSize = Attr("maxImageSize", "dimension")
        val showMotionSpec = Attr("showMotionSpec", "reference")
        val hideMotionSpec = Attr("hideMotionSpec", "reference")
        val shapeAppearance = MaterialShape.shapeAppearance
        val shapeAppearanceOverlay = MaterialShape.shapeAppearanceOverlay
    }

    object FloatingActionButton_Behavior_Layout {
        val behavior_autoHide = Attr("behavior_autoHide", "boolean")
    }

    object FlowLayout {
        val itemSpacing = Attr("itemSpacing", "dimension")
        val lineSpacing = Attr("lineSpacing", "dimension")
    }

    object ForegroundLinearLayout {
        val foreground = Attr("foreground", "")
        val foregroundGravity = Attr("foregroundGravity", "")
        val foregroundInsidePadding = Attr("foregroundInsidePadding", "boolean")
    }

    object Insets {
        val paddingBottomSystemWindowInsets = Attr("paddingBottomSystemWindowInsets", "boolean")
        val paddingLeftSystemWindowInsets = Attr("paddingLeftSystemWindowInsets", "boolean")
        val paddingRightSystemWindowInsets = Attr("paddingRightSystemWindowInsets", "boolean")
    }

    object MaterialAlertDialog {
        val backgroundInsetStart = Attr("backgroundInsetStart", "dimension")
        val backgroundInsetTop = Attr("backgroundInsetTop", "dimension")
        val backgroundInsetEnd = Attr("backgroundInsetEnd", "dimension")
        val backgroundInsetBottom = Attr("backgroundInsetBottom", "dimension")
    }

    object MaterialAlertDialogTheme {
        val materialAlertDialogTheme = Attr("materialAlertDialogTheme", "reference")
        val materialAlertDialogTitlePanelStyle = Attr("materialAlertDialogTitlePanelStyle", "reference")
        val materialAlertDialogTitleIconStyle = Attr("materialAlertDialogTitleIconStyle", "reference")
        val materialAlertDialogTitleTextStyle = Attr("materialAlertDialogTitleTextStyle", "reference")
        val materialAlertDialogBodyTextStyle = Attr("materialAlertDialogBodyTextStyle", "reference")
    }

    object MaterialButton {
        val checkable = Attr("checkable", "")
        val insetLeft = Attr("insetLeft", "")
        val insetRight = Attr("insetRight", "")
        val insetTop = Attr("insetTop", "")
        val insetBottom = Attr("insetBottom", "")
        val backgroundTint = Attr("backgroundTint", "")
        val backgroundTintMode = Attr("backgroundTintMode", "")
        val elevation = View.elevation
        val icon = Attr("icon", "reference")
        val iconSize = Attr("iconSize", "dimension")
        val iconPadding = Attr("iconPadding", "dimension")
        val iconGravity = Attr("iconGravity", "flag", mutableListOf("start" to 0x1L, "textStart" to 0x2L, "end" to 0x3L, "textEnd" to 0x4L))
        val iconTint = Attr("iconTint", "color")
        val iconTintMode = Attr("iconTintMode", "")
        val shapeAppearance = Attr("shapeAppearance", "")
        val shapeAppearanceOverlay = Attr("shapeAppearanceOverlay", "")
        val strokeColor = Attr("strokeColor", "")
        val strokeWidth = Attr("strokeWidth", "")
        val cornerRadius = Attr("cornerRadius", "dimension")
        val rippleColor = Attr("rippleColor", "")
    }

    object MaterialButtonToggleGroup {
        val singleSelection = Attr("singleSelection", "")
        val selectionRequired = Attr("selectionRequired", "")
        val checkedButton = Attr("checkedButton", "reference")
    }

    object MaterialCalendar {
        val windowFullscreen = Attr("windowFullscreen", "")
        val dayStyle = Attr("dayStyle", "reference")
        val dayInvalidStyle = Attr("dayInvalidStyle", "reference")
        val daySelectedStyle = Attr("daySelectedStyle", "reference")
        val dayTodayStyle = Attr("dayTodayStyle", "reference")
        val yearStyle = Attr("yearStyle", "reference")
        val yearSelectedStyle = Attr("yearSelectedStyle", "reference")
        val yearTodayStyle = Attr("yearTodayStyle", "reference")
        val rangeFillColor = Attr("rangeFillColor", "color")
    }

    object MaterialCalendarItem {
        val insetLeft = Attr("insetLeft", "")
        val insetTop = Attr("insetTop", "")
        val insetRight = Attr("insetRight", "")
        val insetBottom = Attr("insetBottom", "")
        val itemFillColor = Attr("itemFillColor", "color")
        val itemTextColor = Attr("itemTextColor", "")
        val itemStrokeColor = Attr("itemStrokeColor", "color")
        val itemStrokeWidth = Attr("itemStrokeWidth", "dimension")
        val itemShapeAppearance = Attr("itemShapeAppearance", "")
        val itemShapeAppearanceOverlay = Attr("itemShapeAppearanceOverlay", "")
    }

    object MaterialCardView {
        val checkable = Attr("checkable", "")
        val cardForegroundColor = Attr("cardForegroundColor", "color")
        val checkedIcon = Attr("checkedIcon", "")
        val checkedIconTint = Attr("checkedIconTint", "color")
        val rippleColor = Attr("rippleColor", "")
        val state_dragged = Attr("state_dragged", "boolean")
        val strokeColor = Attr("strokeColor", "")
        val strokeWidth = Attr("strokeWidth", "")
        val shapeAppearance = Attr("shapeAppearance", "")
        val shapeAppearanceOverlay = Attr("shapeAppearanceOverlay", "")
    }

    object MaterialCheckBox {
        val useMaterialThemeColors = Attr("useMaterialThemeColors", "")
        val buttonTint = Attr("buttonTint", "")
    }

    object MaterialRadioButton {
        val useMaterialThemeColors = Attr("useMaterialThemeColors", "")
    }

    object MaterialShape {
        val shapeAppearance = Attr("shapeAppearance", "reference")
        val shapeAppearanceOverlay = Attr("shapeAppearanceOverlay", "reference")
    }

    object MaterialTextAppearance {
        val lineHeight = Attr("lineHeight", "")
    }

    object MaterialTextView {
        val textAppearance = Attr("textAppearance", "")
        val lineHeight = Attr("lineHeight", "")
    }

    object NavigationView {
        val background = Attr("background", "")
        val fitsSystemWindows = Attr("fitsSystemWindows", "")
        val maxWidth = Attr("maxWidth", "")
        val elevation = View.elevation
        val menu = Attr("menu", "reference")
        val itemIconTint = Attr("itemIconTint", "color")
        val itemTextColor = Attr("itemTextColor", "")
        val itemBackground = Attr("itemBackground", "reference")
        val itemTextAppearance = Attr("itemTextAppearance", "reference")
        val headerLayout = Attr("headerLayout", "reference")
        val itemHorizontalPadding = Attr("itemHorizontalPadding", "dimension")
        val itemIconPadding = Attr("itemIconPadding", "dimension")
        val itemIconSize = Attr("itemIconSize", "dimension")
        val itemMaxLines = Attr("itemMaxLines", "integer")
        val itemShapeAppearance = Attr("itemShapeAppearance", "")
        val itemShapeAppearanceOverlay = Attr("itemShapeAppearanceOverlay", "")
        val itemShapeInsetStart = Attr("itemShapeInsetStart", "dimension")
        val itemShapeInsetTop = Attr("itemShapeInsetTop", "dimension")
        val itemShapeInsetEnd = Attr("itemShapeInsetEnd", "dimension")
        val itemShapeInsetBottom = Attr("itemShapeInsetBottom", "dimension")
        val itemShapeFillColor = Attr("itemShapeFillColor", "color")
    }

    object ScrimInsetsFrameLayout {
        val insetForeground = Attr("insetForeground", "color|reference")
    }

    object ScrollingViewBehavior_Layout {
        val behavior_overlapTop = Attr("behavior_overlapTop", "dimension")
    }

    object ShapeAppearance {
        val cornerSize = Attr("cornerSize", "dimension|fraction")
        val cornerSizeTopLeft = Attr("cornerSizeTopLeft", "dimension|fraction")
        val cornerSizeTopRight = Attr("cornerSizeTopRight", "dimension|fraction")
        val cornerSizeBottomRight = Attr("cornerSizeBottomRight", "dimension|fraction")
        val cornerSizeBottomLeft = Attr("cornerSizeBottomLeft", "dimension|fraction")
        val cornerFamily = Attr("cornerFamily", "enum", mutableListOf("rounded" to 0L, "cut" to 1L))
        val cornerFamilyTopLeft = Attr("cornerFamilyTopLeft", "enum", mutableListOf("rounded" to 0L, "cut" to 1L))
        val cornerFamilyTopRight = Attr("cornerFamilyTopRight", "enum", mutableListOf("rounded" to 0L, "cut" to 1L))
        val cornerFamilyBottomRight = Attr("cornerFamilyBottomRight", "enum", mutableListOf("rounded" to 0L, "cut" to 1L))
        val cornerFamilyBottomLeft = Attr("cornerFamilyBottomLeft", "enum", mutableListOf("rounded" to 0L, "cut" to 1L))
    }

    object ShapeableImageView {
        val strokeWidth = Attr("strokeWidth", "")
        val strokeColor = Attr("strokeColor", "")
    }

    object Slider {
        val value = Attr("value", "")
        val valueFrom = Attr("valueFrom", "")
        val valueTo = Attr("valueTo", "")
        val stepSize = Attr("stepSize", "")
        val haloColor = Attr("haloColor", "color")
        val haloRadius = Attr("haloRadius", "dimension")
        val labelBehavior = Attr("labelBehavior", "enum", mutableListOf("floating" to 0L, "withinBounds" to 1L, "gone" to 2L))
        val labelStyle = Attr("labelStyle", "reference")
        val thumbColor = Attr("thumbColor", "color")
        val thumbElevation = Attr("thumbElevation", "dimension")
        val thumbRadius = Attr("thumbRadius", "dimension")
        val tickColor = Attr("tickColor", "color")
        val tickColorActive = Attr("tickColorActive", "color")
        val tickColorInactive = Attr("tickColorInactive", "color")
        val trackColor = Attr("trackColor", "color")
        val trackColorActive = Attr("trackColorActive", "color")
        val trackColorInactive = Attr("trackColorInactive", "color")
        val trackHeight = Attr("trackHeight", "dimension")
    }

    object Snackbar {
        val snackbarStyle = Attr("snackbarStyle", "reference")
        val snackbarButtonStyle = Attr("snackbarButtonStyle", "reference")
        val snackbarTextViewStyle = Attr("snackbarTextViewStyle", "reference")
    }

    object SnackbarLayout {
        val maxWidth = Attr("maxWidth", "")
        val elevation = View.elevation
        val maxActionInlineWidth = Attr("maxActionInlineWidth", "dimension")
        val animationMode = Attr("animationMode", "enum", mutableListOf("slide" to 0L, "fade" to 1L))
        val backgroundOverlayColorAlpha = Attr("backgroundOverlayColorAlpha", "float")
        val backgroundTint = Attr("backgroundTint", "")
        val backgroundTintMode = Attr("backgroundTintMode", "")
        val actionTextColorAlpha = Attr("actionTextColorAlpha", "float")
    }

    object SwitchMaterial {
        val useMaterialThemeColors = Attr("useMaterialThemeColors", "")
    }

    object TabItem {
        val text = Attr("text", "")
        val icon = Attr("icon", "")
        val layout = Attr("layout", "")
    }

    object TabLayout {
        val tabIndicatorColor = Attr("tabIndicatorColor", "color")
        val tabIndicatorHeight = Attr("tabIndicatorHeight", "dimension")
        val tabContentStart = Attr("tabContentStart", "dimension")
        val tabBackground = Attr("tabBackground", "reference")
        val tabIndicator = Attr("tabIndicator", "reference")
        val tabIndicatorGravity = Attr("tabIndicatorGravity", "enum", mutableListOf("bottom" to 0L, "center" to 1L, "top" to 2L, "stretch" to 3L))
        val tabIndicatorAnimationDuration = Attr("tabIndicatorAnimationDuration", "integer")
        val tabIndicatorFullWidth = Attr("tabIndicatorFullWidth", "boolean")
        val tabMode = Attr("tabMode", "enum", mutableListOf("scrollable" to 0L, "fixed" to 1L, "auto" to 2L))
        val tabGravity = Attr("tabGravity", "enum", mutableListOf("fill" to 0L, "center" to 1L, "start" to 2L))
        val tabInlineLabel = Attr("tabInlineLabel", "boolean")
        val tabMinWidth = Attr("tabMinWidth", "dimension")
        val tabMaxWidth = Attr("tabMaxWidth", "dimension")
        val tabTextAppearance = Attr("tabTextAppearance", "reference")
        val tabTextColor = Attr("tabTextColor", "color")
        val tabSelectedTextColor = Attr("tabSelectedTextColor", "color")
        val tabPaddingStart = Attr("tabPaddingStart", "dimension")
        val tabPaddingTop = Attr("tabPaddingTop", "dimension")
        val tabPaddingEnd = Attr("tabPaddingEnd", "dimension")
        val tabPaddingBottom = Attr("tabPaddingBottom", "dimension")
        val tabPadding = Attr("tabPadding", "dimension")
        val tabIconTint = Attr("tabIconTint", "color")
        val tabIconTintMode = Attr("tabIconTintMode", "enum", mutableListOf("src_over" to 3L, "src_in" to 5L, "src_atop" to 9L, "multiply" to 14L, "screen" to 15L, "add" to 16L))
        val tabRippleColor = Attr("tabRippleColor", "color")
        val tabUnboundedRipple = Attr("tabUnboundedRipple", "boolean")
    }

    object TextInputLayout {
        val enabled = Attr("enabled", "")
        val hint = Attr("hint", "")
        val textColorHint = Attr("textColorHint", "")
        val hintEnabled = Attr("hintEnabled", "boolean")
        val hintAnimationEnabled = Attr("hintAnimationEnabled", "boolean")
        val hintTextAppearance = Attr("hintTextAppearance", "reference")
        val hintTextColor = Attr("hintTextColor", "color")
        val helperText = Attr("helperText", "string")
        val helperTextEnabled = Attr("helperTextEnabled", "boolean")
        val helperTextTextAppearance = Attr("helperTextTextAppearance", "reference")
        val helperTextTextColor = Attr("helperTextTextColor", "color")
        val errorEnabled = Attr("errorEnabled", "boolean")
        val errorTextAppearance = Attr("errorTextAppearance", "reference")
        val errorTextColor = Attr("errorTextColor", "color")
        val errorContentDescription = Attr("errorContentDescription", "string")
        val errorIconDrawable = Attr("errorIconDrawable", "reference")
        val errorIconTint = Attr("errorIconTint", "reference")
        val errorIconTintMode = Attr("errorIconTintMode", "enum", mutableListOf("src_over" to 3L, "src_in" to 5L, "src_atop" to 9L, "multiply" to 14L, "screen" to 15L))
        val counterEnabled = Attr("counterEnabled", "boolean")
        val counterMaxLength = Attr("counterMaxLength", "integer")
        val counterTextAppearance = Attr("counterTextAppearance", "reference")
        val counterTextColor = Attr("counterTextColor", "reference")
        val counterOverflowTextAppearance = Attr("counterOverflowTextAppearance", "reference")
        val counterOverflowTextColor = Attr("counterOverflowTextColor", "reference")
        val placeholderText = Attr("placeholderText", "string")
        val placeholderTextAppearance = Attr("placeholderTextAppearance", "reference")
        val placeholderTextColor = Attr("placeholderTextColor", "color")
        val prefixText = Attr("prefixText", "string")
        val prefixTextAppearance = Attr("prefixTextAppearance", "reference")
        val prefixTextColor = Attr("prefixTextColor", "color")
        val suffixText = Attr("suffixText", "string")
        val suffixTextAppearance = Attr("suffixTextAppearance", "reference")
        val suffixTextColor = Attr("suffixTextColor", "color")
        val startIconDrawable = Attr("startIconDrawable", "reference")
        val startIconContentDescription = Attr("startIconContentDescription", "string")
        val startIconCheckable = Attr("startIconCheckable", "boolean")
        val startIconTint = Attr("startIconTint", "color")
        val startIconTintMode = Attr("startIconTintMode", "enum", mutableListOf("src_over" to 3L, "src_in" to 5L, "src_atop" to 9L, "multiply" to 14L, "screen" to 15L))
        val endIconMode = Attr("endIconMode", "enum", mutableListOf("custom" to -1L, "none" to 0L, "password_toggle" to 1L, "clear_text" to 2L, "dropdown_menu" to 3L))
        val endIconDrawable = Attr("endIconDrawable", "reference")
        val endIconContentDescription = Attr("endIconContentDescription", "string")
        val endIconCheckable = Attr("endIconCheckable", "boolean")
        val endIconTint = Attr("endIconTint", "color")
        val endIconTintMode = Attr("endIconTintMode", "enum", mutableListOf("src_over" to 3L, "src_in" to 5L, "src_atop" to 9L, "multiply" to 14L, "screen" to 15L))
        val boxBackgroundMode = Attr("boxBackgroundMode", "enum", mutableListOf("none" to 0L, "filled" to 1L, "outline" to 2L))
        val boxCollapsedPaddingTop = Attr("boxCollapsedPaddingTop", "dimension")
        val boxCornerRadiusTopStart = Attr("boxCornerRadiusTopStart", "dimension")
        val boxCornerRadiusTopEnd = Attr("boxCornerRadiusTopEnd", "dimension")
        val boxCornerRadiusBottomStart = Attr("boxCornerRadiusBottomStart", "dimension")
        val boxCornerRadiusBottomEnd = Attr("boxCornerRadiusBottomEnd", "dimension")
        val boxStrokeColor = Attr("boxStrokeColor", "color")
        val boxStrokeErrorColor = Attr("boxStrokeErrorColor", "color")
        val boxBackgroundColor = Attr("boxBackgroundColor", "color")
        val boxStrokeWidth = Attr("boxStrokeWidth", "dimension")
        val boxStrokeWidthFocused = Attr("boxStrokeWidthFocused", "dimension")
        val shapeAppearance = Attr("shapeAppearance", "")
        val shapeAppearanceOverlay = Attr("shapeAppearanceOverlay", "")
        val passwordToggleEnabled = Attr("passwordToggleEnabled", "boolean")
        val passwordToggleDrawable = Attr("passwordToggleDrawable", "reference")
        val passwordToggleContentDescription = Attr("passwordToggleContentDescription", "string")
        val passwordToggleTint = Attr("passwordToggleTint", "color")
        val passwordToggleTintMode = Attr("passwordToggleTintMode", "enum", mutableListOf("src_over" to 3L, "src_in" to 5L, "src_atop" to 9L, "multiply" to 14L, "screen" to 15L))
    }

    object ThemeEnforcement {
        val enforceMaterialTheme = Attr("enforceMaterialTheme", "boolean")
        val enforceTextAppearance = Attr("enforceTextAppearance", "boolean")
        val textAppearance = Attr("textAppearance", "")
    }

    object Tooltip {
        val text = Attr("text", "")
        val textAppearance = Attr("textAppearance", "")
        val layout_margin = Attr("layout_margin", "")
        val minWidth = Attr("minWidth", "")
        val minHeight = Attr("minHeight", "")
        val padding = Attr("padding", "")
        val backgroundTint = Attr("backgroundTint", "color")
    }
}

/**
 * import xml.dom.minidom as mnd
 * import sys
 *
 * file = mnd.parse("attrs.xml")  # D:\xxx\xxx\xxx\Android\Sdk\platforms\android-29\data\res\values\attrs.xml
 *
 * outf = open("attrs.kt", "w", encoding='utf-8')
 * old = sys.stdout
 * sys.stdout = outf
 *
 * print("// format的可能的值：'dimension', 'fraction', 'reference', 'color', 'string', 'boolean', 'integer', 'float', 'enum', 'flags'，后面两个是重点\n" +
 *       "data class Attr(val name: kotlin.String, val format: kotlin.String, val values: kotlin.MutableMap<kotlin.String, kotlin.Int>? = null)\n")
 * print("class Attributes2 {")
 *
 * for ds in file.getElementsByTagName("declare-styleable"):
 *     print("    object " + ds.getAttribute("name") + " {")
 *     for attr in ds.getElementsByTagName("attr"):
 *         name = attr.getAttribute("name")
 *         format = attr.getAttribute("format")
 *         subEnums = attr.getElementsByTagName("enum")
 *         subFlags = attr.getElementsByTagName("flag")
 *         if format == '':
 *             if subEnums.length > 0 and subFlags.length > 0:
 *                 format = 'enum|flag'
 *             elif subFlags.length > 0:
 *                 format = 'flag'
 *             elif subEnums.length > 0:
 *                 format = 'enum'
 *         if subEnums.length > 0 or subFlags.length > 0:
 *             first = True
 *             items = '        val {} = Attr("{}", "{}", mutableMapOf('.format(
 *                 name, name, format)
 *             for enum in subEnums:
 *                 value = enum.getAttribute("value")
 *                 name = enum.getAttribute("name")
 *                 if value.startswith('0x') and int(value, 16) <= 2147483647 or value[0].isdigit() and int(value) <= 2147483647:
 *                     value = value + 'L'
 *                 if first:
 *                     items = items + '"{}" to {}'.format(name, value)
 *                     first = False
 *                 else:
 *                     items = items + ', "{}" to {}'.format(name, value)
 *             for flag in subFlags:
 *                 value = flag.getAttribute("value")
 *                 name = flag.getAttribute("name")
 *                 if value.startswith('0x') and int(value, 16) <= 2147483647 or value[0].isdigit() and int(value) <= 2147483647:
 *                     value = value + 'L'
 *                 if first:
 *                     items = items + '"{}" to {}'.format(name, value)
 *                     first = False
 *                 else:
 *                     items = items + ', "{}" to {}'.format(name, value)
 *             print(items + '))')
 *         else:
 *             print('        val {} = Attr("{}", "{}")'.format(name, name, format))
 *     print("    }\n")
 *
 * print("}")
 *
 * sys.stdout = old
 * outf.close()
 *
 * # 注意，使用该代码生成该文件后，又使用正则替换对该文件进行了一些修改，即该代码生成对文件并非最终版本。
 */

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
 * insert
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

/**
 * layout @[package:]layout/filename
 */

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

/**
 * font @[package:]font/font_name
 * <font-family>
 *     <font
 *         android:font="@[package:]font/font_to_include"
 *         android:fontStyle=["normal" | "italic"]
 *         android:fontWeight="weight_value" />
 * </font-family>
 */

/**
 * android.graphics.drawable.DrawableInflater
 * private Drawable inflateFromTag(@NonNull String name) {
 *     switch (name) {
 *         case "selector":
 *             return new StateListDrawable();
 *         case "animated-selector":
 *             return new AnimatedStateListDrawable();
 *         case "level-list":
 *             return new LevelListDrawable();
 *         case "layer-list":
 *             return new LayerDrawable();
 *         case "transition":
 *             return new TransitionDrawable();
 *         case "ripple":
 *             return new RippleDrawable();
 *         case "adaptive-icon":
 *             return new AdaptiveIconDrawable();
 *         case "color":
 *             return new ColorDrawable();
 *         case "shape":
 *             return new GradientDrawable();
 *         case "vector":
 *             return new VectorDrawable();
 *         case "animated-vector":
 *             return new AnimatedVectorDrawable();
 *         case "scale":
 *             return new ScaleDrawable();
 *         case "clip":
 *             return new ClipDrawable();
 *         case "rotate":
 *             return new RotateDrawable();
 *         case "animated-rotate":
 *             return new AnimatedRotateDrawable();
 *         case "animation-list":
 *             return new AnimationDrawable();
 *         case "inset":
 *             return new InsetDrawable();
 *         case "bitmap":
 *             return new BitmapDrawable();
 *         case "nine-patch":
 *             return new NinePatchDrawable();
 *         case "animated-image":
 *             return new AnimatedImageDrawable();
 *         default:
 *             return null;
 *     }
 * }
 */
