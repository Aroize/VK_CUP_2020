package ru.rain.ifmo.atask.presentation.fragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ru.rain.ifmo.atask.R
import ru.rain.ifmo.atask.data.models.VKDoc
import ru.rain.ifmo.atask.data.models.VKDocType
import java.util.*

class ContentFragment : Fragment() {

    private lateinit var doc: VKDoc

    private lateinit var mainView: View

    private lateinit var webView: WebView

    private lateinit var downloadContainer: View

    private lateinit var downLoadBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        doc = extractBundle(arguments)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("ContentTag", "onCreateView timestamp=${System.currentTimeMillis()}")
        initViews(inflater, container)
        resolveContent()
        return mainView
    }


    private fun resolveContent() {
        when (doc.type) {
            VKDocType.TEXT, VKDocType.IMAGE, VKDocType.GIF -> {
                //Work with WebView
                if (doc.ext == "pdf" || doc.ext == "txt") {
                    downloadContainer.visibility = View.VISIBLE
                    return
                }
                setupWebView()
                webView.visibility = View.VISIBLE
                val formatted = doc.url.replace(Regex("&no_preview=1"), "")
                webView.loadUrl(
                    formatted
                )
            }
            VKDocType.VIDEO, VKDocType.AUDIO -> {
                setupWebView()
                webView.visibility = View.VISIBLE
                showContent()
            }
            else -> {
                //Suggest to download this doc
                downloadContainer.visibility = View.VISIBLE
            }
        }
    }

    private fun showContent() {
        val pageStream = resources.assets.open("page.html")
        val scanner = Scanner(pageStream)
        val stringBuffer = StringBuffer()
        while (scanner.hasNextLine()) {
            stringBuffer.append(scanner.nextLine())
        }
        val data = stringBuffer.toString().format(doc.url)
        webView.loadData(data, "text/html", "utf-8")
    }

    private fun initViews(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) {
        mainView = inflater.inflate(R.layout.content_fragment, container, false)
        webView = mainView.findViewById(R.id.webView_content)
        downloadContainer = mainView.findViewById(R.id.download_view)
        downLoadBtn = mainView.findViewById(R.id.download_btn)
        downLoadBtn.setOnClickListener { download() }
    }

    private fun download() {
        if (ContextCompat.checkSelfPermission(
                context as Context,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity as Activity, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION
            )
        } else
            applyDownload()
    }

    private fun applyDownload() {
        val dm = context!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(doc.url))
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, doc.title)
        dm.enqueue(request)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION) {
            if (permissions.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                applyDownload()
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        webView.settings.javaScriptEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.isVerticalScrollBarEnabled = true
        webView.isHorizontalScrollBarEnabled = true
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
    }

    override fun onDestroy() {
        super.onDestroy()
        (mainView as ViewGroup).removeAllViews()
        webView.clearHistory()
        webView.clearCache(true)
        webView.loadUrl("about:blank")
        webView.onPause()
        webView.pauseTimers()
        webView.destroy()
    }

    companion object {
        fun createBundle(doc: VKDoc): Bundle {
            val bundle = Bundle()
            bundle.putParcelable(DOC_KEY, doc)
            return bundle
        }

        fun extractBundle(bundle: Bundle?): VKDoc {
            return bundle!!.getParcelable<VKDoc>(DOC_KEY) as VKDoc
        }

        private const val DOC_KEY = "vk.doc.key"

        private const val REQUEST_PERMISSION = 101
    }
}