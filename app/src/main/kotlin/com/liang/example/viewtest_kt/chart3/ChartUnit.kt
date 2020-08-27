package com.liang.example.viewtest_kt.chart3

import android.graphics.Canvas
import android.graphics.Paint
import com.liang.example.basic_ktx.EnumHelper
import com.liang.example.basic_ktx.MutablePair

/**
 * @author liangyuying.lyy75@bytedance.com
 * @date 2020/8/24
 * <p>
 */
open class ChartUnit {
    open var extras: MutableMap<String, Any>? = null
    open var children: MutableList<ChartUnit>? = null
        set(value) {
            field?.forEach {
                it.parent = null
                it.chartView = null
            }
            field = value
            field?.forEach {
                it.parent = this
                it.chartView = this.chartView
            }
        }
    open var parent: ChartUnit? = null
        set(value) {
            field = value
            children?.forEach {
                it.parent = this
            }
        }
    open var chartView: ChartView? = null
        set(value) {
            field = value
            children?.forEach {
                it.chartView = field
            }
        }
    open var state: Int = State.NORMAL
    open var styles: MutableMap<Int, ChartUnitStyle>? = null
    open var style: ChartUnitStyle?
        get() = styles?.get(state)
        set(value) {
            styles = set(styles, state, value)
        }

    open fun setExtra(key: String, extra: Any?) {
        extras = set(extras, key, extra)
    }

    open fun addChild(child: ChartUnit) {
        children = add(children, child)
    }

    open fun setStatedStyle(state: Int, style: ChartUnitStyle) {
        styles = set(styles, state, style)
    }

    open fun update(parent: ChartUnit?, chartView: ChartView?) {
        this.parent = parent
        this.chartView = chartView
        update()
    }

