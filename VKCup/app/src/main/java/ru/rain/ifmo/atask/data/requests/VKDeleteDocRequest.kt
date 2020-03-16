package ru.rain.ifmo.atask.data.requests

import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject
import ru.rain.ifmo.atask.data.models.VKDoc
import ru.rain.ifmo.atask.presentation.activity.AuthActivity

class VKDeleteDocRequest(doc: VKDoc) : VKRequest<Int>("docs.delete") {
    init {
        addParam("owner_id", AuthActivity.token!!.userId)
        addParam("doc_id", doc.id)
    }

    override fun parse(r: JSONObject): Int {
        return r.getInt("response")
    }
}