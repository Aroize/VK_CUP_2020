package ru.rain.ifmo.vkcuptaske.presentation.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKTokenExpiredHandler
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import kotlinx.android.synthetic.main.activity_main.*
import ru.rain.ifmo.vkcuptaske.R
import ru.rain.ifmo.vkcuptaske.presentation.fragment.PostFragment

class MainActivity : AppCompatActivity() {

    private val tokenTracker = object : VKTokenExpiredHandler {
        override fun onTokenExpired() {
            token = null
            tryLogin()
        }
    }

    private var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        pick_photo_btn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_PERMISSION
                )
            } else {
                pickPhoto()
            }
        }
    }

    private fun pickPhoto() {
        val mIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        if (packageManager.queryIntentActivities(mIntent, 0).isNotEmpty()) {
            startActivityForResult(mIntent, PICK_PHOTO)
        } else {
            Toast.makeText(this, "No picker on the phone", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (token == null) {
            tryLogin()
        } else {
            log("onResume() called with photoUri=$photoUri")
            if (photoUri != null &&
                supportFragmentManager.findFragmentById(R.id.fragment_container) == null
            ) {
                log("Starting new fragment")
                val fragment = PostFragment()
                fragment.arguments = PostFragment.createBundle(photoUri as Uri)
                supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        VK.addTokenExpiredHandler(tokenTracker)
    }

    override fun onStop() {
        super.onStop()
        VK.addTokenExpiredHandler(tokenTracker)
    }

    override fun onDestroy() {
        super.onDestroy()
        VK.logout()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        log("request code=$requestCode result code=$resultCode")
        if (requestCode == PICK_PHOTO && data != null) {
            photoUri = data.data
            log("New content=$photoUri")
        } else {
            val callback = object : VKAuthCallback {
                override fun onLogin(token: VKAccessToken) {
                    MainActivity.token = token
                }

                override fun onLoginFailed(errorCode: Int) {
                    val builder = AlertDialog.Builder(this@MainActivity)
                    when (errorCode) {
                        VKAuthCallback.AUTH_CANCELED -> {
                            builder.setMessage(R.string.rejected_login)
                        }
                        VKAuthCallback.UNKNOWN_ERROR -> {
                            builder.setMessage(R.string.unknown_error)
                        }
                    }
                    val dialog = builder.create()
                    dialog.setOnDismissListener { tryLogin() }
                    dialog.show()
                }
            }
            if (data == null || !VK.onActivityResult(requestCode, resultCode, data, callback)) {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION) {
            if (permissions.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                pickPhoto()
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun tryLogin() {
        VK.login(this, listOf(VKScope.WALL, VKScope.PHOTOS))
    }

    fun animateDimIn(animator: ObjectAnimator) {
        val dimAnimator = dimAnimator(true)
        val set = AnimatorSet()
        set.interpolator = LinearInterpolator()
        set.playTogether(animator, dimAnimator)
        set.start()
    }

    private fun dimAnimator(`in`: Boolean): ValueAnimator {
        val values = intArrayOf(0x00, 0x7E)
        if (!`in`)
            values.reverse()
        val animator = ValueAnimator.ofInt(*values)
        animator.addUpdateListener {
            val dim = it.animatedValue as Int shl 24
            main_view.foreground = ColorDrawable(dim)
        }
        return animator
    }

    fun animateDimOut(animator: ObjectAnimator) {
        val dimAnimator = dimAnimator(false)
        val set = AnimatorSet()
        set.interpolator = LinearInterpolator()
        set.playTogether(animator, dimAnimator)
        set.addListener ({
            supportFragmentManager.beginTransaction()
                .remove(
                    supportFragmentManager.findFragmentById(R.id.fragment_container)!!
                )
                .commit()
        })
        set.start()
    }

    companion object {
        var token: VKAccessToken? = null

        private const val REQUEST_PERMISSION = 101

        private const val PICK_PHOTO = 202

        fun log(message: String) {
            Log.d("TASK_E", message)
        }
    }
}
