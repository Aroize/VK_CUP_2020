package ru.rain.ifmo.dtask

import android.app.Application
import android.util.Log
import com.vk.api.sdk.auth.VKAccessToken
import ru.rain.ifmo.dtask.domain.mvp.BasePresenter
import ru.rain.ifmo.dtask.domain.mvp.MvpView
import ru.rain.ifmo.dtask.presentation.presenter.AlbumContentPresenter
import ru.rain.ifmo.dtask.presentation.presenter.AlbumListPresenter
import ru.rain.ifmo.dtask.presentation.view.AlbumContentView
import ru.rain.ifmo.dtask.presentation.view.AlbumListView
import java.lang.Exception

class App : Application() {
    companion object {

        private val albumListPresenter = AlbumListPresenter()
        private val albumContentPresenter = AlbumContentPresenter()

        fun<T : MvpView> delegatePresenter(view: T) : BasePresenter<*> {
            return when (view) {
                is AlbumListView -> albumListPresenter
                is AlbumContentView -> albumContentPresenter
                else -> throw IllegalArgumentException("$view is not instance of MvpView")
            }
        }

        var token: VKAccessToken? = null

        fun log(message: String) {
            Log.d("TASK_D", message)
        }

        fun log(e: Exception) {
            log("$e", e)
        }

        fun log(message: String, e: Exception) {
            Log.d("TASK_D", message, e)
        }

        const val VK_API_VERSION = "5.103"

    }
}