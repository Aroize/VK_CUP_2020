package ru.rain.ifmo.dtask.presentation.fragment

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.requests.VKRequest
import ru.rain.ifmo.dtask.App
import ru.rain.ifmo.dtask.R
import ru.rain.ifmo.dtask.data.models.VKAlbum
import ru.rain.ifmo.dtask.data.models.VKPhoto
import ru.rain.ifmo.dtask.data.requests.VKAddPhotoRequest
import ru.rain.ifmo.dtask.domain.recycler.ShakingAdapter
import ru.rain.ifmo.dtask.domain.recycler.ShakingListener
import ru.rain.ifmo.dtask.domain.recycler.ShakingViewHolder
import ru.rain.ifmo.dtask.presentation.activity.AlbumActivity
import ru.rain.ifmo.dtask.presentation.presenter.AlbumContentPresenter
import ru.rain.ifmo.dtask.presentation.view.AlbumContentView
import java.io.File
import java.io.FileOutputStream

class AlbumContentFragment : ShakingFragment(), ShakingListener<VKPhoto>, AlbumContentView {

    private val presenter: AlbumContentPresenter by lazy { App.delegatePresenter(this) as AlbumContentPresenter }

    private lateinit var data: ArrayList<VKPhoto>
    override lateinit var toolbar: Toolbar
    private lateinit var album: VKAlbum
    private lateinit var rootView: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var backBtn: AppCompatImageButton
    private lateinit var cancelBtn: AppCompatImageButton
    private val adapter = PhotoAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        album = extractBundle(arguments as Bundle)
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.album_content_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootView = view
        toolbar = view.findViewById(R.id.toolbar)
        backBtn = view.findViewById(R.id.back_btn)
        backBtn.setOnClickListener {
            (requireActivity() as AlbumActivity).openAlbumList()
        }
        cancelBtn = view.findViewById(R.id.cancel_btn)
        recyclerView = view.findViewById(R.id.recycler_view)
        view.findViewById<AppCompatImageButton>(R.id.add_btn).apply {
            if (album.id < 0)
                visibility = View.GONE
            else
                setOnClickListener {
                    pickPhoto()
                }
        }
        view.findViewById<View>(R.id.edit_btn).apply {
            if (album.id < -100)
                visibility = View.GONE
        }
        (recyclerView.layoutManager as GridLayoutManager).spanCount = 3
        recyclerView.adapter = adapter
        view.findViewById<TextView>(R.id.collapsing_title).text = album.title
        view.findViewById<TextView>(R.id.toolbar_title).text = ""
        view.findViewById<View>(R.id.edit_btn).setOnClickListener {
            onLongClick()
        }
        rootView.findViewById<View>(R.id.back_btn).apply {
            visibility = View.VISIBLE
            setOnClickListener {
                backPressed()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION) {
            if (permissions.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendIntent()
            }
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_PHOTO) {
            if (data?.data != null) {
                val photoUri = data.data!!
                uploadPhoto(photoUri)
            }
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadPhoto(photoUri: Uri) {
        val file = File(requireActivity().filesDir, "${System.currentTimeMillis()}.jpeg")
        val outputStream = FileOutputStream(file)
        val inputStream = requireActivity().contentResolver.openInputStream(photoUri)
        inputStream?.use { input ->
            outputStream.use { output ->
                output.write(input.readBytes())
            }
        }
        val uri = Uri.fromFile(file)
        VK.execute(VKAddPhotoRequest(album.id, uri), object : VKApiCallback<VKPhoto> {
            override fun fail(error: Exception) {
                App.log(error)
            }

            override fun success(result: VKPhoto) {
                App.log("photo is uploaded $result")
                data.add(result)
                adapter.notifyItemInserted(data.size - 1)
                file.delete()
            }
        })
    }

    private fun pickPhoto() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSION
            )
        } else {
            sendIntent()
        }
    }

    private fun sendIntent() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(intent, REQUEST_PHOTO)
        } else {
            Toast.makeText(requireActivity(), R.string.no_picker, Toast.LENGTH_LONG).show()
        }
    }

    fun backPressed() {
        if (adapter.isShaking) {
            adapter.isShaking = false
            editModeOff()
            rootView.findViewById<View>(R.id.back_btn).visibility = View.VISIBLE
        } else {
            (requireActivity() as AlbumActivity).openAlbumList()
        }
    }

    override fun onEditModeOff() {
        adapter.isShaking = false
        rootView.findViewById<View>(R.id.back_btn).visibility = View.VISIBLE
        if (album.id < 0)
            rootView.findViewById<View>(R.id.add_btn).visibility = View.GONE
    }

    override fun onClick(t: VKPhoto) {
        val index = data.indexOf(t)
        (requireActivity() as AlbumActivity).openExpanded(album, index)
    }

    override fun delete(t: VKPhoto) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm)
            .setMessage(R.string.are_you_sure_photo)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.yes) { _, _ ->
                val index = data.indexOf(t)
                data.removeAt(index)
                adapter.notifyItemRemoved(index)
                presenter.deletePhoto(t)
            }
            .show()
    }

    override fun onLongClick() {
        if (album.id > -100) {
            editModeOn()
            adapter.isShaking = true
        }
    }

    override fun album() = album

    override fun dataChanged(newData: ArrayList<VKPhoto>) {
        data = newData
        adapter.notifyDataSetChanged()
    }

    override fun dataUpdated(oldSize: Int, size: Int) {
        adapter.notifyItemRangeInserted(oldSize, size)
    }

    private inner class PhotoViewHolder(view: View) : ShakingViewHolder<VKPhoto>(this, view) {

        private val imageView = view.findViewById<AppCompatImageView>(R.id.shaking_view)

        override var item: VKPhoto = VKPhoto()
            set(value) {
                field = value
                Glide.with(this@AlbumContentFragment)
                    .load(value.preview)
                    .centerCrop()
                    .into(imageView)
            }
    }

    private inner class PhotoAdapter() : ShakingAdapter<VKPhoto>() {
        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: ShakingViewHolder<VKPhoto>, position: Int) {
            super.onBindViewHolder(holder, position)
            holder.item = data[position]
            val size = data.size
            if (position == size - 1) {
                presenter.requestPhotos(offset = size)
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ShakingViewHolder<VKPhoto> {
            return PhotoViewHolder(
                layoutInflater.inflate(R.layout.image_item, parent, false)
            )
        }
    }

    companion object {
        fun createBundle(album: VKAlbum) = Bundle().apply {
            putParcelable(KEY, album)
        }

        private fun extractBundle(bundle: Bundle) = bundle.getParcelable<VKAlbum>(KEY) as VKAlbum

        private const val KEY = "extra.album"

        private const val REQUEST_PERMISSION = 101

        private const val REQUEST_PHOTO = 202
    }
}