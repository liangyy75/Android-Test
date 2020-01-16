package com.liang.example.basic_block

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.liang.example.view_ktx.strId
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("MemberVisibilityCanBePrivate", "UNCHECKED_CAST", "LeakingThis")
open class Block : ActivityProxy {
    open var vmp: ViewModelProvider? = null
    open var sdc: DataCenter<String>? = null
    open var cdc: DataCenter<Class<*>>? = null
    open var bh: BlockHandler? = null
    open val blockKey: String
        get() = hashCode().toString()

    open var blockGroup: BlockGroup? = null
    open var blockManager: BlockManager? = null

    open var context: Context? = null
    open var inflater: LayoutInflater? = null
    open var inflateViewAsync: Boolean = false
    open var inflateViewDelay: Long = 0L
    open var viewCustomTask: Runnable? = null  // 用于设置view，注意这时候parent可能还是null
    open var afterInflatedTask: Runnable? = null
    open var refreshTask: Runnable? = null

    open var layoutId: Int = 0
    open var parent: ViewGroup? = null
    open var view: View? = null
    open val viewId: Int
        get() = view?.id ?: View.NO_ID
    open var strId: String? = null  // fromXml 和 fromJson 后的关键
        set(value) {
            field = value
            view?.strId = value
        }
    open var inflated = AtomicBoolean(false)

    constructor(@LayoutRes layoutId: Int, strId: String?) : super() {
        this.layoutId = layoutId
        this.strId = strId
    }

    constructor(view: View, strId: String?) : super() {
        this.view = view
        this.strId = strId
        this.view!!.strId = strId
        inflated.compareAndSet(false, true)
    }

    // init

    open fun init(context: Context, setProvider: Boolean): Block {
        if (this.sdc?.getData(blockKey) != null) {
            return this
        }
        this.context = context
        this.inflater = LayoutInflater.from(context)
        if (setProvider && context is ViewModelStoreOwner) {
            val factory: ViewModelProvider.Factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application)
            val modelStore = context.viewModelStore
            this.vmp = ViewModelProvider(modelStore, factory)
            this.sdc = viewModelStoreGet.invoke(modelStore, MODEL_KEY_SWB) as? DataCenter<String>
            this.cdc = viewModelStoreGet.invoke(modelStore, MODEL_KEY_CWB) as? DataCenter<Class<*>>
            if (this.sdc == null) {
                this.sdc = DataCenter()
                viewModelStorePut.invoke(modelStore, MODEL_KEY_SWB, this.sdc)
            }
            if (this.cdc == null) {
                this.cdc = DataCenter()
                viewModelStorePut.invoke(modelStore, MODEL_KEY_CWB, this.cdc)
            }
        } else {
            this.sdc = DataCenter()
            this.cdc = DataCenter()
        }
        this.sdc?.putData(blockKey, STATUS_INITIAL, this)
        if (this.view != null) {
            this.sdc?.putData(blockKey, STATUS_INFLATED, this)
        }
        return this
    }

    open fun init(block: Block): Block {
        if (this.sdc?.getData(blockKey) != null) {
            return this
        }
        initInBlock(block)
        this.sdc?.putData(blockKey, STATUS_INITIAL, this)
        return this
    }

    open fun init(blockGroup: BlockGroup): Block {
        if (this.sdc?.getData(blockKey) != null) {
            return this
        }
        initInBlock(blockGroup)
        this.blockGroup = blockGroup
        this.sdc?.putData(blockKey, STATUS_INITIAL, this)
        return this
    }

    open fun init(blockManager: BlockManager): Block {
        if (this.sdc?.getData(blockKey) != null) {
            return this
        }
        initInBlock(blockManager)
        this.blockGroup = blockManager
        this.blockManager = blockManager
        this.sdc?.putData(blockKey, STATUS_INITIAL, this)
        return this
    }

    protected fun initInBlock(block: Block) {
        this.context = block.context
        this.inflater = block.inflater
        this.inflateViewAsync = block.inflateViewAsync

        this.vmp = block.vmp
        this.sdc = block.sdc
        this.cdc = block.cdc
        this.bh = block.bh

        this.blockGroup = block.blockGroup
        this.blockManager = block.blockManager
    }

    // build

    open fun beforeInflateView() = Unit
    open fun afterInflateView() = Unit
    open fun onInflateView(context: Context, inflater: LayoutInflater, parent: ViewGroup?): View? = inflater.inflate(layoutId, null, false)

    open fun inflate(parent: ViewGroup?): Block {
        if (view != null || inflated.get()) {
            if (view != null && view!!.parent == null) {
                attach(parent)
            }
            return this
        }
        this.parent = parent
        val inflateTask = Runnable {
            beforeInflateView()
            view = onInflateView(this.context!!, this.inflater!!, parent) ?: this.inflater!!.inflate(layoutId, null, false)
            view!!.strId = this.strId
            this.sdc?.putData(blockKey, STATUS_INFLATED, this)
            inflated.compareAndSet(false, true)
            Log.d("Block", "inflate -- view: ${view!!.javaClass.name}, viewId: $viewId, ${parent?.javaClass?.name}, ${this.javaClass.name}")
            attach(parent)
            viewCustomTask?.run()
            viewCustomTask = null
            afterInflatedTask?.run()
            afterInflatedTask = null
            afterInflateView()
        }
        if (inflateViewAsync) {
            this.bh?.post(inflateTask, inflateViewDelay, BlockHandler.TYPE_NEW_THREAD)
        } else {
            inflateTask.run()
        }
        return this
    }

    open fun <T : View> setViewCustomTask(task: T.() -> Unit): Block {
        viewCustomTask = Runnable { (this.view as T).task() }
        return this
    }

    open fun setInflatedTask(task: Block.() -> Unit): Block {
        afterInflatedTask = Runnable { this.task() }
        return this
    }

    // refresh

    open fun refresh() = refreshTask?.run() ?: Unit
    open fun refreshGroup(): Unit = blockGroup?.refreshGroup() ?: Unit
    open fun refreshManager(): Unit = blockManager?.refreshManager() ?: Unit

    open fun setRefreshTask(task: Block.() -> Unit): Block {
        refreshTask = Runnable { this.task() }
        return this
    }

    // attach / detach / release

    open fun attach(parent: ViewGroup?, index: Int = -1) {
        if (view!!.parent == null) {
            val task = when {
                blockGroup != null -> Runnable { blockGroup!!.attachBlock(this) }
                parent != null -> Runnable {
                    parent.addView(view, index)
                    this.sdc?.putData(blockKey, STATUS_ATTACHED, this)
                }
                else -> null
            }
            if (task != null) {
                if (Looper.getMainLooper().thread != Thread.currentThread()) {
                    this.bh?.post(task, 0L, BlockHandler.TYPE_MAIN_THREAD)
                } else {
                    task.run()
                }
            }
        }
    }

    open fun detach() {
        if (this.parent != null && this.view != null && this.view!!.parent == this.parent) {
            this.parent?.removeView(this.view)
            this.parent = null
            this.sdc?.putData(blockKey, STATUS_DETACHED, this)
        }
    }

    @CallSuper
    open fun release() {
        detach()
        this.sdc?.putData(blockKey, STATUS_DESTROYED, this)
        this.sdc?.release(this)
        this.cdc?.release(this)
        this.bh?.release(this)

        this.vmp = null
        this.sdc = null
        this.cdc = null
        this.bh = null

        this.blockManager = null
        this.blockGroup = null

        this.context = null
        this.inflater = null
        this.inflated.set(false)
        this.view = null
        this.parent = null
    }

    // xml / json

    open fun toJson(): String {
        TODO()
    }

    open fun fromJson(jsonStr: String): Block {
        TODO()
        return this
    }

    open fun toXml(): String {
        TODO()
    }

    open fun fromXml(xmlStr: String): Block {
        TODO()
        return this
    }

    // const

    companion object {
        const val STATUS_INITIAL = 0
        const val STATUS_INFLATED = STATUS_INITIAL + 1
        const val STATUS_ATTACHED = STATUS_INFLATED + 1
        const val STATUS_DETACHED = STATUS_ATTACHED + 1
        const val STATUS_UN_INFLATED = STATUS_DETACHED + 1
        const val STATUS_DESTROYED = STATUS_UN_INFLATED + 1

        const val MODEL_KEY_SWB = "MODEL_KEY_SWB"
        const val MODEL_KEY_CWB = "MODEL_KEY_CWB"
    }
}

