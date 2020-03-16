package ru.rain.ifmo.taskg.data.models

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

data class VKCity(
    val id: Int = 0,
    val title: String = "",
    val important: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeByte(if (important) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VKCity> {
        override fun createFromParcel(parcel: Parcel): VKCity {
            return VKCity(parcel)
        }

        override fun newArray(size: Int): Array<VKCity?> {
            return arrayOfNulls(size)
        }

        fun parse(jsonObject: JSONObject) =  VKCity(
            id = jsonObject.optInt("id"),
            title = jsonObject.optString("title"),
            important = jsonObject.has("important")
        )
    }
}