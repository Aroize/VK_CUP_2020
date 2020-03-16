package ru.rain.ifmo.taskg.presentation.presenter

import androidx.core.util.forEach
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import ru.rain.ifmo.taskg.data.models.VKGroup
import ru.rain.ifmo.taskg.data.models.VKMarketItem
import ru.rain.ifmo.taskg.data.requests.VKGetMarketItems
import ru.rain.ifmo.taskg.domain.mvp.BasePresenter
import ru.rain.ifmo.taskg.presentation.activity.ShopsListActivity
import ru.rain.ifmo.taskg.presentation.view.GoodsListView

class GoodsListPresenter : BasePresenter<GoodsListView>() {

    private var group = VKGroup()

    override var viewState: GoodsListView? = null

    private val marketItems = arrayListOf<VKMarketItem>()

    override fun onDetach() {}

    override fun onAttach() {
        val currentGroup = viewState?.getGroup() as VKGroup
        if (currentGroup.id == group.id) {
            viewState?.getChangedItems()?.forEach { key, value ->
                marketItems.find { it.id == key }?.apply { isFavorite = value }
            }
            viewState?.showMarketItems(marketItems)
        } else {
            marketItems.clear()
            group = currentGroup
            requestMarketItems()
        }
    }

    fun requestMarketItems(count: Int = 20, offset: Int = 0) {
        ShopsListActivity.log("requesting id=${group.id}")
        VK.execute(VKGetMarketItems(
            -(group.id),
            count,
            offset
        ), object : VKApiCallback<List<VKMarketItem>>{
            override fun fail(error: Exception) {
                ShopsListActivity.log(error)
            }

            override fun success(result: List<VKMarketItem>) {
                val filtered = result.filter { !marketItems.contains(it) }
                marketItems.addAll(filtered)
                viewState?.showMarketItems(filtered)
            }
        })
    }
}