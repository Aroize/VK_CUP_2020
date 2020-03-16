package ru.rain.ifmo.atask.domain.interactor

import io.reactivex.rxjava3.core.Completable
import ru.rain.ifmo.atask.data.models.VKDoc

interface RenameDeleteInteractor {
    fun deleteDoc(doc: VKDoc): Completable

    fun renameDoc(doc: VKDoc): Completable
}