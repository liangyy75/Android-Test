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
import com.liang.example.basic_ktx.EnumHelper
import com.liang.example.basic_ktx.MutablePair
import kotlin.math.sqrt

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
    }

    open fun drawItem(canvas: Canvas, item: ChartItemBase) {}

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
 * chart中的border
 */
open class ChartBorder {
    open var borderColor: Int = 0
    open var borderWidth: Float = 0f
    open var borderRadius: Float = 0f
    open var borderStyle: Int = ChartLineBase.Style.SOLID
    open var dashArray: MutableList<Float>? = null
    open var useSpace: Boolean = true

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

open class ChartPosition {
    open var x: Float = Invalid.INVALID_XY
    open var y: Float = Invalid.INVALID_XY
    open var xOffset: Float = Invalid.INVALID_XYOffset
    open var yOffset: Float = Invalid.INVALID_XYOffset
    open var width: Float = Invalid.INVALID_WH
    open var height: Float = Invalid.INVALID_WH
    open var xMode: Int = Mode.ABS
    open var yMode: Int = Mode.ABS
    open var wMode: Int = Mode.ABS
    open var hMode: Int = Mode.ABS

    object Mode {
        const val POS_MODE = "posMode"
        val ABS = EnumHelper[POS_MODE]
        val ABS_TO_PARENT = EnumHelper[POS_MODE]
        val REL_TO_PARENT = EnumHelper[POS_MODE]
        val REL_TO_ROOT = EnumHelper[POS_MODE]  // 即相对于ChartView
    }

    object Invalid {
        const val INVALID_XY = -1f
        const val INVALID_XYOffset = -1f
        const val INVALID_WH = -1f
    }
}

/**
 * 组成Chart的所有组件共同的祖先，定义所有组件共同的事件
 */
open class ChartItemBase(open var color: Int) {
    open var changed: Boolean = true
    open var clickListener: ClickListener? = null
    open var state: Int = ChartState.NORMAL
    open var aboutItems: MutableMap<Int, ChartItemBase>? = null
    open var zIndex: Int = 0
    open var position: ChartPosition? = null  // 不要主动set，尽量用pos/size
    open var border: ChartBorder? = null
        set(value) {
            field = value
            changed = true
        }
    open var background: ChartBackground? = null
        set(value) {
            field = value
            changed = true
        }
    open var parentItem: ChartItemBase? = null
        set(value) {
            field = value
            updatePosOffset()
            childItems?.forEach {
                it.parentItem = this
                it.updatePosOffset()
            }
        }
    open var childItems: MutableList<ChartItemBase>? = null
        set(value) {
            field = value
            childItems?.forEach {
                it.parentItem = this
                it.updatePosOffset()
            }
        }
    open var chartView: ChartView? = null
        set(value) {
            field = value
            updatePosOffset()
            childItems?.forEach {
                it.chartView = field
                it.updatePosOffset()
            }
        }

    open fun pos(x: Float, y: Float, xMode: Int = ChartPosition.Mode.ABS, yMode: Int = ChartPosition.Mode.ABS) {
        if (this.position == null) {
            this.position = ChartPosition()
        }
        val pos = this.position!!
        if (pos.x == x && pos.y == y && pos.xMode == xMode && pos.yMode == yMode) {
            return
        }
        pos.x = x
        pos.y = y
        pos.xMode = xMode
        pos.yMode = yMode
        updatePosOffset(false)
        childItems?.forEach {
            it.updatePosOffset()
        }
    }

