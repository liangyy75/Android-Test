@file:Suppress("MemberVisibilityCanBePrivate", "unused", "UNCHECKED_CAST")

package com.liang.example.xml_ktx

import android.annotation.SuppressLint
import java.math.BigDecimal
import java.math.BigInteger

// https://github.com/zeux/pugixml.git
// https://github.com/leethomason/tinyxml2.git
// org.kxml2
// TODO: CDATA区 特殊字符 处理指令 文档声明
// TODO: xpath

enum class XmlStyle {
    STANDARD,
    COMMENTS,
}

enum class XmlValueType {
    NUL,
    BOOL,
    NUMBER,
    STRING,
}

enum class XmlNodeType {
    DOCUMENT,  // 根结点
    DECLARATION,  // xml声明，即 <?xml version="1.0" encoding="UTF-8" ?>
    COMMENT,  // 注释
    ELEMENT,  // 元素
    ATTRIBUTE,  // 属性
    TEXT,  // 文本
}

enum class XmlNumberType {
    BYTE,
    SHORT,
    INT,
    LONG,
    BIG_INTEGER,
    FLOAT,
    DOUBLE,
    BIG_DECIMAL,
    CHAR,
    UNKNOWN,
}

enum class XmlStrategy {
    SIMPLEST,
    USE_NULL,
    USE_BLANK,
    USE_NULL_AND_BLANK;

    fun useNull(): Boolean = this == USE_NULL_AND_BLANK || this == USE_NULL
    fun useBlank(): Boolean = this == USE_NULL_AND_BLANK || this == USE_BLANK
}

interface SimpleXmlValue<T : Any> : Cloneable {
    fun valueType(): XmlValueType
    fun value(): T?
    fun string(): String
    public override fun clone(): SimpleXmlValue<T>
}

interface SimpleXmlNode : Cloneable {
    fun tag(): String? = null
    fun elementType(): XmlNodeType
    fun string(): String
    public override fun clone(): SimpleXmlNode = this
}

interface SimpleXmlNode2 : SimpleXmlNode {
    val children: MutableList<SimpleXmlNode>?
    val siblings: List<SimpleXmlNode>?
    val offSpring: List<SimpleXmlNode>?
    val attributes: MutableList<SimpleXmlAttribute>?
    val parent: SimpleXmlNode2?
    val root: SimpleXmlNode2?

    fun add(node: SimpleXmlNode, index: Int = -1) = if (index == -1) {
        children?.add(node)
        Unit
    } else children?.add(index, node) ?: Unit

    fun remove(node: SimpleXmlNode) = children?.remove(node) ?: false
    fun remove(index: Int) = children?.removeAt(index)
    fun clear() = children?.clear() ?: Unit
    fun set(node: SimpleXmlNode, index: Int) = children?.set(index, node)
    fun get(index: Int): SimpleXmlNode? = children?.get(index)
    fun indexOf(node: SimpleXmlNode): Int = children?.indexOf(node) ?: -1
    val size: Int

    fun addAttr(attr: SimpleXmlAttribute, index: Int = -1) = if (index == -1) {
        attributes?.add(attr)
        Unit
    } else attributes?.add(index, attr)

    fun removeAttr(attr: SimpleXmlAttribute) = attributes?.remove(attr) ?: false
    fun removeAttr(index: Int) = attributes?.removeAt(index)
    fun clearAttr() = attributes?.clear() ?: Unit
    fun setAttr(attr: SimpleXmlAttribute, index: Int) = attributes?.set(index, attr)
    fun getAttr(index: Int): SimpleXmlAttribute? = attributes?.get(index)
    fun indexOfAttr(attr: SimpleXmlAttribute): Int = attributes?.indexOf(attr) ?: -1
    val attrSize: Int
    val xmlns: List<SimpleXmlAttribute>?

    fun first() = get(0)
    fun firstAttr() = getAttr(0)
    fun last() = get(size - 1)
    fun lastAttr() = getAttr(attrSize - 1)

    operator fun iterator() = children?.iterator() ?: XML_EMPTY_CHILDREN.iterator()
    fun iteratorAttr() = attributes?.iterator() ?: XML_EMPTY_ATTRIBUTES.iterator()

    fun insertBefore(node: SimpleXmlNode, before: SimpleXmlNode) = add(node, indexOf(before))
    fun insertAfter(node: SimpleXmlNode, after: SimpleXmlNode) = add(node, indexOf(after) + 1)
    fun previousSibling() = parent?.get(parent!!.indexOf(this) - 1)
    fun nextSibling() = parent?.get(parent!!.indexOf(this) + 1)

