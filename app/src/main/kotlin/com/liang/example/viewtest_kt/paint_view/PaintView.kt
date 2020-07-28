@file:Suppress("LeakingThis", "unused")

package com.liang.example.viewtest_kt.paint_view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator

// 线段转折处不够平滑 -- 手指轨迹
open class PaintView : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    protected open val mPath = Path()
    protected open val mPaint = Paint()

    init {
        mPaint.color = Color.GREEN
        mPaint.style = Paint.Style.STROKE
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mPath.moveTo(event.x, event.y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                mPath.lineTo(event.x, event.y)
                postInvalidate()
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawPath(mPath, mPaint)
    }

    open fun reset() {
        mPath.reset()
        invalidate()
    }
}

// https://blog.csdn.net/harvic880925/article/details/50995587 手指轨迹
open class PaintView2 : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    protected open val mPath = Path()
    protected open val mPaint = Paint()
    protected open var mPreX = 0f
    protected open var mPreY = 0f

    init {
        mPaint.style = Paint.Style.STROKE
        mPaint.color = Color.GREEN
        mPaint.strokeWidth = 2f
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mPreX = event.x
                mPreY = event.y
                mPath.lineTo(mPreX, mPreY)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val endX = (mPreX + event.x) / 2
                val endY = (mPreY + event.y) / 2
                mPath.quadTo(mPreX, mPreY, endX, endY)
                mPreX = event.x
                mPreY = event.y
                postInvalidate()
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawPath(mPath, mPaint)
    }

    open fun reset() {
        mPath.reset()
        invalidate()
    }
}

// 波浪效果
open class PaintView3 : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    protected open val mPath = Path()
    protected open val mPaint = Paint()
    protected open var mOriginX = 0f
    protected open var mOriginY = 0f
    protected open var mItemWaveLength = 400f
    protected open var dx = 0

    open fun setOrigin(x: Float, y: Float) {
        mOriginX = x
        mOriginY = y
    }

    init {
        mPaint.style = Paint.Style.STROKE
        mPaint.color = Color.GREEN
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        mPath.reset()
        val halfWaveLen = mItemWaveLength / 2
        mPath.moveTo(-mItemWaveLength + dx, mOriginY)
        var i = -mItemWaveLength
        while (i <= width + mItemWaveLength) {
            mPath.rQuadTo(halfWaveLen / 2, -50f, halfWaveLen, 0f)
            mPath.rQuadTo(halfWaveLen / 2, 50f, halfWaveLen, 0f)
            i += mItemWaveLength
        }
        mPath.lineTo(width.toFloat(), height.toFloat())
        mPath.lineTo(0f, height.toFloat())
        mPath.close()
        canvas.drawPath(mPath, mPaint)
    }

    open fun startAnim(): ValueAnimator = ValueAnimator.ofInt(0, mItemWaveLength.toInt()).apply {
        duration = 2000
        duration = 2000
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener { animation ->
            dx = animation.animatedValue as Int
            postInvalidate()
        }
        start()
    }
}
