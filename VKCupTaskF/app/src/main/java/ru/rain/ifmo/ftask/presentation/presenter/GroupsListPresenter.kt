package ru.rain.ifmo.ftask.presentation.presenter

import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import ru.rain.ifmo.ftask.data.models.VKGroup
import ru.rain.ifmo.ftask.domain.interactor.GroupsInteractor
import ru.rain.ifmo.ftask.domain.mvp.BasePresenter
import ru.rain.ifmo.ftask.presentation.view.GroupsListView
import java.net.SocketTimeoutException
import kotlin.math.min

class GroupsListPresenter constructor(
    private val groupsInteractor: GroupsInteractor
) : BasePresenter<GroupsListView>() {

    private val disposable = CompositeDisposable()

    private var currentData = arrayListOf<VKGroup>()

    private var setToLeave = hashSetOf<VKGroup>()

    override var viewState: GroupsListView? = null

    fun isPicked(vkGroup: VKGroup) = setToLeave.contains(vkGroup)

    fun addToLeave(group: VKGroup) {
        setToLeave.add(group)
        updateBtnCounter()
    }

    fun removeFromLeave(group: VKGroup) {
        setToLeave.remove(group)
        if (setToLeave.isEmpty())
            viewState?.hideSizeString()
        else
            updateBtnCounter()
    }

    private fun updateBtnCounter() {
        viewState?.showLeaveSizeString(
            min(MAX_GROUPS, setToLeave.size).toString()
        )
    }

    override fun onAttach() {
        if (setToLeave.isEmpty())
            viewState?.hideSizeString()
        else
            updateBtnCounter()
        if (currentData.isEmpty()) {
            viewState?.showLoading()
            requestGroups()
        } else {
            viewState?.addGroups(currentData)
        }
    }

    fun requestGroups(count: Int = 20, offset: Int = 0) {
        disposable.add(
            groupsInteractor.getGroups(count, offset)
                .map { it.filter { vkGroup ->  !currentData.contains(vkGroup) } }
                .map { currentData.addAll(it); it }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({
                    it.forEach { vkGroup ->
                        Log.d("GroupsTag", vkGroup.toString())
                    }
                    viewState?.addGroups(it)
                    viewState?.hideLoading()
                    if (setToLeave.isNotEmpty())
                        updateBtnCounter()
                }, {
                    Log.e("GroupsTag", "Exception while requesting groups", it)
                    if (it is SocketTimeoutException) {
                        requestGroups(count, offset)
                    }
                })
        )
    }

    override fun onDetach() {}

    @Suppress("UNCHECKED_CAST")
    fun unsubscribe() {
        Log.d("UnsubscribeTag", "Unsubscribe size : ${setToLeave.size}")
        if (setToLeave.isNotEmpty()) {
            val copy = setToLeave.clone() as HashSet<VKGroup>
            setToLeave.clear()
            viewState?.hideSizeString()
            viewState?.removeAll(copy)
            disposable.add(
                groupsInteractor.leaveGroups(copy)
                    .subscribe ({
                        Log.d("UnsubscribeTag", "Unsubscribed")
                    }, {
                        Log.d("UnsubscribeTag", "Exception", it)
                    })
            )
        }
    }

    companion object {
        private const val MAX_GROUPS = 99
    }
}