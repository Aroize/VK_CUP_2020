package ru.rain.ifmo.ftask.domain

import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs

abstract class AppBarStateChangeListener : AppBarLayout.OnOffsetChangedListener {

    enum class AppBarLayoutState {
        COLLAPSED,
        EXPANDED,
        IDLE
    }

    private var currentState = AppBarLayoutState.IDLE

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        appBarLayout ?: return
        when {
            abs(verticalOffset) < appBarLayout.totalScrollRange -> {
                if (currentState != AppBarLayoutState.EXPANDED) {
                    onStateChanged(appBarLayout, AppBarLayoutState.EXPANDED)
                }
                currentState = AppBarLayoutState.EXPANDED
            }
            abs(verticalOffset) >= appBarLayout.totalScrollRange -> {
                if (currentState != AppBarLayoutState.COLLAPSED) {
                    onStateChanged(appBarLayout, AppBarLayoutState.COLLAPSED)
                }
                currentState = AppBarLayoutState.COLLAPSED
            }
            else -> {
                if (currentState != AppBarLayoutState.IDLE) {
                    onStateChanged(appBarLayout, AppBarLayoutState.IDLE)
                }
                currentState = AppBarLayoutState.IDLE
            }
        }
    }

    abstract fun onStateChanged(appBarLayout: AppBarLayout, state: AppBarLayoutState)
}