    protected open fun updatePosOffset(care: Boolean = true) {
        val pos = this.position ?: return
        val x = pos.x
        val y = pos.y
        val xMode = pos.xMode
        val yMode = pos.yMode
        val posParent = this.parentItem?.position
        val oldXOff = pos.xOffset
        val oldYOff = pos.yOffset
        if (!care || (xMode != ChartPosition.Mode.ABS && xMode != ChartPosition.Mode.ABS_TO_PARENT)) {
            pos.xOffset = when {
                xMode == ChartPosition.Mode.ABS_TO_PARENT && posParent != null && posParent.xOffset != ChartPosition.Invalid.INVALID_XYOffset ->
                    posParent.xOffset + x
                xMode == ChartPosition.Mode.REL_TO_PARENT && posParent != null && posParent.xOffset != ChartPosition.Invalid.INVALID_XYOffset
                        && posParent.width != ChartPosition.Invalid.INVALID_WH -> posParent.xOffset + x * posParent.width
                xMode == ChartPosition.Mode.ABS -> x
                chartView != null -> this.chartView!!.measuredWidth * x
                else -> ChartPosition.Invalid.INVALID_XY
            }
        }
        if (!care || (yMode != ChartPosition.Mode.ABS && yMode != ChartPosition.Mode.ABS_TO_PARENT)) {
            pos.yOffset = when {
                yMode == ChartPosition.Mode.ABS_TO_PARENT && posParent != null && posParent.yOffset != ChartPosition.Invalid.INVALID_XYOffset ->
                    posParent.yOffset + y
                yMode == ChartPosition.Mode.REL_TO_PARENT && posParent != null && posParent.yOffset != ChartPosition.Invalid.INVALID_XYOffset
                        && posParent.height != ChartPosition.Invalid.INVALID_WH -> posParent.yOffset + y * posParent.height
                yMode == ChartPosition.Mode.ABS -> y
                chartView != null -> this.chartView!!.measuredHeight * y
                else -> ChartPosition.Invalid.INVALID_XY
            }
        }
        changed = pos.xOffset != oldXOff || pos.yOffset != oldYOff
    }

    open fun size(width: Float, height: Float, wMode: Int = ChartPosition.Mode.ABS, hMode: Int = ChartPosition.Mode.ABS) {
        if (this.position == null) {
            this.position = ChartPosition()
        }
        val pos = this.position!!
        if (pos.width == width && pos.height == height && pos.wMode == wMode && pos.hMode == hMode) {
            return
        }
        pos.width = width
        pos.height = height
        pos.wMode = wMode
        pos.hMode = hMode
        childItems?.forEach {
            it.updatePosOffset()
        }
        changed = true
    }

    open fun inArea(x: Float, y: Float): Boolean {
        val pos = position ?: return false
        return x >= pos.xOffset && x <= pos.xOffset + pos.width && y >= pos.yOffset && y <= pos.yOffset + pos.height
    }

    open fun addChild(child: ChartItemBase) {
        child.parentItem = this
        if (this.childItems == null) {
            this.childItems = mutableListOf(child)
        } else if (!this.childItems!!.contains(child)) {
            this.childItems!!.add(child)
        } else {
            return
        }
        changed = true
    }

    open fun removeChild(child: ChartItemBase) {
        if (this.childItems != null && this.childItems!!.contains(child)) {
            child.parentItem = null
            this.childItems!!.remove(child)
            changed = true
        }
    }

    open fun handleChild(field: ChartItemBase?, value: ChartItemBase?) {
        if (field == value) {
            return
        }
        if (value != null) {
            addChild(value)
        }
        if (field != null) {
            removeChild(field)
        }
    }

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
        val UNTOUCHED = EnumHelper[STATE]
        val ENABLED = EnumHelper[STATE]
        val DISABLED = EnumHelper[STATE]
    }
}

/**
 * chart中的线
 */
