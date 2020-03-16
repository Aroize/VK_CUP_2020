package ru.rain.ifmo.taskg

import org.json.JSONArray

inline fun JSONArray.foreach(func: (Any) -> Unit): Unit {
    for (i in 0 until this.length()) {
        func(this[i])
    }
}