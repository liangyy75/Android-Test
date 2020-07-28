package com.liang.example.viewtest_kt.page_indicator

import androidx.viewpager.widget.ViewPager

interface PagerIndicator : ViewPager.OnPageChangeListener {
    fun setViewPager(vp: ViewPager)

    fun setViewPager(vp: ViewPager, currentItem: Int)

    fun setCurrentItem(currentItem: Int)

    fun setOnPageChangeListener(listener: ViewPager.OnPageChangeListener?)

    fun notifyDataSetChanged()
}
