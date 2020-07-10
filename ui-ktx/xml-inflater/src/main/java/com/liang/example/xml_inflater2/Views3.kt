@file:Suppress("unused", "UNUSED_ANONYMOUS_PARAMETER")

package com.liang.example.xml_inflater2

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.StateSet
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.webkit.WebView
import android.widget.AbsSeekBar
import android.widget.CalendarView
import android.widget.DatePicker
import android.widget.ImageSwitcher
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.SearchView
import android.widget.SeekBar
import android.widget.TextSwitcher
import android.widget.TextView
import android.widget.TimePicker
import android.widget.VideoView
import android.widget.ViewAnimator
import android.widget.ViewFlipper
import android.widget.ViewSwitcher
import com.liang.example.basic_ktx.ReflectHelper
import com.liang.example.xml_inflater2.ViewParserHelper.parseBlendMode
import com.liang.example.xml_inflater2.ViewParserHelper.parseTintMode
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

open class WebViewParser(viewGroupParser: ViewGroupParser) : BaseViewParser<WebView>(
        "WebView", ResViewType.VIEW_BARRIER, mutableListOf(viewGroupParser)) {
    override fun prepare() = Unit
    override fun makeView(node: Node): WebView? = WebView(apm.context)
}

open class SurfaceViewParser(viewParser: ViewParser) : BaseViewParser<SurfaceView>(
        "SurfaceView", ResViewType.VIEW_SURFACE, mutableListOf(viewParser)) {
    override fun prepare() = Unit
    override fun makeView(node: Node): SurfaceView? = SurfaceView(apm.context)
}

open class TextureViewParser(viewParser: ViewParser) : BaseViewParser<TextureView>(
        "TextureView", ResViewType.VIEW_TEXTURE, mutableListOf(viewParser)) {
    override fun prepare() = Unit
    override fun makeView(node: Node): TextureView? = TextureView(apm.context)
}

open class VideoViewParser(surfaceViewParser: SurfaceViewParser) : BaseViewParser<VideoView>(
        "VideoView", ResViewType.VIEW_VIDEO, mutableListOf(surfaceViewParser)) {
    override fun prepare() = Unit
    override fun makeView(node: Node): VideoView? = VideoView(apm.context)
}

