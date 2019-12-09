package com.example.uilib.widget2

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.lifecycle.*

abstract class Widget : LifecycleObserver, LifecycleOwner {
    interface WidgetCallback {
        fun startActivity(intent: Intent?)
        fun startActivity(intent: Intent?, @Nullable options: Bundle?)
        fun startActivityForResult(intent: Intent?, requestCode: Int)
        fun startActivityForResult(intent: Intent?, requestCode: Int, @Nullable options: Bundle?)
        fun <T : ViewModel> getViewModel(clazz: Class<T>): T
        fun <T : ViewModel> getViewModel(clazz: Class<T>, @NonNull factory: ViewModelProvider.Factory): T
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
    open fun onStart() = lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

    @CallSuper
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    open fun onResume() = lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

    @CallSuper
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    open fun onPause() = lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)

    @CallSuper
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    open fun onStop() = lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)

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
            subWidgetManager!!.dataCenter = dataCenter
        }
    }

    open fun startActivity(intent: Intent?) = widgetCallback!!.startActivity(intent)

    open fun startActivity(intent: Intent?, @Nullable options: Bundle?) = widgetCallback!!.startActivity(intent, options)

    open fun startActivityForResult(intent: Intent?, requestCode: Int) = widgetCallback!!.startActivityForResult(intent, requestCode)

    open fun startActivityForResult(intent: Intent?, requestCode: Int, @Nullable options: Bundle?) =
            widgetCallback!!.startActivityForResult(intent, requestCode, options)

    open fun <T : ViewModel> getViewModel(clazz: Class<T>): T = widgetCallback!!.getViewModel(clazz)

    open fun <T : ViewModel> getViewModel(clazz: Class<T>, @NonNull factory: ViewModelProvider.Factory): T = widgetCallback!!.getViewModel(clazz, factory)

    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}
    open fun onConfigurationChanged(newConfig: Configuration?) {}
}
