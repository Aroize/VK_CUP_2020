package ru.rain.ifmo.ftask.domain.interactor

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import ru.rain.ifmo.ftask.data.models.VKGroup
import ru.rain.ifmo.ftask.data.models.VKGroupInfo

interface GroupsInteractor {
    fun getGroups(count: Int, offset: Int): Single<List<VKGroup>>

    fun leaveGroups(collection: Collection<VKGroup>): Completable

    fun groupInfo(id: Int): Single<VKGroupInfo>
}