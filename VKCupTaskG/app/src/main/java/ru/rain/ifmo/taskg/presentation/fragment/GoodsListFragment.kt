package ru.rain.ifmo.taskg.presentation.fragment

import android.os.Bundle
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.util.forEach
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.rain.ifmo.taskg.App
import ru.rain.ifmo.taskg.R
import ru.rain.ifmo.taskg.data.models.VKGroup
import ru.rain.ifmo.taskg.data.models.VKMarketItem
import ru.rain.ifmo.taskg.presentation.activity.GoodsActivity
import ru.rain.ifmo.taskg.presentation.activity.ShopsListActivity
import ru.rain.ifmo.taskg.presentation.presenter.GoodsListPresenter
import ru.rain.ifmo.taskg.presentation.view.GoodsListView

class GoodsListFragment : Fragment(), GoodsListView {

    private val presenter by lazy { App.delegatePresenter(this) as GoodsListPresenter }

    private lateinit var recyclerView: RecyclerView

    private lateinit var group: VKGroup

    private val adapter = MarketItemAdapter()

    override fun onStop() {
        super.onStop()
        presenter.detach()
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        group = extractBundle(arguments as Bundle)
        ShopsListActivity.log("Extracted group=$group")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        recyclerView =
            inflater.inflate(R.layout.goods_list_fragment, container, false) as RecyclerView
        (recyclerView.layoutManager as GridLayoutManager).spanCount = 2
        recyclerView.adapter = adapter
        return recyclerView
    }

    private fun openMarketItem(marketItem: VKMarketItem) {
        if (marketItem.id > 0)
            (activity as GoodsActivity).openMarketItem(marketItem)
    }

    override fun getGroup() = group

    override fun showMarketItems(list: List<VKMarketItem>) {
        val size = adapter.data.size
        adapter.data.addAll(list)
        adapter.notifyItemRangeInserted(size, list.size)
    }

    override fun getChangedItems(): SparseBooleanArray {
        val result = markedMap!!.clone()
        markedMap = null
        return result
    }

    private inner class MarketItemHolder(view: View) : RecyclerView.ViewHolder(view) {

        init {
            itemView.setOnClickListener {
                openMarketItem(marketItem)
            }
        }

        private val itemPhoto = view.findViewById<ImageView>(R.id.item_photo).apply {
            clipToOutline = true
        }

        private val itemTitle = view.findViewById<TextView>(R.id.item_title)

        private val itemPrice = view.findViewById<TextView>(R.id.item_price)

        private var marketItem = VKMarketItem()

        fun bind(marketItem: VKMarketItem) {
            this.marketItem = marketItem
            Glide.with(this@GoodsListFragment)
                .load(marketItem.photo)
                .centerCrop()
                .into(itemPhoto)
            itemTitle.text = marketItem.title
            itemPrice.text = marketItem.price.text
        }
    }

    private inner class MarketItemAdapter(): RecyclerView.Adapter<MarketItemHolder>() {

        val data = ArrayList<VKMarketItem>()

        override fun getItemCount() = data.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketItemHolder {
            return MarketItemHolder(
                layoutInflater.inflate(R.layout.market_item, parent, false)
            )
        }

        override fun onBindViewHolder(holder: MarketItemHolder, position: Int) {
            holder.bind(data[position])
            val size = data.size
            if (position == size - 1) {
                presenter.requestMarketItems(offset = size)
            }
        }
    }

    companion object {
        fun createBundle(
            group: VKGroup,
            markedMap: SparseBooleanArray
        ) = Bundle().apply {
            putParcelable(KEY_GROUP, group)
            GoodsListFragment.markedMap = markedMap
        }

        private var markedMap: SparseBooleanArray? = null

        private fun extractBundle(bundle: Bundle): VKGroup =
            bundle.getParcelable<VKGroup>(KEY_GROUP) as VKGroup

        private const val KEY_GROUP = "vk.com.group"
    }
}