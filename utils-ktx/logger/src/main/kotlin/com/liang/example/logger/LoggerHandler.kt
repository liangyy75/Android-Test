@file:Suppress("unused")

package com.liang.example.logger

import java.io.Closeable
import java.io.FileWriter
import java.io.Flushable
import java.io.OutputStream
import java.io.PrintStream
import java.io.PrintWriter
import java.io.Writer
import kotlin.collections.ArrayList

interface DefaultLoggerHandler : LoggerInter {}

interface FormatLoggerHandler {
    fun handleMsg(msg: String): Int
}

class NullableDefaultLoggerHandler(var handler: DefaultLoggerHandler? = null) : DefaultLoggerHandler {
    override var logLevel: LoggerLevel
        get() = handler?.logLevel ?: LoggerLevel.NONE
        set(value) {
            if (handler != null) handler!!.logLevel = value
        }

    override fun v(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            handler?.v(tag, msg, t, args, depth + 1) ?: 0

    override fun d(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            handler?.d(tag, msg, t, args, depth + 1) ?: 0

    override fun i(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            handler?.i(tag, msg, t, args, depth + 1) ?: 0

    override fun w(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            handler?.w(tag, msg, t, args, depth + 1) ?: 0

    override fun e(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            handler?.e(tag, msg, t, args, depth + 1) ?: 0

    override fun wtf(tag: Any, msg: String, t: Throwable?, vararg args: Any?, depth: Int): Int =
            handler?.wtf(tag, msg, t, args, depth + 1) ?: 0
}

class NullableFormatLoggerHandler(var handler: FormatLoggerHandler? = null) : FormatLoggerHandler {
    override fun handleMsg(msg: String): Int = handler?.handleMsg(msg) ?: 0
}

abstract class CachedLoggerHandler(
        private val closable: Closeable,
        private val flushable: Flushable,
        private val newLine: Boolean = false,
        private val maxCachedSize: Int = 64
) : FormatLoggerHandler {
    protected val cachedMsgList: ArrayList<String> = ArrayList(maxCachedSize)
    private var nowCachedSize: Int = 0

    override fun handleMsg(msg: String): Int {
        cachedMsgList.add(if (newLine) msg + "\n" else msg)
        nowCachedSize++
        if (nowCachedSize == maxCachedSize) {
            cachedHandleMsg()
            cachedMsgList.clear()
            nowCachedSize = 0
        }
        return msg.length
    }

    abstract fun cachedHandleMsg()

    fun flush() {
        cachedHandleMsg()
        flushable.flush()
    }

    fun close() {
        cachedHandleMsg()
        closable.close()
    }
}

open class OutputStreamLoggerHandler(
        private val output: OutputStream,
        newLine: Boolean = true,
        maxCachedSize: Int = 64
) : CachedLoggerHandler(output, output, newLine, maxCachedSize) {
    override fun cachedHandleMsg() = run { cachedMsgList.forEach { output.write(it.toByteArray()) }; output.flush() }
}

open class PrintStreamLoggerHandler(
        private val printStream: PrintStream,
        maxCachedSize: Int = 64
) : CachedLoggerHandler(printStream, printStream, false, maxCachedSize) {
    override fun cachedHandleMsg() = run { cachedMsgList.forEach { printStream.println(it) }; printStream.flush() }
}

open class WriterLoggerHandler(
        private val writer: Writer,
        newLine: Boolean = true,
        maxCachedSize: Int = 64
) : CachedLoggerHandler(writer, writer, newLine, maxCachedSize) {
    override fun cachedHandleMsg() = cachedMsgList.forEach { writer.write(it) }
}

open class PrintWriterLoggerHandler(
        private val printWriter: PrintWriter,
        maxCachedSize: Int = 64
) : CachedLoggerHandler(printWriter, printWriter, false, maxCachedSize) {
    override fun cachedHandleMsg() = run { cachedMsgList.forEach { printWriter.println(it) }; printWriter.flush() }
}

open class MultiFormatHandlersLogger(logLevel: LoggerLevel = DEFAULT_LEVEL) : LogLoggerImpl(logLevel) {
    protected val loggerHandlers = mutableMapOf<String, FormatLoggerHandler>()
    val loggerFormatter = DefaultLoggerFormatter()

    fun getLoggerHandler(name: String) = loggerHandlers.get(name)
    fun addLoggerHandler(name: String, handler: FormatLoggerHandler) = run { loggerHandlers[name] = handler }
    fun hasLoggerHandler(name: String) = loggerHandlers.containsKey(name)
    fun removeLoggerHandler(name: String) = loggerHandlers.remove(name)

    override fun log(tag: String, msg: String, t: Throwable?, level: LoggerLevel, vararg args: Any?, depth: Int): Int =
            if (logLevel > level || loggerHandlers.isEmpty()) 0
            else loggerHandlers.toList().map {
                it.second.handleMsg(loggerFormatter.formatMsg(it.first, tag, t, level, msg, depth + 1, *args))
            }.sum()

    override fun logImpl(
            tag: String,
            msg: String,
            t: Throwable?,
            level: LoggerLevel,
            vararg args: Any?,
            depth: Int
    ): Int = 0

    fun close() =
            loggerHandlers.forEach { if (it.value is CachedLoggerHandler) (it.value as CachedLoggerHandler).close() }

    fun flush() =
            loggerHandlers.forEach { if (it.value is CachedFormatLogger) (it.value as CachedFormatLogger).flush() }
}

fun getSystemOutLoggerHandler(maxCachedSize: Int = 1) =
        PrintStreamLoggerHandler(System.out, maxCachedSize)

fun getFileWriterLoggerHandler(path: String, append: Boolean = false, maxCachedSize: Int = 64) =
        WriterLoggerHandler(FileWriter(path, append), true, maxCachedSize)

fun getSystemAndFileLogger(
        path: String,
        cs1: Int = 1,
        cs2: Int = 64,
        name1: String = "system",
        name2: String = "file",
        append: Boolean = false
): MultiFormatHandlersLogger {
    val logger = MultiFormatHandlersLogger()
    // logger.addLoggerHandler(name1, PrintStreamLoggerHandler(System.out, cs1))
    // logger.addLoggerHandler(name2, WriterLoggerHandler(FileWriter(path, append), true, cs2))
    logger.addLoggerHandler(name1, PrintStreamLoggerHandler(System.out, cs1))
    logger.addLoggerHandler(name2, WriterLoggerHandler(FileWriter(path, append), true, cs2))
    return logger
}
