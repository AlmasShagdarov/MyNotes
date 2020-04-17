package com.beone.todoapp.task

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.beone.todoapp.R


class ListExampleAdapter(context: Context, private val list: Array<Int>) : BaseAdapter() {
    private val inflator: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val spinView = inflator.inflate(R.layout.spinner_item, null)
        val imageView = spinView.findViewById(R.id.img_color) as ImageView
        imageView.setImageResource(list[position])
        return spinView
    }
}
