package ru.rain.ifmo.taskg

import android.app.Application
import android.content.Intent
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKTokenExpiredHandler
import com.vk.api.sdk.auth.VKAccessToken
import ru.rain.ifmo.taskg.domain.mvp.BasePresenter
import ru.rain.ifmo.taskg.domain.mvp.MvpView
import ru.rain.ifmo.taskg.presentation.activity.AuthActivity
import ru.rain.ifmo.taskg.presentation.activity.ShopsListActivity
import ru.rain.ifmo.taskg.presentation.presenter.GoodsListPresenter
import ru.rain.ifmo.taskg.presentation.presenter.ShopsListPresenter
import ru.rain.ifmo.taskg.presentation.view.GoodsListView
import ru.rain.ifmo.taskg.presentation.view.ShopsListView

class App : Application() {

    private val tokenHandler =  object : VKTokenExpiredHandler {
        override fun onTokenExpired() {
            token = null
            startActivity(Intent(this@App, AuthActivity::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        VK.addTokenExpiredHandler(tokenHandler)
    }

    companion object {

        var token: VKAccessToken? = null

        private val shopsListPresenter = ShopsListPresenter()
        private val goodsPresenter = GoodsListPresenter()

        fun<T : MvpView> delegatePresenter(view: T) : BasePresenter<*> {
            return when (view) {
                is ShopsListView -> shopsListPresenter
                is GoodsListView -> goodsPresenter
                else -> throw IllegalArgumentException("no instance of available presenter")
            }
        }
    }
}