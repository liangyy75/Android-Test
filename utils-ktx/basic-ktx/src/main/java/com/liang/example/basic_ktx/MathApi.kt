package com.liang.example.basic_ktx

import android.graphics.PointF
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author liangyuying.lyy75@bytedance.com
 * @date 2020/8/6
 * <p>
 * math util
 */

val pi = PI.toFloat()

// 旋转：在平面坐标上，任意点P(x1,y1)，绕一个坐标点Q(x2,y2)旋转θ角度后,新的坐标设为(x, y)的计算公式：
fun rotatePoint(x1: Float, y1: Float, x2: Float, y2: Float, angle: Float): PointF {
    val a = angle / 180 * pi
    return PointF((x1 - x2) * cos(a) - (y1 - y2) * sin(a) + x2, (x1 - x2) * sin(a) + (y1 - y2) * cos(a) + y2)
}

fun rotatePoints(xys: MutableList<Float>, cx: Float, cy: Float, angle: Float): MutableList<Float> {
    val result = mutableListOf<Float>()
    val a = angle / 180 * pi
    (0 until xys.size step 2).forEach { i ->
        val x = xys[i]
        val y = xys.getOrNull(i + 1) ?: return@forEach
        result.add((x - cx) * cos(a) - (y - cy) * sin(a) + cx)
        result.add((x - cx) * sin(a) + (y - cy) * cos(a) + cy)
    }
    return result
}

// 平移

// 缩放

// skew
fun skewPoint(x: Float, y: Float, sk1: Float, sk2: Float): PointF = PointF(x + y * sk1, y + x * sk2)
fun skewPoints(xys: MutableList<Float>, sk1: Float, sk2: Float): MutableList<Float> {
    val result = mutableListOf<Float>()
    (0 until xys.size step 2).forEach { i ->
        val x = xys[i]
        val y = xys.getOrNull(i + 1) ?: return@forEach
        result.add(x + y * sk1)
        result.add(y + x * sk2)
    }
    return result
}

fun getXYByLimitedX(x1: Float, y1: Float, x2: Float, y2: Float, x: Float): Float = (y1 - y2) * (x - x1) / (x1 - x2) + y1
fun getXYByLimitedY(x1: Float, y1: Float, x2: Float, y2: Float, y: Float): Float = (y - y1) * (x1 - x2) / (y1 - y2) + x1

fun getXYByLimitedUpperXY(x1: Float, y1: Float, x2: Float, y2: Float, x: Float, y: Float): MutableList<Float> {
    TODO("")
}

fun getXYByLimitedLowerXY(x1: Float, y1: Float, x2: Float, y2: Float, x: Float, y: Float): MutableList<Float> {
    TODO("")
}