open class CalendarViewParser(frameLayoutParser: FrameLayoutParser) : BaseViewParser<CalendarView>(
        "CalendarView", ResViewType.VIEW_CALENDAR, mutableListOf(frameLayoutParser)) {
    override fun prepare() {
        registerNotNull(Attrs.CalendarView.firstDayOfWeek) { a, v1, p, v2: Int -> v1.firstDayOfWeek = v2 }
        registerNotNull(Attrs.CalendarView.minDate) { a, v1, p, v2: String -> v1.minDate = DATE_FORMATTER.parse(v2)?.time ?: 1900 }
        registerNotNull(Attrs.CalendarView.maxDate) { a, v1, p, v2: String -> v1.maxDate = DATE_FORMATTER.parse(v2)?.time ?: 2100 }
        registerNotNull(Attrs.CalendarView.monthTextAppearance) { a, v1, p, v2: Int ->
            val mDelegate = ReflectHelper.get("mDelegate", v1) ?: return@registerNotNull
            val mDayPickerView = ReflectHelper.get("mDayPickerView", mDelegate) ?: return@registerNotNull
            val mAdapter = ReflectHelper.get("mAdapter", mDayPickerView) ?: return@registerNotNull
            ReflectHelper.invokeN("setMonthTextAppearance", mAdapter, v2)
        }
        registerNotNull(Attrs.CalendarView.weekDayTextAppearance) { a, v1, p, v2: Int ->
            val mDelegate = ReflectHelper.get("mDelegate", v1) ?: return@registerNotNull
            val mDayPickerView = ReflectHelper.get("mDayPickerView", mDelegate) ?: return@registerNotNull
            val mAdapter = ReflectHelper.get("mAdapter", mDayPickerView) ?: return@registerNotNull
            ReflectHelper.invokeN("setDayOfWeekTextAppearance", mAdapter, v2)
        }
        registerNotNull(Attrs.CalendarView.dateTextAppearance) { a, v1, p, v2: Int ->
            val mDelegate = ReflectHelper.get("mDelegate", v1) ?: return@registerNotNull
            val mDayPickerView = ReflectHelper.get("mDayPickerView", mDelegate) ?: return@registerNotNull
            val mAdapter = ReflectHelper.get("mAdapter", mDayPickerView) ?: return@registerNotNull
            ReflectHelper.invokeN("setDayTextAppearance", mAdapter, v2)
        }
        registerNotNull(Attrs.CalendarView.daySelectorColor) { a, v1, p, v2: String ->
            val colors = loadColorStateList(v2) ?: return@registerNotNull
            val mDelegate = ReflectHelper.get("mDelegate", v1) ?: return@registerNotNull
            val mDayPickerView = ReflectHelper.get("mDayPickerView", mDelegate) ?: return@registerNotNull
            val mAdapter = ReflectHelper.get("mAdapter", mDayPickerView) ?: return@registerNotNull
            ReflectHelper.invokeN("setDaySelectorColor", mAdapter, colors)
        }
        // registerNotNull(Attrs.CalendarView.dayHighlightColor) { a, v1, p, v2: Int -> }
        // registerNotNull(Attrs.CalendarView.calendarViewMode) { a, v1, p, v2: Int -> TODO() }
        registerNotNull(Attrs.CalendarView.showWeekNumber) { a, v1, p, v2: Boolean -> v1.showWeekNumber = v2 }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            registerNotNull(Attrs.CalendarView.shownWeekCount) { a, v1, p, v2: Int -> v1.shownWeekCount = v2 }
            registerNotNull(Attrs.CalendarView.selectedWeekBackgroundColor) { a, v1, p, v2: Int -> v1.selectedWeekBackgroundColor = v2 }
            registerNotNull(Attrs.CalendarView.focusedMonthDateColor) { a, v1, p, v2: Int -> v1.focusedMonthDateColor = v2 }
            registerNotNull(Attrs.CalendarView.unfocusedMonthDateColor) { a, v1, p, v2: Int -> v1.unfocusedMonthDateColor = v2 }
            registerNotNull(Attrs.CalendarView.weekNumberColor) { a, v1, p, v2: Int -> v1.weekNumberColor = v2 }
            registerNotNull(Attrs.CalendarView.weekSeparatorLineColor) { a, v1, p, v2: Int -> v1.weekSeparatorLineColor = v2 }
            registerNotNull(Attrs.CalendarView.selectedDateVerticalBar) { a, v1, p, v2: Int ->
                v1.selectedDateVerticalBar = ResStore.loadDrawable(v2, apm.context, true)
            }
        }
    }

    override fun makeView(node: Node): CalendarView? = CalendarView(apm.context)

    companion object {
        const val DATE_FORMAT = "MM/dd/yyyy"
        val DATE_FORMATTER: DateFormat = SimpleDateFormat(DATE_FORMAT)
    }
}

