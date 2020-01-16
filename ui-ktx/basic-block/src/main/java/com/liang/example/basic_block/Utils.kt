package com.liang.example.basic_block

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import java.lang.reflect.Method

/**
 * mutableMap的removeIf
 *
 * @param map
 * @param filter
 */
fun <K, V> removeIf(map: MutableMap<K, V>, filter: (Map.Entry<K, V>) -> Boolean): MutableList<Pair<K, V>> {
    val iterator = map.entries.iterator()
    val result = mutableListOf<Pair<K, V>>()
    while (iterator.hasNext()) {
        val entry = iterator.next()
        if (filter(entry)) {
            iterator.remove()
            result.add(entry.key to entry.value)
        }
    }
    return result
}

open class WithToken<T>(var data: T, val token: Any)

val viewModelStoreGet: Method = ViewModelStore::class.java.getDeclaredMethod("get", String::class.java).apply { isAccessible = true }
val viewModelStorePut: Method = ViewModelStore::class.java.getDeclaredMethod("put", String::class.java, ViewModel::class.java)
        .apply { isAccessible = true }
