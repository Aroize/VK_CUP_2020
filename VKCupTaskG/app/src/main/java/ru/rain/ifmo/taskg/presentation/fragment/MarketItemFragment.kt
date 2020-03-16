package ru.rain.ifmo.taskg.presentation.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.rain.ifmo.taskg.App
import ru.rain.ifmo.taskg.R
import ru.rain.ifmo.taskg.data.models.VKMarketItem
import ru.rain.ifmo.taskg.data.requests.VKFavoriteProductCommand
import ru.rain.ifmo.taskg.presentation.activity.GoodsActivity
import ru.rain.ifmo.taskg.presentation.activity.ShopsListActivity

class MarketItemFragment : Fragment() {

    private lateinit var marketItem: VKMarketItem

    private lateinit var mainView: View

    private lateinit var favouriteBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        marketItem = extractBundle(arguments as Bundle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainView = inflater.inflate(R.layout.market_fragment, container, false)
        initViews()
        return mainView
    }

    private fun initViews() {
        mainView.findViewById<TextView>(R.id.item_title).text =
            marketItem.title
        mainView.findViewById<TextView>(R.id.item_price).text =
            marketItem.price.text
        mainView.findViewById<TextView>(R.id.item_description).text =
            marketItem.description
        mainView.findViewById<ViewPager>(R.id.photos).adapter = PhotoAdapter()
        favouriteBtn = mainView.findViewById(R.id.favourite_btn)
        favouriteBtn.setOnClickListener {
            marketItem.isFavorite = !marketItem.isFavorite
            (activity as GoodsActivity).itemMarked(marketItem.id, marketItem.isFavorite)
            updateBtnStyle()
            sendChangeFavourite()
        }
        updateBtnStyle()
    }

    private fun sendChangeFavourite() {
        ShopsListActivity.log("marketItem id=${marketItem.id} owner=${marketItem.ownerId}")
        VK.execute(
            VKFavoriteProductCommand(
            id = marketItem.id,
            ownerId = marketItem.ownerId,
            isFavorite = marketItem.isFavorite
        ), object : VKApiCallback<Int> {
            override fun fail(error: Exception) {
                ShopsListActivity.log(error)
            }

            override fun success(result: Int) {
                ShopsListActivity.log("changed status result=$result")
            }
        })
    }

    private fun updateBtnStyle() {
        if (marketItem.isFavorite) {
            favouriteBtn.setTextColor(
                ContextCompat.getColor(
                    activity as Context,
                    R.color.colorAccent
                )
            )
            favouriteBtn.backgroundTintList =
                ContextCompat.getColorStateList(activity as Context, R.color.colorBtnTint)
            favouriteBtn.setText(R.string.is_favourite)
        } else {
            favouriteBtn.setTextColor(Color.WHITE)
            favouriteBtn.backgroundTintList =
                ContextCompat.getColorStateList(activity as Context, R.color.colorAccent)
            favouriteBtn.setText(R.string.not_favourite)
        }
    }

    private inner class PhotoAdapter : PagerAdapter() {
        override fun getCount() = marketItem.photos.size

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view =
                layoutInflater.inflate(R.layout.viewpager_item, container, false) as ImageView
            Glide.with(this@MarketItemFragment)
                .load(marketItem.photos[position].url)
                .fitCenter()
                .into(view)
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
    }

    companion object {
        fun createBundle(marketItem: VKMarketItem) = Bundle().apply {
            putParcelable(KEY_MARKET, marketItem)
        }

        private fun extractBundle(bundle: Bundle): VKMarketItem {
            return bundle.getParcelable<VKMarketItem>(KEY_MARKET) as VKMarketItem
        }

        private const val KEY_MARKET = "vk.com.market.item"
    }
}