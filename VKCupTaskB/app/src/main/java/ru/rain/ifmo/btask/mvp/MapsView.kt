package ru.rain.ifmo.btask.mvp

import ru.rain.ifmo.btask.data.models.VKGroup
import ru.rain.ifmo.btask.data.models.VKPhoto

interface MapsView : MvpView {
    fun setTab(currentTab: MapsPresenter.TABS)
    fun showGroups(result: List<VKGroup>)
    fun showEvents(result: List<VKGroup>)
    fun clearMarkers()
    fun showToast()
    fun showPhotos(result: List<VKPhoto>)
}