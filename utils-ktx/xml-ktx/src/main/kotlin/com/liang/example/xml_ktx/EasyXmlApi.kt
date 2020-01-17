@file:Suppress("MemberVisibilityCanBePrivate", "unused", "UNCHECKED_CAST")

package com.liang.example.xml_ktx

import android.annotation.SuppressLint
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.collections.HashMap

// https://github.com/zeux/pugixml.git
// https://github.com/leethomason/tinyxml2.git
// org.kxml2
// TODO: CDATA区 特殊字符 处理指令 文档声明
// TODO: xPath

enum class XmlValueType {
    // NUL,
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
                it.setRightStrategy(strategy)
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
    fun findElementByAttribute(attr: EasyXmlAttribute) =
            findElementByAction { it is EasyXmlNode2 && (if (attributes != null) attr in attributes!! else false) }

    fun findElementsByTagName(tag: String) = findElementsByAction { it.tag() == tag }
    fun findElementsByAttribute(attr: EasyXmlAttribute) =
            findElementsByAction { it is EasyXmlNode2 && (if (attributes != null) attr in attributes!! else false) }
}

abstract class EasyXmlValueAdapter<T : Any>(var mValue: T?, protected val mType: XmlValueType) : EasyXmlValue<T> {
    override fun valueType(): XmlValueType = mType
    override fun value(): T? = mValue
    override fun string(): String = "\"${mValue?.toString() ?: "null"}\""
    override fun clone(): EasyXmlValue<T> = this
}

// open class EasyXmlNull : EasyXmlValueAdapter<Unit>(null, XmlValueType.NUL) {
//     override fun clone(): EasyXmlValue<Unit> = EasyXmlNull()
// }

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
        XmlStrategy.SIMPLEST -> """<?xml version="$version" encoding="$encoding"${attributes?.joinToString("") { " " + it.string() }
                ?: ""}?>"""
        else -> "<?xml version=\"$version\" encoding=\"$encoding\"${attributes?.joinToString("") { "\n\t" + it.string() }
                ?: ""}?>"
    }
}

open class EasyXmlComment(open var mContent: String, open var mStrategy: XmlStrategy = XmlStrategy.SIMPLEST) : EasyXmlNode {
    override fun elementType(): XmlNodeType = XmlNodeType.COMMENT
    override fun string(): String = if (mStrategy == XmlStrategy.SIMPLEST) "<!--$mContent-->" else "<!-- $mContent -->"
    override fun hashCode(): Int = mContent.hashCode()
    override fun equals(other: Any?): Boolean = other === this || other is EasyXmlComment && other.mContent == mContent
}

const val EMPTY_STRING = ""
val XML_VALUE_FALSE = EasyXmlBool(false)
val XML_VALUE_TRUE = EasyXmlBool(true)
val XML_VALUE_EMPTY_STRING = EasyXmlString(EMPTY_STRING)

val XML_EMPTY_CHILDREN: MutableList<EasyXmlNode> = mutableListOf()
val XML_EMPTY_ATTRIBUTES: MutableList<EasyXmlAttribute> = mutableListOf()

var DEFAULT_VERSION = "1.0"
var DEFAULT_ENCODING = "UTF-8"

open class EasyXmlParser {
    open fun parseXml(xmlStr: String): EasyXmlDocument = EasyXmlDomParseTask().parseXml(xmlStr)
    open fun parseXmlOrThrow(xmlStr: String): EasyXmlDocument = EasyXmlDomParseTask().parseXmlOrThrow(xmlStr)
    open fun parseXmlOrNull(xmlStr: String): EasyXmlDocument? = EasyXmlDomParseTask().parseXmlOrNull(xmlStr)

    open class EasyXmlPullParseTask(val xmlStr: String, val throwEx: Boolean = false) {
        protected val mLength = xmlStr.length
        protected var mIndex = 0
        protected var mFailReason: String? = null
        protected var mType: Int? = START_DOCUMENT
        protected var mText = StringBuilder()
        protected var mTag: String? = null
        protected var mStartTagClosed: Boolean = false

        protected val mTagPath = mutableListOf<String>()
        protected val mAttributes = HashMap<String, String>()
        protected val mEntityMap = HashMap<String, String>()
        protected val mEscapeMap = HashMap<String, String>()
        protected val mEffectiveTagChars = mutableListOf<Char>()
        protected val mEffectiveAttrChars = mutableListOf<Char>()

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

            mEffectiveTagChars.add('-')
            mEffectiveTagChars.add('_')
            mEffectiveTagChars.add('.')

            mEffectiveAttrChars.addAll(mEffectiveTagChars)
            mEffectiveAttrChars.add(':')
        }

