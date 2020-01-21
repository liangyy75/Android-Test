@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.liang.example.xml_inflater

import com.liang.example.xml_ktx.EasyXmlElement
import com.liang.example.xml_ktx.EasyXmlParser
import com.liang.example.xml_ktx.EasyXmlText

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

open class Node(open var name: String) {
    open var parent: Node? = null
    open var text = EMPTY_TEXT
    open var children: MutableList<Node> = EMPTY_CHILDREN
    open var attributes: MutableList<NodeAttr> = EMPTY_ATTRIBUTES

    operator fun get(attrName: String) = attributes.find { it.name == attrName }?.value

    open class NodeAttr(open var name: String, open var value: String, open var nameSpace: String? = null)

    companion object {
        val EMPTY_CHILDREN = mutableListOf<Node>()
        val EMPTY_ATTRIBUTES = mutableListOf<NodeAttr>()
        const val EMPTY_TEXT = ""
    }
}

open class XmlTransformer {
    open val parser = EasyXmlParser.EasyXmlDomParseTask()

    open fun run(s: String): Node = transform(parser.parseXmlOrThrow(s))

    open fun transform(element: EasyXmlElement): Node {
        val result = Node(element.tag())
        result.attributes = element.attributes?.map { Node.NodeAttr(it.mName, it.mValue.string(), it.mNameSpace) }?.toMutableList()
                ?: Node.EMPTY_ATTRIBUTES
        result.children = element.children?.filterIsInstance(EasyXmlElement::class.java)?.map { transform(it) }?.toMutableList()
                ?: Node.EMPTY_CHILDREN
        result.text = element.children?.find { it is EasyXmlText }?.string() ?: Node.EMPTY_TEXT
        return result
    }
}

open class XInflater(private val attrProcessorManager: IAttrProcessorManager) : IAttrProcessorManager by attrProcessorManager
