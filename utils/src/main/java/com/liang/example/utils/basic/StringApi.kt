package com.liang.example.utils.basic

fun String.isNumber(): Boolean = this.toIntOrNull() != null

fun format(formatter: String, map: Map<String, String>): String {
    val list = map.values.toList()
    return formatter.replace(Regex("%\\((.*?)\\)")) {
        val origin = it.groupValues[1]
        val number = origin.toIntOrNull()
        if (number != null) list[number] else (map.get(origin) ?: "")
    }
}
