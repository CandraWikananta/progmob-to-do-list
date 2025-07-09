package com.progmob.todolist

import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

// Data model
data class TaskModel(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val priority: String,
    val dueDate: String,
    val dueTime: String,
    val completed: Boolean
)

private fun formatDate(inputDate: String): String {
    return try {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEE, dd MMMM", Locale.getDefault())
        val date = inputFormat.parse(inputDate)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        inputDate
    }
}

class TaskAdapter(
    val taskList: MutableList<TaskModel>,
    private val onCheckedChanged: (TaskModel) -> Unit,
    private val onItemClick: (TaskModel) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskCheckbox: CheckBox = itemView.findViewById(R.id.taskCheckbox)
        val taskTitleText: TextView = itemView.findViewById(R.id.taskTitleText)
        val taskCategoryText: TextView = itemView.findViewById(R.id.taskCategoryText)
        val taskDateText: TextView = itemView.findViewById(R.id.taskDateText)
        val taskPriorityText: TextView = itemView.findViewById(R.id.taskPriorityText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]

        // Set text
        holder.taskTitleText.text = task.title
        holder.taskCategoryText.text = task.category
        holder.taskDateText.text = "${formatDate(task.dueDate)} | ${task.dueTime}"
        holder.taskPriorityText.text = task.priority

        holder.taskCheckbox.setOnCheckedChangeListener(null)
        holder.taskCheckbox.isChecked = task.completed

        fun updateStrikeThrough(view: TextView, isCompleted: Boolean) {
            view.paintFlags = if (isCompleted) {
                view.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                view.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }

        updateStrikeThrough(holder.taskTitleText, task.completed)
        updateStrikeThrough(holder.taskCategoryText, task.completed)
        updateStrikeThrough(holder.taskDateText, task.completed)
        updateStrikeThrough(holder.taskPriorityText, task.completed)

        // Checkbox logic
        holder.taskCheckbox.setOnCheckedChangeListener { _, isChecked ->
            val db = DatabaseHelper(holder.itemView.context)
            db.updateTaskCompleted(task.id, isChecked)

            val updatedTask = task.copy(completed = isChecked)

            if (isChecked) {
                Handler(Looper.getMainLooper()).postDelayed({
                    val currentPosition = holder.adapterPosition
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        taskList.removeAt(currentPosition)
                        notifyItemRemoved(currentPosition)
                        onCheckedChanged(updatedTask)
                    }
                }, 600)
            } else {
                taskList[position] = updatedTask
                notifyItemChanged(position)
                onCheckedChanged(updatedTask)
            }
        }

        // Item click (Detail dialog)
        holder.itemView.setOnClickListener {
            onItemClick(task)
        }
    }

    override fun getItemCount(): Int = taskList.size

    fun deleteTaskAndRemoveAt(position: Int, dbHelper: DatabaseHelper): Boolean {
        val task = taskList[position]
        val result = dbHelper.deleteTask(task.id)
        return if (result > 0) {
            taskList.removeAt(position)
            notifyItemRemoved(position)

            if (taskList.isEmpty()) {
                onCheckedChanged(task.copy(completed = false))
            }

            true
        } else {
            false
        }
    }
}

