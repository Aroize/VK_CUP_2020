package ru.rain.ifmo.dtask.presentation.view

import ru.rain.ifmo.dtask.data.models.VKAlbum
import ru.rain.ifmo.dtask.data.models.VKPhoto
import ru.rain.ifmo.dtask.domain.mvp.MvpView
import kotlin.collections.ArrayList

interface AlbumContentView : MvpView {
    fun album(): VKAlbum
    fun dataChanged(newData: ArrayList<VKPhoto>)
    fun dataUpdated(oldSize: Int, size: Int)
}