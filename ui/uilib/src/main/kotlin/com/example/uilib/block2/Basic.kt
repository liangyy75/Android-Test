package com.example.uilib.block2

interface Consumer<T> {
    fun accept(t: T)
}

interface Callable<V> {
    fun call(): V
}
