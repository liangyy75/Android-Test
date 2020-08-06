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
    open val unit = ChartUnit().setStyle(ChartUnitStyle()
            .p(ChartPosition().m(ChartPosition.PosMode.ALIGN_ROOT))
            .c(ChartColor().c(Color.WHITE)))
            .cv(this)

    private fun testSymbol() {
        val dp50 = dp5 * 10
        val position = ChartPosition().w(dp50).h(dp50)
        val style = ChartUnitStyle()
                .p(position)
                .pd(ChartPadding(dp5))
                .b(ChartBorder().bw(dp5).bc(getColor(R.color.green100)))
                .c(ChartColor().c(getColor(R.color.red100)))
                .setShape(ChartUnitStyle.ShapeType.CIRCLE)
                .ts(ChartTextStyle("test-text"))
        unit.addChild(ChartUnit().setStyle(style))
                .addChild(ChartUnit().setStyle(style.copy()
                        .p(position.copy().l(dp50))
                        .setShape(ChartUnitStyle.ShapeType.SQUARE)))
                .addChild(ChartUnit().setStyle(style.copy()
                        .p(position.copy().l(dp50 * 2))
                        .setShape(ChartUnitStyle.ShapeType.RECANTAGE)))
                .addChild(ChartUnit().setStyle(style.copy()
                        .p(position.copy().l(dp50 * 3))
                        .setShape(ChartUnitStyle.ShapeType.ROUND_RECTANGLE).rR(dp5)))
                .addChild(ChartUnit().setStyle(style.copy()
                        .p(position.copy().l(dp50 * 4))
                        .setShape(ChartUnitStyle.ShapeType.DIAMOND)))
                .addChild(ChartUnit().setStyle(style.copy()
                        .p(position.copy().l(dp50 * 5))
                        .setShape(ChartUnitStyle.ShapeType.TRIANGLE)))
                .addChild(ChartUnit().setStyle(style.copy()
                        .p(position.copy().l(0f).t(dp50))
                        .setShape(ChartUnitStyle.ShapeType.TRIANGLEDOWN)))
    }

    init {
        // testSymbol()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        unit.updatePos()
        // ApiManager.LOGGER.d(TAG, unit.string())
        drawItem(canvas, unit)
    }

    open fun drawItem(canvas: Canvas, unit: ChartUnit) {
        val style = unit.style
                ?: return returnLog("style is null while state is ${unit.state}", Unit)
        val position = style.position ?: return returnLog("position is null", Unit)
        val transform = style.transform
        val left = position.trueLeft
        val top = position.trueTop
        val width = position.trueWidth
        val height = position.trueHeight
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
        val border = style.border
        val contentColor = style.contentColor
        style.pathStyle?.apply {
            strokePaint.reset()
            strokePaint.strokeCap = toPaintCap()
            strokePaint.strokeJoin = toPaintJoin()
            strokePaint.pathEffect = effect
        }
        val lineFlag = unit is ChartArea && unit.isLine && unit.lineContentStyle != null
        if (border != null || contentColor != null || lineFlag) {
            val path = Path()
            if (unit is ChartArea) {
                val xys = unit.xys
                (0 until xys.size step 2).forEach { i ->
                    if (i == 0) {
                        path.moveTo(xys[i] + left, (xys.getOrNull(i + 1)
                                ?: return returnLog("xys.size should be even", Unit)) + top)
                    } else {
                        path.lineTo(xys[i] + left, (xys.getOrNull(i + 1)
                                ?: return returnLog("xys.size should be even", Unit)) + top)
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
                        close()
                    }
                    ChartUnitStyle.ShapeType.TRIANGLE -> path.apply {
                        moveTo(left, top + height)
                        lineTo(left + width / 2, top)
                        lineTo(left + width, top + height)
                        close()
                    }
                    ChartUnitStyle.ShapeType.TRIANGLEDOWN -> path.apply {
                        moveTo(left + width / 2, top + height)
                        lineTo(left, top)
                        lineTo(left + width, top)
                        close()
                    }
                    else -> Unit
                }
            }
            if (contentColor != null) {
                mainPaint.reset()
                if (contentColor.set(mainPaint, width, height)) {
                    mainPaint.style = Paint.Style.FILL
                    canvas.drawPath(path, mainPaint)
                }
            }
            if (border != null) {
                border.set(strokePaint)
                canvas.drawPath(path, strokePaint)
            }
            if (lineFlag) {
                (unit as ChartArea).lineContentStyle!!.set(strokePaint)
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

    open var trueWidth: Float = 0f
    open var trueHeight: Float = 0f
    open var trueLeft: Float = 0f
    open var trueTop: Float = 0f

    open fun l(left: Float?): ChartPosition {
        this.left = left
        return this
    }

    open fun t(top: Float?): ChartPosition {
        this.top = top
        return this
    }

    open fun r(right: Float?): ChartPosition {
        this.right = right
        return this
    }

    open fun b(bottom: Float?): ChartPosition {
        this.bottom = bottom
        return this
    }

    open fun w(width: Float): ChartPosition {
        this.width = width
        return this
    }

    open fun h(height: Float): ChartPosition {
        this.height = height
        return this
    }

    open fun m(mode: Int): ChartPosition {
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
    }

    override fun toString(): String = "{width: $width, height: $height, left: $left, top: $top, right: $right, bottom: $bottom, mode: $mode, " +
            "trueWidth: $trueWidth, trueHeight: $trueHeight, trueLeft: $trueLeft, trueTop: $trueTop}"
}

open class ChartBorder {
    open var borderWidth: Float = 0f
    open var borderColor: Int = Color.WHITE
    // open var inner: Boolean = false  // 表示border是占用width还是不占用，对ChartArea无效
    // TODO: box: content-box, padding-box, border-box

    open fun bw(borderWidth: Float): ChartBorder {
        this.borderWidth = borderWidth
        return this
    }

    open fun bc(borderColor: Int): ChartBorder {
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

    open fun c(color: Int?): ChartColor {
        this.color = color
        return this
    }

    open fun bg(id: Int): ChartColor {
        this.bgResId = id
        return this
    }

    open fun bg(drawable: Drawable?): ChartColor {
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

    open fun r(rotation: Float): ChartTransform {
        this.rotation = rotation
        return this
    }

    open fun sx(scaleX: Float): ChartTransform {
        this.scaleX = scaleX
        return this
    }

    open fun sy(scaleY: Float): ChartTransform {
        this.scaleY = scaleY
        return this
    }

    open fun s(scaleX: Float, scaleY: Float): ChartTransform {
        this.scaleX = scaleX
        this.scaleY = scaleY
        return this
    }

    open fun tx(translateX: Float): ChartTransform {
        this.translateX = translateX
        return this
    }

    open fun ty(translateY: Float): ChartTransform {
        this.translateY = translateY
        return this
    }

    open fun t(translateX: Float, translateY: Float): ChartTransform {
        this.translateX = translateX
        this.translateY = translateY
        return this
    }

    open fun skx(skewX: Float): ChartTransform {
        this.skewX = skewX
        return this
    }

    open fun sky(skewY: Float): ChartTransform {
        this.skewY = skewY
        return this
    }

    open fun sk(skewX: Float, skewY: Float): ChartTransform {
        this.skewX = skewX
        this.skewY = skewY
        return this
    }

    open fun seq(sequence: String): ChartTransform {
        this.sequence = sequence
        return this
    }

    open fun tf(transform: Transform?): ChartTransform {
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

    open fun j(join: Int): ChartPathStyle {
        this.join = join
        return this
    }

    open fun c(cap: Int): ChartPathStyle {
        this.cap = cap
        return this
    }

    open fun e(effect: PathEffect?): ChartPathStyle {
        this.effect = effect
        return this
    }

    open fun d(ccwDirection: Boolean): ChartPathStyle {
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

    open fun c(color: Int): ChartTextStyle {
        this.color = color
        return this
    }

    open fun s(size: Float): ChartTextStyle {
        this.size = size
        return this
    }

    open fun w(weight: Int): ChartTextStyle {
        this.weight = weight
        return this
    }

    open fun a(align: Int): ChartTextStyle {
        this.align = align
        return this
    }

    open fun d(decoration: Int): ChartTextStyle {
        this.decoration = decoration
        return this
    }

    open fun i(isItalic: Boolean): ChartTextStyle {
        this.isItalic = isItalic
        return this
    }

    open fun t(text: String?): ChartTextStyle {
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

    open fun p(padding: Float): ChartPadding {
        this.padding = padding
        return this
    }

    open fun pl(padding: Float): ChartPadding {
        this.padding = padding
        return this
    }

    open fun pt(padding: Float): ChartPadding {
        this.padding = padding
        return this
    }

    open fun pr(padding: Float): ChartPadding {
        this.padding = padding
        return this
    }

    open fun pb(padding: Float): ChartPadding {
        this.padding = padding
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
    open var shape: Int = ShapeType.RECANTAGE  // 在ChartArea中无效
    open var roundRadius: Float = 0f
    open var close: Boolean = true
    open var isLine: Boolean = false
    open var lineContentStyle: ChartBorder? = null
    open var lineShapeStyle: Int = LineShapeStyle.SOLID
    open var xys: MutableList<Float>? = null
    open var trueXys: MutableList<Float>? = null

    open fun xy(x: Float, y: Float): ChartShapeStyle {
        if (xys != null) {
            xys!!.add(x)
            xys!!.add(y)
        } else {
            xys = mutableListOf(x, y)
        }
        return this
    }

    open fun c(close: Boolean): ChartShapeStyle {
        this.close = close
        return this
    }

    open fun l(isLine: Boolean): ChartShapeStyle {
        this.isLine = isLine
        return this
    }

    open fun lcs(lineStyle: ChartBorder?): ChartShapeStyle {
        this.lineContentStyle = lineStyle
        isLine = lineStyle != null
        return this
    }

    open fun lss(lineShapeStyle: Int): ChartShapeStyle {
        this.lineShapeStyle = lineShapeStyle
        isLine = true
        return this
    }

    open fun setShape(shape: Int): ChartShapeStyle {
        this.shape = shape
        return this
    }

    open fun rR(roundRadius: Float): ChartShapeStyle {
        this.roundRadius = roundRadius
        return this
    }

    open fun copy(): ChartShapeStyle = ChartShapeStyle().setOther(this)

    open fun setOther(other: ChartShapeStyle): ChartShapeStyle {
        this.shape = other.shape
        this.roundRadius = other.roundRadius
        this.close = other.close
        this.isLine = other.isLine
        this.lineContentStyle = other.lineContentStyle
        this.lineShapeStyle = other.lineShapeStyle
        this.xys = other.xys
        return this
    }

    open fun deepCopy(): ChartShapeStyle = ChartShapeStyle().setDeepOther(this)

    open fun setDeepOther(other: ChartShapeStyle): ChartShapeStyle {
        this.shape = other.shape
        this.roundRadius = other.roundRadius
        this.close = other.close
        this.isLine = other.isLine
        this.lineContentStyle = other.lineContentStyle?.copy()
        this.lineShapeStyle = other.lineShapeStyle
        this.xys = other.xys?.toMutableList()
        return this
    }

    override fun toString(): String = "{shape: $shape, roundRadius: $roundRadius, close: $close, isLine: $isLine, lineContentStyle: " +
            "$lineContentStyle, lineShapeStyle: $lineShapeStyle, xys: ${xys?.joinToString("-")}, trueXys: ${trueXys?.joinToString("-")}}"

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
    open var pathStyle: ChartPathStyle? = null
    open var padding: ChartPadding? = null
    open var shapeStyle: ChartShapeStyle? = null

    open fun z(zIndex: Int): ChartUnitStyle {
        this.zIndex = zIndex
        return this
    }

    open fun p(position: ChartPosition?): ChartUnitStyle {
        this.position = position
        return this
    }

    open fun b(border: ChartBorder?): ChartUnitStyle {
        this.border = border
        return this
    }

    open fun c(color: ChartColor?): ChartUnitStyle {
        this.contentColor = color
        return this
    }

    open fun bg(background: ChartColor?): ChartUnitStyle {
        this.background = background
        return this
    }

    open fun t(transform: ChartTransform?): ChartUnitStyle {
        this.transform = transform
        return this
    }

    open fun ts(textStyle: ChartTextStyle?): ChartUnitStyle {
        this.textStyle = textStyle
        return this
    }

    open fun ps(pathStyle: ChartPathStyle?): ChartUnitStyle {
        this.pathStyle = pathStyle
        return this
    }

    open fun pd(padding: ChartPadding?): ChartUnitStyle {
        this.padding = padding
        return this
    }

    open fun ss(shapeStyle: ChartShapeStyle?): ChartUnitStyle {
        this.shapeStyle = shapeStyle
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
        this.pathStyle = this@ChartUnitStyle.pathStyle
        this.padding = this@ChartUnitStyle.padding
        this.shapeStyle = this@ChartUnitStyle.shapeStyle
    }

    open fun deepCopy(): ChartUnitStyle = ChartUnitStyle().apply {
        this.zIndex = this@ChartUnitStyle.zIndex
        this.position = this@ChartUnitStyle.position?.copy()
        this.border = this@ChartUnitStyle.border?.copy()
        this.contentColor = this@ChartUnitStyle.contentColor?.copy()
        this.background = this@ChartUnitStyle.background?.copy()
        this.transform = this@ChartUnitStyle.transform?.copy()
        this.textStyle = this@ChartUnitStyle.textStyle?.copy()
        this.pathStyle = this@ChartUnitStyle.pathStyle?.copy()
        this.padding = this@ChartUnitStyle.padding?.copy()
        this.shapeStyle = this@ChartUnitStyle.shapeStyle?.deepCopy()
    }

    override fun toString(): String = "{zIndex: $zIndex\n\tposition: $position\n\tborder: $border\n\tbackground: $contentColor\n\t" +
            "transform: $transform\n\ttextStyle: $textStyle\n\tpathStyle: $pathStyle\n\tpadding: $padding\n\tshapeStyle: $shapeStyle}"
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
            setStatedStyle(state, value)
        }

    open fun copy(): ChartUnit = ChartUnit().setOther(this)

    open fun setOther(other: ChartUnit): ChartUnit {
        this.extras = other.extras
        this.children = other.children
        this.parent = other.parent
        this.chartView = other.chartView
        this.state = other.state
        this.styles = other.styles
        return this
    }

    open fun deepCopy(): ChartUnit = ChartUnit().setDeepOther(this)

    open fun setDeepOther(other: ChartUnit): ChartUnit {
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
        return this
    }

    open fun string(depth: Int = 0): String = "style: $style" + children?.joinToString { "\n" + "\t".repeat(depth) + it.string(depth + 1) }

    open fun updatePos(): ChartUnit {
        val position = this.style?.position ?: return this
        val pWidth: Float
        val pHeight: Float
        val pTop: Float
        val pLeft: Float = if (position.mode == ChartPosition.PosMode.ALIGN_PARENT && parent != null) {
            val pPosition = parent!!.style?.position ?: return this
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
            return this
        }
        position.trueWidth = trueSize(position.width, pWidth)
        position.trueHeight = trueSize(position.height, pHeight)
        val left = position.left
        val right = position.right
        position.trueLeft = when {
            left != null -> pLeft + trueSize(left, pWidth)
            right != null -> pLeft + trueSize2(right, pWidth, position.trueWidth)
            else -> 0f
        }
        val top = position.top
        val bottom = position.bottom
        position.trueTop = when {
            top != null -> pTop + trueSize(top, pHeight)
            bottom != null -> pTop + trueSize2(bottom, pHeight, position.trueHeight)
            else -> 0f
        }
        handlePadding(position)
        children?.forEach {
            it.updatePos()
        }
        // TODO: transform 对这里造成的影响
        return this
    }

    protected open fun handlePadding(position: ChartPosition) {
        val padding = style?.padding
        if (padding != null) {
            padding.truePL = trueSize(padding.paddingLeft ?: padding.padding, position.trueWidth)
            padding.truePR = trueSize(padding.paddingRight ?: padding.padding, position.trueWidth)
            position.trueWidth = position.trueWidth - padding.truePL - padding.truePR
            position.trueLeft += padding.truePL
            padding.truePT = trueSize(padding.paddingTop ?: padding.padding, position.trueHeight)
            padding.truePB = trueSize(padding.paddingBottom ?: padding.padding, position.trueHeight)
            position.trueHeight = position.trueHeight - padding.truePT - padding.truePB
            position.trueTop += padding.truePT
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

    open fun p(parent: ChartUnit?): ChartUnit {
        this.parent = parent
        return this
    }

    open fun cv(chartView: ChartView?): ChartUnit {
        this.chartView = chartView
        return this
    }

    open fun s(state: Int): ChartUnit {
        this.state = state
        return this
    }

    open fun setStatedStyle(state: Int, style: ChartUnitStyle?): ChartUnit {
        if (styles != null && style == null) {
            styles!!.remove(state)
        } else if (styles == null && style != null) {
            styles = mutableMapOf(state to style)
        } else if (styles != null && style != null) {
            styles!![state] = style
        }
        return this
    }

    open fun setStyle(style: ChartUnitStyle?): ChartUnit {
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

// ChartPadding 无效
open class ChartArea() : ChartUnit() {
    override fun updatePos(): ChartUnit {
        if (xys.size < 4) {
            return this
        }
        val position = this.style?.position ?: ChartPosition()
        this.style?.p(position) ?: return this
        val pWidth: Float
        val pHeight: Float
        val pTop: Float
        val pLeft: Float = if (position.mode == ChartPosition.PosMode.ALIGN_PARENT && parent != null) {
            val pPosition = parent!!.style?.position ?: return this
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
            return this
        }
        trueXys.clear()
        var minX = xys[0]
        var maxX = xys[0]
        var minY = xys[1]
        var maxY = xys[1]
        xys.forEachIndexed { index, xy ->
            val flag = index % 2 == 0
            val temp = trueSize(xy, when {
                flag -> pWidth
                else -> pHeight
            })
            trueXys.add(temp)
            if (flag) {
                minX = min(minX, temp)
                maxX = max(maxX, temp)
            } else {
                minY = min(minY, temp)
                maxY = max(maxY, temp)
            }
        }
        position.trueWidth = maxX - minX
        position.trueHeight = maxY - minY
        position.trueLeft = minX + pLeft
        position.trueTop = minY + pTop
        if (lineContentStyle != null) {
            position.trueWidth += lineContentStyle!!.borderWidth
            position.trueHeight += lineContentStyle!!.borderWidth
        }
        handlePadding(position)
        children?.forEach {
            it.updatePos()
        }
        return this
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

    open fun min(max: Float?): ChartAxis {
        this.max = max
        return this
    }

    override fun copy(): ChartUnit = ChartAxis().setOther(this)

    override fun setOther(other: ChartUnit): ChartUnit {
        if (other is ChartAxis) {
            this.symbols = other.symbols
            this.max = other.max
            this.min = other.min
        }
        return super.setOther(other)
    }

    override fun deepCopy(): ChartUnit = ChartAxis().setDeepOther(this)

    override fun setDeepOther(other: ChartUnit): ChartUnit {
        if (other is ChartAxis) {
            this.symbols = other.symbols?.toMutableMap()
            this.max = other.max
            this.min = other.min
        }
        return super.setDeepOther(other)
    }
}

open class ChartLegend

open class Chart
