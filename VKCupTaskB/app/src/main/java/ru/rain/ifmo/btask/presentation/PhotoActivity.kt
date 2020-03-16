package ru.rain.ifmo.btask.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.maps.android.clustering.Cluster
import ru.rain.ifmo.btask.R
import ru.rain.ifmo.btask.clustering.GroupClusterItem
import ru.rain.ifmo.btask.data.models.VKPhoto

class PhotoActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager

    private val data = ArrayList<GroupClusterItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.photo_activity)
        supportActionBar?.hide()
        cluster?.items?.forEach {
            data.add(it)
        }
        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = PhotoAdapter()
        findViewById<ImageButton>(R.id.back_btn).setOnClickListener {
            onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        item = null
        cluster = null
    }

    private inner class PhotoAdapter : PagerAdapter() {
        override fun getCount() = cluster?.size ?: 1

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = layoutInflater.inflate(R.layout.image_item, container, false) as ImageView
            if (cluster != null) {
                Glide.with(this@PhotoActivity)
                    .load((data[position].item as VKPhoto).orig)
                    .into(view)
            } else {
                Glide.with(this@PhotoActivity)
                    .load((item!!.item as VKPhoto).orig)
                    .into(view)
            }
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
    }

    companion object {
        fun createIntent(context: Context, item: GroupClusterItem): Intent {
            PhotoActivity.item = item
            return Intent(context, PhotoActivity::class.java)
        }

        fun createIntent(context: Context, cluster: Cluster<GroupClusterItem>): Intent {
            PhotoActivity.cluster = cluster
            return Intent(context, PhotoActivity::class.java)
        }

        private var item: GroupClusterItem? = null

        private var cluster: Cluster<GroupClusterItem>? = null
    }
}