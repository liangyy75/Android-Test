package com.example.uilib.block

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList

open class Block : ActivityProxy() {
    companion object {
        const val KEY_INFLATED = "key_inflated"
    }

    open lateinit var swb: WhiteBoard<String>
    open lateinit var cwb: WhiteBoard<Class<*>>
    open lateinit var rxHandler: RxHandlerProxy

    open var blockGroup: BlockGroup? = null
    open var blockManager: BlockManager? = null
    override var ai: ActivityInter
        get() = blockManager!!
        set(value) {}

    // init

    open fun init(swb: WhiteBoard<String>, cwb: WhiteBoard<Class<*>>, h: RxHandlerProxy, bg: BlockGroup? = null, bm: BlockManager? = null) {
        this.swb = swb
        this.cwb = cwb
        this.rxHandler = h
        this.blockGroup = bg
        this.blockManager = blockManager
        this.swb.putData(KEY_INFLATED, false)
    }

    // inflate

    open lateinit var context: Context
    open lateinit var inflater: LayoutInflater
    open lateinit var view: View
    open var inflated = AtomicBoolean(false)

    @LayoutRes
    open var layoutId: Int = 0
    open val inflateViewAsync: Boolean = false
    open val inflateViewDelay: Long = 0L
    open fun beforeInflateView() = Unit
    open fun afterInflateView() = Unit

    fun inflate(context: Context, inflater: LayoutInflater, parent: ViewGroup?) {
        if (inflated.get()) {
            return
        }
        this.context = context
        this.inflater = inflater
        val inflateTask = Runnable {
            beforeInflateView()
            view = inflater.inflate(layoutId, parent, false)
            blockGroup?.addViewOfBlock(this)
            inflated.compareAndSet(false, true)
            putData(KEY_INFLATED, true)
            afterInflateView()
        }
        if (inflateViewAsync) {
            post(inflateTask, inflateViewDelay, RxHandlerProxy.TYPE_ASYNC_TASK_POOL)
        } else {
            inflateTask.run()
        }
    }

    // recycle

    open fun recycle() {
        this.rxHandler.release(this)
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
    open fun sendEmptyMessage(what: Int, delayMillis: Long = 0L, type: Int = RxHandlerProxy.TYPE_IMMEDIATE) = rxHandler.sendEmptyMessage(null, what, delayMillis, type)
    open fun sendMessage(msg: Message, delayMillis: Long = 0L, type: Int = RxHandlerProxy.TYPE_IMMEDIATE) = rxHandler.sendMessage(null, msg, delayMillis, type)
    open fun post(r: Runnable, delayMillis: Long = 0L, type: Int = RxHandlerProxy.TYPE_IMMEDIATE) = rxHandler.post(null, r, delayMillis, type)

    open fun dealConsumerInner(what: Int, consumer: Consumer<Message>? = null) = this.rxHandler.dealConsumerWithToken(what, this, consumer)
    open fun sendEmptyMessageInner(what: Int, delayMillis: Long = 0L, type: Int = RxHandlerProxy.TYPE_IMMEDIATE) = rxHandler.sendEmptyMessage(this, what, delayMillis, type)
    open fun sendMessageInner(msg: Message, delayMillis: Long = 0L, type: Int = RxHandlerProxy.TYPE_IMMEDIATE) = rxHandler.sendMessage(this, msg, delayMillis, type)
    open fun postInner(r: Runnable, delayMillis: Long = 0L, type: Int = RxHandlerProxy.TYPE_IMMEDIATE) = rxHandler.post(this, r, delayMillis, type)

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

    override fun onActivityCreated() = Unit
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

open class BlockGroup : Block() {
    open val children: MutableList<Block> = Collections.synchronizedList(ArrayList<Block>())
    open lateinit var viewGroup: ViewGroup
    override var view: View
        get() = viewGroup
        set(value) {
            if (value is ViewGroup) {
                viewGroup = value
            }
        }

    override fun refresh() = refreshGroup()
    override fun refreshGroup(): Unit = children.forEach { it.refresh() }

    open fun addBlock(block: Block) {
        if (block in children) {
            return
        }
        children.add(block)
        if (block.inflated.get()) {
            block.recycle()
        }
        block.init(this.swb, this.cwb, this.rxHandler, this, this.blockManager)
        block.inflate(context, inflater, viewGroup)
    }

    open fun addBlockIf(condition: Boolean, block: Block) {
        if (condition) {
            addBlock(block)
        }
    }

    open fun addBlockIf(condition: () -> Boolean, block: Block) {
        if (condition()) {
            addBlock(block)
        }
    }

    open fun addViewOfBlock(block: Block) {
        TODO()
    }
}

open class BlockManager : BlockGroup(), FragmentLifeCycleInter, ActivityLifeCycleInter {
    companion object {
        const val KEY_FRAGMENT_STATE = "fragmentState"
        const val KEY_ACTIVITY_STATE = "activityState"
    }

    open val groups: MutableList<BlockGroup> = Collections.synchronizedList(ArrayList<BlockGroup>())
    open var bundle: Bundle? = null

    // activity proxy

    protected var innerActivity: Activity? = null
    protected var innerFragment: Fragment? = null
    override var ai: ActivityInter
        get() = this
        set(value) {
            throw RuntimeException("cann't set ai of blockManager")
        }

    override fun getActivity(): Activity? = if (innerActivity == null && innerFragment != null) {
        innerFragment!!.activity
    } else {
        innerActivity
    }

    override fun getFragment(): Fragment? = innerFragment
    override fun getFragmentManager(): FragmentManager? = getFragmentActivity()?.supportFragmentManager

    override fun getFragmentActivity(): FragmentActivity? = if (innerActivity is FragmentActivity) {
        innerActivity as FragmentActivity
    } else {
        null
    }

    override fun finish() = innerActivity?.finish() ?: Unit
    override fun startActivity(intent: Intent) = innerActivity?.startActivity(intent) ?: Unit
    override fun startActivityForResult(intent: Intent, requestCode: Int) = innerActivity?.startActivityForResult(intent, requestCode)
            ?: Unit

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) = innerActivity?.startActivityForResult(intent, requestCode, options)
            ?: Unit

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
    }

