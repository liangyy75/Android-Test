@file:Suppress("unused", "LeakingThis", "BooleanLiteralArgument")

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
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.liang.example.androidtest.R
import com.liang.example.basic_ktx.EnumHelper
import com.liang.example.basic_ktx.getXYByLimitedX
import com.liang.example.basic_ktx.getXYByLimitedY
import com.liang.example.utils.ApiManager
import com.liang.example.utils.r.dp2px
import com.liang.example.utils.r.getColor
import com.liang.example.utils.r.getDrawable
import com.liang.example.utils.r.sp2Px
import kotlin.math.max
import kotlin.math.min

val dp5 = dp2px(5f).toFloat()

// 【canvas/paint/matrix快速上手/复习】https://blog.csdn.net/huaxun66/article/details/52222643

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
    protected open val path = Path()
    open val unit = ChartUnit().style(ChartUnitStyle()
            .position(ChartPosition().mode(ChartPosition.PosMode.ALIGN_ROOT))
            .color(ChartColor().color(Color.WHITE)))
            .chartView(this)
    open var children3: List<ChartUnit>? = null
    open var testFlag = 1

    protected open fun testSymbol() {
        val dp50 = dp5 * 10
        val position = ChartPosition().width(dp50).height(dp50)
        val style = ChartUnitStyle()
                .position(position)
                .padding(ChartPadding(dp5))
                .border(ChartBorder(getColor(R.color.green100), dp5))
                .color(ChartColor(getColor(R.color.red100)))
                .shape(ChartUnitStyle.ShapeType.CIRCLE)
                .textStyle(ChartTextStyle("text"))
        unit.addChild(ChartUnit().style(style))
                .addChild(ChartUnit().style(style.copy()
                        .position(position.copy().left(dp50))
                        .shape(ChartUnitStyle.ShapeType.SQUARE)))
                .addChild(ChartUnit().style(style.copy()
                        .position(position.copy().left(dp50 * 2))
                        .shape(ChartUnitStyle.ShapeType.RECANTAGE)))
                .addChild(ChartUnit().style(style.copy()
                        .position(position.copy().left(dp50 * 3))
                        .shape(ChartUnitStyle.ShapeType.ROUND_RECTANGLE).roundRadius(dp5)))
                .addChild(ChartUnit().style(style.copy()
                        .position(position.copy().left(dp50 * 4))
                        .shape(ChartUnitStyle.ShapeType.DIAMOND)))
                .addChild(ChartUnit().style(style.copy()
                        .position(position.copy().left(dp50 * 5))
                        .shape(ChartUnitStyle.ShapeType.TRIANGLE)))
                .addChild(ChartUnit().style(style.copy()
                        .position(position.copy().left(0f).top(dp50))
                        .shape(ChartUnitStyle.ShapeType.TRIANGLEDOWN)))
                .addChild(ChartUnit().style(style.copy()
                        .position(position.copy().left(dp50).top(dp50))
                        .shape(ChartUnitStyle.ShapeType.POLYGON)
                        .shapeStyle(ChartShapeStyle()
                                .mode(ChartPosition.PosMode.ALIGN_SELF)
                                .xy(0.5f, 0f)
                                .xy(0f, 0.4f)
                                .xy(0.2f, 1f)
                                .xy(0.8f, 1f)
                                .xy(1f, 0.4f))
                        .close(true)))
                .addChild(ChartUnit().style(style.copy()
                        .position(position.copy().left(dp50 * 2).top(dp50).width(dp50 * 4))
                        .shape(ChartUnitStyle.ShapeType.LINE)
                        .border(null)
                        .padding(null)
                        .shapeStyle(ChartShapeStyle().mode(ChartPosition.PosMode.ALIGN_SELF)
                                .xy(0f, 0.3f)
                                .xy(0.1f, 0.2f)
                                .xy(0.13f, 0f)
                                .xy(0.2f, 0.4f)
                                .xy(0.3f, 0.25f)
                                .xy(0.36f, 0.45f)
                                .xy(0.45f, 0.65f)
                                .xy(0.56f, 0.95f)
                                .xy(0.6f, 0.75f)
                                .xy(0.74f, 0.8f)
                                .xy(0.79f, 0.85f)
                                .xy(0.84f, 0.77f)
                                .xy(0.9f, 0.82f)
                                .xy(1f, 0.8f)
                                .lineStyle(ChartBorder(getColor(R.color.green100), dp5 / 2.5f)))))
        val children2 = unit.children!!.map {
            val child = it.deepCopy()
            child.style!!.border(null)
            child.style!!.position!!.top((child.style!!.position!!.top ?: 0f) + dp50 * 2)
            child.xRange(0.2f, 0.8f)
        }
        children3 = unit.children!!.map {
            val child = it.deepCopy()
            child.style!!.position!!.top((child.style!!.position!!.top ?: 0f) + dp50 * 4)
            child.xRange(0f, 1f)
        }
        unit.children!!.addAll(children2)
        unit.children!!.addAll(children3!!)
    }

    protected open fun testBarChart() {
        val yAxis = ChartUnit()
        val color = ChartColor.ofColor(Color.BLACK)

        val childBounds = ChartTextStyle("0.00f").bounds(mainPaint)

        val childStyle = ChartUnitStyle().color(color)
        val childPos = ChartPosition()
                .width(dp5 * 2)
                .height(dp5 / 5f)
                .right(0f)
        listOf(0.1f, 0.25f, 0.36f, 0.51f, 0.63f, 0.78f, 0.9f).forEach {
            yAxis.addChild(ChartUnit().style(childStyle.copy()
                    .position(childPos.copy().top(it, p = true))
                    .textStyle(ChartTextStyle(it.toString()).x(-childBounds.width() / 2f, p = false))))
        }

        val textStyle = ChartTextStyle("y axis")
        val bounds = textStyle.bounds(mainPaint)
        val left = bounds.width() + dp5 * 4f + childBounds.width()
        val height = dp5 * 80 - bounds.height() * 2 - dp5 * 3.5f
        textStyle.x(bounds.width() / 2 - left)
        val position = ChartPosition()
                .width(dp5 / 5)
                .height(height)
                .left(left)
        yAxis.style(ChartUnitStyle()
                .textStyle(textStyle)
                .position(position)
                .color(color))

        val xAxis = ChartUnit()

        childPos.width(dp5 / 5f).height(dp5 * 2).top(1f, p = true)
        listOf(0.1f, 0.3f, 0.45f, 0.7f, 0.8f, 0.95f).forEach {
            xAxis.addChild(ChartUnit().style(childStyle.copy()
                    .position(childPos.copy().left(it, p = true))
                    .textStyle(ChartTextStyle(it.toString()).y(dp5 * 2.5f + childBounds.height() / 2, p = false))))
        }

        xAxis.style(ChartUnitStyle()
                .position(ChartPosition()
                        .width(dp5 * 60 - left)
                        .height(dp5 / 5)
                        .left(left)
                        .top(height))
                .textStyle(ChartTextStyle("x axis")
                        .y(dp5 * 3.5f + childBounds.height() * 1.5f, p = false))
                .color(color))

        unit.addChild(xAxis).addChild(yAxis)
    }

    init {
        when (testFlag) {
            0 -> testSymbol()
            else -> testBarChart()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        unit.update()
        ApiManager.LOGGER.d(TAG, unit.string())
        drawItem(canvas, unit)
    }

    open fun drawItem(canvas: Canvas, unit: ChartUnit) {
        val style = unit.style
                ?: return returnLog("style is null while state is ${unit.state}", Unit)
        val transform = style.transform
        val left = unit.trueLeft
        val top = unit.trueTop
        val width = unit.trueWidth
        val height = unit.trueHeight
        if (transform != null) {
            canvas.save()
            val px = left + width / 2
            val py = top + height / 2
            transform.sequence.split(";").forEach {
                if (it == "translate" && (transform.translateX != 0f || transform.translateY != 0f)) {
                    canvas.translate(transform.translateX, transform.translateY)
                } else if (it == "scale" && (transform.scaleX != 0f || transform.scaleY != 0f)) {
                    canvas.scale(transform.scaleX, transform.scaleY, px, py)
                } else if (it == "rotate" && transform.rotation != 0f) {
                    canvas.rotate(transform.rotation, px, py)
                } else if (it == "skew" && (transform.skewX != 0f || transform.skewY != 0f)) {
                    canvas.skew(transform.skewX, transform.skewY)
                }
            }
            transform.transform?.doTransform(canvas)
        }
        style.strokePathStyle?.set(strokePaint)
        style.contentPathStyle?.set(mainPaint)
        // TODO: draw
        path.reset()
        val pathDir = style.strokePathStyle?.toPathDirection() ?: Path.Direction.CCW
        when (style.shape) {
            ChartUnitStyle.ShapeType.NONE -> Unit
            ChartUnitStyle.ShapeType.CIRCLE -> path.addCircle(unit.trueXys[0], unit.trueXys[1], unit.trueXys[2], pathDir)
            ChartUnitStyle.ShapeType.ROUND_RECTANGLE -> path.addRoundRect(left, top, left + width, top + height, unit.style!!.roundRadius, pathDir)
            else -> {
                xysToPath(when (unit.animXys.size > 0) {
                    true -> unit.animXys
                    else -> unit.trueXys
                }, this.path)
            }
        }
        if (style.close) {
            path.close()
        }
        if (style.shape != ChartUnitStyle.ShapeType.LINE) {
            unit.trueOpXys?.apply {
                val path2 = Path()
                xysToPath(this, path2)
                path.op(path2, unit.pathOp)
            }
        }
        if (style.shape != ChartUnitStyle.ShapeType.NONE) {
            if (style.shape == ChartUnitStyle.ShapeType.LINE) {
                style.shapeStyle?.lineContentStyle?.set(strokePaint)
                canvas.drawPath(path, strokePaint)
            } else {
                style.contentColor?.set(mainPaint, width, height)
                canvas.drawPath(path, mainPaint)
            }
            style.background?.apply {
                set(mainPaint, width, height)
                canvas.drawPath(path, mainPaint)
            }
            style.border?.apply {
                set(strokePaint)
                canvas.drawPath(path, strokePaint)
            }
        }
        val textStyle = style.textStyle
        if (textStyle?.text != null) {
            val text = textStyle.text!!
            textPaint.reset()
            textPaint.color = textStyle.color
            textPaint.textSize = textStyle.size
            textPaint.isFakeBoldText = textStyle.isBold()
            textPaint.textAlign = textStyle.toPaintAlign()
            when (textStyle.decoration) {
                ChartTextStyle.Decoration.THROUGH -> textPaint.isStrikeThruText = true
                ChartTextStyle.Decoration.BELOW -> textPaint.isUnderlineText = true
                else -> Unit  // TODO: above
            }
            if (textStyle.isItalic) {
                textPaint.flags = Paint.UNDERLINE_TEXT_FLAG.or(textPaint.flags)
            }
            val fm = textPaint.fontMetrics
            canvas.drawText(text, unit.trueTextX, unit.trueTextY - (fm.bottom + fm.top) / 2, textPaint)
        }
        unit.children?.forEach {
            drawItem(canvas, it)
        }
        if (transform != null) {
            canvas.restore()
        }
    }

    protected open fun xysToPath(xys: MutableList<Float>, path: Path) {
        path.moveTo(xys[0], xys[1])
        (3 until xys.size step 2).forEach { i ->
            path.lineTo(xys[i - 1], xys[i])
        }
    }

    open fun <T> returnLog(msg: String, value: T): T {
        ApiManager.LOGGER.d(TAG, msg)
        return value
    }

    companion object {
        private val TAG = ChartView::class.java.simpleName
    }
}

