package ru.rain.ifmo.ftask.data.requests

import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject

class VKLeaveGroupRequest(id: Int) : VKRequest<Int>("groups.leave"){
    init {
        addParam("group_id", id)
    }

    override fun parse(r: JSONObject): Int {
        return r.optInt("response")
    }
}