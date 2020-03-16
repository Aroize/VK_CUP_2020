package ru.rain.ifmo.ftask.presentation.view

import ru.rain.ifmo.ftask.data.models.VKGroup
import ru.rain.ifmo.ftask.domain.mvp.MvpView

interface GroupsListView : MvpView {
    fun addGroups(list: List<VKGroup>)

    fun showLeaveSizeString(str: String)

    fun hideSizeString()

    fun removeAll(collection: Collection<VKGroup>)

    fun showLoading()

    fun hideLoading()
}