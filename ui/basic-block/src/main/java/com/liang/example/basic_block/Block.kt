package com.liang.example.basic_block

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.liang.example.view_ktx.EMPTY_VIEW_STR_ID
import com.liang.example.view_ktx.strId
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("MemberVisibilityCanBePrivate", "UNCHECKED_CAST", "LeakingThis")
open class Block : ActivityProxy {
    protected var viewModelProvider: ViewModelProvider? = null
    protected var swb: DataCenter<String>? = null
    protected var cwb: DataCenter<Class<*>>? = null
    protected var blockHandler: BlockHandler? = null
    open val blockKey: String
        get() = hashCode().toString()

    protected var blockGroup: BlockGroup? = null
    protected var blockManager: BlockManager? = null

    protected var context: Context? = null
    protected var inflater: LayoutInflater? = null
    open var inflateViewAsync: Boolean = false
    open var inflateViewDelay: Long = 0L
    protected var viewCustomTask: Runnable? = null  // 用于设置view，注意这时候parent可能还是null
    protected var afterInflatedTask: Runnable? = null

    protected var layoutId: Int = 0
    open var parent: ViewGroup? = null
    open var view: View? = null
    open val viewId: Int
        get() = view?.id ?: View.NO_ID
    protected var strId: String? = null  // fromXml 和 fromJson 后的关键
    protected var inflated = AtomicBoolean(false)

    constructor(@LayoutRes layoutId: Int, strId: String?) : super() {
        this.layoutId = layoutId
        this.strId = strId
    }

    constructor(view: View, strId: String?) : super() {
        this.view = view
        this.strId = strId
        if (this.strId != null) {
            this.view!!.strId = strId!!
        }
        inflated.compareAndSet(false, true)
        putData(blockKey, STATUS_INFLATED, this)
    }

    // init

