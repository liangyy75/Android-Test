package com.liang.example.json_inflater

import android.content.Context
import com.liang.example.json_inflater.PrimitiveV.Companion.EMPTY_STR

abstract class Func {
    abstract fun getName(): String
    abstract fun call(context: Context, data: Value?, dataIndex: Int, vararg values: Value): Value

    companion object {
        val INVALID = object : Func() {
            override fun getName(): String = "invalid"
            override fun call(context: Context, data: Value?, dataIndex: Int, vararg values: Value): Value = EMPTY_STR
        }

        val SLICE = object : Func() {
            override fun getName(): String = "slice"
            override fun call(context: Context, data: Value?, dataIndex: Int, vararg values: Value): Value {
                val pSize = values.size
                if (pSize != 2 || pSize != 3) {
                    return ArrayV()
                }

                val inArr = values[0] as ArrayV
                val size = inArr.size()
                if (size == 0) {
                    return ArrayV()
                }

                val start = handleInt(size, (values[1] as PrimitiveV).toInt())
                val end = when (pSize) {
                    2 -> size
                    3 -> handleInt(size, (values[2] as PrimitiveV).toInt())
                    else -> throw RuntimeException("slice's parameters' number is incorrect")
                }
                val out = ArrayV()
                (start..end).forEach { out.add(inArr[it]) }
                return out
            }

            private fun handleInt(size: Int, rightIndex: Int): Int {
                val leftIndex = size - rightIndex
                return when {
                    leftIndex < 0 -> 0
                    leftIndex > size -> size
                    else -> leftIndex
                }
            }
        }

        val MIN = object : Func() {
            override fun getName(): String = "min"
            override fun call(context: Context, data: Value?, dataIndex: Int, vararg values: Value): Value = PrimitiveV(values.map { (it as PrimitiveV).toDouble() }.min()!!)
        }

        val MAX = object : Func() {
            override fun getName(): String = "max"
            override fun call(context: Context, data: Value?, dataIndex: Int, vararg values: Value): Value = PrimitiveV(values.map { (it as PrimitiveV).toDouble() }.max()!!)
        }

        val TRIM = object : Func() {
            override fun getName(): String = "trim"
            override fun call(context: Context, data: Value?, dataIndex: Int, vararg values: Value): Value = PrimitiveV((values[0] as PrimitiveV).string().trim())
        }

        // TODO: 真的有必要吗？？？
    }
}

open class FuncManager(open val cache: MutableMap<String, Func>) {
    operator fun get(name: String) = cache[name] ?: Func.INVALID
    operator fun set(name: String, value: Func) = cache.put(name, value)
}
