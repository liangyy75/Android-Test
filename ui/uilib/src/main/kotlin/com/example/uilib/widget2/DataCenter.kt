package com.example.uilib.widget2

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.annotation.MainThread
import androidx.lifecycle.*

@Suppress("UNCHECKED_CAST", "unused", "MemberVisibilityCanBePrivate")
class DataCenter : ViewModel() {
    private val dataStore: MutableMap<String, Any?> = HashMap()
    private val liveDataMap: MutableMap<String, NextLiveData<KVData>?> = HashMap()
    var lifecycleOwner: LifecycleOwner? = null
    private var mainThread: Thread? = null
    private val handler = Handler(Looper.getMainLooper())

    fun put(bundle: Bundle?): DataCenter {
        if (bundle == null) {
            return this
        }
        if (notMainThread()) {
            handler.post { put(bundle) }
            return this
        }
        for (key in bundle.keySet()) {
            if (key == null) {
                continue
            }
            put(key, bundle[key])
        }
        return this
    }

    fun put(key: String, data: Any?): DataCenter {
        if (notMainThread()) {
            handler.post { put(key, data) }
            return this
        }
        dataStore[key] = data
        liveDataMap[key]?.value = KVData(key, data)
        return this
    }

    //    public void remove(String key) {
    //        dataStore.remove(key);
    //        mObserverMap.remove(key);
    //    }
    @Deprecated("")
    operator fun <T> get(key: String?): T? {
        val value = dataStore[key]
        return if (value != null) {
            value as T
        } else null
    }

    /**
     * default 被使用的场景：
     * 1. DataCenter 中没有这个 key
     * 2. key 对应的值的类型，无法强转到 defaultValue 的类型
     */
    operator fun <T : Any> get(key: String?, defaultValue: T?): T? {
        if (!dataStore.containsKey(key)) {
            return defaultValue
        }
        return DataCenter[dataStore[key], defaultValue]
    }

    fun has(key: String?): Boolean = dataStore.containsKey(key)

    @MainThread
    fun observe(key: String?, observer: Observer<KVData?>?): DataCenter {
        return observe(key, observer, false)
    }

    @MainThread
    fun observe(key: String?, observer: Observer<KVData?>?, notifyWhenObserve: Boolean): DataCenter {
        if (TextUtils.isEmpty(key) || observer == null) {
            return this
        }
        val liveData = getLiveData(key!!)
        liveData.observe(lifecycleOwner!!, observer, notifyWhenObserve)
        return this
    }

    @MainThread
    fun observeForever(key: String?, observer: Observer<KVData?>?): DataCenter {
        return observeForever(key, observer, false)
    }

    @MainThread
    fun observeForever(key: String?, observer: Observer<KVData?>?, notifyWhenObserve: Boolean): DataCenter {
        if (TextUtils.isEmpty(key) || observer == null) {
            return this
        }
        val liveData = getLiveData(key!!)
        liveData.observeForever(observer, notifyWhenObserve)
        return this
    }

    private fun getLiveData(key: String): NextLiveData<KVData> {
        var liveData = liveDataMap[key]
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
    fun removeObserver(key: String?, observer: Observer<KVData?>?): DataCenter {
        if (TextUtils.isEmpty(key) || observer == null) {
            return this
        }
        val liveData = liveDataMap[key]
        liveData?.removeObserver(observer)
        return this
    }

    @MainThread
    fun removeObserver(observer: Observer<KVData?>?): DataCenter {
        if (observer == null) {
            return this
        }
        for (liveData in liveDataMap.values) {
            liveData?.removeObserver(observer)
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
    private fun notMainThread(): Boolean {
        if (mainThread == null) {
            mainThread = Looper.getMainLooper().thread
        }
        return Thread.currentThread() !== mainThread
    }

    companion object {
        fun create(viewModelProvider: ViewModelProvider, lifecycleOwner: LifecycleOwner?): DataCenter =
                viewModelProvider.get(DataCenter::class.java).apply { this.lifecycleOwner = lifecycleOwner }

        operator fun <T : Any> get(real: Any?, defaultValue: T?): T? {
            if (real == null) {
                return null
            }
            return if (defaultValue == null || real is Number && defaultValue is Number || defaultValue.javaClass.isAssignableFrom(real.javaClass)) {
                real as T
            } else {
                defaultValue
            }
        }
    }
}
