@file:Suppress("unused")

package com.liang.example.viewtest.chart

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View

object EnumHelper {
    private val map = mutableMapOf<String, Int>()

    operator fun get(name: String): Int {
        val result = map[name]?.plus(1) ?: 1
        map[name] = result
        return result
    }

    fun get2(name: String): Int {
        val result = map[name]?.times(2) ?: 1
        map[name] = result
        return result
    }
}

open class ChartView : View {
    constructor(c: Context) : super(c)
    constructor(c: Context, attributeSet: AttributeSet?) : super(c, attributeSet)
    constructor(c: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(c, attributeSet, defStyleAttr)
}

open class ChartText(
        open var text: String,
        open var color: Long = Color.BLACK.toLong(),
        open var size: Float = 12f,
        open var weight: Int = NORMAL,
        open var align: Int = CENTER_HORIZONTAL.or(TOP),
        open var angle: Float = 0f
) {
    open var x: Int = -1
    open var y: Int = -1
    open var clickListener: OnClickListener? = null

    open fun pos(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    interface OnClickListener {
        fun onClick() = Unit
        fun onLongClick() = Unit
    }

    companion object {
        private const val TEXT_ALIGN = "textAlign"
        val NONE = EnumHelper.get2(TEXT_ALIGN)
        val LEFT = EnumHelper.get2(TEXT_ALIGN)
        val RIGHT = EnumHelper.get2(TEXT_ALIGN)
        val TOP = EnumHelper.get2(TEXT_ALIGN)
        val BOTTOM = EnumHelper.get2(TEXT_ALIGN)
        val CENTER = EnumHelper.get2(TEXT_ALIGN)
        val CENTER_VERTICAL = EnumHelper.get2(TEXT_ALIGN)
        val CENTER_HORIZONTAL = EnumHelper.get2(TEXT_ALIGN)

        private const val FONT_WEIGHT = "fontWeight"
        val THIN = EnumHelper[FONT_WEIGHT]
        val NORMAL = EnumHelper[FONT_WEIGHT]
        val BOLD = EnumHelper[FONT_WEIGHT]
    }
}

open class ChartSymbolStyle(open var shapeStyle: Int, open var contentStyle: Int, open var size: Float, open var textMargin: Float) {
    companion object {
        const val SYMBOL_SHAPE_TYPE = "symbolShapeType"
        val NONE = EnumHelper[SYMBOL_SHAPE_TYPE]
        val CIRCLE = EnumHelper[SYMBOL_SHAPE_TYPE]
        val SQUARE = EnumHelper[SYMBOL_SHAPE_TYPE]
        val DIAMOND = EnumHelper[SYMBOL_SHAPE_TYPE]
        val TRIANGLEDOWN = EnumHelper[SYMBOL_SHAPE_TYPE]
        val TRIANGLE = EnumHelper[SYMBOL_SHAPE_TYPE]

        private const val SYMBOL_CONTENT_TYPE = "symbolContentType"
        val FILL = EnumHelper[SYMBOL_CONTENT_TYPE]
        val INNER_BLACK = EnumHelper[SYMBOL_CONTENT_TYPE]
        val BORDER_BLACK = EnumHelper[SYMBOL_CONTENT_TYPE]
    }
}

open class ChartAxisSymbol(open var name: String, open var value: String, open var position: Float)

open class ChartAxisSymbol2(
        n: String, v: String, p: Float,
        open val shapeStyle: Int = NONE,
        open val contentStyle: Int = ChartSymbolStyle.FILL
) : ChartAxisSymbol(n, v, p) {
    companion object {
        val NONE = EnumHelper[ChartSymbolStyle.SYMBOL_SHAPE_TYPE]
        val DOT = EnumHelper[ChartSymbolStyle.SYMBOL_SHAPE_TYPE]
        val DOWN_LINE = EnumHelper[ChartSymbolStyle.SYMBOL_SHAPE_TYPE]
        val UP_LINE = EnumHelper[ChartSymbolStyle.SYMBOL_SHAPE_TYPE]
    }
}

open class ChartAxis(
        open var coordinators: MutableList<ChartAxisSymbol>,
        open var coordinatorStyle: ChartText = ChartText("", Color.GRAY.toLong(), 10f, ChartText.THIN, ChartText.NONE),
        open var title: ChartText? = null,
        open var style: Int = ChartAxisSymbol2.DOT,
        open var color: Long = Color.GRAY.toLong(),
        open var width: Float = 2f
) {
    open var symbolDotRadius: Float = -1f
    open var symbolLineLength: Float = 10f
    open var symbolTextMargin: Float = 5f  // margin between symbol and text
}

open class ChartArea(open var xStartPos: Float, open var xEndPos: Float, open var yStartPos: Float, open var yEndPos: Float, open var bgColor: Long) {}

open class ChartData() {}

open class ChartModel(
        open var type: Int,
        open var subTitle: ChartText? = null,
        open var title: ChartText? = null,
        open var stacking: Int = STACK_NONE,  // 是否将图表每个数据列的值叠加在一起
        open var zoom: Int = ZOOM_NONE,
        open var xAxis: ChartAxis? = null,
        open var yAxis: ChartAxis? = null,
        open var bgColor: Long = Color.WHITE.toLong()
) {
    companion object {
        private const val CHART_STYLE = "charStyle"
        val COLUMN = EnumHelper.get2(CHART_STYLE)
        val BAR = EnumHelper.get2(CHART_STYLE)
        val AREA = EnumHelper.get2(CHART_STYLE)
        val AREASPLINE = EnumHelper.get2(CHART_STYLE)
        val LINE = EnumHelper.get2(CHART_STYLE)
        val SPLINE = EnumHelper.get2(CHART_STYLE)
        val SCATTER = EnumHelper.get2(CHART_STYLE)
        val PIE = EnumHelper.get2(CHART_STYLE)
        val BUBBLE = EnumHelper.get2(CHART_STYLE)
        val PYRAMID = EnumHelper.get2(CHART_STYLE)
        val FUNNEL = EnumHelper.get2(CHART_STYLE)
        val COLUMNRANGE = EnumHelper.get2(CHART_STYLE)
        val AREARANGE = EnumHelper.get2(CHART_STYLE)
        val AREASPLINERANGE = EnumHelper.get2(CHART_STYLE)
        val BOXPLOT = EnumHelper.get2(CHART_STYLE)
        val WATERFALL = EnumHelper.get2(CHART_STYLE)
        val POLYGON = EnumHelper.get2(CHART_STYLE)
        val GAUGE = EnumHelper.get2(CHART_STYLE)
        val ERRORBAR = EnumHelper.get2(CHART_STYLE)

        private const val STACK_STYLE = "stackStyle"
        val STACK_NONE = EnumHelper[STACK_STYLE]
        val STACK_NORMAL = EnumHelper[STACK_STYLE]
        val STACK_PERCENT = EnumHelper[STACK_STYLE]

        private const val ZOOM_TYPE = "zoomType"
        val ZOOM_NONE = EnumHelper[ZOOM_TYPE]
        val ZOOM_X = EnumHelper[ZOOM_TYPE]
        val ZOOM_Y = EnumHelper[ZOOM_TYPE]
        val ZOOM_XY = EnumHelper[ZOOM_TYPE]
    }
}