    fun previousSibling(tag: String) = parent?.get(parent!!.indexOf(this) - 1)
    fun nextSibling(tag: String) = parent?.get(parent!!.indexOf(this) + 1)

    fun findElementsByAction(action: (SimpleXmlNode) -> Boolean): List<SimpleXmlNode>? {
        if (children.isNullOrEmpty()) {
            return null
        }
        val result = mutableListOf<SimpleXmlNode>()
        children!!.forEach {
            if (action(it)) {
                result.add(it)
            }
            if (it is SimpleXmlNode2) {
                val temp = it.findElementsByAction(action)
                if (!temp.isNullOrEmpty()) {
                    result.addAll(temp)
                }
            }
        }
        return result
    }

    fun findElementByAction(action: (SimpleXmlNode) -> Boolean): SimpleXmlNode? {
        children?.forEach {
            if (action(it)) {
                return it
            }
            if (it is SimpleXmlNode2) {
                val temp = it.findElementByAction(action)
                if (temp != null) {
                    return it
                }
            }
        }
        return null
    }

    fun findElementByTagName(tag: String) = findElementByAction { it.tag() == tag }
    @SuppressLint("CI_ByteDanceKotlinRules_List_Contains_Not_Allow")
    fun findElementByAttribute(attr: SimpleXmlAttribute) = findElementByAction { it is SimpleXmlNode2 && it.attributes?.contains(attr) ?: false }

    fun findElementsByTagName(tag: String) = findElementsByAction { it.tag() == tag }
    @SuppressLint("CI_ByteDanceKotlinRules_List_Contains_Not_Allow")
    fun findElementsByAttribute(attr: SimpleXmlAttribute) = findElementsByAction { it is SimpleXmlNode2 && it.attributes?.contains(attr) ?: false }
}

abstract class SimpleXmlValueAdapter<T : Any>(var mValue: T?, protected val mType: XmlValueType) : SimpleXmlValue<T> {
    override fun valueType(): XmlValueType = mType
    override fun value(): T? = mValue
    override fun string(): String = mValue?.toString() ?: "null"
    override fun clone(): SimpleXmlValue<T> = this
}

open class SimpleXmlNull : SimpleXmlValueAdapter<Unit>(null, XmlValueType.NUL) {
    override fun clone(): SimpleXmlValue<Unit> = SimpleXmlNull()
}

open class SimpleXmlBool(b: Boolean?) : SimpleXmlValueAdapter<Boolean>(b, XmlValueType.BOOL) {
    override fun clone(): SimpleXmlValue<Boolean> = SimpleXmlBool(mValue)
}

open class SimpleXmlNumber(n: Number?) : SimpleXmlValueAdapter<Number>(n, XmlValueType.NUMBER) {
    protected var mNumberType: XmlNumberType = XmlNumberType.UNKNOWN

    constructor(n: Byte?) : this(n as? Number) {
        mNumberType = XmlNumberType.BYTE
    }

    constructor(n: Short?) : this(n as? Number) {
        mNumberType = XmlNumberType.SHORT
    }

    constructor(n: Int?) : this(n as? Number) {
        mNumberType = XmlNumberType.INT
    }

    constructor(n: Long?) : this(n as? Number) {
        mNumberType = XmlNumberType.LONG
    }

    constructor(n: Float?) : this(n as? Number) {
        mNumberType = XmlNumberType.FLOAT
    }

    constructor(n: Double?) : this(n as? Number) {
        mNumberType = XmlNumberType.DOUBLE
    }

    constructor(n: Char?) : this(n?.toLong() as? Number) {
        mNumberType = XmlNumberType.CHAR
    }

    constructor(n: BigInteger?) : this(n as? Number) {
        mNumberType = XmlNumberType.BIG_INTEGER
    }

    constructor(n: BigDecimal?) : this(n as? Number) {
        mNumberType = XmlNumberType.BIG_DECIMAL
    }

    constructor(n: Number?, nt: XmlNumberType) : this(n) {
        mNumberType = nt
    }

    override fun clone(): SimpleXmlValue<Number> = SimpleXmlNumber(mValue, mNumberType)

