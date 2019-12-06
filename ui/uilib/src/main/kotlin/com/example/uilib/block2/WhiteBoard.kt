package com.example.uilib.block2

import android.content.Intent
import android.os.Bundle
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNCHECKED_CAST", "unused")
class WhiteBoard {
    private val data: MutableMap<String, Any> = ConcurrentHashMap()
    private val subjectMap: MutableMap<String, Subject<Any>> = ConcurrentHashMap()

    fun <T> getObservable(key: String): Observable<T> {
        val res: Subject<Any>
        if (subjectMap.containsKey(key)) {
            res = subjectMap[key]!!
        } else {
            res = PublishSubject.create<Any>()
            subjectMap[key] = res
        }
        return if (data[key] != null) {
            res.startWith(data[key])
        } else {
            res
        } as Observable<T>
    }

    fun notifyDataChanged(key: String?) {
        if (subjectMap.containsKey(key)) {
            subjectMap[key]!!.onNext(if (data[key] == null) NULL_OBJECT else data[key]!!)
        }
    }

    fun removeData(key: String?) {
        data.remove(key)
        notifyDataChanged(key)
    }

    fun getData(key: String?): Any? {
        return data[key]
    }

    fun putData(key: String, value: Any?) {
        if (value == null) {
            removeData(key)
        } else {
            data[key] = value
            notifyDataChanged(key)
        }
    }

    fun putDataWithoutNotify(key: String, value: Any?) {
        if (value == null) {
            data.remove(key)
        } else {
            data[key] = value
        }
    }

    fun putBundle(arguments: Bundle?) {
        if (arguments == null) {
            return
        }
        for (key in arguments.keySet()) {
            putData(key, arguments[key])
        }
    }

    fun putIntent(intent: Intent?) {
        if (intent == null) {
            return
        }
        putBundle(intent.extras)
    }

    private val classData: MutableMap<Class<*>, Any> = ConcurrentHashMap()
    private val classSubjectMap: MutableMap<Class<*>, Subject<Any>> = ConcurrentHashMap()

    fun <T> getData(tClass: Class<T>): T? {
        for (s in classData.keys) {
            if (tClass.isAssignableFrom(s)) {
                return classData[s] as T?
            }
        }
        return null
    }

    fun <T> getObservable(key: Class<T>): Observable<T> {
        val res: Subject<Any>
        if (classSubjectMap.containsKey(key)) {
            res = classSubjectMap[key]!!
        } else {
            res = PublishSubject.create<Any>()
            classSubjectMap[key] = res
        }
        return if (getData(key) != null) {
            res.startWith(getData(key))
        } else {
            res
        } as Observable<T>
    }

    fun <T : Any> putData(value: T?) {
        if (value != null) {
            classData[value.javaClass] = value
            notifyDataChanged(value.javaClass)
        }
    }

    fun <T : Any> putDataWithoutNotify(value: T?) {
        if (value != null) {
            classData[value.javaClass] = value
        }
    }

    fun removeData(tClass: Class<*>) {
        for (s in classData.keys) {
            if (tClass.isAssignableFrom(s)) {
                classData.remove(s)
            }
        }
        notifyDataChanged(tClass)
    }

    fun notifyDataChanged(key: Class<*>) {
        val o: Any? = getData(key)
        for (s in classSubjectMap.keys) {
            if (s.isAssignableFrom(key)) {
                classSubjectMap[s]!!.onNext(o ?: NULL_OBJECT)
            }
        }
    }

    companion object {
        var NULL_OBJECT = Any()
    }
}
