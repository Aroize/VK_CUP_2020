package ru.rain.ifmo.dtask.domain.recycler

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import ru.rain.ifmo.dtask.R

abstract class ShakingViewHolder<T>(
    private val shakingListener: ShakingListener<T>,
    view: View
) : RecyclerView.ViewHolder(view) {

    abstract var item: T

    protected val shakingItem: View = view.findViewById<View>(R.id.shaking_view)

    protected val deleteBtn: View = view.findViewById<View>(R.id.delete_btn)

    init {

        deleteBtn.setOnClickListener {
            shakingListener.delete(item)
        }

        shakingItem.setOnClickListener {
            shakingListener.onClick(item)
        }
        shakingItem.setOnLongClickListener {
            shakingListener.onLongClick()
            true
        }
    }

    open fun startShaking() {
        val animation = AnimationUtils.loadAnimation(shakingItem.context, R.anim.shaking)
        animation.repeatCount = Animation.INFINITE
        shakingItem.startAnimation(animation)
        deleteBtn.visibility = View.VISIBLE
        shakingItem.isEnabled = false
    }

    open fun stopShaking() {
        shakingItem.isEnabled = true
        shakingItem.clearAnimation()
        deleteBtn.visibility = View.INVISIBLE
    }
}