        // < 后面的内容
        protected fun parseStartTag(): Int? {
            val begin = mIndex - 1
            var ch = xmlStr[begin]
            while (Character.isLetterOrDigit(ch) || ch in mEffectiveTagChars) {  // TODO: 待确认
                ch = getNextChar() ?: return null
            }
            if (mIndex - 1 == begin) {
                return makeFail<Int>("start tag should not be empty! ch: $ch")
            }
            mTagPath.add(xmlStr.substring(begin, mIndex - 1))
            mTag = mTagPath.last()
            mType = START_TAG
            parseAttributes() ?: return null
            return mType
        }

        protected fun parseAttributes(): Int? {
            consumeWhiteSpaces()
            var ch = getNextChar() ?: return null
            mAttributes.clear()
            while (ch != '>') {
                if (Character.isLetterOrDigit(ch) || ch in mEffectiveAttrChars) {
                    mText.clear()
                    while (ch != '=') {
                        mText.append(ch)
                        ch = getNextChar() ?: return null
                    }
                    val key = mText.toString()
                    getNextChar() ?: return null
                    ch = getNextChar() ?: return null
                    mText.clear()
                    while (ch != '"') {
                        mText.append(ch)
                        ch = getNextChar() ?: return null
                    }
                    mAttributes[key] = mText.toString()
                    mText.clear()
                } else {
                    return makeFail("attribute's name should be letter or digit: $ch")
                }
                consumeWhiteSpaces()
                ch = getNextChar() ?: return null
                if (ch == '?' || ch == '/') {
                    if (mIndex >= mLength) {
                        return makeFail<Int>("read error! xml format is wrong!")
                    }
                    if (xmlStr[mIndex] == '>') {
                        if (ch == '/') {
                            mTagPath.removeAt(mTagPath.size - 1)
                            mStartTagClosed = true
                        }
                        mIndex++
                        break
                    }
                }
            }
            return mType
        }

        // </ 后面的内容
        protected fun parseEndTag(): Int? {
            if (mTagPath.isEmpty()) {
                return makeFail<Int>("xml format error!!!")
            }
            val begin = mIndex
            var ch = getNextChar() ?: return null
            while (ch != '>') {
                ch = getNextChar() ?: return null
            }
            if (mIndex == begin) {
                return makeFail<Int>("end tag should not be empty!")
            }
            val tag = xmlStr.substring(begin, mIndex - 1)
            if (mTagPath.isEmpty()) {
                return makeFail<Int>("there must be corresponding start tag to the end tag: $tag")
            }
            if (tag != mTagPath.last()) {
                return makeFail<Int>("end tag is wrong, now start tag is ${mTagPath.last()}, but end tag is $tag")
            }
            mTag = mTagPath.removeAt(mTagPath.size - 1)
            mType = END_TAG
            return mType
        }