open class DatePickerParser(frameLayoutParser: FrameLayoutParser) : BaseViewParser<DatePicker>(
        "DataPicker", ResViewType.VIEW_DATE_PICKER, mutableListOf(frameLayoutParser)) {
    override fun prepare() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            registerNotNull(Attrs.DatePicker.firstDayOfWeek) { a, v1, p, v2: Int -> v1.firstDayOfWeek = v2 }
        }
        registerMulti<Any>(Attrs.DatePicker.startYear to 1900, Attrs.DatePicker.minDate to null) { v, p, vs, helper ->
            val s = helper.getNullable(Attrs.DatePicker.minDate) as? String
            v.minDate = when {
                !s.isNullOrEmpty() -> mDateFormat.parse(s)?.time ?: Date(helper[Attrs.DatePicker.startYear] as Int, 0, 1).time
                else -> Date(helper[Attrs.DatePicker.startYear] as Int, 0, 1).time
            }
        }
        registerMulti<Any>(Attrs.DatePicker.endYear to 2100, Attrs.DatePicker.maxDate to null) { v, p, vs, helper ->
            val s = helper.getNullable(Attrs.DatePicker.maxDate) as? String
            v.maxDate = when {
                !s.isNullOrEmpty() -> mDateFormat.parse(s)?.time ?: Date(helper[Attrs.DatePicker.endYear] as Int, 0, 1).time
                else -> Date(helper[Attrs.DatePicker.endYear] as Int, 0, 1).time
            }
        }
        registerNotNull(Attrs.DatePicker.spinnersShown) { a, v1, p, v2: Boolean -> v1.spinnersShown = v2 }
        registerNotNull(Attrs.DatePicker.calendarViewShown) { a, v1, p, v2: Boolean -> v1.calendarViewShown = v2 }
        registerNotNull(Attrs.DatePicker.internalLayout) { a, v1, p, v2: Int ->
            TODO("not implemented")
        }  // DatePickerCalendarDelegate
        registerNotNull(Attrs.DatePicker.legacyLayout) { a, v1, p, v2: Int ->
            TODO("not implemented")
        }  // DatePickerSpinnerDelegate
        registerNotNull(Attrs.DatePicker.headerTextColor) { a, v1, p, v2: String ->
            val colors = loadColorStateList(v2) ?: return@registerNotNull
            val mDelegate = ReflectHelper.get("mDelegate", v1)
            (ReflectHelper.get("mHeaderYear", mDelegate) as? TextView)?.setTextColor(colors)
            (ReflectHelper.get("mHeaderMonthDay", mDelegate) as? TextView)?.setTextColor(colors)
        }  // DatePickerCalendarDelegate
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            registerNotNull(Attrs.DatePicker.headerBackground) { a, v1, p, v2: String ->
                val drawable = loadDrawable(v2) ?: return@registerNotNull
                val mDelegate = ReflectHelper.get("mDelegate", v1) ?: return@registerNotNull
                val mContainer = ReflectHelper.get("mContainer", mDelegate) as? ViewGroup ?: return@registerNotNull
                mContainer.background = drawable
            }
        }
        // registerNotNull(Attrs.DatePicker.yearListItemTextAppearance) { a, v1, p, v2: Int -> }
        // registerNotNull(Attrs.DatePicker.yearListItemActivatedTextAppearance) { a, v1, p, v2: Int -> }
        // registerNotNull(Attrs.DatePicker.calendarTextColor) { a, v1, p, v2: Int -> }
        // registerNotNull(Attrs.DatePicker.headerMonthTextAppearance) { a, v1, p, v2: Int -> }
        // registerNotNull(Attrs.DatePicker.headerDayOfMonthTextAppearance) { a, v1, p, v2: Int -> }
        // registerNotNull(Attrs.DatePicker.headerYearTextAppearance) { a, v1, p, v2: Int -> }
        // registerNotNull(Attrs.DatePicker.dayOfWeekBackground) { a, v1, p, v2: Int -> }
        // registerNotNull(Attrs.DatePicker.dayOfWeekTextAppearance) { a, v1, p, v2: Int -> }
        // registerNotNull(Attrs.DatePicker.yearListSelectorColor) { a, v1, p, v2: Int -> }
        registerMulti(Attrs.DatePicker.dialogMode to false, Attrs.DatePicker.datePickerMode to /*DatePicker.MODE_SPINNER*/ 1) { v, p, vs, helper ->
            val datePickerMode = helper[Attrs.DatePicker.datePickerMode] as Int
            val mMode = if (helper[Attrs.DatePicker.dialogMode] as? Boolean == true && datePickerMode == /*DatePicker.MODE_CALENDAR*/ 2) {
                apm.context.resources.getInteger(ReflectHelper.getIntStatic("date_picker_mode",
                        ReflectHelper.findCls("com.android.internal.R\$integer")))
            } else {
                datePickerMode
            }
            ReflectHelper.setInt("mMode", v, mMode)
            when (mMode) {
                2 -> Unit
                else -> Unit
            }
            // TODO("mDelegate")
        }
    }

    override fun makeView(node: Node): DatePicker? = DatePicker(apm.context)

    companion object {
        const val DATE_FORMAT = "MM/dd/yyyy"
        @SuppressLint("SimpleDateFormat")
        val mDateFormat: DateFormat = SimpleDateFormat(DATE_FORMAT)
    }
}

open class NumberPickerParser(linearLayoutParser: LinearLayoutParser) : BaseViewParser<NumberPicker>(
        "NumberPicker", ResViewType.VIEW_NUMBER_PICKER, mutableListOf(linearLayoutParser)) {
    override fun prepare() {
        registerNotNull(Attrs.NumberPicker.solidColor) { a, v1, p, v2: Int -> ReflectHelper.setInt("mSolidColor", v1, v2) }
        registerNotNull(Attrs.NumberPicker.selectionDivider) { a, v1, p, v2: Int ->
            ReflectHelper.set("mSelectionDivider", v1, ResStore.loadDrawable(v2, apm.context) ?: return@registerNotNull)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            registerNotNull(Attrs.NumberPicker.selectionDividerHeight) { a, v1, p, v2: Float -> v1.selectionDividerHeight = v2.toInt() }
        }
        registerNotNull(Attrs.NumberPicker.selectionDividersDistance) { a, v1, p, v2: Float ->
            ReflectHelper.setInt("mSelectionDividersDistance", v1, v2.toInt())
        }
        registerNotNull(Attrs.NumberPicker.internalMinHeight) { a, v1, p, v2: Float ->
            ReflectHelper.setInt("mMinHeight", v1, v2.toInt())
        }
        registerNotNull(Attrs.NumberPicker.internalMaxHeight) { a, v1, p, v2: Float ->
            ReflectHelper.setInt("mMaxHeight", v1, v2.toInt())
        }
        registerNotNull(Attrs.NumberPicker.internalMinWidth) { a, v1, p, v2: Float ->
            ReflectHelper.setInt("mMinWidth", v1, v2.toInt())
        }
        registerNotNull(Attrs.NumberPicker.internalMaxWidth) { a, v1, p, v2: Float ->
            ReflectHelper.setInt("mMaxWidth", v1, v2.toInt())
        }
        registerNotNull(Attrs.NumberPicker.internalLayout) { a, v1, p, v2: Int ->
            TODO("not implement")
        }
        registerNotNull(Attrs.NumberPicker.virtualButtonPressedDrawable) { a, v1, p, v2: Int ->
            ReflectHelper.set("mVirtualButtonPressedDrawable", v1, ResStore.loadDrawable(v2, apm.context) ?: return@registerNotNull)
        }
        registerNotNull(Attrs.NumberPicker.hideWheelUntilFocused) { a, v1, p, v2: Boolean ->
            ReflectHelper.setBoolean("mHideWheelUntilFocused", v1, v2)
        }
    }

    override fun makeView(node: Node): NumberPicker? = NumberPicker(apm.context)
}

