package ru.rain.ifmo.dtask.data.requests

import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONObject
import ru.rain.ifmo.dtask.App
import ru.rain.ifmo.dtask.data.models.VKPhoto
import ru.rain.ifmo.dtask.forEach

class VKPhotoGetRequest(
    private val albumId: Int,
    private val count: Int,
    private val offset: Int
) : ApiCommand<List<VKPhoto>>() {
    override fun onExecute(manager: VKApiManager): List<VKPhoto> {
        val call = if (albumId > -100)
            VKMethodCall.Builder()
                .method("photos.get")
                .args(
                    hashMapOf(
                        "album_id" to "$albumId",
                        "count" to "$count",
                        "offset" to "$offset"
                    )
                )
                .version(App.VK_API_VERSION)
                .build()
        else
            VKMethodCall.Builder()
                .method("photos.getUserPhotos")
                .args(
                    hashMapOf(
                        "count" to "$count",
                        "offset" to "$offset"
                    )
                )
                .version(App.VK_API_VERSION)
                .build()
        return manager.execute(call, VKApiResponseParser<List<VKPhoto>> {
            val json = JSONObject(it).getJSONObject("response")
            val count = json.optInt("count")
            val result = ArrayList<VKPhoto>(count)
            val items = json.optJSONArray("items")
            items?.forEach { vkPhoto ->
                vkPhoto as JSONObject
                result.add(VKPhoto.parse(vkPhoto))
            }
            return@VKApiResponseParser result
        })
    }
}