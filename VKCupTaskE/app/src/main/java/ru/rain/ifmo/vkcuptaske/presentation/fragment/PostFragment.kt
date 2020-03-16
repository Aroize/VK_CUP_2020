package ru.rain.ifmo.vkcuptaske.presentation.fragment

import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.fragment.app.Fragment
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import ru.rain.ifmo.vkcuptaske.R
import ru.rain.ifmo.vkcuptaske.data.requests.VKWallPostCommand
import ru.rain.ifmo.vkcuptaske.presentation.activity.MainActivity
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.min

class PostFragment : Fragment() {

    private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        handleOnGlobalLayout()
    }

    private lateinit var photoUri: Uri

    private lateinit var mainView: View

    private lateinit var photoToShare: ImageView

    private lateinit var message: EditText

    private lateinit var send: Button

    private lateinit var progressBar: ProgressBar

    private var savedKeyboardHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        photoUri = extractBundle(arguments as Bundle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainView = inflater.inflate(R.layout.post_fragment, container, false)
        mainView.findViewById<ImageButton>(R.id.dismiss_fragment).setOnClickListener {
            mainView.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
            if (savedKeyboardHeight != 0) {
                val im =
                    activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                im.hideSoftInputFromWindow(mainView.windowToken, 0)
                keyboardAnimation(0)
                mainView.postDelayed({
                    (activity as MainActivity).animateDimOut(animateTranslation(false))
                }, KEYBOARD_DURATION)
            } else
                (activity as MainActivity).animateDimOut(animateTranslation(false))
        }
        initView()
        return mainView
    }

    override fun onStart() {
        super.onStart()
        mainView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

    override fun onStop() {
        super.onStop()
        mainView.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
    }

    @Suppress("deprecation")
    private fun initView() {
        (mainView as ViewGroup).layoutTransition.enableTransitionType(
            LayoutTransition.APPEARING
        )
        send = mainView.findViewById<Button>(R.id.send_btn)
        send.setOnClickListener {
            sendPost()
        }
        progressBar = mainView.findViewById(R.id.progress_bar)
        message = mainView.findViewById(R.id.post_message)
        photoToShare = mainView.findViewById(R.id.photo_to_share)
        var bitmap =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(activity!!.contentResolver, photoUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(activity!!.contentResolver, photoUri)
            }
        photoToShare.post {
            val display = activity!!.windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val width =
                size.x - photoToShare.marginStart - photoToShare.marginEnd
            val height = (width.toFloat() / 2).toInt()
            photoToShare.layoutParams.width = width
            photoToShare.layoutParams.height = height
            photoToShare.requestLayout()
            bitmap = resizeBitmap(bitmap, width to height)
            val bitmapDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
            bitmapDrawable.cornerRadius = resources.getDimensionPixelSize(R.dimen.corners).toFloat()
            photoToShare.setImageDrawable(bitmapDrawable)
            mainView.visibility = View.VISIBLE
            val animator = animateTranslation(true, height)
            (activity as MainActivity).animateDimIn(animator)
        }
    }

    private fun sendPost() {
        val fileUri = resolvePhotoUri()
        VK.execute(VKWallPostCommand(
            message = message.text.toString(),
            photos = listOf(fileUri)
        ), object : VKApiCallback<Int> {
            override fun fail(error: Exception) {
                MainActivity.log(error.toString())
                Log.e("TASK_E", "exception", error)
                hideLoad()
            }
            override fun success(result: Int) {
                MainActivity.log("success=$result")
                val file = File(context!!.filesDir, "image.jpeg")
                if (file.exists())
                    file.delete()
                hideLoad()
                mainView.findViewById<ImageButton>(R.id.dismiss_fragment).performClick()
            }
        })
        showLoad()
    }

    private fun showLoad() {
        send.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    private fun hideLoad() {
        send.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }

    private fun resolvePhotoUri(): Uri {
        val file = File(context!!.filesDir, "image.jpeg")
        if (file.exists())
            file.delete()
        val inputStream = context!!.contentResolver.openInputStream(photoUri)
        with(inputStream) {
            this as InputStream
            with(FileOutputStream(file)) {
                val readBytes = readBytes()
                write(readBytes)
            }
        }
        return Uri.fromFile(file)
    }

    private fun animateTranslation(`in`: Boolean, height: Int = 0): ObjectAnimator {
        val values = floatArrayOf(mainView.y - height, mainView.y + mainView.height + height)
        if (`in`)
            values.reverse()
        val animator = ObjectAnimator.ofFloat(mainView, "y", *values)
        animator.duration = ANIMATION_DURATION
        return animator
    }

    private fun handleOnGlobalLayout() {
        val point = Point()
        activity!!.windowManager.defaultDisplay.getSize(point)
        val rect = Rect()
        mainView.getWindowVisibleDisplayFrame(rect)
        val keyboardHeight = point.y - rect.bottom
        MainActivity.log("keyboard height=$keyboardHeight")
        if (keyboardHeight > 0 && savedKeyboardHeight == 0) {
            savedKeyboardHeight = keyboardHeight
            keyboardAnimation(-savedKeyboardHeight)
        } else if (keyboardHeight == 0) {
            //Wtf android
            keyboardAnimation(0)
            savedKeyboardHeight = 0
        }
    }

    private fun keyboardAnimation(height: Int) {
        val animator = ObjectAnimator.ofFloat(mainView, "translationY", height.toFloat())
        animator.duration = KEYBOARD_DURATION
        animator.interpolator = LinearInterpolator()
        animator.start()
    }

    private fun resizeBitmap(src: Bitmap, containerSize: Pair<Int, Int>): Bitmap {
        val srcSize = src.width to src.height
        val scale = min(
            containerSize.first / srcSize.first.toFloat(),
            containerSize.second / srcSize.second.toFloat()
        )
        return Bitmap.createScaledBitmap(
            src,
            (scale * srcSize.first).toInt(),
            (scale * srcSize.second).toInt(),
            false
        )
    }

    companion object {
        fun createBundle(uri: Uri): Bundle {
            return Bundle().apply {
                putString(PHOTO_URI, uri.toString())
            }
        }

        fun extractBundle(bundle: Bundle): Uri {
            return Uri.parse(bundle.getString(PHOTO_URI))
        }

        private const val PHOTO_URI = "picked.photo.vk"

        const val ANIMATION_DURATION = 500L

        const val KEYBOARD_DURATION = 200L
    }
}

