package com.progmob.todolist.model

data class TaskModel(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val priority: String,
    val dueDate: String,
    val dueTime: String
)
