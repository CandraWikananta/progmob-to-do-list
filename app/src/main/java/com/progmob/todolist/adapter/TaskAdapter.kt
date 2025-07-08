package com.progmob.todolist

import android.graphics.Paint
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

// Fungsi untuk memformat tanggal
private fun formatDate(inputDate: String): String {
    return try {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEE, dd MMMM", Locale.getDefault())
        val date = inputFormat.parse(inputDate)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        inputDate // fallback
    }
}

class TaskAdapter(
    private val taskList: MutableList<TaskModel>,
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

        // Hindari pemanggilan listener lama (recycle issue)
        holder.taskCheckbox.setOnCheckedChangeListener(null)

        // Set status checkbox
        holder.taskCheckbox.isChecked = task.completed

        // Fungsi bantu untuk strike-through
        fun updateStrikeThrough(view: TextView, isCompleted: Boolean) {
            view.paintFlags = if (isCompleted) {
                view.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                view.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }

        // Update tampilan strike-through berdasarkan status completed
        updateStrikeThrough(holder.taskTitleText, task.completed)
        updateStrikeThrough(holder.taskCategoryText, task.completed)
        updateStrikeThrough(holder.taskDateText, task.completed)
        updateStrikeThrough(holder.taskPriorityText, task.completed)

        // Listener baru untuk checkbox
        holder.taskCheckbox.setOnCheckedChangeListener { _, isChecked ->
            val db = DatabaseHelper(holder.itemView.context)
            db.updateTaskCompleted(task.id, isChecked)

            // Update model di list supaya tampilan langsung berubah
            taskList[position] = task.copy(completed = isChecked)

            notifyItemChanged(position)
        }


        // Klik item → buka detail modal
        holder.itemView.setOnClickListener {
            val dialog = TaskDetailDialog(task)
            dialog.show((it.context as AppCompatActivity).supportFragmentManager, "TaskDetailDialog")
        }
    }

    override fun getItemCount(): Int = taskList.size
}
