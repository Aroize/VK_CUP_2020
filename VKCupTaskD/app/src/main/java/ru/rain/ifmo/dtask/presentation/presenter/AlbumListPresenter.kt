package ru.rain.ifmo.dtask.presentation.presenter

import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import ru.rain.ifmo.dtask.App
import ru.rain.ifmo.dtask.data.models.VKAlbum
import ru.rain.ifmo.dtask.data.requests.VKCreateAlbumRequest
import ru.rain.ifmo.dtask.data.requests.VKDeleteAlbumsRequest
import ru.rain.ifmo.dtask.data.requests.VKGetAlbumRequest
import ru.rain.ifmo.dtask.data.requests.VKGetAlbumsRequest
import ru.rain.ifmo.dtask.domain.mvp.BasePresenter
import ru.rain.ifmo.dtask.presentation.view.AlbumListView

class AlbumListPresenter : BasePresenter<AlbumListView>() {
    override var viewState: AlbumListView? = null

    private var pickedAlbum = VKAlbum()

    val data = ArrayList<VKAlbum>()

    override fun onAttach() {
        if (data.isEmpty())
            requestAlbums()
        if (pickedAlbum.id != 0)
            updateAlbum()
    }

    private fun updateAlbum() {
        VK.execute(VKGetAlbumRequest(pickedAlbum.id), object : VKApiCallback<VKAlbum>{
            override fun fail(error: Exception) {
                App.log(error)
            }

            override fun success(result: VKAlbum) {
                val index = data.indexOfFirst { it.id == result.id }
                data.removeAt(index)
                data.add(index, result)
                viewState?.updateItem(index)
            }
        })
    }

    override fun onDetach() {

    }

    fun requestAlbums(count: Int = 20, offset: Int = 0) {
        VK.execute(VKGetAlbumsRequest(count, offset), object : VKApiCallback<List<VKAlbum>>{
            override fun fail(error: Exception) {
                App.log(error)
            }

            override fun success(result: List<VKAlbum>) {
                val filtered = result.filter { !data.contains(it) }
                val size = data.size
                data.addAll(filtered)
                viewState?.updateData(size, filtered.size)
            }
        })
    }

    fun deleteAlbum(t: VKAlbum) {
        VK.execute(VKDeleteAlbumsRequest(t.id), object : VKApiCallback<Int> {
            override fun fail(error: Exception) {
                App.log(error)
            }

            override fun success(result: Int) {
                App.log("Successfully deleted album with id=${t.id}")
            }
        })
    }

    fun picked(t: VKAlbum) {
        pickedAlbum = t
    }

    fun createAlbum(text: String) {
        VK.execute(VKCreateAlbumRequest(text), object : VKApiCallback<VKAlbum>{
            override fun fail(error: Exception) {
                App.log(error)
            }

            override fun success(result: VKAlbum) {
                data.add(result)
                viewState?.updateData(data.size - 1, 1)
            }
        })
    }
}