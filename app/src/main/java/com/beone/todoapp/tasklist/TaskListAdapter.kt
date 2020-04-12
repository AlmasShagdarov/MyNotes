package com.beone.todoapp.tasklist

import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beone.todoapp.R
import com.beone.todoapp.database.Task
import com.beone.todoapp.databinding.ListItemTaskBinding


class TaskAdapter(private val itemClick: OnItemClick) :
    ListAdapter<Task, TaskAdapter.ViewHolder>(TaskDiffCallback()) {

    private val selectedItems = SparseBooleanArray()
    private var adapterList: List<Task>? = null
    private var isSelectedAll = false

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, itemClick, position, selectedItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun submitList(list: List<Task>?) {
        super.submitList(list)
        adapterList = list
    }

    fun getItemTask(position: Int): Task {
        return getItem(position)
    }

    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun toggleSelection(position: Int) {
        when (selectedItems.get(position, false)) {
            true -> selectedItems.delete(position)
            false -> selectedItems.put(position, true)
        }
        notifyItemChanged(position)
    }

    fun setSelectionAll(){
        adapterList?.let {
            for (i in it.indices) {
                when (isSelectedAll) {
                    true -> selectedItems.delete(i)
                    false -> selectedItems.put(i, true)
                }
            }
        }
        isSelectedAll = !isSelectedAll
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<Int>? {
        val items: MutableList<Int> = ArrayList(selectedItems.size())
        for (i in 0 until selectedItems.size()) {
            items.add(selectedItems.keyAt(i))
        }
        return items
    }

    fun selectedItemCount(): Int {
        return selectedItems.size()
    }

    class ViewHolder private constructor(private val binding: ListItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemTaskBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }


        fun bind(
            item: Task,
            itemClick: OnItemClick,
            position: Int,
            selectedItems: SparseBooleanArray
        ) {
            binding.task = item
            binding.txtContent.transitionName = "transition$position"
            binding.noteColor.transitionName = "color$position"
            binding.item.setOnLongClickListener {
                itemClick.onLongPress(it, item, position)
                true
            }
            binding.item.setOnClickListener {
                itemClick.onItemClick(it, item, position)
            }
            toggleItemBackground(binding, position, selectedItems)
            binding.executePendingBindings()

        }

        private fun toggleItemBackground(
            binding: ListItemTaskBinding,
            position: Int,
            selectedItems: SparseBooleanArray
        ) {
            val selectedColor = when (selectedItems.get(position, false)) {
                true -> ContextCompat.getColor(binding.item.context, R.color.light_grey)
                false -> ContextCompat.getColor(binding.item.context, R.color.white)
            }
            binding.card.setCardBackgroundColor(selectedColor)
        }
    }
}

class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.taskId == newItem.taskId
    }

    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.taskTitle == newItem.taskTitle && oldItem.taskColor == newItem.taskColor
    }

}


interface OnItemClick {
    fun onItemClick(view: View?, task: Task, position: Int)
    fun onLongPress(view: View?, task: Task, position: Int)
}

