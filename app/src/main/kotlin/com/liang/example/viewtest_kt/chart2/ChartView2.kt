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
    protected open val path = Path()
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
                .s(ChartUnitStyle.ShapeType.CIRCLE)
                .ts(ChartTextStyle("test-text"))
        unit.addChild(ChartUnit().setStyle(style))
                .addChild(ChartUnit().setStyle(style.copy()
                        .p(position.copy().l(dp50))
                        .s(ChartUnitStyle.ShapeType.SQUARE)))
                .addChild(ChartUnit().setStyle(style.copy()
                        .p(position.copy().l(dp50 * 2))
                        .s(ChartUnitStyle.ShapeType.RECANTAGE)))
                .addChild(ChartUnit().setStyle(style.copy()
                        .p(position.copy().l(dp50 * 3))
                        .s(ChartUnitStyle.ShapeType.ROUND_RECTANGLE).rR(dp5)))
                .addChild(ChartUnit().setStyle(style.copy()
                        .p(position.copy().l(dp50 * 4))
                        .s(ChartUnitStyle.ShapeType.DIAMOND)))
                .addChild(ChartUnit().setStyle(style.copy()
                        .p(position.copy().l(dp50 * 5))
                        .s(ChartUnitStyle.ShapeType.TRIANGLE)))
                .addChild(ChartUnit().setStyle(style.copy()
                        .p(position.copy().l(0f).t(dp50))
                        .s(ChartUnitStyle.ShapeType.TRIANGLEDOWN)))
    }

    init {
        testSymbol()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        unit.update()
        // ApiManager.LOGGER.d(TAG, unit.string())
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
        style.pathStyle?.apply {
            strokePaint.reset()
            strokePaint.strokeCap = toPaintCap()
            strokePaint.strokeJoin = toPaintJoin()
            strokePaint.pathEffect = effect
        }
        // TODO: draw
        path.reset()
        val pathDir = style.pathStyle?.toPathDirection() ?: Path.Direction.CCW
        when (style.shape) {
            ChartUnitStyle.ShapeType.NONE -> Unit
            ChartUnitStyle.ShapeType.CIRCLE -> path.addCircle(unit.trueXys[0], unit.trueXys[1], unit.trueXys[2], pathDir)
            ChartUnitStyle.ShapeType.ROUND_RECTANGLE -> path.addRoundRect(left, top, left + width, top + height, unit.style!!.roundRadius, pathDir)
            else -> {
                val xys = unit.trueXys
                path.moveTo(xys[0], xys[1])
                (3 until xys.size step 2).forEach { i ->
                    path.lineTo(xys[i - 1], xys[i])
                }
            }
        }
        if (style.shape != ChartUnitStyle.ShapeType.NONE) {
        }
        style.contentColor?.set(mainPaint, width, height)
        style.background?.set(mainPaint, width, height)
        style.shapeStyle?.lineContentStyle?.set(mainPaint)
        style.border?.set(strokePaint)
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

    override fun toString(): String = "{width: $width, height: $height, left: $left, top: $top, right: $right, bottom: $bottom, mode: $mode}"
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
    open var lineContentStyle: ChartBorder? = null
    open var lineShapeStyle: Int = LineShapeStyle.SOLID
    open var xys: MutableList<Float> = mutableListOf()
    open var mode: Int = ChartPosition.PosMode.ALIGN_PARENT

    open fun xy(x: Float, y: Float): ChartShapeStyle {
        xys.add(x)
        xys.add(y)
        return this
    }

    open fun lcs(lineStyle: ChartBorder?): ChartShapeStyle {
        this.lineContentStyle = lineStyle
        return this
    }

    open fun lss(lineShapeStyle: Int): ChartShapeStyle {
        this.lineShapeStyle = lineShapeStyle
        return this
    }

    open fun m(mode: Int): ChartShapeStyle {
        this.mode = mode
        return this
    }

    open fun copy(): ChartShapeStyle = ChartShapeStyle().setOther(this)

    open fun setOther(other: ChartShapeStyle): ChartShapeStyle {
        this.lineContentStyle = other.lineContentStyle
        this.lineShapeStyle = other.lineShapeStyle
        this.xys = other.xys
        return this
    }

    open fun deepCopy(): ChartShapeStyle = ChartShapeStyle().setDeepOther(this)

    open fun setDeepOther(other: ChartShapeStyle): ChartShapeStyle {
        this.lineContentStyle = other.lineContentStyle?.copy()
        this.lineShapeStyle = other.lineShapeStyle
        this.xys = other.xys.toMutableList()
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
    open var pathStyle: ChartPathStyle? = null
    open var padding: ChartPadding? = null
    open var shapeStyle: ChartShapeStyle? = null

    open var shape: Int = ShapeType.RECANTAGE
    open var roundRadius: FloatArray = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    open var close: Boolean = false

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

    open fun s(shape: Int): ChartUnitStyle {
        this.shape = shape
        return this
    }

    open fun rR(roundRadius: FloatArray): ChartUnitStyle {
        this.roundRadius = roundRadius
        return this
    }

    open fun rR(roundRadius: Float): ChartUnitStyle {
        this.roundRadius = floatArrayOf(roundRadius, roundRadius, roundRadius, roundRadius, roundRadius, roundRadius, roundRadius, roundRadius)
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

    override fun toString(): String = "{zIndex: $zIndex, shape: $shape, roundRadius: $roundRadius\n\tposition: $position\n\tborder: $border\n\t" +
            "background: $contentColor\n\ttransform: $transform\n\ttextStyle: $textStyle\n\tpathStyle: $pathStyle\n\tpadding: $padding\n\t" +
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
            setStatedStyle(state, value)
        }

    open var trueWidth: Float = 0f
    open var trueHeight: Float = 0f
    open var trueLeft: Float = 0f
    open var trueTop: Float = 0f
    open var trueXys: MutableList<Float> = mutableListOf()
    open var trueTextX: Float = 0f
    open var trueTextY: Float = 0f

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

    open fun update(): ChartUnit {
        val style = this.style ?: return this
        val shapeStyle = style.shapeStyle
        val position = style.position
        val mode = shapeStyle?.mode ?: position?.mode ?: return this
        val pWidth: Float
        val pHeight: Float
        val pTop: Float
        val pLeft: Float = if (mode == ChartPosition.PosMode.ALIGN_PARENT && parent != null) {
            val parent = this.parent ?: return this
            pWidth = parent.trueWidth
            pHeight = parent.trueHeight
            pTop = parent.trueTop
            parent.trueLeft
        } else if (mode == ChartPosition.PosMode.ALIGN_ROOT && chartView != null) {
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
            calTruePosByShapeConfig(shapeStyle, pWidth, pHeight, pLeft, pTop)
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
        handlePadding()
        children?.forEach {
            it.update()
        }
        // TODO: transform 对这里造成的影响
        return this
    }

    protected open fun calTruePosByShapeConfig(shapeStyle: ChartShapeStyle, pWidth: Float, pHeight: Float, pLeft: Float, pTop: Float) {
        trueXys.clear()
        val xys = shapeStyle.xys
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

        trueXys.clear()
        val size = min(trueWidth, trueHeight)
        when (shape) {
            ChartUnitStyle.ShapeType.CIRCLE -> this.addTrueXY(trueLeft + trueWidth / 2, trueTop + trueHeight / 2).trueXys.add(size / 2)
            ChartUnitStyle.ShapeType.ROUND_RECTANGLE -> {}  // 这里做不了什么
            ChartUnitStyle.ShapeType.SQUARE -> {
                val wSpace = (trueWidth - size) / 2
                val hSpace = (trueHeight - size) / 2
                addTrueXY(trueLeft + wSpace, trueTop + hSpace)
                addTrueXY(trueLeft + wSpace, trueTop + hSpace + trueHeight)
                addTrueXY(trueLeft + wSpace + trueWidth, trueTop + hSpace + trueHeight)
                addTrueXY(trueLeft + wSpace + trueWidth, trueTop + hSpace)
            }
            ChartUnitStyle.ShapeType.RECANTAGE -> {
                addTrueXY(trueLeft, trueTop)
                addTrueXY(trueLeft, trueTop + trueHeight)
                addTrueXY(trueLeft + trueWidth, trueTop + trueHeight)
                addTrueXY(trueLeft + trueWidth, trueTop)
            }
            ChartUnitStyle.ShapeType.DIAMOND -> {
                addTrueXY(trueLeft + trueWidth / 2, trueTop)
                addTrueXY(trueLeft, trueTop + trueHeight / 2)
                addTrueXY(trueLeft + trueWidth / 2, trueTop + trueHeight)
                addTrueXY(trueLeft + trueWidth, trueTop + trueHeight / 2)
                style!!.close = true
            }
            ChartUnitStyle.ShapeType.TRIANGLE -> {
                addTrueXY(trueLeft, trueTop + trueHeight)
                addTrueXY(trueLeft + trueWidth / 2, trueTop)
                addTrueXY(trueLeft + trueWidth, trueTop + trueHeight)
                style!!.close = true
            }
            ChartUnitStyle.ShapeType.TRIANGLEDOWN -> {
                addTrueXY(trueLeft, trueTop)
                addTrueXY(trueLeft + trueWidth / 2, trueTop + trueHeight)
                addTrueXY(trueLeft + trueWidth, trueTop)
                style!!.close = true
            }
        }
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
