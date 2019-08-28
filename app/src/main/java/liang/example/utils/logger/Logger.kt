package liang.example.utils.logger

import android.util.Log
import java.io.*
import kotlin.collections.ArrayList

enum class LoggerLevel { VERBOSE, DEBUG, INFO, WARN, ERROR, FATAL }

var DEFAULT_LEVEL: LoggerLevel = LoggerLevel.VERBOSE
var DEFAULT_TAG: String = "LoggerApi"
private val EMPTY_ARRAY = emptyArray<Any>()

/**
 * log(Object, String)
 * log(Object, Throwable)
 * log(Object, String, Object...)
 * log(Object, String, Throwable, Object...)
 */
interface LoggerInter {
    var logLevel: LoggerLevel
    fun isLoggable(level: LoggerLevel): Boolean = level >= logLevel

    fun v(tag: Any, msg: String): Int = v(tag, msg, t = null, args = *EMPTY_ARRAY)
    fun d(tag: Any, msg: String): Int = d(tag, msg, t = null, args = *EMPTY_ARRAY)
    fun i(tag: Any, msg: String): Int = i(tag, msg, t = null, args = *EMPTY_ARRAY)
    fun w(tag: Any, msg: String): Int = w(tag, msg, t = null, args = *EMPTY_ARRAY)
    fun e(tag: Any, msg: String): Int = e(tag, msg, t = null, args = *EMPTY_ARRAY)
    fun wtf(tag: Any, msg: String): Int = wtf(tag, msg, t = null, args = *EMPTY_ARRAY)

    fun v(tag: Any, t: Throwable?): Int = v(tag, "", t = null, args = *EMPTY_ARRAY)
    fun d(tag: Any, t: Throwable?): Int = d(tag, "", t = null, args = *EMPTY_ARRAY)
    fun i(tag: Any, t: Throwable?): Int = i(tag, "", t = null, args = *EMPTY_ARRAY)
    fun w(tag: Any, t: Throwable?): Int = w(tag, "", t = null, args = *EMPTY_ARRAY)
    fun e(tag: Any, t: Throwable?): Int = e(tag, "", t = null, args = *EMPTY_ARRAY)
    fun wtf(tag: Any, t: Throwable?): Int = wtf(tag, "", t = null, args = *EMPTY_ARRAY)

    fun v(tag: Any, msg: String, vararg args: Any?): Int = v(tag, msg, t = null, args = *args)
    fun d(tag: Any, msg: String, vararg args: Any?): Int = d(tag, msg, t = null, args = *args)
    fun i(tag: Any, msg: String, vararg args: Any?): Int = i(tag, msg, t = null, args = *args)
    fun w(tag: Any, msg: String, vararg args: Any?): Int = w(tag, msg, t = null, args = *args)
    fun e(tag: Any, msg: String, vararg args: Any?): Int = e(tag, msg, t = null, args = *args)
    fun wtf(tag: Any, msg: String, vararg args: Any?): Int = wtf(tag, msg, t = null, args = *args)

    fun v(tag: Any = DEFAULT_TAG, msg: String, t: Throwable? = null, vararg args: Any? = EMPTY_ARRAY): Int
    fun d(tag: Any = DEFAULT_TAG, msg: String, t: Throwable? = null, vararg args: Any? = EMPTY_ARRAY): Int
    fun i(tag: Any = DEFAULT_TAG, msg: String, t: Throwable? = null, vararg args: Any? = EMPTY_ARRAY): Int
    fun w(tag: Any = DEFAULT_TAG, msg: String, t: Throwable? = null, vararg args: Any? = EMPTY_ARRAY): Int
    fun e(tag: Any = DEFAULT_TAG, msg: String, t: Throwable? = null, vararg args: Any? = EMPTY_ARRAY): Int
    fun wtf(tag: Any = DEFAULT_TAG, msg: String, t: Throwable? = null, vararg args: Any? = EMPTY_ARRAY): Int
}

class AndroidLogLogger(override var logLevel: LoggerLevel = DEFAULT_LEVEL) : LoggerInter {
    private fun log(tag: String, msg: String, t: Throwable?, method1: (String, String) -> Int,
                    method2: (String, String, Throwable) -> Int, level: LoggerLevel, vararg args: Any?): Int =
            when {
                logLevel > level -> 0
                t == null -> method1.invoke(tag, if (args.isNotEmpty()) String.format(msg, *args) else msg)
                else -> method2.invoke(tag, if (args.isNotEmpty()) String.format(msg, *args) else msg, t)
            }

