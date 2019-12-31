package com.liang.example.utils.logger

import com.liang.example.utils.basic.format
import java.text.SimpleDateFormat
import java.util.*

interface LoggerFormatter {
    fun formatMsg(
            name: String,
            tag: String,
            t: Throwable?,
            level: LoggerLevel,
            msg: String,
            depth: Int,
            vararg args: Any?
    ): String
}

class DefaultLoggerFormatter : LoggerFormatter {
    var totalFormatStr = "%(time)/%(name) - %(classInfo) - %(tag)/%(level): %(msg)\n%(throwable)"
    var lossMsgFormatStr = "%(time)/%(name) - %(classInfo) - %(tag)/%(level):\n%(throwable)"
    var lossExceptionFormatStr = "%(time)/%(name) - %(classInfo) - %(tag)/%(level): %(msg)"
    var emptyFormatStr = "%(time)/%(name) - %(classInfo) - %(tag)/%(level):"
    var classInfoFormatStr = "[%(fileName)/%(lineNumber) %(className)/%(methodName)]"
    var dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA)
    var showPackageFlag = false

    override fun formatMsg(
            name: String,
            tag: String,
            t: Throwable?,
            level: LoggerLevel,
            msg: String,
            depth: Int,
            vararg args: Any?
    ): String {
        // Thread.currentThread().stackTrace.forEach { println("    ${it.fileName}/${it.lineNumber} ${it.className}/${it.methodName}") }
        val cte = Thread.currentThread().stackTrace[depth]
        val className = cte.className
        val map1 = mapOf(
                "fileName" to cte.fileName,
                "lineNumber" to cte.lineNumber.toString(),
                "className" to if (showPackageFlag) className else className.substring(className.lastIndexOf('.') + 1),
                "methodName" to cte.methodName
        )
        val msgStr = if (args.isNotEmpty()) String.format(msg, *args) else msg
        val map2 = mutableMapOf(
                "time" to dateFormatter.format(Date()),
                "name" to name,
                "tag" to tag,
                "level" to level.toString(),
                "classInfo" to format(classInfoFormatStr, map1)
        )
        val msgFlag = if (msgStr.isNotEmpty()) {
            map2["msg"] = msgStr
            true
        } else false
        val exFlag = if (t != null) {
            map2["throwable"] = t.toString()
            true
        } else false
        return format(
                when {
                    msgFlag && exFlag -> totalFormatStr
                    msgFlag -> lossExceptionFormatStr
                    exFlag -> lossMsgFormatStr
                    else -> emptyFormatStr
                }, map2
        )
    }
}

class AndroidLoggerFormatter : LoggerFormatter {
    var formatStr = "%(name) - %(classInfo) - %(msg)"
    var lossNameformatStr = "%(classInfo) - %(msg)"
    var classInfoFormatStr = "[%(fileName)/%(lineNumber) %(className)/%(methodName)]"
    var showPackageFlag = false

    override fun formatMsg(name: String, tag: String, t: Throwable?, level: LoggerLevel, msg: String, depth: Int, vararg args: Any?): String {
        // Thread.currentThread().stackTrace.forEach { println("    ${it.fileName}/${it.lineNumber} ${it.className}/${it.methodName}") }
        val cte = Thread.currentThread().stackTrace[depth]
        val className = cte.className
        val map1 = mapOf(
                "fileName" to cte.fileName,
                "lineNumber" to cte.lineNumber.toString(),
                "className" to if (showPackageFlag) className else className.substring(className.lastIndexOf('.') + 1),
                "methodName" to cte.methodName
        )
        val map2 = mutableMapOf(
                "msg" to msg,
                "classInfo" to format(classInfoFormatStr, map1)
        )
        val nameFlag = if (name.isNotEmpty()) {
            map2["name"] = name
            true
        } else false
        return format(if (nameFlag) formatStr else lossNameformatStr, map2)
    }
}
