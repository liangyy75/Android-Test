@file:Suppress("MemberVisibilityCanBePrivate", "unused", "UNCHECKED_CAST")

package com.liang.example.xml_ktx

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Reader
import java.io.Writer
import java.util.Hashtable
import java.util.Locale
import java.util.Vector

// https://github.com/zeux/pugixml.git
// https://github.com/leethomason/tinyxml2.git
// org.kxml2

class XmlPullParserException(s: String) : Exception(s) {
    var detail: Throwable? = null
    var row = -1
    var column = -1

    constructor(msg: String?, parser: XmlPullParser?, chain: Throwable?) : this((if (msg == null) "" else "$msg ")
            + (if (parser == null) "" else "(position:" + parser.getPositionDescription() + ") ")
            + if (chain == null) "" else "caused by: $chain") {
        if (parser != null) {
            row = parser.getLineNumber()
            column = parser.getColumnNumber()
        }
        detail = chain
    }

    fun printStackTrace() {
        if (detail == null) {
            super.printStackTrace()
        } else {
            synchronized(System.err) {
                System.err.println(super.message + "; nested exception is:")
                detail!!.printStackTrace()
            }
        }
    }
}

interface XmlPullParser {
    companion object {
        const val NO_NAMESPACE = ""
        const val START_DOCUMENT = 0
        const val END_DOCUMENT = 1
        const val START_TAG = 2
        const val END_TAG = 3
        const val TEXT = 4
        const val CDSECT = 5
        const val ENTITY_REF = 6
        const val IGNORABLE_WHITESPACE = 7
        const val PROCESSING_INSTRUCTION = 8
        const val COMMENT = 9
        const val DOCDECL = 10
        val TYPES = arrayOf("START_DOCUMENT", "END_DOCUMENT", "START_TAG", "END_TAG", "TEXT", "CDSECT", "ENTITY_REF", "IGNORABLE_WHITESPACE", "PROCESSING_INSTRUCTION", "COMMENT", "DOCDECL")
        const val FEATURE_PROCESS_NAMESPACES = "http://xmlpull.org/v1/doc/features.html#process-namespaces"
        const val FEATURE_REPORT_NAMESPACE_ATTRIBUTES = "http://xmlpull.org/v1/doc/features.html#report-namespace-prefixes"
        const val FEATURE_PROCESS_DOCDECL = "http://xmlpull.org/v1/doc/features.html#process-docdecl"
        const val FEATURE_VALIDATION = "http://xmlpull.org/v1/doc/features.html#validation"
    }

    @Throws(XmlPullParserException::class)
    fun setFeature(feature: String, value: Boolean)

    fun getFeature(feature: String): Boolean

    @Throws(XmlPullParserException::class)
    fun setProperty(property: String, value: Any)

    fun getProperty(property: String): Any?

    @Throws(XmlPullParserException::class)
    fun setInput(reader: Reader?)

    @Throws(XmlPullParserException::class)
    fun setInput(`is`: InputStream, _enc: String?)

    fun getInputEncoding(): String?

    @Throws(XmlPullParserException::class)
    fun defineEntityReplacementText(entity: String, value: String)

    @Throws(XmlPullParserException::class)
    fun getNamespaceCount(depth: Int): Int

    @Throws(XmlPullParserException::class)
    fun getNamespacePrefix(pos: Int): String

    @Throws(XmlPullParserException::class)
    fun getNamespaceUri(pos: Int): String

    fun getNamespace(prefix: String?): String?

    fun getDepth(): Int

    fun getPositionDescription(): String?

    fun getLineNumber(): Int

    fun getColumnNumber(): Int

    @Throws(XmlPullParserException::class)
    fun isWhitespace(): Boolean

    fun getText(): String?

    fun getTextCharacters(poslen: IntArray): CharArray?

    fun getNamespace(): String?

    fun getName(): String?

    fun getPrefix(): String?

    @Throws(XmlPullParserException::class)
    fun isEmptyElementTag(): Boolean

    fun getAttributeCount(): Int

    fun getAttributeNamespace(index: Int): String?

    fun getAttributeName(index: Int): String

    fun getAttributePrefix(index: Int): String?

    fun getAttributeType(index: Int): String?

    fun isAttributeDefault(index: Int): Boolean

    fun getAttributeValue(index: Int): String?

    fun getAttributeValue(namespace: String?, name: String?): String?

    @Throws(XmlPullParserException::class)
    fun getEventType(): Int

    @Throws(XmlPullParserException::class, IOException::class)
    operator fun next(): Int

    @Throws(XmlPullParserException::class, IOException::class)
    fun nextToken(): Int

    @Throws(XmlPullParserException::class, IOException::class)
    fun require(type: Int, namespace: String?, name: String?)

    @Throws(XmlPullParserException::class, IOException::class)
    fun nextText(): String?

    @Throws(XmlPullParserException::class, IOException::class)
    fun nextTag(): Int
}

interface XmlSerializer {
    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun setFeature(name: String, value: Boolean)

    fun getFeature(name: String): Boolean

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun setProperty(name: String, value: Any)

    fun getProperty(name: String): Any?

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    fun setOutput(os: OutputStream, encoding: String?)

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    fun setOutput(writer: Writer)

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    fun startDocument(encoding: String?, standalone: Boolean?)

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    fun endDocument()

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    fun setPrefix(prefix: String?, namespace: String?)

    @Throws(IllegalArgumentException::class)
    fun getPrefix(namespace: String?, create: Boolean): String?

    fun getDepth(): Int

    fun getNamespace(): String?

    fun getName(): String?

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    fun startTag(namespace: String?, name: String): XmlSerializer

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    fun attribute(namespace: String?, name: String, value: String): XmlSerializer

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    fun endTag(namespace: String?, name: String): XmlSerializer

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    fun text(text: String): XmlSerializer

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    fun text(text: CharArray, start: Int, len: Int): XmlSerializer

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    fun cdsect(data: String)

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    fun entityRef(name: String)

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    fun processingInstruction(pi: String)

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    fun comment(comment: String)

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    fun docdecl(dd: String)

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    fun ignorableWhitespace(s: String)

    @Throws(IOException::class)
    fun flush()
}

open class Node {
    companion object {
        const val DOCUMENT = 0
        const val ELEMENT = 2
        const val TEXT = 4
        const val CDSECT = 5
        const val ENTITY_REF = 6
        const val IGNORABLE_WHITESPACE = 7
        const val PROCESSING_INSTRUCTION = 8
        const val COMMENT = 9
        const val DOCDECL = 10

        fun createElement(namespace: String?, name: String?): Element {
            val e = Element()
            e.namespace = namespace ?: ""
            e.name = name
            return e
        }
    }

    open var children: Vector<Any>? = null
    open var types: StringBuffer? = null

    /**
     * 添加child，这个child可以是String或者Element
     *
     * @param index
     * @param type
     * @param child
     */
    open fun addChild(index: Int, type: Int, child: Any) {
        if (children == null) {
            children = Vector()
            types = StringBuffer()
        }
        if (type == ELEMENT) {
            if (child !is Element) {
                throw RuntimeException("Element obj expected)")
            }
            child.parent = this
        } else if (child !is String) {
            throw RuntimeException("String expected")
        }
        children!!.insertElementAt(child, index)
        types!!.insert(index, type.toChar())
    }

    open fun addChild(type: Int, child: Any) = addChild(getChildCount(), type, child)

    open fun getChild(index: Int): Any = children!!.elementAt(index)

    open fun getChildCount(): Int = if (children == null) 0 else children!!.size

    open fun getElement(index: Int): Element? {
        val child = getChild(index)
        return if (child is Element) {
            child
        } else {
            null
        }
    }

