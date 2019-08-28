package liang.example.utils.logger

import java.io.*
import kotlin.collections.ArrayList

interface LoggerHandler {
    fun handleMsg(msg: String): Int
}

class NullableLoggerHandler(var handler: LoggerHandler? = null) : LoggerHandler {
    override fun handleMsg(msg: String): Int = handler?.handleMsg(msg) ?: 0
}

abstract class CachedLoggerHandler(
        private val closable: Closeable,
        private val newLine: Boolean = false,
        private val maxCachedSize: Int = 64
) : LoggerHandler {
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

    fun close() {
        cachedHandleMsg()
        closable.close()
    }
}

open class OutputStreamLoggerHandler(
        private val output: OutputStream,
        newLine: Boolean = true,
        maxCachedSize: Int = 64
) : CachedLoggerHandler(output, newLine, maxCachedSize) {
    override fun cachedHandleMsg() = run { cachedMsgList.forEach { output.write(it.toByteArray()) }; output.flush() }
}

open class PrintStreamLoggerHandler(
        private val printStream: PrintStream,
        maxCachedSize: Int = 64
) : CachedLoggerHandler(printStream, false, maxCachedSize) {
    override fun cachedHandleMsg() = run { cachedMsgList.forEach { printStream.println(it) }; printStream.flush() }
}

open class WriterLoggerHandler(
        private val writer: Writer,
        newLine: Boolean = true,
        maxCachedSize: Int = 64
) : CachedLoggerHandler(writer, newLine, maxCachedSize) {
    override fun cachedHandleMsg() = cachedMsgList.forEach { writer.write(it) }
}

open class PrintWriterLoggerHandler(
        private val printWriter: PrintWriter,
        maxCachedSize: Int = 64
) : CachedLoggerHandler(printWriter, false, maxCachedSize) {
    override fun cachedHandleMsg() = run { cachedMsgList.forEach { printWriter.println(it) }; printWriter.flush() }
}

// TODO: appendable fileLoggerHandler

open class MultiHandlersLogger(logLevel: LoggerLevel = DEFAULT_LEVEL) : LogLoggerImpl(logLevel) {
    protected val loggerHandlers = mutableMapOf<String, LoggerHandler>()
    val loggerFormatter: LoggerFormatter = DefaultLoggerFormatter()

    fun getLoggerHandler(name: String) = loggerHandlers.get(name)
    fun addLoggerHandler(name: String, handler: LoggerHandler) = loggerHandlers.plus(Pair(name, handler))
    fun hasLoggerHandler(name: String) = loggerHandlers.containsKey(name)
    fun removeLoggerHandler(name: String) = loggerHandlers.remove(name)

    override fun log(tag: String, msg: String, t: Throwable?, level: LoggerLevel, vararg args: Any?): Int =
            if (logLevel >= level || loggerHandlers.isEmpty()) 0
            else loggerHandlers.toList().map { it.second.handleMsg(loggerFormatter.formatMsg(it.first, tag, t, level, msg, *args)) }.sum()

    override fun logImpl(tag: String, msg: String, t: Throwable?, level: LoggerLevel, vararg args: Any?): Int = 0

    fun close() = loggerHandlers.forEach { if (it.value is CachedLoggerHandler) (it.value as CachedLoggerHandler).close() }
}

fun getSystemOutLoggerHandler(maxCachedSize: Int = 1) =
        PrintStreamLoggerHandler(System.out, maxCachedSize)

fun getFilePrintWriterLoggerHandler(path: String, append: Boolean = false, maxCachedSize: Int = 64) =
        WriterLoggerHandler(FileWriter(path, append), true, maxCachedSize)

fun getSystemAndFileLoggerHandler(path: String, cs1: Int = 1, cs2: Int = 64, name1: String = "system", name2: String = "file", append: Boolean = false): MultiHandlersLogger {
    val logger = MultiHandlersLogger()
    logger.addLoggerHandler(name1, PrintStreamLoggerHandler(System.out, cs1))
    logger.addLoggerHandler(name2, WriterLoggerHandler(FileWriter(path, append), true, cs2))
    return logger
}
