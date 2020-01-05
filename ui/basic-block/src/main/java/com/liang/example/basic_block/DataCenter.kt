package com.liang.example.basic_block

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Observable
import java.util.Observer
import java.util.concurrent.ConcurrentHashMap

@Suppress("MemberVisibilityCanBePrivate", "UNCHECKED_CAST")
open class DataCenter<T> : ViewModel(), Observer {
    /**
     * 每个 observer 刚 add 进来的时候就获取当前数据值
     */
    open class WithNowObservable(var now: Any? = NUL_OBJ) : Observable() {
        @Synchronized
        override fun addObserver(o: Observer?) {
            if (now != NUL_OBJ) {
                o?.update(this, now)
            }
            super.addObserver(o)
        }

        @Synchronized
        override fun notifyObservers(arg: Any?) {
            now = arg
            super.notifyObservers(arg)
        }

        companion object {
            val NUL_OBJ = Any()
        }
    }

    // 数据存储
    protected val dataMap = ConcurrentHashMap<T, Any>()
    // 订阅器集合
    protected val observableMap = ConcurrentHashMap<T, Observable>()
    // 数据中心状态
    @Volatile
    protected var active: Boolean = true
    // tokenData
    protected val tokenDataList = mutableListOf<WithToken<T>>()

    /**
     * 放入/删除数据，并且通知 observer
     *
     * @param key
     * @param data
     */
    @JvmOverloads
    open fun putData(key: T, data: Any?, token: Any? = null) = if (data == null) {
        observableMap[key]?.notifyObservers()
        observableMap.remove(key)
        liveDataMap[key]?.postValue(data)
        liveDataMap.remove(key)
        dataMap.remove(key)
    } else {
        if (active) {
            observableMap[key]?.notifyObservers(data)
        }
        liveDataMap[key]?.postValue(data)
        if (token != null && !dataMap.containsKey(key)) {
            tokenDataList.add(WithToken(key, token))
        }
        dataMap.put(key, data)
    }

    /**
     * 放入/删除数据
     *
     * @param key
     * @param data
     */
    @JvmOverloads
    open fun setData(key: T, data: Any?, token: Any? = null) = if (data == null) {
        dataMap.remove(key)
    } else {
        if (token != null && !dataMap.containsKey(key)) {
            tokenDataList.add(WithToken(key, token))
        }
        dataMap.put(key, data)
    }

    /**
     * 获取数据
     *
     * @param key
     * @param default
     */
    @JvmOverloads
    open fun getData(key: T, default: Any? = null) = dataMap[key] ?: default

    /**
     * 获取带类型数据
     *
     * @param key
     * @param default
     */
    @JvmOverloads
    open fun <T2 : Any> getTypedData(key: T, default: T2? = null) = dataMap[key] as? T2 ?: default

    /**
     * 获取 Observable ，这个 Observable 会对 key 对应的数据进行监听
     *
     * @param key
     */
    @JvmOverloads
    open fun getObservable(key: T, withNow: Boolean = false): Observable {
        if (!observableMap.containsKey(key)) {
            observableMap[key] = if (withNow) {
                WithNowObservable(if (dataMap.containsKey(key)) {
                    dataMap[key]
                } else {
                    WithNowObservable.NUL_OBJ
                })
            } else {
                Observable()
            }
        }
        return observableMap[key]!!
    }

    /**
     * 更新数据中心状态
     *
     * @param o
     * @param arg
     */
    override fun update(o: Observable?, arg: Any?) {
        if (arg is Boolean) {
            active = arg
        }
    }

    /**
     * 根据 token 清除数据/Observable/LiveData
     */
    open fun release(token: Any?) {
        if (token == null) {
            dataMap.clear()
            observableMap.clear()
            tokenDataList.clear()
        } else {
            tokenDataList.removeAll { tokenData ->
                if (tokenData.data == token) {
                    putData(tokenData.data, null)
                    return@removeAll true
                }
                false
            }
        }
    }

    // 因为 androidx.appcompat:appcompat 自带 liveData ，就还是添加 liveData 的代码吧，如果不依赖 liveData 的时候就可以去掉下面的代码了，
    // 上面对于 ViewModel 的继承以及 liveData.postValue 也可以去掉了 。如果不能用 viewModel ，但又必须要保存状态，可以重写 activity 的 onRetainNonConfigurationInstance
    protected val liveDataMap = ConcurrentHashMap<T, MutableLiveData<Any?>>()

    /**
     * 获取 LiveData
     *
     * @param key
     */
    open fun getLiveData(key: T): MutableLiveData<Any?> {
        if (!liveDataMap.containsKey(key)) {
            liveDataMap[key] = MutableLiveData()
        }
        return liveDataMap[key]!!
    }
}

@JvmOverloads
fun DataCenter<String>.putBundle(bundle: Bundle?, token: Any? = null) =
        if (bundle != null) bundle.keySet().forEach { putData(it, bundle.get(it), token) } else Unit

@JvmOverloads
fun DataCenter<String>.putIntent(intent: Intent?, token: Any? = null) {
    if (intent != null) {
        val extras = intent.extras
        extras?.keySet()?.forEach { putData(it, extras.get(it), token) }
    }
}

@JvmOverloads
fun DataCenter<Class<*>>.putData(value: Any, token: Any? = null) = putData(value::class.java, value, token)

@JvmOverloads
fun DataCenter<Class<*>>.setData(value: Any, token: Any? = null) = setData(value::class.java, value, token)
