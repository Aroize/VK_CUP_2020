package ru.rain.ifmo.dtask.data.requests

import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONObject
import ru.rain.ifmo.dtask.App
import ru.rain.ifmo.dtask.data.models.VKAlbum

class VKCreateAlbumRequest(
    private val title: String
) : ApiCommand<VKAlbum>() {
    override fun onExecute(manager: VKApiManager): VKAlbum {
        val call = VKMethodCall.Builder()
            .method("photos.createAlbum")
            .args("title", title)
            .version(App.VK_API_VERSION)
            .build()
        return manager.execute(
            call,
            VKApiResponseParser<VKAlbum> {
                VKAlbum.parse(JSONObject(it).getJSONObject("response"))
            })
    }
}