/* unit's style */

/* -20~0是相对于parent-width的-1000%~+1000%，-40～-20是相对于parent-height的-1000%~+1000%，0~是正常数值，-40~也是正常数值 */
// position.*
// padding.*
// text.x|y
// shape.xys|opPathXys
// TODO: border.width  transform

const val START_PERCENT_WIDTH = -0
const val MIDDLE_PERCENT_WIDTH = -10
const val END_PERCENT_WIDTH = -20

const val START_PERCENT_HEIGHT = -20
const val MIDDLE_PERCENT_HEIGHT = -30
const val END_PERCENT_HEIGHT = -40

@JvmOverloads
fun posVal(value: Float, width: Boolean = true, percent: Boolean = true): Float = when {
    percent && width -> value + MIDDLE_PERCENT_WIDTH
    percent && !width -> value + MIDDLE_PERCENT_HEIGHT
    !percent && value < 0f -> value + END_PERCENT_HEIGHT
    else -> value
}

open class ChartPosition {
    open var width: Float = -1f
    open var height: Float = -1f
    open var left: Float? = null
    open var top: Float? = null
    open var right: Float? = null
    open var bottom: Float? = null
    open var mode: Int = PosMode.ALIGN_PARENT

    // TODO: left / top / right/ bottom 各自有单独的mode

    open fun left(left: Float, w: Boolean = true, p: Boolean = false): ChartPosition {
        this.left = posVal(left, w, p)
        return this
    }

    open fun top(top: Float, w: Boolean = false, p: Boolean = false): ChartPosition {
        this.top = posVal(top, w, p)
        return this
    }

    open fun right(right: Float, w: Boolean = true, p: Boolean = false): ChartPosition {
        this.right = posVal(right, w, p)
        return this
    }

    open fun bottom(bottom: Float, w: Boolean = false, p: Boolean = false): ChartPosition {
        this.bottom = posVal(bottom, w, p)
        return this
    }

    open fun width(width: Float, w: Boolean = true, p: Boolean = false): ChartPosition {
        this.width = posVal(width, w, p)
        return this
    }

    open fun height(height: Float, w: Boolean = false, p: Boolean = false): ChartPosition {
        this.height = posVal(height, w, p)
        return this
    }

    open fun mode(mode: Int): ChartPosition {
        this.mode = mode
        return this
    }

    open fun copy(): ChartPosition = ChartPosition().apply {
        this.width = this@ChartPosition.width
        this.height = this@ChartPosition.height
        this.left = this@ChartPosition.left
        this.top = this@ChartPosition.top
        this.right = this@ChartPosition.right
        this.bottom = this@ChartPosition.bottom
        this.mode = this@ChartPosition.mode
    }

    object PosMode {
        const val POS_MODE = "chartItemPosMode"
        val ALIGN_PARENT = EnumHelper[POS_MODE]
        val ALIGN_ROOT = EnumHelper[POS_MODE]
        val ALIGN_SELF = EnumHelper[POS_MODE]  // 专门给line/polygon用的
    }

