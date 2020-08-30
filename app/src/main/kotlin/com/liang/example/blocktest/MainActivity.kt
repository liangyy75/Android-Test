package com.liang.example.blocktest

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.liang.example.androidtest.ApplicationTest
import com.liang.example.androidtest.R
import com.liang.example.utils.view.showToast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        com.liang.example.androidtest.MainActivity.bindActivityList(names, descs, authors, created, updated, classes, this, "View_Block_Widget_Fragment")

        val textView = (application as ApplicationTest).textView
        (findViewById<View>(R.id.test_activity_root) as LinearLayout).addView(textView, 0)
        textView.setOnClickListener {
            MODE = true
            testBlockAndWidgetAndFragment(textView)
        }
    }

    private var flag = 0
    private var startCount = 0
    private val blockTotalTime = mutableListOf<Long>()
    private val blockAverageTime = mutableListOf<Long>()
    private val blockOnCreateTotalTime = mutableListOf<Long>()
    private val widgetTotalTime = mutableListOf<Long>()
    private val widgetAverageTime = mutableListOf<Long>()
    private val widgetOnCreateTotalTime = mutableListOf<Long>()
    private val fragmentTotalTime = mutableListOf<Long>()
    private val fragmentAverageTime = mutableListOf<Long>()
    private val fragmentOnCreateTotalTime = mutableListOf<Long>()

    @SuppressLint("SetTextI18n")
    private fun testBlockAndWidgetAndFragment(textView: TextView) {
        if (startCount >= 5) {
            textView.text = "block's total: ${blockTotalTime.average()}, block's average: ${blockAverageTime.average()}, block's onCreate: ${blockOnCreateTotalTime.average()}; " +
                    "widget's total: ${widgetTotalTime.average()}, widget's average: ${widgetAverageTime.average()}, widget's onCreate: ${widgetOnCreateTotalTime.average()}; " +
                    "fragment's total: ${fragmentTotalTime.average()}, fragment's average: ${fragmentAverageTime.average()}, fragment's onCreate: ${fragmentOnCreateTotalTime.average()}; "
            return
        }
        textView.postDelayed({
            if (flag == 0) {
                startActivityForResult(Intent(this, MainActivity5::class.java), REQUEST_CODE)
            } else if (flag == 1) {
                startActivityForResult(Intent(this, MainActivity6::class.java), REQUEST_CODE)
            } else {
                startActivityForResult(Intent(this, MainActivity7::class.java), REQUEST_CODE)
                startCount++
            }
        }, DELAYED_TIME)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val totalTime = data.getLongExtra(TOTAL_TIME_KEY, 0L)
            val averageTime = data.getLongExtra(AVERAGE_TIME_KEY, 0L)
            val onCreateTotalTime = data.getLongExtra(ONCREATE_TOTAL_TIME_KEY, 0L)
            val msg = if (flag == 0) {
                blockTotalTime.add(totalTime)
                blockAverageTime.add(averageTime)
                blockOnCreateTotalTime.add(onCreateTotalTime)
                "block"
            } else if (flag == 1) {
                widgetTotalTime.add(totalTime)
                widgetAverageTime.add(averageTime)
                widgetOnCreateTotalTime.add(onCreateTotalTime)
                "widget"
            } else {
                fragmentTotalTime.add(totalTime)
                fragmentAverageTime.add(averageTime)
                fragmentOnCreateTotalTime.add(onCreateTotalTime)
                "fragment"
            }
            showToast("total time of $msg: $totalTime, average time of $msg: $averageTime, onCreate time of $msg: $onCreateTotalTime")
            flag = (flag + 1) % 3
            testBlockAndWidgetAndFragment((application as ApplicationTest).textView)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onDestroy() {
        super.onDestroy()
        (application as ApplicationTest).textView.apply {
            setOnClickListener(null)
            text = "app text"
        }
    }

    companion object {
        var MODE = false
        const val SIZE = 300
        const val ASYNC = true
        const val DELAYED_TIME = 3000L
        const val REQUEST_CODE = 33333
        const val TOTAL_TIME_KEY = "TOTAL_TIME_KEY"
        const val ONCREATE_TOTAL_TIME_KEY = "SYNC_TOTAL_TIME_KEY"
        const val AVERAGE_TIME_KEY = "AVERAGE_TIME_KEY"
    }
}
