package ru.rain.ifmo.taskg.data.models

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

data class VKGroup(
    val id: Int = 0,
    val name: String = "",
    val screenName: String = "",
    val isClosed: Int = 0,
    val isMember: Boolean = false,
    val photo50: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(screenName)
        parcel.writeInt(isClosed)
        parcel.writeByte(if (isMember) 1 else 0)
        parcel.writeString(photo50)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VKGroup> {
        override fun createFromParcel(parcel: Parcel): VKGroup {
            return VKGroup(parcel)
        }

        override fun newArray(size: Int): Array<VKGroup?> {
            return arrayOfNulls(size)
        }

        fun parse(json: JSONObject) = VKGroup(
            id = json.optInt("id"),
            name = json.optString("name"),
            screenName = json.optString("screen_name"),
            isClosed = json.optInt("is_closed"),
            isMember = json.optInt("is_member") == 1,
            photo50 = json.optString("photo_50")
        )
    }
}