    override fun toString(): String = "{width: $width, height: $height, left: $left, top: $top, right: $right, bottom: $bottom, mode: $mode}"
}

open class ChartBorder() {
    open var borderWidth: Float = 0f
    open var borderColor: Int = Color.WHITE
    // open var inner: Boolean = false  // 表示border是占用width还是不占用，对ChartArea无效
    // TODO: box: content-box, padding-box, border-box

    constructor(borderColor: Int, borderWidth: Float) : this() {
        this.borderColor = borderColor
        this.borderWidth = borderWidth
    }

    open fun borderWidth(borderWidth: Float): ChartBorder {
        this.borderWidth = borderWidth
        return this
    }

    open fun borderColor(borderColor: Int): ChartBorder {
        this.borderColor = borderColor
        return this
    }

    // open fun inner(inner: Boolean): ChartBorder {
    //     this.inner = inner
    //     return this
    // }

    open fun copy() = ChartBorder().apply {
        this.borderColor = this@ChartBorder.borderColor
        this.borderWidth = this@ChartBorder.borderWidth
    }

    open fun set(paint: Paint): ChartBorder {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidth
        paint.color = borderColor
        return this
    }

    override fun toString(): String = "{borderWidth: $borderWidth, borderColor: $borderColor}"
}

open class ChartColor() {
    open var color: Int? = null
    open var bgResId: Int = 0
    open var bgDrawable: Drawable? = null

    constructor(color: Int?) : this() {
        this.color = color
    }

    // TODO: 将color与bg-color分开
    // TODO: bgPos
    // TODO: 渐变色
    // TODO: bg-box: content-box, padding-box, border-box

    open fun color(color: Int?): ChartColor {
        this.color = color
        return this
    }

    open fun id(id: Int): ChartColor {
        this.bgResId = id
        return this
    }

    open fun drawable(drawable: Drawable?): ChartColor {
        this.bgDrawable = drawable
        return this
    }

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

    open fun copy(): ChartColor = ChartColor().apply {
        this.color = this@ChartColor.color
        this.bgResId = this@ChartColor.bgResId
        this.bgDrawable = this@ChartColor.bgDrawable
    }

