package com.liang.example.blocktest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setMargins
import com.example.uilib.origin.Widget
import com.example.uilib.origin.WidgetManager
import com.liang.example.androidtest.R
import com.liang.example.recyclerviewtest.ExampleItem
import com.liang.example.utils.r.dp2px
import com.liang.example.view_ktx.setPadding

class TestWidget(val item: ExampleItem) : Widget() {
    override val layoutId: Int
        get() = layoutIds[item.anInt % 3]

    override fun onCreate() {
        super.onCreate()
        val view = containerView ?: return
        view.findViewById<TextView>(R.id.test_recyclerview_item_num)?.text = item.anInt.toString()
        view.findViewById<TextView>(R.id.test_recyclerview_item_str).text = item.string
        view.findViewById<TextView>(R.id.test_recyclerview_item_double).text = item.getaDouble().toString()
        view.findViewById<TextView>(R.id.test_recyclerview_item_bool).text = item.isaBoolean().toString()
    }

    companion object {
        private val layoutIds = arrayOf(R.layout.item2_recyclerview_list, R.layout.item2_recyclerview_list2, R.layout.item2_recyclerview_list3)
    }
}

class MainActivity6 : AppCompatActivity() {
    private val ids = mutableListOf<Int>()
    lateinit var widgetManager: WidgetManager
    var endTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        widgetManager = WidgetManager.of(this@MainActivity6, null)
        val contentView = View.inflate(this, R.layout.layout_scroll, null)
        widgetManager.contentView = contentView
        val containerView = View.inflate(this, R.layout.layout_linear, null)
        (containerView as? LinearLayout)?.apply {
            this.orientation = LinearLayout.VERTICAL
            (contentView as? ScrollView)?.addView(this, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT))
            val size = MainActivity.SIZE
            (0 until size).forEach { i ->
                val id = View.generateViewId()
                ids.add(id)
                generateWidgetContainer(this, id, i)
                widgetManager.load(id, TestWidget(ExampleItem("string$i", i, i + 0.5, i % 2 == 0)), MainActivity.ASYNC, true)
            }
        }
        setContentView(contentView)
        endTime = System.currentTimeMillis()
        if (MainActivity.MODE) {
            contentView.postDelayed({
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra(MainActivity.TOTAL_TIME_KEY, widgetManager.allTime)
                    putExtra(MainActivity.ONCREATE_TOTAL_TIME_KEY, endTime - widgetManager.startTime)
                    putExtra(MainActivity.AVERAGE_TIME_KEY, widgetManager.averageTime)
                })
                finish()
            }, MainActivity.DELAYED_TIME)
        }
    }

    private fun generateWidgetContainer(containerView: LinearLayout, id: Int, index: Int) {
        (View.inflate(this, R.layout.layout_relative, null) as? RelativeLayout)?.run {
            this.id = id
            this.setPadding(padding)
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, lp_height)
            lp.setMargins(margin)
            containerView.addView(this, lp)
            this.setBackgroundResource(drawables[index % 3])
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("UseTime", "all time to create widget: " + widgetManager.allTime + ", and average time: " + widgetManager.averageTime
                + ", onCreate time: " + (this.endTime - widgetManager.startTime))
    }

    companion object {
        private val drawables = arrayOf(
                R.drawable.bg_item_recyclerview_list,
                R.drawable.bg_item_recyclerview_list2,
                R.drawable.bg_item_recyclerview_list3
        )

        private val margin = dp2px(10f)
        private val padding = dp2px(10f)
        private val lp_height = dp2px(100f)
    }
}
