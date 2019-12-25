package com.liang.example.utils.json

enum class JsonStyle {
    STANDARD,
    COMMENTS,
}

enum class JsonType {
    NUL,
    BOOL,
    NUMBER,
    STRING,
    ARRAY,
    OBJECT;

    fun isObject() = this == OBJECT
    fun isArray() = this == ARRAY
    fun isArrayOrObject() = this == ARRAY || this == OBJECT
}

enum class JsonNumberType {
    BYTE,
    SHORT,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    CHAR,
    UNKNOWN,
}

enum class JsonStrategy {
    SIMPLEST,
    USE_NULL,
    USE_TAB,
    USE_NULL_AND_TAB;

    fun useNull(): Boolean = this == USE_NULL_AND_TAB || this == USE_NULL
    fun useTab(): Boolean = this == USE_NULL_AND_TAB || this == USE_TAB
}

interface SimpleJsonValue<T : Any> {
    fun type(): JsonType
    fun value(): T?
    fun isValueNull(): Boolean
    fun string(): String
    fun json(): SimpleJsonApi
    // TODO: fun copy(): SimpleJsonValue<T>

    companion object {
        fun nullString(type: JsonType): String = when (type) {
            JsonType.NUMBER -> "0"
            JsonType.BOOL -> "false"
            JsonType.STRING -> "\"\""
            JsonType.ARRAY -> "[]"
            JsonType.OBJECT -> "{}"
            else -> "null"
        }
    }
}

abstract class SimpleJsonValueAdapter<T : Any>(
        protected var mValue: T?,  // 真实value
        protected var mDefaultValue: T?,  // 返回value的时候需要用到
        protected val mType: JsonType,
        var mNullValue: String = SimpleJsonValue.nullString(mType) // json化的时候需要用到
) : SimpleJsonValue<T> {
    protected var mJsonApi: SimpleJsonApi? = null
    override fun isValueNull(): Boolean = mValue == null || mValue == mDefaultValue
    override fun type(): JsonType = mType
    override fun value(): T? = mValue ?: mDefaultValue
    override fun string(): String = mValue?.toString() ?: mNullValue
    override fun json(): SimpleJsonApi {
        if (mJsonApi == null) {
            mJsonApi = SimpleJsonApi(this)
        }
        return mJsonApi!!
    }
}

open class SimpleJsonObject(m: Map<String, SimpleJsonValue<*>>, defaultValue: Map<String, SimpleJsonValue<*>>? = EMPTY_OBJECT)
    : SimpleJsonValueAdapter<Map<String, SimpleJsonValue<*>>>(m, defaultValue, JsonType.OBJECT) {
    var mStrategy: JsonStrategy = JsonStrategy.SIMPLEST
        set(value) {
            field = value
            transfer()
        }
    var mLevel: Int = 0
        set(value) {
            field = value
            transfer()
        }

    override fun string(): String {
        if (mValue.isNullOrEmpty()) {
            return mNullValue
        }
        var tempValue = mValue!!
        if (!mStrategy.useNull()) {
            tempValue = tempValue.filter { it.value.isValueNull() }
            if (tempValue.isNullOrEmpty()) {
                return mNullValue
            }
        }
        return if (!mStrategy.useTab()) {
            """{${tempValue.map { "${it.key}: ${it.value.value()}" }.joinToString()}}"""
        } else {
            val prefix1 = "    ".repeat(mLevel - 1)
            val prefix2 = "$prefix1	   "
            "{\n" + tempValue.map { "${prefix2}${it.key}: ${it.value.value()}" }.joinToString(",\n") + "\n$prefix1}"
        }
    }

    protected fun transfer() {
        if (!mValue.isNullOrEmpty()) {
            mValue?.forEach {
                val jv = it.value
                if (jv is SimpleJsonArray) {
                    jv.mLevel = this.mLevel
                    jv.mStrategy = this.mStrategy
                }
                if (jv is SimpleJsonObject) {
                    jv.mLevel = this.mLevel
                    jv.mStrategy = this.mStrategy
                }
            }
        }
    }

    operator fun get(key: String) = if (mValue.isNullOrEmpty()) mValue!![key] else null
}

