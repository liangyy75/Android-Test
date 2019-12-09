package com.example.uilib.block

import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.annotation.LayoutRes
import com.example.uilib.R

open class FrameBlockGroup(@LayoutRes override var layoutId: Int = R.layout.layout_frame) : BlockGroup(layoutId) {
    override fun generateLayoutParams(w: Int, h: Int) = FrameLayout.LayoutParams(w, h)
}

open class RelativeBlockGroup(@LayoutRes override var layoutId: Int = R.layout.layout_relative) : BlockGroup(layoutId) {
    override fun generateLayoutParams(w: Int, h: Int) = RelativeLayout.LayoutParams(w, h)
}

open class LinearBlockGroup(@LayoutRes override var layoutId: Int) : BlockGroup(layoutId) {}

open class ScrollBlockGroup(@LayoutRes override var layoutId: Int) : BlockGroup(layoutId) {}

open class DrawerBlockGroup(@LayoutRes override var layoutId: Int) : BlockGroup(layoutId) {}

open class ToolbarBlockGroup(@LayoutRes override var layoutId: Int) : BlockGroup(layoutId) {}

open class CoordinatorBlockGroup(@LayoutRes override var layoutId: Int) : BlockGroup(layoutId) {}

open class GridBlockGroup(@LayoutRes override var layoutId: Int) : BlockGroup(layoutId) {}

open class ListBlockGroup(@LayoutRes override var layoutId: Int) : BlockGroup(layoutId) {}

open class RecyclerBlockGroup(@LayoutRes override var layoutId: Int) : BlockGroup(layoutId) {}

// TODO: blockGroups
