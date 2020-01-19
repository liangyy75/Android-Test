@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.liang.example.xml_inflater

import com.liang.example.xml_inflater.values.IAttrProcessorManager

var tag: String = "String-Inflater"
var debugFlag: Boolean = false
var throwFlag: Boolean = false

abstract class FormatValue<T>(protected open var value: T) {
    protected open var str: String? = null  // 修改值之后记得还原 str 为 null

    open fun string(): String {
        if (str == null) {
            str = innerString()
        }
        return str!!
    }

    open fun copy(): FormatValue<T> {
        val result = innerCopy()
        result.str = str
        return result
    }

    protected open fun innerString(): String = value.toString()
    protected abstract fun innerCopy(): FormatValue<T>
    open fun value(): T = value
}

open class XInflater(private val attrProcessorManager: IAttrProcessorManager) : IAttrProcessorManager by attrProcessorManager
