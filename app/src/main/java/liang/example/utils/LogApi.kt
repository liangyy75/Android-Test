package liang.example.utils

import android.util.Log
import java.lang.Exception

enum class LoggerLevel { VERBOSE, DEBUG, INFO, WARN, ERROR, FATAL }
@Suppress("unused")
enum class LoggerResult { SUCCESSFULLY, FAILED }

var DEFAULT_LOGGER_LEVEL: LoggerLevel = LoggerLevel.VERBOSE
var DEFAULT_TAG: String = "LoggerApi"
private val EMPTY_ARRAY = emptyArray<Any>()

/**
 * log(Throwable)
 * log(String)
 * log(String, Throwable, Object...)
 * log(Object, String)
 * log(Object, Throwable, Object...)
 * log(Object, String, Throwable, Object...)
 */
@Suppress("unused")
interface LoggerInter {
    var logLevel: LoggerLevel
    fun isLoggable(level: LoggerLevel): Boolean

    fun v(msg: String): Int = v(DEFAULT_TAG, msg = msg, t = null)
    fun d(msg: String): Int = d(DEFAULT_TAG, msg = msg, t = null)
    fun i(msg: String): Int = i(DEFAULT_TAG, msg = msg, t = null)
    fun w(msg: String): Int = w(DEFAULT_TAG, msg = msg, t = null)
    fun e(msg: String): Int = e(DEFAULT_TAG, msg = msg, t = null)
    fun wtf(msg: String): Int = wtf(DEFAULT_TAG, msg = msg, t = null)

    fun v(t: Throwable?): Int = v(DEFAULT_TAG, msg = "", t = null)
    fun d(t: Throwable?): Int = d(DEFAULT_TAG, msg = "", t = null)
    fun i(t: Throwable?): Int = i(DEFAULT_TAG, msg = "", t = null)
    fun w(t: Throwable?): Int = w(DEFAULT_TAG, msg = "", t = null)
    fun e(t: Throwable?): Int = e(DEFAULT_TAG, msg = "", t = null)
    fun wtf(t: Throwable?): Int = wtf(DEFAULT_TAG, msg = "", t = null)

    // fun v(msg: String, vararg args: Any?): Int = v(DEFAULT_TAG, msg, t = null, args = *args)
    // fun d(msg: String, vararg args: Any?): Int = d(DEFAULT_TAG, msg, t = null, args = *args)
    // fun i(msg: String, vararg args: Any?): Int = i(DEFAULT_TAG, msg, t = null, args = *args)
    // fun w(msg: String, vararg args: Any?): Int = w(DEFAULT_TAG, msg, t = null, args = *args)
    // fun e(msg: String, vararg args: Any?): Int = e(DEFAULT_TAG, msg, t = null, args = *args)
    // fun wtf(msg: String, vararg args: Any?): Int = wtf(DEFAULT_TAG, msg, t = null, args = *args)

    fun v(msg: String, t: Throwable?, vararg args: Any?): Int = v(DEFAULT_TAG, msg, t = t, args = *args)
    fun d(msg: String, t: Throwable?, vararg args: Any?): Int = d(DEFAULT_TAG, msg, t = t, args = *args)
    fun i(msg: String, t: Throwable?, vararg args: Any?): Int = i(DEFAULT_TAG, msg, t = t, args = *args)
    fun w(msg: String, t: Throwable?, vararg args: Any?): Int = w(DEFAULT_TAG, msg, t = t, args = *args)
    fun e(msg: String, t: Throwable?, vararg args: Any?): Int = e(DEFAULT_TAG, msg, t = t, args = *args)
    fun wtf(msg: String, t: Throwable?, vararg args: Any?): Int = wtf(DEFAULT_TAG, msg, t = t, args = *args)

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

// TODO
@Suppress("unused")
interface LoggerHandler {
    fun handle()
}

// TODO
class DefaultLogger(override var logLevel: LoggerLevel = DEFAULT_LOGGER_LEVEL) : LoggerInter {
    fun log(tag: String, msg: String, t: Throwable?, method1: (String, String) -> Int, method2: (String, String, Throwable) -> Int, level: LoggerLevel) =
            when {
                logLevel >= level -> 0
                t == null -> method1.invoke(tag, msg)
                else -> method2.invoke(tag, msg, t)
            }

    override fun v(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int =
            log(tag.toString(), if (args.size > 0) String.format(msg, args) else msg, t, { t2, m2 -> Log.v(t2, m2) }, { t2, m2, t3 -> Log.v(t2, m2, t3) }, LoggerLevel.VERBOSE)

    override fun d(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int =
            log(tag.toString(), if (args.size > 0) String.format(msg, args) else msg, t, { t2, m2 -> Log.d(t2, m2) }, { t2, m2, t3 -> Log.d(t2, m2, t3) }, LoggerLevel.DEBUG)

    override fun i(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int =
            log(tag.toString(), if (args.size > 0) String.format(msg, args) else msg, t, { t2, m2 -> Log.i(t2, m2) }, { t2, m2, t3 -> Log.i(t2, m2, t3) }, LoggerLevel.INFO)

    override fun w(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int =
            log(tag.toString(), if (args.size > 0) String.format(msg, args) else msg, t, { t2, m2 -> Log.w(t2, m2) }, { t2, m2, t3 -> Log.w(t2, m2, t3) }, LoggerLevel.WARN)

    override fun e(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int =
            log(tag.toString(), if (args.size > 0) String.format(msg, args) else msg, t, { t2, m2 -> Log.e(t2, m2) }, { t2, m2, t3 -> Log.e(t2, m2, t3) }, LoggerLevel.ERROR)

    override fun wtf(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int =
            log(tag.toString(), if (args.size > 0) String.format(msg, args) else msg, t, { t2, m2 -> Log.wtf(t2, m2) }, { t2, m2, t3 -> Log.wtf(t2, m2, t3) }, LoggerLevel.FATAL)

    override fun isLoggable(level: LoggerLevel): Boolean = logLevel < level
}

// TODO
class FileLogger(override var logLevel: LoggerLevel = DEFAULT_LOGGER_LEVEL) : LoggerInter {
    override fun v(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun d(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun i(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun w(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun e(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun wtf(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isLoggable(level: LoggerLevel): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class LoggerManager(override var logLevel: LoggerLevel = DEFAULT_LOGGER_LEVEL, useDefault: Boolean = false) : LoggerInter {
    var logger: LoggerInter? = null
    // TODO: 只是一个 logger ，还是用 HashMap 来管理

    init {
        if (useDefault) logger = DefaultLogger(logLevel)
    }

    override fun v(tag: Any, msg: String, t: Throwable?, vararg args: Any?): Int =
            logger?.v(tag, msg, t = t, args = *args) ?: 0

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

fun main() {
    val logger = LoggerManager(useDefault = true)
    val t = Exception("test exception")
    // logger.i("msg", *arrayOf<Any>("smg"))
    // logger.i("msg", t, "smg", "wtf")
    logger.i(t)
    logger.i("msg")
    logger.i("msg", t)
    logger.i("msg", t, "smg")
    logger.i("tag", "msg")
    logger.i("tag", "msg", t)
    logger.i("tag", "msg", "smg")
    logger.i("tag", "msg", t, "smg")
}
