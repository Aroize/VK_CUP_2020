package ru.rain.ifmo.dtask.presentation.presenter

import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import ru.rain.ifmo.dtask.App
import ru.rain.ifmo.dtask.data.models.VKAlbum
import ru.rain.ifmo.dtask.data.models.VKPhoto
import ru.rain.ifmo.dtask.data.requests.VKDeleteAlbumsRequest
import ru.rain.ifmo.dtask.data.requests.VKDeletePhotoRequest
import ru.rain.ifmo.dtask.data.requests.VKPhotoGetRequest
import ru.rain.ifmo.dtask.domain.mvp.BasePresenter
import ru.rain.ifmo.dtask.presentation.view.AlbumContentView

class AlbumContentPresenter : BasePresenter<AlbumContentView>() {

    private var cachedData = hashMapOf<Int, ArrayList<VKPhoto>>()

    private var currentAlbum = VKAlbum()

    val data: ArrayList<VKPhoto>
    get() {
        var cached = cachedData[currentAlbum.id]
        if (cached == null) {
            cached = arrayListOf()
            cachedData[currentAlbum.id] = cached
        }
        return cached
    }

    override var viewState: AlbumContentView? = null

    override fun onAttach() {
        val album = viewState!!.album()
        if (album.id != currentAlbum.id) {
            currentAlbum = album
        }
        if (cachedData[currentAlbum.id].isNullOrEmpty()) {
            requestPhotos()
        }
    }

    fun requestPhotos(count: Int = 20, offset: Int = 0) {
        VK.execute(
            VKPhotoGetRequest(currentAlbum.id, count, offset),
            object : VKApiCallback<List<VKPhoto>> {
                override fun fail(error: Exception) {
                    App.log(error)
                }

                override fun success(result: List<VKPhoto>) {
                    cachePhotos(result as ArrayList)
                }
            })
    }

    private fun cachePhotos(result: ArrayList<VKPhoto>) {
        val cached = cachedData[currentAlbum.id]
        if (cached == null) {
            cachedData[currentAlbum.id] = result
            viewState?.dataChanged(result)

        } else {
            val filtered = result.filter { !cached.contains(it) }
            val size = cached.size
            cached.addAll(filtered)
            viewState?.dataUpdated(size, filtered.size)
        }
    }

    override fun onDetach() {

    }

    fun deletePhoto(t: VKPhoto) {
        VK.execute(VKDeletePhotoRequest(t), object : VKApiCallback<Int> {
            override fun fail(error: Exception) {
                App.log(error)
            }

            override fun success(result: Int) {
                App.log("Success deleting photo with id=${t.id}")
            }
        })
    }
}