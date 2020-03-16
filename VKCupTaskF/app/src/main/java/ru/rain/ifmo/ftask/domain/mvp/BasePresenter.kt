package ru.rain.ifmo.ftask.domain.mvp

abstract class BasePresenter<T : MvpView> {

    abstract var viewState: T?

    fun attach(view: T) {
        viewState = view
        onAttach()
    }

    fun detach() {
        onDetach()
        viewState = null
    }

    abstract fun onAttach()

    abstract fun onDetach()
}