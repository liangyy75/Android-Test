package com.liang.example.json_inflater

fun parseBoolean(value: Value?): Boolean =
        value != null && value is PrimitiveV && value.toBoolean()
                || value != null && value !is NullV && value.string().toBoolean()
