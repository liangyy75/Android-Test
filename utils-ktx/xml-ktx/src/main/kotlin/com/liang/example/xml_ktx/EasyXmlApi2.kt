package com.liang.example.xml_ktx

import android.annotation.SuppressLint
import com.liang.example.xml_ktx.EasyXmlPullParser.Companion.END_DOCUMENT
import com.liang.example.xml_ktx.EasyXmlPullParser.Companion.END_TAG
import com.liang.example.xml_ktx.EasyXmlPullParser.Companion.START_TAG
import com.liang.example.xml_ktx.EasyXmlPullParser.Companion.TEXT
import com.liang.example.xml_ktx.EasyXmlPullParser.Companion.XML_DECL

// TODO: 去掉基本类型的 EasyXmlValue 吧，只保留 EasyXmlElement 就好了， EasyXmlAttribute 也可以用 pair 代替了

open class EasyXmlElement2(
        open var tag: String,
        open var attributes: MutableList<Triple<String, String, String?>>? = null,  // name, value, nameSpace
        open var children: MutableList<EasyXmlChild>? = null
) {
    open var parent: EasyXmlElement2? = null

    open fun addChild(child: EasyXmlChild) {
        when (children) {
            null -> children = mutableListOf(child)
            else -> children!!.add(child)
        }
        child.element?.parent = this
    }
}

open class EasyXmlChild(open var element: EasyXmlElement2? = null, open var text: String? = null) {
    companion object {
        fun element(e: EasyXmlElement2) = EasyXmlChild(e)
        fun text(t: String) = EasyXmlChild(null, t)  // t的预处理——将<>换成 &lt; &gt;
    }
}

open class EasyXmlApi2 {
    open fun toXml(node: EasyXmlElement2): String {
        val tag = node.tag
        val attributes = node.attributes
        val children = node.children
        return "<$tag" + when {
            attributes.isNullOrEmpty() -> ""
            else -> (attributes.joinToString("") {
                when (it.third) {
                    null -> " ${it.first}=\"${it.second}\""
                    else -> " ${it.third}:${it.first}=\"${it.second}\""
                }
            })
        } + when {
            children.isNullOrEmpty() -> "/>"
            else -> ">" + children.joinToString {
                when {
                    it.element != null -> toXml(it.element!!)
                    else -> it.text!!
                }
            } + "</$tag>"
        }
    }
}

open class EasyXmlPullParser(var xmlStr: String, var throwEx: Boolean = false) {
    protected var mLength = xmlStr.length
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

    open fun reset(xmlStr: String, throwEx: Boolean = false) {
        this.xmlStr = xmlStr
        this.throwEx = throwEx
        this.mLength = xmlStr.length
        this.mIndex = 0
        this.mFailReason = null
        this.mType = START_DOCUMENT
        this.mText.clear()
        this.mTag = null
        this.mStartTagClosed = false
        this.mTagPath.clear()
        this.mAttributes.clear()
    }

    // < 后面的内容
    protected fun parseStartTag(): Int? {
        val begin = mIndex - 1
        var ch = xmlStr[begin]
        while (Character.isLetterOrDigit(ch) || ch in mEffectiveTagChars) {  // TODO: 待确认
            ch = getNextChar() ?: return null
        }
        mIndex--
        if (mIndex == begin) {
            return makeFail<Int>("start tag should not be empty! ch: $ch")
        }
        mTagPath.add(xmlStr.substring(begin, mIndex))
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
                getNextChar() ?: return null
                ch = getNextChar() ?: return null
                val key = mText.toString()
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
        if (mType != START_TAG && mType != END_TAG && mType != ENTITY_REF && mType != COMMENT || mTagPath.isEmpty()) {
            return makeFail<Int>("text only can be placed as element's child: ($mType, ${EVENT_NAMES[mType]}), ch: ${xmlStr[mIndex]}")
        }
        var ch = getNextChar() ?: return null
        mText.clear()
        while (ch != '<') {
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
        mIndex--
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
        if (mType != END_TAG && mType != COMMENT && mType == END_DOCUMENT && mType == TEXT) {
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
        var ch = xmlStr[mIndex]
        if (ch != '<') {
            return parseText()
        }

        mIndex++
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
            if (mIndex < 2 || !compareExpected(end, false, consume = true)) {
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

open class EasyXmlDomParser {
    protected var mResult: EasyXmlElement2? = null
    protected var mFailReason: String? = null

    open fun parseXml(xmlStr: String): EasyXmlElement2 = parseXmlByPull(xmlStr, false)!!
    open fun parseXmlOrNull(xmlStr: String): EasyXmlElement2? = parseXmlByPull(xmlStr, false)
    open fun parseXmlOrThrow(xmlStr: String): EasyXmlElement2 = parseXmlByPull(xmlStr, true)
            ?: throw RuntimeException(mFailReason ?: "unknown reason")

    open fun parseXmlByPull(xmlStr: String, throwEx: Boolean): EasyXmlElement2? {
        val task = EasyXmlPullParser(xmlStr, throwEx)
        var event = task.getEventType()
        var first = true
        var currentParent: EasyXmlElement2? = null
        while (event != END_DOCUMENT) {
            when (event) {
                XML_DECL -> Unit
                START_TAG -> {
                    val tag = task.getTag() ?: return makeFail<EasyXmlElement2>("parse error: document tag is null!!!", task.throwEx)
                    val attributes = when {
                        task.getAttributes().isNotEmpty() -> task.getAttributes().map { parseAttribute(it.toPair(), task.throwEx) }.toMutableList()
                        else -> null
                    }
                    if (first) {
                        mResult = EasyXmlElement2(tag, attributes)
                        currentParent = mResult
                        first = false
                    } else {
                        val element = EasyXmlElement2(tag, attributes)
                        currentParent!!.addChild(EasyXmlChild.element(element))
                        if (!task.getStartTagClosed()) {
                            currentParent = element
                        }
                    }
                }
                END_TAG -> {
                    if (task.getTag() != currentParent!!.tag) {
                        val failReason = "parse error: end tag(${task.getTag()}), but start tag(${currentParent.tag})"
                        return makeFail<EasyXmlElement2>(failReason, task.throwEx)
                    }
                    currentParent = currentParent.parent
                }
                TEXT -> currentParent!!.addChild(EasyXmlChild.text(task.getText()))
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

    protected fun parseAttribute(nameValue: Pair<String, String>, throwEx: Boolean): Triple<String, String, String?> {
        val name = nameValue.first
        return when (val splitIndex = name.indexOf(':')) {
            -1 -> Triple<String, String, String?>(name, nameValue.second, null)
            else -> Triple<String, String, String?>(name.substring(splitIndex + 1), nameValue.second, name.substring(0, splitIndex))
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
