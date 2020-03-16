package ru.rain.ifmo.btask.clustering

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import de.hdodenhof.circleimageview.CircleImageView
import ru.rain.ifmo.btask.R
import ru.rain.ifmo.btask.createCustomMarker
import ru.rain.ifmo.btask.data.models.VKGroup
import ru.rain.ifmo.btask.data.models.VKPhoto

class GroupClusterRenderer(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<GroupClusterItem>
) : DefaultClusterRenderer<GroupClusterItem>(context, map, clusterManager) {

    @SuppressLint("InflateParams")
    override fun onBeforeClusterItemRendered(
        item: GroupClusterItem?,
        markerOptions: MarkerOptions?
    ) {
        item as GroupClusterItem
        val markerItem = if (item.item is VKGroup) {
            val view = LayoutInflater.from(context).inflate(R.layout.group_marker_item, null)
            view.findViewById<TextView>(R.id.count).visibility = View.GONE
            val photo = item.item.photo
            val roundedBitmap = RoundedBitmapDrawableFactory.create(context.resources, photo)
            roundedBitmap.cornerRadius = context.resources.getDimensionPixelSize(R.dimen.radius).toFloat()
            view.findViewById<CircleImageView>(R.id.marker_item).setImageDrawable(roundedBitmap)
            view
        } else {
            item.item as VKPhoto
            val view = LayoutInflater.from(context).inflate(R.layout.marker_item, null)
            view.findViewById<ImageView>(R.id.marker_item).apply {
                clipToOutline = true
                val photo = item.item.bitmap
                val roundedBitmap = RoundedBitmapDrawableFactory.create(context.resources, photo)
                roundedBitmap.cornerRadius = context.resources.getDimensionPixelSize(R.dimen.radius).toFloat()
                setImageDrawable(roundedBitmap)
            }
            view.findViewById<TextView>(R.id.count).visibility = View.GONE
            view
        }
        markerOptions?.icon(
            BitmapDescriptorFactory.fromBitmap(
                createCustomMarker(context, markerItem)
            )
        )
    }

    @SuppressLint("InflateParams")
    override fun onBeforeClusterRendered(
        cluster: Cluster<GroupClusterItem>?,
        markerOptions: MarkerOptions?
    ) {
        cluster as Cluster<GroupClusterItem>
        val item = cluster.items.random().item
        val markerItem = if (item is VKGroup) {
            val view = LayoutInflater.from(context).inflate(R.layout.group_marker_item, null)
            view.findViewById<TextView>(R.id.count).text = cluster.size.toString()
            val photo = item.photo
            val roundedBitmap = RoundedBitmapDrawableFactory.create(context.resources, photo)
            roundedBitmap.cornerRadius = context.resources.getDimensionPixelSize(R.dimen.radius).toFloat()
            view.findViewById<CircleImageView>(R.id.marker_item).setImageDrawable(roundedBitmap)
            view
        } else {
            item as VKPhoto
            val view = LayoutInflater.from(context).inflate(R.layout.marker_item, null)
            view.findViewById<ImageView>(R.id.marker_item).apply {
                clipToOutline = true
                val photo = item.bitmap
                val roundedBitmap = RoundedBitmapDrawableFactory.create(context.resources, photo)
                roundedBitmap.cornerRadius = context.resources.getDimensionPixelSize(R.dimen.radius).toFloat()
                setImageDrawable(roundedBitmap)
            }
            view.findViewById<TextView>(R.id.count).text = cluster.size.toString()
            view
        }
        markerOptions?.icon(
            BitmapDescriptorFactory.fromBitmap(
                createCustomMarker(context, markerItem)
            )
        )
    }

    override fun shouldRenderAsCluster(cluster: Cluster<GroupClusterItem>?): Boolean {
        cluster ?: return false
        return cluster.size > 2
    }
}