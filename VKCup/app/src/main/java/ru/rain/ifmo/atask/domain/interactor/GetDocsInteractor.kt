package ru.rain.ifmo.atask.domain.interactor

import io.reactivex.rxjava3.core.Observable
import ru.rain.ifmo.atask.data.models.VKDoc

interface GetDocsInteractor {
    fun getDocs(count: Int, offset: Int): Observable<List<VKDoc>>
}