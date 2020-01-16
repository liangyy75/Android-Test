@file:Suppress("MemberVisibilityCanBePrivate", "unused", "UNCHECKED_CAST")

package com.liang.example.xml_ktx

import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import kotlin.collections.HashMap

// https://github.com/zeux/pugixml.git
// https://github.com/leethomason/tinyxml2.git
// org.kxml2
// TODO: CDATA区 特殊字符 处理指令 文档声明
// TODO: xPath

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
    USE_BLANK;

    fun useBlank(): Boolean = this == USE_BLANK
}

interface EasyXmlValue<T : Any> : Cloneable {
    fun valueType(): XmlValueType
    fun value(): T?
    fun string(): String
    public override fun clone(): EasyXmlValue<T>
}

interface EasyXmlNode : Cloneable {
    fun tag(): String? = null
    fun elementType(): XmlNodeType
    fun string(): String
    public override fun clone(): EasyXmlNode = this
}

interface EasyXmlNode2 : EasyXmlNode {
    val children: MutableList<EasyXmlNode>?
    val siblings: List<EasyXmlNode>?
    val offSpring: List<EasyXmlNode>?
    val attributes: MutableList<EasyXmlAttribute>?
    var parent: EasyXmlNode2?
    var root: EasyXmlNode2?
    var strategy: XmlStrategy
    var depth: Int

    fun setRightDepth(depth: Int) {
        this.depth = depth
        val nextDepth = depth + 1
        children?.forEach {
            if (it is EasyXmlNode2) {
                it.depth = nextDepth
            }
        }
    }

    fun setRightStrategy(strategy: XmlStrategy) {
        this.strategy = strategy
        children?.forEach {
            if (it is EasyXmlNode2) {
                it.strategy = strategy
            }
            if (it is EasyXmlComment) {
                it.mStrategy = strategy
            }
        }
    }

    fun setRightRoot(root: EasyXmlNode2?) {
        val right = root ?: this
        this.root = right
        this.children?.forEach {
            if (it is EasyXmlNode2) {
                it.root = right
            }
        }
    }

    fun add(node: EasyXmlNode, index: Int = -1) {
        if (children != null) {
            if (index == -1) {
                children?.add(node)
            } else {
                children?.add(index, node)
            }
            if (node is EasyXmlNode2) {
                node.parent = this
                node.setRightRoot(this.root)
                node.setRightDepth(this.depth + 1)
            }
        }
    }

    fun remove(node: EasyXmlNode): Boolean {
        if (children != null) {
            val result = children!!.remove(node)
            if (result) {
                if (node is EasyXmlNode2) {
                    node.parent = null
                    node.setRightRoot(null)
                    node.setRightDepth(0)
                }
                return true
            }
        }
        return false
    }

    fun remove(index: Int): EasyXmlNode? {
        if (children != null) {
            val node = children!!.removeAt(index)
            if (node is EasyXmlNode2) {
                node.parent = null
                node.setRightRoot(null)
                node.setRightDepth(0)
            }
            return node
        }
        return null
    }

    fun clear() {
        children?.forEach {
            if (it is EasyXmlNode2) {
                it.parent = null
                it.setRightRoot(null)
                it.setRightDepth(0)
            }
        }
        children?.clear()
    }

    operator fun set(node: EasyXmlNode, index: Int) {
        if (children != null) {
            val old = children!!.set(index, node)
            if (old is EasyXmlNode2) {
                old.setRightRoot(null)
                old.parent = null
                old.setRightDepth(0)
            }
            if (node is EasyXmlNode2) {
                node.parent = this
                node.setRightRoot(this.root)
                node.setRightDepth(this.depth + 1)
            }
        }
    }

    operator fun get(index: Int): EasyXmlNode? = children?.get(index)
    fun indexOf(node: EasyXmlNode): Int = children?.indexOf(node) ?: -1
    operator fun contains(node: EasyXmlNode) = if (children != null) node in children!! else false

    val size: Int

    fun addAttr(attr: EasyXmlAttribute, index: Int = -1) = if (index == -1) {
        attributes?.add(attr)
        Unit
    } else attributes?.add(index, attr)

