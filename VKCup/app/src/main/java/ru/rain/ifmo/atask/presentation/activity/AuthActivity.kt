package ru.rain.ifmo.atask.presentation.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKTokenExpiredHandler
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import ru.rain.ifmo.atask.R
import ru.rain.ifmo.atask.data.models.VKDoc
import ru.rain.ifmo.atask.presentation.fragment.ContentFragment
import ru.rain.ifmo.atask.presentation.fragment.DocListFragment

class AuthActivity : AppCompatActivity() {
    companion object {
        var token: VKAccessToken? = null
    }

    private val tokenTracker = object : VKTokenExpiredHandler {
        override fun onTokenExpired() {
            token = null
            tryLogin()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        supportActionBar?.setTitle(R.string.doc_title)
    }

    override fun onResume() {
        super.onResume()
        if (token == null) {
            tryLogin()
        }
        else
            if (supportFragmentManager.findFragmentById(R.id.container) == null)
                supportFragmentManager.beginTransaction()
                    .add(R.id.container, DocListFragment())
                    .commit()
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
        val callback = object : VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                AuthActivity.token = token
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

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.container)
        fragment ?: super.onBackPressed()
        if (fragment is ContentFragment) {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.scale_in, R.anim.scale_out)
                .replace(R.id.container, DocListFragment())
                .commit()
        } else
            super.onBackPressed()
    }

    private fun tryLogin() {
        VK.login(this, listOf(VKScope.DOCS))
    }

    fun docSelected(doc: VKDoc) {
        val fragment = ContentFragment()
        fragment.arguments = ContentFragment.createBundle(doc)
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.scale_in, 0)
            .replace(R.id.container, fragment)
            .commit()
    }
}