    open val style2: ChartUnitStyle2 = ChartUnitStyle2()
    open fun update() {
        val style = style ?: return
        val chartView = chartView ?: return
        val parentStyle = parent?.style2
        val rootWidth = chartView.measuredWidth.toFloat() - chartView.paddingLeft - chartView.paddingRight
        val rootHeight = chartView.measuredHeight.toFloat() - chartView.paddingTop - chartView.paddingBottom
        val parentLeft: Float
        val parentRight: Float
        val parentWidth = when (parentStyle) {
            null -> {
                parentLeft = chartView.paddingLeft.toFloat()
                parentRight = rootWidth + parentLeft
                rootWidth
            }
            else -> {
                parentLeft = parentStyle.paddingLeft + parentStyle.left
                parentRight = parentStyle.right - parentStyle.paddingRight
                parentStyle.width - parentStyle.paddingLeft - parentStyle.paddingRight
            }
        }
        val parentTop: Float
        val parentBottom: Float
        val parentHeight = when (parentStyle) {
            null -> {
                parentTop = chartView.paddingTop.toFloat()
                parentBottom = rootWidth + parentTop
                rootHeight
            }
            else -> {
                parentTop = parentStyle.paddingTop + parentStyle.top
                parentBottom = parentStyle.height + parentTop - (parentStyle.bottom ?: 0f) - parentStyle.paddingBottom
                parentStyle.height - parentStyle.paddingTop - parentStyle.paddingBottom
            }
        }

        val width = calc(style.position?.width, 0f, 0f, rootWidth, rootHeight, parentWidth, parentHeight)
        val height = calc(style.position?.height, 0f, 0f, rootWidth, rootHeight, parentWidth, parentHeight)
        style2.width = width
        style2.height = height

        style2.marginLeft = calc(style.margin?.getGL(), width, height, rootWidth, rootHeight, parentWidth, parentHeight)
        style2.marginTop = calc(style.margin?.getGT(), width, height, rootWidth, rootHeight, parentWidth, parentHeight)
        style2.marginRight = calc(style.margin?.getGR(), width, height, rootWidth, rootHeight, parentWidth, parentHeight)
        style2.marginBottom = calc(style.margin?.getGB(), width, height, rootWidth, rootHeight, parentWidth, parentHeight)

        if (style.position?.right != null) {
            style2.right = parentRight - calc(style.position!!.right!!, width, height, rootWidth, rootHeight, parentWidth, parentHeight) - style2.marginRight
            style2.left = style2.right - style2.width
        } else {
            style2.left = calc(style.position?.left, width, height, rootWidth, rootHeight, parentWidth, parentHeight) + style2.marginLeft + parentLeft
            style2.right = style2.left + style2.width
        }
        if (style.position?.bottom != null) {
            style2.bottom = parentBottom - calc(style.position!!.bottom!!, width, height, rootWidth, rootHeight, parentWidth, parentHeight) - style2.marginBottom
            style2.top = style2.bottom - style2.height
        } else {
            style2.top = calc(style.position?.top, width, height, rootWidth, rootHeight, parentWidth, parentHeight)
            style2.bottom = style2.top + style2.height
        }

        style2.paddingLeft = calc(style.padding?.getGL(), width, height, rootWidth, rootHeight, parentWidth, parentHeight)
        style2.paddingTop = calc(style.padding?.getGT(), width, height, rootWidth, rootHeight, parentWidth, parentHeight)
        style2.paddingRight = calc(style.padding?.getGR(), width, height, rootWidth, rootHeight, parentWidth, parentHeight)
        style2.paddingBottom = calc(style.padding?.getGB(), width, height, rootWidth, rootHeight, parentWidth, parentHeight)
        val innerLeft = style2.left + style2.paddingLeft
        val innerRight = style2.right - style2.paddingRight
        val innerTop = style2.top + style2.paddingTop
        val innerBottom = style2.bottom + style2.paddingBottom

        style2.borderWidths = style.borders?.map { calc(it.width, width, height, rootWidth, rootHeight, parentWidth, parentHeight) }

        style2.backgrounds = style.backgrounds?.map {
            MutablePair(innerLeft + calc(it.x, width, height, rootWidth, rootHeight, parentWidth, parentHeight),
                    innerTop + calc(it.y, width, height, rootWidth, rootHeight, parentWidth, parentHeight))
        }

        style2.transforms = style.transforms?.mapNotNull {
            when (it) {
                is ChartTransform.ChartTranslate -> MutablePair(calc(it.x, width, height, rootWidth, rootHeight, parentWidth, parentHeight),
                        calc(it.y, width, height, rootWidth, rootHeight, parentWidth, parentHeight))
                else -> null
            }
        }

        style2.texts = style.texts?.map {
            MutablePair(calc(it.x, width, height, rootWidth, rootHeight, parentWidth, parentHeight),
                    calc(it.y, width, height, rootWidth, rootHeight, parentWidth, parentHeight))
        }
        style2.textTransforms = style.texts?.map {
            it.transforms?.mapNotNull { t ->
                when (t) {
                    is ChartTransform.ChartTranslate -> MutablePair(calc(t.x, width, height, rootWidth, rootHeight, parentWidth, parentHeight),
                            calc(t.y, width, height, rootWidth, rootHeight, parentWidth, parentHeight))
                    else -> null
                }
            }
        }

        val shapes = style.shapes
        if (shapes != null) {
            style2.shapes = shapes.map { it.xys.map { v -> calc(v, width, height, rootWidth, rootHeight, parentWidth, parentHeight) } }
            style2.shapeWidths = shapes.map { calc(it.lineContentStyle?.width, width, height, rootWidth, rootHeight, parentWidth, parentHeight) }
            style2.shapeOps = shapes.map { it.opPathXys?.map { v -> calc(v, width, height, rootWidth, rootHeight, parentWidth, parentHeight) } }
            style2.shapeTransforms = style.shapes?.map {
                it.transforms?.mapNotNull { t ->
                    when (t) {
                        is ChartTransform.ChartTranslate -> MutablePair(calc(t.x, width, height, rootWidth, rootHeight, parentWidth, parentHeight),
                                calc(t.y, width, height, rootWidth, rootHeight, parentWidth, parentHeight))
                        else -> null
                    }
                }
            }
        }

        children?.let {
            it.sortBy { child -> child.style?.zIndex ?: 0 }
            it.forEach { child -> child.update(this, chartView) }
        }
    }

    open fun draw(canvas: Canvas, main: Paint, stroke: Paint) {
        children?.forEach { child -> child.draw(canvas, main, stroke) }
    }

    object State {
        const val STATE = "chartItemState"
        val NONE = EnumHelper[STATE]  // 不可见
        val NORMAL = EnumHelper[STATE]
        val SELECTED = EnumHelper[STATE]
        val UNSELECTED = EnumHelper[STATE]
        val ENABLED = EnumHelper[STATE]
        val DISABLED = EnumHelper[STATE]
    }
}

open class ChartLinearUnit : ChartUnit() {
    open var horizontal: Boolean = false
}

open class ChartRelativeUnit : ChartUnit()
