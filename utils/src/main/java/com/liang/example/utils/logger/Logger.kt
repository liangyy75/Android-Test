package com.liang.example.utils.logger

import android.util.Log
import java.io.*
import kotlin.collections.ArrayList

enum class LoggerLevel {
    VERBOSE, DEBUG, INFO, WARN, ERROR, FATAL, NONE;

    override fun toString(): String {
        return super.toString().padEnd(7, ' ')
    }
}

var DEFAULT_LEVEL: LoggerLevel = LoggerLevel.VERBOSE
var DEFAULT_TAG: String = "LoggerApi"
private val EMPTY_ARRAY = emptyArray<Any>()

// TODO: path
/**
 * log(Object, String)
 * log(Object, Throwable)
 * log(Object, String, Object...)
 * log(Object, String, Throwable, Object...)
 */
interface LoggerInter {
    /**
     * format ==>
     * 0 -- Thread.java/1559 java.lang.Thread/getStackTrace
     * 1 -- LoggerFormatter.kt/36 com.liangyy75.kotlin.logger.DefaultLoggerFormatter/formatMsg
     * 2 -- Logger.kt/239 com.liangyy75.kotlin.logger.FormatLogger/logImpl
     * 3 -- Logger.kt/206 com.liangyy75.kotlin.logger.LogLoggerImpl/log
     * 4 -- Logger.kt/209 com.liangyy75.kotlin.logger.LogLoggerImpl/v
     * 5 -- Logger.kt/161 com.liangyy75.kotlin.logger.MultiLogger/v
     * 6 -- Logger.kt/58 com.liangyy75.kotlin.logger.LoggerInter$DefaultImpls/v
     * 7 -- Logger.kt/147 com.liangyy75.kotlin.logger.MultiLogger/v
     * 8 -- TestLogger.kt/27 com.liangyy75.kotlin.test.TestLoggerKt/testMultipleHandlersLogger -- final
     */
    var logLevel: LoggerLevel

    fun isLoggable(level: LoggerLevel): Boolean = level >= logLevel

    fun v(tag: Any, msg: String): Int = v(tag, msg, t = null, args = *EMPTY_ARRAY, depth = 4)
    fun d(tag: Any, msg: String): Int = d(tag, msg, t = null, args = *EMPTY_ARRAY, depth = 4)
    fun i(tag: Any, msg: String): Int = i(tag, msg, t = null, args = *EMPTY_ARRAY, depth = 4)
    fun w(tag: Any, msg: String): Int = w(tag, msg, t = null, args = *EMPTY_ARRAY, depth = 4)
    fun e(tag: Any, msg: String): Int = e(tag, msg, t = null, args = *EMPTY_ARRAY, depth = 4)
    fun wtf(tag: Any, msg: String): Int = wtf(tag, msg, t = null, args = *EMPTY_ARRAY, depth = 4)

    fun v(tag: Any, t: Throwable?): Int = v(tag, "", t = null, args = *EMPTY_ARRAY, depth = 4)
    fun d(tag: Any, t: Throwable?): Int = d(tag, "", t = null, args = *EMPTY_ARRAY, depth = 4)
    fun i(tag: Any, t: Throwable?): Int = i(tag, "", t = null, args = *EMPTY_ARRAY, depth = 4)
    fun w(tag: Any, t: Throwable?): Int = w(tag, "", t = null, args = *EMPTY_ARRAY, depth = 4)
    fun e(tag: Any, t: Throwable?): Int = e(tag, "", t = null, args = *EMPTY_ARRAY, depth = 4)
    fun wtf(tag: Any, t: Throwable?): Int = wtf(tag, "", t = null, args = *EMPTY_ARRAY, depth = 4)

    fun v(tag: Any, msg: String, vararg args: Any?): Int = v(tag, msg, t = null, args = *args, depth = 4)
    fun d(tag: Any, msg: String, vararg args: Any?): Int = d(tag, msg, t = null, args = *args, depth = 4)
    fun i(tag: Any, msg: String, vararg args: Any?): Int = i(tag, msg, t = null, args = *args, depth = 4)
    fun w(tag: Any, msg: String, vararg args: Any?): Int = w(tag, msg, t = null, args = *args, depth = 4)
    fun e(tag: Any, msg: String, vararg args: Any?): Int = e(tag, msg, t = null, args = *args, depth = 4)
    fun wtf(tag: Any, msg: String, vararg args: Any?): Int = wtf(tag, msg, t = null, args = *args, depth = 4)

