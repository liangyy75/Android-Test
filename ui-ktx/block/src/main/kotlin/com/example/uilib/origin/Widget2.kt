package com.example.uilib.origin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.RestrictTo
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.OnLifecycleEvent
import java.util.WeakHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.collections.HashMap

open class Widget2 : LifecycleObserver, LifecycleOwner {
    internal var widgetHost: WidgetHost? = null
    internal var isDestroyed: Boolean = false
    protected lateinit var container: ViewGroup
    protected var contentView: View? = null
    internal var isFirstLoadedInternal: Boolean = true

    private val lifecycleRegistry by lazy { LifecycleRegistry(this) }

    internal val childWidgetManager by lazy {
        WidgetManager2.of(lifecycle, requireWidgetHost(), contentView)
    }

    val isFirstLoaded: Boolean
        get() = isFirstLoadedInternal

    open val layoutId: Int
        @LayoutRes
        get() = 0

    internal fun setContentView(contentView: View) {
        this.contentView = contentView
    }

    internal fun setContainerView(containerView: ViewGroup) {
        this.container = containerView
    }

    @CallSuper
    open fun onCreate() {
        isDestroyed = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    internal fun create() {
        onCreate()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    @CallSuper
    open fun onStart() = Unit

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    internal fun start() {
        onStart()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    @CallSuper
    open fun onResume() = Unit

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    internal fun resume() {
        onResume()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    @CallSuper
    open fun onPause() = Unit

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    internal fun pause() {
        onPause()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    @CallSuper
    open fun onStop() = Unit

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    internal fun stop() {
        onStop()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    @CallSuper
    open fun onDestroy() {
        isDestroyed = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    internal fun destroy() {
        onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    fun getHost(): Any {
        return requireWidgetHost().requireWidgetHost()
    }

    internal fun requireWidgetHost(): WidgetHost = requireNotNull(widgetHost)

    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}
    protected fun startActivity(intent: Intent) = requireWidgetHost().startActivity(intent)
    protected fun startActivity(intent: Intent, options: Bundle?) = requireWidgetHost().startActivity(intent, options)
    protected fun startActivityForResult(intent: Intent, requestCode: Int) = requireWidgetHost().startActivityForResult(intent, requestCode, this)
    protected fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) =
            requireWidgetHost().startActivityForResult(intent, requestCode, options, this)

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}

open class WidgetHost : Fragment() {
    private var myParentFragment: Fragment? = null
    private val activityResultRequests = hashMapOf<Int, Widget2>()
    private val widgetManagerCallbackList: MutableList<WidgetLoadStateCallback> = mutableListOf()
    private val managerMap: MutableMap<Lifecycle, WidgetManager2> = WeakHashMap()
    private val listeners = mutableSetOf<() -> Unit>()

    internal operator fun get(lifecycle: Lifecycle): WidgetManager2? = managerMap[lifecycle]

    internal operator fun set(lifecycle: Lifecycle, manager: WidgetManager2) {
        managerMap[lifecycle] = manager
    }

    fun registerOnClearedListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    private fun clear() {
        listeners.forEach { it() }
        listeners.clear()
    }

    internal fun requireWidgetContext(): Context {
        return requireContext()
    }

    internal fun requireWidgetHost(): Any {
        return parentFragment ?: requireHost()
    }

    internal fun startActivityForResult(intent: Intent, requestCode: Int, widget: Widget2) {
        activityResultRequests[requestCode] = widget
        super.startActivityForResult(intent, requestCode)
    }

    internal fun startActivityForResult(
            intent: Intent,
            requestCode: Int,
            options: Bundle?,
            widget: Widget2
    ) {
        activityResultRequests[requestCode] = widget
        super.startActivityForResult(intent, requestCode, options)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val widget = activityResultRequests[requestCode]
        widget?.onActivityResult(requestCode, resultCode, data)
        activityResultRequests.remove(requestCode)
    }

    fun registerWidgetLoadStateCallback(callback: WidgetLoadStateCallback) {
        synchronized(widgetManagerCallbackList) {
            widgetManagerCallbackList.add(callback)
        }
    }

    fun unregisterWidgetLoadStateCallback(callback: WidgetLoadStateCallback) {
        synchronized(widgetManagerCallbackList) {
            widgetManagerCallbackList.remove(callback)
        }
    }

    internal fun dispatchWidgetLoaded(widget: Widget2) {
        widgetManagerCallbackList.forEach {
            it.onWidgetLoaded(widget)
        }
        if (widget.isFirstLoaded) {
            widget.isFirstLoadedInternal = false
        }
    }

    internal fun dispatchWidgetUnloaded(widget: Widget2) {
        widgetManagerCallbackList.forEach {
            it.onWidgetUnloaded(widget)
        }
    }

    interface WidgetLoadStateCallback {
        fun onWidgetLoaded(widget: Widget2) = Unit
        fun onWidgetUnloaded(widget: Widget2) = Unit
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    companion object {

        private val TAG = WidgetHost::class.java.canonicalName

        fun of(activity: FragmentActivity): WidgetHost = createHost(activity, null)
        fun of(fragment: Fragment): WidgetHost = createHost(null, fragment)
        fun of(widget: Widget2): WidgetHost = widget.requireWidgetHost()

        internal fun createHost(
                activity: FragmentActivity?,
                fragment: Fragment?
        ): WidgetHost {
            val fragmentManager: FragmentManager =
                    activity?.supportFragmentManager
                            ?: fragment!!.childFragmentManager
            return (fragmentManager.findFragmentByTag(TAG) as? WidgetHost)
                    ?: WidgetHost()
                            .also { widgetHost ->
                                widgetHost.myParentFragment = fragment
                                fragment
                                        ?.fragmentManager
                                        ?.registerFragmentLifecycleCallbacks(
                                                object : FragmentManager.FragmentLifecycleCallbacks() {
                                                    override fun onFragmentViewDestroyed(
                                                            fm: FragmentManager,
                                                            f: Fragment
                                                    ) {
                                                        if (f === widgetHost.myParentFragment) {
                                                            fm.unregisterFragmentLifecycleCallbacks(this)
                                                            f.childFragmentManager.beginTransaction()
                                                                    .remove(widgetHost).commitNowAllowingStateLoss()
                                                            widgetHost.clear()
                                                        }
                                                    }
                                                }, false
                                        )
                                fragmentManager
                                        .beginTransaction().add(widgetHost, TAG)
                                        .commitNowAllowingStateLoss()
                            }
        }
    }
}

class WidgetManager2 internal constructor(
        widgetHost: WidgetHost,
        private val contentView: View?,
        private val parentLifecycle: Lifecycle
) {
    private val asyncLayoutInflater: AsyncLayoutInflater by lazy { AsyncLayoutInflater(widgetHost.requireWidgetContext()) }
    private val syncLayoutInflater: LayoutInflater by lazy { LayoutInflater.from(widgetHost.requireWidgetContext()) }
    private val widgets = CopyOnWriteArrayList<Widget2>()
    private val widgetViewGroupHashMap = HashMap<Widget2, ViewGroup>()

    private var widgetHostInternal: WidgetHost? = widgetHost

    private val widgetHost: WidgetHost get() = requireNotNull(widgetHostInternal)

    init {
        widgetHost.registerOnClearedListener {
            widgets.forEach { unload(it) }
            widgets.clear()
            widgetHostInternal = null
        }
    }

    /**
     * Load a widget and attach its view to the root view
     *
     * [stubId] Id of the stub view this widget will be attached to
     * [asyncInflate] If true, a [AsyncLayoutInflater] will be used to inflate the widget's view
     */
    fun load(@IdRes stubId: Int, widget: Widget2, asyncInflate: Boolean = true): WidgetManager2 {
        if (widget.layoutId <= 0) return load(widget)
        val content =
                requireNotNull(contentView) { "make sure this WidgetManager is created with rootView" }
        widget.widgetHost = widgetHost
        val container = content.findViewById<ViewGroup>(stubId)
        widget.setContainerView(container)
        widgetViewGroupHashMap[widget] = container

        if (asyncInflate) {
            asyncLayoutInflater.inflate(widget.layoutId, container) { view, _, _ ->
                if (widgetHostInternal == null
                        || widgetHost.isDetached
                        || widgetHost.isRemoving
                        || parentLifecycle.currentState == Lifecycle.State.DESTROYED
                ) {
                    return@inflate
                }
                continueLoad(widget, container, view)
            }
        } else {
            continueLoad(
                    widget, container,
                    syncLayoutInflater.inflate(widget.layoutId, container, false)
            )
        }
        return this
    }

    /**
     * Load a widget without UI
     */
    fun load(widget: Widget2): WidgetManager2 {
        if (widgets.contains(widget)) {
            return this
        }
        widget.widgetHost = widgetHost
        widgets.add(widget)
        widgetHost.dispatchWidgetLoaded(widget)
        parentLifecycle.addObserver(widget)
        return this
    }

    /**
     * Unload a widget from the WidgetManager
     */
    fun unload(widget: Widget2): WidgetManager2 {
        parentLifecycle.removeObserver(widget)
        when (parentLifecycle.currentState) {
            Lifecycle.State.INITIALIZED -> {
            }
            Lifecycle.State.DESTROYED -> if (!widget.isDestroyed) {
                widget.destroy()
            }
            Lifecycle.State.CREATED -> widget.destroy()
            Lifecycle.State.STARTED -> {
                widget.stop()
                widget.destroy()
            }
            Lifecycle.State.RESUMED -> {
                widget.pause()
                widget.stop()
                widget.destroy()
            }
        }
        widget.widgetHost = null
        widgets.remove(widget)
        if (widgetViewGroupHashMap.containsKey(widget)) {
            val container = widgetViewGroupHashMap[widget]
            container!!.removeAllViews()
            widgetViewGroupHashMap.remove(widget)
        }
        widgetHost.dispatchWidgetUnloaded(widget)
        return this
    }

    private fun continueLoad(widget: Widget2, parentView: ViewGroup, contentView: View) {
        widget.setContentView(contentView)
        parentView.addView(contentView)
        widgets.add(widget)
        widgetHost.dispatchWidgetLoaded(widget)
        parentLifecycle.addObserver(widget)
    }

    companion object {

        /**
         * get a WidgetManager scoped to this [activity]. you always get the same one with the same [activity]
         *
         * without [rootView], [WidgetManager.load] with idRes will fail.
         *
         * Note: call after Activity.setContentView() when [rootView] is not null.
         */
        fun of(activity: FragmentActivity, rootView: View? = null): WidgetManager2 =
                create(activity, null, rootView)

        /**
         * get a WidgetManager scoped to this [fragment]. you always get the same one with the same [fragment]
         *
         * without [rootView], [WidgetManager.load] with idRes will fail.
         *
         * Note: call from Fragment.onViewCreated() when [rootView] is not null.
         */
        fun of(fragment: Fragment, rootView: View? = null): WidgetManager2 =
                create(null, fragment, rootView)

        private fun create(
                fragmentActivity: FragmentActivity?,
                fragment: Fragment?,
                rootView: View?
        ): WidgetManager2 =
                WidgetHost
                        .createHost(fragmentActivity, fragment)
                        .let { of(it.lifecycle, it, rootView) }

        /**
         * get a WidgetManager scoped to this [widget]. you always get the same one with the same [widget]
         *
         * Note: call after [widget] is loaded to its WidgetManager, otherwise this WidgetManager's [load] with idRes will fail.
         */
        fun of(widget: Widget2): WidgetManager2 = widget.childWidgetManager

        fun of(
                lifecycle: Lifecycle,
                widgetHost: WidgetHost,
                rootView: View? = null
        ): WidgetManager2 {
            return widgetHost[lifecycle]
                    ?: WidgetManager2(widgetHost, rootView, lifecycle)
                            .also { widgetHost[lifecycle] = it }
        }
    }
}
