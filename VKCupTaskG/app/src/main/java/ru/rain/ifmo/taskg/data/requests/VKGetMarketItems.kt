package ru.rain.ifmo.taskg.data.requests

import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONObject
import ru.rain.ifmo.taskg.data.models.VKMarketItem
import ru.rain.ifmo.taskg.foreach
import ru.rain.ifmo.taskg.presentation.activity.ShopsListActivity

class VKGetMarketItems(
    private val id: Int,
    private val count: Int = 20,
    private val offset: Int = 20
) : ApiCommand<List<VKMarketItem>>() {

    override fun onExecute(manager: VKApiManager): List<VKMarketItem> {
        val call = VKMethodCall.Builder()
            .method("market.get")
            .args(hashMapOf(
                "owner_id" to "$id",
                "count" to "$count",
                "offset" to "$offset",
                "extended" to "1",
                "lang" to "0"
            ))
            .version("5.103")
            .build()
        return manager.execute<List<VKMarketItem>?>(call, VKApiResponseParser { parse(JSONObject(it)) })!!
    }

    fun parse(r: JSONObject): List<VKMarketItem> {
        val json = r.getJSONObject("response")
        val items = json.getJSONArray("items")
        val result = ArrayList<VKMarketItem>(items.length())
        items.foreach {
            it as JSONObject
            ShopsListActivity.log(it.toString())
            result.add(VKMarketItem.parse(it))
        }
        return result
    }
}