package com.liang.example.blocktest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.uilib.origin.Block
import com.example.uilib.origin.BlockGroup
import com.example.uilib.origin.BlockManager
import com.liang.example.androidtest.R
import com.liang.example.recyclerviewtest.ExampleItem
import java.util.concurrent.Executors

/**
 * test for blocks
 */
class TestBlock(val item: ExampleItem) : Block() {
    override fun onCreateView(inflater: LayoutInflater?, parent: ViewGroup?): View? = inflater?.inflate(layoutIds[item.anInt % 3], parent, false)

    override fun onViewCreated() {
        super.onViewCreated()
        val view = view ?: return
        view.findViewById<TextView>(R.id.test_recyclerview_item_num)?.text = item.anInt.toString()
        view.findViewById<TextView>(R.id.test_recyclerview_item_str).text = item.string
        view.findViewById<TextView>(R.id.test_recyclerview_item_double).text = item.getaDouble().toString()
        view.findViewById<TextView>(R.id.test_recyclerview_item_bool).text = item.isaBoolean().toString()
    }

    companion object {
        private val layoutIds = arrayOf(R.layout.item_recyclerview_list, R.layout.item_recyclerview_list2, R.layout.item_recyclerview_list3)
    }
}

class MainActivity5 : AppCompatActivity() {
    var endTime = 0L
    lateinit var blockManager: BlockManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        blockManager = BlockManager(this).apply {
            singleThreadExecutor = Executors.newSingleThreadExecutor()
            asyncInflateView = MainActivity.ASYNC
            val blockGroup = object : BlockGroup() {
                override fun onCreateView(inflater: LayoutInflater?, parent: ViewGroup?): View? {
                    val view = inflater?.inflate(R.layout.layout_linear, parent, false) as? LinearLayout
                    view?.orientation = LinearLayout.VERTICAL
                    return view
                }
            }
            addBlock(blockGroup)
            val size = MainActivity.SIZE
            (0 until size).forEach { i ->
                blockGroup.addBlock(TestBlock(ExampleItem("string$i", i, i + 0.5, i % 2 == 0)))
            }
        }
        val contentView = blockManager.build(R.layout.layout_scroll)
        setContentView(contentView)
        endTime = System.currentTimeMillis()
        if (MainActivity.MODE) {
            contentView.postDelayed({
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra(MainActivity.TOTAL_TIME_KEY, blockManager.allTime)
                    putExtra(MainActivity.ONCREATE_TOTAL_TIME_KEY, endTime - blockManager.startTime)
                    putExtra(MainActivity.AVERAGE_TIME_KEY, blockManager.averageTime)
                })
                finish()
            }, MainActivity.DELAYED_TIME)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("UseTime", "all time to create block: " + blockManager.allTime + ", and average time: " + blockManager.averageTime
                + ", onCreate time: " + (this.endTime - blockManager.startTime))
    }
}
