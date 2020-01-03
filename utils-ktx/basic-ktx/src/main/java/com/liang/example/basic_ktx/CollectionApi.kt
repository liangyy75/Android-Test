package com.liang.example.basic_ktx

interface Filter<T> {
    fun execute(t: T): Boolean
}

fun <K, V> MutableMap<K, V>.removeIf(filter: Filter<Map.Entry<K, V>>): Boolean {
    var removed = false
    val iterator = entries.iterator()
    while (iterator.hasNext()) {
        if (filter.execute(iterator.next())) {
            iterator.remove()
            removed = true
        }
    }
    return removed
}
