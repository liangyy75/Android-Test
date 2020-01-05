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
    open val addBlockTasks: MutableList<Runnable> = Collections.synchronizedList(ArrayList())
    open val viewGroup: ViewGroup?
        get() = view as? ViewGroup
    private val syncObject = Any()

    // constructor

    constructor(@LayoutRes layoutId: Int, strId: String?) : super(layoutId, strId)
    constructor(viewGroup: ViewGroup, strId: String?) : super(viewGroup, strId)

    // init

    override fun init(context: Context, setProvider: Boolean): BlockGroup = super.init(context, setProvider) as BlockGroup
    override fun init(block: Block): BlockGroup = super.init(block) as BlockGroup
    override fun init(blockGroup: BlockGroup): BlockGroup = super.init(blockGroup) as BlockGroup
    override fun init(blockManager: BlockManager): BlockGroup = super.init(blockManager) as BlockGroup

    // build

    @CallSuper
    override fun afterInflateView() {
        val r = Runnable {
            children.filter { it.getInflated() }.forEach { addViewOfBlock(it) }
            addBlockTasks.forEach { it.run() }
            addBlockTasks.clear()
        }
        if (Looper.getMainLooper().thread == Thread.currentThread()) {
            post(r, 0L, BlockHandler.TYPE_MAIN_THREAD)
        } else {
            r.run()
        }
    }

    override fun <T : View> setViewCustomTask(task: T.() -> Unit): BlockGroup = super.setViewCustomTask(task) as BlockGroup
    override fun setInflatedTask(task: Block.() -> Unit): BlockGroup = super.setInflatedTask(task) as BlockGroup

    // refresh

    override fun refresh() = refreshGroup()
    override fun refreshGroup(): Unit = children.forEach { it.refresh() }

    // recycle

    override fun recycle() {
        TODO()
    }

    override fun load() {
        TODO()
    }

    override fun unload() {
        TODO()
    }

    override fun copyAndInit(strId: String): BlockGroup {
        TODO()
        return this
    }

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

    private fun innerAddBlock(block: Block, index: Int = -1): BlockGroup {
        if (block in children) {
            return this
        }
        if (index == -1) {
            children.add(block)
        } else {
            children.add(index, block)
        }
        if (block.getInflated()) {
            block.recycle()
        }
        block.init(this)
        block.inflateViewAsync = inflateViewAsync
        block.inflate(viewGroup)
        return this
    }

    private fun checkTask(runnable: Runnable): BlockGroup {
        if (inflated.get()) {
            runnable.run()
        } else {
            addBlockTasks.add(runnable)
        }
        return this
    }

    open fun addBlock(block: Block) = innerAddBlock(block)
    open fun addBlockIf(condition: Boolean, block: Block) = if (condition) addBlock(block) else this
    open fun addBlockIf(condition: () -> Boolean, block: Block) = if (condition()) addBlock(block) else this

    open fun addBlockLater(block: Block) = checkTask(Runnable { innerAddBlock(block) })
    open fun addBlockLaterIf(condition: Boolean, block: Block) = if (condition) checkTask(Runnable { innerAddBlock(block) }) else this
    open fun addBlockLaterIf(condition: () -> Boolean, block: Block) =
            if (condition()) checkTask(Runnable { innerAddBlock(block) }) else this

    open fun insertBlock(block: Block, index: Int) = innerAddBlock(block, index)
    open fun insertBlockIf(condition: Boolean, block: Block, index: Int) = if (condition) innerAddBlock(block, index) else this
    open fun insertBlockIf(condition: () -> Boolean, block: Block, index: Int) = if (condition()) innerAddBlock(block, index) else this

    open fun insertBlockLater(block: Block, index: Int) = checkTask(Runnable { innerAddBlock(block, index) })
    open fun insertBlockLaterIf(condition: Boolean, block: Block, index: Int) =
            if (condition) checkTask(Runnable { innerAddBlock(block, index) }) else this

    open fun insertBlockLaterIf(condition: () -> Boolean, block: Block, index: Int) =
            if (condition()) checkTask(Runnable { innerAddBlock(block, index) }) else this

    open fun addViewOfBlock(block: Block): BlockGroup = synchronized(syncObject) {
        if (viewGroup == null) {
            return this
        }
        var lastBlock: Block? = null
        for (child in children) {
            if (block == child) {
                break
            }
            if (child.getInflated()) {
                lastBlock = child
            }
        }
        if (lastBlock == null) {
            viewGroup!!.addView(block.view)
        } else {
            viewGroup!!.addView(block.view, viewGroup!!.indexOfChild(lastBlock.view) + 1)
        }
        block.parent = viewGroup!!
        return this
    }

    // remove

    open fun removeBlock(block: Block): BlockGroup = synchronized(syncObject) {
        children.remove(block)
        if (block.getInflated()) {
            viewGroup?.removeView(block.view)
        }
        return this
    }

    open fun removeBlock(index: Int): BlockGroup = synchronized(syncObject) {
        val block = children.removeAt(index)
        if (block.getInflated()) {
            viewGroup?.removeView(block.view)
        }
        return this
    }

    open fun removeBlockIf(condition: Boolean, block: Block) = if (condition) removeBlock(block) else this
    open fun removeBlockIf(condition: () -> Boolean, block: Block) = if (condition()) removeBlock(block) else this

    open fun removeBlockIf(condition: Boolean, index: Int) = if (condition) removeBlock(index) else this
    open fun removeBlockIf(condition: () -> Boolean, index: Int) = if (condition()) removeBlock(index) else this

    // replace

    open fun replaceBlock(newBlock: Block, oldBlock: Block): BlockGroup = synchronized(syncObject) {
        if (oldBlock !in children) {
            return this
        }
        val index = children.indexOf(oldBlock)
        children.remove(oldBlock)
        children.add(index, newBlock)
        if (oldBlock.getInflated()) {
            viewGroup?.removeView(oldBlock.view)
        }
        if (newBlock.getInflated()) {
            newBlock.recycle()
        }
        newBlock.init(this)
        newBlock.inflate(viewGroup)
        return this
    }

    open fun replaceBlock(newBlock: Block, index: Int): BlockGroup = synchronized(syncObject) {
        val oldBlock = children.removeAt(index)
        children.add(index, newBlock)
        if (oldBlock.getInflated()) {
            viewGroup?.removeView(oldBlock.view)
        }
        if (newBlock.getInflated()) {
            newBlock.recycle()
        }
        newBlock.init(this)
        newBlock.inflate(viewGroup)
        return this
    }

    open fun replaceBlockIf(condition: Boolean, newBlock: Block, oldBlock: Block) = if (condition) replaceBlock(newBlock, oldBlock) else this
    open fun replaceBlockIf(condition: () -> Boolean, newBlock: Block, oldBlock: Block) = if (condition()) replaceBlock(newBlock, oldBlock) else this

    open fun replaceBlockIf(condition: Boolean, newBlock: Block, index: Int) = if (condition) replaceBlock(newBlock, index) else this
    open fun replaceBlockIf(condition: () -> Boolean, newBlock: Block, index: Int) = if (condition()) replaceBlock(newBlock, index) else this

    // find

    open fun getBlock(index: Int) = children[index]
    open fun getInflatedBlock(index: Int) = if (children[index].getInflated()) children[index] else null

    open fun getBlocks() = children
    open fun getInflatedBlocks() = children.filter { it.getInflated() }
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
    open fun findBlockByStrId(strId: String) = children.find { it.getStringId() == strId }
}

