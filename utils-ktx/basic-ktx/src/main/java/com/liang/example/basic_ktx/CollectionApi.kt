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

@Suppress("LeakingThis")
open class KKMap<K, V> : MutableMap<K, V> {
    open val kvMap: MutableMap<K, V>
    open val vkMap: MutableMap<V, K>

    constructor() {
        kvMap = HashMap()
        vkMap = HashMap()
    }

    constructor(initialCapacity: Int, loadFactor: Float = 0.75f) {
        kvMap = HashMap(initialCapacity, loadFactor)
        vkMap = HashMap(initialCapacity, loadFactor)
    }

    constructor(kvMap: MutableMap<K, V>, vkMap: MutableMap<V, K>) {
        this.kvMap = kvMap
        this.vkMap = vkMap
    }

    constructor(vararg pairs: Pair<K, V>) {
        this.kvMap = HashMap(pairs.size, 0.75f)
        this.vkMap = HashMap(pairs.size, 0.75f)
        pairs.forEach { (k, v) ->
            this.kvMap[k] = v
            this.vkMap[v] = k
        }
    }

    override fun containsKey(key: K): Boolean = kvMap.containsKey(key)
    override fun containsValue(value: V): Boolean = vkMap.containsKey(value)
    override operator fun get(key: K): V? = kvMap[key]
    open fun getVK(key: V): K? = vkMap[key]
    override fun isEmpty(): Boolean = kvMap.isEmpty()

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = kvMap.entries
    override val keys: MutableSet<K>
        get() = kvMap.keys
    override val size: Int
        get() = kvMap.size
    override val values: MutableCollection<V>
        get() = kvMap.values

    open val vkEntries: MutableSet<MutableMap.MutableEntry<V, K>>
        get() = vkMap.entries
    open val vkKeys: MutableSet<V>
        get() = vkMap.keys
    open val vkSize: Int
        get() = vkMap.size
    open val vkValues: MutableCollection<K>
        get() = vkMap.values

    override fun clear() {
        kvMap.clear()
        vkMap.clear()
    }

    override fun put(key: K, value: V): V? {
        vkMap[value] = key
        return kvMap.put(key, value)
    }

    operator fun set(key: K, value: V) = put(key, value)

    override fun putAll(from: Map<out K, V>) = from.forEach { put(it.key, it.value) }

    override fun remove(key: K): V? {
        val result = kvMap.remove(key)
        result?.let { vkMap.remove(it) }
        return result
    }

    open fun reverse(): KKMap<V, K> {
        val result = KKMap<V, K>(kvMap.size)
        vkMap.forEach { result.put(it.key, it.value) }
        return result
    }
}
