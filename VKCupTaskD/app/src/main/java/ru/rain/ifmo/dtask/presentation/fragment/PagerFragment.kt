package ru.rain.ifmo.dtask.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import ru.rain.ifmo.dtask.App
import ru.rain.ifmo.dtask.R
import ru.rain.ifmo.dtask.data.models.VKAlbum
import ru.rain.ifmo.dtask.data.models.VKPhoto
import ru.rain.ifmo.dtask.presentation.activity.AlbumActivity
import ru.rain.ifmo.dtask.presentation.presenter.AlbumContentPresenter
import ru.rain.ifmo.dtask.presentation.view.AlbumContentView

class PagerFragment : Fragment(), AlbumContentView {

    private val presenter: AlbumContentPresenter by lazy { App.delegatePresenter(this) as AlbumContentPresenter }

    private lateinit var album: VKAlbum
    private var index: Int = 0

    private var data = ArrayList<VKPhoto>()
    private val adapter = ImageAdapter()

    private lateinit var toolbarTitle: TextView
    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        extractBundle()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.pager_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarTitle = view.findViewById(R.id.toolbar_title)
        viewPager = view.findViewById(R.id.viewpager)
        viewPager.adapter = adapter
        view.findViewById<View>(R.id.back_btn).setOnClickListener {
            backPressed()
        }
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) = Unit
            override fun onPageScrolled(pos: Int, posOff: Float, posiOffPixels: Int) = Unit
            override fun onPageSelected(position: Int) {
                toolbarTitle.text = getString(R.string.from, position + 1, data.size)
                if (position == data.size - 1) {
                    presenter.requestPhotos(offset = data.size)
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
        data = presenter.data
        adapter.notifyDataSetChanged()
        viewPager.currentItem = index
        toolbarTitle.text = getString(R.string.from, index + 1, data.size)
    }

    override fun onStop() {
        super.onStop()
        presenter.detach()
    }

    override fun album() = album

    override fun dataChanged(newData: ArrayList<VKPhoto>) {
        data = newData
        adapter.notifyDataSetChanged()
    }

    override fun dataUpdated(oldSize: Int, size: Int) {
        adapter.notifyDataSetChanged()
    }

    private fun extractBundle() {
        arguments?.let {
            index = it.getInt(KEY_INDEX)
            album = it.getParcelable<VKAlbum>(KEY_ALBUM) as VKAlbum
        }
    }

    fun backPressed() {
        (requireActivity() as AlbumActivity).openAlbum(album)
    }

    private inner class ImageAdapter : PagerAdapter() {
        override fun getCount() = data.size

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = layoutInflater.inflate(R.layout.full_image_item, container, false)
            container.addView(view)
            App.log("orig=${data[position].orig}")
            Glide.with(this@PagerFragment)
                .load(data[position].orig)
                .into(view as ImageView)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
    }

    companion object {
        fun createBundle(index: Int, album: VKAlbum) = Bundle().apply {
            putInt(KEY_INDEX, index)
            putParcelable(KEY_ALBUM, album)
        }

        private const val KEY_INDEX = "key.index"
        private const val KEY_ALBUM = "key.album"
    }
}