    fun removeAttr(attr: EasyXmlAttribute) = attributes?.remove(attr) ?: false
    fun removeAttr(index: Int) = attributes?.removeAt(index)
    fun clearAttr() = attributes?.clear() ?: Unit
    operator fun set(attr: EasyXmlAttribute, index: Int) = attributes?.set(index, attr)
    fun getAttr(index: Int): EasyXmlAttribute? = attributes?.get(index)
    fun indexOfAttr(attr: EasyXmlAttribute): Int = attributes?.indexOf(attr) ?: -1
    operator fun contains(attr: EasyXmlAttribute) = if (attributes != null) attr in attributes!! else false

    val attrSize: Int
    val xmlns: List<EasyXmlAttribute>?

    fun first() = get(0)
    fun firstAttr() = getAttr(0)
    fun last() = get(size - 1)
    fun lastAttr() = getAttr(attrSize - 1)

    operator fun iterator() = children?.iterator() ?: XML_EMPTY_CHILDREN.iterator()
    fun iteratorAttr() = attributes?.iterator() ?: XML_EMPTY_ATTRIBUTES.iterator()

    fun insertBefore(node: EasyXmlNode, before: EasyXmlNode) = add(node, indexOf(before))
    fun insertAfter(node: EasyXmlNode, after: EasyXmlNode) = add(node, indexOf(after) + 1)
    fun previousSibling() = parent?.get(parent!!.indexOf(this) - 1)
    fun nextSibling() = parent?.get(parent!!.indexOf(this) + 1)

    fun previousSibling(tag: String) = parent?.get(parent!!.indexOf(this) - 1)
    fun nextSibling(tag: String) = parent?.get(parent!!.indexOf(this) + 1)

    fun findElementsByAction(action: (EasyXmlNode) -> Boolean): List<EasyXmlNode>? {
        if (children.isNullOrEmpty()) {
            return null
        }
        val result = mutableListOf<EasyXmlNode>()
        children!!.forEach {
            if (action(it)) {
                result.add(it)
            }
            if (it is EasyXmlNode2) {
                val temp = it.findElementsByAction(action)
                if (!temp.isNullOrEmpty()) {
                    result.addAll(temp)
                }
            }
        }
        return result
    }

    fun findElementByAction(action: (EasyXmlNode) -> Boolean): EasyXmlNode? {
        children?.forEach {
            if (action(it)) {
                return it
            }
            if (it is EasyXmlNode2) {
                val temp = it.findElementByAction(action)
                if (temp != null) {
                    return it
                }
            }
        }
        return null
    }

    fun findElementByTagName(tag: String) = findElementByAction { it.tag() == tag }
    fun findElementByAttribute(attr: EasyXmlAttribute) = findElementByAction { it is EasyXmlNode2 && (if (attributes != null) attr in attributes!! else false) }
    fun findElementsByTagName(tag: String) = findElementsByAction { it.tag() == tag }
    fun findElementsByAttribute(attr: EasyXmlAttribute) = findElementsByAction { it is EasyXmlNode2 && (if (attributes != null) attr in attributes!! else false) }
}

abstract class EasyXmlValueAdapter<T : Any>(var mValue: T?, protected val mType: XmlValueType) : EasyXmlValue<T> {
    override fun valueType(): XmlValueType = mType
    override fun value(): T? = mValue
    override fun string(): String = "\"${mValue?.toString() ?: "null"}\""
    override fun clone(): EasyXmlValue<T> = this
}

open class EasyXmlNull : EasyXmlValueAdapter<Unit>(null, XmlValueType.NUL) {
    override fun clone(): EasyXmlValue<Unit> = EasyXmlNull()
}

open class EasyXmlBool(b: Boolean?) : EasyXmlValueAdapter<Boolean>(b, XmlValueType.BOOL) {
    override fun clone(): EasyXmlValue<Boolean> = EasyXmlBool(mValue)
}

