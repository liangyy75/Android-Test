package com.liang.example.basic_block

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import java.util.*
import kotlin.collections.ArrayList

open class BlockGroup : Block {
    open val children: MutableList<Block> = Collections.synchronizedList(ArrayList())
    open val viewGroup: ViewGroup?
        get() = view as? ViewGroup
    private val syncObject = Any()

    // constructor

    constructor(@LayoutRes layoutId: Int, strId: String?) : super(layoutId, strId)
    constructor(viewGroup: ViewGroup, strId: String?) : super(viewGroup, strId)

    // change return

    override fun init(context: Context, setProvider: Boolean): BlockGroup = super.init(context, setProvider) as BlockGroup
    override fun init(block: Block): BlockGroup = super.init(block) as BlockGroup
    override fun init(blockGroup: BlockGroup): BlockGroup = super.init(blockGroup) as BlockGroup
    override fun init(blockManager: BlockManager): BlockGroup = super.init(blockManager) as BlockGroup

    // build

    override fun inflate(parent: ViewGroup?): BlockGroup = super.inflate(parent) as BlockGroup

    @CallSuper
    override fun afterInflateView() {
        val r = Runnable { children.filter { it.inflated.get() }.forEach { attachBlock(it) } }
        if (Looper.getMainLooper().thread == Thread.currentThread()) {
            this.bh?.post(r, 0L, BlockHandler.TYPE_MAIN_THREAD)
        } else {
            r.run()
        }
    }

    override fun <T : View> setViewCustomTask(task: T.() -> Unit): BlockGroup = super.setViewCustomTask(task) as BlockGroup
    override fun setInflatedTask(task: Block.() -> Unit): BlockGroup = super.setInflatedTask(task) as BlockGroup

    // refresh / release

    override fun refresh() = refreshGroup()
    override fun refreshGroup() {
        refreshTask?.run()
        children.forEach { it.refresh() }
    }

    override fun setRefreshTask(task: Block.() -> Unit): BlockGroup = super.setRefreshTask(task) as BlockGroup

    override fun release() {
        children.forEach { it.release() }
        children.clear()
        super.release()
    }

    // activity's on

    override fun onNewIntent(intent: Intent) = children.forEach { it.onNewIntent(intent) }
    override fun onBackPressed() = children.forEach { it.onBackPressed() }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
            children.forEach { it.onActivityResult(requestCode, resultCode, data) }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) =
            children.forEach { it.onRequestPermissionsResult(requestCode, permissions, grantResults) }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        children.forEach { if (it.onKeyDown(keyCode, event)) return true }
        return false
    }

    // add

    open fun innerAddBlock(block: Block, index: Int = -1): BlockGroup {
        if (block in children) {
            return this
        }
        if (index == -1) {
            children.add(block)
        } else {
            children.add(index, block)
        }
        if (block.inflated.get() || block.parent != null) {
            block.release()
        }
        block.init(this)
        block.inflateViewAsync = inflateViewAsync
        block.inflate(viewGroup)
        return this
    }

    open fun addBlock(block: Block) = innerAddBlock(block)

    open fun insertBlock(block: Block, index: Int) = innerAddBlock(block, index)

    open fun attachBlock(block: Block): BlockGroup = synchronized(syncObject) {
        if (viewGroup == null) {
            return this
        }
        var lastBlock: Block? = null
        for (child in children) {
            if (block == child) {
                break
            }
            if (child.inflated.get()) {
                lastBlock = child
            }
        }
        if (lastBlock == null) {
            viewGroup!!.addView(block.view)
        } else {
            viewGroup!!.addView(block.view, viewGroup!!.indexOfChild(lastBlock.view) + 1)
        }
        block.parent = viewGroup!!
        this.sdc?.putData(block.blockKey, STATUS_ATTACHED, block)
        return this
    }

    // remove

    open fun removeBlock(block: Block): BlockGroup = synchronized(syncObject) {
        children.remove(block)
        if (block.inflated.get()) {
            viewGroup?.removeView(block.view)
        }
        block.release()
        return this
    }

    open fun removeBlock(index: Int): BlockGroup = synchronized(syncObject) {
        val block = children.removeAt(index)
        if (block.inflated.get()) {
            viewGroup?.removeView(block.view)
        }
        block.release()
        return this
    }

    // replace

    open fun replaceBlock(newBlock: Block, oldBlock: Block): BlockGroup = synchronized(syncObject) {
        if (oldBlock !in children) {
            return this
        }
        val index = children.indexOf(oldBlock)
        children.remove(oldBlock)
        children.add(index, newBlock)
        if (oldBlock.inflated.get()) {
            viewGroup?.removeView(oldBlock.view)
        }
        if (newBlock.inflated.get()) {
            newBlock.release()
        }
        newBlock.init(this)
        newBlock.inflate(viewGroup)
        return this
    }

    open fun replaceBlock(newBlock: Block, index: Int): BlockGroup = synchronized(syncObject) {
        val oldBlock = children.removeAt(index)
        children.add(index, newBlock)
        if (oldBlock.inflated.get()) {
            viewGroup?.removeView(oldBlock.view)
        }
        if (newBlock.inflated.get()) {
            newBlock.release()
        }
        newBlock.init(this)
        newBlock.inflate(viewGroup)
        return this
    }

    // find

    open fun getBlock(index: Int) = children[index]
    open fun getBlocks() = children
    open fun getLeafBlocks(): List<Block> {
        val leafBlocks: MutableList<Block> = ArrayList()
        for (child in children) {
            if (child is BlockGroup) {
                leafBlocks.addAll(child.getLeafBlocks())
            } else {
                leafBlocks.add(child)
            }
        }
        return leafBlocks
    }

    open fun findBlockById(id: Int) = children.find { it.viewId == id }
    open fun findBlockByTag(tag: Any) = children.find { it.view?.tag ?: 0 == tag }
    open fun findBlockByStrId(strId: String?) = children.find { it.strId == strId }

    // xml / json

    override fun toJson(): String {
        TODO()
    }

    override fun fromJson(jsonStr: String): BlockGroup {
        TODO()
        return this
    }

    override fun toXml(): String {
        TODO()
    }

    override fun fromXml(xmlStr: String): BlockGroup {
        TODO()
        return this
    }
}

