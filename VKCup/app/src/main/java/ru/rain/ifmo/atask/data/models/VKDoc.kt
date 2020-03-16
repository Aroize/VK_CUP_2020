package ru.rain.ifmo.atask.data.models

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject
import ru.rain.ifmo.atask.toDocType

data class VKDoc(
    val id: Int = 0,
    var title: String = "",
    val size: Long = 0,
    val ext: String = "",
    val url: String = "",
    val date: Int = 0,
    val type: VKDocType = VKDocType.UNDEFINED,
    val preview: VKDocPreview? = null,
    val tags: Array<String> = emptyArray()
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt().toDocType(),
        parcel.readParcelable(VKDocPreview::class.java.classLoader),
        parcel.readArray(String::class.java.classLoader) as Array<String>
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeLong(size)
        parcel.writeString(ext)
        parcel.writeString(url)
        parcel.writeInt(date)
        parcel.writeParcelable(preview, flags)
        parcel.writeArray(tags)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VKDoc

        if (id != other.id) return false
        if (title != other.title) return false
        if (size != other.size) return false
        if (ext != other.ext) return false
        if (url != other.url) return false
        if (date != other.date) return false
        if (type != other.type) return false
        if (preview != other.preview) return false
        if (!tags.contentEquals(other.tags)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + title.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + ext.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + date
        result = 31 * result + type.hashCode()
        result = 31 * result + (preview?.hashCode() ?: 0)
        result = 31 * result + tags.contentHashCode()
        return result
    }

    companion object CREATOR : Parcelable.Creator<VKDoc> {
        override fun createFromParcel(parcel: Parcel): VKDoc {
            return VKDoc(parcel)
        }

        override fun newArray(size: Int): Array<VKDoc?> {
            return arrayOfNulls(size)
        }

        fun parse(jsonObject: JSONObject): VKDoc {
            val tags =
                if (jsonObject.has("tags")) {
                    val jsonTags = jsonObject.getJSONArray("tags")
                    Array<String>(jsonTags.length()){jsonTags.optString(it)}
                }
                else
                    emptyArray<String>()
            return VKDoc(
                id = jsonObject.optInt("id"),
                title = jsonObject.optString("title"),
                size = jsonObject.optLong("size"),
                ext = jsonObject.optString("ext"),
                url = jsonObject.optString("url"),
                date = jsonObject.optInt("date"),
                type = jsonObject.optInt("type").toDocType(),
                preview = if (jsonObject.has("preview"))
                    VKDocPreview.parse(jsonObject.getJSONObject("preview"))
                else
                    null,
                tags = tags
                )
        }
    }
}