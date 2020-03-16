package ru.rain.ifmo.dtask.data.requests

import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONObject
import ru.rain.ifmo.dtask.App
import ru.rain.ifmo.dtask.data.models.VKAlbum
import ru.rain.ifmo.dtask.parseSizes

class VKGetAlbumRequest(
    private val albumId: Int
) : ApiCommand<VKAlbum>() {
    override fun onExecute(manager: VKApiManager): VKAlbum {
        val call = VKMethodCall.Builder()
            .method("photos.getAlbums")
            .args("album_ids", "$albumId")
            .args("need_covers", "1")
            .args("lang", "0")
            .args("photo_sizes", "1")
            .version(App.VK_API_VERSION)
            .build()
        return manager.execute(call, VKApiResponseParser<VKAlbum> {
            val json = JSONObject(it).getJSONObject("response").getJSONArray("items")
            val album = json.getJSONObject(0)
            val sizes = album.getJSONArray("sizes")
            val (preview, url) = sizes.parseSizes()
            val result = VKAlbum.parse(album)
            if (preview.isNotBlank())
                result.thumbSrc = preview
            else
                result.thumbSrc = url
            result
        })
    }
}