    override fun value(): Number? = when {
        mValue == null -> null
        mNumberType == XmlNumberType.CHAR -> mValue!!
        mNumberType == XmlNumberType.UNKNOWN -> mValue!!
        mNumberType == XmlNumberType.BYTE -> mValue!!.toByte()
        mNumberType == XmlNumberType.SHORT -> mValue!!.toShort()
        mNumberType == XmlNumberType.INT -> mValue!!.toInt()
        mNumberType == XmlNumberType.LONG -> mValue!!.toLong()
        mNumberType == XmlNumberType.BIG_INTEGER -> mValue!! as BigInteger
        mNumberType == XmlNumberType.FLOAT -> mValue!!.toFloat()
        mNumberType == XmlNumberType.DOUBLE -> mValue!!.toDouble()
        mNumberType == XmlNumberType.BIG_DECIMAL -> mValue!! as BigDecimal
        else -> null
    }

    override fun string(): String = when {
        mNumberType != XmlNumberType.UNKNOWN -> mValue?.toString() ?: "null"
        else -> "null"
    }

    open fun numberType(): XmlNumberType = mNumberType
    open fun charValue(): Char = (mValue as? Long)?.toChar() ?: '\u0000'
}

open class SimpleXmlString(s: String?) : SimpleXmlValueAdapter<String>(s, XmlValueType.STRING) {
    override fun string(): String {
        val tempValue = mValue ?: return "null"
        val resultBuilder = StringBuilder("\"")
        var i = 0
        val length = tempValue.length
        while (i < length) {
            val ch = tempValue[i]
            // val chInt = ch.toInt()
            resultBuilder.append(
                    when {
                        ch == '\\' -> "\\\\"
                        ch == '"' -> "\\\""
                        ch == '\b' -> "\\b"
                        ch == '\u000C' -> "\\f"
                        ch == '\n' -> "\\n"
                        ch == '\r' -> "\\r"
                        ch == '\t' -> "\\t"
                        else -> ch
                    }
            )
            i++
        }
        return resultBuilder.append("\"").toString()
    }

    override fun clone(): SimpleXmlValue<String> = SimpleXmlString(mValue)
}

open class SimpleXmlElement(open var mTag: String) : SimpleXmlNode2 {
    open var mStrategy: XmlStrategy = XmlStrategy.SIMPLEST
    open var mDepth: Int = 0
    open var mParent: SimpleXmlNode2? = null
    open var mRoot: SimpleXmlNode2? = null
    open var mChildren: MutableList<SimpleXmlNode>? = null
    open var mAttributes: MutableList<SimpleXmlAttribute>? = null

    open fun setRightDepth(depth: Int) {
        mDepth = depth
        val nextDepth = depth + 1
    }

    open fun setRightStrategy(strategy: XmlStrategy) {
        mStrategy = strategy
    }

    override val children: MutableList<SimpleXmlNode>?
        get() = mChildren
    override val siblings: List<SimpleXmlNode>?
        get() = parent?.children
    override val offSpring: List<SimpleXmlNode>?
        get() {
            if (mChildren.isNullOrEmpty()) {
                return null
            }
            val result = mutableListOf<SimpleXmlNode>()
            mChildren!!.forEach {
                result.add(it)
                if (it is SimpleXmlNode2) {
                    val temp = it.offSpring
                    if (!temp.isNullOrEmpty()) {
                        result.addAll(temp)
                    }
                }
            }
            return result
        }
    override val attributes: MutableList<SimpleXmlAttribute>?
        get() = mAttributes
    override val parent: SimpleXmlNode2?
        get() = mParent
    override val root: SimpleXmlNode2?
        get() = mRoot

    override val size: Int
        get() = children?.size ?: 0
    override val attrSize: Int
        get() = attributes?.size ?: 0
    override val xmlns: List<SimpleXmlAttribute>?
        get() = attributes?.filter { it.isXmlns }

    override fun tag(): String = mTag
    override fun elementType(): XmlNodeType = XmlNodeType.ELEMENT

    override fun string(): String {
        val noChild = mChildren.isNullOrEmpty()
        val noAttr = mAttributes.isNullOrEmpty()
        val useBlank = mStrategy.useBlank()
        val depth1 = "\t".repeat(mDepth)
        val depth2 = "$depth1\t"
        return when {
            noChild && noAttr && useBlank -> "<$mTag />"
            noChild && noAttr && !useBlank -> "<$mTag/>"
            !noChild && noAttr && useBlank -> "<$mTag>\n" + mChildren!!.joinToString("\n") { depth2 + it.string() } + "\n${depth1}</$mTag>"
            !noChild && noAttr && !useBlank -> "<$mTag>" + mChildren!!.joinToString("") { it.string() } + "</$mTag>"
            noChild && !noAttr && useBlank -> "<$mTag\n" + mAttributes!!.joinToString("\n") { depth2 + it.string() } + " />"
            noChild && !noAttr && !useBlank -> "<$mTag " + mAttributes!!.joinToString(" ") { it.string() } + "/>"
            !noChild && !noAttr && useBlank -> "<$mTag\n" + mAttributes!!.joinToString("\n") {
                depth2 + it.string()
            } + " />\n" + mChildren!!.joinToString("\n") { depth2 + it.string() } + "\n${depth1}</$mTag>"
            else -> "<$mTag " + mAttributes!!.joinToString(" ") { it.string() } + ">" + "</$mTag>"
        }
    }
}

