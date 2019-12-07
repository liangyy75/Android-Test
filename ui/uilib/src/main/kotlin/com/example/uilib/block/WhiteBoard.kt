package com.example.uilib.block

import android.content.Intent
import android.os.Bundle
import io.reactivex.Observable
import io.reactivex.annotations.Nullable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.util.concurrent.ConcurrentHashMap

class WhiteBoard<Key> {
    companion object {
        val NULL_OBJECT = Any()
    }

    private val dataMap = ConcurrentHashMap<Key, Any>()
    private val subjectMap = ConcurrentHashMap<Key, Subject<Any>>()

    fun notifyDataChanged(key: Key) = subjectMap[key]?.onNext(dataMap[key] ?: NULL_OBJECT)

    fun putData(key: Key, value: Any?) {
        putDataWithoutNotify(key, value)
        notifyDataChanged(key)
    }

    fun putDataWithoutNotify(key: Key, value: Any?) {
        if (value == null) {
            dataMap.remove(key)
        } else {
            dataMap[key] = value
        }
    }

    fun removeData(key: Key) {
        dataMap.remove(key)
        notifyDataChanged(key)
    }

    @Nullable
    fun getData(key: Key) = dataMap[key]

    @Suppress("UNCHECKED_CAST")
    fun <T2 : Any?> getObservable(key: Key, threadSafe: Boolean): Observable<T2> {
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
}

fun WhiteBoard<String>.putBundle(arguments: Bundle?) {
    return if (arguments != null) arguments.keySet().forEach { putData(it, arguments.get(it)) } else Unit
}

fun WhiteBoard<String>.putIntent(intent: Intent?): Unit? {
    return if (intent != null) {
        val extras = intent.extras
        if (extras != null) {
            extras.keySet().forEach { putData(it, extras.get(it)) }
        } else Unit
    } else Unit
}

fun <T : Any> WhiteBoard<Class<*>>.putData(value: T) = putData(value::class.java, value)
fun <T : Any> WhiteBoard<Class<*>>.putDataWithoutNotify(value: T) = putDataWithoutNotify(value::class.java, value)
