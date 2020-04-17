package com.beone.todoapp.util

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter


@BindingAdapter("setTaskColor")
fun ImageView.setTaskColor(color: Int) {
    setImageResource(colorArray[color])
}
@BindingAdapter("visibility")
fun ImageView.setVisibility(isGone: Boolean) {
    visibility = if (isGone) {
        View.GONE
    } else {
        View.VISIBLE
    }
}








