@file: Suppress("UNCHECKED_CAST", "unused")

package com.example.uilib.widget2

import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

open class KVData(@field:NonNull @get:NonNull @param:NonNull val key: String, @field:Nullable @param:Nullable private val data: Any?) {
    @Nullable
    open fun <T> getData(): T? = if (data == null) null else data as T

    @Nullable
    open fun <T : Any> getData(defaultValue: T?): T? = DataCenter[data, defaultValue]
}

open class NextLiveData<T> : MutableLiveData<T>() {
    private var mLatestVersion = -1
    private val nextObserverMap: MutableMap<Observer<*>, NextObserver<in T?>> = HashMap()

    @MainThread
    override fun observe(@NonNull owner: LifecycleOwner, @NonNull observer: Observer<in T?>) {
        observe(owner, observer, false)
    }

    @MainThread
    open fun observe(@NonNull owner: LifecycleOwner, @NonNull observer: Observer<in T?>, notifyWhenObserve: Boolean) {
        if (nextObserverMap.containsKey(observer)) {
            return
        }
        val nextObserver = NextObserver(mLatestVersion, observer, notifyWhenObserve)
        nextObserverMap[observer] = nextObserver
        super.observe(owner, nextObserver)
    }

    @MainThread
    override fun observeForever(@NonNull observer: Observer<in T?>) {
        observeForever(observer, false)
    }

    @MainThread
    open fun observeForever(@NonNull observer: Observer<in T?>, notifyWhenObserve: Boolean) {
        if (nextObserverMap.containsKey(observer)) {
            return
        }
        val nextObserver = NextObserver(mLatestVersion, observer, notifyWhenObserve)
        nextObserverMap[observer] = nextObserver
        super.observeForever(nextObserver)
    }

    override fun removeObserver(@NonNull observer: Observer<in T?>) {
        val nextObserver = nextObserverMap.remove(observer)
        if (nextObserver != null) {
            super.removeObserver(nextObserver)
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
        if (observer.javaClass == NextObserver<*>::javaClass) {
            var key: Observer<*>? = null
            val entries: MutableSet<MutableMap.MutableEntry<Observer<*>, NextObserver<in T?>>> = nextObserverMap.entries
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
    override fun setValue(@Nullable t: T?) {
        mLatestVersion++
        super.setValue(t)
    }

    private inner class NextObserver<T> internal constructor(private val initVersion: Int, private val observer: Observer<in T?>,
                                                             private val notifyWhenObserve: Boolean) : Observer<T?> {
        override fun onChanged(@Nullable t: T?) {
            if (!notifyWhenObserve && initVersion >= mLatestVersion) {
                return
            }
            observer.onChanged(t)
        }
    }
}

interface NonNullObserver<T> : Observer<T> {
    override fun onChanged(t: T)
}