    override fun v(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int =
            log(tag.toString(), msg, t, { t2, m2 -> Log.v(t2, m2) }, { t2, m2, t3 -> Log.v(t2, m2, t3) }, LoggerLevel.VERBOSE, *args)

    override fun d(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int =
            log(tag.toString(), msg, t, { t2, m2 -> Log.d(t2, m2) }, { t2, m2, t3 -> Log.d(t2, m2, t3) }, LoggerLevel.DEBUG, *args)

    override fun i(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int =
            log(tag.toString(), msg, t, { t2, m2 -> Log.i(t2, m2) }, { t2, m2, t3 -> Log.i(t2, m2, t3) }, LoggerLevel.INFO, *args)

    override fun w(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int =
            log(tag.toString(), msg, t, { t2, m2 -> Log.w(t2, m2) }, { t2, m2, t3 -> Log.w(t2, m2, t3) }, LoggerLevel.WARN, *args)

    override fun e(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int =
            log(tag.toString(), msg, t, { t2, m2 -> Log.e(t2, m2) }, { t2, m2, t3 -> Log.e(t2, m2, t3) }, LoggerLevel.ERROR, *args)

    override fun wtf(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int =
            log(tag.toString(), msg, t, { t2, m2 -> Log.wtf(t2, m2) }, { t2, m2, t3 -> Log.wtf(t2, m2, t3) }, LoggerLevel.FATAL, *args)
}

class NullableLogger(override var logLevel: LoggerLevel = DEFAULT_LEVEL, useAndroid: Boolean = false) : LoggerInter {
    private var logger: LoggerInter? = null
    // TODO: 只是一个 logger ，还是用 HashMap 来管理

    init {
        if (useAndroid) logger = AndroidLogLogger(logLevel)
    }

    override fun v(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int =
            logger?.v(tag, msg, t, *args) ?: 0

    override fun d(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int =
            logger?.d(tag, msg, t = t, args = *args) ?: 0

    override fun i(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int =
            logger?.i(tag, msg, t = t, args = *args) ?: 0

    override fun w(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int =
            logger?.w(tag, msg, t = t, args = *args) ?: 0

    override fun e(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int =
            logger?.e(tag, msg, t = t, args = *args) ?: 0

    override fun wtf(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int =
            logger?.wtf(tag, msg, t = t, args = *args) ?: 0

    override fun isLoggable(level: LoggerLevel): Boolean = logger?.isLoggable(level) ?: false
}

abstract class LogLoggerImpl(override var logLevel: LoggerLevel = DEFAULT_LEVEL) : LoggerInter {
    abstract fun logImpl(tag: String, msg: String, t: Throwable?, level: LoggerLevel, vararg args: Any?): Int
    open fun log(tag: String, msg: String, t: Throwable?, level: LoggerLevel, vararg args: Any?): Int =
            if (logLevel <= level) logImpl(tag, msg, t, level, *args) else 0

    override fun v(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int = log(tag.toString(), msg, t, LoggerLevel.VERBOSE, *args)
    override fun d(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int = log(tag.toString(), msg, t, LoggerLevel.DEBUG, *args)
    override fun i(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int = log(tag.toString(), msg, t, LoggerLevel.INFO, *args)
    override fun w(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int = log(tag.toString(), msg, t, LoggerLevel.WARN, *args)
    override fun e(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int = log(tag.toString(), msg, t, LoggerLevel.ERROR, *args)
    override fun wtf(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int = log(tag.toString(), msg, t, LoggerLevel.FATAL, *args)
}

abstract class FormatLogger(logLevel: LoggerLevel = DEFAULT_LEVEL) : LogLoggerImpl(logLevel) {
    val loggerFormatter: LoggerFormatter = DefaultLoggerFormatter()

    override fun logImpl(tag: String, msg: String, t: Throwable?, level: LoggerLevel, vararg args: Any?): Int =
            formatLog(loggerFormatter.formatMsg("", tag, t, level, msg, *args))

    abstract fun formatLog(msg: String): Int
}

abstract class CachedFormatLogger(
        private val closable: Closeable,
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
) : CachedFormatLogger(output, newLine, maxCachedSize, logLevel) {
    override fun cachedFormatLog() = run { cachedMsgList.forEach { output.write(it.toByteArray()) }; output.flush() }
}

open class PrintStreamLogger(
        private val printStream: PrintStream,
        maxCachedSize: Int = 64,
        logLevel: LoggerLevel = DEFAULT_LEVEL
) : CachedFormatLogger(printStream, false, maxCachedSize, logLevel) {
    override fun cachedFormatLog() = run { cachedMsgList.forEach { printStream.println(it) }; printStream.flush() }
}

open class WriterLogger(
        private val writer: Writer,
        newLine: Boolean = true,
        maxCachedSize: Int = 64,
        logLevel: LoggerLevel = DEFAULT_LEVEL
) : CachedFormatLogger(writer, newLine, maxCachedSize, logLevel) {
    override fun cachedFormatLog() = cachedMsgList.forEach { writer.write(it + "\n") }
}

open class PrintWriterLogger(
        private val printWriter: PrintWriter,
        maxCachedSize: Int = 64,
        logLevel: LoggerLevel = DEFAULT_LEVEL
) : CachedFormatLogger(printWriter, false, maxCachedSize, logLevel) {
    override fun cachedFormatLog() = run { cachedMsgList.forEach { printWriter.println(it) }; printWriter.flush() }
}

@JvmOverloads
fun getSystemOutLogger(maxCachedSize: Int = 1, logLevel: LoggerLevel = DEFAULT_LEVEL) =
        PrintStreamLogger(System.out, maxCachedSize, logLevel)

@JvmOverloads
fun getFilePrintWriterLogger(path: String, append: Boolean = false, maxCachedSize: Int = 64, logLevel: LoggerLevel = DEFAULT_LEVEL) =
        WriterLogger(FileWriter(path, append), true, maxCachedSize, logLevel)
