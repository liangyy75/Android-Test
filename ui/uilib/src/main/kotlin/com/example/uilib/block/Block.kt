@file: Suppress("UNCHECKED_CAST")

package com.example.uilib.block

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.atomic.AtomicBoolean

// TODO: CallSuper
open class Block : ActivityProxy() {
    companion object {
        const val KEY_INFLATED = "key_inflated"
    }

    open lateinit var swb: WhiteBoard<String>
    open lateinit var cwb: WhiteBoard<Class<*>>
    open lateinit var rxHandler: RxHandler

    open var blockGroup: BlockGroup? = null
    open var blockManager: BlockManager? = null
    override var ai: ActivityInter
        get() = blockManager!!
        set(_) {}

    // init

    open fun init(swb: WhiteBoard<String>, cwb: WhiteBoard<Class<*>>, h: RxHandler, bg: BlockGroup? = null, bm: BlockManager? = null) {
        this.swb = swb
        this.cwb = cwb
        this.rxHandler = h
        this.blockGroup = bg
        if (bg != null) {
            this.parent = bg.viewGroup
        }
        this.blockManager = blockManager
        this.swb.putData(KEY_INFLATED, false)
    }

    // inflate

    open lateinit var context: Context
    open lateinit var inflater: LayoutInflater
    open var parent: ViewGroup? = null
    open var view: View? = null
    open val viewId: Int
        get() = if (inflated.get() && view != null) view!!.id else View.NO_ID
    open var inflated = AtomicBoolean(false)

    @LayoutRes
    open var layoutId: Int = 0
    open var inflateViewAsync: Boolean = false
    open val inflateViewDelay: Long = 0L

    protected var afterInflateListener: Runnable? = null
    open fun beforeInflateView() = Unit
    open fun afterInflateView() = Unit
    open fun onInflateView(context: Context, inflater: LayoutInflater, parent: ViewGroup?): View? = inflater.inflate(layoutId, null, false)

    open fun <T : View> setInflatedCallback(callback: (T) -> Unit) {
        afterInflateListener = Runnable { callback(view as T) }
    }

    open fun inflate(context: Context, inflater: LayoutInflater, parent: ViewGroup?) {
        if (inflated.get()) {
            return
        }
        this.context = context
        this.inflater = inflater
        this.parent = parent
        val inflateTask = Runnable {
            beforeInflateView()
            view = onInflateView(context, inflater, parent) ?: inflater.inflate(layoutId, null, false)
            Log.d("Block", "inflate -- view: $view, viewId: $viewId, $parent, ${this.javaClass.name}")
            if (view!!.parent == null) {
                if (!inflateViewAsync) {
                    blockGroup?.addViewOfBlock(this)
                } else if (blockGroup != null) {
                    post(Runnable { blockGroup?.addViewOfBlock(this) }, type = RxHandler.TYPE_MAIN_THREAD)
                }
            }
            inflated.compareAndSet(false, true)
            putData(KEY_INFLATED, true)
            afterInflateListener?.run()
            afterInflateView()
        }
        if (inflateViewAsync) {
            post(inflateTask, inflateViewDelay, RxHandler.TYPE_NEW_THREAD)
        } else {
            inflateTask.run()
        }
    }

    // recycle

    open fun recycle() {
        this.rxHandler.release(this)
        if (this.view != null && this.view!!.parent != null) {
            (this.view!!.parent as ViewGroup).removeView(this.view)
        }
        this.parent = null
    }

    // refresh

    open fun refresh() = Unit
    open fun refreshGroup(): Unit = blockGroup?.refreshGroup() ?: Unit
    open fun refreshManager(): Unit = blockManager?.refreshManager() ?: Unit

    // observable / data

    open fun putData(key: String, value: Any?) = swb.putData(key, value)
    open fun putDataWithoutNotify(key: String, value: Any?) = swb.putDataWithoutNotify(key, value)
    open fun removeData(key: String) = swb.removeData(key)
    open fun getData(key: String) = swb.getData(key)
    open fun getObservable(key: String, threadSafe: Boolean = false): Observable<Any?> = swb.getObservable(key, threadSafe)

