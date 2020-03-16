package ru.rain.ifmo.dtask

import org.json.JSONArray
import org.json.JSONObject

inline fun JSONArray.forEach(func: (Any) -> Unit) {
    for (i in 0 until this.length())
        func(this[i])
}

fun JSONArray.parseSizes(): Pair<String, String> {
    var chosenType = 'a'
    var url = ""
    var preview = ""
    this.forEach {
        it as JSONObject
        val type = it.optString("type")
        if (type.isNotBlank() && type[0] > chosenType) {
            chosenType = type[0]
            url = it.optString("src")
        }
        if (type[0] == 'y')
            preview = it.optString("src")
    }
    if (preview.isBlank())
        preview = url
    return preview to url
}