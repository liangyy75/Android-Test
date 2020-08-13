@file:Suppress("UNCHECKED_CAST", "unused", "DEPRECATION", "LeakingThis")

package com.example.uilib.origin

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import java.util.concurrent.CopyOnWriteArrayList

open class DataCenter : ViewModel() {
    protected open val dataStore: MutableMap<String?, Any?> = HashMap()
    protected open val liveDataMap: MutableMap<String, NextLiveData<KVData>> = HashMap()
    open var lifecycleOwner: LifecycleOwner? = null
    protected open var mainThread: Thread? = null
    protected open val handler: Handler = Handler(Looper.getMainLooper())

    companion object {
        fun create(viewModelProvider: ViewModelProvider, lifecycleOwner: LifecycleOwner?): DataCenter? {
            val dataCenter: DataCenter = viewModelProvider.get(DataCenter::class.java)
            dataCenter.lifecycleOwner = lifecycleOwner
            return dataCenter
        }

        fun <T : Any> get(real: Any?, defaultValue: T?): T? {
            if (real == null) {
                return null
            }
            return if (defaultValue == null || real is Number && defaultValue is Number || defaultValue.javaClass.isAssignableFrom(real.javaClass)) {
                real as T?
            } else {
                defaultValue
            }
        }
    }

    open fun put(bundle: Bundle?): DataCenter {
        if (notMainThread()) {
            handler.post { put(bundle) }
            return this
        }
        if (bundle == null) {
            return this
        }
        for (key in bundle.keySet()) {
            if (key == null) {
                continue
            }
            val value: Any? = bundle.get(key)
            put(key, value)
        }
        return this
    }

    open fun put(key: String, data: Any?): DataCenter {
        if (notMainThread()) {
            handler.post { put(key, data) }
            return this
        }
        dataStore[key] = data
        liveDataMap[key]?.value = KVData(key, data)
        return this
    }

    @Deprecated("")
    open operator fun <T> get(key: String?): T? = dataStore[key] as? T

    /**
     * default 被使用的场景：
     * 1. DataCenter 中没有这个 key
     * 2. key 对应的值的类型，无法强转到 defaultValue 的类型
     */
    open operator fun <T : Any> get(key: String?, defaultValue: T?): T? = when {
        !dataStore.containsKey(key) -> defaultValue
        else -> get(dataStore[key], defaultValue)
    }

    open fun has(key: String?): Boolean = dataStore.containsKey(key)

    @JvmOverloads
    @MainThread
    open fun observe(key: String?, observer: Observer<KVData?>?, notifyWhenObserve: Boolean = false): DataCenter {
        if (key.isNullOrEmpty() || observer == null) {
            return this
        }
        lifecycleOwner?.let { getLiveData(key)?.observe(it, observer, notifyWhenObserve) }
        return this
    }

    @JvmOverloads
    @MainThread
    open fun observeForever(key: String?, observer: Observer<KVData?>?, notifyWhenObserve: Boolean = false): DataCenter {
        if (key.isNullOrEmpty() || observer == null) {
            return this
        }
        getLiveData(key)?.observeForever(observer, notifyWhenObserve)
        return this
    }

    protected open fun getLiveData(key: String): NextLiveData<KVData>? {
        var liveData: NextLiveData<KVData>? = liveDataMap[key]
        if (liveData == null) {
            liveData = NextLiveData()
            if (dataStore.containsKey(key)) {
                liveData.value = KVData(key, dataStore[key])
            }
            liveDataMap[key] = liveData
        }
        return liveData
    }

    @MainThread
    open fun removeObserver(key: String?, observer: Observer<KVData?>?): DataCenter {
        if (key.isNullOrEmpty() || observer == null) {
            return this
        }
        liveDataMap[key]?.removeObserver(observer)
        return this
    }

    @MainThread
    open fun removeObserver(observer: Observer<KVData?>?): DataCenter {
        if (observer == null) {
            return this
        }
        for (liveData in liveDataMap.values) {
            liveData.removeObserver(observer)
        }
        return this
    }

    override fun onCleared() {
        dataStore.clear()
        liveDataMap.clear()
        lifecycleOwner = null
        handler.removeCallbacksAndMessages(null)
    }

    @SuppressLint("NewApi")
    protected open fun notMainThread(): Boolean {
        if (mainThread == null) {
            mainThread = Looper.getMainLooper().thread
        }
        return Thread.currentThread() !== mainThread
    }
}

open class KVData(
        @field:NonNull @get:NonNull @param:NonNull val key: String,
        @field:Nullable @param:Nullable private val data: Any?) {
    @Nullable
    open fun <T> getData(): T? = if (data == null) null else data as T

    @Nullable
    open fun <T : Any> getData(defaultValue: T?): T? = DataCenter.get(data, defaultValue)
}

