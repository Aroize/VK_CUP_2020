package ru.rain.ifmo.dtask.presentation.view

import ru.rain.ifmo.dtask.domain.mvp.MvpView

interface AlbumListView : MvpView {
    fun updateData(oldSize: Int, size: Int)
    fun updateItem(index: Int)
}