package ru.rain.ifmo.btask.mvp

abstract class BasePresenter<T: MvpView> {
    abstract var viewState : T?

    abstract fun onAttach()

    abstract fun onDetach()

    open fun attach(view: T) {
        viewState = view
        onAttach()
    }

    open fun detach() {
        onDetach()
        viewState = null
    }
}