@file:Suppress("unused")

package com.liang.example.viewtest_kt.chart2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathEffect
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.liang.example.basic_ktx.EnumHelper
import com.liang.example.utils.r.dp2px
import com.liang.example.utils.r.getDrawable
import kotlin.math.min

/**
 * @author liangyuying
 * @date 2020/8/3
 * <p>
 * chart view
 */
open class ChartView : View {
    constructor(c: Context) : super(c)
    constructor(c: Context, attributeSet: AttributeSet?) : super(c, attributeSet)
    constructor(c: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(c, attributeSet, defStyleAttr)

    open val mainPaint = Paint()
    open val strokePaint = Paint()
    open val textPaint = Paint()
    open val unit = ChartUnit().apply {
        this.styles = mutableMapOf(ChartUnit.State.NORMAL to ChartUnitStyle().apply {
            this.position = ChartPosition().apply {
                this.left = dp2px(100f).toFloat()
                this.top = dp2px(200f).toFloat()
                this.width = dp2px(200f).toFloat()
                this.height = dp2px(100f).toFloat()
            }
            this.background = ChartBackground().apply {
                this.color = Color.RED
            }
        })
        updatePos()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        drawItem(canvas, unit)
    }

    open fun drawItem(canvas: Canvas, unit: ChartUnit) {
        val style = unit.styles?.get(unit.state)
                ?: return returnLog("style is null while state is ${unit.state}", Unit)
        val position = style.position ?: return returnLog("position is null", Unit)
        mainPaint.reset()
        val transform = style.transform
        if (transform != null) {
            canvas.save()
            transform.sequence.split(";").forEach {
                if (it == "translate" && (transform.translateX != 0f || transform.translateY != 0f)) {
                    canvas.translate(transform.translateX, transform.translateY)
                } else if (it == "scale" && (transform.scaleX != 0f || transform.scaleY != 0f)) {
                    canvas.scale(transform.scaleX, transform.scaleY)
                } else if (it == "rotate" && transform.rotation != 0f) {
                    canvas.rotate(transform.rotation)
                } else if (it == "skew" && (transform.skewX != 0f || transform.skewY != 0f)) {
                    canvas.skew(transform.skewX, transform.skewY)
                }
            }
            transform.transform?.doTransform(canvas)
        }
        val border = style.border
        val background = style.background
        style.pathStyle?.apply {
            strokePaint.reset()
            strokePaint.strokeCap = toPaintCap()
            strokePaint.strokeJoin = toPaintJoin()
            strokePaint.pathEffect = effect
        }
        val left = position.trueLeft
        val top = position.trueTop
        val width = position.trueWidth
        val height = position.trueHeight
        if (border != null || background != null) {
            val path = Path()
            val bgBitmap = background?.getBgBitmap()
            if (bgBitmap == null) {
                mainPaint.color = background?.color ?: Color.WHITE
            } else {
                val bitmapShader = BitmapShader(bgBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                val scaledMatrix = Matrix()
                scaledMatrix.setScale(width / bgBitmap.width, height / bgBitmap.height)
                bitmapShader.setLocalMatrix(scaledMatrix)
                mainPaint.shader = bitmapShader
            }
            if (border != null) {
                strokePaint.strokeWidth = border.borderWidth
                strokePaint.color = border.borderColor
            }
            if (unit is ChartArea) {
                val xys = unit.xys
                (0 until xys.size step 2).forEach { i ->
                    if (i == 0) {
                        path.moveTo(xys[i], xys[i + 1])
                    } else {
                        path.lineTo(xys[i], xys[i + 1])
                    }
                }
                if (unit.close) {
                    path.close()
                }
            } else {
                val dir = style.pathStyle?.toPathDirection() ?: Path.Direction.CCW
                val size = min(width, height)
                when (style.shape) {
                    ChartUnitStyle.ShapeType.NONE -> Unit
                    ChartUnitStyle.ShapeType.CIRCLE -> path.addCircle(left + width / 2, top + height / 2, size / 2, dir)
                    ChartUnitStyle.ShapeType.SQUARE -> path.addRect(left + (width - size) / 2, top + (height - size) / 2, left + (width + size) / 2,
                            top + (height + size) / 2, dir)
                    ChartUnitStyle.ShapeType.RECANTAGE -> path.addRect(left, top, left + width, top + height, dir)
                    ChartUnitStyle.ShapeType.ROUND_RECTANGLE -> path.addRoundRect(left, top, left + width, top + height, style.roundRadius,
                            style.roundRadius, dir)
                    ChartUnitStyle.ShapeType.DIAMOND -> path.apply {
                        moveTo(left + width / 2, top)
                        lineTo(left + width, top + height / 2)
                        lineTo(left + width / 2, top + height)
                        lineTo(left, top + height / 2)
                        lineTo(left + width / 2, top)
                    }
                    ChartUnitStyle.ShapeType.TRIANGLE -> path.apply {
                        moveTo(left, top + height)
                        lineTo(left + width / 2, top)
                        lineTo(left + width, top + height)
                        lineTo(left, top + height)
                    }
                    ChartUnitStyle.ShapeType.TRIANGLEDOWN -> path.apply {
                        moveTo(left + width / 2, top + height)
                        lineTo(left, top)
                        lineTo(left + width, top)
                        lineTo(left + width / 2, top + height)
                    }
                    else -> Unit
                }
            }
            canvas.drawPath(path, mainPaint)
            if (border != null) {
                canvas.drawPath(path, strokePaint)
            }
        }
        val textStyle = style.textStyle
        if (textStyle?.text != null) {
            textStyle.apply {
                val text = this.text!!
                textPaint.reset()
                textPaint.color = color
                textPaint.textSize = size
                textPaint.isFakeBoldText = isBold()
                textPaint.textAlign = toPaintAlign()
                when (decoration) {
                    ChartTextStyle.Decoration.THROUGH -> textPaint.isStrikeThruText = true
                    ChartTextStyle.Decoration.BELOW -> textPaint.isUnderlineText = true
                    else -> Unit  // TODO: above
                }
                if (isItalic) {
                    textPaint.flags = Paint.UNDERLINE_TEXT_FLAG.or(textPaint.flags)
                }
                val fm = textPaint.fontMetrics
                canvas.drawText(text, left + width / 2, top + height / 2 - (fm.bottom + fm.top) / 2, textPaint)
            }
        }
        if (transform != null) {
            canvas.restore()
        }
    }

    open fun <T> returnLog(msg: String, value: T): T {
        Log.d(TAG, msg)
        return value
    }

    companion object {
        private val TAG = ChartView::class.java.simpleName
    }
}

/* unit's style */

open class ChartPosition {
    open var width: Float = -1f  /* -1~0是相对于parent的百分比，-3~-2是相对于chartView的百分比，0~是正常数值 */
    open var height: Float = -1f
    open var left: Float? = null
    open var top: Float? = null
    open var right: Float? = null
    open var bottom: Float? = null
    open var mode: Int = PosMode.ALIGN_PARENT

    // TODO: left / top / right/ bottom 各自有单独的mode

    open var trueWidth: Float = 0f
    open var trueHeight: Float = 0f
    open var trueLeft: Float = 0f
    open var trueTop: Float = 0f

    object PosMode {
        const val POS_MODE = "chartItemPosMode"
        val ALIGN_PARENT = EnumHelper[POS_MODE]
        val ALIGN_ROOT = EnumHelper[POS_MODE]
    }
}

open class ChartBorder {
    open var borderWidth: Float = 0f
    open var borderColor: Int = Color.WHITE
}

open class ChartBackground {
    open var color: Int = Color.WHITE
    open var bgResId: Int = 0
    open var bgDrawable: Drawable? = null

    // TODO: bgPos
    // TODO: 渐变色

    open fun getBgBitmap(): Bitmap? {
        val bgDrawable = this.bgDrawable ?: getDrawable(bgResId) ?: return null
        this.bgDrawable = bgDrawable
        if (bgDrawable is BitmapDrawable) {
            return bgDrawable.bitmap
        }
        val w = bgDrawable.intrinsicWidth
        val h = bgDrawable.intrinsicHeight
        val bitmap: Bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        bgDrawable.setBounds(0, 0, w, h)
        bgDrawable.draw(canvas)
        return bitmap
    }
}

open class ChartTransform {
    open var rotation: Float = 0f
    open var scaleX: Float = 1f
    open var scaleY: Float = 1f
    open var translateX: Float = 0f
    open var translateY: Float = 0f
    open var skewX: Float = 0f
    open var skewY: Float = 0f
    open var sequence: String = ""  // "translate;scale;rotate;skew"
    open var transform: Transform? = null

    interface Transform {
        fun doTransform(canvas: Canvas)
    }
}

open class ChartPathStyle {
    open var join: Int = Join.BEVEL
    open var cap: Int = Cap.BUTT
    open var effect: PathEffect? = null
    open var ccwDirection: Boolean = true

    object Cap {
        const val LINE_CAP = "chartLineCap"
        val ROUND = EnumHelper[LINE_CAP]
        val BUTT = EnumHelper[LINE_CAP]
        val SQUARE = EnumHelper[LINE_CAP]
    }

    object Join {
        const val LINE_JOIN = "chartLineJoin"
        val MITER = EnumHelper[LINE_JOIN]
        val ROUND = EnumHelper[LINE_JOIN]
        val BEVEL = EnumHelper[LINE_JOIN]
    }

    open fun toPaintJoin(): Paint.Join = when (join) {
        Join.ROUND -> Paint.Join.ROUND
        Join.MITER -> Paint.Join.MITER
        else -> Paint.Join.BEVEL
    }

    open fun toPaintCap(): Paint.Cap = when (cap) {
        Cap.ROUND -> Paint.Cap.ROUND
        Cap.SQUARE -> Paint.Cap.SQUARE
        else -> Paint.Cap.BUTT
    }

    open fun toPathDirection(): Path.Direction = when (ccwDirection) {
        true -> Path.Direction.CCW
        else -> Path.Direction.CW
    }
}

open class ChartTextStyle {
    open var color: Int = Color.BLACK
    open var size: Float = 12f
    open var weight: Int = Weight.NORMAL
    open var align: Int = Align.CENTER
    open var decoration: Int = Decoration.NONE
    open var isItalic: Boolean = false
    open var text: String? = null

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

    open fun isBold() = weight == Weight.BOLD

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
    open var position: ChartPosition? = null
    open var border: ChartBorder? = null
    open var background: ChartBackground? = null
    open var transform: ChartTransform? = null
    open var textStyle: ChartTextStyle? = null
    open var pathStyle: ChartPathStyle? = null

    open var shape: Int = ShapeType.RECANTAGE  // 在ChartArea中无效
    open var roundRadius: Float = 0f

    object ShapeType {
        const val SYMBOL_SHAPE_TYPE = "symbolShapeType"
        val NONE = EnumHelper[SYMBOL_SHAPE_TYPE]
        val CIRCLE = EnumHelper[SYMBOL_SHAPE_TYPE]  // min(width,height)是直径
        val SQUARE = EnumHelper[SYMBOL_SHAPE_TYPE]  // min(width,height)是边长
        val RECANTAGE = EnumHelper[SYMBOL_SHAPE_TYPE]  // width/height是边长
        val ROUND_RECTANGLE = EnumHelper[SYMBOL_SHAPE_TYPE]  // width/height是边长，roundRadius是边角的半径
        val DIAMOND = EnumHelper[SYMBOL_SHAPE_TYPE]  // width/height是边长
        val TRIANGLE = EnumHelper[SYMBOL_SHAPE_TYPE]  // width/height是底边长/高
        val TRIANGLEDOWN = EnumHelper[SYMBOL_SHAPE_TYPE]  // width/height是底边长/高
    }
}

/* base unit */

open class ChartUnit {
    open var extras: MutableMap<String, Any>? = null
    open var children: MutableList<ChartUnit>? = null
        set(value) {
            field = value
            field?.forEach {
                it.parent = this
                it.chartView = this.chartView
            }
        }
    open var parent: ChartUnit? = null
        set(value) {
            field = value
            children?.forEach {
                it.parent = this
            }
        }
    open var chartView: ChartView? = null
        set(value) {
            field = value
            children?.forEach {
                it.chartView = field
            }
        }
    open var state: Int = State.NORMAL
    open var styles: MutableMap<Int, ChartUnitStyle>? = null
    open var style: ChartUnitStyle?
        get() = styles?.get(state)
        set(value) {
            when {
                value != null -> styles?.put(state, value)
                else -> styles?.remove(state)
            }
        }

    open fun updatePos() {
        val position = this.style?.position ?: return
        val pWidth: Float
        val pHeight: Float
        val pTop: Float
        val pLeft: Float = if (position.mode == ChartPosition.PosMode.ALIGN_PARENT && parent != null) {
            val pPosition = parent!!.style?.position ?: return
            pWidth = pPosition.trueWidth
            pHeight = pPosition.trueHeight
            pTop = pPosition.trueTop
            pPosition.trueLeft
        } else if (position.mode == ChartPosition.PosMode.ALIGN_ROOT && chartView != null) {
            pWidth = chartView!!.measuredWidth.toFloat()
            pHeight = chartView!!.measuredHeight.toFloat()
            pTop = 0f
            0f
        } else {
            return
        }
        position.trueWidth = trueSize(position.width, pWidth)
        position.trueHeight = trueSize(position.height, pHeight)
        val left = position.left
        if (left != null) {
            position.trueLeft = pLeft + trueSize(left, pWidth)
        }
        val right = position.right
        if (right != null) {
            position.trueLeft = pLeft + trueSize2(right, pWidth, position.trueWidth)
        }
        val top = position.top
        if (top != null) {
            position.trueTop = pTop + trueSize(top, pHeight)
        }
        val bottom = position.bottom
        if (bottom != null) {
            position.trueTop = pTop + trueSize2(bottom, pHeight, position.trueHeight)
        }
        children?.forEach {
            it.updatePos()
        }
    }

    protected open fun trueSize(flag: Float, pFlag: Float): Float = when {
        flag < -1f -> flag + 1
        flag >= 0f -> flag
        else -> pFlag * -flag
    }

    protected open fun trueSize2(flag: Float, pFlag: Float, tFlag: Float): Float = when {
        flag >= 0f -> pFlag - tFlag - flag
        flag < -1f -> pFlag - tFlag - flag - 1
        else -> pFlag * (flag + 1) - tFlag
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

open class ChartArea(open var xys: MutableList<Float>) : ChartUnit() {
    open var close: Boolean = true
    open var stroke: Boolean = false
}

/* high-level unit */

open class ChartAxis

open class ChartLegend