open class TimePickerParser(frameLayoutParser: FrameLayoutParser) : BaseViewParser<TimePicker>(
        "TimePicker", ResViewType.VIEW_TIME_PICKER, mutableListOf(frameLayoutParser)) {
    override fun prepare() {
        registerNotNull(Attrs.TimePicker.legacyLayout) { a, v1, p, v2: Int -> TODO("not implemented") }
        registerNotNull(Attrs.TimePicker.internalLayout) { a, v1, p, v2: Int -> TODO("not implemented") }
        registerNotNull(Attrs.TimePicker.headerTextColor) { a, v1, p, v2: String ->
            val colors = loadColorStateList(v2) ?: return@registerNotNull
            val mDelegate = ReflectHelper.get("mDelegate", v1)
            (ReflectHelper.get("mHourView", mDelegate) as? TextView)?.setTextColor(colors)
            (ReflectHelper.get("mSeparatorView", mDelegate) as? TextView)?.setTextColor(colors)
            (ReflectHelper.get("mMinuteView", mDelegate) as? TextView)?.setTextColor(colors)
            (ReflectHelper.get("mAmLabel", mDelegate) as? TextView)?.setTextColor(colors)
            (ReflectHelper.get("mPmLabel", mDelegate) as? TextView)?.setTextColor(colors)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            registerNotNull(Attrs.TimePicker.headerBackground) { a, v1, p, v2: String ->
                val drawable = loadDrawable(v2) ?: return@registerNotNull
                val mDelegate = ReflectHelper.get("mDelegate", v1) ?: return@registerNotNull
                (ReflectHelper.get("mRadialTimePickerHeader", mDelegate) as? View)?.background = drawable
                (ReflectHelper.get("mRadialTimePickerHeader", mDelegate) as? View)?.background = loadDrawable(v2) ?: return@registerNotNull
            }
        }
        registerMulti<String>(Attrs.TimePicker.numbersTextColor to null, Attrs.TimePicker.numbersInnerTextColor to null)
        { v, p, vs, helper ->
            val mDelegate = ReflectHelper.get("mDelegate", v) ?: return@registerMulti
            val mTextColor = ReflectHelper.get("mTextColor", mDelegate) as? Array<ColorStateList> ?: return@registerMulti
            mTextColor[0] = loadColorStateList(helper.getNullable(Attrs.TimePicker.numbersTextColor)) ?: ColorStateList.valueOf(MISSING_COLOR)
            mTextColor[2] = loadColorStateList(helper.getNullable(Attrs.TimePicker.numbersInnerTextColor)) ?: ColorStateList.valueOf(MISSING_COLOR)
            mTextColor[1] = mTextColor[0]
        }
        register(Attrs.TimePicker.numbersBackgroundColor) { a, v1, p, v2: Int? ->
            val mDelegate = ReflectHelper.get("mDelegate", v1) ?: return@register
            (ReflectHelper.get("mPaintBackground", mDelegate) as? Paint)?.color = v2 ?: apm.context.resources.getColor(
                    ReflectHelper.getIntStatic("timepicker_default_numbers_background_color_material", ReflectHelper.findCls("com.android.internal.R\$color")))
        }
        register(Attrs.TimePicker.numbersSelectorColor) { a, v1, p, v2: String? ->
            val mDelegate = ReflectHelper.get("mDelegate", v1) ?: return@register
            val colors = loadColorStateList(v2)
            val selectorActivatedColor = if (colors != null && states != null) {
                colors.getColorForState(states, 0)
            } else {
                MISSING_COLOR
            }
            (ReflectHelper.get("mPaintCenter", mDelegate) as? Paint)?.color = selectorActivatedColor
            ReflectHelper.setInt("mSelectorColor", mDelegate, selectorActivatedColor)
        }
        // registerNotNull(Attrs.TimePicker.headerAmPmTextAppearance) { a, v1, p, v2: Int -> }
        // registerNotNull(Attrs.TimePicker.headerTimeTextAppearance) { a, v1, p, v2: Int -> }
        // registerNotNull(Attrs.TimePicker.amPmTextColor) { a, v1, p, v2: Int -> }
        // registerNotNull(Attrs.TimePicker.amPmBackgroundColor) { a, v1, p, v2: Int -> }
        registerMulti(Attrs.TimePicker.dialogMode to false, Attrs.TimePicker.timePickerMode to 1) { v, p, vs, helper ->
            val requestedMode = helper[Attrs.TimePicker.timePickerMode] as? Int ?: 1
            val mMode = if (requestedMode == 2 || helper[Attrs.TimePicker.dialogMode] as Boolean) {
                apm.context.resources.getInteger(ReflectHelper.getIntStatic("time_picker_mode", ReflectHelper.findCls("com.android.internal.R.integer")))
            } else {
                requestedMode
            }
            ReflectHelper.setInt("mMode", v, mMode)
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    override fun makeView(node: Node): TimePicker? = TimePicker(apm.context)

    companion object {
        const val MISSING_COLOR = Color.MAGENTA

        val states = ReflectHelper.invokeS<IntArray>("get", StateSet::class.java,
                ReflectHelper.getIntStatic("VIEW_STATE_ENABLED", StateSet::class.java)
                        or ReflectHelper.getIntStatic("VIEW_STATE_ACTIVATED", StateSet::class.java))
    }
}

open class ProgressBarParser(viewParser: ViewParser) : BaseViewParser<ProgressBar>(
        "ProgressBar", ResViewType.VIEW_PROGRESS_BAR, mutableListOf(viewParser)) {
    override fun prepare() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerNotNull(Attrs.ProgressBar.min) { a, v1, p, v2: Int -> v1.min = v2 }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            registerNotNull(Attrs.ProgressBar.minWidth) { a, v1, p, v2: Float -> v1.minWidth = v2.toInt() }
            registerNotNull(Attrs.ProgressBar.maxWidth) { a, v1, p, v2: Float -> v1.maxWidth = v2.toInt() }
            registerNotNull(Attrs.ProgressBar.minHeight) { a, v1, p, v2: Float -> v1.minHeight = v2.toInt() }
            registerNotNull(Attrs.ProgressBar.maxHeight) { a, v1, p, v2: Float -> v1.maxHeight = v2.toInt() }
        } else {
            registerNotNull(Attrs.ProgressBar.minWidth) { a, v1, p, v2: Float -> ReflectHelper.setInt("mMinWidth", v1, v2.toInt()) }
            registerNotNull(Attrs.ProgressBar.maxWidth) { a, v1, p, v2: Float -> ReflectHelper.setInt("mMaxWidth", v1, v2.toInt()) }
            registerNotNull(Attrs.ProgressBar.minHeight) { a, v1, p, v2: Float -> ReflectHelper.setInt("mMinHeight", v1, v2.toInt()) }
            registerNotNull(Attrs.ProgressBar.maxHeight) { a, v1, p, v2: Float -> ReflectHelper.setInt("mMaxHeight", v1, v2.toInt()) }
        }
        registerNotNull(Attrs.ProgressBar.max) { a, v1, p, v2: Int -> v1.max = v2 }
        registerNotNull(Attrs.ProgressBar.progress) { a, v1, p, v2: Int -> v1.progress = v2 }
        registerNotNull(Attrs.ProgressBar.secondaryProgress) { a, v1, p, v2: Int -> v1.secondaryProgress = v2 }
        registerMulti(Attrs.ProgressBar.indeterminate to false, Attrs.ProgressBar.indeterminateOnly to false) { v, p, vs, helper ->
            val mOnlyIndeterminate = helper[Attrs.ProgressBar.indeterminateOnly]
            ReflectHelper.setBoolean("mOnlyIndeterminate", v, mOnlyIndeterminate)
            v.isIndeterminate = mOnlyIndeterminate || helper[Attrs.ProgressBar.indeterminate]
        }
        registerNotNull(Attrs.ProgressBar.indeterminateDrawable) { a, v1, p, v2: Int ->
            v1.indeterminateDrawable = ResStore.loadDrawable(v2, apm.context) ?: return@registerNotNull
        }
        registerNotNull(Attrs.ProgressBar.progressDrawable) { a, v1, p, v2: Int ->
            v1.progressDrawable = ResStore.loadDrawable(v2, apm.context) ?: return@registerNotNull
        }
        registerNotNull(Attrs.ProgressBar.indeterminateDuration) { a, v1, p, v2: Int ->
            ReflectHelper.setInt("mDuration", v1, v2)
        }
        registerNotNull(Attrs.ProgressBar.indeterminateBehavior) { a, v1, p, v2: Int ->
            ReflectHelper.setInt("mBehavior", v1, v2)
        }
        registerNotNull(Attrs.ProgressBar.interpolator) { a, v1, p, v2: Int -> v1.interpolator = AnimationUtils.loadInterpolator(apm.context, v2) }
        // registerNotNull(Attrs.ProgressBar.animationResolution) { a, v1, p, v2: Int ->  }
        registerNotNull(Attrs.ProgressBar.mirrorForRtl) { a, v1, p, v2: Boolean -> ReflectHelper.setBoolean("mMirrorForRtl", v1, v2) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            registerNotNull(Attrs.ProgressBar.progressTint) { a, v1, p, v2: String ->
                v1.progressTintList = loadColorStateList(v2) ?: return@registerNotNull
            }
            registerNotNull(Attrs.ProgressBar.progressBackgroundTint) { a, v1, p, v2: String ->
                v1.progressBackgroundTintList = loadColorStateList(v2) ?: return@registerNotNull
            }
            registerNotNull(Attrs.ProgressBar.secondaryProgressTint) { a, v1, p, v2: String ->
                v1.secondaryProgressTintList = loadColorStateList(v2) ?: return@registerNotNull
            }
            registerNotNull(Attrs.ProgressBar.indeterminateTint) { a, v1, p, v2: String ->
                v1.indeterminateTintList = loadColorStateList(v2) ?: return@registerNotNull
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                registerNotNull(Attrs.ProgressBar.progressTintMode) { a, v1, p, v2: Int -> v1.progressTintBlendMode = parseBlendMode(v2, null) }
                registerNotNull(Attrs.ProgressBar.progressBackgroundTintMode) { a, v1, p, v2: Int ->
                    v1.progressBackgroundTintBlendMode = parseBlendMode(v2, null)
                }
                registerNotNull(Attrs.ProgressBar.secondaryProgressTintMode) { a, v1, p, v2: Int ->
                    v1.secondaryProgressTintBlendMode = parseBlendMode(v2, null)
                }
                registerNotNull(Attrs.ProgressBar.indeterminateTintMode) { a, v1, p, v2: Int ->
                    v1.indeterminateTintBlendMode = parseBlendMode(v2, null)
                }
            } else {
                registerNotNull(Attrs.ProgressBar.progressTintMode) { a, v1, p, v2: Int -> v1.progressTintMode = parseTintMode(v2, null) }
                registerNotNull(Attrs.ProgressBar.progressBackgroundTintMode) { a, v1, p, v2: Int ->
                    v1.progressBackgroundTintMode = parseTintMode(v2, null)
                }
                registerNotNull(Attrs.ProgressBar.secondaryProgressTintMode) { a, v1, p, v2: Int ->
                    v1.secondaryProgressTintMode = parseTintMode(v2, null)
                }
                registerNotNull(Attrs.ProgressBar.indeterminateTintMode) { a, v1, p, v2: Int -> v1.indeterminateTintMode = parseTintMode(v2, null) }
            }
        }
    }

    override fun makeView(node: Node): ProgressBar? = ProgressBar(apm.context)
}