    fun v(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int = v(tag, msg, t = t, args = *args, depth = 4)
    fun d(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int = d(tag, msg, t = t, args = *args, depth = 4)
    fun i(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int = i(tag, msg, t = t, args = *args, depth = 4)
    fun w(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int = w(tag, msg, t = t, args = *args, depth = 4)
    fun e(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int = e(tag, msg, t = t, args = *args, depth = 4)
    fun wtf(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int =
            wtf(tag, msg, t = t, args = *args, depth = 4)

    fun v(
            tag: Any = DEFAULT_TAG,
            msg: String,
            t: Throwable? = null,
            vararg args: Any? = EMPTY_ARRAY,
            depth: Int = 3
    ): Int

    fun d(
            tag: Any = DEFAULT_TAG,
            msg: String,
            t: Throwable? = null,
            vararg args: Any? = EMPTY_ARRAY,
            depth: Int = 3
    ): Int

    fun i(
            tag: Any = DEFAULT_TAG,
            msg: String,
            t: Throwable? = null,
            vararg args: Any? = EMPTY_ARRAY,
            depth: Int = 3
    ): Int

    fun w(
            tag: Any = DEFAULT_TAG,
            msg: String,
            t: Throwable? = null,
            vararg args: Any? = EMPTY_ARRAY,
            depth: Int = 3
    ): Int

    fun e(
            tag: Any = DEFAULT_TAG,
            msg: String,
            t: Throwable? = null,
            vararg args: Any? = EMPTY_ARRAY,
            depth: Int = 3
    ): Int

    fun wtf(
            tag: Any = DEFAULT_TAG,
            msg: String,
            t: Throwable? = null,
            vararg args: Any? = EMPTY_ARRAY,
            depth: Int = 3
    ): Int
}

interface NamedLoggerInter : LoggerInter {
    var name: String
}

class AndroidLogLogger(override var logLevel: LoggerLevel = DEFAULT_LEVEL) : LoggerInter {
    private inline fun log(tag: String, msg: String, t: Throwable?, method1: (String, String) -> Int,
                           method2: (String, String, Throwable) -> Int, level: LoggerLevel, vararg args: Any?): Int =
            when {
                logLevel > level -> 0
                t == null -> method1.invoke(tag, if (args.isNotEmpty()) String.format(msg, *args) else msg)
                else -> method2.invoke(tag, if (args.isNotEmpty()) String.format(msg, *args) else msg, t)
            }

    override fun v(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            log(tag.toString(), msg, t, { t2, m2 -> Log.v(t2, m2) }, { t2, m2, t3 -> Log.v(t2, m2, t3) }, LoggerLevel.VERBOSE, *args)

    override fun d(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            log(tag.toString(), msg, t, { t2, m2 -> Log.d(t2, m2) }, { t2, m2, t3 -> Log.d(t2, m2, t3) }, LoggerLevel.DEBUG, *args)

    override fun i(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            log(tag.toString(), msg, t, { t2, m2 -> Log.i(t2, m2) }, { t2, m2, t3 -> Log.i(t2, m2, t3) }, LoggerLevel.INFO, *args)

    override fun w(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            log(tag.toString(), msg, t, { t2, m2 -> Log.w(t2, m2) }, { t2, m2, t3 -> Log.w(t2, m2, t3) }, LoggerLevel.WARN, *args)

    override fun e(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            log(tag.toString(), msg, t, { t2, m2 -> Log.e(t2, m2) }, { t2, m2, t3 -> Log.e(t2, m2, t3) }, LoggerLevel.ERROR, *args)

    override fun wtf(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            log(tag.toString(), msg, t, { t2, m2 -> Log.wtf(t2, m2) }, { t2, m2, t3 -> Log.wtf(t2, m2, t3) }, LoggerLevel.FATAL, *args)
}

class FormatAndroidLogLogger(override var logLevel: LoggerLevel = DEFAULT_LEVEL) : NamedLoggerInter {
    override var name: String = ""
    val loggerFormatter = AndroidLoggerFormatter()
    var path = 7

    /**
     * format ==>
     * 0 -- VMStack.java/-2 dalvik.system.VMStack/getThreadStackTrace
     * 1 -- Thread.java/1538 java.lang.Thread/getStackTrace
     * 2 -- LoggerFormatter.kt/79 com.com.liang.example.utils.logger.AndroidLoggerFormatter/formatMsg
     * 3 -- Logger.kt/429 com.com.liang.example.utils.logger.FormatAndroidLogLogger/d
     * 4 -- Logger.kt/237 com.com.liang.example.utils.logger.MultiLogger/d
     * 5 -- Logger.kt/39 com.com.liang.example.utils.logger.LoggerInter$DefaultImpls/d
     * 6 -- Logger.kt/217 com.com.liang.example.utils.logger.MultiLogger/d
     * 7 -- ...
     */
    private inline fun log(tag: String, msg: String, t: Throwable?, method1: (String, String) -> Int,
                           method2: (String, String, Throwable) -> Int, level: LoggerLevel, vararg args: Any?): Int {
        val msgStr = if (args.isNotEmpty()) java.lang.String.format(msg, *args) else msg
        val formatMsg = loggerFormatter.formatMsg(name, "", null, level, msgStr, path)
        return when {
            logLevel > level -> 0
            t == null -> method1.invoke(tag, formatMsg)
            else -> method2.invoke(tag, formatMsg, t)
        }
    }

    override fun v(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            log(tag.toString(), msg, t, { t2, m2 -> Log.v(t2, m2) }, { t2, m2, t3 -> Log.v(t2, m2, t3) }, LoggerLevel.VERBOSE, *args)

    override fun d(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            log(tag.toString(), msg, t, { t2, m2 -> Log.d(t2, m2) }, { t2, m2, t3 -> Log.d(t2, m2, t3) }, LoggerLevel.DEBUG, *args)

    override fun i(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            log(tag.toString(), msg, t, { t2, m2 -> Log.i(t2, m2) }, { t2, m2, t3 -> Log.i(t2, m2, t3) }, LoggerLevel.INFO, *args)

    override fun w(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            log(tag.toString(), msg, t, { t2, m2 -> Log.w(t2, m2) }, { t2, m2, t3 -> Log.w(t2, m2, t3) }, LoggerLevel.WARN, *args)

    override fun e(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            log(tag.toString(), msg, t, { t2, m2 -> Log.e(t2, m2) }, { t2, m2, t3 -> Log.e(t2, m2, t3) }, LoggerLevel.ERROR, *args)

    override fun wtf(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            log(tag.toString(), msg, t, { t2, m2 -> Log.wtf(t2, m2) }, { t2, m2, t3 -> Log.wtf(t2, m2, t3) }, LoggerLevel.FATAL, *args)
}

class NullableLogger(override var logLevel: LoggerLevel = DEFAULT_LEVEL, useDefault: Boolean = false) : LoggerInter {
    var logger: LoggerInter? = null

    init {
        if (useDefault) logger = AndroidLogLogger(logLevel)
    }

    override fun v(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            logger?.v(tag, msg, t, *args, depth = depth + 1) ?: 0

    override fun d(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            logger?.d(tag, msg, t = t, args = *args, depth = depth + 1) ?: 0

    override fun i(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            logger?.i(tag, msg, t = t, args = *args, depth = depth + 1) ?: 0

    override fun w(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            logger?.w(tag, msg, t = t, args = *args, depth = depth + 1) ?: 0

    override fun e(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            logger?.e(tag, msg, t = t, args = *args, depth = depth + 1) ?: 0

    override fun wtf(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            logger?.wtf(tag, msg, t = t, args = *args, depth = depth + 1) ?: 0

    override fun isLoggable(level: LoggerLevel): Boolean = logger?.isLoggable(level) ?: false
}

open class MultiLogger(override var logLevel: LoggerLevel = DEFAULT_LEVEL) : LoggerInter {
    protected val loggers = mutableMapOf<String, LoggerInter>()

    fun getLogger(name: String) = loggers[name]
    fun hasLogger(name: String) = loggers.containsKey(name)
    fun removeLogger(name: String) = loggers.remove(name)
    fun addLogger(name: String, logger: LoggerInter) {
        loggers[name] = logger
        if (logger is NamedLoggerInter) logger.name = name
    }

    override fun v(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            if (logLevel < LoggerLevel.VERBOSE || loggers.isEmpty()) 0
            else loggers.toList().map {
                it.second.v(tag, msg, t, *args, depth = depth + 1)
            }.sum()

    override fun d(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            if (logLevel < LoggerLevel.DEBUG || loggers.isEmpty()) 0
            else loggers.toList().map {
                it.second.d(tag, msg, t, *args, depth = depth + 1)
            }.sum()

    override fun i(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            if (logLevel < LoggerLevel.INFO || loggers.isEmpty()) 0
            else loggers.toList().map {
                it.second.i(tag, msg, t, *args, depth = depth + 1)
            }.sum()

    override fun w(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            if (logLevel < LoggerLevel.WARN || loggers.isEmpty()) 0
            else loggers.toList().map {
                it.second.w(tag, msg, t, *args, depth = depth + 1)
            }.sum()

    override fun e(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            if (logLevel < LoggerLevel.ERROR || loggers.isEmpty()) 0
            else loggers.toList().map {
                it.second.e(tag, msg, t, *args, depth = depth + 1)
            }.sum()

    override fun wtf(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            if (logLevel < LoggerLevel.FATAL || loggers.isEmpty()) 0
            else loggers.toList().map {
                it.second.wtf(tag, msg, t, *args, depth = depth + 1)
            }.sum()
}

abstract class LogLoggerImpl(override var logLevel: LoggerLevel = DEFAULT_LEVEL) : LoggerInter {
    abstract fun logImpl(
            tag: String,
            msg: String,
            t: Throwable?,
            level: LoggerLevel,
            vararg args: Any?,
            depth: Int
    ): Int

    open fun log(tag: String, msg: String, t: Throwable?, level: LoggerLevel, vararg args: Any?, depth: Int): Int =
            if (logLevel >= level) logImpl(tag, msg, t, level, *args, depth = depth + 1) else 0

    override fun v(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            log(tag.toString(), msg, t, LoggerLevel.VERBOSE, *args, depth = depth + 1)

    override fun d(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            log(tag.toString(), msg, t, LoggerLevel.DEBUG, *args, depth = depth + 1)

    override fun i(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            log(tag.toString(), msg, t, LoggerLevel.INFO, *args, depth = depth + 1)

    override fun w(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            log(tag.toString(), msg, t, LoggerLevel.WARN, *args, depth = depth + 1)

    override fun e(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            log(tag.toString(), msg, t, LoggerLevel.ERROR, *args, depth = depth + 1)

    override fun wtf(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            log(tag.toString(), msg, t, LoggerLevel.FATAL, *args, depth = depth + 1)
}

abstract class FormatLogger(logLevel: LoggerLevel = DEFAULT_LEVEL) : LogLoggerImpl(logLevel), NamedLoggerInter {
    override var name: String = ""
    val loggerFormatter = DefaultLoggerFormatter()

    override fun logImpl(
            tag: String,
            msg: String,
            t: Throwable?,
            level: LoggerLevel,
            vararg args: Any?,
            depth: Int
    ): Int =
            formatLog(loggerFormatter.formatMsg(name, tag, t, level, msg, depth + 1, *args))

    abstract fun formatLog(msg: String): Int
}

abstract class CachedFormatLogger(
        private val closable: Closeable,
        private val flushable: Flushable,
        private val newLine: Boolean = false,
        private val maxCachedSize: Int = 64,
        logLevel: LoggerLevel = DEFAULT_LEVEL
) : FormatLogger(logLevel) {
    protected val cachedMsgList: ArrayList<String> = ArrayList(maxCachedSize)
    private var nowCachedSize: Int = 0

    override fun formatLog(msg: String): Int {
        cachedMsgList.add(if (newLine) msg + "\n" else msg)
        nowCachedSize++
        if (nowCachedSize == maxCachedSize) {
            cachedFormatLog()
            cachedMsgList.clear()
            nowCachedSize = 0
        }
        return msg.length
    }

    abstract fun cachedFormatLog()

    fun flush() {
        cachedFormatLog()
        flushable.flush()
    }

    fun close() {
        cachedFormatLog()
        closable.close()
    }
}

open class OutputStreamLogger(
        private val output: OutputStream,
        newLine: Boolean = true,
        maxCachedSize: Int = 64,
        logLevel: LoggerLevel = DEFAULT_LEVEL
) : CachedFormatLogger(output, output, newLine, maxCachedSize, logLevel) {
    override fun cachedFormatLog() = run { cachedMsgList.forEach { output.write(it.toByteArray()) }; output.flush() }
}

open class PrintStreamLogger(
        private val printStream: PrintStream,
        maxCachedSize: Int = 64,
        logLevel: LoggerLevel = DEFAULT_LEVEL
) : CachedFormatLogger(printStream, printStream, false, maxCachedSize, logLevel) {
    override fun cachedFormatLog() = run { cachedMsgList.forEach { printStream.println(it) }; printStream.flush() }
}

open class WriterLogger(
        private val writer: Writer,
        newLine: Boolean = true,
        maxCachedSize: Int = 64,
        logLevel: LoggerLevel = DEFAULT_LEVEL
) : CachedFormatLogger(writer, writer, newLine, maxCachedSize, logLevel) {
    override fun cachedFormatLog() = cachedMsgList.forEach { writer.write(it) }
}

open class PrintWriterLogger(
        private val printWriter: PrintWriter,
        maxCachedSize: Int = 64,
        logLevel: LoggerLevel = DEFAULT_LEVEL
) : CachedFormatLogger(printWriter, printWriter, false, maxCachedSize, logLevel) {
    override fun cachedFormatLog() = run { cachedMsgList.forEach { printWriter.println(it) }; printWriter.flush() }
}

@JvmOverloads
fun getSystemOutLogger(maxCachedSize: Int = 1, logLevel: LoggerLevel = DEFAULT_LEVEL) =
        PrintStreamLogger(System.out, maxCachedSize, logLevel)

@JvmOverloads
fun getFileWriterLogger(
        path: String,
        append: Boolean = false,
        maxCachedSize: Int = 64,
        logLevel: LoggerLevel = DEFAULT_LEVEL
) =
        WriterLogger(FileWriter(path, append), true, maxCachedSize, logLevel)

@JvmOverloads
fun getStandardLogger(logLevel: LoggerLevel, masks: Int = 1, names: List<String>, params: Map<String, Any> = emptyMap()): MultiLogger {
    val multiLogger = MultiLogger(logLevel)
    var index: Int = 0
    if (masks.and(1) == 1) {
        Log.d(DEFAULT_TAG, "get Android Log")
        val format = if (params.containsKey("FormatAndroidLog")) params["FormatAndroidLog"] as Boolean else true
        multiLogger.addLogger(names[index++], if (format) FormatAndroidLogLogger(logLevel) else AndroidLogLogger(logLevel))
    }
    if (masks.and(2) == 2) {
        Log.d(DEFAULT_TAG, "get System Log")
        var cachedSize = if (params.containsKey("SystemOutCachedSize")) params["SystemOutCachedSize"] as Int else 1
        if (cachedSize < 1) cachedSize = 1
        multiLogger.addLogger(names[index++], PrintStreamLogger(System.out, cachedSize, logLevel))
    }
    if (masks.and(4) == 4) {
        Log.d(DEFAULT_TAG, "get File Log")
        var cachedSize = if (params.containsKey("FileCachedSize")) params["FileCachedSize"] as Int else 1
        if (cachedSize < 1) cachedSize = 1
        val path = params["FilePath"] as String
        val append = if (params.containsKey("FileAppendable")) params["FileAppendable"] as Boolean else false
        val newLine = if (params.containsKey("FileNewLine")) params["FileNewLine"] as Boolean else false
        multiLogger.addLogger(names[index], WriterLogger(FileWriter(path, append), newLine, cachedSize, logLevel))
    }
    return multiLogger
}
