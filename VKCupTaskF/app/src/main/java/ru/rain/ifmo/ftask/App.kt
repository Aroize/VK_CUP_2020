package ru.rain.ifmo.ftask

import android.app.Application
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import ru.rain.ifmo.ftask.domain.interactor.GroupsInteractorImpl
import ru.rain.ifmo.ftask.domain.mvp.BasePresenter
import ru.rain.ifmo.ftask.domain.mvp.MvpView
import ru.rain.ifmo.ftask.presentation.fragment.GroupsListFragment
import ru.rain.ifmo.ftask.presentation.presenter.GroupsListPresenter

class App : Application() {
    companion object {
        fun<T : MvpView> delegatePresenter(mvpView: T): BasePresenter<*> {
            return when (mvpView) {
                is GroupsListFragment -> GroupsListPresenter(
                    GroupsInteractorImpl()
                )
                else -> throw IllegalArgumentException("No such presenter for $mvpView")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        RxJavaPlugins.setErrorHandler {}
    }
}