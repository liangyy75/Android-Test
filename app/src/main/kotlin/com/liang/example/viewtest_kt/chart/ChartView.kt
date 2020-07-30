@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.liang.example.viewtest_kt.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.liang.example.androidtest.R
import com.liang.example.utils.r.dp2px
import kotlin.math.sqrt

/**
 * 用来帮助Enum的，并且方便扩展、删除
 */
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

/**
 * 保留原本状态，之后旋转，然后action，最后恢复
 */
fun Canvas.rotate(degrees: Float, px: Float, py: Float, action: Canvas.() -> Unit) {
    this.save()
    this.rotate(degrees, px, py)
    this.action()
    this.restore()
}

/**
 * 绘制Chart的View
 */
@Suppress("LeakingThis")
open class ChartView : View {
    constructor(c: Context) : super(c)
    constructor(c: Context, attributeSet: AttributeSet?) : super(c, attributeSet)
    constructor(c: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(c, attributeSet, defStyleAttr)

    open val mainPaint = Paint()
    open val strokePaint = Paint()

    init {
        mainPaint.style = Paint.Style.FILL
        strokePaint.style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        testSymbol(canvas)
        testAxis(canvas)
    }

    protected open fun testSymbol(canvas: Canvas) {
        val dp5 = dp2px(5f).toFloat()
        val dp10 = dp5 * 2
        val dp30 = dp10 * 3
        val dp50 = dp10 * 5
        val resources = context.resources
        val white = Color.WHITE
        val red = resources.getColor(R.color.red300)
        val green = context.resources.getColor(R.color.green300)
        val style = ChartSymbolBase(ChartSymbolBase.ShapeType.NONE, ChartSymbolBase.ContentType.NORMAL, dp30, red)
                .apply {
                    strokeSize = dp5
                    strokeColor = green
                }
        (1..4).forEach { style2 ->
            style.contentStyle = style2
            (2..6).forEach { style1 ->
                style.shapeStyle = style1
                drawSymbol(canvas, style, dp50 * (style1 - 1), dp50 * style2, white)
            }
        }
    }

    protected open fun testAxis(canvas: Canvas) {}

    open fun drawSymbol(canvas: Canvas, base: ChartSymbolBase, x: Float, y: Float, bgColor: Int /* chart's bg or area's bg */) {
        when (base.contentStyle) {
            ChartSymbolBase.ContentType.NORMAL -> mainPaint.color = base.color
            ChartSymbolBase.ContentType.INNER_EMPTY -> {
                mainPaint.color = bgColor
                strokePaint.color = base.color
            }
            ChartSymbolBase.ContentType.BORDER_EMPTY -> {
                mainPaint.color = base.color
                strokePaint.color = bgColor
            }
            ChartSymbolBase.ContentType.INNER_AND_BORDER -> {
                mainPaint.color = base.color
                strokePaint.color = base.strokeColor
            }
        }
        var drawStroke = false
        if (base.contentStyle != ChartSymbolBase.ContentType.NORMAL) {
            drawStroke = true
            strokePaint.strokeWidth = base.strokeSize
        }
        when (base.shapeStyle) {
            ChartSymbolBase.ShapeType.NONE -> Unit
            ChartSymbolBase.ShapeType.CIRCLE -> drawCircleSymbol(canvas, x, y, base, drawStroke)
            ChartSymbolBase.ShapeType.SQUARE -> drawSquareSymbol(base, canvas, x, y, drawStroke)
            ChartSymbolBase.ShapeType.DIAMOND -> canvas.rotate(45f, x, y) { drawSquareSymbol(base, this, x, y, drawStroke) }
            ChartSymbolBase.ShapeType.TRIANGLE -> drawTriangleSymbol(base, x, y, canvas, false, drawStroke)
            ChartSymbolBase.ShapeType.TRIANGLEDOWN -> drawTriangleSymbol(base, x, y, canvas, true, drawStroke)
        }
    }

    protected open fun drawCircleSymbol(canvas: Canvas, x: Float, y: Float, base: ChartSymbolBase, drawStroke: Boolean) {
        val halfSize = base.size / 2
        canvas.drawCircle(x, y, halfSize, mainPaint)
        if (drawStroke) {
            canvas.drawCircle(x, y, halfSize, strokePaint)
        }
    }

    protected open fun drawTriangleSymbol(base: ChartSymbolBase, x: Float, y: Float, canvas: Canvas, down: Boolean, drawStroke: Boolean) {
        val temp = base.size / 2
        var temp2 = base.size * SQUARE_ROOT_3 / 6
        var temp3 = base.size / SQUARE_ROOT_3
        if (down) {
            temp2 = -temp2
            temp3 = -temp3
        }
        val path = Path()
        path.fillType = Path.FillType.EVEN_ODD
        path.moveTo(x - temp, y + temp2)
        path.lineTo(x + temp, y + temp2)
        path.lineTo(x, y - temp3)
        path.close()
        canvas.drawPath(path, mainPaint)
        if (drawStroke) {
            canvas.drawPath(path, strokePaint)
        }
    }

    protected open fun drawSquareSymbol(base: ChartSymbolBase, canvas: Canvas, x: Float, y: Float, drawStroke: Boolean) {
        val temp = base.size / 2
        canvas.drawRect(x - temp, y - temp, x + temp, y + temp, mainPaint)
        if (drawStroke) {
            canvas.drawRect(x - temp, y - temp, x + temp, y + temp, strokePaint)
        }
    }

    companion object {
        val SQUARE_ROOT_3 = sqrt(3f)
    }
}

/**
 * 组成Chart的所有组件共同的祖先，定义所有组件共同的事件
 */
open class ChartItemBase(open var color: Int) {
    open var clickListener: ClickListener? = null
    open var state: Int = ChartState.NORMAL
    open var aboutItems: MutableMap<Int, ChartItemBase>? = null
    open var zIndex: Int = 0

