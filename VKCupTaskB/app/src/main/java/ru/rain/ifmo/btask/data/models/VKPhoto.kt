package ru.rain.ifmo.btask.data.models

import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONObject
import ru.rain.ifmo.btask.forEach

data class VKPhoto(
    val id: Int,
    val albumId: Int,
    val ownerId: Int,
    val latLng: LatLng,
    val preview: String,
    var bitmap: Bitmap? = null,
    val orig: String
) {
    companion object {
        fun parse(json: JSONObject): VKPhoto {
            val parsed = parseSizes(json.getJSONArray("sizes"))
            val latLng = LatLng(json.optDouble("lat"), json.optDouble("long"))
            return VKPhoto(
                id = json.optInt("id"),
                albumId = json.optInt("album_id"),
                ownerId = json.optInt("owner_id"),
                latLng = latLng,
                preview = parsed.first,
                orig = parsed.second
            )
        }

        private fun parseSizes(array: JSONArray): Pair<String, String> {
            val preview = array.getJSONObject(0).getString("url")
            var type = 'a'
            var url: String = ""
            array.forEach {
                it as JSONObject
                val curType = it.optString("type")
                if (curType.isNotBlank() && curType[0] > type) {
                    type = curType[0]
                    url = it.optString("url")
                }
            }
            return preview to url
        }
    }
}