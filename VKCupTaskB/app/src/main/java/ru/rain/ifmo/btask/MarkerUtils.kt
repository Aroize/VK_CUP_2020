package ru.rain.ifmo.btask

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import org.json.JSONArray

fun createCustomMarker(context: Context, marker: View): Bitmap {
    val metrics = DisplayMetrics()
    (context as Activity).windowManager.defaultDisplay.getMetrics(metrics)
    marker.layoutParams = ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT)
    marker.measure(metrics.widthPixels, metrics.heightPixels)
    marker.layout(0, 0, metrics.widthPixels, metrics.heightPixels)
    marker.setLayerType(View.LAYER_TYPE_HARDWARE, null)
    val bitmap = Bitmap.createBitmap(
        marker.measuredWidth,
        marker.measuredHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    marker.draw(canvas)
    return bitmap
}

inline fun JSONArray.forEach(func: (Any) -> Unit) {
    forEachIndexed { it, _ ->
        func(it)
    }
}

inline fun JSONArray.forEachIndexed(func: (Any, Int) -> Unit) {
    for (i in 0 until this.length()) {
        func(this[i], i)
    }
}