open class NextLiveData<T> : MutableLiveData<T?>() {
    private var mLatestVersion = -1
    private val nextObserverMap: MutableMap<Observer<*>?, NextObserver<*>?>? = HashMap()

    @JvmOverloads
    @MainThread
    open fun observe(owner: LifecycleOwner, observer: Observer<T?>, notifyWhenObserve: Boolean = false) {
        if (nextObserverMap!!.containsKey(observer)) {
            return
        }
        val nextObserver = NextObserver<T?>(mLatestVersion, observer, notifyWhenObserve)
        nextObserverMap[observer] = nextObserver
        super.observe(owner, nextObserver)
    }

    @JvmOverloads
    @MainThread
    open fun observeForever(observer: Observer<T?>, notifyWhenObserve: Boolean = false) {
        if (nextObserverMap!!.containsKey(observer)) {
            return
        }
        val nextObserver = NextObserver<T?>(mLatestVersion, observer, notifyWhenObserve)
        nextObserverMap[observer] = nextObserver
        super.observeForever(nextObserver)
    }

    override fun removeObserver(observer: Observer<in T?>) {
        val nextObserver = nextObserverMap!!.remove(observer)
        if (nextObserver != null) {
            super.removeObserver(nextObserver as? Observer<in T?> ?: return)
            return
        }
        // Note: 外部调用 NextLiveData.removeObserver 时，我们能够在 map 中找到对应的 NextObserver
        // 但是 LiveData 内部也可能调用 removeObserver 方法
        // 比如 Lifecycle 发生变化时，LifecycleBoundObserver 可能在内部会调用到
        // 内部调用时，传进来的参数是 NextObserver，我们用它作为 key 在 map 中是找不到的
        // 所以需要单独处理这种情况
        // 首先保证 super.removeObserver 一定能被调用到
        super.removeObserver(observer)
        // 其次从 map 中删除对应的 Entry
        if (observer is NextLiveData<*>.NextObserver<*>) {
            var key: Observer<*>? = null
            val entries: MutableSet<MutableMap.MutableEntry<Observer<*>?, NextObserver<*>?>> = nextObserverMap.entries
            for (entry in entries) {
                if (observer == entry.value) {
                    key = entry.key
                    break
                }
            }
            if (key != null) {
                nextObserverMap.remove(key)
            }
        }
    }

    @MainThread
    override fun setValue(t: T?) {
        mLatestVersion++
        super.setValue(t)
    }

    private inner class NextObserver<T> internal constructor(
            private val initVersion: Int,
            private val observer: Observer<T?>?,
            private val notifyWhenObserve: Boolean) : Observer<T?> {
        override fun onChanged(t: T?) {
            if (!notifyWhenObserve && initVersion >= mLatestVersion) {
                return
            }
            observer?.onChanged(t)
        }
    }
}

abstract class Widget : LifecycleObserver, LifecycleOwner {
    interface WidgetCallback {
        fun startActivity(intent: Intent?)
        fun startActivity(intent: Intent?, options: Bundle?)
        fun startActivityForResult(intent: Intent?, requestCode: Int)
        fun startActivityForResult(intent: Intent?, requestCode: Int, options: Bundle?)
        fun <T : ViewModel?> getViewModel(clazz: Class<T>): T?
        fun <T : ViewModel?> getViewModel(clazz: Class<T>, factory: ViewModelProvider.Factory): T?
        val fragment: Fragment
    }

    open var widgetCallback: WidgetCallback? = null
    open var context: Context? = null
    open var containerView: ViewGroup? = null
    open var contentView: View? = null
    open var isViewValid = false
    open var isDestroyed = false
    open var dataCenter: DataCenter? = null
    open var subWidgetManager: WidgetManager? = null
    open var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    @get:LayoutRes
    open val layoutId: Int
        get() = 0

    @CallSuper
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    open fun onCreate() {
        isViewValid = true
        isDestroyed = false
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    @CallSuper
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    open fun onStart() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    @CallSuper
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    open fun onResume() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    @CallSuper
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    open fun onPause() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    @CallSuper
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    open fun onStop() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    @CallSuper
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    open fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        if (subWidgetManager != null) {
            widgetCallback!!.fragment.childFragmentManager.beginTransaction().remove(subWidgetManager!!).commitNowAllowingStateLoss()
        }
        isDestroyed = true
        isViewValid = false
    }

