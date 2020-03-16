package ru.rain.ifmo.atask

import android.app.Application
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import ru.rain.ifmo.atask.domain.interactor.GetDocsInteractorImpl
import ru.rain.ifmo.atask.domain.interactor.RenameDeleteInteractorImpl
import ru.rain.ifmo.atask.domain.mvp.BasePresenter
import ru.rain.ifmo.atask.domain.mvp.MvpView
import ru.rain.ifmo.atask.presentation.DocListPresenter
import ru.rain.ifmo.atask.presentation.fragment.DocListFragment
import java.lang.IllegalArgumentException

class App : Application() {
    companion object {

        private val docListPresenter = DocListPresenter(
            GetDocsInteractorImpl(),
            RenameDeleteInteractorImpl()
        )

        fun<T : MvpView> delegatePresenter(mvpView: T): BasePresenter<*> {
            when (mvpView) {
                is DocListFragment -> {
                    return docListPresenter
                }
            }
            throw IllegalArgumentException("instance of non-mvp view was provided (mark class with MvpView)")
        }
    }

    override fun onCreate() {
        super.onCreate()
        RxJavaPlugins.setErrorHandler {  }
    }
}