    open fun set(paint: Paint, w: Float, h: Float): Boolean {
        val bgBitmap = getBgBitmap()
        if (bgBitmap == null) {
            if (color != null) {
                paint.color = color!!
            } else {
                return false
            }
        } else {
            val bitmapShader = BitmapShader(bgBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            val scaledMatrix = Matrix()
            scaledMatrix.setScale(w / bgBitmap.width, h / bgBitmap.height)
            bitmapShader.setLocalMatrix(scaledMatrix)
            paint.reset()
            paint.shader = bitmapShader
        }
        return true
    }

    override fun toString(): String = "{color: $color, bgResId: $bgResId, bgDrawable: $bgDrawable}"

    companion object {
        fun ofColor(color: Int?) = when {
            color == null -> null
            else -> ChartColor(color)
        }

        fun ofBgResId(bgResId: Int?) = when {
            bgResId == null -> null
            else -> ChartColor().id(bgResId)
        }

        fun ofDrawable(drawable: Drawable?) = when {
            drawable == null -> null
            else -> ChartColor().drawable(drawable)
        }
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

    // TODO: px, py

    interface Transform {
        fun doTransform(canvas: Canvas)
    }

    open fun rotation(rotation: Float): ChartTransform {
        this.rotation = rotation
        return this
    }

    open fun scaleX(scaleX: Float): ChartTransform {
        this.scaleX = scaleX
        return this
    }

    open fun scaleY(scaleY: Float): ChartTransform {
        this.scaleY = scaleY
        return this
    }

    open fun s(scaleX: Float, scaleY: Float): ChartTransform {
        this.scaleX = scaleX
        this.scaleY = scaleY
        return this
    }

    open fun translateX(translateX: Float): ChartTransform {
        this.translateX = translateX
        return this
    }

    open fun translateY(translateY: Float): ChartTransform {
        this.translateY = translateY
        return this
    }

    open fun t(translateX: Float, translateY: Float): ChartTransform {
        this.translateX = translateX
        this.translateY = translateY
        return this
    }

    open fun skewX(skewX: Float): ChartTransform {
        this.skewX = skewX
        return this
    }

    open fun skewY(skewY: Float): ChartTransform {
        this.skewY = skewY
        return this
    }

    open fun sk(skewX: Float, skewY: Float): ChartTransform {
        this.skewX = skewX
        this.skewY = skewY
        return this
    }

    open fun sequence(sequence: String): ChartTransform {
        this.sequence = sequence
        return this
    }

    open fun transform(transform: Transform?): ChartTransform {
        this.transform = transform
        return this
    }

    open fun copy(): ChartTransform = ChartTransform().apply {
        this.rotation = this@ChartTransform.rotation
        this.scaleX = this@ChartTransform.scaleX
        this.scaleY = this@ChartTransform.scaleY
        this.translateX = this@ChartTransform.translateX
        this.translateY = this@ChartTransform.translateY
        this.skewX = this@ChartTransform.skewX
        this.skewY = this@ChartTransform.skewY
        this.sequence = this@ChartTransform.sequence
        this.transform = this@ChartTransform.transform
    }

    override fun toString(): String = "{rotation: $rotation, scaleX: $scaleX, scaleY: $scaleY, translateX: $translateX, translateY: $translateY, " +
            "skewX: $skewX, skewY: $skewY, sequence: $sequence, transform: $transform}"
}

open class ChartPathStyle {
    open var join: Int = Join.BEVEL
    open var cap: Int = Cap.BUTT
    open var effect: PathEffect? = null
    open var ccwDirection: Boolean = true
    open var opPath: Path? = null  // TODO

    open fun join(join: Int): ChartPathStyle {
        this.join = join
        return this
    }

    open fun cap(cap: Int): ChartPathStyle {
        this.cap = cap
        return this
    }

    open fun effect(effect: PathEffect?): ChartPathStyle {
        this.effect = effect
        return this
    }

    open fun ccwDirection(ccwDirection: Boolean): ChartPathStyle {
        this.ccwDirection = ccwDirection
        return this
    }

    open fun opPath(opPath: Path?): ChartPathStyle {
        this.opPath = opPath
        return this
    }

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

    open fun copy(): ChartPathStyle = ChartPathStyle().apply {
        this.join = this@ChartPathStyle.join
        this.cap = this@ChartPathStyle.cap
        this.effect = this@ChartPathStyle.effect
        this.ccwDirection = this@ChartPathStyle.ccwDirection
        this.opPath = this@ChartPathStyle.opPath
    }

    open fun set(paint: Paint): ChartPathStyle {
        paint.strokeCap = toPaintCap()
        paint.strokeJoin = toPaintJoin()
        paint.pathEffect = effect
        return this
    }

    override fun toString(): String = "{join: $join, cap: $cap, effect: $effect, direction: $ccwDirection, opPath: $opPath}"
}

open class ChartTextStyle(open var text: String? = null) {
    open var color: Int = Color.BLACK
    open var size: Float = sp2Px(12f).toFloat()
    open var weight: Int = Weight.NORMAL
    open var align: Int = Align.CENTER
    open var decoration: Int = Decoration.NONE
    open var isItalic: Boolean = false

    open var x: Float = posVal(0.5f, true)
    open var y: Float = posVal(0.5f, false)

    open fun color(color: Int): ChartTextStyle {
        this.color = color
        return this
    }

    open fun size(size: Float): ChartTextStyle {
        this.size = size
        return this
    }

    open fun weight(weight: Int): ChartTextStyle {
        this.weight = weight
        return this
    }

    open fun align(align: Int): ChartTextStyle {
        this.align = align
        return this
    }

    open fun decoration(decoration: Int): ChartTextStyle {
        this.decoration = decoration
        return this
    }

    open fun isItalic(isItalic: Boolean): ChartTextStyle {
        this.isItalic = isItalic
        return this
    }

    open fun text(text: String?): ChartTextStyle {
        this.text = text
        return this
    }

    open fun x(x: Float, w: Boolean = true, p: Boolean = true): ChartTextStyle {
        this.x = posVal(x, w, p)
        return this
    }

    open fun y(y: Float, w: Boolean = false, p: Boolean = true): ChartTextStyle {
        this.y = posVal(y, w, p)
        return this
    }

    open fun copy(): ChartTextStyle = ChartTextStyle().apply {
        this.text = this@ChartTextStyle.text
        this.color = this@ChartTextStyle.color
        this.size = this@ChartTextStyle.size
        this.weight = this@ChartTextStyle.weight
        this.align = this@ChartTextStyle.align
        this.decoration = this@ChartTextStyle.decoration
        this.isItalic = this@ChartTextStyle.isItalic
        this.x = this@ChartTextStyle.x
        this.y = this@ChartTextStyle.y
    }

    override fun toString(): String = "{text: $text, color: $color, size: $size, weight: $weight, align: $align, decoration: $decoration, " +
            "isItalic: $isItalic, x: $x, y: $y}"

    open fun set(paint: Paint): ChartTextStyle {
        paint.reset()
        paint.color = color
        paint.textSize = size
        paint.isFakeBoldText = isBold()
        paint.textAlign = toPaintAlign()
        when (decoration) {
            Decoration.THROUGH -> paint.isStrikeThruText = true
            Decoration.BELOW -> paint.isUnderlineText = true
            else -> Unit  // TODO: above
        }
        if (isItalic) {
            paint.flags = Paint.UNDERLINE_TEXT_FLAG.or(paint.flags)
        }
        return this
    }

    open fun bounds(paint: Paint): Rect {
        set(paint)
        val bounds = Rect()
        val text = this.text ?: return bounds
        paint.getTextBounds(text, 0, text.length, bounds)
        return bounds
    }

    open fun toPaintAlign(): Paint.Align = when (this.align) {
        Align.END -> Paint.Align.RIGHT
        Align.CENTER -> Paint.Align.CENTER
        else -> Paint.Align.LEFT
    }

    open fun isBold() = weight == Weight.BOLD

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

    companion object {
        val WIDTH_PAINT = Paint()
    }
}

open class ChartPadding() {
    constructor(padding: Float) : this() {
        this.padding = padding
    }

    open var padding: Float = 0f  /* -1~0是相对于self-width的百分比，-2～-1是相对于self-height的百分比，0~是正常数值，-2~也是正常数值 */
    open var paddingLeft: Float? = null
    open var paddingTop: Float? = null
    open var paddingRight: Float? = null
    open var paddingBottom: Float? = null

    open fun padding(padding: Float, w: Boolean = true, p: Boolean = false): ChartPadding {
        this.padding = posVal(padding, w, p)
        return this
    }

    open fun paddingLeft(paddingLeft: Float, w: Boolean = true, p: Boolean = false): ChartPadding {
        this.paddingLeft = posVal(paddingLeft, w, p)
        return this
    }

    open fun paddingTop(paddingTop: Float, w: Boolean = false, p: Boolean = false): ChartPadding {
        this.paddingTop = posVal(paddingTop, w, p)
        return this
    }

    open fun paddingRight(paddingRight: Float, w: Boolean = true, p: Boolean = false): ChartPadding {
        this.paddingRight = posVal(paddingRight, w, p)
        return this
    }

    open fun paddingBottom(paddingBottom: Float, w: Boolean = false, p: Boolean = false): ChartPadding {
        this.paddingBottom = posVal(paddingBottom, w, p)
        return this
    }

    open fun getPL() = paddingLeft ?: padding
    open fun getPT() = paddingTop ?: padding
    open fun getPR() = paddingRight ?: padding
    open fun getPB() = paddingBottom ?: padding

    open fun copy(): ChartPadding = ChartPadding().apply {
        this.padding = this@ChartPadding.padding
        this.paddingLeft = this@ChartPadding.paddingLeft
        this.paddingTop = this@ChartPadding.paddingTop
        this.paddingRight = this@ChartPadding.paddingRight
        this.paddingBottom = this@ChartPadding.paddingBottom
    }

    override fun toString(): String = "{padding: $padding, pl: $paddingLeft, pt: $paddingTop, pr: $paddingRight, pb: $paddingBottom}"
}

open class ChartShapeStyle {
    open var lineContentStyle: ChartBorder? = null
    open var lineShapeStyle: Int = LineShapeStyle.SOLID  // TODO
    open var xys: MutableList<Float> = mutableListOf()
    open var mode: Int = ChartPosition.PosMode.ALIGN_PARENT
    open var opPathXys: MutableList<Float>? = null
    open var pathOp: Path.Op = Path.Op.INTERSECT

    open fun xy(x: Float, y: Float, xP: Boolean = true, yP: Boolean = true, xw: Boolean = true, yw: Boolean = false): ChartShapeStyle {
        xys.add(posVal(x, xw, xP))
        xys.add(posVal(y, yw, yP))
        return this
    }

    open fun lineStyle(lineStyle: ChartBorder?): ChartShapeStyle {
        this.lineContentStyle = lineStyle
        return this
    }

    open fun lineShapeStyle(lineShapeStyle: Int): ChartShapeStyle {
        this.lineShapeStyle = lineShapeStyle
        return this
    }

    open fun mode(mode: Int): ChartShapeStyle {
        this.mode = mode
        return this
    }

    open fun opXY(x: Float, y: Float, xP: Boolean = true, yP: Boolean = true, xw: Boolean = true, yw: Boolean = false): ChartShapeStyle {
        if (opPathXys == null) {
            opPathXys = mutableListOf(posVal(x, xw, xP), posVal(y, yw, yP))
        } else {
            opPathXys!!.add(posVal(x, xw, xP))
            opPathXys!!.add(posVal(y, yw, yP))
        }
        return this
    }

    open fun pathOp(pathOp: Path.Op): ChartShapeStyle {
        this.pathOp = pathOp
        return this
    }

    open fun copy(): ChartShapeStyle = ChartShapeStyle().other(this)

    open fun other(other: ChartShapeStyle): ChartShapeStyle {
        this.lineContentStyle = other.lineContentStyle
        this.lineShapeStyle = other.lineShapeStyle
        this.xys = other.xys
        this.mode = other.mode
        this.opPathXys = other.opPathXys
        return this
    }

    open fun deepCopy(): ChartShapeStyle = ChartShapeStyle().deepOther(this)

    open fun deepOther(other: ChartShapeStyle): ChartShapeStyle {
        this.lineContentStyle = other.lineContentStyle?.copy()
        this.lineShapeStyle = other.lineShapeStyle
        this.xys = other.xys.toMutableList()
        this.mode = other.mode
        this.opPathXys = other.opPathXys?.toMutableList()
        return this
    }

    override fun toString(): String = "{mode: $mode, lineContentStyle: $lineContentStyle, lineShapeStyle: $lineShapeStyle, pathOp: $pathOp" +
            "xys: ${xys.joinToString("-")}, opXys: ${xys.joinToString("-")}}"

    object LineShapeStyle {
        const val LINE_STYLE = "chartLineShapeStyle"
        val SOLID = EnumHelper[LINE_STYLE]
        val DASH = EnumHelper[LINE_STYLE]
        val DOUBLE = EnumHelper[LINE_STYLE]
    }
}

open class ChartUnitStyle {
    open var zIndex: Int = 0  // TODO: zIndex
    open var position: ChartPosition? = null
    open var border: ChartBorder? = null
    open var contentColor: ChartColor? = null
    open var background: ChartColor? = null  // TODO: background
    open var transform: ChartTransform? = null
    open var textStyle: ChartTextStyle? = null
    open var strokePathStyle: ChartPathStyle? = null
    open var contentPathStyle: ChartPathStyle? = null
    open var padding: ChartPadding? = null
    open var shapeStyle: ChartShapeStyle? = null

    open var shape: Int = ShapeType.RECANTAGE
    open var roundRadius: FloatArray = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    open var close: Boolean = false

    open fun zIndex(zIndex: Int): ChartUnitStyle {
        this.zIndex = zIndex
        return this
    }

    open fun position(position: ChartPosition?): ChartUnitStyle {
        this.position = position
        return this
    }

    open fun border(border: ChartBorder?): ChartUnitStyle {
        this.border = border
        return this
    }

    open fun color(color: ChartColor?): ChartUnitStyle {
        this.contentColor = color
        return this
    }

    open fun background(background: ChartColor?): ChartUnitStyle {
        this.background = background
        return this
    }

    open fun transform(transform: ChartTransform?): ChartUnitStyle {
        this.transform = transform
        return this
    }

    open fun textStyle(textStyle: ChartTextStyle?): ChartUnitStyle {
        this.textStyle = textStyle
        return this
    }

    open fun strokePathStyle(strokePathStyle: ChartPathStyle?): ChartUnitStyle {
        this.strokePathStyle = strokePathStyle
        return this
    }

    open fun contentPathStyle(contentPathStyle: ChartPathStyle?): ChartUnitStyle {
        this.contentPathStyle = contentPathStyle
        return this
    }

    open fun padding(padding: ChartPadding?): ChartUnitStyle {
        this.padding = padding
        return this
    }

    open fun shapeStyle(shapeStyle: ChartShapeStyle?): ChartUnitStyle {
        this.shapeStyle = shapeStyle
        return this
    }

    open fun shape(shape: Int): ChartUnitStyle {
        this.shape = shape
        return this
    }

    open fun roundRadius(roundRadius: FloatArray): ChartUnitStyle {
        this.roundRadius = roundRadius
        return this
    }

    open fun roundRadius(roundRadius: Float): ChartUnitStyle {
        this.roundRadius = floatArrayOf(roundRadius, roundRadius, roundRadius, roundRadius, roundRadius, roundRadius, roundRadius, roundRadius)
        return this
    }

    open fun close(close: Boolean): ChartUnitStyle {
        this.close = close
        return this
    }

    open fun copy(): ChartUnitStyle = ChartUnitStyle().apply {
        this.zIndex = this@ChartUnitStyle.zIndex
        this.position = this@ChartUnitStyle.position
        this.border = this@ChartUnitStyle.border
        this.contentColor = this@ChartUnitStyle.contentColor
        this.background = this@ChartUnitStyle.background
        this.transform = this@ChartUnitStyle.transform
        this.textStyle = this@ChartUnitStyle.textStyle
        this.strokePathStyle = this@ChartUnitStyle.strokePathStyle
        this.contentPathStyle = this@ChartUnitStyle.contentPathStyle
        this.padding = this@ChartUnitStyle.padding
        this.shapeStyle = this@ChartUnitStyle.shapeStyle
        this.close = this@ChartUnitStyle.close
        this.shape = this@ChartUnitStyle.shape
        this.roundRadius = this@ChartUnitStyle.roundRadius
    }

    open fun deepCopy(): ChartUnitStyle = ChartUnitStyle().apply {
        this.zIndex = this@ChartUnitStyle.zIndex
        this.position = this@ChartUnitStyle.position?.copy()
        this.border = this@ChartUnitStyle.border?.copy()
        this.contentColor = this@ChartUnitStyle.contentColor?.copy()
        this.background = this@ChartUnitStyle.background?.copy()
        this.transform = this@ChartUnitStyle.transform?.copy()
        this.textStyle = this@ChartUnitStyle.textStyle?.copy()
        this.strokePathStyle = this@ChartUnitStyle.strokePathStyle?.copy()
        this.contentPathStyle = this@ChartUnitStyle.contentPathStyle?.copy()
        this.padding = this@ChartUnitStyle.padding?.copy()
        this.shapeStyle = this@ChartUnitStyle.shapeStyle?.deepCopy()
        this.close = this@ChartUnitStyle.close
        this.shape = this@ChartUnitStyle.shape
        this.roundRadius = this@ChartUnitStyle.roundRadius.copyOf()
    }

    override fun toString(): String = "{zIndex: $zIndex, shape: $shape, close: $close, roundRadius: ${roundRadius.joinToString("-")}\n\t" +
            "position: $position\n\tborder: $border\n\tcontentColor: $contentColor\n\tbackground: $background\n\ttransform: $transform\n\t" +
            "textStyle: $textStyle\n\tcontentPathStyle: $contentPathStyle\n\tstrokePathStyle: $strokePathStyle\n\tpadding: $padding\n\t" +
            "shapeStyle: $shapeStyle}"

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
        val LINE = EnumHelper[SYMBOL_SHAPE_TYPE]  // xys -> 曲线、波浪线
        val POLYGON = EnumHelper[SYMBOL_SHAPE_TYPE]  // 多边形
    }
}

/* base unit */

open class ChartUnit {
    open var extras: MutableMap<String, Any>? = null
    open var children: MutableList<ChartUnit>? = null
        set(value) {
            field?.forEach {
                it.parent = null
                it.chartView = null
            }
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
            statedStyle(state, value)
        }