open class AbsSeekBarParser(progressBarParser: ProgressBarParser) : BaseViewParser<AbsSeekBar>(
        "AbsSeekBar", ResViewType.VIEW_ABS_SEEK_BAR, mutableListOf(progressBarParser)) {
    override fun prepare() {
        registerNotNull(Attrs.SeekBar.thumb) { a, v1, p, v2: Int -> v1.thumb = ResStore.loadDrawable(v2, apm.context) ?: return@registerNotNull }
        registerNotNull(Attrs.SeekBar.thumbOffset) { a, v1, p, v2: Float -> v1.thumbOffset = v2.toInt() }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            registerNotNull(Attrs.SeekBar.splitTrack) { a, v1, p, v2: Boolean -> v1.splitTrack = v2 }
            registerNotNull(Attrs.SeekBar.thumbTint) { a, v1, p, v2: String -> v1.thumbTintList = loadColorStateList(v2) ?: return@registerNotNull }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                registerNotNull(Attrs.SeekBar.thumbTintMode) { a, v1, p, v2: Int -> v1.thumbTintBlendMode = parseBlendMode(v2, null) }
            } else {
                registerNotNull(Attrs.SeekBar.thumbTintMode) { a, v1, p, v2: Int -> v1.thumbTintMode = parseTintMode(v2, null) }
            }
        }
        registerMulti(Attrs.SeekBar.useDisabledAlpha to true, Attrs.Theme.disabledAlpha to 0.9f) { v, p, vs, helper ->
            if (helper[Attrs.SeekBar.useDisabledAlpha] as? Boolean == true) {
                ReflectHelper.setFloat("mDisabledAlpha", v, helper[Attrs.Theme.disabledAlpha] as Float)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerNotNull(Attrs.SeekBar.tickMark) { a, v1, p, v2: Int ->
                v1.tickMark = ResStore.loadDrawable(v2, apm.context) ?: return@registerNotNull
            }
            registerNotNull(Attrs.SeekBar.tickMarkTint) { a, v1, p, v2: String ->
                v1.tickMarkTintList = loadColorStateList(v2) ?: return@registerNotNull
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                registerNotNull(Attrs.SeekBar.tickMarkTintMode) { a, v1, p, v2: Int -> v1.tickMarkTintBlendMode = parseBlendMode(v2, null) }
            } else {
                registerNotNull(Attrs.SeekBar.tickMarkTintMode) { a, v1, p, v2: Int -> v1.tickMarkTintMode = parseTintMode(v2, null) }
            }
        }
    }

    override fun makeView(node: Node): AbsSeekBar? = throw RuntimeException("not supported: cannot create $rootName")
}

