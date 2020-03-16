package ru.rain.ifmo.ftask.presentation.fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.appbar.AppBarLayout
import ru.rain.ifmo.ftask.App
import ru.rain.ifmo.ftask.R
import ru.rain.ifmo.ftask.data.models.VKGroup
import ru.rain.ifmo.ftask.domain.AppBarStateChangeListener
import ru.rain.ifmo.ftask.domain.AppBarStateChangeListener.AppBarLayoutState.*
import ru.rain.ifmo.ftask.presentation.activity.AuthActivity
import ru.rain.ifmo.ftask.presentation.presenter.GroupsListPresenter
import ru.rain.ifmo.ftask.presentation.view.GroupsListView

class GroupsListFragment : Fragment(), GroupsListView {

    private val presenter by lazy { App.delegatePresenter(this) as GroupsListPresenter }

    private val adapter = GroupsAdapter()

    private lateinit var mainView: LinearLayout

    private lateinit var recyclerView: RecyclerView

    private lateinit var appBarLayout: AppBarLayout

    private lateinit var toolBar: Toolbar

    private lateinit var sizeBar: View

    private lateinit var countOfGroups: TextView

    private lateinit var progressBar: ProgressBar

    private lateinit var coordinatorLayout: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainView = inflater.inflate(R.layout.fragment_groups_list, container, false) as LinearLayout
        initViews()
        return mainView
    }

    private fun initViews() {
        appBarLayout = mainView.findViewById(R.id.appbar_layout)
        progressBar = mainView.findViewById(R.id.progress_bar)
        coordinatorLayout = mainView.findViewById(R.id.coordinator_layout)
        toolBar = mainView.findViewById(R.id.toolbar)
        appBarLayout.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout, state: AppBarLayoutState) {
                when (state) {
                    COLLAPSED -> {
                        toolBar.visibility = View.VISIBLE
                    }
                    EXPANDED -> {
                        toolBar.visibility = View.INVISIBLE
                    }
                    IDLE -> {
                    }
                }
            }
        })
        recyclerView = mainView.findViewById(R.id.recycler_view)
        recyclerView.adapter = adapter
        (recyclerView.layoutManager as GridLayoutManager).also {
            it.spanCount = 3
        }
        sizeBar = mainView.findViewById(R.id.size_view)
        sizeBar.setOnClickListener {
            presenter.unsubscribe()
        }
        countOfGroups = sizeBar.findViewById(R.id.count_of_groups)
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
    }

    override fun onStop() {
        super.onStop()
        presenter.detach()
    }

    override fun addGroups(list: List<VKGroup>) {
        val size = adapter.data.size
        if(!adapter.data.containsAll(list)) {
            adapter.data.addAll(list)
            adapter.notifyItemRangeInserted(size, list.size)
        }
    }

    override fun hideSizeString() {
        sizeBar.visibility = View.GONE
    }

    override fun showLeaveSizeString(str: String) {
        if (sizeBar.visibility != View.VISIBLE)
            sizeBar.visibility = View.VISIBLE
        countOfGroups.text = str
    }

    private fun openInfo(group: VKGroup): Boolean {
        (activity as AuthActivity).openExtraInfo(group)
        return group.id != 0
    }

    override fun removeAll(collection: Collection<VKGroup>) {
        collection.forEach { vkGroup ->
            val index = adapter.data.indexOfFirst { it.id == vkGroup.id }
            if (index != -1) {
                adapter.data.removeAt(index)
                adapter.notifyItemRemoved(index)
            }
        }
    }

    override fun showLoading() {
        progressBar.visibility = View.VISIBLE
        hideSizeString()
        coordinatorLayout.visibility = View.GONE
    }

    override fun hideLoading() {
        progressBar.visibility = View.GONE
        coordinatorLayout.visibility = View.VISIBLE
    }

    private inner class GroupHolder(view: View) : RecyclerView.ViewHolder(view) {

        private var group = VKGroup()

        private val groupAvatar =
            itemView.findViewById<ImageButton>(R.id.group_avatar).also {
                it.setOnClickListener {
                    if (presenter.isPicked(group)) {
                        presenter.removeFromLeave(group)
                        setUnpicked()
                    } else {
                        presenter.addToLeave(group)
                        setPicked()
                    }
                }

                it.setOnLongClickListener {
                    openInfo(group)
                }
            }

        private val groupName = itemView.findViewById<TextView>(R.id.group_name)

        fun setPicked() {
            /*
            * I know, that in API >= 23 we can use width and height in layer-list
            * It is written for compatibility with API 21 and 22, but then this performance
            * I liked more, than using layer-list, but you can find drawable resource, which
            * You can use in API >= 23
            * Just replace code below in two sections: for newest api and old,
            * In new api block return :
            * ContextCompat.getDrawable(context as Context, R.drawable.rounded_group_m)
            * */
            val circleLayer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ContextCompat.getDrawable(context as Context, R.drawable.rounded_group_m)
            } else {
                val circle = ContextCompat.getDrawable(context as Context, R.drawable.rounded_group)
                val tick =
                    BitmapFactory.decodeResource(resources, R.drawable.ic_check_circle_28)
                val shared = Bitmap.createScaledBitmap(
                    tick,
                    resources.getDimensionPixelSize(R.dimen.tick_size),
                    resources.getDimensionPixelSize(R.dimen.tick_size),
                    false
                )
                val drawable = BitmapDrawable(resources, shared)
                val result = LayerDrawable(arrayOf(circle, drawable))
                result.setLayerInset(
                    1,
                    resources.getDimensionPixelSize(R.dimen.tick_inset),
                    resources.getDimensionPixelSize(R.dimen.tick_inset),
                    0,
                    0
                )
                result
            }

            prepareGlide()
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        resource as Drawable
                        groupAvatar.setImageDrawable(LayerDrawable(arrayOf(resource, circleLayer)))
                        return true
                    }
                })
                .into(groupAvatar)
            Log.d("CheckTag", "End setting picked ${group.id}")
        }

        fun setUnpicked() {
            Log.d("CheckTag", "Start setting unpicked ${group.id}")
            prepareGlide()
                .into(groupAvatar)
        }

        fun prepareGlide(): RequestBuilder<Drawable> {
            return Glide.with(this@GroupsListFragment)
                .load(
                    if (group.photo200.isBlank())
                        R.drawable.no_avatar
                    else
                        group.photo200
                )
                .placeholder(R.drawable.no_avatar)
                .override(resources.getDimensionPixelSize(R.dimen.avatar_size))
                .circleCrop()
        }

        fun bind(group: VKGroup) {
            this.group = group
            if (presenter.isPicked(group))
                setPicked()
            else
                setUnpicked()
            groupName.text = group.name
        }
    }

    private inner class GroupsAdapter : RecyclerView.Adapter<GroupHolder>() {

        val data = arrayListOf<VKGroup>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupHolder {
            return GroupHolder(
                layoutInflater.inflate(R.layout.group_list_item, parent, false)
            )
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: GroupHolder, position: Int) {
            holder.bind(data[position])
            val size = data.size
            if (position == size - 1) {
                presenter.requestGroups(offset = size)
            }
        }

    }
}