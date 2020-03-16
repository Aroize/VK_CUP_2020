package ru.rain.ifmo.dtask.data.requests

import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONObject
import ru.rain.ifmo.dtask.App
import ru.rain.ifmo.dtask.data.models.VKAlbum
import ru.rain.ifmo.dtask.forEach
import ru.rain.ifmo.dtask.parseSizes

class VKGetAlbumsRequest(
    private val count: Int,
    private val offset: Int
) : ApiCommand<List<VKAlbum>>() {
    override fun onExecute(manager: VKApiManager): List<VKAlbum> {
        val call = VKMethodCall.Builder()
            .method("photos.getAlbums")
            .args(hashMapOf(
                "count" to "$count",
                "offset" to "$offset",
                "need_system" to "1",
                "need_covers" to "1",
                "photo_sizes" to "1",
                "lang" to "0"
            ))
            .version(App.VK_API_VERSION)
            .build()
        return manager.execute(call, VKAlbumListResponseParser())
    }

    private class VKAlbumListResponseParser : VKApiResponseParser<List<VKAlbum>> {
        override fun parse(response: String): List<VKAlbum> {
            val json = JSONObject(response).getJSONObject("response")
            val count = json.optInt("count")
            val items = json.getJSONArray("items")
            val result = ArrayList<VKAlbum>(count)
            items.forEach {
                it as JSONObject
                val parsed = VKAlbum.parse(it)
                val sizes = it.optJSONArray("sizes")
                val (preview, url) = sizes!!.parseSizes()
                if (preview.isNotBlank())
                    parsed.thumbSrc = preview
                else
                    parsed.thumbSrc = url
                result.add(parsed)
            }
            return result
        }
    }
}