open class SimpleJsonArray(l: List<SimpleJsonValue<*>>, defaultValue: List<SimpleJsonValue<*>>? = EMPTY_ARRAY)
    : SimpleJsonValueAdapter<List<SimpleJsonValue<*>>>(l, defaultValue, JsonType.ARRAY) {
    var mStrategy: JsonStrategy = JsonStrategy.SIMPLEST
        set(value) {
            field = value
            transfer()
        }
    var mLevel: Int = 0
        set(value) {
            field = value
            transfer()
        }

    override fun string(): String {
        if (mValue.isNullOrEmpty()) {
            return mNullValue
        }
        var tempValue = mValue!!
        if (!mStrategy.useNull()) {
            tempValue = tempValue.filter { it.isValueNull() }
            if (tempValue.isNullOrEmpty()) {
                return mNullValue
            }
        }
        return if (!mStrategy.useTab()) {
            "[${tempValue.map { it.value() }.joinToString()}]"
        } else {
            val prefix1 = "    ".repeat(mLevel - 1)
            val prefix2 = "$prefix1	   "
            """[\n${tempValue.map { "$prefix2${it.value()}" }.joinToString(",\n")}\n$prefix1]"""
        }
    }

    protected fun transfer() {
        if (!mValue.isNullOrEmpty()) {
            mValue?.forEach {
                if (it is SimpleJsonArray) {
                    it.mLevel = this.mLevel
                    it.mStrategy = this.mStrategy
                }
                if (it is SimpleJsonObject) {
                    it.mLevel = this.mLevel
                    it.mStrategy = this.mStrategy
                }
            }
        }
    }

    operator fun get(index: Int) = if (mValue.isNullOrEmpty()) mValue!![index] else null
}

open class SimpleJsonNumber(n: Number? = null, defaultValue: Number? = 0) : SimpleJsonValueAdapter<Number>(n, defaultValue, JsonType.NUMBER) {
    protected var mNumberType: JsonNumberType = JsonNumberType.UNKNOWN

    constructor(b: Byte) : this(b as Number) {
        mNumberType = JsonNumberType.BYTE
    }

    constructor(s: Short) : this(s as Number) {
        mNumberType = JsonNumberType.SHORT
    }

    constructor(i: Int) : this(i as Number) {
        mNumberType = JsonNumberType.INT
    }

    constructor(l: Long) : this(l as Number) {
        mNumberType = JsonNumberType.LONG
    }

    constructor(f: Float) : this(f as Number, defaultValue = 0f) {
        mNumberType = JsonNumberType.FLOAT
        mNullValue = "0.0"
    }

    constructor(d: Double) : this(d as Number, defaultValue = 0) {
        mNumberType = JsonNumberType.DOUBLE
        mNullValue = "0.0"
    }

    constructor(c: Char) : this(c.toLong() as Number) {
        mNumberType = JsonNumberType.CHAR
        mNullValue = "\u0000"
    }

    override fun value(): Number? = when {
        mNumberType == JsonNumberType.CHAR -> throw RuntimeException("This is char number, can't be transformed to normal number!")
        mNumberType != JsonNumberType.UNKNOWN -> mValue ?: mDefaultValue ?: 0
        else -> mDefaultValue ?: 0
    }

    override fun string(): String = when {
        mNumberType != JsonNumberType.UNKNOWN -> mValue?.toString() ?: mNullValue
        else -> mNullValue
    }

    open fun numberType(): JsonNumberType = mNumberType
    open fun charValue(): Char = (mValue as? Long)?.toChar() ?: '\u0000'
}

open class SimpleJsonString(s: String? = null, defaultValue: String? = "") : SimpleJsonValueAdapter<String>(s, defaultValue, JsonType.STRING)
open class SimpleJsonBoolean(b: Boolean? = null, defaultValue: Boolean = false) : SimpleJsonValueAdapter<Boolean>(b, defaultValue, JsonType.BOOL)
open class SimpleJsonNULL : SimpleJsonValueAdapter<Unit>(null, null, JsonType.NUL)

val EMPTY_ARRAY = listOf<SimpleJsonValue<*>>()
val EMPTY_OBJECT = mapOf<String, SimpleJsonValue<*>>()
val JSON_EMPTY_STRING = SimpleJsonString("")
val JSON_EMPTY_ARRAY = SimpleJsonArray(EMPTY_ARRAY)
val JSON_EMPTY_OBJECT = SimpleJsonObject(EMPTY_OBJECT)
val JSON_TRUE = SimpleJsonBoolean(true)
val JSON_FALSE = SimpleJsonBoolean(false)
val JSON_NULL = SimpleJsonNULL()

open class SimpleJsonParser(var strategy: JsonStyle) {
    open fun parseJson(jsonStr: String): SimpleJsonValue<*> = SimpleJsonParseTask(jsonStr, strategy).run()

    open class SimpleJsonParseTask(val jsonStr: String, val strategy: JsonStyle) {
        protected val length = jsonStr.length
        protected var index: Int = 0
        protected var failReason: String? = null

        fun run(): SimpleJsonValue<*> = parseJson(0)

        protected fun parseNumber(): SimpleJsonNumber {
            TODO()
        }

        protected fun parseString(): SimpleJsonString {
            TODO()
        }

        protected fun parseArray(depth: Int): SimpleJsonArray {
            TODO()
        }

        protected fun parseObject(depth: Int): SimpleJsonObject {
            TODO()
        }