    open var trueWidth: Float = 0f
    open var trueHeight: Float = 0f
    open var trueLeft: Float = 0f
    open var trueTop: Float = 0f
    open var trueXys: MutableList<Float> = mutableListOf()

    open var truePL = 0f
    open var truePT = 0f
    open var truePR = 0f
    open var truePB = 0f

    open var trueTextX: Float = 0f
    open var trueTextY: Float = 0f

    open var trueOpXys: MutableList<Float>? = null
    open var animChanged2 = false
    open val pathOp: Path.Op
        get() = style?.shapeStyle?.pathOp ?: Path.Op.INTERSECT

    open fun string(depth: Int = 0): String = "trueWidth: $trueWidth, trueHeight: $trueHeight, trueLeft: $trueLeft, trueTop: $trueTop, " +
            "truePL: $truePL, truePT: $truePT, truePR: $truePR, truePB: $truePB" +
            "trueXys: ${trueXys.joinToString("-")}, trueTextX: $trueTextX, trueTextY: $trueTextY, trueFromX: $trueFromX, trueToX: $trueToX, " +
            "trueFromY: $trueFromY, trueToY: $trueToY, animXys: ${animXys.joinToString("-")}, trueOpXys: ${trueOpXys?.joinToString("-") ?: "null"}, " +
            "style: $style\n\tchildren: " + children?.joinToString { "\n" + "\t".repeat(depth) + it.string(depth + 1) }

