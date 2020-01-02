package com.liang.example.utils.r

import android.content.Context
import android.content.res.Resources

val density = Resources.getSystem().displayMetrics.density
val scaledDensity = Resources.getSystem().displayMetrics.scaledDensity

fun dp2px(dpValue: Float): Int = (0.5f + dpValue * density).toInt()
fun dp2px(dpValue: Float, context: Context): Int = (0.5f + dpValue * context.resources.displayMetrics.density).toInt()

fun px2dp(pxValue: Float): Float = pxValue / density
fun px2dp(pxValue: Float, context: Context): Float = pxValue / context.resources.displayMetrics.density

fun dip2px(dpValue: Float): Int = (0.5f + dpValue * density).toInt()
fun dip2px(dpValue: Float, context: Context): Int = (dpValue * context.resources.displayMetrics.density + 0.5f).toInt()

fun px2dip(pxValue: Float): Float = pxValue / density
fun px2dip(pxValue: Float, context: Context): Float = pxValue / context.resources.displayMetrics.density

fun sp2Px(spValue: Float): Int = (spValue * scaledDensity + 0.5f).toInt()
fun sp2Px(spValue: Float, context: Context): Int = (spValue * context.resources.displayMetrics.scaledDensity + 0.5f).toInt()
