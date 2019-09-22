package com.liang.example.utils.view

import android.graphics.Outline
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.RequiresApi

// [Android里把View切换圆角的方法](https://blog.csdn.net/wujiang_android/article/details/90710120)

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun clipViewCircle(view: View) {
    view.clipToOutline = true
    view.outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) =
                outline.setOval(0, 0, view.getWidth(), view.getHeight())
    }
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun clipViewCornerByDp(view: View, pixel: Int) {
    view.clipToOutline = true
    view.outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) =
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), pixel.toFloat())
    }
}
