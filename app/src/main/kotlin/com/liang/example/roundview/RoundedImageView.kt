package com.liang.example.roundview

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader.TileMode
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.Log
import android.widget.ImageView.ScaleType
import androidx.annotation.IntDef
import com.liang.example.basic_ktx.only

@Retention(AnnotationRetention.SOURCE)
@IntDef(Corner.TOP_LEFT, Corner.TOP_RIGHT, Corner.BOTTOM_LEFT, Corner.BOTTOM_RIGHT)
annotation class Corner {
    companion object {
        const val TOP_LEFT = 0
        const val TOP_RIGHT = 1
        const val BOTTOM_RIGHT = 2
        const val BOTTOM_LEFT = 3
    }
}

open class RoundedDrawable(open val mBitmap: Bitmap) : Drawable() {
    protected open var mBounds = RectF()
    protected open var mDrawableRect = RectF()

    protected open var mBitmapPaint: Paint = Paint()
    protected open var mBitmapWidth = mBitmap.width
    protected open var mBitmapHeight = mBitmap.height
    protected open var mBitmapRect = RectF()

    protected open var mBorderRect = RectF()
    protected open var mBorderPaint: Paint = Paint()
    protected open var mShaderMatrix: Matrix = Matrix()
    protected open var mSquareCornersRect = RectF()

    open var mTileModeX = TileMode.CLAMP
        set(value) {
            if (field != value) {
                field = value
                mRebuildShader = true
                invalidateSelf()
            }
        }
    protected open var mTileModeY = TileMode.CLAMP
        set(value) {
            if (field != value) {
                field = value
                mRebuildShader = true
                invalidateSelf()
            }
        }
    protected open var mRebuildShader = true

    open var mCornerRadius = 0f
    protected open var mCornersRounded = booleanArrayOf(true, true, true, true)  // [ topLeft, topRight, bottomLeft, bottomRight ]

    open var mOval = false
    open var mBorderWidth = 0f
        set(value) {
            field = value
            mBorderPaint.strokeWidth = field
        }
    open var mBorderColor = ColorStateList.valueOf(DEFAULT_BORDER_COLOR)
    open var mScaleType = ScaleType.FIT_CENTER
        set(value) {
            if (field != value) {
                field = value
                updateShaderMatrix()
            }
        }

    init {
        mBitmapRect.set(0f, 0f, mBitmapWidth.toFloat(), mBitmapHeight.toFloat())
        mBitmapPaint.style = Paint.Style.FILL
        mBitmapPaint.isAntiAlias = true

        mBorderPaint.style = Paint.Style.STROKE
        mBorderPaint.isAntiAlias = true
        mBorderPaint.color = mBorderColor.getColorForState(state, DEFAULT_BORDER_COLOR)
        mBorderPaint.strokeWidth = mBorderWidth
    }

    override fun isStateful(): Boolean = mBorderColor.isStateful

    override fun onStateChange(state: IntArray?): Boolean {
        val newColor = mBorderColor.getColorForState(state, 0)
        return when {
            mBorderPaint.color != newColor -> {
                mBorderPaint.color = newColor
                true
            }
            else -> super.onStateChange(state)
        }
    }