    /**
     * 根据namespace和name查找child的index
     *
     * @param namespace
     * @param name
     * @param startIndex 开始查找的位置
     */
    open fun indexOf(namespace: String?, name: String, startIndex: Int): Int {
        (startIndex until getChildCount()).forEach {
            val child = getElement(it)
            if (child != null && name == child.name && (namespace == null || namespace == child.namespace)) {
                return it
            }
        }
        return -1
    }

    /**
     * 根据namespace和name查找child，如果child不存在或者child多于1个，那么runtimeException
     *
     * @param namespace
     * @param name
     */
    open fun getElement(namespace: String?, name: String): Element? {
        val i = indexOf(namespace, name, 0)
        val j = indexOf(namespace, name, i + 1)
        if (i == -1 || j != -1) throw java.lang.RuntimeException(
                "Element {$namespace}$name${if (i == -1) {
                    " not found in "
                } else {
                    " more than once in "
                }}$this")
        return getElement(i)
    }

    open fun getType(index: Int): Int = types!![index].toInt()

    open fun isText(index: Int): Boolean {
        val type = getType(index)
        return type == TEXT || type == IGNORABLE_WHITESPACE || type == CDSECT
    }

    open fun getText(index: Int): String? = if (isText(index)) getChild(index) as String else null

    /** Recursively builds the child elements from the given parser until an end tag or end document is found. The end tag is not consumed.
     *
     * @param parser
     */
    @Throws(IOException::class, XmlPullParserException::class)
    open fun parse(parser: XmlPullParser) {
        var leave = false
        do {
            when (val type = parser.getEventType()) {
                XmlPullParser.START_TAG -> {
                    val child: Element = createElement(parser.getNamespace(), parser.getName())
                    addChild(ELEMENT, child)
                    child.parse(parser)
                }
                XmlPullParser.END_DOCUMENT, XmlPullParser.END_TAG -> leave = true
                else -> {
                    if (parser.getText() != null) {
                        addChild(if (type == XmlPullParser.ENTITY_REF) {
                            TEXT
                        } else {
                            type
                        }, parser.getText()!!)
                    } else if (type == XmlPullParser.ENTITY_REF && parser.getName() != null) {
                        addChild(ENTITY_REF, parser.getName()!!)
                    }
                    parser.nextToken()
                }
            }
        } while (!leave)
    }

    open fun removeChild(index: Int) {
        children!!.removeElementAt(index)
        val n = types!!.length - 1
        (index until n).forEach {
            types!!.setCharAt(it, types!![it + 1])
        }
        types!!.setLength(n)
    }

    /** Writes this node to the given XmlWriter. For node and document, this method is identical to writeChildren, except that the stream is flushed automatically.
     *
     * @param writer
     */
    @Throws(IOException::class)
    open fun write(writer: XmlSerializer) {
        writeChildren(writer)
        writer.flush()
    }

    @Throws(IOException::class)
    open fun writeChildren(writer: XmlSerializer) {
        if (children.isNullOrEmpty()) return
        for (i in 0 until children!!.size) {
            val type = getType(i)
            val child: Any = children!!.elementAt(i)!!
            when (type) {
                ELEMENT -> (child as Element).write(writer)
                TEXT -> writer.text(child as String)
                IGNORABLE_WHITESPACE -> writer.ignorableWhitespace(child as String)
                CDSECT -> writer.cdsect(child as String)
                COMMENT -> writer.comment(child as String)
                ENTITY_REF -> writer.entityRef(child as String)
                PROCESSING_INSTRUCTION -> writer.processingInstruction(child as String)
                DOCDECL -> writer.docdecl(child as String)
                else -> throw RuntimeException("Illegal type: $type")
            }
        }
    }
}

open class Element : Node() {
    open var parent: Node? = null
    open var namespace: String? = null
    open var name: String? = null
    open var attributes: Vector<Any>? = null
    open var prefixes: Vector<Any>? = null

    open fun init() = Unit

    open fun clear() {
        attributes = null
        children = null
    }

    open fun getAttributeCount(): Int = if (attributes == null) 0 else attributes!!.size

    open fun getAttributeNamespace(index: Int): String = (attributes!!.elementAt(index) as Array<String>)[0]

    open fun getAttributeName(index: Int): String = (attributes!!.elementAt(index) as Array<String>)[1]

    open fun getAttributeValue(index: Int): String = (attributes!!.elementAt(index) as Array<String>)[2]

    open fun getAttributeValue(namespace: String?, name: String): String? {
        (0 until getAttributeCount()).forEach {
            if (name == getAttributeName(it) && (namespace == null || namespace == getAttributeNamespace(it))) {
                return getAttributeValue(it)
            }
        }
        return null
    }

    open fun getRoot(): Node {
        var current: Element = this
        while (current.parent != null) {
            if (current.parent !is Element) {
                return current.parent!!
            }
            current = current.parent as Element
        }
        return current
    }

    open fun getNamespaceCount(): Int = if (prefixes == null) 0 else prefixes!!.size

    open fun getNamespacePrefix(i: Int): String? = (prefixes!!.elementAt(i) as Array<String?>)[0]

    open fun getNamespaceUri(i: Int): String? = (prefixes!!.elementAt(i) as Array<String?>)[1]

    open fun getNamespaceUri(prefix: String?): String? {
        (0 until getNamespaceCount()).forEach {
            if (prefix == getNamespacePrefix(it)) {
                return getNamespaceUri(it)
            }
        }
        return if (parent is Element) (parent as Element).getNamespaceUri(prefix) else null
    }

    open fun setPrefix(prefix: String?, namespace: String?) {
        if (prefixes == null) {
            prefixes = Vector()
        }
        prefixes!!.addElement(arrayOf(prefix, namespace))
    }

    open fun setAttribute(namespace: String?, name: String, value: String?) {
        var innerNamespace = namespace
        if (attributes == null) {
            attributes = Vector()
        }
        if (innerNamespace == null) {
            innerNamespace = ""
        }
        for (i in attributes!!.indices.reversed()) {
            val attributes = attributes!!.elementAt(i) as Array<String>
            if (attributes[0] == innerNamespace && attributes[1] == name) {
                if (value == null) {
                    this.attributes!!.removeElementAt(i)
                } else {
                    attributes[2] = value
                }
                return
            }
        }
        attributes!!.addElement(arrayOf(innerNamespace, name, value))
    }

    @Throws(IOException::class, XmlPullParserException::class)
    override fun parse(parser: XmlPullParser) {
        for (i in parser.getNamespaceCount(parser.getDepth() - 1) until parser.getNamespaceCount(parser.getDepth())) {
            setPrefix(parser.getNamespacePrefix(i), parser.getNamespaceUri(i))
        }
        for (i in 0 until parser.getAttributeCount()) {
            setAttribute(parser.getAttributeNamespace(i), parser.getAttributeName(i), parser.getAttributeValue(i))
        }
        init()
        if (parser.isEmptyElementTag()) {
            parser.nextToken()
        } else {
            parser.nextToken()
            super.parse(parser)
            if (getChildCount() == 0) {
                addChild(IGNORABLE_WHITESPACE, "")
            }
        }
        parser.require(XmlPullParser.END_TAG, namespace, name)
        parser.nextToken()
    }

    @Throws(IOException::class)
    override fun write(writer: XmlSerializer) {
        if (prefixes != null) {
            prefixes!!.indices.forEach {
                writer.setPrefix(getNamespacePrefix(it), getNamespaceUri(it))
            }
        }
        writer.startTag(namespace!!, name!!)
        val len = getAttributeCount()
        (0 until len).forEach {
            writer.attribute(getAttributeNamespace(it), getAttributeName(it), getAttributeValue(it))
        }
        writeChildren(writer)
        writer.endTag(namespace!!, name!!)
    }
}

open class Document : Node() {
    var rootIndex = -1
    var encoding: String? = null
    var standalone: Boolean? = null

    open fun getName(): String? = "#document"

