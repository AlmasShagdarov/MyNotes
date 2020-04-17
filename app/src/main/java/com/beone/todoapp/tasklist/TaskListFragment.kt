package com.beone.todoapp.tasklist

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.beone.todoapp.R
import com.beone.todoapp.database.Task
import com.beone.todoapp.databinding.FragmentTaskListBinding
import com.beone.todoapp.util.hideKeyboard
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.list_item_task.view.*


class TaskListFragment : Fragment() {

    private lateinit var binding: FragmentTaskListBinding
    private lateinit var adapter: TaskAdapter
    private lateinit var actionCallback: ActionCallback
    private var actionMode: ActionMode? = null
    private lateinit var searchView: SearchView
    private val taskListViewModel: TaskListViewModel by viewModels {
        TaskListViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentTaskListBinding.inflate(inflater, container, false)
        actionCallback = ActionCallback()
        val itemClick = initItemClick()
        adapter = TaskAdapter(itemClick)
        initBinding()
        setupSearchView()
        observeNavigationNewTask()
        observeTasks()
        return binding.root
    }

    private fun initItemClick(): OnItemClick {
        return object : OnItemClick {
            override fun onItemClick(view: View?, task: Task, position: Int) {
                when (actionMode != null) {
                    true -> toggleActionBar(position)
                    false -> {
                        searchView.onActionViewCollapsed()
                        hideKeyboard()
                        navigateToTask(task, view!!)
                    }
                }
            }

            override fun onLongPress(view: View?, task: Task, position: Int) {
                toggleActionBar(position)
            }
        }
    }

    private fun initBinding() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            recyclerview.adapter = adapter
            addButton.setOnClickListener {
                taskListViewModel.createNewTask()
            }
            recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0 && binding.addButton.visibility == View.VISIBLE) {
                        binding.addButton.hide()
                    } else if (dy < 0 && binding.addButton.visibility != View.VISIBLE)
                        binding.addButton.show()
                }
            })
        }

    }

    private fun observeNavigationNewTask() {
        taskListViewModel.navigateToNewTask.observe(viewLifecycleOwner, Observer { id ->
            id?.let {
                val action = TaskListFragmentDirections.actionTaskListFragmentToTaskFragment(
                    it,
                    getString(R.string.new_note)
                )
                findNavController().navigate(action)
                taskListViewModel.navigateToNewTask()
            }
        })
    }

    private fun setupSearchView() {
        val searchItem = binding.toolbar.menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView
        with(searchView) {
            queryHint = requireActivity().getString(R.string.search)

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    searchView.onActionViewCollapsed()
                    return true
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    taskListViewModel.filterText.value = "%$query%"
                    return false
                }
            })
        }
    }

    private fun toggleActionBar(position: Int) {
        if (actionMode == null) {
            actionMode = requireActivity().startActionMode(actionCallback)
        }
        toggleSelection(position)
    }

    private fun toggleSelection(position: Int) {
        adapter.toggleSelection(position)
        val count: Int = adapter.selectedItemCount()
        actionMode!!.title = count.toString()
        actionMode!!.invalidate()

    }

    private fun toggleSelectionAll() {
        adapter.setSelectionAll()
        val count: Int = adapter.selectedItemCount()
        actionMode!!.title = count.toString()
        actionMode!!.invalidate()
    }

    private fun observeTasks() {
        taskListViewModel.tasks.observe(viewLifecycleOwner, Observer {
            binding.listIsEmpty = !it.isNullOrEmpty()
            adapter.setAdapterList(it)
            adapter.submitList(it)
            clearSelectedItems()
        })
    }

    private fun clearSelectedItems() {
        adapter.clearSelection()
        actionMode?.let {
            it.title = adapter.selectedItemCount().toString()
            it.invalidate()
        }
    }

    private fun navigateToTask(task: Task, view: View) {
        val extras = FragmentNavigatorExtras(
            view.txt_content to "title",
            view.noteColor to "color",
            binding.addButton to "fab"

        )

        val action = TaskListFragmentDirections.actionTaskListFragmentToTaskFragment(
            task.taskId,
            task.taskTitle
        )
        findNavController().navigate(action, extras)
    }

    private fun deleteSelectedTasks() {
        val selectedItemPositions = adapter.getSelectedItems()
        val deleteList = arrayListOf<Long>()
        for (i in selectedItemPositions!!.indices.reversed()) {
            val position = selectedItemPositions[i]
            val taskId = adapter.getItemTask(position).taskId
            deleteList.add(taskId)
        }
        taskListViewModel.onDeleteSelectedTasks(deleteList)
        adapter.notifyDataSetChanged()
    }

    inner class ActionCallback : ActionMode.Callback {


        override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
            mode.menuInflater.inflate(R.menu.action_mode_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.deleteItems -> showAlertDialog(mode)
                R.id.selectAll -> toggleSelectionAll()
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            adapter.clearSelection()
            actionMode = null
        }
    }

    private fun showAlertDialog(mode: ActionMode) {
        val dialog = MaterialAlertDialogBuilder(requireActivity())
            .setTitle(getString(R.string.delete_selected_tasks))
            .setMessage(getString(R.string.are_you_sure))
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
                mode.finish()
            }
            .setPositiveButton(getString(R.string.confirm)) { dialog, _ ->
                deleteSelectedTasks()
                dialog.dismiss()
                mode.finish()
                showSnackBar()
            }.create()

        dialog.setOnShowListener {
            val positiveColor = ContextCompat.getColor(requireActivity(), R.color.green)
            val negativeColor = ContextCompat.getColor(requireActivity(), R.color.red)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(positiveColor)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(negativeColor)
        }
        dialog.show()
        searchView.onActionViewCollapsed()
    }

    private fun showSnackBar() {
        Snackbar.make(
            binding.root,
            getString(R.string.tasks_successfully_deleted),
            Snackbar.LENGTH_SHORT
        )
            .show()
    }
}
