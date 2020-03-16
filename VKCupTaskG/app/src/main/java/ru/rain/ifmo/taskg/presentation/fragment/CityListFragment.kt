package ru.rain.ifmo.taskg.presentation.fragment

import android.animation.ObjectAnimator
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.animation.addListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import ru.rain.ifmo.taskg.R
import ru.rain.ifmo.taskg.data.models.VKCity
import ru.rain.ifmo.taskg.presentation.activity.ShopsListActivity
import java.util.*

class CityListFragment : Fragment() {

    private lateinit var mainView: View

    private lateinit var cityList: List<VKCity>

    private lateinit var picked: VKCity

    private lateinit var recyclerView: RecyclerView

    private lateinit var adapter: CityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val (cityList, picked) = extractBundle(arguments as Bundle)
        this.cityList = cityList
        this.picked = picked
        adapter = CityAdapter(cityList)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainView = inflater.inflate(R.layout.city_list_fragment, container, false)
        initViews()
        mainView.post { animateIn() }
        return mainView
    }

    private fun animateIn() {
        (activity as ShopsListActivity).animateIn()
    }

    private fun initViews() {
        mainView.findViewById<ImageButton>(R.id.close).setOnClickListener {
            closeFragment()
        }
        recyclerView = mainView.findViewById(R.id.city_list)
        recyclerView.adapter = adapter
    }

    private fun closeFragment() {
        (activity as ShopsListActivity).closeCityList(picked)
    }

    fun dismissAnimator(): ObjectAnimator {
        return ObjectAnimator.ofFloat(mainView, "translationY", mainView.height.toFloat())
    }

    fun inAnimator(): ObjectAnimator {
        val rect = Rect()
        mainView.getLocalVisibleRect(rect)
        val result = ObjectAnimator.ofFloat(mainView, "y", rect.bottom.toFloat(), rect.top.toFloat())
        result.addListener({},{
            mainView.visibility = View.VISIBLE
        })
        return result
    }

    private inner class CityViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        init {
            view.setOnClickListener {
                ShopsListActivity.log("city id=${city.id} name=${city.title} picked")
                val removed = adapter.cityList.indexOf(picked)
                picked = city
                val new = adapter.cityList.indexOf(picked)
                adapter.notifyItemChanged(removed)
                adapter.notifyItemChanged(new)
            }
        }

        private var city = VKCity()

        private val isPickedIcon = view.findViewById<ImageView>(R.id.is_picked)

        private val cityName = view.findViewById<TextView>(R.id.city_name)

        fun bind(city: VKCity, isPicked: Boolean) {
            this.city = city
            isPickedIcon.visibility = if (isPicked) View.VISIBLE else View.INVISIBLE
            cityName.text = city.title
        }
    }

    private inner class CityAdapter(
        val cityList: List<VKCity>
    ) : RecyclerView.Adapter<CityViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
            return CityViewHolder(
                layoutInflater.inflate(R.layout.city_item, parent, false)
            )
        }

        override fun getItemCount() = cityList.size

        override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
            val city = cityList[position]
            holder.bind(city, city.id == picked.id)
        }

    }

    companion object {

        fun createBundle(list: List<VKCity>, picked: VKCity): Bundle {
            return Bundle().apply {
                putParcelableArrayList(CITY_LIST, list as ArrayList<VKCity>)
                putParcelable(PICKED_CITY, picked)
            }
        }

        fun extractBundle(bundle: Bundle): Pair<List<VKCity>, VKCity> {
            val list = bundle.getParcelableArrayList<VKCity>(CITY_LIST) as List<VKCity>
            val picked = bundle.getParcelable<VKCity>(PICKED_CITY) as VKCity
            return list to picked
        }

        private const val CITY_LIST = "vk.com.city"

        private const val PICKED_CITY = "vk.com.picked"
    }
}