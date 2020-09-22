package com.liang.example.blocktest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
// import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import com.liang.example.androidtest.R
import com.liang.example.recyclerviewtest.ExampleItem
import com.liang.example.utils.r.dp2px
import com.liang.example.view_ktx.setPadding

class TestFragment(val item: ExampleItem) : Fragment() {
    var endTime = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(layoutIds[item.anInt % 3], container, true)
        endTime = System.currentTimeMillis()
        view?.apply {
            findViewById<TextView>(R.id.test_recyclerview_item_num)?.text = item.anInt.toString()
            findViewById<TextView>(R.id.test_recyclerview_item_str).text = item.string
            findViewById<TextView>(R.id.test_recyclerview_item_double).text = item.getaDouble().toString()
            findViewById<TextView>(R.id.test_recyclerview_item_bool).text = item.isaBoolean().toString()
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    companion object {
        private val layoutIds = arrayOf(R.layout.item2_recyclerview_list, R.layout.item2_recyclerview_list2, R.layout.item2_recyclerview_list3)
    }
}

class MainActivity7 : AppCompatActivity() {
    private val ids = mutableListOf<Int>()
    private val allFragments = mutableListOf<TestFragment>()
    var startTime = 0L
    var endTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startTime = System.currentTimeMillis()
        val contentView = View.inflate(this, R.layout.layout_scroll, null)
        val containerView = View.inflate(this, R.layout.layout_linear, null)
        val transaction = supportFragmentManager.beginTransaction()
        (containerView as? LinearLayout)?.apply {
            this.orientation = LinearLayout.VERTICAL
            (contentView as? ScrollView)?.addView(this, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT))
            val size = MainActivity.SIZE
            (0 until size).forEach { i ->
                val id = View.generateViewId()
                ids.add(id)
                generateFragmentContainer(this, id, i)
                val fragment = TestFragment(ExampleItem("string$i", i, i + 0.5, i % 2 == 0))
                allFragments.add(fragment)
                transaction.add(id, fragment)
            }
        }
        transaction.commit()
        setContentView(contentView)
        endTime = System.currentTimeMillis()
        if (MainActivity.MODE) {
            contentView.postDelayed({
                setResult(Activity.RESULT_OK, Intent().apply {
                    val endTime2 = allFragments.maxBy { it.endTime }!!.endTime
                    val totalTime = allFragments.sumBy { (it.endTime - startTime).toInt() }.toLong()
                    putExtra(MainActivity.TOTAL_TIME_KEY, endTime2 - startTime)
                    putExtra(MainActivity.ONCREATE_TOTAL_TIME_KEY, endTime - startTime)
                    putExtra(MainActivity.AVERAGE_TIME_KEY, totalTime / MainActivity.SIZE)
                })
                finish()
            }, MainActivity.DELAYED_TIME)
        }
    }

    private fun generateFragmentContainer(containerView: LinearLayout, id: Int, index: Int) {
        (View.inflate(this, R.layout.layout_relative, null) as? RelativeLayout)?.run {
            this.id = id
            this.setPadding(padding)
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, lp_height)
            lp.setMargins(margin, margin, margin, margin)
            containerView.addView(this, lp)
            this.setBackgroundResource(drawables[index % 3])
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val endTime = allFragments.maxBy { it.endTime }!!.endTime
        val totalTime = allFragments.sumBy { (it.endTime - startTime).toInt() }.toLong()
        Log.d("UseTime", "all time to create fragment: " + (endTime - startTime) + ", and average time: " + (totalTime / MainActivity.SIZE)
                + ", onCreate time: " + (this.endTime - startTime))
        allFragments.clear()
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
