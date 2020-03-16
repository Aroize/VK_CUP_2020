package ru.rain.ifmo.dtask.presentation.fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import ru.rain.ifmo.dtask.R

abstract class ShakingFragment : Fragment() {
    abstract var toolbar: Toolbar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.cancel_btn).setOnClickListener {
            editModeOff()
            onEditModeOff()
        }
    }

    fun editModeOn() {
        toolbar.apply {
            findViewById<View>(R.id.cancel_btn).visibility = View.VISIBLE
            findViewById<View>(R.id.add_btn).visibility = View.GONE
            findViewById<View>(R.id.edit_btn).visibility = View.GONE
            findViewById<View>(R.id.back_btn).visibility = View.GONE
        }
    }

    fun editModeOff() {
        toolbar.apply {
            findViewById<View>(R.id.cancel_btn).visibility = View.GONE
            findViewById<View>(R.id.add_btn).visibility = View.VISIBLE
            findViewById<View>(R.id.edit_btn).visibility = View.VISIBLE
        }
    }

    abstract fun onEditModeOff()
}