    override fun addChild(index: Int, type: Int, child: Any) {
        if (type == ELEMENT) {
            rootIndex = index
        } else if (rootIndex >= index) {
            rootIndex++
        }
        super.addChild(index, type, child)
    }

    override fun removeChild(index: Int) {
        if (index == rootIndex) rootIndex = -1 else if (index < rootIndex) rootIndex--
        super.removeChild(index)
    }

    open fun getRootElement(): Element {
        if (rootIndex == -1) {
            throw RuntimeException("Document has no root element!")
        }
        return getChild(rootIndex) as Element
    }

    @Throws(IOException::class, XmlPullParserException::class)
    override fun parse(parser: XmlPullParser) {
        parser.require(XmlPullParser.START_DOCUMENT, null, null)
        parser.nextToken()
        encoding = parser.getInputEncoding()
        standalone = parser.getProperty("http://xmlpull.org/v1/doc/properties.html#xmldecl-standalone") as Boolean
        super.parse(parser)
        if (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
            throw java.lang.RuntimeException("Document end expected!")
        }
    }

    @Throws(IOException::class)
    override fun write(writer: XmlSerializer) {
        writer.startDocument(encoding, standalone)
        writeChildren(writer)
        writer.endDocument()
    }
}

class KXmlParser : XmlPullParser {
    private var location: Any? = null
    // general
    private var version: String? = null
    private var standalone: Boolean? = null
    private var processNsp = false
    private var relaxed = false
    private var entityMap: Hashtable<Any, Any>? = null
    private var depth = 0
    private var elementStack = arrayOfNulls<String>(16)
    private var nspStack = arrayOfNulls<String>(8)
    private var nspCounts = IntArray(4)
    // source
    private var reader: Reader? = null
    private var encoding: String? = null
    private val srcBuf: CharArray = CharArray(if (Runtime.getRuntime().freeMemory() >= 1048576) 8192 else 128)
    private var srcPos = 0
    private var srcCount = 0
    private var line = 0
    private var column = 0
    // txtbuffer
    private var txtBuf = CharArray(128)
    private var txtPos = 0
    // Event-related
    private var type = 0
    //private String text;
    private var isWhitespace = false
    private var namespace: String? = null
    private var prefix: String? = null
    private var name: String? = null
    private var degenerated = false
    private var attributeCount = 0
    private var attributes = arrayOfNulls<String>(16)
    private var stackMismatch = 0
    private var error: String? = null
    /**
     * A separate peek buffer seems simpler than managing
     * wrap around in the first level read buffer  */
    private val peek = IntArray(2)
    private var peekCount = 0
    private var wasCR = false
    private var unresolved = false
    private var token = false
    private fun isProp(n1: String, prop: Boolean, n2: String): Boolean {
        if (!n1.startsWith("http://xmlpull.org/v1/doc/")) return false
        return if (prop) n1.substring(42) == n2 else n1.substring(40) == n2
    }

    @Throws(XmlPullParserException::class)
    private fun adjustNsp(): Boolean {
        var any = false
        var i = 0
        while (i < attributeCount shl 2) {
            // * 4 - 4; i >= 0; i -= 4) {
            var attrName = attributes[i + 2]
            val cut = attrName!!.indexOf(':')
            var prefix: String
            if (cut != -1) {
                prefix = attrName.substring(0, cut)
                attrName = attrName.substring(cut + 1)
            } else if (attrName == "xmlns") {
                prefix = attrName
                attrName = null
            } else {
                i += 4
                continue
            }
            if (prefix != "xmlns") {
                any = true
            } else {
                val j = nspCounts[depth]++ shl 1
                nspStack = ensureCapacity(nspStack, j + 2)
                nspStack[j] = attrName
                nspStack[j + 1] = attributes[i + 3]
                if (attrName != null && attributes[i + 3] == "") error("illegal empty namespace")
                //  prefixMap = new PrefixMap (prefixMap, attrName, attr.getValue ());
//System.out.println (prefixMap);
                System.arraycopy(
                        attributes,
                        i + 4,
                        attributes,
                        i,
                        (--attributeCount shl 2) - i)
                i -= 4
            }
            i += 4
        }
        if (any) {
            i = (attributeCount shl 2) - 4
            while (i >= 0) {
                var attrName = attributes[i + 2]
                val cut = attrName!!.indexOf(':')
                if (cut == 0 && !relaxed) throw java.lang.RuntimeException(
                        "illegal attribute name: $attrName at $this") else if (cut != -1) {
                    val attrPrefix = attrName.substring(0, cut)
                    attrName = attrName.substring(cut + 1)
                    val attrNs = getNamespace(attrPrefix)
                    if (attrNs == null && !relaxed) throw java.lang.RuntimeException(
                            "Undefined Prefix: $attrPrefix in $this")
                    attributes[i] = attrNs
                    attributes[i + 1] = attrPrefix
                    attributes[i + 2] = attrName
                    /*
                                        if (!relaxed) {
                                            for (int j = (attributeCount << 2) - 4; j > i; j -= 4)
                                                if (attrName.equals(attributes[j + 2])
                                                    && attrNs.equals(attributes[j]))
                                                    exception(
                                                        "Duplicate Attribute: {"
                                                            + attrNs
                                                            + "}"
                                                            + attrName);
                                        }
                        */
                }
                i -= 4
            }
        }
        val cut = name!!.indexOf(':')
        if (cut == 0) error("illegal tag name: $name")
        if (cut != -1) {
            prefix = name!!.substring(0, cut)
            name = name!!.substring(cut + 1)
        }
        namespace = getNamespace(prefix!!)
        if (namespace == null) {
            if (prefix != null) error("undefined prefix: $prefix")
            namespace = XmlPullParser.NO_NAMESPACE
        }
        return any
    }

    private fun ensureCapacity(arr: Array<String?>, required: Int): Array<String?> {
        if (arr.size >= required) return arr
        val bigger = arrayOfNulls<String>(required + 16)
        System.arraycopy(arr, 0, bigger, 0, arr.size)
        return bigger
    }

    @Throws(XmlPullParserException::class)
    private fun error(desc: String) {
        if (relaxed) {
            if (error == null) error = "ERR: $desc"
        } else exception(desc)
    }

    @Throws(XmlPullParserException::class)
    private fun exception(desc: String) {
        throw XmlPullParserException(
                if (desc.length < 100) desc else desc.substring(0, 100) + "\n",
                this,
                null)
    }

