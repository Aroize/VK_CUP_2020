package ru.rain.ifmo.ftask.data.requests

import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject
import ru.rain.ifmo.ftask.data.models.VKGroup

class VKGetGroupsRequest(
    count: Int = 20,
    offset: Int = 0
) : VKRequest<List<VKGroup>>("groups.get") {
    init {
        addParam("count", count)
        addParam("offset", offset)
        addParam("extended", 1)
    }

    override fun parse(r: JSONObject): List<VKGroup> {
        val result = arrayListOf<VKGroup>()
        val items = r.getJSONObject("response").getJSONArray("items")
        for (i in 0 until items.length()) {
            result.add(VKGroup.parse(items.getJSONObject(i)))
        }
        return result
    }
}