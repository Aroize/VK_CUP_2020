package ru.rain.ifmo.atask.domain.interactor

import com.vk.api.sdk.VK
import com.vk.api.sdk.exceptions.VKApiException
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import ru.rain.ifmo.atask.data.models.VKDoc
import ru.rain.ifmo.atask.data.requests.VKDeleteDocRequest
import ru.rain.ifmo.atask.data.requests.VKEditDocRequest

class RenameDeleteInteractorImpl : RenameDeleteInteractor {
    override fun deleteDoc(doc: VKDoc): Completable {
        return Completable.create {
            val result = VK.executeSync(VKDeleteDocRequest(doc))
            if (result == 1)
                it.onComplete()
            else
                it.onError(VKApiException("Response : $result"))
        }
            .subscribeOn(Schedulers.io())
    }

    override fun renameDoc(doc: VKDoc): Completable {
        return Completable.create {
            val result = VK.executeSync(VKEditDocRequest(doc))
            if (result == 1)
                it.onComplete()
            else
                it.onError(VKApiException("Response : $result"))
        }
            .subscribeOn(Schedulers.io())
    }
}