    // TODO: 判断点在area中

    interface ClickListener {
        fun onClick(base: ChartItemBase, x: Float, y: Float)
        fun onLongClick(base: ChartItemBase, x: Float, y: Float)
    }

    object ChartState {
        const val STATE = "state"
        val NONE = EnumHelper[STATE]  // 也就是平时看不见
        val NORMAL = EnumHelper[STATE]
        val TOUCHING = EnumHelper[STATE]
        val TOUCHED = EnumHelper[STATE]
        val ENABLED = EnumHelper[STATE]
        val DISABLED = EnumHelper[STATE]
    }
}

/**
 * chart中的border
 */
open class ChartBorder {
    open var borderColor: Int = 0
    open var borderWidth: Float = 0f
    open var borderRadius: Float = 0f
    open var borderStyle: Int = ChartLineBase.Style.SOLID
    open var dashArray: MutableList<Float>? = null

    open var padding: Float = 0f
    open var paddingLeft: Float = 0f
    open var paddingRight: Float = 0f
    open var paddingTop: Float = 0f
    open var paddingBottom: Float = 0f
}

/**
 * chart中的background
 */
open class ChartBackground(
        open var bgColor: Int,
        open var bgImage: Int = 0,
        open var bgDrawable: Drawable? = null
)

/**
 * chart中的线
 */
open class ChartLineBase(
        color: Int,
        open var width: Float
) : ChartItemBase(color) {
    open var style: Int = Style.SOLID
    open var startStyle: Int = EndStyle.NONE
    open var endStyle: Int = EndStyle.NONE
    open var text: ChartText? = null
    open var dashArray: MutableList<Float>? = null
    open var length: Float = -1f

    object Style {
        const val LINE_STYLE = "lineStyle"
        val SOLID = EnumHelper[LINE_STYLE]
        val DASH = EnumHelper[LINE_STYLE]
        val DOUBLE = EnumHelper[LINE_STYLE]
    }

    object EndStyle {
        const val LINE_END_STYLE = "lineEndStyle"
        val NONE = EnumHelper[LINE_END_STYLE]
        val ROUND = EnumHelper[LINE_END_STYLE]
        // TODO: 箭头、三角等其他线条两端样式
    }
}

/**
 * chart中的symbol，可以是 “数据点”、“x/y轴的数值标记”、“每类数据对应的legend”等等
 */
open class ChartSymbolBase(
        open var shapeStyle: Int,
        open var contentStyle: Int,
        open var size: Float,
        color: Int
) : ChartItemBase(color) {
    open var textMargin: Float = 0f
    open var strokeColor: Int = Color.WHITE
    open var strokeSize: Float = -1f

    object ShapeType {
        const val SYMBOL_SHAPE_TYPE = "symbolShapeType"
        val NONE = EnumHelper[SYMBOL_SHAPE_TYPE]
        val CIRCLE = EnumHelper[SYMBOL_SHAPE_TYPE]  // size是直径
        val SQUARE = EnumHelper[SYMBOL_SHAPE_TYPE]  // size是边长
        val DIAMOND = EnumHelper[SYMBOL_SHAPE_TYPE]  // size是边长
        val TRIANGLEDOWN = EnumHelper[SYMBOL_SHAPE_TYPE]  // size是边长
        val TRIANGLE = EnumHelper[SYMBOL_SHAPE_TYPE]  // size是边长
    }

    object ContentType {
        const val SYMBOL_CONTENT_TYPE = "symbolContentType"
        val NORMAL = EnumHelper[SYMBOL_CONTENT_TYPE]
        val INNER_EMPTY = EnumHelper[SYMBOL_CONTENT_TYPE]
        val BORDER_EMPTY = EnumHelper[SYMBOL_CONTENT_TYPE]
        val INNER_AND_BORDER = EnumHelper[SYMBOL_CONTENT_TYPE]
    }
}

/**
 * chart中的area
 */
open class ChartAreaBase(open var xys: MutableList<Float>) : ChartItemBase(0) {
    open var background: ChartBackground? = null
    open var border: ChartBorder? = null
}

open class ChartText(
        open var text: String,
        open var size: Float,
        color: Int = Color.BLACK
) : ChartItemBase(color) {
    open var weight: Int = Weight.NORMAL
    open var align: Int = Align.CENTER
    open var angle: Float = 0f
    open var x: Int = -1
    open var y: Int = -1
    open var decoration: Int = Decoration.NONE
    open var border: ChartBorder? = null
    open var background: ChartBackground? = null

    open fun pos(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    object Align {
        const val TEXT_ALIGN = "textAlign"
        val START = EnumHelper.get2(TEXT_ALIGN)
        val END = EnumHelper.get2(TEXT_ALIGN)
        val CENTER = EnumHelper.get2(TEXT_ALIGN)
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

open class ChartTitleText(
        text: String, size: Float, color: Int = Color.BLACK
) : ChartText(text, size, color) {
    override var weight: Int = Weight.BOLD
    override var align: Int = Align.CENTER_HORIZONTAL.or(Align.TOP)

    object Align {
        const val TEXT_ALIGN = "textAlign"
        val LEFT = EnumHelper.get2(TEXT_ALIGN)
        val RIGHT = EnumHelper.get2(TEXT_ALIGN)
        val TOP = EnumHelper.get2(TEXT_ALIGN)
        val BOTTOM = EnumHelper.get2(TEXT_ALIGN)
        val CENTER = EnumHelper.get2(TEXT_ALIGN)
        val CENTER_VERTICAL = EnumHelper.get2(TEXT_ALIGN)
        val CENTER_HORIZONTAL = EnumHelper.get2(TEXT_ALIGN)
    }
}

open class ChartAxisSymbol(
        open var name: String,
        open var value: String,
        open var position: Float
) {
    open var base: ChartSymbolBase? = null
    open var textStyle: ChartText? = null

    object ShapeType {
        val NONE = EnumHelper[ChartSymbolBase.ShapeType.SYMBOL_SHAPE_TYPE]
        val DOT = EnumHelper[ChartSymbolBase.ShapeType.SYMBOL_SHAPE_TYPE]
        val DOWN_LINE = EnumHelper[ChartSymbolBase.ShapeType.SYMBOL_SHAPE_TYPE]
        val UP_LINE = EnumHelper[ChartSymbolBase.ShapeType.SYMBOL_SHAPE_TYPE]
    }
}

open class ChartAxis(
        open var max: Float,
        open var min: Float,
        open var width: Float,
        color: Int = Color.GRAY
) : ChartItemBase(color) {
    open var symbols: MutableList<ChartAxisSymbol>? = null
    open var symbolStyle: ChartSymbolBase = ChartSymbolBase(ChartAxisSymbol.ShapeType.DOWN_LINE, ChartSymbolBase.ContentType.NORMAL, width, Color.GRAY)
    open var symbolTextStyle: ChartText = ChartText("", 10f, Color.GRAY).apply {
        weight = ChartText.Weight.THIN
        align = ChartText.Align.CENTER
    }
    open var title: ChartText? = null

    open var symbolDotRadius: Float = -1f
    open var symbolLineLength: Float = 10f
    open var symbolTextMargin: Float = 5f  // margin between symbol and text

    open var reversed: Boolean = false
    open var opposite: Boolean = false

    open var offset: Float = 0f
    open var symbolStep: Int = 0

    open var lines: MutableMap<Float, ChartLineBase>? = null
    open var areas: MutableList<ChartAreaBase>? = null

    // TODO: inGrid
}

open class ChartData

open class ChartToolTip(
        open var textFormat: ChartTitleText,
        xys: MutableList<Float>
) : ChartAreaBase(xys)

open class ChartPane : ChartItemBase(0) {
    open var startAngle: Float = 0f
    open var endAngle: Float = 0f
    open var background: ChartBackground? = null
    open var strokeWidth: Float = 0f
    open var strokeColor: Int = 0
}

/**
 * chart中每类数据对应的legend(标记)
 */
open class ChartLegend(
        open var text: ChartText,
        open var width: Float,
        open var height: Float,
        open var symbol: ChartSymbolBase
) : ChartItemBase(0) {
    open var border: ChartBorder? = null
    open var background: ChartBackground? = null
}

open class ChartModel(open var type: Int) {
    open var title: ChartTitleText? = null
    open var subTitle: ChartTitleText? = null
    open var zoom: Int = ZoomType.ZOOM_NONE
    open var xAxis: ChartAxis? = null
    open var yAxis: ChartAxis? = null
    open var xAxisArr: MutableList<ChartAxis>? = null
    open var yAxisArr: MutableList<ChartAxis>? = null

    open var legend: ChartLegend? = null

    open var width: Float = 0f
    open var height: Float = 0f

    open var background: ChartBackground? = null
    open var border: ChartBorder? = null
    open var innerBorder: ChartBorder? = null

    open var ignoreHiddenData: Boolean = true
    open var gridClickable: Boolean = false
    open var stacking: Int = StackStyle.STACK_NONE  // 是否将图表每个数据列的值叠加在一起

    object Style {
        const val CHART_STYLE = "charStyle"
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
    }

    object StackStyle {
        const val STACK_STYLE = "stackStyle"
        val STACK_NONE = EnumHelper[STACK_STYLE]
        val STACK_NORMAL = EnumHelper[STACK_STYLE]
        val STACK_PERCENT = EnumHelper[STACK_STYLE]
    }

    object ZoomType {
        const val ZOOM_TYPE = "zoomType"
        val ZOOM_NONE = EnumHelper[ZOOM_TYPE]
        val ZOOM_X = EnumHelper[ZOOM_TYPE]
        val ZOOM_Y = EnumHelper[ZOOM_TYPE]
        val ZOOM_XY = EnumHelper[ZOOM_TYPE]
    }
}