open class ChartLineBase(
        color: Int,
        _lineWidth: Float
) : ChartItemBase(color) {
    open var lineWidth: Float = _lineWidth
        set(value) {
            field = value
            changed = true
        }
    open var style: Int = Style.SOLID
        set(value) {
            field = value
            changed = true
        }
    open var startStyle: Int = EndStyle.NONE
        set(value) {
            field = value
            changed = true
        }
    open var endStyle: Int = EndStyle.NONE
        set(value) {
            field = value
            changed = true
        }
    open var text: ChartText? = null
        set(value) {
            handleChild(field, value)
            field = value
            changed = true
        }
    open var dashArray: MutableList<Float>? = null
        set(value) {
            field = value
            changed = true
        }
    open var length: Float = -1f
        set(value) {
            field = value
            changed = true
        }

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
        _shapeStyle: Int,
        _contentStyle: Int,
        _size: Float,
        color: Int
) : ChartItemBase(color) {
    open var shapeStyle: Int = _shapeStyle
        set(value) {
            field = value
            changed = true
        }
    open var contentStyle: Int = _contentStyle
        set(value) {
            field = value
            changed = true
        }
    open var size: Float = _size
        set(value) {
            field = value
            changed = true
        }
    open var textMargin: Float = 0f
        set(value) {
            field = value
            changed = true
        }
    open var strokeColor: Int = Color.WHITE
        set(value) {
            field = value
            changed = true
        }
    open var strokeSize: Float = -1f
        set(value) {
            field = value
            changed = true
        }

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
open class ChartAreaBase(_xys: MutableList<Float>) : ChartItemBase(0) {
    open var xys: MutableList<Float> = _xys
        set(value) {
            field = value
            changed = true
        }
    open var shapeStyle: Int = Style.POLYGON
        set(value) {
            field = value
            changed = true
        }
    open var startAngle: Float = 0f
        set(value) {
            field = value
            changed = true
        }
    open var endAngle: Float = 360f
        set(value) {
            field = value
            changed = true
        }

    object Style {
        const val AREA_STYLE = "areaStyle"
        val CIRCLE = EnumHelper[AREA_STYLE]
        val OVAL = EnumHelper[AREA_STYLE]
        val ARC = EnumHelper[AREA_STYLE]
        val POLYGON = EnumHelper[AREA_STYLE]
    }
}

open class ChartText(
        _text: String,
        _size: Float,
        color: Int = Color.BLACK
) : ChartItemBase(color) {
    open var text: String = _text
        set(value) {
            field = value
            changed = true
        }
    open var size: Float = _size
        set(value) {
            field = value
            changed = true
        }
    open var weight: Int = Weight.NORMAL
        set(value) {
            field = value
            changed = true
        }
    open var align: Int = Align.CENTER
        set(value) {
            field = value
            changed = true
        }
    open var angle: Float = 0f
        set(value) {
            field = value
            changed = true
        }
    open var decoration: Int = Decoration.NONE
        set(value) {
            field = value
            changed = true
        }
    open var isItalic: Boolean = false
        set(value) {
            field = value
            changed = true
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
        set(value) {
            field = value
            changed = true
        }
    override var align: Int = Align.CENTER_HORIZONTAL.or(Align.TOP)
        set(value) {
            field = value
            changed = true
        }

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
        _name: String,
        _value: String,
        _position: Float,
        _color: Int = Color.GRAY
) : ChartItemBase(_color) {
    open var name: String = _name
        set(value) {
            field = value
            changed = true
        }
    open var value: String = _value
        set(value) {
            field = value
            changed = true
        }
    open var axisPos: Float = _position
        set(value) {
            field = value
            changed = true
        }
    open var base: ChartSymbolBase? = null
        set(value) {
            field = value
            changed = true
        }
    open var text: ChartText? = null
        set(value) {
            handleChild(field, value)
            field = value
            changed = true
        }

    object ShapeType {
        val NONE = EnumHelper[ChartSymbolBase.ShapeType.SYMBOL_SHAPE_TYPE]
        val DOT = EnumHelper[ChartSymbolBase.ShapeType.SYMBOL_SHAPE_TYPE]
        val DOWN_LINE = EnumHelper[ChartSymbolBase.ShapeType.SYMBOL_SHAPE_TYPE]
        val UP_LINE = EnumHelper[ChartSymbolBase.ShapeType.SYMBOL_SHAPE_TYPE]
    }
}

open class ChartAxis(
        _max: Float,
        _min: Float,
        _lineWidth: Float,
        color: Int = Color.GRAY
) : ChartItemBase(color) {
    open var max: Float = _max
        set(value) {
            field = value
            changed = true
        }
    open var min: Float = _min
        set(value) {
            field = value
            changed = true
        }
    open var lineWidth: Float = _lineWidth
        set(value) {
            field = value
            changed = true
        }
    open var symbols: MutableList<ChartAxisSymbol>? = null
        set(value) {
            field?.forEach {
                removeChild(it)
            }
            field = value
            field?.forEach {
                addChild(it)
            }
            changed = true
        }
    open var symbolStyle: ChartSymbolBase = ChartSymbolBase(ChartAxisSymbol.ShapeType.DOWN_LINE, ChartSymbolBase.ContentType.NORMAL, _lineWidth, Color.GRAY)
        set(value) {
            handleChild(field, value)
            field = value
            changed = true
        }
    open var symbolTextStyle: ChartText = ChartText("", 10f, Color.GRAY).apply {
        weight = ChartText.Weight.THIN
        align = ChartText.Align.CENTER
    }
        set(value) {
            handleChild(field, value)
            field = value
            changed = true
        }
    open var title: ChartText? = null
        set(value) {
            handleChild(field, value)
            field = value
            changed = true
        }

    open var symbolDotRadius: Float = -1f
        set(value) {
            field = value
            changed = true
        }
    open var symbolLineLength: Float = 10f
        set(value) {
            field = value
            changed = true
        }
    open var symbolTextMargin: Float = 5f  // margin between symbol and text
        set(value) {
            field = value
            changed = true
        }

    open var rotation: Float = 0f
        set(value) {
            field = value
            changed = true
        }
    open var reversed: Boolean = false
        set(value) {
            field = value
            changed = true
        }
    open var opposite: Boolean = false
        set(value) {
            field = value
            changed = true
        }

    open var offset: Float = 0f
        set(value) {
            field = value
            changed = true
        }
    open var symbolStep: Int = 0
        set(value) {
            field = value
            changed = true
        }

    open var lines: MutableMap<Float, ChartLineBase>? = null
        set(value) {
            field?.forEach {
                removeChild(it.value)
            }
            field = value
            field?.forEach {
                addChild(it.value)
            }
            changed = true
        }
    open var areas: MutableList<ChartAreaBase>? = null
        set(value) {
            field?.forEach {
                removeChild(it)
            }
            field = value
            field?.forEach {
                addChild(it)
            }
            changed = true
        }

    open fun addSymbol(symbol: ChartAxisSymbol) {
        if (this.symbols == null) {
            this.symbols = mutableListOf(symbol)
        } else if (!this.symbols!!.contains(symbol)) {
            addChild(symbol)
            this.symbols!!.add(symbol)
        }
    }

    open fun removeSymbol(symbol: ChartAxisSymbol) {
        removeChild(symbol)
        this.symbols?.remove(symbol)
    }

    open fun addLine(pos: Float, line: ChartLineBase) {
        if (this.lines == null) {
            this.lines = mutableMapOf(pos to line)
        } else if (!this.lines!!.containsKey(pos)) {
            addChild(line)
            this.lines!![pos] = line
        }
    }

    open fun removeLine(line: ChartLineBase) {
        removeChild(line)
        val lines = this.lines ?: return
        var key: Float? = null
        for ((pos, line2) in lines) {
            if (line2 == line) {
                key = pos
                break
            }
        }
        if (key != null) {
            lines.remove(key)
        }
    }

    open fun addArea(area: ChartAreaBase) {
        if (this.areas == null) {
            this.areas = mutableListOf(area)
        } else if (!this.areas!!.contains(area)) {
            addChild(area)
            this.areas!!.add(area)
        }
    }

    open fun removeArea(area: ChartAreaBase) {
        removeChild(area)
        this.areas?.remove(area)
    }
}

/**
 * chart中每类数据对应的legend(标记)
 */
open class ChartLegend(_text: ChartText) : ChartItemBase(0) {
    open var text: ChartText = _text
        set(value) {
            handleChild(field, value)
            field = value
            changed = true
        }
    open var symbol: ChartSymbolBase? = null
        set(value) {
            handleChild(field, value)
            field = value
            changed = true
        }

    object ShapeType {
        val LINE_AND_SYMBOL = EnumHelper[ChartSymbolBase.ShapeType.SYMBOL_SHAPE_TYPE]
        val SYMBOL = EnumHelper[ChartSymbolBase.ShapeType.SYMBOL_SHAPE_TYPE]
        val LINE = EnumHelper[ChartSymbolBase.ShapeType.SYMBOL_SHAPE_TYPE]
    }
}

open class ChartData(_x: Float, _y: Float) : ChartItemBase(0) {
    open var x: Float = _x
    open var y: Float = _y
    open var x2: Float = -1f
    open var y2: Float = -1f
    open var extra: MutableMap<String, Any>? = null
    open var showLegend: Boolean = false
    open var label: ChartText? = null
    open var legend: ChartLegend? = null
    open var subDataList: ChartDataList? = null
    open var subChartModel: ChartModel? = null
    open var selectedAll: Boolean = false
}

open class ChartDataList(
        open var dataList: MutableList<MutablePair<Boolean, ChartData>>,
        open var name: String
) : ChartItemBase(0) {
    open var commonLegend: ChartLegend? = null
    open var symbols: MutableList<ChartSymbolBase>? = null  // ChartData对应的Symbol
    open var splits: MutableMap<Int, Boolean>? = null
    open var circlePanels: MutableList<ChartAreaBase>? = null  // 这是由dataList生成的，不要去设置
    open var circlePanelMargin: Float = 10f
}

open class ChartToolTip(
        open var titleTextFormat: ChartTitleText,
        open var contentTextFormat: ChartText,
        open var alignStyle: Int = Align.TOP.or(Align.CENTER),
        open var offset: Float = 0f,
        open var margin: Float = 0f,
        open var contentTextFormats: MutableList<ChartText>? = null
) : ChartAreaBase(mutableListOf()) {
    open fun getContentTextFormat(item: ChartItemBase): ChartText? = null

    object Align {
        const val TOOL_TIP_ALIGN_STYLE = "toolTipShapeStyle"
        val LEFT = EnumHelper.get2(TOOL_TIP_ALIGN_STYLE)
        val RIGHT = EnumHelper.get2(TOOL_TIP_ALIGN_STYLE)
        val TOP = EnumHelper.get2(TOOL_TIP_ALIGN_STYLE)
        val BOTTOM = EnumHelper.get2(TOOL_TIP_ALIGN_STYLE)
        val CENTER = EnumHelper.get2(TOOL_TIP_ALIGN_STYLE)
    }
}

open class ChartLegendPanel(open var legends: MutableList<ChartLegend>) : ChartItemBase(0)

open class ChartModel(open var type: Int) {
    open var title: ChartTitleText? = null
    open var subTitle: ChartTitleText? = null
    open var zoom: Int = ZoomType.ZOOM_NONE
    open var xAxis: ChartAxis? = null
    open var yAxis: ChartAxis? = null
    open var xAxisArr: MutableList<ChartAxis>? = null
        set(value) {
            field = value
            field?.forEach {
                it.rotation = 90f
            }
        }
    open var yAxisArr: MutableList<ChartAxis>? = null

    open var legend: ChartLegend? = null
    open var legends: MutableList<ChartLegend>? = null  // 同时用做labels
    open var legendPanel: ChartLegendPanel? = null
    open var dataList: ChartDataList? = null
    open var dataLists: MutableList<MutablePair<Int, ChartDataList>>? = null  // first是style
    open var dataLabel: ChartText? = null

    open var background: ChartBackground? = null
    open var border: ChartBorder? = null
    open var innerBorder: ChartBorder? = null
    open var position: ChartPosition? = null

    open var ignoreHiddenData: Boolean = true
    open var gridClickable: Boolean = false
    open var stacking: Int = StackStyle.STACK_NONE  // 是否将图表每个数据列的值叠加在一起

    open var barRadius: Float? = null
    open var lineStyle: Int = ChartLineBase.Style.SOLID
    open var startPieAngle: Float = 0f
    open var endPieAngle: Float = 360f

    open var items: MutableList<ChartItemBase>? = null

    object Style {
        const val CHART_STYLE = "charStyle"
        val BAR = EnumHelper.get2(CHART_STYLE)
        val AREA = EnumHelper.get2(CHART_STYLE)
        val LINE = EnumHelper.get2(CHART_STYLE)
        val SCATTER = EnumHelper.get2(CHART_STYLE)
        val PIE = EnumHelper.get2(CHART_STYLE)
        val AREASPLINE = EnumHelper.get2(CHART_STYLE)
        val SPLINE = EnumHelper.get2(CHART_STYLE)
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

    object BarStyle {
        const val BAR_STYLE = "barStyle"
        val NORMAL = EnumHelper[BAR_STYLE]
        val TRIANGLE = EnumHelper[BAR_STYLE]
        val CIRCLE = EnumHelper[BAR_STYLE]
        val OVAL = EnumHelper[BAR_STYLE]
    }

    object StackStyle {
        const val STACK_STYLE = "stackStyle"
        val STACK_NONE = EnumHelper[STACK_STYLE]
        val STACK_NORMAL = EnumHelper[STACK_STYLE]
        val STACK_PERCENT = EnumHelper[STACK_STYLE]
        val STACK_GROUP = EnumHelper[STACK_STYLE]  // 分组堆叠
        val STACK_GROUP_PERCENT = EnumHelper[STACK_STYLE]  // 百分比分组堆叠
    }

    object ZoomType {
        const val ZOOM_TYPE = "zoomType"
        val ZOOM_NONE = EnumHelper[ZOOM_TYPE]
        val ZOOM_X = EnumHelper[ZOOM_TYPE]
        val ZOOM_Y = EnumHelper[ZOOM_TYPE]
        val ZOOM_XY = EnumHelper[ZOOM_TYPE]
    }
}

// todo: 面积范围图 https://www.highcharts.com.cn/demo/highcharts/arearange、迷你图 https://www.highcharts.com.cn/demo/highcharts/sparkline、
//  流图 https://www.highcharts.com.cn/demo/highcharts/streamgraph、韦恩图 https://www.highcharts.com.cn/demo/highcharts/venn-diagram、
//  欧拉图 https://www.highcharts.com.cn/demo/highcharts/euler-diagram、3D气泡图 https://www.highcharts.com.cn/demo/highcharts/bubble-3d、
//  气泡填充图 https://www.highcharts.com.cn/demo/highcharts/packed-bubble
