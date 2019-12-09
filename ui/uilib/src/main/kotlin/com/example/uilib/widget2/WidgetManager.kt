package com.example.uilib.widget2

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
import com.example.uilib.widget2.Widget.WidgetCallback
import java.util.concurrent.CopyOnWriteArrayList

class WidgetManager : Fragment() {
    var widgetCallback: WidgetCallback = object : WidgetCallback {
        override fun startActivity(intent: Intent?) {
            this@WidgetManager.startActivity(intent)
        }

        override fun startActivity(intent: Intent?, @Nullable options: Bundle?) {
            this@WidgetManager.startActivity(intent, options)
        }

        override fun startActivityForResult(intent: Intent?, requestCode: Int) {
            this@WidgetManager.startActivityForResult(intent, requestCode)
        }

        override fun startActivityForResult(intent: Intent?, requestCode: Int, @Nullable options: Bundle?) {
            this@WidgetManager.startActivityForResult(intent, requestCode, options)
        }

        override fun <T : ViewModel> getViewModel(clazz: Class<T>): T {
            return if (myParentFragment != null) {
                ViewModelProviders.of(myParentFragment!!).get(clazz)
            } else {
                ViewModelProviders.of(activity!!).get(clazz)
            }
        }

        override fun <T : ViewModel> getViewModel(clazz: Class<T>, @NonNull factory: ViewModelProvider.Factory): T {
            return if (myParentFragment != null) {
                ViewModelProviders.of(myParentFragment!!, factory).get(clazz)
            } else {
                ViewModelProviders.of(activity!!, factory).get(clazz)
            }
        }

        override val fragment: Fragment
            get() = this@WidgetManager
    }
    var myParentFragment: Fragment? = null
    var contentView: View? = null
    var myContext: Context? = null
    var asyncLayoutInflater: AsyncLayoutInflater? = null
    var syncLayoutInflater: LayoutInflater? = null
    var widgets = CopyOnWriteArrayList<Widget>()
    var widgetViewGroupHashMap = HashMap<Widget, ViewGroup>()
    var dataCenter: DataCenter? = null
        set(value) {
            field = value
            widgets.forEach { it.dataCenter = value }
        }
    var configured = false
    var parentDestroyedCallback: FragmentManager.FragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
            if (f === parentFragment) {
                fm.unregisterFragmentLifecycleCallbacks(this)
                f.childFragmentManager.beginTransaction().remove(this@WidgetManager).commitNowAllowingStateLoss()
            }
        }
    }

    // 某些情况下，业务方需要继承 WidgetManager 来拓展功能，但是原有的 create() 方法是 static 的，无法返回子类
    // 这里将 static create() 改为 非 static 的 config()
    fun config(fragmentActivity: FragmentActivity?, fragment: Fragment?, rootView: View?, context: Context?) {
        if (configured) {
            return
        }
        val fragmentManager = fragmentActivity?.supportFragmentManager ?: (fragment?.childFragmentManager ?: return)
        myParentFragment = fragment
        contentView = rootView
        myContext = context
        asyncLayoutInflater = AsyncLayoutInflater(context!!)
        syncLayoutInflater = LayoutInflater.from(context)
        if (fragment != null && fragment.fragmentManager != null) {
            fragment.fragmentManager!!.registerFragmentLifecycleCallbacks(parentDestroyedCallback, false)
        }
        fragmentManager.beginTransaction().add(this, TAG).commitNowAllowingStateLoss()
        configured = true
    }

    fun load(@IdRes containerId: Int, widget: Widget?, async: Boolean = true): WidgetManager {
        if (widget == null) {
            return this
        }
        widget.widgetCallback = widgetCallback
        widget.context = myContext
        widget.dataCenter = dataCenter
        val container = contentView!!.findViewById<ViewGroup>(containerId)
        widget.containerView = container
        widgetViewGroupHashMap[widget] = container
        if (widget.layoutId == 0) {
            continueLoad(widget, container, null)
            return this
        }
        if (async) {
            asyncLayoutInflater!!.inflate(widget.layoutId, container) { view: View?, resId: Int, parent: ViewGroup? ->
                if (!isRemoving && !isDetached && lifecycle.currentState != Lifecycle.State.DESTROYED) {
                    continueLoad(widget, container, view)
                }
            }
        } else {
            continueLoad(widget, container, syncLayoutInflater!!.inflate(widget.layoutId, container, false));
        }
        return this
    }

    fun continueLoad(widget: Widget, parentView: ViewGroup?, contentView: View?) {
        if (widget.containerView == null) { // 说明在 view 异步 inflate 的时候，该 Widget 又被 unload 。这种情况下就不再去 load 了
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
    fun load(widget: Widget?): WidgetManager {
        if (widget == null) {
            return this
        }
        widget.widgetCallback = widgetCallback
        widget.context = myContext
        widget.dataCenter = dataCenter
        widgets.add(widget)
        lifecycle.addObserver(widget)
        return this
    }

    fun unload(widget: Widget?): WidgetManager? {
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
        widgets.remove(widget)
        if (widgetViewGroupHashMap.containsKey(widget)) {
            widgetViewGroupHashMap[widget]!!.removeAllViews()
            widgetViewGroupHashMap.remove(widget)
        }
        return this
    }

    override fun onDestroy() {
        super.onDestroy()
        myParentFragment = null
        widgets.clear()
        widgetViewGroupHashMap.clear()
        dataCenter = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        widgets.forEach { it.onActivityResult(requestCode, resultCode, data) }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        widgets.forEach { it.onConfigurationChanged(newConfig) }
    }

    companion object {
        val TAG = WidgetManager::class.java.canonicalName

        /**
         * Note: call after Activity.setContentView()
         */
        fun of(activity: FragmentActivity?, rootView: View?): WidgetManager = WidgetManager().apply { config(activity, null, rootView, activity) }

        /**
         * Note: call from Fragment.onViewCreated()
         */
        fun of(fragment: Fragment, rootView: View?): WidgetManager = WidgetManager().apply { config(null, fragment, rootView, fragment.context) }
    }
}