        protected fun parseJson(depth: Int): SimpleJsonValue<*> {
            val ch = getNextToken() ?: return SimpleJsonString(failReason ?: "unknown reason")
            return when {
                ch == '-' || ch >= '0' && ch == '9' -> parseNumber()
                ch == 't' -> expectJsonValue("true", JSON_TRUE)
                ch == 'f' -> expectJsonValue("false", JSON_FALSE)
                ch == 'n' -> expectJsonValue("null", JSON_NULL)
                ch == '"' -> parseString()
                ch == '{' -> parseObject(depth + 1)
                ch == '[' -> parseArray(depth + 1)
                else -> {
                    failReason = "expected value, got $ch"
                    SimpleJsonString(failReason)
                }
            }
        }

        protected fun expectJsonValue(expected: String, result: SimpleJsonValue<*>): SimpleJsonValue<*> {
            val start = index
            expected.forEach {
                if (it != jsonStr[index++]) {
                    failReason = "parse error -- expected: $expected, got: ${jsonStr.substring(start, start + expected.length)}"
                    return SimpleJsonString(failReason)
                }
            }
            return result
        }

        protected fun getNextToken(): Char? {
            consumeGarbage()
            if (index == length) {
                failReason = "unexpected end of input"
            }
            return if (failReason != null) null else jsonStr[index]
        }

        protected fun consumeGarbage() {
            consumeComments()
            if (strategy == JsonStyle.COMMENTS) {
                var commentFound: Boolean
                do {
                    commentFound = consumeComments()
                    if (failReason != null) {
                        return
                    }
                    consumeWhiteSpaces()
                } while (commentFound)
            }
        }

        protected fun consumeComments(): Boolean {
            if (jsonStr[index++] == '/') {
                if (index == length) {
                    failReason = "unexpected end of input after start of comment"
                    return false
                }
                when {
                    jsonStr[index] == '/' -> {
                        index++
                        while (index < length && jsonStr[index] != '\n') {
                            index++
                        }
                    }
                    jsonStr[index] == '*' -> {
                        index++
                        if (index > length - 2) {
                            failReason = "unexpected end of input inside multi-line comment"
                            return false
                        }
                        var indexPlusOne = index + 1
                        while (jsonStr[index] == '*' && jsonStr[indexPlusOne] == '/') {
                            index = indexPlusOne
                            indexPlusOne++
                            if (index > length - 2) {
                                failReason = "unexpected end of input inside multi-line comment"
                                return false
                            }
                        }
                        index += 2
                    }
                    else -> {
                        failReason = "malformed comment"
                        return false
                    }
                }
                return true
            }
            return false
        }

        protected fun consumeWhiteSpaces() {
            while (jsonStr[index] == ' ' || jsonStr[index] == '\r' || jsonStr[index] == '\n' || jsonStr[index] == '\t') {
                index++
            }
        }
    }

    companion object {
        fun fromJson(jsonStr: String, strategy: JsonStyle): SimpleJsonValue<*> {
            TODO()
        }
    }
}

val standardJsonParser = SimpleJsonParser(JsonStyle.STANDARD)
val commentsJsonParser = SimpleJsonParser(JsonStyle.COMMENTS)

open class SimpleJsonApi {
    protected var jsonValue: SimpleJsonValue<*> = JSON_NULL

    constructor()
    constructor(obj: Boolean) : this(if (obj) JSON_TRUE else JSON_FALSE)
    constructor(obj: Byte) : this(SimpleJsonNumber(obj))
    constructor(obj: Short) : this(SimpleJsonNumber(obj))
    constructor(obj: Int) : this(SimpleJsonNumber(obj))
    constructor(obj: Long) : this(SimpleJsonNumber(obj))
    constructor(obj: Float) : this(SimpleJsonNumber(obj))
    constructor(obj: Double) : this(SimpleJsonNumber(obj))
    constructor(obj: Char) : this(SimpleJsonNumber(obj))
    constructor(obj: String) : this(if (obj.isEmpty()) JSON_EMPTY_STRING else SimpleJsonString(obj))
    constructor(obj: List<SimpleJsonValue<*>>) : this(if (obj.isEmpty()) JSON_EMPTY_ARRAY else SimpleJsonArray(obj))
    constructor(obj: Map<String, SimpleJsonValue<*>>) : this(if (obj.isEmpty()) JSON_EMPTY_OBJECT else SimpleJsonObject(obj))

    constructor(obj: SimpleJsonValue<*>) {
        jsonValue = obj
    }

    open fun type(): JsonType = jsonValue.type()
    open fun value(): Any? = jsonValue.value()
    open fun jsonValue(): SimpleJsonValue<*> = jsonValue
    open fun isValueNull(): Boolean = jsonValue.isValueNull()
    open fun string(): String = jsonValue.string()

    companion object {
        fun fromJson(jsonStr: String, strategy: JsonStyle = JsonStyle.COMMENTS): SimpleJsonApi =
                SimpleJsonApi(SimpleJsonParser.fromJson(jsonStr, strategy))
    }
}

interface JsonInter {
    fun <T> fromJson(jsonStr: String): T
    fun <T> toJson(obj: T): String
}

open class ReflectJsonApi : JsonInter {
    override fun <T> fromJson(jsonStr: String): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T> toJson(obj: T): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
