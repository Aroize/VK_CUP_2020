package ru.rain.ifmo.btask.clustering

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import ru.rain.ifmo.btask.data.models.VKGroup
import ru.rain.ifmo.btask.data.models.VKPhoto

data class GroupClusterItem(val item: Any, val index: Int) : ClusterItem {
    override fun getPosition(): LatLng {
        return when(item) {
            is VKPhoto -> item.latLng
            is VKGroup -> item.addresses[index].latLng
            else -> throw IllegalArgumentException("Bruh")
        }
    }

    override fun getSnippet() = ""

    override fun getTitle() = ""
}