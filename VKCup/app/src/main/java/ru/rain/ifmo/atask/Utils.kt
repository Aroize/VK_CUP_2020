package ru.rain.ifmo.atask

import org.json.JSONArray
import ru.rain.ifmo.atask.data.models.VKDocType
import java.io.File

inline fun JSONArray.foreach(func: (Any) -> Unit) {
    for (i in 0 until length()) {
        func(get(i))
    }
}

fun Int.toDocType() = when(this) {
    1 -> VKDocType.TEXT
    2 -> VKDocType.ARCHIVE
    3 -> VKDocType.GIF
    4 -> VKDocType.IMAGE
    5 -> VKDocType.AUDIO
    6 -> VKDocType.VIDEO
    7 -> VKDocType.E_BOOK
    else -> VKDocType.UNDEFINED
}

fun Long.toFormattedBytes(endings: Array<String>): String {
    return when (this) {
        in 0..1023 -> {
            "$this ${endings[0]}"
        }
        in 1024..1_048_576 -> {
            val bytes = this.toFloat() / 1024
            "%.${2}f ${endings[1]}".format(bytes)
        }
        else -> {
            val bytes = this.toFloat() / 1_048_576
            "%.${2}f ${endings[2]}".format(bytes)
        }
    }
}