open class EasyXmlNumber(n: Number?) : EasyXmlValueAdapter<Number>(n, XmlValueType.NUMBER) {
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

    override fun clone(): EasyXmlValue<Number> = EasyXmlNumber(mValue, mNumberType)

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
        mNumberType != XmlNumberType.UNKNOWN -> "\"${mValue?.toString() ?: "null"}\""
        else -> "\"null\""
    }

    open fun numberType(): XmlNumberType = mNumberType
    open fun charValue(): Char = (mValue as? Long)?.toChar() ?: '\u0000'
}

open class EasyXmlString(s: String?) : EasyXmlValueAdapter<String>(s, XmlValueType.STRING) {
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

    override fun clone(): EasyXmlValue<String> = EasyXmlString(mValue)
}

open class EasyXmlElement(open var mTag: String) : EasyXmlNode2 {
    override var strategy: XmlStrategy = XmlStrategy.SIMPLEST
    override var depth: Int = 0
    open var mChildren: MutableList<EasyXmlNode>? = mutableListOf()
    open var mAttributes: MutableList<EasyXmlAttribute>? = mutableListOf()

    override val children: MutableList<EasyXmlNode>?
        get() = mChildren
    override val siblings: List<EasyXmlNode>?
        get() = parent?.children
    override val offSpring: List<EasyXmlNode>?
        get() {
            if (mChildren.isNullOrEmpty()) {
                return null
            }
            val result = mutableListOf<EasyXmlNode>()
            mChildren!!.forEach {
                result.add(it)
                if (it is EasyXmlNode2) {
                    val temp = it.offSpring
                    if (!temp.isNullOrEmpty()) {
                        result.addAll(temp)
                    }
                }
            }
            return result
        }
    override val attributes: MutableList<EasyXmlAttribute>?
        get() = mAttributes
    override var parent: EasyXmlNode2? = null
    override var root: EasyXmlNode2? = null

    override val size: Int
        get() = children?.size ?: 0
    override val attrSize: Int
        get() = attributes?.size ?: 0
    override val xmlns: List<EasyXmlAttribute>?
        get() = attributes?.filter { it.isXmlns }

    override fun tag(): String = mTag
    override fun elementType(): XmlNodeType = XmlNodeType.ELEMENT

    override fun string(): String {
        val noChild = mChildren.isNullOrEmpty()
        val noAttr = mAttributes.isNullOrEmpty()
        val useBlank = strategy.useBlank()
        val depth1 = "\t".repeat(depth)
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
            } + " >\n" + mChildren!!.joinToString("\n") { depth2 + it.string() } + "\n${depth1}</$mTag>"
            else -> "<$mTag " + mAttributes!!.joinToString(" ") { it.string() } + ">" + mChildren!!.joinToString("") { it.string() } + "</$mTag>"
        }
    }
}

open class EasyXmlAttribute(open var mName: String, open var mValue: EasyXmlValue<*>, open var mNameSpace: String? = null) : EasyXmlNode {
    override fun elementType(): XmlNodeType = XmlNodeType.ATTRIBUTE
    override fun string(): String = """${if (mNameSpace != null) "$mNameSpace:" else ""}$mName=${mValue.string()}"""
    open val isXmlns: Boolean
        get() = mNameSpace == "xmlns"

    override fun hashCode(): Int =
            (if (mNameSpace != null) arrayOf(mName, mValue.string(), mNameSpace) else arrayOf(mName, mValue.string())).contentHashCode()

    override fun equals(other: Any?): Boolean = this === other || other is EasyXmlAttribute
            && other.mName == mName && other.mValue.string() == other.mValue.string() && other.mNameSpace == mNameSpace
}

open class EasyXmlText(open var mText: String? = null) : EasyXmlNode {
    override fun elementType(): XmlNodeType = XmlNodeType.TEXT
    override fun string(): String = mText ?: ""
    override fun hashCode(): Int = mText?.hashCode() ?: EMPTY_STRING.hashCode()
    override fun equals(other: Any?): Boolean = this === other || other is EasyXmlText && other.mText == mText
}

