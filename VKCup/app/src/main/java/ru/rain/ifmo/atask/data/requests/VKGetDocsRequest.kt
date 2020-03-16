package ru.rain.ifmo.atask.data.requests

import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject
import ru.rain.ifmo.atask.data.models.VKDoc
import ru.rain.ifmo.atask.foreach
import java.util.ArrayList

class VKGetDocsRequest(
    count: Int = 20,
    offset: Int = 0,
    type: Int = 0
) : VKRequest<List<VKDoc>>("docs.get") {
    init {
        addParam("count", count)
        addParam("offset", offset)
        addParam("type", type)
        addParam("return_tags", 1)
    }

    override fun parse(r: JSONObject): List<VKDoc> {
        val result = ArrayList<VKDoc>()
        val items = r.getJSONObject("response").getJSONArray("items")
        items.foreach {
            it as JSONObject
            result.add(VKDoc.parse(it))
        }
        return result
    }
}