package com.beone.todoapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_list_table")
data class Task(
    @PrimaryKey(autoGenerate = true)
    var taskId: Long = 0L,

    @ColumnInfo(name = "taskStartTime")
    val taskStartTime: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "taskContent")
    var taskContent: String = "",

    @ColumnInfo(name = "taskColor")
    var taskColor: Int = 0,

    @ColumnInfo(name = "taskTitle")
    var taskTitle: String = ""
)