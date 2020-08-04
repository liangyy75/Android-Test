package com.liang.example.viewtest_kt.chart2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.liang.example.basic_ktx.EnumHelper

/**
 * @author liangyuying.lyy75@bytedance.com
 * @date 2020/8/3
 * <p>
 * todo 描述
 */
open class ChartView : View {
    constructor(c: Context) : super(c)
    constructor(c: Context, attributeSet: AttributeSet?) : super(c, attributeSet)
    constructor(c: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(c, attributeSet, defStyleAttr)

    open val mainPaint = Paint()
    open val strokePaint = Paint()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
    }

    open fun drawItem(canvas: Canvas, unit: ChartUnit) {
    }
}

/* unit's style */

open class ChartBorder {
    open var borderWidth: Float = 0f
    open var borderColor: Int = Color.WHITE
    open var borderRadius: Float = 0f
}

open class ChartTransform {
    open var rotation: Float = 0f
    open var scaleX: Float = 1f
    open var scaleY: Float = 1f
    open var translateX: Float = 0f
    open var translateY: Float = 0f
    open var skewX: Float = 0f
    open var skewY: Float = 0f
}

open class ChartBackground {
    open var color: Int = Color.WHITE
    open var bgResId: Int = 0
    open var bgDrawable: Drawable? = null
}

open class ChartPosition {
    open var width: Float = -1f  /* -1~0是相对于parent的百分比，-3~-2是相对于chartView的百分比，0~是正常数值 */
    open var height: Float = -1f
    open var left: Float = 0f
    open var top: Float = 0f
    open var right: Float = 0f
    open var bottom: Float = 0f
    open var mode: Int = ChartUnitStyle.PosMode.RES_PARENT
}

open class ChartTextStyle {
    open var size: Float = 12f
    open var weight: Int = Weight.NORMAL
    open var align: Int = Align.START
    open var decoration: Int = Decoration.NONE
    open var isItalic: Boolean = false

    object Align {
        const val TEXT_ALIGN = "textAlign"
        val START = EnumHelper.get2(TEXT_ALIGN)
        val END = EnumHelper.get2(TEXT_ALIGN)
        val CENTER = EnumHelper.get2(TEXT_ALIGN)
    }

    open fun toPaintAlign(): Paint.Align = when (this.align) {
        Align.END -> Paint.Align.RIGHT
        Align.CENTER -> Paint.Align.CENTER
        else -> Paint.Align.LEFT
    }

    object Weight {
        const val FONT_WEIGHT = "fontWeight"
        val THIN = EnumHelper[FONT_WEIGHT]
        val NORMAL = EnumHelper[FONT_WEIGHT]
        val BOLD = EnumHelper[FONT_WEIGHT]
    }

    object Decoration {
        const val DECORATION = "decoration"
        val NONE = EnumHelper[DECORATION]
        val ABOVE = EnumHelper[DECORATION]
        val THROUGH = EnumHelper[DECORATION]
        val BELOW = EnumHelper[DECORATION]
    }
}

open class ChartUnitStyle {
    open var zIndex: Int = 0
    open var state: Int = State.NORMAL
    open var border: ChartBorder? = null
    open var transform: ChartTransform? = null
    open var background: ChartBackground? = null
    open var position: ChartPosition? = null
    open var textStyle: ChartTextStyle? = null

    object PosMode {
        const val POS_MODE = "chartItemPosMode"
        val RES_PARENT = EnumHelper[POS_MODE]
        val ABS_PARENT = EnumHelper[POS_MODE]
        val RES_ROOT = EnumHelper[POS_MODE]
        val ABS_ROOT = EnumHelper[POS_MODE]
    }

    object State {
        const val STATE = "chartItemState"
        val NONE = EnumHelper[STATE]  // 不可见
        val NORMAL = EnumHelper[STATE]
        val SELECTED = EnumHelper[STATE]
        val UNSELECTED = EnumHelper[STATE]
        val ENABLED = EnumHelper[STATE]
        val DISABLED = EnumHelper[STATE]
    }
}

/* base unit */

open class ChartUnit {
    open var extras: MutableMap<String, Any>? = null
    open var parent: ChartUnit? = null
    open var children: MutableList<ChartUnit>? = null
    open var chartView: ChartView? = null
    open var aboutItems: MutableMap<Int, ChartUnitStyle>? = null
}

open class ChartArea(open var xys: MutableList<Float>) : ChartUnit()

open class ChartText(open var text: String) : ChartUnit()

/* high-level unit */

open class ChartAxis

open class ChartLegend
