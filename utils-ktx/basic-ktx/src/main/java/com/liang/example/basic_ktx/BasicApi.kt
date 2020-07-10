package com.liang.example.basic_ktx

open class KVPair<K, V>(open var first: K, open var second: V)

fun Boolean.toInt() = if (this) 1 else 0