open class SimpleXmlAttribute(open var mName: String, open var mValue: SimpleXmlValue<*>, open var mNameSpace: String? = null) : SimpleXmlNode {
    override fun elementType(): XmlNodeType = XmlNodeType.ATTRIBUTE
    override fun string(): String = """${mNameSpace ?: ""}$mName="$mValue""""
    open val isXmlns: Boolean
        get() = mNameSpace == "xmlns"

    override fun hashCode(): Int = (if (mNameSpace != null) arrayOf(mName, mValue, mNameSpace) else arrayOf(mName, mValue)).contentHashCode()
    override fun equals(other: Any?): Boolean = this === other || other is SimpleXmlAttribute
            && other.mName == mName && other.mValue == other.mValue && other.mNameSpace == mNameSpace
}

open class SimpleXmlText(open var mText: String? = null) : SimpleXmlNode {
    override fun elementType(): XmlNodeType = XmlNodeType.TEXT
    override fun string(): String = mText ?: ""
    override fun hashCode(): Int = mText?.hashCode() ?: EMPTY_STRING.hashCode()
    override fun equals(other: Any?): Boolean = this === other || other is SimpleXmlText && other.mText == mText
}

open class SimpleXmlDocument(mTag: String, open val declaration: SimpleXmlDeclaration) : SimpleXmlElement(mTag) {
    override var parent: SimpleXmlNode2?
        get() = null
        set(value) {}
    override var root: SimpleXmlNode2?
        get() = this
        set(value) {}
    override var mDepth: Int
        get() = 0
        set(value) {}

    override fun setRightStrategy(strategy: XmlStrategy) {
        declaration.mStrategy = strategy
        super.setRightStrategy(strategy)
    }

    override fun elementType(): XmlNodeType = XmlNodeType.DOCUMENT
    override fun string(): String = declaration.string() + if (mStrategy.useBlank()) "\n" else "" + super.string()
}

open class SimpleXmlDeclaration(
        open var version: String, open var encoding: String = "UTF-8",
        open var attributes: MutableList<SimpleXmlAttribute>? = null
) : SimpleXmlNode {
    open var mStrategy: XmlStrategy = XmlStrategy.SIMPLEST
    override fun elementType(): XmlNodeType = XmlNodeType.DECLARATION
    override fun string(): String = when (mStrategy) {
        XmlStrategy.SIMPLEST -> """<?xml version="$version" encoding="$encoding" ${attributes?.joinToString(" ") { it.string() } ?: ""} ?>"""
        else -> "<?xml version=\"$version\" encoding=\"$encoding\"\n${attributes?.joinToString("\n") { "\t" + it.string() } ?: ""} ?>"
    }
}

open class SimpleXmlComment(open var mContent: String, open var mStrategy: XmlStrategy = XmlStrategy.SIMPLEST) : SimpleXmlNode {
    override fun elementType(): XmlNodeType = XmlNodeType.COMMENT
    override fun string(): String = if (mStrategy == XmlStrategy.SIMPLEST) "<!--$mContent-->" else "<!-- $mContent -->"
    override fun hashCode(): Int = mContent.hashCode()
    override fun equals(other: Any?): Boolean = other === this || other is SimpleXmlComment && other.mContent == mContent
}

const val EMPTY_STRING = ""
val XML_VALUE_NULL = SimpleXmlNull()
val XML_VALUE_FALSE = SimpleXmlBool(false)
val XML_VALUE_TRUE = SimpleXmlBool(true)
val XML_VALUE_EMPTY_STRING = SimpleXmlString(EMPTY_STRING)

val XML_EMPTY_CHILDREN: MutableList<SimpleXmlNode> = mutableListOf()
val XML_EMPTY_ATTRIBUTES: MutableList<SimpleXmlAttribute> = mutableListOf()