open class SeekBarParser(absSeekBarParser: AbsSeekBarParser) : BaseViewParser<SeekBar>(
        "SeekBar", ResViewType.VIEW_SEEK_BAR, mutableListOf(absSeekBarParser)) {
    override fun prepare() = Unit
    override fun makeView(node: Node): SeekBar? = SeekBar(apm.context)
}

open class RatingBarParser(absSeekBarParser: AbsSeekBarParser) : BaseViewParser<RatingBar>(
        "RatingBar", ResViewType.VIEW_RATING_BAR, mutableListOf(absSeekBarParser)) {
    override fun prepare() {
        registerNotNull(Attrs.RatingBar.isIndicator) { a, v1, p, v2: Boolean -> v1.setIsIndicator(v2) }
        registerNotNull(Attrs.RatingBar.numStars) { a, v1, p, v2: Int -> v1.numStars = v2 }
        registerNotNull(Attrs.RatingBar.rating) { a, v1, p, v2: Float -> v1.rating = v2 }
        registerNotNull(Attrs.RatingBar.stepSize) { a, v1, p, v2: Float -> v1.stepSize = v2 }
    }

    override fun makeView(node: Node): RatingBar? = RatingBar(apm.context)
}

open class SearchViewParser(linearLayoutParser: LinearLayoutParser) : BaseViewParser<SearchView>(
        "SearchView", ResViewType.VIEW_SEARCH, mutableListOf(linearLayoutParser)) {
    override fun prepare() {
        registerNotNull(Attrs.SearchView.layout) { a, v1, p, v2: Int -> TODO("not implemented") }
        registerNotNull(Attrs.SearchView.suggestionRowLayout) { a, v1, p, v2: Int -> TODO("not implemented") }
        registerNotNull(Attrs.SearchView.commitIcon) { a, v1, p, v2: Int -> TODO("not implemented") }
        registerNotNull(Attrs.SearchView.iconifiedByDefault) { a, v1, p, v2: Int -> }
        registerNotNull(Attrs.SearchView.maxWidth) { a, v1, p, v2: Float -> v1.maxWidth = v2.toInt() }
        registerNotNull(Attrs.SearchView.queryHint) { a, v1, p, v2: String -> v1.queryHint = v2 }
        registerNotNull(Attrs.SearchView.defaultQueryHint) { a, v1, p, v2: String -> ReflectHelper.set("defaultQueryHint", v1, v2) }
        registerNotNull(Attrs.SearchView.imeOptions) { a, v1, p, v2: Int -> v1.imeOptions = v2 }
        registerNotNull(Attrs.SearchView.inputType) { a, v1, p, v2: Int -> v1.inputType = v2 }
        registerNotNull(Attrs.SearchView.closeIcon) { a, v1, p, v2: Int ->
            (ReflectHelper.get("mCloseButton", v1) as? ImageView)?.setImageDrawable(ResStore.loadDrawable(v2, apm.context) ?: return@registerNotNull)
        }
        registerNotNull(Attrs.SearchView.goIcon) { a, v1, p, v2: Int ->
            (ReflectHelper.get("mGoButton", v1) as? ImageView)?.setImageDrawable(ResStore.loadDrawable(v2, apm.context) ?: return@registerNotNull)
        }
        registerNotNull(Attrs.SearchView.searchIcon) { a, v1, p, v2: Int ->
            (ReflectHelper.get("mSearchButton", v1) as? ImageView)?.setImageDrawable(ResStore.loadDrawable(v2, apm.context) ?: return@registerNotNull)
            (ReflectHelper.get("mCollapsedIcon", v1) as? ImageView)?.setImageDrawable(ResStore.loadDrawable(v2, apm.context) ?: return@registerNotNull)
        }
        registerNotNull(Attrs.SearchView.searchHintIcon) { a, v1, p, v2: Int ->
            ReflectHelper.set("mSearchHintIcon", v1, ResStore.loadDrawable(v2, apm.context) ?: return@registerNotNull)
        }
        registerNotNull(Attrs.SearchView.voiceIcon) { a, v1, p, v2: Int ->
            (ReflectHelper.get("mVoiceButton", v1) as? ImageView)?.setImageDrawable(ResStore.loadDrawable(v2, apm.context) ?: return@registerNotNull)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            registerNotNull(Attrs.SearchView.queryBackground) { a, v1, p, v2: Int ->
                (ReflectHelper.get("mSearchPlate", v1) as? View)?.background = ResStore.loadDrawable(v2, apm.context) ?: return@registerNotNull
            }
            registerNotNull(Attrs.SearchView.submitBackground) { a, v1, p, v2: Int ->
                (ReflectHelper.get("mSubmitArea", v1) as? View)?.background = ResStore.loadDrawable(v2, apm.context) ?: return@registerNotNull
            }
        }
    }

    override fun makeView(node: Node): SearchView? = SearchView(apm.context)
}