    // copy

    open fun copy(): ChartUnit = ChartUnit().other(this)

    open fun other(other: ChartUnit): ChartUnit {
        this.extras = other.extras
        this.children = other.children
        this.parent = other.parent
        this.chartView = other.chartView
        this.state = other.state
        this.styles = other.styles
        this.changed = true
        this.fromX = other.fromX
        this.fromY = other.fromY
        this.toX = other.toX
        this.toY = other.toY
        this.animChanged = false
        return this
    }

    open fun deepCopy(): ChartUnit = ChartUnit().deepOther(this)

    open fun deepOther(other: ChartUnit): ChartUnit {
        if (other.extras != null) {
            this.extras = mutableMapOf()
            other.extras!!.forEach {
                this.extras!![it.key] = it.value
            }
        }
        this.children = other.children?.map { it.deepCopy() }?.toMutableList()
        this.parent = other.parent
        this.chartView = other.chartView
        this.state = other.state
        this.styles = other.styles?.map { it.key to it.value.deepCopy() }?.toMap()?.toMutableMap()
        this.changed = true
        this.fromX = other.fromX
        this.fromY = other.fromY
        this.toX = other.toX
        this.toY = other.toY
        this.animChanged = false
        return this
    }

    // update

    open var changed: Boolean = true
    open fun update(): ChartUnit {
        updateSelf()
        updateAnim()
        calcOpPath()
        children?.forEach {
            it.update()
        }
        // TODO: transform 对这里造成的影响
        return this
    }

    open fun updateSelf(): ChartUnit {
        if (!changed) {
            return this
        }
        changed = false
        val style = this.style ?: return this
        val shapeStyle = style.shapeStyle
        val position = style.position
        val shapeMode = shapeStyle?.mode
        val posMode = position?.mode ?: shapeMode ?: return this
        val pWidth: Float
        val pHeight: Float
        val pTop: Float
        val pLeft: Float = if (posMode == ChartPosition.PosMode.ALIGN_PARENT && parent != null) {
            val parent = this.parent ?: return this
            pWidth = parent.trueWidth
            pHeight = parent.trueHeight
            pTop = parent.trueTop
            parent.trueLeft
        } else if (posMode == ChartPosition.PosMode.ALIGN_ROOT && chartView != null) {
            pWidth = chartView!!.measuredWidth.toFloat()
            pHeight = chartView!!.measuredHeight.toFloat()
            pTop = 0f
            0f
        } else {
            return this
        }
        val shape = style.shape
        if (shape == ChartUnitStyle.ShapeType.NONE) {
            return this
        } else if ((shape == ChartUnitStyle.ShapeType.LINE || shape == ChartUnitStyle.ShapeType.POLYGON) && shapeStyle != null) {
            if (shapeMode == ChartPosition.PosMode.ALIGN_SELF) {
                if (position != null) {
                    calcTruePosByPosAndShape(position, pWidth, pHeight, pLeft, pTop, shape, shapeStyle)
                } else {
                    return this  // TODO: throw error
                }
            } else {
                calTruePosByShapeConfig(shapeStyle, pWidth, pHeight, pLeft, pTop)
                if (shape == ChartUnitStyle.ShapeType.POLYGON) {
                    handlePadding()
                }
            }
        } else if (position != null) {
            calcTruePosByPosConfig(position, pWidth, pHeight, pLeft, pTop, shape)
        } else {
            return this
        }
        val textStyle = style.textStyle
        if (textStyle != null) {
            trueTextX = trueLeft + trueSize(textStyle.x, trueWidth, trueHeight)
            trueTextY = trueTop + trueSize(textStyle.y, trueWidth, trueHeight)
        }
        return this
    }

    protected open fun calcTruePosByPosAndShape(position: ChartPosition, pWidth: Float, pHeight: Float, pLeft: Float, pTop: Float, shape: Int,
                                                shapeStyle: ChartShapeStyle) {
        calcTruePosByPosConfig(position, pWidth, pHeight, pLeft, pTop, shape)
        trueXys.clear()
        trueXys.addAll(shapeStyle.xys.mapIndexed { index, xy ->
            when {
                index % 2 == 0 -> trueSize(xy, trueWidth, trueHeight) + trueLeft
                else -> trueSize(xy, trueWidth, trueHeight) + trueTop
            }
        })
        val lineContentStyle = shapeStyle.lineContentStyle
        if (lineContentStyle != null) {
            trueWidth += lineContentStyle.borderWidth
            trueHeight += lineContentStyle.borderWidth
        }
    }

    protected open fun calTruePosByShapeConfig(shapeStyle: ChartShapeStyle, pWidth: Float, pHeight: Float, pLeft: Float, pTop: Float) {
        trueXys.clear()
        val xys = shapeStyle.xys
        var minX = xys[0]
        var maxX = xys[0]
        var minY = xys[1]
        var maxY = xys[1]
        trueXys.addAll(xys.mapIndexed { index, xy ->
            val flag = index % 2 == 0
            val temp = trueSize(xy, pWidth, pHeight)
            if (flag) {
                minX = min(minX, temp)
                maxX = max(maxX, temp)
            } else {
                minY = min(minY, temp)
                maxY = max(maxY, temp)
            }
            temp
        })
        trueWidth = maxX - minX
        trueHeight = maxY - minY
        trueLeft = minX + pLeft
        trueTop = minY + pTop
        val lineContentStyle = shapeStyle.lineContentStyle
        if (lineContentStyle != null) {
            trueWidth += lineContentStyle.borderWidth
            trueHeight += lineContentStyle.borderWidth
        }
    }