    open fun putBundle(bundle: Bundle?) = swb.putBundle(bundle)
    open fun putIntent(intent: Intent?) = swb.putIntent(intent)

    open fun putData(key: Class<*>, value: Any?) = cwb.putData(key, value)
    open fun putDataWithoutNotify(key: Class<*>, value: Any?) = cwb.putDataWithoutNotify(key, value)
    open fun removeData(key: Class<*>) = cwb.removeData(key)
    open fun getData(key: Class<*>) = cwb.getData(key)
    open fun getObservable(key: Class<*>, threadSafe: Boolean = false): Observable<Any?> = cwb.getObservable(key, threadSafe)

    open fun putData(value: Any) = cwb.putData(value)
    open fun putDataWithoutNotify(value: Any) = cwb.putDataWithoutNotify(value)

    // handler / disposable

    open fun dealConsumer(what: Int, consumer: Consumer<Message>? = null) = this.rxHandler.dealConsumer(what, consumer)
    open fun sendEmptyMessage(what: Int, delayMillis: Long = 0L, type: Int = RxHandler.TYPE_IMMEDIATE) = rxHandler.sendEmptyMessage(null, what, delayMillis, type)
    open fun sendMessage(msg: Message, delayMillis: Long = 0L, type: Int = RxHandler.TYPE_IMMEDIATE) = rxHandler.sendMessage(null, msg, delayMillis, type)
    open fun post(r: Runnable, delayMillis: Long = 0L, type: Int = RxHandler.TYPE_IMMEDIATE) = rxHandler.post(null, r, delayMillis, type)

    open fun dealConsumerInner(what: Int, consumer: Consumer<Message>? = null) = this.rxHandler.dealConsumerWithToken(what, this, consumer)
    open fun sendEmptyMessageInner(what: Int, delayMillis: Long = 0L, type: Int = RxHandler.TYPE_IMMEDIATE) = rxHandler.sendEmptyMessage(this, what, delayMillis, type)
    open fun sendMessageInner(msg: Message, delayMillis: Long = 0L, type: Int = RxHandler.TYPE_IMMEDIATE) = rxHandler.sendMessage(this, msg, delayMillis, type)
    open fun postInner(r: Runnable, delayMillis: Long = 0L, type: Int = RxHandler.TYPE_IMMEDIATE) = rxHandler.post(this, r, delayMillis, type)

    open fun register(disposable: Disposable, inBlock: Boolean = false) = rxHandler.register(disposable, if (inBlock) this else null)
    open fun registerAll(inBlock: Boolean, vararg disposables: Disposable) = rxHandler.registerAll(if (inBlock) this else null, *disposables)
}

open class FragmentBlock : Block(), FragmentLifeCycleInter {
    open var bundle: Bundle? = null

    // lifecycle

    override fun onAttach(context: Context) {
        this.context = context
    }

    override fun onCreate(bundle: Bundle?) {
        this.bundle = bundle
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, bundle: Bundle?): View? {
        this.bundle = bundle
        return this.view
    }

    override fun onActivityCreated(bundle: Bundle?) {
        this.bundle = bundle
    }

    override fun onStart() = Unit
    override fun onResume() = Unit
    override fun onPause() = Unit
    override fun onStop() = Unit
    override fun onDestroyView() = Unit
    override fun onDestroy() = this.rxHandler.release(this)
    override fun onDetach() = Unit

    override fun onSaveInstanceState(bundle: Bundle) = Unit
}

open class ActivityBlock : Block(), ActivityLifeCycleInter {
    open var bundle: Bundle? = null

    override fun onCreate(bundle: Bundle?) {
        this.bundle = bundle
    }

    override fun onRestart() = Unit
    override fun onStart() = Unit
    override fun onResume() = Unit
    override fun onPause() = Unit
    override fun onStop() = Unit
    override fun onDestroy() = this.rxHandler.release(this)

    override fun onSaveInstanceState(bundle: Bundle) = Unit
    override fun onRestoreInstanceState(bundle: Bundle) {
        this.bundle = bundle
    }
}
