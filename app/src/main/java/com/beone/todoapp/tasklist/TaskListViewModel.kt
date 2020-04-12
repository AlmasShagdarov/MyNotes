package com.beone.todoapp.tasklist

import android.app.Application
import androidx.lifecycle.*
import com.beone.todoapp.database.Task
import com.beone.todoapp.database.TaskDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskListViewModel(
    app: Application
) : AndroidViewModel(app) {

    private val database = TaskDatabase.getDatabase(app).taskDatabaseDao
    private val _navigateToNewTask = MutableLiveData<Long?>()
    val navigateToNewTask: LiveData<Long?>
        get() = _navigateToNewTask
    val filterText = MutableLiveData("")
    val tasks = Transformations.switchMap(filterText) {
        if (it == null || it.isEmpty())
            database.getAllTask()
        else database.getTasksByTitle(filterText.value)
    }


    fun navigateToNewTask() {
        _navigateToNewTask.value = null
    }

    fun createNewTask() {
        viewModelScope.launch {
            val task = Task()
            insert(task)
            _navigateToNewTask.value = getTaskId()
        }
    }

    private suspend fun getTaskId() = withContext(Dispatchers.IO) { database.getLastTask().taskId }

    private suspend fun insert(task: Task) = withContext(Dispatchers.IO) {
        database.insertTask(task)

    }

    fun onDeleteSelectedTasks(idList: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            database.deleteSelectedTasksById(idList)
        }
    }
}


