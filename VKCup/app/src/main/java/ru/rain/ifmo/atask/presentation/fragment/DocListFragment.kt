package ru.rain.ifmo.atask.presentation.fragment

import android.content.Context
import android.content.DialogInterface
import android.hardware.input.InputManager
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.text.TextPaint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.rain.ifmo.atask.App
import ru.rain.ifmo.atask.R
import ru.rain.ifmo.atask.data.models.VKDoc
import ru.rain.ifmo.atask.data.models.VKDocType
import ru.rain.ifmo.atask.presentation.DocListPresenter
import ru.rain.ifmo.atask.presentation.DocListView
import ru.rain.ifmo.atask.presentation.activity.AuthActivity
import ru.rain.ifmo.atask.toFormattedBytes
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DocListFragment : Fragment(), DocListView {

    private lateinit var endings: Array<String>

    private lateinit var selectItems: Array<String>

    private val adapter = DocAdapter()

    private val todayCalendar = Calendar.getInstance()
    private var calculatedDayCalendar = Calendar.getInstance()
    private val fullDateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val curYearFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())

    private val presenter: DocListPresenter by lazy { App.delegatePresenter(this) as DocListPresenter }

    private lateinit var mainView: View

    private lateinit var docList: RecyclerView

    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initViews(inflater, container)
        endings = resources.getStringArray(R.array.byte_endings)
        selectItems = resources.getStringArray(R.array.cases_list)
        return mainView
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
    }

    override fun onStop() {
        super.onStop()
        presenter.detach()
    }

    private fun initViews(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) {
        mainView = inflater.inflate(R.layout.doc_list_fragment, container, false)
        docList = mainView.findViewById(R.id.doc_list)
        docList.adapter = adapter
        progressBar = mainView.findViewById(R.id.progress_load)
        progressBar.visibility = View.VISIBLE
    }

    override fun addDocs(list: List<VKDoc>) {
        val size = adapter.data.size
        adapter.data.addAll(list)
        adapter.notifyItemRangeChanged(size, list.size)
    }

    override fun showSpinner() {
        progressBar.visibility = View.VISIBLE
        docList.visibility = View.GONE
    }

    override fun hideSpinner() {
        progressBar.visibility = View.GONE
        docList.visibility = View.VISIBLE
    }

    override fun removeDoc(index: Int) {
        if (adapter.data.size > index) {
            adapter.data.removeAt(index)
            adapter.notifyItemRemoved(index)
        }
    }

    override fun scrollTo(chosenIndex: Int) {
        docList.scrollToPosition(chosenIndex)
    }

    fun rename(doc: VKDoc, newName: String) {
        presenter.rename(doc, newName)
    }

    private fun docSelected(doc: VKDoc) {
        if (doc.id == 0)
            return
        val index = adapter.data.indexOf(doc)
        presenter.chosenDoc(index)
        (activity as AuthActivity).docSelected(doc)
    }

    private inner class DocViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val previewImage =
            view.findViewById<ImageView>(R.id.image_preview).apply {
                clipToOutline = true
            }

        private var doc = VKDoc()

        private val title = view.findViewById<TextView>(R.id.item_title)

        private val textContainer = view.findViewById<LinearLayout>(R.id.text_container)

        private val additionalInfo = view.findViewById<TextView>(R.id.additional_info)

        private val tags = view.findViewById<TextView>(R.id.doc_tags)

        private val tagsContainer = view.findViewById<View>(R.id.tags_container)

        private val moreBtn = view.findViewById<ImageView>(R.id.btn_more).apply {
            setOnClickListener { popupWindow.show() }
        }

        private val popupWindow: ListPopupWindow = ListPopupWindow(view.context)

        init {
            itemView.setOnClickListener { docSelected(doc) }
            initPopupWindow()
        }

        private fun initPopupWindow() {
            val data = ArrayList<HashMap<String, Any>>()
            var map = hashMapOf<String, Any>("TEXT" to selectItems[0])
            data.add(map)
            map = hashMapOf("TEXT" to selectItems[1])
            data.add(map)
            val adapter = SimpleAdapter(
                requireContext(),
                data,
                R.layout.drop_down_item,
                arrayOf("TEXT"),
                intArrayOf(R.id.spinner_item)
            )
            popupWindow.anchorView = moreBtn
            popupWindow.width = resources.getDimensionPixelSize(R.dimen.popup_width)
            popupWindow.setAdapter(adapter)
            popupWindow.setOnItemClickListener { _, _, position, _ ->
                when (position) {
                    0 -> {
                        val editText = EditText(requireActivity()).apply {
                            maxLines = 1
                            val filter =
                                InputFilter.LengthFilter(resources.getInteger(R.integer.max_chars))
                            filters = arrayOf(filter)
                            inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                            val text = doc.title.substringBeforeLast('.')
                            setText(text)
                            setSelection(text.length)
                        }
                        AlertDialog.Builder(requireActivity())
                            .setTitle(R.string.renaming)
                            .setMessage(R.string.renaming_msg)
                            .setView(editText)
                            .setPositiveButton(R.string.renaming) { _, _ ->
                                val newName = editText.text.toString()
                                if (doc.title != newName) {
                                    rename(doc, newName)
                                    title.text = newName
                                }
                                hideKeyBoard(editText)
                            }
                            .setNegativeButton(android.R.string.cancel) { _, _ ->
                                hideKeyBoard(editText)
                            }
                            .show()
                    }
                    1 -> {
                        presenter.delete(doc)
                    }
                }
                popupWindow.dismiss()
            }
        }

        private fun hideKeyBoard(editText: EditText) {
            val im = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(editText.windowToken, 0)
        }

        fun bind(doc: VKDoc) {
            if (doc.tags.isEmpty()) {
                title.isSingleLine = false
                tagsContainer.visibility = View.GONE
            } else {
                title.isSingleLine = true
                tagsContainer.visibility = View.VISIBLE
                tags.text = doc.tags.joinToString(separator = ",")
            }
            this.doc = doc
            cutOff()
            if (doc.preview != null && doc.preview.previewS.isNotBlank()) {
                Glide.with(this@DocListFragment)
                    .load(doc.preview.previewS)
                    .centerCrop()
                    .placeholder(R.drawable.ic_placeholder_document_other_72)
                    .into(previewImage)
            } else {
                val resId = when (doc.type) {
                    VKDocType.TEXT -> R.drawable.ic_placeholder_document_text_72
                    VKDocType.ARCHIVE -> R.drawable.ic_placeholder_document_archive_72
                    VKDocType.VIDEO -> R.drawable.ic_placeholder_document_video_72
                    VKDocType.AUDIO -> R.drawable.ic_placeholder_document_music_72
                    VKDocType.E_BOOK -> R.drawable.ic_placeholder_document_book_72
                    else -> R.drawable.ic_placeholder_document_other_72
                }
                Glide.with(this@DocListFragment)
                    .load(resId)
                    .fitCenter()
                    .into(previewImage)
            }
            additionalInfo.text = getString(
                R.string.additional_info_template,
                doc.ext.toUpperCase(Locale.getDefault()),
                doc.size.toFormattedBytes(endings),
                format(Date((doc.date).toLong() * 1000))
            )
        }

        private fun cutOff() {
            textContainer.post {
                val withoutExt = doc.title.substringBeforeLast('.')
                val result = placeThreeDots(withoutExt, textContainer.width, title.paint)
                title.text = result
            }
        }

        private fun format(date: Date): String {
            calculatedDayCalendar.time = date
            return if (calculatedDayCalendar[Calendar.YEAR] == todayCalendar[Calendar.YEAR]) {
                if (calculatedDayCalendar[Calendar.DAY_OF_YEAR] + 1 == todayCalendar[Calendar.DAY_OF_YEAR]) {
                    getString(R.string.yesterday)
                } else
                    curYearFormatter.format(date).toLowerCase(Locale.getDefault())
            } else {
                fullDateFormatter.format(date).toLowerCase(Locale.getDefault())
            }
        }
    }

    private fun placeThreeDots(
        withoutExt: String,
        width: Int,
        paint: TextPaint
    ): String {
        val strWidth = FloatArray(withoutExt.length)
        val dotsWidthValues = FloatArray(3)
        paint.getTextWidths(withoutExt, strWidth)
        if (strWidth.sum().toInt() <= width)
            return withoutExt
        paint.getTextWidths("...", dotsWidthValues)
        val dotsWidth = dotsWidthValues.sum()
        val dynamicWidth = FloatArray(withoutExt.length)
        var saveIndex = 0
        strWidth.forEachIndexed { index, fl ->
            if (index == 0)
                dynamicWidth[index] = fl
            else
                dynamicWidth[index] = dynamicWidth[index - 1] + fl
            if ((dynamicWidth[index] + dotsWidth).toInt() < width) {
                saveIndex = index
            } else {
                return@forEachIndexed
            }
        }
        return "${withoutExt.substring(0, saveIndex)}..."
    }

    private inner class DocAdapter : RecyclerView.Adapter<DocViewHolder>() {

        val data = ArrayList<VKDoc>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocViewHolder {
            val view = layoutInflater.inflate(R.layout.doc_list_item, parent, false)
            return DocViewHolder(view)
        }

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: DocViewHolder, position: Int) {
            holder.bind(data[position])
            val size = data.size
            if (position == size - 1) {
                presenter.requestDocs(offset = size)
            }
        }

    }
}