open class ViewAnimatorParser(frameLayoutParser: FrameLayoutParser) : BaseViewParser<ViewAnimator>(
        "ViewAnimator", ResViewType.VIEW_ANIMATOR, mutableListOf(frameLayoutParser)) {
    override fun prepare() {
        registerNotNull(Attrs.ViewAnimator.animateFirstView) { a, v1, p, v2: Boolean -> v1.animateFirstView = v2 }
        registerNotNull(Attrs.ViewAnimator.inAnimation) { a, v1, p, v2: Int ->
            v1.inAnimation = ResStore.loadAnimation(v2, apm.context) ?: return@registerNotNull
        }
        registerNotNull(Attrs.ViewAnimator.outAnimation) { a, v1, p, v2: Int ->
            v1.outAnimation = ResStore.loadAnimation(v2, apm.context) ?: return@registerNotNull
        }
    }

    override fun makeView(node: Node): ViewAnimator? = ViewAnimator(apm.context)
}

open class ViewFlipperParser(viewAnimatorParser: ViewAnimatorParser) : BaseViewParser<ViewFlipper>(
        "ViewFlipper", ResViewType.VIEW_FLIPPER, mutableListOf(viewAnimatorParser)) {
    override fun prepare() {
        registerNotNull(Attrs.ViewFlipper.autoStart) { a, v1, p, v2: Boolean -> v1.isAutoStart = v2 }
        registerNotNull(Attrs.ViewFlipper.flipInterval) { a, v1, p, v2: Int -> v1.flipInterval = v2 }
    }

    override fun makeView(node: Node): ViewFlipper? = ViewFlipper(apm.context)
}

open class ViewSwitcherParser(viewAnimatorParser: ViewAnimatorParser) : BaseViewParser<ViewSwitcher>(
        "ViewSwitcher", ResViewType.VIEW_SWITCHER, mutableListOf(viewAnimatorParser)) {
    override fun prepare() = Unit
    override fun makeView(node: Node): ViewSwitcher? = ViewSwitcher(apm.context)
}

open class TextSwitcherParser(viewSwitcherParser: ViewSwitcherParser) : BaseViewParser<TextSwitcher>(
        "TextSwitcher", ResViewType.VIEW_TEXT_SWITCHER, mutableListOf(viewSwitcherParser)) {
    override fun prepare() = Unit
    override fun makeView(node: Node): TextSwitcher? = TextSwitcher(apm.context)
}

open class ImageSwitcherParser(viewSwitcherParser: ViewSwitcherParser) : BaseViewParser<ImageSwitcher>(
        "ImageSwitcher", ResViewType.VIEW_IMAGE_SWITCHER, mutableListOf(viewSwitcherParser)) {
    override fun prepare() = Unit
    override fun makeView(node: Node): ImageSwitcher? = ImageSwitcher(apm.context)
}
