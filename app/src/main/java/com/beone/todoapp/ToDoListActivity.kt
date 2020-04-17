package com.beone.todoapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ToDoListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo_list)

    }
}