open class FragmentBlockGroup : BlockGroup, FragmentLifeCycleInter {
    open var bundle: Bundle? = null

    constructor(@LayoutRes layoutId: Int, strId: String?) : super(layoutId, strId)
    constructor(viewGroup: ViewGroup, strId: String?) : super(viewGroup, strId)

    override fun onAttach(context: Context) {
        this.context = context
        this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onAttach(context) }
    }

    override fun onCreate(bundle: Bundle?) {
        this.bundle = bundle
        this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onCreate(bundle) }
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, bundle: Bundle?): View? {
        this.bundle = bundle
        this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onCreateView(inflater, parent, bundle) }
        return this.view
    }

    override fun onActivityCreated(bundle: Bundle?) =
            this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onActivityCreated(bundle) }

    override fun onStart() = this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onStart() }
    override fun onResume() = this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onResume() }
    override fun onPause() = this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onPause() }
    override fun onStop() = this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onStop() }
    override fun onDestroyView() = this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onDestroyView() }
    override fun onDestroy() = this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onDestroy() }
    override fun onDetach() = this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onDetach() }
    override fun onSaveInstanceState(bundle: Bundle) =
            this.children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onSaveInstanceState(bundle) }
}

open class ActivityBlockGroup : BlockGroup, ActivityLifeCycleInter {
    open var bundle: Bundle? = null

    constructor(@LayoutRes layoutId: Int, strId: String?) : super(layoutId, strId)
    constructor(viewGroup: ViewGroup, strId: String?) : super(viewGroup, strId)

    override fun onCreate(bundle: Bundle?) {
        this.bundle = bundle
        this.children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onCreate(bundle) }
    }

    override fun onRestart() = this.children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onRestart() }
    override fun onStart() = this.children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onStart() }
    override fun onResume() = this.children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onResume() }
    override fun onPause() = this.children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onPause() }
    override fun onStop() = this.children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onStop() }
    override fun onDestroy() = this.children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onDestroy() }
    override fun onSaveInstanceState(bundle: Bundle) =
            this.children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onSaveInstanceState(bundle) }

    override fun onRestoreInstanceState(bundle: Bundle) {
        this.bundle = bundle
        this.children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onRestoreInstanceState(bundle) }
    }
}

// 1. initInContext / initInBlock / initInGroup / initInManager
// 2. default constructor / constructor(layoutId) / constructor(view)
// 3. TODO: recycle / load / unload / copy
// 4. refresh / refreshGroup / refreshManager
// 5. TODO: parseFromXml / parseToXml / parseFromJson / parseToJson
//
// 6. TODO: view 的增、删、改、查
// 7. block 的增、删、改、查
// 8. afterInflateView
// 9. TODO: viewBuilder / blockBuilder
