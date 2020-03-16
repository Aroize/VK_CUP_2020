package ru.rain.ifmo.atask.presentation

import ru.rain.ifmo.atask.data.models.VKDoc
import ru.rain.ifmo.atask.domain.mvp.MvpView

interface DocListView : MvpView {
    fun addDocs(list: List<VKDoc>)

    fun removeDoc(index: Int)

    fun showSpinner()

    fun hideSpinner()

    fun scrollTo(chosenIndex: Int)
}