    // init

    open fun initInActivity(activity: Activity) {
        innerActivity = activity
        putData(KEY_ACTIVITY_STATE, ActivityLifeCycleInter.ORIGINAL)
    }

    open fun initInFragment(fragment: Fragment) {
        innerActivity = fragment.activity
        innerFragment = fragment
        putData(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.ORIGIN)
    }

    open fun initInBlockManager(blockManager: BlockManager) {}

    // refresh

    override fun refresh() = refreshManager()
    override fun refreshGroup() = refreshManager()
    override fun refreshManager(): Unit = groups.forEach { it.refreshGroup() }

    // lifecycle

    open fun putDataIfExists(key: String, value: Any?) {
        if (this.swb.getData(key) != null) {
            this.swb.putData(key, value)
        }
    }

    override fun onAttach(context: Context) {
        this.context = context
    }

    override fun onCreate(bundle: Bundle?) {
        putDataIfExists(KEY_ACTIVITY_STATE, ActivityLifeCycleInter.STATE_CREATE)
        putDataIfExists(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.STATE_CREATE)
    }

    override fun onRestart() {
        putDataIfExists(KEY_ACTIVITY_STATE, ActivityLifeCycleInter.STATE_RESTART)
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, bundle: Bundle?): View? {
        putDataIfExists(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.STATE_CREATE_VIEW)
        return this.view
    }

    override fun onActivityCreated() {
        putDataIfExists(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.STATE_ACTIVITY_CREATE)
    }

    override fun onStart() {
        putDataIfExists(KEY_ACTIVITY_STATE, ActivityLifeCycleInter.STATE_START)
        putDataIfExists(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.STATE_START)
    }

    override fun onResume() {
        putDataIfExists(KEY_ACTIVITY_STATE, ActivityLifeCycleInter.STATE_RESUME)
        putDataIfExists(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.STATE_RESUME)
    }

    override fun onPause() {
        putDataIfExists(KEY_ACTIVITY_STATE, ActivityLifeCycleInter.STATE_PAUSE)
        putDataIfExists(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.STATE_PAUSE)
    }

    override fun onStop() {
        putDataIfExists(KEY_ACTIVITY_STATE, ActivityLifeCycleInter.STATE_STOP)
        putDataIfExists(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.STATE_STOP)
    }

    override fun onDestroyView() {
        putDataIfExists(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.STATE_DESTROY_VIEW)
    }

    override fun onDestroy() {
        putDataIfExists(KEY_ACTIVITY_STATE, ActivityLifeCycleInter.STATE_DESTROY)
        putDataIfExists(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.STATE_DESTROY)
    }

    override fun onDetach() {
        putDataIfExists(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.STATE_DETACH)
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        putDataIfExists(KEY_ACTIVITY_STATE, ActivityLifeCycleInter.STATE_SAVE_INSTANCE_STATE)
        putDataIfExists(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.STATE_SAVE_INSTANCE_STATE)
    }

    override fun onRestoreInstanceState(bundle: Bundle) {
        this.bundle = bundle
        putDataIfExists(KEY_ACTIVITY_STATE, ActivityLifeCycleInter.STATE_RESTORE_INSTANCE_STATE)
    }
}

// block -> fragment
// blockGroup -> fragmentManager
// blockManager -> activity / fragment
