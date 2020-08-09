@file:Suppress("unused", "LeakingThis")

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
                                .xy(-0.5f, 0f)
                                .xy(0f, -0.4f)
                                .xy(-0.2f, -1f)
                                .xy(-0.8f, -1f)
                                .xy(-1f, -0.4f))
                        .close(true)))
                .addChild(ChartUnit().style(style.copy()
                        .position(position.copy().left(dp50 * 2).top(dp50).width(dp50 * 4))
                        .shape(ChartUnitStyle.ShapeType.LINE)
                        .border(null)
                        .padding(null)
                        .shapeStyle(ChartShapeStyle().mode(ChartPosition.PosMode.ALIGN_SELF)
                                .xy(0f, -0.3f)
                                .xy(-0.1f, -0.2f)
                                .xy(-0.13f, 0.1f)
                                .xy(-0.2f, -0.4f)
                                .xy(-0.3f, -0.25f)
                                .xy(-0.36f, -0.45f)
                                .xy(-0.45f, -0.65f)
                                .xy(-0.56f, -0.95f)
                                .xy(-0.6f, -0.75f)
                                .xy(-0.74f, -0.8f)
                                .xy(-0.79f, -0.85f)
                                .xy(-0.84f, -0.77f)
                                .xy(-0.9f, -0.82f)
                                .xy(-1f, -0.8f)
                                .lineStyle(ChartBorder(getColor(R.color.green100), dp5 / 2.5f)))))
        unit.children?.addAll(0, unit.children?.map {
            val child = it.deepCopy().xRange(-0.2f, -0.8f)
            child.style!!.position!!.top((child.style!!.position!!.top ?: 0f) + dp50 * 2)
            child
        } ?: listOf())
    }

    init {
        testSymbol()
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
                val xys = when(unit.animXys.size > 0) {
                    true -> unit.animXys
                    else -> unit.trueXys
                }
                path.moveTo(xys[0], xys[1])
                (3 until xys.size step 2).forEach { i ->
                    path.lineTo(xys[i - 1], xys[i])
                }
                unit.animXys.clear()
            }
        }
        if (style.close) {
            path.close()
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

    open fun <T> returnLog(msg: String, value: T): T {
        ApiManager.LOGGER.d(TAG, msg)
        return value
    }

    companion object {
        private val TAG = ChartView::class.java.simpleName
    }
}

/* unit's style */

open class ChartPosition {
    open var width: Float = -1f  /* -1~0是相对于parent的百分比，0~是正常数值 */
    open var height: Float = -1f
    open var left: Float? = null
    open var top: Float? = null
    open var right: Float? = null
    open var bottom: Float? = null
    open var mode: Int = PosMode.ALIGN_PARENT

    // TODO: left / top / right/ bottom 各自有单独的mode

    open fun left(left: Float?): ChartPosition {
        this.left = left
        return this
    }

    open fun top(top: Float?): ChartPosition {
        this.top = top
        return this
    }

    open fun right(right: Float?): ChartPosition {
        this.right = right
        return this
    }

    open fun bottom(bottom: Float?): ChartPosition {
        this.bottom = bottom
        return this
    }

    open fun width(width: Float): ChartPosition {
        this.width = width
        return this
    }

    open fun height(height: Float): ChartPosition {
        this.height = height
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
    }

    open fun set(paint: Paint): ChartPathStyle {
        paint.strokeCap = toPaintCap()
        paint.strokeJoin = toPaintJoin()
        paint.pathEffect = effect
        return this
    }

    override fun toString(): String = "{join: $join, cap: $cap, effect: $effect, direction: $ccwDirection}"
}

open class ChartTextStyle(open var text: String? = null) {
    open var color: Int = Color.BLACK
    open var size: Float = sp2Px(12f).toFloat()
    open var weight: Int = Weight.NORMAL
    open var align: Int = Align.CENTER
    open var decoration: Int = Decoration.NONE
    open var isItalic: Boolean = false

    open var x: Float = -0.5f
    open var y: Float = -0.5f

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

    open fun x(x: Float): ChartTextStyle {
        this.x = x
        return this
    }

