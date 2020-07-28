package com.liang.example.viewtest_kt.temp

import android.animation.ObjectAnimator
import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.liang.example.androidtest.R
import com.liang.example.context_ktx.SimpleActivity
import com.liang.example.recyclerviewtest.recycler1.RVAdapterTest
import com.liang.example.recyclerviewtest.recycler1.RVViewHolderTest
import com.liang.example.utils.getScreenWidthPixels
import com.liang.example.utils.r.dp2px
import com.liang.example.view_ktx.layoutHeight
import com.liang.example.viewtest.cornerview.CornerView
import com.liang.example.viewtest.viewpager.PagerAdapterTest

class PagerPagerActivity : SimpleActivity() {
    lateinit var pagerAdapter: PagerAdapterTest<MutableList<MutableList<String>>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagerpager)
        val viewpager = findViewById<ViewPager>(R.id.viewpager)
        val indicatorParent = findViewById<FrameLayout>(R.id.indicator)
        val cover: View = View(this)
        val dp48 = dp2px(48f)
        val dp10 = dp2px(10f)
        val dp72 = dp2px(72f)
        val dp20 = dp10 * 2
        val dataSet = mutableListOf<MutableList<MutableList<String>>>()
        (0..2).forEach { i ->
            dataSet.add((i..i + 4).map { i2 ->
                (i2..i2 + 7).map { i3 -> "$i-$i2-$i3" }.toMutableList()
            }.toMutableList())
        }
        val totalWidth = getScreenWidthPixels(this)
        val totalHeight = viewpager.layoutHeight - dp20
        pagerAdapter = PagerAdapterTest<MutableList<MutableList<String>>>(dataSet, object : PagerAdapterTest.PagerAdapterHolder<MutableList<MutableList<String>>> {
            override fun instantiateItem(container: ViewGroup, position: Int, data: MutableList<MutableList<String>>?): View {
                val linearLayout = LinearLayout(this@PagerPagerActivity)
                linearLayout.orientation = LinearLayout.VERTICAL
                linearLayout.gravity = Gravity.CENTER_HORIZONTAL

                val viewpager2 = ViewPager(this@PagerPagerActivity)
                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, totalHeight)
                lp.setMargins(0, 0, 0, dp10)
                linearLayout.addView(viewpager2, lp)

                val indicatorParent2 = LinearLayout(this@PagerPagerActivity)
                indicatorParent2.orientation = LinearLayout.HORIZONTAL
                indicatorParent2.gravity = Gravity.CENTER_VERTICAL
                linearLayout.addView(indicatorParent2, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp10))

                val pagerAdapter2 = PagerAdapterTest<MutableList<String>>(data, object : PagerAdapterTest.PagerAdapterHolder<MutableList<String>> {
                    override fun instantiateItem(container: ViewGroup, position: Int, data: MutableList<String>?): View {
                        data ?: return TextView(this@PagerPagerActivity).apply {
                            gravity = Gravity.CENTER
                            text = "empty"
                            textSize = 30f
                        }
                        val recyclerView = RecyclerView(this@PagerPagerActivity)
                        recyclerView.adapter = object : RVAdapterTest<String>(data, this@PagerPagerActivity, R.layout.item_recycler_of_pager, recyclerView) {
                            override fun bindView(viewHolder: RVViewHolderTest?, data: String?, position: Int) {
                                viewHolder ?: return
                                (viewHolder.root as TextView).text = data ?: return
                            }
                        }
                        recyclerView.layoutManager = GridLayoutManager(this@PagerPagerActivity, 4, GridLayoutManager.VERTICAL, false)
                        val hSpace = ((totalWidth - 4 * dp72) / 4).shr(1)
                        val vSpace = ((totalHeight - 2 * dp72) / 2).shr(1)
                        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
                            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                                outRect.set(hSpace, vSpace, hSpace, vSpace)
                            }
                        })
                        return recyclerView
                    }

                    private var lastIndex = -1
                    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any, view: View?) {
                        if (lastIndex >= 0) {
                            indicatorParent2.getChildAt(lastIndex)?.setBackgroundColor(resources.getColor(R.color.colorcccccc))
                        }
                        lastIndex = position
                        indicatorParent2.getChildAt(lastIndex)?.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))
                    }
                })
                viewpager2.adapter = pagerAdapter2

                pagerAdapter2.setUseController(true, PagerAdapterTest.IndicatorHolder { index2, _ ->
                    val view = CornerView(this@PagerPagerActivity)
                    view.setCorner(dp10.shl(1).toFloat())
                    view.setBackgroundColor(resources.getColor(R.color.colorcccccc))
                    val lp2 = LinearLayout.LayoutParams(dp10, dp10)
                    lp2.setMargins(dp10, 0, dp10, 0)
                    indicatorParent2.addView(view, index2, lp2)
                    return@IndicatorHolder view
                }, viewpager2)
                return linearLayout
            }

            override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any, view: View?) {
                cover.translationX = position * dp48.toFloat()
            }
        })
        pagerAdapter.setUseController(true, object : PagerAdapterTest.IndicatorHolder<MutableList<MutableList<String>>> {
            private var coverAnim: ObjectAnimator? = null

            init {
                cover.setBackgroundColor(R.color.colorcccccc)
                val lp = LinearLayout.LayoutParams(dp48, dp48)
                indicatorParent.addView(cover, lp)
            }

            override fun getIndicator(index: Int, data: MutableList<MutableList<String>>?): View {
                val textView = TextView(this@PagerPagerActivity)
                textView.text = index.toString()
                textView.textSize = 20f
                textView.gravity = Gravity.CENTER
                val lp = LinearLayout.LayoutParams(dp48, dp48)
                lp.setMargins(dp48 * index, 0, 0, 0)
                indicatorParent.addView(textView, lp)
                return textView
            }

            override fun onIndicatorClick(index: Int, data: MutableList<MutableList<String>>?, view: View?) {
                if (coverAnim != null && coverAnim!!.isRunning) {
                    coverAnim!!.cancel()
                }
                coverAnim = ObjectAnimator.ofFloat(cover, "translationX", cover.translationX, dp48 * index.toFloat())
                coverAnim!!.duration = 500L
                coverAnim!!.start()
            }
        }, viewpager)
        viewpager.adapter = pagerAdapter

        val recyclerView = RecyclerView(this@PagerPagerActivity)
        recyclerView.layoutParams = LinearLayout.LayoutParams(totalWidth, totalHeight)
        recyclerView.adapter = object : RVAdapterTest<String>((0..7).map { "$it" }, this@PagerPagerActivity, R.layout.item_recycler_of_pager, recyclerView) {
            override fun bindView(viewHolder: RVViewHolderTest?, data: String?, position: Int) {
                viewHolder ?: return
                (viewHolder.root as TextView).text = data ?: return
            }
        }
        recyclerView.layoutManager = GridLayoutManager(this@PagerPagerActivity, 4, GridLayoutManager.VERTICAL, false)
        val hSpace = ((totalWidth - 4 * dp72) / 4).shr(1)
        val vSpace = ((totalHeight - 2 * dp72) / 2).shr(1)
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.set(hSpace, vSpace, hSpace, vSpace)
            }
        })
        (viewpager.parent as LinearLayout).addView(recyclerView, LinearLayout.LayoutParams(totalWidth, totalHeight))
    }
}
