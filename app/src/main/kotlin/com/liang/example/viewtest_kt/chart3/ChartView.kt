package com.liang.example.viewtest_kt.chart3

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * @author liangyuying.lyy75@bytedance.com
 * @date 2020/8/25
 * <p>
 * todo 描述
 */
open class ChartView : View {
    constructor(c: Context) : super(c)
    constructor(c: Context, attributeSet: AttributeSet?) : super(c, attributeSet)
    constructor(c: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(c, attributeSet, defStyleAttr)

    open val main = Paint()
    open val stroke = Paint()
    open var chartUnit: ChartUnit? = null

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }
}
