package ru.rain.ifmo.taskg.data.requests

import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject
import ru.rain.ifmo.taskg.data.models.VKGroup
import ru.rain.ifmo.taskg.foreach

class VKSearchGroupsRequest(
    count: Int = 0,
    offset: Int = 0,
    cityId: Int = 0
) : VKRequest<List<VKGroup>>("groups.search") {
    init {
        addParam("q", " ")
        addParam("market", 1)
        addParam("count", count)
        addParam("offset", offset)
        addParam("city_id", cityId)
    }

    override fun parse(r: JSONObject): List<VKGroup> {
        val result = arrayListOf<VKGroup>()
        val json = r.getJSONObject("response").getJSONArray("items")
        json.foreach {
            it as JSONObject
            result.add(VKGroup.parse(it))
        }
        return result
    }

}