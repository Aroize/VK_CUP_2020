package ru.rain.ifmo.ftask.data.models

import android.os.Parcel
import android.os.Parcelable

data class VKGroupInfo(
    val membersCount: Int,
    val friendsCount: Int,
    val description: String,
    val lastPostDate: Int,
    val link: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(membersCount)
        parcel.writeInt(friendsCount)
        parcel.writeString(description)
        parcel.writeInt(lastPostDate)
        parcel.writeString(link)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VKGroupInfo> {
        override fun createFromParcel(parcel: Parcel): VKGroupInfo {
            return VKGroupInfo(parcel)
        }

        override fun newArray(size: Int): Array<VKGroupInfo?> {
            return arrayOfNulls(size)
        }
    }
}