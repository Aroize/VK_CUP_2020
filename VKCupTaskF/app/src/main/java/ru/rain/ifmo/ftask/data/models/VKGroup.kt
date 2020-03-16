package ru.rain.ifmo.ftask.data.models

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

data class VKGroup(
    val id: Int = 0,
    val name: String = "",
    val photo200: String = "",
    var deactivated: Boolean = false
) : Parcelable  {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt() == 1
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(photo200)
        parcel.writeInt(if (deactivated) 1 else 0)
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

        fun parse(jsonObject: JSONObject) = VKGroup(
            id = jsonObject.optInt("id"),
            name = jsonObject.optString("name"),
            photo200 = jsonObject.optString("photo_200"),
            deactivated = jsonObject.has("deactivated")
        )
    }
}