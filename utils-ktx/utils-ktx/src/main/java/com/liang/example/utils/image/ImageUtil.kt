package com.liang.example.utils.image

import android.graphics.Bitmap
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import java.nio.IntBuffer


fun getRoundRectBitmap(bitmap: Bitmap, radius: Float, targetWidth: Float, targetHeight: Float): Bitmap {
    val paint = Paint()
    paint.isAntiAlias = true
    paint.isFilterBitmap = true
    val bmWidth: Int = bitmap.width
    val bmHeight: Int = bitmap.height
    val rectF = RectF(0f, 0f, targetWidth, targetHeight)
    val canvas = Canvas(bitmap)
    paint.xfermode = null
    canvas.drawRoundRect(rectF, radius, radius, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, Rect(0, 0, bmWidth, bmHeight), rectF, paint)
    return bitmap
}

fun getRoundRectBitmap2(bitmap: Bitmap, radius: Float, targetWidth: Int, targetHeight: Int): Bitmap {
    val result = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val rect = Rect(0, 0, bitmap.width, bitmap.height)
    val rectF = RectF(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat())
    val paint = Paint()
    paint.isAntiAlias = true
    paint.isFilterBitmap = true
    val path = Path()
    path.addRoundRect(rectF, radius, radius, Path.Direction.CW)
    canvas.clipPath(path, Region.Op.INTERSECT)
    canvas.drawBitmap(bitmap, rect, rectF, paint)
    return result
}

const val CORNER_TOP_LEFT = 1
const val CORNER_TOP_RIGHT = 1 shl 1
const val CORNER_BOTTOM_LEFT = 1 shl 2
const val CORNER_BOTTOM_RIGHT = 1 shl 3
const val CORNER_ALL = CORNER_TOP_LEFT or CORNER_TOP_RIGHT or CORNER_BOTTOM_LEFT or CORNER_BOTTOM_RIGHT

/**
 * 把图片某固定角变成圆角
 *
 * @param bitmap 需要修改的图片
 * @param pixels 圆角的弧度
 * @param corners 需要显示圆弧的位置
 * @return 圆角图片
 */
fun toRoundCorner(bitmap: Bitmap, pixels: Int, corners: Int): Bitmap {
    //创建一个等大的画布
    val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val color = -0xbdbdbe
    val paint = Paint()
    //获取一个跟图片相同大小的矩形
    val rect = Rect(0, 0, bitmap.width, bitmap.height)
    //生成包含坐标的矩形对象
    val rectF = RectF(rect)
    //圆角的半径
    val roundPx = pixels.toFloat()
    paint.isAntiAlias = true //去锯齿
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    //绘制圆角矩形
    canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
    //异或将需要变为圆角的位置的二进制变为0
    val notRoundedCorners = corners xor CORNER_ALL
    //哪个角不是圆角我再把你用矩形画出来
    if (notRoundedCorners and CORNER_TOP_LEFT != 0) {
        canvas.drawRect(0f, 0f, roundPx, roundPx, paint)
    }
    if (notRoundedCorners and CORNER_TOP_RIGHT != 0) {
        canvas.drawRect(rectF.right - roundPx, 0f, rectF.right, roundPx, paint)
    }
    if (notRoundedCorners and CORNER_BOTTOM_LEFT != 0) {
        canvas.drawRect(0f, rectF.bottom - roundPx, roundPx, rectF.bottom, paint)
    }
    if (notRoundedCorners and CORNER_BOTTOM_RIGHT != 0) {
        canvas.drawRect(rectF.right - roundPx, rectF.bottom - roundPx, rectF.right, rectF.bottom, paint)
    }
    //通过SRC_IN的模式取源图片和圆角矩形重叠部分
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    //绘制成Bitmap对象
    canvas.drawBitmap(bitmap, rect, rect, paint)
    return output
}

fun blendedBitmap(source: Bitmap, layer: Bitmap, blendMode: BlendMode?, alpha: Float): Bitmap? {
    val base = source.copy(Bitmap.Config.ARGB_8888, true)
    val blend = layer.copy(Bitmap.Config.ARGB_8888, false)
    val buffBase: IntBuffer = IntBuffer.allocate(base.width * base.height)
    base.copyPixelsToBuffer(buffBase)
    buffBase.rewind()
    val buffBlend: IntBuffer = IntBuffer.allocate(blend.width * blend.height)
    blend.copyPixelsToBuffer(buffBlend)
    buffBlend.rewind()
    val buffOut: IntBuffer = IntBuffer.allocate(base.width * base.height)
    buffOut.rewind()
    while (buffOut.position() < buffOut.limit()) {
        val filterInt: Int = buffBlend.get()
        val srcInt: Int = buffBase.get()
        val redValueFilter: Int = Color.red(filterInt)
        val greenValueFilter: Int = Color.green(filterInt)
        val blueValueFilter: Int = Color.blue(filterInt)
        val redValueSrc: Int = Color.red(srcInt)
        val greenValueSrc: Int = Color.green(srcInt)
        val blueValueSrc: Int = Color.blue(srcInt)
        val redValueFinal = hardlight(redValueFilter, redValueSrc)
        val greenValueFinal = hardlight(greenValueFilter, greenValueSrc)
        val blueValueFinal = hardlight(blueValueFilter, blueValueSrc)
        val pixel: Int = Color.argb(255, redValueFinal, greenValueFinal, blueValueFinal)
        buffOut.put(pixel)
    }
    buffOut.rewind()
    base.copyPixelsFromBuffer(buffOut)
    blend.recycle()
    return base
}

fun hardlight(in1: Int, in2: Int): Int {
    val image = in2.toFloat()
    val mask = in1.toFloat()
    return (if (image < 128) 2 * mask * image / 255 else 255 - 2 * (255 - mask) * (255 - image) / 255).toInt()
}
