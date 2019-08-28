package liang.example.utils.logger

import liang.example.utils.format
import java.text.SimpleDateFormat
import java.util.*

interface LoggerFormatter {
    fun formatMsg(name: String, tag: String, t: Throwable?, level: LoggerLevel, msg: String, vararg args: Any?): String
}

class DefaultLoggerFormatter : LoggerFormatter {
    var totalFormatStr = "%(time) %(tag)/%(level): %(msg)\n%(throwable)"
    var lossMsgFormatStr = "%(time) %(tag)/%(level):\n%(throwable)"
    var lossExceptionFormatStr = "%(time) %(tag)/%(level): %(msg)"
    var emptyFormatStr = "%(time) %(tag)/%(level)"
    var dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA)

    override fun formatMsg(
            name: String,
            tag: String,
            t: Throwable?,
            level: LoggerLevel,
            msg: String,
            vararg args: Any?
    ): String {
        val msgStr = if (args.isNotEmpty()) String.format(msg, *args) else msg
        val map = mapOf(
                "time" to dateFormatter.format(Date()),
                "name" to name,
                "tag" to tag,
                "level" to level.toString()
        )
        val msgFlag = if (msgStr.isNotEmpty()) {
            map.plus(Pair("msg", msgStr))
            true
        } else false
        val exFlag = if (t != null) {
            map.plus("throwable" to (t.toString()))
            true
        } else false
        return format(
                when {
                    msgFlag && exFlag -> totalFormatStr
                    msgFlag -> lossExceptionFormatStr
                    exFlag -> lossMsgFormatStr
                    else -> emptyFormatStr
                }, map
        )
    }
}
