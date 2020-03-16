package ru.rain.ifmo.dtask.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.rain.ifmo.dtask.App
import ru.rain.ifmo.dtask.R
import ru.rain.ifmo.dtask.data.models.VKAlbum
import ru.rain.ifmo.dtask.domain.recycler.ShakingAdapter
import ru.rain.ifmo.dtask.domain.recycler.ShakingListener
import ru.rain.ifmo.dtask.domain.recycler.ShakingViewHolder
import ru.rain.ifmo.dtask.presentation.activity.AlbumActivity
import ru.rain.ifmo.dtask.presentation.presenter.AlbumListPresenter
import ru.rain.ifmo.dtask.presentation.view.AlbumListView

class AlbumListFragment : ShakingFragment(), ShakingListener<VKAlbum>, AlbumListView  {

    private val presenter: AlbumListPresenter by lazy { App.delegatePresenter(this) as AlbumListPresenter }

    override lateinit var toolbar: Toolbar

    private lateinit var recyclerView: RecyclerView

    private lateinit var data: ArrayList<VKAlbum>

    private val adapter = AlbumAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.album_list_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recycler_view)
        (recyclerView.layoutManager as GridLayoutManager).spanCount = 2
        recyclerView.adapter = adapter
        view.findViewById<View>(R.id.add_btn).setOnClickListener {
            createAlbum()
        }
        view.findViewById<View>(R.id.edit_btn).setOnClickListener {
            onLongClick()
        }
        toolbar = view.findViewById(R.id.toolbar)
    }

    private fun createAlbum() {
        val editText = EditText(requireActivity())
        AlertDialog.Builder(requireContext())
            .setView(editText)
            .setTitle(R.string.create_album)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.yes) { _, _ ->
                val text = editText.text.trim().toString()
                if (text.length > 1) {
                    presenter.createAlbum(text)
                } else {
                    Toast.makeText(requireActivity(), R.string.too_short, Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
        data = presenter.data
    }

    override fun onStop() {
        super.onStop()
        presenter.detach()
    }

    override fun onEditModeOff() {
        adapter.isShaking = false
    }

    override fun delete(t: VKAlbum) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm)
            .setMessage(R.string.are_you_sure_album)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.yes) { _, _ ->
                val index = data.indexOf(t)
                data.removeAt(index)
                adapter.notifyItemRemoved(index)
                presenter.deleteAlbum(t)
            }
            .show()
    }

    override fun onClick(t: VKAlbum) {
        presenter.picked(t)
        (requireActivity() as AlbumActivity).openAlbum(t)
    }

    override fun onLongClick() {
        editModeOn()
        adapter.isShaking = true
    }

    override fun updateData(oldSize: Int, size: Int) {
        adapter.notifyItemRangeInserted(oldSize, size)
    }

    override fun updateItem(index: Int) {
        adapter.notifyItemChanged(index)
    }

    fun backPressed(): Boolean {
        if (adapter.isShaking) {
            adapter.isShaking = false
            editModeOff()
            return true
        }
        return false
    }

    private inner class AlbumHolder(view: View) : ShakingViewHolder<VKAlbum>(this, view) {

        init {
            shakingItem.clipToOutline = true
        }

        private val title: TextView = view.findViewById(R.id.album_title)
        private val albumSize: TextView = view.findViewById(R.id.album_size)

        override var item: VKAlbum = VKAlbum()
        set(value) {
            field=value
            Glide.with(this@AlbumListFragment)
                .load(item.thumbSrc)
                .centerCrop()
                .placeholder(R.drawable.camera_400)
                .into(shakingItem as AppCompatImageView)
            title.text = value.title
            val size = when (val albumSizeInt = value.size) {
                in 0..999 -> {
                    val photos = resources.getQuantityString(R.plurals.photos, albumSizeInt)
                    "$albumSizeInt $photos"
                }
                else -> {
                    val photos = resources.getQuantityString(R.plurals.photos, albumSizeInt % 100)
                    "%.1f $photos".format((albumSizeInt).toDouble() / 1000)
                }
            }
            albumSize.text = size
        }

        override fun startShaking() {
            if (item.id < 0) {
                itemView.alpha = 0.5f
                shakingItem.isEnabled = false
            } else {
                super.startShaking()
            }
        }

        override fun stopShaking() {
            deleteBtn.visibility = View.INVISIBLE
            if (item.id < 0) {
                itemView.alpha = 1f
                shakingItem.isEnabled = true
            } else {
                super.stopShaking()
            }
        }
    }

    private inner class AlbumAdapter : ShakingAdapter<VKAlbum>() {
        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: ShakingViewHolder<VKAlbum>, position: Int) {
            holder.item = data[position]
            super.onBindViewHolder(holder, position)
            val size = data.size
            if (position == size - 1) {
                presenter.requestAlbums(offset = size)
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ShakingViewHolder<VKAlbum> {
            return AlbumHolder(
                layoutInflater.inflate(R.layout.album_item, parent, false)
            )
        }
    }
}