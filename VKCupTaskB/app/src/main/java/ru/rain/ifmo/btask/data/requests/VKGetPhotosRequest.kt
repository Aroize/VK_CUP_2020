package ru.rain.ifmo.btask.data.requests

import android.graphics.BitmapFactory
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.internal.ApiCommand
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import ru.rain.ifmo.btask.data.models.VKPhoto
import ru.rain.ifmo.btask.forEach

class VKGetPhotosRequest : ApiCommand<List<VKPhoto>>() {
    override fun onExecute(manager: VKApiManager): List<VKPhoto> {
        val call = VKMethodCall.Builder()
            .method("photos.getAll")
            .args(hashMapOf(
                "extended" to "1",
                "photo_sizes" to "1",
                "skip_hidden" to "1"
            ))
            .version(manager.config.version)
            .build()
        return manager.execute(call, VKApiResponseParser<List<VKPhoto>> {
            val json = JSONObject(it).getJSONObject("response")
            val items = json.getJSONArray("items")
            val result = ArrayList<VKPhoto>(items.length())
            items.forEach { vkPhoto ->
                val client = OkHttpClient()
                vkPhoto as JSONObject
                if (vkPhoto.has("lat") && vkPhoto.has("long")) {
                    val parsed = VKPhoto.parse(vkPhoto)
                    result.add(parsed)
                    val request = Request.Builder()
                        .url(parsed.preview)
                        .build()
                    val response = client.newCall(request).execute()
                    val body = response.body()!!.bytes()
                    parsed.bitmap = BitmapFactory.decodeByteArray(body, 0, body.size)
                }
            }
            result
        })
    }
}