package ru.rain.ifmo.btask.data.requests

import android.graphics.BitmapFactory
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.internal.ApiCommand
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import ru.rain.ifmo.btask.App
import ru.rain.ifmo.btask.data.models.VKGroup
import ru.rain.ifmo.btask.forEach

class VKGetGroupsRequest(private val filter: String) : ApiCommand<List<VKGroup>>() {
    override fun onExecute(manager: VKApiManager): List<VKGroup> {
        val call = VKMethodCall.Builder()
            .method("groups.get")
            .args("extended", 1)
            .args("filter", filter)
            .args("fields", "photo_50,description")
            .version(manager.config.version)
            .build()
        val groups = getGroups(call, manager)
        App.log("Groups delivered ${groups.size}")
        groups.forEach {
            App.log("requesting address for group=$it")
            val addressCall = VKMethodCall.Builder()
                .method("groups.getAddresses")
                .args("group_id", it.id)
                .version("5.103")
                .build()
            val addresses = manager.execute(addressCall, VKApiResponseParser<Array<VKGroup.Address>> { string ->
                val json = JSONObject(string).getJSONObject("response")
                val items = json.getJSONArray("items")
                VKGroup.parseAddresses(items)
            })
            it.addresses = addresses
        }
        return groups
    }

    private fun getGroups(call: VKMethodCall, manager: VKApiManager): List<VKGroup> {
        return manager.execute(call, VKApiResponseParser<List<VKGroup>> {
            val response = JSONObject(it).getJSONObject("response")
            val items = response.getJSONArray("items")
            val result = ArrayList<VKGroup>(items.length())
            val client = OkHttpClient()
            items.forEach { group ->
                group as JSONObject
                val parsed = VKGroup.parse(group)
                val photoUrl = group.getString("photo_50")
                val request = Request.Builder()
                    .url(photoUrl)
                    .build()
                val photoResponse = client.newCall(request).execute()
                val body = photoResponse.body()
                if (body != null) {
                    val bytes = body.bytes()
                    parsed.photo = BitmapFactory.decodeByteArray(
                        bytes,
                        0,
                        bytes.size
                    )
                }
                result.add(parsed)
            }
            result
        })
    }
}