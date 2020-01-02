package com.liang.example.utils.r

import android.view.animation.*

fun getZoomIn(): AnimationSet {
    val zoomIn = AnimationSet(true)
    zoomIn.interpolator = AccelerateInterpolator()
    zoomIn.duration = android.R.integer.config_mediumAnimTime.toLong()
    zoomIn.addAnimation(ScaleAnimation(2f, 1f, 2f, 1f, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f))
    zoomIn.addAnimation(AlphaAnimation(0f, 1f))
    return zoomIn
}

fun getZoomOut(): AnimationSet {
    val zoomIn = AnimationSet(true)
    zoomIn.interpolator = AccelerateInterpolator()
    zoomIn.duration = android.R.integer.config_mediumAnimTime.toLong()
    zoomIn.addAnimation(ScaleAnimation(1f, .5f, 1f, .5f, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f))
    zoomIn.addAnimation(AlphaAnimation(1f, 0f))
    return zoomIn
}