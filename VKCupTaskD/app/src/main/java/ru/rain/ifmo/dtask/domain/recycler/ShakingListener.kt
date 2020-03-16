package ru.rain.ifmo.dtask.domain.recycler

interface ShakingListener<T> {
    fun onLongClick()

    fun onClick(t: T)

    fun delete(t: T)
}