open class EasyXmlDocument(mTag: String, open val declaration: EasyXmlDeclaration = EasyXmlDeclaration()) : EasyXmlElement(mTag) {
    override var parent: EasyXmlNode2?
        get() = null
        set(value) {}
    override var root: EasyXmlNode2?
        get() = this
        set(value) {}
    override var depth: Int
        get() = 0
        set(value) {}

    override fun setRightStrategy(strategy: XmlStrategy) {
        declaration.mStrategy = strategy
        super.setRightStrategy(strategy)
    }

    override fun elementType(): XmlNodeType = XmlNodeType.DOCUMENT
    override fun string(): String = declaration.string() + (if (strategy.useBlank()) "\n" else "") + super.string()
}

open class EasyXmlDeclaration(
        open var version: String = DEFAULT_VERSION, open var encoding: String = DEFAULT_ENCODING,
        open var attributes: MutableList<EasyXmlAttribute>? = null
) : EasyXmlNode {
    open var mStrategy: XmlStrategy = XmlStrategy.SIMPLEST
    override fun elementType(): XmlNodeType = XmlNodeType.DECLARATION
    override fun string(): String = when (mStrategy) {
        XmlStrategy.SIMPLEST -> """<?xml version="$version" encoding="$encoding"${attributes?.joinToString("") { " " + it.string() } ?: ""}?>"""
        else -> "<?xml version=\"$version\" encoding=\"$encoding\"${attributes?.joinToString("") { "\n\t" + it.string() } ?: ""}?>"
    }
}

open class EasyXmlComment(open var mContent: String, open var mStrategy: XmlStrategy = XmlStrategy.SIMPLEST) : EasyXmlNode {
    override fun elementType(): XmlNodeType = XmlNodeType.COMMENT
    override fun string(): String = if (mStrategy == XmlStrategy.SIMPLEST) "<!--$mContent-->" else "<!-- $mContent -->"
    override fun hashCode(): Int = mContent.hashCode()
    override fun equals(other: Any?): Boolean = other === this || other is EasyXmlComment && other.mContent == mContent
}

const val EMPTY_STRING = ""
val XML_VALUE_NULL = EasyXmlNull()
val XML_VALUE_FALSE = EasyXmlBool(false)
val XML_VALUE_TRUE = EasyXmlBool(true)
val XML_VALUE_EMPTY_STRING = EasyXmlString(EMPTY_STRING)

val XML_EMPTY_CHILDREN: MutableList<EasyXmlNode> = mutableListOf()
val XML_EMPTY_ATTRIBUTES: MutableList<EasyXmlAttribute> = mutableListOf()

var DEFAULT_VERSION = "1.0"
var DEFAULT_ENCODING = "UTF-8"

open class EasyXmlParser(var strategy: XmlStyle) {
    open fun parseXml(xmlStr: String): EasyXmlDocument = TODO()
    open fun parseXmlOrThrow(xmlStr: String): EasyXmlDocument = TODO()
    open fun parseXmlOrNull(xmlStr: String): EasyXmlDocument? = TODO()

    open class EasyXmlPullParseTask(val xmlStr: String, val strategy: XmlStyle, val throwEx: Boolean = false) {
        protected val mLength = xmlStr.length
        protected var mIndex: Int = 0
        protected var mFailReason: String? = null
        protected var mType: Int? = START_DOCUMENT
        protected var mText: StringBuilder = StringBuilder()
        protected val mEntityMap = HashMap<String, String>()
        protected val mEscapeMap = HashMap<String, String>()
        protected val mTagPath = Stack<String>()
        protected val mAttributes = HashMap<String, String>()

        init {
            mEntityMap["amp"] = "&"
            mEntityMap["gt"] = ">"
            mEntityMap["lt"] = "<"
            mEntityMap["apos"] = "'"
            mEntityMap["quot"] = "\""

            mEscapeMap["\\b"] = "\b"
            mEscapeMap["\\f"] = "\u000C"
            mEscapeMap["\\n"] = "\n"
            mEscapeMap["\\r"] = "\r"
            mEscapeMap["\\b"] = "\b"
            mEscapeMap["\\\\"] = "\\"
        }