    protected open fun calcTruePosByPosConfig(position: ChartPosition, pWidth: Float, pHeight: Float, pLeft: Float, pTop: Float, shape: Int) {
        this.trueWidth = trueSize(position.width, pWidth, pHeight)
        this.trueHeight = trueSize(position.height, pWidth, pHeight)
        val left = position.left
        val right = position.right
        this.trueLeft = when {
            left != null -> pLeft + trueSize(left, pWidth, pHeight)
            right != null -> pLeft + pWidth - this.trueWidth - trueSize(right, pWidth, pHeight)
            else -> 0f
        }
        val top = position.top
        val bottom = position.bottom
        this.trueTop = when {
            top != null -> pTop + trueSize(top, pWidth, pHeight)
            bottom != null -> pTop + pHeight - this.trueHeight - trueSize(bottom, pWidth, pHeight)
            else -> 0f
        }

        handlePadding()

        trueXys.clear()
        val size = min(trueWidth, trueHeight)
        when (shape) {
            ChartUnitStyle.ShapeType.CIRCLE -> this.addTrueXYs(trueLeft + trueWidth / 2, trueTop + trueHeight / 2, size / 2)
            ChartUnitStyle.ShapeType.ROUND_RECTANGLE -> this.addTrueXYs(trueLeft, trueTop, trueLeft + trueWidth, trueTop + trueHeight)
            ChartUnitStyle.ShapeType.SQUARE -> {
                val wSpace = (trueWidth - size) / 2
                val hSpace = (trueHeight - size) / 2
                addTrueXYs(trueLeft + wSpace, trueTop + hSpace, trueLeft + wSpace, trueTop + hSpace + trueHeight, trueLeft + wSpace + trueWidth,
                        trueTop + hSpace + trueHeight, trueLeft + wSpace + trueWidth, trueTop + hSpace)
                style!!.close = true
            }
            ChartUnitStyle.ShapeType.RECANTAGE -> {
                addTrueXYs(trueLeft, trueTop, trueLeft, trueTop + trueHeight, trueLeft + trueWidth, trueTop + trueHeight, trueLeft + trueWidth,
                        trueTop)
                style!!.close = true
            }
            ChartUnitStyle.ShapeType.DIAMOND -> {
                addTrueXYs(trueLeft + trueWidth / 2, trueTop, trueLeft, trueTop + trueHeight / 2, trueLeft + trueWidth / 2, trueTop + trueHeight,
                        trueLeft + trueWidth, trueTop + trueHeight / 2)
                style!!.close = true
            }
            ChartUnitStyle.ShapeType.TRIANGLE -> {
                addTrueXYs(trueLeft, trueTop + trueHeight, trueLeft + trueWidth / 2, trueTop, trueLeft + trueWidth, trueTop + trueHeight)
                style!!.close = true
            }
            ChartUnitStyle.ShapeType.TRIANGLEDOWN -> {
                addTrueXYs(trueLeft, trueTop, trueLeft + trueWidth / 2, trueTop + trueHeight, trueLeft + trueWidth, trueTop)
                style!!.close = true
            }
        }
    }

    open fun addTrueXYs(vararg xys: Float): ChartUnit {
        this.trueXys.addAll(xys.toTypedArray())
        return this
    }

    protected open fun addTrueXY(x: Float, y: Float): ChartUnit {
        this.trueXys.add(x)
        this.trueXys.add(y)
        return this
    }

    protected open fun handlePadding() {
        val padding = style?.padding
        if (padding != null) {
            truePL = trueSize(padding.getPL(), this.trueWidth, this.trueHeight)
            truePR = trueSize(padding.getPR(), this.trueWidth, this.trueHeight)
            this.trueWidth = this.trueWidth - truePL - truePR
            this.trueLeft += truePL
            truePT = trueSize(padding.getPT(), this.trueWidth, this.trueHeight)
            truePB = trueSize(padding.getPB(), this.trueWidth, this.trueHeight)
            this.trueHeight = this.trueHeight - truePT - truePB
            this.trueTop += truePT
        }
    }

    // 用于 position.width|height|left|top|right|bottom, padding.*, textStyle.x|y, shapeStyle.xys|opPathXys, fromX|fromY|toX|toY
    protected open fun trueSize(flag: Float, width: Float, height: Float): Float = when {
        flag < END_PERCENT_HEIGHT -> flag - END_PERCENT_HEIGHT
        flag >= START_PERCENT_WIDTH -> flag
        flag < END_PERCENT_WIDTH && flag >= END_PERCENT_HEIGHT -> height * (flag - MIDDLE_PERCENT_HEIGHT)
        else -> width * (flag - MIDDLE_PERCENT_WIDTH)
    }

    // anim2

    protected open fun calcOpPath() {
        if (style == null || style!!.shape == ChartUnitStyle.ShapeType.LINE) {
            return
        }
        val opPathXys = style?.shapeStyle?.opPathXys
        if (opPathXys != null) {
            opPathXys.apply {
                initTrueOpXys()
                trueOpXys!!.addAll(this.mapIndexed { index, xy ->
                    when {
                        index % 2 == 0 -> trueSize(xy, trueWidth, trueHeight) + trueLeft
                        else -> trueSize(xy, trueWidth, trueHeight) + trueTop
                    }
                })
            }
        } else if (animChanged2) {
            animChanged2 = false
            initTrueOpXys()
            trueFromX = trueSize(fromX, trueWidth, trueHeight) + trueLeft
            trueFromY = trueSize(fromY, trueWidth, trueHeight) + trueTop
            trueToX = trueSize(toX, trueWidth, trueHeight) + trueLeft
            trueToY = trueSize(toY, trueWidth, trueHeight) + trueTop
            trueOpXys!!.addAll(listOf(trueFromX, trueFromY, trueFromX, trueToY, trueToX, trueToY, trueToX, trueFromY, trueFromX, trueFromY))
        }
    }

    protected open fun initTrueOpXys() {
        if (trueOpXys == null) {
            trueOpXys = mutableListOf()
        } else {
            trueOpXys!!.clear()
        }
    }

    // anim1 -- width / height

    open var fromX: Float = 0f
    open var fromY: Float = 0f
    open var toX: Float = -1f
    open var toY: Float = -1f
    open var animXys: MutableList<Float> = mutableListOf()
    open var animChanged = false

    open var trueFromX: Float = 0f
    open var trueFromY: Float = 0f
    open var trueToX: Float = 0f
    open var trueToY: Float = 0f

    open fun xRange(from: Float, to: Float, p: Boolean = true): ChartUnit {
        if (p) {
            this.fromX = posVal(from)
            this.toX = posVal(to)
        } else {
            this.fromX = from
            this.toX = to
        }
        // animChanged = true  // todo: 算法错误
        animChanged = true
        animChanged2 = true
        return this
    }

    open fun yRange(from: Float, to: Float, p: Boolean = true): ChartUnit {
        if (p) {
            this.fromY = posVal(from, false)
            this.toY = posVal(to, false)
        } else {
            this.fromY = from
            this.toY = to
        }
        // animChanged = true  // todo: 算法错误
        animChanged = true
        animChanged2 = true
        return this
    }

    open fun updateAnim(): ChartUnit {
        val shape = this.style?.shape ?: return this
        if (!animChanged || trueXys.size <= 4 || shape != ChartUnitStyle.ShapeType.LINE) {
            return this
        }
        animChanged = false
        trueFromX = trueSize(fromX, trueWidth, trueHeight) + trueLeft
        trueFromY = trueSize(fromY, trueWidth, trueHeight) + trueTop
        trueToX = trueSize(toX, trueWidth, trueHeight) + trueLeft
        trueToY = trueSize(toY, trueWidth, trueHeight) + trueTop
        this.animXys.clear()
        when (shape) {
            ChartUnitStyle.ShapeType.NONE -> Unit
            ChartUnitStyle.ShapeType.CIRCLE -> Unit /*TODO("what should be doing here")*/
            ChartUnitStyle.ShapeType.ROUND_RECTANGLE -> animXys.addAll(listOf(trueFromX, trueFromY, trueToX, trueToY, trueXys[4]))
            else -> {
                var lastX = trueXys[0]
                var lastY = trueXys[1]
                if (lastX in trueFromX..trueToX && lastY in trueFromY..trueToY) {
                    animXys.add(lastX)
                    animXys.add(lastY)
                }
                (3 until trueXys.size step 2).forEach {
                    val temp = clip(it, lastX, lastY)
                    lastX = temp.first
                    lastY = temp.second
                }
                if (style!!.close) {
                    clip(1, lastX, lastY)
                }
            }
        }
        return this
    }

