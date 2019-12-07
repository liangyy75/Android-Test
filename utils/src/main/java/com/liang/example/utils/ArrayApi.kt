package com.liang.example.utils

fun <T> plus(vararg arrays: Array<T>) = arrays.reduce { a1, a2 -> a1.plus(a2) }
