package ru.rain.ifmo.ftask.domain.interactor

import android.util.Log
import com.vk.api.sdk.VK
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import io.reactivex.rxjava3.schedulers.Schedulers
import ru.rain.ifmo.ftask.data.models.VKGroup
import ru.rain.ifmo.ftask.data.models.VKGroupInfo
import ru.rain.ifmo.ftask.data.requests.VKExtraInfoCommand
import ru.rain.ifmo.ftask.data.requests.VKGetGroupsRequest
import ru.rain.ifmo.ftask.data.requests.VKLeaveGroupRequest
import java.lang.Exception

class GroupsInteractorImpl : GroupsInteractor {
    override fun getGroups(count: Int, offset: Int): Single<List<VKGroup>> {
        return Single.create<List<VKGroup>> {
            try {
                val list = VK.executeSync(VKGetGroupsRequest(count, offset))
                    .filter { vkGroup ->  !vkGroup.deactivated }
                list.forEach { vkGroup ->
                    Log.i("GroupsTag", vkGroup.toString())
                }
                it.onSuccess(list)
            } catch (e: Exception) {
                it.onError(e)
            }
        }
            .subscribeOn(Schedulers.io())
    }

    override fun leaveGroups(collection: Collection<VKGroup>): Completable {
        return Completable.create {
            collection.forEach { vkGroup ->
                val i = VK.executeSync(VKLeaveGroupRequest(vkGroup.id))
                Log.i("GroupsTag", "group with ${vkGroup.id} - result=$i")
            }
            it.onComplete()
        }
            .subscribeOn(Schedulers.io())
    }

    override fun groupInfo(id: Int): Single<VKGroupInfo> {
        return Single.create { emitter: SingleEmitter<VKGroupInfo> ->
            val result = VK.executeSync(VKExtraInfoCommand(id))
            emitter.onSuccess(result)
        }
            .subscribeOn(Schedulers.io())
    }
}