    /**
     * 如果 Widget 自身布局很复杂，需要支持 sub Widget，那么可以在 onCreate() 中调用该方法
     * 然后直接使用 subWidgetManager 即可
     */
    open fun enableSubWidgetManager() {
        if (subWidgetManager == null) {
            subWidgetManager = WidgetManager.of(widgetCallback!!.fragment, contentView)
            subWidgetManager!!.setDataCenter(dataCenter)
        }
    }

    open fun startActivity(intent: Intent?) {
        widgetCallback!!.startActivity(intent)
    }

    open fun startActivity(intent: Intent?, options: Bundle?) {
        widgetCallback!!.startActivity(intent, options)
    }

    open fun startActivityForResult(intent: Intent?, requestCode: Int) {
        widgetCallback!!.startActivityForResult(intent, requestCode)
    }

    open fun startActivityForResult(intent: Intent?, requestCode: Int, options: Bundle?) {
        widgetCallback!!.startActivityForResult(intent, requestCode, options)
    }

    open fun <T : ViewModel?> getViewModel(clazz: Class<T>): T? = widgetCallback!!.getViewModel(clazz)
    open fun <T : ViewModel?> getViewModel(clazz: Class<T>, factory: ViewModelProvider.Factory): T? {
        return widgetCallback!!.getViewModel(clazz, factory)
    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}
    open fun onConfigurationChanged(newConfig: Configuration?) {}
}

