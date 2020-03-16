package ru.rain.ifmo.dtask.domain.recycler

import androidx.recyclerview.widget.RecyclerView

abstract class ShakingAdapter<T> : RecyclerView.Adapter<ShakingViewHolder<T>>() {

    var isShaking: Boolean = false
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ShakingViewHolder<T>, position: Int) {
        if (isShaking)
            holder.startShaking()
        else
            holder.stopShaking()
    }
}