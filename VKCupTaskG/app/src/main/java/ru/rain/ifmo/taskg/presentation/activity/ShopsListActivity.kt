package ru.rain.ifmo.taskg.presentation.activity

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.animation.addListener
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.rain.ifmo.taskg.App
import ru.rain.ifmo.taskg.R
import ru.rain.ifmo.taskg.data.models.VKCity
import ru.rain.ifmo.taskg.data.models.VKGroup
import ru.rain.ifmo.taskg.presentation.fragment.CityListFragment
import ru.rain.ifmo.taskg.presentation.presenter.ShopsListPresenter
import ru.rain.ifmo.taskg.presentation.view.ShopsListView

class ShopsListActivity : AppCompatActivity(), ShopsListView {

    private val openFragmentClickListener = View.OnClickListener {
        presenter.chooseCity()
    }

    private val adapter = GroupAdapter()

    private val presenter: ShopsListPresenter by lazy { App.delegatePresenter(this) as ShopsListPresenter }

    private lateinit var toolbar: Toolbar

    private lateinit var toolbarTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
    }

    override fun onStop() {
        super.onStop()
        presenter.detach()
    }

    override fun updateToolbarTitle(city: String) {
        runOnUiThread { toolbarTitle.text = getString(R.string.shops_template, city) }
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title)
        toolbarTitle.setOnClickListener(openFragmentClickListener)
        findViewById<ImageButton>(R.id.dropdown_icon).setOnClickListener(openFragmentClickListener)
        recycler_view.adapter = adapter
    }

    override fun showCities(cityList: List<VKCity>, picked: VKCity) {
        val fragment = CityListFragment()
        fragment.arguments = CityListFragment.createBundle(cityList, picked)
        toolbar.visibility = View.GONE
        recycler_view.visibility = View.GONE
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, fragment)
            .commit()
    }

    override fun showGroups(list: List<VKGroup>, erasePrev: Boolean) {
        if (erasePrev) {
            log("Erase previous groups")
            adapter.data.clear()
            adapter.data.addAll(list)
            adapter.notifyDataSetChanged()
        } else {
            log("Filter groups")
            val size = adapter.data.size
            adapter.data.addAll(list)
            adapter.notifyItemRangeInserted(size, list.size)
        }
    }

    fun closeCityList(picked: VKCity) {
        presenter.picked(picked)
        toolbar.visibility = View.VISIBLE
        recycler_view.visibility = View.VISIBLE
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        fragment ?: return
        fragment as CityListFragment
        val dismissAnimator = fragment.dismissAnimator()
        val dimAnimator = dimAnimator()
        val set = AnimatorSet()
        set.playTogether(dismissAnimator, dimAnimator)
        set.addListener ({
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
        })
        set.duration = ANIMATION_DURATION
        set.interpolator = AccelerateInterpolator()
        set.start()
    }

    private fun dimAnimator(reverse: Boolean = false): ValueAnimator {
        val values = intArrayOf(0x7E, 0x00)
        if (reverse)
            values.reverse()
        val animator = ValueAnimator.ofInt(*values)
        animator.addUpdateListener {
            val value = it.animatedValue as Int
            root_view.background = ColorDrawable(value shl 24)
        }
        return animator
    }

    fun animateIn() {
        val dim = dimAnimator(true)
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as CityListFragment
        val anim = fragment.inAnimator()
        val set = AnimatorSet()
        set.playTogether(anim, dim)
        set.duration = ANIMATION_DURATION
        set.interpolator = AccelerateInterpolator()
        set.start()
    }

    private inner class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        init {
            itemView.setOnClickListener {
                openMarket(group)
            }
        }

        private var group = VKGroup()

        private val groupStatuses = resources.getStringArray(R.array.group_status)

        private val groupName = view.findViewById<TextView>(R.id.group_name)

        private val groupAvatar = view.findViewById<ImageView>(R.id.group_avatar)

        private val groupStatus = view.findViewById<TextView>(R.id.group_status)

        fun bind(group: VKGroup) {
            this.group = group
            groupStatus.text = groupStatuses[group.isClosed]
            groupName.text = group.name
            Glide.with(this@ShopsListActivity)
                .load(group.photo50)
                .circleCrop()
                .placeholder(R.drawable.no_avarar_50)
                .into(groupAvatar)
        }
    }

    private fun openMarket(group: VKGroup) {
        log("open market group id=${group.id}")
        startActivity(
            GoodsActivity.createIntent(this, group)
        )
    }

    private inner class GroupAdapter(): RecyclerView.Adapter<GroupViewHolder>() {

        val data = arrayListOf<VKGroup>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
            return GroupViewHolder(
                layoutInflater.inflate(R.layout.group_item, parent, false)
            )
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
            holder.bind(data[position])
            val size = data.size
            log("pos=$position size=$size")
            if (position == size - 1) {
                presenter.requestGroups(offset = size)
            }
        }

    }

    companion object {

        fun createIntent(packageContext: Context) = Intent(packageContext, ShopsListActivity::class.java)

        fun log(message: String) {
            Log.d("TASK_G", message)
        }

        fun log(e: Exception) {
            Log.d("TASK_G", "Exception", e)
        }

        private const val ANIMATION_DURATION = 500L
    }
}
