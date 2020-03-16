package ru.rain.ifmo.taskg.presentation.view

import ru.rain.ifmo.taskg.data.models.VKCity
import ru.rain.ifmo.taskg.data.models.VKGroup
import ru.rain.ifmo.taskg.domain.mvp.MvpView

interface ShopsListView : MvpView {
    fun updateToolbarTitle(city: String)

    fun showCities(cityList: List<VKCity>, picked: VKCity)
    fun showGroups(list: List<VKGroup>, erasePrev: Boolean)
}