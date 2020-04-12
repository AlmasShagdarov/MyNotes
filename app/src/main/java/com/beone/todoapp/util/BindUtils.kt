package com.beone.todoapp.util

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.beone.todoapp.database.Task


@BindingAdapter("setTaskString")
fun TextView.setTaskString(item: Task?) {
    item?.let {
        text = item.taskContent.formatHTMLtoEditable().toString()
    }
}

@BindingAdapter("setTaskDate")
fun TextView.setTaskDate(item: Task?) {
    item?.let {
        text = item.taskStartTime.convertLongToDateString()
    }
}

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








