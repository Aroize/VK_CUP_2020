package ru.rain.ifmo.btask

import android.app.Application
import android.util.Log
import com.vk.api.sdk.auth.VKAccessToken
import ru.rain.ifmo.btask.mvp.BasePresenter
import ru.rain.ifmo.btask.mvp.MapsPresenter
import ru.rain.ifmo.btask.mvp.MapsView
import ru.rain.ifmo.btask.mvp.MvpView

class App : Application() {
    companion object {

        private val mapsPresenter = MapsPresenter()

        fun<T: MvpView> delegatePresenter(view: T) : BasePresenter<*> {
            return when (view) {
                is MapsView -> mapsPresenter
                else -> throw IllegalArgumentException("Wrong instance provided")
            }
        }

        var token: VKAccessToken? = null

        fun log(msg: String) {
            Log.d("TASK_B", msg)
        }
    }
}