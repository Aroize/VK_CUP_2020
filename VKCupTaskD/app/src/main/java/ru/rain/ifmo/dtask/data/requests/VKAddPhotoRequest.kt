package ru.rain.ifmo.dtask.data.requests

import android.net.Uri
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKHttpPostCall
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONObject
import ru.rain.ifmo.dtask.App
import ru.rain.ifmo.dtask.data.models.VKPhoto
import ru.rain.ifmo.dtask.forEach
import java.lang.Exception
import java.util.concurrent.TimeUnit

class VKAddPhotoRequest(
    private val albumId: Int,
    private val fileUri: Uri
) : ApiCommand<VKPhoto>() {
    override fun onExecute(manager: VKApiManager): VKPhoto {
        App.log("Start uploading")
        val uploadServerCall = VKMethodCall.Builder()
            .method("photos.getUploadServer")
            .args("album_id", "$albumId")
            .version(App.VK_API_VERSION)
            .build()
        val uploadServer = manager.execute(uploadServerCall, VKApiResponseParser<VKUploadServer> {
            try {
                val json = JSONObject(it).getJSONObject("response")
                VKUploadServer(
                    uploadUrl = json.optString("upload_url"),
                    albumId = json.optInt("album_id"),
                    userId = json.optInt("user_id")
                )
            } catch (e: Exception) {
                throw VKApiIllegalResponseException(e)
            }
        })
        App.log("Upload server: $uploadServer")
        val saveInfoCall = VKHttpPostCall.Builder()
            .url(uploadServer.uploadUrl)
            .args("file1", fileUri)
            .timeout(TimeUnit.SECONDS.toMillis(10))
            .retryCount(3)
            .build()
        val fileUploadInfo = manager.execute(saveInfoCall, null ,VKApiResponseParser<VKFileUploadInfo> {
            try {
                val json = JSONObject(it)
                VKFileUploadInfo(
                    server = json.optInt("server"),
                    photoList = json.optString("photos_list"),
                    aid = json.optInt("aid"),
                    hash = json.optString("hash")
                )
            } catch (e: Exception) {
                throw VKApiIllegalResponseException(e)
            }
        })
        App.log("File upload info: $fileUploadInfo")
        val saveCall = VKMethodCall.Builder()
            .method("photos.save")
            .args(hashMapOf(
                "album_id" to "$albumId",
                "server" to "${fileUploadInfo.server}",
                "photos_list" to fileUploadInfo.photoList,
                "aid" to "${fileUploadInfo.aid}",
                "hash" to fileUploadInfo.hash
            ))
            .version(App.VK_API_VERSION)
            .build()
        return manager.execute(saveCall, VKApiResponseParser<VKPhoto> {
            try {
                val json = JSONObject(it).getJSONArray("response").getJSONObject(0)
                VKPhoto.parse(json)
            } catch (e: Exception) {
                throw VKApiIllegalResponseException(e)
            }
        })
    }

    private data class VKUploadServer(
        val uploadUrl: String,
        val albumId: Int,
        val userId: Int
    )

    private data class VKFileUploadInfo(
        val server: Int,
        val photoList: String,
        val aid: Int,
        val hash: String
    )
}