open class FragmentBlock : Block, FragmentLifeCycleInter {
    open var bundle: Bundle? = null

    constructor(@LayoutRes layoutId: Int, strId: String?) : super(layoutId, strId)
    constructor(view: View, strId: String?) : super(view, strId)

    // lifecycle

    @CallSuper
    override fun onAttach(context: Context) {
        this.context = context
    }

    @CallSuper
    override fun onCreate(bundle: Bundle?) {
        this.bundle = bundle
    }

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, bundle: Bundle?): View? {
        this.bundle = bundle
        return this.view
    }

    @CallSuper
    override fun onActivityCreated(bundle: Bundle?) {
        this.bundle = bundle
    }

    override fun onStart() = Unit
    override fun onResume() = Unit
    override fun onPause() = Unit
    override fun onStop() = Unit
    override fun onDestroyView() = Unit

    @CallSuper
    override fun onDestroy() = this.release()

    override fun onDetach() = Unit

    override fun onSaveInstanceState(bundle: Bundle) = Unit
}

open class ActivityBlock : Block, ActivityLifeCycleInter {
    open var bundle: Bundle? = null

    constructor(@LayoutRes layoutId: Int, strId: String?) : super(layoutId, strId)
    constructor(view: View, strId: String?) : super(view, strId)

    @CallSuper
    override fun onCreate(bundle: Bundle?) {
        this.bundle = bundle
    }

    override fun onRestart() = Unit
    override fun onStart() = Unit
    override fun onResume() = Unit
    override fun onPause() = Unit
    override fun onStop() = Unit

    @CallSuper
    override fun onDestroy() = this.release()

    override fun onSaveInstanceState(bundle: Bundle) = Unit

    @CallSuper
    override fun onRestoreInstanceState(bundle: Bundle) {
        this.bundle = bundle
    }
}

// 1. initInContext / initInBlock / initInGroup / initInManager
// 2. default constructor / constructor(layoutId) / constructor(view)
// 3. attach / detach / copy / release
// 4. refresh / refreshGroup / refreshManager
// 5. TODO: parseFromXml(inflate) / parseToXml / parseFromJson / parseToJson
//
// 6. inflate / beforeInflateView / onInflateView / afterInflateView --> 因为有了各种init，所以不需要依靠inflate来设置字段了
// 7. observableData / liveData -- add / remove / get
// 8. consumer / message / runnable / disposable
