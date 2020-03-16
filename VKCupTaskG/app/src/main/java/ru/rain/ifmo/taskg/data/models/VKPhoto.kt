package ru.rain.ifmo.taskg.data.models

import org.json.JSONArray
import org.json.JSONObject
import ru.rain.ifmo.taskg.foreach

data class VKPhoto(
    val id: Int = 0,
    val url: String = ""
) {
    companion object {
        fun parseArray(array: JSONArray): Array<VKPhoto> {
            val result = Array<VKPhoto>(array.length()){ VKPhoto() }
            var i = 0
            array.foreach {
                it as JSONObject
                result[i++] = VKPhoto.parse(it)
            }
            return result
        }

        private fun parse(json: JSONObject): VKPhoto {
            val sizes = json.getJSONArray("sizes")
            var find = ""
            sizes.foreach {
                it as JSONObject
                if (it.optString("type") == "x") {
                    find = it.optString("url")
                }
            }
            return VKPhoto(
                id = json.optInt("id"),
                url = find
                )
        }
    }
}