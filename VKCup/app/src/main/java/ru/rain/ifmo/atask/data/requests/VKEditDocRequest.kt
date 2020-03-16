package ru.rain.ifmo.atask.data.requests

import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject
import ru.rain.ifmo.atask.data.models.VKDoc
import ru.rain.ifmo.atask.presentation.activity.AuthActivity

class VKEditDocRequest(
    doc: VKDoc
) : VKRequest<Int>("docs.edit") {
    init {
        val title = if (doc.ext.isNotBlank()) "${doc.title}.${doc.ext}" else doc.title
        addParam("owner_id", AuthActivity.token!!.userId)
        addParam("doc_id", doc.id)
        addParam("title", title)
    }

    override fun parse(r: JSONObject): Int {
        return r.getInt("response")
    }
}