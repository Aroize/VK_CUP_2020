package ru.rain.ifmo.dtask.data.models

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

data class VKAlbum(
    val id: Int = 0,
    val ownerId: Int = 0,
    val thumbId: Int = 0,
    val title: String = "",
    var size: Int = 0,
    var thumbSrc: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(ownerId)
        parcel.writeInt(thumbId)
        parcel.writeString(title)
        parcel.writeInt(size)
        parcel.writeString(thumbSrc)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VKAlbum> {
        override fun createFromParcel(parcel: Parcel): VKAlbum {
            return VKAlbum(parcel)
        }

        override fun newArray(size: Int): Array<VKAlbum?> {
            return arrayOfNulls(size)
        }

        fun parse(json: JSONObject) = VKAlbum(
            id = json.optInt("id"),
            ownerId = json.optInt("owner_id"),
            thumbId = json.optInt("thumb_id"),
            title = json.optString("title"),
            size = json.optInt("size"),
            thumbSrc = json.optString("thumb_src")
        )
    }
}