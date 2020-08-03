package com.liang.example.viewtest_kt.chart

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

/**
 * @author 梁毓颖
 * @date 2020/7/31
 * <p>
 * canvas's helper class
 */

/**
 * 保留原本状态，之后旋转，然后action，最后恢复
 */
fun Canvas.rotate(degrees: Float, action: Canvas.() -> Unit) {
    this.save()
    this.rotate(degrees)
    this.action()
    this.restore()
}

fun Canvas.rotate(degrees: Float, px: Float, py: Float, action: Canvas.() -> Unit) {
    this.save()
    this.rotate(degrees, px, py)
    this.action()
    this.restore()
}

fun Canvas.translate(x: Float, y: Float, action: Canvas.() -> Unit) {
    this.save()
    this.translate(x, y)
    this.action()
    this.restore()
}

fun Canvas.scale(x: Float, y: Float, action: Canvas.() -> Unit) {
    this.save()
    this.scale(x, y)
    this.action()
    this.restore()
}

fun Canvas.skew(x: Float, y: Float, action: Canvas.() -> Unit) {
    this.save()
    this.skew(x, y)
    this.action()
    this.restore()
}

fun Canvas.mainAndStroke(main: Paint, stroke: Paint, drawStroke: Boolean, action: Canvas.(paint: Paint) -> Unit) {
    this.action(main)
    if (drawStroke) {
        this.action(stroke)
    }
}
