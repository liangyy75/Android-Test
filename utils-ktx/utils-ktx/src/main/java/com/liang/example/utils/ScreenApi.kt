package com.liang.example.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.WindowManager
import android.util.DisplayMetrics
import android.graphics.Bitmap
import android.app.Activity
import android.graphics.Rect


private fun getDisplayMetrics(context: Context): DisplayMetrics {
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val outMetrics = DisplayMetrics()
    wm.defaultDisplay.getMetrics(outMetrics)
    return outMetrics
}

fun getScreenWidthPixels(context: Context): Int = getDisplayMetrics(context).widthPixels
fun getScreenHeightPixels(context: Context): Int = getDisplayMetrics(context).heightPixels
fun getScreenDensityDpi(context: Context): Int = getDisplayMetrics(context).densityDpi
fun getScreenDensity(context: Context): Float = getDisplayMetrics(context).density
fun getScreenXDpi(context: Context): Float = getDisplayMetrics(context).xdpi
fun getScreenYDpi(context: Context): Float = getDisplayMetrics(context).ydpi
fun getScreenScaleDensity(context: Context): Float = getDisplayMetrics(context).scaledDensity

// 状态栏高度
@SuppressLint("PrivateApi")
@Deprecated("use unsafe methods")
fun getStatusHeight(context: Context): Int {
    var statusHeight = -1
    try {
        val clazz = Class.forName("com.android.internal.R\$dimen.xml")
        val `object` = clazz.newInstance()
        val height = Integer.parseInt(clazz.getField("status_bar_height").get(`object`)?.toString()
                ?: "-1")
        statusHeight = context.resources.getDimensionPixelSize(height)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return statusHeight
}

// 获取当前屏幕截图，包含状态栏
@Deprecated("use deprecated methods")
@JvmOverloads
fun snapShot(activity: Activity, x: Int = 0, y: Int = 0, rw: Int = 0, rh: Int = 0): Bitmap? {
    val view = activity.window.decorView
    view.isDrawingCacheEnabled = true
    view.buildDrawingCache()
    val bmp = view.drawingCache
    val displayMetrics = getDisplayMetrics(activity)
    val bp: Bitmap? = Bitmap.createBitmap(bmp, x, x, displayMetrics.widthPixels - rw, displayMetrics.heightPixels - rh)
    view.destroyDrawingCache()
    return bp
}

fun snapShotWithoutStatusBar(activity: Activity): Bitmap? {
    val frame = Rect()
    activity.window.decorView.getWindowVisibleDisplayFrame(frame)
    return snapShot(activity, 0, 0, 0, frame.top)
}
