package ru.rain.ifmo.btask.mvp

import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import ru.rain.ifmo.btask.App
import ru.rain.ifmo.btask.data.models.VKGroup
import ru.rain.ifmo.btask.data.models.VKPhoto
import ru.rain.ifmo.btask.data.requests.VKGetGroupsRequest
import ru.rain.ifmo.btask.data.requests.VKGetPhotosRequest
import ru.rain.ifmo.btask.mvp.MapsPresenter.TABS.*

class MapsPresenter : BasePresenter<MapsView>() {

    enum class TABS {
        EVENT,
        PHOTO,
        GROUP
    }

    private var photoAlreadyRequested: Boolean = false
    private var eventsAlreadyRequested: Boolean = false
    private var groupsAlreadyRequested: Boolean = false
    private var currentTab = EVENT

    private var cachedEvents = ArrayList<VKGroup>()
    private var cachedGroups = ArrayList<VKGroup>()
    private var cachedPhotos = ArrayList<VKPhoto>()

    override var viewState: MapsView? = null

    override fun onAttach() {
    }

    override fun onDetach() {}

    fun mapIsReady() {
        App.log("map is ready")
        viewState?.setTab(currentTab)
        when (currentTab) {
            EVENT -> {
                if (cachedEvents.isNotEmpty() || eventsAlreadyRequested) {
                    App.log("cache size = ${cachedEvents.size}")
                    cachedEvents.forEach {
                        App.log(it.toString())
                    }
                    viewState?.showEvents(cachedEvents)
                } else {
                    App.log("requesting events")
                    viewState?.showToast()
                    requestEvents()
                }
            }
            PHOTO -> {
                if (cachedPhotos.isNotEmpty() || photoAlreadyRequested) {
                    viewState?.showPhotos(cachedPhotos)
                } else {
                    photoAlreadyRequested = true
                    requestPhotos()
                }
            }
            GROUP -> {
                if (cachedGroups.isNotEmpty() || groupsAlreadyRequested) {
                    App.log("cache size = ${cachedGroups.size}")
                    viewState?.showGroups(cachedGroups)
                } else {
                    App.log("requesting groups")
                    viewState?.showToast()
                    requestGroups()
                }
            }
        }
    }

    private fun requestPhotos() {
        VK.execute(VKGetPhotosRequest(), object  : VKApiCallback<List<VKPhoto>>{
            override fun fail(error: Exception) {
                App.log("$error")
            }

            override fun success(result: List<VKPhoto>) {
                cachedPhotos.addAll(result)
                if (currentTab == PHOTO)
                    viewState?.showPhotos(result)
            }
        })
    }

    private fun requestEvents() {
        VK.execute(VKGetGroupsRequest("events"), object : VKApiCallback<List<VKGroup>> {
            override fun fail(error: Exception) {
                App.log(error.toString())
            }

            override fun success(result: List<VKGroup>) {
                cachedEvents.addAll(result)
                if (currentTab == EVENT)
                    viewState?.showEvents(result)
            }
        })
    }

    private fun requestGroups() {
        VK.execute(VKGetGroupsRequest("groups,hasAddress"), object : VKApiCallback<List<VKGroup>> {
            override fun fail(error: Exception) {
                App.log(error.toString())
            }

            override fun success(result: List<VKGroup>) {
                cachedGroups.addAll(result)
                if (currentTab == GROUP)
                    viewState?.showGroups(result)
            }
        })
    }

    fun tabSelected(position: Int) {
        viewState?.clearMarkers()
        when (position) {
            0 -> {
                currentTab = EVENT
                viewState?.setTab(currentTab)
            }
            1 -> {
                currentTab = PHOTO
                viewState?.setTab(currentTab)
            }
            2 -> {
                currentTab = GROUP
                viewState?.setTab(currentTab)
            }
        }
        mapIsReady()
    }
}