package com.liang.example.viewtest_kt.chart

import android.animation.ValueAnimator
import android.os.Bundle
import com.liang.example.androidtest.R
import com.liang.example.context_ktx.SimpleActivity
import kotlinx.android.synthetic.main.activity_test_chart.chart

/**
 * @author liangyuying.lyy75@bytedance.com
 * @date 2020/7/30
 * <p>
 * 测试ChartView
 */
class MainActivity : SimpleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_chart)
    }

    override fun onResume() {
        super.onResume()
        if (chart.testFlag == 0) {
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 5000
                addUpdateListener {
                    val value = it.animatedValue as Float
                    chart.children3?.forEach {
                        it.xRange(0f, -value)
                    }
                    chart.invalidate()
                }
                start()
            }
        }
    }
}
