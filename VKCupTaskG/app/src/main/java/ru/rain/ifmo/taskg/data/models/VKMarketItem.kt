package ru.rain.ifmo.taskg.data.models

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

data class VKMarketItem(
    val id: Int = 0,
    val ownerId: Int = 0,
    val title: String = "",
    val description: String = "",
    val photo: String = "",
    var isFavorite: Boolean = false,
    val price: VKPrice = VKPrice(),
    val photos: Array<VKPhoto> = emptyArray()
) : Parcelable {

    @Suppress("UNCHECKED_CAST")
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt() == 1,
        parcel.readParcelable(VKPrice::class.java.classLoader)!!,
        parcel.readArray(VKPhoto::class.java.classLoader) as Array<VKPhoto>
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(ownerId)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(photo)
        parcel.writeInt(if (isFavorite) 1 else 0)
        parcel.writeParcelable(price, flags)
        parcel.writeArray(photos)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VKMarketItem

        if (id != other.id) return false
        if (ownerId != other.ownerId) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + ownerId
        result = 31 * result + title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + photo.hashCode()
        result = 31 * result + price.hashCode()
        result = 31 * result + photos.contentHashCode()
        return result
    }

    companion object CREATOR : Parcelable.Creator<VKMarketItem> {
        override fun createFromParcel(parcel: Parcel): VKMarketItem {
            return VKMarketItem(parcel)
        }

        override fun newArray(size: Int): Array<VKMarketItem?> {
            return arrayOfNulls(size)
        }

        fun parse(json: JSONObject) = VKMarketItem(
            id = json.optInt("id"),
            ownerId = json.optInt("owner_id"),
            title = json.optString("title"),
            description = json.optString("description"),
            photo = json.optString("thumb_photo"),
            price = VKPrice.parse(json.getJSONObject("price")),
            photos = VKPhoto.parseArray(json.getJSONArray("photos")),
            isFavorite = json.has("is_favorite")
        )
    }

    data class VKPrice(
        val id: Int = 0,
        val amount: Int = 0,
        val name: String = "",
        val text: String = ""
    ) : Parcelable {

        constructor(parcel: Parcel): this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString()!!,
            parcel.readString()!!
        )

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeInt(id)
            dest.writeInt(amount)
            dest.writeString(name)
            dest.writeString(text)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<VKPrice> {
            override fun createFromParcel(source: Parcel): VKPrice {
                return VKPrice(source)
            }

            override fun newArray(size: Int): Array<VKPrice?> {
                return arrayOfNulls<VKPrice>(size)
            }

            fun parse(json: JSONObject) = VKPrice(
                amount = json.optInt("amount"),
                id = json.getJSONObject("currency").optInt("id"),
                name = json.getJSONObject("currency").optString("name"),
                text = json.optString("text")
            )
        }
    }
}