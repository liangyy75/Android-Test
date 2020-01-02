@file:Suppress("MemberVisibilityCanBePrivate", "UNCHECKED_CAST")

package com.example.uilib.block2

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import io.reactivex.Observable
import io.reactivex.annotations.Nullable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.util.concurrent.ConcurrentHashMap

/**
 * 1. viewModel，可以在activity中保留
 * 2. liveData
 * 3. rxJava -- subject
 * 4. set / get data ，一个简单的数据容器
 */
open class WhiteBoard<T> : ViewModel() {
    private val dataMap = ConcurrentHashMap<T, Any>()
    private val subjectMap = ConcurrentHashMap<T, Subject<Any>>()
    private val liveDataMap = ConcurrentHashMap<T, MutableLiveData<Any?>>()

    fun notifyDataChanged(key: T) {
        subjectMap[key]?.onNext(dataMap[key] ?: NULL_OBJECT)
        liveDataMap[key]?.postValue(dataMap[key])
    }

    fun putData(key: T, value: Any?) {
        setData(key, value)
        notifyDataChanged(key)
    }

    fun setData(key: T, value: Any?) {
        if (value == null) {
            dataMap.remove(key)
        } else {
            dataMap[key] = value
        }
    }

    fun removeData(key: T) {
        dataMap.remove(key)
        subjectMap[key]?.onComplete()
        subjectMap.remove(key)
        liveDataMap.remove(key)
    }

    @Nullable
    fun getData(key: T) = dataMap[key]

    // rxJava -- subject
    @Suppress("UNCHECKED_CAST")
    fun <T2 : Any?> getObservable(key: T, threadSafe: Boolean): Observable<T2> {
        if (!subjectMap.containsKey(key)) {
            subjectMap[key] = PublishSubject.create<Any>()
            if (threadSafe) {
                subjectMap[key] = subjectMap[key]!!.toSerialized()
            }
        }
        val res: Subject<Any> = subjectMap[key]!!
        return if (dataMap[key] != null) {
            res.startWith(dataMap[key])
        } else {
            res
        } as Observable<T2>
    }

    // liveData
    fun getLiveData(key: T): MutableLiveData<Any?> {
        if (!liveDataMap.containsKey(key)) {
            liveDataMap[key] = if (dataMap.containsKey(key)) {
                StrongLiveData<Any?>(dataMap[key])
            } else {
                StrongLiveData()
            }
        }
        return liveDataMap[key]!!
    }

    companion object {
        val NULL_OBJECT = Any()

        fun <T> create() = WhiteBoard<T>()

        fun <T> of(key: String, provider: StrongViewModelProvider): WhiteBoard<T> {
            var result = provider.get<WhiteBoard<T>>(key)
            if (result == null) {
                result = WhiteBoard()
                provider[key] = result
            }
            return result
        }

        inline fun <reified T> of(provider: StrongViewModelProvider): WhiteBoard<T> = of("WhiteBoard${T::class.java.canonicalName}", provider)

        inline fun <reified T> of(storeOwner: ViewModelStoreOwner): WhiteBoard<T> =
                of(StrongViewModelProvider(storeOwner))

        inline fun <reified T> of(storeOwner: ViewModelStoreOwner, factory: ViewModelProvider.Factory): WhiteBoard<T> =
                of(StrongViewModelProvider(storeOwner, factory))

        inline fun <reified T> of(store: ViewModelStore, factory: ViewModelProvider.Factory): WhiteBoard<T> =
                of(StrongViewModelProvider(store, factory))
    }
}

fun WhiteBoard<String>.putBundle(bundle: Bundle?) = if (bundle != null) bundle.keySet().forEach { putData(it, bundle.get(it)) } else Unit
fun WhiteBoard<String>.putIntent(intent: Intent?) {
    if (intent != null) {
        val extras = intent.extras
        extras?.keySet()?.forEach { putData(it, extras.get(it)) }
    }
}

fun <T : Any> WhiteBoard<Class<*>>.putData(value: T) = putData(value::class.java, value)
fun <T : Any> WhiteBoard<Class<*>>.setData(value: T) = setData(value::class.java, value)
