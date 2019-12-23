package com.liang.example.utils

import android.content.Context

object DensityApi {
    fun dpToPx(context: Context, dp: Float): Int = (dp * context.resources.displayMetrics.density + 0.5f * if (dp >= 0) 1 else -1).toInt()
    fun spToPx(context: Context, spValue: Float): Int = (spValue * context.resources.displayMetrics.scaledDensity + 0.5f).toInt()
}
