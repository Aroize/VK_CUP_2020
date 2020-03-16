package ru.rain.ifmo.dtask.data.models

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject
import ru.rain.ifmo.dtask.forEach

data class VKPhoto(
    val id: Int = 0,
    val albumId: Int = 0,
    val ownerId: Int = 0,
    val preview: String = "",
    val orig: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(albumId)
        parcel.writeInt(ownerId)
        parcel.writeString(preview)
        parcel.writeString(orig)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VKPhoto> {
        override fun createFromParcel(parcel: Parcel): VKPhoto {
            return VKPhoto(parcel)
        }

        override fun newArray(size: Int): Array<VKPhoto?> {
            return arrayOfNulls(size)
        }

        fun parse(json: JSONObject): VKPhoto {
            val sizes = json.getJSONArray("sizes")
            var chosenType = 'a'
            var url = ""
            var preview = ""
            sizes.forEach {
                it as JSONObject
                val type = it.optString("type")
                if (type.isNotBlank() && type[0] > chosenType) {
                    chosenType = type[0]
                    url = it.optString("url")
                }
                if (type[0] == 'x')
                    preview = it.optString("url")
            }
            return VKPhoto(
                id = json.optInt("id"),
                albumId = json.optInt("album_id"),
                ownerId = json.optInt("owner_id"),
                preview = preview,
                orig = url
            )
        }
    }

}