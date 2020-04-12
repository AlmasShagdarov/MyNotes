package com.beone.todoapp.task

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.beone.todoapp.database.Task
import com.beone.todoapp.database.TaskDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskViewModel(
    taskKey: Long,
    app: Application
) : AndroidViewModel(app) {

    val database = TaskDatabase.getDatabase(app).taskDatabaseDao

    private val _navigateToList = MutableLiveData(false)
    val navigateToList: LiveData<Boolean>
        get() = _navigateToList
    val task = database.getTaskById(taskKey)

    fun updateTask(text: String, textSpanned: String, color: Int) {
        viewModelScope.launch {
            task.value?.let {
                val title = text.split("\n")[0]
                it.taskContent = textSpanned
                it.taskTitle = title
                it.taskColor = color
                updateTask(it)
                _navigateToList.value = true
            }
        }
    }

    private suspend fun updateTask(task: Task) = withContext(Dispatchers.IO) {
        database.updateTask(task)
    }
}