open class WidgetManager : Fragment() {
    open var widgetCallback: Widget.WidgetCallback = object : Widget.WidgetCallback {
        override fun startActivity(intent: Intent?) = this@WidgetManager.startActivity(intent)
        override fun startActivity(intent: Intent?, options: Bundle?) = this@WidgetManager.startActivity(intent, options)
        override fun startActivityForResult(intent: Intent?, requestCode: Int) = this@WidgetManager.startActivityForResult(intent, requestCode)
        override fun startActivityForResult(intent: Intent?, requestCode: Int, options: Bundle?) =
                this@WidgetManager.startActivityForResult(intent, requestCode, options)

        override fun <T : ViewModel?> getViewModel(clazz: Class<T>): T? = when {
            parentFragment2 != null -> ViewModelProviders.of(parentFragment2!!).get<T>(clazz)
            activity != null -> ViewModelProviders.of(activity!!).get<T>(clazz)
            else -> null
        }

        override fun <T : ViewModel?> getViewModel(clazz: Class<T>, factory: ViewModelProvider.Factory): T? = when {
            parentFragment2 != null -> ViewModelProviders.of(parentFragment2!!, factory).get(clazz)
            activity != null -> ViewModelProviders.of(activity!!, factory).get(clazz)
            else -> null
        }

        override val fragment: Fragment
            get() = this@WidgetManager
    }
    open var parentFragment2: Fragment? = null
    open var contentView: View? = null
    open var context2: Context? = null
    open var asyncLayoutInflater: AsyncLayoutInflater? = null
    open var syncLayoutInflater: LayoutInflater? = null
    open var widgets: MutableList<Widget> = CopyOnWriteArrayList()
    open var widgetViewGroupHashMap: MutableMap<Widget, ViewGroup> = HashMap()
    open var dataCenter: DataCenter? = null
    open var configured = false
    open var parentDestroyedCallback: FragmentManager.FragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
            if (f === parentFragment2) {
                fm.unregisterFragmentLifecycleCallbacks(this)
                f.childFragmentManager.beginTransaction()
                        .remove(this@WidgetManager).commitNowAllowingStateLoss()
            }
        }
    }
    open var widgetConfigHandler: IWidgetConfigHandler? = null

    // 某些情况下，业务方需要继承 WidgetManager 来拓展功能，但是原有的 create() 方法是 static 的，无法返回子类
    // 这里将 static create() 改为 非 static 的 config()
    open fun config(fragmentActivity: FragmentActivity?, fragment: Fragment?, rootView: View?, context: Context) {
        if (configured) {
            return
        }
        val fragmentManager: FragmentManager = fragmentActivity?.supportFragmentManager ?: (fragment?.childFragmentManager ?: return)
        parentFragment2 = fragment
        contentView = rootView
        this.context2 = context
        asyncLayoutInflater = AsyncLayoutInflater(context)
        syncLayoutInflater = LayoutInflater.from(context)
        if (fragment != null && fragment.fragmentManager != null) {
            fragment.fragmentManager!!.registerFragmentLifecycleCallbacks(parentDestroyedCallback, false)
        }
        fragmentManager.beginTransaction().add(this, TAG).commitNowAllowingStateLoss()
        configured = true
    }

    open fun setDataCenter(dataCenter: DataCenter?): WidgetManager {
        this.dataCenter = dataCenter
        for (widget in widgets) {
            widget.dataCenter = dataCenter
        }
        return this
    }

    open fun setWidgetConfigHandler(handler: IWidgetConfigHandler?): WidgetManager? {
        widgetConfigHandler = handler
        return this
    }

    @JvmOverloads
    open fun load(@IdRes containerId: Int, widget: Widget?, async: Boolean = true): WidgetManager {
        if (widget == null) {
            return this
        }
        widget.widgetCallback = widgetCallback
        widget.context = context2
        widget.dataCenter = dataCenter
        val container = contentView!!.findViewById<ViewGroup>(containerId)
        widget.containerView = container
        widgetViewGroupHashMap[widget] = container
        widgetConfigHandler?.onLoad(widget)
        if (widget.layoutId == 0) {
            continueLoad(widget, container, null)
            return this
        }
        return if (async) {
            asyncLayoutInflater!!.inflate(widget.layoutId, container) { view, _, _ ->
                if (isRemoving || isDetached || lifecycle.currentState == Lifecycle.State.DESTROYED) {
                    return@inflate
                }
                continueLoad(widget, container, view)
            }
            this
        } else {
            val contentView: View = syncLayoutInflater!!.inflate(widget.layoutId, container, false)
            continueLoad(widget, container, contentView)
            this
        }
    }

    open fun continueLoad(widget: Widget, parentView: ViewGroup?, contentView: View?) {
        if (widget.containerView == null) {
            // 说明在 view 异步 inflate 的时候，该 Widget 又被 unload
            // 这种情况下就不再去 load 了
            return
        }
        widget.contentView = contentView
        if (parentView != null && contentView != null) {
            parentView.addView(contentView)
        }
        widgets.add(widget)
        lifecycle.addObserver(widget)
    }

    /**
     * 加载没有 View 的 Widget
     */
    open fun load(widget: Widget?): WidgetManager {
        if (widget == null) {
            return this
        }
        widget.widgetCallback = widgetCallback
        widget.context = context2
        widget.dataCenter = dataCenter
        widgets.add(widget)
        lifecycle.addObserver(widget)
        widgetConfigHandler?.onLoad(widget)
        return this
    }

    open fun unload(widget: Widget?): WidgetManager {
        if (widget == null) {
            return this
        }
        lifecycle.removeObserver(widget)
        if (widget.isViewValid) {
            when (lifecycle.currentState) {
                Lifecycle.State.INITIALIZED -> {
                }
                Lifecycle.State.DESTROYED -> if (!widget.isDestroyed) {
                    widget.onDestroy()
                }
                Lifecycle.State.CREATED -> widget.onDestroy()
                Lifecycle.State.STARTED -> {
                    widget.onStop()
                    widget.onDestroy()
                }
                Lifecycle.State.RESUMED -> {
                    widget.onPause()
                    widget.onStop()
                    widget.onDestroy()
                }
                else -> {
                }
            }
        }
        widget.containerView = null
        widget.context = null
        widget.widgetCallback = null
        widget.dataCenter = null
        widgetConfigHandler?.onUnload(widget)
        widgets.remove(widget)
        if (widgetViewGroupHashMap.containsKey(widget)) {
            val container = widgetViewGroupHashMap[widget]
            container!!.removeAllViews()
            widgetViewGroupHashMap.remove(widget)
        }
        return this
    }

    override fun onDestroy() {
        super.onDestroy()
        parentFragment2 = null
        if (widgetConfigHandler != null) {
            for (widget in widgets) {
                widgetConfigHandler!!.onDestroy(widget)
            }
        }
        widgets.clear()
        widgetViewGroupHashMap.clear()
        dataCenter = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        for (widget in widgets) {
            widget.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        for (widget in widgets) {
            widget.onConfigurationChanged(newConfig)
        }
    }

    // 提供在 Widget 创建或销毁的过程中进行额外配置的接口
    interface IWidgetConfigHandler {
        // 显式调用 load 时
        fun onLoad(@NonNull widget: Widget?)

        // 显式调用 unload 时
        fun onUnload(@NonNull widget: Widget?)

        // 跟随 WidgetManager 被销毁时
        fun onDestroy(@NonNull widget: Widget?)
    }

    companion object {
        val TAG = WidgetManager::class.java.canonicalName

        /**
         * Note: call after Activity.setContentView()
         */
        fun of(activity: FragmentActivity, rootView: View?): WidgetManager {
            val widgetManager = WidgetManager()
            widgetManager.config(activity, null, rootView, activity)
            return widgetManager
        }

        /**
         * Note: call from Fragment.onViewCreated()
         */
        fun of(fragment: Fragment, rootView: View?): WidgetManager {
            val widgetManager = WidgetManager()
            widgetManager.config(null, fragment, rootView, fragment.context!!)
            return widgetManager
        }
    }
}
