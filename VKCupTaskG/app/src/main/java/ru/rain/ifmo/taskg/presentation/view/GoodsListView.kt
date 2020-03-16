package ru.rain.ifmo.taskg.presentation.view

import android.util.SparseBooleanArray
import ru.rain.ifmo.taskg.data.models.VKGroup
import ru.rain.ifmo.taskg.data.models.VKMarketItem
import ru.rain.ifmo.taskg.domain.mvp.MvpView

interface GoodsListView : MvpView {
    fun getGroup(): VKGroup
    fun showMarketItems(list: List<VKMarketItem>)
    fun getChangedItems(): SparseBooleanArray
}