package com.example.uilib.block2

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import io.reactivex.annotations.Nullable
import java.util.*

@Suppress("unused", "HasPlatformType", "MemberVisibilityCanBePrivate")
open class BlockGroup(@IdRes var mCurrentId: Int = View.NO_ID) : Block() {
    open val mChildren = Collections.synchronizedList<Block>(ArrayList<Block>())
    open var mContainer: ViewGroup? = null

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int): BlockGroup {
        super.setPadding(left, top, right, bottom)
        return this
    }

    override fun setBackgroundColor(@ColorInt backgroundColor: Int): BlockGroup {
        super.setBackgroundColor(backgroundColor)
        return this
    }

    override fun initOtherUI() {
        if (left >= 0 && top >= 0 && right >= 0 && bottom >= 0 && mContainer != null) {
            mContainer!!.setPadding(left, top, right, bottom)
        }
        if (mView != null && backgroundColor != 0) {
            mView!!.setBackgroundColor(backgroundColor)
        }
    }

    open fun getBlock(index: Int): Block {
        return mChildren[index]
    }

    open fun getLeafBlocks(): List<Block> {
        val leafBlocks: MutableList<Block> = ArrayList()
        for (child in mChildren) {
            if (child is BlockGroup) {
                val blockGroup: BlockGroup = child
                leafBlocks.addAll(blockGroup.getLeafBlocks())
            } else {
                leafBlocks.add(child)
            }
        }
        return leafBlocks
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?): View? {
        if (mCurrentId != View.NO_ID && parent != null) {
            mContainer = parent.findViewById<View>(mCurrentId) as ViewGroup
            if (mContainer == null) {
                throw RuntimeException("Split does not has this child:$mCurrentId")
            }
            return mContainer
        }
        return null
    }

    override fun beforeOnViewCreate() {
        if (mContainer == null) {
            mContainer = mView as ViewGroup?
        }
        val it = mChildren.iterator()
        while (it.hasNext()) {
            val block = it.next()
            if (block == null || !block.create(mContainer!!, mContext!!, mWhiteBoard!!, mInflater!!, mBlockManager!!)) {
                it.remove()
            }
        }
    }

    open fun addBlockIf(condition: Boolean, block: Block?): BlockGroup {
        if (condition) {
            addBlock(block)
        }
        return this
    }

    open fun addBlock(block: Block?): BlockGroup {
        if (block == null) {
            return this
        }
        block.mParent = this
        //支持创建后添加
        if (isCreated) {
            mChildren.add(block)
            if (!block.create(mContainer!!, mContext!!, mWhiteBoard!!, mInflater!!, mBlockManager!!)) {
                mChildren.remove(block)
            }
        } else {
            mChildren.add(block)
        }
        return this
    }

    open fun findBlockGroupById(childid: Int): BlockGroup {
        for (block in mChildren) {
            if (block is BlockGroup && block.mCurrentId == childid) {
                return block
            }
        }
        val moduleGroup = BlockGroup(childid)
        addBlock(moduleGroup)
        return moduleGroup
    }

    override fun refreshBlock() {
        super.refreshBlock()
        for (child in getAddedChildren()) {
            child.refreshBlock()
        }
    }

    @CallSuper
    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        for (child in getAddedChildren()) {
            child.onActivityCreated(savedInstanceState)
        }
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        for (child in getAddedChildren()) {
            child.onStart()
        }
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        for (child in getAddedChildren()) {
            child.onResume()
        }
    }

    @CallSuper
    override fun onPause() {
        for (child in getAddedChildren()) {
            child.onPause()
        }
        super.onPause()
    }

    @CallSuper
    override fun onStop() {
        for (child in getAddedChildren()) {
            child.onStop()
        }
        super.onStop()
    }

    @CallSuper
    override fun onDestroy() {
        for (child in getAddedChildren()) {
            child.onDestroy()
        }
        super.onDestroy()
    }

    @CallSuper
    override fun onDestroyView() {
        for (child in getAddedChildren()) {
            child.onDestroyView()
        }
        super.onDestroyView()
    }

    @CallSuper
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        for (child in getAddedChildren()) {
            child.onActivityResult(requestCode, resultCode, data)
        }
    }

    @CallSuper
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        var res = false
        for (child in getAddedChildren()) {
            res = res or child.onKeyDown(keyCode, event)
        }
        return res
    }

    open fun getAddedChildren(): List<Block> {
        val blocks: MutableList<Block> = ArrayList()
        for (mChild in mChildren) {
            if (mChild.isCreated) {
                blocks.add(mChild)
            }
        }
        return blocks
    }

    open fun addView(block: Block) {
        val view: View = block.mView ?: return
        var lastBlock: Block? = null
        for (child in mChildren) {
            if (child === block) {
                break
            }
            if (child.mView != null && child.mView!!.parent === mContainer) {
                lastBlock = child
            }
        }
        if (lastBlock != null) {
            val position = mContainer!!.indexOfChild(lastBlock.mView)
            mContainer!!.addView(view, position + 1)
        } else {
            mContainer!!.addView(view)
        }
    }
}