        // < 后面的内容
        protected fun parseStartTag(): Int? {
            val begin = mIndex
            var ch = getNextChar() ?: return null
            while (!Character.isLetterOrDigit(ch)) {  // TODO: 待确认
                ch = getNextChar() ?: return null
            }
            if (mIndex == begin) {
                return makeFail<Int>("start tag should not be empty!")
            }
            mTagPath.push(xmlStr.substring(begin, mIndex - 1))
            parseAttributes() ?: return null
            mType = START_TAG
            return mType
        }

        protected fun parseAttributes(): Int? {
            consumeWhiteSpaces()
            var ch = getNextChar() ?: return null
            mAttributes.clear()
            while (ch != '>') {
                if (ch == '"') {
                    ch = getNextChar() ?: return null
                    mText.clear()
                    while (ch != '"') {
                        mText.append(ch)
                        ch = getNextChar() ?: return null
                    }
                    val key = mText.toString()
                    ch = getNextChar() ?: return null
                    mText.clear()
                    while (ch != '"') {
                        mText.append(ch)
                        ch = getNextChar() ?: return null
                    }
                    mAttributes[key] = mText.toString()
                    mText.clear()
                } else {
                    return makeFail("attribute should be around by \"")
                }
                consumeWhiteSpaces()
                ch = getNextChar() ?: return null
            }
            return mType
        }

        // </ 后面的内容
        protected fun parseEndTag(): Int? {
            val begin = mIndex
            var ch = getNextChar() ?: return null
            while (ch != '>') {
                ch = getNextChar() ?: return null
            }
            if (mIndex == begin) {
                return makeFail<Int>("end tag should not be empty!")
            }
            val tag = xmlStr.substring(begin, mIndex - 1)
            if (mTagPath.empty()) {
                return makeFail<Int>("there must be corresponding start tag to the end tag: $tag")
            }
            if (tag != mTagPath.peek()) {
                return makeFail<Int>("end tag is wrong, now start tag is ${mTagPath.peek()}, but end tag is $tag")
            }
            mTagPath.pop()
            mType = END_TAG
            return mType
        }

        protected fun parseText(): Int? {
            if (mType != START_TAG || mType != END_TAG || mType != ENTITY_REF) {
                return makeFail<Int>("text only can be placed as element's child")
            }
            var ch = getNextChar() ?: return null
            mText.clear()
            while (ch != '>') {
                when (ch) {
                    '&' -> {
                        mType = ENTITY_REF
                        for ((fake, real) in mEntityMap) {
                            if (compareExpected(fake, true)) {
                                mText.append(real)
                                mIndex += fake.length
                                break
                            }
                        }
                    }
                    '\\' -> {
                        mType = TEXT
                        for ((fake, real) in mEscapeMap) {
                            if (compareExpected(fake, true)) {
                                mText.append(real)
                                mIndex += fake.length
                                break
                            }
                        }
                    }
                    else -> {
                        mType = TEXT
                        mText.append(ch)
                    }
                }
                ch = getNextChar() ?: return null
            }
            mType = TEXT
            return mType
        }

        // <?xml 后面的内容 ，注意，这里不管 <?xml 有多少个 attribute 了，也就是不限定与 version encoding standalone
        protected fun parseXmlDeclaration(): Int? {
            parseAttributes() ?: return null
            mType = XML_DECL
            return mType
        }

        protected fun parseEndDocument(): Int? {
            if (mType == START_DOCUMENT) {
                return makeFail<Int>("empty document!!!")
            }
            if (mType != END_TAG) {
                return makeFail<Int>("xml format error!!!")
            }
            mType = END_DOCUMENT
            return mType
        }

