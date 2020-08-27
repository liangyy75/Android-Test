@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.liang.example.viewtest_kt.chart3

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
import com.liang.example.basic_ktx.EnumHelper
import com.liang.example.basic_ktx.MutablePair
import com.liang.example.utils.r.getDrawable
import com.liang.example.utils.r.sp2Px

/**
 * @author liangyuying
 * @date 2020/8/19
 * <p>
 * chart view version 3
 */
open class ChartView3 : View {
    constructor(c: Context) : super(c)
    constructor(c: Context, attributeSet: AttributeSet?) : super(c, attributeSet)
    constructor(c: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(c, attributeSet, defStyleAttr)
}

// 100% | number
// p | r | s | a
// w | h
// wrap-content
open class ChartPosition {
    open var width = "100%"
    open var height = "100%"
    open var left: String? = null
    open var right: String? = null
    open var top: String? = null
    open var bottom: String? = null
}

open class ChartBorder {  // 与 css 的 outline 类似
    open var width: String = "10"
    open var color: Int = Color.TRANSPARENT
    open var style: Int = Style.DASHED
    open var composition: Int = Composition.LEFT.or(Composition.RIGHT).or(Composition.TOP).or(Composition.BOTTOM)  // 这个只适用于长方形(悲)

    object Style {
        const val CHART_BORDER_STYLE = "chartBorderStyle"
        val SOLID = EnumHelper[CHART_BORDER_STYLE]  // setPathEffect(null)
        val DASHED = EnumHelper[CHART_BORDER_STYLE]  // setPathEffect(DashPathEffect(floatArrayOf(10f), 0))
        val DOTTED = EnumHelper[CHART_BORDER_STYLE]  // setPathEffect(PathDashPathEffect(path, 12, 0, PathDashPathEffect.Style.ROTATE))
    }

    object Composition {
        const val CHART_BORDER_COMPOSITION = "chartBorderComposition"
        val LEFT = EnumHelper.get2(CHART_BORDER_COMPOSITION)
        val RIGHT = EnumHelper.get2(CHART_BORDER_COMPOSITION)
        val TOP = EnumHelper.get2(CHART_BORDER_COMPOSITION)
        val BOTTOM = EnumHelper.get2(CHART_BORDER_COMPOSITION)
    }
}

open class ChartColor {
    open var color: Int? = null
    open var resId: Int = 0
    open var drawable: Drawable? = null

    open fun getBgBitmap(): Bitmap? {
        val bgDrawable = this.drawable ?: getDrawable(resId) ?: return null
        this.drawable = bgDrawable
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

    companion object {
        fun ofColor(color: Int?) = when (color) {
            null -> null
            else -> ChartColor().apply { this.color = color }
        }

        fun ofBgResId(bgResId: Int?) = when (bgResId) {
            null -> null
            else -> ChartColor().apply { this.resId = bgResId }
        }

        fun ofDrawable(drawable: Drawable?) = when (drawable) {
            null -> null
            else -> ChartColor().apply { this.drawable = drawable }
        }
    }
}

open class ChartBackground : ChartColor() {
    open var box: Int = Box.CONTENT_BOX
    open var x: String = "0"
    open var y: String = "0"

    open var shape: Int = ShapeType.RECANTAGE
    open var roundRadius: FloatArray = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)

    object Box {
        const val CHART_COLOR_BOX = "chartColorBox"
        val CONTENT_BOX = EnumHelper[CHART_COLOR_BOX]
        val PADDING_BOX = EnumHelper[CHART_COLOR_BOX]
        val BORDER_BOX = EnumHelper[CHART_COLOR_BOX]
        val MARGIN_BOX = EnumHelper[CHART_COLOR_BOX]
    }

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

interface ChartTransform {
    open class ChartTranslate(open var x: String = "0", open var y: String = "0") : ChartTransform
    open class ChartRotate(open var degrees: Float, px: String, py: String) : ChartTranslate(px, py)
    open class ChartScale(open var sx: Float, open var sy: Float, px: String, py: String) : ChartTranslate(px, py)
    open class ChartSkew(open var kx: Float, open var ky: Float, px: String, py: String) : ChartTranslate(px, py)
}

open class ChartPathStyle {
    open var join: Int = Join.BEVEL
    open var cap: Int = Cap.BUTT
    open var effect: PathEffect? = null
    open var ccwDirection: Boolean = true
    open var opPath: Path? = null

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

