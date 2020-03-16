package ru.rain.ifmo.dtask.presentation.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.rain.ifmo.dtask.R
import ru.rain.ifmo.dtask.data.models.VKAlbum
import ru.rain.ifmo.dtask.presentation.fragment.AlbumContentFragment
import ru.rain.ifmo.dtask.presentation.fragment.AlbumListFragment
import ru.rain.ifmo.dtask.presentation.fragment.PagerFragment

class AlbumActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.album_activity)
    }

    override fun onResume() {
        super.onResume()
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        fragment ?: openAlbumList()
    }

    fun openAlbumList() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right)
            .replace(R.id.fragment_container, AlbumListFragment())
            .commit()
    }

    fun openAlbum(album: VKAlbum) {
        val fragment = AlbumContentFragment()
        fragment.arguments = AlbumContentFragment.createBundle(album)
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onBackPressed() {
        when (val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)) {
            is AlbumContentFragment -> { fragment.backPressed() }
            is AlbumListFragment -> {
                if (!fragment.backPressed())
                    super.onBackPressed()
            }
            is PagerFragment -> fragment.backPressed()
            else -> super.onBackPressed()
        }
    }

    fun openExpanded(album: VKAlbum, index: Int) {
        val fragment = PagerFragment()
        fragment.arguments = PagerFragment.createBundle(index, album)
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right)
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    companion object {
        fun createIntent(`package`: Context) = Intent(`package`, AlbumActivity::class.java)
    }
}