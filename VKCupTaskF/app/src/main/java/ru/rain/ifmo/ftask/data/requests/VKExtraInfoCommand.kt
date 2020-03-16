package ru.rain.ifmo.ftask.data.requests

import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONObject
import ru.rain.ifmo.ftask.data.models.VKGroupInfo

class VKExtraInfoCommand(private val id: Int) : ApiCommand<VKGroupInfo>() {
    override fun onExecute(manager: VKApiManager): VKGroupInfo {
        val descriptionCall = VKMethodCall.Builder()
            .method("groups.getById")
            .args(
                hashMapOf(
                    "group_id" to id.toString(),
                    "fields" to "description,members_count"
                )
            )
            .version(manager.config.version)
            .build()
        val description = manager.execute(descriptionCall, VKApiResponseParser<Array<String>> {
            val array = Array<String>(3) { "" }
            val r = JSONObject(it).getJSONArray("response").getJSONObject(0)
            array[0] = r.optString("description")
            array[1] = r.optInt("members_count").toString()
            array[2] = r.optString("screen_name")
            array
        })
        val countParser = VKApiResponseParser<Int> {
            val r = JSONObject(it)
            r.getJSONObject("response").optInt("count")
        }
        val friendsCall = VKMethodCall.Builder()
            .method("groups.getMembers")
            .args(
                hashMapOf(
                    "group_id" to id.toString(),
                    "count" to "0",
                    "filter" to "friends"
                )
            )
            .version(manager.config.version)
            .build()
        val friends = manager.execute(friendsCall, countParser)
        val wallCall = VKMethodCall.Builder()
            .method("wall.get")
            .args(
                hashMapOf(
                    "owner_id" to "-$id",
                    "count" to "2"
                )
            )
            .version(manager.config.version)
            .build()
        val lastPostDate = manager.execute(wallCall, VKApiResponseParser<Int> {
            val r = JSONObject(it).getJSONObject("response").getJSONArray("items")
            if (r.length() < 2)
                return@VKApiResponseParser 0
            val index =
                if (r.getJSONObject(0).optInt("is_pinned") == 1) {
                    1
                } else {
                    0
                }
            r.getJSONObject(index).optInt("date")
        })
        return VKGroupInfo(
            description[1].toInt(), friends, description[0], lastPostDate, description[2]
        )
    }
}