    open fun set(paint: Paint): ChartPathStyle {
        paint.strokeCap = toPaintCap()
        paint.strokeJoin = toPaintJoin()
        paint.pathEffect = effect
        return this
    }
}

open class ChartText(open var text: String) {
    open var color: Int = Color.BLACK  // TODO: ChartColor
    open var size: Float = sp2Px(12f).toFloat()
    open var weight: Int = Weight.NORMAL
    open var align: Int = Align.CENTER
    open var decoration: Int = Decoration.NONE
    open var isItalic: Boolean = false

    open var x: String = "50%s"
    open var y: String = "50%s"
    open var transforms: MutableList<ChartTransform>? = null

    open fun set(paint: Paint): ChartText {
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

open class ChartGap {
    open var gap: String? = null
    open var gapLeft: String? = null
    open var gapTop: String? = null
    open var gapRight: String? = null
    open var gapBottom: String? = null

    open fun getGL() = gapLeft ?: gap
    open fun getGT() = gapTop ?: gap
    open fun getGR() = gapRight ?: gap
    open fun getGB() = gapBottom ?: gap
}

open class ChartShape {
    open var lineContentStyle: ChartBorder? = null
    open var lineShapeStyle: Int = LineShapeStyle.SOLID
    open var xys: MutableList<String> = mutableListOf()
    open var opPathXys: MutableList<String>? = null
    open var pathOp: Path.Op = Path.Op.INTERSECT
    open var color: ChartColor? = null
    open var transforms: MutableList<ChartTransform>? = null

    open fun addXY(x: String, y: String) {
        xys.add(x)
        xys.add(y)
    }

    open fun addOpPathXY(x: String, y: String) {
        opPathXys = add(opPathXys, x)
        opPathXys!!.add(y)
    }

    object LineShapeStyle {
        const val LINE_STYLE = "chartLineShapeStyle"
        val SOLID = EnumHelper[LINE_STYLE]
        val DASH = EnumHelper[LINE_STYLE]
        val DOUBLE = EnumHelper[LINE_STYLE]
    }
}

open class ChartUnitStyle(open var shape: Int = ShapeType.RECANTAGE) {
    open var zIndex: Int = 0
    open var position: ChartPosition? = null
    open var padding: ChartGap? = null
    open var margin: ChartGap? = null
    open var borders: MutableList<ChartBorder>? = null
    open var backgrounds: MutableList<ChartBackground>? = null
    open var transforms: MutableList<ChartTransform>? = null
    open var texts: MutableList<ChartText>? = null
    open var shapes: MutableList<ChartShape>? = null

    open fun addBorder(border: ChartBorder) {
        borders = add(borders, border)
    }

    open fun addBackground(background: ChartBackground) {
        backgrounds = add(backgrounds, background)
    }

    open fun addTransform(transform: ChartTransform) {
        transforms = add(transforms, transform)
    }

    open fun addText(text: ChartText) {
        texts = add(texts, text)
    }

    open fun addShape(shape: ChartShape) {
        shapes = add(shapes, shape)
    }

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

open class ChartUnitStyle2 {
    open var width: Float = 0f
    open var height: Float = 0f
    open var left: Float = 0f
    open var top: Float = 0f
    open var right: Float = 0f
    open var bottom: Float = 0f

    open var paddingLeft = 0f
    open var paddingRight = 0f
    open var paddingTop = 0f
    open var paddingBottom = 0f

    open var marginLeft = 0f
    open var marginRight = 0f
    open var marginTop = 0f
    open var marginBottom = 0f

    open var borderWidths: List<Float>? = null
    open var backgrounds: List<MutablePair<Float, Float>>? = null
    open var transforms: List<MutablePair<Float, Float>>? = null
    open var texts: List<MutablePair<Float, Float>>? = null
    open var textTransforms: List<List<MutablePair<Float, Float>>?>? = null
    open var shapes: List<List<Float>>? = null
    open var shapeWidths: List<Float>? = null
    open var shapeTransforms: List<List<MutablePair<Float, Float>>?>? = null
    open var shapeOps: List<List<Float>?>? = null
}

fun <E> add(l: MutableList<E>?, e: E): MutableList<E> = when {
    l == null -> mutableListOf(e)
    !l.contains(e) -> {
        l.add(e)
        l
    }
    else -> l
}

fun <K, V> set(m: MutableMap<K, V>?, k: K, v: V?): MutableMap<K, V>? = when {
    v == null -> {
        m?.remove(k)
        m
    }
    m == null -> mutableMapOf(k to v)
    else -> {
        m[k] = v
        m
    }
}

open class CalcObj {
    open var w: Float = 0f
    open var h: Float = 0f
    open var rw: Float = 0f
    open var rh: Float = 0f
    open var pw: Float = 0f
    open var ph: Float = 0f

    open var l: Float = 0f
    open var t: Float = 0f
    open var rl: Float = 0f
    open var rt: Float = 0f
    open var pl: Float = 0f
    open var pt: Float = 0f
}

fun calc(v: String?, width: Float, height: Float, rootWidth: Float, rootHeight: Float, parentWidth: Float, parentHeight: Float): Float {
    return calc(v, CalcObj().apply {
        w = width
        h = height
        rw = rootWidth
        rh = rootHeight
        pw = parentWidth
        ph = parentHeight
    })
}

fun calc(v: String?, obj: CalcObj): Float {
    if (v.isNullOrEmpty()) {
        return 0f
    }
    val len = v.length
    when (v[len - 1]) {
        '%' -> return v.substring(0, len - 1).toFloat() / 100 * obj.pw
        in '0'..'9' -> return v.toFloat()
        'w' -> return when (v[len - 2]) {
            'p' -> v.substring(0, len - 3).toFloat() / 100 * obj.pw
            'r' -> v.substring(0, len - 3).toFloat() / 100 * obj.rw
            's' -> v.substring(0, len - 3).toFloat() / 100 * obj.w
            '%' -> v.substring(0, len - 2).toFloat() / 100 * obj.pw
            else -> 0f
        }
        'h' -> return when (v[len - 2]) {
            'p' -> v.substring(0, len - 3).toFloat() / 100 * obj.ph
            'r' -> v.substring(0, len - 3).toFloat() / 100 * obj.rh
            's' -> v.substring(0, len - 3).toFloat() / 100 * obj.h
            '%' -> v.substring(0, len - 2).toFloat() / 100 * obj.ph
            else -> 0f
        }
        'p' -> return when (v[len - 2]) {
            'w' -> v.substring(0, len - 3).toFloat() / 100 * obj.pw
            'h' -> v.substring(0, len - 3).toFloat() / 100 * obj.ph
            '%' -> v.substring(0, len - 2).toFloat() / 100 * obj.pw
            else -> 0f
        }
        's' -> return when (v[len - 2]) {
            'w' -> v.substring(0, len - 3).toFloat() / 100 * obj.w
            'h' -> v.substring(0, len - 3).toFloat() / 100 * obj.h
            '%' -> v.substring(0, len - 2).toFloat() / 100 * obj.w
            else -> 0f
        }
        'r' -> return when (v[len - 2]) {
            'w' -> v.substring(0, len - 3).toFloat() / 100 * obj.rw
            'h' -> v.substring(0, len - 3).toFloat() / 100 * obj.rh
            '%' -> v.substring(0, len - 2).toFloat() / 100 * obj.rw
            else -> 0f
        }
        else -> return 0f
    }
}

// 1. 使用 String ，或者 ChartValue ，还是 Int 。目前选择 String
// 2. Paint.Style.STROKE 画出来的 border 是一半在里，一半在外的。使用 path.transform(matrix, outputPath) ， matrix 可以 scale / rotate / skew / translate
// 3. transform 应该交给 path ，同时使用 path.computeBounds 计算宽高
// 4. border 与 canvas.clipPath / clipRegion

// 1. contentPathStyle, borderPathStyle, borders, backgrounds, texts, shapes, transforms, margin, padding
// 2. TODO: linear, relative, frame
// 3. bg-box: content-box, padding-box, border-box, margin-box 依靠 path.transform 计算吧
// 4. border-shadow, shadow, border-style, line-style. How to do?
//     1. paint.setPathEffect
//     2. paint.setShadowLayer
//     3. paint.setColorFilter
// 5. TODO: 渐变色
