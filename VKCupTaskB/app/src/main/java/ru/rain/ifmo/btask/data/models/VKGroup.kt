package ru.rain.ifmo.btask.data.models

import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONObject
import ru.rain.ifmo.btask.forEachIndexed

data class VKGroup(
    val id: Int = 0,
    val name: String = "",
    val screenName: String = "",
    val type: String = "",
    val description : String = "",
    var addresses: Array<Address> = emptyArray(),
    var photo: Bitmap? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VKGroup

        if (id != other.id) return false
        if (name != other.name) return false
        if (type != other.type) return false
        if (!addresses.contentEquals(other.addresses)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + addresses.contentHashCode()
        return result
    }

    companion object {
        fun parse(json: JSONObject) = VKGroup(
            id = json.optInt("id"),
            name = json.optString("name"),
            screenName = json.optString("screen_name"),
            type = json.optString("type"),
            description = json.optString("description")
        )

        fun parseAddresses(jsonArray: JSONArray): Array<Address> {
            val result = Array(jsonArray.length()){ Address(LatLng(0.0, 0.0), "") }
            jsonArray.forEachIndexed { it, i ->
                it as JSONObject
                val lat = it.optDouble("latitude")
                val long = it.optDouble("longitude")
                val address = it.optString("address")
                result[i] = Address(LatLng(lat, long), address)
            }
            return result
        }
    }

    data class Address(
        val latLng: LatLng,
        val address: String
    )

}