    /**
     * common base for next and nextToken. Clears the state, except from
     * txtPos and whitespace. Does not set the type variable  */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun nextImpl() {
        if (reader == null) exception("No Input specified")
        if (type == XmlPullParser.END_TAG) depth--
        while (true) {
            attributeCount = -1
            // degenerated needs to be handled before error because of possible
// processor expectations(!)
            if (degenerated) {
                degenerated = false
                type = XmlPullParser.END_TAG
                return
            }
            if (error != null) {
                for (element in error!!) {
                    push(element.toInt())
                    //				text = error;
                }
                error = null
                type = XmlPullParser.COMMENT
                return
            }
            if (relaxed
                    && (stackMismatch > 0 || peek(0) == -1 && depth > 0)) {
                val sp = depth - 1 shl 2
                type = XmlPullParser.END_TAG
                namespace = elementStack[sp]
                prefix = elementStack[sp + 1]
                name = elementStack[sp + 2]
                if (stackMismatch != 1) error = "missing end tag /$name inserted"
                if (stackMismatch > 0) stackMismatch--
                return
            }
            prefix = null
            name = null
            namespace = null
            //            text = null;
            type = peekType()
            when (type) {
                XmlPullParser.ENTITY_REF -> {
                    pushEntity()
                    return
                }
                XmlPullParser.START_TAG -> {
                    parseStartTag(false)
                    return
                }
                XmlPullParser.END_TAG -> {
                    parseEndTag()
                    return
                }
                XmlPullParser.END_DOCUMENT -> return
                XmlPullParser.TEXT -> {
                    pushText('<'.toInt(), !token)
                    if (depth == 0) {
                        if (isWhitespace) type = XmlPullParser.IGNORABLE_WHITESPACE
                        // make exception switchable for instances.chg... !!!!
//	else 
//    exception ("text '"+getText ()+"' not allowed outside root element");
                    }
                    return
                }
                else -> {
                    type = parseLegacy(token)
                    if (type != XML_DECL) return
                }
            }
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun parseLegacy(push: Boolean): Int {
        var pushVar = push
        var req = ""
        val term: Int
        val result: Int
        var prev = 0
        read() // <
        var c = read()
        when (c) {
            '?'.toInt() -> {
                if ((peek(0) == 'x'.toInt() || peek(0) == 'X'.toInt())
                        && (peek(1) == 'm'.toInt() || peek(1) == 'M'.toInt())) {
                    if (pushVar) {
                        push(peek(0))
                        push(peek(1))
                    }
                    read()
                    read()
                    if ((peek(0) == 'l'.toInt() || peek(0) == 'L'.toInt()) && peek(1) <= ' '.toInt()) {
                        if (line != 1 || column > 4) error("PI must not start with xml")
                        parseStartTag(true)
                        if (attributeCount < 1 || "version" != attributes[2]) error("version expected")
                        version = attributes[3]
                        var pos = 1
                        if (pos < attributeCount
                                && "encoding" == attributes[2 + 4]) {
                            encoding = attributes[3 + 4]
                            pos++
                        }
                        if (pos < attributeCount
                                && "standalone" == attributes[4 * pos + 2]) {
                            val st = attributes[3 + 4 * pos]
                            if ("yes" == st) standalone = true else if ("no" == st) standalone = false else error("illegal standalone value: $st")
                            pos++
                        }
                        if (pos != attributeCount) error("illegal xmldecl")
                        isWhitespace = true
                        txtPos = 0
                        return XML_DECL
                    }
                }
                /*            int c0 = read ();
                        int c1 = read ();
                        int */term = '?'.toInt()
                result = XmlPullParser.PROCESSING_INSTRUCTION
            }
            '!'.toInt() -> {
                when {
                    peek(0) == '-'.toInt() -> {
                        result = XmlPullParser.COMMENT
                        req = "--"
                        term = '-'.toInt()
                    }
                    peek(0) == '['.toInt() -> {
                        result = XmlPullParser.CDSECT
                        req = "[CDATA["
                        term = ']'.toInt()
                        pushVar = true
                    }
                    else -> {
                        result = XmlPullParser.DOCDECL
                        req = "DOCTYPE"
                        term = -1
                    }
                }
            }
            else -> {
                error("illegal: <$c")
                return XmlPullParser.COMMENT
            }
        }
        for (element in req) read(element)
        if (result == XmlPullParser.DOCDECL) parseDoctype(pushVar) else {
            while (true) {
                c = read()
                if (c == -1) {
                    error(UNEXPECTED_EOF)
                    return XmlPullParser.COMMENT
                }
                if (pushVar) push(c)
                if ((term == '?'.toInt() || c == term)
                        && peek(0) == term && peek(1) == '>'.toInt()) break
                prev = c
            }
            if (term == '-'.toInt() && prev == '-'.toInt()) error("illegal comment delimiter: --->")
            read()
            read()
            if (pushVar && term != '?'.toInt()) txtPos--
        }
        return result
    }

    /** precondition: &lt! consumed  */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun parseDoctype(push: Boolean) {
        var nesting = 1
        var quoted = false
        // read();
        while (true) {
            val i = read()
            when (i) {
                -1 -> {
                    error(UNEXPECTED_EOF)
                    return
                }
                39 -> quoted = !quoted  // '\''
                60 -> if (!quoted) nesting++  // '<'
                62 -> if (!quoted) {  // '>'
                    if (--nesting == 0) {
                        return
                    }
                }
            }
            if (push) push(i)
        }
    }

    /* precondition: &lt;/ consumed */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun parseEndTag() {
        read() // '<'
        read() // '/'
        name = readName()
        skip()
        read('>')
        val sp = depth - 1 shl 2
        if (depth == 0) {
            error("element stack empty")
            type = XmlPullParser.COMMENT
            return
        }
        if (name != elementStack[sp + 3]) {
            error("expected: /" + elementStack[sp + 3] + " read: " + name)
            // become case insensitive in relaxed mode
            var probe = sp
            while (probe >= 0 && name!!.toLowerCase(Locale.US) != elementStack[probe + 3]!!.toLowerCase(Locale.US)) {
                stackMismatch++
                probe -= 4
            }
            if (probe < 0) {
                stackMismatch = 0
                //			text = "unexpected end tag ignored";
                type = XmlPullParser.COMMENT
                return
            }
        }
        namespace = elementStack[sp]
        prefix = elementStack[sp + 1]
        name = elementStack[sp + 2]
    }

    @Throws(IOException::class)
    private fun peekType(): Int {
        return when (peek(0)) {
            -1 -> XmlPullParser.END_DOCUMENT
            38 -> XmlPullParser.ENTITY_REF  // '&'
            60 -> when (peek(1)) {  // '<'
                47 -> XmlPullParser.END_TAG  // '/'
                63, 33 -> LEGACY  // '?', '!'
                else -> XmlPullParser.START_TAG
            }
            else -> XmlPullParser.TEXT
        }
    }

    private operator fun get(pos: Int): String {
        return String(txtBuf, pos, txtPos - pos)
    }

    /*
    private final String pop (int pos) {
    String result = new String (txtBuf, pos, txtPos - pos);
    txtPos = pos;
    return result;
    }
    */
    private fun push(c: Int) {
        isWhitespace = isWhitespace and (c <= ' '.toInt())
        if (txtPos == txtBuf.size) {
            val bigger = CharArray(txtPos * 4 / 3 + 4)
            System.arraycopy(txtBuf, 0, bigger, 0, txtPos)
            txtBuf = bigger
        }
        txtBuf[txtPos++] = c.toChar()
    }

    /** Sets name and attributes  */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun parseStartTag(xmldecl: Boolean) {
        if (!xmldecl) read()
        name = readName()
        attributeCount = 0
        while (true) {
            skip()
            val c = peek(0)
            if (xmldecl) {
                if (c == '?'.toInt()) {
                    read()
                    read('>')
                    return
                }
            } else {
                if (c == '/'.toInt()) {
                    degenerated = true
                    read()
                    skip()
                    read('>')
                    break
                }
                if (c == '>'.toInt() && !xmldecl) {
                    read()
                    break
                }
            }
            if (c == -1) {
                error(UNEXPECTED_EOF)
                //type = COMMENT;
                return
            }
            val attrName = readName()
            if (attrName.isEmpty()) {
                error("attr name expected")
                //type = COMMENT;
                break
            }
            var i = attributeCount++ shl 2
            attributes = ensureCapacity(attributes, i + 4)
            attributes[i++] = ""
            attributes[i++] = null
            attributes[i++] = attrName
            skip()
            if (peek(0) != '='.toInt()) {
                error("Attr.value missing f. $attrName")
                attributes[i] = "1"
            } else {
                read('=')
                skip()
                var delimiter = peek(0)
                if (delimiter != '\''.toInt() && delimiter != '"'.toInt()) {
                    error("attr value delimiter missing!")
                    delimiter = ' '.toInt()
                } else read()
                val p = txtPos
                pushText(delimiter, true)
                attributes[i] = get(p)
                txtPos = p
                if (delimiter != ' '.toInt()) read() // skip endquote
            }
        }
        val sp = depth++ shl 2
        elementStack = ensureCapacity(elementStack, sp + 4)
        elementStack[sp + 3] = name
        if (depth >= nspCounts.size) {
            val bigger = IntArray(depth + 4)
            System.arraycopy(nspCounts, 0, bigger, 0, nspCounts.size)
            nspCounts = bigger
        }
        nspCounts[depth] = nspCounts[depth - 1]
        /*
        		if(!relaxed){
                for (int i = attributeCount - 1; i > 0; i--) {
                    for (int j = 0; j < i; j++) {
                        if (getAttributeName(i).equals(getAttributeName(j)))
                            exception("Duplicate Attribute: " + getAttributeName(i));
                    }
                }
        		}
        */if (processNsp) adjustNsp() else namespace = ""
        elementStack[sp] = namespace
        elementStack[sp + 1] = prefix
        elementStack[sp + 2] = name
    }

    /**
     * result: isWhitespace; if the setName parameter is set,
     * the name of the entity is stored in "name"  */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun pushEntity() {
        push(read()) // &
        val pos = txtPos
        while (true) {
            val c = read()
            if (c == ';'.toInt()) break
            if (c < 128 && (c < '0'.toInt() || c > '9'.toInt())
                    && (c < 'a'.toInt() || c > 'z'.toInt())
                    && (c < 'A'.toInt() || c > 'Z'.toInt())
                    && c != '_'.toInt() && c != '-'.toInt() && c != '#'.toInt()) {
                if (!relaxed) {
                    error("unterminated entity ref")
                }
                //; ends with:"+(char)c);           
                if (c != -1) push(c)
                return
            }
            push(c)
        }
        val code = get(pos)
        txtPos = pos - 1
        if (token && type == XmlPullParser.ENTITY_REF) {
            name = code
        }
        if (code[0] == '#') {
            val c = if (code[1] == 'x') code.substring(2).toInt(16) else code.substring(1).toInt()
            push(c)
            return
        }
        val result = entityMap!![code] as String?
        unresolved = result == null
        if (unresolved) {
            if (!token) error("unresolved: &$code;")
        } else {
            for (element in result!!) {
                push(element.toInt())
            }
        }
    }

    /** types:
     * '<': parse to any token (for nextToken ())
     * '"': parse to quote
     * ' ': parse to whitespace or '>'
     */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun pushText(delimiter: Int, resolveEntities: Boolean) {
        var next = peek(0)
        var cbrCount = 0
        while (next != -1 && next != delimiter) { // covers eof, '<', '"'
            if (delimiter == ' '.toInt()) if (next <= ' '.toInt() || next == '>'.toInt()) break
            if (next == '&'.toInt()) {
                if (!resolveEntities) break
                pushEntity()
            } else if (next == '\n'.toInt() && type == XmlPullParser.START_TAG) {
                read()
                push(' '.toInt())
            } else push(read())
            if (next == '>'.toInt() && cbrCount >= 2 && delimiter != ']'.toInt()) error("Illegal: ]]>")
            if (next == ']'.toInt()) cbrCount++ else cbrCount = 0
            next = peek(0)
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun read(c: Char) {
        val a = read()
        if (a != c.toInt()) error("expected: '" + c + "' actual: '" + a.toChar() + "'")
    }

    @Throws(IOException::class)
    private fun read(): Int {
        val result: Int
        if (peekCount == 0) result = peek(0) else {
            result = peek[0]
            peek[0] = peek[1]
        }
        //		else {
//			result = peek[0]; 
//			System.arraycopy (peek, 1, peek, 0, peekCount-1);
//		}
        peekCount--
        column++
        if (result == '\n'.toInt()) {
            line++
            column = 1
        }
        return result
    }

    /** Does never read more than needed  */
    @Throws(IOException::class)
    private fun peek(pos: Int): Int {
        while (pos >= peekCount) {
            var nw: Int
            when {
                srcBuf.size <= 1 -> nw = reader!!.read()
                srcPos < srcCount -> nw = srcBuf[srcPos++].toInt()
                else -> {
                    srcCount = reader!!.read(srcBuf, 0, srcBuf.size)
                    nw = if (srcCount <= 0) -1 else srcBuf[0].toInt()
                    srcPos = 1
                }
            }
            if (nw == '\r'.toInt()) {
                wasCR = true
                peek[peekCount++] = 10  // '\n'
            } else {
                if (nw == '\n'.toInt()) {
                    if (!wasCR) peek[peekCount++] = 10  // '\n'
                } else peek[peekCount++] = nw
                wasCR = false
            }
        }
        return peek[pos]
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readName(): String {
        val pos = txtPos
        var c = peek(0)
        if ((c < 'a'.toInt() || c > 'z'.toInt())
                && (c < 'A'.toInt() || c > 'Z'.toInt())
                && c != '_'.toInt() && c != ':'.toInt() && c < 0x0c0 && !relaxed) error("name expected")
        do {
            push(read())
            c = peek(0)
        } while (c >= 'a'.toInt() && c <= 'z'.toInt()
                || c >= 'A'.toInt() && c <= 'Z'.toInt()
                || c >= '0'.toInt() && c <= '9'.toInt()
                || c == '_'.toInt() || c == '-'.toInt() || c == ':'.toInt() || c == '.'.toInt() || c >= 0x0b7)
        val result = get(pos)
        txtPos = pos
        return result
    }

    @Throws(IOException::class)
    private fun skip() {
        while (true) {
            val c = peek(0)
            if (c > ' '.toInt() || c == -1) break
            read()
        }
    }

    //  public part starts here...
    @Throws(XmlPullParserException::class)
    override fun setInput(reader: Reader?) {
        this.reader = reader
        line = 1
        column = 0
        type = XmlPullParser.START_DOCUMENT
        name = null
        namespace = null
        degenerated = false
        attributeCount = -1
        encoding = null
        version = null
        standalone = null
        if (reader == null) {
            return
        }
        srcPos = 0
        srcCount = 0
        peekCount = 0
        depth = 0
        entityMap = Hashtable()
        entityMap!!["amp"] = "&"
        entityMap!!["apos"] = "'"
        entityMap!!["gt"] = ">"
        entityMap!!["lt"] = "<"
        entityMap!!["quot"] = "\""
    }

    @Throws(XmlPullParserException::class)
    override fun setInput(`is`: InputStream, _enc: String?) {
        srcPos = 0
        srcCount = 0
        var enc = _enc
        try {
            if (enc == null) { // read four bytes 
                var chk = 0
                while (srcCount < 4) {
                    val i = `is`.read()
                    if (i == -1) break
                    chk = chk shl 8 or i
                    srcBuf[srcCount++] = i.toChar()
                }
                if (srcCount == 4) {
                    when (chk) {
                        0x00000FEFF -> {
                            enc = "UTF-32BE"
                            srcCount = 0
                        }
                        -0x20000 -> {
                            enc = "UTF-32LE"
                            srcCount = 0
                        }
                        0x03c -> {
                            enc = "UTF-32BE"
                            srcBuf[0] = '<'
                            srcCount = 1
                        }
                        0x03c000000 -> {
                            enc = "UTF-32LE"
                            srcBuf[0] = '<'
                            srcCount = 1
                        }
                        0x0003c003f -> {
                            enc = "UTF-16BE"
                            srcBuf[0] = '<'
                            srcBuf[1] = '?'
                            srcCount = 2
                        }
                        0x03c003f00 -> {
                            enc = "UTF-16LE"
                            srcBuf[0] = '<'
                            srcBuf[1] = '?'
                            srcCount = 2
                        }
                        0x03c3f786d -> {
                            while (true) {
                                val i = `is`.read()
                                if (i == -1) break
                                srcBuf[srcCount++] = i.toChar()
                                if (i == '>'.toInt()) {
                                    val s = String(srcBuf, 0, srcCount)
                                    var i0 = s.indexOf("encoding")
                                    if (i0 != -1) {
                                        while (s[i0] != '"'
                                                && s[i0] != '\'') i0++
                                        val deli = s[i0++]
                                        val i1 = s.indexOf(deli, i0)
                                        enc = s.substring(i0, i1)
                                    }
                                    break
                                }
                            }
                        }
                        else -> when {
                            chk and -0x10000 == -0x1010000 -> {
                                enc = "UTF-16BE"
                                srcBuf[0] = (srcBuf[2].toInt() shl 8 or srcBuf[3].toInt()).toChar()
                                srcCount = 1
                            }
                            chk and -0x10000 == -0x20000 -> {
                                enc = "UTF-16LE"
                                srcBuf[0] = (srcBuf[3].toInt() shl 8 or srcBuf[2].toInt()).toChar()
                                srcCount = 1
                            }
                            chk and -0x100 == -0x10444100 -> {
                                enc = "UTF-8"
                                srcBuf[0] = srcBuf[3]
                                srcCount = 1
                            }
                        }
                    }
                }
            }
            if (enc == null) enc = "UTF-8"
            val sc = srcCount
            setInput(InputStreamReader(`is`, enc))
            encoding = _enc
            srcCount = sc
        } catch (e: java.lang.Exception) {
            throw XmlPullParserException(
                    "Invalid stream or encoding: $e",
                    this,
                    e)
        }
    }

    override fun getFeature(feature: String): Boolean =
            if (XmlPullParser.FEATURE_PROCESS_NAMESPACES == feature) processNsp else if (isProp(feature, false, "relaxed")) relaxed else false

    override fun getInputEncoding(): String {
        return encoding!!
    }

    @Throws(XmlPullParserException::class)
    override fun defineEntityReplacementText(entity: String, value: String) {
        if (entityMap == null) {
            throw RuntimeException("entity replacement text must be defined after setInput!")
        }
        entityMap!![entity] = value
    }

    override fun getProperty(property: String): Any? = when {
        isProp(property, true, "xmldecl-version") -> version!!
        isProp(property, true, "xmldecl-standalone") -> standalone!!
        isProp(property, true, "location") -> if (location != null) location!! else reader.toString()
        else -> null
    }

    override fun getNamespaceCount(depth: Int): Int {
        if (depth > this.depth) throw IndexOutOfBoundsException()
        return nspCounts[depth]
    }

    override fun getNamespacePrefix(pos: Int): String {
        return nspStack[pos shl 1]!!
    }

    override fun getNamespaceUri(pos: Int): String {
        return nspStack[(pos shl 1) + 1]!!
    }

    override fun getNamespace(prefix: String?): String? {
        if ("xml" == prefix) return "http://www.w3.org/XML/1998/namespace"
        if ("xmlns" == prefix) return "http://www.w3.org/2000/xmlns/"
        var i = (getNamespaceCount(depth) shl 1) - 2
        while (i >= 0) {
            if (prefix == null) {
                if (nspStack[i] == null) return nspStack[i + 1]!!
            } else if (prefix == nspStack[i]) return nspStack[i + 1]!!
            i -= 2
        }
        return null
    }

    override fun getDepth(): Int = depth

    override fun getPositionDescription(): String {
        val buf = StringBuffer(if (type < XmlPullParser.TYPES.size) XmlPullParser.TYPES[type] else "unknown")
        buf.append(' ')
        when {
            type == XmlPullParser.START_TAG || type == XmlPullParser.END_TAG -> {
                if (degenerated) buf.append("(empty) ")
                buf.append('<')
                if (type == XmlPullParser.END_TAG) buf.append('/')
                if (prefix != null) buf.append("{$namespace}$prefix:")
                buf.append(name)
                val cnt = attributeCount shl 2
                var i = 0
                while (i < cnt) {
                    buf.append(' ')
                    if (attributes[i + 1] != null) buf.append(
                            "{" + attributes[i] + "}" + attributes[i + 1] + ":")
                    buf.append(attributes[i + 2].toString() + "='" + attributes[i + 3] + "'")
                    i += 4
                }
                buf.append('>')
            }
            type == XmlPullParser.IGNORABLE_WHITESPACE -> Unit
            type != XmlPullParser.TEXT -> buf.append(getText())
            isWhitespace -> buf.append("(whitespace)")
            else -> {
                var text = getText()!!
                if (text.length > 16) text = text.substring(0, 16) + "..."
                buf.append(text)
            }
        }
        buf.append("@$line:$column")
        if (location != null) {
            buf.append(" in ")
            buf.append(location)
        } else if (reader != null) {
            buf.append(" in ")
            buf.append(reader.toString())
        }
        return buf.toString()
    }

    override fun getLineNumber(): Int {
        return line
    }

    override fun getColumnNumber(): Int {
        return column
    }

    @Throws(XmlPullParserException::class)
    override fun isWhitespace(): Boolean {
        if (type != XmlPullParser.TEXT && type != XmlPullParser.IGNORABLE_WHITESPACE && type != XmlPullParser.CDSECT) {
            exception(ILLEGAL_TYPE)
        }
        return isWhitespace
    }

    override fun getText(): String? = if (type < XmlPullParser.TEXT || type == XmlPullParser.ENTITY_REF && unresolved) null else get(0)

    override fun getTextCharacters(poslen: IntArray): CharArray? {
        if (type >= XmlPullParser.TEXT) {
            if (type == XmlPullParser.ENTITY_REF) {
                poslen[0] = 0
                poslen[1] = name!!.length
                return name!!.toCharArray()
            }
            poslen[0] = 0
            poslen[1] = txtPos
            return txtBuf
        }
        poslen[0] = -1
        poslen[1] = -1
        return null
    }

    override fun getNamespace(): String {
        return namespace!!
    }

    override fun getName(): String {
        return name!!
    }

    override fun getPrefix(): String {
        return prefix!!
    }

    @Throws(XmlPullParserException::class)
    override fun isEmptyElementTag(): Boolean {
        if (type != XmlPullParser.START_TAG) exception(ILLEGAL_TYPE)
        return degenerated
    }

    override fun getAttributeCount(): Int {
        return attributeCount
    }

    override fun getAttributeType(index: Int): String {
        return "CDATA"
    }

    override fun isAttributeDefault(index: Int): Boolean {
        return false
    }

    override fun getAttributeNamespace(index: Int): String {
        if (index >= attributeCount) throw IndexOutOfBoundsException()
        return attributes[index shl 2]!!
    }

    override fun getAttributeName(index: Int): String {
        if (index >= attributeCount) throw IndexOutOfBoundsException()
        return attributes[(index shl 2) + 2]!!
    }

    override fun getAttributePrefix(index: Int): String {
        if (index >= attributeCount) throw IndexOutOfBoundsException()
        return attributes[(index shl 2) + 1]!!
    }

    override fun getAttributeValue(index: Int): String {
        if (index >= attributeCount) throw IndexOutOfBoundsException()
        return attributes[(index shl 2) + 3]!!
    }

    override fun getAttributeValue(namespace: String?, name: String?): String? {
        var i = (attributeCount shl 2) - 4
        while (i >= 0) {
            if (attributes[i + 2] == name && (namespace == null || attributes[i] == namespace)) {
                return attributes[i + 3]!!
            }
            i -= 4
        }
        return null
    }

    @Throws(XmlPullParserException::class)
    override fun getEventType(): Int {
        return type
    }

    @Throws(XmlPullParserException::class, IOException::class)
    override fun next(): Int {
        txtPos = 0
        isWhitespace = true
        var minType = 9999
        token = false
        do {
            nextImpl()
            if (type < minType) minType = type
            //	    if (curr <= TEXT) type = curr; 
        } while (minType > XmlPullParser.ENTITY_REF // ignorable
                || minType >= XmlPullParser.TEXT && peekType() >= XmlPullParser.TEXT)
        type = minType
        if (type > XmlPullParser.TEXT) type = XmlPullParser.TEXT
        return type
    }

    @Throws(XmlPullParserException::class, IOException::class)
    override fun nextToken(): Int {
        isWhitespace = true
        txtPos = 0
        token = true
        nextImpl()
        return type
    }

    //
// utility methods to make XML parsing easier ...
    @Throws(XmlPullParserException::class, IOException::class)
    override fun nextTag(): Int {
        next()
        if (type == XmlPullParser.TEXT && isWhitespace) next()
        if (type != XmlPullParser.END_TAG && type != XmlPullParser.START_TAG) exception("unexpected type")
        return type
    }

    @Throws(XmlPullParserException::class, IOException::class)
    override fun require(type: Int, namespace: String?, name: String?) {
        if (type != this.type || namespace != null && namespace != getNamespace()
                || name != null && name != getName()) exception(
                "expected: " + XmlPullParser.TYPES[type] + " {" + namespace + "}" + name)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    override fun nextText(): String? {
        if (type != XmlPullParser.START_TAG) {
            exception("precondition: START_TAG")
        }
        next()
        val result: String?
        if (type == XmlPullParser.TEXT) {
            result = getText()
            next()
        } else {
            result = ""
        }
        if (type != XmlPullParser.END_TAG) {
            exception("END_TAG expected")
        }
        return result
    }

    @Throws(XmlPullParserException::class)
    override fun setFeature(feature: String, value: Boolean) {
        if (XmlPullParser.FEATURE_PROCESS_NAMESPACES == feature) processNsp = value else if (isProp(feature, false, "relaxed")) relaxed = value else exception("unsupported feature: $feature")
    }

    @Throws(XmlPullParserException::class)
    override fun setProperty(property: String, value: Any) {
        location = if (isProp(property, true, "location")) value else throw XmlPullParserException("unsupported property: $property")
    }

    /**
     * Skip sub tree that is currently porser positioned on.
     * <br></br>NOTE: parser must be on START_TAG and when funtion returns
     * parser will be positioned on corresponding END_TAG.
     */
//	Implementation copied from Alek's mail... 
    @Throws(XmlPullParserException::class, IOException::class)
    fun skipSubTree() {
        require(XmlPullParser.START_TAG, null, null)
        var level = 1
        while (level > 0) {
            val eventType = next()
            if (eventType == XmlPullParser.END_TAG) {
                --level
            } else if (eventType == XmlPullParser.START_TAG) {
                ++level
            }
        }
    }

    companion object {
        private const val UNEXPECTED_EOF = "Unexpected EOF"
        private const val ILLEGAL_TYPE = "Wrong event type"
        private const val LEGACY = 999
        private const val XML_DECL = 998
    }
}

class KXmlSerializer : XmlSerializer {
    //    static final String UNDEFINED = ":";
    private var writer: Writer? = null
    private var pending = false
    private var auto = 0
    private var depth = 0
    private var elementStack = arrayOfNulls<String>(12)
    //nsp/prefix/name
    private var nspCounts = IntArray(4)
    private var nspStack = arrayOfNulls<String>(8)
    //prefix/nsp; both empty are ""
    private var indent = BooleanArray(4)
    private var unicode = false
    private var encoding: String? = null

    @Throws(IOException::class)
    private fun check(close: Boolean) {
        if (!pending) return
        depth++
        pending = false
        if (indent.size <= depth) {
            val hlp = BooleanArray(depth + 4)
            System.arraycopy(indent, 0, hlp, 0, depth)
            indent = hlp
        }
        indent[depth] = indent[depth - 1]
        for (i in nspCounts[depth - 1] until nspCounts[depth]) {
            writer!!.write(' '.toInt())
            writer!!.write("xmlns")
            if ("" != nspStack[i * 2]) {
                writer!!.write(':'.toInt())
                writer!!.write(nspStack[i * 2]!!)
            } else check(!("" == getNamespace() && "" != nspStack[i * 2 + 1])) { "Cannot set default namespace for elements in no namespace" }
            writer!!.write("=\"")
            writeEscaped(nspStack[i * 2 + 1], '"'.toInt())
            writer!!.write('"'.toInt())
        }
        if (nspCounts.size <= depth + 1) {
            val hlp = IntArray(depth + 8)
            System.arraycopy(nspCounts, 0, hlp, 0, depth + 1)
            nspCounts = hlp
        }
        nspCounts[depth + 1] = nspCounts[depth]
        //   nspCounts[depth + 2] = nspCounts[depth];
        writer!!.write(if (close) " />" else ">")
    }

    @Throws(IOException::class)
    private fun writeEscaped(s: String?, quot: Int) {
        for (element in s!!) {
            var breakFlag = false
            when (element) {
                '\n', '\r', '\t' -> if (quot == -1) {
                    writer!!.write(element.toInt())
                } else {
                    writer!!.write("&#" + element.toInt() + ';')
                }
                '&' -> writer!!.write("&amp;")
                '>' -> writer!!.write("&gt;")
                '<' -> writer!!.write("&lt;")
                '"', '\'' -> {
                    if (element.toInt() == quot) {
                        writer!!.write(if (element == '"') "&quot;" else "&apos;")
                        breakFlag = true
                    } else {
                        // if(c < ' ')
                        //     throw new IllegalArgumentException("Illegal control code:"+((int) c));
                        if (element >= ' ' && element != '@' && (element.toInt() < 127 || unicode)) {
                            writer!!.write(element.toInt())
                        } else {
                            writer!!.write("&#" + element.toInt() + ";")
                        }
                    }
                }
                else -> if (element >= ' ' && element != '@' && (element.toInt() < 127 || unicode)) {
                    writer!!.write(element.toInt())
                } else {
                    writer!!.write("&#" + element.toInt() + ";")
                }
            }
            if (breakFlag) {
                break
            }
        }
    }

    /*
    	private final void writeIndent() throws IOException {
    		writer.write("\r\n");
    		for (int i = 0; i < depth; i++)
    			writer.write(' ');
    	}*/
    @Throws(IOException::class)
    override fun docdecl(dd: String) {
        writer!!.write("<!DOCTYPE")
        writer!!.write(dd)
        writer!!.write(">")
    }

    @Throws(IOException::class)
    override fun endDocument() {
        while (depth > 0) {
            endTag(
                    elementStack[depth * 3 - 3]!!,
                    elementStack[depth * 3 - 1]!!)
        }
        flush()
    }

    @Throws(IOException::class)
    override fun entityRef(name: String) {
        check(false)
        writer!!.write('&'.toInt())
        writer!!.write(name)
        writer!!.write(';'.toInt())
    }

    override fun getFeature(name: String): Boolean { //return false;
        return if ("http://xmlpull.org/v1/doc/features.html#indent-output"
                ==
                name) indent[depth] else false
    }

    override fun getPrefix(namespace: String?, create: Boolean): String? {
        return try {
            getPrefix(namespace, false, create)!!
        } catch (e: IOException) {
            throw java.lang.RuntimeException(e.toString())
        }
    }

    @Throws(IOException::class)
    private fun getPrefix(namespace: String?, includeDefault: Boolean, create: Boolean): String? {
        var i = nspCounts[depth + 1] * 2 - 2
        while (i >= 0) {
            if (nspStack[i + 1] == namespace && (includeDefault || nspStack[i] != "")) {
                var cand = nspStack[i]
                for (j in i + 2 until nspCounts[depth + 1] * 2) {
                    if (nspStack[j] == cand) {
                        cand = null
                        break
                    }
                }
                if (cand != null) return cand
            }
            i -= 2
        }
        if (!create) {
            return null
        }
        var prefix: String?
        if ("" == namespace) {
            prefix = ""
        } else {
            do {
                prefix = "n" + auto++
                i = nspCounts[depth + 1] * 2 - 2
                while (i >= 0) {
                    if (prefix == nspStack[i]) {
                        prefix = null
                        break
                    }
                    i -= 2
                }
            } while (prefix == null)
        }
        val p = pending
        pending = false
        setPrefix(prefix, namespace)
        pending = p
        return prefix
    }

    override fun getProperty(name: String): Any {
        throw java.lang.RuntimeException("Unsupported property")
    }

    @Throws(IOException::class)
    override fun ignorableWhitespace(s: String) {
        text(s)
    }

    override fun setFeature(name: String, value: Boolean) {
        if ("http://xmlpull.org/v1/doc/features.html#indent-output"
                == name) {
            indent[depth] = value
        } else throw java.lang.RuntimeException("Unsupported Feature")
    }

    override fun setProperty(name: String, value: Any) {
        throw java.lang.RuntimeException(
                "Unsupported Property:$value")
    }

    @Throws(IOException::class)
    override fun setPrefix(prefix: String?, namespace: String?) {
        val prefix2 = prefix ?: ""
        val namespace2 = namespace ?: ""
        check(false)
        val defined = getPrefix(namespace2, includeDefault = true, create = false)
        // boil out if already defined
        if (prefix2 == defined) return
        var pos = nspCounts[depth + 1]++ shl 1
        if (nspStack.size < pos + 1) {
            val hlp = arrayOfNulls<String>(nspStack.size + 16)
            System.arraycopy(nspStack, 0, hlp, 0, pos)
            nspStack = hlp
        }
        nspStack[pos++] = prefix2
        nspStack[pos] = namespace2
    }

    override fun setOutput(writer: Writer) {
        this.writer = writer
        // elementStack = new String[12]; //nsp/prefix/name
        //nspCounts = new int[4];
        //nspStack = new String[8]; //prefix/nsp
        //indent = new boolean[4];
        nspCounts[0] = 2
        nspCounts[1] = 2
        nspStack[0] = ""
        nspStack[1] = ""
        nspStack[2] = "xml"
        nspStack[3] = "http://www.w3.org/XML/1998/namespace"
        pending = false
        auto = 0
        depth = 0
        unicode = false
    }

    @Throws(IOException::class)
    override fun setOutput(os: OutputStream, encoding: String?) {
        setOutput(encoding?.let { OutputStreamWriter(os, it) } ?: OutputStreamWriter(os))
        this.encoding = encoding
        if (encoding != null && encoding.toLowerCase(Locale.US).startsWith("utf")) {
            unicode = true
        }
    }

    @Throws(IOException::class)
    override fun startDocument(encoding: String?, standalone: Boolean?) {
        writer!!.write("<?xml version='1.0' ")
        if (encoding != null) {
            this.encoding = encoding
            if (encoding.toLowerCase(Locale.US).startsWith("utf")) unicode = true
        }
        if (this.encoding != null) {
            writer!!.write("encoding='")
            writer!!.write(this.encoding!!)
            writer!!.write("' ")
        }
        if (standalone != null) {
            writer!!.write("standalone='")
            writer!!.write(
                    if (standalone) "yes" else "no")
            writer!!.write("' ")
        }
        writer!!.write("?>")
    }

    @Throws(IOException::class)
    override fun startTag(namespace: String?, name: String): XmlSerializer {
        check(false)
        //        if (namespace == null)
//            namespace = "";
        if (indent[depth]) {
            writer!!.write("\r\n")
            for (i in 0 until depth) writer!!.write("  ")
        }
        var esp = depth * 3
        if (elementStack.size < esp + 3) {
            val hlp = arrayOfNulls<String>(elementStack.size + 12)
            System.arraycopy(elementStack, 0, hlp, 0, esp)
            elementStack = hlp
        }
        val prefix = if (namespace == null) {
            ""
        } else {
            getPrefix(namespace, includeDefault = true, create = true)!!
        }
        if ("" == namespace) {
            for (i in nspCounts[depth] until nspCounts[depth + 1]) {
                check(!("" == nspStack[i * 2] && "" != nspStack[i * 2 + 1])) { "Cannot set default namespace for elements in no namespace" }
            }
        }
        elementStack[esp++] = namespace
        elementStack[esp++] = prefix
        elementStack[esp] = name
        writer!!.write('<'.toInt())
        if ("" != prefix) {
            writer!!.write(prefix)
            writer!!.write(':'.toInt())
        }
        writer!!.write(name)
        pending = true
        return this
    }

    @Throws(IOException::class)
    override fun attribute(namespace: String?, name: String, value: String): XmlSerializer {
        var namespaceVar: String? = namespace
        check(pending) { "illegal position for attribute" }
        //        int cnt = nspCounts[depth];
        if (namespaceVar == null) namespaceVar = ""
        //		depth--;
        //		pending = false;
        val prefix = if ("" == namespaceVar) "" else getPrefix(namespaceVar, includeDefault = false, create = true)!!
        //		pending = true;
        //		depth++;
        /*        if (cnt != nspCounts[depth]) {
                    writer.write(' ');
                    writer.write("xmlns");
                    if (nspStack[cnt * 2] != null) {
                        writer.write(':');
                        writer.write(nspStack[cnt * 2]);
                    }
                    writer.write("=\"");
                    writeEscaped(nspStack[cnt * 2 + 1], '"');
                    writer.write('"');
                }
                */writer!!.write(' '.toInt())
        if ("" != prefix) {
            writer!!.write(prefix)
            writer!!.write(':'.toInt())
        }
        writer!!.write(name)
        writer!!.write('='.toInt())
        val q = if (value.indexOf('"') == -1) '"' else '\''
        writer!!.write(q.toInt())
        writeEscaped(value, q.toInt())
        writer!!.write(q.toInt())
        return this
    }

    @Throws(IOException::class)
    override fun flush() {
        check(false)
        writer!!.flush()
    }

    /*
    	public void close() throws IOException {
    		check();
    		writer.close();
    	}
    */
    @Throws(IOException::class)
    override fun endTag(namespace: String?, name: String): XmlSerializer {
        if (!pending) depth--
        //        if (namespace == null)
        //          namespace = "";
        require(!((namespace == null
                && elementStack[depth * 3] != null)
                || (namespace != null
                && namespace != elementStack[depth * 3])
                || elementStack[depth * 3 + 2] != name)) { "</{$namespace}$name> does not match start" }
        if (pending) {
            check(true)
            depth--
        } else {
            if (indent[depth + 1]) {
                writer!!.write("\r\n")
                for (i in 0 until depth) writer!!.write("  ")
            }
            writer!!.write("</")
            val prefix = elementStack[depth * 3 + 1]!!
            if ("" != prefix) {
                writer!!.write(prefix)
                writer!!.write(':'.toInt())
            }
            writer!!.write(name)
            writer!!.write('>'.toInt())
        }
        nspCounts[depth + 1] = nspCounts[depth]
        return this
    }

    override fun getNamespace(): String? = if (getDepth() == 0) null else elementStack[getDepth() * 3 - 3]!!

    override fun getName(): String? = if (getDepth() == 0) null else elementStack[getDepth() * 3 - 1]!!

    override fun getDepth(): Int = if (pending) depth + 1 else depth

    @Throws(IOException::class)
    override fun text(text: String): XmlSerializer {
        check(false)
        indent[depth] = false
        writeEscaped(text, -1)
        return this
    }

    @Throws(IOException::class)
    override fun text(text: CharArray, start: Int, len: Int): XmlSerializer {
        text(String(text, start, len))
        return this
    }

    @Throws(IOException::class)
    override fun cdsect(data: String) {
        check(false)
        writer!!.write("<![CDATA[")
        writer!!.write(data)
        writer!!.write("]]>")
    }

    @Throws(IOException::class)
    override fun comment(comment: String) {
        check(false)
        writer!!.write("<!--")
        writer!!.write(comment)
        writer!!.write("-->")
    }

    @Throws(IOException::class)
    override fun processingInstruction(pi: String) {
        check(false)
        writer!!.write("<?")
        writer!!.write(pi)
        writer!!.write("?>")
    }
}