        protected fun parseText(): Int? {
            if (mType != START_TAG || mType != END_TAG || mType != ENTITY_REF || mType != COMMENT || mTagPath.isEmpty()) {
                return makeFail<Int>("text only can be placed as element's child: $mType")
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
            consumeWhiteSpaces()
            mType = XML_DECL
            return mType
        }

        protected fun parseEndDocument(): Int? {
            if (mType == START_DOCUMENT) {
                return makeFail<Int>("empty document!!!")
            }
            if (mType != END_TAG || mType != COMMENT || mType == END_DOCUMENT || mType == TEXT) {
                return makeFail<Int>("xml format error!!!")
            }
            mType = END_DOCUMENT
            return mType
        }

        open fun getNext(): Int? {
            consumeWhiteSpaces()
            mTag = null
            mStartTagClosed = false
            if (mIndex >= mLength || mType == END_TAG && mTagPath.isEmpty()) {
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
            val result = xmlStr.substring(mIndex, expectLen + mIndex).equals(expect, ignoreCase)
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

        override fun toString(): String = "failReason: $mFailReason, index: $mIndex, length: $mLength, less:\n" +
                "${if (mIndex < mLength) xmlStr.substring(mIndex) else "none"},\n" +
                "type: ($mType: ${EVENT_NAMES[mType]}), text: $mText, tag: $mTag, startTagClosed: ${mStartTagClosed},\n" +
                "tagPath: ${mTagPath.joinToString(" --> ")},\n" +
                "attributes: ${showMap(mAttributes)},\n" +
                "entityMap: ${showMap(mEntityMap)}, escapeMap: ${showMap(mEscapeMap)},\n" +
                "effectiveTagChars: ${mEffectiveTagChars.joinToString()}, " +
                "effectiveAttrChars: ${mEffectiveAttrChars.joinToString()}"

        protected fun showMap(map: Map<String, String>) = map.toList().joinToString { "(${it.first} : ${it.second})" }

        open fun getEventType() = mType
        open fun getFailReason() = mFailReason
        open fun getText() = mText.toString()
        open fun getTagPath() = mTagPath
        open fun getTag() = mTag
        open fun getStartTagClosed() = mStartTagClosed
        open fun getAttribute(key: String) = mAttributes[key]
        open fun getAttributes() = mAttributes
        open fun attrsIterator() = mAttributes.iterator()
    }

    open class EasyXmlSaxParseTask {
        open fun run(xmlStr: String, throwEx: Boolean, handler: ContentHandler) {
            val task = EasyXmlPullParseTask(xmlStr, throwEx)
            var event = task.getEventType()
            while (event != END_DOCUMENT) {
                when (event) {
                    START_DOCUMENT -> handler.startDocument()
                    XML_DECL -> handler.xmlDeclaration(task.getAttributes(), task.getFailReason())
                    START_TAG -> handler.startTag(task.getTag(), task.getAttributes(), task.getStartTagClosed(), task.getFailReason())
                    TEXT -> handler.text(task.getText(), task.getFailReason())
                    END_TAG -> handler.endTag(task.getTag(), task.getFailReason())
                    END_DOCUMENT -> handler.endDocument(task.getFailReason())

                    COMMENT -> handler.comment(task.getText(), task.getFailReason())
                    CDSECT -> handler.cdata(task.getText(), task.getFailReason())
                    PROCESSING_INSTRUCTION -> handler.processingInstruction(task.getText(), task.getFailReason())
                    DOCDECL -> handler.docType(task.getText(), task.getFailReason())

                    else -> handler.unknown(event)
                }
                if (task.getNext() == null) {
                    handler.error(task.getFailReason())
                    return
                }
                event = task.getEventType()
            }
            handler.endDocument(task.getFailReason())
        }

        interface ContentHandler {
            fun event(eventType: Int?, failReason: String?)
            fun error(failReason: String?)
            fun unknown(eventType: Int?) = Unit

            fun startDocument()
            fun xmlDeclaration(attributes: HashMap<String, String>?, failReason: String?)
            fun startTag(tagName: String?, attributes: HashMap<String, String>?, tagClosed: Boolean, failReason: String?)
            fun text(text: String?, failReason: String?)
            fun endTag(tagName: String?, failReason: String?)
            fun endDocument(failReason: String?)

            fun comment(text: String?, failReason: String?) = Unit
            fun cdata(text: String?, failReason: String?) = Unit
            fun processingInstruction(text: String?, failReason: String?) = Unit
            fun docType(text: String?, failReason: String?) = Unit
        }
    }

    open class EasyXmlDomParseTask {
        protected var mResult: EasyXmlDocument? = null
        protected var mFailReason: String? = null

        open fun parseXml(xmlStr: String): EasyXmlDocument = parseXmlByPull(xmlStr, false)!!
        open fun parseXmlOrNull(xmlStr: String): EasyXmlDocument? = parseXmlByPull(xmlStr, false)
        open fun parseXmlOrThrow(xmlStr: String): EasyXmlDocument = parseXmlByPull(xmlStr, true)
                ?: throw RuntimeException(mFailReason ?: "unknown reason")

        open fun parseXmlByPull(xmlStr: String, throwEx: Boolean): EasyXmlDocument? {
            val task = EasyXmlPullParseTask(xmlStr, throwEx)
            var xmlDeclaration: EasyXmlDeclaration? = null
            var event = task.getEventType()
            var first = true
            var currentParent: EasyXmlNode2? = null
            while (event != END_DOCUMENT) {
                when (event) {
                    XML_DECL -> xmlDeclaration = parseXmlDeclaration(task) ?: return null
                    START_TAG -> {
                        if (first) {
                            xmlDeclaration = xmlDeclaration ?: EasyXmlDeclaration()
                            mResult = EasyXmlDocument(task.getTag()
                                    ?: return makeFail<EasyXmlDocument>("parse error: document tag is null!!!", task.throwEx), xmlDeclaration)
                            task.getAttributes().forEach {
                                mResult!!.addAttr(parseAttribute(it.toPair(), task.throwEx) ?: return null)
                            }
                            currentParent = mResult
                            first = false
                        } else {
                            val element = EasyXmlElement(task.getTag()
                                    ?: return makeFail<EasyXmlDocument>("parse error: start tag is null!!!", task.throwEx))
                            task.getAttributes().forEach { element.addAttr(parseAttribute(it.toPair(), task.throwEx) ?: return null) }
                            currentParent!!.add(element)
                            if (!task.getStartTagClosed()) {
                                currentParent = element
                            }
                        }
                    }
                    END_TAG -> {
                        if (task.getTag() != currentParent!!.tag()) {
                            val failReason = "parse error: end tag(${task.getTag()}), but start tag(${currentParent.tag()})"
                            return makeFail<EasyXmlDocument>(failReason, task.throwEx)
                        }
                        currentParent = currentParent.parent
                    }
                    TEXT -> currentParent!!.add(EasyXmlText(task.getText()))
                    else -> Unit /* 忽略 cdata / comment / doctype / processing instruction 等等 */
                }
                if (task.getNext() == null) {
                    mFailReason = task.getFailReason()
                    return null
                }
                event = task.getEventType()
            }
            return mResult
        }

        protected fun parseXmlDeclaration(task: EasyXmlPullParseTask): EasyXmlDeclaration? {
            val attributes = task.getAttributes()
            val version = attributes["version"] ?: DEFAULT_VERSION
            val encoding = attributes["encoding"] ?: DEFAULT_ENCODING
            attributes.remove("version")
            attributes.remove("encoding")
            val xmlAttributes: MutableList<EasyXmlAttribute>? = if (attributes.isEmpty()) {
                null
            } else {
                val temp = mutableListOf<EasyXmlAttribute>()
                for (attr in attributes) {
                    temp.add(parseAttribute(attr.toPair(), task.throwEx) ?: return null)
                }
                temp
            }
            return EasyXmlDeclaration(version, encoding, xmlAttributes)
        }

        protected fun parseAttribute(nameValue: Pair<String, String>, throwEx: Boolean): EasyXmlAttribute? {
            val name = nameValue.first
            // val value = nameValue.second
            // val xmlValue = if (value.isNotEmpty()) {
            //     when {
            //         value == "true" -> XML_VALUE_TRUE
            //         value == "false" -> XML_VALUE_FALSE
            //         value[0] == '-' || value[0] in allDigitCharRange -> {
            //             var decimalFlag = false
            //             for (ch in value) {
            //                 if (ch == '.' || ch == 'e' || ch == 'E') {
            //                     decimalFlag = true
            //                     break
            //                 }
            //             }
            //             try {
            //                 if (decimalFlag) {
            //                     val d = value.toDouble()
            //                     if (!d.isInfinite()) {
            //                         EasyXmlNumber(d)
            //                     } else {
            //                         EasyXmlNumber(value.toBigDecimal())
            //                     }
            //                 } else {
            //                     try {
            //                         EasyXmlNumber(value.toLong())
            //                     } catch (e: NumberFormatException) {
            //                         EasyXmlNumber(value.toBigInteger())
            //                     }
            //                 }
            //             } catch (e: NumberFormatException) {
            //                 EasyXmlString(value)
            //             }
            //         }
            //         else -> EasyXmlString(value)
            //     }
            // } else {
            //     EasyXmlString("")
            // }
            val xmlValue = EasyXmlString(nameValue.second)
            return when (val splitIndex = name.indexOf(':')) {
                -1 -> EasyXmlAttribute(name, xmlValue)
                else -> EasyXmlAttribute(name.substring(splitIndex + 1), xmlValue, name.substring(0, splitIndex))
            }
        }

        protected fun <T> makeFail(reason: String, throwEx: Boolean, result: T? = null): T? {
            mFailReason = reason
            if (throwEx) {
                throw RuntimeException(mFailReason)
            }
            return result
        }
    }

    companion object {
        @SuppressLint("UseSparseArrays")
        val EVENT_NAMES = HashMap<Int, String>()
        val allDigitCharRange = '0'..'9'

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

        /** init */

        init {
            EVENT_NAMES[START_DOCUMENT] = "START_DOCUMENT"
            EVENT_NAMES[END_DOCUMENT] = "END_DOCUMENT"
            EVENT_NAMES[START_TAG] = "START_TAG"
            EVENT_NAMES[END_TAG] = "END_TAG"
            EVENT_NAMES[TEXT] = "TEXT"
            EVENT_NAMES[COMMENT] = "COMMENT"
            EVENT_NAMES[CDSECT] = "CDSECT"
            EVENT_NAMES[ENTITY_REF] = "ENTITY_REF"
            EVENT_NAMES[IGNORABLE_WHITESPACE] = "IGNORABLE_WHITESPACE"
            EVENT_NAMES[PROCESSING_INSTRUCTION] = "PROCESSING_INSTRUCTION"
            EVENT_NAMES[DOCDECL] = "DOCDECL"
            EVENT_NAMES[XML_DECL] = "XML_DECL"
        }
    }
}

// TODO: EasyXmlParser -- inputStream 之类的
