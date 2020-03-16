package ru.rain.ifmo.taskg.presentation.presenter

import android.content.Context
import androidx.core.content.edit
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.rain.ifmo.taskg.data.models.VKCity
import ru.rain.ifmo.taskg.data.models.VKGroup
import ru.rain.ifmo.taskg.data.requests.VKCitiesCommand
import ru.rain.ifmo.taskg.data.requests.VKSearchGroupsRequest
import ru.rain.ifmo.taskg.domain.mvp.BasePresenter
import ru.rain.ifmo.taskg.presentation.activity.ShopsListActivity
import ru.rain.ifmo.taskg.presentation.view.ShopsListView
import java.lang.Exception

class ShopsListPresenter : BasePresenter<ShopsListView>() {

    override var viewState: ShopsListView? = null

    private val cityCases = hashMapOf<Int, String>()

    private var city: VKCity = VKCity()
        set(value) {
            field = value
            requestGroups(erase = true)
        }

    private val groupsMap = hashMapOf<Int, List<VKGroup>>()

    private var cityList: List<VKCity> = emptyList()

    override fun onDetach() {}

    override fun onAttach() {
        ShopsListActivity.log("Attach")
        if (cityList.isEmpty()) {
            ShopsListActivity.log("requesting cities")
            requestCities()
        } else {
            if (city.id > 0) {
                ShopsListActivity.log("setting city from attach")
                updateToolbar(city)
                requestGroups(erase = true)
            }
        }
    }

    private fun updateToolbar(city: VKCity) {
        val found = cityCases[city.id]
        if (found != null) {
            viewState?.updateToolbarTitle(found)
        } else {
            Thread {
                val case: String
                val client = OkHttpClient()
                val urlBuilder =
                    HttpUrl.parse("https://ws3.morpher.ru/russian/declension")!!.newBuilder()
                urlBuilder.addQueryParameter("s", city.title)
                val request = Request.Builder()
                    .url(urlBuilder.build())
                    .build()
                val response = client.newCall(request).execute()
                val xml = response.body()!!.string()
                val start = xml.indexOf("<П>")
                val end = xml.indexOf("</П>")
                case = xml.substring(start + 3, end)
                cityCases[city.id] = case
                viewState?.updateToolbarTitle(case)
            }.start()
        }
    }

    private fun requestCities() {
        VK.execute(VKCitiesCommand(), object : VKApiCallback<List<VKCity>> {
            override fun fail(error: Exception) {
                ShopsListActivity.log(error)
                requestCities()
            }

            override fun success(result: List<VKCity>) {
                cityList = result
                if (city.id == 0) {
                    ShopsListActivity.log("setting city from request")
                    city = cityList.first()
                    updateToolbar(city)
                }
            }
        })
    }

    internal fun requestGroups(count: Int = 20, offset: Int = 0, erase: Boolean = false) {
        if (groupsMap.contains(city.id) && offset < groupsMap[city.id]!!.size) {
            ShopsListActivity.log("showing cached groups")
            viewState?.showGroups(groupsMap[city.id]!!, erase)
        } else {
            VK.execute(
                VKSearchGroupsRequest(count, offset, city.id),
                object : VKApiCallback<List<VKGroup>> {
                    override fun fail(error: Exception) {
                        ShopsListActivity.log(error)
                    }

                    override fun success(result: List<VKGroup>) {
                        ShopsListActivity.log("showing groups from network")
                        if (groupsMap[city.id] != null) {
                            val current = groupsMap[city.id]!!
                            val filtered = result.filter { !current.contains(it) }
                            (groupsMap[city.id] as ArrayList).addAll(filtered)
                            viewState?.showGroups(filtered, erase)
                        } else {
                            groupsMap[city.id] = result
                            viewState?.showGroups(result, erase)
                        }
                    }
                })
        }
    }

    fun chooseCity() {
        viewState?.showCities(cityList, city)
    }

    fun picked(picked: VKCity) {
        ShopsListActivity.log("picked = $picked")
        if (picked.id != city.id) {
            city = picked
            updateToolbar(city)
        }
    }
}