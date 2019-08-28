package liang.example.utils

fun String.isNumber(): Boolean = this.toIntOrNull() != null

fun format(formatter: String, map: Map<String, String>): String {
    val list = map.values.toList()
    return formatter.replace(Regex("%\\((.*?)\\)")) {
        val origin = it.groupValues[0]
        val number = origin.toIntOrNull()
        return@replace if (number != null) list[number] else (map.get(origin) ?: "")
    }
}