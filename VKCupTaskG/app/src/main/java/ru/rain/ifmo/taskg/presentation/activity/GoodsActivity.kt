package ru.rain.ifmo.taskg.presentation.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.SparseBooleanArray
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.util.set
import ru.rain.ifmo.taskg.R
import ru.rain.ifmo.taskg.data.models.VKGroup
import ru.rain.ifmo.taskg.data.models.VKMarketItem
import ru.rain.ifmo.taskg.presentation.fragment.GoodsListFragment
import ru.rain.ifmo.taskg.presentation.fragment.MarketItemFragment

class GoodsActivity : AppCompatActivity(){

    private lateinit var group: VKGroup

    private lateinit var toolBar: Toolbar

    private lateinit var toolbarTitle: TextView

    private val markedMap = SparseBooleanArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.goods_activity)
        initViews()
        group = extractIntent(intent)
    }

    private fun initViews() {
        toolBar = findViewById(R.id.toolbar)
        toolbarTitle = findViewById(R.id.toolbar_title)
        findViewById<ImageButton>(R.id.back_btn).setOnClickListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            fragment ?: onBackPressed()
            when (fragment) {
                is GoodsListFragment -> onBackPressed()
                is MarketItemFragment -> openListFragment()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        fragment ?: openListFragment()
    }

    private fun openListFragment() {
        val fragment = GoodsListFragment()
        fragment.arguments = GoodsListFragment.createBundle(group, markedMap)
        markedMap.clear()
        toolbarTitle.text = getString(
            R.string.group_goods,
            group.name
        )
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.enter_from_left,
                R.anim.exit_to_right
            )
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun openMarketItem(marketItem: VKMarketItem) {
        ShopsListActivity.log(marketItem.toString())
        val fragment = MarketItemFragment()
        fragment.arguments = MarketItemFragment.createBundle(marketItem)
        toolbarTitle.text = marketItem.title
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left
            )
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun itemMarked(id: Int, favourite: Boolean) {
        markedMap[id] = favourite
    }

    companion object {
        fun createIntent(packageContext: Context, group: VKGroup) =
            Intent(packageContext, GoodsActivity::class.java).apply {
                putExtra(EXTRA_GROUP, group)
            }

        private fun extractIntent(intent: Intent) =
            intent.getParcelableExtra<VKGroup>(EXTRA_GROUP)

        private const val EXTRA_GROUP = "vk.group.extra"

    }
}