        protected fun getEventType(): Int? {
            consumeWhiteSpaces()
            if (mIndex >= mLength) {
                return parseEndDocument()
            }
            var ch = xmlStr[mIndex++]
            if (ch != '<') {
                return parseText()
            }

            ch = getNextChar() ?: return null
            val start: String
            val end: String
            when (ch) {
                '!' -> {
                    ch = getNextChar() ?: return null
                    when (ch) {
                        '[' -> {
                            mType = CDSECT
                            start = "CDATA["
                            end = "]]"
                        }
                        '-' -> {
                            mType = COMMENT
                            start = "-"
                            end = "--"
                        }
                        else -> {
                            mType = DOCDECL
                            start = "OCTYPE"
                            end = ""
                        }
                    }
                }
                '?' -> {
                    if (compareExpected("xml", true, consume = true)) {
                        return parseXmlDeclaration()
                    }
                    mType = PROCESSING_INSTRUCTION
                    start = ""
                    end = "?"
                }
                '/' -> return parseEndTag()
                else -> return parseStartTag()
            }

            if (start.isNotEmpty() && !compareExpected(start, false, consume = true)) {
                return makeFail<Int>("xml format is wrong, now type is $mType, and expect $start")
            }
            ch = getNextChar() ?: return null
            mText.clear()
            while (ch != '>') {
                mText.append(ch)
                ch = getNextChar() ?: return null
            }
            if (end.isNotEmpty()) {
                mIndex = mIndex - end.length - 1
                if (mIndex < 2) {
                    return makeFail<Int>("xml format is wrong, now type is $mType, and expect $end")
                }
                if (!compareExpected(end, false, consume = true)) {
                    return makeFail<Int>("xml format is wrong, now type is $mType, and expect $end")
                }
                mIndex++
            }
            return mType
        }

        protected fun compareExpected(expect: String, ignoreCase: Boolean, consume: Boolean = false): Boolean {
            val expectLen = expect.length
            if (mLength - mIndex < expectLen) {
                return false
            }
            val result = xmlStr.substring(mIndex, expectLen).equals(expect, ignoreCase)
            if (result && consume) {
                mIndex += expectLen
            }
            return result
        }

        protected fun getNextChar(): Char? {
            if (mIndex >= mLength) {
                return makeFail<Char>("read error! xml format is wrong!")
            }
            return xmlStr[mIndex++]
        }

        protected fun consumeWhiteSpaces() {
            while (mIndex < mLength && (xmlStr[mIndex] == ' ' || xmlStr[mIndex] == '\r' || xmlStr[mIndex] == '\n' || xmlStr[mIndex] == '\t')) {
                mIndex++
            }
        }

        protected fun <T> makeFail(reason: String, result: T? = null): T? {
            mFailReason = reason
            if (throwEx) {
                throw RuntimeException(this.toString())
            }
            return result
        }

        override fun toString(): String {
            val tagPath = StringBuilder()
            if (!mTagPath.empty()) {
                tagPath.append(mTagPath.pop())
            }
            while (!mTagPath.empty()) {
                tagPath.append(".").append(mTagPath.pop())
            }
            return "failReason: $mFailReason, index: $mIndex, length: $mLength, less: ${if (mIndex < mLength) xmlStr.substring(mIndex) else "none"}, " +
                    "type: $mType, text: $mText, tagPath: $tagPath"
        }
    }

    open class EasyXmlSaxParseTask {}

    open class EasyXmlDomParseTask {}

    companion object {
        /** pull */
        const val START_DOCUMENT = 0
        const val END_DOCUMENT = 1
        const val START_TAG = 2
        const val END_TAG = 3
        const val TEXT = 4
        const val COMMENT = 9  // <!-- xxx -->

        // TODO:
        const val CDSECT = 5  // <![CDATA[ xxx ]]>
        const val ENTITY_REF = 6  // &xxx，如 &gt;(>) &lt;(<) &amp;(&) &apos;(') &quot;(")
        const val IGNORABLE_WHITESPACE = 7  // 完全空白的 text / comment
        const val PROCESSING_INSTRUCTION = 8  // <?Yyy xxx ?>
        const val DOCDECL = 10  // <!DOCTYPE xxx>

        const val XML_DECL = 998  // <?xml version="1.0" encoding="UTF-8" standalone="true" ?>

        /** dom */

        /** sax */
    }
}
