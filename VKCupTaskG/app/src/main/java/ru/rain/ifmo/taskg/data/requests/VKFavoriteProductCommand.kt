package ru.rain.ifmo.taskg.data.requests

import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONObject

class VKFavoriteProductCommand(
    private val id: Int,
    private val ownerId: Int,
    private val isFavorite: Boolean
) : ApiCommand<Int>() {
    override fun onExecute(manager: VKApiManager): Int {
        val method = if (isFavorite) "fave.addProduct" else "fave.removeProduct"
        val call = VKMethodCall.Builder()
            .method(method)
            .args(hashMapOf(
                "owner_id" to "$ownerId",
                "id" to "$id"
            ))
            .version("5.103")
            .build()
        manager.config.version
        return manager.execute(call, VKApiResponseParser<Int> {
            JSONObject(it).optInt("response")
        })
    }
}