package ru.rain.ifmo.taskg.domain.mvp

abstract class BasePresenter<T : MvpView> {

    abstract var viewState: T?

    open fun attach(view: T) {
        viewState = view
        onAttach()
    }

    open fun detach() {
        onDetach()
        viewState = null
    }

    abstract fun onDetach()

    abstract fun onAttach()
}