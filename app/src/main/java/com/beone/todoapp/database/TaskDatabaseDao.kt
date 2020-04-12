package com.beone.todoapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDatabaseDao {

    @Insert
    fun insertTask(task: Task)

    @Update
    fun updateTask(task: Task)

    @Query("DELETE FROM task_list_table")
    suspend fun deleteAllTask()

    @Query("DELETE FROM task_list_table WHERE taskId = :key")
    fun deleteTaskById(key: Long)

    @Query("SELECT * FROM task_list_table ORDER BY taskId DESC")
    fun getAllTask(): LiveData<List<Task>>

    @Query("SELECT * FROM task_list_table ORDER BY taskId DESC LIMIT 1")
    suspend fun getLastTask(): Task

    @Query("SELECT taskContent FROM task_list_table WHERE taskId = :key")
    fun getTaskContentById(key: Long): LiveData<String>

    @Query("SELECT * FROM task_list_table WHERE taskId = :key")
    fun getTaskById(key: Long): LiveData<Task>

    @Query("delete from task_list_table where taskId in (:idList)")
    fun deleteSelectedTasksById(idList: List<Long>)

    @Query("SELECT * FROM task_list_table WHERE taskTitle LIKE :search")
    fun getTasksByTitle(search: String?): LiveData<List<Task>>
}