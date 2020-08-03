package com.liang.example.basic_ktx

open class KVPair<K, V>(open var first: K, open var second: V)

fun Boolean.toInt() = if (this) 1 else 0

open class MutablePair<T1, T2>(open var first: T1, open var second: T2)
open class MutableTriple<T1, T2, T3>(open var first: T1, open var second: T2, open var third: T3)

open class Tuple4<T1, T2, T3, T4>(open var first: T1, open var second: T2, open var third: T3, open var fourth: T4)

/**
 * 用来帮助Enum的，并且方便扩展、删除
 */
object EnumHelper {
    private val map = mutableMapOf<String, Int>()

    operator fun get(name: String): Int {
        val result = map[name]?.plus(1) ?: 1
        map[name] = result
        return result
    }

    fun get2(name: String): Int {
        val result = map[name]?.times(2) ?: 1
        map[name] = result
        return result
    }
}
