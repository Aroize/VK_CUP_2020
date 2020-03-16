package ru.rain.ifmo.btask.presentation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import com.vk.api.sdk.utils.VKUtils
import ru.rain.ifmo.btask.App
import ru.rain.ifmo.btask.R

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        supportActionBar?.hide()
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
    }

    override fun onResume() {
        super.onResume()
        if (App.token == null) {
            tryLogin()
        } else {
            val intent = MapsActivity.createIntent(this)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(
                intent
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = object : VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                App.token = token
                val intent = MapsActivity.createIntent(this@AuthActivity)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(
                    intent
                )
            }

            override fun onLoginFailed(errorCode: Int) {
                val builder = AlertDialog.Builder(this@AuthActivity)
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

    private fun tryLogin() {
        VK.login(this, listOf(VKScope.PHOTOS, VKScope.GROUPS))
    }
}
