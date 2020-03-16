package ru.rain.ifmo.atask.domain.mvp

abstract class BasePresenter<T : MvpView> {

    abstract var viewState: T?

    fun attach(view: T) {
        viewState = view
        onAttach()
    }

    abstract fun onAttach()

    fun detach() {
        onDetach()
        viewState = null
    }

    abstract fun onDetach()
}