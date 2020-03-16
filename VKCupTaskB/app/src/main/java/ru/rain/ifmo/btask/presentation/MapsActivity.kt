package ru.rain.ifmo.btask.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.tabs.TabLayout
import com.google.maps.android.clustering.ClusterManager
import ru.rain.ifmo.btask.App
import ru.rain.ifmo.btask.R
import ru.rain.ifmo.btask.clustering.GroupClusterItem
import ru.rain.ifmo.btask.clustering.GroupClusterRenderer
import ru.rain.ifmo.btask.data.models.VKGroup
import ru.rain.ifmo.btask.data.models.VKPhoto
import ru.rain.ifmo.btask.mvp.MapsPresenter
import ru.rain.ifmo.btask.mvp.MapsPresenter.TABS
import ru.rain.ifmo.btask.mvp.MapsView

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, TabLayout.OnTabSelectedListener,
    MapsView {

    private lateinit var clusterManager: ClusterManager<GroupClusterItem>
    private var events = ArrayList<GroupClusterItem>()
    private var groups = ArrayList<GroupClusterItem>()
    private var photos = ArrayList<GroupClusterItem>()

    private lateinit var mMap: GoogleMap

    private lateinit var tabLayout: TabLayout

    private val presenter: MapsPresenter by lazy { App.delegatePresenter(this) as MapsPresenter }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_maps)
        tabLayout = findViewById(R.id.tablayout)
        tabLayout.addOnTabSelectedListener(this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
    }

    override fun onStop() {
        super.onStop()
        presenter.detach()
    }

    override fun showToast() {
        Toast.makeText(this, R.string.pls_wait, Toast.LENGTH_LONG).show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        clusterManager = ClusterManager(this, mMap)
        mMap.setOnMarkerClickListener(clusterManager)
        clusterManager.setOnClusterItemClickListener {
            when (it.item) {
                is VKPhoto -> {
                    val photoIntent = PhotoActivity.createIntent(this@MapsActivity, it)
                    startActivity(
                        photoIntent
                    )
                }
                is VKGroup -> {
                    val fragment = ExtraInfoFragment()
                    ExtraInfoFragment.item = it
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_up, 0)
                        .replace(R.id.fragment_container, fragment)
                        .commit()
                }
            }
            true
        }
        clusterManager.setOnClusterClickListener {
            if (it.items.random().item is VKPhoto) {
                val photoIntent = PhotoActivity.createIntent(this@MapsActivity, it)
                startActivity(
                    photoIntent
                )
            }
            return@setOnClusterClickListener false
        }
        clusterManager.renderer = GroupClusterRenderer(this, mMap, clusterManager)
        mMap.setOnCameraIdleListener(clusterManager)
        presenter.mapIsReady()
    }

    override fun setTab(currentTab: TABS) {
        val index = when (currentTab) {
            TABS.EVENT -> 0
            TABS.PHOTO -> 1
            TABS.GROUP -> 2
        }
        val tab = tabLayout.getTabAt(index)
        tab?.select()
    }

    override fun showGroups(result: List<VKGroup>) {
        result.forEach { group ->
            for (i in group.addresses.indices) {
                val item = GroupClusterItem(group, i)
                groups.add(item)
                clusterManager.addItem(item)
            }
        }
        clusterManager.cluster()
    }

    override fun showEvents(result: List<VKGroup>) {
        result.forEach { event ->
            for (i in event.addresses.indices) {
                val item = GroupClusterItem(event, i)
                events.add(item)
                clusterManager.addItem(item)
            }
        }
        clusterManager.cluster()
    }

    override fun showPhotos(result: List<VKPhoto>) {
        result.forEach { event ->
            val item = GroupClusterItem(event, 0)
            photos.add(item)
            clusterManager.addItem(item)
        }
        clusterManager.cluster()
    }

    override fun clearMarkers() {
        clusterManager.clearItems()
        groups.clear()
        events.clear()
        clusterManager.cluster()
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        tab ?: return
        App.log("tab selected ${tab.position}")
        presenter.tabSelected(tab.position)
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {

    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {

    }

    fun closeAdditionalInfo() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        fragment ?: return
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(0, R.anim.slide_down)
            .remove(fragment)
            .commit()
    }

    companion object {
        fun createIntent(`package`: Context) = Intent(`package`, MapsActivity::class.java)
    }
}