    protected open fun clip(it: Int, lastX: Float, lastY: Float): Pair<Float, Float> {
        val nowX = trueXys[it - 1]
        val nowY = trueXys[it]
        if (lastX < trueFromX && nowX >= trueFromX) {
            addXYByLimitedX(lastX, lastY, nowX, nowY, trueFromX)
        } else if (lastX <= trueToX && nowX > trueToX) {
            addXYByLimitedX(lastX, lastY, nowX, nowY, trueToX)
        }
        if (nowX in trueFromX..trueToX && nowY in trueFromY..trueToY) {
            animXys.add(nowX)
            animXys.add(nowY)
        }
        return Pair(nowX, nowY)
    }

    protected open fun addXYByLimitedX(lastX: Float, lastY: Float, nowX: Float, nowY: Float, x: Float) {
        val tempY = getXYByLimitedX(lastX, lastY, nowX, nowY, x)
        if (tempY in trueFromY..trueToY) {
            animXys.add(x)
            animXys.add(tempY)
            if (nowY < trueFromY) {
                animXys.add(getXYByLimitedY(lastX, lastY, nowX, nowY, trueFromY))
                animXys.add(trueFromY)
            } else if (nowY > trueToY) {
                animXys.add(getXYByLimitedY(lastX, lastY, nowX, nowY, trueToY))
                animXys.add(trueToY)
            }
        }
    }

    // setter

    open fun setExtra(key: String, value: Any?): ChartUnit {
        if (extras == null && value != null) {
            extras = mutableMapOf(key to value)
        } else if (extras != null && value != null) {
            extras!![key] = value
        } else if (extras != null && value == null) {
            extras!!.remove(key)
        }
        return this
    }

    open fun addChild(child: ChartUnit?): ChartUnit {
        if (child == null) {
            return this
        }
        if (children == null) {
            children = mutableListOf(child)
            child.parent = this
            child.chartView = this.chartView
        } else if (!children!!.contains(child)) {
            children!!.add(child)
            child.parent = this
            child.chartView = this.chartView
        }
        return this
    }

    open fun removeChild(child: ChartUnit?): ChartUnit {
        if (child != null && children?.contains(child) == true) {
            children!!.remove(child)
            child.parent = null
            child.chartView = null
        }
        return this
    }

    open fun parent(parent: ChartUnit?): ChartUnit {
        this.parent = parent
        return this
    }

    open fun chartView(chartView: ChartView?): ChartUnit {
        this.chartView = chartView
        return this
    }

    open fun state(state: Int): ChartUnit {
        this.state = state
        return this
    }

    open fun statedStyle(state: Int, style: ChartUnitStyle?): ChartUnit {
        if (styles != null && style == null) {
            styles!!.remove(state)
        } else if (styles == null && style != null) {
            styles = mutableMapOf(state to style)
        } else if (styles != null && style != null) {
            styles!![state] = style
        }
        return this
    }

    open fun style(style: ChartUnitStyle?): ChartUnit {
        this.style = style
        return this
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

/* high-level unit */

open class ChartAxis : ChartUnit() {
    companion object {
        private val DEFAULT_SYMBOL_LONG_EDGE = dp2px(10f).toFloat()
        private val DEFAULT_SYMBOL_SHORT_EDGE = dp2px(1f).toFloat()
        private val color = ChartColor(Color.BLACK)
    }

    open var line: ChartUnit = ChartUnit().style(ChartUnitStyle()
            .position(ChartPosition().width(DEFAULT_SYMBOL_SHORT_EDGE).right(0f))
            .color(color))
    open var title: ChartUnit = ChartUnit()
    open var symbols: MutableMap<Float, ChartUnit>? = null
        set(value) {
            field?.forEach { removeChild(it.value) }
            field = value
            field?.forEach { addChild(it.value) }
        }
    open var max: Float? = null
    open var min: Float? = null
    open var horizontal: Boolean = false
        set(value) {
            field = value
            if (field) {
                defaultSymbolStyle.position!!.width(DEFAULT_SYMBOL_SHORT_EDGE).height(DEFAULT_SYMBOL_LONG_EDGE).top(1f, p = true)
                line.style!!.position!!.height(DEFAULT_SYMBOL_SHORT_EDGE).right = null
            } else {
                defaultSymbolStyle.position!!.width(DEFAULT_SYMBOL_LONG_EDGE).height(DEFAULT_SYMBOL_SHORT_EDGE).right(DEFAULT_SYMBOL_SHORT_EDGE)
                line.style!!.position!!.width(DEFAULT_SYMBOL_SHORT_EDGE).right(0f).left = null
            }
        }
    open var defaultSymbolStyle: ChartUnitStyle = ChartUnitStyle()
            .position(ChartPosition().width(DEFAULT_SYMBOL_LONG_EDGE).height(DEFAULT_SYMBOL_SHORT_EDGE).right(DEFAULT_SYMBOL_SHORT_EDGE))
            .color(color)

    // todo: title - width|height / titleLeftMargin / titleRightMargin / symbolText - width|height / symbolTextRightMargin / symbol - width|height / line - width|height

    override fun update(): ChartUnit {
        super.update()
        val textStyle = style?.textStyle
        if (textStyle != null) {
            trueTextX = trueLeft + trueSize(textStyle.x, trueWidth, trueHeight)
            trueTextY = trueTop + trueSize(textStyle.y, trueWidth, trueHeight)
        }
        return this
    }

    open fun setSymbol(position: Float, symbol: ChartUnit?): ChartAxis {
        if (symbol != null) {
            if (symbols == null) {
                symbols = mutableMapOf(position to symbol)
            } else {
                symbols!![position] = symbol
            }
        } else if (symbols != null && symbols!!.containsKey(position)) {
            symbols!!.remove(position)
        }
        return this
    }

    open fun getSymbol(position: Float, copyFlag: Int = 0): ChartUnit {
        var result = symbols?.get(position)
        if (result == null) {
            result = ChartUnit()
                    .style(when (copyFlag) {
                        0 -> defaultSymbolStyle
                        1 -> defaultSymbolStyle.copy()
                        else -> defaultSymbolStyle.deepCopy()
                    })
            setSymbol(position, result)
        }
        return result
    }

    open fun max(max: Float?): ChartAxis {
        this.max = max
        return this
    }

    open fun min(min: Float?): ChartAxis {
        this.max = max
        return this
    }

    open fun horizontal(horizontal: Boolean): ChartAxis {
        this.horizontal = horizontal
        return this
    }

    open fun defaultSymbolStyle(defaultSymbolStyle: ChartUnitStyle): ChartAxis {
        val old = this.defaultSymbolStyle
        this.defaultSymbolStyle = defaultSymbolStyle
        symbols?.forEach { (_, symbol) ->
            if (symbol.style == old) {
                symbol.style(this.defaultSymbolStyle)
            }
        }
        return this
    }

    override fun copy(): ChartUnit = ChartAxis().other(this)

    override fun other(other: ChartUnit): ChartUnit {
        if (other is ChartAxis) {
            this.symbols = other.symbols
            this.max = other.max
            this.min = other.min
        }
        return super.other(other)
    }

    override fun deepCopy(): ChartUnit = ChartAxis().deepOther(this)

    override fun deepOther(other: ChartUnit): ChartUnit {
        if (other is ChartAxis) {
            this.symbols = other.symbols?.toMutableMap()
            this.max = other.max
            this.min = other.min
        }
        return super.deepOther(other)
    }
}

open class ChartLegend

open class Chart
