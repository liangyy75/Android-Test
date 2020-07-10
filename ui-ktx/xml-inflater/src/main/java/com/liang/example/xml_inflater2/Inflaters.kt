package com.liang.example.xml_inflater2

import android.util.Log
import com.liang.example.basic_ktx.KKMap
import com.liang.example.json_ktx.JsonStyle
import com.liang.example.json_ktx.SimpleJsonArray
import com.liang.example.json_ktx.SimpleJsonObject
import com.liang.example.json_ktx.SimpleJsonParser
import com.liang.example.xml_ktx.EasyXmlElement
import com.liang.example.xml_ktx.EasyXmlParser
import com.liang.example.xml_ktx.EasyXmlText
import java.util.*

var tag: String = "Inflaters"
var debugFlag: Boolean = false
var throwFlag: Boolean = false

fun <T> makeFail(reason: String, value: T? = null): T? {
    if (throwFlag) {
        throw RuntimeException(reason)
    }
    if (debugFlag) {
        Log.w(tag, reason)
    }
    return value
}

open class AtomicType(var type: Int = 1) {
    val typeMaps = KKMap<String, Int>()

    fun inc(name: String): Int {
        typeMaps[name.toLowerCase(Locale.getDefault()).replace('_', '-')] = type
        return type++
    }

    companion object {
        val ATTR_TYPE = AtomicType()
        val RES_TYPE = AtomicType()
    }
}

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
    open var parent: Node? = null  // TODO
    open var text = EMPTY_TEXT
    open var children: MutableList<Node> = EMPTY_CHILDREN
    open var attributes: MutableList<NodeAttr> = EMPTY_ATTRIBUTES

    operator fun set(attr: Attr, value: String) = set(attr.name, value)

    operator fun set(attrName: String, value: String) {
        val attr = attributes.find { it.name == attrName }
        if (attr != null) {
            attr.value = value
        } else {
            attributes.add(Node.NodeAttr(attrName, value))
        }
    }

    operator fun get(attr: Attr): String? {
        val attrName = attr.name
        return attributes.find { it.name == attrName }?.value
    }

    operator fun contains(attr: Attr): Boolean {
        val attrName = attr.name
        return attributes.find { it.name == attrName } != null
    }

    operator fun get(attrName: String) = attributes.find { it.name == attrName }?.value
    operator fun contains(attrName: String) = attributes.find { it.name == attrName } != null

    operator fun get(index: Int) = children[index]
    open fun forEach(action: (Node) -> Unit) = children.forEach(action)

    open class NodeAttr(open var name: String, open var value: String, open var nameSpace: String? = null) {
        open fun copy() = NodeAttr(name, value, nameSpace)
    }

    open fun copy(): Node {
        val result = Node(name)
        result.text = text
        if (children.isNotEmpty()) {
            result.children = children.map { it.copy() }.toMutableList()
        }
        if (attributes.isNotEmpty()) {
            result.attributes = attributes.map { it.copy() }.toMutableList()
        }
        return result
    }

    companion object {
        val EMPTY_CHILDREN = mutableListOf<Node>()
        val EMPTY_ATTRIBUTES = mutableListOf<NodeAttr>()
        const val EMPTY_TEXT = ""
    }
}

open class XmlTransformer {
    open val parser = EasyXmlParser.EasyXmlDomParseTask()

    open fun run(s: String, name: String? = null): Node {
        val result = transform(parser.parseXmlOrThrow(s))
        if (name != null) {
            result.attributes.add(Node.NodeAttr(Attrs.FreeRes.name.name, name))
        }
        return result
    }

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

open class JsonTransformer {
    open val parser = SimpleJsonParser.SimpleJsonParseTask("", JsonStyle.COMMENTS, throwFlag)

    open fun run(s: String, name: String? = null): Node? {
        val result = transform(when {
            throwFlag -> parser.run(s) as SimpleJsonObject
            else -> parser.runOrNull(s) as? SimpleJsonObject ?: return null
        }) ?: return null
        if (name != null) {
            result.attributes.add(Node.NodeAttr(Attrs.FreeRes.name.name, name))
        }
        return result
    }

    open fun transform(element: SimpleJsonObject): Node? {
        val result = Node(element["tag"]?.string() ?: return makeFail("json element should have tag sub string element"))
        result.attributes = element.value()?.mapNotNull {
            val value = it.value
            if (value != null) {
                val name = it.key
                val index = name.indexOf(':')
                if (index == -1) {
                    Node.NodeAttr(name, value.string())
                } else {
                    Node.NodeAttr(name.substring(0, index), value.string(), name.substring(index + 1))
                }
            } else null
        }?.toMutableList()
                ?: Node.EMPTY_ATTRIBUTES
        result.children = (element["children"] as? SimpleJsonArray)?.value2()?.filterIsInstance<SimpleJsonObject>()
                ?.mapNotNull { transform(it) }?.toMutableList() ?: Node.EMPTY_CHILDREN
        result.text = element.get("text")?.string() ?: Node.EMPTY_TEXT
        return result
    }
}

/**
 * AtomicType
 * Attr
 * AttrProcessor
 * AttrProcessorManager
 * AttrValue
 */
