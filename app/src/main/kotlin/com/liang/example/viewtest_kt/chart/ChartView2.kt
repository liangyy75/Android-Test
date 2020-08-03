package com.liang.example.viewtest_kt.chart

import com.liang.example.basic_ktx.EnumHelper

/**
 * @author liangyuying.lyy75@bytedance.com
 * @date 2020/8/3
 * <p>
 * todo 描述
 */
class ChartView2

/**
 * 组成Chart的所有组件共同的祖先，定义所有组件共同的事件
 */
open class ChartItem(open var color: Int) {
    open var changed: Boolean = true
    open var clickListener: ClickListener? = null
    open var state: Int = ChartState.NORMAL
    open var aboutItems: MutableMap<Int, ChartItem>? = null
    open var zIndex: Int = 0
    open var position: ChartPosition? = null  // 不要主动set，尽量用pos/size
    open var border: ChartBorder? = null
        set(value) {
            field = value
            changed = true
        }
    open var background: ChartBackground? = null
        set(value) {
            field = value
            changed = true
        }
    open var parentItem: ChartItem? = null
        set(value) {
            field = value
            updatePosOffset()
            childItems?.forEach {
                it.parentItem = this
                it.updatePosOffset()
            }
        }
    open var childItems: MutableList<ChartItem>? = null
        set(value) {
            field = value
            childItems?.forEach {
                it.parentItem = this
                it.updatePosOffset()
            }
        }
    open var chartView: ChartView? = null
        set(value) {
            field = value
            updatePosOffset()
            childItems?.forEach {
                it.chartView = field
                it.updatePosOffset()
            }
        }
    open var rotation: Float = 0f
        set(value) {
            field = value
            changed = true
        }

    open fun pos(x: Float, y: Float, xMode: Int = ChartPosition.Mode.ABS, yMode: Int = ChartPosition.Mode.ABS) {
        if (this.position == null) {
            this.position = ChartPosition()
        }
        val pos = this.position!!
        if (pos.x == x && pos.y == y && pos.xMode == xMode && pos.yMode == yMode) {
            return
        }
        pos.x = x
        pos.y = y
        pos.xMode = xMode
        pos.yMode = yMode
        updatePosOffset(false)
        childItems?.forEach {
            it.updatePosOffset()
        }
    }

    protected open fun updatePosOffset(care: Boolean = true) {
        val pos = this.position ?: return
        val x = pos.x
        val y = pos.y
        val xMode = pos.xMode
        val yMode = pos.yMode
        val posParent = this.parentItem?.position
        val oldXOff = pos.xOffset
        val oldYOff = pos.yOffset
        if (!care || (xMode != ChartPosition.Mode.ABS && xMode != ChartPosition.Mode.ABS_TO_PARENT)) {
            pos.xOffset = when {
                xMode == ChartPosition.Mode.ABS_TO_PARENT && posParent != null && posParent.xOffset != ChartPosition.Invalid.INVALID_XYOffset ->
                    posParent.xOffset + x
                xMode == ChartPosition.Mode.REL_TO_PARENT && posParent != null && posParent.xOffset != ChartPosition.Invalid.INVALID_XYOffset
                        && posParent.width != ChartPosition.Invalid.INVALID_WH -> posParent.xOffset + x * posParent.width
                xMode == ChartPosition.Mode.ABS -> x
                chartView != null -> this.chartView!!.measuredWidth * x
                else -> ChartPosition.Invalid.INVALID_XY
            }
        }
        if (!care || (yMode != ChartPosition.Mode.ABS && yMode != ChartPosition.Mode.ABS_TO_PARENT)) {
            pos.yOffset = when {
                yMode == ChartPosition.Mode.ABS_TO_PARENT && posParent != null && posParent.yOffset != ChartPosition.Invalid.INVALID_XYOffset ->
                    posParent.yOffset + y
                yMode == ChartPosition.Mode.REL_TO_PARENT && posParent != null && posParent.yOffset != ChartPosition.Invalid.INVALID_XYOffset
                        && posParent.height != ChartPosition.Invalid.INVALID_WH -> posParent.yOffset + y * posParent.height
                yMode == ChartPosition.Mode.ABS -> y
                chartView != null -> this.chartView!!.measuredHeight * y
                else -> ChartPosition.Invalid.INVALID_XY
            }
        }
        changed = pos.xOffset != oldXOff || pos.yOffset != oldYOff
    }

    open fun size(width: Float, height: Float, wMode: Int = ChartPosition.Mode.ABS, hMode: Int = ChartPosition.Mode.ABS) {
        if (this.position == null) {
            this.position = ChartPosition()
        }
        val pos = this.position!!
        if (pos.width == width && pos.height == height && pos.wMode == wMode && pos.hMode == hMode) {
            return
        }
        pos.width = width
        pos.height = height
        pos.wMode = wMode
        pos.hMode = hMode
        childItems?.forEach {
            it.updatePosOffset()
        }
        changed = true
    }

    open fun inArea(x: Float, y: Float): Boolean {
        val pos = position ?: return false
        return x >= pos.xOffset && x <= pos.xOffset + pos.width && y >= pos.yOffset && y <= pos.yOffset + pos.height
    }

    open fun addChild(child: ChartItem) {
        child.parentItem = this
        if (this.childItems == null) {
            this.childItems = mutableListOf(child)
        } else if (!this.childItems!!.contains(child)) {
            this.childItems!!.add(child)
        } else {
            return
        }
        changed = true
    }

    open fun removeChild(child: ChartItem) {
        if (this.childItems != null && this.childItems!!.contains(child)) {
            child.parentItem = null
            this.childItems!!.remove(child)
            changed = true
        }
    }

    open fun handleChild(field: ChartItem?, value: ChartItem?) {
        if (field == value) {
            return
        }
        if (value != null) {
            addChild(value)
        }
        if (field != null) {
            removeChild(field)
        }
    }

    interface ClickListener {
        fun onClick(base: ChartItem, x: Float, y: Float)
        fun onLongClick(base: ChartItem, x: Float, y: Float)
    }

    object ChartState {
        const val STATE = "state"
        val NONE = EnumHelper[STATE]  // 也就是平时看不见
        val NORMAL = EnumHelper[STATE]
        val TOUCHING = EnumHelper[STATE]
        val TOUCHED = EnumHelper[STATE]
        val UNTOUCHED = EnumHelper[STATE]
        val ENABLED = EnumHelper[STATE]
        val DISABLED = EnumHelper[STATE]
    }
}
