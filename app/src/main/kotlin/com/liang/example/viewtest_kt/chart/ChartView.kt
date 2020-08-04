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
import com.liang.example.basic_ktx.addIfNotContainsOrNull
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

    open fun drawItem(canvas: Canvas, item: ChartItemBase) {
        if (item.state == ChartItemBase.ChartState.NONE) {
            return
        }
        mainPaint.reset()
        strokePaint.reset()
        mainPaint.color = item.color
    }

    open fun drawText(canvas: Canvas, text: ChartText, _x: Float?, _y: Float?, bgColor: Int) {
        val x = _x ?: text.position?.x ?: return
        val y = _y ?: text.position?.y ?: return
        canvas.rotate(text.rotation, x, y) {
            mainPaint.textSize = text.size
            mainPaint.textAlign = text.toPaintAlign()
            if (text.decoration == ChartText.Decoration.BELOW) {
                mainPaint.isUnderlineText = true
            } else if (text.decoration == ChartText.Decoration.ABOVE) {
                mainPaint.isStrikeThruText = true
            }
            if (text.isItalic) {
                mainPaint.textSkewX = -0.5f
            }
            if (text.weight == ChartText.Weight.BOLD) {
                mainPaint.isFakeBoldText = true
            }
            drawText(text.text, x, y, mainPaint)
        }
    }

    open fun drawSymbol(canvas: Canvas, symbol: ChartSymbolBase, _x: Float?, _y: Float?, bgColor: Int /* chart's bg or area's bg */) {
        val x = _x ?: symbol.position?.x ?: return
        val y = _y ?: symbol.position?.y ?: return
        when (symbol.contentStyle) {
            ChartSymbolBase.ContentType.INNER_EMPTY -> {
                mainPaint.color = bgColor
                strokePaint.color = symbol.color
            }
            ChartSymbolBase.ContentType.BORDER_EMPTY -> strokePaint.color = bgColor
            ChartSymbolBase.ContentType.INNER_AND_BORDER -> strokePaint.color = symbol.strokeColor
            ChartAxisSymbol.ShapeType.DOWN_LINE, ChartAxisSymbol.ShapeType.UP_LINE -> mainPaint.strokeWidth = symbol.size
        }
        var drawStroke = false
        if (symbol.contentStyle != ChartSymbolBase.ContentType.NORMAL) {
            drawStroke = true
            strokePaint.strokeWidth = symbol.strokeSize
        }
        val halfSize = symbol.size / 2
        when (symbol.shapeStyle) {
            ChartSymbolBase.ShapeType.CIRCLE -> canvas.mainAndStroke(mainPaint, strokePaint, drawStroke) { drawCircle(x, y, halfSize, it) }
            ChartSymbolBase.ShapeType.SQUARE -> canvas.mainAndStroke(mainPaint, strokePaint, drawStroke) {
                drawRect(x - halfSize, y - halfSize, x + halfSize, y + halfSize, it)
            }
            ChartSymbolBase.ShapeType.DIAMOND -> canvas.rotate(45f, x, y) {
                canvas.mainAndStroke(mainPaint, strokePaint, drawStroke) { drawRect(x - halfSize, y - halfSize, x + halfSize, y + halfSize, it) }
            }
            ChartSymbolBase.ShapeType.TRIANGLE -> drawTriangleSymbol(halfSize, symbol, x, y, canvas, false, drawStroke)
            ChartSymbolBase.ShapeType.TRIANGLEDOWN -> drawTriangleSymbol(halfSize, symbol, x, y, canvas, true, drawStroke)
            ChartAxisSymbol.ShapeType.DOWN_LINE -> canvas.drawLine(x, y, x, y + symbol.length, mainPaint)
            ChartAxisSymbol.ShapeType.UP_LINE -> canvas.drawLine(x, y, x, y - symbol.length, mainPaint)
        }
    }

    protected open fun drawTriangleSymbol(halfSize: Float, base: ChartSymbolBase, x: Float, y: Float, canvas: Canvas, down: Boolean,
                                          drawStroke: Boolean) {
        var temp2 = base.size * SQUARE_ROOT_3 / 6
        var temp3 = base.size / SQUARE_ROOT_3
        if (down) {
            temp2 = -temp2
            temp3 = -temp3
        }
        val path = Path()
        path.fillType = Path.FillType.EVEN_ODD
        path.moveTo(x - halfSize, y + temp2)
        path.lineTo(x + halfSize, y + temp2)
        path.lineTo(x, y - temp3)
        path.close()
        canvas.mainAndStroke(mainPaint, strokePaint, drawStroke) { drawPath(path, it) }
    }

    open fun drawAxis(canvas: Canvas, axis: ChartAxis, _x: Float?, _y: Float?, bgColor: Int) {
        val x = _x ?: axis.position?.x ?: return
        val y = _y ?: axis.position?.y ?: return
        canvas.rotate(axis.rotation, x, y) {
            val x2 = x + axis.lineLength
            val y2 = y + axis.lineWidth + axis.offset
            this.drawLines(floatArrayOf(x, y, x2, y2), mainPaint)
            val base = axis.lineLength / (axis.max - axis.min)
            val y3 = y + axis.lineWidth / 2 + axis.offset
            axis.symbols?.forEachIndexed { index, it ->
                if (index % axis.symbolStep != 0) {
                    return@forEachIndexed
                }
                if (it.length == -1f) {
                    it.length = axis.symbolLineLength
                }
                val x3 = when {
                    axis.reversed -> x2 - it.axisPos * base
                    else -> it.axisPos * base + x
                }
                val symbol = it.base ?: axis.symbolStyle
                drawSymbol(canvas, symbol, x3, y2, bgColor)
                val spaces = symbol.getSpaces()
                drawText(this, it.text ?: axis.symbolTextStyle.apply {
                    text = it.value
                }, x3, when {
                    axis.textOpposite -> y2 + spaces[1] - symbol.textMargin
                    else -> y2 + spaces[3] + symbol.textMargin
                }, bgColor)
            }
            axis.title
            axis.areas?.forEach {
            }
            axis.lines?.forEach {
            }
        }
    }

    companion object {
        val SQUARE_ROOT_2 = sqrt(2f)
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
    open var rotation: Float = 0f
        set(value) {
            field = value
            changed = true
        }
    open var extra: MutableMap<String, Any>? = null

    open fun addExtra(key: String, value: Any) {
        if (extra == null) {
            extra = mutableMapOf(key to value)
        } else {
            extra!![key] = value
        }
    }

    open fun removeExtra(key: String): Any? {
        return extra?.remove(key)
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
            changed = true
        } else {
            changed = this.childItems!!.addIfNotContainsOrNull(child)
        }
    }

    open fun removeChild(child: ChartItemBase) {
        if (this.childItems != null && this.childItems!!.contains(child)) {
            child.parentItem = null
            if (this.childItems!!.remove(child)) {
                changed = true
            }
        }
    }

    open fun removeChildAt(index: Int) {
        removeChild(childItems?.getOrNull(index) ?: return)
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
    open var length: Float = -1f
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

    open fun getSpaces(): FloatArray {
        var halfSize = size / 2
        if (contentStyle != ContentType.NORMAL) {
            halfSize += strokeSize
        }
        return when (shapeStyle) {
            ShapeType.CIRCLE -> floatArrayOf(halfSize, halfSize, halfSize, halfSize)
            ShapeType.SQUARE -> floatArrayOf(halfSize, halfSize, halfSize, halfSize)
            ShapeType.DIAMOND -> {
                val temp = halfSize * ChartView.SQUARE_ROOT_2
                floatArrayOf(temp, temp, temp, temp)
            }
            ShapeType.TRIANGLE -> floatArrayOf(halfSize, size * ChartView.SQUARE_ROOT_3 / 6, halfSize, size / ChartView.SQUARE_ROOT_3)
            ShapeType.TRIANGLEDOWN -> floatArrayOf(halfSize, size / ChartView.SQUARE_ROOT_3, halfSize, size * ChartView.SQUARE_ROOT_3 / 6)
            ChartAxisSymbol.ShapeType.DOWN_LINE -> floatArrayOf(halfSize, 0f, halfSize, length)
            ChartAxisSymbol.ShapeType.UP_LINE -> floatArrayOf(halfSize, length, halfSize, 0f)
            else -> floatArrayOf(0f, 0f, 0f, 0f)
        }
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
        _value: String,
        _position: Float,
        _color: Int = Color.GRAY
) : ChartItemBase(_color) {
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
            if (value != null && value.length == -1f) {
                value.length = this.length
            }
            changed = true
        }
    open var text: ChartText? = null
        set(value) {
            handleChild(field, value)
            field = value
            changed = true
        }
    open var length: Float = -1f
        set(value) {
            field = value
            if (base != null && base!!.length == -1f) {
                base!!.length = value
            }
            changed = true
        }

    object ShapeType {
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
    open var lineLength: Float = -1f
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

    open var symbolLineLength: Float = 10f
        set(value) {
            field = value
            changed = true
        }
    open var symbolTextMargin: Float = 5f  // margin between symbol and text
        set(value) {
            field = value
            this.symbolStyle.textMargin = value
            changed = true
        }

    open var reversed: Boolean = false
        set(value) {
            field = value
            changed = true
        }
    open var textOpposite: Boolean = false
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

    open fun removeSymbolAt(index: Int) {
        removeSymbol(this.symbols?.getOrNull(index) ?: return)
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

    open fun removeLineAt(pos: Float) {
        removeLine(this.lines?.getOrElse(pos, { null }) ?: return)
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
        set(value) {
            field = value
            changed = true
        }
    open var y: Float = _y
        set(value) {
            field = value
            changed = true
        }
    open var x2: Float = -1f
        set(value) {
            field = value
            changed = true
        }
    open var y2: Float = -1f
        set(value) {
            field = value
            changed = true
        }
    open var showLegend: Boolean = false
        set(value) {
            field = value
            changed = true
        }
    open var label: ChartText? = null
        set(value) {
            handleChild(field, value)
            field = value
            changed = true
        }
    open var legend: ChartLegend? = null
        set(value) {
            handleChild(field, value)
            field = value
            changed = true
        }
    open var subDataList: ChartDataList? = null
        set(value) {
            handleChild(field, value)
            field = value
            changed = true
        }
    open var subChartModel: ChartModel? = null
        set(value) {
            field = value
            changed = true
        }
}

open class ChartDataList(
        _name: String,
        _dataList: MutableList<ChartData> = mutableListOf()
) : ChartItemBase(0) {
    open var dataList: MutableList<ChartData> = _dataList
    open var name: String = _name
        set(value) {
            field = value
            changed = true
        }
    open var commonLegend: ChartLegend? = null
        set(value) {
            handleChild(field, value)
            field = value
            changed = true
        }
    open var symbols: MutableList<ChartSymbolBase>? = null  // ChartData对应的Symbol
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
    open var splits: MutableMap<Int, Boolean>? = null
        set(value) {
            field = value
            changed = true
        }
    open var panels: MutableList<ChartAreaBase>? = null  // 这是由dataList生成的，不要去设置
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
    open var panelMargin: Float = 10f
        set(value) {
            field = value
            changed = true
        }
    open var selectedAll: Boolean = false
        set(value) {
            field = value
            changed = true
        }

    open fun addData(data: ChartData) {
        dataList.add(data)
        changed = true
    }

    open fun removeData(data: ChartData) {
        if (dataList.remove(data)) {
            changed = true
        }
    }

    open fun removeDataAt(index: Int) {
        removeData(dataList.getOrNull(index) ?: return)
    }

    open fun addSymbol(symbol: ChartSymbolBase) {
        addChild(symbol)
        if (symbols == null) {
            symbols = mutableListOf(symbol)
        } else if (!symbols!!.contains(symbol)) {
            addChild(symbol)
            symbols!!.add(symbol)
        }
    }

    open fun removeSymbol(symbol: ChartSymbolBase) {
        removeChild(symbol)
        symbols?.remove(symbol)
    }

    open fun removeSymbolAt(index: Int) {
        removeSymbol(symbols?.getOrNull(index) ?: return)
    }

    open fun setSplit(index: Int, split: Boolean) {
        if (splits == null && !split) {
            splits = mutableMapOf(index to split)
            changed = true
        } else if (splits != null && splits!![index] != split) {
            splits!![index] = split
            changed = true
        }
    }

    open fun addPanel(panel: ChartAreaBase) {
        if (panels == null) {
            panels = mutableListOf(panel)
        } else if (!panels!!.contains(panel)) {
            addChild(panel)
            panels!!.add(panel)
        }
    }

    open fun removePanel(panel: ChartAreaBase) {
        this.panels?.remove(panel)
        removeChild(panel)
    }

    open fun removePanelAt(index: Int) {
        removePanel(this.panels?.getOrNull(index) ?: return)
    }
}

open class ChartToolTip(
        _titleFormat: ChartTitleText,
        _contentFormat: ChartText
) : ChartAreaBase(mutableListOf()) {
    open var titleFormat: ChartTitleText = _titleFormat
        set(value) {
            handleChild(field, value)
            field = value
            changed = true
        }
    open var contentFormat: ChartText = _contentFormat
        set(value) {
            handleChild(field, value)
            field = value
            changed = true
        }
    open var alignStyle: Int = Align.TOP.or(Align.CENTER)
        set(value) {
            field = value
            changed = true
        }
    open var offset: Float = 0f
        set(value) {
            field = value
            changed = true
        }
    open var margin: Float = 0f
        set(value) {
            field = value
            changed = true
        }
    open var contentTextFormats: MutableList<ChartText>? = null
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
    open var contentHolder: ContentHolder? = null
        set(value) {
            field = value
            changed = true
        }

    open fun getContentTextFormat(index: Int): ChartText? = contentHolder?.getContentTextFormat(index)

    object Align {
        const val TOOL_TIP_ALIGN_STYLE = "toolTipShapeStyle"
        val LEFT = EnumHelper.get2(TOOL_TIP_ALIGN_STYLE)
        val RIGHT = EnumHelper.get2(TOOL_TIP_ALIGN_STYLE)
        val TOP = EnumHelper.get2(TOOL_TIP_ALIGN_STYLE)
        val BOTTOM = EnumHelper.get2(TOOL_TIP_ALIGN_STYLE)
        val CENTER = EnumHelper.get2(TOOL_TIP_ALIGN_STYLE)
    }

    interface ContentHolder {
        fun getContentTextFormat(index: Int): ChartText?
    }
}

open class ChartLegendPanel(_legends: MutableList<ChartLegend>) : ChartItemBase(0) {
    open var legends: MutableList<ChartLegend> = _legends
        set(value) {
            field = value
            changed = true
        }
}

open class ChartModel(_type: Int) {
    open var type: Int = _type
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
