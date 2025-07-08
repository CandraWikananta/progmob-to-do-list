package com.progmob.todolist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class TaskModel(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val priority: String,
    val dueDate: String,
    val dueTime: String
)

class TaskAdapter(
    private val taskList: List<TaskModel>,
    private val onItemClick: (TaskModel) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskTitleText: TextView = itemView.findViewById(R.id.taskTitleText)
        val taskCategoryText: TextView = itemView.findViewById(R.id.taskCategoryText)
        val taskDateText: TextView = itemView.findViewById(R.id.taskDateText)
        val taskPriorityText: TextView = itemView.findViewById(R.id.taskPriorityText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false) // file XML item layout
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.taskTitleText.text = task.title
        holder.taskCategoryText.text = task.category
        holder.taskDateText.text = "${task.dueDate} | ${task.dueTime}"
        holder.taskPriorityText.text = task.priority

        // Handle click to open detail
        holder.itemView.setOnClickListener {
            onItemClick(task)
        }
    }

    override fun getItemCount(): Int = taskList.size
}
