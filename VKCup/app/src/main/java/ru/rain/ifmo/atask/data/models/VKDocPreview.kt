package ru.rain.ifmo.atask.data.models

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject
import ru.rain.ifmo.atask.foreach

data class VKDocPreview(
    var previewS: String = "",
    var original: String = "",
    var video: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(previewS)
        parcel.writeString(original)
        parcel.writeString(video)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VKDocPreview> {
        override fun createFromParcel(parcel: Parcel): VKDocPreview {
            return VKDocPreview(parcel)
        }

        override fun newArray(size: Int): Array<VKDocPreview?> {
            return arrayOfNulls(size)
        }

        fun parse(jsonObject: JSONObject): VKDocPreview {
            val parsed = VKDocPreview()
            if (jsonObject.has("photo")) {
                val sizes = jsonObject.getJSONObject("photo").getJSONArray("sizes")
                sizes.foreach {
                    it as JSONObject
                    when (it.optString("type")) {
                        "o" -> parsed.original = it.optString("src")
                        "s" -> parsed.previewS = it.optString("src")
                    }
                }
            }
            if (jsonObject.has("video")) {
                parsed.video = jsonObject.getJSONObject("video").optString("src")
            }
            return parsed
        }
    }

}