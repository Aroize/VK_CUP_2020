package ru.rain.ifmo.dtask.data.requests

import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONObject
import ru.rain.ifmo.dtask.App
import ru.rain.ifmo.dtask.data.models.VKPhoto

class VKDeletePhotoRequest(
    private val photo: VKPhoto
) : ApiCommand<Int>() {
    override fun onExecute(manager: VKApiManager): Int {
        val call = VKMethodCall.Builder()
            .method("photos.delete")
            .args(hashMapOf(
                "owner_id" to "${photo.ownerId}",
                "photo_id" to "${photo.id}"
            ))
            .version(App.VK_API_VERSION)
            .build()
        return manager.execute(call, VKApiResponseParser<Int> { JSONObject(it).optInt("response") })
    }
}