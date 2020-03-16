package ru.rain.ifmo.ftask.presentation.activity

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import androidx.core.view.children
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKTokenExpiredHandler
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import kotlinx.android.synthetic.main.auth_activity.*
import ru.rain.ifmo.ftask.R
import ru.rain.ifmo.ftask.data.models.VKGroup
import ru.rain.ifmo.ftask.presentation.fragment.ExtraGroupInfoFragment
import ru.rain.ifmo.ftask.presentation.fragment.GroupsListFragment
import kotlin.math.min

class AuthActivity : AppCompatActivity() {

    private val tokenHandler = object : VKTokenExpiredHandler {
        override fun onTokenExpired() {
            tryLogin()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.auth_activity)
        supportActionBar?.hide()
    }

    private fun tryLogin() {
        VK.login(this, listOf(VKScope.GROUPS, VKScope.WALL))
    }

    override fun onResume() {
        super.onResume()
        if (token == null) {
            tryLogin()
        } else {
            openListFragment()
        }
    }

    override fun onStart() {
        super.onStart()
        VK.addTokenExpiredHandler(tokenHandler)
    }

    override fun onStop() {
        super.onStop()
        VK.removeTokenExpiredHandler(tokenHandler)
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
        if (data == null || !VK.onActivityResult(requestCode, resultCode, data, callback))
            super.onActivityResult(requestCode, resultCode, data)
    }

    private fun openListFragment() {
        if (supportFragmentManager.findFragmentByTag(LIST_FRAGMENT_TAG) == null)
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, GroupsListFragment(), LIST_FRAGMENT_TAG)
                .commit()
    }

    fun openExtraInfo(group: VKGroup) {
        extra_container.setOnClickListener { }
        val fragment = ExtraGroupInfoFragment()
        fragment.arguments = ExtraGroupInfoFragment.createBundle(group)
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.enter_from_bottom, 0)
            .add(R.id.extra_container, fragment, EXTRA_GROUP_INFO)
            .commit()
        createDimAnimator(true).start()
    }

    fun closeInfo() {
        extra_container.isClickable = false
        val fragment = supportFragmentManager.findFragmentByTag(EXTRA_GROUP_INFO)
        fragment ?: return
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(0, R.anim.exit_to_bottom)
            .remove(fragment)
            .commit()
        createDimAnimator(false).start()
    }

    private fun createDimAnimator(`in`: Boolean = false): ValueAnimator {
        val values = if (!`in`) intArrayOf(0x7E, 0x00) else intArrayOf(0x00, 0x7E)
        val animator = ValueAnimator.ofInt(*values)
        animator.addUpdateListener {
            val value = it.animatedValue as Int shl 24
            fragment_container.foreground = ColorDrawable(value)
        }
        animator.duration = resources.getInteger(R.integer.anim_duration).toLong()
        return animator
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentByTag(EXTRA_GROUP_INFO)
        fragment ?: super.onBackPressed()
        closeInfo()
    }

    companion object {
        var token: VKAccessToken? = null

        const val LIST_FRAGMENT_TAG = "group.list.fragment.tag"

        const val EXTRA_GROUP_INFO = "info.group"
    }
}