package com.liang.example.json_ktx

import com.liang.example.json_ktx.SimpleJsonParser.Companion.allDigitCharRange
import com.liang.example.json_ktx.SimpleJsonParser.Companion.nonZeroDigitCharRange

// TODO: 去掉基本类型的 SimpleJsonValue 吧，只保留 SimpleJsonArray / SimpleJsonObject 就好了

open class SimpleJsonApi2 {
    open fun toJson(map: Map<String, Any>): String = "{" + map.toList().joinToString(",") { "${handleString(it.first)}:" + handleValue(it.second) } + "}"
    open fun toJson(array: List<Any>): String = "[" + array.joinToString(",") { handleValue(it) } + "]"
    open fun handleString(v: String): String = "\"${v.replace("\"", "\\\"")}\""
    open fun handleValue(v: Any): String = when (v) {
        is String -> handleString(v)
        is List<*> -> toJson(v as List<Any>)
        is Map<*, *> -> toJson(v as Map<String, Any>)
        else -> v.toString()
    }

    open class SimpleJsonParseTask2(_jsonStr: String, val strategy: JsonStyle, val throwEx: Boolean = false) {
        var jsonStr: String = _jsonStr
            set(value) {
                field = value
                length = value.length
            }
        protected var length = _jsonStr.length
        protected var index: Int = 0
        protected var failReason: String? = null

        fun run(jsonStr: String): Any {
            this.jsonStr = jsonStr
            index = 0
            failReason = null
            return run()
        }

        fun runOrNull(jsonStr: String): Any? {
            this.jsonStr = jsonStr
            index = 0
            failReason = null
            return runOrNull()
        }

        fun runOrNull(): Any? {
            val result = parseJson()
            if (index < length) {
                consumeGarbage()
                if (failReason != null) {
                    return null
                }
                if (index < length) {
                    failReason = "invalid format, can't parse end"
                    return null
                }
            }
            if (result == null && failReason == null) {
                failReason = "unknown reason"
            }
            return result
        }

        fun run(): Any {
            val result = parseJson() ?: return failReason ?: "unknown reason"
            if (index < length) {
                consumeGarbage()
                if (failReason != null) {
                    return failReason!!
                }
                if (index < length) {
                    return "invalid format, can't parse end"
                }
            }
            return result
        }

        protected fun parseNumber(): Number? {
            if (index >= length) {
                return makeFail<Number>("unexpected start of input in number", null)
            }
            val start = index
            // symbol part
            if (jsonStr[index] == '-') {
                index++
                if (index >= length) {
                    return makeFail<Number>("at least one digit required after -", null)
                }
            }
            // integer part
            if (jsonStr[index] == '0') {
                index++
                if (index >= length) {
                    return makeFail<Number>("-0 is not a invalid number's present", null)
                }
                if (jsonStr[index] in nonZeroDigitCharRange) {
                    return makeFail<Number>("leading 0s not permitted in numbers", null)
                }
            } else if (jsonStr[index] in allDigitCharRange) {
                index++
                while (index < length && jsonStr[index] in allDigitCharRange) {
                    index++
                }
                if (index >= length) {
                    return makeIntegerResult(jsonStr.substring(start))
                }
            } else {
                return makeFail<Number>("invalid ${jsonStr[index]} in number", null)
            }
            // dot part
            var ch = jsonStr[index]
            if (ch != '.' && ch != 'e' && ch != 'E') {
                return makeIntegerResult(jsonStr.substring(start, index))
            }
            // decimal part
            if (ch == '.') {
                index++
                if (index >= length || jsonStr[index] !in allDigitCharRange) {
                    return makeFail("at least one digit required in fractional part", null)
                }
                while (index < length && jsonStr[index] in allDigitCharRange) {
                    index++
                }
                if (index >= length) {
                    return makeDecimalResult(jsonStr.substring(start))
                }
            }
            // exponent part
            ch = jsonStr[index]
            if (ch == 'e' || ch == 'E') {
                index++
                if (index >= length) {
                    return makeFail<Number>("at least one digit required in exponent part", null)
                }
                ch = jsonStr[index]
                if (ch == '+' || ch == '-') {
                    index++
                    if (index >= length) {
                        return makeFail<Number>("at least one digit required in exponent part", null)
                    }
                }
                if (jsonStr[index] !in allDigitCharRange) {
                    return makeFail<Number>("at least one digit required in exponent part", null)
                }
                while (index < length && jsonStr[index] in allDigitCharRange) {
                    index++
                }
            }
            return makeDecimalResult(jsonStr.substring(start, index))
        }

        protected fun makeIntegerResult(s: String): Number? {
            return if (s[0] == '-' && s.length < 20 || s.length < 19) {
                s.toLongOrNull() ?: return makeFail("invalid integer number format", null)
            } else {
                s.toBigIntegerOrNull() ?: return makeFail("invalid big integer number format", null)
            }
        }

        protected fun makeDecimalResult(s: String): Number? {
            return if (s[0] == '-' && s.length < 20 || s.length < 19) {
                s.toDoubleOrNull() ?: return makeFail("invalid decimal number format", null)
            } else {
                s.toBigDecimalOrNull() ?: return makeFail("invalid big decimal number format", null)
            }
        }

        protected fun parseString(): String? = innerParseString()

        protected fun innerParseString(): String? {
            if (index >= length || jsonStr[index] != '"') {
                return makeFail<String>(
                        "unexpected start of input in string: ${if (index >= length) "end" else jsonStr[index].toString()}",
                        null
                )
            }
            index++
            val resultBuilder = StringBuilder()
            while (true) {
                if (index >= length) {
                    return makeFail<String>("unexpected end of input in string", null)
                }
                var ch = jsonStr[index++]
                if (ch == '"') {
                    return resultBuilder.toString()
                }
                if (ch == '\\') {
                    if (index >= length) {
                        return makeFail<String>("required one character after escape symbol", null)
                    }
                    ch = jsonStr[index++]
                    when (ch) {
                        '"' -> resultBuilder.append(ch)
                        else -> resultBuilder.append('\\') // else -> return makeFail<String>("invalid escape character $ch", null)
                    }
                } else {
                    resultBuilder.append(ch)
                }
            }
        }

        protected fun parseArray(): List<Any?>? {
            if (index >= length || jsonStr[index] != '[') {
                return makeFail<List<Any?>>(
                        "unexpected start of input in array: ${if (index >= length) "end" else
                            jsonStr[index].toString()}", null
                )
            }
            index++
            var ch = getNextToken() ?: return makeFail<List<Any?>>("invalid array's present, lack of \"]\"", null)
            val itemList = mutableListOf<Any?>()
            if (ch != ']') {
                index--
                while (true) {
                    itemList.add(parseJson() ?: return null)
                    ch = getNextToken()
                            ?: return makeFail<List<Any?>>("invalid array's present, lack of \"]\"", null)
                    if (ch == ']') {
                        break
                    }
                    if (ch != ',') {
                        return makeFail<List<Any?>>("expected ',' in list, got '$ch'", null)
                    }
                    consumeGarbage()
                }
            }
            return itemList
        }

        protected fun parseObject(): Map<String, Any?>? {
            if (index >= length || jsonStr[index] != '{') {
                return makeFail<Map<String, Any?>>(
                        "unexpected start of input in object: ${if (index >= length) "end" else
                            jsonStr[index].toString()}", null
                )
            }
            index++
            var ch = getNextToken()
                    ?: return makeFail<Map<String, Any?>>("invalid object's present, lack of \"}\"", null)
            val itemMap = mutableMapOf<String, Any?>()
            if (ch != '}') {
                index--
                while (true) {
                    val key = innerParseString() ?: return null
                    ch = getNextToken()
                            ?: return makeFail<Map<String, Any?>>("invalid object's present, lack of \"}\"", null)
                    if (ch != ':') {
                        return makeFail<Map<String, Any?>>("expected ':' in object, got '$ch'", null)
                    }
                    itemMap[key] = (parseJson() ?: return null)
                    ch = getNextToken()
                            ?: return makeFail<Map<String, Any?>>("invalid object's present, lack of \"}\"", null)
                    if (ch == '}') {
                        break
                    }
                    if (ch != ',') {
                        return makeFail<Map<String, Any?>>("expected ',' in object, got '$ch'", null)
                    }
                    consumeGarbage()
                }
            }
            return itemMap
        }

        protected fun parseJson(): Any? {
            val ch = getNextToken() ?: return makeFail<Char>("json format is incorrect, the json string become empty before end parsing", null)
            index--
            return when (ch) {
                '-', in allDigitCharRange -> parseNumber()
                't' -> expectJsonValue("true", true)
                'f' -> expectJsonValue("false", false)
                'n' -> expectJsonValue("null", null)
                '"' -> parseString()
                '{' -> parseObject()
                '[' -> parseArray()
                else -> makeFail("expected value, got $ch", null)
            }
        }

        protected fun expectJsonValue(expected: String, result: Any?): Any? {
            if (length - index < expected.length) {
                index = length
                return makeFail<Any>("parse error -- expected: $expected, got: ${jsonStr.substring(index)}", null)
            }
            val start = index
            expected.forEach {
                if (it != jsonStr[index++]) {
                    return makeFail<Any>(
                            "parse error -- expected: $expected, got: " + jsonStr.substring(
                                    start, start
                                    + expected.length
                            ), null
                    )
                }
            }
            return result
        }

        protected fun getNextToken(): Char? {
            consumeGarbage()
            return if (index >= length) makeFail<Char>("unexpected end of input", null) else jsonStr[index++]
        }

        protected fun consumeGarbage() {
            consumeWhiteSpaces()
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
            if (index < length && jsonStr[index] == '/') {
                index++
                if (index >= length) {
                    return makeFail("unexpected end of input after start of comment", false) as Boolean
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
                            return makeFail("unexpected end of input inside multi-line comment", false) as Boolean
                        }
                        var indexPlusOne = index + 1
                        while (!(jsonStr[index] == '*' && jsonStr[indexPlusOne] == '/')) {
                            index = indexPlusOne
                            indexPlusOne++
                            if (indexPlusOne == length) {
                                index = length
                                return makeFail("unexpected end of input inside multi-line comment", false) as Boolean
                            }
                        }
                        index += 2
                    }
                    else -> return makeFail("malformed comment", false) as Boolean
                }
                return true
            }
            return false
        }

        protected fun consumeWhiteSpaces() {
            while (index < length && (jsonStr[index] == ' ' || jsonStr[index] == '\r' || jsonStr[index] == '\n' || jsonStr[index] == '\t')) {
                index++
            }
        }

        protected fun <T> makeFail(reason: String, result: T?): T? {
            failReason = reason
            if (throwEx) {
                throw RuntimeException("failReason: $failReason, index: $index, length: $length, less: ${if (index < length) jsonStr.substring(index) else "none"}")
            }
            return result
        }
    }

    open fun parseJsonOrThrow(jsonStr: String, strategy: JsonStyle = JsonStyle.STANDARD): Any = SimpleJsonParseTask2(jsonStr, strategy, true).run()
    open fun parseJsonOrNull(jsonStr: String, strategy: JsonStyle = JsonStyle.STANDARD): Any? = SimpleJsonParseTask2(jsonStr, strategy, true).runOrNull()
}