    open fun init(context: Context, setProvider: Boolean): Block {
        if (getData(blockKey) != null) {
            return this
        }
        this.context = context
        this.inflater = LayoutInflater.from(context)
        if (setProvider && context is ViewModelStoreOwner) {
            val factory: ViewModelProvider.Factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application)
            val modelStore = context.viewModelStore
            this.viewModelProvider = ViewModelProvider(modelStore, factory)
            this.swb = viewModelStoreGet.invoke(modelStore, MODEL_KEY_SWB) as? DataCenter<String>
            this.cwb = viewModelStoreGet.invoke(modelStore, MODEL_KEY_CWB) as? DataCenter<Class<*>>
            if (this.swb == null) {
                this.swb = DataCenter()
                viewModelStorePut.invoke(modelStore, MODEL_KEY_SWB, this.swb)
            }
            if (this.cwb == null) {
                this.cwb = DataCenter()
                viewModelStorePut.invoke(modelStore, MODEL_KEY_CWB, this.cwb)
            }
        } else {
            this.swb = DataCenter()
            this.cwb = DataCenter()
        }
        putData(blockKey, STATUS_INITIAL, this)
        return this
    }

    open fun init(block: Block): Block {
        if (getData(blockKey) != null) {
            return this
        }
        initInBlock(block)
        putData(blockKey, STATUS_INITIAL, this)
        return this
    }

    open fun init(blockGroup: BlockGroup): Block {
        if (getData(blockKey) != null) {
            return this
        }
        initInBlock(blockGroup)
        this.blockGroup = blockGroup
        putData(blockKey, STATUS_INITIAL, this)
        return this
    }

    open fun init(blockManager: BlockManager): Block {
        if (getData(blockKey) != null) {
            return this
        }
        initInBlock(blockManager)
        this.blockGroup = blockManager
        this.blockManager = blockManager
        putData(blockKey, STATUS_INITIAL, this)
        return this
    }

    protected fun initInBlock(block: Block) {
        this.context = block.context
        this.inflater = block.inflater
        this.inflateViewAsync = block.inflateViewAsync

        this.viewModelProvider = block.viewModelProvider
        this.swb = block.swb
        this.cwb = block.cwb
        this.blockHandler = block.blockHandler

        this.blockGroup = block.blockGroup
        this.blockManager = block.blockManager
    }

    // build

    open fun beforeInflateView() = Unit
    open fun afterInflateView() = Unit
    open fun onInflateView(context: Context, inflater: LayoutInflater, parent: ViewGroup?): View? = inflater.inflate(layoutId, null, false)

    open fun inflate(parent: ViewGroup?): Block {
        if (view != null || layoutId == 0 || getTypedData<Int>(blockKey) != STATUS_INITIAL) {
            return this
        }
        this.parent = parent
        val inflateTask = Runnable {
            beforeInflateView()
            view = onInflateView(this.context!!, this.inflater!!, parent) ?: this.inflater!!.inflate(layoutId, null, false)
            if (this.strId != null) {
                view!!.strId = this.strId!!
            }
            putData(blockKey, STATUS_INFLATED, this)
            inflated.compareAndSet(false, true)
            Log.d("Block", "inflate -- view: ${view!!.javaClass.name}, viewId: $viewId, ${parent?.javaClass?.name}, ${this.javaClass.name}")
            if (view!!.parent == null) {
                val task = when {
                    blockGroup != null -> Runnable {
                        blockGroup!!.addViewOfBlock(this)
                        putData(blockKey, STATUS_ATTACHED, this)
                    }
                    parent != null -> Runnable {
                        parent.addView(view)
                        putData(blockKey, STATUS_ATTACHED, this)
                    }
                    else -> null
                }
                if (task != null) {
                    if (inflateViewAsync) {
                        post(task, 0L, BlockHandler.TYPE_MAIN_THREAD)
                    } else {
                        task.run()
                    }
                }
            } else {
                putData(blockKey, STATUS_ATTACHED, this)
            }
            viewCustomTask?.run()
            viewCustomTask = null
            afterInflatedTask?.run()
            afterInflatedTask = null
            afterInflateView()
        }
        if (inflateViewAsync) {
            post(inflateTask, inflateViewDelay, BlockHandler.TYPE_NEW_THREAD)
        } else {
            inflateTask.run()
        }
        return this
    }

    open fun getInflated(): Boolean = inflated.get()
    open fun getStringId(): String = strId ?: EMPTY_VIEW_STR_ID

    open fun <T : View> setViewCustomTask(task: T.() -> Unit): Block {
        viewCustomTask = Runnable { (this.view as T).task() }
        return this
    }

    open fun setInflatedTask(task: Block.() -> Unit): Block {
        afterInflatedTask = Runnable { this.task() }
        return this
    }

    // refresh

    open fun refresh() = Unit
    open fun refreshGroup(): Unit = blockGroup?.refreshGroup() ?: Unit
    open fun refreshManager(): Unit = blockManager?.refreshManager() ?: Unit

    // recycle / load / unload / copy / release

    open fun recycle() {
        TODO()
    }

    open fun load() {
        TODO()
    }

    open fun unload() {
        TODO()
    }

    open fun copyAndInit(strId: String) = Block(this.layoutId, strId).init(this)

    open fun releaseBlock() {
        this.swb?.release(this)
        this.cwb?.release(this)
        this.blockHandler?.release(this)
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

    // holderByApp

    open fun holderByApp(app: Application) {
        TODO()
    }

    // BlockHandler

    @JvmOverloads
    open fun dealExecutor(arg1: Int, executor: Executor? = null, token: Any? = null): Executor? =
            blockHandler?.dealExecutor(arg1, executor, token)

    @JvmOverloads
    open fun dealCallback(what: Int, callback: Handler.Callback? = null, token: Any? = null): Handler.Callback? =
            blockHandler?.dealCallback(what, callback, token)

    @JvmOverloads
    open fun sendMessage(what: Int, msg: Message, delayMillis: Long = 0L, arg1: Int = BlockHandler.TYPE_IMMEDIATE, token: Any? = null) =
            blockHandler?.sendMessage(what, msg, delayMillis, arg1, token) ?: Unit

    @JvmOverloads
    open fun sendEmptyMessage(what: Int, delayMillis: Long = 0L, arg1: Int = BlockHandler.TYPE_IMMEDIATE, token: Any? = null) =
            blockHandler?.sendEmptyMessage(what, delayMillis, arg1, token) ?: Unit

    @JvmOverloads
    open fun post(r: Runnable, delayMillis: Long = 0L, arg1: Int = BlockHandler.TYPE_IMMEDIATE, token: Any? = null) =
            blockHandler?.post(r, delayMillis, arg1, token) ?: Unit

    open fun releaseBlockHandler(token: Any?) = blockHandler?.release(token) ?: Unit

    // DataCenter<String>

    @JvmOverloads
    open fun putData(key: String, data: Any?, token: Any? = null) = this.swb?.putData(key, data, token) ?: Unit

    @JvmOverloads
    open fun setData(key: String, data: Any?, token: Any? = null) = this.swb?.setData(key, data, token) ?: Unit

    @JvmOverloads
    open fun getData(key: String, default: Any? = null) = this.swb?.getData(key, default)

    @JvmOverloads
    open fun <T> getTypedData(key: String, default: T? = null) = this.swb?.getData(key, default) as? T

    @JvmOverloads
    open fun putIntent(intent: Intent, token: Any? = null) = this.swb?.putIntent(intent, token) ?: Unit

    @JvmOverloads
    open fun putBundle(bundle: Bundle, token: Any? = null) = this.swb?.putBundle(bundle, token) ?: Unit

    @JvmOverloads
    open fun getObservable(key: String, withNow: Boolean = false) = this.swb?.getObservable(key, withNow)

    open fun getLiveData(key: String) = this.swb?.getLiveData(key)

    open fun releaseSwb(token: Any?) = this.swb?.release(token) ?: Unit

    // DataCenter<Class<*>>

    @JvmOverloads
    open fun putData(key: Class<*>, data: Any?, token: Any? = null) = this.cwb?.putData(key, data, token) ?: Unit

    @JvmOverloads
    open fun setData(key: Class<*>, data: Any?, token: Any? = null) = this.cwb?.setData(key, data, token) ?: Unit

    @JvmOverloads
    open fun getData(key: Class<*>, default: Any? = null) = this.cwb?.getData(key, default)

    @JvmOverloads
    open fun <T> getTypedData(key: Class<*>, default: T? = null) = this.cwb?.getData(key, default) as? T

    @JvmOverloads
    open fun putData(data: Any, token: Any? = null) = this.cwb?.putData(data, token) ?: Unit

    @JvmOverloads
    open fun setData(data: Any, token: Any? = null) = this.cwb?.setData(data, token) ?: Unit

    @JvmOverloads
    open fun getObservable(key: Class<*>, withNow: Boolean = false) = this.cwb?.getObservable(key, withNow)

    open fun getLiveData(key: Class<*>) = this.cwb?.getLiveData(key)

    open fun releaseCwb(token: Any?) = this.cwb?.release(token) ?: Unit

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
    override fun onDestroy() = this.releaseBlock()

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
    override fun onDestroy() = this.releaseBlock()

    override fun onSaveInstanceState(bundle: Bundle) = Unit

    @CallSuper
    override fun onRestoreInstanceState(bundle: Bundle) {
        this.bundle = bundle
    }
}

// 1. initInContext / initInBlock / initInGroup / initInManager
// 2. default constructor / constructor(layoutId) / constructor(view)
// 3. TODO: recycle / load / unload / copy
// 4. refresh / refreshGroup / refreshManager
// 5. TODO: parseFromXml(inflate) / parseToXml / parseFromJson / parseToJson
//
// 6. inflate / beforeInflateView / onInflateView / afterInflateView --> 因为有了各种init，所以不需要依靠inflate来设置字段了
// 7. observableData / liveData -- add / remove / get
// 8. consumer / message / runnable / disposable
// 9. TODO: holderByApp