open class FragmentBlockGroup : BlockGroup, FragmentLifeCycleInter {
    open var bundle: Bundle? = null

    constructor(@LayoutRes layoutId: Int, strId: String?) : super(layoutId, strId)
    constructor(viewGroup: ViewGroup, strId: String?) : super(viewGroup, strId)

    @CallSuper
    override fun onAttach(context: Context) {
        this.context = context
        this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onAttach(context) }
    }

    @CallSuper
    override fun onCreate(bundle: Bundle?) {
        this.bundle = bundle
        this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onCreate(bundle) }
    }

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, bundle: Bundle?): View? {
        this.bundle = bundle
        this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onCreateView(inflater, parent, bundle) }
        return this.view
    }

    @CallSuper
    override fun onActivityCreated(bundle: Bundle?) =
            this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onActivityCreated(bundle) }

    @CallSuper
    override fun onStart() = this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onStart() }

    @CallSuper
    override fun onResume() = this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onResume() }

    @CallSuper
    override fun onPause() = this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onPause() }

    @CallSuper
    override fun onStop() = this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onStop() }

    @CallSuper
    override fun onDestroyView() = this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onDestroyView() }

    @CallSuper
    override fun onDestroy() = this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onDestroy() }

    @CallSuper
    override fun onDetach() = this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onDetach() }

    @CallSuper
    override fun onSaveInstanceState(bundle: Bundle) =
            this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onSaveInstanceState(bundle) }
}

open class ActivityBlockGroup : BlockGroup, ActivityLifeCycleInter {
    open var bundle: Bundle? = null

    constructor(@LayoutRes layoutId: Int, strId: String?) : super(layoutId, strId)
    constructor(viewGroup: ViewGroup, strId: String?) : super(viewGroup, strId)

    @CallSuper
    override fun onCreate(bundle: Bundle?) {
        this.bundle = bundle
        this.children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onCreate(bundle) }
    }

    @CallSuper
    override fun onRestart() = this.children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onRestart() }

    @CallSuper
    override fun onStart() = this.children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onStart() }

    @CallSuper
    override fun onResume() = this.children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onResume() }

    @CallSuper
    override fun onPause() = this.children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onPause() }

    @CallSuper
    override fun onStop() = this.children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onStop() }

    @CallSuper
    override fun onDestroy() = this.children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onDestroy() }

    @CallSuper
    override fun onSaveInstanceState(bundle: Bundle) =
            this.children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onSaveInstanceState(bundle) }

    @CallSuper
    override fun onRestoreInstanceState(bundle: Bundle) {
        this.bundle = bundle
        this.children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onRestoreInstanceState(bundle) }
    }
}

fun BlockGroup.addBlockIf(block: Block, condition: Boolean) = if (condition) addBlock(block) else this
fun BlockGroup.addBlockIf(block: Block, condition: () -> Boolean) = if (condition()) addBlock(block) else this

fun BlockGroup.insertBlockIf(block: Block, index: Int, condition: Boolean) = if (condition) innerAddBlock(block, index) else this
fun BlockGroup.insertBlockIf(block: Block, index: Int, condition: () -> Boolean) = if (condition()) innerAddBlock(block, index) else this

fun BlockGroup.removeBlockIf(block: Block, condition: Boolean) = if (condition) removeBlock(block) else this
fun BlockGroup.removeBlockIf(block: Block, condition: () -> Boolean) = if (condition()) removeBlock(block) else this

fun BlockGroup.removeBlockIf(index: Int, condition: Boolean) = if (condition) removeBlock(index) else this
fun BlockGroup.removeBlockIf(index: Int, condition: () -> Boolean) = if (condition()) removeBlock(index) else this

fun BlockGroup.replaceBlockIf(newBlock: Block, oldBlock: Block, condition: Boolean) = if (condition) replaceBlock(newBlock, oldBlock) else this
fun BlockGroup.replaceBlockIf(newBlock: Block, oldBlock: Block, condition: () -> Boolean) = if (condition()) replaceBlock(newBlock, oldBlock) else this

fun BlockGroup.replaceBlockIf(newBlock: Block, index: Int, condition: Boolean) = if (condition) replaceBlock(newBlock, index) else this
fun BlockGroup.replaceBlockIf(newBlock: Block, index: Int, condition: () -> Boolean) = if (condition()) replaceBlock(newBlock, index) else this

// 1. initInContext / initInBlock / initInGroup / initInManager
// 2. default constructor / constructor(layoutId) / constructor(view)
// 3. attach / detach / copy / release
// 4. refresh / refreshGroup / refreshManager
// 5. TODO: parseFromXml / parseToXml / parseFromJson / parseToJson
//
// 6. TODO: view 的增、删、改、查
// 7. block 的增、删、改、查
// 8. afterInflateView
// 9. TODO: viewBuilder / blockBuilder