    open fun y(y: Float): ChartTextStyle {
        this.y = y
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

    open var padding: Float = 0f  // -1~0是parent百分比，0～是正常数值
    open var paddingLeft: Float? = null
    open var paddingTop: Float? = null
    open var paddingRight: Float? = null
    open var paddingBottom: Float? = null

    open var truePL = 0f
    open var truePT = 0f
    open var truePR = 0f
    open var truePB = 0f

    open fun padding(padding: Float): ChartPadding {
        this.padding = padding
        return this
    }

    open fun paddingLeft(paddingLeft: Float): ChartPadding {
        this.paddingLeft = paddingLeft
        return this
    }

    open fun paddingTop(paddingTop: Float): ChartPadding {
        this.paddingTop = paddingTop
        return this
    }

    open fun paddingRight(paddingRight: Float): ChartPadding {
        this.paddingRight = paddingRight
        return this
    }

    open fun paddingBottom(paddingBottom: Float): ChartPadding {
        this.paddingBottom = paddingBottom
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

    override fun toString(): String = "{padding: $padding, pl: $paddingLeft, pt: $paddingTop, pr: $paddingRight, pb: $paddingBottom, " +
            "truePL: $truePL, truePT: $truePT, truePR: $truePR, truePB: $truePB}"
}

open class ChartShapeStyle {
    open var lineContentStyle: ChartBorder? = null
    open var lineShapeStyle: Int = LineShapeStyle.SOLID
    open var xys: MutableList<Float> = mutableListOf()
    open var mode: Int = ChartPosition.PosMode.ALIGN_PARENT

    open fun xy(x: Float, y: Float): ChartShapeStyle {
        xys.add(x)
        xys.add(y)
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

    open fun copy(): ChartShapeStyle = ChartShapeStyle().other(this)

    open fun other(other: ChartShapeStyle): ChartShapeStyle {
        this.lineContentStyle = other.lineContentStyle
        this.lineShapeStyle = other.lineShapeStyle
        this.xys = other.xys
        this.mode = other.mode
        return this
    }

    open fun deepCopy(): ChartShapeStyle = ChartShapeStyle().deepOther(this)

    open fun deepOther(other: ChartShapeStyle): ChartShapeStyle {
        this.lineContentStyle = other.lineContentStyle?.copy()
        this.lineShapeStyle = other.lineShapeStyle
        this.xys = other.xys.toMutableList()
        this.mode = other.mode
        return this
    }

    override fun toString(): String = "{mode: $mode, lineContentStyle: $lineContentStyle, lineShapeStyle: $lineShapeStyle, " +
            "xys: ${xys.joinToString("-")}}"

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
    open var trueTextX: Float = 0f
    open var trueTextY: Float = 0f

    open fun string(depth: Int = 0): String = "trueWidth: $trueWidth, trueHeight: $trueHeight, trueLeft: $trueLeft, trueTop: $trueTop, " +
            "trueXys: ${trueXys.joinToString("-")}, trueTextX: $trueTextX, trueTextY: $trueTextY, trueFromX: $trueFromX, trueToX: $trueToX, " +
            "trueFromY: $trueFromY, trueToY: $trueToY, animXys: ${animXys.joinToString("-")}, style: $style" +
            children?.joinToString { "\n" + "\t".repeat(depth) + it.string(depth + 1) }

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
            trueTextX = trueLeft + trueSize(textStyle.x, trueWidth)
            trueTextY = trueTop + trueSize(textStyle.y, trueHeight)
        }
        return this
    }

    protected open fun calcTruePosByPosAndShape(position: ChartPosition, pWidth: Float, pHeight: Float, pLeft: Float, pTop: Float, shape: Int,
                                                shapeStyle: ChartShapeStyle) {
        calcTruePosByPosConfig(position, pWidth, pHeight, pLeft, pTop, shape)
        trueXys.clear()
        trueXys.addAll(shapeStyle.xys.mapIndexed { index, xy ->
            when {
                index % 2 == 0 -> trueSize(xy, trueWidth) + trueLeft
                else -> trueSize(xy, trueHeight) + trueTop
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
            val temp = trueSize(xy, when {
                flag -> pWidth
                else -> pHeight
            })
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
        this.trueWidth = trueSize(position.width, pWidth)
        this.trueHeight = trueSize(position.height, pHeight)
        val left = position.left
        val right = position.right
        this.trueLeft = when {
            left != null -> pLeft + trueSize(left, pWidth)
            right != null -> pLeft + trueSize2(right, pWidth, this.trueWidth)
            else -> 0f
        }
        val top = position.top
        val bottom = position.bottom
        this.trueTop = when {
            top != null -> pTop + trueSize(top, pHeight)
            bottom != null -> pTop + trueSize2(bottom, pHeight, this.trueHeight)
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
            padding.truePL = trueSize(padding.paddingLeft ?: padding.padding, this.trueWidth)
            padding.truePR = trueSize(padding.paddingRight ?: padding.padding, this.trueWidth)
            this.trueWidth = this.trueWidth - padding.truePL - padding.truePR
            this.trueLeft += padding.truePL
            padding.truePT = trueSize(padding.paddingTop ?: padding.padding, this.trueHeight)
            padding.truePB = trueSize(padding.paddingBottom ?: padding.padding, this.trueHeight)
            this.trueHeight = this.trueHeight - padding.truePT - padding.truePB
            this.trueTop += padding.truePT
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

    open fun xRange(from: Float, to: Float): ChartUnit {
        this.fromX = from
        this.toX = to
        animChanged = true
        return this
    }

    open fun yRange(from: Float, to: Float): ChartUnit {
        this.fromY = from
        this.toY = to
        animChanged = true
        return this
    }

    open fun updateAnim(): ChartUnit {
        val shape = this.style?.shape ?: return this
        if (!animChanged || trueXys.size <= 4) {
            return this
        }
        animChanged = false
        trueFromX = trueSize(fromX, trueWidth) + trueLeft
        trueFromY = trueSize(fromY, trueHeight) + trueTop
        trueToX = trueSize(toX, trueWidth) + trueLeft
        trueToX = trueSize(toY, trueHeight) + trueTop
        animXys.clear()
        when (shape) {
            ChartUnitStyle.ShapeType.NONE -> Unit
            ChartUnitStyle.ShapeType.CIRCLE -> TODO("what should be doing here")
            ChartUnitStyle.ShapeType.ROUND_RECTANGLE -> animXys.addAll(listOf(trueFromX, trueFromY, trueToX, trueToY, trueXys[4]))
            else -> {
                var lastX = trueXys[0]
                var lastY = trueXys[1]
                (3 until trueXys.size step 2).forEach {
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
                    lastX = nowX
                    lastY = nowY
                }
            }
        }
        return this
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
    open var symbols: MutableMap<Float, ChartUnit>? = null
        set(value) {
            field?.forEach { removeChild(it.value) }
            field = value
            field?.forEach { addChild(it.value) }
        }
    open var max: Float? = null
    open var min: Float? = null

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

    open fun max(max: Float?): ChartAxis {
        this.max = max
        return this
    }

    open fun min(min: Float?): ChartAxis {
        this.max = max
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
