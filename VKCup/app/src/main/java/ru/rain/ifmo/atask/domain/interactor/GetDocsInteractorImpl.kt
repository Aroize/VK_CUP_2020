package ru.rain.ifmo.atask.domain.interactor

import android.util.Log
import com.vk.api.sdk.VK
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.schedulers.Schedulers
import ru.rain.ifmo.atask.data.models.VKDoc
import ru.rain.ifmo.atask.data.requests.VKGetDocsRequest

class GetDocsInteractorImpl : GetDocsInteractor {
    override fun getDocs(count: Int, offset: Int): Observable<List<VKDoc>> {
        //This observable will emit two lists: One from cache and one from net
        //Also he will find diff between them in background thread
        //For now - only network
        return Observable.create {emitter: ObservableEmitter<List<VKDoc>> ->
            try {
                val list = VK.executeSync(VKGetDocsRequest(count, offset))
                list.forEach {
                    Log.i("DocListTag", "id=${it.id} title=${it.title}")
                }
                emitter.onNext(list)
                emitter.onComplete()
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
            .subscribeOn(Schedulers.io())
    }
}