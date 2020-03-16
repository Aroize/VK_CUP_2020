package ru.rain.ifmo.ftask

import android.content.res.Resources
import java.text.SimpleDateFormat
import java.util.*

fun Int.convertPrefix(): String {
    return when(this) {
        in 0..999 -> this.toString()
        in 1000..999_999 -> "%.1fK".format(this.toDouble() / 1000)
        else -> "%.1fM".format(this.toDouble() / 1_000_000)
    }
}

fun Long.toVKInfo(resources: Resources): String {
    if (this == 0L)
        return resources.getString(R.string.no_posts)
    val date = Date(this)
    val otherCalendar = Calendar.getInstance(Locale.getDefault())
    otherCalendar.time = date
    val today = Date(System.currentTimeMillis())
    val todayCalendar = Calendar.getInstance(Locale.getDefault())
    todayCalendar.time = today
    if (todayCalendar[Calendar.YEAR] != otherCalendar[Calendar.YEAR]) {
        val formatter = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        return resources.getString(
            R.string.last_post,
            formatter.format(date).toLowerCase(Locale.getDefault())
        )
    } else {
        val currentDayOfYear = todayCalendar[Calendar.DAY_OF_YEAR]
        val otherDayOfYear = otherCalendar[Calendar.DAY_OF_YEAR]
        if (currentDayOfYear == otherDayOfYear)
            return resources.getString(R.string.today)
        if (currentDayOfYear - 1 == otherDayOfYear)
            return resources.getString(R.string.yesterday)
    }
    val formatter = SimpleDateFormat("d MMMM", Locale.getDefault())
    return resources.getString(
        R.string.last_post,
        formatter.format(date).toLowerCase(Locale.getDefault())
    )
}