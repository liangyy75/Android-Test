@file:Suppress("UNCHECKED_CAST")

package com.liang.example.basic_ktx

fun <T> plus(vararg arrays: Array<T>) = arrays.reduce { a1, a2 -> a1.plus(a2) }

fun <T> Array<T?>.toNotNullList(): List<T> = this.map { it!! }
fun <T> Array<T>.toNullableList(): List<T?> = this.map { it as T? }

fun <T> Array<T?>.toNotNullArray(): Array<T> = this as Array<T>
fun <T> Array<T>.toNullableArray(): Array<T?> = this as Array<T?>

fun <T> List<T?>.toNotNullList(): List<T> = this as List<T>
// fun <T> List<T>.toNullableList(): List<T?> = this

inline fun <reified T> List<T?>.toNotNullArray(): Array<T> = (this as List<T>).toTypedArray()
inline fun <reified T> List<T>.toNullableArray(): Array<T?> = (this as List<T?>).toTypedArray()
