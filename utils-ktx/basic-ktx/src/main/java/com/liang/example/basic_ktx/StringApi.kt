package com.liang.example.basic_ktx

fun format(formatter: String, map: Map<String, String>): String {
    if (formatter.isEmpty() || map.isEmpty()) {
        return formatter
    }
    val list = map.values.toList()
    return formatter.replace(Regex("%\\((.*?)\\)")) {
        val origin = it.groupValues[1]
        val number = origin.toIntOrNull()
        if (number != null) {
            list[number]
        } else {
            (map[origin] ?: "")
        }
    }
}

fun format(formatter: String, vararg args: Any?): String {
    if (formatter.isEmpty() || args.isEmpty()) {
        return formatter
    }
    return formatter.replace(Regex("%\\((.*?)\\)")) { args[it.groupValues[1].toInt()].toString() }
}