    protected open fun updateShaderMatrix() {
        val scale: Float
        when (mScaleType) {
            ScaleType.CENTER -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.reset()
                mShaderMatrix.setTranslate((mBorderRect.width() - mBitmapWidth) * 0.5f + 0.5f, (mBorderRect.height() - mBitmapHeight) * 0.5f + 0.5f)
            }
            ScaleType.CENTER_CROP -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.reset()
                var dx = 0f
                var dy = 0f
                if (mBitmapWidth * mBorderRect.height() > mBorderRect.width() * mBitmapHeight) {
                    scale = mBorderRect.height() / mBitmapHeight.toFloat()
                    dx = (mBorderRect.width() - mBitmapWidth * scale) * 0.5f
                } else {
                    scale = mBorderRect.width() / mBitmapWidth.toFloat()
                    dy = (mBorderRect.height() - mBitmapHeight * scale) * 0.5f
                }
                mShaderMatrix.setScale(scale, scale)
                mShaderMatrix.postTranslate((dx + 0.5f).toInt() + mBorderWidth / 2, (dy + 0.5f).toInt() + mBorderWidth / 2)
            }
            ScaleType.CENTER_INSIDE -> {
                mShaderMatrix.reset()
                scale = if (mBitmapWidth <= mBounds.width() && mBitmapHeight <= mBounds.height()) {
                    1.0f
                } else {
                    (mBounds.width() / mBitmapWidth.toFloat()).coerceAtMost(mBounds.height() / mBitmapHeight.toFloat())
                }
                val dx = (mBounds.width() - mBitmapWidth * scale) * 0.5f + 0.5f
                val dy = (mBounds.height() - mBitmapHeight * scale) * 0.5f + 0.5f
                mShaderMatrix.setScale(scale, scale)
                mShaderMatrix.postTranslate(dx, dy)
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
            ScaleType.FIT_CENTER -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.CENTER)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
            ScaleType.FIT_END -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.END)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
            ScaleType.FIT_START -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.START)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
            ScaleType.FIT_XY -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.reset()
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
            else -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.CENTER)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
        }
        mDrawableRect.set(mBorderRect)
        mRebuildShader = true
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        mBounds.set(bounds)
        updateShaderMatrix()
    }

    override fun draw(canvas: Canvas) {
        if (mRebuildShader) {
            val bitmapShader = BitmapShader(mBitmap, mTileModeX, mTileModeY)
            if (mTileModeX == TileMode.CLAMP && mTileModeY == TileMode.CLAMP) {
                bitmapShader.setLocalMatrix(mShaderMatrix)
            }
            mBitmapPaint.shader = bitmapShader
            mRebuildShader = false
        }
        if (mOval) {
            if (mBorderWidth > 0) {
                canvas.drawOval(mDrawableRect, mBitmapPaint)
                canvas.drawOval(mBorderRect, mBorderPaint)
            } else {
                canvas.drawOval(mDrawableRect, mBitmapPaint)
            }
        } else {
            if (mCornersRounded.any { it }) {
                val radius = mCornerRadius
                if (mBorderWidth > 0) {
                    canvas.drawRoundRect(mDrawableRect, radius, radius, mBitmapPaint)
                    canvas.drawRoundRect(mBorderRect, radius, radius, mBorderPaint)
                    redrawBitmapForSquareCorners(canvas)
                    redrawBorderForSquareCorners(canvas)
                } else {
                    canvas.drawRoundRect(mDrawableRect, radius, radius, mBitmapPaint)
                    redrawBitmapForSquareCorners(canvas)
                }
            } else {
                canvas.drawRect(mDrawableRect, mBitmapPaint)
                if (mBorderWidth > 0) {
                    canvas.drawRect(mBorderRect, mBorderPaint)
                }
            }
        }
    }

    protected open fun redrawBitmapForSquareCorners(canvas: Canvas) {
        if (mCornersRounded.all { it } || mCornerRadius == 0f) {
            return
        }
        val left = mDrawableRect.left
        val top = mDrawableRect.top
        val right = left + mDrawableRect.width()
        val bottom = top + mDrawableRect.height()
        val radius = mCornerRadius
        if (!mCornersRounded[Corner.TOP_LEFT]) {
            mSquareCornersRect[left, top, left + radius] = top + radius
            canvas.drawRect(mSquareCornersRect, mBitmapPaint)
        }
        if (!mCornersRounded[Corner.TOP_RIGHT]) {
            mSquareCornersRect[right - radius, top, right] = radius
            canvas.drawRect(mSquareCornersRect, mBitmapPaint)
        }
        if (!mCornersRounded[Corner.BOTTOM_RIGHT]) {
            mSquareCornersRect[right - radius, bottom - radius, right] = bottom
            canvas.drawRect(mSquareCornersRect, mBitmapPaint)
        }
        if (!mCornersRounded[Corner.BOTTOM_LEFT]) {
            mSquareCornersRect[left, bottom - radius, left + radius] = bottom
            canvas.drawRect(mSquareCornersRect, mBitmapPaint)
        }
    }

    protected open fun redrawBorderForSquareCorners(canvas: Canvas) {
        if (mCornersRounded.all { it } || mCornerRadius == 0f) {
            return
        }
        val left = mDrawableRect.left
        val top = mDrawableRect.top
        val right = left + mDrawableRect.width()
        val bottom = top + mDrawableRect.height()
        val radius = mCornerRadius
        val offset = mBorderWidth / 2
        if (!mCornersRounded[Corner.TOP_LEFT]) {
            canvas.drawLine(left - offset, top, left + radius, top, mBorderPaint)
            canvas.drawLine(left, top - offset, left, top + radius, mBorderPaint)
        }
        if (!mCornersRounded[Corner.TOP_RIGHT]) {
            canvas.drawLine(right - radius - offset, top, right, top, mBorderPaint)
            canvas.drawLine(right, top - offset, right, top + radius, mBorderPaint)
        }
        if (!mCornersRounded[Corner.BOTTOM_RIGHT]) {
            canvas.drawLine(right - radius - offset, bottom, right + offset, bottom, mBorderPaint)
            canvas.drawLine(right, bottom - radius, right, bottom, mBorderPaint)
        }
        if (!mCornersRounded[Corner.BOTTOM_LEFT]) {
            canvas.drawLine(left - offset, bottom, left + radius, bottom, mBorderPaint)
            canvas.drawLine(left, bottom - radius, left, bottom, mBorderPaint)
        }
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun getAlpha(): Int = mBitmapPaint.alpha
    override fun setAlpha(alpha: Int) {
        mBitmapPaint.alpha = alpha
        invalidateSelf()
    }

    override fun getColorFilter(): ColorFilter? = mBitmapPaint.colorFilter
    override fun setColorFilter(cf: ColorFilter?) {
        mBitmapPaint.colorFilter = cf
        invalidateSelf()
    }

    override fun setDither(dither: Boolean) {
        mBitmapPaint.isDither = dither
        invalidateSelf()
    }

    override fun setFilterBitmap(filter: Boolean) {
        mBitmapPaint.isFilterBitmap = filter
        invalidateSelf()
    }

    override fun getIntrinsicWidth(): Int = mBitmapWidth
    override fun getIntrinsicHeight(): Int = mBitmapHeight

    open fun getCornerRadius(@Corner corner: Int): Float = if (mCornersRounded[corner]) mCornerRadius else 0f
    open fun setCornerRadius(radius: Float): RoundedDrawable? {
        setCornerRadius(radius, radius, radius, radius)
        return this
    }

    open fun setCornerRadius(@Corner corner: Int, radius: Float): RoundedDrawable? {
        require(!(radius != 0f && mCornerRadius != 0f && mCornerRadius != radius)) { "Multiple nonzero corner radii not yet supported." }
        if (radius == 0f) {
            if (mCornersRounded.only(corner)) {
                mCornerRadius = 0f
            }
            mCornersRounded[corner] = false
        } else {
            if (mCornerRadius == 0f) {
                mCornerRadius = radius
            }
            mCornersRounded[corner] = true
        }
        return this
    }

    open fun setCornerRadius(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float): RoundedDrawable {
        val radiusSet: MutableSet<Float> = mutableSetOf(topLeft, topRight, bottomRight, bottomLeft)
        radiusSet.remove(0f)
        require(radiusSet.size <= 1) { "Multiple nonzero corner radii not yet supported." }
        mCornerRadius = if (radiusSet.isNotEmpty()) {
            val radius = radiusSet.iterator().next()
            require(!(java.lang.Float.isInfinite(radius) || java.lang.Float.isNaN(radius) || radius < 0)) { "Invalid radius value: $radius" }
            radius
        } else {
            0f
        }
        mCornersRounded[Corner.TOP_LEFT] = topLeft > 0
        mCornersRounded[Corner.TOP_RIGHT] = topRight > 0
        mCornersRounded[Corner.BOTTOM_RIGHT] = bottomRight > 0
        mCornersRounded[Corner.BOTTOM_LEFT] = bottomLeft > 0
        return this
    }

    open fun setBorderColor(colors: ColorStateList?): RoundedDrawable {
        mBorderColor = colors ?: ColorStateList.valueOf(0)
        mBorderPaint.color = mBorderColor.getColorForState(state, DEFAULT_BORDER_COLOR)
        return this
    }

    open fun toBitmap(): Bitmap? = drawableToBitmap(this)

    companion object {
        const val TAG = "RoundedDrawable"
        val DEFAULT_BORDER_COLOR: Int = Color.BLACK

        fun fromBitmap(bitmap: Bitmap?): RoundedDrawable? = bitmap?.let { RoundedDrawable(it) }

        fun fromDrawable(drawable: Drawable?): Drawable? {
            if (drawable != null) {
                if (drawable is RoundedDrawable) {
                    return drawable
                } else if (drawable is LayerDrawable) {
                    val cs = drawable.mutate().constantState
                    val ld: LayerDrawable = (cs?.newDrawable() ?: drawable) as LayerDrawable
                    val num: Int = ld.numberOfLayers
                    for (i in 0 until num) {
                        val d: Drawable = ld.getDrawable(i)
                        ld.setDrawableByLayerId(ld.getId(i), fromDrawable(d))
                    }
                    return ld
                }
                val bm: Bitmap? = drawableToBitmap(drawable)
                if (bm != null) {
                    return RoundedDrawable(bm)
                }
            }
            return drawable
        }

        fun drawableToBitmap(drawable: Drawable): Bitmap? {
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }
            var bitmap: Bitmap? = null
            val width = drawable.intrinsicWidth.coerceAtLeast(2)
            val height = drawable.intrinsicHeight.coerceAtLeast(2)
            try {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
            } catch (e: Throwable) {
                e.printStackTrace()
                Log.w(TAG, "Failed to create bitmap from drawable!")
            }
            return bitmap
        }
    }
}

open class RoundedImageView {}

open class RoundedTransformationBuilder
